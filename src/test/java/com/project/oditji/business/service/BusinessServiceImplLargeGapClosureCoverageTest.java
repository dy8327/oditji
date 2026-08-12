package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.goods.vo.ProductOptionVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/**
 * BusinessServiceImpl에 마지막까지 남기 쉬운 복합조건의 반대쪽 피연산자를 한 번에 보완합니다.
 */
class BusinessServiceImplLargeGapClosureCoverageTest {

    @TempDir
    Path tempDirectory;

    private BusinessDAO businessDAO;
    private NotificationService notificationService;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        notificationService = mock(NotificationService.class);
        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                notificationService,
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void orderSalesAndBusinessNumberShouldCoverRemainingNullEmptyAndFormatOperands() {
        when(businessDAO.selectBusinessOrderList(10L, 0, 10))
                .thenReturn(Collections.emptyList());
        assertTrue(service.getBusinessOrderList(10L, 1, 10).isEmpty());

        LocalDate startDate = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate endDate = LocalDate.of(2026, Month.AUGUST, 5);
        SettlementManageVO nullName = new SettlementManageVO();
        nullName.setProductName(null);
        when(businessDAO.selectBusinessSalesStatus(11L, startDate, endDate))
                .thenReturn(nullName);
        assertEquals(
                "판매 상품 없음",
                service.getBusinessSalesStatus(11L, startDate, endDate).getProductName());

        String nullBusinessNumber = null;
        assertThrows(
                IllegalArgumentException.class,
                () -> service.isBusinessNumberAvailable(nullBusinessNumber));

        String malformedBusinessNumber = "1234567890";
        assertThrows(
                IllegalArgumentException.class,
                () -> service.isBusinessNumberAvailable(malformedBusinessNumber));
    }

    @Test
    void deliveryNotificationShouldCoverNullBlankTextAndUnsupportedStatusBranches() {
        DeliveryManageVO delivery = new DeliveryManageVO();
        delivery.setMemberNo(7L);
        delivery.setOrderItemNo(8L);

        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                (Object) null,
                "PREPARING");
        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                delivery,
                null);

        delivery.setProductName(null);
        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                delivery,
                "PREPARING");

        delivery.setProductName("   ");
        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                delivery,
                "SHIPPING");

        delivery.setProductName("  테스트 상품  ");
        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                delivery,
                "DELIVERED");

        ReflectionTestUtils.invokeMethod(
                service,
                "createDeliveryStatusNotification",
                delivery,
                "UNKNOWN");

        verify(notificationService, times(3)).createForMember(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void optionHelpersShouldCoverOptionProductWithNullListAndNullPreviousStock() {
        GoodsManageVO clothesWithoutOptions = new GoodsManageVO();
        clothesWithoutOptions.setProductType("CLOTHES");
        clothesWithoutOptions.setStock(9);
        ReflectionTestUtils.setField(clothesWithoutOptions, "optionList", null);

        ReflectionTestUtils.invokeMethod(
                service,
                "applyOptionTotalStock",
                clothesWithoutOptions);
        assertEquals(9, clothesWithoutOptions.getStock());

        GoodsManageVO shoesWithoutOptions = new GoodsManageVO();
        shoesWithoutOptions.setProductType("SHOES");
        shoesWithoutOptions.setStock(4);
        ReflectionTestUtils.setField(shoesWithoutOptions, "optionList", null);
        ReflectionTestUtils.invokeMethod(
                service,
                "applyOptionTotalStock",
                shoesWithoutOptions);
        assertEquals(4, shoesWithoutOptions.getStock());

        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(90L);
        goods.setProductName("재입고 상품");

        ProductOptionVO existing = option(901L, "BLACK", "M", null);
        ProductOptionVO updated = option(null, "BLACK", "M", 2);
        when(businessDAO.updateProductOption(updated)).thenReturn(1);

        java.util.Set<Long> maintained = new java.util.HashSet<Long>();
        ReflectionTestUtils.invokeMethod(
                service,
                "updateExistingProductOption",
                goods,
                updated,
                existing,
                maintained);

        assertTrue(maintained.contains(901L));
        assertEquals(Long.valueOf(901L), updated.getOptionNo());
        verify(notificationService).createOptionRestockNotifications(
                90L,
                901L,
                "재입고 상품",
                "BLACK",
                "M");
    }

    @Test
    void selectedDetailCounterAndDeletionHelperShouldCoverSecondOperandsAndZeroDeleteResult() {
        org.springframework.mock.web.MockMultipartFile empty =
                new org.springframework.mock.web.MockMultipartFile(
                        "detail",
                        "empty.png",
                        "image/png",
                        new byte[0]);
        org.springframework.mock.web.MockMultipartFile selected =
                new org.springframework.mock.web.MockMultipartFile(
                        "detail",
                        "selected.png",
                        "image/png",
                        new byte[] { 1 });

        Object rawCount = ReflectionTestUtils.invokeMethod(
                service,
                "countSelectedDetailImages",
                (Object) new org.springframework.web.multipart.MultipartFile[] {
                        null,
                        empty,
                        selected
                });
        assertEquals(1, ((Integer) rawCount).intValue());

        List<Path> deletionTargets = new ArrayList<Path>();
        when(businessDAO.deleteProductDetailImageByPath(
                50L,
                "/uploads/product/missing.png"))
                .thenReturn(0);

        ReflectionTestUtils.invokeMethod(
                service,
                "deleteRequestedDetailProductImages",
                50L,
                java.util.Set.of("/uploads/product/missing.png"),
                deletionTargets);

        assertTrue(deletionTargets.isEmpty());

        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(
                service,
                "addProductImagePathForDeletion",
                "/outside/not-product.png",
                deletionTargets));
        assertTrue(deletionTargets.isEmpty());
    }

    @Test
    void actorNormalizationShouldKeepNullAndPositiveButClearNonPositiveValue() {
        GoodsManageVO nullActor = new GoodsManageVO();
        nullActor.setActorNo(null);
        ReflectionTestUtils.invokeMethod(service, "normalizeActorNo", nullActor);
        assertNull(nullActor.getActorNo());

        GoodsManageVO positiveActor = new GoodsManageVO();
        positiveActor.setActorNo(5L);
        ReflectionTestUtils.invokeMethod(service, "normalizeActorNo", positiveActor);
        assertEquals(Long.valueOf(5L), positiveActor.getActorNo());

        GoodsManageVO zeroActor = new GoodsManageVO();
        zeroActor.setActorNo(0L);
        ReflectionTestUtils.invokeMethod(service, "normalizeActorNo", zeroActor);
        assertNull(zeroActor.getActorNo());

        assertEquals(
                Boolean.FALSE,
                ReflectionTestUtils.invokeMethod(service, "isOptionProduct", "ETC"));
    }

    private ProductOptionVO option(
            Long optionNo,
            String colorName,
            String sizeName,
            Integer stock) {
        ProductOptionVO option = new ProductOptionVO();
        option.setOptionNo(optionNo);
        option.setColorName(colorName);
        option.setSizeName(sizeName);
        option.setStock(stock);
        return option;
    }
}
