package com.project.oditji.search.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

@Service
public class SearchServiceImpl implements SearchService {

    private static final String CONTENT_TYPE_MOVIE = "MOVIE";
    private static final String CONTENT_TYPE_TV = "TV";

    /*
     * TMDB의 adult=false만으로 걸러지지 않는 오분류 콘텐츠를 위한
     * ODITJI 자체 차단 키워드입니다.
     *
     * 일반 작품까지 과도하게 제외하지 않도록
     * 단일 단어보다 명확한 성인 콘텐츠 표현 위주로 등록합니다.
     */
    private static final String[] BLOCKED_ADULT_KEYWORDS = {
            "성인영화",
            "에로영화",
            "포르노",
            "porn",
            "porno",
            "adultmovie",
            "섹스무비",
            "19금에로",
            "바람난형수님",
            "형수님참교육"
    };

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.language:ko-KR}")
    private String language;

    @Value("${tmdb.api.region:KR}")
    private String region;

    private final TmdbDAO tmdbDAO;
    private final TmdbProviderCacheService tmdbProviderCacheService;
    private final HttpClient httpClient;

    private final Map<String, Integer> movieGenreMap;
    private final Map<String, Integer> tvGenreMap;

    public SearchServiceImpl(
            TmdbDAO tmdbDAO,
            TmdbProviderCacheService tmdbProviderCacheService) {

        this.tmdbDAO = tmdbDAO;
        this.tmdbProviderCacheService = tmdbProviderCacheService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.movieGenreMap = createMovieGenreMap();
        this.tvGenreMap = createTvGenreMap();
    }

    /**
     * 검색어가 없는 경우
     *
     * 필터 있음:
     * - /discover/movie
     * - /discover/tv
     *
     * 필터 없음:
     * - /trending/all/week
     */
    @Override
    public SearchResultPageVO getPopularContent(
            int page,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        int currentPage = normalizePage(page);

        List<String> normalizedContentTypes =
                normalizeContentTypes(contentTypes);

        List<String> normalizedGenreCodes =
                normalizeStringList(genreCodes);

        List<String> normalizedProviderIds =
                normalizeStringList(providerIds);

        boolean hasGenreFilter = !normalizedGenreCodes.isEmpty();
        boolean hasProviderFilter = !normalizedProviderIds.isEmpty();
        boolean hasContentTypeFilter =
                contentTypes != null && !contentTypes.isEmpty();

        if (!hasGenreFilter
                && !hasProviderFilter
                && !hasContentTypeFilter) {

            return attachPlatformLogos(
                    callTrendingApi(currentPage)
            );
        }

        return attachPlatformLogos(
                callDiscoverApis(
                        null,
                        currentPage,
                        normalizedContentTypes,
                        normalizedGenreCodes,
                        normalizedProviderIds
                )
        );
    }

    /**
     * 검색어가 있는 경우
     *
     * 필터 없음:
     * - /search/multi
     * - 또는 카테고리 하나만 선택되면 /search/movie, /search/tv
     *
     * 장르/플랫폼 필터 있음:
     * - /discover API 호출 후 현재 결과에서 검색어 추가 검사
     */
    @Override
    public SearchResultPageVO searchByTmdb(
            String keyword,
            int page,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        int currentPage = normalizePage(page);

        String normalizedKeyword =
                keyword == null ? "" : keyword.trim();

        List<String> normalizedContentTypes =
                normalizeContentTypes(contentTypes);

        List<String> normalizedGenreCodes =
                normalizeStringList(genreCodes);

        List<String> normalizedProviderIds =
                normalizeStringList(providerIds);

        if (normalizedKeyword.isEmpty()) {
            return getPopularContent(
                    currentPage,
                    normalizedContentTypes,
                    normalizedGenreCodes,
                    normalizedProviderIds
            );
        }

        boolean hasGenreFilter = !normalizedGenreCodes.isEmpty();
        boolean hasProviderFilter = !normalizedProviderIds.isEmpty();

        if (hasGenreFilter || hasProviderFilter) {
            return attachPlatformLogos(
                    callDiscoverApis(
                            normalizedKeyword,
                            currentPage,
                            normalizedContentTypes,
                            normalizedGenreCodes,
                            normalizedProviderIds
                    )
            );
        }

        if (normalizedContentTypes.size() == 1) {
            String contentType = normalizedContentTypes.get(0);

            if (CONTENT_TYPE_MOVIE.equals(contentType)) {
                return attachPlatformLogos(
                        callMovieSearchApi(
                                normalizedKeyword,
                                currentPage
                        )
                );
            }

            if (CONTENT_TYPE_TV.equals(contentType)) {
                return attachPlatformLogos(
                        callTvSearchApi(
                                normalizedKeyword,
                                currentPage
                        )
                );
            }
        }

        return attachPlatformLogos(
                callMultiSearchApi(
                        normalizedKeyword,
                        currentPage
                )
        );
    }

    /**
     * 영화와 TV Discover API 호출 결과 합치기
     */
    private SearchResultPageVO callDiscoverApis(
            String keyword,
            int page,
            List<String> contentTypes,
            List<String> genreCodes,
            List<String> providerIds) {

        List<SearchResultVO> mergedList =
                new ArrayList<SearchResultVO>();

        int totalPages = 0;
        int totalResults = 0;

        for (String contentType : contentTypes) {
            SearchResultPageVO partialPage;

            if (CONTENT_TYPE_MOVIE.equals(contentType)) {
                partialPage = callMovieDiscoverApi(
                        page,
                        genreCodes,
                        providerIds
                );
            } else if (CONTENT_TYPE_TV.equals(contentType)) {
                partialPage = callTvDiscoverApi(
                        page,
                        genreCodes,
                        providerIds
                );
            } else {
                continue;
            }

            mergedList.addAll(partialPage.getResultList());

            totalPages = Math.max(
                    totalPages,
                    partialPage.getTotalPages()
            );

            totalResults += partialPage.getTotalResults();
        }

        mergedList = removeDuplicateResults(mergedList);

        if (keyword != null && !keyword.trim().isEmpty()) {
            mergedList = filterByKeyword(
                    mergedList,
                    keyword
            );

            /*
             * TMDB Discover API 전체 결과에 검색어가 적용된 것이 아니라
             * 현재 받아온 페이지 결과에 Java 필터를 적용한 것이므로,
             * 여기서는 화면에 실제 출력되는 개수로 맞춘다.
             */
            totalResults = mergedList.size();

            if (mergedList.isEmpty()) {
                totalPages = 0;
            } else {
                totalPages = Math.max(totalPages, 1);
            }
        }

        sortByPopularity(mergedList);

        SearchResultPageVO pageVO =
                new SearchResultPageVO();

        pageVO.setResultList(mergedList);
        pageVO.setPage(page);
        pageVO.setTotalPages(totalPages);
        pageVO.setTotalResults(totalResults);

        return pageVO;
    }

    /**
     * 조건 없는 인기 콘텐츠
     */
    private SearchResultPageVO callTrendingApi(int page) {

        String apiUrl = baseUrl
                + "/trending/all/week"
                + "?language=" + encode(language)
                + "&page=" + page;

        JSONObject root = callTmdbApi(apiUrl);

        SearchResultPageVO pageVO =
                createPageInformation(root);

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        JSONArray results = root.optJSONArray("results");

        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);

                if (item == null || shouldExcludeContent(item)) {
                    continue;
                }

                String mediaType =
                        item.optString("media_type", "");

                if ("movie".equalsIgnoreCase(mediaType)) {
                    resultList.add(convertMovie(item));
                } else if ("tv".equalsIgnoreCase(mediaType)) {
                    resultList.add(convertTv(item));
                }
            }
        }

        pageVO.setResultList(resultList);

        return pageVO;
    }

    /**
     * 통합 검색
     */
    private SearchResultPageVO callMultiSearchApi(
            String keyword,
            int page) {

        String apiUrl = baseUrl
                + "/search/multi"
                + "?query=" + encode(keyword)
                + "&language=" + encode(language)
                + "&region=" + encode(region)
                + "&page=" + page
                + "&include_adult=false";

        JSONObject root = callTmdbApi(apiUrl);

        SearchResultPageVO pageVO =
                createPageInformation(root);

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        JSONArray results = root.optJSONArray("results");

        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);

                if (item == null || shouldExcludeContent(item)) {
                    continue;
                }

                String mediaType =
                        item.optString("media_type", "");

                if ("movie".equalsIgnoreCase(mediaType)) {
                    resultList.add(convertMovie(item));
                } else if ("tv".equalsIgnoreCase(mediaType)) {
                    resultList.add(convertTv(item));
                }
            }
        }

        pageVO.setResultList(resultList);

        return pageVO;
    }

    /**
     * 영화 제목 검색
     */
    private SearchResultPageVO callMovieSearchApi(
            String keyword,
            int page) {

        String apiUrl = baseUrl
                + "/search/movie"
                + "?query=" + encode(keyword)
                + "&language=" + encode(language)
                + "&region=" + encode(region)
                + "&page=" + page
                + "&include_adult=false";

        JSONObject root = callTmdbApi(apiUrl);

        return convertMoviePage(root);
    }

    /**
     * TV 제목 검색
     */
    private SearchResultPageVO callTvSearchApi(
            String keyword,
            int page) {

        String apiUrl = baseUrl
                + "/search/tv"
                + "?query=" + encode(keyword)
                + "&language=" + encode(language)
                + "&page=" + page
                + "&include_adult=false";

        JSONObject root = callTmdbApi(apiUrl);

        return convertTvPage(root);
    }

    /**
     * 영화 장르/플랫폼 검색
     */
    private SearchResultPageVO callMovieDiscoverApi(
            int page,
            List<String> genreCodes,
            List<String> providerIds) {

        String genreQuery = buildGenreQuery(
                genreCodes,
                movieGenreMap
        );

        String providerQuery =
                buildProviderQuery(providerIds);

        StringBuilder apiUrl = new StringBuilder();

        apiUrl.append(baseUrl)
                .append("/discover/movie")
                .append("?language=")
                .append(encode(language))
                .append("&region=")
                .append(encode(region))
                .append("&watch_region=")
                .append(encode(region))
                .append("&sort_by=popularity.desc")
                .append("&include_adult=false")
                .append("&page=")
                .append(page);

        if (!genreQuery.isEmpty()) {
            apiUrl.append("&with_genres=")
                    .append(encode(genreQuery));
        }

        if (!providerQuery.isEmpty()) {
            apiUrl.append("&with_watch_providers=")
                    .append(encode(providerQuery));

            apiUrl.append("&with_watch_monetization_types=flatrate");
        }

        JSONObject root =
                callTmdbApi(apiUrl.toString());

        return convertMoviePage(root);
    }

    /**
     * TV 장르/플랫폼 검색
     */
    private SearchResultPageVO callTvDiscoverApi(
            int page,
            List<String> genreCodes,
            List<String> providerIds) {

        String genreQuery = buildGenreQuery(
                genreCodes,
                tvGenreMap
        );

        String providerQuery =
                buildProviderQuery(providerIds);

        StringBuilder apiUrl = new StringBuilder();

        apiUrl.append(baseUrl)
                .append("/discover/tv")
                .append("?language=")
                .append(encode(language))
                .append("&watch_region=")
                .append(encode(region))
                .append("&sort_by=popularity.desc")
                .append("&include_adult=false")
                .append("&page=")
                .append(page);

        if (!genreQuery.isEmpty()) {
            apiUrl.append("&with_genres=")
                    .append(encode(genreQuery));
        }

        if (!providerQuery.isEmpty()) {
            apiUrl.append("&with_watch_providers=")
                    .append(encode(providerQuery));

            apiUrl.append("&with_watch_monetization_types=flatrate");
        }

        JSONObject root =
                callTmdbApi(apiUrl.toString());

        return convertTvPage(root);
    }

    /**
     * TMDB API 공통 호출
     */
    private JSONObject callTmdbApi(String apiUrl) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8
                            )
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                System.err.println(
                        "TMDB API 호출 실패"
                                + " / status="
                                + response.statusCode()
                                + " / url="
                                + apiUrl
                );

                return new JSONObject();
            }

            String responseBody = response.body();

            if (responseBody == null
                    || responseBody.trim().isEmpty()) {

                return new JSONObject();
            }

            return new JSONObject(responseBody);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.err.println(
                    "TMDB API 호출 중 인터럽트 발생: "
                            + e.getMessage()
            );

            return new JSONObject();

        } catch (IOException | IllegalArgumentException e) {
            System.err.println(
                    "TMDB API 호출 중 오류 발생: "
                            + e.getMessage()
            );

            return new JSONObject();
        }
    }

    private SearchResultPageVO convertMoviePage(
            JSONObject root) {

        SearchResultPageVO pageVO =
                createPageInformation(root);

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        JSONArray results = root.optJSONArray("results");

        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);

                if (item != null && !shouldExcludeContent(item)) {
                    resultList.add(convertMovie(item));
                }
            }
        }

        pageVO.setResultList(resultList);

        return pageVO;
    }

    private SearchResultPageVO convertTvPage(
            JSONObject root) {

        SearchResultPageVO pageVO =
                createPageInformation(root);

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        JSONArray results = root.optJSONArray("results");

        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);

                if (item != null && !shouldExcludeContent(item)) {
                    resultList.add(convertTv(item));
                }
            }
        }

        pageVO.setResultList(resultList);

        return pageVO;
    }

    private SearchResultPageVO createPageInformation(
            JSONObject root) {

        SearchResultPageVO pageVO =
                new SearchResultPageVO();

        pageVO.setPage(root.optInt("page", 1));
        pageVO.setTotalPages(
                root.optInt("total_pages", 0)
        );
        pageVO.setTotalResults(
                root.optInt("total_results", 0)
        );

        return pageVO;
    }

    private SearchResultVO convertMovie(
            JSONObject item) {

        SearchResultVO resultVO =
                new SearchResultVO();

        resultVO.setTmdbId(
                getNullableLong(item, "id")
        );

        resultVO.setContentType(
                CONTENT_TYPE_MOVIE
        );

        resultVO.setTitle(
                getNullableString(item, "title")
        );

        resultVO.setOriginalTitle(
                getNullableString(
                        item,
                        "original_title"
                )
        );

        resultVO.setOverview(
                getNullableString(item, "overview")
        );

        resultVO.setPosterPath(
                getNullableString(
                        item,
                        "poster_path"
                )
        );

        resultVO.setBackdropPath(
                getNullableString(
                        item,
                        "backdrop_path"
                )
        );

        resultVO.setReleaseDate(
                getNullableString(
                        item,
                        "release_date"
                )
        );

        resultVO.setTmdbScore(
                getNullableDouble(
                        item,
                        "vote_average"
                )
        );

        resultVO.setPopularity(
                getNullableDouble(
                        item,
                        "popularity"
                )
        );

        return resultVO;
    }

    private SearchResultVO convertTv(
            JSONObject item) {

        SearchResultVO resultVO =
                new SearchResultVO();

        resultVO.setTmdbId(
                getNullableLong(item, "id")
        );

        resultVO.setContentType(
                CONTENT_TYPE_TV
        );

        resultVO.setTitle(
                getNullableString(item, "name")
        );

        resultVO.setOriginalTitle(
                getNullableString(
                        item,
                        "original_name"
                )
        );

        resultVO.setOverview(
                getNullableString(item, "overview")
        );

        resultVO.setPosterPath(
                getNullableString(
                        item,
                        "poster_path"
                )
        );

        resultVO.setBackdropPath(
                getNullableString(
                        item,
                        "backdrop_path"
                )
        );

        resultVO.setReleaseDate(
                getNullableString(
                        item,
                        "first_air_date"
                )
        );

        resultVO.setTmdbScore(
                getNullableDouble(
                        item,
                        "vote_average"
                )
        );

        resultVO.setPopularity(
                getNullableDouble(
                        item,
                        "popularity"
                )
        );

        return resultVO;
    }

    /**
     * TMDB 검색 결과에 한국 기준 OTT 제공처를 조회한 뒤,
     * DB의 OTT_PLATFORM.LOGO_IMAGE 정보를 연결합니다.
     */
    private SearchResultPageVO attachPlatformLogos(
            SearchResultPageVO pageVO) {

        if (pageVO == null
                || pageVO.getResultList() == null
                || pageVO.getResultList().isEmpty()) {

            return pageVO;
        }

        List<OttPlatformVO> activePlatformList =
                tmdbDAO.selectActivePlatformList();

        Map<String, OttPlatformVO> platformMap =
                createPlatformMap(activePlatformList);

        System.out.println(
                "[검색 OTT DB 플랫폼 키] "
                        + platformMap.keySet()
        );

        List<SearchResultVO> filteredResultList =
                new ArrayList<SearchResultVO>();

        for (SearchResultVO resultVO
                : pageVO.getResultList()) {

            if (resultVO == null
                    || resultVO.getTmdbId() == null
                    || resultVO.getContentType() == null) {

                continue;
            }

            List<OttPlatformVO> resultPlatformList =
                    findPlatformList(
                            resultVO.getTmdbId(),
                            resultVO.getContentType(),
                            platformMap
                    );

            /*
             * DB에 등록된 활성 OTT 6개 중 하나와도 매칭되지 않으면
             * 검색 결과 자체에서 제외합니다.
             *
             * Apple TV, Crunchyroll처럼 TMDB 응답에는 존재하지만
             * ODITJI 지원 플랫폼이 아닌 제공처만 있는 콘텐츠는
             * 이 지점에서 제거됩니다.
             */
            if (resultPlatformList == null
                    || resultPlatformList.isEmpty()) {

                continue;
            }

            resultVO.setPlatformList(
                    resultPlatformList
            );

            filteredResultList.add(resultVO);
        }

        pageVO.setResultList(filteredResultList);

        return pageVO;
    }

    private Map<String, OttPlatformVO> createPlatformMap(
            List<OttPlatformVO> activePlatformList) {

        Map<String, OttPlatformVO> platformMap =
                new HashMap<String, OttPlatformVO>();

        if (activePlatformList == null) {
            return platformMap;
        }

        for (OttPlatformVO platformVO : activePlatformList) {

            if (platformVO == null
                    || platformVO.getPlatformName() == null) {

                continue;
            }

            String key = normalizePlatformName(
                    platformVO.getPlatformName()
            );

            if (!key.isEmpty()) {
                platformMap.put(key, platformVO);
            }
        }

        return platformMap;
    }

    private List<OttPlatformVO> findPlatformList(
            Long tmdbId,
            String contentType,
            Map<String, OttPlatformVO> platformMap) {

        List<OttPlatformVO> resultList =
                new ArrayList<OttPlatformVO>();

        JSONObject root =
                tmdbProviderCacheService
                        .getWatchProviderResult(
                                tmdbId,
                                contentType
                        );

        JSONObject results =
                root.optJSONObject("results");

        if (results == null) {
            return resultList;
        }

        JSONObject korea =
                results.optJSONObject(region);

        if (korea == null) {
            return resultList;
        }

        System.out.println(
                "[검색 OTT 한국 응답] tmdbId="
                        + tmdbId
                        + ", type="
                        + contentType
                        + ", data="
                        + korea
        );

        Set<Integer> duplicateCheck =
                new HashSet<Integer>();

        /* 정액제 구독 */
        addPlatformsFromProviderArray(
                "flatrate",
                korea.optJSONArray("flatrate"),
                platformMap,
                duplicateCheck,
                resultList
        );

        /* 광고 포함 무료 시청 */
        addPlatformsFromProviderArray(
                "ads",
                korea.optJSONArray("ads"),
                platformMap,
                duplicateCheck,
                resultList
        );

        /* 무료 시청 */
        addPlatformsFromProviderArray(
                "free",
                korea.optJSONArray("free"),
                platformMap,
                duplicateCheck,
                resultList
        );

        return resultList;
    }

    private void addPlatformsFromProviderArray(
            String providerType,
            JSONArray providerArray,
            Map<String, OttPlatformVO> platformMap,
            Set<Integer> duplicateCheck,
            List<OttPlatformVO> resultList) {

        if (providerArray == null) {
            return;
        }

        for (int i = 0;
                i < providerArray.length();
                i++) {

            JSONObject provider =
                    providerArray.optJSONObject(i);

            if (provider == null) {
                continue;
            }

            int providerId =
                    provider.optInt("provider_id", 0);

            String providerName =
                    provider.optString(
                            "provider_name",
                            ""
                    );

            String key =
                    normalizePlatformName(providerName);

            OttPlatformVO platformVO =
                    platformMap.get(key);

            System.out.println(
                    "[검색 OTT 매칭] type="
                            + providerType
                            + ", providerId="
                            + providerId
                            + ", TMDB 이름="
                            + providerName
                            + ", 정규화="
                            + key
                            + ", DB 매칭="
                            + (platformVO == null
                                    ? "실패"
                                    : platformVO.getPlatformName())
            );

            if (platformVO == null) {
                continue;
            }

            Integer platformNo =
                    platformVO.getPlatformNo();

            if (platformNo == null
                    || duplicateCheck.add(platformNo)) {

                resultList.add(platformVO);
            }
        }
    }

    private String normalizePlatformName(
            String platformName) {

        if (platformName == null) {
            return "";
        }

        String normalized = platformName
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        if (normalized.contains("netflix")) {
            return "netflix";
        }

        if (normalized.contains("tving")) {
            return "tving";
        }

        if (normalized.contains("wavve")) {
            return "wavve";
        }

        if (normalized.contains("disney")) {
            return "disney";
        }

        if (normalized.contains("watcha")) {
            return "watcha";
        }

        if (normalized.contains("coupang")) {
            return "coupang";
        }

        return normalized;
    }

    /**
     * 장르 체크박스 여러 개 선택 시 OR 조건
     *
     * 예:
     * ACTION, COMEDY
     * 영화 → 28|35
     */
    private String buildGenreQuery(
            List<String> genreCodes,
            Map<String, Integer> genreMap) {

        if (genreCodes == null
                || genreCodes.isEmpty()) {

            return "";
        }

        List<String> genreIds =
                new ArrayList<String>();

        Set<String> duplicateCheck =
                new HashSet<String>();

        for (String genreCode : genreCodes) {
            if (genreCode == null
                    || genreCode.trim().isEmpty()) {

                continue;
            }

            String normalizedCode =
                    genreCode.trim().toUpperCase();

            Integer genreId =
                    genreMap.get(normalizedCode);

            if (genreId == null) {
                continue;
            }

            String genreIdText =
                    String.valueOf(genreId);

            if (duplicateCheck.add(genreIdText)) {
                genreIds.add(genreIdText);
            }
        }

        return String.join("|", genreIds);
    }

    /**
     * 플랫폼 체크박스 여러 개 선택 시 OR 조건
     */
    private String buildProviderQuery(
            List<String> providerIds) {

        if (providerIds == null
                || providerIds.isEmpty()) {

            return "";
        }

        List<String> normalizedIds =
                new ArrayList<String>();

        Set<String> duplicateCheck =
                new HashSet<String>();

        for (String providerId : providerIds) {
            if (providerId == null
                    || providerId.trim().isEmpty()) {

                continue;
            }

            String normalizedId =
                    providerId.trim();

            try {
                Integer.parseInt(normalizedId);

                if (duplicateCheck.add(normalizedId)) {
                    normalizedIds.add(normalizedId);
                }

            } catch (NumberFormatException e) {
                // 숫자가 아닌 provider ID는 제외
            }
        }

        return String.join("|", normalizedIds);
    }

    private List<String> normalizeContentTypes(
            List<String> contentTypes) {

        List<String> normalizedList =
                new ArrayList<String>();

        if (contentTypes != null) {
            for (String contentType : contentTypes) {
                if (contentType == null) {
                    continue;
                }

                String normalizedType =
                        contentType.trim().toUpperCase();

                if (CONTENT_TYPE_MOVIE.equals(normalizedType)
                        && !normalizedList.contains(
                                CONTENT_TYPE_MOVIE
                        )) {

                    normalizedList.add(
                            CONTENT_TYPE_MOVIE
                    );
                }

                if (CONTENT_TYPE_TV.equals(normalizedType)
                        && !normalizedList.contains(
                                CONTENT_TYPE_TV
                        )) {

                    normalizedList.add(
                            CONTENT_TYPE_TV
                    );
                }
            }
        }

        // 카테고리 미선택은 영화 + TV 전체
        if (normalizedList.isEmpty()) {
            normalizedList.add(CONTENT_TYPE_MOVIE);
            normalizedList.add(CONTENT_TYPE_TV);
        }

        return normalizedList;
    }

    private List<String> normalizeStringList(
            List<String> values) {

        List<String> normalizedList =
                new ArrayList<String>();

        if (values == null) {
            return normalizedList;
        }

        for (String value : values) {
            if (value == null
                    || value.trim().isEmpty()) {

                continue;
            }

            normalizedList.add(value.trim());
        }

        return normalizedList;
    }

    private int normalizePage(int page) {
        return page <= 0 ? 1 : page;
    }

    private List<SearchResultVO> filterByKeyword(
            List<SearchResultVO> resultList,
            String keyword) {

        List<SearchResultVO> filteredList =
                new ArrayList<SearchResultVO>();

        if (resultList == null
                || resultList.isEmpty()) {

            return filteredList;
        }

        if (keyword == null
                || keyword.trim().isEmpty()) {

            filteredList.addAll(resultList);
            return filteredList;
        }

        String normalizedKeyword =
                keyword.trim().toLowerCase();

        for (SearchResultVO resultVO : resultList) {
            if (containsIgnoreCase(
                    resultVO.getTitle(),
                    normalizedKeyword
            )) {
                filteredList.add(resultVO);
                continue;
            }

            if (containsIgnoreCase(
                    resultVO.getOriginalTitle(),
                    normalizedKeyword
            )) {
                filteredList.add(resultVO);
                continue;
            }

            if (containsIgnoreCase(
                    resultVO.getOverview(),
                    normalizedKeyword
            )) {
                filteredList.add(resultVO);
            }
        }

        return filteredList;
    }

    private boolean containsIgnoreCase(
            String target,
            String normalizedKeyword) {

        return target != null
                && target.toLowerCase()
                        .contains(normalizedKeyword);
    }

    private List<SearchResultVO> removeDuplicateResults(
            List<SearchResultVO> resultList) {

        List<SearchResultVO> uniqueList =
                new ArrayList<SearchResultVO>();

        Set<String> keySet =
                new HashSet<String>();

        if (resultList == null) {
            return uniqueList;
        }

        for (SearchResultVO resultVO : resultList) {
            if (resultVO == null
                    || resultVO.getTmdbId() == null
                    || resultVO.getContentType() == null) {

                continue;
            }

            String key =
                    resultVO.getContentType()
                            + "_"
                            + resultVO.getTmdbId();

            if (keySet.add(key)) {
                uniqueList.add(resultVO);
            }
        }

        return uniqueList;
    }

    private void sortByPopularity(
            List<SearchResultVO> resultList) {

        resultList.sort((first, second) -> {
            double firstPopularity =
                    first.getPopularity() == null
                            ? 0.0
                            : first.getPopularity();

            double secondPopularity =
                    second.getPopularity() == null
                            ? 0.0
                            : second.getPopularity();

            return Double.compare(
                    secondPopularity,
                    firstPopularity
            );
        });
    }

    private Map<String, Integer> createMovieGenreMap() {

        Map<String, Integer> map =
                new HashMap<String, Integer>();

        map.put("ACTION", 28);
        map.put("ADVENTURE", 12);
        map.put("ANIMATION", 16);
        map.put("COMEDY", 35);
        map.put("CRIME", 80);
        map.put("DOCUMENTARY", 99);
        map.put("DRAMA", 18);
        map.put("FAMILY", 10751);
        map.put("FANTASY", 14);
        map.put("HISTORY", 36);
        map.put("HORROR", 27);
        map.put("MUSIC", 10402);
        map.put("MYSTERY", 9648);
        map.put("ROMANCE", 10749);
        map.put("SCI_FI", 878);
        map.put("THRILLER", 53);
        map.put("WAR", 10752);
        map.put("WESTERN", 37);

        return map;
    }

    private Map<String, Integer> createTvGenreMap() {

        Map<String, Integer> map =
                new HashMap<String, Integer>();

        map.put("ACTION", 10759);
        map.put("ADVENTURE", 10759);
        map.put("ANIMATION", 16);
        map.put("COMEDY", 35);
        map.put("CRIME", 80);
        map.put("DOCUMENTARY", 99);
        map.put("DRAMA", 18);
        map.put("FAMILY", 10751);
        map.put("KIDS", 10762);
        map.put("MYSTERY", 9648);
        map.put("NEWS", 10763);
        map.put("REALITY", 10764);
        map.put("SCI_FI", 10765);
        map.put("FANTASY", 10765);
        map.put("SOAP", 10766);
        map.put("TALK", 10767);
        map.put("WAR", 10768);
        map.put("WESTERN", 37);

        /*
         * TV에는 영화와 같은 THRILLER 장르 ID가 없으므로
         * 미스터리 장르로 대응
         */
        map.put("THRILLER", 9648);

        /*
         * TV에는 영화와 같은 ROMANCE 장르 ID가 없으므로
         * 드라마 장르로 대응
         */
        map.put("ROMANCE", 18);

        /*
         * TV에는 별도 HORROR 장르 ID가 없으므로
         * 미스터리 장르로 대응
         */
        map.put("HORROR", 9648);

        return map;
    }

    /**
     * TMDB 성인 플래그와 ODITJI 자체 차단 키워드를 함께 검사합니다.
     */
    private boolean shouldExcludeContent(JSONObject item) {

        if (item == null) {
            return true;
        }

        if (item.optBoolean("adult", false)) {
            return true;
        }

        String title = firstNonBlank(
                getNullableString(item, "title"),
                getNullableString(item, "name")
        );

        String originalTitle = firstNonBlank(
                getNullableString(item, "original_title"),
                getNullableString(item, "original_name")
        );

        String overview = getNullableString(item, "overview");

        String normalizedTarget = normalizeAdultFilterText(
                safeText(title)
                        + " "
                        + safeText(originalTitle)
                        + " "
                        + safeText(overview)
        );

        for (String blockedKeyword : BLOCKED_ADULT_KEYWORDS) {

            String normalizedKeyword =
                    normalizeAdultFilterText(blockedKeyword);

            if (!normalizedKeyword.isEmpty()
                    && normalizedTarget.contains(
                            normalizedKeyword
                    )) {

                System.out.println(
                        "[ODITJI 성인 콘텐츠 제외] "
                                + safeText(title)
                                + " / keyword="
                                + blockedKeyword
                );

                return true;
            }
        }

        return false;
    }

    /**
     * 공백, 특수문자, 대소문자 차이를 제거해 비교합니다.
     */
    private String normalizeAdultFilterText(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String firstNonBlank(String first, String second) {

        if (first != null && !first.isBlank()) {
            return first;
        }

        return second;
    }

    private String getNullableString(
            JSONObject object,
            String key) {

        if (object == null
                || !object.has(key)
                || object.isNull(key)) {

            return null;
        }

        String value = object.optString(key, null);

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        return value;
    }

    private Long getNullableLong(
            JSONObject object,
            String key) {

        if (object == null
                || !object.has(key)
                || object.isNull(key)) {

            return null;
        }

        return object.optLong(key);
    }

    private Double getNullableDouble(
            JSONObject object,
            String key) {

        if (object == null
                || !object.has(key)
                || object.isNull(key)) {

            return null;
        }

        return object.optDouble(key);
    }

    private String encode(String value) {
        if (value == null) {
            return "";
        }

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}