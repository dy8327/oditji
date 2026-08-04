package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** TMDB 클라이언트의 설정 접근자와 URL 인코딩을 네트워크 없이 검증합니다. */
class TmdbApiClientTest {

    private TmdbApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = new TmdbApiClient();
        ReflectionTestUtils.setField(
                apiClient,
                "baseUrl",
                "https://api.test/3");
        ReflectionTestUtils.setField(
                apiClient,
                "language",
                "ko-KR");
        ReflectionTestUtils.setField(
                apiClient,
                "region",
                "KR");
    }

    @Test
    void gettersShouldReturnConfiguredValues() {
        assertEquals("https://api.test/3", apiClient.getBaseUrl());
        assertEquals("ko-KR", apiClient.getLanguage());
        assertEquals("KR", apiClient.getRegion());
    }

    @Test
    void encodeShouldHandleSpacesSymbolsUnicodeAndNull() {
        assertEquals("", apiClient.encode(null));
        assertEquals("hello+world", apiClient.encode("hello world"));
        assertEquals("a%2Bb%26c", apiClient.encode("a+b&c"));
        assertEquals("%ED%95%9C%EA%B5%AD", apiClient.encode("한국"));
    }
}
