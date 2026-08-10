package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 관리자 상품 이미지 실제 삭제 helper의 slash, 상위경로, IOException, 비활성 트랜잭션 분기를 보완합니다.
 */
class AdminServiceImplImageEdgeCoverageTest {

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
    void directDeleteRegistrationShouldDeleteExistingFileWhenNoSynchronizationIsActive() throws Exception {
        Path file = tempDirectory.resolve("direct.png");
        Files.writeString(file, "image");

        ReflectionTestUtils.invokeMethod(
                service,
                "registerImageFileDeleteAfterCommit",
                List.of("/uploads/product/direct.png"));

        assertFalse(Files.exists(file));
    }

    @Test
    void physicalDeleteShouldSupportBackslashAndNoSlashFileNames() throws Exception {
        Path first = tempDirectory.resolve("first.png");
        Path second = tempDirectory.resolve("second.png");

        Files.writeString(first, "first");
        Files.writeString(second, "second");

        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                "folder\\first.png");

        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                "second.png");

        assertFalse(Files.exists(first));
        assertFalse(Files.exists(second));
    }

    @Test
    void parentTraversalFilenameShouldBeRejectedWithoutTouchingParentDirectory() {
        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "deletePhysicalImageFile",
                        ".."));

        assertTrue(Files.exists(tempDirectory));
    }

    @Test
    void nonEmptyDirectoryDeleteFailureShouldBeCaughtAndDirectoryShouldRemain() throws Exception {
        Path directory = tempDirectory.resolve("non-empty");
        Files.createDirectories(directory);
        Files.writeString(
                directory.resolve("child.txt"),
                "child");

        assertDoesNotThrow(
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "deletePhysicalImageFile",
                        "non-empty"));

        assertTrue(Files.exists(directory));
    }
}
