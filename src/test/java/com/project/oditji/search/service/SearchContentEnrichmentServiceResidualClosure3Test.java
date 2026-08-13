package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 콘텐츠 보강 서비스의 공통 상세/텍스트/배열 helper 잔여 조건을 보완합니다. */
class SearchContentEnrichmentServiceResidualClosure3Test {

    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                mock(SearchContentManualOverrideService.class),
                new SearchContentAgeRatingResolver());
    }

    @Test
    void commonDetailShouldFillMissingValuesAndKeepExistingValues() {
        CachedContentVO missing = new CachedContentVO();
        JSONObject detail = new JSONObject()
                .put("poster_path", "/poster.jpg")
                .put("popularity", 123.4);

        invokeVoid("fillCommonBasicDetail", missing, detail);
        assertEquals("/poster.jpg", missing.getPosterPath());
        assertEquals(Double.valueOf(123.4), missing.getPopularity());

        CachedContentVO existing = new CachedContentVO();
        existing.setPosterPath("/existing.jpg");
        existing.setPopularity(10.0);
        invokeVoid("fillCommonBasicDetail", existing, detail);
        assertEquals("/existing.jpg", existing.getPosterPath());
        assertEquals(Double.valueOf(10.0), existing.getPopularity());
    }

    @Test
    void scalarAndTextHelpersShouldCoverNullBlankAndPresentInputs() {
        assertFalse(Boolean.TRUE.equals(invoke("hasText", (Object) null)));
        assertFalse(Boolean.TRUE.equals(invoke("hasText", "   ")));
        assertTrue(Boolean.TRUE.equals(invoke("hasText", "value")));

        assertEquals("", invoke("safeText", (Object) null));
        assertEquals("value", invoke("safeText", "value"));
        assertEquals("fallback", invoke("firstNonBlank", " ", "fallback"));
        assertNull(invoke("firstNonBlank", " ", null));

        JSONObject values = new JSONObject()
                .put("positive", 5)
                .put("zero", 0)
                .put("text", " value ");
        assertEquals(Integer.valueOf(5), invoke("nullablePositiveInteger", values, "positive"));
        assertNull(invoke("nullablePositiveInteger", values, "zero"));
        assertEquals(" value ", invoke("nullableString", values, "text"));
    }

    @Test
    void arrayParsersShouldCoverNullInvalidDuplicateAndValidValues() {
        assertNull(invoke("parseGenreNames", (Object) null));

        JSONArray genres = new JSONArray()
                .put(JSONObject.NULL)
                .put(new JSONObject().put("name", " "))
                .put(new JSONObject().put("name", "드라마"))
                .put(new JSONObject().put("name", "드라마"))
                .put(new JSONObject().put("name", "코미디"));
        assertEquals("드라마, 코미디", invoke("parseGenreNames", genres));

        JSONArray runtimes = new JSONArray()
                .put(JSONObject.NULL)
                .put(0)
                .put(-1)
                .put(45)
                .put(60);
        assertEquals(Integer.valueOf(45), invoke("parseFirstPositiveInteger", runtimes));
        assertNull(invoke("parseFirstPositiveInteger", (Object) null));
    }

    private void invokeVoid(String method, Object... args) {
        ReflectionTestUtils.invokeMethod(service, method, args);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
