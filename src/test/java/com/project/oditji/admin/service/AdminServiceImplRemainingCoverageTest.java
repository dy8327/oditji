package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.admin.vo.MemberManageVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 관리자 서비스의 삭제 유예일, 번호 검증, 이미지 물리 삭제/트랜잭션 분기를 보완합니다.
 */
class AdminServiceImplRemainingCoverageTest {

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

    @Test
    void remainingDeleteDaysShouldIgnoreNonWithdrawnAndMissingTimestampThenCalculatePositiveDays() {
        MemberManageVO active =
                new MemberManageVO();
        active.setStatus("ACTIVE");

        ReflectionTestUtils.invokeMethod(
                service,
                "fillRemainingDeleteDays",
                active);

        assertNull(active.getRemainingDeleteDays());

        MemberManageVO noTimestamp =
                new MemberManageVO();
        noTimestamp.setStatus("WITHDRAWN");

        ReflectionTestUtils.invokeMethod(
                service,
                "fillRemainingDeleteDays",
                noTimestamp);

        assertNull(
                noTimestamp.getRemainingDeleteDays());

        MemberManageVO recent =
                new MemberManageVO();
        recent.setStatus("WITHDRAWN");
        recent.setWithdrawnAt(
                LocalDateTime.now());

        ReflectionTestUtils.invokeMethod(
                service,
                "fillRemainingDeleteDays",
                recent);

        assertTrue(
                recent.getRemainingDeleteDays() >= 6);
    }

    @Test
    void eventAndProductNumberValidatorsShouldCoverNullZeroAndPositiveValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateEventNo",
                        (Object) null));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateEventNo",
                        0L));

        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateEventNo",
                        1L));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateProductNo",
                        -1L));

        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateProductNo",
                        1L));
    }

    @Test
    void imageDeletionRegistrationShouldIgnoreNullAndEmptyLists() {
        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "registerImageFileDeleteAfterCommit",
                        (Object) null));

        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "registerImageFileDeleteAfterCommit",
                        List.of()));
    }

    @Test
    void directImageDeletionShouldHandleNullBlankTrailingSlashAndExistingFile() throws Exception {
        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                (Object) null);

        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                "   ");

        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                "folder/");

        Path image =
                tempDirectory.resolve(
                        "delete-me.png");
        Files.writeString(
                image,
                "image");

        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                "/uploads/product/delete-me.png");

        assertFalse(Files.exists(image));
    }

    @Test
    void transactionSynchronizationShouldDeleteFileOnlyAfterCommit() throws Exception {
        Path image =
                tempDirectory.resolve(
                        "after-commit.png");
        Files.writeString(
                image,
                "image");

        TransactionSynchronizationManager
                .initSynchronization();

        try {
            ReflectionTestUtils.invokeMethod(
                    service,
                    "registerImageFileDeleteAfterCommit",
                    List.of(
                            "/uploads/product/after-commit.png"));

            assertTrue(Files.exists(image));

            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager
                            .getSynchronizations();

            assertEquals(1, synchronizations.size());

            synchronizations.forEach(
                    TransactionSynchronization::afterCommit);

            assertFalse(Files.exists(image));

        } finally {
            TransactionSynchronizationManager
                    .clearSynchronization();
        }
    }
}
