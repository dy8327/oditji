package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessDashboardVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;
import com.project.oditji.tmdb.vo.ActorVO;

/** 사업자 대시보드·주문·정산·콘텐츠 조회 핵심 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessServiceCoreCoverageTest {

    @TempDir
    Path tempDirectory;

    @Mock
    private BusinessDAO businessDAO;
    @Mock
    private ContentService contentService;
    @Mock
    private SearchContentStore searchContentStore;
    @Mock
    private TmdbService tmdbService;
    @Mock
    private NotificationService notificationService;

    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BusinessServiceImpl(
                businessDAO,
                contentService,
                searchContentStore,
                tmdbService,
                notificationService,
                tempDirectory.resolve("product").toString());
    }

    @Test
    void businessLookupAndDashboardShouldValidateAndCalculateRate() {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(10L);
        when(businessDAO.selectBusinessByMemberNo(1L)).thenReturn(business);
        assertSame(business, service.getBusinessByMemberNo(1L));
        assertThrows(IllegalArgumentException.class,
                () -> service.getBusinessByMemberNo(0L));

        when(businessDAO.selectTodaySalesByBusinessNo(10L)).thenReturn(50000L);
        when(businessDAO.selectTodayOrderCountByBusinessNo(10L)).thenReturn(5);
        when(businessDAO.selectTodayCustomerCountByBusinessNo(10L)).thenReturn(3);
        when(businessDAO.selectTodayClickCountByBusinessNo(10L)).thenReturn(20);
        when(businessDAO.selectWaitingSettlementAmountByBusinessNo(10L))
                .thenReturn(12000L);
        when(businessDAO.selectWaitingProductCountByBusinessNo(10L)).thenReturn(2);
        when(businessDAO.selectPopularProductsByBusinessNo(10L, 5))
                .thenReturn(List.of(new GoodsManageVO()));

        BusinessDashboardVO dashboard = service.getBusinessDashboard(10L);
        assertEquals(50000L, dashboard.getTodaySales());
        assertEquals(5, dashboard.getTodayOrderCount());
        assertEquals(3, dashboard.getTodayCustomerCount());
        assertEquals(20, dashboard.getClickCount());
        assertEquals(25.0, dashboard.getPurchaseRate());
        assertEquals(1, dashboard.getPopularProducts().size());
    }

    @Test
    void dashboardShouldUseZeroRateAndEmptyPopularList() {
        when(businessDAO.selectTodayClickCountByBusinessNo(10L)).thenReturn(0);
        when(businessDAO.selectPopularProductsByBusinessNo(10L, 5))
                .thenReturn(null);

        BusinessDashboardVO dashboard = service.getBusinessDashboard(10L);
        assertEquals(0.0, dashboard.getPurchaseRate());
        assertTrue(dashboard.getPopularProducts().isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> service.getBusinessDashboard(-1L));
    }

    @Test
    void businessOrderListShouldAttachItemsByOrderNumber() {
        OrderVO firstOrder = new OrderVO();
        firstOrder.setOrderNo(100L);
        OrderVO secondOrder = new OrderVO();
        secondOrder.setOrderNo(200L);

        OrderItemVO firstItem = new OrderItemVO();
        firstItem.setOrderNo(100L);
        OrderItemVO secondItem = new OrderItemVO();
        secondItem.setOrderNo(100L);

        when(businessDAO.selectBusinessOrderList(10L))
                .thenReturn(List.of(firstOrder, secondOrder));
        when(businessDAO.selectBusinessOrderItemList(10L))
                .thenReturn(List.of(firstItem, secondItem));

        List<OrderVO> result = service.getBusinessOrderList(10L);
        assertEquals(2, result.size());
        assertEquals(2, firstOrder.getItems().size());
        assertTrue(secondOrder.getItems().isEmpty());

        when(businessDAO.selectBusinessOrderList(11L)).thenReturn(null);
        assertTrue(service.getBusinessOrderList(11L).isEmpty());
        when(businessDAO.selectBusinessOrderItemList(12L)).thenReturn(null);
        assertTrue(service.getBusinessOrderItemList(12L).isEmpty());
    }

    @Test
    void salesStatusAndHistoryShouldValidateDatesAndProvideDefaults() {
        LocalDate start = LocalDate.of(2026, 8, 1);
        LocalDate end = LocalDate.of(2026, 8, 4);

        when(businessDAO.selectBusinessSalesStatus(10L, start, end))
                .thenReturn(null);
        SettlementManageVO empty = service.getBusinessSalesStatus(10L, start, end);
        assertEquals("판매 상품 없음", empty.getProductName());

        SettlementManageVO blankName = new SettlementManageVO();
        blankName.setProductName(" ");
        when(businessDAO.selectBusinessSalesStatus(11L, start, end))
                .thenReturn(blankName);
        assertEquals("판매 상품 없음",
                service.getBusinessSalesStatus(11L, start, end).getProductName());

        when(businessDAO.selectBusinessSalesHistory(10L, start, end))
                .thenReturn(null);
        assertTrue(service.getBusinessSalesHistory(10L, start, end).isEmpty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getBusinessSalesStatus(0L, start, end));
        assertThrows(IllegalArgumentException.class,
                () -> service.getBusinessSalesStatus(10L, null, end));
        assertThrows(IllegalArgumentException.class,
                () -> service.getBusinessSalesStatus(10L, end, start));
    }

    @Test
    void settlementQueriesAndUpdateShouldCoverSuccessAndFailure() {
        when(businessDAO.selectMonthlySettlementSummary(10L)).thenReturn(null);
        assertTrue(service.getMonthlySettlementSummary(10L) != null);
        when(businessDAO.selectSettlementPaymentHistory(10L)).thenReturn(null);
        assertTrue(service.getSettlementPaymentHistory(10L).isEmpty());

        SettlementManageVO account = new SettlementManageVO();
        when(businessDAO.selectSettlementAccount(10L)).thenReturn(account);
        assertSame(account, service.getSettlementAccount(10L));

        SettlementRequestVO request = new SettlementRequestVO();
        request.setRequestNo(100L);
        request.setOrderCount(2);
        request.setSettledAmount(50000L);
        request.setBankName("은행");
        request.setAccountNumber("123-456");
        request.setAccountHolder("예금주");

        when(businessDAO.selectSettlementRequestTarget(10L))
                .thenReturn(request);
        when(businessDAO.insertSettlementRequest(request)).thenReturn(1);
        when(businessDAO.updateSettlementRequestNo(10L, 100L))
                .thenReturn(2);

        service.requestSettlementConfirmation(10L);

        verify(businessDAO).insertSettlementRequest(request);
        verify(businessDAO).updateSettlementRequestNo(10L, 100L);
        verify(notificationService).createForAdmins(
                "SETTLEMENT_REQUEST",
                "사업자 정산 요청",
                "사업자가 정산금 지급을 요청했습니다.",
                "/admin/settlement/main",
                "SETTLEMENT_REQUEST",
                100L);

        when(businessDAO.updateSettlementAccount(
                10L,
                "은행",
                "123-456",
                "예금주")).thenReturn(1);
        service.updateSettlementAccount(
                10L,
                " 은행 ",
                " 123-456 ",
                " 예금주 ");

        assertThrows(IllegalArgumentException.class,
                () -> service.updateSettlementAccount(10L, "", "12345", "예금주"));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateSettlementAccount(10L, "은행", "abc", "예금주"));

        when(businessDAO.selectSettlementRequestTarget(11L))
                .thenReturn(null);
        assertThrows(IllegalStateException.class,
                () -> service.requestSettlementConfirmation(11L));
    }

    @Test
    void databaseContentSearchShouldTrimBlankAndHandleNullResult() {
        ContentSearchVO content = new ContentSearchVO();
        when(businessDAO.selectBusinessContentList("검색어"))
                .thenReturn(List.of(content));
        assertEquals(1, service.getContentList(" 검색어 ").size());

        when(businessDAO.selectBusinessContentList(null)).thenReturn(null);
        assertTrue(service.getContentList(" ").isEmpty());
    }

    @Test
    void cachedContentSearchShouldMatchTitleOriginalAndSearchText() {
        CachedContentVO titleMatch = cached(1L, "MOVIE", "검색 영화", "Original", "메타");
        CachedContentVO originalMatch = cached(2L, "TV", "다른 제목", "Search Original", "메타");
        CachedContentVO textMatch = cached(3L, "TV", "다른 제목", "Original", "actor search");
        CachedContentVO invalid = cached(null, null, "검색 무효", "", "");

        when(searchContentStore.getAll()).thenReturn(java.util.Arrays.asList(
                null,
                invalid,
                titleMatch,
                originalMatch,
                textMatch));

        assertEquals(1, service.getCachedContentList("검색 영화").size());
        assertEquals(1, service.getCachedContentList("search original").size());
        assertEquals(1, service.getCachedContentList("actor search").size());
        assertTrue(service.getCachedContentList(" ").isEmpty());
    }

    @Test
    void actorPreviewShouldValidateCacheAndConvertValidActors() {
        CachedContentVO cached = cached(1L, "MOVIE", "영화", "", "");
        when(searchContentStore.findByTmdbIdAndContentType(1L, "MOVIE"))
                .thenReturn(cached);
        ActorVO valid = new ActorVO();
        valid.setTmdbActorId(100L);
        valid.setActorName("배우");
        valid.setCharacterName("역할");
        ActorVO invalid = new ActorVO();
        when(tmdbService.getContentActorPreview(1L, " movie "))
                .thenReturn(java.util.Arrays.asList(null, invalid, valid));

        List<ActorSearchVO> result = service.getActorPreview(1L, " movie ");
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getTmdbActorId());
        assertEquals("배우", result.get(0).getActorName());

        assertThrows(IllegalArgumentException.class,
                () -> service.getActorPreview(null, "MOVIE"));
        assertThrows(IllegalArgumentException.class,
                () -> service.getActorPreview(1L, "BOOK"));
        when(searchContentStore.findByTmdbIdAndContentType(2L, "TV"))
                .thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.getActorPreview(2L, "TV"));
    }

    @Test
    void contentAndProductLookupMethodsShouldValidateAndReturnEmptyLists() {
        ContentSearchVO content = new ContentSearchVO();
        when(businessDAO.selectContentByNo(1L)).thenReturn(content);
        assertSame(content, service.getContentByNo(1L));

        when(businessDAO.selectActorListByContentNo(1L)).thenReturn(null);
        assertTrue(service.getActorListByContentNo(1L).isEmpty());
        when(businessDAO.selectApprovedProductListByBusinessNo(10L))
                .thenReturn(null);
        assertTrue(service.getApprovedProductListByBusinessNo(10L).isEmpty());
        when(businessDAO.selectProductListByBusinessNo(10L)).thenReturn(null);
        assertTrue(service.getProductListByBusinessNo(10L).isEmpty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getContentByNo(0L));
        when(businessDAO.selectContentByNo(2L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.getContentByNo(2L));
        assertThrows(IllegalArgumentException.class,
                () -> service.getApprovedProductListByBusinessNo(0L));
    }

    @Test
    void businessNumberAvailabilityShouldNormalizeAndDelegate() {
        when(businessDAO.countByBusinessNumber("123-45-67890")).thenReturn(0);
        assertTrue(service.isBusinessNumberAvailable("123-45-67890"));
        verify(businessDAO).countByBusinessNumber("123-45-67890");

        when(businessDAO.countByBusinessNumber(anyString())).thenReturn(1);
        assertFalse(service.isBusinessNumberAvailable("999-99-99999"));
        assertThrows(IllegalArgumentException.class,
                () -> service.isBusinessNumberAvailable(" "));
    }

    private CachedContentVO cached(
            Long id,
            String type,
            String title,
            String originalTitle,
            String searchText) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setTitle(title);
        content.setOriginalTitle(originalTitle);
        content.setSearchText(searchText);
        content.setGenreText("드라마");
        content.setAgeRating("15세 이상 관람가");
        return content;
    }
}
