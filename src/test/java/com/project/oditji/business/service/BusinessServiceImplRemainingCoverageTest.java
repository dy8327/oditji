package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.review.vo.ProductReviewVO;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;

/**
 * BusinessServiceImpl에서 기존 커버리지 테스트에 남기 쉬운 조건 분기와
 * 파일 처리 실패/정리 경로를 추가로 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class BusinessServiceImplRemainingCoverageTest {

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
    private Path productDirectory;
    private Path eventDirectory;

    @BeforeEach
    void setUp() {
        productDirectory = tempDirectory.resolve("products");
        eventDirectory = tempDirectory.resolve("events");

        service = new BusinessServiceImpl(
                businessDAO,
                contentService,
                searchContentStore,
                tmdbService,
                notificationService,
                productDirectory.toString(),
                eventDirectory.toString());
    }

    @Test
    void dashboardShouldKeepRecentCollectionsAndAverageRating() {
        List<OrderItemVO> recentOrders = List.of(new OrderItemVO());
        List<ProductReviewVO> recentReviews = List.of(new ProductReviewVO());

        when(businessDAO.selectTodayClickCountByBusinessNo(10L)).thenReturn(10);
        when(businessDAO.selectRecentOrdersByBusinessNo(10L)).thenReturn(recentOrders);
        when(businessDAO.selectRecentReviewsByBusinessNo(10L)).thenReturn(recentReviews);
        when(businessDAO.selectAverageRatingByBusinessNo(10L)).thenReturn(4.5);
        when(businessDAO.selectPopularProductsByBusinessNo(10L, 5))
                .thenReturn(Collections.emptyList());

        var dashboard = service.getBusinessDashboard(10L);

        assertSame(recentOrders, dashboard.getRecentOrders());
        assertSame(recentReviews, dashboard.getRecentReviews());
        assertEquals(4.5, dashboard.getAverageRating());
    }

    @Test
    void salesAndSettlementQueryMethodsShouldCoverRemainingSuccessAndValidation() {
        LocalDate startDate = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate endDate = LocalDate.of(2026, Month.AUGUST, 10);

        SettlementManageVO salesStatus = new SettlementManageVO();
        salesStatus.setProductName("정상 상품");
        when(businessDAO.selectBusinessSalesStatus(10L, startDate, endDate))
                .thenReturn(salesStatus);
        assertSame(salesStatus, service.getBusinessSalesStatus(10L, startDate, endDate));

        List<SettlementManageVO> history = List.of(new SettlementManageVO());
        when(businessDAO.selectBusinessSalesHistory(10L, startDate, endDate, 10, 10))
                .thenReturn(history);
        assertSame(history, service.getBusinessSalesHistory(10L, startDate, endDate, 2, 10));

        when(businessDAO.selectBusinessSalesHistoryCount(10L, startDate, endDate))
                .thenReturn(3);
        assertEquals(3, service.getBusinessSalesHistoryCount(10L, startDate, endDate));

        SettlementManageVO summary = new SettlementManageVO();
        when(businessDAO.selectMonthlySettlementSummary(10L)).thenReturn(summary);
        assertSame(summary, service.getMonthlySettlementSummary(10L));

        List<SettlementRequestVO> paymentHistory = List.of(new SettlementRequestVO());
        when(businessDAO.selectSettlementPaymentHistory(10L)).thenReturn(paymentHistory);
        assertSame(paymentHistory, service.getSettlementPaymentHistory(10L));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBusinessSalesStatus(10L, startDate, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getMonthlySettlementSummary(0L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getSettlementPaymentHistory(-1L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getSettlementAccount(0L));
    }

    @Test
    void settlementRequestShouldRejectEveryIncompleteTargetShape() {
        SettlementRequestVO nullOrderCount = validSettlementRequest(101L);
        nullOrderCount.setOrderCount(null);
        when(businessDAO.selectSettlementRequestTarget(11L)).thenReturn(nullOrderCount);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(11L));

        SettlementRequestVO zeroOrderCount = validSettlementRequest(102L);
        zeroOrderCount.setOrderCount(0);
        when(businessDAO.selectSettlementRequestTarget(12L)).thenReturn(zeroOrderCount);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(12L));

        SettlementRequestVO nullAmount = validSettlementRequest(103L);
        nullAmount.setSettledAmount(null);
        when(businessDAO.selectSettlementRequestTarget(13L)).thenReturn(nullAmount);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(13L));

        SettlementRequestVO zeroAmount = validSettlementRequest(104L);
        zeroAmount.setSettledAmount(0L);
        when(businessDAO.selectSettlementRequestTarget(14L)).thenReturn(zeroAmount);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(14L));

        SettlementRequestVO nullBank = validSettlementRequest(105L);
        nullBank.setBankName(null);
        when(businessDAO.selectSettlementRequestTarget(15L)).thenReturn(nullBank);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(15L));

        SettlementRequestVO blankBank = validSettlementRequest(106L);
        blankBank.setBankName(" ");
        when(businessDAO.selectSettlementRequestTarget(16L)).thenReturn(blankBank);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(16L));

        SettlementRequestVO nullAccount = validSettlementRequest(107L);
        nullAccount.setAccountNumber(null);
        when(businessDAO.selectSettlementRequestTarget(17L)).thenReturn(nullAccount);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(17L));

        SettlementRequestVO blankAccount = validSettlementRequest(108L);
        blankAccount.setAccountNumber(" ");
        when(businessDAO.selectSettlementRequestTarget(18L)).thenReturn(blankAccount);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(18L));

        SettlementRequestVO nullHolder = validSettlementRequest(109L);
        nullHolder.setAccountHolder(null);
        when(businessDAO.selectSettlementRequestTarget(19L)).thenReturn(nullHolder);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(19L));

        SettlementRequestVO blankHolder = validSettlementRequest(110L);
        blankHolder.setAccountHolder(" ");
        when(businessDAO.selectSettlementRequestTarget(20L)).thenReturn(blankHolder);
        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(20L));
    }

    @Test
    void settlementRequestShouldRejectInsertAndLinkFailures() {
        SettlementRequestVO insertFailure = validSettlementRequest(201L);
        when(businessDAO.selectSettlementRequestTarget(21L)).thenReturn(insertFailure);
        when(businessDAO.insertSettlementRequest(insertFailure)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(21L));

        SettlementRequestVO linkFailure = validSettlementRequest(202L);
        when(businessDAO.selectSettlementRequestTarget(22L)).thenReturn(linkFailure);
        when(businessDAO.insertSettlementRequest(linkFailure)).thenReturn(1);
        when(businessDAO.updateSettlementRequestNo(22L, 202L)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.requestSettlementConfirmation(22L));

        verify(notificationService, never()).createForAdmins(
                eq("SETTLEMENT_REQUEST"),
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                anyLong());
    }

    @Test
    void settlementAccountShouldCoverNullFieldsAndUpdateFailure() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSettlementAccount(0L, "은행", "12345", "예금주"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSettlementAccount(10L, null, "12345", "예금주"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSettlementAccount(10L, "은행", null, "예금주"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSettlementAccount(10L, "은행", "12345", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSettlementAccount(10L, "은행", "1234", "예금주"));

        when(businessDAO.updateSettlementAccount(10L, "은행", "12345", "예금주"))
                .thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> service.updateSettlementAccount(10L, "은행", "12345", "예금주"));
    }

    @Test
    void deliveryUpdateShouldCoverSameStatusPreparingAndDeliveredNotifications() {
        DeliveryManageVO sameStatus = delivery(
                "PREPARING",
                300L,
                301L,
                400L,
                "동일 상태 상품");
        when(businessDAO.selectBusinessDeliveryItem(10L, 301L)).thenReturn(sameStatus);
        when(businessDAO.mergeDelivery(any(DeliveryManageVO.class))).thenReturn(1);
        when(businessDAO.updateOrderItemDeliveryStatus(10L, 301L, "PREPARING"))
                .thenReturn(1);

        service.updateBusinessDelivery(10L, 301L, null, null, "PREPARING");

        verify(notificationService, never()).createForMember(
                eq(400L),
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                any(String.class),
                eq(301L));

        DeliveryManageVO preparing = delivery(
                "CONFIRMED",
                310L,
                311L,
                410L,
                " ");
        when(businessDAO.selectBusinessDeliveryItem(10L, 311L)).thenReturn(preparing);
        when(businessDAO.updateOrderItemDeliveryStatus(10L, 311L, "PREPARING"))
                .thenReturn(1);

        service.updateBusinessDelivery(10L, 311L, " ", " ", "preparing");

        verify(notificationService).createForMember(
                410L,
                "DELIVERY_PREPARING",
                "배송 준비 시작",
                "주문하신 상품의 배송 준비가 시작되었습니다.",
                "/order/list",
                "ORDER_ITEM",
                311L);

        DeliveryManageVO delivered = delivery(
                "SHIPPING",
                320L,
                321L,
                420L,
                null);
        when(businessDAO.selectBusinessDeliveryItem(10L, 321L)).thenReturn(delivered);
        when(businessDAO.updateOrderItemDeliveryStatus(10L, 321L, "DELIVERED"))
                .thenReturn(1);

        service.updateBusinessDelivery(
                10L,
                321L,
                "택배",
                "123456",
                "delivered");

        verify(notificationService).createForMember(
                420L,
                "DELIVERY_DELIVERED",
                "배송 완료",
                "주문하신 상품의 배송이 완료되었습니다.",
                "/order/list",
                "ORDER_ITEM",
                321L);
    }

    @Test
    void deliveryUpdateShouldCoverPersistenceFailuresAndLengthChecks() {
        DeliveryManageVO mergeFailure = delivery(
                "CONFIRMED",
                330L,
                331L,
                430L,
                "상품");
        when(businessDAO.selectBusinessDeliveryItem(10L, 331L)).thenReturn(mergeFailure);
        when(businessDAO.mergeDelivery(any(DeliveryManageVO.class)))
                .thenReturn(0, 1);

        assertThrows(
                IllegalStateException.class,
                () -> service.updateBusinessDelivery(
                        10L,
                        331L,
                        null,
                        null,
                        "PREPARING"));

        DeliveryManageVO itemFailure = delivery(
                "CONFIRMED",
                340L,
                341L,
                440L,
                "상품");
        when(businessDAO.selectBusinessDeliveryItem(10L, 341L)).thenReturn(itemFailure);
        when(businessDAO.updateOrderItemDeliveryStatus(10L, 341L, "PREPARING"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.updateBusinessDelivery(
                        10L,
                        341L,
                        null,
                        null,
                        "PREPARING"));

        DeliveryManageVO longTrackingItem = delivery(
                "CONFIRMED",
                350L,
                351L,
                450L,
                "상품");
        when(businessDAO.selectBusinessDeliveryItem(10L, 351L)).thenReturn(longTrackingItem);
        String longTrackingNumber = "1".repeat(101);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateBusinessDelivery(
                        10L,
                        351L,
                        null,
                        longTrackingNumber,
                        "PREPARING"));

        DeliveryManageVO longCourierItem = delivery(
                "CONFIRMED",
                360L,
                361L,
                460L,
                "상품");
        when(businessDAO.selectBusinessDeliveryItem(10L, 361L)).thenReturn(longCourierItem);
        String longCourierName = "가".repeat(51);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateBusinessDelivery(
                        10L,
                        361L,
                        longCourierName,
                        null,
                        "PREPARING"));
    }

    @Test
    void privateDeliveryHelpersShouldCoverNullAndUnknownStatusBranches() {
        DeliveryManageVO delivery = delivery(
                "CONFIRMED",
                400L,
                401L,
                500L,
                "상품");

        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                null,
                "SHIPPING");
        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                delivery,
                null);
        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                delivery,
                "UNKNOWN");

        verifyNoInteractions(notificationService);

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validateDeliveryStatusTransition",
                "UNKNOWN",
                "PREPARING"));
    }

    @Test
    void cachedContentSearchShouldCoverLimitAndShortCircuitConditions() {
        List<CachedContentVO> cachedContents = new ArrayList<>();
        cachedContents.add(null);
        cachedContents.add(cached(null, "MOVIE", "검색 제외 1"));
        cachedContents.add(cached(2L, null, "검색 제외 2"));

        for (long id = 10L; id < 111L; id++) {
            cachedContents.add(cached(id, "MOVIE", "검색 콘텐츠 " + id));
        }

        when(searchContentStore.getAll()).thenReturn(cachedContents);

        List<ContentSearchVO> result = service.getCachedContentList("검색");

        assertEquals(100, result.size());
        assertEquals(10L, result.get(0).getTmdbId());
        assertEquals(109L, result.get(99).getTmdbId());

        assertTrue(service.getCachedContentList(null).isEmpty());
    }

    @Test
    void actorPreviewAndActorListShouldCoverEmptyAndPresentBranches() {
        CachedContentVO firstCached = cached(501L, "MOVIE", "영화");
        when(searchContentStore.findByTmdbIdAndContentType(501L, "MOVIE"))
                .thenReturn(firstCached);
        when(tmdbService.getContentActorPreview(501L, "MOVIE")).thenReturn(null);
        assertTrue(service.getActorPreview(501L, "MOVIE").isEmpty());

        CachedContentVO secondCached = cached(502L, "TV", "드라마");
        when(searchContentStore.findByTmdbIdAndContentType(502L, "TV"))
                .thenReturn(secondCached);
        when(tmdbService.getContentActorPreview(502L, "TV"))
                .thenReturn(Collections.emptyList());
        assertTrue(service.getActorPreview(502L, "TV").isEmpty());

        ContentSearchVO storedContent = new ContentSearchVO();
        ActorSearchVO actor = new ActorSearchVO();
        when(businessDAO.selectContentByNo(50L)).thenReturn(storedContent);
        when(businessDAO.selectActorListByContentNo(50L)).thenReturn(List.of(actor));
        assertEquals(1, service.getActorListByContentNo(50L).size());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getActorListByContentNo(0L));

        when(businessDAO.selectContentByNo(51L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getActorListByContentNo(51L));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getActorPreview(0L, "MOVIE"));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getActorPreview(1L, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getActorPreview(1L, " "));
    }

    @Test
    void jsonlProductRegistrationShouldStoreContentResolveActorAndNotifyAdmin() {
        GoodsManageVO product = validProduct();
        product.setTmdbId(777L);
        product.setContentType(" movie ");
        product.setTmdbActorId(900L);

        when(searchContentStore.findByTmdbIdAndContentType(777L, "MOVIE"))
                .thenReturn(cached(777L, "MOVIE", "캐시 영화"));
        when(contentService.ensureContentStored(777L, " movie ")).thenReturn(300);

        ActorSearchVO wrongActor = new ActorSearchVO();
        wrongActor.setActorNo(40L);
        wrongActor.setTmdbActorId(800L);
        ActorSearchVO selectedActor = new ActorSearchVO();
        selectedActor.setActorNo(50L);
        selectedActor.setTmdbActorId(900L);
        when(businessDAO.selectActorListByContentNo(300L))
                .thenReturn(java.util.Arrays.asList(null, wrongActor, selectedActor));
        when(businessDAO.countContentActor(300L, 50L)).thenReturn(1);
        when(businessDAO.insertProduct(product)).thenAnswer(invocation -> {
            GoodsManageVO target = invocation.getArgument(0);
            target.setProductNo(700L);
            return 1;
        });
        when(businessDAO.insertProductImage(product)).thenReturn(1);

        long productNo = service.registerProduct(
                product,
                image("main.WEBP", "image/webp"),
                null);

        assertEquals(700L, productNo);
        assertEquals(300L, product.getContentNo());
        assertEquals(50L, product.getActorNo());
        assertEquals("WAITING", product.getStatus());
        verify(notificationService).createForAdmins(
                "PRODUCT_REQUEST",
                "상품 승인 요청",
                "테스트 상품 상품의 등록 승인 요청이 접수되었습니다.",
                "/admin/product/list?tab=waiting",
                "PRODUCT",
                700L);
    }

    @Test
    void jsonlProductRegistrationShouldRejectInvalidCacheAndMissingActor() {
        GoodsManageVO invalidType = validProduct();
        invalidType.setTmdbId(801L);
        invalidType.setContentType("BOOK");
        MultipartFile invalidTypeImage = image("invalid-type.png", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(
                        invalidType,
                        invalidTypeImage,
                        null));

        GoodsManageVO missingCache = validProduct();
        missingCache.setTmdbId(802L);
        missingCache.setContentType("MOVIE");
        when(searchContentStore.findByTmdbIdAndContentType(802L, "MOVIE"))
                .thenReturn(null);
        MultipartFile missingCacheImage = image("missing-cache.png", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(
                        missingCache,
                        missingCacheImage,
                        null));

        GoodsManageVO missingActor = validProduct();
        missingActor.setTmdbId(803L);
        missingActor.setContentType("TV");
        missingActor.setTmdbActorId(990L);
        when(searchContentStore.findByTmdbIdAndContentType(803L, "TV"))
                .thenReturn(cached(803L, "TV", "캐시 드라마"));
        when(contentService.ensureContentStored(803L, "TV")).thenReturn(303);
        when(businessDAO.selectActorListByContentNo(303L)).thenReturn(null);

        MultipartFile missingActorImage = image("missing-actor.png", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(
                        missingActor,
                        missingActorImage,
                        null));
    }

    @Test
    void registerProductShouldCleanImageForMissingProductNoAndImageInsertFailure() throws Exception {
        GoodsManageVO missingProductNo = validProduct();
        missingProductNo.setContentNo(901L);
        when(businessDAO.selectContentByNo(901L)).thenReturn(new ContentSearchVO());
        when(businessDAO.insertProduct(missingProductNo)).thenReturn(1);

        MultipartFile missingProductNoImage = image("missing-no.png", "image/png");
        assertThrows(
                IllegalStateException.class,
                () -> service.registerProduct(
                        missingProductNo,
                        missingProductNoImage,
                        null));
        assertDirectoryHasNoFiles(productDirectory);

        GoodsManageVO imageInsertFailure = validProduct();
        imageInsertFailure.setContentNo(902L);
        when(businessDAO.selectContentByNo(902L)).thenReturn(new ContentSearchVO());
        when(businessDAO.insertProduct(imageInsertFailure)).thenAnswer(invocation -> {
            GoodsManageVO target = invocation.getArgument(0);
            target.setProductNo(9020L);
            return 1;
        });
        when(businessDAO.insertProductImage(imageInsertFailure)).thenReturn(0);

        MultipartFile imageRowFailureImage = image("image-row-failure.png", "image/png");
        assertThrows(
                IllegalStateException.class,
                () -> service.registerProduct(
                        imageInsertFailure,
                        imageRowFailureImage,
                        null));
        assertDirectoryHasNoFiles(productDirectory);
    }

    @Test
    void eventQueriesShouldCoverValidationNonNullListAndEmptyConnectedProducts() {
        EventManageVO listEvent = validEvent();
        listEvent.setEventNo(1000L);
        when(businessDAO.selectEventListByBusinessNo(10L, "검색", 0, 5))
                .thenReturn(List.of(listEvent));
        assertEquals(
                1,
                service.getEventListByBusinessNo(10L, " 검색 ", 1, 5).size());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getEventListByBusinessNo(0L, null, 1, 5));

        EventManageVO approved = validEvent();
        approved.setEventNo(1001L);
        when(businessDAO.selectApprovedEventForBusiness(1001L, 10L)).thenReturn(approved);
        when(businessDAO.selectEventProductListByEventNo(1001L)).thenReturn(null);

        EventManageVO result = service.getApprovedEventForBusiness(1001L, 10L);
        assertTrue(result.getConnectedProducts().isEmpty());
        assertEquals(10L, result.getBusinessNo());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getApprovedEventForBusiness(0L, 10L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getApprovedEventForBusiness(1001L, 0L));

        when(businessDAO.selectApprovedEventForBusiness(1002L, 10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getApprovedEventForBusiness(1002L, 10L));
    }

    @Test
    void eventValidationShouldCoverRequiredFieldsAndProductOwnership() {
        EventManageVO invalidBusiness = validEvent();
        invalidBusiness.setBusinessNo(0L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(invalidBusiness, null));

        EventManageVO nullTitle = validEvent();
        nullTitle.setTitle(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(nullTitle, null));

        EventManageVO blankTitle = validEvent();
        blankTitle.setTitle(" ");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(blankTitle, null));

        EventManageVO longTitle = validEvent();
        longTitle.setTitle("가".repeat(201));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(longTitle, null));

        EventManageVO missingStart = validEvent();
        missingStart.setStartDate(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(missingStart, null));

        EventManageVO missingEnd = validEvent();
        missingEnd.setEndDate(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(missingEnd, null));

        EventManageVO reversedDates = validEvent();
        reversedDates.setEndDate(reversedDates.getStartDate().minusDays(1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(reversedDates, null));

        EventManageVO nullProducts = validEvent();
        nullProducts.setProductNoList(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(nullProducts, null));

        EventManageVO emptyProducts = validEvent();
        emptyProducts.setProductNoList(Collections.emptyList());
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(emptyProducts, null));

        EventManageVO nullRates = validEvent();
        nullRates.setDiscountRateList(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(nullRates, null));

        EventManageVO mismatchedRates = validEvent();
        mismatchedRates.setDiscountRateList(List.of(10, 20));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(mismatchedRates, null));

        EventManageVO nullProductNo = validEvent();
        nullProductNo.setProductNoList(Collections.singletonList(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(nullProductNo, null));

        EventManageVO zeroProductNo = validEvent();
        zeroProductNo.setProductNoList(List.of(0L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(zeroProductNo, null));

        EventManageVO nullRate = validEvent();
        nullRate.setDiscountRateList(Collections.singletonList(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(nullRate, null));

        EventManageVO negativeRate = validEvent();
        negativeRate.setDiscountRateList(List.of(-1));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(negativeRate, null));

        EventManageVO excessiveRate = validEvent();
        excessiveRate.setDiscountRateList(List.of(101));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(excessiveRate, null));

        EventManageVO foreignProduct = validEvent();
        when(businessDAO.countProductByBusinessNo(1L, 10L, null)).thenReturn(0);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerEvent(foreignProduct, null));
    }

    @Test
    void eventImageValidationShouldCoverFilenameExtensionTypeAndSizeBranches() {
        MultipartFile oversized = mock(MultipartFile.class);
        when(oversized.getSize()).thenReturn(10L * 1024L * 1024L + 1L);
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateEventImage", oversized));

        MultipartFile nullFilename = mock(MultipartFile.class);
        when(nullFilename.getSize()).thenReturn(1L);
        when(nullFilename.getOriginalFilename()).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateEventImage", nullFilename));

        MultipartFile blankFilename = mock(MultipartFile.class);
        when(blankFilename.getSize()).thenReturn(1L);
        when(blankFilename.getOriginalFilename()).thenReturn(" ");
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(service, "validateEventImage", blankFilename));

        MultipartFile noExtensionImage = image("banner", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateEventImage",
                        noExtensionImage));

        MultipartFile emptyExtensionImage = image("banner.", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateEventImage",
                        emptyExtensionImage));

        MultipartFile invalidExtensionImage = image("banner.txt", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateEventImage",
                        invalidExtensionImage));

        MultipartFile invalidContentTypeImage = image("banner.png", "text/plain");
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateEventImage",
                        invalidContentTypeImage));

        MultipartFile validJpegImage = image("banner.JPEG", null);
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "validateEventImage",
                validJpegImage));
    }

    @Test
    void registerEventWithImageShouldSaveBannerAndNotifyAdmin() throws Exception {
        EventManageVO event = validEvent();
        when(businessDAO.countProductByBusinessNo(1L, 10L, null)).thenReturn(1);
        when(businessDAO.insertEvent(event)).thenAnswer(invocation -> {
            EventManageVO target = invocation.getArgument(0);
            target.setEventNo(1100L);
            return 1;
        });
        when(businessDAO.insertEventProduct(1100L, 1L, 10)).thenReturn(1);

        long eventNo = service.registerEvent(
                event,
                image("banner.JPG", "image/jpeg"));

        assertEquals(1100L, eventNo);
        assertNotNull(event.getBannerImage());
        assertTrue(event.getBannerImage().startsWith("/uploads/event/"));
        assertDirectoryFileCount(eventDirectory, 1L);
        verify(notificationService).createForAdmins(
                "EVENT_REQUEST",
                "이벤트 승인 요청",
                "여름 이벤트 이벤트의 등록 승인 요청이 접수되었습니다.",
                "/admin/event/list?tab=waiting",
                "EVENT",
                1100L);
    }

    @Test
    void registerEventShouldCleanImageWhenDatabaseOrFileSaveFails() throws Exception {
        EventManageVO insertFailure = validEvent();
        when(businessDAO.countProductByBusinessNo(1L, 10L, null)).thenReturn(1);
        when(businessDAO.insertEvent(insertFailure)).thenReturn(0);

        MultipartFile insertFailureImage = image("insert-failure.png", "image/png");
        assertThrows(
                IllegalStateException.class,
                () -> service.registerEvent(
                        insertFailure,
                        insertFailureImage));
        assertDirectoryHasNoFiles(eventDirectory);

        EventManageVO ioFailureEvent = validEvent();
        MultipartFile ioFailureImage = mock(MultipartFile.class);
        when(ioFailureImage.isEmpty()).thenReturn(false);
        when(ioFailureImage.getSize()).thenReturn(1L);
        when(ioFailureImage.getOriginalFilename()).thenReturn("io-failure.png");
        when(ioFailureImage.getContentType()).thenReturn("image/png");
        when(ioFailureImage.getInputStream()).thenThrow(new IOException("forced"));

        assertThrows(
                IllegalStateException.class,
                () -> service.registerEvent(ioFailureEvent, ioFailureImage));
        assertDirectoryHasNoFiles(eventDirectory);
    }

    @Test
    void registerEventShouldRejectMissingGeneratedNoAndRelationInsertFailure() {
        EventManageVO missingGeneratedNo = validEvent();
        when(businessDAO.countProductByBusinessNo(1L, 10L, null)).thenReturn(1);
        when(businessDAO.insertEvent(missingGeneratedNo)).thenReturn(1);
        assertThrows(
                IllegalStateException.class,
                () -> service.registerEvent(missingGeneratedNo, null));

        EventManageVO relationFailure = validEvent();
        when(businessDAO.insertEvent(relationFailure)).thenAnswer(invocation -> {
            EventManageVO target = invocation.getArgument(0);
            target.setEventNo(1200L);
            return 1;
        });
        when(businessDAO.insertEventProduct(1200L, 1L, 10)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.registerEvent(relationFailure, null));
    }

    @Test
    void updateApprovedEventWithImageShouldCleanNewFileOnDeleteOrInsertFailure() throws Exception {
        EventManageVO existingDeleteFailure = approvedEvent(1300L);
        when(businessDAO.selectApprovedEventForBusiness(1300L, 10L))
                .thenReturn(existingDeleteFailure);
        when(businessDAO.countProductByBusinessNo(1L, 10L, 1300L)).thenReturn(1);

        EventManageVO deleteFailure = validEvent();
        deleteFailure.setEventNo(1300L);
        when(businessDAO.updateApprovedEvent(deleteFailure)).thenReturn(1);
        when(businessDAO.deleteEventProductByEventNo(1300L)).thenReturn(0);

        MultipartFile deleteFailureImage = image("delete-failure.png", "image/png");
        assertThrows(
                IllegalStateException.class,
                () -> service.updateApprovedEvent(
                        deleteFailure,
                        deleteFailureImage));
        assertDirectoryHasNoFiles(eventDirectory);

        EventManageVO existingInsertFailure = approvedEvent(1301L);
        when(businessDAO.selectApprovedEventForBusiness(1301L, 10L))
                .thenReturn(existingInsertFailure);
        when(businessDAO.countProductByBusinessNo(1L, 10L, 1301L)).thenReturn(1);

        EventManageVO insertFailure = validEvent();
        insertFailure.setEventNo(1301L);
        when(businessDAO.updateApprovedEvent(insertFailure)).thenReturn(1);
        when(businessDAO.deleteEventProductByEventNo(1301L)).thenReturn(1);
        when(businessDAO.insertEventProduct(1301L, 1L, 10)).thenReturn(0);

        MultipartFile relationFailureImage = image("relation-failure.png", "image/png");
        assertThrows(
                IllegalStateException.class,
                () -> service.updateApprovedEvent(
                        insertFailure,
                        relationFailureImage));
        assertDirectoryHasNoFiles(eventDirectory);
    }

    @Test
    void eventExtensionShouldRejectLongReasonAndDatabaseFailure() {
        EventManageVO existing = approvedEvent(1400L);
        existing.setTitle("연장 이벤트");
        when(businessDAO.selectApprovedEventForBusiness(1400L, 10L)).thenReturn(existing);

        LocalDate newEndDate = existing.getEndDate().plusDays(7);
        String longReason = "사".repeat(1001);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.extendApprovedEvent(
                        1400L,
                        10L,
                        newEndDate,
                        longReason));

        when(businessDAO.extendApprovedEvent(1400L, 10L, newEndDate)).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> service.extendApprovedEvent(
                        1400L,
                        10L,
                        newEndDate,
                        "정상 사유"));
    }

    @Test
    void productImagePathHelpersShouldCoverSafeUnsafeAndAfterCommitDeletion() throws Exception {
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "resolveProductImagePhysicalPath",
                (Object) null));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "resolveProductImagePhysicalPath",
                " "));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "resolveProductImagePhysicalPath",
                "/outside/image.png"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "resolveProductImagePhysicalPath",
                "/uploads/product/"));
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "resolveProductImagePhysicalPath",
                "/uploads/product/sub/image.png"));

        Path expected = productDirectory.resolve("safe.png").toAbsolutePath().normalize();
        Path resolved = ReflectionTestUtils.invokeMethod(
                service,
                "resolveProductImagePhysicalPath",
                "\\uploads\\product\\safe.png");
        assertEquals(expected, resolved);

        Files.createDirectories(productDirectory);
        Path deleteAfterCommit = Files.writeString(
                productDirectory.resolve("delete-after-commit.png"),
                "image",
                StandardCharsets.UTF_8);

        TransactionSynchronizationManager.initSynchronization();
        try {
            ReflectionTestUtils.invokeMethod(
                    service,
                    "registerProductImageFilesForDeletionAfterCommit",
                    List.of(deleteAfterCommit));

            assertTrue(Files.exists(deleteAfterCommit));

            for (TransactionSynchronization synchronization
                    : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            assertFalse(Files.exists(deleteAfterCommit));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private SettlementRequestVO validSettlementRequest(long requestNo) {
        SettlementRequestVO request = new SettlementRequestVO();
        request.setRequestNo(requestNo);
        request.setOrderCount(1);
        request.setSettledAmount(10000L);
        request.setBankName("테스트은행");
        request.setAccountNumber("12345");
        request.setAccountHolder("예금주");
        return request;
    }

    private DeliveryManageVO delivery(
            String status,
            Long orderNo,
            Long orderItemNo,
            Long memberNo,
            String productName) {
        DeliveryManageVO item = new DeliveryManageVO();
        item.setStatus(status);
        item.setOrderNo(orderNo);
        item.setOrderItemNo(orderItemNo);
        item.setMemberNo(memberNo);
        item.setProductName(productName);
        return item;
    }

    private CachedContentVO cached(Long tmdbId, String contentType, String title) {
        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setOriginalTitle("Original");
        content.setSearchText("search");
        content.setGenreText("드라마");
        content.setAgeRating("15세 이상 관람가");
        return content;
    }

    private GoodsManageVO validProduct() {
        GoodsManageVO product = new GoodsManageVO();
        product.setBusinessNo(10L);
        product.setProductName(" 테스트 상품 ");
        product.setProductType("ETC");
        product.setPrice(10000L);
        product.setDiscountRate(10);
        product.setStock(5);
        product.setDescription(" 설명 ");
        return product;
    }

    private EventManageVO validEvent() {
        EventManageVO event = new EventManageVO();
        event.setBusinessNo(10L);
        event.setTitle(" 여름 이벤트 ");
        event.setDescription("설명");
        event.setStartDate(LocalDate.of(2026, Month.AUGUST, 10));
        event.setEndDate(LocalDate.of(2026, Month.AUGUST, 31));
        event.setStatus("APPROVED");
        event.setProductNoList(List.of(1L));
        event.setDiscountRateList(List.of(10));
        return event;
    }

    private EventManageVO approvedEvent(long eventNo) {
        EventManageVO event = validEvent();
        event.setEventNo(eventNo);
        event.setTitle("승인 이벤트");
        event.setBannerImage("/uploads/event/old.png");
        return event;
    }

    private MockMultipartFile image(String filename, String contentType) {
        return new MockMultipartFile(
                "image",
                filename,
                contentType,
                "image-data".getBytes(StandardCharsets.UTF_8));
    }

    private void assertDirectoryHasNoFiles(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }

        try (var files = Files.list(directory)) {
            assertEquals(0L, files.count());
        }
    }

    private void assertDirectoryFileCount(Path directory, long expectedCount) throws Exception {
        assertTrue(Files.exists(directory));
        try (var files = Files.list(directory)) {
            assertEquals(expectedCount, files.count());
        }
    }
}
