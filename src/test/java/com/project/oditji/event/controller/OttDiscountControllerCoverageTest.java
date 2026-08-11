package com.project.oditji.event.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.common.vo.PageVO;
import com.project.oditji.event.service.OttDiscountService;
import com.project.oditji.event.vo.OttDiscountVO;

/** 사용자 OTT 할인 화면의 페이징 목록과 히어로 데이터 구성을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class OttDiscountControllerCoverageTest {

    @Mock
    private OttDiscountService ottDiscountService;

    private OttDiscountController controller;

    @BeforeEach
    void setUp() {
        controller = new OttDiscountController(ottDiscountService);
    }

    @Test
    void pageShouldClampRequestedPageAndExposeListHeroAndFilters() {
        OttDiscountVO item = new OttDiscountVO();
        OttDiscountVO hero = new OttDiscountVO();
        when(ottDiscountService.getDiscountListCount("WAVVE", "MEMBERSHIP"))
                .thenReturn(19);
        when(ottDiscountService.getDiscountList("WAVVE", "MEMBERSHIP", 3, 9))
                .thenReturn(List.of(item));
        when(ottDiscountService.getHeroDiscount("WAVVE", "MEMBERSHIP"))
                .thenReturn(hero);

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.ottDiscountPage("WAVVE", "MEMBERSHIP", 99, model);

        assertEquals("event/ottDiscount", view);
        assertEquals(List.of(item), model.get("discountList"));
        assertSame(hero, model.get("heroItem"));
        assertEquals("WAVVE", model.get("selectedPlatform"));
        assertEquals("MEMBERSHIP", model.get("selectedCategory"));

        PageVO pageVO = (PageVO) model.get("pageVO");
        assertEquals(3, pageVO.getCurrentPage());
        assertEquals(9, pageVO.getPageSize());
        assertEquals(19, pageVO.getTotalCount());
        assertEquals(3, pageVO.getTotalPage());

        verify(ottDiscountService).getDiscountList("WAVVE", "MEMBERSHIP", 3, 9);
        verify(ottDiscountService).getHeroDiscount("WAVVE", "MEMBERSHIP");
    }
}
