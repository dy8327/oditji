package com.project.oditji.tmdb.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

import tools.jackson.databind.json.JsonMapper;

/**
 * 실제 TMDB 네트워크 호출 없이 입력 검증과 JSON 캐시 플랫폼 저장 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class TmdbServiceImplValidationCoverageTest {

    @Mock
    private TmdbDAO tmdbDAO;

    @Mock
    private JsonMapper jsonMapper;

    private TmdbServiceImpl tmdbService;

    @BeforeEach
    void setUp() {
        tmdbService = new TmdbServiceImpl(tmdbDAO, jsonMapper);
    }

    @Test
    void detailForSaveShouldRejectMissingIdAndUnsupportedType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getDetailForSave(null, "MOVIE"));
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getDetailForSave(1L, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getDetailForSave(1L, "DOCUMENTARY"));

        verifyNoInteractions(tmdbDAO, jsonMapper);
    }

    @Test
    void actorPreviewShouldRejectMissingAndUnsupportedContentInformation() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getContentActorPreview(null, "MOVIE"));
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getContentActorPreview(0L, "MOVIE"));
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getContentActorPreview(1L, " "));
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getContentActorPreview(1L, "OTHER"));

        verifyNoInteractions(tmdbDAO, jsonMapper);
    }

    @Test
    void invalidContentShouldBeIgnoredBeforePlatformOrPeopleLookup() {
        ContentVO noNumber = content(0, 100L, "MOVIE");
        ContentVO noTmdbId = content(10, null, "MOVIE");
        ContentVO noType = content(10, 100L, null);

        assertDoesNotThrow(() -> tmdbService.saveContentPlatform(null));
        assertDoesNotThrow(() -> tmdbService.saveContentPlatform(noNumber));
        assertDoesNotThrow(() -> tmdbService.saveContentPlatform(noTmdbId));
        assertDoesNotThrow(() -> tmdbService.saveContentPlatform(noType));
        assertDoesNotThrow(() -> tmdbService.saveContentPlatform(
                noNumber,
                List.of("netflix")));

        assertDoesNotThrow(() -> tmdbService.saveContentPeople(null));
        assertDoesNotThrow(() -> tmdbService.saveContentPeople(noNumber));
        assertDoesNotThrow(() -> tmdbService.saveContentPeople(noTmdbId));
        assertDoesNotThrow(() -> tmdbService.saveContentPeople(noType));

        verifyNoInteractions(tmdbDAO, jsonMapper);
    }

    @Test
    void platformKeysShouldNormalizeDeduplicateAndInsertOnlyMissingRelations() {
        ContentVO content = content(10, 100L, "MOVIE");

        when(tmdbDAO.findPlatformNo("Netflix")).thenReturn(1);
        when(tmdbDAO.findPlatformNo("TVING")).thenReturn(2);
        when(tmdbDAO.findPlatformNo("wavve")).thenReturn(3);
        when(tmdbDAO.findPlatformNo("Disney Plus")).thenReturn(4);
        when(tmdbDAO.findPlatformNo("Watcha")).thenReturn(5);
        when(tmdbDAO.findPlatformNo("Coupangplay")).thenReturn(6);
        when(tmdbDAO.existsContentPlatform(eq(10), anyInt()))
                .thenReturn(0);
        when(tmdbDAO.existsContentPlatform(10, 1)).thenReturn(1);

        tmdbService.saveContentPlatform(
                content,
                Arrays.asList(
                        " Netflix ",
                        "NETFLIX",
                        "tving",
                        "WA-VVE",
                        "Disney+",
                        "watcha",
                        "coupang play",
                        "unsupported",
                        null,
                        " "));

        verify(tmdbDAO, times(6)).findPlatformNo(
                org.mockito.ArgumentMatchers.anyString());
        verify(tmdbDAO, times(6)).existsContentPlatform(
                eq(10),
                anyInt());
        verify(tmdbDAO, never()).insertContentPlatform(10, 1);
        verify(tmdbDAO).insertContentPlatform(10, 2);
        verify(tmdbDAO).insertContentPlatform(10, 3);
        verify(tmdbDAO).insertContentPlatform(10, 4);
        verify(tmdbDAO).insertContentPlatform(10, 5);
        verify(tmdbDAO).insertContentPlatform(10, 6);
    }

    @Test
    void missingPlatformNumberShouldSkipRelationInsert() {
        ContentVO content = content(20, 200L, "TV");
        when(tmdbDAO.findPlatformNo("Watcha")).thenReturn(null);

        tmdbService.saveContentPlatform(content, List.of("watcha"));

        verify(tmdbDAO).findPlatformNo("Watcha");
        verify(tmdbDAO, never()).existsContentPlatform(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
        verify(tmdbDAO, never()).insertContentPlatform(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void personFilmographyShouldRejectInvalidIdAndRoleBeforeApiCall() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getPersonFilmography(null, "ACTOR"));
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getPersonFilmography(0L, "ACTOR"));
        assertThrows(
                IllegalArgumentException.class,
                () -> tmdbService.getPersonFilmography(1L, "WRITER"));

        verifyNoInteractions(tmdbDAO, jsonMapper);
    }

    private ContentVO content(
            int contentNo,
            Long tmdbId,
            String contentType) {
        ContentVO content = new ContentVO();
        content.setContentNo(contentNo);
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        return content;
    }
}
