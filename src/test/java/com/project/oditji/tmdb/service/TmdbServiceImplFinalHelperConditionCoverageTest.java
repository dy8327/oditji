package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

/** TmdbServiceImpl의 순수 helper에 남은 단락 조건을 집중 검증합니다. */
class TmdbServiceImplFinalHelperConditionCoverageTest {

    private JsonMapper jsonMapper;
    private TmdbServiceImpl service;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().build();
        service = new TmdbServiceImpl(mock(TmdbDAO.class), jsonMapper);
    }

    @Test
    void contentPolicyHelpersShouldCoverJsonNullBlankAndUnknownGenreBranches() throws Exception {
        assertTrue(invokeBoolean("shouldExcludeContent", json("null")));
        assertFalse(invokeBoolean("shouldExcludeContent", json("{\"title\":\"정상\",\"original_title\":null}")));

        assertNull(invoke("convertGenreIdsToText", json("[]"), "MOVIE"));
        assertNull(invoke("convertGenreIdsToText", json("[999999]"), "TV"));
        assertEquals("코미디", invoke("convertGenreIdsToText", json("[35,35]"), "MOVIE"));
    }

    @Test
    void nameRuntimeAndNullableHelpersShouldCoverLimitZeroDuplicateMissingAndZeroValues() throws Exception {
        assertNull(invoke("parseNameList", json("[{\"name\":\"A\"}]"), 0));
        assertEquals("A", invoke("parseNameList", json("[{\"name\":\"A\"},{\"name\":\"A\"},{\"name\":\" \"}]"), 5));
        assertNull(invoke("parseTvRuntime", json("{}")));
        assertNull(invoke("nullableInt", json("null")));
        assertNull(invoke("nullableInt", json("0")));
        assertEquals(Integer.valueOf(45), (Integer) invoke("nullableInt", json("45")));
        assertNull(invoke("nullableDouble", json("null")));
        assertEquals(Double.valueOf(8.2), (Double) invoke("nullableDouble", json("8.2")));
    }

    @Test
    void ageRatingCountryHelpersShouldCoverUnsupportedCountriesAndNullConversions() throws Exception {
        assertFalse(invokeBoolean("isSupportedAgeRatingCountry", "JP"));
        assertTrue(invokeBoolean("isSupportedAgeRatingCountry", "KR"));
        assertTrue(invokeBoolean("isSupportedAgeRatingCountry", "US"));

        JsonNode country = json("{\"release_dates\":[{\"certification\":\"\"},{\"certification\":\"R\"}]}");
        assertNull(invoke("extractSupportedMovieCountryAgeRating", country, "JP"));
        assertEquals("18", invoke("extractSupportedMovieCountryAgeRating", country, "US"));
        assertNull(invoke("extractSupportedTvCountryAgeRating", json("{\"rating\":\"TV-14\"}"), "JP"));
        assertEquals("15", invoke("extractSupportedTvCountryAgeRating", json("{\"rating\":\"TV-14\"}"), "US"));
    }

    @Test
    void productionParticipationAndNameConversionShouldCoverDirectorDepartmentsAndFallbacks() {
        assertFalse(invokeBoolean("isProductionParticipation", "Director", "Production"));
        assertTrue(invokeBoolean("isProductionParticipation", "", "Writing"));
        assertTrue(invokeBoolean("isProductionParticipation", "Unknown", "Production"));
        assertFalse(invokeBoolean("isProductionParticipation", "Unknown", "Camera"));

        assertEquals("Production", invoke("convertParticipationName", null, "Production"));
        assertEquals("Writing", invoke("convertParticipationName", "   ", "Writing"));
        assertEquals("원안", invoke("convertParticipationName", "Original Story", "Writing"));
        assertEquals("Unknown Job", invoke("convertParticipationName", "Unknown Job", "Crew"));
    }

    @Test
    void participationMergePriorityAndSortShouldCoverEmptyDuplicatePosterDateAndPopularityBranches() {
        assertNull(invoke("mergeParticipationNames", null, "   "));
        assertEquals("각본, 원안", invoke("mergeParticipationNames", "각본, 원안", "원안, 각본"));

        FilmographyVO withPoster = filmography(1L, "/poster.jpg", "2026-01-01", 1.0, "각본");
        FilmographyVO withoutPoster = filmography(1L, null, "2026-01-01", 9.0, "원안");
        assertEquals(-1, ((Integer) invoke("compareFilmographyPriority", withPoster, withoutPoster)).intValue());
        assertEquals(1, ((Integer) invoke("compareFilmographyPriority", withoutPoster, withPoster)).intValue());

        Map<String, FilmographyVO> map = new LinkedHashMap<String, FilmographyVO>();
        FilmographyVO noDateHigh = filmography(2L, null, null, 9.0, null);
        FilmographyVO noDateLow = filmography(3L, null, "   ", 1.0, null);
        FilmographyVO dated = filmography(4L, null, "2026-08-01", 0.5, null);
        map.put("a", noDateLow);
        map.put("b", dated);
        map.put("c", noDateHigh);

        @SuppressWarnings("unchecked")
        List<FilmographyVO> sorted = (List<FilmographyVO>) invoke("sortFilmographyList", map);
        assertEquals(4L, sorted.get(0).getTmdbId());
        assertEquals(2L, sorted.get(1).getTmdbId());
    }

    private FilmographyVO filmography(
            Long tmdbId,
            String poster,
            String releaseDate,
            Double popularity,
            String participationName) {
        FilmographyVO vo = new FilmographyVO();
        vo.setTmdbId(tmdbId);
        vo.setPosterPath(poster);
        vo.setReleaseDate(releaseDate);
        vo.setPopularity(popularity);
        vo.setParticipationName(participationName);
        return vo;
    }

    private JsonNode json(String text) throws Exception {
        return jsonMapper.readTree(text);
    }

    private Object invoke(String methodName, Object... args) {
        return ReflectionTestUtils.invokeMethod(service, methodName, args);
    }

    private boolean invokeBoolean(String methodName, Object... args) {
        return Boolean.TRUE.equals(invoke(methodName, args));
    }
}
