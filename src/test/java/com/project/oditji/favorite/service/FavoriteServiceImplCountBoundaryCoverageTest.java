package com.project.oditji.favorite.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.favorite.dao.FavoriteDAO;
import com.project.oditji.notification.service.NotificationService;

/** 찜 개수 조회의 null 회원번호와 정상 DAO 위임 분기를 보완합니다. */
class FavoriteServiceImplCountBoundaryCoverageTest {

    private FavoriteDAO favoriteDAO;
    private FavoriteServiceImpl service;

    @BeforeEach
    void setUp() {
        favoriteDAO = mock(FavoriteDAO.class);
        service = new FavoriteServiceImpl(
                favoriteDAO,
                mock(NotificationService.class));
    }

    @Test
    void favoriteCountShouldReturnZeroWithoutDaoForNullMemberAndDelegateForMember() {
        assertEquals(0, service.getFavoriteCount(null));
        verify(favoriteDAO, never()).countFavoriteByMemberNo(org.mockito.ArgumentMatchers.anyLong());

        when(favoriteDAO.countFavoriteByMemberNo(7L)).thenReturn(3);

        assertEquals(3, service.getFavoriteCount(7L));
        verify(favoriteDAO).countFavoriteByMemberNo(7L);
    }
}
