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

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultPageVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * SearchContentPageCacheService의 기존 테스트에서 남기기 쉬운
 * 필터 정규화, 페이징, 관련 콘텐츠 추천 이유 분기를 추가 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class SearchContentPageCacheServiceRemainingCoverageTest {

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private TmdbDAO tmdbDAO;

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                searchContentStore,
                tmdbDAO);
    }

    @Test
    void filtersShouldCoverGenreAliasesAgeVariantsAndProviderNormalization() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);

        List<CachedContentVO> data = List.of(
                content(1L, "MOVIE", "액션 영화", "액션·모험", "감독1", "배우1",
                        "15 세 이상 관람가", today.minusDays(100).toString(),
                        List.of("netflix"), 8.0, 100.0),
                content(2L, "TV", "SF 드라마", "드라마, SF·판타지", "감독2", "배우2",
                        "12세 이상 관람가", today.minusDays(5).toString(),
                        List.of("tving"), 8.1, 90.0),
                content(3L, "TV", "토크 예능", "토크, 코미디", "감독3", "배우3",
                        "7세 이상 관람가", today.minusDays(6).toString(),
                        List.of("wavve"), 7.9, 80.0),
                content(4L, "TV", "리얼리티 예능", "리얼리티", "감독4", "배우4",
                        "19세 이상 관람가", today.minusDays(7).toString(),
                        List.of("disney"), 7.8, 70.0),
                content(5L, "MOVIE", "로맨스 영화", "연속극", "감독5", "배우5",
                        "전체 이용가", today.minusDays(8).toString(),
                        List.of("watcha"), 7.7, 60.0),
                content(6L, "MOVIE", "등급 미정", "범죄", "감독6", "배우6",
                        "NR", today.minusDays(9).toString(),
                        List.of("coupang"), 7.6, 50.0),
                content(7L, "MOVIE", "사용자 등급", "미스터리", "감독7", "배우7",
                        "PG-13", today.minusDays(10).toString(),
                        List.of("netflix"), 7.5, 40.0));

        stub(data, createPlatforms());

        List<String> noisyCategories = new ArrayList<String>();
        noisyCategories.add(" movie ");
        noisyCategories.add(null);
        noisyCategories.add("MOVIE");

        assertSingleTitle(service.getContentPage(
                "", 1, 10,
                noisyCategories,
                List.of("action"),
                List.of("8"),
                List.of("15세")),
                "액션 영화");

        assertSingleTitle(service.getContentPage(
                "", 1, 10,
                List.of("DRAMA"),
                List.of("SCI_FI"),
                List.of(" TVING "),
                List.of("12 세")),
                "SF 드라마");

        assertSingleTitle(service.getContentPage(
                "", 1, 10,
                List.of("VARIETY"),
                List.of("COMEDY"),
                List.of("356"),
                List.of("7세")),
                "토크 예능");

        assertSingleTitle(service.getContentPage(
                "", 1, 10,
                List.of("VARIETY"),
                List.of(),
                List.of("Disney Plus"),
                List.of("18세")),
                "리얼리티 예능");

        assertSingleTitle(service.getContentPage(
                "", 1, 10,
                List.of("MOVIE"),
                List.of("ROMANCE"),
                List.of("97"),
                List.of("전체")),
                "로맨스 영화");

        List<String> noisyAgeRatings = new ArrayList<String>();
        noisyAgeRatings.add("unrated");
        noisyAgeRatings.add("not rated");
        noisyAgeRatings.add("NR");
        noisyAgeRatings.add(null);
        noisyAgeRatings.add(" ");

        SearchResultPageVO unknownAgePage = service.getContentPage(
                "", 1, 10,
                List.of(),
                List.of(),
                List.of("283"),
                noisyAgeRatings);
        assertSingleTitle(unknownAgePage, "등급 미정");

        assertSingleTitle(service.getContentPage(
                "", 1, 10,
                List.of(),
                List.of(),
                List.of("넷플릭스"),
                List.of("PG-13")),
                "사용자 등급");
    }

    @Test
    void categoryFiltersShouldCoverAnimationDocumentaryDramaAndUnknownCategory() {
        List<CachedContentVO> data = List.of(
                content(11L, "MOVIE", "애니 영화", "애니메이션, 가족", "감독", "배우",
                        "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 50.0),
                content(12L, "TV", "다큐 TV", "다큐멘터리", "감독", "배우",
                        "전체 관람가", "2026-01-02", List.of("netflix"), 8.0, 49.0),
                content(13L, "TV", "정상 드라마", "드라마, 범죄", "감독", "배우",
                        "15세 이상 관람가", "2026-01-03", List.of("netflix"), 8.0, 48.0),
                content(14L, "MOVIE", "일반 영화", "드라마, 범죄", "감독", "배우",
                        "15세 이상 관람가", "2026-01-04", List.of("netflix"), 8.0, 47.0),
                content(15L, "TV", "드라마 아닌 TV", "범죄", "감독", "배우",
                        "15세 이상 관람가", "2026-01-05", List.of("netflix"), 8.0, 46.0));

        stub(data, createPlatforms());

        assertSingleTitle(pageForCategory("ANIMATION"), "애니 영화");
        assertSingleTitle(pageForCategory("DOCUMENTARY"), "다큐 TV");
        assertSingleTitle(pageForCategory("DRAMA"), "정상 드라마");
        assertSingleTitle(pageForCategory("MOVIE"), "일반 영화");
        assertEquals(0, pageForCategory("UNKNOWN").getTotalResults());
    }

    @Test
    void keywordSearchShouldUseStoredSearchTextFallbackAndUnicodeNormalization() {
        CachedContentVO precomputed = content(
                21L, "MOVIE", "표시 제목", "액션", null, null,
                "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 30.0);
        precomputed.setSearchText("저장검색문자열token");

        CachedContentVO fallbackBlank = content(
                22L, "MOVIE", "ＡＢＣ-영화", "코미디", null, null,
                "전체 관람가", "2026-01-02", List.of("netflix"), 8.0, 29.0);
        fallbackBlank.setOriginalTitle(null);
        fallbackBlank.setSearchText("   ");

        CachedContentVO director = content(
                23L, "MOVIE", "다른 제목", "범죄", "검색 감독", null,
                "전체 관람가", "2026-01-03", List.of("netflix"), 8.0, 28.0);

        CachedContentVO cast = content(
                24L, "MOVIE", "또 다른 제목", "범죄", null, "검색 배우",
                "전체 관람가", "2026-01-04", List.of("netflix"), 8.0, 27.0);

        stub(List.of(precomputed, fallbackBlank, director, cast), createPlatforms());

        assertSingleTitle(service.getContentPage(
                "저장 검색 문자열 TOKEN", 1, 10,
                List.of(), List.of(), List.of(), List.of()),
                "표시 제목");

        SearchResultPageVO normalizedTitle = service.getContentPage(
                "ABC 영화", 1, 10,
                List.of(), List.of(), List.of(), List.of());
        assertSingleTitle(normalizedTitle, "ＡＢＣ-영화");
        assertEquals("TITLE", normalizedTitle.getResultList().get(0).getMatchType());

        SearchResultPageVO directorPage = service.getContentPage(
                "검색감독", 1, 10,
                List.of(), List.of(), List.of(), List.of());
        assertEquals("PERSON", directorPage.getResultList().get(0).getMatchType());
        assertEquals("감독", directorPage.getResultList().get(0).getMatchedPersonRole());

        SearchResultPageVO castPage = service.getContentPage(
                "검색배우", 1, 10,
                List.of(), List.of(), List.of(), List.of());
        assertEquals("PERSON", castPage.getResultList().get(0).getMatchType());
        assertEquals("배우", castPage.getResultList().get(0).getMatchedPersonRole());

        assertEquals(0, service.getContentPage(
                "없는검색어", 1, 10,
                List.of(), List.of(), List.of(), List.of()).getTotalResults());
    }

    @Test
    void pagingAndSortDefaultsShouldCoverEmptySecondPageAndNullSortValues() {
        List<CachedContentVO> data = new ArrayList<CachedContentVO>();

        for (int index = 1; index <= 21; index++) {
            CachedContentVO item = content(
                    100L + index,
                    "MOVIE",
                    "제목 " + index,
                    "액션",
                    "감독",
                    "배우",
                    "15세 이상 관람가",
                    "2026-01-" + String.format("%02d", Math.min(index, 28)),
                    List.of("netflix"),
                    index == 2 ? null : Double.valueOf(index),
                    index == 3 ? null : Double.valueOf(index));
            data.add(item);
        }

        stub(data, createPlatforms());

        ContentListPageVO secondPage = service.getContentListPage(
                null,
                null,
                2,
                null,
                null,
                null,
                null);
        assertEquals(2, secondPage.getCurrentPage());
        assertEquals(2, secondPage.getTotalPages());
        assertEquals(21, secondPage.getTotalResults());
        assertEquals(1, secondPage.getContentList().size());

        ContentListPageVO invalidValues = service.getContentListPage(
                "invalid-type",
                "invalid-sort",
                0,
                List.of(),
                List.of(),
                List.of(),
                List.of());
        assertEquals(1, invalidValues.getCurrentPage());
        assertEquals(20, invalidValues.getContentList().size());

        ContentListPageVO latest = service.getContentListPage(
                "new",
                null,
                1,
                List.of(),
                List.of(),
                List.of(),
                List.of());
        assertTrue(latest.getTotalResults() >= 0);

        SearchResultPageVO emptyPage = service.getContentPage(
                "찾을수없음",
                99,
                0,
                List.of(),
                List.of(),
                List.of());
        assertEquals(0, emptyPage.getTotalPages());
        assertEquals(99, emptyPage.getPage());
        assertTrue(emptyPage.getResultList().isEmpty());
    }

    @Test
    void previewShouldCoverEmptyResultAndPreviewLargerThanResult() {
        assertTrue(service.getFirstPagePreview(
                "",
                0,
                10,
                List.of(),
                List.of(),
                List.of(),
                List.of()).isEmpty());

        List<CachedContentVO> data = List.of(
                content(201L, "MOVIE", "하나", "액션", "감독", "배우",
                        "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 2.0),
                content(202L, "MOVIE", "둘", "액션", "감독", "배우",
                        "전체 관람가", "2026-01-02", List.of("netflix"), 8.0, 1.0));
        stub(data, createPlatforms());

        assertTrue(service.getFirstPagePreview(
                "없는 검색어",
                3,
                1,
                List.of(),
                List.of(),
                List.of(),
                List.of()).isEmpty());

        assertEquals(2, service.getFirstPagePreview(
                "",
                10,
                1,
                List.of(),
                List.of(),
                List.of(),
                List.of()).size());
    }

    @Test
    void platformMetadataShouldIgnoreInvalidRowsAndUnknownKeys() {
        CachedContentVO content = content(
                301L, "MOVIE", "플랫폼 테스트", "액션", "감독", "배우",
                "전체 관람가", "2026-01-01",
                List.of("netflix", "unknown-platform"), 8.0, 10.0);

        List<OttPlatformVO> platforms = new ArrayList<OttPlatformVO>();
        platforms.add(null);
        platforms.add(platform(1, null));
        platforms.add(platform(2, "Unknown OTT"));
        platforms.add(platform(3, "Netflix"));

        stub(List.of(content), platforms);

        SearchResultPageVO page = service.getContentPage(
                "", 1, 10,
                List.of(), List.of(), List.of(), List.of());

        assertEquals(1, page.getTotalResults());
        assertEquals(1, page.getResultList().get(0).getPlatformList().size());
        assertEquals("Netflix",
                page.getResultList().get(0).getPlatformList().get(0).getPlatformName());
    }

    @Test
    void providerFilterShouldRejectContentWithoutPlatformsForDefaultAndSelectedProvider() {
        CachedContentVO withoutPlatform = content(
                401L, "MOVIE", "OTT 없음", "액션", "감독", "배우",
                "전체 관람가", "2026-01-01",
                List.of(), 8.0, 10.0);
        CachedContentVO netflix = content(
                402L, "MOVIE", "넷플릭스 있음", "액션", "감독", "배우",
                "전체 관람가", "2026-01-02",
                List.of("netflix"), 8.0, 9.0);

        stub(List.of(withoutPlatform, netflix), createPlatforms());

        SearchResultPageVO noProviderSelected = service.getContentPage(
                "", 1, 10,
                List.of(), List.of(), List.of(), List.of());
        assertSingleTitle(noProviderSelected, "넷플릭스 있음");

        SearchResultPageVO selectedProvider = service.getContentPage(
                "", 1, 10,
                List.of(), List.of(), List.of("netflix"), List.of());
        assertSingleTitle(selectedProvider, "넷플릭스 있음");

        SearchResultPageVO unsupportedProvider = service.getContentPage(
                "", 1, 10,
                List.of(), List.of(), List.of("지원하지않음"), List.of());
        assertSingleTitle(unsupportedProvider, "넷플릭스 있음");
    }

    @Test
    void todayContentShouldIgnoreBadDatesDeduplicateRecentAndFillFromAllContent() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);

        List<CachedContentVO> data = List.of(
                content(501L, "MOVIE", "최근작", "액션", "감독", "배우",
                        "전체 관람가", today.minusDays(1).toString(),
                        List.of("netflix"), 8.0, 100.0),
                content(502L, "MOVIE", "잘못된 날짜", "액션", "감독", "배우",
                        "전체 관람가", "not-a-date",
                        List.of("netflix"), 8.0, 90.0),
                content(503L, "MOVIE", "빈 날짜", "액션", "감독", "배우",
                        "전체 관람가", " ",
                        List.of("netflix"), 8.0, 80.0),
                content(504L, "MOVIE", "과거작", "액션", "감독", "배우",
                        "전체 관람가", today.minusDays(200).toString(),
                        List.of("netflix"), 8.0, 70.0),
                content(505L, "MOVIE", "미래작", "액션", "감독", "배우",
                        "전체 관람가", today.plusDays(1).toString(),
                        List.of("netflix"), 8.0, 60.0));

        stub(data, createPlatforms());

        List<SearchResultVO> todayContent = service.getMainTodayContent(4);

        assertEquals(4, todayContent.size());
        assertEquals("최근작", todayContent.get(0).getTitle());
        assertEquals(4, todayContent.stream()
                .map(item -> item.getContentType() + ":" + item.getTmdbId())
                .distinct()
                .count());
    }

    @Test
    void relatedReasonShouldPreferDirectorThenCastThenMainGenreThenGenreThenCategory() {
        ContentVO current = currentContent(
                601L, "MOVIE", "액션, 코미디", "공통감독", "공통배우");

        assertRelatedType(current,
                content(602L, "MOVIE", "감독 일치", "액션, 범죄", "공통감독", "다른배우",
                        "15세 이상 관람가", "2026-01-01", List.of("netflix"), 8.0, 50.0),
                "DIRECTOR");

        assertRelatedType(current,
                content(603L, "MOVIE", "배우 일치", "액션, 범죄", "다른감독", "공통배우",
                        "15세 이상 관람가", "2026-01-01", List.of("netflix"), 8.0, 50.0),
                "CAST");

        ContentVO mainGenreCurrent = currentContent(
                604L, "MOVIE", "드라마, 액션", "감독A", "배우A");
        assertRelatedType(mainGenreCurrent,
                content(605L, "MOVIE", "주 장르 일치", "액션, 범죄", "감독B", "배우B",
                        "15세 이상 관람가", "2026-01-01", List.of("netflix"), 8.0, 50.0),
                "MAIN_GENRE");

        ContentVO genreCurrent = currentContent(
                606L, "MOVIE", "액션, 코미디", "감독A", "배우A");
        assertRelatedType(genreCurrent,
                content(607L, "MOVIE", "일반 장르 일치", "코미디, 범죄", "감독B", "배우B",
                        "15세 이상 관람가", "2026-01-01", List.of("netflix"), 8.0, 50.0),
                "GENRE");

        assertRelatedType(genreCurrent,
                content(608L, "MOVIE", "분류만 일치", "범죄, 미스터리", "감독B", "배우B",
                        "15세 이상 관람가", "2026-01-01", List.of("netflix"), 8.0, 50.0),
                "CATEGORY");
    }

    @Test
    void relatedCategoryShouldCoverAnimationDocumentaryVarietyDramaAndEmptyGenres() {
        assertRelatedCategory(
                currentContent(701L, "MOVIE", "애니메이션, 액션", "감독A", "배우A"),
                content(702L, "TV", "애니 후보", "애니메이션, 가족", "감독B", "배우B",
                        "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 10.0));

        assertRelatedCategory(
                currentContent(703L, "MOVIE", "다큐멘터리", "감독A", "배우A"),
                content(704L, "TV", "다큐 후보", "다큐멘터리", "감독B", "배우B",
                        "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 10.0));

        assertRelatedCategory(
                currentContent(705L, "TV", "토크", "감독A", "배우A"),
                content(706L, "TV", "리얼리티 후보", "리얼리티", "감독B", "배우B",
                        "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 10.0));

        assertRelatedCategory(
                currentContent(707L, "TV", "드라마", "감독A", "배우A"),
                content(708L, "TV", "드라마 후보", "드라마, 범죄", "감독B", "배우B",
                        "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 10.0));

        ContentVO emptyCurrent = currentContent(709L, "MOVIE", null, null, null);
        assertRelatedCategory(emptyCurrent,
                content(710L, "MOVIE", "장르 없음", null, null, null,
                        "전체 관람가", "2026-01-01", List.of("netflix"), 8.0, 10.0));
    }

    @Test
    void relatedScoringShouldHandleDuplicateValuesExcludedMainGenreAndDifferentCandidates() {
        ContentVO current = currentContent(
                801L,
                "TV",
                "드라마, 드라마, 뉴스, 액션, 액션",
                "감독A, 감독A",
                "배우A, 배우A");

        List<CachedContentVO> candidates = List.of(
                content(802L, "TV", "강한 후보", "드라마, 액션, 범죄", "감독A", "배우A",
                        "15세 이상 관람가", "2026-01-01", List.of("netflix"), 7.0, 10.0),
                content(803L, "TV", "약한 후보", "드라마, 액션", "감독B", "배우B",
                        "15세 이상 관람가", "2026-01-02", List.of("netflix"), 9.0, 100.0),
                content(804L, "MOVIE", "다른 분류", "액션", "감독A", "배우A",
                        "15세 이상 관람가", "2026-01-03", List.of("netflix"), 10.0, 1000.0));

        stub(candidates, createPlatforms());

        List<SearchResultVO> related = service.getRelatedContentList(current, 10);

        assertEquals(2, related.size());
        assertEquals("강한 후보", related.get(0).getTitle());
        assertFalse(related.get(0).getRecommendationReason().isBlank());
    }

    private SearchResultPageVO pageForCategory(String category) {
        return service.getContentPage(
                "",
                1,
                10,
                List.of(category),
                List.of(),
                List.of(),
                List.of());
    }

    private void assertRelatedType(
            ContentVO current,
            CachedContentVO candidate,
            String expectedType) {

        stub(List.of(candidate), createPlatforms());

        List<SearchResultVO> related = service.getRelatedContentList(
                current,
                1);

        assertEquals(1, related.size());
        assertEquals(expectedType,
                related.get(0).getRecommendationReasonType());
        assertFalse(related.get(0).getRecommendationReason().isBlank());
    }

    private void assertRelatedCategory(
            ContentVO current,
            CachedContentVO candidate) {

        stub(List.of(candidate), createPlatforms());

        List<SearchResultVO> related = service.getRelatedContentList(
                current,
                1);

        assertEquals(1, related.size());
        assertFalse(related.get(0).getRecommendationReason().isBlank());
    }

    private void assertSingleTitle(
            SearchResultPageVO page,
            String expectedTitle) {

        assertEquals(1, page.getTotalResults());
        assertEquals(expectedTitle,
                page.getResultList().get(0).getTitle());
    }

    private void stub(
            List<CachedContentVO> data,
            List<OttPlatformVO> platforms) {

        when(searchContentStore.getAll()).thenReturn(data);
        when(tmdbDAO.selectActivePlatformList()).thenReturn(platforms);
    }

    private ContentVO currentContent(
            Long tmdbId,
            String contentType,
            String genreText,
            String director,
            String castNames) {

        ContentVO current = new ContentVO();
        current.setTmdbId(tmdbId);
        current.setContentType(contentType);
        current.setGenreText(genreText);
        current.setDirector(director);
        current.setCastNames(castNames);
        return current;
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
        content.setOriginalTitle(title == null ? null : title + " 원제");
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

    private OttPlatformVO platform(
            int platformNo,
            String name) {

        OttPlatformVO platform = new OttPlatformVO();
        platform.setPlatformNo(platformNo);
        platform.setPlatformName(name);
        return platform;
    }
}
