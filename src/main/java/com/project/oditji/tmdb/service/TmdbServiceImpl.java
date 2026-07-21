package com.project.oditji.tmdb.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
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

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.FilmographyVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.TmdbVO;

@Service
public class TmdbServiceImpl implements TmdbService {

    private static final int MOVIE_LOAD_PAGE_COUNT = 10;
    private static final int TV_LOAD_PAGE_COUNT = 15;
    private static final int CAST_SAVE_LIMIT = 5;

    private static final Map<Integer, String> MOVIE_GENRES =
            createMovieGenreMap();

    private static final Map<Integer, String> TV_GENRES =
            createTvGenreMap();

    private final TmdbDAO tmdbDAO;
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

    public TmdbServiceImpl(
            TmdbDAO tmdbDAO,
            JsonMapper jsonMapper) {
        this.tmdbDAO = tmdbDAO;
        this.restTemplate = new RestTemplate();
        this.jsonMapper = jsonMapper;
    }

    @Override
    public int loadMovieData() {
        return loadBasicData(
                "movie",
                "MOVIE",
                MOVIE_LOAD_PAGE_COUNT);
    }

    @Override
    public int loadTvData() {
        return loadBasicData(
                "tv",
                "TV",
                TV_LOAD_PAGE_COUNT);
    }

    private int loadBasicData(
            String apiType,
            String contentType,
            int loadPageCount) {

        int saveCount = 0;
        String providerIds = getSupportedProviderIdText(apiType, null);

        if (providerIds.isBlank()) {
            return 0;
        }

        for (int page = 1; page <= loadPageCount; page++) {

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

                if (shouldExcludeContent(item)) {
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
                    item.path("title").asString(null),
                    item.path("original_title").asString(null)));
            vo.setOriginalTitle(item.path("original_title").asString(null));
            vo.setReleaseDate(parseDate(
                    item.path("release_date").asString(null)));
        } else {
            vo.setTitle(firstNonBlank(
                    item.path("name").asString(null),
                    item.path("original_name").asString(null)));
            vo.setOriginalTitle(item.path("original_name").asString(null));
            vo.setReleaseDate(parseDate(
                    item.path("first_air_date").asString(null)));
        }

        vo.setOverview(item.path("overview").asString(null));
        vo.setPosterPath(item.path("poster_path").asString(null));
        vo.setBackdropPath(item.path("backdrop_path").asString(null));
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
                    provider.path("provider_name").asString(null));

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
                root.path("title").asString(null),
                root.path("original_title").asString(null)));
        vo.setOriginalTitle(
                root.path("original_title").asString(null));
        vo.setOverview(root.path("overview").asString(null));
        vo.setPosterPath(root.path("poster_path").asString(null));
        vo.setBackdropPath(root.path("backdrop_path").asString(null));
        vo.setReleaseDate(parseDate(
                root.path("release_date").asString(null)));
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
                root.path("name").asString(null),
                root.path("original_name").asString(null)));
        vo.setOriginalTitle(
                root.path("original_name").asString(null));
        vo.setOverview(root.path("overview").asString(null));
        vo.setPosterPath(root.path("poster_path").asString(null));
        vo.setBackdropPath(root.path("backdrop_path").asString(null));
        vo.setReleaseDate(parseDate(
                root.path("first_air_date").asString(null)));
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
            String actorName = cast.path("name").asString(null);

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
                        cast.path("profile_path").asString(null));
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
                                cast.path("character").asString(null),
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
                    crew.path("job").asString(null))) {
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
                personNode.path("name").asString(null);

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
                            .asString(null));
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
                                    .asString(null));

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
            return "TVING";
        }

        if (normalized.contains("wavve")) {
            return "Wavve";
        }

        if (normalized.contains("disney")) {
            return "Disney Plus";
        }

        if (normalized.contains("watcha")) {
            return "Watcha";
        }

        if (normalized.contains("coupang")) {
            return "Coupangplay";
        }

        return null;
    }

    private boolean shouldExcludeContent(JsonNode item) {

        if (item == null || item.isNull()) {
            return true;
        }

        if (item.path("adult").asBoolean(false)) {
            return true;
        }

        String title = firstNonBlank(
                item.path("title").asString(null),
                item.path("name").asString(null));

        String originalTitle = firstNonBlank(
                item.path("original_title").asString(null),
                item.path("original_name").asString(null));

        String overview = item.path("overview").asString("");

        String checkText = normalizeBlockedText(
                (title == null ? "" : title)
                + " "
                + (originalTitle == null ? "" : originalTitle)
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
            if (checkText.contains(normalizeBlockedText(keyword))) {
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
                .replaceAll("[^\\p{L}\\p{N}]", "");
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

            return jsonMapper.readTree(
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
            String name = genre.path("name").asString(null);

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
                    crew.path("job").asString(null))) {
                continue;
            }

            String name = crew.path("name").asString(null);

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
                    creator.path("name").asString(null);

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

            String name = cast.path("name").asString(null);

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
                    country.path("iso_3166_1").asString(null);

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
                                .asString(null),
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
                    country.path("iso_3166_1").asString(null);

            if (!"KR".equals(countryCode)
                    && !"US".equals(countryCode)) {
                continue;
            }

            String converted = convertAgeRating(
                    countryCode,
                    country.path("rating").asString(null),
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

    @Override
    public PersonFilmographyVO getPersonFilmography(
            Long tmdbPersonId,
            String role) {

        if (tmdbPersonId == null || tmdbPersonId <= 0) {
            throw new IllegalArgumentException(
                    "유효한 TMDB 인물 ID가 필요합니다.");
        }

        String normalizedRole = role == null
                ? "ACTOR"
                : role.trim().toUpperCase(Locale.ROOT);

        if (!"ACTOR".equals(normalizedRole)
                && !"DIRECTOR".equals(normalizedRole)
                && !"CREATOR".equals(normalizedRole)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 인물 역할입니다: " + role);
        }

        String url = tmdbApiBaseUrl
                + "/person/" + tmdbPersonId
                + "?language=" + tmdbApiLanguage
                + "&append_to_response=combined_credits";

        JsonNode root = callTmdbApi(url);

        PersonFilmographyVO person = new PersonFilmographyVO();
        person.setTmdbPersonId(tmdbPersonId);
        person.setPersonName(root.path("name").asString(null));
        person.setProfilePath(root.path("profile_path").asString(null));
        person.setBiography(root.path("biography").asString(null));
        person.setBirthday(root.path("birthday").asString(null));
        person.setPlaceOfBirth(root.path("place_of_birth").asString(null));
        person.setRole(normalizedRole);

        JsonNode credits = root.path("combined_credits");

        person.setCastList(createCastFilmographyList(
                credits.path("cast")));

        person.setDirectorList(createCrewFilmographyList(
                credits.path("crew"),
                "DIRECTOR"));

        person.setProductionList(createCrewFilmographyList(
                credits.path("crew"),
                "PRODUCTION"));

        return person;
    }

    private List<FilmographyVO> createCastFilmographyList(
            JsonNode castItems) {

        Map<String, FilmographyVO> uniqueMap =
                new LinkedHashMap<String, FilmographyVO>();

        if (castItems == null || !castItems.isArray()) {
            return new ArrayList<FilmographyVO>();
        }

        for (JsonNode item : castItems) {

            FilmographyVO filmography =
                    createFilmographyVO(item);

            if (filmography == null) {
                continue;
            }

            filmography.setParticipationCategory("CAST");
            filmography.setParticipationName(
                    item.path("character").asString(null));

            putFilmographyWithPriority(
                    uniqueMap,
                    item.path("media_type").asString(),
                    filmography);
        }

        return sortFilmographyList(uniqueMap);
    }

    private List<FilmographyVO> createCrewFilmographyList(
            JsonNode crewItems,
            String category) {

        Map<String, FilmographyVO> uniqueMap =
                new LinkedHashMap<String, FilmographyVO>();

        if (crewItems == null || !crewItems.isArray()) {
            return new ArrayList<FilmographyVO>();
        }

        for (JsonNode item : crewItems) {

            String job = item.path("job").asString("");
            String department =
                    item.path("department").asString("");

            boolean matches;

            if ("DIRECTOR".equals(category)) {
                matches = "Director".equalsIgnoreCase(job);
            } else {
                matches = isProductionParticipation(
                        job, department);
            }

            if (!matches) {
                continue;
            }

            FilmographyVO filmography =
                    createFilmographyVO(item);

            if (filmography == null) {
                continue;
            }

            filmography.setParticipationCategory(category);
            filmography.setParticipationName(
                    convertParticipationName(job, department));

            putFilmographyWithPriority(
                    uniqueMap,
                    item.path("media_type").asString(),
                    filmography);
        }

        return sortFilmographyList(uniqueMap);
    }

    private FilmographyVO createFilmographyVO(
            JsonNode item) {

        if (item == null || item.isNull()
                || shouldExcludeContent(item)) {
            return null;
        }

        String mediaType =
                item.path("media_type").asString(null);

        if (!"movie".equals(mediaType)
                && !"tv".equals(mediaType)) {
            return null;
        }

        long tmdbId = item.path("id").asLong();

        if (tmdbId <= 0) {
            return null;
        }

        FilmographyVO filmography =
                new FilmographyVO();

        filmography.setTmdbId(tmdbId);
        filmography.setContentType(
                "movie".equals(mediaType)
                        ? "MOVIE"
                        : "TV");

        filmography.setTitle(firstNonBlank(
                item.path("title").asString(null),
                item.path("name").asString(null)));

        filmography.setOriginalTitle(firstNonBlank(
                item.path("original_title").asString(null),
                item.path("original_name").asString(null)));

        filmography.setPosterPath(
                item.path("poster_path").asString(null));

        filmography.setReleaseDate(firstNonBlank(
                item.path("release_date").asString(null),
                item.path("first_air_date").asString(null)));

        filmography.setTmdbScore(
                nullableDouble(item.path("vote_average")));

        filmography.setPopularity(
                nullableDouble(item.path("popularity")));

        return filmography;
    }

    private void putFilmographyWithPriority(
            Map<String, FilmographyVO> uniqueMap,
            String mediaType,
            FilmographyVO filmography) {

        String key = mediaType + "-"
                + filmography.getTmdbId();

        FilmographyVO existing = uniqueMap.get(key);

        if (existing == null) {
            uniqueMap.put(key, filmography);
            return;
        }

        String mergedParticipation =
                mergeParticipationNames(
                        existing.getParticipationName(),
                        filmography.getParticipationName());

        if (compareFilmographyPriority(
                filmography, existing) < 0) {

            filmography.setParticipationName(
                    mergedParticipation);

            uniqueMap.put(key, filmography);

        } else {

            existing.setParticipationName(
                    mergedParticipation);
        }
    }

    private List<FilmographyVO> sortFilmographyList(
            Map<String, FilmographyVO> uniqueMap) {

        List<FilmographyVO> list =
                new ArrayList<FilmographyVO>(
                        uniqueMap.values());

        list.sort((first, second) -> {

            String firstDate = first.getReleaseDate();
            String secondDate = second.getReleaseDate();

            boolean firstDateEmpty =
                    firstDate == null || firstDate.isBlank();

            boolean secondDateEmpty =
                    secondDate == null || secondDate.isBlank();

            if (firstDateEmpty && secondDateEmpty) {
                return compareNullableDoubleDescending(
                        first.getPopularity(),
                        second.getPopularity());
            }

            if (firstDateEmpty) {
                return 1;
            }

            if (secondDateEmpty) {
                return -1;
            }

            int dateCompare =
                    secondDate.compareTo(firstDate);

            if (dateCompare != 0) {
                return dateCompare;
            }

            return compareNullableDoubleDescending(
                    first.getPopularity(),
                    second.getPopularity());
        });

        return list;
    }

    private boolean isProductionParticipation(
            String job,
            String department) {

        if ("Director".equalsIgnoreCase(job)) {
            return false;
        }

        return "Creator".equalsIgnoreCase(job)
                || "Executive Producer".equalsIgnoreCase(job)
                || "Producer".equalsIgnoreCase(job)
                || "Co-Producer".equalsIgnoreCase(job)
                || "Associate Producer".equalsIgnoreCase(job)
                || "Writer".equalsIgnoreCase(job)
                || "Screenplay".equalsIgnoreCase(job)
                || "Story".equalsIgnoreCase(job)
                || "Novel".equalsIgnoreCase(job)
                || "Original Story".equalsIgnoreCase(job)
                || "Original Music Composer".equalsIgnoreCase(job)
                || "Writing".equalsIgnoreCase(department)
                || "Production".equalsIgnoreCase(department);
    }

    private String convertParticipationName(
            String job,
            String department) {

        if (job == null || job.isBlank()) {
            return department;
        }

        if ("Director".equalsIgnoreCase(job)) {
            return "감독";
        }
        if ("Creator".equalsIgnoreCase(job)) {
            return "크리에이터";
        }
        if ("Executive Producer".equalsIgnoreCase(job)) {
            return "책임 프로듀서";
        }
        if ("Producer".equalsIgnoreCase(job)) {
            return "프로듀서";
        }
        if ("Co-Producer".equalsIgnoreCase(job)) {
            return "공동 프로듀서";
        }
        if ("Associate Producer".equalsIgnoreCase(job)) {
            return "협력 프로듀서";
        }
        if ("Writer".equalsIgnoreCase(job)) {
            return "각본";
        }
        if ("Screenplay".equalsIgnoreCase(job)) {
            return "각색";
        }
        if ("Story".equalsIgnoreCase(job)
                || "Original Story".equalsIgnoreCase(job)) {
            return "원안";
        }
        if ("Novel".equalsIgnoreCase(job)) {
            return "원작";
        }
        if ("Original Music Composer".equalsIgnoreCase(job)) {
            return "음악";
        }

        return job;
    }

    private String mergeParticipationNames(
            String first,
            String second) {

        Set<String> values =
                new LinkedHashSet<String>();

        addParticipationValues(values, first);
        addParticipationValues(values, second);

        return values.isEmpty()
                ? null
                : String.join(", ", values);
    }

    private void addParticipationValues(
            Set<String> values,
            String text) {

        if (text == null || text.isBlank()) {
            return;
        }

        String[] tokens = text.split(",");

        for (String token : tokens) {

            String value = token.trim();

            if (!value.isEmpty()) {
                values.add(value);
            }
        }
    }

    private int compareFilmographyPriority(
            FilmographyVO first,
            FilmographyVO second) {

        boolean firstHasPoster =
                first.getPosterPath() != null
                && !first.getPosterPath().isBlank();

        boolean secondHasPoster =
                second.getPosterPath() != null
                && !second.getPosterPath().isBlank();

        if (firstHasPoster != secondHasPoster) {
            return firstHasPoster ? -1 : 1;
        }

        return compareNullableDoubleDescending(
                first.getPopularity(),
                second.getPopularity());
    }

    private int compareNullableDoubleDescending(
            Double first,
            Double second) {

        if (first == null && second == null) {
            return 0;
        }

        if (first == null) {
            return 1;
        }

        if (second == null) {
            return -1;
        }

        return Double.compare(second, first);
    }

}