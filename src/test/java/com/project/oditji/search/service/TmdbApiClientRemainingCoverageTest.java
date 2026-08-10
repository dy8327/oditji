package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * TMDB API 클라이언트의 null 응답, 2xx 하한, 전체 재시도 및 IO 재시도 성공 분기를 보완합니다.
 */
class TmdbApiClientRemainingCoverageTest {

    private TmdbApiClient apiClient;
    private HttpClient httpClient;
    private HttpResponse<String> response;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        apiClient = new TmdbApiClient();
        httpClient = mock(HttpClient.class);
        response = mock(HttpResponse.class);

        ReflectionTestUtils.setField(apiClient, "httpClient", httpClient);
        ReflectionTestUtils.setField(apiClient, "token", "token");
        ReflectionTestUtils.setField(apiClient, "requestDelayMillis", 0L);
        Thread.interrupted();
    }

    @AfterEach
    void clearInterruptFlag() {
        Thread.interrupted();
    }

    @Test
    void nullResponseBodyShouldFailClearly() throws Exception {
        stubResponse();
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> apiClient.get("https://api.test/null-body"));

        assertTrue(exception.getMessage().contains("본문이 비어"));
    }

    @Test
    void statusBelowTwoHundredShouldUseHttpErrorBranch() throws Exception {
        stubResponse();
        when(response.statusCode()).thenReturn(199);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> apiClient.get("https://api.test/199"));

        assertTrue(exception.getMessage().contains("HTTP 오류: 199"));
    }

    @Test
    void fourRateLimitResponsesShouldReachRetryLimit() throws Exception {
        stubResponse();
        when(response.statusCode()).thenReturn(429);

        Thread.currentThread().interrupt();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> apiClient.get("https://api.test/rate-limit"));

        assertEquals(
                "TMDB API 재시도 횟수를 초과했습니다.",
                exception.getMessage());
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void retryableIoFailuresShouldEventuallyReturnSuccessfulJson() throws Exception {
        when(httpClient.send(
                any(HttpRequest.class),
                anyBodyHandler()))
                .thenThrow(new IOException("first"))
                .thenThrow(new IOException("second"))
                .thenThrow(new IOException("third"))
                .thenReturn(response);

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"id\":77}");

        Thread.currentThread().interrupt();

        JSONObject result = apiClient.get("https://api.test/retry-success");

        assertEquals(77L, result.getLong("id"));
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void positiveSleepWithoutInterruptShouldCompleteNormally() {
        ReflectionTestUtils.invokeMethod(
                apiClient,
                "sleepQuietly",
                1L);

        assertFalse(Thread.currentThread().isInterrupted());
    }

    private void stubResponse() throws Exception {
        when(httpClient.send(
                any(HttpRequest.class),
                anyBodyHandler()))
                .thenReturn(response);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse.BodyHandler<String> anyBodyHandler() {
        return any(HttpResponse.BodyHandler.class);
    }
}
