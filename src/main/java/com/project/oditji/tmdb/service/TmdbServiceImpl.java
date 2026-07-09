package com.project.oditji.tmdb.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.TmdbVO;

@Service
public class TmdbServiceImpl implements TmdbService {

    private static final int TV_GENRE_ANIMATION = 16;
    private static final int TV_GENRE_DOCUMENTARY = 99;
    private static final int TV_GENRE_REALITY = 10764;
    private static final int TV_GENRE_TALK = 10767;

    private static final int MOVIE_GENRE_ACTION = 28;
    private static final int MOVIE_GENRE_COMEDY = 35;
    private static final int MOVIE_GENRE_THRILLER = 53;
    private static final int MOVIE_GENRE_ROMANCE = 10749;
    private static final int MOVIE_GENRE_CRIME = 80;
    private static final int MOVIE_GENRE_FANTASY = 14;
    private static final int MOVIE_GENRE_HORROR = 27;
    private static final int MOVIE_GENRE_MYSTERY = 9648;
    private static final int MOVIE_GENRE_SF = 878;
    private static final int MOVIE_GENRE_DRAMA = 18;

    private static final int TV_GENRE_ACTION_ADVENTURE = 10759;
    private static final int TV_GENRE_COMEDY = 35;
    private static final int TV_GENRE_CRIME = 80;
    private static final int TV_GENRE_DRAMA = 18;
    private static final int TV_GENRE_MYSTERY = 9648;
    private static final int TV_GENRE_SF_FANTASY = 10765;
    private static final int TV_GENRE_SOAP = 10766;

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String tmdbApiBaseUrl;

    @Value("${tmdb.api.language:ko-KR}")
    private String tmdbApiLanguage;

    private final TmdbDAO tmdbDAO;

    TmdbServiceImpl(TmdbDAO tmdbDAO) {
        this.tmdbDAO = tmdbDAO;
    }

    @Override
    public int loadMovieData() {

        int saveCount = 0;

        try {

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<String>(headers);

            String providerIdText =
                    getSupportedProviderIdText(
                            restTemplate,
                            objectMapper,
                            entity,
                            null);

            if (providerIdText == null || providerIdText.isBlank()) {
                System.out.println("지원 OTT Provider ID 조회 실패");
                return 0;
            }

            for (int page = 1; page <= 10; page++) {

                String url =
                        tmdbApiBaseUrl
                        + "/discover/movie"
                        + "?language=ko-KR"
                        + "&region=KR"
                        + "&watch_region=KR"
                        + "&with_watch_monetization_types=flatrate"
                        + "&with_watch_providers=" + providerIdText
                        + "&sort_by=popularity.desc"
                        + "&page=" + page;

                ResponseEntity<String> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                String.class);

                JsonNode root = objectMapper.readTree(response.getBody());

                JsonNode results = root.get("results");

                if (results == null || !results.isArray()) {
                    continue;
                }

                Iterator<JsonNode> iterator = results.iterator();

                while (iterator.hasNext()) {

                    JsonNode movie = iterator.next();

                    Long tmdbId = movie.path("id").asLong();

                    if (tmdbId == null || tmdbId == 0) {
                        continue;
                    }

                    if (tmdbDAO.existsContent(tmdbId, "MOVIE") > 0) {
                        continue;
                    }

                    TmdbVO vo = new TmdbVO();

                    vo.setTmdbId(tmdbId);
                    vo.setContentType("MOVIE");

                    String title = movie.path("title").asText();
                    String originalTitle = movie.path("original_title").asText();

                    if (title == null || title.isBlank()) {
                        title = originalTitle;
                    }

                    vo.setTitle(title);
                    vo.setOriginalTitle(originalTitle);
                    vo.setOverview(movie.path("overview").asText());
                    vo.setPosterPath(movie.path("poster_path").asText());
                    vo.setBackdropPath(movie.path("backdrop_path").asText());

                    String releaseDate = movie.path("release_date").asText();

                    if (releaseDate != null && !releaseDate.isBlank()) {
                        vo.setReleaseDate(LocalDate.parse(releaseDate));
                    }

                    vo.setTmdbScore(movie.path("vote_average").asDouble());

                    tmdbDAO.insertContent(vo);

                    saveCount++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("영화 기본 데이터 적재 완료 : " + saveCount + "건");

        return saveCount;
    }

    @Override
    public int updateMovieDetailData() {

        int updateCount = 0;

        try {

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<String>(headers);

            for (TmdbVO vo : tmdbDAO.selectContentList()) {

                if (!"MOVIE".equals(vo.getContentType())) {
                    continue;
                }

                String url =
                        tmdbApiBaseUrl
                        + "/movie/"
                        + vo.getTmdbId()
                        + "?language=ko-KR"
                        + "&append_to_response=credits,release_dates";

                ResponseEntity<String> response =
                        restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                entity,
                                String.class);

                JsonNode root = objectMapper.readTree(response.getBody());

                vo.setGenreText(parseGenreText(root.path("genres")));

                if (!root.path("runtime").isMissingNode()
                        && !root.path("runtime").isNull()) {

                    vo.setRuntime(root.path("runtime").asInt());
                }

                vo.setDirector(parseDirector(root.path("credits").path("crew")));
                vo.setCastNames(parseCastNames(root.path("credits").path("cast")));
                vo.setAgeRating(extractAgeRating(root));

                tmdbDAO.updateContentDetail(vo);

                updateCount++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("영화 상세 정보 보강 완료 : " + updateCount + "건");

        return updateCount;
    }

    @Override
    public int loadMoviePlatformData() {

        int saveCount = 0;

        try {

            for (TmdbVO vo : tmdbDAO.selectContentList()) {

                if (!"MOVIE".equals(vo.getContentType())) {
                    continue;
                }

                Integer contentNo =
                        tmdbDAO.findContentNo(
                                vo.getTmdbId(),
                                vo.getContentType());

                if (contentNo == null) {
                    continue;
                }

                ContentVO content = new ContentVO();

                content.setContentNo(contentNo);
                content.setTmdbId(vo.getTmdbId());
                content.setContentType(vo.getContentType());
                content.setTitle(vo.getTitle());

                saveContentPlatform(content);

                saveCount++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("영화 OTT 매핑 시도 완료 : " + saveCount + "건");

        return saveCount;
    }

    @Override
    public int loadMovieFullData() {

        int movieCount = loadMovieData();

        int detailCount = updateMovieDetailData();

        int platformCount = loadMoviePlatformData();

        return movieCount + detailCount + platformCount;
    }

    @Override
    public int loadTvData() {

        System.out.println("TV 데이터 적재는 아직 미구현 상태입니다.");

        return 0;
    }

    @Override
    public int loadAllData() {

        int movieCount = loadMovieFullData();
        int tvCount = loadTvData();

        return movieCount + tvCount;
    }

    @Override
    public List<SearchResultVO> searchMulti(
            String keyword,
            int page,
            List<String> platformList,
            List<String> categoryList,
            List<String> genreList) {

        List<SearchResultVO> resultList = new ArrayList<SearchResultVO>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return resultList;
        }

        if (page <= 0) {
            page = 1;
        }

        List<String> selectedPlatformList =
                normalizeSelectedPlatformList(platformList);

        List<String> selectedCategoryList =
                normalizeSelectedCategoryList(categoryList);

        List<String> selectedGenreList =
                normalizeSelectedGenreList(genreList);

        try {

            String encodedKeyword =
                    URLEncoder.encode(
                            keyword.trim(),
                            StandardCharsets.UTF_8);

            String url =
                    tmdbApiBaseUrl
                    + "/search/multi"
                    + "?query=" + encodedKeyword
                    + "&language=" + tmdbApiLanguage
                    + "&page=" + page
                    + "&include_adult=false";

            JsonNode root = callTmdbApi(url);

            JsonNode results = root.path("results");

            if (!results.isArray()) {
                return resultList;
            }

            for (JsonNode item : results) {

                String mediaType = item.path("media_type").asText();

                if (!"movie".equals(mediaType) && !"tv".equals(mediaType)) {
                    continue;
                }

                Long tmdbId = item.path("id").asLong();

                if (tmdbId == null || tmdbId == 0) {
                    continue;
                }

                JsonNode genreIdsNode = item.path("genre_ids");

                if (!matchesCategory(
                        mediaType,
                        genreIdsNode,
                        selectedCategoryList)) {

                    continue;
                }

                if (!matchesGenre(
                        mediaType,
                        genreIdsNode,
                        selectedGenreList)) {

                    continue;
                }

                List<String> supportedPlatformNameList =
                        getSupportedKrPlatformNameList(
                                tmdbId,
                                mediaType);

                if (!hasMatchedPlatform(
                        supportedPlatformNameList,
                        selectedPlatformList)) {

                    continue;
                }

                SearchResultVO vo =
                        createSearchResultVOFromSearchItem(
                                item,
                                mediaType,
                                tmdbId);

                resultList.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    @Override
    public List<SearchResultVO> getPopularKrOttContent(
            int page,
            List<String> platformList,
            List<String> categoryList,
            List<String> genreList) {

        List<SearchResultVO> resultList = new ArrayList<SearchResultVO>();

        if (page <= 0) {
            page = 1;
        }

        List<String> selectedPlatformList =
                normalizeSelectedPlatformList(platformList);

        List<String> selectedCategoryList =
                normalizeSelectedCategoryList(categoryList);

        List<String> selectedGenreList =
                normalizeSelectedGenreList(genreList);

        try {

            if (shouldIncludeMovieCategory(selectedCategoryList)) {

                List<SearchResultVO> movieList =
                        getPopularMovieList(
                                page,
                                selectedPlatformList,
                                selectedCategoryList,
                                selectedGenreList);

                resultList.addAll(movieList);
            }

            if (shouldIncludeTvCategory(selectedCategoryList)) {

                List<SearchResultVO> tvList =
                        getPopularTvList(
                                page,
                                selectedPlatformList,
                                selectedCategoryList,
                                selectedGenreList);

                resultList.addAll(tvList);
            }

            Collections.sort(
                    resultList,
                    new Comparator<SearchResultVO>() {

                        @Override
                        public int compare(SearchResultVO o1, SearchResultVO o2) {

                            Double p1 = o1.getPopularity();
                            Double p2 = o2.getPopularity();

                            if (p1 == null) {
                                p1 = 0.0;
                            }

                            if (p2 == null) {
                                p2 = 0.0;
                            }

                            return Double.compare(p2, p1);
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    @Override
    public ContentVO getDetailForSave(Long tmdbId, String contentType) {

        if (tmdbId == null) {
            throw new IllegalArgumentException("TMDB ID가 없습니다.");
        }

        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("콘텐츠 타입이 없습니다.");
        }

        String normalizedType = contentType.trim().toUpperCase();

        if ("MOVIE".equals(normalizedType)) {
            return getMovieDetailForSave(tmdbId);
        }

        if ("TV".equals(normalizedType)) {
            return getTvDetailForSave(tmdbId);
        }

        throw new IllegalArgumentException("지원하지 않는 콘텐츠 타입입니다: " + contentType);
    }

    @Override
    public void saveContentPlatform(ContentVO content) {

        if (content == null) {
            return;
        }

        if (content.getContentNo() <= 0
                || content.getTmdbId() == null
                || content.getContentType() == null) {

            return;
        }

        try {

            String apiType = null;

            if ("MOVIE".equals(content.getContentType())) {
                apiType = "movie";
            } else if ("TV".equals(content.getContentType())) {
                apiType = "tv";
            } else {
                return;
            }

            String url =
                    tmdbApiBaseUrl
                    + "/"
                    + apiType
                    + "/"
                    + content.getTmdbId()
                    + "/watch/providers";

            JsonNode root = callTmdbApi(url);

            JsonNode kr =
                    root.path("results")
                        .path("KR");

            if (kr.isMissingNode() || kr.isNull()) {
                return;
            }

            JsonNode flatrate = kr.path("flatrate");

            if (!flatrate.isArray()) {
                return;
            }

            for (JsonNode provider : flatrate) {

                String tmdbProviderName =
                        provider.path("provider_name").asText(null);

                String platformName =
                        convertTmdbProviderName(tmdbProviderName);

                if (platformName == null) {
                    continue;
                }

                Integer platformNo =
                        tmdbDAO.findPlatformNo(platformName);

                if (platformNo == null) {
                    continue;
                }

                if (tmdbDAO.existsContentPlatform(
                        content.getContentNo(),
                        platformNo) > 0) {

                    continue;
                }

                String watchUrl =
                        createPlatformSearchUrl(
                                platformName,
                                content.getTitle());

                tmdbDAO.insertContentPlatform(
                        content.getContentNo(),
                        platformNo,
                        watchUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<SearchResultVO> getMainPopularContent() {

        return getCombinedMainContentList(
                "/movie/popular",
                "/tv/popular",
                true,
                20);
    }

    @Override
    public List<SearchResultVO> getMainTodayContent() {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        try {

            String url =
                    tmdbApiBaseUrl
                    + "/trending/all/day"
                    + "?language=" + tmdbApiLanguage;

            JsonNode root = callTmdbApi(url);
            JsonNode results = root.path("results");

            if (!results.isArray()) {
                return resultList;
            }

            for (JsonNode item : results) {

                String mediaType =
                        item.path("media_type").asText();

                if (!"movie".equals(mediaType)
                        && !"tv".equals(mediaType)) {

                    continue;
                }

                Long tmdbId = item.path("id").asLong();

                if (tmdbId == null || tmdbId == 0) {
                    continue;
                }

                SearchResultVO vo =
                        createSearchResultVOFromSearchItem(
                                item,
                                mediaType,
                                tmdbId);

                resultList.add(vo);

                if (resultList.size() >= 20) {
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    @Override
    public List<SearchResultVO> getMainRecommendedContent() {

        return getCombinedMainContentList(
                "/movie/top_rated",
                "/tv/top_rated",
                false,
                20);
    }

    private List<SearchResultVO> getCombinedMainContentList(
            String movieApiPath,
            String tvApiPath,
            boolean sortByPopularity,
            int limit) {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        try {

            String movieUrl =
                    tmdbApiBaseUrl
                    + movieApiPath
                    + "?language=" + tmdbApiLanguage
                    + "&region=KR"
                    + "&page=1";

            String tvUrl =
                    tmdbApiBaseUrl
                    + tvApiPath
                    + "?language=" + tmdbApiLanguage
                    + "&page=1";

            addMainContentItems(
                    resultList,
                    callTmdbApi(movieUrl).path("results"),
                    "MOVIE");

            addMainContentItems(
                    resultList,
                    callTmdbApi(tvUrl).path("results"),
                    "TV");

            Collections.sort(
                    resultList,
                    new Comparator<SearchResultVO>() {

                        @Override
                        public int compare(
                                SearchResultVO o1,
                                SearchResultVO o2) {

                            if (sortByPopularity) {

                                double value1 =
                                        o1.getPopularity() == null
                                        ? 0.0
                                        : o1.getPopularity();

                                double value2 =
                                        o2.getPopularity() == null
                                        ? 0.0
                                        : o2.getPopularity();

                                return Double.compare(value2, value1);
                            }

                            double value1 =
                                    o1.getTmdbScore() == null
                                    ? 0.0
                                    : o1.getTmdbScore();

                            double value2 =
                                    o2.getTmdbScore() == null
                                    ? 0.0
                                    : o2.getTmdbScore();

                            return Double.compare(value2, value1);
                        }
                    });

            if (resultList.size() > limit) {
                return new ArrayList<SearchResultVO>(
                        resultList.subList(0, limit));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    private void addMainContentItems(
            List<SearchResultVO> resultList,
            JsonNode results,
            String contentType) {

        if (results == null || !results.isArray()) {
            return;
        }

        for (JsonNode item : results) {

            Long tmdbId = item.path("id").asLong();

            if (tmdbId == null || tmdbId == 0) {
                continue;
            }

            SearchResultVO vo =
                    createSearchResultVOFromDiscoverItem(
                            item,
                            contentType,
                            tmdbId);

            resultList.add(vo);
        }
    }

    private List<SearchResultVO> getPopularMovieList(
            int page,
            List<String> selectedPlatformList,
            List<String> selectedCategoryList,
            List<String> selectedGenreList) {

        List<SearchResultVO> movieList = new ArrayList<SearchResultVO>();

        if (!matchesCategory(
                "movie",
                null,
                selectedCategoryList)) {

            return movieList;
        }

        try {

            String providerIdText =
                    getSupportedProviderIdTextForApi(selectedPlatformList);

            if (providerIdText == null || providerIdText.isBlank()) {
                return movieList;
            }

            String url =
                    tmdbApiBaseUrl
                    + "/discover/movie"
                    + "?language=" + tmdbApiLanguage
                    + "&region=KR"
                    + "&watch_region=KR"
                    + "&with_watch_monetization_types=flatrate"
                    + "&with_watch_providers=" + providerIdText
                    + "&sort_by=popularity.desc"
                    + "&page=" + page;

            JsonNode root = callTmdbApi(url);

            JsonNode results = root.path("results");

            if (!results.isArray()) {
                return movieList;
            }

            for (JsonNode item : results) {

                Long tmdbId = item.path("id").asLong();

                if (tmdbId == null || tmdbId == 0) {
                    continue;
                }

                if (!matchesGenre(
                        "movie",
                        item.path("genre_ids"),
                        selectedGenreList)) {

                    continue;
                }

                SearchResultVO vo =
                        createSearchResultVOFromDiscoverItem(
                                item,
                                "MOVIE",
                                tmdbId);

                movieList.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return movieList;
    }

    private List<SearchResultVO> getPopularTvList(
            int page,
            List<String> selectedPlatformList,
            List<String> selectedCategoryList,
            List<String> selectedGenreList) {

        List<SearchResultVO> tvList = new ArrayList<SearchResultVO>();

        try {

            String providerIdText =
                    getSupportedProviderIdTextForApi(selectedPlatformList);

            if (providerIdText == null || providerIdText.isBlank()) {
                return tvList;
            }

            String url =
                    tmdbApiBaseUrl
                    + "/discover/tv"
                    + "?language=" + tmdbApiLanguage
                    + "&watch_region=KR"
                    + "&with_watch_monetization_types=flatrate"
                    + "&with_watch_providers=" + providerIdText
                    + "&sort_by=popularity.desc"
                    + "&page=" + page;

            JsonNode root = callTmdbApi(url);

            JsonNode results = root.path("results");

            if (!results.isArray()) {
                return tvList;
            }

            for (JsonNode item : results) {

                Long tmdbId = item.path("id").asLong();

                if (tmdbId == null || tmdbId == 0) {
                    continue;
                }

                JsonNode genreIdsNode = item.path("genre_ids");

                if (!matchesCategory(
                        "tv",
                        genreIdsNode,
                        selectedCategoryList)) {

                    continue;
                }

                if (!matchesGenre(
                        "tv",
                        genreIdsNode,
                        selectedGenreList)) {

                    continue;
                }

                SearchResultVO vo =
                        createSearchResultVOFromDiscoverItem(
                                item,
                                "TV",
                                tmdbId);

                tvList.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tvList;
    }

    private boolean matchesCategory(
            String mediaType,
            JsonNode genreIdsNode,
            List<String> selectedCategoryList) {

        if (selectedCategoryList == null || selectedCategoryList.isEmpty()) {
            return true;
        }

        if ("movie".equals(mediaType)) {
            return selectedCategoryList.contains("MOVIE");
        }

        if (!"tv".equals(mediaType)) {
            return false;
        }

        String tvCategory =
                resolveTvCategory(genreIdsNode);

        return selectedCategoryList.contains(tvCategory);
    }

    private String resolveTvCategory(JsonNode genreIdsNode) {

        if (containsGenreId(genreIdsNode, TV_GENRE_ANIMATION)) {
            return "ANIMATION";
        }

        if (containsGenreId(genreIdsNode, TV_GENRE_REALITY)
                || containsGenreId(genreIdsNode, TV_GENRE_TALK)) {

            return "ENTERTAINMENT";
        }

        if (containsGenreId(genreIdsNode, TV_GENRE_DOCUMENTARY)) {
            return "DOCUMENTARY";
        }

        return "DRAMA";
    }

    private boolean matchesGenre(
            String mediaType,
            JsonNode genreIdsNode,
            List<String> selectedGenreList) {

        if (selectedGenreList == null || selectedGenreList.isEmpty()) {
            return true;
        }

        if (genreIdsNode == null || !genreIdsNode.isArray()) {
            return false;
        }

        for (String selectedGenre : selectedGenreList) {

            if (matchesSingleGenre(
                    mediaType,
                    genreIdsNode,
                    selectedGenre)) {

                return true;
            }
        }

        return false;
    }

    private boolean matchesSingleGenre(
            String mediaType,
            JsonNode genreIdsNode,
            String selectedGenre) {

        if (selectedGenre == null) {
            return false;
        }

        if ("movie".equals(mediaType)) {
            return matchesMovieGenre(genreIdsNode, selectedGenre);
        }

        if ("tv".equals(mediaType)) {
            return matchesTvGenre(genreIdsNode, selectedGenre);
        }

        return false;
    }

    private boolean matchesMovieGenre(
            JsonNode genreIdsNode,
            String selectedGenre) {

        if ("ACTION".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_ACTION);
        }

        if ("COMEDY".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_COMEDY);
        }

        if ("THRILLER".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_THRILLER);
        }

        if ("ROMANCE".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_ROMANCE);
        }

        if ("CRIME".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_CRIME);
        }

        if ("FANTASY".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_FANTASY);
        }

        if ("HORROR".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_HORROR);
        }

        if ("MYSTERY".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_MYSTERY);
        }

        if ("SF".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_SF);
        }

        if ("DRAMA".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, MOVIE_GENRE_DRAMA);
        }

        return false;
    }

    private boolean matchesTvGenre(
            JsonNode genreIdsNode,
            String selectedGenre) {

        if ("ACTION".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_ACTION_ADVENTURE);
        }

        if ("COMEDY".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_COMEDY);
        }

        if ("THRILLER".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_MYSTERY)
                    || containsGenreId(genreIdsNode, TV_GENRE_CRIME)
                    || containsGenreId(genreIdsNode, TV_GENRE_SF_FANTASY);
        }

        if ("ROMANCE".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_DRAMA)
                    || containsGenreId(genreIdsNode, TV_GENRE_SOAP);
        }

        if ("CRIME".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_CRIME);
        }

        if ("FANTASY".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_SF_FANTASY);
        }

        if ("HORROR".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_MYSTERY)
                    || containsGenreId(genreIdsNode, TV_GENRE_SF_FANTASY);
        }

        if ("MYSTERY".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_MYSTERY);
        }

        if ("SF".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_SF_FANTASY);
        }

        if ("DRAMA".equals(selectedGenre)) {
            return containsGenreId(genreIdsNode, TV_GENRE_DRAMA)
                    || containsGenreId(genreIdsNode, TV_GENRE_SOAP);
        }

        return false;
    }

    private boolean containsGenreId(
            JsonNode genreIdsNode,
            int genreId) {

        if (genreIdsNode == null || !genreIdsNode.isArray()) {
            return false;
        }

        for (JsonNode genreNode : genreIdsNode) {

            if (genreNode.asInt() == genreId) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldIncludeMovieCategory(
            List<String> selectedCategoryList) {

        if (selectedCategoryList == null || selectedCategoryList.isEmpty()) {
            return true;
        }

        return selectedCategoryList.contains("MOVIE");
    }

    private boolean shouldIncludeTvCategory(
            List<String> selectedCategoryList) {

        if (selectedCategoryList == null || selectedCategoryList.isEmpty()) {
            return true;
        }

        if (selectedCategoryList.contains("DRAMA")) {
            return true;
        }

        if (selectedCategoryList.contains("ANIMATION")) {
            return true;
        }

        if (selectedCategoryList.contains("ENTERTAINMENT")) {
            return true;
        }

        if (selectedCategoryList.contains("DOCUMENTARY")) {
            return true;
        }

        return false;
    }

    private SearchResultVO createSearchResultVOFromSearchItem(
            JsonNode item,
            String mediaType,
            Long tmdbId) {

        SearchResultVO vo = new SearchResultVO();

        vo.setTmdbId(tmdbId);

        if ("movie".equals(mediaType)) {

            vo.setContentType("MOVIE");

            String title = item.path("title").asText(null);
            String originalTitle = item.path("original_title").asText(null);

            if (title == null || title.isBlank()) {
                title = originalTitle;
            }

            vo.setTitle(title);
            vo.setReleaseDate(item.path("release_date").asText(null));

        } else {

            vo.setContentType("TV");

            String title = item.path("name").asText(null);
            String originalTitle = item.path("original_name").asText(null);

            if (title == null || title.isBlank()) {
                title = originalTitle;
            }

            vo.setTitle(title);
            vo.setReleaseDate(item.path("first_air_date").asText(null));
        }

        vo.setOverview(item.path("overview").asText(null));
        vo.setPosterPath(item.path("poster_path").asText(null));

        if (!item.path("vote_average").isMissingNode()
                && !item.path("vote_average").isNull()) {

            vo.setTmdbScore(item.path("vote_average").asDouble());
        }

        if (!item.path("popularity").isMissingNode()
                && !item.path("popularity").isNull()) {

            vo.setPopularity(item.path("popularity").asDouble());
        }

        return vo;
    }

    private SearchResultVO createSearchResultVOFromDiscoverItem(
            JsonNode item,
            String contentType,
            Long tmdbId) {

        SearchResultVO vo = new SearchResultVO();

        vo.setTmdbId(tmdbId);
        vo.setContentType(contentType);

        if ("MOVIE".equals(contentType)) {

            String title = item.path("title").asText(null);
            String originalTitle = item.path("original_title").asText(null);

            if (title == null || title.isBlank()) {
                title = originalTitle;
            }

            vo.setTitle(title);
            vo.setReleaseDate(item.path("release_date").asText(null));

        } else {

            String title = item.path("name").asText(null);
            String originalTitle = item.path("original_name").asText(null);

            if (title == null || title.isBlank()) {
                title = originalTitle;
            }

            vo.setTitle(title);
            vo.setReleaseDate(item.path("first_air_date").asText(null));
        }

        vo.setOverview(item.path("overview").asText(null));
        vo.setPosterPath(item.path("poster_path").asText(null));

        if (!item.path("vote_average").isMissingNode()
                && !item.path("vote_average").isNull()) {

            vo.setTmdbScore(item.path("vote_average").asDouble());
        }

        if (!item.path("popularity").isMissingNode()
                && !item.path("popularity").isNull()) {

            vo.setPopularity(item.path("popularity").asDouble());
        }

        return vo;
    }

    private boolean hasMatchedPlatform(
            List<String> supportedPlatformNameList,
            List<String> selectedPlatformList) {

        if (supportedPlatformNameList == null
                || supportedPlatformNameList.isEmpty()) {

            return false;
        }

        if (selectedPlatformList == null
                || selectedPlatformList.isEmpty()) {

            return true;
        }

        for (String selectedPlatform : selectedPlatformList) {

            if (supportedPlatformNameList.contains(selectedPlatform)) {
                return true;
            }
        }

        return false;
    }

    private List<String> getSupportedKrPlatformNameList(
            Long tmdbId,
            String mediaType) {

        List<String> platformNameList = new ArrayList<String>();

        if (tmdbId == null || tmdbId == 0) {
            return platformNameList;
        }

        if (!"movie".equals(mediaType) && !"tv".equals(mediaType)) {
            return platformNameList;
        }

        try {

            String url =
                    tmdbApiBaseUrl
                    + "/"
                    + mediaType
                    + "/"
                    + tmdbId
                    + "/watch/providers";

            JsonNode root = callTmdbApi(url);

            JsonNode kr =
                    root.path("results")
                        .path("KR");

            if (kr.isMissingNode() || kr.isNull()) {
                return platformNameList;
            }

            JsonNode flatrate = kr.path("flatrate");

            if (!flatrate.isArray()) {
                return platformNameList;
            }

            for (JsonNode provider : flatrate) {

                String tmdbProviderName =
                        provider.path("provider_name").asText(null);

                String platformName =
                        convertTmdbProviderName(tmdbProviderName);

                if (platformName == null) {
                    continue;
                }

                if (!platformNameList.contains(platformName)) {
                    platformNameList.add(platformName);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return platformNameList;
    }

    private String getSupportedProviderIdTextForApi(
            List<String> selectedPlatformList) {

        String providerIdText = "";

        try {

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<String>(headers);

            providerIdText =
                    getSupportedProviderIdText(
                            restTemplate,
                            objectMapper,
                            entity,
                            selectedPlatformList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return providerIdText;
    }

    private String getSupportedProviderIdText(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            HttpEntity<String> entity,
            List<String> selectedPlatformList) {

        Set<String> providerIdSet = new LinkedHashSet<String>();

        try {

            String url =
                    tmdbApiBaseUrl
                    + "/watch/providers/movie"
                    + "?language=ko-KR"
                    + "&watch_region=KR";

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode results = root.path("results");

            if (!results.isArray()) {
                return "";
            }

            for (JsonNode provider : results) {

                String providerName =
                        provider.path("provider_name").asText();

                int providerId =
                        provider.path("provider_id").asInt();

                String platformName =
                        convertTmdbProviderName(providerName);

                if (platformName == null) {
                    continue;
                }

                if (selectedPlatformList != null
                        && !selectedPlatformList.isEmpty()
                        && !selectedPlatformList.contains(platformName)) {

                    continue;
                }

                if (providerId > 0) {
                    providerIdSet.add(String.valueOf(providerId));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return String.join("|", providerIdSet);
    }

    private List<String> normalizeSelectedPlatformList(
            List<String> platformList) {

        List<String> selectedPlatformList = new ArrayList<String>();

        if (platformList == null || platformList.isEmpty()) {
            return selectedPlatformList;
        }

        for (String platform : platformList) {

            String normalizedPlatform = normalizePlatformName(platform);

            if (normalizedPlatform == null) {
                continue;
            }

            if (!selectedPlatformList.contains(normalizedPlatform)) {
                selectedPlatformList.add(normalizedPlatform);
            }
        }

        return selectedPlatformList;
    }

    private String normalizePlatformName(String platformName) {

        if (platformName == null) {
            return null;
        }

        String name = platformName.trim();

        if ("Netflix".equalsIgnoreCase(name)) {
            return "Netflix";
        }

        if ("TVING".equalsIgnoreCase(name)) {
            return "TVING";
        }

        if ("wavve".equalsIgnoreCase(name)) {
            return "wavve";
        }

        if ("Disney Plus".equalsIgnoreCase(name)
                || "Disney+".equalsIgnoreCase(name)
                || "DisneyPlus".equalsIgnoreCase(name)) {

            return "Disney Plus";
        }

        if ("Watcha".equalsIgnoreCase(name)
                || "WATCHA".equalsIgnoreCase(name)) {

            return "Watcha";
        }

        return null;
    }

    private List<String> normalizeSelectedCategoryList(
            List<String> categoryList) {

        List<String> selectedCategoryList = new ArrayList<String>();

        if (categoryList == null || categoryList.isEmpty()) {
            return selectedCategoryList;
        }

        for (String category : categoryList) {

            String normalizedCategory = normalizeCategoryName(category);

            if (normalizedCategory == null) {
                continue;
            }

            if (!selectedCategoryList.contains(normalizedCategory)) {
                selectedCategoryList.add(normalizedCategory);
            }
        }

        return selectedCategoryList;
    }

    private String normalizeCategoryName(String categoryName) {

        if (categoryName == null) {
            return null;
        }

        String name = categoryName.trim();

        if ("MOVIE".equalsIgnoreCase(name)) {
            return "MOVIE";
        }

        if ("DRAMA".equalsIgnoreCase(name)) {
            return "DRAMA";
        }

        if ("ANIMATION".equalsIgnoreCase(name)) {
            return "ANIMATION";
        }

        if ("ENTERTAINMENT".equalsIgnoreCase(name)
                || "REALITY".equalsIgnoreCase(name)
                || "VARIETY".equalsIgnoreCase(name)) {

            return "ENTERTAINMENT";
        }

        if ("DOCUMENTARY".equalsIgnoreCase(name)
                || "DOCU".equalsIgnoreCase(name)) {

            return "DOCUMENTARY";
        }

        return null;
    }

    private List<String> normalizeSelectedGenreList(
            List<String> genreList) {

        List<String> selectedGenreList = new ArrayList<String>();

        if (genreList == null || genreList.isEmpty()) {
            return selectedGenreList;
        }

        for (String genre : genreList) {

            String normalizedGenre = normalizeGenreName(genre);

            if (normalizedGenre == null) {
                continue;
            }

            if (!selectedGenreList.contains(normalizedGenre)) {
                selectedGenreList.add(normalizedGenre);
            }
        }

        return selectedGenreList;
    }

    private String normalizeGenreName(String genreName) {

        if (genreName == null) {
            return null;
        }

        String name = genreName.trim();

        if ("ACTION".equalsIgnoreCase(name)) {
            return "ACTION";
        }

        if ("COMEDY".equalsIgnoreCase(name)) {
            return "COMEDY";
        }

        if ("THRILLER".equalsIgnoreCase(name)) {
            return "THRILLER";
        }

        if ("ROMANCE".equalsIgnoreCase(name)) {
            return "ROMANCE";
        }

        if ("CRIME".equalsIgnoreCase(name)) {
            return "CRIME";
        }

        if ("FANTASY".equalsIgnoreCase(name)) {
            return "FANTASY";
        }

        if ("HORROR".equalsIgnoreCase(name)) {
            return "HORROR";
        }

        if ("MYSTERY".equalsIgnoreCase(name)) {
            return "MYSTERY";
        }

        if ("SF".equalsIgnoreCase(name)
                || "SCIENCE_FICTION".equalsIgnoreCase(name)
                || "SCIENCEFICTION".equalsIgnoreCase(name)) {

            return "SF";
        }

        if ("DRAMA".equalsIgnoreCase(name)) {
            return "DRAMA";
        }

        return null;
    }

    private JsonNode callTmdbApi(String url) {

        try {

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper objectMapper = new ObjectMapper();

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity = new HttpEntity<String>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class);

            return objectMapper.readTree(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("TMDB API 호출에 실패했습니다. URL=" + url);
    }

    private ContentVO getMovieDetailForSave(Long tmdbId) {

        String url =
                tmdbApiBaseUrl
                + "/movie/"
                + tmdbId
                + "?language="
                + tmdbApiLanguage
                + "&append_to_response=credits,release_dates";

        JsonNode root = callTmdbApi(url);

        ContentVO vo = new ContentVO();

        vo.setTmdbId(root.path("id").asLong());
        vo.setContentType("MOVIE");

        String title = root.path("title").asText(null);
        String originalTitle = root.path("original_title").asText(null);

        if (title == null || title.isBlank()) {
            title = originalTitle;
        }

        vo.setTitle(title);
        vo.setOriginalTitle(originalTitle);
        vo.setOverview(root.path("overview").asText(null));
        vo.setPosterPath(root.path("poster_path").asText(null));
        vo.setBackdropPath(root.path("backdrop_path").asText(null));
        vo.setReleaseDate(parseDate(root.path("release_date").asText(null)));
        vo.setGenreText(parseGenreText(root.path("genres")));

        if (!root.path("runtime").isMissingNode()
                && !root.path("runtime").isNull()) {

            vo.setRuntime(root.path("runtime").asInt());
        }

        vo.setEpisodeCount(null);
        vo.setDirector(parseDirector(root.path("credits").path("crew")));
        vo.setCastNames(parseCastNames(root.path("credits").path("cast")));
        vo.setAgeRating(extractAgeRating(root));

        if (!root.path("vote_average").isMissingNode()
                && !root.path("vote_average").isNull()) {

            vo.setTmdbScore(root.path("vote_average").asDouble());
        }

        return vo;
    }

    private ContentVO getTvDetailForSave(Long tmdbId) {

        String url =
                tmdbApiBaseUrl
                + "/tv/"
                + tmdbId
                + "?language="
                + tmdbApiLanguage
                + "&append_to_response=credits,content_ratings";

        JsonNode root = callTmdbApi(url);

        ContentVO vo = new ContentVO();

        vo.setTmdbId(root.path("id").asLong());
        vo.setContentType("TV");

        String title = root.path("name").asText(null);
        String originalTitle = root.path("original_name").asText(null);

        if (title == null || title.isBlank()) {
            title = originalTitle;
        }

        vo.setTitle(title);
        vo.setOriginalTitle(originalTitle);
        vo.setOverview(root.path("overview").asText(null));
        vo.setPosterPath(root.path("poster_path").asText(null));
        vo.setBackdropPath(root.path("backdrop_path").asText(null));
        vo.setReleaseDate(parseDate(root.path("first_air_date").asText(null)));
        vo.setGenreText(parseGenreText(root.path("genres")));
        vo.setRuntime(parseTvRuntime(root.path("episode_run_time")));

        if (!root.path("number_of_episodes").isMissingNode()
                && !root.path("number_of_episodes").isNull()) {

            vo.setEpisodeCount(root.path("number_of_episodes").asInt());
        }

        vo.setDirector(parseTvCreator(root.path("created_by")));
        vo.setCastNames(parseCastNames(root.path("credits").path("cast")));
        vo.setAgeRating(extractTvAgeRating(root));

        if (!root.path("vote_average").isMissingNode()
                && !root.path("vote_average").isNull()) {

            vo.setTmdbScore(root.path("vote_average").asDouble());
        }

        return vo;
    }

    private LocalDate parseDate(String dateText) {

        if (dateText == null || dateText.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateText);
        } catch (Exception e) {
            return null;
        }
    }

    private String parseGenreText(JsonNode genresNode) {

        if (genresNode == null || !genresNode.isArray()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (JsonNode genre : genresNode) {

            String genreName = genre.path("name").asText(null);

            if (genreName == null || genreName.trim().isEmpty()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(genreName);
        }

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }

    private String parseDirector(JsonNode crewNode) {

        if (crewNode == null || !crewNode.isArray()) {
            return null;
        }

        for (JsonNode crew : crewNode) {

            String job = crew.path("job").asText();

            if ("Director".equals(job)) {
                return crew.path("name").asText(null);
            }
        }

        return null;
    }

    private String parseTvCreator(JsonNode createdByNode) {

        if (createdByNode == null || !createdByNode.isArray()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (JsonNode creator : createdByNode) {

            String name = creator.path("name").asText(null);

            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(name);
        }

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }

    private String parseCastNames(JsonNode castNode) {

        if (castNode == null || !castNode.isArray()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int castCount = 0;

        for (JsonNode cast : castNode) {

            if (castCount >= 5) {
                break;
            }

            String castName = cast.path("name").asText(null);

            if (castName == null || castName.trim().isEmpty()) {
                continue;
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(castName);
            castCount++;
        }

        if (sb.length() == 0) {
            return null;
        }

        return sb.toString();
    }

    private Integer parseTvRuntime(JsonNode episodeRunTimeNode) {

        if (episodeRunTimeNode == null
                || !episodeRunTimeNode.isArray()
                || episodeRunTimeNode.size() == 0) {

            return null;
        }

        JsonNode firstRuntime = episodeRunTimeNode.get(0);

        if (firstRuntime == null || firstRuntime.isNull()) {
            return null;
        }

        int runtime = firstRuntime.asInt();

        if (runtime <= 0) {
            return null;
        }

        return runtime;
    }

    private String convertTmdbProviderName(String tmdbProviderName) {

        if (tmdbProviderName == null) {
            return null;
        }

        String name = tmdbProviderName.trim();

        String normalized =
                name.toLowerCase()
                        .replace(" ", "")
                        .replace("_", "")
                        .replace("-", "")
                        .replace("+", "");

        if (normalized.contains("netflix")
                || name.contains("넷플릭스")) {

            return "Netflix";
        }

        if (normalized.contains("tving")
                || name.contains("티빙")) {

            return "TVING";
        }

        if (normalized.contains("wavve")
                || name.contains("웨이브")) {

            return "wavve";
        }

        if (normalized.contains("disney")
                || name.contains("디즈니")) {

            return "Disney Plus";
        }

        if (normalized.contains("watcha")
                || name.contains("왓챠")) {

            return "Watcha";
        }

        return null;
    }

    private String extractAgeRating(JsonNode root) {

        JsonNode results =
                root.path("release_dates")
                    .path("results");

        if (!results.isArray()) {
            return "UNKNOWN";
        }

        String usRating = null;

        for (JsonNode country : results) {

            String countryCode =
                    country.path("iso_3166_1").asText();

            if (!"KR".equals(countryCode)
                    && !"US".equals(countryCode)) {

                continue;
            }

            JsonNode releaseDates =
                    country.path("release_dates");

            if (!releaseDates.isArray()) {
                continue;
            }

            for (JsonNode releaseDate : releaseDates) {

                String certification =
                        releaseDate.path("certification").asText();

                String normalizedRating =
                        normalizeAgeRatingByCountry(
                                countryCode,
                                certification);

                if (normalizedRating == null) {
                    continue;
                }

                if ("KR".equals(countryCode)) {
                    return normalizedRating;
                }

                if ("US".equals(countryCode)
                        && usRating == null) {

                    usRating = normalizedRating;
                }
            }
        }

        if (usRating != null) {
            return usRating;
        }

        return "UNKNOWN";
    }

    private String normalizeAgeRatingByCountry(
            String countryCode,
            String certification) {

        if (certification == null) {
            return null;
        }

        String rating = certification.trim();

        if (rating.isBlank()) {
            return null;
        }

        String normalized =
                rating.toUpperCase()
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("_", "");

        if ("KR".equals(countryCode)) {

            if ("ALL".equals(normalized)
                    || "전체관람가".equals(rating)
                    || "전체".equals(rating)) {

                return "ALL";
            }

            if ("12".equals(normalized)
                    || "12세".equals(rating)
                    || "12세이상관람가".equals(rating)
                    || "12세관람가".equals(rating)) {

                return "12";
            }

            if ("15".equals(normalized)
                    || "15세".equals(rating)
                    || "15세이상관람가".equals(rating)
                    || "15세관람가".equals(rating)) {

                return "15";
            }

            if ("18".equals(normalized)
                    || "19".equals(normalized)
                    || "18세".equals(rating)
                    || "19세".equals(rating)
                    || "18세이상관람가".equals(rating)
                    || "19세이상".equals(rating)
                    || "청소년관람불가".equals(rating)) {

                return "18";
            }

            if (rating.contains("청소년")) {
                return "18";
            }

            if ("NR".equals(normalized)
                    || "UNKNOWN".equals(normalized)) {

                return "UNKNOWN";
            }

            return null;
        }

        if ("US".equals(countryCode)) {

            if ("G".equals(normalized)) {
                return "ALL";
            }

            if ("PG".equals(normalized)) {
                return "12";
            }

            if ("PG13".equals(normalized)) {
                return "15";
            }

            if ("R".equals(normalized)
                    || "NC17".equals(normalized)) {

                return "18";
            }

            if ("NR".equals(normalized)
                    || "UNRATED".equals(normalized)
                    || "NOTRATED".equals(normalized)) {

                return "UNKNOWN";
            }

            return null;
        }

        return null;
    }

    private String extractTvAgeRating(JsonNode root) {

        JsonNode results =
                root.path("content_ratings")
                    .path("results");

        if (!results.isArray()) {
            return "UNKNOWN";
        }

        String usRating = null;

        for (JsonNode country : results) {

            String countryCode =
                    country.path("iso_3166_1").asText();

            if (!"KR".equals(countryCode)
                    && !"US".equals(countryCode)) {

                continue;
            }

            String rating =
                    country.path("rating").asText();

            String normalizedRating =
                    normalizeTvAgeRatingByCountry(
                            countryCode,
                            rating);

            if (normalizedRating == null) {
                continue;
            }

            if ("KR".equals(countryCode)) {
                return normalizedRating;
            }

            if ("US".equals(countryCode)
                    && usRating == null) {

                usRating = normalizedRating;
            }
        }

        if (usRating != null) {
            return usRating;
        }

        return "UNKNOWN";
    }

    private String normalizeTvAgeRatingByCountry(
            String countryCode,
            String certification) {

        if (certification == null) {
            return null;
        }

        String rating = certification.trim();

        if (rating.isBlank()) {
            return null;
        }

        String normalized =
                rating.toUpperCase()
                        .replace(" ", "")
                        .replace("-", "")
                        .replace("_", "");

        if ("KR".equals(countryCode)) {

            if ("ALL".equals(normalized)
                    || "전체".equals(rating)
                    || "전체관람가".equals(rating)
                    || "전체이용가".equals(rating)) {

                return "ALL";
            }

            if ("7".equals(normalized)
                    || "7세".equals(rating)
                    || "7세이상".equals(rating)) {

                return "ALL";
            }

            if ("12".equals(normalized)
                    || "12세".equals(rating)
                    || "12세이상".equals(rating)
                    || "12세이상관람가".equals(rating)) {

                return "12";
            }

            if ("15".equals(normalized)
                    || "15세".equals(rating)
                    || "15세이상".equals(rating)
                    || "15세이상관람가".equals(rating)) {

                return "15";
            }

            if ("18".equals(normalized)
                    || "19".equals(normalized)
                    || "18세".equals(rating)
                    || "19세".equals(rating)
                    || "18세이상".equals(rating)
                    || "19세이상".equals(rating)
                    || "청소년관람불가".equals(rating)) {

                return "18";
            }

            if (rating.contains("청소년")) {
                return "18";
            }

            if ("NR".equals(normalized)
                    || "UNKNOWN".equals(normalized)) {

                return "UNKNOWN";
            }

            return null;
        }

        if ("US".equals(countryCode)) {

            if ("TVY".equals(normalized)
                    || "TVY7".equals(normalized)
                    || "TVG".equals(normalized)) {

                return "ALL";
            }

            if ("TVPG".equals(normalized)) {
                return "12";
            }

            if ("TV14".equals(normalized)) {
                return "15";
            }

            if ("TVMA".equals(normalized)) {
                return "18";
            }

            if ("NR".equals(normalized)
                    || "UNRATED".equals(normalized)
                    || "NOTRATED".equals(normalized)) {

                return "UNKNOWN";
            }

            return null;
        }

        return null;
    }

    private String createPlatformSearchUrl(
            String platformName,
            String title) {

        String keyword = "";

        if (title != null) {
            keyword =
                    URLEncoder.encode(
                            title,
                            StandardCharsets.UTF_8);
        }

        if ("Netflix".equals(platformName)) {
            return "https://www.netflix.com/search?q=" + keyword;
        }

        if ("TVING".equals(platformName)) {
            return "https://www.tving.com/search?keyword=" + keyword;
        }

        if ("wavve".equals(platformName)) {
            return "https://www.wavve.com/search?searchWord=" + keyword;
        }

        if ("Disney Plus".equals(platformName)) {
            return "https://www.disneyplus.com/search/" + keyword;
        }

        if ("Watcha".equals(platformName)) {
            return "https://watcha.com/search?query=" + keyword;
        }

        return "";
    }

    @Override
    public int updateTvDetailData() {
        
        throw new UnsupportedOperationException("Unimplemented method 'updateTvDetailData'");
    }

    @Override
    public int loadTvPlatformData() {
        
        throw new UnsupportedOperationException("Unimplemented method 'loadTvPlatformData'");
    }

    @Override
    public int loadTvFullData() {
        
        throw new UnsupportedOperationException("Unimplemented method 'loadTvFullData'");
    }
}