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

    private static final String MOVIE = "MOVIE";
    private static final String TV = "TV";

    private static final String CATEGORY_MOVIE = "MOVIE";
    private static final String CATEGORY_DRAMA = "DRAMA";
    private static final String CATEGORY_ANIMATION = "ANIMATION";
    private static final String CATEGORY_VARIETY = "VARIETY";
    private static final String CATEGORY_DOCUMENTARY = "DOCUMENTARY";
    private static final int PERSON_LIMIT = 5;
    private static final int PERSON_CREDIT_LIMIT = 40;

    @Value("${tmdb.api.base-url}") private String baseUrl;
    @Value("${tmdb.api.token}") private String token;
    @Value("${tmdb.api.language:ko-KR}") private String language;
    @Value("${tmdb.api.region:KR}") private String region;

    private final TmdbDAO tmdbDAO;
    private final TmdbProviderCacheService tmdbProviderCacheService;
    private final HttpClient httpClient;
    private final Map<String, Integer> movieGenreMap;
    private final Map<String, Integer> tvGenreMap;

    public SearchServiceImpl(TmdbDAO tmdbDAO,
                             TmdbProviderCacheService tmdbProviderCacheService) {
        this.tmdbDAO = tmdbDAO;
        this.tmdbProviderCacheService = tmdbProviderCacheService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.movieGenreMap = createMovieGenreMap();
        this.tvGenreMap = createTvGenreMap();
    }

    @Override
    public SearchResultPageVO getPopularContent(
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        int currentPage = normalizePage(page);
        List<String> categories = normalizeContentCategories(contentCategories);
        List<String> genres = normalizeStringList(genreCodes);
        List<String> providers = normalizeStringList(providerIds);

        boolean hasCategoryFilter =
                contentCategories != null && !contentCategories.isEmpty();

        if (!hasCategoryFilter
                && genres.isEmpty()
                && providers.isEmpty()) {

            return attachAndFilterPlatforms(
                    callTrendingApi(currentPage),
                    providers
            );
        }

        SearchResultPageVO pageVO = callCategoryDiscoverApis(
                currentPage,
                categories,
                genres,
                providers
        );

        return attachAndFilterPlatforms(pageVO, providers);
    }

    @Override
    public SearchResultPageVO searchByTmdb(
            String keyword,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        int currentPage = normalizePage(page);
        String query = keyword == null ? "" : keyword.trim();

        List<String> categories =
                normalizeContentCategories(contentCategories);

        List<String> genres =
                normalizeStringList(genreCodes);

        List<String> providers =
                normalizeStringList(providerIds);

        if (query.isEmpty()) {
            return getPopularContent(
                    currentPage,
                    categories,
                    genres,
                    providers
            );
        }

        SearchResultPageVO titlePage =
                callTitleSearch(query, currentPage, categories);

        List<SearchResultVO> merged =
                new ArrayList<SearchResultVO>();

        if (titlePage.getResultList() != null) {
            merged.addAll(titlePage.getResultList());
        }

        if (currentPage == 1) {
            merged.addAll(searchPersonCredits(query));
        }

        merged = removeDuplicateResults(merged);
        merged = filterByContentCategories(merged, categories);
        merged = filterByGenreCodes(merged, genres);
        sortByPopularity(merged);

        SearchResultPageVO mergedPage =
                new SearchResultPageVO();

        mergedPage.setResultList(merged);
        mergedPage.setPage(currentPage);
        mergedPage.setTotalPages(titlePage.getTotalPages());
        mergedPage.setTotalResults(
                Math.max(titlePage.getTotalResults(), merged.size())
        );

        return attachAndFilterPlatforms(mergedPage, providers);
    }

    private SearchResultPageVO callTitleSearch(
            String keyword,
            int page,
            List<String> contentCategories) {

        if (contentCategories.size() == 1) {
            String category = contentCategories.get(0);

            if (CATEGORY_MOVIE.equals(category)) {
                return callMovieSearchApi(keyword, page);
            }

            if (CATEGORY_DRAMA.equals(category)
                    || CATEGORY_VARIETY.equals(category)) {
                return callTvSearchApi(keyword, page);
            }
        }

        return callMultiSearchApi(keyword, page);
    }

    private List<SearchResultVO> searchPersonCredits(String keyword) {
        List<SearchResultVO> result = new ArrayList<SearchResultVO>();
        String url = baseUrl + "/search/person"
                + "?query=" + encode(keyword)
                + "&language=" + encode(language)
                + "&page=1&include_adult=false";
        JSONObject root = callTmdbApi(url);
        JSONArray people = root.optJSONArray("results");
        if (people == null) return result;

        int personCount = Math.min(people.length(), PERSON_LIMIT);
        for (int i = 0; i < personCount; i++) {
            JSONObject person = people.optJSONObject(i);
            if (person == null) continue;
            long personId = person.optLong("id", 0L);
            String personName = person.optString("name", keyword);
            String department = person.optString("known_for_department", "Acting");
            if (personId <= 0) continue;

            String creditUrl = baseUrl + "/person/" + personId + "/combined_credits"
                    + "?language=" + encode(language);
            JSONObject creditRoot = callTmdbApi(creditUrl);
            addPersonCastCredits(result, creditRoot.optJSONArray("cast"), personName);
            addPersonCrewCredits(result, creditRoot.optJSONArray("crew"), personName, department);
        }
        return result;
    }

    private void addPersonCastCredits(List<SearchResultVO> target,
                                      JSONArray cast,
                                      String personName) {
        if (cast == null) return;
        int count = Math.min(cast.length(), PERSON_CREDIT_LIMIT);
        for (int i = 0; i < count; i++) {
            JSONObject item = cast.optJSONObject(i);
            if (item == null || shouldExcludeContent(item)) continue;
            SearchResultVO vo = convertCredit(item);
            if (vo == null) continue;
            vo.setMatchType("PERSON");
            vo.setMatchedPersonName(personName);
            vo.setMatchedPersonRole("배우");
            target.add(vo);
        }
    }

    private void addPersonCrewCredits(List<SearchResultVO> target,
                                      JSONArray crew,
                                      String personName,
                                      String department) {
        if (crew == null) return;
        int count = Math.min(crew.length(), PERSON_CREDIT_LIMIT);
        for (int i = 0; i < count; i++) {
            JSONObject item = crew.optJSONObject(i);
            if (item == null || shouldExcludeContent(item)) continue;
            String job = item.optString("job", "");
            String crewDepartment = item.optString("department", department);
            if (!"Director".equalsIgnoreCase(job)
                    && !"Directing".equalsIgnoreCase(crewDepartment)
                    && !"Creator".equalsIgnoreCase(job)) {
                continue;
            }
            SearchResultVO vo = convertCredit(item);
            if (vo == null) continue;
            vo.setMatchType("PERSON");
            vo.setMatchedPersonName(personName);
            vo.setMatchedPersonRole("감독");
            target.add(vo);
        }
    }

    private SearchResultVO convertCredit(JSONObject item) {
        String mediaType = item.optString("media_type", "");
        if ("movie".equalsIgnoreCase(mediaType)) return convertMovie(item);
        if ("tv".equalsIgnoreCase(mediaType)) return convertTv(item);
        return null;
    }

    private SearchResultPageVO callTrendingApi(int page) {
        String url = baseUrl + "/trending/all/week?language=" + encode(language) + "&page=" + page;
        JSONObject root = callTmdbApi(url);
        SearchResultPageVO pageVO = createPageInformation(root);
        List<SearchResultVO> list = new ArrayList<SearchResultVO>();
        JSONArray results = root.optJSONArray("results");
        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);
                if (item == null || shouldExcludeContent(item)) continue;
                SearchResultVO vo = convertCredit(item);
                if (vo != null) list.add(vo);
            }
        }
        pageVO.setResultList(list);
        return pageVO;
    }

    private SearchResultPageVO callMultiSearchApi(String keyword, int page) {
        String url = baseUrl + "/search/multi?query=" + encode(keyword)
                + "&language=" + encode(language)
                + "&region=" + encode(region)
                + "&page=" + page + "&include_adult=false";
        JSONObject root = callTmdbApi(url);
        SearchResultPageVO pageVO = createPageInformation(root);
        List<SearchResultVO> list = new ArrayList<SearchResultVO>();
        JSONArray results = root.optJSONArray("results");
        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);
                if (item == null || shouldExcludeContent(item)) continue;
                SearchResultVO vo = convertCredit(item);
                if (vo != null) list.add(vo);
            }
        }
        pageVO.setResultList(list);
        return pageVO;
    }

    private SearchResultPageVO callMovieSearchApi(String keyword, int page) {
        String url = baseUrl + "/search/movie?query=" + encode(keyword)
                + "&language=" + encode(language)
                + "&region=" + encode(region)
                + "&page=" + page + "&include_adult=false";
        return convertMoviePage(callTmdbApi(url));
    }

    private SearchResultPageVO callTvSearchApi(String keyword, int page) {
        String url = baseUrl + "/search/tv?query=" + encode(keyword)
                + "&language=" + encode(language)
                + "&page=" + page + "&include_adult=false";
        return convertTvPage(callTmdbApi(url));
    }

    private SearchResultPageVO callCategoryDiscoverApis(
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        List<SearchResultVO> merged =
                new ArrayList<SearchResultVO>();

        int totalPages = 0;
        int totalResults = 0;

        if (contentCategories.isEmpty()) {
            SearchResultPageVO moviePage = callDiscover(
                    "movie",
                    page,
                    buildGenreQuery(genreCodes, movieGenreMap),
                    providerIds
            );

            SearchResultPageVO tvPage = callDiscover(
                    "tv",
                    page,
                    buildGenreQuery(genreCodes, tvGenreMap),
                    providerIds
            );

            merged.addAll(moviePage.getResultList());
            merged.addAll(tvPage.getResultList());

            totalPages = Math.max(
                    moviePage.getTotalPages(),
                    tvPage.getTotalPages()
            );

            totalResults =
                    moviePage.getTotalResults()
                    + tvPage.getTotalResults();
        } else {
            for (String category : contentCategories) {
                List<SearchResultPageVO> partialPages =
                        callCategoryDiscover(category, page, providerIds);

                for (SearchResultPageVO partialPage : partialPages) {
                    if (partialPage == null) {
                        continue;
                    }

                    if (partialPage.getResultList() != null) {
                        merged.addAll(partialPage.getResultList());
                    }

                    totalPages = Math.max(
                            totalPages,
                            partialPage.getTotalPages()
                    );

                    totalResults += partialPage.getTotalResults();
                }
            }

            merged = filterByContentCategories(
                    merged,
                    contentCategories
            );

            merged = filterByGenreCodes(
                    merged,
                    genreCodes
            );
        }

        merged = removeDuplicateResults(merged);
        sortByPopularity(merged);

        SearchResultPageVO pageVO =
                new SearchResultPageVO();

        pageVO.setResultList(merged);
        pageVO.setPage(page);
        pageVO.setTotalPages(totalPages);
        pageVO.setTotalResults(totalResults);

        return pageVO;
    }

    private List<SearchResultPageVO> callCategoryDiscover(
            String category,
            int page,
            List<String> providerIds) {

        List<SearchResultPageVO> result =
                new ArrayList<SearchResultPageVO>();

        if (CATEGORY_MOVIE.equals(category)) {
            result.add(
                    callDiscover("movie", page, "", providerIds)
            );
            return result;
        }

        if (CATEGORY_DRAMA.equals(category)) {
            result.add(
                    callDiscover("tv", page, "18", providerIds)
            );
            return result;
        }

        if (CATEGORY_ANIMATION.equals(category)) {
            result.add(
                    callDiscover("movie", page, "16", providerIds)
            );
            result.add(
                    callDiscover("tv", page, "16", providerIds)
            );
            return result;
        }

        if (CATEGORY_VARIETY.equals(category)) {
            result.add(
                    callDiscover("tv", page, "10764|10767", providerIds)
            );
            return result;
        }

        if (CATEGORY_DOCUMENTARY.equals(category)) {
            result.add(
                    callDiscover("movie", page, "99", providerIds)
            );
            result.add(
                    callDiscover("tv", page, "99", providerIds)
            );
        }

        return result;
    }

    private SearchResultPageVO callDiscover(
            String media,
            int page,
            String genreQuery,
            List<String> providerIds) {

        StringBuilder url = new StringBuilder(baseUrl)
                .append("/discover/")
                .append(media)
                .append("?language=")
                .append(encode(language))
                .append("&watch_region=")
                .append(encode(region))
                .append("&sort_by=popularity.desc")
                .append("&include_adult=false")
                .append("&page=")
                .append(page);

        if ("movie".equals(media)) {
            url.append("&region=").append(encode(region));
        }

        String providers = buildProviderQuery(providerIds);

        if (genreQuery != null && !genreQuery.isEmpty()) {
            url.append("&with_genres=")
                    .append(encode(genreQuery));
        }

        if (!providers.isEmpty()) {
            url.append("&with_watch_providers=")
                    .append(encode(providers));

            url.append("&with_watch_monetization_types=flatrate");
        }

        JSONObject root = callTmdbApi(url.toString());

        return "movie".equals(media)
                ? convertMoviePage(root)
                : convertTvPage(root);
    }

    private SearchResultPageVO attachAndFilterPlatforms(SearchResultPageVO pageVO,
                                                        List<String> selectedProviderIds) {
        if (pageVO == null || pageVO.getResultList() == null) return pageVO;
        List<OttPlatformVO> activePlatforms = tmdbDAO.selectActivePlatformList();
        Map<String, OttPlatformVO> platformMap = createPlatformMap(activePlatforms);
        Set<String> selectedKeys = providerIdsToKeys(selectedProviderIds);
        List<SearchResultVO> filtered = new ArrayList<SearchResultVO>();

        for (SearchResultVO resultVO : pageVO.getResultList()) {
            if (resultVO == null || resultVO.getTmdbId() == null || resultVO.getContentType() == null) continue;
            List<OttPlatformVO> platforms = findPlatformList(
                    resultVO.getTmdbId(), resultVO.getContentType(), platformMap);
            if (platforms.isEmpty()) continue;
            if (!selectedKeys.isEmpty() && !containsSelectedPlatform(platforms, selectedKeys)) continue;
            resultVO.setPlatformList(platforms);
            filtered.add(resultVO);
        }
        pageVO.setResultList(filtered);
        return pageVO;
    }

    private boolean containsSelectedPlatform(List<OttPlatformVO> platforms,
                                             Set<String> selectedKeys) {
        for (OttPlatformVO platform : platforms) {
            if (platform != null && selectedKeys.contains(normalizePlatformName(platform.getPlatformName()))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> providerIdsToKeys(List<String> ids) {
        Set<String> keys = new HashSet<String>();
        if (ids == null) return keys;
        for (String id : ids) {
            if ("8".equals(id)) keys.add("netflix");
            else if ("1883".equals(id)) keys.add("tving");
            else if ("356".equals(id)) keys.add("wavve");
            else if ("337".equals(id)) keys.add("disney");
            else if ("97".equals(id)) keys.add("watcha");
            else if ("283".equals(id)) keys.add("coupang");
        }
        return keys;
    }

    private List<OttPlatformVO> findPlatformList(Long tmdbId,
                                                 String contentType,
                                                 Map<String, OttPlatformVO> platformMap) {
        List<OttPlatformVO> result = new ArrayList<OttPlatformVO>();
        JSONObject root = tmdbProviderCacheService.getWatchProviderResult(tmdbId, contentType);
        JSONObject results = root.optJSONObject("results");
        if (results == null) return result;
        JSONObject korea = results.optJSONObject(region);
        if (korea == null) return result;
        Set<Integer> duplicate = new HashSet<Integer>();
        addProviders(korea.optJSONArray("flatrate"), platformMap, duplicate, result);
        addProviders(korea.optJSONArray("ads"), platformMap, duplicate, result);
        addProviders(korea.optJSONArray("free"), platformMap, duplicate, result);
        return result;
    }

    private void addProviders(JSONArray array,
                              Map<String, OttPlatformVO> platformMap,
                              Set<Integer> duplicate,
                              List<OttPlatformVO> result) {
        if (array == null) return;
        for (int i = 0; i < array.length(); i++) {
            JSONObject provider = array.optJSONObject(i);
            if (provider == null) continue;
            OttPlatformVO platform = platformMap.get(
                    normalizePlatformName(provider.optString("provider_name", "")));
            if (platform == null) continue;
            Integer no = platform.getPlatformNo();
            if (no == null || duplicate.add(no)) result.add(platform);
        }
    }

    private Map<String, OttPlatformVO> createPlatformMap(List<OttPlatformVO> list) {
        Map<String, OttPlatformVO> map = new HashMap<String, OttPlatformVO>();
        if (list == null) return map;
        for (OttPlatformVO platform : list) {
            if (platform != null && platform.getPlatformName() != null) {
                map.put(normalizePlatformName(platform.getPlatformName()), platform);
            }
        }
        return map;
    }

    private String normalizePlatformName(String name) {
        if (name == null) return "";
        String normalized = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (normalized.contains("netflix")) return "netflix";
        if (normalized.contains("tving")) return "tving";
        if (normalized.contains("wavve")) return "wavve";
        if (normalized.contains("disney")) return "disney";
        if (normalized.contains("watcha")) return "watcha";
        if (normalized.contains("coupang")) return "coupang";
        return normalized;
    }

    private SearchResultPageVO convertMoviePage(JSONObject root) {
        SearchResultPageVO pageVO = createPageInformation(root);
        List<SearchResultVO> list = new ArrayList<SearchResultVO>();
        JSONArray results = root.optJSONArray("results");
        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);
                if (item != null && !shouldExcludeContent(item)) list.add(convertMovie(item));
            }
        }
        pageVO.setResultList(list);
        return pageVO;
    }

    private SearchResultPageVO convertTvPage(JSONObject root) {
        SearchResultPageVO pageVO = createPageInformation(root);
        List<SearchResultVO> list = new ArrayList<SearchResultVO>();
        JSONArray results = root.optJSONArray("results");
        if (results != null) {
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.optJSONObject(i);
                if (item != null && !shouldExcludeContent(item)) list.add(convertTv(item));
            }
        }
        pageVO.setResultList(list);
        return pageVO;
    }

    private SearchResultPageVO createPageInformation(JSONObject root) {
        SearchResultPageVO pageVO = new SearchResultPageVO();
        pageVO.setPage(root.optInt("page", 1));
        pageVO.setTotalPages(root.optInt("total_pages", 0));
        pageVO.setTotalResults(root.optInt("total_results", 0));
        return pageVO;
    }

    private SearchResultVO convertMovie(JSONObject item) {
        SearchResultVO vo = new SearchResultVO();
        vo.setTmdbId(nullableLong(item, "id"));
        vo.setContentType(MOVIE);
        vo.setTitle(nullableString(item, "title"));
        vo.setOriginalTitle(nullableString(item, "original_title"));
        vo.setOverview(nullableString(item, "overview"));
        vo.setPosterPath(nullableString(item, "poster_path"));
        vo.setBackdropPath(nullableString(item, "backdrop_path"));
        vo.setReleaseDate(nullableString(item, "release_date"));
        vo.setTmdbScore(nullableDouble(item, "vote_average"));
        vo.setPopularity(nullableDouble(item, "popularity"));
        vo.setGenreText(joinGenreNames(item.optJSONArray("genre_ids"), movieGenreMap));
        vo.setMatchType("TITLE");
        return vo;
    }

    private SearchResultVO convertTv(JSONObject item) {
        SearchResultVO vo = new SearchResultVO();
        vo.setTmdbId(nullableLong(item, "id"));
        vo.setContentType(TV);
        vo.setTitle(nullableString(item, "name"));
        vo.setOriginalTitle(nullableString(item, "original_name"));
        vo.setOverview(nullableString(item, "overview"));
        vo.setPosterPath(nullableString(item, "poster_path"));
        vo.setBackdropPath(nullableString(item, "backdrop_path"));
        vo.setReleaseDate(nullableString(item, "first_air_date"));
        vo.setTmdbScore(nullableDouble(item, "vote_average"));
        vo.setPopularity(nullableDouble(item, "popularity"));
        vo.setGenreText(joinGenreNames(item.optJSONArray("genre_ids"), tvGenreMap));
        vo.setMatchType("TITLE");
        return vo;
    }

    private List<SearchResultVO> filterByContentCategories(
            List<SearchResultVO> list,
            List<String> contentCategories) {

        if (contentCategories == null
                || contentCategories.isEmpty()) {
            return list;
        }

        List<SearchResultVO> result =
                new ArrayList<SearchResultVO>();

        for (SearchResultVO vo : list) {
            if (vo == null) {
                continue;
            }

            for (String category : contentCategories) {
                if (matchesContentCategory(vo, category)) {
                    result.add(vo);
                    break;
                }
            }
        }

        return result;
    }

    private boolean matchesContentCategory(
            SearchResultVO vo,
            String category) {

        String contentType =
                vo.getContentType() == null
                        ? ""
                        : vo.getContentType().toUpperCase();

        String genreText =
                vo.getGenreText() == null
                        ? ""
                        : vo.getGenreText();

        boolean animation =
                genreText.contains("애니메이션");

        boolean documentary =
                genreText.contains("다큐멘터리");

        boolean variety =
                genreText.contains("리얼리티")
                || genreText.contains("토크");

        if (CATEGORY_MOVIE.equals(category)) {
            return MOVIE.equals(contentType)
                    && !animation
                    && !documentary;
        }

        if (CATEGORY_DRAMA.equals(category)) {
            return TV.equals(contentType)
                    && genreText.contains("드라마")
                    && !animation
                    && !documentary
                    && !variety;
        }

        if (CATEGORY_ANIMATION.equals(category)) {
            return animation;
        }

        if (CATEGORY_VARIETY.equals(category)) {
            return TV.equals(contentType)
                    && variety;
        }

        if (CATEGORY_DOCUMENTARY.equals(category)) {
            return documentary;
        }

        return false;
    }

    private List<SearchResultVO> filterByGenreCodes(List<SearchResultVO> list,
                                                    List<String> genreCodes) {
        if (genreCodes == null || genreCodes.isEmpty()) return list;
        Set<Integer> movieIds = genreCodeSet(genreCodes, movieGenreMap);
        Set<Integer> tvIds = genreCodeSet(genreCodes, tvGenreMap);
        List<SearchResultVO> result = new ArrayList<SearchResultVO>();
        for (SearchResultVO vo : list) {
            String genreText = vo.getGenreText();
            if (genreText == null || genreText.isEmpty()) continue;
            Set<Integer> targetIds = MOVIE.equals(vo.getContentType()) ? movieIds : tvIds;
            if (containsAnyGenreName(genreText, targetIds,
                    MOVIE.equals(vo.getContentType()) ? movieGenreMap : tvGenreMap)) {
                result.add(vo);
            }
        }
        return result;
    }

    private boolean containsAnyGenreName(String genreText,
                                         Set<Integer> ids,
                                         Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (ids.contains(entry.getValue()) && genreText.contains(displayGenreName(entry.getKey()))) return true;
        }
        return false;
    }

    private Set<Integer> genreCodeSet(List<String> codes, Map<String, Integer> map) {
        Set<Integer> result = new HashSet<Integer>();
        for (String code : codes) {
            Integer id = map.get(code.toUpperCase());
            if (id != null) result.add(id);
        }
        return result;
    }

    private String joinGenreNames(JSONArray ids, Map<String, Integer> map) {
        if (ids == null) return null;
        List<String> names = new ArrayList<String>();
        for (int i = 0; i < ids.length(); i++) {
            int id = ids.optInt(i, -1);
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == id) {
                    String name = displayGenreName(entry.getKey());
                    if (!names.contains(name)) names.add(name);
                    break;
                }
            }
        }
        return String.join(", ", names);
    }

    private String displayGenreName(String code) {
        Map<String, String> names = new HashMap<String, String>();
        names.put("ACTION", "액션"); names.put("ADVENTURE", "모험"); names.put("ANIMATION", "애니메이션");
        names.put("COMEDY", "코미디"); names.put("CRIME", "범죄"); names.put("DOCUMENTARY", "다큐멘터리");
        names.put("DRAMA", "드라마"); names.put("FAMILY", "가족"); names.put("FANTASY", "판타지");
        names.put("HISTORY", "역사"); names.put("HORROR", "공포"); names.put("MUSIC", "음악");
        names.put("MYSTERY", "미스터리"); names.put("ROMANCE", "로맨스"); names.put("SCI_FI", "SF");
        names.put("THRILLER", "스릴러"); names.put("WAR", "전쟁"); names.put("WESTERN", "서부");
        names.put("KIDS", "키즈"); names.put("NEWS", "뉴스"); names.put("REALITY", "리얼리티");
        names.put("SOAP", "연속극"); names.put("TALK", "토크");
        return names.getOrDefault(code, code);
    }

    private String buildGenreQuery(List<String> codes, Map<String, Integer> map) {
        List<String> ids = new ArrayList<String>();
        for (String code : codes) {
            Integer id = map.get(code.toUpperCase());
            if (id != null && !ids.contains(String.valueOf(id))) ids.add(String.valueOf(id));
        }
        return String.join("|", ids);
    }

    private String buildProviderQuery(List<String> ids) {
        List<String> result = new ArrayList<String>();
        for (String id : ids) {
            try {
                Integer.parseInt(id);
                if (!result.contains(id)) result.add(id);
            } catch (NumberFormatException ignored) {}
        }
        return String.join("|", result);
    }

    private List<String> normalizeContentCategories(
            List<String> categories) {

        List<String> result =
                new ArrayList<String>();

        if (categories == null) {
            return result;
        }

        for (String category : categories) {
            if (category == null) {
                continue;
            }

            String normalized =
                    category.trim().toUpperCase();

            if ((CATEGORY_MOVIE.equals(normalized)
                    || CATEGORY_DRAMA.equals(normalized)
                    || CATEGORY_ANIMATION.equals(normalized)
                    || CATEGORY_VARIETY.equals(normalized)
                    || CATEGORY_DOCUMENTARY.equals(normalized))
                    && !result.contains(normalized)) {

                result.add(normalized);
            }
        }

        return result;
    }

    private List<String> normalizeStringList(List<String> values) {
        List<String> result = new ArrayList<String>();
        if (values == null) return result;
        for (String value : values) {
            if (value != null && !value.trim().isEmpty() && !result.contains(value.trim())) result.add(value.trim());
        }
        return result;
    }

    private List<SearchResultVO> removeDuplicateResults(List<SearchResultVO> list) {
        List<SearchResultVO> result = new ArrayList<SearchResultVO>();
        Map<String, SearchResultVO> map = new HashMap<String, SearchResultVO>();
        for (SearchResultVO vo : list) {
            if (vo == null || vo.getTmdbId() == null || vo.getContentType() == null) continue;
            String key = vo.getContentType() + "_" + vo.getTmdbId();
            SearchResultVO old = map.get(key);
            if (old == null) {
                map.put(key, vo); result.add(vo);
            } else if ("PERSON".equals(vo.getMatchType()) && !"PERSON".equals(old.getMatchType())) {
                old.setMatchType("PERSON");
                old.setMatchedPersonName(vo.getMatchedPersonName());
                old.setMatchedPersonRole(vo.getMatchedPersonRole());
            }
        }
        return result;
    }

    private void sortByPopularity(List<SearchResultVO> list) {
        list.sort((a, b) -> Double.compare(
                b.getPopularity() == null ? 0.0 : b.getPopularity(),
                a.getPopularity() == null ? 0.0 : a.getPopularity()));
    }

    private JSONObject callTmdbApi(String apiUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/json")
                .GET().build();
        try {
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) return new JSONObject();
            return response.body() == null || response.body().isBlank()
                    ? new JSONObject() : new JSONObject(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new JSONObject();
        } catch (IOException | IllegalArgumentException e) {
            return new JSONObject();
        }
    }

    private boolean shouldExcludeContent(JSONObject item) {
        return item == null || item.optBoolean("adult", false);
    }

    private int normalizePage(int page) { return page <= 0 ? 1 : page; }
    private String encode(String value) { return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8); }
    private String nullableString(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return null;
        String value = o.optString(key, null);
        return value == null || value.trim().isEmpty() ? null : value;
    }
    private Long nullableLong(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return null;
        return o.optLong(key);
    }
    private Double nullableDouble(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) return null;
        return o.optDouble(key);
    }

    private Map<String, Integer> createMovieGenreMap() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("ACTION",28); map.put("ADVENTURE",12); map.put("ANIMATION",16); map.put("COMEDY",35);
        map.put("CRIME",80); map.put("DOCUMENTARY",99); map.put("DRAMA",18); map.put("FAMILY",10751);
        map.put("FANTASY",14); map.put("HISTORY",36); map.put("HORROR",27); map.put("MUSIC",10402);
        map.put("MYSTERY",9648); map.put("ROMANCE",10749); map.put("SCI_FI",878); map.put("THRILLER",53);
        map.put("WAR",10752); map.put("WESTERN",37);
        return map;
    }

    private Map<String, Integer> createTvGenreMap() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("ACTION",10759); map.put("ADVENTURE",10759); map.put("ANIMATION",16); map.put("COMEDY",35);
        map.put("CRIME",80); map.put("DOCUMENTARY",99); map.put("DRAMA",18); map.put("FAMILY",10751);
        map.put("KIDS",10762); map.put("MYSTERY",9648); map.put("NEWS",10763); map.put("REALITY",10764);
        map.put("SCI_FI",10765); map.put("FANTASY",10765); map.put("SOAP",10766); map.put("TALK",10767);
        map.put("WAR",10768); map.put("WESTERN",37); map.put("THRILLER",9648); map.put("ROMANCE",18);
        map.put("HORROR",9648);
        return map;
    }
}
