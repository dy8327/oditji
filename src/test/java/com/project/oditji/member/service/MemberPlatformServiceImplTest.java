package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

import com.project.oditji.member.dao.MemberPlatformDAO;
import com.project.oditji.member.vo.PlatformVO;

/**
 * 회원별 이용 OTT 선택 정책과 저장 흐름을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class MemberPlatformServiceImplTest {

    @Mock
    private MemberPlatformDAO memberPlatformDAO;

    private MemberPlatformServiceImpl memberPlatformService;

    @BeforeEach
    void setUp() {
        memberPlatformService = new MemberPlatformServiceImpl(memberPlatformDAO);
    }

    @Test
    void findPlatformListShouldReturnEmptyListWhenDaoReturnsNull() {

        when(memberPlatformDAO.findPlatformList()).thenReturn(null);

        assertTrue(memberPlatformService.findPlatformList().isEmpty());
    }

    @Test
    void findMemberPlatformListShouldSkipDaoWhenMemberIsNull() {

        assertTrue(memberPlatformService.findMemberPlatformList(null).isEmpty());
        verify(memberPlatformDAO, never())
                .selectMemberSelectedPlatformList(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void saveMemberPlatformsShouldRejectMissingSelection() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> memberPlatformService.saveMemberPlatforms(1L, List.of(), "N"));

        assertEquals(
                "이용 중인 OTT를 선택하거나 'OTT 없음'을 선택해주세요.",
                exception.getMessage());
    }

    @Test
    void saveMemberPlatformsShouldRejectPlatformAndNoOttTogether() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> memberPlatformService.saveMemberPlatforms(1L, List.of(1L), "Y"));

        assertEquals(
                "OTT 없음과 다른 OTT는 동시에 선택할 수 없습니다.",
                exception.getMessage());
    }

    @Test
    void saveMemberPlatformsShouldDeleteOnlyWhenNoOttSelected() {

        memberPlatformService.saveMemberPlatforms(1L, null, "Y");

        verify(memberPlatformDAO).deleteMemberPlatforms(1L);
        verify(memberPlatformDAO, never()).insertMemberPlatform(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void saveMemberPlatformsShouldReplaceExistingSelections() {

        memberPlatformService.saveMemberPlatforms(
                1L,
                java.util.Arrays.asList(2L, null, 5L),
                "N");

        verify(memberPlatformDAO).deleteMemberPlatforms(1L);
        verify(memberPlatformDAO).insertMemberPlatform(1L, 2L);
        verify(memberPlatformDAO).insertMemberPlatform(1L, 5L);
    }

    @Test
    void countMemberPlatformShouldReturnZeroForNullMember() {

        assertEquals(0, memberPlatformService.countMemberPlatform(null));
        verify(memberPlatformDAO, never()).countMemberPlatform(
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void countMemberPlatformShouldReturnDaoCount() {

        when(memberPlatformDAO.countMemberPlatform(1L)).thenReturn(3);

        assertEquals(3, memberPlatformService.countMemberPlatform(1L));
    }

    @Test
    void findMemberPlatformListShouldReturnDaoResult() {

        PlatformVO platform = new PlatformVO();
        List<PlatformVO> expected = List.of(platform);
        when(memberPlatformDAO.selectMemberSelectedPlatformList(1L))
                .thenReturn(expected);

        assertEquals(expected, memberPlatformService.findMemberPlatformList(1L));
    }
}
