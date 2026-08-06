package com.project.oditji.wish.service;

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

import com.project.oditji.wish.dao.WishDAO;
import com.project.oditji.wish.vo.WishVO;

/** 상품 찜 서비스의 누락 입력과 빈 조회 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class WishServiceImplBranchCoverageTest {

    @Mock
    private WishDAO wishDAO;

    private WishServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WishServiceImpl(wishDAO);
    }

    @Test
    void toggleWishShouldRejectNullAndMissingProduct() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.toggleWish(null));

        WishVO missingProduct = new WishVO();
        missingProduct.setMemberNo(1L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.toggleWish(missingProduct));
    }

    @Test
    void isWishedShouldCoverIncompletePositiveAndNegativeResults() {
        WishVO missingMember = new WishVO();
        missingMember.setProductNo(10);
        assertFalse(service.isWished(missingMember));

        WishVO missingProduct = new WishVO();
        missingProduct.setMemberNo(1L);
        assertFalse(service.isWished(missingProduct));
        verify(wishDAO, never()).countWish(missingProduct);

        WishVO valid = wish(1L, 10);
        when(wishDAO.countWish(valid)).thenReturn(0, 2);
        assertFalse(service.isWished(valid));
        assertTrue(service.isWished(valid));
    }

    @Test
    void collectionQueriesShouldCoverNullDaoAndEmptyInputs() {
        when(wishDAO.selectWishList(1L)).thenReturn(null);
        assertTrue(service.selectWishList(1L).isEmpty());

        assertTrue(service.getWishedProductNoSet(null).isEmpty());
        when(wishDAO.selectWishedProductNoList(1L)).thenReturn(List.of());
        assertTrue(service.getWishedProductNoSet(1L).isEmpty());
    }

    private WishVO wish(Long memberNo, Integer productNo) {
        WishVO wish = new WishVO();
        wish.setMemberNo(memberNo);
        wish.setProductNo(productNo);
        return wish;
    }
}
