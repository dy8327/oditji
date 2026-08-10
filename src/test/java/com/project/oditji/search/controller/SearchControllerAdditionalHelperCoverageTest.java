package com.project.oditji.search.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.vo.SearchVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.wish.service.WishService;

/**
 * SearchController의 남은 입력값/제목 helper 분기를 직접 보완합니다.
 */
class SearchControllerAdditionalHelperCoverageTest {

    private SearchController controller;

    @BeforeEach
    void setUp() {
        controller = new SearchController(
                mock(SearchContentPageCacheService.class),
                mock(GoodsService.class),
                mock(TmdbDAO.class),
                mock(WishService.class));
    }

    @Test
    void numericHelpersShouldCoverNullNegativeZeroAndPositiveValues() {
        assertNull(invoke("normalizePrice", (Object) null));
        assertEquals(0, ((Integer) invoke("normalizePrice", -100)).intValue());
        assertEquals(500, ((Integer) invoke("normalizePrice", 500)).intValue());

        assertEquals(0, ((Integer) invoke("calculateTotalPages", 0, 10)).intValue());
        assertEquals(0, ((Integer) invoke("calculateTotalPages", 10, 0)).intValue());
        assertEquals(3, ((Integer) invoke("calculateTotalPages", 21, 10)).intValue());

        assertEquals(1, ((Integer) invoke("normalizePage", 0)).intValue());
        assertEquals(4, ((Integer) invoke("normalizePage", 4)).intValue());
    }

    @Test
    void keywordAndTabNormalizationShouldCoverEveryFallback() {
        assertEquals("", invoke("normalizeKeyword", (Object) null));
        assertEquals("hello", invoke("normalizeKeyword", "  hello  "));

        assertEquals("ALL", invoke("normalizeSearchTab", (Object) null));
        assertEquals("CONTENT", invoke("normalizeSearchTab", " content "));
        assertEquals("GOODS", invoke("normalizeSearchTab", " goods "));
        assertEquals("ALL", invoke("normalizeSearchTab", "unknown"));
        assertEquals("ALL", invoke("normalizeSearchTab", "   "));
    }

    @Test
    void listNormalizersShouldRemoveNullBlankDuplicateAndUnsupportedValues() {
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) invoke(
                "normalizeContentCategories",
                Arrays.asList(null, " ", " movie ", "MOVIE", "drama", "bad"));

        assertEquals(List.of("MOVIE", "DRAMA"), categories);

        @SuppressWarnings("unchecked")
        List<String> safe = (List<String>) invoke(
                "createSafeList",
                Arrays.asList(null, " ", " A ", "A", "B"));

        assertEquals(List.of("A", "B"), safe);
    }

    @Test
    void searchTitleShouldCoverNullAndEmptySearchObjects() {
        assertEquals(
                "지금 인기 있는 콘텐츠와 상품",
                invoke("makeSearchTitle", (Object) null));

        SearchVO blank = new SearchVO();
        blank.setKeyword("   ");
        blank.setSearchTab("unexpected");

        assertEquals(
                "지금 인기 있는 콘텐츠와 상품",
                invoke("makeSearchTitle", blank));

        SearchVO goodsFilter = new SearchVO();
        goodsFilter.setDiscountOnly(true);

        assertEquals(
                "선택 조건 검색 결과",
                invoke("makeSearchTitle", goodsFilter));

        assertTrue(((String) invoke("makeSearchTitle", goodsFilter)).contains("조건"));
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                controller,
                methodName,
                arguments);
    }
}
