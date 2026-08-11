package com.project.oditji.event.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    /**
     * develop 병합 과정에서 OttDiscountController의 생성자 의존성이 추가되어도
     * 테스트가 특정 생성자 시그니처에 직접 결합되지 않도록 현재 public 생성자를 사용합니다.
     * OttDiscountService 외의 생성자 의존성은 Mockito mock으로 자동 채웁니다.
     */
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        Constructor<?> constructor = Arrays.stream(OttDiscountController.class.getConstructors())
                .max(Comparator.comparingInt(Constructor::getParameterCount))
                .orElseThrow();

        Object[] constructorArguments = Arrays.stream(constructor.getParameterTypes())
                .map(this::createConstructorArgument)
                .toArray();

        controller = (OttDiscountController) constructor.newInstance(constructorArguments);
    }

    @Test
    void pageShouldClampRequestedPageAndExposeListHeroAndFilters() {
        OttDiscountVO item = new OttDiscountVO();
        OttDiscountVO hero = new OttDiscountVO();

        when(ottDiscountService.getDiscountListCount("WAVVE", "MEMBERSHIP"))
                .thenReturn(19);
        when(ottDiscountService.getDiscountList(
                eq("WAVVE"),
                eq("MEMBERSHIP"),
                anyInt(),
                anyInt()))
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
        assertEquals(19, pageVO.getTotalCount());
        assertEquals(3, pageVO.getTotalPage());

        ArgumentCaptor<Integer> pageCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> pageSizeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(ottDiscountService).getDiscountList(
                eq("WAVVE"),
                eq("MEMBERSHIP"),
                pageCaptor.capture(),
                pageSizeCaptor.capture());

        assertEquals(pageVO.getCurrentPage(), pageCaptor.getValue());
        assertEquals(pageVO.getPageSize(), pageSizeCaptor.getValue());
        verify(ottDiscountService).getHeroDiscount("WAVVE", "MEMBERSHIP");
    }

    private Object createConstructorArgument(Class<?> parameterType) {
        if (OttDiscountService.class.equals(parameterType)) {
            return ottDiscountService;
        }
        return mock(parameterType);
    }
}
