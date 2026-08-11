package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.goods.vo.ProductOptionVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;

/** 사업자 서비스의 배송/옵션/세부이미지/캐시 변환 helper 잔여 분기를 보완합니다. */
class BusinessServiceImplFinalHelperGapCoverageTest {

    @TempDir
    Path tempDirectory;

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
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void deliveryHelpersShouldCoverMissingExistingCreationAndSuccessfulSave() {
        long businessNo = 3L;
        long orderItemNo = 4L;

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "getBusinessDeliveryItem",
                        businessNo,
                        orderItemNo));

        DeliveryManageVO current = new DeliveryManageVO();
        when(businessDAO.selectBusinessDeliveryItem(businessNo, orderItemNo)).thenReturn(current);
        assertSame(
                current,
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getBusinessDeliveryItem",
                        businessNo,
                        orderItemNo));

        DeliveryManageVO created = ReflectionTestUtils.invokeMethod(
                service,
                "createDelivery",
                businessNo,
                orderItemNo,
                5L,
                "택배",
                "123",
                "SHIPPING");

        assertEquals(Long.valueOf(3L), created.getBusinessNo());
        assertEquals(Long.valueOf(4L), created.getOrderItemNo());
        assertEquals(Long.valueOf(5L), created.getOrderNo());
        assertEquals("SHIPPING", created.getStatus());

        when(businessDAO.mergeDelivery(created)).thenReturn(1);
        when(businessDAO.updateOrderItemDeliveryStatus(businessNo, orderItemNo, "SHIPPING")).thenReturn(1);

        ReflectionTestUtils.invokeMethod(
                service,
                "saveDelivery",
                created,
                businessNo,
                orderItemNo,
                "SHIPPING");

        verify(businessDAO).mergeDelivery(created);
    }

    @Test
    void optionHelpersShouldSumStocksBuildMapDeleteRemovedAndNormalizeKeys() {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductType("CLOTHES");
        goods.setStock(100);

        ProductOptionVO first = option(11L, " Black ", " M ", null);
        ProductOptionVO second = option(12L, "White", "L", 3);
        List<ProductOptionVO> options = new ArrayList<ProductOptionVO>();
        options.add(null);
        options.add(first);
        options.add(second);
        goods.setOptionList(options);

        ReflectionTestUtils.invokeMethod(service, "applyOptionTotalStock", goods);
        assertEquals(3, goods.getStock());

        Object rawMap = ReflectionTestUtils.invokeMethod(service, "createExistingOptionMap", List.of(first, second));
        Map<?, ?> optionMap = (Map<?, ?>) rawMap;
        assertSame(first, optionMap.get("BLACK|M"));
        assertSame(second, optionMap.get("WHITE|L"));

        ReflectionTestUtils.invokeMethod(service, "deleteProductOptions", List.of(first));
        verify(businessDAO).deleteProductOptionByOptionNo(11L);

        Set<Long> maintained = new HashSet<Long>();
        maintained.add(11L);
        ReflectionTestUtils.invokeMethod(service, "deleteRemovedProductOptions", List.of(first, second), maintained);
        verify(businessDAO).deleteProductOptionByOptionNo(12L);

        assertEquals("|", ReflectionTestUtils.invokeMethod(service, "createProductOptionKey", (Object) null, (Object) null));
        assertEquals("BLACK|M", ReflectionTestUtils.invokeMethod(service, "createProductOptionKey", " black ", " m "));
        assertEquals(Boolean.TRUE, ReflectionTestUtils.invokeMethod(service, "isOptionProduct", "SHOES"));
        assertNotEquals(Boolean.TRUE, ReflectionTestUtils.invokeMethod(service, "isOptionProduct", "GOODS"));
    }

    @Test
    void detailImageHelpersShouldSkipEmptyFilesAndSaveSelectedImage() {
        MockMultipartFile empty = new MockMultipartFile(
                "detail",
                "",
                "application/octet-stream",
                new byte[0]);
        MockMultipartFile image = new MockMultipartFile(
                "detail",
                "detail.PNG",
                "image/png",
                new byte[] { 1, 2, 3 });
        MockMultipartFile[] files = new MockMultipartFile[] { null, empty, image };

        assertEquals(
                Boolean.TRUE,
                ReflectionTestUtils.invokeMethod(service, "hasSelectedDetailImage", (Object) files));
        assertEquals(
                1,
                ((Integer) ReflectionTestUtils.invokeMethod(
                        service,
                        "countSelectedDetailImages",
                        (Object) files)).intValue());

        ReflectionTestUtils.invokeMethod(service, "validateDetailImages", (Object) files);

        when(businessDAO.insertProductImage(any(GoodsManageVO.class))).thenReturn(1);
        List<Path> savedPaths = new ArrayList<Path>();
        ReflectionTestUtils.invokeMethod(
                service,
                "saveDetailProductImages",
                10L,
                files,
                savedPaths);

        assertEquals(1, savedPaths.size());
        assertTrue(Files.exists(savedPaths.get(0)));
    }

    @Test
    void cachedContentHelpersShouldValidateSelectionMatchAndConvertMetadata() {
        String invalidType = "AUDIO";
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateCachedContentSelection",
                        1L,
                        invalidType));

        when(searchContentStore.findByTmdbIdAndContentType(2L, "MOVIE")).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateCachedContentSelection",
                        2L,
                        " movie "));

        CachedContentVO cached = new CachedContentVO();
        cached.setTmdbId(3L);
        cached.setContentType("TV");
        cached.setTitle("제목");
        cached.setOriginalTitle("Original");
        cached.setSearchText("actor keyword");
        cached.setPosterPath("/poster.jpg");
        cached.setGenreText("드라마");
        cached.setAgeRating("15세 이상 관람가");

        when(searchContentStore.findByTmdbIdAndContentType(3L, "TV")).thenReturn(cached);
        ReflectionTestUtils.invokeMethod(service, "validateCachedContentSelection", 3L, " tv ");

        assertEquals(
                Boolean.TRUE,
                ReflectionTestUtils.invokeMethod(service, "matchesCachedContent", cached, "actor"));
        assertNotEquals(
                Boolean.TRUE,
                ReflectionTestUtils.invokeMethod(service, "matchesCachedContent", cached, "missing"));

        ContentSearchVO converted = ReflectionTestUtils.invokeMethod(service, "convertToContentSearchVO", cached);
        assertEquals(Long.valueOf(3L), converted.getTmdbId());
        assertEquals("TV", converted.getContentType());
        assertEquals("제목", converted.getTitle());
    }

    private ProductOptionVO option(Long optionNo, String color, String size, Integer stock) {
        ProductOptionVO option = new ProductOptionVO();
        option.setOptionNo(optionNo);
        option.setColorName(color);
        option.setSizeName(size);
        option.setStock(stock);
        return option;
    }
}
