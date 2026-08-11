package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 사업자 서비스의 검증/정규화 private helper 잔여 조건을 집중 보완합니다. */
class BusinessServiceImplDeepHelperCoverageTest {

    @TempDir
    Path tempDirectory;

    private BusinessDAO businessDAO;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                mock(NotificationService.class),
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void salesAndKeywordHelpersShouldCoverNullBlankBoundaryAndValidValues() {
        LocalDate start = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate end = LocalDate.of(2026, Month.AUGUST, 31);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateSalesSearchCondition", 0L, start, end));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateSalesSearchCondition", 1L, null, end));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateSalesSearchCondition", 1L, start, null));
        LocalDate later = LocalDate.of(2026, Month.SEPTEMBER, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateSalesSearchCondition", 1L, later, end));
        assertDoesNotThrow(() -> invoke("validateSalesSearchCondition", 1L, start, end));

        assertNull(invoke("normalizeKeyword", new Object[] { null }));
        assertNull(invoke("normalizeKeyword", "   "));
        assertEquals("keyword", invoke("normalizeKeyword", " keyword "));
    }

    @Test
    void deliveryNumberStatusAndTransitionHelpersShouldCoverAllShortCircuits() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryRequestNumbers", 0L, 1L));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryRequestNumbers", 1L, 0L));
        assertDoesNotThrow(() -> invoke("validateDeliveryRequestNumbers", 1L, 1L));

        assertThrows(
                IllegalStateException.class,
                () -> invoke("validateDeliveryChangeAllowed", "CANCEL_REQUEST"));
        assertThrows(
                IllegalStateException.class,
                () -> invoke("validateDeliveryChangeAllowed", "CANCELED"));
        assertThrows(
                IllegalStateException.class,
                () -> invoke("validateDeliveryChangeAllowed", "REFUNDED"));
        assertDoesNotThrow(() -> invoke("validateDeliveryChangeAllowed", "CONFIRMED"));

        assertNull(invoke("normalizeDeliveryStatusFilter", new Object[] { null }));
        assertNull(invoke("normalizeDeliveryStatusFilter", "   "));
        assertNull(invoke("normalizeDeliveryStatusFilter", "all"));
        assertEquals("SHIPPING", invoke("normalizeDeliveryStatusFilter", " shipping "));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("normalizeDeliveryStatusFilter", "UNKNOWN"));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("normalizeDeliveryUpdateStatus", new Object[] { null }));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("normalizeDeliveryUpdateStatus", "   "));
        assertEquals("DELIVERED", invoke("normalizeDeliveryUpdateStatus", " delivered "));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("normalizeDeliveryUpdateStatus", "CONFIRMED"));

        // Map.of() 기반 statusOrder는 null key 조회 시 NPE가 발생하므로,
        // "등록되지 않은 상태"를 사용해 currentStep/nextStep == null 분기를 검증합니다.
        assertDoesNotThrow(() -> invoke("validateDeliveryStatusTransition", "UNKNOWN", "SHIPPING"));
        assertDoesNotThrow(() -> invoke("validateDeliveryStatusTransition", "SHIPPING", "UNKNOWN"));
        assertDoesNotThrow(() -> invoke("validateDeliveryStatusTransition", "PREPARING", "SHIPPING"));
        assertThrows(
                IllegalStateException.class,
                () -> invoke("validateDeliveryStatusTransition", "DELIVERED", "SHIPPING"));
    }

    @Test
    void deliveryDetailsShouldCoverTrackingRequirementAndLengthValidation() {
        assertDoesNotThrow(() -> invoke("validateDeliveryDetails", "PREPARING", null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryDetails", "SHIPPING", null, "123"));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryDetails", "SHIPPING", "", "123"));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryDetails", "DELIVERED", "CJ", null));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryDetails", "DELIVERED", "CJ", ""));

        String longTracking = "1".repeat(101);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryDetails", "PREPARING", "CJ", longTracking));

        String longCourier = "C".repeat(51);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDeliveryDetails", "PREPARING", longCourier, "123"));
        assertDoesNotThrow(() -> invoke("validateDeliveryDetails", "SHIPPING", "CJ", "123"));
    }

    @Test
    void optionAndImagePathHelpersShouldCoverNullBlankDuplicateAndValidInputs() {
        assertFalse((Boolean) invoke("isOptionProduct", new Object[] { null }));
        assertTrue((Boolean) invoke("isOptionProduct", "CLOTHES"));
        assertTrue((Boolean) invoke("isOptionProduct", "SHOES"));
        assertFalse((Boolean) invoke("isOptionProduct", "GOODS"));

        assertEquals("|", invoke("createProductOptionKey", null, null));
        assertEquals("BLACK|XL", invoke("createProductOptionKey", " black ", " xl "));

        Set<String> none = castSet(invoke(
                "filterExistingDetailImagePaths",
                List.of("/uploads/product/a.png"),
                null));
        assertTrue(none.isEmpty());

        String[] deleted = { null, "/uploads/product/missing.png", "/uploads/product/a.png" };
        Set<String> selected = castSet(invoke(
                "filterExistingDetailImagePaths",
                List.of("/uploads/product/a.png"),
                deleted));
        assertEquals(Set.of("/uploads/product/a.png"), selected);

        assertEquals(0, (Integer) invoke("countSelectedDetailImages", new Object[] { null }));
        MultipartFile empty = mock(MultipartFile.class);
        MultipartFile selectedFile = mock(MultipartFile.class);
        when(empty.isEmpty()).thenReturn(true);
        when(selectedFile.isEmpty()).thenReturn(false);
        MultipartFile[] detailImages = { null, empty, selectedFile };
        assertEquals(1, (Integer) invoke("countSelectedDetailImages", (Object) detailImages));

        assertNull(invoke("resolveProductImagePhysicalPath", new Object[] { null }));
        assertNull(invoke("resolveProductImagePhysicalPath", "   "));
        assertNull(invoke("resolveProductImagePhysicalPath", "/images/a.png"));
        assertNull(invoke("resolveProductImagePhysicalPath", "/uploads/product/"));
        assertNull(invoke("resolveProductImagePhysicalPath", "/uploads/product/a/b.png"));
        Object validPath = invoke("resolveProductImagePhysicalPath", "/uploads/product/a.png");
        assertTrue(validPath instanceof Path);
    }

    @Test
    void productUpdateAndEventValidationHelpersShouldCoverRemainingGuards() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductUpdateRequest", new Object[] { null }));

        GoodsManageVO invalidProduct = new GoodsManageVO();
        invalidProduct.setProductNo(0L);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductUpdateRequest", invalidProduct));

        GoodsManageVO validProduct = new GoodsManageVO();
        validProduct.setProductNo(1L);
        assertDoesNotThrow(() -> invoke("validateProductUpdateRequest", validProduct));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventBusinessNo", 0L));
        assertDoesNotThrow(() -> invoke("validateEventBusinessNo", 1L));

        EventManageVO event = new EventManageVO();
        event.setTitle(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndNormalizeEventTitle", event));
        event.setTitle("   ");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndNormalizeEventTitle", event));
        event.setTitle("A".repeat(201));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndNormalizeEventTitle", event));
        event.setTitle(" 이벤트 ");
        invoke("validateAndNormalizeEventTitle", event);
        assertEquals("이벤트", event.getTitle());

        event.setStartDate(null);
        event.setEndDate(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventDates", event));
        event.setStartDate(LocalDate.of(2026, Month.AUGUST, 10));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventDates", event));
        event.setEndDate(LocalDate.of(2026, Month.AUGUST, 9));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventDates", event));
        event.setEndDate(LocalDate.of(2026, Month.AUGUST, 20));
        assertDoesNotThrow(() -> invoke("validateEventDates", event));

        event.setStatus("APPROVED");
        invoke("normalizeEventStatus", event);
        assertEquals("WAITING", event.getStatus());
        invoke("normalizeEventStatus", event);
        assertEquals("WAITING", event.getStatus());
    }

    @Test
    void eventProductListAndDistinctValidationShouldCoverNullEmptyMismatchAndDuplicateCases() {
        List<Long> emptyProductNos = List.of();
        List<Integer> emptyDiscountRates = List.of();
        List<Long> singleProductNo = List.of(1L);
        List<Integer> singleDiscountRate = List.of(10);
        List<Integer> mismatchedDiscountRates = List.of(10, 20);
        List<Long> duplicateProductNos = List.of(1L, 1L);
        List<Long> distinctProductNos = List.of(1L, 2L);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventProductLists", null, emptyDiscountRates));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventProductLists", emptyProductNos, emptyDiscountRates));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventProductLists", singleProductNo, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateEventProductLists", singleProductNo, mismatchedDiscountRates));
        assertDoesNotThrow(() -> invoke("validateEventProductLists", singleProductNo, singleDiscountRate));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDistinctEventProducts", duplicateProductNos));
        assertDoesNotThrow(() -> invoke("validateDistinctEventProducts", distinctProductNos));
    }

    @Test
    void cachedContentActorAndFilenameHelpersShouldCoverBothSides() {
        assertFalse((Boolean) invoke("containsIgnoreCase", null, "abc"));
        assertFalse((Boolean) invoke("containsIgnoreCase", "ABC", "zzz"));
        assertTrue((Boolean) invoke("containsIgnoreCase", "ABC", "abc"));

        GoodsManageVO actorGoods = new GoodsManageVO();
        actorGoods.setActorNo(null);
        invoke("normalizeActorNo", actorGoods);
        assertNull(actorGoods.getActorNo());
        actorGoods.setActorNo(0L);
        invoke("normalizeActorNo", actorGoods);
        assertNull(actorGoods.getActorNo());
        actorGoods.setActorNo(2L);
        invoke("normalizeActorNo", actorGoods);
        assertEquals(Long.valueOf(2L), actorGoods.getActorNo());

        actorGoods.setContentNo(10L);
        actorGoods.setActorNo(null);
        assertDoesNotThrow(() -> invoke("validateContentActor", actorGoods));
        actorGoods.setActorNo(2L);
        when(businessDAO.countContentActor(10L, 2L)).thenReturn(0);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateContentActor", actorGoods));
        when(businessDAO.countContentActor(10L, 2L)).thenReturn(1);
        assertDoesNotThrow(() -> invoke("validateContentActor", actorGoods));

        assertEquals("", invoke("getFileExtension", "filename"));
        assertEquals("", invoke("getFileExtension", "filename."));
        assertEquals("png", invoke("getFileExtension", "IMAGE.PNG"));
    }

    @Test
    void productImageValidationShouldCoverNullEmptySizeNameExtensionContentTypeAndValidImage() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductImage", new Object[] { null }));

        MultipartFile empty = image(true, 0L, "a.png", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductImage", empty));

        MultipartFile tooLarge = image(false, 10L * 1024L * 1024L + 1L, "a.png", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductImage", tooLarge));

        MultipartFile nullName = image(false, 10L, null, "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductImage", nullName));

        MultipartFile blankName = image(false, 10L, "   ", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductImage", blankName));

        MultipartFile badExtension = image(false, 10L, "file.exe", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductImage", badExtension));

        MultipartFile badType = image(false, 10L, "file.png", "text/plain");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateProductImage", badType));

        MultipartFile nullType = image(false, 10L, "file.png", null);
        assertDoesNotThrow(() -> invoke("validateProductImage", nullType));

        MultipartFile valid = image(false, 10L, "file.webp", "image/webp");
        assertDoesNotThrow(() -> invoke("validateProductImage", valid));
    }

    private MultipartFile image(
            boolean empty,
            long size,
            String filename,
            String contentType) {

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(empty);
        when(file.getSize()).thenReturn(size);
        when(file.getOriginalFilename()).thenReturn(filename);
        when(file.getContentType()).thenReturn(contentType);
        return file;
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    @SuppressWarnings("unchecked")
    private Set<String> castSet(Object value) {
        return (Set<String>) value;
    }
}
