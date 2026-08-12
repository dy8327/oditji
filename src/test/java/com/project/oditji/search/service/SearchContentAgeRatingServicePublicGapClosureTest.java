package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 재검사의 빈 대상 조기 종료와 정상 비제한 결과의 잔여 조건을 보완합니다. */
class SearchContentAgeRatingServicePublicGapClosureTest {

    private TmdbApiClient apiClient;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        service = new SearchContentAgeRatingService(
                apiClient,
                mock(SearchContentPolicyService.class),
                new SearchContentAgeRatingResolver());

        ReflectionTestUtils.setField(service, "retryEnabled", true);
        ReflectionTestUtils.setField(service, "retryMaxAttempts", 2);
        ReflectionTestUtils.setField(service, "retryMaxPerRefresh", 100);
        ReflectionTestUtils.setField(service, "workerCount", 1);
        ReflectionTestUtils.setField(service, "manualOverrideEnabled", false);

        when(apiClient.getBaseUrl()).thenReturn("https://api.test/3");
    }

    @Test
    void publicRecheckMethodsShouldReturnWhenNonEmptyInputContainsNoEligibleTarget() {
        CachedContentVO rated = content(1L, "MOVIE", "15세 이상 관람가");

        service.recheckUnknownAgeRatings(List.of(rated));
        service.recheckRestrictedAgeRatings(List.of(rated));

        verify(apiClient, never()).get(anyString());
    }

    @Test
    void restrictedRecheckShouldKeepAdultValueAndUnknownValueWhenFreshLookupIsNotRestricted() {
        CachedContentVO adult = content(10L, "MOVIE", "청소년 관람불가");
        CachedContentVO unknown = content(11L, "MOVIE", "등급 정보 없음");

        when(apiClient.get(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.contains("/movie/10/")) {
                return movieRatings("KR", "15");
            }
            return new JSONObject().put("results", new JSONArray());
        });

        List<CachedContentVO> contents = new ArrayList<CachedContentVO>(
                List.of(adult, unknown));

        service.recheckRestrictedAgeRatings(contents);

        assertEquals("청소년 관람불가", adult.getAgeRating());
        assertEquals("등급 정보 없음", unknown.getAgeRating());
        assertEquals(Boolean.TRUE, adult.getAgeRatingRestrictionChecked());
        assertEquals(Boolean.TRUE, unknown.getAgeRatingRestrictionChecked());
    }

    private CachedContentVO content(Long tmdbId, String type, String ageRating) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(type);
        content.setAgeRating(ageRating);
        return content;
    }

    private JSONObject movieRatings(String countryCode, String certification) {
        JSONObject release = new JSONObject().put("certification", certification);
        JSONObject country = new JSONObject()
                .put("iso_3166_1", countryCode)
                .put("release_dates", new JSONArray().put(release));
        return new JSONObject().put("results", new JSONArray().put(country));
    }
}
