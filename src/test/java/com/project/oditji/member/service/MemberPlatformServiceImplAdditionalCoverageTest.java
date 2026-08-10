package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.project.oditji.member.dao.MemberPlatformDAO;

/**
 * 회원 OTT 서비스의 memberNo null 및 DAO null 반환 분기를 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class MemberPlatformServiceImplAdditionalCoverageTest {

    @Mock
    private MemberPlatformDAO memberPlatformDAO;

    private MemberPlatformServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MemberPlatformServiceImpl(memberPlatformDAO);
    }

    @Test
    void findMemberPlatformsShouldReturnEmptyWhenDaoReturnsNull() {
        when(memberPlatformDAO.selectMemberSelectedPlatformList(1L))
                .thenReturn(null);

        assertTrue(
                service.findMemberPlatformList(1L)
                        .isEmpty());
    }

    @Test
    void saveShouldRejectNullMemberNumberBeforeDaoAccess() {
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.saveMemberPlatforms(
                                null,
                                null,
                                "Y"));

        assertEquals(
                "로그인 정보가 없습니다.",
                exception.getMessage());

        verify(memberPlatformDAO, never())
                .deleteMemberPlatforms(
                        org.mockito.ArgumentMatchers.anyLong());
    }
}
