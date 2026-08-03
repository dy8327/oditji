package com.project.oditji.favorite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.dao.FavoriteDAO;
import com.project.oditji.favorite.vo.FavoriteVO;

/**
 * 콘텐츠 찜 등록, 해제, 조회 분기를 DB 없이 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceImplTest {

    @Mock
    private FavoriteDAO favoriteDAO;

    private FavoriteServiceImpl favoriteService;

    @BeforeEach
    void setUp() {
        favoriteService = new FavoriteServiceImpl(favoriteDAO);
    }

    @Test
    void toggleFavoriteShouldInsertWhenNotAlreadyFavorite() {

        FavoriteVO favorite = createFavorite();
        when(favoriteDAO.countFavorite(favorite)).thenReturn(0);

        boolean result = favoriteService.toggleFavorite(favorite);

        assertTrue(result);
        verify(favoriteDAO).insertFavorite(favorite);
        verify(favoriteDAO, never()).deleteFavorite(favorite);
    }

    @Test
    void toggleFavoriteShouldDeleteWhenAlreadyFavorite() {

        FavoriteVO favorite = createFavorite();
        when(favoriteDAO.countFavorite(favorite)).thenReturn(1);

        boolean result = favoriteService.toggleFavorite(favorite);

        assertFalse(result);
        verify(favoriteDAO).deleteFavorite(favorite);
        verify(favoriteDAO, never()).insertFavorite(favorite);
    }

    @Test
    void toggleFavoriteShouldRejectMissingContentInformation() {

        FavoriteVO favorite = new FavoriteVO();
        favorite.setMemberNo(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.toggleFavorite(favorite));

        assertEquals("콘텐츠 정보가 없습니다.", exception.getMessage());
    }

    @Test
    void isFavoriteShouldReturnFalseForIncompleteRequestWithoutDaoCall() {

        assertFalse(favoriteService.isFavorite(null));
        verify(favoriteDAO, never()).countFavorite(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void isFavoriteByTmdbShouldNormalizeContentType() {

        FavoriteVO favorite = new FavoriteVO();
        favorite.setMemberNo(1L);
        favorite.setTmdbId(100L);
        favorite.setContentType(" movie ");
        when(favoriteDAO.countFavoriteByTmdb(favorite)).thenReturn(1);

        boolean result = favoriteService.isFavoriteByTmdb(favorite);

        assertTrue(result);
        assertEquals("MOVIE", favorite.getContentType());
    }

    @Test
    void isFavoriteByTmdbShouldRejectUnsupportedContentType() {

        FavoriteVO favorite = new FavoriteVO();
        favorite.setMemberNo(1L);
        favorite.setTmdbId(100L);
        favorite.setContentType("ANIMATION");

        assertThrows(
                IllegalArgumentException.class,
                () -> favoriteService.isFavoriteByTmdb(favorite));
    }

    @Test
    void selectFavoriteListShouldReturnEmptyListWhenDaoReturnsNull() {

        when(favoriteDAO.selectFavoriteList(1L)).thenReturn(null);

        assertTrue(favoriteService.selectFavoriteList(1L).isEmpty());
    }

    @Test
    void selectFavoriteListShouldReturnDaoResult() {

        ContentVO content = new ContentVO();
        List<ContentVO> expected = List.of(content);
        when(favoriteDAO.selectFavoriteList(1L)).thenReturn(expected);

        assertEquals(expected, favoriteService.selectFavoriteList(1L));
    }

    private FavoriteVO createFavorite() {

        FavoriteVO favorite = new FavoriteVO();
        favorite.setMemberNo(1L);
        favorite.setContentNo(10L);
        return favorite;
    }
}
