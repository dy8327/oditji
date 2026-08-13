package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.notification.service.NotificationService;

/** 관리자 서비스의 번호 경계값, 탈퇴 잔여일, 커밋 후 이미지 삭제 분기를 보완합니다. */
class AdminServiceImplTransactionAndBoundaryCoverageTest {

    @TempDir
    Path tempDirectory;

    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(
                mock(AdminDAO.class),
                mock(NotificationService.class),
                tempDirectory.toString());
    }

    @AfterEach
    void clearSynchronization() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void numberValidatorsShouldCoverNullZeroAndValidValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventNo", new Object[] { null }));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventNo", 0L));
        assertDoesNotThrow(() -> invoke("validateEventNo", 1L));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductNo", new Object[] { null }));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductNo", 0L));
        assertDoesNotThrow(() -> invoke("validateProductNo", 1L));
    }

    @Test
    void remainingDeleteDaysShouldSkipNonWithdrawnAndNullDateAndClampExpiredMemberToZero() {
        MemberManageVO active = new MemberManageVO();
        active.setStatus("ACTIVE");
        invoke("fillRemainingDeleteDays", active);
        assertNull(active.getRemainingDeleteDays());

        MemberManageVO noDate = new MemberManageVO();
        noDate.setStatus("WITHDRAWN");
        noDate.setWithdrawnAt(null);
        invoke("fillRemainingDeleteDays", noDate);
        assertNull(noDate.getRemainingDeleteDays());

        MemberManageVO expired = new MemberManageVO();
        expired.setStatus("WITHDRAWN");
        expired.setWithdrawnAt(LocalDateTime.now().minusDays(20));
        invoke("fillRemainingDeleteDays", expired);
        assertNotNull(expired.getRemainingDeleteDays());
        assertTrue(expired.getRemainingDeleteDays() >= 0);
    }

    @Test
    void imageDeletionRegistrationShouldReturnForNullAndEmptyLists() {
        assertDoesNotThrow(() -> invoke("registerImageFileDeleteAfterCommit", new Object[] { null }));
        assertDoesNotThrow(() -> invoke("registerImageFileDeleteAfterCommit", List.of()));
    }

    @Test
    void imageDeletionShouldRunImmediatelyWithoutSynchronization() throws Exception {
        Path image = Files.writeString(tempDirectory.resolve("immediate.png"), "image");
        assertTrue(Files.exists(image));

        invoke("registerImageFileDeleteAfterCommit", List.of("/uploads/product/immediate.png"));

        assertFalse(Files.exists(image));
    }

    @Test
    void imageDeletionShouldWaitUntilAfterCommitWhenSynchronizationIsActive() throws Exception {
        Path image = Files.writeString(tempDirectory.resolve("after-commit.png"), "image");
        TransactionSynchronizationManager.initSynchronization();

        invoke("registerImageFileDeleteAfterCommit", List.of("/uploads/product/after-commit.png"));
        assertTrue(Files.exists(image));

        List<TransactionSynchronization> synchronizations =
                TransactionSynchronizationManager.getSynchronizations();
        assertFalse(synchronizations.isEmpty());
        for (TransactionSynchronization synchronization : synchronizations) {
            synchronization.afterCommit();
        }

        assertFalse(Files.exists(image));
    }

    @Test
    void physicalImageDeleteShouldIgnoreNullBlankAndTrailingSlashAndDeletePlainFilename() throws Exception {
        assertDoesNotThrow(() -> invoke("deletePhysicalImageFile", new Object[] { null }));
        assertDoesNotThrow(() -> invoke("deletePhysicalImageFile", "   "));
        assertDoesNotThrow(() -> invoke("deletePhysicalImageFile", "/uploads/product/"));

        Path image = Files.writeString(tempDirectory.resolve("plain.png"), "image");
        invoke("deletePhysicalImageFile", "plain.png");
        assertFalse(Files.exists(image));
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
