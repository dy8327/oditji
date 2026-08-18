package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.json.JsonMapper;

/** TMDB 플랫폼 관계 helper의 guard 및 마지막 중복 조건을 추가 보완합니다. */
class TmdbServiceImplResidualConditionClosure2Test {

    private TmdbDAO tmdbDAO;
    private TmdbServiceImpl service;

    @BeforeEach
    void setUp() {
        tmdbDAO = mock(TmdbDAO.class);
        service = new TmdbServiceImpl(
                tmdbDAO,
                JsonMapper.builder().build());
    }

    @Test
    void platformRelationKeyGuardShouldCoverEveryShortCircuitPosition() {
        assertEquals(0, invokeInt("savePlatformRelationsFromKeys", null, List.of("netflix")));
        assertEquals(0, invokeInt("savePlatformRelationsFromKeys", 0, List.of("netflix")));
        assertEquals(0, invokeInt("savePlatformRelationsFromKeys", 1, null));
        assertEquals(0, invokeInt("savePlatformRelationsFromKeys", 1, List.of()));
    }

    @Test
    void normalizedPlatformKeysShouldSkipNullBlankUnknownAndDeduplicate() {
        when(tmdbDAO.findPlatformNo("Netflix")).thenReturn(8);
        when(tmdbDAO.existsContentPlatform(10, 8)).thenReturn(0);

        int saved = invokeInt(
                "savePlatformRelationsFromKeys",
                10,
                Arrays.asList(null, " ", "unknown", "Netflix", "NET-FLIX"));

        assertEquals(1, saved);
        verify(tmdbDAO).insertContentPlatform(10, 8);
    }

    @Test
    void platformRelationShouldCoverExistingRelationSecondOperand() {
        when(tmdbDAO.findPlatformNo("Netflix")).thenReturn(8);
        when(tmdbDAO.existsContentPlatform(20, 8)).thenReturn(1);

        int saved = invokeInt(
                "savePlatformRelationFromKey",
                20,
                "netflix");

        assertEquals(0, saved);
        verify(tmdbDAO, never()).insertContentPlatform(20, 8);
    }

    @Test
    void platformNormalizerShouldCoverAllSupportedAndUnknownKeys() {
        assertEquals("netflix", invokeString("normalizePlatformKey", "NET-FLIX"));
        assertEquals("tving", invokeString("normalizePlatformKey", "Tving"));
        assertEquals("wavve", invokeString("normalizePlatformKey", "wav-ve"));
        assertEquals("disney", invokeString("normalizePlatformKey", "Disney+"));
        assertEquals("watcha", invokeString("normalizePlatformKey", "Watcha"));
        assertEquals("coupang", invokeString("normalizePlatformKey", "Coupang Play"));
        assertTrue(invokeString("normalizePlatformKey", "Prime Video").isEmpty());
    }

    private int invokeInt(String methodName, Object... arguments) {
        Integer result = ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
        return result.intValue();
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
