package com.project.oditji.favorite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.notification.service.NotificationService;

/** 찜 공개 알림의 오늘/내일 조건과 MOVIE 정규화 잔여 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplResidualReleaseConditionCoverageTest {

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
    void notifyUpcomingReleasesShouldCoverTodayAndExplicitTomorrowConditions() {
        FavoriteReleaseTargetVO today = target(
                1L, 101L, 1001L, "MOVIE", "오늘 작품", 0);
        FavoriteReleaseTargetVO tomorrow = target(
                2L, 102L, 1002L, "TV", "내일 작품", 1);

        when(favoriteDAO.selectFavoriteReleaseTargetList())
                .thenReturn(List.of(today, tomorrow));

        assertEquals(2, service.notifyUpcomingReleases());

        verify(notificationService).createForMember(
                1L,
                "CONTENT_RELEASE",
                "찜한 콘텐츠가 오늘 공개돼요",
                "'오늘 작품'가 오늘 공개됩니다. 지금 만나보세요!",
                "/content/prepare?tmdbId=1001&contentType=MOVIE",
                "CONTENT",
                101L);
        verify(notificationService).createForMember(
                2L,
                "CONTENT_RELEASE",
                "찜한 콘텐츠가 내일 공개돼요",
                "'내일 작품'가 내일 공개됩니다. 놓치지 마세요!",
                "/content/prepare?tmdbId=1002&contentType=TV",
                "CONTENT",
                102L);
    }

    @Test
    void isFavoriteByTmdbShouldCoverValidMovieWithZeroDaoCount() {
        FavoriteVO favorite = new FavoriteVO();
        favorite.setMemberNo(3L);
        favorite.setTmdbId(2001L);
        favorite.setContentType("MOVIE");
        when(favoriteDAO.countFavoriteByTmdb(favorite)).thenReturn(0);

        assertFalse(service.isFavoriteByTmdb(favorite));
        assertEquals("MOVIE", favorite.getContentType());
    }

    private FavoriteReleaseTargetVO target(
            Long memberNo,
            Long contentNo,
            Long tmdbId,
            String contentType,
            String title,
            Integer daysUntilRelease) {

        FavoriteReleaseTargetVO target = new FavoriteReleaseTargetVO();
        target.setMemberNo(memberNo);
        target.setContentNo(contentNo);
        target.setTmdbId(tmdbId);
        target.setContentType(contentType);
        target.setTitle(title);
        target.setDaysUntilRelease(daysUntilRelease);
        return target;
    }
}
