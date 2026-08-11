package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 수동 OTT 보완 파일의 JSON 파싱 실패와 UTF-8 읽기 실패 분기를 검증합니다.
 */
class SearchContentManualOverrideServiceFinalCoverageTest {

    @TempDir
    Path tempDirectory;

    private SearchContentManualOverrideService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentManualOverrideService(mock(TmdbProviderService.class));
        ReflectionTestUtils.setField(service, "enabled", true);
    }

    @Test
    void malformedJsonShouldBecomeClearIllegalStateException() throws IOException {
        Path file = tempDirectory.resolve("malformed.json");
        Files.writeString(file, "{");
        ReflectionTestUtils.setField(service, "overridePath", file.toString());

        assertThrows(IllegalStateException.class, service::reload);
    }

    @Test
    void malformedUtf8ShouldReachIOExceptionBranch() throws IOException {
        Path file = tempDirectory.resolve("invalid-utf8.json");
        Files.write(file, new byte[] {(byte) 0xC3, (byte) 0x28});
        ReflectionTestUtils.setField(service, "overridePath", file.toString());

        assertThrows(IllegalStateException.class, service::reload);
    }
}
