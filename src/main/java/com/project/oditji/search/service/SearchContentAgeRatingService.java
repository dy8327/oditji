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
    private final SearchContentAgeRatingResolver ageRatingResolver;

    private final AtomicReference<Map<String, String>> manualOverrideMap =
            new AtomicReference<Map<String, String>>(Map.of());

    public SearchContentAgeRatingService(
            TmdbApiClient apiClient,
            SearchContentPolicyService contentPolicyService,
            SearchContentAgeRatingResolver ageRatingResolver) {

        this.apiClient = apiClient;
        this.contentPolicyService = contentPolicyService;
        this.ageRatingResolver = ageRatingResolver;
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

        List<CachedContentVO> targets =
                collectRetryTargets(
                        contents,
                        Math.max(1, retryMaxAttempts)
                );

        sortAgeRatingTargets(targets);

        targets = limitAgeRatingTargets(
                targets,
                Math.max(1, retryMaxPerRefresh)
        );

        if (targets.isEmpty()) {
            return;
        }

        try (ExecutorService executorService =
                     Executors.newFixedThreadPool(
                             normalizeWorkerCount()
                     )) {

            List<Future<AgeRatingResult>> futures =
                    submitAgeRatingTasks(
                            executorService,
                            targets
                    );

            List<CachedContentVO> blockedContents =
                    new ArrayList<CachedContentVO>();

            for (Future<AgeRatingResult> future : futures) {
                applyUnknownAgeRatingResult(
                        getAgeRatingResult(
                                future,
                                "연령등급 재보강 작업이 중단되었습니다."
                        ),
                        blockedContents
                );
            }

            removeBlockedContents(
                    contents,
                    blockedContents
            );
        }
    }

    private List<CachedContentVO> collectRetryTargets(
            List<CachedContentVO> contents,
            int normalizedMaxAttempts) {

        List<CachedContentVO> targets =
                new ArrayList<CachedContentVO>();

        for (CachedContentVO content : contents) {
            if (isRetryTarget(
                    content,
                    normalizedMaxAttempts
            )) {
                targets.add(content);
            }
        }

        return targets;
    }

    private void applyUnknownAgeRatingResult(
            AgeRatingResult result,
            List<CachedContentVO> blockedContents) {

        if (result == null
                || !result.success()) {
            return;
        }

        CachedContentVO content = result.content();
        applyAgeRatingCheckMetadata(content, result);

        if (result.restricted()) {
            blockedContents.add(content);
            return;
        }

        content.setAgeRating(
                result.ageRating()
        );

        content.setAgeRatingRetryCount(
                getRetryCount(content) + 1
        );

        /*
         * 실행 중 수동 파일이 바뀌지는 않지만,
         * 수동값 우선 원칙을 명확하게 유지합니다.
         */
        applyManualOverride(content);
    }

    /**
     * 기존 JSONL에 이미 저장되어 있던 청소년 관람불가/등급 미확인 콘텐츠 중
     * 현재 원본 등급 제외 정책을 아직 확인하지 않은 항목을 한 번 검사합니다.
     *
     * 기존 캐시는 R18+, NC-17, TV-MA-S 계열의 원본 값을 저장하지 않았기 때문에
     * 정책 변경 후 한 번은 TMDB 등급 전용 API를 다시 확인해야 합니다.
     *
     * 성공적으로 확인한 콘텐츠에는 ageRatingRestrictionChecked=true를 남겨
     * 이후 48시간 갱신 때 같은 검사를 반복하지 않습니다.
     */
    public void recheckRestrictedAgeRatings(
            List<CachedContentVO> contents) {

        if (contents == null
                || contents.isEmpty()) {
            return;
        }

        List<CachedContentVO> targets =
                collectRestrictionRecheckTargets(contents);

        sortAgeRatingTargets(targets);

        targets = limitAgeRatingTargets(
                targets,
                Math.max(1, retryMaxPerRefresh)
        );

        if (targets.isEmpty()) {
            return;
        }

        try (ExecutorService executorService =
                     Executors.newFixedThreadPool(
                             normalizeWorkerCount()
                     )) {

            List<Future<AgeRatingResult>> futures =
                    submitAgeRatingTasks(
                            executorService,
                            targets
                    );

            List<CachedContentVO> blockedContents =
                    new ArrayList<CachedContentVO>();

            for (Future<AgeRatingResult> future : futures) {
                applyRestrictedAgeRatingResult(
                        getAgeRatingResult(
                                future,
                                "연령등급 제외 정책 재검사가 중단되었습니다."
                        ),
                        blockedContents
                );
            }

            removeBlockedContents(
                    contents,
                    blockedContents
            );
        }
    }

    private List<CachedContentVO> collectRestrictionRecheckTargets(
            List<CachedContentVO> contents) {

        List<CachedContentVO> targets =
                new ArrayList<CachedContentVO>();

        for (CachedContentVO content : contents) {
            if (isRestrictionRecheckTarget(content)) {
                targets.add(content);
            }
        }

        return targets;
    }

    private void sortAgeRatingTargets(
            List<CachedContentVO> targets) {

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
    }

    private List<CachedContentVO> limitAgeRatingTargets(
            List<CachedContentVO> targets,
            int normalizedMaxPerRefresh) {

        if (targets.size() <= normalizedMaxPerRefresh) {
            return targets;
        }

        return new ArrayList<CachedContentVO>(
                targets.subList(
                        0,
                        normalizedMaxPerRefresh
                )
        );
    }

    private int normalizeWorkerCount() {
        return Math.clamp(
                workerCount,
                1,
                12
        );
    }

    private List<Future<AgeRatingResult>> submitAgeRatingTasks(
            ExecutorService executorService,
            List<CachedContentVO> targets) {

        List<Future<AgeRatingResult>> futures =
                new ArrayList<Future<AgeRatingResult>>();

        for (CachedContentVO content : targets) {
            futures.add(
                    executorService.submit(
                            () -> loadAgeRating(content)
                    )
            );
        }

        return futures;
    }

    private AgeRatingResult getAgeRatingResult(
            Future<AgeRatingResult> future,
            String interruptedMessage) {

        try {
            return future.get();

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    interruptedMessage,
                    e
            );

        } catch (ExecutionException e) {

            /*
             * 통신 실패 콘텐츠는 다음 갱신 때 다시 확인합니다.
             */
            return null;
        }
    }

    private void applyRestrictedAgeRatingResult(
            AgeRatingResult result,
            List<CachedContentVO> blockedContents) {

        if (result == null
                || !result.success()) {
            return;
        }

        CachedContentVO content = result.content();
        applyAgeRatingCheckMetadata(content, result);

        if (result.restricted()) {
            blockedContents.add(content);
            return;
        }

        /*
         * 기존 값이 "등급 정보 없음"이었는데 이번 원본 등급 재검사에서
         * 정상 등급을 찾은 경우에는 그 결과도 함께 반영합니다.
         */
        if (AGE_UNKNOWN.equals(
                content.getAgeRating()
        )
                && !AGE_UNKNOWN.equals(
                        result.ageRating()
                )) {

            content.setAgeRating(
                    result.ageRating()
            );
        }

        applyManualOverride(content);
    }

    private void applyAgeRatingCheckMetadata(
            CachedContentVO content,
            AgeRatingResult result) {

        content.setAgeRatingLastCheckedAt(
                result.checkedAt()
        );

        content.setAgeRatingRestrictionChecked(
                Boolean.TRUE
        );
    }

    private void removeBlockedContents(
            List<CachedContentVO> contents,
            List<CachedContentVO> blockedContents) {

        if (!blockedContents.isEmpty()) {
            contents.removeAll(blockedContents);
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

    private boolean isRestrictionRecheckTarget(
            CachedContentVO content) {

        if (content == null
                || contentPolicyService.shouldExcludeContent(content)
                || content.getTmdbId() == null
                || !hasText(content.getContentType())
                || Boolean.TRUE.equals(
                        content.getAgeRatingRestrictionChecked()
                )) {
            return false;
        }

        return AGE_ADULT.equals(
                content.getAgeRating()
        )
                || AGE_UNKNOWN.equals(
                        content.getAgeRating()
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
        boolean restricted;

        if (MOVIE.equals(contentType)) {

            apiUrl =
                    apiClient.getBaseUrl()
                            + "/movie/"
                            + content.getTmdbId()
                            + "/release_dates";

            JSONObject response =
                    apiClient.get(apiUrl);

            restricted =
                    ageRatingResolver.hasRestrictedMovieRating(
                            response
                    );

            ageRating =
                    ageRatingResolver.parseMovieAgeRating(response);

        } else if (TV.equals(contentType)) {

            apiUrl =
                    apiClient.getBaseUrl()
                            + "/tv/"
                            + content.getTmdbId()
                            + "/content_ratings";

            JSONObject response =
                    apiClient.get(apiUrl);

            restricted =
                    ageRatingResolver.hasRestrictedTvRating(
                            response
                    );

            ageRating =
                    ageRatingResolver.parseTvAgeRating(response);

        } else {
            return null;
        }

        return new AgeRatingResult(
                content,
                ageRating,
                LocalDateTime.now().toString(),
                true,
                restricted
        );
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

            return parseManualOverrides(root);

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

    private Map<String, String> parseManualOverrides(
            JSONObject root) {

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

        readFlatManualOverrides(
                root,
                result
        );

        return Map.copyOf(result);
    }

    private void readFlatManualOverrides(
            JSONObject root,
            Map<String, String> result) {

        /*
         * 이전 안내에서 사용한 "MOVIE-12345": "15세 이상 관람가"
         * 형태도 함께 지원합니다.
         */
        for (String key : root.keySet()) {
            if (!isManualSectionKey(key)) {
                readFlatManualOverride(
                        root,
                        key,
                        result
                );
            }
        }
    }

    private boolean isManualSectionKey(
            String key) {

        return MOVIE.equals(key)
                || TV.equals(key);
    }

    private void readFlatManualOverride(
            JSONObject root,
            String key,
            Map<String, String> result) {

        String normalizedKey =
                key.trim()
                        .toUpperCase(Locale.ROOT);

        if (!normalizedKey.startsWith("MOVIE-")
                && !normalizedKey.startsWith("TV-")) {
            return;
        }

        int separatorIndex =
                normalizedKey.indexOf('-');

        if (separatorIndex <= 0
                || separatorIndex
                        >= normalizedKey.length() - 1) {
            return;
        }

        putFlatManualOverride(
                root,
                key,
                normalizedKey,
                separatorIndex,
                result
        );
    }

    private void putFlatManualOverride(
            JSONObject root,
            String key,
            String normalizedKey,
            int separatorIndex,
            Map<String, String> result) {

        try {

            long tmdbId =
                    Long.parseLong(
                            normalizedKey.substring(
                                    separatorIndex + 1
                            )
                    );

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
                                normalizedKey.substring(
                                        0,
                                        separatorIndex
                                ),
                                tmdbId
                        ),
                        ageRating
                );
            }

        } catch (NumberFormatException ignored) {
            /* 잘못된 수동 ID 항목만 제외합니다. */
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
            addManualAgeRating(
                    section,
                    rawTmdbId,
                    contentType,
                    target
            );
        }
    }


    private void addManualAgeRating(
            JSONObject section,
            String rawTmdbId,
            String contentType,
            Map<String, String> target) {

        try {
            long tmdbId =
                    Long.parseLong(
                            rawTmdbId.trim()
                    );

            String ageRating =
                    normalizeManualAgeRating(
                            section.optString(
                                    rawTmdbId,
                                    ""
                            )
                    );

            if (tmdbId > 0
                    && ageRating != null) {

                target.put(
                        createKey(
                                contentType,
                                tmdbId
                        ),
                        ageRating
                );
            }

        } catch (NumberFormatException ignored) {
            /* 잘못된 TMDB ID 항목만 제외합니다. */
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
                ageRatingResolver.normalizeKoreanAgeRating(
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
            boolean success,
            boolean restricted) {
    }
}
