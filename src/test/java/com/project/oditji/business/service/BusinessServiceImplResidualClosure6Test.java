package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
 * 레거시 옵션 등록 helper에 남아 있는 복합 OR 조건을 각 피연산자 위치별로 보완합니다.
 */
class BusinessServiceImplResidualClosure6Test {

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
                "uploads/product",
                "uploads/event");
    }

    @Test
    void legacyOptionValidatorShouldReachEveryInvalidFieldOperand() {
        assertInvalidOption(null);
        assertInvalidOption(option(null, "M", 1));
        assertInvalidOption(option("   ", "M", 1));
        assertInvalidOption(option("RED", null, 1));
        assertInvalidOption(option("RED", "   ", 1));
        assertInvalidOption(option("RED", "M", null));
        assertInvalidOption(option("RED", "M", -1));

        verifyNoInteractions(businessDAO);
    }

    @Test
    void legacyOptionListGuardShouldCoverNullAndNonEmptyFalsePath() {
        GoodsManageVO nullOptions = goods(10L, "CLOTHES");
        nullOptions.setOptionList(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndInsertProductOptions", nullOptions));

        GoodsManageVO valid = goods(11L, "SHOES");
        valid.setOptionList(List.of(option(" BLACK ", " 270 ", 2)));
        when(businessDAO.insertProductOption(any(ProductOptionVO.class))).thenReturn(1);

        assertDoesNotThrow(() -> invoke("validateAndInsertProductOptions", valid));
        assertEquals(2, valid.getStock());
        assertEquals("BLACK", valid.getOptionList().get(0).getColorName());
        assertEquals("270", valid.getOptionList().get(0).getSizeName());
    }

    @Test
    void optionTotalStockGuardShouldCoverShoeNullListAndFilteredNullValues() {
        GoodsManageVO shoesWithoutOptions = goods(20L, "SHOES");
        shoesWithoutOptions.setStock(7);
        ReflectionTestUtils.setField(shoesWithoutOptions, "optionList", null);
        invoke("applyOptionTotalStock", shoesWithoutOptions);
        assertEquals(7, shoesWithoutOptions.getStock());

        GoodsManageVO nonOption = goods(21L, "GOODS");
        nonOption.setStock(8);
        nonOption.setOptionList(List.of(option("RED", "M", 5)));
        invoke("applyOptionTotalStock", nonOption);
        assertEquals(8, nonOption.getStock());

        GoodsManageVO clothes = goods(22L, "CLOTHES");
        List<ProductOptionVO> options = new ArrayList<ProductOptionVO>();
        options.add(null);
        options.add(option("RED", "M", null));
        options.add(option("BLUE", "L", 3));
        clothes.setOptionList(options);

        invoke("applyOptionTotalStock", clothes);
        assertEquals(3, clothes.getStock());
    }

    private void assertInvalidOption(ProductOptionVO option) {
        GoodsManageVO goods = goods(1L, "CLOTHES");
        List<ProductOptionVO> options = new ArrayList<ProductOptionVO>();
        options.add(option);
        goods.setOptionList(options);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndInsertProductOptions", goods));
    }

    private GoodsManageVO goods(long productNo, String productType) {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(productNo);
        goods.setProductType(productType);
        return goods;
    }

    private ProductOptionVO option(String color, String size, Integer stock) {
        ProductOptionVO option = new ProductOptionVO();
        option.setColorName(color);
        option.setSizeName(size);
        option.setStock(stock);
        return option;
    }

    private void invoke(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
