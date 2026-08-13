package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.goods.vo.ProductOptionVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/**
 * 상품 수정 시 OPTION_NO 유지, 신규/수정/삭제 및 옵션 재입고 알림 분기를 보완합니다.
 */
class BusinessServiceImplOptionReplacementCoverageTest {

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
                "uploads/product",
                "uploads/event");
    }

    @Test
    void nonOptionProductShouldDeleteEveryExistingOption() {
        GoodsManageVO goods = goods(100L, "GOODS", "일반 굿즈");
        ProductOptionVO first = option(11L, "RED", "M", 1);
        ProductOptionVO second = option(12L, "BLUE", "L", 2);
        when(businessDAO.selectProductOptionsByProductNo(100L))
                .thenReturn(List.of(first, second));

        invoke("replaceProductOptions", goods);

        verify(businessDAO).deleteProductOptionByOptionNo(11L);
        verify(businessDAO).deleteProductOptionByOptionNo(12L);
        verify(businessDAO, never()).insertProductOption(any(ProductOptionVO.class));
    }

    @Test
    void optionProductShouldUpdateInsertDeleteAndNotifyRestock() {
        GoodsManageVO goods = goods(200L, "CLOTHES", "후드티");
        ProductOptionVO updated = option(null, " red ", " m ", 3);
        ProductOptionVO inserted = option(999L, " green ", " s ", 2);
        goods.setOptionList(List.of(updated, inserted));

        ProductOptionVO existingMatched = option(21L, "RED", "M", 0);
        ProductOptionVO existingRemoved = option(22L, "BLUE", "L", 7);

        when(businessDAO.selectProductOptionsByProductNo(200L))
                .thenReturn(List.of(existingMatched, existingRemoved));
        when(businessDAO.updateProductOption(any(ProductOptionVO.class))).thenReturn(1);
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(1);

        invoke("replaceProductOptions", goods);

        assertEquals(5, goods.getStock());
        assertEquals(21L, updated.getOptionNo());
        assertEquals("red", updated.getColorName());
        assertEquals("m", updated.getSizeName());
        assertEquals(200L, updated.getProductNo());
        assertNull(inserted.getOptionNo());
        assertEquals(200L, inserted.getProductNo());

        verify(businessDAO).updateProductOption(updated);
        verify(businessDAO).insertProductOption(inserted);
        verify(businessDAO).deleteProductOptionByOptionNo(22L);
        verify(notificationService).createOptionRestockNotifications(
                200L, 21L, "후드티", "red", "m");
    }

    @Test
    void insertAndUpdateFailuresShouldPropagate() {
        GoodsManageVO insertGoods = goods(300L, "SHOES", "신발");
        insertGoods.setOptionList(List.of(option(null, "BLACK", "270", 1)));
        when(businessDAO.selectProductOptionsByProductNo(300L)).thenReturn(List.of());
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> invoke("replaceProductOptions", insertGoods));

        GoodsManageVO updateGoods = goods(301L, "CLOTHES", "셔츠");
        updateGoods.setOptionList(List.of(option(null, "WHITE", "L", 2)));
        when(businessDAO.selectProductOptionsByProductNo(301L))
                .thenReturn(List.of(option(31L, "WHITE", "L", 1)));
        when(businessDAO.updateProductOption(any(ProductOptionVO.class))).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> invoke("replaceProductOptions", updateGoods));
    }

    @Test
    void emptyReplacementOptionListShouldBeRejected() {
        GoodsManageVO goods = goods(400L, "CLOTHES", "재킷");
        goods.setOptionList(List.of());
        when(businessDAO.selectProductOptionsByProductNo(400L)).thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("replaceProductOptions", goods));
    }

    private GoodsManageVO goods(long productNo, String type, String name) {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(productNo);
        goods.setProductType(type);
        goods.setProductName(name);
        return goods;
    }

    private ProductOptionVO option(Long optionNo, String color, String size, Integer stock) {
        ProductOptionVO option = new ProductOptionVO();
        option.setOptionNo(optionNo);
        option.setColorName(color);
        option.setSizeName(size);
        option.setStock(stock);
        return option;
    }

    private void invoke(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
