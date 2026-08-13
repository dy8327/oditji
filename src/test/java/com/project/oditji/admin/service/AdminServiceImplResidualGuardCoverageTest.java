package com.project.oditji.admin.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.admin.dao.AdminDAO;
import com.project.oditji.notification.service.NotificationService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** 관리자 상품 이미지 삭제의 로그 guard false 분기를 보완합니다. */
class AdminServiceImplResidualGuardCoverageTest {

    @TempDir
    Path tempDirectory;

    private AdminServiceImpl service;
    private Logger targetLogger;
    private Level originalLevel;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(
                mock(AdminDAO.class),
                mock(NotificationService.class),
                tempDirectory.toString());
        targetLogger = (Logger) LoggerFactory.getLogger(AdminServiceImpl.class);
        originalLevel = targetLogger.getLevel();
        targetLogger.setLevel(Level.OFF);
    }

    @AfterEach
    void tearDown() {
        targetLogger.setLevel(originalLevel);
    }

    @Test
    void imageDeleteShouldCoverWarnAndErrorDisabledBranches() throws Exception {
        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                "..");

        Path nonEmptyDirectory = Files.createDirectory(tempDirectory.resolve("non-empty"));
        Files.writeString(nonEmptyDirectory.resolve("keep.txt"), "keep");

        ReflectionTestUtils.invokeMethod(
                service,
                "deletePhysicalImageFile",
                "non-empty");

        assertTrue(Files.exists(nonEmptyDirectory));
        assertTrue(Files.exists(nonEmptyDirectory.resolve("keep.txt")));
    }
}
