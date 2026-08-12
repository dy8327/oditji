package com.project.oditji.content.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.holiday.service.HolidayService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.verify.service.VerifyService;

/**
 * 콘텐츠 컨트롤러의 목록 정규화 및 성인등급 OR 조건을 보완합니다.
 */
class ContentControllerRemainingCoverageTest {

    private ContentController controller;

    @BeforeEach
    void setUp() {
        controller = new ContentController(
                mock(ContentService.class),
                mock(ReviewService.class),
                mock(FavoriteService.class),
                mock(TmdbDAO.class),
                mock(VerifyService.class),
                mock(GoodsService.class),
                mock(HolidayService.class));
    }

    @Test
    void listTypeNormalizationShouldCoverNullKnownAndUnknownValues() {
        assertEquals(
                "all",
                normalizeType(null));
        assertEquals(
                "popular",
                normalizeType(" Popular "));
        assertEquals(
                "new",
                normalizeType(" NEW "));
        assertEquals(
                "all",
                normalizeType("unknown"));
    }

    @Test
    void listSortNormalizationShouldCoverDefaultsAllAllowedValuesAndFallback() {
        assertEquals(
                "popular",
                normalizeSort(null, "all"));
        assertEquals(
                "latest",
                normalizeSort(" ", "new"));

        assertEquals(
                "popular",
                normalizeSort("POPULAR", "all"));
        assertEquals(
                "rating",
                normalizeSort(" Rating ", "all"));
        assertEquals(
                "latest",
                normalizeSort("LATEST", "all"));
        assertEquals(
                "title",
                normalizeSort("TITLE", "all"));

        assertEquals(
                "latest",
                normalizeSort("invalid", "new"));
        assertEquals(
                "popular",
                normalizeSort("invalid", "popular"));
    }

    @Test
    void safeListAndPageTitleShouldCoverBothSides() {
        List<String> supplied = List.of("A");

        List<String> nullSafe =
                safeList(null);
        List<String> same =
                safeList(supplied);

        assertTrue(nullSafe.isEmpty());
        assertSame(supplied, same);

        assertEquals(
                "인기 콘텐츠",
                pageTitle("popular"));
        assertEquals(
                "신규 콘텐츠",
                pageTitle("new"));
        assertEquals(
                "전체 콘텐츠",
                pageTitle("all"));
    }

    @Test
    void adultRestrictionShouldCoverNullBlankEveryBlockedKeywordAndNormalRating() {
        assertTrue(adultRestricted(null));
        assertTrue(adultRestricted("   "));
        assertTrue(adultRestricted("청소년 관람불가"));
        assertTrue(adultRestricted("19세 이상 관람가"));
        assertTrue(adultRestricted("등급 정보 없음"));
        assertTrue(adultRestricted("Not Rated"));
        assertTrue(adultRestricted("UNRATED"));
        assertTrue(adultRestricted("NR"));

        assertFalse(adultRestricted("15세 이상 관람가"));
        assertFalse(adultRestricted("12세 이상 관람가"));
    }

    private String normalizeType(
            String type) {

        return ReflectionTestUtils.invokeMethod(
                controller,
                "normalizeListType",
                type);
    }

    private String normalizeSort(
            String sort,
            String type) {

        return ReflectionTestUtils.invokeMethod(
                controller,
                "normalizeListSort",
                sort,
                type);
    }

    @SuppressWarnings("unchecked")
    private List<String> safeList(
            List<String> values) {

        return (List<String>)
                ReflectionTestUtils.invokeMethod(
                        controller,
                        "safeList",
                        values);
    }

    private String pageTitle(
            String type) {

        return ReflectionTestUtils.invokeMethod(
                controller,
                "makePageTitle",
                type);
    }

    private boolean adultRestricted(
            String ageRating) {

        Boolean result = ReflectionTestUtils.invokeMethod(
                controller,
                "isAdultRestrictedContent",
                ageRating);

        return Boolean.TRUE.equals(result);
    }
}
