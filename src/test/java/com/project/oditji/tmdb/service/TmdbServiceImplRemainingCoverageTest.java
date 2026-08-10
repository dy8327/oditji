package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.FilmographyVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.TmdbVO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * 기존 TMDB 서비스 테스트에서 남기기 쉬운 조건 분기와 예외 흐름을 보완합니다.
 * 메인 소스의 동작은 변경하지 않고 SonarQube branch/line coverage만 보강합니다.
 */
@ExtendWith(MockitoExtension.class)
class TmdbServiceImplRemainingCoverageTest {

    @Mock
    private TmdbDAO tmdbDAO;

    private JsonMapper jsonMapper;
    private TmdbServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        jsonMapper = JsonMapper.builder().build();
        service = new TmdbServiceImpl(tmdbDAO, jsonMapper);

        ReflectionTestUtils.setField(service, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(service, "token", "test-token");
        ReflectionTestUtils.setField(service, "tmdbApiBaseUrl", "https://tmdb.test/3");
        ReflectionTestUtils.setField(service, "tmdbApiLanguage", "ko-KR");
        ReflectionTestUtils.setField(service, "tmdbApiRegion", "KR");
    }

    @Test
    void movieBasicLoadShouldInsertOnlyEligibleNewContentAndHandleNonArrayResults() {
        when(tmdbDAO.existsContent(101L, "MOVIE")).thenReturn(0);
        when(tmdbDAO.existsContent(102L, "MOVIE")).thenReturn(1);

        expectJson(
                "https://tmdb.test/3/watch/providers/movie?language=ko-KR&watch_region=KR",
                "{\"results\":[{\"provider_id\":8,\"provider_name\":\"Netflix\"}]}");

        expectJson(
                discoverMovieUrl(1),
                """
                {"results":[
                  {"id":101,"title":"정상 영화","original_title":"Normal Movie","release_date":"2026-08-01","genre_ids":[18],"vote_average":8.1},
                  {"id":102,"title":"기존 영화","original_title":"Existing Movie","release_date":"2026-08-02","genre_ids":[28],"vote_average":7.1},
                  {"id":0,"title":"ID 없음"},
                  {"id":103,"title":"성인 플래그","adult":true},
                  {"id":104,"title":"무삭제판 성인영화"}
                ]}
                """);
        expectJson(discoverMovieUrl(2), "{\"results\":{}}");

        for (int page = 3; page <= 10; page++) {
            expectJson(discoverMovieUrl(page), "{\"results\":[]}");
        }

        assertEquals(1, service.loadMovieData());

        ArgumentCaptor<TmdbVO> captor = ArgumentCaptor.forClass(TmdbVO.class);
        verify(tmdbDAO).insertContent(captor.capture());
        assertEquals(101L, captor.getValue().getTmdbId());
        assertEquals("정상 영화", captor.getValue().getTitle());
        verify(tmdbDAO, never()).existsContent(103L, "MOVIE");
        verify(tmdbDAO, never()).existsContent(104L, "MOVIE");
        server.verify();
    }

    @Test
    void updateAndTvPlatformLoadShouldCoverMissingContentNumbersAndMalformedFlatrate() {
        TmdbVO movie = tmdb(201L, "MOVIE");
        TmdbVO other = tmdb(202L, "TV");
        TmdbVO tvMissing = tmdb(301L, "TV");
        TmdbVO tvValid = tmdb(302L, "TV");

        when(tmdbDAO.selectContentList())
                .thenReturn(List.of(movie, other))
                .thenReturn(List.of(movie, tvMissing, tvValid));
        when(tmdbDAO.findContentNo(201L, "MOVIE")).thenReturn(null);
        when(tmdbDAO.findContentNo(301L, "TV")).thenReturn(null);
        when(tmdbDAO.findContentNo(302L, "TV")).thenReturn(30);

        expectJson(
                "https://tmdb.test/3/movie/201?language=ko-KR&append_to_response=credits,release_dates",
                """
                {
                  "genres":[],"runtime":0,
                  "credits":{"cast":[],"crew":[]},
                  "release_dates":{"results":[]}
                }
                """);
        expectJson(
                "https://tmdb.test/3/tv/302/watch/providers",
                "{\"results\":{\"KR\":{\"flatrate\":{}}}}");

        assertEquals(1, service.updateMovieDetailData());
        assertEquals(0, service.loadTvPlatformData());

        verify(tmdbDAO).updateContentDetail(movie);
        verify(tmdbDAO, never()).findContentNo(202L, "TV");
        server.verify();
    }

    @Test
    void saveContentPlatformShouldFallbackForNullAndEmptyKeys() {
        ContentVO tv = content(40, 401L, "TV");
        ContentVO movie = content(41, 402L, "MOVIE");

        when(tmdbDAO.findPlatformNo("Netflix")).thenReturn(null);

        expectJson(
                "https://tmdb.test/3/tv/401/watch/providers",
                "{\"results\":{\"KR\":{\"flatrate\":[{\"provider_name\":\"Netflix\"}]}}}");
        expectJson(
                "https://tmdb.test/3/movie/402/watch/providers",
                "{\"results\":{\"KR\":{\"flatrate\":[{\"provider_name\":\"Unknown Provider\"}]}}}");

        service.saveContentPlatform(tv, null);
        service.saveContentPlatform(movie, List.of());

        verify(tmdbDAO).findPlatformNo("Netflix");
        verify(tmdbDAO, never()).insertContentPlatform(40, 1);
        server.verify();
    }

    @Test
    void supportedProviderLookupShouldCoverMalformedResultsAndPlatformSelectionBranches() {
        String providerUrl =
                "https://tmdb.test/3/watch/providers/movie?language=ko-KR&watch_region=KR";

        expectJson(providerUrl, "{\"results\":{}}");
        expectJson(
                providerUrl,
                """
                {"results":[
                  {"provider_id":8,"provider_name":"Netflix"},
                  {"provider_id":337,"provider_name":"Disney Plus"},
                  {"provider_id":999,"provider_name":"Unknown"},
                  {"provider_id":0,"provider_name":"Netflix"}
                ]}
                """);
        expectJson(
                providerUrl,
                "{\"results\":[{\"provider_id\":356,\"provider_name\":\"Wavve\"}]}");
        expectJson(
                providerUrl,
                """
                {"results":[
                  {"provider_id":8,"provider_name":"Netflix"},
                  {"provider_id":337,"provider_name":"Disney Plus"}
                ]}
                """);

        assertEquals("", invokeString("getSupportedProviderIdText", "movie", null));
        assertEquals("8|337", invokeString("getSupportedProviderIdText", "movie", null));
        assertEquals("356", invokeString("getSupportedProviderIdText", "movie", List.of()));
        assertEquals("8", invokeString(
                "getSupportedProviderIdText",
                "movie",
                List.of("Netflix")));

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "convertTmdbProviderName",
                (Object) null));
        assertEquals("Wavve", ReflectionTestUtils.invokeMethod(
                service,
                "convertTmdbProviderName",
                " WAV_VE "));
        assertEquals("Disney Plus", ReflectionTestUtils.invokeMethod(
                service,
                "convertTmdbProviderName",
                "Disney+"));
        assertEquals("Coupangplay", ReflectionTestUtils.invokeMethod(
                service,
                "convertTmdbProviderName",
                "Coupang-Play"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "convertTmdbProviderName",
                "Prime Video"));
        server.verify();
    }

    @Test
    void actorPreviewShouldCoverMalformedCastNullNameBlankNameAndValidActor() {
        expectJson(
                "https://tmdb.test/3/movie/501?language=ko-KR&append_to_response=credits,release_dates",
                "{\"credits\":{\"cast\":{}}}");
        expectJson(
                "https://tmdb.test/3/movie/502?language=ko-KR&append_to_response=credits,release_dates",
                """
                {"credits":{"cast":[
                  {"id":1},
                  {"id":2,"name":"   "},
                  {"id":0,"name":"ID 없음"},
                  {"id":3,"name":"정상 배우","character":"주연"}
                ]}}
                """);

        assertTrue(service.getContentActorPreview(501L, "MOVIE").isEmpty());
        List<ActorVO> actors = service.getContentActorPreview(502L, "MOVIE");
        assertEquals(1, actors.size());
        assertEquals(3L, actors.get(0).getTmdbActorId());
        assertEquals("정상 배우", actors.get(0).getActorName());
        server.verify();
    }

    @Test
    void actorSaverShouldCoverInvalidRowsPostInsertNullExistingAndLimitBranches() throws Exception {
        JsonNode cast = json("""
                [
                  {"id":0,"name":"ID 없음"},
                  {"id":1},
                  {"id":2,"name":"   "},
                  {"id":3,"name":"신규 배우","character":"신규 역할"},
                  {"id":4,"name":"기존 배우 A","character":"역할 A"},
                  {"id":5,"name":"기존 배우 B","character":"역할 B"},
                  {"id":6,"name":"기존 배우 C","character":"역할 C"},
                  {"id":7,"name":"기존 배우 D","character":"역할 D"},
                  {"id":8,"name":"제한 초과 배우","character":"역할 E"}
                ]
                """);

        when(tmdbDAO.findActorNoByTmdbId(3L))
                .thenReturn((Integer) null)
                .thenReturn((Integer) null);
        when(tmdbDAO.findActorNoByTmdbId(4L)).thenReturn(44);
        when(tmdbDAO.existsContentActor(60, 44)).thenReturn(0);
        when(tmdbDAO.findActorNoByTmdbId(5L)).thenReturn(55);
        when(tmdbDAO.existsContentActor(60, 55)).thenReturn(1);
        when(tmdbDAO.findActorNoByTmdbId(6L)).thenReturn(66);
        when(tmdbDAO.existsContentActor(60, 66)).thenReturn(0);
        when(tmdbDAO.findActorNoByTmdbId(7L)).thenReturn(77);
        when(tmdbDAO.existsContentActor(60, 77)).thenReturn(0);

        ReflectionTestUtils.invokeMethod(service, "saveActorData", 60, (Object) null);
        ReflectionTestUtils.invokeMethod(service, "saveActorData", 60, json("{}"));
        ReflectionTestUtils.invokeMethod(service, "saveActorData", 60, cast);

        verify(tmdbDAO).insertActor(any(ActorVO.class));
        verify(tmdbDAO).insertContentActor(60, 44, "역할 A", 2);
        verify(tmdbDAO, never()).insertContentActor(60, 55, "역할 B", 3);
        verify(tmdbDAO).insertContentActor(60, 66, "역할 C", 4);
        verify(tmdbDAO).insertContentActor(60, 77, "역할 D", 5);
        verify(tmdbDAO, never()).findActorNoByTmdbId(8L);
    }

    @Test
    void directorSaversShouldCoverInvalidNodesNewLookupFailureExistingAndDuplicateRelations() throws Exception {
        JsonNode crew = json("""
                [
                  {"id":1,"name":"작가","job":"Writer"},
                  {"id":0,"name":"ID 없음","job":"Director"},
                  {"id":2,"name":"   ","job":"Director"},
                  {"id":3,"name":"신규 감독","job":"Director"},
                  {"id":4,"name":"기존 감독","job":"Director"}
                ]
                """);
        JsonNode creators = json("""
                [
                  {"id":5,"name":"기존 크리에이터"}
                ]
                """);

        when(tmdbDAO.findDirectorNoByTmdbId(3L))
                .thenReturn((Integer) null)
                .thenReturn((Integer) null);
        when(tmdbDAO.findDirectorNoByTmdbId(4L)).thenReturn(44);
        when(tmdbDAO.existsContentDirector(70, 44)).thenReturn(0);
        when(tmdbDAO.findDirectorNoByTmdbId(5L)).thenReturn(55);
        when(tmdbDAO.existsContentDirector(70, 55)).thenReturn(1);

        ReflectionTestUtils.invokeMethod(service, "saveMovieDirectorData", 70, (Object) null);
        ReflectionTestUtils.invokeMethod(service, "saveMovieDirectorData", 70, json("{}"));
        ReflectionTestUtils.invokeMethod(service, "saveMovieDirectorData", 70, crew);
        ReflectionTestUtils.invokeMethod(service, "saveTvCreatorData", 70, (Object) null);
        ReflectionTestUtils.invokeMethod(service, "saveTvCreatorData", 70, json("{}"));
        ReflectionTestUtils.invokeMethod(service, "saveTvCreatorData", 70, creators);

        verify(tmdbDAO).insertContentDirector(70, 44, "DIRECTOR", 4);
        verify(tmdbDAO, never()).insertContentDirector(70, 55, "CREATOR", 1);
    }

    @Test
    void ageRatingHelpersShouldCoverCountryDateShapeFallbackAndOrConditions() throws Exception {
        assertTrue(invokeBoolean("isSupportedAgeRatingCountry", "KR"));
        assertTrue(invokeBoolean("isSupportedAgeRatingCountry", "US"));
        assertFalse(invokeBoolean("isSupportedAgeRatingCountry", "JP"));
        assertFalse(invokeBoolean("isSupportedAgeRatingCountry", (Object) null));

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "extractSupportedMovieCountryAgeRating",
                json("{\"release_dates\":[]}"),
                "JP"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "extractSupportedTvCountryAgeRating",
                json("{\"rating\":\"15\"}"),
                "JP"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "extractMovieCountryAgeRating",
                json("{\"release_dates\":{}}"),
                "KR"));
        assertEquals("18", ReflectionTestUtils.invokeMethod(
                service,
                "extractMovieCountryAgeRating",
                json("{\"release_dates\":[{\"certification\":\" \"},{\"certification\":\"R\"}]}"),
                "US"));

        JsonNode movieFallback = json("""
                {"release_dates":{"results":[
                  {"iso_3166_1":"US","release_dates":[{"certification":" "}]},
                  {"iso_3166_1":"US","release_dates":[{"certification":"G"}]},
                  {"iso_3166_1":"JP","release_dates":[{"certification":"PG12"}]}
                ]}}
                """);
        assertEquals("ALL", ReflectionTestUtils.invokeMethod(
                service,
                "extractMovieAgeRating",
                movieFallback));

        JsonNode tvFallback = json("""
                {"content_ratings":{"results":[
                  {"iso_3166_1":"KR","rating":" "},
                  {"iso_3166_1":"US","rating":"TV-G"}
                ]}}
                """);
        assertEquals("ALL", ReflectionTestUtils.invokeMethod(
                service,
                "extractTvAgeRating",
                tvFallback));

        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "convertAgeRating",
                "KR",
                "   ",
                false));
        assertEquals("ALL", ReflectionTestUtils.invokeMethod(
                service,
                "convertAgeRating",
                "KR",
                "ALL",
                false));
        assertEquals("ALL", ReflectionTestUtils.invokeMethod(
                service,
                "convertAgeRating",
                "KR",
                "7",
                false));
        assertEquals("18", ReflectionTestUtils.invokeMethod(
                service,
                "convertAgeRating",
                "KR",
                "18",
                false));
        assertEquals("18", ReflectionTestUtils.invokeMethod(
                service,
                "convertAgeRating",
                "KR",
                "청소년관람불가",
                false));
        assertEquals("18", ReflectionTestUtils.invokeMethod(
                service,
                "convertAgeRating",
                "US",
                "R",
                false));
    }

    @Test
    void parserHelpersShouldCoverMalformedGenreRuntimeNamesAndMissingDouble() throws Exception {
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "convertGenreIdsToText",
                (Object) null,
                "MOVIE"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "convertGenreIdsToText",
                json("{}"),
                "MOVIE"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseTvRuntime",
                (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseTvRuntime",
                json("{}")));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseTvRuntime",
                json("[0]")));
        assertEquals(45, ReflectionTestUtils.<Integer>invokeMethod(
                service,
                "parseTvRuntime",
                json("[45]")));

        JsonNode values = json("{}");
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "nullableDouble",
                values.path("missing")));

        assertEquals("A, B", ReflectionTestUtils.invokeMethod(
                service,
                "parseCastNames",
                json("[{\"name\":\"A\"},{\"name\":\"A\"},{},{\"name\":\" \"},{\"name\":\"B\"}]")));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseCastNames",
                json("[]")));
    }

    @Test
    void personFilmographyShouldCoverDefaultDirectorCreatorAndProductionParticipationBranches() {
        expectJson(
                "https://tmdb.test/3/person/901?language=ko-KR&append_to_response=combined_credits",
                productionPersonJson());
        expectJson(
                "https://tmdb.test/3/person/902?language=ko-KR&append_to_response=combined_credits",
                emptyPersonJson("감독 역할"));
        expectJson(
                "https://tmdb.test/3/person/903?language=ko-KR&append_to_response=combined_credits",
                emptyPersonJson("크리에이터 역할"));

        PersonFilmographyVO actor = service.getPersonFilmography(901L, null);
        PersonFilmographyVO director = service.getPersonFilmography(902L, " director ");
        PersonFilmographyVO creator = service.getPersonFilmography(903L, "creator");

        assertEquals("ACTOR", actor.getRole());
        assertEquals(1, actor.getDirectorList().size());
        assertEquals(13, actor.getProductionList().size());
        assertEquals("DIRECTOR", director.getRole());
        assertEquals("CREATOR", creator.getRole());
        assertEquals("테스트 인물", actor.getPersonName());
        assertNotNull(actor.getCastList());
        server.verify();
    }

    @Test
    void filmographyHelpersShouldCoverInvalidRowsExistingPriorityMergeAndDateTie() throws Exception {
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "createFilmographyVO",
                (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "createFilmographyVO",
                json("null")));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "createFilmographyVO",
                json("{\"id\":1,\"media_type\":\"person\"}")));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "createFilmographyVO",
                json("{\"id\":0,\"media_type\":\"movie\",\"title\":\"무효\"}")));

        FilmographyVO tvItem = ReflectionTestUtils.invokeMethod(
                service,
                "createFilmographyVO",
                json("""
                        {
                          "id":10,"media_type":"tv","name":"TV 제목","original_name":"TV Original",
                          "first_air_date":"2026-01-01","poster_path":"/tv.jpg",
                          "vote_average":7.0,"popularity":9.0
                        }
                        """));
        assertEquals("TV", tvItem.getContentType());
        assertEquals("TV 제목", tvItem.getTitle());

        Map<String, FilmographyVO> unique = new LinkedHashMap<String, FilmographyVO>();
        FilmographyVO existing = filmography(20L, "/existing.jpg", 20.0, "2026-01-01", "역할 A");
        FilmographyVO lowerPriority = filmography(20L, null, 100.0, "2026-01-01", "역할 B");
        ReflectionTestUtils.invokeMethod(
                service,
                "putFilmographyWithPriority",
                unique,
                "movie",
                existing);
        ReflectionTestUtils.invokeMethod(
                service,
                "putFilmographyWithPriority",
                unique,
                "movie",
                lowerPriority);
        assertEquals("역할 A, 역할 B", unique.get("movie-20").getParticipationName());
        assertEquals("/existing.jpg", unique.get("movie-20").getPosterPath());

        FilmographyVO samePosterLow = filmography(21L, null, 1.0, "2026-01-01", null);
        FilmographyVO samePosterHigh = filmography(22L, null, 2.0, "2026-01-01", null);
        assertTrue((Integer) ReflectionTestUtils.invokeMethod(
                service,
                "compareFilmographyPriority",
                samePosterHigh,
                samePosterLow) < 0);

        Map<String, FilmographyVO> dateTieMap = new LinkedHashMap<String, FilmographyVO>();
        dateTieMap.put("low", samePosterLow);
        dateTieMap.put("high", samePosterHigh);
        List<FilmographyVO> sorted = ReflectionTestUtils.invokeMethod(
                service,
                "sortFilmographyList",
                dateTieMap);
        assertEquals(22L, sorted.get(0).getTmdbId());

        assertEquals("감독", ReflectionTestUtils.invokeMethod(
                service,
                "mergeParticipationNames",
                " , 감독, ",
                " "));
        assertEquals("원안", ReflectionTestUtils.invokeMethod(
                service,
                "convertParticipationName",
                "Original Story",
                "Writing"));
    }

    @Test
    void productionParticipationShouldCoverEverySupportedJobAndDepartmentCondition() {
        assertTrue(invokeBoolean("isProductionParticipation", "Executive Producer", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Producer", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Co-Producer", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Associate Producer", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Writer", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Screenplay", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Story", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Novel", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Original Story", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Original Music Composer", ""));
        assertTrue(invokeBoolean("isProductionParticipation", "Actor", "Production"));
        assertFalse(invokeBoolean("isProductionParticipation", "Actor", "Acting"));
    }

    @Test
    void tmdbApiFailureShouldBeWrappedAsIllegalStateException() {
        String url =
                "https://tmdb.test/3/movie/999?language=ko-KR&append_to_response=credits,release_dates";
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withServerError());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.getDetailForSave(999L, "MOVIE"));

        assertTrue(exception.getMessage().contains("TMDB API 호출 실패"));
        server.verify();
    }

    @Test
    void saveContentPeopleShouldUseTvCreatorAndMovieDirectorPathsFromPublicEntryPoint() {
        ContentVO tv = content(80, 801L, " tv ");
        ContentVO movie = content(81, 802L, " movie ");

        when(tmdbDAO.findDirectorNoByTmdbId(8011L)).thenReturn(91);
        when(tmdbDAO.existsContentDirector(80, 91)).thenReturn(0);
        when(tmdbDAO.findDirectorNoByTmdbId(8021L)).thenReturn(92);
        when(tmdbDAO.existsContentDirector(81, 92)).thenReturn(0);

        expectJson(
                "https://tmdb.test/3/tv/801?language=ko-KR&append_to_response=credits,content_ratings",
                """
                {
                  "created_by":[{"id":8011,"name":"TV 크리에이터"}],
                  "credits":{"cast":[],"crew":[]}
                }
                """);
        expectJson(
                "https://tmdb.test/3/movie/802?language=ko-KR&append_to_response=credits,release_dates",
                """
                {
                  "credits":{"cast":[],"crew":[{"id":8021,"name":"영화 감독","job":"Director"}]}
                }
                """);

        service.saveContentPeople(tv);
        service.saveContentPeople(movie);

        verify(tmdbDAO).insertContentDirector(80, 91, "CREATOR", 1);
        verify(tmdbDAO).insertContentDirector(81, 92, "DIRECTOR", 1);
        server.verify();
    }

    private String discoverMovieUrl(int page) {
        return "https://tmdb.test/3/discover/movie"
                + "?language=ko-KR"
                + "&region=KR"
                + "&watch_region=KR"
                + "&include_adult=false"
                + "&include_video=false"
                + "&with_watch_monetization_types=flatrate"
                + "&with_watch_providers=8"
                + "&sort_by=popularity.desc"
                + "&page=" + page;
    }

    private String productionPersonJson() {
        return """
                {
                  "name":"테스트 인물",
                  "profile_path":"/person.jpg",
                  "biography":"소개",
                  "birthday":"1990-01-01",
                  "place_of_birth":"서울",
                  "combined_credits":{
                    "cast":[
                      {"id":1,"media_type":"movie","title":"출연작","release_date":"2026-08-01","character":"주연"}
                    ],
                    "crew":[
                      {"id":100,"media_type":"movie","title":"감독작","release_date":"2026-08-13","job":"Director","department":"Directing"},
                      {"id":101,"media_type":"movie","title":"Creator","release_date":"2026-08-12","job":"Creator","department":"Production"},
                      {"id":102,"media_type":"movie","title":"Executive","release_date":"2026-08-11","job":"Executive Producer","department":"Production"},
                      {"id":103,"media_type":"movie","title":"Producer","release_date":"2026-08-10","job":"Producer","department":"Production"},
                      {"id":104,"media_type":"movie","title":"CoProducer","release_date":"2026-08-09","job":"Co-Producer","department":"Production"},
                      {"id":105,"media_type":"movie","title":"Associate","release_date":"2026-08-08","job":"Associate Producer","department":"Production"},
                      {"id":106,"media_type":"movie","title":"Writer","release_date":"2026-08-07","job":"Writer","department":"Writing"},
                      {"id":107,"media_type":"movie","title":"Screenplay","release_date":"2026-08-06","job":"Screenplay","department":"Writing"},
                      {"id":108,"media_type":"movie","title":"Story","release_date":"2026-08-05","job":"Story","department":"Writing"},
                      {"id":109,"media_type":"movie","title":"Novel","release_date":"2026-08-04","job":"Novel","department":"Writing"},
                      {"id":110,"media_type":"movie","title":"Original Story","release_date":"2026-08-03","job":"Original Story","department":"Writing"},
                      {"id":111,"media_type":"movie","title":"Music","release_date":"2026-08-02","job":"Original Music Composer","department":"Sound"},
                      {"id":112,"media_type":"movie","title":"Writing Department","release_date":"2026-08-01","job":"Editor","department":"Writing"},
                      {"id":113,"media_type":"movie","title":"Production Department","release_date":"2026-07-31","job":"Editor","department":"Production"}
                    ]
                  }
                }
                """;
    }

    private String emptyPersonJson(String name) {
        return "{\"name\":\"" + name
                + "\",\"combined_credits\":{\"cast\":[],\"crew\":[]}}";
    }

    private TmdbVO tmdb(Long tmdbId, String contentType) {
        TmdbVO vo = new TmdbVO();
        vo.setTmdbId(tmdbId);
        vo.setContentType(contentType);
        return vo;
    }

    private ContentVO content(int contentNo, Long tmdbId, String contentType) {
        ContentVO vo = new ContentVO();
        vo.setContentNo(contentNo);
        vo.setTmdbId(tmdbId);
        vo.setContentType(contentType);
        return vo;
    }

    private FilmographyVO filmography(
            Long tmdbId,
            String posterPath,
            Double popularity,
            String releaseDate,
            String participationName) {
        FilmographyVO vo = new FilmographyVO();
        vo.setTmdbId(tmdbId);
        vo.setPosterPath(posterPath);
        vo.setPopularity(popularity);
        vo.setReleaseDate(releaseDate);
        vo.setParticipationName(participationName);
        return vo;
    }

    private JsonNode json(String value) throws Exception {
        return jsonMapper.readTree(value);
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        return Boolean.TRUE.equals(ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments));
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }

    private void expectJson(String url, String responseBody) {
        server.expect(requestTo(url))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withSuccess(responseBody, MediaType.APPLICATION_JSON));
    }
}
