package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** HTTP 요청 생성, 성공 응답, 상태 오류, 빈 본문, JSON 오류와 인터럽트를 검증합니다. */
class TmdbApiClientExtendedCoverageTest {

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
        ReflectionTestUtils.setField(apiClient, "token", "token-value");
        ReflectionTestUtils.setField(apiClient, "requestDelayMillis", 0L);
        Thread.interrupted();
    }

    @Test
    void requestShouldContainUriTimeoutAndAuthorizationHeaders() {
        HttpRequest request = ReflectionTestUtils.invokeMethod(
                apiClient,
                "createRequest",
                "https://api.test/3/movie/1");

        assertEquals("https://api.test/3/movie/1", request.uri().toString());
        assertEquals("Bearer token-value", request.headers()
                .firstValue("Authorization")
                .orElseThrow());
        assertEquals("application/json", request.headers()
                .firstValue("accept")
                .orElseThrow());
        assertTrue(request.timeout().isPresent());
    }

    @Test
    void successfulResponseShouldReturnParsedJson() throws Exception {
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"id\":1,\"title\":\"영화\"}");
        when(httpClient.send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        JSONObject result = apiClient.get("https://api.test/3/movie/1");

        assertEquals(1L, result.getLong("id"));
        assertEquals("영화", result.getString("title"));
    }

    @Test
    void nonSuccessBlankAndMalformedResponsesShouldFailClearly() throws Exception {
        when(httpClient.send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        when(response.statusCode()).thenReturn(500);
        IllegalStateException httpError = assertThrows(
                IllegalStateException.class,
                () -> apiClient.get("https://api.test/500"));
        assertTrue(httpError.getMessage().contains("HTTP 오류: 500"));

        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(" ");
        IllegalStateException blankBody = assertThrows(
                IllegalStateException.class,
                () -> apiClient.get("https://api.test/blank"));
        assertTrue(blankBody.getMessage().contains("본문이 비어"));

        when(response.body()).thenReturn("{invalid");
        IllegalStateException malformed = assertThrows(
                IllegalStateException.class,
                () -> apiClient.get("https://api.test/malformed"));
        assertTrue(malformed.getMessage().contains("JSON 응답"));
    }

    @Test
    void interruptedRequestShouldPreserveInterruptFlag() throws Exception {
        when(httpClient.send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new InterruptedException("stop"));

        IllegalStateException interrupted = assertThrows(
                IllegalStateException.class,
                () -> apiClient.get("https://api.test/interrupted"));

        assertTrue(interrupted.getMessage().contains("중단"));
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void finalIoFailureShouldBeWrappedWithoutAdditionalRetry() {
        IOException failure = new IOException("network");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        apiClient,
                        "handleIOException",
                        failure,
                        4));

        assertTrue(exception.getMessage().contains("통신에 실패"));
        assertEquals(failure, exception.getCause());
    }

    @Test
    void sleepShouldReturnImmediatelyOrRestoreInterrupt() {
        ReflectionTestUtils.invokeMethod(apiClient, "sleepQuietly", 0L);

        Thread.currentThread().interrupt();
        ReflectionTestUtils.invokeMethod(apiClient, "sleepQuietly", 1L);
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void rateLimitEncodingAndRetryableIoBranchesShouldBeHandled() throws Exception {
        when(response.statusCode()).thenReturn(429);
        when(httpClient.send(
                any(HttpRequest.class),
                org.mockito.ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(response);

        HttpRequest request = ReflectionTestUtils.invokeMethod(
                apiClient,
                "createRequest",
                "https://api.test/3/movie/1");
        JSONObject rateLimited = ReflectionTestUtils.invokeMethod(
                apiClient,
                "sendRequest",
                request,
                0);

        assertNull(rateLimited);
        assertEquals("", apiClient.encode(null));
        assertTrue(apiClient.encode("한국 영화").contains("%"));

        Thread.currentThread().interrupt();
        ReflectionTestUtils.invokeMethod(
                apiClient,
                "handleIOException",
                new IOException("retry"),
                1);
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void malformedUriShouldBeRejectedBeforeSending() {
        assertThrows(
                IllegalArgumentException.class,
                () -> apiClient.get("http://bad host"));
    }
}
