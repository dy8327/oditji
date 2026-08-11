package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * 옵션 검증의 단락 조건과 재입고 알림 경계값을 세분화하여 보완합니다.
 */
class BusinessServiceImplOptionValidationCoverageTest {

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
    void optionProductAndKeyHelpersShouldCoverBothSides() {
        assertTrue(invokeBoolean("isOptionProduct", "CLOTHES"));
        assertTrue(invokeBoolean("isOptionProduct", "SHOES"));
        assertFalse(invokeBoolean("isOptionProduct", "GOODS"));

        assertEquals("|", invokeString("createProductOptionKey", null, null));
        assertEquals("RED|XL", invokeString("createProductOptionKey", " red ", " xl "));
        assertEquals("|M", invokeString("createProductOptionKey", null, "m"));
        assertEquals("BLUE|", invokeString("createProductOptionKey", "blue", null));
    }

    @Test
    void normalizeValidatorShouldRejectEachInvalidFieldAndNormalizeValidOption() {
        assertThrows(IllegalArgumentException.class,
                () -> invoke("normalizeAndValidateProductOption", null, 1L));

        ProductOptionVO colorNull = option(null, "M", 1);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("normalizeAndValidateProductOption", colorNull, 1L));

        ProductOptionVO colorBlank = option("   ", "M", 1);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("normalizeAndValidateProductOption", colorBlank, 1L));

        ProductOptionVO sizeNull = option("RED", null, 1);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("normalizeAndValidateProductOption", sizeNull, 1L));

        ProductOptionVO sizeBlank = option("RED", "   ", 1);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("normalizeAndValidateProductOption", sizeBlank, 1L));

        ProductOptionVO stockNull = option("RED", "M", null);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("normalizeAndValidateProductOption", stockNull, 1L));

        ProductOptionVO negativeStock = option("RED", "M", -1);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("normalizeAndValidateProductOption", negativeStock, 1L));

        ProductOptionVO valid = option(" red ", " m ", 0);
        assertDoesNotThrow(() -> invoke("normalizeAndValidateProductOption", valid, 77L));
        assertEquals("red", valid.getColorName());
        assertEquals("m", valid.getSizeName());
        assertEquals(77L, valid.getProductNo());
    }

    @Test
    void duplicateAndReplacementValidatorsShouldCoverSuccessAndFailure() {
        Set<String> keys = new HashSet<>();
        assertDoesNotThrow(() -> invoke("validateDuplicateProductOption", keys, "RED|M"));
        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateDuplicateProductOption", keys, "RED|M"));

        List<ProductOptionVO> emptyOptions = List.of();
        List<ProductOptionVO> validOptions = List.of(option("RED", "M", 1));

        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateReplacementOptionList", (Object) null));
        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateReplacementOptionList", emptyOptions));
        assertDoesNotThrow(() -> invoke("validateReplacementOptionList", validOptions));
    }

    @Test
    void optionRestockNotificationShouldCoverEveryBoundary() {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(90L);
        goods.setProductName("후드티");
        ProductOptionVO option = option("BLACK", "XL", 1);

        invoke("notifyOptionRestockIfNeeded", goods, option, 901L, 1, 3);
        invoke("notifyOptionRestockIfNeeded", goods, option, 901L, 0, 0);
        verify(notificationService, never()).createOptionRestockNotifications(
                90L, 901L, "후드티", "BLACK", "XL");

        invoke("notifyOptionRestockIfNeeded", goods, option, 901L, 0, 1);
        verify(notificationService).createOptionRestockNotifications(
                90L, 901L, "후드티", "BLACK", "XL");
    }

    @Test
    void legacyInsertValidatorShouldCoverNonOptionEmptyDuplicateFailureAndSuccess() {
        GoodsManageVO nonOption = goods(10L, "GOODS");
        assertDoesNotThrow(() -> invoke("validateAndInsertProductOptions", nonOption));

        GoodsManageVO empty = goods(11L, "CLOTHES");
        empty.setOptionList(List.of());
        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateAndInsertProductOptions", empty));

        GoodsManageVO invalid = goods(12L, "SHOES");
        invalid.setOptionList(List.of(option("", "270", 1)));
        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateAndInsertProductOptions", invalid));

        GoodsManageVO duplicate = goods(13L, "CLOTHES");
        duplicate.setOptionList(List.of(
                option("Red", "M", 1),
                option(" red ", " m ", 2)));
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(1);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateAndInsertProductOptions", duplicate));

        GoodsManageVO failed = goods(14L, "SHOES");
        failed.setOptionList(List.of(option("BLACK", "270", 1)));
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(0);
        assertThrows(IllegalStateException.class,
                () -> invoke("validateAndInsertProductOptions", failed));

        GoodsManageVO success = goods(15L, "CLOTHES");
        success.setOptionList(List.of(
                option(" BLACK ", " M ", 2),
                option("WHITE", "L", 3)));
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(1);
        assertDoesNotThrow(() -> invoke("validateAndInsertProductOptions", success));
        assertEquals(5, success.getStock());
    }

    private GoodsManageVO goods(long productNo, String type) {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(productNo);
        goods.setProductType(type);
        return goods;
    }

    private ProductOptionVO option(String color, String size, Integer stock) {
        ProductOptionVO option = new ProductOptionVO();
        option.setColorName(color);
        option.setSizeName(size);
        option.setStock(stock);
        return option;
    }

    private boolean invokeBoolean(String methodName, Object... arguments) {
        Boolean result = ReflectionTestUtils.invokeMethod(service, methodName, arguments);
        return Boolean.TRUE.equals(result);
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }

    private void invoke(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
