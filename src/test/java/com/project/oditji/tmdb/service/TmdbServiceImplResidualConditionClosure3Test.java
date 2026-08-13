package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * TMDB 서비스에서 동일 형태로 반복되는 guard와 국가 등급 우선순위의 후반 조건을 보완합니다.
 */
class TmdbServiceImplResidualConditionClosure3Test {

    private JsonMapper mapper;
    private TmdbDAO tmdbDAO;
    private TmdbServiceImpl service;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().build();
        tmdbDAO = mock(TmdbDAO.class);
        service = new TmdbServiceImpl(tmdbDAO, mapper);
    }

    @Test
    void actorPreviewValidationShouldReachNullContentTypeOperand() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getContentActorPreview(1L, null));
    }

    @Test
    void providerRelationShouldCoverUnknownMissingExistingAndInsertPaths() throws Exception {
        JsonNode unknown = json("{\"provider_name\":\"Prime Video\"}");
        assertEquals(0, invokeInt("saveProviderRelation", 10, unknown));

        JsonNode netflix = json("{\"provider_name\":\"Netflix\"}");
        when(tmdbDAO.findPlatformNo("Netflix"))
                .thenReturn(null)
                .thenReturn(8)
                .thenReturn(8);
        when(tmdbDAO.existsContentPlatform(10, 8))
                .thenReturn(1)
                .thenReturn(0);

        assertEquals(0, invokeInt("saveProviderRelation", 10, netflix));
        assertEquals(0, invokeInt("saveProviderRelation", 10, netflix));
        assertEquals(1, invokeInt("saveProviderRelation", 10, netflix));

        verify(tmdbDAO).insertContentPlatform(10, 8);
    }

    @Test
    void preferredCountryRatingShouldKeepFirstUsRatingWhenAnotherUsRowFollows() throws Exception {
        JsonNode movie = json("""
                {
                  "release_dates": {
                    "results": [
                      {
                        "iso_3166_1": "US",
                        "release_dates": [{"certification": "PG"}]
                      },
                      {
                        "iso_3166_1": "US",
                        "release_dates": [{"certification": "R"}]
                      }
                    ]
                  }
                }
                """);

        assertEquals(
                "12",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "extractMovieAgeRating",
                        movie));
    }

    @Test
    void directorRelationShouldCoverNullNameBlankNamePostInsertNullAndExistingRelation() throws Exception {
        JsonNode nullName = json("{\"id\":1}");
        JsonNode blankName = json("{\"id\":2,\"name\":\"   \"}");
        invoke("saveDirectorRelation", 20, nullName, "DIRECTOR", 1);
        invoke("saveDirectorRelation", 20, blankName, "DIRECTOR", 2);

        JsonNode insertedButNotReloaded = json("{\"id\":3,\"name\":\"신규 감독\"}");
        when(tmdbDAO.findDirectorNoByTmdbId(3L))
                .thenReturn(null)
                .thenReturn(null);
        invoke("saveDirectorRelation", 20, insertedButNotReloaded, "DIRECTOR", 3);
        verify(tmdbDAO, never()).insertContentDirector(20, null, "DIRECTOR", 3);

        JsonNode existing = json("{\"id\":4,\"name\":\"기존 감독\"}");
        when(tmdbDAO.findDirectorNoByTmdbId(4L)).thenReturn(44);
        when(tmdbDAO.existsContentDirector(20, 44)).thenReturn(1);
        invoke("saveDirectorRelation", 20, existing, "DIRECTOR", 4);
        verify(tmdbDAO, never()).insertContentDirector(20, 44, "DIRECTOR", 4);
    }

    @Test
    void filmographyJsonNullShouldHitSecondGuardOperand() throws Exception {
        JsonNode jsonNull = json("null");
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "createFilmographyVO",
                jsonNull));
    }

    private JsonNode json(String value) throws Exception {
        return mapper.readTree(value);
    }

    private int invokeInt(String methodName, Object... arguments) {
        Integer result = ReflectionTestUtils.invokeMethod(service, methodName, arguments);
        return result.intValue();
    }

    private void invoke(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
