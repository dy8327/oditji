package com.project.oditji.favorite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.favorite.dao.FavoriteDAO;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.notification.service.NotificationService;

/** 콘텐츠 찜 서비스의 누락 입력, TMDB 유형, 빈 목록 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplBranchCoverageTest {

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
    void toggleFavoriteShouldRejectNullAndMissingMember() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.toggleFavorite(null));

        FavoriteVO missingMember = new FavoriteVO();
        missingMember.setContentNo(10L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.toggleFavorite(missingMember));
    }

    @Test
    void isFavoriteShouldCoverIncompleteAndDaoResults() {
        FavoriteVO missingMember = new FavoriteVO();
        missingMember.setContentNo(10L);
        assertFalse(service.isFavorite(missingMember));

        FavoriteVO missingContent = new FavoriteVO();
        missingContent.setMemberNo(1L);
        assertFalse(service.isFavorite(missingContent));
        verify(favoriteDAO, never()).countFavorite(missingContent);

        FavoriteVO valid = new FavoriteVO();
        valid.setMemberNo(1L);
        valid.setContentNo(10L);
        when(favoriteDAO.countFavorite(valid)).thenReturn(0, 1);
        assertFalse(service.isFavorite(valid));
        assertTrue(service.isFavorite(valid));
    }

    @Test
    void isFavoriteByTmdbShouldCoverEveryGuardAndTvNormalization() {
        assertFalse(service.isFavoriteByTmdb(null));

        FavoriteVO missingMember = tmdbFavorite(null, 10L, "MOVIE");
        assertFalse(service.isFavoriteByTmdb(missingMember));

        FavoriteVO missingTmdb = tmdbFavorite(1L, null, "MOVIE");
        assertFalse(service.isFavoriteByTmdb(missingTmdb));

        FavoriteVO invalidTmdb = tmdbFavorite(1L, 0L, "MOVIE");
        assertFalse(service.isFavoriteByTmdb(invalidTmdb));

        FavoriteVO missingType = tmdbFavorite(1L, 10L, null);
        assertFalse(service.isFavoriteByTmdb(missingType));

        FavoriteVO blankType = tmdbFavorite(1L, 10L, " ");
        assertFalse(service.isFavoriteByTmdb(blankType));

        FavoriteVO tv = tmdbFavorite(1L, 10L, " tv ");
        when(favoriteDAO.countFavoriteByTmdb(tv)).thenReturn(0);
        assertFalse(service.isFavoriteByTmdb(tv));
        assertEquals("TV", tv.getContentType());
    }

    @Test
    void selectFavoriteListShouldReturnEmptyForNullMember() {
        assertTrue(service.selectFavoriteList(null).isEmpty());
        verify(favoriteDAO, never()).selectFavoriteList(org.mockito.ArgumentMatchers.anyLong());
    }

    private FavoriteVO tmdbFavorite(Long memberNo, Long tmdbId, String contentType) {
        FavoriteVO favorite = new FavoriteVO();
        favorite.setMemberNo(memberNo);
        favorite.setTmdbId(tmdbId);
        favorite.setContentType(contentType);
        return favorite;
    }
}
