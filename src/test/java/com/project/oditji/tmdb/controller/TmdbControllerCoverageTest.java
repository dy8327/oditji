package com.project.oditji.tmdb.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.tmdb.service.TmdbService;

/** TMDB 관리자 화면과 전체 적재 처리 경로를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class TmdbControllerCoverageTest {

    @Mock
    private TmdbService tmdbService;

    private TmdbController controller;

    @BeforeEach
    void setUp() {
        controller = new TmdbController(tmdbService);
    }

    @Test
    void pageShouldReturnAdminTmdbView() {
        assertEquals("admin/tmdb", controller.tmdbPage());
    }

    @Test
    void postHandlersShouldDelegateAndRedirectToAdminPage() {
        assertEquals("redirect:/admin/tmdb", controller.loadMovie());
        assertEquals("redirect:/admin/tmdb", controller.updateMovieDetail());
        assertEquals("redirect:/admin/tmdb", controller.loadMoviePlatform());
        assertEquals("redirect:/admin/tmdb", controller.loadMovieFull());
        assertEquals("redirect:/admin/tmdb", controller.loadTv());
        assertEquals("redirect:/admin/tmdb", controller.updateTvDetail());
        assertEquals("redirect:/admin/tmdb", controller.loadTvPlatform());
        assertEquals("redirect:/admin/tmdb", controller.loadTvFull());
        assertEquals("redirect:/admin/tmdb", controller.loadAll());

        verify(tmdbService).loadMovieData();
        verify(tmdbService).updateMovieDetailData();
        verify(tmdbService).loadMoviePlatformData();
        verify(tmdbService).loadMovieFullData();
        verify(tmdbService).loadTvData();
        verify(tmdbService).updateTvDetailData();
        verify(tmdbService).loadTvPlatformData();
        verify(tmdbService).loadTvFullData();
        verify(tmdbService).loadAllData();
    }
}
