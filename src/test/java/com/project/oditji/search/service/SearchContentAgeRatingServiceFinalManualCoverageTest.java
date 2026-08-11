package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 연령등급 서비스의 수동 보강 JSON 및 재조회 helper 잔여 조건을 보완합니다. */
class SearchContentAgeRatingServiceFinalManualCoverageTest {

    private SearchContentAgeRatingResolver resolver;
    private SearchContentPolicyService policyService;
    private SearchContentAgeRatingService service;

    @BeforeEach
    void setUp() {
        resolver = mock(SearchContentAgeRatingResolver.class);
        policyService = mock(SearchContentPolicyService.class);
        service = new SearchContentAgeRatingService(
                mock(TmdbApiClient.class),
                policyService,
                resolver);
    }

    @Test
    void simpleHelpersShouldCoverNullBlankNegativeRetryAndKeyNormalization() {
        assertFalse(booleanValue(invoke("hasText", (Object) null)));
        assertFalse(booleanValue(invoke("hasText", "  ")));
        assertTrue(booleanValue(invoke("hasText", " x ")));
        assertEquals("MOVIE:10", invoke("createKey", " movie ", 10L));

        assertEquals(0, ((Integer) invoke("getRetryCount", (Object) null)).intValue());
        CachedContentVO noRetry = new CachedContentVO();
        assertEquals(0, ((Integer) invoke("getRetryCount", noRetry)).intValue());
        noRetry.setAgeRatingRetryCount(-3);
        assertEquals(0, ((Integer) invoke("getRetryCount", noRetry)).intValue());
        noRetry.setAgeRatingRetryCount(2);
        assertEquals(2, ((Integer) invoke("getRetryCount", noRetry)).intValue());
    }

    @Test
    void manualAgeRatingNormalizationShouldAcceptCanonicalMapResolverValueAndRejectUnknown() {
        assertNull(invoke("normalizeManualAgeRating", (Object) null));
        assertNull(invoke("normalizeManualAgeRating", " "));
        assertEquals("15세 이상 관람가", invoke("normalizeManualAgeRating", " 15세 이상 관람가 "));

        when(resolver.normalizeKoreanAgeRating("15")).thenReturn("15세 이상 관람가");
        when(resolver.normalizeKoreanAgeRating("???")).thenReturn("등급 정보 없음");
        assertEquals("15세 이상 관람가", invoke("normalizeManualAgeRating", "15"));
        assertNull(invoke("normalizeManualAgeRating", "???"));
    }

    @Test
    void flatManualOverrideParserShouldIgnoreUnsupportedMalformedNonPositiveAndUnknownValues() {
        when(resolver.normalizeKoreanAgeRating("15")).thenReturn("15세 이상 관람가");
        when(resolver.normalizeKoreanAgeRating("bad")).thenReturn("등급 정보 없음");

        JSONObject root = new JSONObject()
                .put("OTHER-1", "15")
                .put("MOVIE-x", "15")
                .put("MOVIE-0", "15")
                .put("MOVIE-10", "15")
                .put("TV-20", "bad");
        Map<String, String> result = new LinkedHashMap<String, String>();
        invoke("readFlatManualOverrides", root, result);

        assertEquals(1, result.size());
        assertEquals("15세 이상 관람가", result.get("MOVIE:10"));
    }

    @Test
    void sectionParserShouldIgnoreMissingSectionBadIdsAndKeepValidRows() {
        when(resolver.normalizeKoreanAgeRating("12")).thenReturn("12세 이상 관람가");
        JSONObject root = new JSONObject()
                .put("MOVIE", new JSONObject()
                        .put("bad", "12")
                        .put("0", "12")
                        .put("30", "12"));
        Map<String, String> result = new LinkedHashMap<String, String>();

        invoke("readManualSection", root, "TV", result);
        assertTrue(result.isEmpty());
        invoke("readManualSection", root, "MOVIE", result);
        assertEquals("12세 이상 관람가", result.get("MOVIE:30"));
    }

    @Test
    void retryAndRestrictionTargetsShouldCoverManualOverrideCheckedAndAgeRatingBranches() {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(10L);
        content.setContentType("MOVIE");
        content.setAgeRating("등급 정보 없음");
        content.setAgeRatingRetryCount(0);
        when(policyService.shouldExcludeContent(content)).thenReturn(false);

        assertTrue(booleanValue(invoke("isRetryTarget", content, 2)));
        assertTrue(booleanValue(invoke("isRestrictionRecheckTarget", content)));

        AtomicReference<Map<String, String>> manualOverrides = manualOverrideMap();
        manualOverrides.set(Map.of("MOVIE:10", "15세 이상 관람가"));
        assertFalse(booleanValue(invoke("isRetryTarget", content, 2)));

        content.setAgeRatingRestrictionChecked(Boolean.TRUE);
        assertFalse(booleanValue(invoke("isRestrictionRecheckTarget", content)));
    }


    @SuppressWarnings("unchecked")
    private AtomicReference<Map<String, String>> manualOverrideMap() {
        return (AtomicReference<Map<String, String>>) ReflectionTestUtils.getField(
                service,
                "manualOverrideMap");
    }


    private boolean booleanValue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
