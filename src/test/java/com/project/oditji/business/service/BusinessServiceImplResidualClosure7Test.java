package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessDashboardVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;

/** 사업자 대시보드와 JSONL 콘텐츠 검색의 잔여 ternary/short-circuit를 보완합니다. */
class BusinessServiceImplResidualClosure7Test {

    private BusinessDAO businessDAO;
    private SearchContentStore searchContentStore;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        searchContentStore = mock(SearchContentStore.class);
        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                searchContentStore,
                mock(TmdbService.class),
                mock(NotificationService.class),
                "uploads/product",
                "uploads/event");
    }

    @Test
    void dashboardShouldCoverZeroClickAndNullRecentCollectionFallbacks() {
        when(businessDAO.selectTodayOrderCountByBusinessNo(10L)).thenReturn(3);
        when(businessDAO.selectTodayClickCountByBusinessNo(10L)).thenReturn(0);
        when(businessDAO.selectRecentOrdersByBusinessNo(10L)).thenReturn(null);
        when(businessDAO.selectRecentReviewsByBusinessNo(10L)).thenReturn(null);
        when(businessDAO.selectPopularProductsByBusinessNo(10L, 5)).thenReturn(null);

        BusinessDashboardVO dashboard = service.getBusinessDashboard(10L);

        assertEquals(0.0, dashboard.getPurchaseRate());
        assertTrue(dashboard.getRecentOrders().isEmpty());
        assertTrue(dashboard.getRecentReviews().isEmpty());
        assertTrue(dashboard.getPopularProducts().isEmpty());
    }

    @Test
    void salesStatusShouldCoverBlankProductNameOperand() {
        LocalDate startDate = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate endDate = LocalDate.of(2026, Month.AUGUST, 18);
        SettlementManageVO salesStatus = new SettlementManageVO();
        salesStatus.setProductName("   ");

        when(businessDAO.selectBusinessSalesStatus(11L, startDate, endDate))
                .thenReturn(salesStatus);

        assertEquals(
                "판매 상품 없음",
                service.getBusinessSalesStatus(11L, startDate, endDate)
                        .getProductName());
    }

    @Test
    void cachedContentSearchShouldCoverBlankKeywordAndEveryMatchingField() {
        assertTrue(service.getCachedContentList("   ").isEmpty());

        CachedContentVO titleMatch = cached(
                1L,
                "MOVIE",
                "needle title",
                "other original",
                "other search");
        CachedContentVO originalMatch = cached(
                2L,
                "MOVIE",
                "other title",
                "needle original",
                "other search");
        CachedContentVO searchTextMatch = cached(
                3L,
                "TV",
                "other title",
                "other original",
                "needle search");
        CachedContentVO noMatch = cached(
                4L,
                "TV",
                "other title",
                "other original",
                "other search");

        when(searchContentStore.getAll())
                .thenReturn(List.of(
                        titleMatch,
                        originalMatch,
                        searchTextMatch,
                        noMatch));

        List<ContentSearchVO> result = service.getCachedContentList(" NEEDLE ");

        assertEquals(3, result.size());
        assertEquals(List.of(1L, 2L, 3L),
                result.stream().map(ContentSearchVO::getTmdbId).toList());
    }

    private CachedContentVO cached(
            Long tmdbId,
            String contentType,
            String title,
            String originalTitle,
            String searchText) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setOriginalTitle(originalTitle);
        content.setSearchText(searchText);
        return content;
    }
}
