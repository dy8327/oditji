package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.goods.vo.ProductOptionVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 상품 등록·수정 및 이벤트 재승인 요청의 성공·실패 분기를 추가로 검증합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessServiceImplMutationCoverageTest {

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
                tempDirectory.resolve("products").toString(),
                tempDirectory.resolve("events").toString());
    }

    @Test
    void simpleCountMethodsShouldValidateBusinessAndNormalizeKeyword() {
        when(businessDAO.selectBusinessOrderListCount(10L)).thenReturn(7);
        when(businessDAO.selectBusinessSalesHistoryCount(
                10L,
                LocalDate.of(2026, Month.AUGUST, 1),
                LocalDate.of(2026, Month.AUGUST, 31))).thenReturn(12);
        when(businessDAO.selectProductListCountByBusinessNo(10L, "상품")).thenReturn(3);
        when(businessDAO.selectEventListCountByBusinessNo(10L, null)).thenReturn(4);

        assertEquals(7, service.getBusinessOrderListCount(10L));
        assertEquals(
                12,
                service.getBusinessSalesHistoryCount(
                        10L,
                        LocalDate.of(2026, Month.AUGUST, 1),
                        LocalDate.of(2026, Month.AUGUST, 31)));
        assertEquals(3, service.getProductListCountByBusinessNo(10L, " 상품 "));
        assertEquals(4, service.getEventListCountByBusinessNo(10L, " "));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBusinessOrderListCount(0L));
        LocalDate invalidStartDate = LocalDate.of(2026, Month.AUGUST, 2);
        LocalDate invalidEndDate = LocalDate.of(2026, Month.AUGUST, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBusinessSalesHistoryCount(
                        10L,
                        invalidStartDate,
                        invalidEndDate));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getProductListCountByBusinessNo(-1L, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getEventListCountByBusinessNo(0L, null));
    }

    @Test
    void registerProductShouldSaveLegacyContentImageAndNotification() throws Exception {
        GoodsManageVO goods = validProduct("ETC");
        goods.setContentNo(100L);
        when(businessDAO.selectContentByNo(100L)).thenReturn(new ContentSearchVO());
        when(businessDAO.insertProduct(goods)).thenAnswer(invocation -> {
            GoodsManageVO target = invocation.getArgument(0);
            target.setProductNo(500L);
            return 1;
        });
        when(businessDAO.insertProductImage(goods)).thenReturn(1);
        MockMultipartFile image = image("main.png", "image/png");

        long productNo = service.registerProduct(goods, image, null);

        assertEquals(500L, productNo);
        assertEquals("WAITING", goods.getStatus());
        assertEquals("Y", goods.getIsMain());
        assertNotNull(goods.getImagePath());
        assertTrue(goods.getImagePath().startsWith("/uploads/product/"));
        try (var files = Files.list(tempDirectory.resolve("products"))) {
            assertTrue(files.findAny().isPresent());
        }
        verify(notificationService).createForAdmins(
                "PRODUCT_REQUEST",
                "상품 승인 요청",
                "테스트 상품 상품의 등록 승인 요청이 접수되었습니다.",
                "/admin/product/list?tab=waiting",
                "PRODUCT",
                500L);
    }

    @Test
    void registerClothesShouldTrimOptionsCalculateStockAndSaveEveryCombination() {
        GoodsManageVO goods = validProduct(" clothes ");
        goods.setContentNo(101L);
        ProductOptionVO first = option(" Black ", " M ", 2);
        ProductOptionVO second = option("White", "L", 3);
        goods.setOptionList(List.of(first, second));
        when(businessDAO.selectContentByNo(101L)).thenReturn(new ContentSearchVO());
        when(businessDAO.insertProduct(goods)).thenAnswer(invocation -> {
            GoodsManageVO target = invocation.getArgument(0);
            target.setProductNo(501L);
            return 1;
        });
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(1);
        when(businessDAO.insertProductImage(goods)).thenReturn(1);

        assertEquals(501L, service.registerProduct(goods, image("clothes.jpg", "image/jpeg"), null));
        assertEquals("CLOTHES", goods.getProductType());
        assertEquals(5, goods.getStock());
        assertEquals("Black", first.getColorName());
        assertEquals("M", first.getSizeName());
        assertEquals(501L, first.getProductNo());
        verify(businessDAO).insertProductOption(first);
        verify(businessDAO).insertProductOption(second);
    }

    @Test
    void registerProductShouldRejectMissingContentInvalidOptionsAndDatabaseFailures() {
        MockMultipartFile mainImage = image("main.png", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(null, mainImage, null));

        GoodsManageVO noContent = validProduct("ETC");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(noContent, mainImage, null));

        GoodsManageVO noImage = validProduct("ETC");
        noImage.setContentNo(1L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(noImage, null, null));

        GoodsManageVO duplicateOptions = validProduct("CLOTHES");
        duplicateOptions.setContentNo(2L);
        duplicateOptions.setOptionList(List.of(
                option("Black", "M", 1),
                option(" black ", " m ", 2)));
        when(businessDAO.selectContentByNo(2L)).thenReturn(new ContentSearchVO());
        when(businessDAO.insertProduct(duplicateOptions)).thenAnswer(invocation -> {
            GoodsManageVO target = invocation.getArgument(0);
            target.setProductNo(600L);
            return 1;
        });
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(1);
        MockMultipartFile duplicateImage = image("duplicate.png", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(
                        duplicateOptions,
                        duplicateImage,
                        null));

        GoodsManageVO insertFailure = validProduct("ETC");
        insertFailure.setContentNo(3L);
        when(businessDAO.selectContentByNo(3L)).thenReturn(new ContentSearchVO());
        when(businessDAO.insertProduct(insertFailure)).thenReturn(0);
        MockMultipartFile failureImage = image("failure.png", "image/png");
        assertThrows(
                IllegalStateException.class,
                () -> service.registerProduct(
                        insertFailure,
                        failureImage,
                        null));
    }

    @Test
    void updateProductShouldPersistRequestWithoutNewImage() {
        GoodsManageVO input = validProduct("ETC");
        input.setProductNo(700L);
        input.setActorNo(0L);
        GoodsManageVO existing = new GoodsManageVO();
        existing.setStatus("APPROVED");
        when(businessDAO.selectProductForUpdate(700L, 10L)).thenReturn(existing);
        when(businessDAO.updateProduct(input)).thenReturn(1);

        service.updateProduct(input, null);

        assertEquals("WAITING", input.getStatus());
        assertNull(input.getActorNo());
        verify(businessDAO).deleteProductOptionsByProductNo(700L);
        verify(businessDAO, never()).updateProductMainImage(any(GoodsManageVO.class));
        verify(notificationService).createForAdmins(
                "PRODUCT_REQUEST",
                "상품 재승인 요청",
                "테스트 상품 상품의 수정 승인 요청이 접수되었습니다.",
                "/admin/product/list?tab=waiting",
                "PRODUCT",
                700L);
    }

    @Test
    void updateProductShouldReplaceOptionsAndInsertImageWhenMainImageIsMissing() {
        GoodsManageVO input = validProduct("SHOES");
        input.setProductNo(701L);
        ProductOptionVO option = option("Blue", "270", 4);
        input.setOptionList(List.of(option));
        GoodsManageVO existing = new GoodsManageVO();
        existing.setStatus("APPROVED");
        when(businessDAO.selectProductForUpdate(701L, 10L)).thenReturn(existing);
        when(businessDAO.updateProduct(input)).thenReturn(1);
        when(businessDAO.insertProductOption(option)).thenReturn(1);
        when(businessDAO.updateProductMainImage(input)).thenReturn(0);
        when(businessDAO.insertProductImage(input)).thenReturn(1);

        service.updateProduct(input, image("updated.webp", "image/webp"));

        assertEquals(4, input.getStock());
        assertEquals(701L, option.getProductNo());
        assertTrue(input.getImagePath().endsWith(".webp"));
        verify(businessDAO).insertProductImage(input);
    }

    @Test
    void updateProductShouldRejectOwnershipStateValidationAndPersistenceFailures() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(null, null));

        GoodsManageVO invalidNo = validProduct("ETC");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(invalidNo, null));

        GoodsManageVO missing = validProduct("ETC");
        missing.setProductNo(800L);
        when(businessDAO.selectProductForUpdate(800L, 10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(missing, null));

        GoodsManageVO deleteRequested = validProduct("ETC");
        deleteRequested.setProductNo(801L);
        GoodsManageVO deleteState = new GoodsManageVO();
        deleteState.setStatus("DELETE_REQUESTED");
        when(businessDAO.selectProductForUpdate(801L, 10L)).thenReturn(deleteState);
        assertThrows(
                IllegalStateException.class,
                () -> service.updateProduct(deleteRequested, null));

        GoodsManageVO updateFailure = validProduct("ETC");
        updateFailure.setProductNo(802L);
        GoodsManageVO approved = new GoodsManageVO();
        approved.setStatus("APPROVED");
        when(businessDAO.selectProductForUpdate(802L, 10L)).thenReturn(approved);
        when(businessDAO.updateProduct(updateFailure)).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> service.updateProduct(updateFailure, null));
    }

    @Test
    void updateApprovedEventShouldKeepOldBannerReplaceProductsAndNotifyAdmins() {
        EventManageVO existing = validEvent(900L);
        existing.setBannerImage("/uploads/event/old.png");
        when(businessDAO.selectApprovedEventForBusiness(900L, 10L)).thenReturn(existing);
        when(businessDAO.selectEventProductListByEventNo(900L)).thenReturn(List.of());

        EventManageVO input = validEvent(900L);
        input.setTitle(" 수정 이벤트 ");
        input.setProductNoList(List.of(100L, 101L));
        input.setDiscountRateList(List.of(10, 20));
        when(businessDAO.countProductByBusinessNo(100L, 10L, 900L)).thenReturn(1);
        when(businessDAO.countProductByBusinessNo(101L, 10L, 900L)).thenReturn(1);
        when(businessDAO.updateApprovedEvent(input)).thenReturn(1);
        when(businessDAO.deleteEventProductByEventNo(900L)).thenReturn(2);
        when(businessDAO.insertEventProduct(eq(900L), anyLong(), anyInt()))
                .thenReturn(1);

        service.updateApprovedEvent(input, null);

        assertEquals("수정 이벤트", input.getTitle());
        assertEquals("WAITING", input.getStatus());
        assertEquals("/uploads/event/old.png", input.getBannerImage());
        verify(businessDAO).insertEventProduct(900L, 100L, 10);
        verify(businessDAO).insertEventProduct(900L, 101L, 20);
        verify(notificationService).createForAdmins(
                "EVENT_REQUEST",
                "이벤트 재승인 요청",
                "수정 이벤트 이벤트의 수정 승인 요청이 접수되었습니다.",
                "/admin/event/list?tab=waiting",
                "EVENT",
                900L);
    }

    @Test
    void updateApprovedEventShouldValidateInputProductListsAndDatabaseResults() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateApprovedEvent(null, null));

        EventManageVO missing = validEvent(901L);
        when(businessDAO.selectApprovedEventForBusiness(901L, 10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateApprovedEvent(missing, null));

        EventManageVO duplicate = validEvent(902L);
        duplicate.setProductNoList(List.of(100L, 100L));
        duplicate.setDiscountRateList(List.of(10, 20));
        when(businessDAO.selectApprovedEventForBusiness(902L, 10L)).thenReturn(validEvent(902L));
        when(businessDAO.selectEventProductListByEventNo(902L)).thenReturn(List.of());
        when(businessDAO.countProductByBusinessNo(100L, 10L, 902L)).thenReturn(1);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateApprovedEvent(duplicate, null));

        EventManageVO updateFailure = validEvent(903L);
        when(businessDAO.selectApprovedEventForBusiness(903L, 10L)).thenReturn(validEvent(903L));
        when(businessDAO.selectEventProductListByEventNo(903L)).thenReturn(List.of());
        when(businessDAO.countProductByBusinessNo(100L, 10L, 903L)).thenReturn(1);
        when(businessDAO.updateApprovedEvent(updateFailure)).thenReturn(0);
        assertThrows(
                IllegalStateException.class,
                () -> service.updateApprovedEvent(updateFailure, null));
    }

    private GoodsManageVO validProduct(String type) {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setBusinessNo(10L);
        goods.setProductName(" 테스트 상품 ");
        goods.setProductType(type);
        goods.setPrice(10000L);
        goods.setDiscountRate(10);
        goods.setStock(5);
        goods.setDescription(" 설명 ");
        return goods;
    }

    private ProductOptionVO option(String color, String size, int stock) {
        ProductOptionVO option = new ProductOptionVO();
        option.setColorName(color);
        option.setSizeName(size);
        option.setStock(stock);
        return option;
    }

    private EventManageVO validEvent(Long eventNo) {
        EventManageVO event = new EventManageVO();
        event.setEventNo(eventNo);
        event.setBusinessNo(10L);
        event.setTitle("테스트 이벤트");
        event.setStartDate(LocalDate.of(2026, Month.AUGUST, 1));
        event.setEndDate(LocalDate.of(2026, Month.AUGUST, 31));
        event.setStatus("APPROVED");
        event.setProductNoList(List.of(100L));
        event.setDiscountRateList(List.of(10));
        return event;
    }

    private MockMultipartFile image(String filename, String contentType) {
        return new MockMultipartFile(
                "image",
                filename,
                contentType,
                "image-data".getBytes(StandardCharsets.UTF_8));
    }
}