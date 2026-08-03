package com.project.oditji.search.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * 후보 콘텐츠의 상세정보, 배우, 감독, 연령등급, 러닝타임과
 * 한국 지원 OTT 정보를 보강합니다.
 *
 * 한 콘텐츠의 보강 실패는 해당 콘텐츠만 제외하며 터미널에는 출력하지 않습니다.
 */
@Service
public class SearchContentEnrichmentService {

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";

    private static final String JSON_RESULTS = "results";
    private static final String JSON_COUNTRY_CODE = "iso_3166_1";
    private static final String JSON_RELEASE_DATES = "release_dates";

    private static final String AGE_ALL = "전체 관람가";
    private static final String AGE_7 = "7세 이상 관람가";
    private static final String AGE_12 = "12세 이상 관람가";
    private static final String AGE_15 = "15세 이상 관람가";
    private static final String AGE_ADULT = "청소년 관람불가";
    private static final String AGE_UNKNOWN = "등급 정보 없음";
    private static final int CAST_LIMIT = 5;

    @Value("${search.content-cache.worker-count:5}")
    private int workerCount;

    private final TmdbApiClient apiClient;
    private final SearchContentManualOverrideService manualOverrideService;

    public SearchContentEnrichmentService(
            TmdbApiClient apiClient,
            SearchContentManualOverrideService manualOverrideService) {
        this.apiClient = apiClient;
        this.manualOverrideService = manualOverrideService;
    }

    /**
     * 이전 스냅샷을 상세 API 호출 없이 재사용할 수 있는지 확인합니다.
     */
    public boolean hasReusableDetail(CachedContentVO previous) {
        if (previous == null
                || previous.getTmdbId() == null
                || !hasText(previous.getContentType())
                || !hasText(previous.getTitle())
                || !hasText(previous.getGenreText())
                || previous.getPlatformKeys() == null
                || previous.getPlatformKeys().isEmpty()
                || !hasText(previous.getSearchText())) {
            return false;
        }

        if (!isNormalizedAgeRating(previous.getAgeRating())) {
            return false;
        }

        if (TV.equals(previous.getContentType())
                && (previous.getEpisodeCount() == null
                || !hasText(previous.getLastAirDate()))) {
            /*
             * 기존 스냅샷에 최근 회차 공개일이 없으면
             * TV 상세 API를 다시 호출하여 한 번 보강합니다.
             */
            return false;
        }

        /* 수동 등록 영화는 러닝타임이 없으면 한 번 더 상세 보강합니다. */
        return !MOVIE.equals(previous.getContentType())
                || !manualOverrideService.contains(previous)
                || previous.getRuntime() != null;
    }

    /**
     * 재사용 가능한 기존 상세정보를 새 후보에 모두 복사합니다.
     */
    public void copyReusableDetail(
            CachedContentVO source,
            CachedContentVO target) {
        if (source == null || target == null) {
            return;
        }
        target.setTitle(source.getTitle());
        target.setOriginalTitle(source.getOriginalTitle());
        target.setPosterPath(source.getPosterPath());
        target.setReleaseDate(source.getReleaseDate());
        target.setLastAirDate(source.getLastAirDate());
        target.setGenreText(source.getGenreText());
        target.setTmdbScore(source.getTmdbScore());
        target.setPopularity(source.getPopularity());
        target.setRuntime(source.getRuntime());
        target.setEpisodeCount(source.getEpisodeCount());
        target.setDirector(source.getDirector());
        target.setCastNames(source.getCastNames());
        target.setAgeRating(source.getAgeRating());
        target.setAgeRatingRestrictionChecked(
                source.getAgeRatingRestrictionChecked()
        );
        target.setPlatformKeys(source.getPlatformKeys() == null
                ? new ArrayList<String>()
                : new ArrayList<String>(source.getPlatformKeys()));
    }

    public List<CachedContentVO> enrichBatch(
            List<CachedContentVO> batch,
            TmdbProviderRegistry providerRegistry) {

        int normalizedWorkers =
                Math.clamp(
                        workerCount,
                        1,
                        12
                );

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        try (ExecutorService executorService =
                     Executors.newFixedThreadPool(
                             normalizedWorkers
                     )) {

            List<Future<CachedContentVO>> futures =
                    new ArrayList<Future<CachedContentVO>>();

            for (CachedContentVO candidate
                    : batch) {

                Callable<CachedContentVO> task =
                        () -> enrichCandidate(
                                candidate,
                                providerRegistry
                        );

                futures.add(
                        executorService.submit(task)
                );
            }

            for (Future<CachedContentVO> future
                    : futures) {

                try {

                    CachedContentVO content =
                            future.get();

                    if (content != null) {
                        result.add(content);
                    }

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();

                    throw new IllegalStateException(
                            "검색 콘텐츠 상세 보강이 중단되었습니다.",
                            e
                    );

                } catch (ExecutionException e) {

                    /*
                     * 한 콘텐츠 실패로 전체 수집을 중단하지 않습니다.
                     *
                     * 실패 콘텐츠는 다음 갱신 또는 재시작 때 다시 시도합니다.
                     */
                }
            }
        }

        return result;
    }

    private CachedContentVO enrichCandidate(
            CachedContentVO candidate,
            TmdbProviderRegistry providerRegistry) {

        String mediaType =
                MOVIE.equals(
                        candidate.getContentType()
                )
                        ? "movie"
                        : "tv";

        String appendedResponses =
                MOVIE.equals(candidate.getContentType())
                        ? "credits,release_dates"
                        : "credits,content_ratings";

        String detailUrl =
                apiClient.getBaseUrl()
                        + "/"
                        + mediaType
                        + "/"
                        + candidate.getTmdbId()
                        + "?language="
                        + apiClient.encode(apiClient.getLanguage())
                        + "&append_to_response="
                        + appendedResponses;

        JSONObject detail =
                apiClient.get(
                        detailUrl
                );

        if (detail.isEmpty()
                || detail.optLong(
                        "id",
                        0L
                ) <= 0) {

            return null;
        }

        /*
         * TMDB 원본 등급 기준으로 서비스 제외 대상을 먼저 차단합니다.
         *
         * 영화:
         * - JP R18+  -> 제외
         * - US NC-17 -> 제외
         *
         * TV:
         * - US TV-MA-S
         * - US TV-MA-LS
         * - US TV-MA-SV
         * - US TV-MA-LSV
         *
         * TV-MA, TV-MA-L, TV-MA-V는 유지합니다.
         */
        if (hasRestrictedSourceAgeRating(
                candidate.getContentType(),
                detail
        )) {
            return null;
        }

        /*
         * 상세 응답의 원본 등급을 실제로 검사한 콘텐츠임을 기록합니다.
         * 기존 JSONL은 이 값이 없으므로 별도 재검사를 한 번 수행합니다.
         */
        candidate.setAgeRatingRestrictionChecked(
                Boolean.TRUE
        );

        /*
         * 수동 보완 JSON에서 직접 추가한 후보는 Discover 응답을
         * 거치지 않을 수 있으므로 상세 API에서 기본 화면 정보도 채웁니다.
         */
        fillBasicContentDetail(
                candidate,
                detail
        );

        /*
         * Discover 응답에 장르나 평점이 빠진 경우
         * 상세 API 값으로 보완합니다.
         */
        if (!hasText(candidate.getGenreText())) {

            candidate.setGenreText(
                    parseGenreNames(
                            detail.optJSONArray("genres")
                    )
            );
        }

        if (candidate.getTmdbScore() == null) {

            candidate.setTmdbScore(
                    nullableDouble(
                            detail,
                            "vote_average"
                    )
            );
        }

        if (MOVIE.equals(
                candidate.getContentType()
        )) {

            candidate.setEpisodeCount(null);

            candidate.setAgeRating(
                    parseMovieAgeRating(
                            detail.optJSONObject(JSON_RELEASE_DATES)
                    )
            );

            JSONObject credits =
                    detail.optJSONObject(
                            "credits"
                    );

            candidate.setDirector(
                    parseMovieDirector(
                            credits == null
                                    ? null
                                    : credits.optJSONArray(
                                            "crew"
                                    )
                    )
            );

        } else {

            candidate.setAgeRating(
                    parseTvAgeRating(
                            detail.optJSONObject("content_ratings")
                    )
            );

            candidate.setEpisodeCount(
                    nullablePositiveInteger(
                            detail,
                            "number_of_episodes"
                    )
            );

            candidate.setDirector(
                    parseTvCreator(
                            detail.optJSONArray(
                                    "created_by"
                            )
                    )
            );
        }

        JSONObject credits =
                detail.optJSONObject(
                        "credits"
                );

        candidate.setCastNames(
                parseCastNames(
                        credits == null
                                ? null
                                : credits.optJSONArray(
                                        "cast"
                                )
                )
        );

        candidate.setPlatformKeys(
                loadPlatformKeys(
                        candidate.getTmdbId(),
                        candidate.getContentType(),
                        mediaType,
                        providerRegistry
                )
        );

        /*
         * TMDB watch/providers 결과와 수동 보완 JSON을 합칩니다.
         */
        manualOverrideService.apply(
                candidate
        );

        candidate.setSearchText(
                createSearchText(candidate)
        );

        return candidate;
    }

    private void fillBasicContentDetail(
            CachedContentVO candidate,
            JSONObject detail) {

        if (candidate == null
                || detail == null) {
            return;
        }

        if (MOVIE.equals(candidate.getContentType())) {
            fillMovieBasicDetail(
                    candidate,
                    detail
            );
        } else {
            fillTvBasicDetail(
                    candidate,
                    detail
            );
        }

        fillCommonBasicDetail(
                candidate,
                detail
        );
    }

    private void fillMovieBasicDetail(
            CachedContentVO candidate,
            JSONObject detail) {

        if (!hasText(candidate.getTitle())) {
            candidate.setTitle(
                    firstNonBlank(
                            nullableString(detail, "title"),
                            nullableString(detail, "original_title")
                    )
            );
        }

        if (!hasText(candidate.getOriginalTitle())) {
            candidate.setOriginalTitle(
                    nullableString(detail, "original_title")
            );
        }

        if (!hasText(candidate.getReleaseDate())) {
            candidate.setReleaseDate(
                    nullableString(detail, "release_date")
            );
        }

        /*
         * 영화 상세 API의 runtime 값은 분 단위 러닝타임입니다.
         */
        if (candidate.getRuntime() == null) {
            candidate.setRuntime(
                    nullablePositiveInteger(
                            detail,
                            "runtime"
                    )
            );
        }
    }

    private void fillTvBasicDetail(
            CachedContentVO candidate,
            JSONObject detail) {

        if (!hasText(candidate.getTitle())) {
            candidate.setTitle(
                    firstNonBlank(
                            nullableString(detail, "name"),
                            nullableString(detail, "original_name")
                    )
            );
        }

        if (!hasText(candidate.getOriginalTitle())) {
            candidate.setOriginalTitle(
                    nullableString(detail, "original_name")
            );
        }

        if (!hasText(candidate.getReleaseDate())) {
            candidate.setReleaseDate(
                    nullableString(detail, "first_air_date")
            );
        }

        /*
         * TV 상세 응답의 last_episode_to_air.air_date를 가장 우선 사용합니다.
         * 최근 회차 객체가 없거나 날짜가 비어 있으면 last_air_date로 대체합니다.
         */
        candidate.setLastAirDate(
                resolveTvLastAirDate(detail)
        );

        /*
         * TV 상세 API는 대표 회차 러닝타임을 배열로 반환하므로
         * 첫 번째 유효한 양수 값을 사용합니다.
         */
        if (candidate.getRuntime() == null) {
            candidate.setRuntime(
                    parseFirstPositiveInteger(
                            detail.optJSONArray(
                                    "episode_run_time"
                            )
                    )
            );
        }
    }

    private void fillCommonBasicDetail(
            CachedContentVO candidate,
            JSONObject detail) {

        if (!hasText(candidate.getPosterPath())) {
            candidate.setPosterPath(
                    nullableString(detail, "poster_path")
            );
        }

        if (candidate.getPopularity() == null) {
            candidate.setPopularity(
                    nullableDouble(detail, "popularity")
            );
        }
    }

    private List<String> loadPlatformKeys(
            Long tmdbId,
            String contentType,
            String mediaType,
            TmdbProviderRegistry providerRegistry) {

        JSONObject root =
                apiClient.get(
                        apiClient.getBaseUrl()
                                + "/"
                                + mediaType
                                + "/"
                                + tmdbId
                                + "/watch/providers"
                );

        JSONObject results =
                root.optJSONObject(
                        JSON_RESULTS
                );

        if (results == null) {
            return new ArrayList<String>();
        }

        JSONObject korea =
                results.optJSONObject(
                        apiClient.getRegion()
                );

        if (korea == null) {
            return new ArrayList<String>();
        }

        Map<Integer, String> supportedProviderMap =
                providerRegistry.getProviderMap(
                        contentType
                );

        Set<String> keys =
                new LinkedHashSet<String>();

        /*
         * 구매 및 대여 콘텐츠는 제외하고
         * 구독형 제공처만 저장합니다.
         */
        addPlatformKeysByProviderId(
                korea.optJSONArray(
                        "flatrate"
                ),
                supportedProviderMap,
                keys
        );

        return new ArrayList<String>(keys);
    }

    private void addPlatformKeysByProviderId(
            JSONArray providers,
            Map<Integer, String> supportedProviderMap,
            Set<String> keys) {

        if (providers == null) {
            return;
        }

        for (int index = 0;
             index < providers.length();
             index++) {

            JSONObject provider =
                    providers.optJSONObject(index);

            if (provider == null) {
                continue;
            }

            String platformKey =
                    supportedProviderMap.get(
                            provider.optInt(
                                    "provider_id",
                                    0
                            )
                    );

            if (platformKey != null
                    && !platformKey.isBlank()) {

                keys.add(platformKey);
            }
        }
    }

    private String parseGenreNames(
            JSONArray genres) {

        if (genres == null) {
            return null;
        }

        List<String> names =
                new ArrayList<String>();

        for (int index = 0;
             index < genres.length();
             index++) {

            JSONObject genre =
                    genres.optJSONObject(index);

            if (genre == null) {
                continue;
            }

            String name =
                    genre.optString(
                            "name",
                            ""
                    ).trim();

            if (!name.isEmpty()
                    && !names.contains(name)) {

                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    /**
     * 상세 응답에 서비스 제외 대상 원본 등급이 포함되어 있는지 확인합니다.
     *
     * KR 등급이 먼저 존재하더라도 JP R18+ 또는 US NC-17이 확인되면
     * 콘텐츠 자체를 공용 저장소에서 제외합니다.
     */
    private boolean hasRestrictedSourceAgeRating(
            String contentType,
            JSONObject detail) {

        if (detail == null
                || !hasText(contentType)) {
            return false;
        }

        if (MOVIE.equalsIgnoreCase(contentType)) {

            JSONObject releaseDatesRoot =
                    detail.optJSONObject(
                            JSON_RELEASE_DATES
                    );

            JSONArray countries =
                    releaseDatesRoot == null
                            ? null
                            : releaseDatesRoot.optJSONArray(
                                    JSON_RESULTS
                            );

            return containsMovieCertification(
                    countries,
                    "JP",
                    "R18+"
            )
                    || containsMovieCertification(
                            countries,
                            "US",
                            "NC17"
                    );
        }

        if (TV.equalsIgnoreCase(contentType)) {

            JSONObject contentRatingsRoot =
                    detail.optJSONObject(
                            "content_ratings"
                    );

            JSONArray ratings =
                    contentRatingsRoot == null
                            ? null
                            : contentRatingsRoot.optJSONArray(
                                    JSON_RESULTS
                            );

            String usRating =
                    findTvRating(
                            ratings,
                            "US"
                    );

            return isRestrictedUsTvRating(
                    usRating
            );
        }

        return false;
    }

    /**
     * 특정 국가의 영화 release_dates 중 제한 등급이 하나라도 있는지 확인합니다.
     */
    private boolean containsMovieCertification(
            JSONArray countries,
            String countryCode,
            String restrictedCode) {

        if (countries == null
                || !hasText(countryCode)
                || !hasText(restrictedCode)) {
            return false;
        }

        JSONArray releaseDates =
                findMovieReleaseDates(
                        countries,
                        countryCode
                );

        return containsCertification(
                releaseDates,
                normalizeRatingCode(
                        restrictedCode
                )
        );
    }

    private JSONArray findMovieReleaseDates(
            JSONArray countries,
            String countryCode) {

        for (int index = 0;
             index < countries.length();
             index++) {

            JSONObject country =
                    countries.optJSONObject(index);

            if (country != null
                    && countryCode.equalsIgnoreCase(
                            country.optString(
                                    JSON_COUNTRY_CODE,
                                    ""
                            )
                    )) {

                return country.optJSONArray(
                        JSON_RELEASE_DATES
                );
            }
        }

        return null;
    }

    private boolean containsCertification(
            JSONArray releaseDates,
            String normalizedRestrictedCode) {

        if (releaseDates == null) {
            return false;
        }

        for (int releaseIndex = 0;
             releaseIndex < releaseDates.length();
             releaseIndex++) {

            JSONObject release =
                    releaseDates.optJSONObject(
                            releaseIndex
                    );

            if (release != null
                    && normalizedRestrictedCode.equals(
                            normalizeRatingCode(
                                    release.optString(
                                            "certification",
                                            ""
                                    )
                            )
                    )) {
                return true;
            }
        }

        return false;
    }

    /**
     * 미국 TV 등급 중 성적 상황(S) 설명자가 명시된 TV-MA 조합만 제외합니다.
     *
     * 제외:
     * TV-MA-S, TV-MA-LS, TV-MA-SV, TV-MA-LSV
     *
     * 유지:
     * TV-MA, TV-MA-L, TV-MA-V
     */
    private boolean isRestrictedUsTvRating(
            String rawRating) {

        String normalized =
                normalizeRatingCode(
                        rawRating
                );

        return "TVMAS".equals(normalized)
                || "TVMALS".equals(normalized)
                || "TVMASV".equals(normalized)
                || "TVMALSV".equals(normalized);
    }

    /**
     * 등급 코드 비교용 문자열을 생성합니다.
     * 공백, 하이픈, 밑줄은 제거하고 + 기호는 유지합니다.
     */
    private String normalizeRatingCode(
            String rawRating) {

        if (!hasText(rawRating)) {
            return "";
        }

        return rawRating.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");
    }

    private String parseMovieAgeRating(
            JSONObject releaseDatesRoot) {

        if (releaseDatesRoot == null) {
            return AGE_UNKNOWN;
        }

        JSONArray countries =
                releaseDatesRoot.optJSONArray(
                        JSON_RESULTS
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
                                    JSON_COUNTRY_CODE,
                                    ""
                            )
                    )) {

                continue;
            }

            JSONArray releaseDates =
                    country.optJSONArray(
                            JSON_RELEASE_DATES
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
            JSONObject contentRatingsRoot) {

        if (contentRatingsRoot == null) {
            return AGE_UNKNOWN;
        }

        JSONArray ratings =
                contentRatingsRoot.optJSONArray(
                        JSON_RESULTS
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
                                    JSON_COUNTRY_CODE,
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

        /*
         * 알 수 없는 한국 등급 원문은 그대로 노출하지 않습니다.
         */
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

            case "NR":
            case "NOTRATED":
            case "UNRATED":
                return AGE_UNKNOWN;

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
            case "TV-MA-S":
            case "TV-MA-LS":
            case "TV-MA-SV":
            case "TV-MA-LSV":
                return AGE_ADULT;

            case "NR":
            case "NOTRATED":
            case "UNRATED":
                return AGE_UNKNOWN;

            default:
                return AGE_UNKNOWN;
        }
    }

    public boolean isNormalizedAgeRating(
            String ageRating) {

        if (!hasText(ageRating)) {
            return false;
        }

        return AGE_ALL.equals(ageRating)
                || AGE_7.equals(ageRating)
                || AGE_12.equals(ageRating)
                || AGE_15.equals(ageRating)
                || AGE_ADULT.equals(ageRating)
                || AGE_UNKNOWN.equals(ageRating);
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    private String parseMovieDirector(
            JSONArray crew) {

        if (crew == null) {
            return null;
        }

        List<String> names =
                new ArrayList<String>();

        for (int index = 0;
             index < crew.length();
             index++) {

            JSONObject person =
                    crew.optJSONObject(index);

            if (person == null
                    || !"Director".equalsIgnoreCase(
                            person.optString(
                                    "job",
                                    ""
                            )
                    )) {

                continue;
            }

            String name =
                    person.optString(
                            "name",
                            ""
                    ).trim();

            if (!name.isEmpty()
                    && !names.contains(name)) {

                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private String parseTvCreator(
            JSONArray creators) {

        if (creators == null) {
            return null;
        }

        List<String> names =
                new ArrayList<String>();

        for (int index = 0;
             index < creators.length();
             index++) {

            JSONObject creator =
                    creators.optJSONObject(index);

            if (creator == null) {
                continue;
            }

            String name =
                    creator.optString(
                            "name",
                            ""
                    ).trim();

            if (!name.isEmpty()
                    && !names.contains(name)) {

                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private String parseCastNames(
            JSONArray cast) {

        if (cast == null) {
            return null;
        }

        List<String> names =
                new ArrayList<String>();

        for (int index = 0;
             index < cast.length()
                     && names.size() < CAST_LIMIT;
             index++) {

            JSONObject actor =
                    cast.optJSONObject(index);

            if (actor == null) {
                continue;
            }

            String name =
                    actor.optString(
                            "name",
                            ""
                    ).trim();

            if (!name.isEmpty()
                    && !names.contains(name)) {

                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    public String createSearchText(
            CachedContentVO content) {

        return normalizeSearchText(
                safeText(
                        content.getTitle()
                )
                        + " "
                        + safeText(
                                content.getOriginalTitle()
                        )
                        + " "
                        + safeText(
                                content.getDirector()
                        )
                        + " "
                        + safeText(
                                content.getCastNames()
                        )
        );
    }

    private String normalizeSearchText(
            String value) {

        if (value == null) {
            return "";
        }

        return Normalizer.normalize(
                        value,
                        Normalizer.Form.NFKC
                )
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^\\p{L}\\p{N}]",
                        ""
                );
    }

    private String safeText(
            String value) {

        return value == null
                ? ""
                : value;
    }

    private String firstNonBlank(
            String first,
            String second) {

        return first != null
                && !first.isBlank()
                ? first
                : second;
    }

    private String nullableString(
            JSONObject json,
            String key) {

        if (!json.has(key)
                || json.isNull(key)) {

            return null;
        }

        String value =
                json.optString(
                        key,
                        null
                );

        return value == null
                || value.isBlank()
                ? null
                : value;
    }

    /**
     * TV 상세정보에서 최근 회차 공개일을 추출합니다.
     *
     * @param detail TMDB TV 상세 응답
     * @return 최근 회차 공개일 또는 null
     */
    private String resolveTvLastAirDate(
            JSONObject detail) {

        if (detail == null) {
            return null;
        }

        JSONObject lastEpisode =
                detail.optJSONObject(
                        "last_episode_to_air"
                );

        String episodeAirDate =
                lastEpisode == null
                        ? null
                        : nullableString(
                                lastEpisode,
                                "air_date"
                        );

        return firstNonBlank(
                episodeAirDate,
                nullableString(
                        detail,
                        "last_air_date"
                )
        );
    }

    private Integer nullablePositiveInteger(
            JSONObject json,
            String key) {

        if (!json.has(key)
                || json.isNull(key)) {

            return null;
        }

        int value =
                json.optInt(
                        key,
                        0
                );

        return value <= 0
                ? null
                : value;
    }

    private Integer parseFirstPositiveInteger(
            JSONArray values) {

        if (values == null) {
            return null;
        }

        for (int index = 0;
             index < values.length();
             index++) {

            int value =
                    values.optInt(
                            index,
                            0
                    );

            if (value > 0) {
                return value;
            }
        }

        return null;
    }

    private Double nullableDouble(
            JSONObject json,
            String key) {

        if (!json.has(key)
                || json.isNull(key)) {

            return null;
        }

        return json.optDouble(key);
    }
}
