package com.project.oditji.search.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.project.oditji.common.config.CacheConfig;

@Service
public class TmdbProviderCacheService {

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    @Value("${tmdb.api.token}")
    private String token;

    private final HttpClient httpClient;

    public TmdbProviderCacheService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Cacheable(
            cacheNames = CacheConfig.TMDB_PROVIDER_CACHE,
            key = "#contentType.toUpperCase() + '_' + #tmdbId",
            sync = true
    )
    public JSONObject getWatchProviderResult(
            Long tmdbId,
            String contentType) {

        String mediaType =
                "MOVIE".equalsIgnoreCase(contentType)
                        ? "movie"
                        : "tv";

        String apiUrl =
                baseUrl
                        + "/"
                        + mediaType
                        + "/"
                        + tmdbId
                        + "/watch/providers";

        return callTmdbApi(apiUrl);
    }

    private JSONObject callTmdbApi(String apiUrl) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(20))
                .header(
                        "Authorization",
                        "Bearer " + token
                )
                .header(
                        "accept",
                        "application/json"
                )
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8
                            )
                    );

            if (response.statusCode() < 200
                    || response.statusCode() >= 300) {

                System.err.println(
                        "TMDB 제공처 API 호출 실패"
                                + " / status="
                                + response.statusCode()
                                + " / url="
                                + apiUrl
                );

                return new JSONObject();
            }

            String responseBody =
                    response.body();

            if (responseBody == null
                    || responseBody.isBlank()) {

                return new JSONObject();
            }

            return new JSONObject(responseBody);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            System.err.println(
                    "TMDB 제공처 API 호출 중 인터럽트 발생: "
                            + e.getMessage()
            );

            return new JSONObject();

        } catch (IOException
                | IllegalArgumentException e) {

            System.err.println(
                    "TMDB 제공처 API 호출 중 오류 발생: "
                            + e.getMessage()
            );

            return new JSONObject();
        }
    }
}