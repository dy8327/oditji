package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.content.vo.FilmographyVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * SonarQube에 남은 TmdbServiceImpl의 실제 잔여 조건을 보완합니다.
 * main 소스의 방어 로직은 변경하지 않고 테스트로 진입 가능한 분기만 검증합니다.
 */
class TmdbServiceImplResidualConditionClosure4Test {

    private JsonMapper jsonMapper;
    private TmdbServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();

        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);

        service = new TmdbServiceImpl(
                mock(TmdbDAO.class),
                jsonMapper);

        ReflectionTestUtils.setField(
                service,
                "restTemplate",
                restTemplate);
        ReflectionTestUtils.setField(
                service,
                "token",
                "test-token");
        ReflectionTestUtils.setField(
                service,
                "tmdbApiBaseUrl",
                "https://tmdb.test/3");
        ReflectionTestUtils.setField(
                service,
                "tmdbApiLanguage",
                "ko-KR");
        ReflectionTestUtils.setField(
                service,
                "tmdbApiRegion",
                "KR");
    }

    @Test
    void actorPreviewShouldCoverSupportedTvOperand() {
        server.expect(requestTo(
                "https://tmdb.test/3/tv/901?language=ko-KR"
                        + "&append_to_response=credits,content_ratings"))
                .andRespond(withSuccess(
                        "{\"credits\":{\"cast\":[]}}",
                        MediaType.APPLICATION_JSON));

        assertTrue(
                service.getContentActorPreview(
                        901L,
                        "TV")
                        .isEmpty());

        server.verify();
    }

    @Test
    void providerNameShouldCoverTvingAndWatcha() {
        assertEquals(
                "TVING",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "convertTmdbProviderName",
                        " T_V-I+N G "));

        assertEquals(
                "Watcha",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "convertTmdbProviderName",
                        " WATCH-A "));
    }

    @Test
    void parserGuardsShouldCoverNullNameAndNullSourceOperands()
            throws Exception {

        assertNull(
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseGenreText",
                        json("[{}]")));

        assertNull(
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseDirector",
                        (Object) null));

        assertNull(
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseNameList",
                        null,
                        5));
    }

    @Test
    void filmographyFactoriesShouldCoverNullAndNonArrayInputs()
            throws Exception {

        List<FilmographyVO> nullCast =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createCastFilmographyList",
                        (Object) null);

        List<FilmographyVO> objectCast =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createCastFilmographyList",
                        json("{}"));

        List<FilmographyVO> nullCrew =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createCrewFilmographyList",
                        null,
                        "DIRECTOR");

        List<FilmographyVO> objectCrew =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createCrewFilmographyList",
                        json("{}"),
                        "DIRECTOR");

        assertTrue(nullCast.isEmpty());
        assertTrue(objectCast.isEmpty());
        assertTrue(nullCrew.isEmpty());
        assertTrue(objectCrew.isEmpty());
    }

    @Test
    void filmographySorterShouldCoverBlankDateOperands() {
        Map<String, FilmographyVO> firstBlankMap =
                new LinkedHashMap<String, FilmographyVO>();

        firstBlankMap.put(
                "dated",
                filmography(
                        1L,
                        "2026-08-18",
                        1.0));
        firstBlankMap.put(
                "blank",
                filmography(
                        2L,
                        "   ",
                        9.0));

        List<FilmographyVO> firstBlankSorted =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "sortFilmographyList",
                        firstBlankMap);

        assertEquals(
                1L,
                firstBlankSorted.get(0).getTmdbId());

        Map<String, FilmographyVO> secondBlankMap =
                new LinkedHashMap<String, FilmographyVO>();

        secondBlankMap.put(
                "blank",
                filmography(
                        3L,
                        "   ",
                        9.0));
        secondBlankMap.put(
                "dated",
                filmography(
                        4L,
                        "2026-08-19",
                        1.0));

        List<FilmographyVO> secondBlankSorted =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "sortFilmographyList",
                        secondBlankMap);

        assertEquals(
                4L,
                secondBlankSorted.get(0).getTmdbId());
    }

    private JsonNode json(String value) throws Exception {
        return jsonMapper.readTree(value);
    }

    private FilmographyVO filmography(
            Long tmdbId,
            String releaseDate,
            Double popularity) {

        FilmographyVO filmography = new FilmographyVO();
        filmography.setTmdbId(tmdbId);
        filmography.setReleaseDate(releaseDate);
        filmography.setPopularity(popularity);
        return filmography;
    }
}
