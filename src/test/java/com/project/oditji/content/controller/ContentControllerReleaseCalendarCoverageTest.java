package com.project.oditji.content.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.holiday.service.HolidayService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.verify.service.VerifyService;

/** 출시 알림 캘린더 Controller의 기본값, 월 검증, 날짜 그룹화 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ContentControllerReleaseCalendarCoverageTest {

    @Mock
    private ContentService contentService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private FavoriteService favoriteService;
    @Mock
    private TmdbDAO tmdbDAO;
    @Mock
    private VerifyService verifyService;
    @Mock
    private GoodsService goodsService;
    @Mock
    private HolidayService holidayService;

    private ContentController controller;

    @BeforeEach
    void setUp() {
        controller = new ContentController(
                contentService,
                reviewService,
                favoriteService,
                tmdbDAO,
                verifyService,
                goodsService,
                holidayService);
    }

    @Test
    void releaseCalendarShouldGroupContentsAndExposeMonthNavigation() {
        int january = Month.JANUARY.getValue();
        SearchResultVO first = result("첫 작품", "2026-01-03");
        SearchResultVO sameDay = result("같은 날 작품", "2026-01-03");
        SearchResultVO later = result("다른 날 작품", "2026-01-20");
        when(contentService.getReleaseCalendarContent(2026, january))
                .thenReturn(List.of(first, sameDay, later));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.releaseCalendar(2026, january, model);

        assertEquals("content/releaseCalendar", view);
        assertEquals(2026, model.get("targetYear"));
        assertEquals(january, model.get("targetMonth"));
        assertEquals(31, model.get("daysInMonth"));
        assertEquals(2025, model.get("prevYear"));
        assertEquals(Month.DECEMBER.getValue(), model.get("prevMonth"));
        assertEquals(2026, model.get("nextYear"));
        assertEquals(Month.FEBRUARY.getValue(), model.get("nextMonth"));
        assertFalse((Boolean) model.get("isCurrentMonth"));

        @SuppressWarnings("unchecked")
        Map<Integer, List<SearchResultVO>> releaseByDay =
                (Map<Integer, List<SearchResultVO>>) model.get("releaseByDay");
        assertEquals(2, releaseByDay.get(3).size());
        assertEquals("다른 날 작품", releaseByDay.get(20).get(0).getTitle());
    }

    @Test
    void releaseCalendarShouldUseCurrentYearAndMonthWhenParametersAreNull() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);
        when(contentService.getReleaseCalendarContent(
                today.getYear(), today.getMonthValue())).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        controller.releaseCalendar(null, null, model);

        assertEquals(today.getYear(), model.get("targetYear"));
        assertEquals(today.getMonthValue(), model.get("targetMonth"));
        assertTrue((Boolean) model.get("isCurrentMonth"));
    }

    @Test
    void releaseCalendarShouldReplaceLowAndHighInvalidMonthsWithCurrentMonth() {
        LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);
        when(contentService.getReleaseCalendarContent(anyInt(), anyInt()))
                .thenReturn(List.of());

        ExtendedModelMap lowMonthModel = new ExtendedModelMap();
        controller.releaseCalendar(2024, 0, lowMonthModel);
        assertEquals(today.getMonthValue(), lowMonthModel.get("targetMonth"));

        ExtendedModelMap highMonthModel = new ExtendedModelMap();
        controller.releaseCalendar(2024, 13, highMonthModel);
        assertEquals(today.getMonthValue(), highMonthModel.get("targetMonth"));
        assertFalse((Boolean) highMonthModel.get("isCurrentMonth"));
    }

    @Test
    void releaseCalendarShouldExposeFirstDayOfWeekUsingSundayZeroConvention() {
        int march = Month.MARCH.getValue();
        when(contentService.getReleaseCalendarContent(2026, march)).thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        controller.releaseCalendar(2026, march, model);

        YearMonth target = YearMonth.of(2026, march);
        int expected = target.atDay(1).getDayOfWeek().getValue() % 7;
        assertEquals(expected, model.get("firstDayOfWeek"));
    }

    private SearchResultVO result(String title, String releaseDate) {
        SearchResultVO result = new SearchResultVO();
        result.setTitle(title);
        result.setReleaseDate(releaseDate);
        return result;
    }
}
