package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/** 검색 캐시의 예정작 정책, 내부 전체검색, 변환 helper 잔여 분기를 보완합니다. */
class SearchContentPageCacheServiceFinalGapCoverageTest {

    private SearchContentStore searchContentStore;
    private TmdbDAO tmdbDAO;
    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        searchContentStore = mock(SearchContentStore.class);
        tmdbDAO = mock(TmdbDAO.class);
        service = new SearchContentPageCacheService(searchContentStore, tmdbDAO);
    }

    @Test
    void upcomingPolicyShouldKeepPastAndThreeDayWindowButRemoveLaterFutureContent() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);

        SearchResultVO past = result(1L, "과거", today.minusDays(1).toString());
        SearchResultVO tomorrow = result(2L, "내일", today.plusDays(1).toString());
        SearchResultVO thirdDay = result(3L, "3일", today.plusDays(3).toString());
        SearchResultVO fourthDay = result(4L, "4일", today.plusDays(4).toString());
        SearchResultVO invalid = result(5L, "날짜없음", "invalid");

        List<SearchResultVO> values = new ArrayList<SearchResultVO>();
        values.add(null);
        values.add(past);
        values.add(tomorrow);
        values.add(thirdDay);
        values.add(fourthDay);
        values.add(invalid);

        ReflectionTestUtils.invokeMethod(service, "applyUpcomingReleasePolicy", values);

        assertEquals(5, values.size());
        assertFalse(values.contains(fourthDay));
        assertFalse(past.isUpcoming());
        assertTrue(tomorrow.isUpcoming());
        assertTrue(thirdDay.isUpcoming());
        assertFalse(invalid.isUpcoming());
    }

    @Test
    void internalSearchShouldConvertMatchingContentAndSetPersonMatchInformation() {
        CachedContentVO content = cached(10L, "MOVIE", "검색 제목", "감독 이름", "배우 이름");
        content.setPlatformKeys(List.of("netflix"));
        content.setPopularity(50.0);
        content.setTmdbScore(8.0);

        when(searchContentStore.getAll()).thenReturn(List.of(content));
        when(tmdbDAO.selectActivePlatformList()).thenReturn(List.of());

        Object raw = ReflectionTestUtils.invokeMethod(
                service,
                "searchAll",
                "감독이름",
                List.of(),
                List.of(),
                List.of(),
                List.of());

        List<?> values = (List<?>) raw;
        assertEquals(1, values.size());

        SearchResultVO result = (SearchResultVO) values.get(0);
        assertEquals("PERSON", result.getMatchType());
        assertEquals("감독", result.getMatchedPersonRole());
        assertEquals("감독 이름", result.getMatchedPersonName());
        assertTrue(result.getPlatformList().isEmpty());
    }

    @Test
    void toSearchResultShouldCoverEmptyKeywordCastMatchAndGenreFallback() {
        CachedContentVO content = cached(20L, "TV", "제목", null, "배우A, 배우B");
        content.setOriginalTitle("Original");
        content.setPosterPath("/poster.jpg");
        content.setReleaseDate("2026-08-01");
        content.setLastAirDate("2026-08-10");
        content.setGenreText("드라마");
        content.setAgeRating("15세 이상 관람가");
        content.setTmdbScore(7.5);
        content.setPopularity(40.0);
        content.setEpisodeCount(12);
        content.setPlatformKeys(List.of());

        SearchResultVO emptyKeyword = ReflectionTestUtils.invokeMethod(
                service,
                "toSearchResultVO",
                content,
                new HashMap<String, OttPlatformVO>(),
                "");

        assertEquals("TITLE", emptyKeyword.getMatchType());
        assertEquals(Integer.valueOf(12), emptyKeyword.getEpisodeCount());

        SearchResultVO cast = ReflectionTestUtils.invokeMethod(
                service,
                "toSearchResultVO",
                content,
                new HashMap<String, OttPlatformVO>(),
                "배우a");

        assertEquals("PERSON", cast.getMatchType());
        assertEquals("배우", cast.getMatchedPersonRole());
        assertEquals("배우A, 배우B", cast.getMatchedPersonName());

        assertEquals("액션", ReflectionTestUtils.invokeMethod(service, "displayGenreName", "ACTION"));
        assertEquals("UNKNOWN", ReflectionTestUtils.invokeMethod(service, "displayGenreName", "UNKNOWN"));
    }

    @Test
    void distinctContentShouldIgnoreIncompleteValuesAndKeepFirstDuplicate() {
        Map<String, SearchResultVO> selected = new LinkedHashMap<String, SearchResultVO>();

        ReflectionTestUtils.invokeMethod(service, "putDistinctContent", selected, (Object) null);

        SearchResultVO noId = result(null, "id없음", null);
        ReflectionTestUtils.invokeMethod(service, "putDistinctContent", selected, noId);

        SearchResultVO noType = result(1L, "type없음", null);
        noType.setContentType(null);
        ReflectionTestUtils.invokeMethod(service, "putDistinctContent", selected, noType);

        SearchResultVO first = result(2L, "첫번째", null);
        SearchResultVO duplicate = result(2L, "두번째", null);
        ReflectionTestUtils.invokeMethod(service, "putDistinctContent", selected, first);
        ReflectionTestUtils.invokeMethod(service, "putDistinctContent", selected, duplicate);

        assertEquals(1, selected.size());
        assertSame(first, selected.values().iterator().next());
        assertEquals("2_movie", ReflectionTestUtils.invokeMethod(service, "buildTmdbKey", 2L, " MOVIE "));
    }

    private CachedContentVO cached(Long id, String type, String title, String director, String castNames) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setTitle(title);
        content.setDirector(director);
        content.setCastNames(castNames);
        return content;
    }

    private SearchResultVO result(Long id, String title, String releaseDate) {
        SearchResultVO result = new SearchResultVO();
        result.setTmdbId(id);
        result.setContentType("MOVIE");
        result.setTitle(title);
        result.setReleaseDate(releaseDate);
        return result;
    }
}
