package com.project.oditji.wish.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.wish.dao.WishDAO;
import com.project.oditji.wish.vo.WishVO;

/**
 * 상품 찜 등록, 해제, 목록 조회 기능을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class WishServiceImplTest {

    @Mock
    private WishDAO wishDAO;

    private WishServiceImpl wishService;

    @BeforeEach
    void setUp() {
        wishService = new WishServiceImpl(wishDAO);
    }

    @Test
    void toggleWishShouldInsertWhenNotAlreadyWished() {

        WishVO wish = createWish();
        when(wishDAO.countWish(wish)).thenReturn(0);

        assertTrue(wishService.toggleWish(wish));
        verify(wishDAO).insertWish(wish);
        verify(wishDAO, never()).deleteWish(wish);
    }

    @Test
    void toggleWishShouldDeleteWhenAlreadyWished() {

        WishVO wish = createWish();
        when(wishDAO.countWish(wish)).thenReturn(1);

        assertFalse(wishService.toggleWish(wish));
        verify(wishDAO).deleteWish(wish);
        verify(wishDAO, never()).insertWish(wish);
    }

    @Test
    void toggleWishShouldRejectMissingMember() {

        WishVO wish = new WishVO();
        wish.setProductNo(10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> wishService.toggleWish(wish));

        assertEquals("회원 정보가 없습니다.", exception.getMessage());
    }

    @Test
    void isWishedShouldReturnFalseForIncompleteRequest() {

        assertFalse(wishService.isWished(null));
        verify(wishDAO, never()).countWish(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void selectWishListShouldReturnEmptyListWhenMemberIsNull() {
        assertTrue(wishService.selectWishList(null).isEmpty());
    }

    @Test
    void selectWishListShouldReturnDaoResult() {

        GoodsVO goods = new GoodsVO();
        List<GoodsVO> expected = List.of(goods);
        when(wishDAO.selectWishList(1L)).thenReturn(expected);

        assertEquals(expected, wishService.selectWishList(1L));
    }

    @Test
    void getWishedProductNoSetShouldRemoveDuplicateNumbers() {

        when(wishDAO.selectWishedProductNoList(1L))
                .thenReturn(List.of(10, 10, 20));

        Set<Integer> result = wishService.getWishedProductNoSet(1L);

        assertEquals(Set.of(10, 20), result);
    }

    @Test
    void getWishedProductNoSetShouldReturnEmptySetForNullDaoResult() {

        when(wishDAO.selectWishedProductNoList(1L)).thenReturn(null);

        assertTrue(wishService.getWishedProductNoSet(1L).isEmpty());
    }

    private WishVO createWish() {

        WishVO wish = new WishVO();
        wish.setMemberNo(1L);
        wish.setProductNo(10);
        return wish;
    }
}
