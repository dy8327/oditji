package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.json.JsonMapper;

/** TMDB JSON 플랫폼 키 정규화와 CONTENT_PLATFORM 저장 helper 잔여 분기를 보완합니다. */
class TmdbServiceImplPlatformGapCoverageTest {

    private TmdbDAO tmdbDAO;
    private TmdbServiceImpl service;

    @BeforeEach
    void setUp() {
        tmdbDAO = mock(TmdbDAO.class);
        service = new TmdbServiceImpl(tmdbDAO, JsonMapper.builder().build());
    }

    @Test
    void platformKeyNormalizationAndDbNameConversionShouldCoverEverySupportedProvider() {
        assertEquals("", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", (Object) null));
        assertEquals("netflix", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", " Netflix "));
        assertEquals("tving", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", "TVING"));
        assertEquals("wavve", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", "wa-vve"));
        assertEquals("disney", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", "Disney+"));
        assertEquals("watcha", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", "WATCHA"));
        assertEquals("coupang", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", "coupang play"));
        assertEquals("", ReflectionTestUtils.invokeMethod(service, "normalizePlatformKey", "unknown"));

        assertEquals("Netflix", ReflectionTestUtils.invokeMethod(service, "convertPlatformKeyToDbName", "netflix"));
        assertEquals("TVING", ReflectionTestUtils.invokeMethod(service, "convertPlatformKeyToDbName", "tving"));
        assertEquals("wavve", ReflectionTestUtils.invokeMethod(service, "convertPlatformKeyToDbName", "wavve"));
        assertEquals("Disney Plus", ReflectionTestUtils.invokeMethod(service, "convertPlatformKeyToDbName", "disney"));
        assertEquals("Watcha", ReflectionTestUtils.invokeMethod(service, "convertPlatformKeyToDbName", "watcha"));
        assertEquals("Coupangplay", ReflectionTestUtils.invokeMethod(service, "convertPlatformKeyToDbName", "coupang"));
        assertNull(ReflectionTestUtils.invokeMethod(service, "convertPlatformKeyToDbName", "other"));
    }

    @Test
    void saveFromKeysShouldRejectInvalidInputDeduplicateAndSkipUnknownKeys() {
        assertEquals(
                0,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "savePlatformRelationsFromKeys",
                        null,
                        List.of("netflix"))).intValue());
        assertEquals(
                0,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "savePlatformRelationsFromKeys",
                        0,
                        List.of("netflix"))).intValue());
        assertEquals(
                0,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "savePlatformRelationsFromKeys",
                        1,
                        (Object) null)).intValue());
        assertEquals(
                0,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "savePlatformRelationsFromKeys",
                        1,
                        List.of())).intValue());

        when(tmdbDAO.findPlatformNo("Netflix")).thenReturn(8);
        when(tmdbDAO.existsContentPlatform(10, 8)).thenReturn(0);

        int saved = ((Integer) ReflectionTestUtils.invokeMethod(
                service,
                "savePlatformRelationsFromKeys",
                10,
                List.of(" Netflix ", "netflix", "unknown"))).intValue();

        assertEquals(1, saved);
        verify(tmdbDAO).insertContentPlatform(10, 8);
    }

    @Test
    void singleRelationShouldSkipUnknownMissingAndExistingPlatforms() {
        assertEquals(
                0,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "savePlatformRelationFromKey",
                        10,
                        "unknown")).intValue());

        when(tmdbDAO.findPlatformNo("TVING")).thenReturn(null);
        assertEquals(
                0,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "savePlatformRelationFromKey",
                        10,
                        "tving")).intValue());

        when(tmdbDAO.findPlatformNo("Watcha")).thenReturn(97);
        when(tmdbDAO.existsContentPlatform(10, 97)).thenReturn(1);
        assertEquals(
                0,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "savePlatformRelationFromKey",
                        10,
                        "watcha")).intValue());
    }
}
