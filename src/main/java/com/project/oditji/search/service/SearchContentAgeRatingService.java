package com.project.oditji.search.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 검색 캐시의 연령등급 재보강과 수동 등급 보완을 전담합니다.
 *
 * 기존 전체 상세 보강을 다시 실행하지 않고,
 * 영화는 /release_dates, TV는 /content_ratings만 호출하여
 * "등급 정보 없음" 콘텐츠의 요청량을 최소화합니다.
 *
 * 재조회 후에도 등급을 찾지 못한 콘텐츠는 재조회 횟수를 JSONL에 남기고,
 * 수동 확인용 missing-age-rating-candidates.json 파일로 별도 출력합니다.
 */
@Service
public class SearchContentAgeRatingService {

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";

    private static final String AGE_ALL = "전체 관람가";
    private static final String AGE_7 = "7세 이상 관람가";
    private static final String AGE_12 = "12세 이상 관람가";
    private static final String AGE_15 = "15세 이상 관람가";
    private static final String AGE_ADULT = "청소년 관람불가";
    private static final String AGE_UNKNOWN = "등급 정보 없음";

    @Value("${search.content-cache.age-rating-retry-enabled:true}")
    private boolean retryEnabled;

    /**
     * 최초 상세 보강 이후 허용할 추가 등급 재조회 횟수입니다.
     * 기본값 1이면 기존 "등급 정보 없음" 콘텐츠를 한 번만 다시 확인합니다.
     */
    @Value("${search.content-cache.age-rating-retry-max-attempts:2}")
    private int retryMaxAttempts;

    /**
     * 한 번의 캐시 갱신에서 처리할 최대 등급 재조회 건수입니다.
     * 현재 약 1만 건 누락을 한 번에 보강할 수 있도록 기본값을 10000으로 둡니다.
     */
    @Value("${search.content-cache.age-rating-retry-max-per-refresh:10000}")
    private int retryMaxPerRefresh;

    @Value("${search.content-cache.worker-count:5}")
    private int workerCount;

    @Value("${search.content-cache.manual-age-rating-override-enabled:true}")
    private boolean manualOverrideEnabled;

    @Value("${search.content-cache.manual-age-rating-override-path:C:/oditji/cache/manual-age-rating-overrides.json}")
    private String manualOverridePath;

    @Value("${search.content-cache.missing-age-rating-path:C:/oditji/cache/missing-age-rating-candidates.json}")
    private String missingCandidatePath;

    private final TmdbApiClient apiClient;
    private final SearchContentPolicyService contentPolicyService;

    private final AtomicReference<Map<String, String>> manualOverrideMap =
            new AtomicReference<Map<String, String>>(Map.of());

    public SearchContentAgeRatingService(
            TmdbApiClient apiClient,
            SearchContentPolicyService contentPolicyService) {

        this.apiClient = apiClient;
        this.contentPolicyService = contentPolicyService;
    }

    /**
     * 캐시 갱신 시작 시 수동 등급 파일을 다시 읽습니다.
     */
    public void reload() {
        manualOverrideMap.set(loadManualOverrides());
    }

    /**
     * 기존 캐시에 수동 등급값을 먼저 적용합니다.
     * 수동값이 존재하면 TMDB 재조회보다 우선합니다.
     */
    public void applyManualOverrides(
            List<CachedContentVO> contents) {

        if (contents == null) {
            return;
        }

        for (CachedContentVO content : contents) {
            applyManualOverride(content);
        }
    }

    /**
     * 단일 콘텐츠에 수동 등급값을 적용합니다.
     */
    public void applyManualOverride(
            CachedContentVO content) {

        if (content == null
                || contentPolicyService.shouldExcludeContent(content)
                || content.getTmdbId() == null
                || !hasText(content.getContentType())) {
            return;
        }

        String manualAgeRating = manualOverrideMap.get().get(
                createKey(
                        content.getContentType(),
                        content.getTmdbId()
                )
        );

        if (manualAgeRating != null) {
            content.setAgeRating(manualAgeRating);
        }
    }

    /**
     * 이전 스냅샷의 "등급 정보 없음" 콘텐츠만 전용 API로 다시 확인합니다.
     *
     * API 호출 자체가 실패한 경우에는 재조회 횟수를 증가시키지 않아
     * 다음 갱신 때 다시 시도할 수 있게 합니다.
     */
    public void recheckUnknownAgeRatings(
            List<CachedContentVO> contents) {

        if (!retryEnabled
                || contents == null
                || contents.isEmpty()) {
            return;
        }

        int normalizedMaxAttempts =
                Math.max(1, retryMaxAttempts);

        int normalizedMaxPerRefresh =
                Math.max(1, retryMaxPerRefresh);

        List<CachedContentVO> targets =
                new ArrayList<CachedContentVO>();

        for (CachedContentVO content : contents) {

            if (!isRetryTarget(
                    content,
                    normalizedMaxAttempts
            )) {
                continue;
            }

            targets.add(content);
        }

        targets.sort(
                Comparator
                        .comparing(
                                CachedContentVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                CachedContentVO::getContentType,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                CachedContentVO::getTmdbId,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
        );

        if (targets.size() > normalizedMaxPerRefresh) {
            targets = new ArrayList<CachedContentVO>(
                    targets.subList(
                            0,
                            normalizedMaxPerRefresh
                    )
            );
        }

        if (targets.isEmpty()) {
            return;
        }

        int normalizedWorkers =
                Math.max(
                        1,
                        Math.min(
                                workerCount,
                                12
                        )
                );

        try (ExecutorService executorService =
                     Executors.newFixedThreadPool(
                             normalizedWorkers
                     )) {

            List<Future<AgeRatingResult>> futures =
                    new ArrayList<Future<AgeRatingResult>>();

            for (CachedContentVO content : targets) {

                Callable<AgeRatingResult> task =
                        () -> loadAgeRating(content);

                futures.add(
                        executorService.submit(task)
                );
            }

            for (Future<AgeRatingResult> future : futures) {

                try {

                    AgeRatingResult result =
                            future.get();

                    if (result == null
                            || !result.success()) {
                        continue;
                    }

                    CachedContentVO content =
                            result.content();

                    content.setAgeRating(
                            result.ageRating()
                    );

                    content.setAgeRatingRetryCount(
                            getRetryCount(content) + 1
                    );

                    content.setAgeRatingLastCheckedAt(
                            result.checkedAt()
                    );

                    /*
                     * 실행 중 수동 파일이 바뀌지는 않지만,
                     * 수동값 우선 원칙을 명확하게 유지합니다.
                     */
                    applyManualOverride(content);

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();

                    throw new IllegalStateException(
                            "연령등급 재보강 작업이 중단되었습니다.",
                            e
                    );

                } catch (ExecutionException e) {

                    /*
                     * 특정 콘텐츠의 통신 실패는 재조회 횟수로 계산하지 않습니다.
                     * 다음 캐시 갱신에서 다시 시도합니다.
                     */
                }
            }
        }
    }

    /**
     * 신규 콘텐츠의 최초 전체 상세 보강 시각을 기록합니다.
     * 추가 재조회 횟수는 증가시키지 않습니다.
     */
    public void markLookupCompleted(
            CachedContentVO content) {

        if (content == null) {
            return;
        }

        content.setAgeRatingLastCheckedAt(
                LocalDateTime.now().toString()
        );
    }

    /**
     * 추가 재조회까지 끝났지만 여전히 등급이 없는 콘텐츠를
     * 수동 보강 대상 JSON으로 출력합니다.
     */
    public void writeMissingCandidates(
            List<CachedContentVO> contents) {

        if (!hasText(missingCandidatePath)) {
            return;
        }

        int normalizedMaxAttempts =
                Math.max(1, retryMaxAttempts);

        List<CachedContentVO> missing =
                new ArrayList<CachedContentVO>();

        if (contents != null) {

            for (CachedContentVO content : contents) {

                if (content == null
                        || contentPolicyService.shouldExcludeContent(content)
                        || !AGE_UNKNOWN.equals(
                                content.getAgeRating()
                        )
                        || getRetryCount(content)
                                < normalizedMaxAttempts) {
                    continue;
                }

                missing.add(content);
            }
        }

        missing.sort(
                Comparator
                        .comparing(
                                CachedContentVO::getPopularity,
                                Comparator.nullsLast(
                                        Comparator.reverseOrder()
                                )
                        )
                        .thenComparing(
                                CachedContentVO::getContentType,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                        .thenComparing(
                                CachedContentVO::getTmdbId,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
        );

        JSONArray movieArray =
                new JSONArray();

        JSONArray tvArray =
                new JSONArray();

        for (CachedContentVO content : missing) {

            JSONObject item =
                    createMissingCandidateJson(
                            content
                    );

            if (MOVIE.equalsIgnoreCase(
                    content.getContentType()
            )) {
                movieArray.put(item);
            } else if (TV.equalsIgnoreCase(
                    content.getContentType()
            )) {
                tvArray.put(item);
            }
        }

        JSONObject root =
                new JSONObject();

        root.put(
                "generatedAt",
                LocalDateTime.now().toString()
        );

        root.put(
                "total",
                missing.size()
        );

        root.put(
                "movieCount",
                movieArray.length()
        );

        root.put(
                "tvCount",
                tvArray.length()
        );

        root.put(
                MOVIE,
                movieArray
        );

        root.put(
                TV,
                tvArray
        );

        writeJsonFile(
                missingCandidatePath,
                root
        );
    }

    private boolean isRetryTarget(
            CachedContentVO content,
            int normalizedMaxAttempts) {

        if (content == null
                || contentPolicyService.shouldExcludeContent(content)
                || content.getTmdbId() == null
                || !hasText(content.getContentType())
                || !AGE_UNKNOWN.equals(
                        content.getAgeRating()
                )) {
            return false;
        }

        if (manualOverrideMap.get().containsKey(
                createKey(
                        content.getContentType(),
                        content.getTmdbId()
                )
        )) {
            return false;
        }

        return getRetryCount(content)
                < normalizedMaxAttempts;
    }

    private AgeRatingResult loadAgeRating(
            CachedContentVO content) {

        if (content == null
                || content.getTmdbId() == null
                || !hasText(content.getContentType())) {
            return null;
        }

        String contentType =
                content.getContentType()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        String apiUrl;
        String ageRating;

        if (MOVIE.equals(contentType)) {

            apiUrl =
                    apiClient.getBaseUrl()
                            + "/movie/"
                            + content.getTmdbId()
                            + "/release_dates";

            JSONObject response =
                    apiClient.get(apiUrl);

            ageRating =
                    parseMovieAgeRating(response);

        } else if (TV.equals(contentType)) {

            apiUrl =
                    apiClient.getBaseUrl()
                            + "/tv/"
                            + content.getTmdbId()
                            + "/content_ratings";

            JSONObject response =
                    apiClient.get(apiUrl);

            ageRating =
                    parseTvAgeRating(response);

        } else {
            return null;
        }

        return new AgeRatingResult(
                content,
                ageRating,
                LocalDateTime.now().toString(),
                true
        );
    }

    private String parseMovieAgeRating(
            JSONObject response) {

        if (response == null) {
            return AGE_UNKNOWN;
        }

        JSONArray countries =
                response.optJSONArray(
                        "results"
                );

        /*
         * 1순위는 국내(KR) 등급입니다.
         */
        String koreaRating =
                findMovieCertification(
                        countries,
                        "KR"
                );

        if (hasText(koreaRating)) {
            return normalizeKoreanAgeRating(
                    koreaRating
            );
        }

        /*
         * 국내 등급이 없을 때 일본(JP) 영화 등급을 보조 기준으로 사용합니다.
         * 프로젝트 정책:
         * - PG12  -> 15세 이상 관람가
         * - R15+  -> 청소년 관람불가
         */
        String japanRating =
                findMovieCertification(
                        countries,
                        "JP"
                );

        if (hasText(japanRating)) {
            String convertedJapanRating =
                    convertJapanMovieAgeRating(
                            japanRating
                    );

            if (!AGE_UNKNOWN.equals(
                    convertedJapanRating
            )) {
                return convertedJapanRating;
            }
        }

        /*
         * KR/JP에서 사용할 수 있는 등급이 없을 때 기존 US 변환을 사용합니다.
         */
        String usRating =
                findMovieCertification(
                        countries,
                        "US"
                );

        return hasText(usRating)
                ? convertUsMovieAgeRating(
                        usRating
                )
                : AGE_UNKNOWN;
    }

    private String findMovieCertification(
            JSONArray countries,
            String countryCode) {

        if (countries == null) {
            return null;
        }

        for (int index = 0;
             index < countries.length();
             index++) {

            JSONObject country =
                    countries.optJSONObject(index);

            if (country == null
                    || !countryCode.equalsIgnoreCase(
                            country.optString(
                                    "iso_3166_1",
                                    ""
                            )
                    )) {
                continue;
            }

            JSONArray releaseDates =
                    country.optJSONArray(
                            "release_dates"
                    );

            if (releaseDates == null) {
                continue;
            }

            for (int releaseIndex = 0;
                 releaseIndex < releaseDates.length();
                 releaseIndex++) {

                JSONObject release =
                        releaseDates.optJSONObject(
                                releaseIndex
                        );

                if (release == null) {
                    continue;
                }

                String certification =
                        release.optString(
                                "certification",
                                ""
                        ).trim();

                if (!certification.isEmpty()) {
                    return certification;
                }
            }
        }

        return null;
    }

    private String parseTvAgeRating(
            JSONObject response) {

        if (response == null) {
            return AGE_UNKNOWN;
        }

        JSONArray ratings =
                response.optJSONArray(
                        "results"
                );

        String koreaRating =
                findTvRating(
                        ratings,
                        "KR"
                );

        if (hasText(koreaRating)) {
            return normalizeKoreanAgeRating(
                    koreaRating
            );
        }

        String usRating =
                findTvRating(
                        ratings,
                        "US"
                );

        return hasText(usRating)
                ? convertUsTvAgeRating(
                        usRating
                )
                : AGE_UNKNOWN;
    }

    private String findTvRating(
            JSONArray ratings,
            String countryCode) {

        if (ratings == null) {
            return null;
        }

        for (int index = 0;
             index < ratings.length();
             index++) {

            JSONObject rating =
                    ratings.optJSONObject(index);

            if (rating == null
                    || !countryCode.equalsIgnoreCase(
                            rating.optString(
                                    "iso_3166_1",
                                    ""
                            )
                    )) {
                continue;
            }

            String value =
                    rating.optString(
                            "rating",
                            ""
                    ).trim();

            if (!value.isEmpty()) {
                return value;
            }
        }

        return null;
    }

    private String normalizeKoreanAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("_", "");

        if ("ALL".equals(normalized)
                || "전체".equals(normalized)
                || "전체관람가".equals(normalized)
                || "전체이용가".equals(normalized)
                || "0".equals(normalized)
                || "0+".equals(normalized)) {
            return AGE_ALL;
        }

        if ("7".equals(normalized)
                || "7+".equals(normalized)
                || normalized.contains("7세")) {
            return AGE_7;
        }

        if ("12".equals(normalized)
                || "12+".equals(normalized)
                || normalized.contains("12세")) {
            return AGE_12;
        }

        if ("15".equals(normalized)
                || "15+".equals(normalized)
                || normalized.contains("15세")) {
            return AGE_15;
        }

        if ("18".equals(normalized)
                || "18+".equals(normalized)
                || "19".equals(normalized)
                || "19+".equals(normalized)
                || normalized.contains("18세")
                || normalized.contains("19세")
                || normalized.contains("청소년관람불가")
                || normalized.contains("청불")
                || normalized.contains("제한상영가")) {
            return AGE_ADULT;
        }

        return AGE_UNKNOWN;
    }

    /**
     * 일본 영화 등급을 ODITJI 내부 연령등급으로 변환합니다.
     *
     * 현재 프로젝트에서 확정한 수동 변환 정책만 적용합니다.
     * 알 수 없는 일본 등급은 임의로 추정하지 않고 기존 US 보조 조회로 넘깁니다.
     */
    private String convertJapanMovieAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "-")
                        .replace("-", "");

        switch (normalized) {
            case "PG12":
                return AGE_15;

            case "R15+":
            case "R15":
                return AGE_ADULT;

            default:
                return AGE_UNKNOWN;
        }
    }

    private String convertUsMovieAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "-");

        switch (normalized) {
            case "G":
                return AGE_ALL;

            case "PG":
            case "PG-13":
            case "PG13":
                return AGE_12;

            case "R":
                return AGE_15;

            case "NC-17":
            case "NC17":
                return AGE_ADULT;

            default:
                return AGE_UNKNOWN;
        }
    }

    private String convertUsTvAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return AGE_UNKNOWN;
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "-");

        switch (normalized) {
            case "TV-Y":
            case "TVY":
            case "TV-G":
            case "TVG":
                return AGE_ALL;

            case "TV-Y7":
            case "TVY7":
                return AGE_7;

            case "TV-PG":
            case "TVPG":
                return AGE_12;

            case "TV-14":
            case "TV14":
                return AGE_15;

            case "TV-MA":
            case "TVMA":
                return AGE_ADULT;

            default:
                return AGE_UNKNOWN;
        }
    }

    private Map<String, String> loadManualOverrides() {

        if (!manualOverrideEnabled
                || !hasText(manualOverridePath)) {
            return Map.of();
        }

        Path path =
                Path.of(manualOverridePath);

        if (!Files.exists(path)
                || !Files.isRegularFile(path)) {
            return Map.of();
        }

        try {

            JSONObject root =
                    new JSONObject(
                            Files.readString(
                                    path,
                                    StandardCharsets.UTF_8
                            )
                    );

            Map<String, String> result =
                    new LinkedHashMap<String, String>();

            readManualSection(
                    root,
                    MOVIE,
                    result
            );

            readManualSection(
                    root,
                    TV,
                    result
            );

            /*
             * 이전 안내에서 사용한 "MOVIE-12345": "15세 이상 관람가"
             * 형태도 함께 지원합니다.
             */
            for (String key : root.keySet()) {

                if (MOVIE.equals(key)
                        || TV.equals(key)) {
                    continue;
                }

                String normalizedKey =
                        key.trim()
                                .toUpperCase(Locale.ROOT);

                if (!normalizedKey.startsWith("MOVIE-")
                        && !normalizedKey.startsWith("TV-")) {
                    continue;
                }

                int separatorIndex =
                        normalizedKey.indexOf('-');

                if (separatorIndex <= 0
                        || separatorIndex
                                >= normalizedKey.length() - 1) {
                    continue;
                }

                String contentType =
                        normalizedKey.substring(
                                0,
                                separatorIndex
                        );

                String rawId =
                        normalizedKey.substring(
                                separatorIndex + 1
                        );

                try {

                    long tmdbId =
                            Long.parseLong(rawId);

                    String ageRating =
                            normalizeManualAgeRating(
                                    root.optString(
                                            key,
                                            ""
                                    )
                            );

                    if (tmdbId > 0
                            && ageRating != null) {
                        result.put(
                                createKey(
                                        contentType,
                                        tmdbId
                                ),
                                ageRating
                        );
                    }

                } catch (NumberFormatException ignored) {
                    /* 잘못된 수동 ID 항목만 제외합니다. */
                }
            }

            return Map.copyOf(result);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "수동 연령등급 보완 파일을 읽지 못했습니다: "
                            + path.toAbsolutePath(),
                    e
            );

        } catch (JSONException e) {

            throw new IllegalStateException(
                    "수동 연령등급 보완 JSON 형식이 올바르지 않습니다: "
                            + path.toAbsolutePath(),
                    e
            );
        }
    }

    private void readManualSection(
            JSONObject root,
            String contentType,
            Map<String, String> target) {

        JSONObject section =
                root.optJSONObject(contentType);

        if (section == null) {
            return;
        }

        for (String rawTmdbId : section.keySet()) {

            try {

                long tmdbId =
                        Long.parseLong(
                                rawTmdbId.trim()
                        );

                if (tmdbId <= 0) {
                    continue;
                }

                String ageRating =
                        normalizeManualAgeRating(
                                section.optString(
                                        rawTmdbId,
                                        ""
                                )
                        );

                if (ageRating == null) {
                    continue;
                }

                target.put(
                        createKey(
                                contentType,
                                tmdbId
                        ),
                        ageRating
                );

            } catch (NumberFormatException ignored) {
                /* 잘못된 TMDB ID 항목만 제외합니다. */
            }
        }
    }

    private String normalizeManualAgeRating(
            String rawAgeRating) {

        if (!hasText(rawAgeRating)) {
            return null;
        }

        String trimmed =
                rawAgeRating.trim();

        if (AGE_ALL.equals(trimmed)
                || AGE_7.equals(trimmed)
                || AGE_12.equals(trimmed)
                || AGE_15.equals(trimmed)
                || AGE_ADULT.equals(trimmed)) {
            return trimmed;
        }

        String normalized =
                normalizeKoreanAgeRating(
                        trimmed
                );

        return AGE_UNKNOWN.equals(normalized)
                ? null
                : normalized;
    }

    private JSONObject createMissingCandidateJson(
            CachedContentVO content) {

        JSONObject item =
                new JSONObject();

        putNullable(
                item,
                "tmdbId",
                content.getTmdbId()
        );

        putNullable(
                item,
                "title",
                content.getTitle()
        );

        putNullable(
                item,
                "releaseDate",
                content.getReleaseDate()
        );

        putNullable(
                item,
                "popularity",
                content.getPopularity()
        );

        putNullable(
                item,
                "retryCount",
                content.getAgeRatingRetryCount()
        );

        putNullable(
                item,
                "lastCheckedAt",
                content.getAgeRatingLastCheckedAt()
        );

        return item;
    }

    private void writeJsonFile(
            String filePath,
            JSONObject json) {

        Path path =
                Path.of(filePath);

        Path parent =
                path.getParent();

        try {

            if (parent != null) {
                Files.createDirectories(parent);
            }

            Files.writeString(
                    path,
                    json.toString(2),
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "연령등급 수동 보강 대상 파일을 저장하지 못했습니다: "
                            + path.toAbsolutePath(),
                    e
            );
        }
    }

    private int getRetryCount(
            CachedContentVO content) {

        if (content == null
                || content.getAgeRatingRetryCount() == null) {
            return 0;
        }

        return Math.max(
                0,
                content.getAgeRatingRetryCount()
        );
    }

    private String createKey(
            String contentType,
            long tmdbId) {

        return contentType.trim()
                .toUpperCase(Locale.ROOT)
                + ":"
                + tmdbId;
    }

    private void putNullable(
            JSONObject json,
            String key,
            Object value) {

        json.put(
                key,
                value == null
                        ? JSONObject.NULL
                        : value
        );
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    private record AgeRatingResult(
            CachedContentVO content,
            String ageRating,
            String checkedAt,
            boolean success) {
    }
}
