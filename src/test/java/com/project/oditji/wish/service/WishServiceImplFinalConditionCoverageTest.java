package com.project.oditji.wish.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.wish.dao.WishDAO;

/** 회원별 상품 찜 개수 조회의 null/정상 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class WishServiceImplFinalConditionCoverageTest {

    @Mock
    private WishDAO wishDAO;

    private WishServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new WishServiceImpl(wishDAO);
    }

    @Test
    void wishCountShouldReturnZeroForNullAndDelegateForMember() {
        assertEquals(0, service.getWishCount(null));

        when(wishDAO.countWishByMemberNo(7L)).thenReturn(3);
        assertEquals(3, service.getWishCount(7L));
        verify(wishDAO).countWishByMemberNo(7L);
    }
}
