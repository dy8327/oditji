package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Discover 서비스의 URL/연도/provider helper 잔여 조건을 보완합니다. */
class SearchContentDiscoverServiceFinalUrlCoverageTest {

    private TmdbApiClient apiClient;
    private SearchContentDiscoverService service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TmdbApiClient.class);
        service = new SearchContentDiscoverService(apiClient, mock(SearchContentPolicyService.class));
        when(apiClient.getBaseUrl()).thenReturn("https://api.example.test/3");
        when(apiClient.getLanguage()).thenReturn("ko-KR");
        when(apiClient.getRegion()).thenReturn("KR");
        when(apiClient.encode(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void providerJoinShouldSkipNullZeroNegativeAndKeepPositiveIds() {
        Set<Integer> ids = new LinkedHashSet<Integer>();
        ids.add(null);
        ids.add(0);
        ids.add(-1);
        ids.add(8);
        ids.add(337);
        assertEquals("8|337", invoke("joinProviderIds", ids));
    }

    @Test
    void discoverUrlsShouldUseMovieAndTvSpecificYearFilters() {
        String movie = invoke("buildDiscoverUrl", "movie", 2026, 2, "8|337");
        assertTrue(movie.contains("primary_release_year=2026"));
        assertTrue(movie.contains("region=KR"));
        assertTrue(movie.contains("include_video=false"));
        assertFalse(movie.contains("first_air_date_year="));

        String tv = invoke("buildDiscoverUrl", "tv", 2025, 3, "8");
        assertTrue(tv.contains("first_air_date_year=2025"));
        assertFalse(tv.contains("primary_release_year="));

        String supplement = invoke("buildSupplementDiscoverUrl", "tv", 2024, 1);
        assertTrue(supplement.contains("first_air_date_year=2024"));
        assertFalse(supplement.contains("with_watch_providers="));
    }

    @Test
    void firstNonBlankShouldCoverNullBlankAndPreferredFirstValue() {
        assertEquals("second", invoke("firstNonBlank", null, "second"));
        assertEquals("second", invoke("firstNonBlank", " ", "second"));
        assertEquals("first", invoke("firstNonBlank", "first", "second"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
