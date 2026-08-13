package com.project.oditji.favorite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.favorite.dao.FavoriteDAO;
import com.project.oditji.favorite.vo.FavoriteReleaseTargetVO;
import com.project.oditji.notification.service.NotificationService;

/** 찜 콘텐츠 공개 예정 알림 생성의 null/empty/skip/success 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplReleaseCoverageTest {

    @Mock
    private FavoriteDAO favoriteDAO;

    @Mock
    private NotificationService notificationService;

    private FavoriteServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FavoriteServiceImpl(favoriteDAO, notificationService);
    }

    @Test
    void notifyUpcomingReleasesShouldReturnZeroForNullTargetList() {
        when(favoriteDAO.selectFavoriteReleaseTargetList()).thenReturn(null);

        assertEquals(0, service.notifyUpcomingReleases());
        verify(notificationService, never()).createForMember(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void notifyUpcomingReleasesShouldReturnZeroForEmptyTargetList() {
        when(favoriteDAO.selectFavoriteReleaseTargetList()).thenReturn(List.of());

        assertEquals(0, service.notifyUpcomingReleases());
        verify(notificationService, never()).createForMember(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void notifyUpcomingReleasesShouldSkipInvalidTargetsAndNotifyValidMember() {
        FavoriteReleaseTargetVO missingMember = target(
                null, 21L, 31L, "MOVIE", "회원 없음");
        FavoriteReleaseTargetVO valid = target(
                10L, 22L, 32L, "TV", "내일 공개");

        when(favoriteDAO.selectFavoriteReleaseTargetList())
                .thenReturn(java.util.Arrays.asList(null, missingMember, valid));

        int notifiedCount = service.notifyUpcomingReleases();

        assertEquals(1, notifiedCount);
        verify(notificationService).createForMember(
                10L,
                "CONTENT_RELEASE",
                "찜한 콘텐츠가 내일 공개돼요",
                "'내일 공개'가 내일 공개됩니다. 놓치지 마세요!",
                "/content/prepare?tmdbId=32&contentType=TV",
                "CONTENT",
                22L);
    }

    @Test
    void notifyUpcomingReleasesShouldNotifyEveryValidTarget() {
        FavoriteReleaseTargetVO first = target(
                1L, 101L, 1001L, "MOVIE", "영화");
        FavoriteReleaseTargetVO second = target(
                2L, 102L, 1002L, "TV", "시리즈");

        when(favoriteDAO.selectFavoriteReleaseTargetList())
                .thenReturn(List.of(first, second));

        assertEquals(2, service.notifyUpcomingReleases());

        verify(notificationService).createForMember(
                1L,
                "CONTENT_RELEASE",
                "찜한 콘텐츠가 내일 공개돼요",
                "'영화'가 내일 공개됩니다. 놓치지 마세요!",
                "/content/prepare?tmdbId=1001&contentType=MOVIE",
                "CONTENT",
                101L);
        verify(notificationService).createForMember(
                2L,
                "CONTENT_RELEASE",
                "찜한 콘텐츠가 내일 공개돼요",
                "'시리즈'가 내일 공개됩니다. 놓치지 마세요!",
                "/content/prepare?tmdbId=1002&contentType=TV",
                "CONTENT",
                102L);
    }

    private FavoriteReleaseTargetVO target(
            Long memberNo,
            Long contentNo,
            Long tmdbId,
            String contentType,
            String title) {

        FavoriteReleaseTargetVO target = new FavoriteReleaseTargetVO();
        target.setMemberNo(memberNo);
        target.setContentNo(contentNo);
        target.setTmdbId(tmdbId);
        target.setContentType(contentType);
        target.setTitle(title);
        return target;
    }
}
