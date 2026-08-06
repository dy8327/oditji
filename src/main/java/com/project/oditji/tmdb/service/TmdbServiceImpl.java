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
import com.project.oditji.common.util.TmdbGenreUtil;
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

    private static final String API_TYPE_MOVIE = "movie";
    private static final String CONTENT_TYPE_MOVIE = "MOVIE";
    private static final String QUERY_LANGUAGE = "?language=";
    private static final String JSON_RESULTS = "results";
    private static final String JSON_TITLE = "title";
    private static final String JSON_ORIGINAL_TITLE = "original_title";
    private static final String JSON_RELEASE_DATE = "release_date";
    private static final String JSON_ORIGINAL_NAME = "original_name";
    private static final String JSON_FIRST_AIR_DATE = "first_air_date";
    private static final String JSON_OVERVIEW = "overview";
    private static final String JSON_POSTER_PATH = "poster_path";
    private static final String JSON_BACKDROP_PATH = "backdrop_path";
    private static final String JSON_VOTE_AVERAGE = "vote_average";
    private static final String JSON_GENRES = "genres";
    private static final String JSON_CREDITS = "credits";
    private static final String JSON_CREATED_BY = "created_by";
    private static final String JSON_PROFILE_PATH = "profile_path";
    private static final String JSON_CHARACTER = "character";
    private static final String JSON_MEDIA_TYPE = "media_type";
    private static final String JOB_DIRECTOR = "Director";
    private static final String ROLE_DIRECTOR = "DIRECTOR";
    private static final String AGE_RATING_UNKNOWN = "UNKNOWN";
    private static final String PLATFORM_KEY_NETFLIX = "netflix";
    private static final String PLATFORM_KEY_TVING = "tving";
    private static final String PLATFORM_KEY_WAVVE = "wavve";
    private static final String PLATFORM_KEY_DISNEY = "disney";
    private static final String PLATFORM_KEY_WATCHA = "watcha";
    private static final String PLATFORM_KEY_COUPANG = "coupang";

    private static final Map<Integer, String> MOVIE_GENRES =
            TmdbGenreUtil.movieGenres();

    private static final Map<Integer, String> TV_GENRES =
            TmdbGenreUtil.tvGenres();

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
                API_TYPE_MOVIE,
                CONTENT_TYPE_MOVIE,
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
                    + QUERY_LANGUAGE + tmdbApiLanguage
                    + "&region=" + tmdbApiRegion
                    + "&watch_region=" + tmdbApiRegion
                    + "&include_adult=false"
                    + (API_TYPE_MOVIE.equals(apiType) ? "&include_video=false" : "")
                    + "&with_watch_monetization_types=flatrate"
                    + "&with_watch_providers=" + providerIds
                    + "&sort_by=popularity.desc"
                    + "&page=" + page;

            JsonNode results = callTmdbApi(url).path(JSON_RESULTS);

            if (results.isArray()) {
                for (JsonNode item : results) {

                    long tmdbId = item.path("id").asLong();

                    if (!shouldExcludeContent(item)
                            && tmdbId > 0
                            && tmdbDAO.existsContent(
                                    tmdbId,
                                    contentType
                            ) == 0) {

                        TmdbVO vo =
                                createBasicTmdbVO(
                                        item,
                                        contentType
                                );
                        tmdbDAO.insertContent(vo);
                        saveCount++;
                    }
                }
            }
        }

        return saveCount;
    }

    private TmdbVO createBasicTmdbVO(JsonNode item, String contentType) {

        TmdbVO vo = new TmdbVO();
        vo.setTmdbId(item.path("id").asLong());
        vo.setContentType(contentType);

        if (CONTENT_TYPE_MOVIE.equals(contentType)) {
            vo.setTitle(firstNonBlank(
                    item.path(JSON_TITLE).asString(null),
                    item.path(JSON_ORIGINAL_TITLE).asString(null)));
            vo.setOriginalTitle(item.path(JSON_ORIGINAL_TITLE).asString(null));
            vo.setReleaseDate(parseDate(
                    item.path(JSON_RELEASE_DATE).asString(null)));
        } else {
            vo.setTitle(firstNonBlank(
                    item.path("name").asString(null),
                    item.path(JSON_ORIGINAL_NAME).asString(null)));
            vo.setOriginalTitle(item.path(JSON_ORIGINAL_NAME).asString(null));
            vo.setReleaseDate(parseDate(
                    item.path(JSON_FIRST_AIR_DATE).asString(null)));
        }

        vo.setOverview(item.path(JSON_OVERVIEW).asString(null));
        vo.setPosterPath(item.path(JSON_POSTER_PATH).asString(null));
        vo.setBackdropPath(item.path(JSON_BACKDROP_PATH).asString(null));
        vo.setGenreText(convertGenreIdsToText(
                item.path("genre_ids"), contentType));
        vo.setTmdbScore(nullableDouble(item.path(JSON_VOTE_AVERAGE)));

        return vo;
    }

    @Override
    public int updateMovieDetailData() {
        return updateDetailData(CONTENT_TYPE_MOVIE);
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

            if (CONTENT_TYPE_MOVIE.equals(contentType)) {
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
        vo.setGenreText(parseGenreText(root.path(JSON_GENRES)));
        vo.setRuntime(nullableInt(root.path("runtime")));
        vo.setEpisodeCount(null);
        vo.setDirector(limitLength(
                parseDirector(root.path(JSON_CREDITS).path("crew")), 100));
        vo.setCastNames(limitLength(
                parseCastNames(root.path(JSON_CREDITS).path("cast")), 500));
        vo.setAgeRating(extractMovieAgeRating(root));
    }

    private void fillTvDetail(TmdbVO vo, JsonNode root) {
        vo.setGenreText(parseGenreText(root.path(JSON_GENRES)));
        vo.setRuntime(parseTvRuntime(root.path("episode_run_time")));
        vo.setEpisodeCount(nullableInt(root.path("number_of_episodes")));
        vo.setDirector(limitLength(
                parseTvCreator(root.path(JSON_CREATED_BY)), 100));
        vo.setCastNames(limitLength(
                parseCastNames(root.path(JSON_CREDITS).path("cast")), 500));
        vo.setAgeRating(extractTvAgeRating(root));
    }

    @Override
    public int loadMoviePlatformData() {
        return loadPlatformData(CONTENT_TYPE_MOVIE);
    }

    @Override
    public int loadTvPlatformData() {
        return loadPlatformData("TV");
    }

    private int loadPlatformData(String contentType) {

        int saveCount = 0;

        for (TmdbVO vo : tmdbDAO.selectContentList()) {

            if (contentType.equals(vo.getContentType())) {
                Integer contentNo = tmdbDAO.findContentNo(
                        vo.getTmdbId(), contentType);

                if (contentNo != null) {
                    saveCount += savePlatformRelations(
                            contentNo,
                            vo.getTmdbId(),
                            contentType);
                }
            }
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

        if (CONTENT_TYPE_MOVIE.equals(normalizedType)) {
            return createMovieContentVO(tmdbId);
        }

        if ("TV".equals(normalizedType)) {
            return createTvContentVO(tmdbId);
        }

        throw new IllegalArgumentException(
                "지원하지 않는 콘텐츠 타입입니다: "
                + contentType);
    }

    /**
     * 기존 호출부와 초기 적재 로직을 유지하기 위한 호환 메서드입니다.
     * 플랫폼 키가 전달되지 않으므로 기존 TMDB watch/providers API를 사용합니다.
     */
    @Override
    public void saveContentPlatform(ContentVO content) {

        saveContentPlatform(
                content,
                Collections.emptyList()
        );
    }

    /**
     * 상세페이지 진입 시 검색 JSON 캐시의 platformKeys를 우선 사용하여
     * CONTENT_PLATFORM 관계를 저장합니다.
     *
     * JSON 캐시는 검색·목록 화면에 실제 노출된 OTT 정보를 이미 보유하므로,
     * 상세 진입 시 TMDB watch/providers를 다시 호출했을 때 발생할 수 있는
     * 제공자 응답 시점 차이와 Wavve 누락 문제를 방지할 수 있습니다.
     *
     * platformKeys가 null이거나 비어 있는 콘텐츠만 기존 TMDB API 방식으로
     * 대체하여 초기 적재 및 다른 기존 호출부의 동작을 유지합니다.
     */
    @Override
    public void saveContentPlatform(
            ContentVO content,
            List<String> platformKeys) {

        if (content == null
                || content.getContentNo() <= 0
                || content.getTmdbId() == null
                || content.getContentType() == null) {
            return;
        }

        List<String> safePlatformKeys =
                platformKeys == null
                        ? Collections.emptyList()
                        : platformKeys;

        if (!safePlatformKeys.isEmpty()) {

            savePlatformRelationsFromKeys(
                    content.getContentNo(),
                    safePlatformKeys
            );

            return;
        }

        savePlatformRelations(
                content.getContentNo(),
                content.getTmdbId(),
                content.getContentType()
        );
    }

    /**
     * JSON 캐시에서 전달된 내부 플랫폼 키를 DB 플랫폼명으로 변환하고,
     * 기존 관계가 없는 OTT만 CONTENT_PLATFORM에 추가합니다.
     *
     * 지원 키:
     * netflix, tving, wavve, disney, watcha, coupang
     */
    private int savePlatformRelationsFromKeys(
            Integer contentNo,
            List<String> platformKeys) {

        if (contentNo == null
                || contentNo <= 0
                || platformKeys == null
                || platformKeys.isEmpty()) {

            return 0;
        }

        Set<String> normalizedKeys =
                new LinkedHashSet<String>();

        for (String platformKey : platformKeys) {

            String normalizedKey =
                    normalizePlatformKey(platformKey);

            if (!normalizedKey.isEmpty()) {
                normalizedKeys.add(normalizedKey);
            }
        }

        int saveCount = 0;

        for (String normalizedKey : normalizedKeys) {
            saveCount += savePlatformRelationFromKey(
                    contentNo,
                    normalizedKey
            );
        }

        return saveCount;
    }


    private int savePlatformRelationFromKey(
            Integer contentNo,
            String normalizedKey) {

        String platformName =
                convertPlatformKeyToDbName(
                        normalizedKey
                );

        if (platformName == null) {
            return 0;
        }

        Integer platformNo =
                tmdbDAO.findPlatformNo(
                        platformName
                );

        if (platformNo == null
                || tmdbDAO.existsContentPlatform(
                        contentNo,
                        platformNo
                ) > 0) {

            return 0;
        }

        tmdbDAO.insertContentPlatform(
                contentNo,
                platformNo
        );

        return 1;
    }

    /**
     * JSON 플랫폼 키의 대소문자, 공백, 특수문자 차이를 제거합니다.
     */
    private String normalizePlatformKey(
            String platformKey) {

        if (platformKey == null) {
            return "";
        }

        String normalized =
                platformKey.trim()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9]", "");

        if (normalized.contains(PLATFORM_KEY_NETFLIX)) {
            return PLATFORM_KEY_NETFLIX;
        }

        if (normalized.contains(PLATFORM_KEY_TVING)) {
            return PLATFORM_KEY_TVING;
        }

        if (normalized.contains(PLATFORM_KEY_WAVVE)) {
            return PLATFORM_KEY_WAVVE;
        }

        if (normalized.contains(PLATFORM_KEY_DISNEY)) {
            return PLATFORM_KEY_DISNEY;
        }

        if (normalized.contains(PLATFORM_KEY_WATCHA)) {
            return PLATFORM_KEY_WATCHA;
        }

        if (normalized.contains(PLATFORM_KEY_COUPANG)) {
            return PLATFORM_KEY_COUPANG;
        }

        return "";
    }

    /**
     * 검색 JSON 내부 키를 현재 OTT_PLATFORM 테이블의 플랫폼명으로 변환합니다.
     * Mapper에서도 대소문자를 무시하므로 DB 표기 변경에도 안전하게 조회됩니다.
     */
    private String convertPlatformKeyToDbName(
            String platformKey) {

        if (PLATFORM_KEY_NETFLIX.equals(platformKey)) {
            return "Netflix";
        }

        if (PLATFORM_KEY_TVING.equals(platformKey)) {
            return "TVING";
        }

        if (PLATFORM_KEY_WAVVE.equals(platformKey)) {
            return PLATFORM_KEY_WAVVE;
        }

        if (PLATFORM_KEY_DISNEY.equals(platformKey)) {
            return "Disney Plus";
        }

        if (PLATFORM_KEY_WATCHA.equals(platformKey)) {
            return "Watcha";
        }

        if (PLATFORM_KEY_COUPANG.equals(platformKey)) {
            return "Coupangplay";
        }

        return null;
    }

    private int savePlatformRelations(
            Integer contentNo,
            Long tmdbId,
            String contentType) {

        String apiType =
                CONTENT_TYPE_MOVIE.equals(contentType) ? API_TYPE_MOVIE : "tv";

        JsonNode flatrate = callTmdbApi(
                tmdbApiBaseUrl
                + "/" + apiType
                + "/" + tmdbId
                + "/watch/providers")
                .path(JSON_RESULTS)
                .path(tmdbApiRegion)
                .path("flatrate");

        if (!flatrate.isArray()) {
            return 0;
        }

        int saveCount = 0;

        for (JsonNode provider : flatrate) {
            saveCount += saveProviderRelation(
                    contentNo,
                    provider
            );
        }

        return saveCount;
    }


    private int saveProviderRelation(
            Integer contentNo,
            JsonNode provider) {

        String platformName =
                convertTmdbProviderName(
                        provider.path("provider_name")
                                .asString(null)
                );

        if (platformName == null) {
            return 0;
        }

        Integer platformNo =
                tmdbDAO.findPlatformNo(
                        platformName
                );

        if (platformNo == null
                || tmdbDAO.existsContentPlatform(
                        contentNo,
                        platformNo
                ) > 0) {

            return 0;
        }

        tmdbDAO.insertContentPlatform(
                contentNo,
                platformNo
        );

        return 1;
    }

    /**
     * 상품 등록 화면에서 DB 저장 전에 표시할 출연 배우를 TMDB에서 조회합니다.
     * 실제 저장 제한과 동일하게 상위 CAST_SAVE_LIMIT명만 반환합니다.
     */
    @Override
    public List<ActorVO> getContentActorPreview(
            Long tmdbId,
            String contentType) {

        if (tmdbId == null
                || tmdbId <= 0
                || contentType == null
                || contentType.isBlank()) {

            throw new IllegalArgumentException(
                    "배우 조회에 필요한 콘텐츠 정보가 없습니다."
            );
        }

        String normalizedType =
                contentType.trim()
                        .toUpperCase(Locale.ROOT);

        if (!CONTENT_TYPE_MOVIE.equals(normalizedType)
                && !"TV".equals(normalizedType)) {

            throw new IllegalArgumentException(
                    "지원하지 않는 콘텐츠 타입입니다."
            );
        }

        JsonNode castNode =
                getDetailRoot(
                        tmdbId,
                        normalizedType,
                        true
                ).path(JSON_CREDITS).path("cast");

        if (!castNode.isArray()) {
            return Collections.emptyList();
        }

        List<ActorVO> actorList =
                new ArrayList<ActorVO>();

        int displayOrder = 1;

        for (JsonNode cast : castNode) {

            long tmdbActorId = cast.path("id").asLong();
            String actorName = cast.path("name").asString(null);

            if (displayOrder <= CAST_SAVE_LIMIT
                    && tmdbActorId > 0
                    && actorName != null
                    && !actorName.isBlank()) {

                ActorVO actor = new ActorVO();
                actor.setTmdbActorId(tmdbActorId);
                actor.setActorName(limitLength(actorName, 100));
                actor.setProfilePath(
                        cast.path(JSON_PROFILE_PATH).asString(null)
                );
                actor.setCharacterName(
                        limitLength(
                                cast.path(JSON_CHARACTER).asString(null),
                                100
                        )
                );
                actor.setDisplayOrder(displayOrder);

                actorList.add(actor);
                displayOrder++;
            }
        }

        return actorList;
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
                root.path(JSON_CREDITS).path("cast"));

        if (CONTENT_TYPE_MOVIE.equals(contentType)) {
            saveMovieDirectorData(
                    contentNo,
                    root.path(JSON_CREDITS).path("crew"));
        } else {
            saveTvCreatorData(
                    contentNo,
                    root.path(JSON_CREATED_BY));

            saveMovieDirectorData(
                    contentNo,
                    root.path(JSON_CREDITS).path("crew"));
        }
    }

    private ContentVO createMovieContentVO(Long tmdbId) {
        return createContentVO(tmdbId, CONTENT_TYPE_MOVIE);
    }

    private ContentVO createTvContentVO(Long tmdbId) {
        return createContentVO(tmdbId, "TV");
    }

    private ContentVO createContentVO(
            Long tmdbId,
            String contentType) {

        boolean movie = CONTENT_TYPE_MOVIE.equals(contentType);
        JsonNode root = getDetailRoot(tmdbId, contentType, true);

        String titleKey = movie ? JSON_TITLE : "name";
        String originalTitleKey = movie
                ? JSON_ORIGINAL_TITLE
                : JSON_ORIGINAL_NAME;
        String releaseDateKey = movie
                ? JSON_RELEASE_DATE
                : JSON_FIRST_AIR_DATE;

        ContentVO vo = new ContentVO();
        vo.setTmdbId(root.path("id").asLong());
        vo.setContentType(contentType);
        vo.setTitle(firstNonBlank(
                root.path(titleKey).asString(null),
                root.path(originalTitleKey).asString(null)));
        vo.setOriginalTitle(root.path(originalTitleKey).asString(null));
        vo.setOverview(root.path(JSON_OVERVIEW).asString(null));
        vo.setPosterPath(root.path(JSON_POSTER_PATH).asString(null));
        vo.setBackdropPath(root.path(JSON_BACKDROP_PATH).asString(null));
        vo.setReleaseDate(parseDate(
                root.path(releaseDateKey).asString(null)));
        vo.setGenreText(parseGenreText(root.path(JSON_GENRES)));
        vo.setCastNames(limitLength(
                parseCastNames(root.path(JSON_CREDITS).path("cast")),
                500));
        vo.setTmdbScore(nullableDouble(
                root.path(JSON_VOTE_AVERAGE)));

        if (movie) {
            fillMovieSpecificContent(vo, root);
        } else {
            fillTvSpecificContent(vo, root);
        }

        return vo;
    }

    private void fillMovieSpecificContent(
            ContentVO vo,
            JsonNode root) {

        vo.setRuntime(nullableInt(root.path("runtime")));
        vo.setEpisodeCount(null);
        vo.setDirector(limitLength(
                parseDirector(root.path(JSON_CREDITS).path("crew")),
                100));
        vo.setAgeRating(extractMovieAgeRating(root));
    }

    private void fillTvSpecificContent(
            ContentVO vo,
            JsonNode root) {

        vo.setRuntime(parseTvRuntime(root.path("episode_run_time")));
        vo.setEpisodeCount(nullableInt(root.path("number_of_episodes")));
        vo.setDirector(limitLength(
                parseTvCreator(root.path(JSON_CREATED_BY)),
                100));
        vo.setAgeRating(extractTvAgeRating(root));
    }

    private JsonNode getDetailRoot(
            Long tmdbId,
            String contentType,
            boolean includeCredits) {

        if (CONTENT_TYPE_MOVIE.equals(contentType)) {
            return callTmdbApi(
                    tmdbApiBaseUrl
                    + "/movie/" + tmdbId
                    + QUERY_LANGUAGE + tmdbApiLanguage
                    + (includeCredits
                            ? "&append_to_response=credits,release_dates"
                            : ""));
        }

        return callTmdbApi(
                tmdbApiBaseUrl
                + "/tv/" + tmdbId
                + QUERY_LANGUAGE + tmdbApiLanguage
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

            long tmdbActorId = cast.path("id").asLong();
            String actorName = cast.path("name").asString(null);

            if (displayOrder <= CAST_SAVE_LIMIT
                    && tmdbActorId > 0
                    && actorName != null
                    && !actorName.isBlank()) {

                Integer actorNo =
                        tmdbDAO.findActorNoByTmdbId(tmdbActorId);

                if (actorNo == null) {
                    ActorVO actor = new ActorVO();
                    actor.setTmdbActorId(tmdbActorId);
                    actor.setActorName(
                            limitLength(actorName, 100));
                    actor.setProfilePath(
                            cast.path(JSON_PROFILE_PATH).asString(null));
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
                                    cast.path(JSON_CHARACTER).asString(null),
                                    100),
                            displayOrder);
                }

                displayOrder++;
            }
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

            if (!JOB_DIRECTOR.equals(
                    crew.path("job").asString(null))) {
                continue;
            }

            saveDirectorRelation(
                    contentNo,
                    crew,
                    ROLE_DIRECTOR,
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
                    personNode.path(JSON_PROFILE_PATH)
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
                + QUERY_LANGUAGE + tmdbApiLanguage
                + "&watch_region=" + tmdbApiRegion)
                .path(JSON_RESULTS);

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

            if (platformName != null
                    && providerId > 0
                    && (selectedPlatforms == null
                            || selectedPlatforms.isEmpty()
                            || selectedPlatforms.contains(
                                    platformName))) {

                ids.add(String.valueOf(providerId));
            }
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

        if (normalized.contains(PLATFORM_KEY_NETFLIX)) {
            return "Netflix";
        }

        if (normalized.contains(PLATFORM_KEY_TVING)) {
            return "TVING";
        }

        if (normalized.contains(PLATFORM_KEY_WAVVE)) {
            return "Wavve";
        }

        if (normalized.contains(PLATFORM_KEY_DISNEY)) {
            return "Disney Plus";
        }

        if (normalized.contains(PLATFORM_KEY_WATCHA)) {
            return "Watcha";
        }

        if (normalized.contains(PLATFORM_KEY_COUPANG)) {
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
                item.path(JSON_TITLE).asString(null),
                item.path("name").asString(null));

        String originalTitle = firstNonBlank(
                item.path(JSON_ORIGINAL_TITLE).asString(null),
                item.path(JSON_ORIGINAL_NAME).asString(null));

        String overview = item.path(JSON_OVERVIEW).asString("");

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

        return CONTENT_TYPE_MOVIE.equals(contentType)
                ? MOVIE_GENRES.get(genreId)
                : TV_GENRES.get(genreId);
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

            if (!JOB_DIRECTOR.equals(
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
        return parseNameList(createdByNode, Integer.MAX_VALUE);
    }

    private String parseCastNames(JsonNode castNode) {
        return parseNameList(castNode, CAST_SAVE_LIMIT);
    }

    private String parseNameList(
            JsonNode sourceNode,
            int limit) {

        if (sourceNode == null || !sourceNode.isArray()) {
            return null;
        }

        List<String> names = new ArrayList<String>();

        for (JsonNode item : sourceNode) {
            if (names.size() >= limit) {
                break;
            }

            String name = item.path("name").asString(null);

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
                root.path("release_dates").path(JSON_RESULTS);

        return extractPreferredCountryAgeRating(
                results,
                this::extractSupportedMovieCountryAgeRating);
    }

    private String extractTvAgeRating(JsonNode root) {

        JsonNode results =
                root.path("content_ratings").path(JSON_RESULTS);

        return extractPreferredCountryAgeRating(
                results,
                this::extractSupportedTvCountryAgeRating);
    }

    private String extractPreferredCountryAgeRating(
            JsonNode results,
            CountryAgeRatingExtractor ratingExtractor) {

        if (!results.isArray()) {
            return AGE_RATING_UNKNOWN;
        }

        String usRating = null;

        for (JsonNode country : results) {
            String countryCode =
                    country.path("iso_3166_1").asString(null);
            String converted = ratingExtractor.extract(
                    country, countryCode);

            if ("KR".equals(countryCode) && converted != null) {
                return converted;
            }

            if ("US".equals(countryCode) && usRating == null) {
                usRating = converted;
            }
        }

        return usRating == null
                ? AGE_RATING_UNKNOWN
                : usRating;
    }

    private String extractSupportedMovieCountryAgeRating(
            JsonNode country,
            String countryCode) {

        if (!isSupportedAgeRatingCountry(countryCode)) {
            return null;
        }

        return extractMovieCountryAgeRating(country, countryCode);
    }

    private String extractSupportedTvCountryAgeRating(
            JsonNode country,
            String countryCode) {

        if (!isSupportedAgeRatingCountry(countryCode)) {
            return null;
        }

        return convertAgeRating(
                countryCode,
                country.path("rating").asString(null),
                true);
    }

    private boolean isSupportedAgeRatingCountry(
            String countryCode) {

        return "KR".equals(countryCode)
                || "US".equals(countryCode);
    }

    private String extractMovieCountryAgeRating(
            JsonNode country,
            String countryCode) {

        JsonNode dates = country.path("release_dates");

        if (!dates.isArray()) {
            return null;
        }

        for (JsonNode item : dates) {
            String converted = convertAgeRating(
                    countryCode,
                    item.path("certification").asString(null),
                    false);

            if (converted != null) {
                return converted;
            }
        }

        return null;
    }

    @FunctionalInterface
    private interface CountryAgeRatingExtractor {
        String extract(JsonNode country, String countryCode);
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
            return convertKoreanAgeRating(value);
        }

        if (!"US".equals(countryCode)) {
            return null;
        }

        return tv
                ? convertUsTvAgeRating(value)
                : convertUsMovieAgeRating(value);
    }

    private String convertKoreanAgeRating(
            String value) {

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

        return AGE_RATING_UNKNOWN;
    }

    private String convertUsTvAgeRating(
            String value) {

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

        return AGE_RATING_UNKNOWN;
    }

    private String convertUsMovieAgeRating(
            String value) {

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

        return AGE_RATING_UNKNOWN;
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
                && !ROLE_DIRECTOR.equals(normalizedRole)
                && !"CREATOR".equals(normalizedRole)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 인물 역할입니다: " + role);
        }

        String url = tmdbApiBaseUrl
                + "/person/" + tmdbPersonId
                + QUERY_LANGUAGE + tmdbApiLanguage
                + "&append_to_response=combined_credits";

        JsonNode root = callTmdbApi(url);

        PersonFilmographyVO person = new PersonFilmographyVO();
        person.setTmdbPersonId(tmdbPersonId);
        person.setPersonName(root.path("name").asString(null));
        person.setProfilePath(root.path(JSON_PROFILE_PATH).asString(null));
        person.setBiography(root.path("biography").asString(null));
        person.setBirthday(root.path("birthday").asString(null));
        person.setPlaceOfBirth(root.path("place_of_birth").asString(null));
        person.setRole(normalizedRole);

        JsonNode credits = root.path("combined_credits");

        person.setCastList(createCastFilmographyList(
                credits.path("cast")));

        person.setDirectorList(createCrewFilmographyList(
                credits.path("crew"),
                ROLE_DIRECTOR));

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
                    item.path(JSON_CHARACTER).asString(null));

            putFilmographyWithPriority(
                    uniqueMap,
                    item.path(JSON_MEDIA_TYPE).asString(),
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

            if (ROLE_DIRECTOR.equals(category)) {
                matches = JOB_DIRECTOR.equalsIgnoreCase(job);
            } else {
                matches = isProductionParticipation(
                        job, department);
            }

            if (matches) {
                FilmographyVO filmography =
                        createFilmographyVO(item);

                if (filmography != null) {
                    filmography.setParticipationCategory(category);
                    filmography.setParticipationName(
                            convertParticipationName(job, department));

                    putFilmographyWithPriority(
                            uniqueMap,
                            item.path(JSON_MEDIA_TYPE).asString(),
                            filmography);
                }
            }
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
                item.path(JSON_MEDIA_TYPE).asString(null);

        if (!API_TYPE_MOVIE.equals(mediaType)
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
                API_TYPE_MOVIE.equals(mediaType)
                        ? CONTENT_TYPE_MOVIE
                        : "TV");

        filmography.setTitle(firstNonBlank(
                item.path(JSON_TITLE).asString(null),
                item.path("name").asString(null)));

        filmography.setOriginalTitle(firstNonBlank(
                item.path(JSON_ORIGINAL_TITLE).asString(null),
                item.path(JSON_ORIGINAL_NAME).asString(null)));

        filmography.setPosterPath(
                item.path(JSON_POSTER_PATH).asString(null));

        filmography.setReleaseDate(firstNonBlank(
                item.path(JSON_RELEASE_DATE).asString(null),
                item.path(JSON_FIRST_AIR_DATE).asString(null)));

        filmography.setTmdbScore(
                nullableDouble(item.path(JSON_VOTE_AVERAGE)));

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

        if (JOB_DIRECTOR.equalsIgnoreCase(job)) {
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

        if (JOB_DIRECTOR.equalsIgnoreCase(job)) {
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

        for (String participationToken : tokens) {

            String value = participationToken.trim();

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