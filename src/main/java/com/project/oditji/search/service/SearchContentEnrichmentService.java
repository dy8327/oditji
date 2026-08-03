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
    private static final String JSON_RELEASE_DATES = "release_dates";

    private static final int CAST_LIMIT = 5;

    @Value("${search.content-cache.worker-count:5}")
    private int workerCount;

    private final TmdbApiClient apiClient;
    private final SearchContentManualOverrideService manualOverrideService;
    private final SearchContentAgeRatingResolver ageRatingResolver;

    public SearchContentEnrichmentService(
            TmdbApiClient apiClient,
            SearchContentManualOverrideService manualOverrideService,
            SearchContentAgeRatingResolver ageRatingResolver) {
        this.apiClient = apiClient;
        this.manualOverrideService = manualOverrideService;
        this.ageRatingResolver = ageRatingResolver;
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

        if (!ageRatingResolver.isNormalizedAgeRating(previous.getAgeRating())) {
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
        if (ageRatingResolver.hasRestrictedSourceAgeRating(
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
                    ageRatingResolver.parseMovieAgeRating(
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
                    ageRatingResolver.parseTvAgeRating(
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
