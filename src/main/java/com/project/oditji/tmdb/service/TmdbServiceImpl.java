package com.project.oditji.tmdb.service;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.TmdbVO;

@Service
public class TmdbServiceImpl implements TmdbService {

    private static final int LOAD_PAGE_COUNT = 10;
    private static final int CAST_SAVE_LIMIT = 5;
    private static final int MAIN_POPULAR_LIMIT = 5;
    private static final int MAIN_SLIDER_LIMIT = 20;

    private static final Map<Integer, String> MOVIE_GENRES =
            createMovieGenreMap();

    private static final Map<Integer, String> TV_GENRES =
            createTvGenreMap();

    private final TmdbDAO tmdbDAO;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String tmdbApiBaseUrl;

    @Value("${tmdb.api.language:ko-KR}")
    private String tmdbApiLanguage;

    @Value("${tmdb.api.region:KR}")
    private String tmdbApiRegion;

    public TmdbServiceImpl(TmdbDAO tmdbDAO) {
        this.tmdbDAO = tmdbDAO;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public int loadMovieData() {
        return loadBasicData("movie", "MOVIE");
    }

    @Override
    public int loadTvData() {
        return loadBasicData("tv", "TV");
    }

    private int loadBasicData(String apiType, String contentType) {

        int saveCount = 0;
        String providerIds = getSupportedProviderIdText(apiType, null);

        if (providerIds.isBlank()) {
            return 0;
        }

        for (int page = 1; page <= LOAD_PAGE_COUNT; page++) {

            String url = tmdbApiBaseUrl
                    + "/discover/" + apiType
                    + "?language=" + tmdbApiLanguage
                    + "&region=" + tmdbApiRegion
                    + "&watch_region=" + tmdbApiRegion
                    + "&include_adult=false"
                    + ("movie".equals(apiType) ? "&include_video=false" : "")
                    + "&with_watch_monetization_types=flatrate"
                    + "&with_watch_providers=" + providerIds
                    + "&sort_by=popularity.desc"
                    + "&page=" + page;

            JsonNode results = callTmdbApi(url).path("results");

            if (!results.isArray()) {
                continue;
            }

            for (JsonNode item : results) {

                if (isAdultContent(item)) {
                    continue;
                }

                long tmdbId = item.path("id").asLong();

                if (tmdbId <= 0
                        || tmdbDAO.existsContent(tmdbId, contentType) > 0) {
                    continue;
                }

                TmdbVO vo = createBasicTmdbVO(item, contentType);
                tmdbDAO.insertContent(vo);
                saveCount++;
            }
        }

        return saveCount;
    }

    private TmdbVO createBasicTmdbVO(JsonNode item, String contentType) {

        TmdbVO vo = new TmdbVO();
        vo.setTmdbId(item.path("id").asLong());
        vo.setContentType(contentType);

        if ("MOVIE".equals(contentType)) {
            vo.setTitle(firstNonBlank(
                    item.path("title").asText(null),
                    item.path("original_title").asText(null)));
            vo.setOriginalTitle(item.path("original_title").asText(null));
            vo.setReleaseDate(parseDate(
                    item.path("release_date").asText(null)));
        } else {
            vo.setTitle(firstNonBlank(
                    item.path("name").asText(null),
                    item.path("original_name").asText(null)));
            vo.setOriginalTitle(item.path("original_name").asText(null));
            vo.setReleaseDate(parseDate(
                    item.path("first_air_date").asText(null)));
        }

        vo.setOverview(item.path("overview").asText(null));
        vo.setPosterPath(item.path("poster_path").asText(null));
        vo.setBackdropPath(item.path("backdrop_path").asText(null));
        vo.setGenreText(convertGenreIdsToText(
                item.path("genre_ids"), contentType));
        vo.setTmdbScore(nullableDouble(item.path("vote_average")));

        return vo;
    }

    @Override
    public int updateMovieDetailData() {
        return updateDetailData("MOVIE");
    }

    @Override
    public int updateTvDetailData() {
        return updateDetailData("TV");
    }

    private int updateDetailData(String contentType) {

        int updateCount = 0;

        for (TmdbVO vo : tmdbDAO.selectContentList()) {

            if (!contentType.equals(vo.getContentType())) {
                continue;
            }

            JsonNode root = getDetailRoot(
                    vo.getTmdbId(), contentType, true);

            if ("MOVIE".equals(contentType)) {
                fillMovieDetail(vo, root);
            } else {
                fillTvDetail(vo, root);
            }

            tmdbDAO.updateContentDetail(vo);

            Integer contentNo = tmdbDAO.findContentNo(
                    vo.getTmdbId(), contentType);

            if (contentNo != null) {
                savePeople(contentNo, contentType, root);
            }

            updateCount++;
        }

        return updateCount;
    }

    private void fillMovieDetail(TmdbVO vo, JsonNode root) {
        vo.setGenreText(parseGenreText(root.path("genres")));
        vo.setRuntime(nullableInt(root.path("runtime")));
        vo.setEpisodeCount(null);
        vo.setDirector(limitLength(
                parseDirector(root.path("credits").path("crew")), 100));
        vo.setCastNames(limitLength(
                parseCastNames(root.path("credits").path("cast")), 500));
        vo.setAgeRating(extractMovieAgeRating(root));
    }

    private void fillTvDetail(TmdbVO vo, JsonNode root) {
        vo.setGenreText(parseGenreText(root.path("genres")));
        vo.setRuntime(parseTvRuntime(root.path("episode_run_time")));
        vo.setEpisodeCount(nullableInt(root.path("number_of_episodes")));
        vo.setDirector(limitLength(
                parseTvCreator(root.path("created_by")), 100));
        vo.setCastNames(limitLength(
                parseCastNames(root.path("credits").path("cast")), 500));
        vo.setAgeRating(extractTvAgeRating(root));
    }

    @Override
    public int loadMoviePlatformData() {
        return loadPlatformData("MOVIE");
    }

    @Override
    public int loadTvPlatformData() {
        return loadPlatformData("TV");
    }

    private int loadPlatformData(String contentType) {

        int saveCount = 0;

        for (TmdbVO vo : tmdbDAO.selectContentList()) {

            if (!contentType.equals(vo.getContentType())) {
                continue;
            }

            Integer contentNo = tmdbDAO.findContentNo(
                    vo.getTmdbId(), contentType);

            if (contentNo == null) {
                continue;
            }

            saveCount += savePlatformRelations(
                    contentNo,
                    vo.getTmdbId(),
                    contentType);
        }

        return saveCount;
    }

    @Override
    public int loadMovieFullData() {
        return loadMovieData()
                + updateMovieDetailData()
                + loadMoviePlatformData();
    }

    @Override
    public int loadTvFullData() {
        return loadTvData()
                + updateTvDetailData()
                + loadTvPlatformData();
    }

    @Override
    public int loadAllData() {
        return loadMovieFullData() + loadTvFullData();
    }

    @Override
    public List<SearchResultVO> searchMulti(String keyword, int page) {
        return searchMulti(
                keyword,
                page,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList());
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
            return getPopularKrOttContent(
                    page, platformList, categoryList, genreList);
        }

        int safePage = page <= 0 ? 1 : page;
        String encodedKeyword = URLEncoder.encode(
                keyword.trim(), StandardCharsets.UTF_8);

        String url = tmdbApiBaseUrl
                + "/search/multi"
                + "?query=" + encodedKeyword
                + "&language=" + tmdbApiLanguage
                + "&page=" + safePage
                + "&include_adult=false";

        JsonNode results = callTmdbApi(url).path("results");

        if (!results.isArray()) {
            return resultList;
        }

        List<String> normalizedPlatforms =
                normalizePlatformList(platformList);

        for (JsonNode item : results) {

            if (isAdultContent(item)) {
                continue;
            }

            String mediaType = item.path("media_type").asText();

            if (!"movie".equals(mediaType)
                    && !"tv".equals(mediaType)) {
                continue;
            }

            String contentType =
                    "movie".equals(mediaType) ? "MOVIE" : "TV";

            if (!matchesCategory(
                    contentType,
                    item.path("genre_ids"),
                    categoryList)
                    || !matchesGenre(
                            contentType,
                            item.path("genre_ids"),
                            genreList)) {
                continue;
            }

            long tmdbId = item.path("id").asLong();

            if (tmdbId <= 0) {
                continue;
            }

            List<String> supportedPlatforms =
                    getSupportedKrPlatformNameList(
                            tmdbId, mediaType);

            if (supportedPlatforms.isEmpty()
                    || !matchesPlatform(
                            supportedPlatforms,
                            normalizedPlatforms)) {
                continue;
            }

            resultList.add(
                    createSearchResultVO(item, contentType));
        }

        return resultList;
    }

    @Override
    public List<SearchResultVO> getPopularKrOttContent(
            int page,
            List<String> platformList,
            List<String> categoryList,
            List<String> genreList) {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        int safePage = page <= 0 ? 1 : page;

        if (shouldIncludeMovies(categoryList)) {
            resultList.addAll(discoverContent(
                    "movie",
                    "MOVIE",
                    safePage,
                    platformList,
                    categoryList,
                    genreList));
        }

        if (shouldIncludeTv(categoryList)) {
            resultList.addAll(discoverContent(
                    "tv",
                    "TV",
                    safePage,
                    platformList,
                    categoryList,
                    genreList));
        }

        resultList.sort(Comparator.comparing(
                SearchResultVO::getPopularity,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return resultList;
    }

    private List<SearchResultVO> discoverContent(
            String apiType,
            String contentType,
            int page,
            List<String> platformList,
            List<String> categoryList,
            List<String> genreList) {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        String providerIds = getSupportedProviderIdText(
                apiType,
                normalizePlatformList(platformList));

        if (providerIds.isBlank()) {
            return resultList;
        }

        String url = tmdbApiBaseUrl
                + "/discover/" + apiType
                + "?language=" + tmdbApiLanguage
                + "&region=" + tmdbApiRegion
                + "&watch_region=" + tmdbApiRegion
                + "&include_adult=false"
                + ("movie".equals(apiType) ? "&include_video=false" : "")
                + "&with_watch_monetization_types=flatrate"
                + "&with_watch_providers=" + providerIds
                + "&sort_by=popularity.desc"
                + "&page=" + page;

        JsonNode results = callTmdbApi(url).path("results");

        if (!results.isArray()) {
            return resultList;
        }

        for (JsonNode item : results) {

            if (isAdultContent(item)
                    || !matchesCategory(
                            contentType,
                            item.path("genre_ids"),
                            categoryList)
                    || !matchesGenre(
                            contentType,
                            item.path("genre_ids"),
                            genreList)) {
                continue;
            }

            resultList.add(
                    createSearchResultVO(item, contentType));
        }

        return resultList;
    }

    @Override
    public List<SearchResultVO> getMainPopularContent() {
        return getCombinedMainContent(
                "/movie/popular",
                "/tv/popular",
                true,
                MAIN_POPULAR_LIMIT);
    }

    @Override
    public List<SearchResultVO> getMainTodayContent() {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        String url = tmdbApiBaseUrl
                + "/trending/all/day"
                + "?language=" + tmdbApiLanguage;

        JsonNode results = callTmdbApi(url).path("results");

        if (!results.isArray()) {
            return resultList;
        }

        for (JsonNode item : results) {

            if (isAdultContent(item)) {
                continue;
            }

            String mediaType = item.path("media_type").asText();

            if (!"movie".equals(mediaType)
                    && !"tv".equals(mediaType)) {
                continue;
            }

            String contentType =
                    "movie".equals(mediaType) ? "MOVIE" : "TV";

            resultList.add(
                    createSearchResultVO(item, contentType));

            if (resultList.size() >= MAIN_SLIDER_LIMIT) {
                break;
            }
        }

        return resultList;
    }

    @Override
    public List<SearchResultVO> getMainRecommendedContent() {
        return getCombinedMainContent(
                "/movie/top_rated",
                "/tv/top_rated",
                false,
                MAIN_SLIDER_LIMIT);
    }

    private List<SearchResultVO> getCombinedMainContent(
            String moviePath,
            String tvPath,
            boolean sortByPopularity,
            int limit) {

        List<SearchResultVO> resultList =
                new ArrayList<SearchResultVO>();

        addMainItems(
                resultList,
                moviePath,
                "MOVIE");

        addMainItems(
                resultList,
                tvPath,
                "TV");

        Comparator<SearchResultVO> comparator;

        if (sortByPopularity) {
            comparator = Comparator.comparing(
                    SearchResultVO::getPopularity,
                    Comparator.nullsLast(
                            Comparator.reverseOrder()));
        } else {
            comparator = Comparator.comparing(
                    SearchResultVO::getTmdbScore,
                    Comparator.nullsLast(
                            Comparator.reverseOrder()));
        }

        resultList.sort(comparator);

        return resultList.size() <= limit
                ? resultList
                : new ArrayList<SearchResultVO>(
                        resultList.subList(0, limit));
    }

    private void addMainItems(
            List<SearchResultVO> resultList,
            String apiPath,
            String contentType) {

        String url = tmdbApiBaseUrl
                + apiPath
                + "?language=" + tmdbApiLanguage
                + "&region=" + tmdbApiRegion
                + "&page=1";

        JsonNode results = callTmdbApi(url).path("results");

        if (!results.isArray()) {
            return;
        }

        for (JsonNode item : results) {

            if (isAdultContent(item)) {
                continue;
            }

            resultList.add(
                    createSearchResultVO(item, contentType));
        }
    }

    private SearchResultVO createSearchResultVO(
            JsonNode item,
            String contentType) {

        SearchResultVO vo = new SearchResultVO();
        vo.setTmdbId(item.path("id").asLong());
        vo.setContentType(contentType);

        if ("MOVIE".equals(contentType)) {
            vo.setTitle(firstNonBlank(
                    item.path("title").asText(null),
                    item.path("original_title").asText(null)));
            vo.setOriginalTitle(
                    item.path("original_title").asText(null));
            vo.setReleaseDate(
                    item.path("release_date").asText(null));
        } else {
            vo.setTitle(firstNonBlank(
                    item.path("name").asText(null),
                    item.path("original_name").asText(null)));
            vo.setOriginalTitle(
                    item.path("original_name").asText(null));
            vo.setReleaseDate(
                    item.path("first_air_date").asText(null));
        }

        vo.setOverview(item.path("overview").asText(null));
        vo.setPosterPath(item.path("poster_path").asText(null));
        vo.setBackdropPath(item.path("backdrop_path").asText(null));
        vo.setGenreText(convertGenreIdsToText(
                item.path("genre_ids"), contentType));
        vo.setTmdbScore(nullableDouble(
                item.path("vote_average")));
        vo.setPopularity(nullableDouble(
                item.path("popularity")));

        return vo;
    }

    @Override
    public ContentVO getDetailForSave(
            Long tmdbId,
            String contentType) {

        if (tmdbId == null) {
            throw new IllegalArgumentException(
                    "TMDB ID가 없습니다.");
        }

        String normalizedType = contentType == null
                ? ""
                : contentType.trim().toUpperCase(Locale.ROOT);

        if ("MOVIE".equals(normalizedType)) {
            return createMovieContentVO(tmdbId);
        }

        if ("TV".equals(normalizedType)) {
            return createTvContentVO(tmdbId);
        }

        throw new IllegalArgumentException(
                "지원하지 않는 콘텐츠 타입입니다: "
                + contentType);
    }

    @Override
    public void saveContentPlatform(ContentVO content) {

        if (content == null
                || content.getContentNo() <= 0
                || content.getTmdbId() == null
                || content.getContentType() == null) {
            return;
        }

        savePlatformRelations(
                content.getContentNo(),
                content.getTmdbId(),
                content.getContentType());
    }

    private int savePlatformRelations(
            Integer contentNo,
            Long tmdbId,
            String contentType) {

        String apiType =
                "MOVIE".equals(contentType) ? "movie" : "tv";

        JsonNode flatrate = callTmdbApi(
                tmdbApiBaseUrl
                + "/" + apiType
                + "/" + tmdbId
                + "/watch/providers")
                .path("results")
                .path(tmdbApiRegion)
                .path("flatrate");

        if (!flatrate.isArray()) {
            return 0;
        }

        int saveCount = 0;

        for (JsonNode provider : flatrate) {

            String platformName = convertTmdbProviderName(
                    provider.path("provider_name").asText(null));

            if (platformName == null) {
                continue;
            }

            Integer platformNo =
                    tmdbDAO.findPlatformNo(platformName);

            if (platformNo == null
                    || tmdbDAO.existsContentPlatform(
                            contentNo, platformNo) > 0) {
                continue;
            }

            tmdbDAO.insertContentPlatform(
                    contentNo, platformNo);
            saveCount++;
        }

        return saveCount;
    }

    @Override
    public void saveContentPeople(ContentVO content) {

        if (content == null
                || content.getContentNo() <= 0
                || content.getTmdbId() == null
                || content.getContentType() == null) {
            return;
        }

        String contentType =
                content.getContentType()
                        .trim()
                        .toUpperCase(Locale.ROOT);

        JsonNode root = getDetailRoot(
                content.getTmdbId(),
                contentType,
                true);

        savePeople(
                content.getContentNo(),
                contentType,
                root);
    }

    private void savePeople(
            Integer contentNo,
            String contentType,
            JsonNode root) {

        saveActorData(
                contentNo,
                root.path("credits").path("cast"));

        if ("MOVIE".equals(contentType)) {
            saveMovieDirectorData(
                    contentNo,
                    root.path("credits").path("crew"));
        } else {
            saveTvCreatorData(
                    contentNo,
                    root.path("created_by"));

            saveMovieDirectorData(
                    contentNo,
                    root.path("credits").path("crew"));
        }
    }

    private ContentVO createMovieContentVO(Long tmdbId) {

        JsonNode root =
                getDetailRoot(tmdbId, "MOVIE", true);

        ContentVO vo = new ContentVO();
        vo.setTmdbId(root.path("id").asLong());
        vo.setContentType("MOVIE");
        vo.setTitle(firstNonBlank(
                root.path("title").asText(null),
                root.path("original_title").asText(null)));
        vo.setOriginalTitle(
                root.path("original_title").asText(null));
        vo.setOverview(root.path("overview").asText(null));
        vo.setPosterPath(root.path("poster_path").asText(null));
        vo.setBackdropPath(root.path("backdrop_path").asText(null));
        vo.setReleaseDate(parseDate(
                root.path("release_date").asText(null)));
        vo.setGenreText(parseGenreText(root.path("genres")));
        vo.setRuntime(nullableInt(root.path("runtime")));
        vo.setEpisodeCount(null);
        vo.setDirector(limitLength(
                parseDirector(root.path("credits").path("crew")),
                100));
        vo.setCastNames(limitLength(
                parseCastNames(root.path("credits").path("cast")),
                500));
        vo.setAgeRating(extractMovieAgeRating(root));
        vo.setTmdbScore(nullableDouble(
                root.path("vote_average")));

        return vo;
    }

    private ContentVO createTvContentVO(Long tmdbId) {

        JsonNode root =
                getDetailRoot(tmdbId, "TV", true);

        ContentVO vo = new ContentVO();
        vo.setTmdbId(root.path("id").asLong());
        vo.setContentType("TV");
        vo.setTitle(firstNonBlank(
                root.path("name").asText(null),
                root.path("original_name").asText(null)));
        vo.setOriginalTitle(
                root.path("original_name").asText(null));
        vo.setOverview(root.path("overview").asText(null));
        vo.setPosterPath(root.path("poster_path").asText(null));
        vo.setBackdropPath(root.path("backdrop_path").asText(null));
        vo.setReleaseDate(parseDate(
                root.path("first_air_date").asText(null)));
        vo.setGenreText(parseGenreText(root.path("genres")));
        vo.setRuntime(parseTvRuntime(
                root.path("episode_run_time")));
        vo.setEpisodeCount(nullableInt(
                root.path("number_of_episodes")));
        vo.setDirector(limitLength(
                parseTvCreator(root.path("created_by")),
                100));
        vo.setCastNames(limitLength(
                parseCastNames(root.path("credits").path("cast")),
                500));
        vo.setAgeRating(extractTvAgeRating(root));
        vo.setTmdbScore(nullableDouble(
                root.path("vote_average")));

        return vo;
    }

    private JsonNode getDetailRoot(
            Long tmdbId,
            String contentType,
            boolean includeCredits) {

        if ("MOVIE".equals(contentType)) {
            return callTmdbApi(
                    tmdbApiBaseUrl
                    + "/movie/" + tmdbId
                    + "?language=" + tmdbApiLanguage
                    + (includeCredits
                            ? "&append_to_response=credits,release_dates"
                            : ""));
        }

        return callTmdbApi(
                tmdbApiBaseUrl
                + "/tv/" + tmdbId
                + "?language=" + tmdbApiLanguage
                + (includeCredits
                        ? "&append_to_response=credits,content_ratings"
                        : ""));
    }

    private void saveActorData(
            Integer contentNo,
            JsonNode castNode) {

        if (castNode == null || !castNode.isArray()) {
            return;
        }

        int displayOrder = 1;

        for (JsonNode cast : castNode) {

            if (displayOrder > CAST_SAVE_LIMIT) {
                break;
            }

            long tmdbActorId = cast.path("id").asLong();
            String actorName = cast.path("name").asText(null);

            if (tmdbActorId <= 0
                    || actorName == null
                    || actorName.isBlank()) {
                continue;
            }

            Integer actorNo =
                    tmdbDAO.findActorNoByTmdbId(tmdbActorId);

            if (actorNo == null) {
                ActorVO actor = new ActorVO();
                actor.setTmdbActorId(tmdbActorId);
                actor.setActorName(
                        limitLength(actorName, 100));
                actor.setProfilePath(
                        cast.path("profile_path").asText(null));
                tmdbDAO.insertActor(actor);
                actorNo =
                        tmdbDAO.findActorNoByTmdbId(tmdbActorId);
            }

            if (actorNo != null
                    && tmdbDAO.existsContentActor(
                            contentNo, actorNo) == 0) {
                tmdbDAO.insertContentActor(
                        contentNo,
                        actorNo,
                        limitLength(
                                cast.path("character").asText(null),
                                100),
                        displayOrder);
            }

            displayOrder++;
        }
    }

    private void saveMovieDirectorData(
            Integer contentNo,
            JsonNode crewNode) {

        if (crewNode == null || !crewNode.isArray()) {
            return;
        }

        int displayOrder = 1;

        for (JsonNode crew : crewNode) {

            if (!"Director".equals(
                    crew.path("job").asText(null))) {
                continue;
            }

            saveDirectorRelation(
                    contentNo,
                    crew,
                    "DIRECTOR",
                    displayOrder++);
        }
    }

    private void saveTvCreatorData(
            Integer contentNo,
            JsonNode createdByNode) {

        if (createdByNode == null
                || !createdByNode.isArray()) {
            return;
        }

        int displayOrder = 1;

        for (JsonNode creator : createdByNode) {
            saveDirectorRelation(
                    contentNo,
                    creator,
                    "CREATOR",
                    displayOrder++);
        }
    }

    private void saveDirectorRelation(
            Integer contentNo,
            JsonNode personNode,
            String directorType,
            Integer displayOrder) {

        long tmdbDirectorId =
                personNode.path("id").asLong();

        String directorName =
                personNode.path("name").asText(null);

        if (tmdbDirectorId <= 0
                || directorName == null
                || directorName.isBlank()) {
            return;
        }

        Integer directorNo =
                tmdbDAO.findDirectorNoByTmdbId(
                        tmdbDirectorId);

        if (directorNo == null) {
            DirectorVO director = new DirectorVO();
            director.setTmdbDirectorId(tmdbDirectorId);
            director.setDirectorName(
                    limitLength(directorName, 100));
            director.setProfilePath(
                    personNode.path("profile_path")
                            .asText(null));
            tmdbDAO.insertDirector(director);

            directorNo =
                    tmdbDAO.findDirectorNoByTmdbId(
                            tmdbDirectorId);
        }

        if (directorNo != null
                && tmdbDAO.existsContentDirector(
                        contentNo, directorNo) == 0) {
            tmdbDAO.insertContentDirector(
                    contentNo,
                    directorNo,
                    directorType,
                    displayOrder);
        }
    }

    private List<String> getSupportedKrPlatformNameList(
            Long tmdbId,
            String mediaType) {

        List<String> result =
                new ArrayList<String>();

        JsonNode flatrate = callTmdbApi(
                tmdbApiBaseUrl
                + "/" + mediaType
                + "/" + tmdbId
                + "/watch/providers")
                .path("results")
                .path(tmdbApiRegion)
                .path("flatrate");

        if (!flatrate.isArray()) {
            return result;
        }

        for (JsonNode provider : flatrate) {

            String platformName =
                    convertTmdbProviderName(
                            provider.path("provider_name")
                                    .asText(null));

            if (platformName != null
                    && !result.contains(platformName)) {
                result.add(platformName);
            }
        }

        return result;
    }

    private String getSupportedProviderIdText(
            String apiType,
            List<String> selectedPlatforms) {

        Set<String> ids = new LinkedHashSet<String>();

        JsonNode results = callTmdbApi(
                tmdbApiBaseUrl
                + "/watch/providers/" + apiType
                + "?language=" + tmdbApiLanguage
                + "&watch_region=" + tmdbApiRegion)
                .path("results");

        if (!results.isArray()) {
            return "";
        }

        for (JsonNode provider : results) {

            String platformName =
                    convertTmdbProviderName(
                            provider.path("provider_name")
                                    .asText(null));

            int providerId =
                    provider.path("provider_id").asInt();

            if (platformName == null || providerId <= 0) {
                continue;
            }

            if (selectedPlatforms != null
                    && !selectedPlatforms.isEmpty()
                    && !selectedPlatforms.contains(
                            platformName)) {
                continue;
            }

            ids.add(String.valueOf(providerId));
        }

        return String.join("|", ids);
    }

    private String convertTmdbProviderName(String name) {

        if (name == null) {
            return null;
        }

        String normalized = name.trim()
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

    private boolean matchesPlatform(
            List<String> supportedPlatforms,
            List<String> selectedPlatforms) {

        if (selectedPlatforms == null
                || selectedPlatforms.isEmpty()) {
            return true;
        }

        for (String selected : selectedPlatforms) {
            if (supportedPlatforms.contains(selected)) {
                return true;
            }
        }

        return false;
    }

    private List<String> normalizePlatformList(
            List<String> platformList) {

        List<String> result = new ArrayList<String>();

        if (platformList == null) {
            return result;
        }

        for (String platform : platformList) {

            if (platform == null) {
                continue;
            }

            String normalized =
                    normalizePlatformInput(platform);

            if (normalized != null
                    && !result.contains(normalized)) {
                result.add(normalized);
            }
        }

        return result;
    }

    private String normalizePlatformInput(String platform) {

        String value = platform.trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "")
                .replace("+", "");

        if (value.contains("netflix")) {
            return "Netflix";
        }
        if (value.contains("tving")) {
            return "Tving";
        }
        if (value.contains("wavve")) {
            return "Wavve";
        }
        if (value.contains("disney")) {
            return "Disney+";
        }
        if (value.contains("watcha")) {
            return "Watcha";
        }
        if (value.contains("coupang")) {
            return "Coupangplay";
        }

        return null;
    }

    private boolean matchesCategory(
            String contentType,
            JsonNode genreIds,
            List<String> categoryList) {

        if (categoryList == null || categoryList.isEmpty()) {
            return true;
        }

        for (String category : categoryList) {

            if (category == null) {
                continue;
            }

            String value =
                    category.trim().toUpperCase(Locale.ROOT);

            if ("MOVIE".equals(contentType)
                    && "MOVIE".equals(value)) {
                return true;
            }

            if (!"TV".equals(contentType)) {
                continue;
            }

            if ("ANIMATION".equals(value)
                    && containsGenreId(genreIds, 16)) {
                return true;
            }

            if (("ENTERTAINMENT".equals(value)
                    || "REALITY".equals(value)
                    || "VARIETY".equals(value))
                    && (containsGenreId(genreIds, 10764)
                    || containsGenreId(genreIds, 10767))) {
                return true;
            }

            if (("DOCUMENTARY".equals(value)
                    || "DOCU".equals(value))
                    && containsGenreId(genreIds, 99)) {
                return true;
            }

            if ("DRAMA".equals(value)
                    && !containsGenreId(genreIds, 16)
                    && !containsGenreId(genreIds, 10764)
                    && !containsGenreId(genreIds, 10767)
                    && !containsGenreId(genreIds, 99)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesGenre(
            String contentType,
            JsonNode genreIds,
            List<String> genreList) {

        if (genreList == null || genreList.isEmpty()) {
            return true;
        }

        for (String genre : genreList) {

            if (genre == null) {
                continue;
            }

            String value =
                    genre.trim().toUpperCase(Locale.ROOT);

            if ("MOVIE".equals(contentType)) {

                if ("ACTION".equals(value)
                        && containsGenreId(genreIds, 28)) {
                    return true;
                }
                if ("COMEDY".equals(value)
                        && containsGenreId(genreIds, 35)) {
                    return true;
                }
                if ("THRILLER".equals(value)
                        && containsGenreId(genreIds, 53)) {
                    return true;
                }
                if ("ROMANCE".equals(value)
                        && containsGenreId(genreIds, 10749)) {
                    return true;
                }
                if ("CRIME".equals(value)
                        && containsGenreId(genreIds, 80)) {
                    return true;
                }
                if ("FANTASY".equals(value)
                        && containsGenreId(genreIds, 14)) {
                    return true;
                }
                if ("HORROR".equals(value)
                        && containsGenreId(genreIds, 27)) {
                    return true;
                }
                if ("MYSTERY".equals(value)
                        && containsGenreId(genreIds, 9648)) {
                    return true;
                }
                if (("SF".equals(value)
                        || "SCIENCE_FICTION".equals(value))
                        && containsGenreId(genreIds, 878)) {
                    return true;
                }
                if ("DRAMA".equals(value)
                        && containsGenreId(genreIds, 18)) {
                    return true;
                }

            } else {

                if ("ACTION".equals(value)
                        && containsGenreId(genreIds, 10759)) {
                    return true;
                }
                if ("COMEDY".equals(value)
                        && containsGenreId(genreIds, 35)) {
                    return true;
                }
                if ("THRILLER".equals(value)
                        && (containsGenreId(genreIds, 9648)
                        || containsGenreId(genreIds, 80)
                        || containsGenreId(genreIds, 10765))) {
                    return true;
                }
                if ("ROMANCE".equals(value)
                        && (containsGenreId(genreIds, 18)
                        || containsGenreId(genreIds, 10766))) {
                    return true;
                }
                if ("CRIME".equals(value)
                        && containsGenreId(genreIds, 80)) {
                    return true;
                }
                if ("FANTASY".equals(value)
                        && containsGenreId(genreIds, 10765)) {
                    return true;
                }
                if ("HORROR".equals(value)
                        && (containsGenreId(genreIds, 9648)
                        || containsGenreId(genreIds, 10765))) {
                    return true;
                }
                if ("MYSTERY".equals(value)
                        && containsGenreId(genreIds, 9648)) {
                    return true;
                }
                if (("SF".equals(value)
                        || "SCIENCE_FICTION".equals(value))
                        && containsGenreId(genreIds, 10765)) {
                    return true;
                }
                if ("DRAMA".equals(value)
                        && (containsGenreId(genreIds, 18)
                        || containsGenreId(genreIds, 10766))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean shouldIncludeMovies(
            List<String> categoryList) {

        if (categoryList == null || categoryList.isEmpty()) {
            return true;
        }

        for (String category : categoryList) {
            if (category != null
                    && "MOVIE".equalsIgnoreCase(
                            category.trim())) {
                return true;
            }
        }

        return false;
    }

    private boolean shouldIncludeTv(
            List<String> categoryList) {

        if (categoryList == null || categoryList.isEmpty()) {
            return true;
        }

        for (String category : categoryList) {

            if (category == null) {
                continue;
            }

            String value =
                    category.trim().toUpperCase(Locale.ROOT);

            if (!"MOVIE".equals(value)) {
                return true;
            }
        }

        return false;
    }

    private boolean containsGenreId(
            JsonNode genreIds,
            int genreId) {

        if (genreIds == null || !genreIds.isArray()) {
            return false;
        }

        for (JsonNode node : genreIds) {
            if (node.asInt() == genreId) {
                return true;
            }
        }

        return false;
    }

    private boolean isAdultContent(JsonNode item) {
        return item != null
                && item.path("adult").asBoolean(false);
    }

    private String convertGenreIdsToText(
            JsonNode genreIds,
            String contentType) {

        if (genreIds == null || !genreIds.isArray()) {
            return null;
        }

        List<String> names = new ArrayList<String>();

        for (JsonNode idNode : genreIds) {

            String name = resolveGenreName(
                    idNode.asInt(), contentType);

            if (name != null && !names.contains(name)) {
                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private String resolveGenreName(
            int genreId,
            String contentType) {

        return "MOVIE".equals(contentType)
                ? MOVIE_GENRES.get(genreId)
                : TV_GENRES.get(genreId);
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
        map.put(10770, "TV 영화");
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

    private JsonNode callTmdbApi(String url) {

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> entity =
                    new HttpEntity<String>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            String.class);

            return objectMapper.readTree(
                    response.getBody());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "TMDB API 호출 실패: " + url, e);
        }
    }

    private LocalDate parseDate(String text) {

        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return null;
        }
    }

    private String parseGenreText(JsonNode genresNode) {

        if (genresNode == null || !genresNode.isArray()) {
            return null;
        }

        List<String> names = new ArrayList<String>();

        for (JsonNode genre : genresNode) {
            String name = genre.path("name").asText(null);

            if (name != null && !name.isBlank()) {
                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private String parseDirector(JsonNode crewNode) {

        if (crewNode == null || !crewNode.isArray()) {
            return null;
        }

        List<String> names = new ArrayList<String>();

        for (JsonNode crew : crewNode) {

            if (!"Director".equals(
                    crew.path("job").asText(null))) {
                continue;
            }

            String name = crew.path("name").asText(null);

            if (name != null
                    && !name.isBlank()
                    && !names.contains(name)) {
                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private String parseTvCreator(JsonNode createdByNode) {

        if (createdByNode == null
                || !createdByNode.isArray()) {
            return null;
        }

        List<String> names = new ArrayList<String>();

        for (JsonNode creator : createdByNode) {
            String name =
                    creator.path("name").asText(null);

            if (name != null
                    && !name.isBlank()
                    && !names.contains(name)) {
                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private String parseCastNames(JsonNode castNode) {

        if (castNode == null || !castNode.isArray()) {
            return null;
        }

        List<String> names = new ArrayList<String>();

        for (JsonNode cast : castNode) {

            if (names.size() >= CAST_SAVE_LIMIT) {
                break;
            }

            String name = cast.path("name").asText(null);

            if (name != null
                    && !name.isBlank()
                    && !names.contains(name)) {
                names.add(name);
            }
        }

        return names.isEmpty()
                ? null
                : String.join(", ", names);
    }

    private Integer parseTvRuntime(JsonNode runtimeNode) {

        if (runtimeNode == null
                || !runtimeNode.isArray()
                || runtimeNode.isEmpty()) {
            return null;
        }

        return nullableInt(runtimeNode.get(0));
    }

    private String extractMovieAgeRating(JsonNode root) {

        JsonNode results =
                root.path("release_dates").path("results");

        if (!results.isArray()) {
            return "UNKNOWN";
        }

        String usRating = null;

        for (JsonNode country : results) {

            String countryCode =
                    country.path("iso_3166_1").asText(null);

            if (!"KR".equals(countryCode)
                    && !"US".equals(countryCode)) {
                continue;
            }

            JsonNode dates =
                    country.path("release_dates");

            if (!dates.isArray()) {
                continue;
            }

            for (JsonNode item : dates) {

                String converted = convertAgeRating(
                        countryCode,
                        item.path("certification")
                                .asText(null),
                        false);

                if (converted == null) {
                    continue;
                }

                if ("KR".equals(countryCode)) {
                    return converted;
                }

                if (usRating == null) {
                    usRating = converted;
                }
            }
        }

        return usRating == null
                ? "UNKNOWN"
                : usRating;
    }

    private String extractTvAgeRating(JsonNode root) {

        JsonNode results =
                root.path("content_ratings").path("results");

        if (!results.isArray()) {
            return "UNKNOWN";
        }

        String usRating = null;

        for (JsonNode country : results) {

            String countryCode =
                    country.path("iso_3166_1").asText(null);

            if (!"KR".equals(countryCode)
                    && !"US".equals(countryCode)) {
                continue;
            }

            String converted = convertAgeRating(
                    countryCode,
                    country.path("rating").asText(null),
                    true);

            if (converted == null) {
                continue;
            }

            if ("KR".equals(countryCode)) {
                return converted;
            }

            if (usRating == null) {
                usRating = converted;
            }
        }

        return usRating == null
                ? "UNKNOWN"
                : usRating;
    }

    private String convertAgeRating(
            String countryCode,
            String certification,
            boolean tv) {

        if (certification == null
                || certification.isBlank()) {
            return null;
        }

        String value = certification.trim()
                .toUpperCase(Locale.ROOT)
                .replace(" ", "")
                .replace("-", "")
                .replace("_", "");

        if ("KR".equals(countryCode)) {
            if (value.contains("ALL")
                    || value.contains("전체")
                    || "7".equals(value)
                    || value.contains("7세")) {
                return "ALL";
            }
            if (value.contains("12")) {
                return "12";
            }
            if (value.contains("15")) {
                return "15";
            }
            if (value.contains("18")
                    || value.contains("19")
                    || value.contains("청소년")) {
                return "18";
            }
            return "UNKNOWN";
        }

        if (!"US".equals(countryCode)) {
            return null;
        }

        if (tv) {
            if ("TVY".equals(value)
                    || "TVY7".equals(value)
                    || "TVG".equals(value)) {
                return "ALL";
            }
            if ("TVPG".equals(value)) {
                return "12";
            }
            if ("TV14".equals(value)) {
                return "15";
            }
            if ("TVMA".equals(value)) {
                return "18";
            }
        } else {
            if ("G".equals(value)) {
                return "ALL";
            }
            if ("PG".equals(value)) {
                return "12";
            }
            if ("PG13".equals(value)) {
                return "15";
            }
            if ("R".equals(value)
                    || "NC17".equals(value)) {
                return "18";
            }
        }

        return "UNKNOWN";
    }

    private String firstNonBlank(
            String first,
            String second) {

        return first != null && !first.isBlank()
                ? first
                : second;
    }

    private Integer nullableInt(JsonNode node) {

        if (node == null
                || node.isMissingNode()
                || node.isNull()) {
            return null;
        }

        int value = node.asInt();

        return value <= 0 ? null : value;
    }

    private Double nullableDouble(JsonNode node) {

        if (node == null
                || node.isMissingNode()
                || node.isNull()) {
            return null;
        }

        return node.asDouble();
    }

    private String limitLength(
            String value,
            int maxLength) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.length() <= maxLength
                ? trimmed
                : trimmed.substring(0, maxLength);
    }
}
