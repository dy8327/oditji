package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * JSONL 공용 저장소 기반 검색·목록·추천·관련 콘텐츠 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class SearchContentPageCacheServiceTest {

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private TmdbDAO tmdbDAO;

    private SearchContentPageCacheService service;
    private List<CachedContentVO> contents;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                searchContentStore,
                tmdbDAO);

        contents = createContents();
        when(searchContentStore.getAll()).thenReturn(contents);
        when(tmdbDAO.selectActivePlatformList())
                .thenReturn(createPlatforms());
    }

    @Test
    void mainPopularShouldSortAndNormalizeLimit() {
        List<SearchResultVO> result = service.getMainPopularContent(2);

        assertEquals(2, result.size());
        assertEquals("인기 영화", result.get(0).getTitle());
        assertEquals("신규 드라마", result.get(1).getTitle());
        assertTrue(service.getMainPopularContent(0).isEmpty());
        assertEquals(7, service.getMainPopularContent(100).size());
    }

    @Test
    void mainTodayShouldPreferRecentReleasedContentAndRejectZeroLimit() {
        List<SearchResultVO> result = service.getMainTodayContent(3);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(
                content -> "오늘 공개".equals(content.getTitle())));
        assertTrue(result.stream().anyMatch(
                content -> "신규 드라마".equals(content.getTitle())));
        assertTrue(service.getMainTodayContent(-1).isEmpty());
    }

    @Test
    void mainRecommendedShouldFilterProviderAndSortByPopularityThenRating() {
        List<SearchResultVO> result = service.getMainRecommendedContent(
                List.of("넷플릭스"),
                10);

        assertEquals(3, result.size());
        assertEquals("인기 영화", result.get(0).getTitle());
        assertTrue(result.stream().allMatch(
                item -> item.getPlatformList().stream().anyMatch(
                        platform -> "Netflix".equals(
                                platform.getPlatformName()))));

        /* 지원하지 않는 OTT 값은 선택값 없음으로 처리되어 전체 지원 콘텐츠를 반환합니다. */
        assertEquals(7, service.getMainRecommendedContent(
                List.of("알 수 없는 OTT"),
                10).size());
    }

    @Test
    void contentPageShouldSearchTitleDirectorAndCast() {
        SearchResultPageVO titlePage = service.getContentPage(
                "인기",
                1,
                10,
                List.of(),
                List.of(),
                List.of(),
                List.of());
        assertEquals(1, titlePage.getTotalResults());
        assertEquals("TITLE", titlePage.getResultList().get(0).getMatchType());

        SearchResultPageVO directorPage = service.getContentPage(
                "홍길동",
                1,
                10,
                List.of(),
                List.of(),
                List.of(),
                List.of());
        /* 테스트 데이터에는 홍길동 감독 콘텐츠가 2개 있습니다. */
        assertEquals(2, directorPage.getTotalResults());
        assertTrue(directorPage.getResultList().stream().allMatch(
                result -> "PERSON".equals(result.getMatchType())));
        assertEquals("감독",
                directorPage.getResultList().get(0).getMatchedPersonRole());

        SearchResultPageVO castPage = service.getContentPage(
                "김배우",
                1,
                10,
                List.of(),
                List.of(),
                List.of(),
                List.of());
        assertEquals("배우",
                castPage.getResultList().get(0).getMatchedPersonRole());
    }

    @Test
    void contentPageShouldApplyCategoryGenreProviderAndAgeFilters() {
        SearchResultPageVO moviePage = service.getContentPage(
                "",
                1,
                10,
                List.of("MOVIE"),
                List.of("ACTION"),
                List.of("8"),
                List.of("15세"));

        assertEquals(1, moviePage.getTotalResults());
        assertEquals("인기 영화", moviePage.getResultList().get(0).getTitle());

        SearchResultPageVO animationPage = service.getContentPage(
                "",
                1,
                10,
                List.of("ANIMATION"),
                List.of("ANIMATION"),
                List.of("disney"),
                List.of("전체 관람가"));
        assertEquals(1, animationPage.getTotalResults());
        assertEquals("애니메이션", animationPage.getResultList().get(0).getTitle());

        SearchResultPageVO documentaryPage = service.getContentPage(
                "",
                1,
                10,
                List.of("DOCUMENTARY"),
                List.of("DOCUMENTARY"),
                List.of(),
                List.of("등급 정보 없음"));
        assertEquals(1, documentaryPage.getTotalResults());
    }

    @Test
    void contentListShouldSortAndClampPage() {
        ContentListPageVO ratingPage = service.getContentListPage(
                "all",
                "rating",
                99,
                List.of(),
                List.of(),
                List.of(),
                List.of());

        assertEquals(1, ratingPage.getCurrentPage());
        assertEquals(7, ratingPage.getTotalResults());
        assertEquals("평점 영화", ratingPage.getContentList().get(0).getTitle());

        ContentListPageVO titlePage = service.getContentListPage(
                "popular",
                "title",
                1,
                List.of(),
                List.of(),
                List.of(),
                List.of());
        assertEquals(7, titlePage.getContentList().size());
        assertEquals("다큐멘터리", titlePage.getContentList().get(0).getTitle());
    }

    @Test
    void newContentListShouldOnlyContainRecentAndNotFutureContent() {
        ContentListPageVO page = service.getContentListPage(
                "new",
                "latest",
                1,
                List.of(),
                List.of(),
                List.of(),
                List.of());

        assertEquals(2, page.getTotalResults());
        assertTrue(page.getContentList().stream().allMatch(
                content -> {
                    LocalDate releaseDate = LocalDate.parse(
                            content.getReleaseDate());
                    return !releaseDate.isBefore(
                            LocalDate.now().minusDays(30))
                            && !releaseDate.isAfter(LocalDate.now());
                }));
    }

    @Test
    void recommendedListAndPreviewShouldHandleLimitsAndEmptyInput() {
        List<SearchResultVO> recommended =
                service.getContentRecommendedList(
                        List.of("DRAMA"),
                        List.of("DRAMA"),
                        List.of("tving"),
                        List.of("12세 이상 관람가"),
                        5);
        assertEquals(1, recommended.size());
        assertEquals("신규 드라마", recommended.get(0).getTitle());

        assertEquals(2, service.getFirstPagePreview(
                "",
                2,
                10,
                List.of(),
                List.of(),
                List.of(),
                List.of()).size());
        assertTrue(service.getFirstPagePreview(
                "",
                0,
                10,
                List.of(),
                List.of(),
                List.of(),
                List.of()).isEmpty());
    }

    @Test
    void relatedContentShouldRemoveCurrentAndDifferentCategories() {
        ContentVO current = new ContentVO();
        current.setTmdbId(1L);
        current.setContentType("MOVIE");
        current.setGenreText("액션, 드라마");
        current.setDirector("홍길동");
        current.setCastNames("김배우, 이배우");

        List<SearchResultVO> related = service.getRelatedContentList(
                current,
                1);

        assertEquals(1, related.size());
        assertEquals("평점 영화", related.get(0).getTitle());
        assertFalse(related.get(0).getRecommendationReason().isBlank());
        assertFalse(related.get(0).getRecommendationReasonType().isBlank());

        assertTrue(service.getRelatedContentList(null, 10).isEmpty());
        assertTrue(service.getRelatedContentList(current, 0).isEmpty());
    }

    @Test
    void invalidCachedRowsAndMissingPlatformMetadataShouldBeIgnoredSafely() {
        List<CachedContentVO> mixed = new ArrayList<>(contents);
        mixed.add(null);
        mixed.add(content(null, null, "무효", "", "", "",
                null, null, List.of(), 1.0, 1.0));
        when(searchContentStore.getAll()).thenReturn(mixed);
        when(tmdbDAO.selectActivePlatformList()).thenReturn(null);

        SearchResultPageVO page = service.getContentPage(
                "",
                -3,
                1000,
                null,
                null,
                null,
                null);

        assertEquals(1, page.getPage());
        assertEquals(7, page.getTotalResults());
        assertTrue(page.getResultList().stream().allMatch(
                item -> item.getPlatformList().isEmpty()));
    }

    private List<CachedContentVO> createContents() {
        LocalDate today = LocalDate.now();

        return List.of(
                content(1L, "MOVIE", "인기 영화", "액션, 드라마",
                        "홍길동", "김배우, 이배우", "15세 이상 관람가",
                        today.minusDays(100).toString(),
                        List.of("netflix"), 8.0, 100.0),
                content(2L, "MOVIE", "평점 영화", "액션, 범죄",
                        "홍길동", "다른배우", "청소년 관람불가",
                        today.minusDays(200).toString(),
                        List.of("netflix"), 9.5, 50.0),
                content(3L, "TV", "신규 드라마", "드라마",
                        "박감독", "김배우", "12세 이상 관람가",
                        today.minusDays(3).toString(),
                        List.of("tving", "netflix"), 7.8, 80.0),
                content(4L, "TV", "애니메이션", "애니메이션, 가족",
                        "애니감독", "성우", "전체 관람가",
                        today.minusDays(60).toString(),
                        List.of("disney"), 8.7, 40.0),
                content(5L, "MOVIE", "다큐멘터리", "다큐멘터리",
                        "다큐감독", "", null,
                        today.minusDays(500).toString(),
                        List.of("watcha"), 7.0, 30.0),
                content(6L, "MOVIE", "오늘 공개", "코미디",
                        "오늘감독", "오늘배우", "7세 이상 관람가",
                        today.toString(),
                        List.of("coupang"), 6.5, 20.0),
                content(7L, "MOVIE", "미래 공개", "코미디",
                        "미래감독", "미래배우", "전체 관람가",
                        today.plusDays(3).toString(),
                        List.of("wavve"), 6.0, 10.0));
    }

    private CachedContentVO content(
            Long tmdbId,
            String contentType,
            String title,
            String genreText,
            String director,
            String castNames,
            String ageRating,
            String releaseDate,
            List<String> platformKeys,
            Double score,
            Double popularity) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setOriginalTitle(title + " 원제");
        content.setGenreText(genreText);
        content.setDirector(director);
        content.setCastNames(castNames);
        content.setAgeRating(ageRating);
        content.setReleaseDate(releaseDate);
        content.setLastAirDate(releaseDate);
        content.setPlatformKeys(platformKeys);
        content.setTmdbScore(score);
        content.setPopularity(popularity);
        content.setSearchText(null);
        return content;
    }

    private List<OttPlatformVO> createPlatforms() {
        return List.of(
                platform(1, "Netflix"),
                platform(2, "TVING"),
                platform(3, "wavve"),
                platform(4, "Disney Plus"),
                platform(5, "Watcha"),
                platform(6, "Coupangplay"));
    }

    private OttPlatformVO platform(int platformNo, String name) {
        OttPlatformVO platform = new OttPlatformVO();
        platform.setPlatformNo(platformNo);
        platform.setPlatformName(name);
        return platform;
    }
}
