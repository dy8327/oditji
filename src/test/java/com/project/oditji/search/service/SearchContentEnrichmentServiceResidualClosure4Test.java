package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 콘텐츠 상세 보강의 TV 기본 분기와 provider 단락 조건을 추가 보완합니다. */
class SearchContentEnrichmentServiceResidualClosure4Test {

    private SearchContentEnrichmentService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentEnrichmentService(
                mock(TmdbApiClient.class),
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingResolver.class));
    }

    @Test
    void basicDetailWithNullContentTypeShouldUseTvFallbackAndCommonDetail() {
        CachedContentVO content = new CachedContentVO();
        JSONObject detail = new JSONObject()
                .put("name", "TV 제목")
                .put("original_name", "TV Original")
                .put("first_air_date", "2026-08-13")
                .put("last_air_date", "2026-08-13")
                .put("poster_path", "/poster.jpg")
                .put("popularity", 1.5);

        ReflectionTestUtils.invokeMethod(
                service,
                "fillBasicContentDetail",
                content,
                detail);

        assertEquals("TV 제목", content.getTitle());
        assertEquals("TV Original", content.getOriginalTitle());
        assertEquals("2026-08-13", content.getLastAirDate());
        assertEquals("/poster.jpg", content.getPosterPath());
    }

    @Test
    void providerAdderShouldCoverNullBlankAndPresentPlatformKeyOperands() {
        JSONArray providers = new JSONArray()
                .put(new JSONObject().put("provider_id", 1))
                .put(new JSONObject().put("provider_id", 2))
                .put(new JSONObject().put("provider_id", 3));

        Map<Integer, String> supported = Map.of(
                2, "   ",
                3, "netflix");
        Set<String> keys = new LinkedHashSet<String>();

        ReflectionTestUtils.invokeMethod(
                service,
                "addPlatformKeysByProviderId",
                providers,
                supported,
                keys);

        assertEquals(Set.of("netflix"), keys);
    }

    @Test
    void genreParserShouldCoverNonEmptyNameWithDuplicateSecondCondition() {
        JSONArray genres = new JSONArray()
                .put(new JSONObject().put("name", "드라마"))
                .put(new JSONObject().put("name", "드라마"));

        assertEquals(
                "드라마",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "parseGenreNames",
                        genres));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "parseGenreNames",
                (Object) null));
    }
}
