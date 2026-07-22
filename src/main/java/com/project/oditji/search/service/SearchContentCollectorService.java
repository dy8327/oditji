package com.project.oditji.search.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.Duration;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * TMDB에서 검색용 콘텐츠를 수집하는 서비스입니다.
 *
 * 주요 기능:
 * 1. TMDB 제공처 목록에서 한국 OTT의 실제 provider ID를 자동 탐색합니다.
 * 2. 전체 후보를 배치 단위로 나누어 상세정보를 보강합니다.
 * 3. 배치가 완료될 때마다 중간 저장 콜백을 호출합니다.
 * 4. 이전 스냅샷의 완성된 콘텐츠는 API를 다시 호출하지 않고 재사용합니다.
 * 5. 전체 수집이 완료되기 전까지 이전 정상 데이터를 유지합니다.
 */
@Service
public class SearchContentCollectorService {

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";

    /**
     * TMDB Discover API가 한 조건에서 제공하는 최대 페이지입니다.
     */
    private static final int TMDB_MAX_PAGE = 500;

    /**
     * 검색 문자열에 저장할 주요 배우 최대 인원입니다.
     */
    private static final int CAST_LIMIT = 5;

    /**
     * ODITJI 내부에서 사용하는 지원 OTT 키입니다.
     *
     * TMDB provider ID와는 별개의 값입니다.
     */
    private static final List<String> SUPPORTED_PLATFORM_KEYS =
            List.of(
                    "netflix",
                    "tving",
                    "wavve",
                    "disney",
                    "watcha",
                    "coupang"
            );

    private static final Map<Integer, String> MOVIE_GENRES =
            createMovieGenreMap();

    private static final Map<Integer, String> TV_GENRES =
            createTvGenreMap();

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String baseUrl;

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.language:ko-KR}")
    private String language;

    @Value("${tmdb.api.region:KR}")
    private String region;

    @Value("${search.content-cache.max-size:50000}")
    private int maxSize;

    @Value("${search.content-cache.movie-ratio:0.5}")
    private double movieRatio;

    @Value("${search.content-cache.start-year:1950}")
    private int startYear;

    @Value("${search.content-cache.worker-count:5}")
    private int workerCount;

    @Value("${search.content-cache.request-delay-ms:80}")
    private long requestDelayMillis;

    /**
     * 한 번에 상세 보강할 콘텐츠 수입니다.
     *
     * 전체 5만 개 Future를 한꺼번에 만들지 않고,
     * 이 배치 크기만큼 처리한 뒤 중간 저장합니다.
     */
    @Value("${search.content-cache.batch-size:500}")
    private int batchSize;

    /**
     * 최근 연도 콘텐츠 보완 수집 사용 여부입니다.
     *
     * 기존 OTT provider 필터 Discover 결과를 우선 사용한 뒤,
     * 최근 콘텐츠 중 provider 인덱스 누락 가능성이 있는 후보만
     * 제한적으로 추가 확인합니다.
     */
    @Value("${search.content-cache.supplement-enabled:true}")
    private boolean supplementEnabled;

    /**
     * 보완 수집 대상 연도 수입니다.
     *
     * 기본값 5는 현재 연도를 포함해 최근 5개 연도를 의미합니다.
     */
    @Value("${search.content-cache.supplement-years:5}")
    private int supplementYears;

    /**
     * 보완 수집에서 연도별로 확인할 최대 Discover 페이지 수입니다.
     *
     * TMDB 전체 500페이지를 다시 순회하지 않고,
     * 인기순 상위 일부 페이지만 확인해 API 요청량을 제한합니다.
     */
    @Value("${search.content-cache.supplement-pages-per-year:10}")
    private int supplementPagesPerYear;

    /**
     * 영화와 TV 각각에서 추가할 수 있는 보완 후보의 최대 개수입니다.
     *
     * 상세 조회와 watch/providers 호출이 과도하게 늘어나는 것을 막습니다.
     */
    @Value("${search.content-cache.supplement-max-candidates-per-type:1000}")
    private int supplementMaxCandidatesPerType;

    /**
     * TMDB 제공처 정보가 누락된 콘텐츠에 대해
     * 외부 JSON 파일의 OTT 정보를 추가할지 결정합니다.
     */
    @Value("${search.content-cache.manual-platform-override-enabled:false}")
    private boolean manualPlatformOverrideEnabled;

    /**
     * 수동 OTT 보완 JSON 파일 경로입니다.
     */
    @Value("${search.content-cache.manual-platform-override-path:}")
    private String manualPlatformOverridePath;

    /**
     * 키: CONTENT_TYPE:TMDB_ID
     * 값: 해당 콘텐츠에 추가할 플랫폼 키 집합
     */
    private volatile Map<String, Set<String>> manualPlatformOverrideMap =
            Map.of();

    private final HttpClient httpClient;

    public SearchContentCollectorService() {

        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                Duration.ofSeconds(15)
                        )
                        .build();
    }

    /**
     * 중간 저장 콜백 없이 최종 결과만 반환하는 호환용 메서드입니다.
     *
     * @param previousSnapshot 이전 검색 콘텐츠 스냅샷
     * @return 최종 수집 결과
     */
    public List<CachedContentVO> collect(
            List<CachedContentVO> previousSnapshot) {

        return collect(
                previousSnapshot,
                ignored -> {
                    /*
                     * 기존 호출부와의 호환을 위한 빈 콜백입니다.
                     */
                }
        );
    }

    /**
     * 검색 콘텐츠 전체 수집을 수행합니다.
     *
     * 배치가 완료될 때마다 checkpointConsumer로 현재까지 확보된
     * 정상 콘텐츠 목록을 전달합니다.
     *
     * @param previousSnapshot 이전 스냅샷
     * @param checkpointConsumer 중간 저장 콜백
     * @return 이번 Discover 후보 기준 최종 콘텐츠
     */
    public List<CachedContentVO> collect(
            List<CachedContentVO> previousSnapshot,
            Consumer<List<CachedContentVO>> checkpointConsumer) {

        int normalizedMaxSize =
                Math.max(
                        100,
                        maxSize
                );

        int normalizedBatchSize =
                Math.max(
                        50,
                        Math.min(
                                batchSize,
                                2000
                        )
                );

        int movieTarget =
                Math.max(
                        0,
                        Math.min(
                                normalizedMaxSize,
                                (int) Math.round(
                                        normalizedMaxSize
                                                * movieRatio
                                )
                        )
                );

        int tvTarget =
                normalizedMaxSize
                        - movieTarget;

        /*
         * 수집을 시작할 때 수동 OTT 보완 파일을 다시 읽습니다.
         * 다음 서버 재시작 또는 정기 갱신에서 JSON 수정 내용이 반영됩니다.
         */
        manualPlatformOverrideMap =
                loadManualPlatformOverrides();

        /*
         * 영화와 TV의 제공처 목록을 각각 조회하여
         * 현재 TMDB에서 사용하는 실제 provider ID를 구성합니다.
         */
        ProviderRegistry providerRegistry =
                loadProviderRegistry();

        if (!providerRegistry.hasAnyProvider()) {

            throw new IllegalStateException(
                    "TMDB 제공처 목록에서 ODITJI 지원 OTT를 찾지 못했습니다."
            );
        }

        System.out.println(
                "검색 콘텐츠 수집 시작: 목표 "
                        + normalizedMaxSize
                        + "건, 배치 "
                        + normalizedBatchSize
                        + "건"
        );

        /*
         * 이전 스냅샷을 콘텐츠 키 기준 Map으로 변환합니다.
         *
         * 이미 상세정보가 완성된 콘텐츠는 API를 다시 호출하지 않고
         * 이전 데이터를 재사용합니다.
         */
        Map<String, CachedContentVO> previousMap =
                createContentMap(
                        previousSnapshot
                );

        List<CachedContentVO> candidates =
                new ArrayList<CachedContentVO>();

        candidates.addAll(
                collectDiscoverCandidates(
                        "movie",
                        MOVIE,
                        movieTarget,
                        providerRegistry.getMovieProviderIds()
                )
        );

        candidates.addAll(
                collectDiscoverCandidates(
                        "tv",
                        TV,
                        tvTarget,
                        providerRegistry.getTvProviderIds()
                )
        );

        /*
         * 안전한 보완 수집:
         *
         * TMDB Discover의 with_watch_providers 인덱스에서 빠진
         * 최근 콘텐츠를 보완하기 위해 OTT 필터 없는 Discover를
         * 최근 연도와 소수 페이지만 제한적으로 확인합니다.
         *
         * 이 단계에서 바로 저장하지 않고, 기존 enrichCandidate()의
         * /watch/providers KR flatrate 검증을 반드시 통과한 콘텐츠만
         * 최종 JSON에 들어갑니다.
         */
        if (supplementEnabled) {

            List<CachedContentVO> supplementCandidates =
                    collectRecentSupplementCandidates();

            candidates.addAll(
                    supplementCandidates
            );

            System.out.println(
                    "검색 콘텐츠 최근 보완 후보 수집 완료: "
                            + supplementCandidates.size()
                            + "건"
            );
        }

        /*
         * 수동 JSON에 등록된 TMDB ID는 Discover 결과에 없어도
         * 직접 후보로 추가합니다.
         */
        candidates.addAll(
                createManualOverrideCandidates()
        );

        candidates =
                removeDuplicate(candidates);

        candidates.sort(
                Comparator.comparing(
                        CachedContentVO::getPopularity,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        if (candidates.size()
                > normalizedMaxSize) {

            candidates =
                    new ArrayList<CachedContentVO>(
                            candidates.subList(
                                    0,
                                    normalizedMaxSize
                            )
                    );
        }

        /*
         * 체크포인트용 Map에는 이전 정상 콘텐츠를 먼저 넣습니다.
         *
         * 따라서 새 수집 도중 서버가 종료되더라도 기존 검색 데이터가
         * 갑자기 일부 배치 데이터만 남는 상태로 축소되지 않습니다.
         */
        Map<String, CachedContentVO> checkpointMap =
                new LinkedHashMap<String, CachedContentVO>(
                        previousMap
                );

        /*
         * 최종 Map은 이번 Discover 후보에 속한 콘텐츠만 보관합니다.
         *
         * 전체 갱신이 성공하면 더 이상 후보에 없는 오래된 콘텐츠는
         * 최종 스냅샷에서 제거됩니다.
         */
        Map<String, CachedContentVO> finalMap =
                new LinkedHashMap<String, CachedContentVO>();

        int reusedCount = 0;

        List<CachedContentVO> pendingCandidates =
                new ArrayList<CachedContentVO>();

        for (CachedContentVO candidate
                : candidates) {

            CachedContentVO previous =
                    previousMap.get(
                            candidate.createContentKey()
                    );

            if (hasReusableDetail(previous)) {

                copyReusableDetail(
                        previous,
                        candidate
                );

                /*
                 * 이전 스냅샷을 재사용하는 콘텐츠에도
                 * 최신 수동 OTT 보완값을 적용합니다.
                 */
                applyManualPlatformOverrides(
                        candidate
                );

                candidate.setSearchText(
                        createSearchText(candidate)
                );

                checkpointMap.put(
                        candidate.createContentKey(),
                        candidate
                );

                finalMap.put(
                        candidate.createContentKey(),
                        candidate
                );

                reusedCount++;

            } else {

                pendingCandidates.add(candidate);
            }
        }

        System.out.println(
                "검색 콘텐츠 이어받기: 기존 "
                        + reusedCount
                        + "건 재사용, 신규 "
                        + pendingCandidates.size()
                        + "건 보강"
        );

        /*
         * 재사용 가능한 데이터가 있으면 신규 API 호출 전에
         * 먼저 현재 상태를 체크포인트로 저장합니다.
         */
        if (!finalMap.isEmpty()) {

            publishCheckpoint(
                    checkpointMap,
                    normalizedMaxSize,
                    checkpointConsumer
            );
        }

        int processedCount = 0;

        /*
         * 전체 후보를 배치 단위로 나누어 처리합니다.
         */
        for (int startIndex = 0;
             startIndex < pendingCandidates.size();
             startIndex += normalizedBatchSize) {

            int endIndex =
                    Math.min(
                            startIndex
                                    + normalizedBatchSize,
                            pendingCandidates.size()
                    );

            List<CachedContentVO> batch =
                    new ArrayList<CachedContentVO>(
                            pendingCandidates.subList(
                                    startIndex,
                                    endIndex
                            )
                    );

            List<CachedContentVO> enrichedBatch =
                    enrichBatch(
                            batch,
                            providerRegistry
                    );

            for (CachedContentVO content
                    : enrichedBatch) {

                /*
                 * 한국 구독형 지원 OTT가 확인된 콘텐츠만 저장합니다.
                 */
                if (content == null
                        || content.getPlatformKeys() == null
                        || content.getPlatformKeys().isEmpty()) {

                    continue;
                }

                String key =
                        content.createContentKey();

                checkpointMap.put(
                        key,
                        content
                );

                finalMap.put(
                        key,
                        content
                );
            }

            processedCount += batch.size();

            /*
             * 완성된 배치가 끝날 때마다 중간 저장합니다.
             */
            publishCheckpoint(
                    checkpointMap,
                    normalizedMaxSize,
                    checkpointConsumer
            );

            System.out.println(
                    "검색 콘텐츠 중간 저장 완료: "
                            + processedCount
                            + "/"
                            + pendingCandidates.size()
                            + "건 처리"
            );
        }

        List<CachedContentVO> finalResult =
                sortAndLimit(
                        new ArrayList<CachedContentVO>(
                                finalMap.values()
                        ),
                        normalizedMaxSize
                );

        System.out.println(
                "검색 콘텐츠 수집 완료: "
                        + finalResult.size()
                        + "건"
        );

        return finalResult;
    }

    /**
     * 현재 체크포인트 목록을 정렬하고 최대 건수로 제한하여 전달합니다.
     */
    private void publishCheckpoint(
            Map<String, CachedContentVO> checkpointMap,
            int limit,
            Consumer<List<CachedContentVO>> checkpointConsumer) {

        List<CachedContentVO> checkpoint =
                sortAndLimit(
                        new ArrayList<CachedContentVO>(
                                checkpointMap.values()
                        ),
                        limit
                );

        checkpointConsumer.accept(checkpoint);
    }

    /**
     * 콘텐츠 목록을 인기도 순으로 정렬하고 최대 건수로 제한합니다.
     */
    private List<CachedContentVO> sortAndLimit(
            List<CachedContentVO> source,
            int limit) {

        source.removeIf(
                content -> content == null
                        || content.getTmdbId() == null
                        || content.getContentType() == null
                        || content.getPlatformKeys() == null
                        || content.getPlatformKeys().isEmpty()
        );

        source.sort(
                Comparator.comparing(
                        CachedContentVO::getPopularity,
                        Comparator.nullsLast(
                                Comparator.reverseOrder()
                        )
                )
        );

        if (source.size() <= limit) {
            return source;
        }

        return new ArrayList<CachedContentVO>(
                source.subList(
                        0,
                        limit
                )
        );
    }

    /**
     * 영화와 TV 제공처 목록에서 실제 provider ID를 동적으로 조회합니다.
     */
    private ProviderRegistry loadProviderRegistry() {

        Map<Integer, String> movieProviderMap =
                loadSupportedProviderMap(
                        "movie",
                        MOVIE
                );

        Map<Integer, String> tvProviderMap =
                loadSupportedProviderMap(
                        "tv",
                        TV
                );

        /*
         * 제공처별 상세 JSON은 출력하지 않고
         * 정상 매핑된 제공처 개수만 운영 로그로 남깁니다.
         */
        System.out.println(
                "검색 OTT 제공처 자동 매핑 완료: 영화 "
                        + movieProviderMap.size()
                        + "개, TV "
                        + tvProviderMap.size()
                        + "개"
        );

        return new ProviderRegistry(
                movieProviderMap,
                tvProviderMap
        );
    }

    /**
     * 지정한 유형의 제공처 목록에서 지원 OTT만 추출합니다.
     */
    private Map<Integer, String> loadSupportedProviderMap(
            String apiType,
            String contentType) {

        String url =
                baseUrl
                        + "/watch/providers/"
                        + apiType
                        + "?language="
                        + encode(language)
                        + "&watch_region="
                        + encode(region);

        JSONObject root =
                callTmdbApi(url);

        JSONArray providers =
                root.optJSONArray(
                        "results"
                );

        Map<Integer, String> result =
                new LinkedHashMap<Integer, String>();

        if (providers == null) {
            return result;
        }

        for (int index = 0;
             index < providers.length();
             index++) {

            JSONObject provider =
                    providers.optJSONObject(index);

            if (provider == null) {
                continue;
            }

            int providerId =
                    provider.optInt(
                            "provider_id",
                            0
                    );

            String platformKey =
                    normalizePlatformName(
                            provider.optString(
                                    "provider_name",
                                    ""
                            )
                    );

            if (providerId > 0
                    && !platformKey.isEmpty()) {

                result.put(
                        providerId,
                        platformKey
                );
            }
        }

        /*
         * 일부 OTT가 TMDB 목록에서 누락된 경우에만
         * 간단한 경고 로그를 출력합니다.
         */
        Set<String> foundKeys =
                new LinkedHashSet<String>(
                        result.values()
                );

        for (String requiredKey
                : SUPPORTED_PLATFORM_KEYS) {

            if (!foundKeys.contains(
                    requiredKey
            )) {

                System.err.println(
                        "검색 OTT 제공처 미발견: type="
                                + contentType
                                + ", platform="
                                + requiredKey
                );
            }
        }

        return result;
    }

    /**
     * Discover API에서 영화 또는 TV 후보를 연도별로 수집합니다.
     */
    private List<CachedContentVO> collectDiscoverCandidates(
            String apiType,
            String contentType,
            int targetCount,
            Set<Integer> providerIds) {

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        if (targetCount <= 0
                || providerIds == null
                || providerIds.isEmpty()) {

            return result;
        }

        String providerFilter =
                joinProviderIds(
                        providerIds
                );

        int currentYear =
                Year.now().getValue() + 1;

        for (int year = currentYear;
             year >= startYear
                     && result.size() < targetCount;
             year--) {

            int page = 1;
            int totalPages = 1;

            while (page <= totalPages
                    && page <= TMDB_MAX_PAGE
                    && result.size() < targetCount) {

                JSONObject root =
                        callTmdbApi(
                                buildDiscoverUrl(
                                        apiType,
                                        year,
                                        page,
                                        providerFilter
                                )
                        );

                totalPages =
                        Math.min(
                                root.optInt(
                                        "total_pages",
                                        0
                                ),
                                TMDB_MAX_PAGE
                        );

                JSONArray items =
                        root.optJSONArray(
                                "results"
                        );

                if (items == null
                        || items.isEmpty()) {

                    break;
                }

                for (int index = 0;
                     index < items.length()
                             && result.size()
                                     < targetCount;
                     index++) {

                    JSONObject item =
                            items.optJSONObject(index);

                    if (item == null
                            || shouldExcludeContent(item)) {

                        continue;
                    }

                    CachedContentVO content =
                            convertDiscoverItem(
                                    item,
                                    contentType
                            );

                    if (content.getTmdbId() != null) {
                        result.add(content);
                    }
                }

                page++;
            }
        }

        return result;
    }


    /**
     * 최근 콘텐츠 보완 후보를 제한적으로 수집합니다.
     *
     * 기존 provider 필터 Discover 결과가 누락시키는 콘텐츠를 찾기 위한
     * 보완 단계이며, 다음 안전장치를 적용합니다.
     *
     * 1. 최근 연도만 확인
     * 2. 연도별 페이지 수 제한
     * 3. 영화/TV별 후보 수 제한
     * 4. 최종 저장 전 watch/providers KR flatrate 재검증
     */
    private List<CachedContentVO> collectRecentSupplementCandidates() {

        int normalizedYears =
                Math.max(
                        1,
                        Math.min(
                                supplementYears,
                                10
                        )
                );

        int normalizedPagesPerYear =
                Math.max(
                        1,
                        Math.min(
                                supplementPagesPerYear,
                                30
                        )
                );

        int normalizedMaxCandidatesPerType =
                Math.max(
                        0,
                        Math.min(
                                supplementMaxCandidatesPerType,
                                5000
                        )
                );

        if (normalizedMaxCandidatesPerType == 0) {
            return new ArrayList<CachedContentVO>();
        }

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        result.addAll(
                collectRecentSupplementCandidatesByType(
                        "movie",
                        MOVIE,
                        normalizedYears,
                        normalizedPagesPerYear,
                        normalizedMaxCandidatesPerType
                )
        );

        result.addAll(
                collectRecentSupplementCandidatesByType(
                        "tv",
                        TV,
                        normalizedYears,
                        normalizedPagesPerYear,
                        normalizedMaxCandidatesPerType
                )
        );

        return removeDuplicate(result);
    }

    /**
     * 특정 콘텐츠 유형의 최근 보완 후보를 수집합니다.
     *
     * OTT provider 필터는 사용하지 않지만, 인기순 상위 일부 페이지로
     * 범위를 제한하여 TMDB 전체 콘텐츠를 무제한 상세 조회하지 않습니다.
     */
    private List<CachedContentVO>
            collectRecentSupplementCandidatesByType(
                    String apiType,
                    String contentType,
                    int yearCount,
                    int pagesPerYear,
                    int maxCandidates) {

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        int currentYear =
                Year.now().getValue() + 1;

        int lastYear =
                Math.max(
                        startYear,
                        currentYear - yearCount + 1
                );

        for (int year = currentYear;
             year >= lastYear
                     && result.size() < maxCandidates;
             year--) {

            for (int page = 1;
                 page <= pagesPerYear
                         && result.size() < maxCandidates;
                 page++) {

                JSONObject root =
                        callTmdbApi(
                                buildSupplementDiscoverUrl(
                                        apiType,
                                        year,
                                        page
                                )
                        );

                int totalPages =
                        Math.min(
                                root.optInt(
                                        "total_pages",
                                        0
                                ),
                                pagesPerYear
                        );

                if (totalPages <= 0
                        || page > totalPages) {

                    break;
                }

                JSONArray items =
                        root.optJSONArray(
                                "results"
                        );

                if (items == null
                        || items.isEmpty()) {

                    break;
                }

                for (int index = 0;
                     index < items.length()
                             && result.size() < maxCandidates;
                     index++) {

                    JSONObject item =
                            items.optJSONObject(index);

                    if (item == null
                            || shouldExcludeContent(item)) {

                        continue;
                    }

                    CachedContentVO content =
                            convertDiscoverItem(
                                    item,
                                    contentType
                            );

                    if (content.getTmdbId() != null) {
                        result.add(content);
                    }
                }
            }
        }

        return removeDuplicate(result);
    }

    /**
     * 최근 보완 수집용 Discover URL을 생성합니다.
     *
     * provider 필터는 사용하지 않지만 인기순, 성인 제외, 연도 조건을
     * 적용하여 후보 범위를 안전하게 제한합니다.
     */
    private String buildSupplementDiscoverUrl(
            String apiType,
            int year,
            int page) {

        StringBuilder url =
                new StringBuilder(baseUrl)
                        .append("/discover/")
                        .append(apiType)
                        .append("?language=")
                        .append(encode(language))
                        .append("&include_adult=false")
                        .append("&sort_by=popularity.desc")
                        .append("&page=")
                        .append(page);

        if ("movie".equals(apiType)) {

            url.append("&region=")
                    .append(encode(region))
                    .append("&primary_release_year=")
                    .append(year)
                    .append("&include_video=false");

        } else {

            url.append("&first_air_date_year=")
                    .append(year);
        }

        return url.toString();
    }

    /**
     * Discover API 주소를 생성합니다.
     */
    private String buildDiscoverUrl(
            String apiType,
            int year,
            int page,
            String providerFilter) {

        StringBuilder url =
                new StringBuilder(baseUrl)
                        .append("/discover/")
                        .append(apiType)
                        .append("?language=")
                        .append(encode(language))
                        .append("&watch_region=")
                        .append(encode(region))
                        .append("&with_watch_monetization_types=flatrate")
                        .append("&with_watch_providers=")
                        .append(
                                encode(
                                        providerFilter
                                )
                        )
                        .append("&include_adult=false")
                        .append("&sort_by=popularity.desc")
                        .append("&page=")
                        .append(page);

        if ("movie".equals(apiType)) {

            url.append("&region=")
                    .append(encode(region))
                    .append("&primary_release_year=")
                    .append(year)
                    .append("&include_video=false");

        } else {

            url.append("&first_air_date_year=")
                    .append(year);
        }

        return url.toString();
    }

    /**
     * provider ID 집합을 TMDB OR 조건으로 연결합니다.
     */
    private String joinProviderIds(
            Set<Integer> providerIds) {

        StringJoiner joiner =
                new StringJoiner("|");

        for (Integer providerId
                : providerIds) {

            if (providerId != null
                    && providerId > 0) {

                joiner.add(
                        String.valueOf(
                                providerId
                        )
                );
            }
        }

        return joiner.toString();
    }

    /**
     * 한 배치의 상세정보를 병렬로 보강합니다.
     *
     * 전체 후보가 아닌 현재 배치만 Future로 생성하여
     * 메모리와 대기 작업 수를 제한합니다.
     */
    private List<CachedContentVO> enrichBatch(
            List<CachedContentVO> batch,
            ProviderRegistry providerRegistry) {

        int normalizedWorkers =
                Math.max(
                        1,
                        Math.min(
                                workerCount,
                                12
                        )
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

                    Throwable cause =
                            e.getCause() == null
                                    ? e
                                    : e.getCause();

                    /*
                     * 한 콘텐츠 실패로 전체 수집을 중단하지 않습니다.
                     *
                     * 실패 콘텐츠는 다음 갱신 또는 재시작 때 다시 시도합니다.
                     */
                    System.err.println(
                            "검색 콘텐츠 상세 보강 실패: "
                                    + cause.getClass()
                                            .getSimpleName()
                                    + " - "
                                    + cause.getMessage()
                    );
                }
            }
        }

        return result;
    }

    /**
     * 콘텐츠 한 건의 상세정보와 한국 구독형 OTT를 조회합니다.
     */
    private CachedContentVO enrichCandidate(
            CachedContentVO candidate,
            ProviderRegistry providerRegistry) {

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
                baseUrl
                        + "/"
                        + mediaType
                        + "/"
                        + candidate.getTmdbId()
                        + "?language="
                        + encode(language)
                        + "&append_to_response="
                        + appendedResponses;

        JSONObject detail =
                callTmdbApi(
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
                            detail.optJSONObject("release_dates")
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
        applyManualPlatformOverrides(
                candidate
        );

        candidate.setSearchText(
                createSearchText(candidate)
        );

        return candidate;
    }


    /**
     * 상세 API 응답에서 제목, 원제, 공개일, 포스터, 인기도 등
     * 기본 콘텐츠 정보를 보완합니다.
     */
    private void fillBasicContentDetail(
            CachedContentVO candidate,
            JSONObject detail) {

        if (candidate == null
                || detail == null) {
            return;
        }

        if (MOVIE.equals(candidate.getContentType())) {

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

        } else {

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
        }

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

    /**
     * 수동 OTT 보완 JSON 파일을 읽어 CONTENT_TYPE:TMDB_ID 형식의
     * Map으로 변환합니다.
     */
    private Map<String, Set<String>> loadManualPlatformOverrides() {

        if (!manualPlatformOverrideEnabled) {
            return Map.of();
        }

        if (!hasText(manualPlatformOverridePath)) {
            System.err.println(
                    "검색 콘텐츠 수동 OTT 보완 경로가 비어 있습니다."
            );
            return Map.of();
        }

        Path path =
                Paths.get(manualPlatformOverridePath);

        if (!Files.exists(path)
                || !Files.isRegularFile(path)) {

            System.err.println(
                    "검색 콘텐츠 수동 OTT 보완 파일을 찾지 못했습니다: "
                            + path.toAbsolutePath()
            );
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

            Map<String, Set<String>> result =
                    new LinkedHashMap<String, Set<String>>();

            readManualOverrideSection(
                    root,
                    MOVIE,
                    result
            );

            readManualOverrideSection(
                    root,
                    TV,
                    result
            );

            Map<String, Set<String>> immutable =
                    new LinkedHashMap<String, Set<String>>();

            for (Map.Entry<String, Set<String>> entry
                    : result.entrySet()) {

                immutable.put(
                        entry.getKey(),
                        Set.copyOf(entry.getValue())
                );
            }

            System.out.println(
                    "검색 콘텐츠 수동 OTT 보완 로드 완료: "
                            + immutable.size()
                            + "개 콘텐츠"
            );

            return Map.copyOf(immutable);

        } catch (IOException e) {

            throw new IllegalStateException(
                    "수동 OTT 보완 파일을 읽지 못했습니다: "
                            + path.toAbsolutePath(),
                    e
            );

        } catch (JSONException e) {

            throw new IllegalStateException(
                    "수동 OTT 보완 JSON 형식이 올바르지 않습니다: "
                            + path.toAbsolutePath(),
                    e
            );
        }
    }

    /**
     * MOVIE 또는 TV 영역에서 플랫폼별 TMDB ID 배열을 읽습니다.
     */
    private void readManualOverrideSection(
            JSONObject root,
            String contentType,
            Map<String, Set<String>> target) {

        JSONObject section =
                root.optJSONObject(contentType);

        if (section == null) {
            return;
        }

        for (String rawPlatformKey
                : section.keySet()) {

            String platformKey =
                    normalizePlatformName(rawPlatformKey);

            if (!SUPPORTED_PLATFORM_KEYS.contains(platformKey)) {

                System.err.println(
                        "검색 콘텐츠 수동 OTT 보완에서 지원하지 않는 플랫폼을 무시합니다: "
                                + rawPlatformKey
                );
                continue;
            }

            JSONArray tmdbIds =
                    section.optJSONArray(rawPlatformKey);

            if (tmdbIds == null) {
                continue;
            }

            for (int index = 0;
                 index < tmdbIds.length();
                 index++) {

                long tmdbId =
                        tmdbIds.optLong(index, 0L);

                if (tmdbId <= 0) {
                    continue;
                }

                String key =
                        createManualOverrideKey(
                                contentType,
                                tmdbId
                        );

                target.computeIfAbsent(
                        key,
                        ignored -> new LinkedHashSet<String>()
                ).add(platformKey);
            }
        }
    }

    /**
     * 수동 보완 JSON의 모든 ID를 Discover 결과와 별개로 후보에 추가합니다.
     */
    private List<CachedContentVO> createManualOverrideCandidates() {

        List<CachedContentVO> result =
                new ArrayList<CachedContentVO>();

        for (String key
                : manualPlatformOverrideMap.keySet()) {

            String[] parts =
                    key.split(":", 2);

            if (parts.length != 2) {
                continue;
            }

            try {

                long tmdbId =
                        Long.parseLong(parts[1]);

                CachedContentVO candidate =
                        new CachedContentVO();

                candidate.setContentType(parts[0]);
                candidate.setTmdbId(tmdbId);

                result.add(candidate);

            } catch (NumberFormatException e) {
                /* 잘못된 수동 ID는 무시합니다. */
            }
        }

        return result;
    }

    /**
     * 기존 플랫폼 목록에 수동 보완 플랫폼을 중복 없이 합칩니다.
     */
    private void applyManualPlatformOverrides(
            CachedContentVO content) {

        if (content == null
                || content.getTmdbId() == null
                || !hasText(content.getContentType())) {
            return;
        }

        Set<String> manualPlatforms =
                manualPlatformOverrideMap.get(
                        createManualOverrideKey(
                                content.getContentType(),
                                content.getTmdbId()
                        )
                );

        if (manualPlatforms == null
                || manualPlatforms.isEmpty()) {
            return;
        }

        LinkedHashSet<String> merged =
                new LinkedHashSet<String>();

        if (content.getPlatformKeys() != null) {
            merged.addAll(content.getPlatformKeys());
        }

        merged.addAll(manualPlatforms);

        content.setPlatformKeys(
                new ArrayList<String>(merged)
        );
    }

    /**
     * 수동 OTT 보완 Map의 공통 키를 생성합니다.
     */
    private String createManualOverrideKey(
            String contentType,
            long tmdbId) {

        return contentType
                + ":"
                + tmdbId;
    }

    /**
     * 이전 스냅샷의 상세정보를 재사용할 수 있는지 확인합니다.
     */
    private boolean hasReusableDetail(
            CachedContentVO previous) {

        if (previous == null
                || previous.getPlatformKeys() == null
                || previous.getPlatformKeys().isEmpty()
                || previous.getSearchText() == null
                || previous.getSearchText().isBlank()) {

            return false;
        }

        /*
         * 이전 JSON에 미국 등급 원문이나 숫자형 한국 등급이 남아 있으면
         * 한국식 표시 문구로 다시 변환하기 위해 재사용하지 않습니다.
         */
        if (!isNormalizedAgeRating(
                previous.getAgeRating()
        )) {

            return false;
        }

        return !TV.equals(
                previous.getContentType()
        )
                || previous.getEpisodeCount() != null;
    }

    /**
     * 이전 스냅샷의 완성된 상세정보를 새 후보에 복사합니다.
     */
    private void copyReusableDetail(
            CachedContentVO source,
            CachedContentVO target) {

        target.setEpisodeCount(
                source.getEpisodeCount()
        );

        target.setDirector(
                source.getDirector()
        );

        target.setCastNames(
                source.getCastNames()
        );

        target.setAgeRating(
                source.getAgeRating()
        );

        target.setPlatformKeys(
                new ArrayList<String>(
                        source.getPlatformKeys()
                )
        );
    }

    /**
     * 콘텐츠별 Watch Providers API에서 한국 구독형 OTT를 조회합니다.
     */
    private List<String> loadPlatformKeys(
            Long tmdbId,
            String contentType,
            String mediaType,
            ProviderRegistry providerRegistry) {

        JSONObject root =
                callTmdbApi(
                        baseUrl
                                + "/"
                                + mediaType
                                + "/"
                                + tmdbId
                                + "/watch/providers"
                );

        JSONObject results =
                root.optJSONObject(
                        "results"
                );

        if (results == null) {
            return new ArrayList<String>();
        }

        JSONObject korea =
                results.optJSONObject(
                        region
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

    /**
     * 동적으로 확인한 provider ID만 내부 플랫폼 키로 변환합니다.
     */
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

    /**
     * Discover 응답을 CachedContentVO로 변환합니다.
     */
    private CachedContentVO convertDiscoverItem(
            JSONObject item,
            String contentType) {

        CachedContentVO content =
                new CachedContentVO();

        content.setTmdbId(
                nullableLong(
                        item,
                        "id"
                )
        );

        content.setContentType(
                contentType
        );

        if (MOVIE.equals(contentType)) {

            content.setTitle(
                    firstNonBlank(
                            nullableString(
                                    item,
                                    "title"
                            ),
                            nullableString(
                                    item,
                                    "original_title"
                            )
                    )
            );

            content.setOriginalTitle(
                    nullableString(
                            item,
                            "original_title"
                    )
            );

            content.setReleaseDate(
                    nullableString(
                            item,
                            "release_date"
                    )
            );

        } else {

            content.setTitle(
                    firstNonBlank(
                            nullableString(
                                    item,
                                    "name"
                            ),
                            nullableString(
                                    item,
                                    "original_name"
                            )
                    )
            );

            content.setOriginalTitle(
                    nullableString(
                            item,
                            "original_name"
                    )
            );

            content.setReleaseDate(
                    nullableString(
                            item,
                            "first_air_date"
                    )
            );
        }

        content.setPosterPath(
                nullableString(
                        item,
                        "poster_path"
                )
        );

        content.setGenreText(
                convertGenreIdsToText(
                        item.optJSONArray(
                                "genre_ids"
                        ),
                        contentType
                )
        );

        content.setTmdbScore(
                nullableDouble(
                        item,
                        "vote_average"
                )
        );

        content.setPopularity(
                nullableDouble(
                        item,
                        "popularity"
                )
        );

        return content;
    }

    /**
     * 상세 API genres 배열을 장르 문자열로 변환합니다.
     */
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
     * 영화 release_dates 응답에서 한국 연령등급을 우선 추출합니다.
     *
     * 한국 등급이 없으면 미국 등급을 사용하고,
     * 둘 다 없으면 화면과 DB에서 사용할 기본 문구를 저장합니다.
     */
    private String parseMovieAgeRating(
            JSONObject releaseDatesRoot) {

        if (releaseDatesRoot == null) {
            return "등급 정보 없음";
        }

        JSONArray countries =
                releaseDatesRoot.optJSONArray(
                        "results"
                );

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

        String usRating =
                findMovieCertification(
                        countries,
                        "US"
                );

        return hasText(usRating)
                ? convertUsMovieAgeRating(
                        usRating
                )
                : "등급 정보 없음";
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

    /**
     * TV content_ratings 응답에서 한국 연령등급을 우선 추출합니다.
     */
    private String parseTvAgeRating(
            JSONObject contentRatingsRoot) {

        if (contentRatingsRoot == null) {
            return "등급 정보 없음";
        }

        JSONArray ratings =
                contentRatingsRoot.optJSONArray(
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
                : "등급 정보 없음";
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

    /**
     * 한국 연령등급 원문을 홈페이지 표시 문구로 통일합니다.
     */
    private String normalizeKoreanAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return "등급 정보 없음";
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

            return "전체 관람가";
        }

        if ("7".equals(normalized)
                || "7+".equals(normalized)
                || normalized.contains("7세")) {

            return "7세 이상 관람가";
        }

        if ("12".equals(normalized)
                || "12+".equals(normalized)
                || normalized.contains("12세")) {

            return "12세 이상 관람가";
        }

        if ("15".equals(normalized)
                || "15+".equals(normalized)
                || normalized.contains("15세")) {

            return "15세 이상 관람가";
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

            return "청소년 관람불가";
        }

        /*
         * 알 수 없는 한국 등급 원문은 그대로 노출하지 않습니다.
         */
        return "등급 정보 없음";
    }

    /**
     * 미국 영화 등급을 홈페이지용 한국식 문구로 변환합니다.
     *
     * 프로젝트 정책에 따라 R 등급은
     * 15세 이상 관람가로 표시합니다.
     */
    private String convertUsMovieAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return "등급 정보 없음";
        }

        String normalized =
                rawRating.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "-");

        switch (normalized) {
            case "G":
                return "전체 관람가";

            case "PG":
            case "PG-13":
            case "PG13":
                return "12세 이상 관람가";

            case "R":
                return "15세 이상 관람가";

            case "NC-17":
            case "NC17":
                return "청소년 관람불가";

            case "NR":
            case "NOTRATED":
            case "UNRATED":
                return "등급 정보 없음";

            default:
                return "등급 정보 없음";
        }
    }

    /**
     * 미국 TV 등급을 홈페이지용 한국식 문구로 변환합니다.
     */
    private String convertUsTvAgeRating(
            String rawRating) {

        if (!hasText(rawRating)) {
            return "등급 정보 없음";
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
                return "전체 관람가";

            case "TV-Y7":
            case "TVY7":
                return "7세 이상 관람가";

            case "TV-PG":
            case "TVPG":
                return "12세 이상 관람가";

            case "TV-14":
            case "TV14":
                return "15세 이상 관람가";

            case "TV-MA":
            case "TVMA":
                return "청소년 관람불가";

            case "NR":
            case "NOTRATED":
            case "UNRATED":
                return "등급 정보 없음";

            default:
                return "등급 정보 없음";
        }
    }

    /**
     * JSON에 저장된 등급이 이미 홈페이지 표시 형식인지 확인합니다.
     *
     * 미국 등급 원문이나 숫자형 등급이 남아 있으면 false를 반환하여
     * 다음 수집에서 해당 콘텐츠만 다시 상세 보강합니다.
     */
    private boolean isNormalizedAgeRating(
            String ageRating) {

        if (!hasText(ageRating)) {
            return false;
        }

        return "전체 관람가".equals(ageRating)
                || "7세 이상 관람가".equals(ageRating)
                || "12세 이상 관람가".equals(ageRating)
                || "15세 이상 관람가".equals(ageRating)
                || "청소년 관람불가".equals(ageRating)
                || "등급 정보 없음".equals(ageRating);
    }

    private boolean hasText(
            String value) {

        return value != null
                && !value.trim().isEmpty();
    }

    /**
     * 영화 크레딧에서 감독 이름을 추출합니다.
     */
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

    /**
     * TV 상세정보에서 제작자 이름을 추출합니다.
     */
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

    /**
     * 주요 배우를 최대 5명까지 추출합니다.
     */
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

    /**
     * 제목, 원제, 감독, 배우를 검색 문자열로 합칩니다.
     */
    private String createSearchText(
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

    /**
     * 이전 스냅샷을 콘텐츠 키 기준 Map으로 변환합니다.
     */
    private Map<String, CachedContentVO> createContentMap(
            List<CachedContentVO> source) {

        Map<String, CachedContentVO> result =
                new LinkedHashMap<String, CachedContentVO>();

        if (source == null) {
            return result;
        }

        for (CachedContentVO content
                : source) {

            if (content == null
                    || content.getTmdbId() == null
                    || content.getContentType() == null) {

                continue;
            }

            result.put(
                    content.createContentKey(),
                    content
            );
        }

        return result;
    }

    /**
     * 콘텐츠 유형과 TMDB ID를 기준으로 중복을 제거합니다.
     */
    private List<CachedContentVO> removeDuplicate(
            List<CachedContentVO> source) {

        return new ArrayList<CachedContentVO>(
                createContentMap(source)
                        .values()
        );
    }

    /**
     * 장르 ID를 한글 장르명으로 변환합니다.
     */
    private String convertGenreIdsToText(
            JSONArray genreIds,
            String contentType) {

        if (genreIds == null) {
            return null;
        }

        Map<Integer, String> genreMap =
                MOVIE.equals(contentType)
                        ? MOVIE_GENRES
                        : TV_GENRES;

        List<String> names =
                new ArrayList<String>();

        for (int index = 0;
             index < genreIds.length();
             index++) {

            String name =
                    genreMap.get(
                            genreIds.optInt(
                                    index,
                                    -1
                            )
                    );

            if (name != null
                    && !names.contains(name)) {

                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    /**
     * 성인 콘텐츠와 차단 키워드 콘텐츠를 제외합니다.
     */
    private boolean shouldExcludeContent(
            JSONObject item) {

        if (item.optBoolean(
                "adult",
                false
        )) {

            return true;
        }

        String checkText =
                normalizeSearchText(
                        safeText(
                                firstNonBlank(
                                        item.optString(
                                                "title",
                                                null
                                        ),
                                        item.optString(
                                                "name",
                                                null
                                        )
                                )
                        )
                                + " "
                                + safeText(
                                        firstNonBlank(
                                                item.optString(
                                                        "original_title",
                                                        null
                                                ),
                                                item.optString(
                                                        "original_name",
                                                        null
                                                )
                                        )
                                )
                );

        String[] blockedKeywords = {
                "성인영화",
                "에로영화",
                "에로틱",
                "포르노",
                "porn",
                "porno",
                "adultmovie",
                "섹스무비",
                "무삭제판",
                "19금에로",
                "바람난형수님",
                "형수님참교육"
        };

        for (String keyword
                : blockedKeywords) {

            if (checkText.contains(
                    normalizeSearchText(keyword)
            )) {

                return true;
            }
        }

        return false;
    }

    /**
     * TMDB 제공처 이름을 ODITJI 플랫폼 키로 변환합니다.
     */
    private String normalizePlatformName(
            String name) {

        if (name == null) {
            return "";
        }

        String normalized =
                normalizeSearchText(name);

        if (normalized.contains("netflix")
                || normalized.contains("넷플릭스")) {

            return "netflix";
        }

        if (normalized.contains("tving")
                || normalized.contains("티빙")) {

            return "tving";
        }

        if (normalized.contains("wavve")
                || normalized.contains("웨이브")) {

            return "wavve";
        }

        if (normalized.contains("disney")
                || normalized.contains("디즈니")) {

            return "disney";
        }

        if (normalized.contains("watcha")
                || normalized.contains("왓챠")) {

            return "watcha";
        }

        if (normalized.contains("coupang")
                || normalized.contains("쿠팡")) {

            return "coupang";
        }

        return "";
    }

    /**
     * TMDB API를 호출합니다.
     *
     * HTTP 429와 일시적 IOException은 최대 4회 재시도합니다.
     */
    private JSONObject callTmdbApi(
            String apiUrl) {

        int retryCount = 0;

        while (retryCount < 4) {

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(
                                    URI.create(apiUrl)
                            )
                            .timeout(
                                    Duration.ofSeconds(30)
                            )
                            .header(
                                    "Authorization",
                                    "Bearer " + token
                            )
                            .header(
                                    "accept",
                                    "application/json"
                            )
                            .GET()
                            .build();

            try {

                HttpResponse<String> response =
                        httpClient.send(
                                request,
                                HttpResponse.BodyHandlers
                                        .ofString(
                                                StandardCharsets.UTF_8
                                        )
                        );

                int statusCode =
                        response.statusCode();

                if (statusCode == 429) {

                    retryCount++;

                    sleepQuietly(
                            1000L * retryCount
                    );

                    continue;
                }

                if (statusCode < 200
                        || statusCode >= 300) {

                    throw new IllegalStateException(
                            "TMDB HTTP 오류: "
                                    + statusCode
                    );
                }

                String body =
                        response.body();

                if (body == null
                        || body.isBlank()) {

                    throw new IllegalStateException(
                            "TMDB 응답 본문이 비어 있습니다."
                    );
                }

                JSONObject result =
                        new JSONObject(body);

                sleepQuietly(
                        requestDelayMillis
                );

                return result;

            } catch (InterruptedException e) {

                Thread.currentThread()
                        .interrupt();

                throw new IllegalStateException(
                        "TMDB API 호출이 중단되었습니다.",
                        e
                );

            } catch (IOException e) {

                retryCount++;

                if (retryCount >= 4) {

                    throw new IllegalStateException(
                            "TMDB API 통신에 실패했습니다.",
                            e
                    );
                }

                sleepQuietly(
                        500L * retryCount
                );

            } catch (JSONException e) {

                throw new IllegalStateException(
                        "TMDB JSON 응답을 해석하지 못했습니다.",
                        e
                );
            }
        }

        throw new IllegalStateException(
                "TMDB API 재시도 횟수를 초과했습니다."
        );
    }

    /**
     * 요청 간격과 재시도 대기를 처리합니다.
     */
    private void sleepQuietly(
            long millis) {

        if (millis <= 0) {
            return;
        }

        try {

            Thread.sleep(millis);

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }
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

    private String encode(
            String value) {

        return URLEncoder.encode(
                value == null
                        ? ""
                        : value,
                StandardCharsets.UTF_8
        );
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

    private Long nullableLong(
            JSONObject json,
            String key) {

        if (!json.has(key)
                || json.isNull(key)) {

            return null;
        }

        long value =
                json.optLong(
                        key,
                        0L
                );

        return value <= 0
                ? null
                : value;
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

    private Double nullableDouble(
            JSONObject json,
            String key) {

        if (!json.has(key)
                || json.isNull(key)) {

            return null;
        }

        return json.optDouble(key);
    }

    private static Map<Integer, String>
            createMovieGenreMap() {

        Map<Integer, String> map =
                new LinkedHashMap<Integer, String>();

        map.put(28, "액션");
        map.put(12, "모험");
        map.put(16, "애니메이션");
        map.put(35, "코미디");
        map.put(80, "범죄");
        map.put(99, "다큐멘터리");
        map.put(18, "드라마");
        map.put(10751, "가족");
        map.put(14, "판타지");
        map.put(36, "역사");
        map.put(27, "공포");
        map.put(10402, "음악");
        map.put(9648, "미스터리");
        map.put(10749, "로맨스");
        map.put(878, "SF");
        map.put(10770, "TV 영화");
        map.put(53, "스릴러");
        map.put(10752, "전쟁");
        map.put(37, "서부");

        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, String>
            createTvGenreMap() {

        Map<Integer, String> map =
                new LinkedHashMap<Integer, String>();

        map.put(10759, "액션·모험");
        map.put(16, "애니메이션");
        map.put(35, "코미디");
        map.put(80, "범죄");
        map.put(99, "다큐멘터리");
        map.put(18, "드라마");
        map.put(10751, "가족");
        map.put(10762, "키즈");
        map.put(9648, "미스터리");
        map.put(10763, "뉴스");
        map.put(10764, "리얼리티");
        map.put(10765, "SF·판타지");
        map.put(10766, "연속극");
        map.put(10767, "토크");
        map.put(10768, "전쟁·정치");
        map.put(37, "서부");

        return Collections.unmodifiableMap(map);
    }

    /**
     * 영화와 TV별 provider ID 매핑을 보관합니다.
     */
    private static final class ProviderRegistry {

        private final Map<Integer, String> movieProviderMap;
        private final Map<Integer, String> tvProviderMap;

        private ProviderRegistry(
                Map<Integer, String> movieProviderMap,
                Map<Integer, String> tvProviderMap) {

            this.movieProviderMap =
                    movieProviderMap == null
                            ? Map.of()
                            : Map.copyOf(
                                    movieProviderMap
                            );

            this.tvProviderMap =
                    tvProviderMap == null
                            ? Map.of()
                            : Map.copyOf(
                                    tvProviderMap
                            );
        }

        private Set<Integer> getMovieProviderIds() {
            return movieProviderMap.keySet();
        }

        private Set<Integer> getTvProviderIds() {
            return tvProviderMap.keySet();
        }

        private Map<Integer, String> getProviderMap(
                String contentType) {

            return MOVIE.equals(contentType)
                    ? movieProviderMap
                    : tvProviderMap;
        }

        private boolean hasAnyProvider() {

            return !movieProviderMap.isEmpty()
                    || !tvProviderMap.isEmpty();
        }
    }
}