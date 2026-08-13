package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.common.interceptor.AccessLogInterceptor;
import com.project.oditji.common.interceptor.AdminCheckInterceptor;
import com.project.oditji.common.interceptor.BusinessCheckInterceptor;
import com.project.oditji.common.interceptor.LoginCheckInterceptor;

/** 리소스 경로의 trailing slash 삼항식 양쪽 분기를 보완합니다. */
class WebConfigFinalConditionCoverageTest {

    @TempDir
    Path tempDirectory;

    @Test
    void resourceLocationShouldHandleDirectoryAndFileLikePaths() throws Exception {
        Path directoryPath = Files.createDirectory(tempDirectory.resolve("profile"));
        Path fileLikePath = tempDirectory.resolve("product-file");

        WebConfig config = new WebConfig(
                mock(LoginCheckInterceptor.class),
                mock(AdminCheckInterceptor.class),
                mock(BusinessCheckInterceptor.class),
                mock(AccessLogInterceptor.class),
                directoryPath.toString(),
                fileLikePath.toString(),
                tempDirectory.resolve("event").toString());

        String directoryLocation = (String) ReflectionTestUtils.invokeMethod(
                config,
                "toResourceLocation",
                directoryPath.toString());
        String fileLocation = (String) ReflectionTestUtils.invokeMethod(
                config,
                "toResourceLocation",
                fileLikePath.toString());

        assertTrue(directoryLocation.endsWith("/"));
        assertTrue(fileLocation.endsWith("/"));
        assertFalse(fileLocation.endsWith("//"));
    }
}
