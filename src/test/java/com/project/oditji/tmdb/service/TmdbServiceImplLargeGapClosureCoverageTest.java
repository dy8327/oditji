package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.vo.FilmographyVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * TMDB 서비스에서 잔여로 남기 쉬운 필모그래피 복합조건의 후반 피연산자를 보완합니다.
 */
class TmdbServiceImplLargeGapClosureCoverageTest {

    private JsonMapper mapper;
    private TmdbServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
        service = new TmdbServiceImpl(mock(TmdbDAO.class), mapper);
    }

    @Test
    void createFilmographyShouldCoverExcludedItemAfterNonNullChecksAndMovieTvOperands() throws Exception {
        JsonNode adultMovie = json("""
                {
                  "id": 101,
                  "media_type": "movie",
                  "title": "정상 제목",
                  "adult": true
                }
                """);
        assertNull(invoke("createFilmographyVO", adultMovie));

        JsonNode invalidType = json("""
                {
                  "id": 102,
                  "media_type": "collection",
                  "title": "유효하지 않은 유형"
                }
                """);
        assertNull(invoke("createFilmographyVO", invalidType));

        FilmographyVO movie = invoke(
                "createFilmographyVO",
                json("""
                        {
                          "id": 103,
                          "media_type": "movie",
                          "title": "영화",
                          "release_date": "2026-08-01",
                          "popularity": 1.0
                        }
                        """));
        assertEquals("MOVIE", movie.getContentType());

        FilmographyVO tv = invoke(
                "createFilmographyVO",
                json("""
                        {
                          "id": 104,
                          "media_type": "tv",
                          "name": "TV",
                          "first_air_date": "2026-08-02",
                          "popularity": 2.0
                        }
                        """));
        assertEquals("TV", tv.getContentType());
    }

    @Test
    void crewFilmographyShouldCoverMatchingJobWhoseContentIsRejected() throws Exception {
        JsonNode directorCrew = json("""
                [
                  {
                    "id": 201,
                    "media_type": "movie",
                    "title": "제외 대상",
                    "adult": true,
                    "job": "Director",
                    "department": "Directing"
                  },
                  {
                    "id": 202,
                    "media_type": "movie",
                    "title": "비감독",
                    "job": "Writer",
                    "department": "Writing"
                  }
                ]
                """);

        List<FilmographyVO> directors = invoke(
                "createCrewFilmographyList",
                directorCrew,
                "DIRECTOR");
        assertEquals(0, directors.size());

        JsonNode productionCrew = json("""
                [
                  {
                    "id": 203,
                    "media_type": "tv",
                    "name": "제외 제작작",
                    "adult": true,
                    "job": "Producer",
                    "department": "Production"
                  },
                  {
                    "id": 204,
                    "media_type": "tv",
                    "name": "비제작 참여",
                    "job": "Actor",
                    "department": "Acting"
                  }
                ]
                """);

        List<FilmographyVO> productions = invoke(
                "createCrewFilmographyList",
                productionCrew,
                "PRODUCTION");
        assertEquals(0, productions.size());
    }

    @Test
    void priorityComparatorShouldCoverNonNullBlankPosterOnBothOperands() {
        FilmographyVO blankPoster = filmography(1L, "   ", null, 10.0);
        FilmographyVO nullPoster = filmography(2L, null, null, 5.0);
        FilmographyVO realPoster = filmography(3L, "/poster.jpg", null, 1.0);

        assertEquals(
                -1,
                Integer.signum((Integer) invoke(
                        "compareFilmographyPriority",
                        blankPoster,
                        nullPoster)));
        assertEquals(
                1,
                Integer.signum((Integer) invoke(
                        "compareFilmographyPriority",
                        blankPoster,
                        realPoster)));
        assertEquals(
                -1,
                Integer.signum((Integer) invoke(
                        "compareFilmographyPriority",
                        realPoster,
                        blankPoster)));
    }

    @Test
    void sorterShouldCoverEachSingleEmptyDateOperandAndSameDatePopularityFallback() {
        Map<String, FilmographyVO> firstEmptyMap = new LinkedHashMap<String, FilmographyVO>();
        firstEmptyMap.put("empty", filmography(10L, null, "   ", 100.0));
        firstEmptyMap.put("dated", filmography(11L, null, "2026-08-01", 1.0));

        List<FilmographyVO> firstSorted = invoke("sortFilmographyList", firstEmptyMap);
        assertEquals(11L, firstSorted.get(0).getTmdbId());

        Map<String, FilmographyVO> secondEmptyMap = new LinkedHashMap<String, FilmographyVO>();
        secondEmptyMap.put("dated", filmography(12L, null, "2026-08-02", 1.0));
        secondEmptyMap.put("empty", filmography(13L, null, null, 100.0));

        List<FilmographyVO> secondSorted = invoke("sortFilmographyList", secondEmptyMap);
        assertEquals(12L, secondSorted.get(0).getTmdbId());

        Map<String, FilmographyVO> sameDateMap = new LinkedHashMap<String, FilmographyVO>();
        sameDateMap.put("low", filmography(14L, null, "2026-08-03", 1.0));
        sameDateMap.put("high", filmography(15L, null, "2026-08-03", 9.0));

        List<FilmographyVO> sameDateSorted = invoke("sortFilmographyList", sameDateMap);
        assertEquals(15L, sameDateSorted.get(0).getTmdbId());
    }

    @Test
    void personFilmographyValidationShouldReachNonNullBlankUnsupportedRoleOperand() {
        String unsupportedRole = "   ";
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getPersonFilmography(1L, unsupportedRole));
    }

    private FilmographyVO filmography(
            Long tmdbId,
            String posterPath,
            String releaseDate,
            Double popularity) {
        FilmographyVO vo = new FilmographyVO();
        vo.setTmdbId(tmdbId);
        vo.setPosterPath(posterPath);
        vo.setReleaseDate(releaseDate);
        vo.setPopularity(popularity);
        return vo;
    }

    private JsonNode json(String value) throws Exception {
        return mapper.readTree(value);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
