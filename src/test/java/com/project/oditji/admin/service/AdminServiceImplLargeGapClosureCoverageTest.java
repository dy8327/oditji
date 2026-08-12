package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.admin.vo.ContentManageVO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * AdminServiceImpl의 페이지/빈 컬렉션/정산 번호 OR 조건의 반대쪽 분기를 묶어서 보완합니다.
 */
class AdminServiceImplLargeGapClosureCoverageTest {

    @TempDir
    Path tempDirectory;

    private AdminDAO adminDAO;
    private NotificationService notificationService;
    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        adminDAO = mock(AdminDAO.class);
        notificationService = mock(NotificationService.class);
        service = new AdminServiceImpl(
                adminDAO,
                notificationService,
                tempDirectory.toString());
    }

    @Test
    void memberListShouldCoverPageClampAndNormalPageOperands() {
        when(adminDAO.selectMemberList(any())).thenReturn(Collections.emptyList());

        assertEquals(
                0,
                service.getMemberList("key", "id", "ACTIVE", "USER", 0, 10).size());
        assertEquals(
                0,
                service.getMemberList("key", "id", "ACTIVE", "USER", 3, 10).size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(adminDAO, times(2)).selectMemberList(captor.capture());

        assertEquals(0, captor.getAllValues().get(0).get("offset"));
        assertEquals(20, captor.getAllValues().get(1).get("offset"));
    }

    @Test
    void updateContentShouldTreatNonNullEmptyPlatformListAsReplacementWithNoInsert() {
        ContentManageVO content = new ContentManageVO();
        content.setContentNo(90L);

        service.updateContent(content, List.of());

        verify(adminDAO).updateContent(content);
        verify(adminDAO).deleteContentPlatforms(90L);
        verify(adminDAO, never()).insertContentPlatform(any(), any());
    }

    @Test
    void bulkProductReviewShouldCoverNonNullEmptyListSecondOperand() {
        assertEquals(
                0,
                service.bulkProductReviewAction(List.of(), "approve"));

        verify(adminDAO, never()).adminDeleteProductReview(any());
    }

    @Test
    void settlementValidationShouldCoverNullRequestNumberBeforeNonPositiveOperand() {
        Long nullRequestNo = null;

        assertThrows(
                IllegalArgumentException.class,
                () -> service.confirmSettlement(nullRequestNo));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.rejectSettlement(nullRequestNo, "사유"));
    }

    @Test
    void memberListShouldAllowReturnedWithdrawnMemberWithNullWithdrawDate() {
        MemberManageVO member = new MemberManageVO();
        member.setStatus("WITHDRAWN");
        member.setWithdrawnAt(null);
        when(adminDAO.selectMemberList(any())).thenReturn(List.of(member));

        assertEquals(
                1,
                service.getMemberList(null, null, null, null, 1, 10).size());
    }
}
