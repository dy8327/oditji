package com.project.oditji.content.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.search.vo.SearchResultVO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Service
public class ContentListTmdbService {

    private static final int DISPLAY_PAGE_SIZE = 20;
    private static final int DISPLAY_COUNT_PER_MEDIA = 10;
    private static final int TMDB_PAGE_SIZE = 20;
    private static final int MAX_TMDB_PAGE = 500;

    private static final Map<Integer, String> MOVIE_GENRES =
            createMovieGenreMap();

    private static final Map<Integer, String> TV_GENRES =
            createTvGenreMap();

    private final RestTemplate restTemplate;
    private final JsonMapper jsonMapper;

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String tmdbApiBaseUrl;

    @Value("${tmdb.api.language:ko-KR}")
    private String tmdbApiLanguage;

    @Value("${tmdb.api.region:KR}")
    private String tmdbApiRegion;

    public ContentListTmdbService(JsonMapper jsonMapper) {
        this.restTemplate = new RestTemplate();
        this.jsonMapper = jsonMapper;
    }

    public ContentListPageVO getContentListPage(
            String type,
            int page,
            List<String> contentCategories,
            List<String> genreCodes,
            List<String> providerIds) {

        String normalizedType = normalizeListType(type);
        int safePage = page <= 0 ? 1 : page;

        int zeroBasedMediaOffset =
                (safePage - 1) * DISPLAY_COUNT_PER_MEDIA;

        int tmdbPage =
                (zeroBasedMediaOffset / TMDB_PAGE_SIZE) + 1;

        int startIndex =
                zeroBasedMediaOffset % TMDB_PAGE_SIZE;

        if (tmdbPage > MAX_TMDB_PAGE) {
            return createEmptyPage(safePage);
        }

        List<String> normalizedCategories =
                normalizeUpperList(contentCategories);

        List<String> normalizedGenres =
                normalizeUpperList(genreCodes);

        List<String> normalizedProviders =
                normalizeProviderList(providerIds);

        DiscoverPage moviePage = shouldIncludeMovie(normalizedCategories)
                ? requestDiscoverPage(
                        "movie",
                        "MOVIE",
                        normalizedType,
                        tmdbPage,
                        normalizedCategories,
                        normalizedGenres,
                        normalizedProviders)
                : new DiscoverPage();

        DiscoverPage tvPage = shouldIncludeTv(normalizedCategories)
                ? requestDiscoverPage(
                        "tv",
                        "TV",
                        normalizedType,
                        tmdbPage,
                        normalizedCategories,
                        normalizedGenres,
                        normalizedProviders)
                : new DiscoverPage();

        List<SearchResultVO> combined =
                new ArrayList<SearchResultVO>();

        combined.addAll(slice(
                moviePage.getResultList(),
                startIndex,
                DISPLAY_COUNT_PER_MEDIA));

        combined.addAll(slice(
                tvPage.getResultList(),
                startIndex,
                DISPLAY_COUNT_PER_MEDIA));

        sortCombinedList(combined, normalizedType);

        if (combined.size() > DISPLAY_PAGE_SIZE) {
            combined = new ArrayList<SearchResultVO>(
                    combined.subList(0, DISPLAY_PAGE_SIZE));
        }

        int movieDisplayPages = calculateDisplayPages(
                moviePage.getTotalResults());

        int tvDisplayPages = calculateDisplayPages(
                tvPage.getTotalResults());

        int totalPages = Math.max(
                movieDisplayPages,
                tvDisplayPages);

        int totalResults =
                safeAdd(
                        moviePage.getTotalResults(),
                        tvPage.getTotalResults());

        ContentListPageVO pageVO =
                new ContentListPageVO();

        pageVO.setContentList(combined);
        pageVO.setCurrentPage(safePage);
        pageVO.setTotalPages(totalPages);
        pageVO.setTotalResults(totalResults);

        return pageVO;
    }

    public List<SearchResultVO> getRecommendedList(
            List<String> providerIds) {

        ContentListPageVO pageVO = getContentListPage(
                "popular",
                1,
                Collections.emptyList(),
                Collections.emptyList(),
                providerIds);

        List<SearchResultVO> list =
                pageVO.getContentList();

        if (list.size() <= 5) {
            return list;
        }

        return new ArrayList<SearchResultVO>(
                list.subList(0, 5));
    }

    private DiscoverPage requestDiscoverPage(
            String apiType,
            String contentType,
            String listType,
            int tmdbPage,
            List<String> categories,
            List<String> genres,
            List<String> providers) {

        String providerIdText =
                resolveProviderIdText(
                        apiType,
                        providers);

        if (providerIdText.isBlank()) {
            return new DiscoverPage();
        }

        StringBuilder url =
                new StringBuilder();

        url.append(tmdbApiBaseUrl)
                .append("/discover/")
                .append(apiType)
                .append("?language=")
                .append(encode(tmdbApiLanguage))
                .append("&region=")
                .append(encode(tmdbApiRegion))
                .append("&watch_region=")
                .append(encode(tmdbApiRegion))
                .append("&include_adult=false")
                .append("&with_watch_monetization_types=flatrate")
                .append("&with_watch_providers=")
                .append(encode(providerIdText))
                .append("&page=")
                .append(tmdbPage);

        if ("movie".equals(apiType)) {
            url.append("&include_video=false");
        }

        appendSortAndDateCondition(
                url,
                apiType,
                listType);

        JsonNode root =
                callTmdbApi(url.toString());

        JsonNode results =
                root.path("results");

        DiscoverPage page =
                new DiscoverPage();

        page.setTotalResults(
                Math.max(
                        0,
                        root.path("total_results").asInt()));

        page.setTotalPages(
                Math.min(
                        MAX_TMDB_PAGE,
                        Math.max(
                                0,
                                root.path("total_pages").asInt())));

        if (!results.isArray()) {
            return page;
        }

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        for (JsonNode item : results) {

            if (shouldExcludeContent(item)) {
                continue;
            }

            JsonNode genreIds =
                    item.path("genre_ids");

            if (!matchesCategory(
                    contentType,
                    genreIds,
                    categories)) {
                continue;
            }

            if (!matchesGenre(
                    contentType,
                    genreIds,
                    genres)) {
                continue;
            }

            SearchResultVO result =
                    createSearchResultVO(
                            item,
                            contentType);

            if (result != null) {
                resultList.add(result);
            }
        }

        page.setResultList(resultList);

        return page;
    }

    private void appendSortAndDateCondition(
            StringBuilder url,
            String apiType,
            String listType) {

        if ("new".equals(listType)) {

            String today =
                    LocalDate.now().toString();

            if ("movie".equals(apiType)) {
                url.append("&sort_by=primary_release_date.desc")
                        .append("&primary_release_date.lte=")
                        .append(today)
                        .append("&vote_count.gte=1");
            } else {
                url.append("&sort_by=first_air_date.desc")
                        .append("&first_air_date.lte=")
                        .append(today)
                        .append("&vote_count.gte=1");
            }

            return;
        }

        if ("all".equals(listType)) {
            url.append("&sort_by=vote_average.desc")
                    .append("&vote_count.gte=100");

            return;
        }

        url.append("&sort_by=popularity.desc");
    }

    private String resolveProviderIdText(
            String apiType,
            List<String> selectedProviders) {

        Set<String> providerIds =
                new LinkedHashSet<String>();

        String url = tmdbApiBaseUrl
                + "/watch/providers/"
                + apiType
                + "?language="
                + encode(tmdbApiLanguage)
                + "&watch_region="
                + encode(tmdbApiRegion);

        JsonNode results =
                callTmdbApi(url).path("results");

        if (!results.isArray()) {
            return "";
        }

        for (JsonNode provider : results) {

            String convertedName =
                    convertProviderName(
                            provider.path("provider_name")
                                    .asString(null));

            int providerId =
                    provider.path("provider_id").asInt();

            if (convertedName == null
                    || providerId <= 0) {
                continue;
            }

            if (selectedProviders != null
                    && !selectedProviders.isEmpty()
                    && !selectedProviders.contains(
                            convertedName)) {
                continue;
            }

            providerIds.add(
                    String.valueOf(providerId));
        }

        return String.join("|", providerIds);
    }

    private SearchResultVO createSearchResultVO(
            JsonNode item,
            String contentType) {

        long tmdbId =
                item.path("id").asLong();

        if (tmdbId <= 0) {
            return null;
        }

        SearchResultVO vo =
                new SearchResultVO();

        vo.setTmdbId(tmdbId);
        vo.setContentType(contentType);

        if ("MOVIE".equals(contentType)) {

            vo.setTitle(firstNonBlank(
                    item.path("title").asString(null),
                    item.path("original_title").asString(null)));

            vo.setOriginalTitle(
                    item.path("original_title")
                            .asString(null));

            vo.setReleaseDate(
                    item.path("release_date")
                            .asString(null));

        } else {

            vo.setTitle(firstNonBlank(
                    item.path("name").asString(null),
                    item.path("original_name").asString(null)));

            vo.setOriginalTitle(
                    item.path("original_name")
                            .asString(null));

            vo.setReleaseDate(
                    item.path("first_air_date")
                            .asString(null));
        }

        vo.setOverview(
                item.path("overview").asString(null));

        vo.setPosterPath(
                item.path("poster_path").asString(null));

        vo.setBackdropPath(
                item.path("backdrop_path").asString(null));

        vo.setGenreText(
                convertGenreIdsToText(
                        item.path("genre_ids"),
                        contentType));

        vo.setTmdbScore(
                nullableDouble(
                        item.path("vote_average")));

        vo.setPopularity(
                nullableDouble(
                        item.path("popularity")));

        return vo;
    }

    private boolean matchesCategory(
            String contentType,
            JsonNode genreIds,
            List<String> categories) {

        if (categories == null
                || categories.isEmpty()) {
            return true;
        }

        for (String category : categories) {

            if ("MOVIE".equals(category)
                    && "MOVIE".equals(contentType)
                    && !containsGenreId(genreIds, 16)
                    && !containsGenreId(genreIds, 99)) {
                return true;
            }

            if ("ANIMATION".equals(category)
                    && containsGenreId(genreIds, 16)) {
                return true;
            }

            if ("DOCUMENTARY".equals(category)
                    && containsGenreId(genreIds, 99)) {
                return true;
            }

            if ("TV".equals(contentType)
                    && "VARIETY".equals(category)
                    && (containsGenreId(genreIds, 10764)
                    || containsGenreId(genreIds, 10767))) {
                return true;
            }

            if ("TV".equals(contentType)
                    && "DRAMA".equals(category)
                    && !containsGenreId(genreIds, 16)
                    && !containsGenreId(genreIds, 99)
                    && !containsGenreId(genreIds, 10764)
                    && !containsGenreId(genreIds, 10767)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesGenre(
            String contentType,
            JsonNode genreIds,
            List<String> genres) {

        if (genres == null || genres.isEmpty()) {
            return true;
        }

        for (String genre : genres) {

            Integer genreId =
                    resolveGenreId(
                            contentType,
                            genre);

            if (genreId != null
                    && containsGenreId(
                            genreIds,
                            genreId)) {
                return true;
            }

            if ("TV".equals(contentType)
                    && "THRILLER".equals(genre)
                    && (containsGenreId(genreIds, 80)
                    || containsGenreId(genreIds, 9648)
                    || containsGenreId(genreIds, 10765))) {
                return true;
            }

            if ("TV".equals(contentType)
                    && "ROMANCE".equals(genre)
                    && (containsGenreId(genreIds, 18)
                    || containsGenreId(genreIds, 10766))) {
                return true;
            }

            if ("TV".equals(contentType)
                    && "HORROR".equals(genre)
                    && (containsGenreId(genreIds, 9648)
                    || containsGenreId(genreIds, 10765))) {
                return true;
            }
        }

        return false;
    }

    private Integer resolveGenreId(
            String contentType,
            String genre) {

        if ("MOVIE".equals(contentType)) {
            return switch (genre) {
                case "ACTION" -> 28;
                case "ADVENTURE" -> 12;
                case "COMEDY" -> 35;
                case "CRIME" -> 80;
                case "FAMILY" -> 10751;
                case "FANTASY" -> 14;
                case "HISTORY" -> 36;
                case "HORROR" -> 27;
                case "MUSIC" -> 10402;
                case "MYSTERY" -> 9648;
                case "ROMANCE" -> 10749;
                case "SCI_FI", "SF" -> 878;
                case "THRILLER" -> 53;
                case "WAR" -> 10752;
                case "WESTERN" -> 37;
                default -> null;
            };
        }

        return switch (genre) {
            case "ACTION", "ADVENTURE" -> 10759;
            case "COMEDY" -> 35;
            case "CRIME" -> 80;
            case "FAMILY" -> 10751;
            case "FANTASY", "SCI_FI", "SF" -> 10765;
            case "MYSTERY" -> 9648;
            case "WAR" -> 10768;
            case "WESTERN" -> 37;
            default -> null;
        };
    }

    private boolean shouldIncludeMovie(
            List<String> categories) {

        if (categories == null
                || categories.isEmpty()) {
            return true;
        }

        return categories.contains("MOVIE")
                || categories.contains("ANIMATION")
                || categories.contains("DOCUMENTARY");
    }

    private boolean shouldIncludeTv(
            List<String> categories) {

        if (categories == null
                || categories.isEmpty()) {
            return true;
        }

        return categories.contains("DRAMA")
                || categories.contains("ANIMATION")
                || categories.contains("VARIETY")
                || categories.contains("DOCUMENTARY");
    }

    private void sortCombinedList(
            List<SearchResultVO> list,
            String type) {

        Comparator<SearchResultVO> comparator;

        if ("new".equals(type)) {

            comparator = Comparator.comparing(
                    SearchResultVO::getReleaseDate,
                    Comparator.nullsLast(
                            Comparator.reverseOrder()));

        } else if ("all".equals(type)) {

            comparator = Comparator.comparing(
                    SearchResultVO::getTmdbScore,
                    Comparator.nullsLast(
                            Comparator.reverseOrder()))
                    .thenComparing(
                            SearchResultVO::getPopularity,
                            Comparator.nullsLast(
                                    Comparator.reverseOrder()));

        } else {

            comparator = Comparator.comparing(
                    SearchResultVO::getPopularity,
                    Comparator.nullsLast(
                            Comparator.reverseOrder()));
        }

        list.sort(comparator);
    }

    private List<SearchResultVO> slice(
            List<SearchResultVO> source,
            int startIndex,
            int size) {

        if (source == null
                || source.isEmpty()
                || startIndex >= source.size()) {
            return new ArrayList<SearchResultVO>();
        }

        int endIndex = Math.min(
                source.size(),
                startIndex + size);

        return new ArrayList<SearchResultVO>(
                source.subList(
                        startIndex,
                        endIndex));
    }

    private int calculateDisplayPages(
            int totalResults) {

        if (totalResults <= 0) {
            return 0;
        }

        int accessibleResults = Math.min(
                totalResults,
                MAX_TMDB_PAGE * TMDB_PAGE_SIZE);

        return (int) Math.ceil(
                accessibleResults
                        / (double) DISPLAY_COUNT_PER_MEDIA);
    }

    private int safeAdd(
            int first,
            int second) {

        long result =
                (long) first + second;

        return result > Integer.MAX_VALUE
                ? Integer.MAX_VALUE
                : (int) result;
    }

    private ContentListPageVO createEmptyPage(
            int page) {

        ContentListPageVO pageVO =
                new ContentListPageVO();

        pageVO.setCurrentPage(page);

        return pageVO;
    }

    private String normalizeListType(String type) {

        String value = type == null
                ? "all"
                : type.trim()
                        .toLowerCase(Locale.ROOT);

        if ("popular".equals(value)
                || "new".equals(value)) {
            return value;
        }

        return "all";
    }

    private List<String> normalizeUpperList(
            List<String> values) {

        List<String> result =
                new ArrayList<String>();

        if (values == null) {
            return result;
        }

        for (String value : values) {

            if (value == null
                    || value.isBlank()) {
                continue;
            }

            String normalized =
                    value.trim()
                            .toUpperCase(Locale.ROOT);

            if (!result.contains(normalized)) {
                result.add(normalized);
            }
        }

        return result;
    }

    private List<String> normalizeProviderList(
            List<String> values) {

        List<String> result =
                new ArrayList<String>();

        if (values == null) {
            return result;
        }

        for (String value : values) {

            String normalized =
                    convertProviderName(value);

            if (normalized != null
                    && !result.contains(normalized)) {
                result.add(normalized);
            }
        }

        return result;
    }

    private String convertProviderName(String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        String normalized =
                name.trim()
                        .toLowerCase(Locale.ROOT)
                        .replace(" ", "")
                        .replace("_", "")
                        .replace("-", "")
                        .replace("+", "");

        if (normalized.contains("netflix")) {
            return "Netflix";
        }

        if (normalized.contains("tving")) {
            return "Tving";
        }

        if (normalized.contains("wavve")) {
            return "Wavve";
        }

        if (normalized.contains("disney")) {
            return "Disney+";
        }

        if (normalized.contains("watcha")) {
            return "Watcha";
        }

        if (normalized.contains("coupang")) {
            return "Coupangplay";
        }

        return null;
    }

    private boolean containsGenreId(
            JsonNode genreIds,
            int genreId) {

        if (genreIds == null
                || !genreIds.isArray()) {
            return false;
        }

        for (JsonNode node : genreIds) {
            if (node.asInt() == genreId) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldExcludeContent(
            JsonNode item) {

        if (item == null
                || item.isNull()
                || item.path("adult")
                        .asBoolean(false)) {
            return true;
        }

        String title =
                firstNonBlank(
                        item.path("title")
                                .asString(null),
                        item.path("name")
                                .asString(null));

        String originalTitle =
                firstNonBlank(
                        item.path("original_title")
                                .asString(null),
                        item.path("original_name")
                                .asString(null));

        String overview =
                item.path("overview")
                        .asString("");

        String checkText =
                normalizeBlockedText(
                        (title == null ? "" : title)
                        + " "
                        + (originalTitle == null
                                ? ""
                                : originalTitle)
                        + " "
                        + overview);

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

        for (String keyword : blockedKeywords) {

            if (checkText.contains(
                    normalizeBlockedText(keyword))) {
                return true;
            }
        }

        return false;
    }

    private String normalizeBlockedText(String value) {

        if (value == null) {
            return "";
        }

        return value.toLowerCase(Locale.ROOT)
                .replaceAll(
                        "[^\\p{L}\\p{N}]",
                        "");
    }

    private String convertGenreIdsToText(
            JsonNode genreIds,
            String contentType) {

        if (genreIds == null
                || !genreIds.isArray()) {
            return null;
        }

        List<String> names =
                new ArrayList<String>();

        Map<Integer, String> genreMap =
                "MOVIE".equals(contentType)
                        ? MOVIE_GENRES
                        : TV_GENRES;

        for (JsonNode node : genreIds) {

            String name =
                    genreMap.get(node.asInt());

            if (name != null
                    && !names.contains(name)) {
                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private static Map<Integer, String> createMovieGenreMap() {

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
        map.put(53, "스릴러");
        map.put(10752, "전쟁");
        map.put(37, "서부");

        return Collections.unmodifiableMap(map);
    }

    private static Map<Integer, String> createTvGenreMap() {

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

    private Double nullableDouble(
            JsonNode node) {

        if (node == null
                || node.isMissingNode()
                || node.isNull()) {
            return null;
        }

        return node.asDouble();
    }

    private String firstNonBlank(
            String first,
            String second) {

        return first != null
                && !first.isBlank()
                ? first
                : second;
    }

    private String encode(String value) {

        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8);
    }

    private JsonNode callTmdbApi(String url) {

        try {

            HttpHeaders headers =
                    new HttpHeaders();

            headers.setBearerAuth(token);

            HttpEntity<String> entity =
                    new HttpEntity<String>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class);

            return jsonMapper.readTree(
                    response.getBody());

        } catch (Exception e) {

            throw new IllegalStateException(
                    "TMDB API 호출 실패: "
                    + url,
                    e);
        }
    }

    private static class DiscoverPage {

        private List<SearchResultVO> resultList;
        private int totalPages;
        private int totalResults;

        DiscoverPage() {
            this.resultList =
                    new ArrayList<SearchResultVO>();
        }

        public List<SearchResultVO> getResultList() {
            return resultList;
        }

        public void setResultList(
                List<SearchResultVO> resultList) {
            this.resultList = resultList;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public int getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(int totalResults) {
            this.totalResults = totalResults;
        }
    }
}
