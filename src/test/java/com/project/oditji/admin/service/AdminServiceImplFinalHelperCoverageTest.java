package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.notification.service.NotificationService;

/** 관리자 서비스의 단순 helper와 파일 경로 경계 조건을 보완합니다. */
class AdminServiceImplFinalHelperCoverageTest {

    @TempDir
    Path tempDir;

    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(
                mock(AdminDAO.class),
                mock(NotificationService.class),
                tempDir.toString());
    }

    @Test
    void parameterHelpersShouldPreserveNullsAndNormalizePagingOffset() {
        Map<?, ?> keyword = invoke("keywordParam", (Object) null);
        Map<?, ?> typed = invoke("keywordSearchTypeParam", "검색", "name");
        Map<?, ?> member = invoke("memberSearchParam", "k", "id", "ACTIVE", "USER");
        Map<String, Object> pagingSource = new java.util.HashMap<String, Object>();
        Map<?, ?> paging = invoke("withPaging", pagingSource, 0, 10);

        assertTrue(keyword.containsKey("keyword"));
        assertNull(keyword.get("keyword"));
        assertEquals("name", typed.get("searchType"));
        assertEquals("ACTIVE", member.get("status"));
        assertEquals(0, paging.get("offset"));
        assertEquals(10, paging.get("pageSize"));
    }

    @Test
    void remainingDeleteDaysShouldSkipNonWithdrawnAndNullDateAndClampExpiredMember() {
        MemberManageVO active = new MemberManageVO();
        active.setStatus("ACTIVE");
        invoke("fillRemainingDeleteDays", active);
        assertNull(active.getRemainingDeleteDays());

        MemberManageVO noDate = new MemberManageVO();
        noDate.setStatus("WITHDRAWN");
        invoke("fillRemainingDeleteDays", noDate);
        assertNull(noDate.getRemainingDeleteDays());

        MemberManageVO expired = new MemberManageVO();
        expired.setStatus("WITHDRAWN");
        expired.setWithdrawnAt(LocalDateTime.now().minusDays(20));
        invoke("fillRemainingDeleteDays", expired);
        assertEquals(0, expired.getRemainingDeleteDays());

        MemberManageVO recent = new MemberManageVO();
        recent.setStatus("WITHDRAWN");
        recent.setWithdrawnAt(LocalDateTime.now().minusDays(1));
        invoke("fillRemainingDeleteDays", recent);
        assertTrue(recent.getRemainingDeleteDays() >= 0);
    }

    @Test
    void identifierValidatorsShouldRejectNullAndNonPositiveValuesAndAcceptPositiveValues() {
        assertThrows(IllegalArgumentException.class, () -> invoke("validateEventNo", (Object) null));
        assertThrows(IllegalArgumentException.class, () -> invoke("validateEventNo", 0L));
        invoke("validateEventNo", 1L);

        assertThrows(IllegalArgumentException.class, () -> invoke("validateProductNo", (Object) null));
        assertThrows(IllegalArgumentException.class, () -> invoke("validateProductNo", -1L));
        invoke("validateProductNo", 1L);
    }

    @Test
    void imageDeleteHelpersShouldIgnoreNullBlankEmptyAndTrailingSlashPaths() {
        assertDoesNotThrow(() -> {
            invoke("registerImageFileDeleteAfterCommit", (Object) null);
            invoke("registerImageFileDeleteAfterCommit", List.of());
            invoke("deletePhysicalImageFile", (Object) null);
            invoke("deletePhysicalImageFile", "   ");
            invoke("deletePhysicalImageFile", "/uploads/product/");
            invoke("deletePhysicalImageFiles", List.of("", "/uploads/product/not-found.jpg"));
        });
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
