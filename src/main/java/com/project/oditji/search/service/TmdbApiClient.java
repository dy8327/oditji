package com.project.oditji.search.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TMDB HTTP 통신을 전담하는 클라이언트입니다.
 *
 * URL 인코딩, 인증 헤더, 요청 제한 대기, 429 및 일시적인 통신 오류 재시도를
 * 한곳에서 관리하여 콘텐츠 수집 서비스가 HTTP 구현 세부사항을 알지 않도록 합니다.
 */
@Component
public class TmdbApiClient {

    @Value("${tmdb.api.base-url:https://api.themoviedb.org/3}")
    private String baseUrl;

    @Value("${tmdb.api.token}")
    private String token;

    @Value("${tmdb.api.language:ko-KR}")
    private String language;

    @Value("${tmdb.api.region:KR}")
    private String region;

    @Value("${search.content-cache.request-delay-ms:80}")
    private long requestDelayMillis;

    private final HttpClient httpClient;

    public TmdbApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getLanguage() {
        return language;
    }

    public String getRegion() {
        return region;
    }

    /**
     * 완성된 TMDB URL을 호출하고 JSON 객체로 반환합니다.
     *
     * @param apiUrl 호출할 전체 URL
     * @return TMDB JSON 응답
     */
    public JSONObject get(String apiUrl) {
        for (int retryCount = 1; retryCount <= 4; retryCount++) {
            try {
                JSONObject result = sendRequest(
                        createRequest(apiUrl),
                        retryCount
                );

                if (result != null) {
                    return result;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("TMDB API 호출이 중단되었습니다.", e);

            } catch (IOException e) {
                handleIOException(e, retryCount);

            } catch (JSONException e) {
                throw new IllegalStateException("TMDB JSON 응답을 해석하지 못했습니다.", e);
            }
        }

        throw new IllegalStateException("TMDB API 재시도 횟수를 초과했습니다.");
    }

    private HttpRequest createRequest(String apiUrl) {
        return HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + token)
                .header("accept", "application/json")
                .GET()
                .build();
    }

    private JSONObject sendRequest(
            HttpRequest request,
            int retryCount) throws IOException, InterruptedException {

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );

        int statusCode = response.statusCode();

        if (statusCode == 429) {
            sleepQuietly(1000L * retryCount);
            return null;
        }

        if (statusCode < 200 || statusCode >= 300) {
            throw new IllegalStateException("TMDB HTTP 오류: " + statusCode);
        }

        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new IllegalStateException("TMDB 응답 본문이 비어 있습니다.");
        }

        JSONObject result = new JSONObject(body);
        sleepQuietly(requestDelayMillis);
        return result;
    }

    private void handleIOException(
            IOException exception,
            int retryCount) {

        if (retryCount >= 4) {
            throw new IllegalStateException("TMDB API 통신에 실패했습니다.", exception);
        }

        sleepQuietly(500L * retryCount);
    }

    public String encode(String value) {
        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    private void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
