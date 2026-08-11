package com.project.oditji.admin.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.event.service.OttDiscountService;
import com.project.oditji.event.vo.OttDiscountVO;

/** 관리자 OTT 할인 CRUD와 필터 유지 리다이렉트 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class AdminOttDiscountControllerCoverageTest {

    @Mock
    private OttDiscountService ottDiscountService;

    private AdminOttDiscountController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminOttDiscountController(ottDiscountService);
    }

    @Test
    void discountListShouldExposeListAndSelectedFilters() {
        OttDiscountVO discount = new OttDiscountVO();
        when(ottDiscountService.getAdminDiscountList("NETFLIX", "CARD", "Y"))
                .thenReturn(List.of(discount));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.discountList("NETFLIX", "CARD", "Y", model);

        assertEquals("admin/discount/discountManage", view);
        assertEquals("discount", model.get("activeMenu"));
        assertEquals(List.of(discount), model.get("discountList"));
        assertEquals("NETFLIX", model.get("selectedPlatform"));
        assertEquals("CARD", model.get("selectedCategory"));
        assertEquals("Y", model.get("selectedStatus"));
    }

    @Test
    void registerSuccessShouldBuildCompleteVoAndKeepAllFilters() {
        when(ottDiscountService.createDiscount(any(OttDiscountVO.class)))
                .thenReturn(true);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String result = controller.register(
                "NETFLIX",
                "CARD",
                "넷플릭스 카드 할인",
                17000,
                12000,
                "월 5천원 할인",
                "상세 설명",
                "테스트카드",
                "https://example.com/discount",
                "BEST",
                "2026-08-01",
                "2026-08-31",
                "NETFLIX",
                "CARD",
                "Y",
                redirect);

        assertEquals(
                "redirect:/admin/discount/list?platform=NETFLIX&category=CARD&status=Y",
                result);
        assertEquals("할인 정보를 등록했습니다.", redirect.getFlashAttributes().get("message"));

        ArgumentCaptor<OttDiscountVO> captor = ArgumentCaptor.forClass(OttDiscountVO.class);
        verify(ottDiscountService).createDiscount(captor.capture());

        OttDiscountVO saved = captor.getValue();
        assertNull(saved.getDiscountId());
        assertEquals("NETFLIX", saved.getPlatformCode());
        assertEquals("넷플릭스", saved.getPlatformName());
        assertEquals("CARD", saved.getCategory());
        assertEquals("넷플릭스 카드 할인", saved.getTitle());
        assertEquals(17000, saved.getRegularPrice());
        assertEquals(12000, saved.getDiscountPrice());
        assertEquals("월 5천원 할인", saved.getDiscountSummary());
        assertEquals("상세 설명", saved.getDescription());
        assertEquals("테스트카드", saved.getCardOrCompany());
        assertEquals("https://example.com/discount", saved.getTargetUrl());
        assertEquals("BEST", saved.getBadgeText());
        assertEquals("2026-08-01", saved.getStartDate());
        assertEquals("2026-08-31", saved.getEndDate());
    }

    @Test
    void registerFailureShouldUseFallbackPlatformNameAndSkipNullFilters() {
        when(ottDiscountService.createDiscount(any(OttDiscountVO.class)))
                .thenReturn(false);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String result = controller.register(
                "UNKNOWN",
                "ETC",
                "기타 할인",
                null,
                null,
                "요약",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                redirect);

        assertEquals("redirect:/admin/discount/list", result);
        assertEquals("등록에 실패했습니다.", redirect.getFlashAttributes().get("message"));

        ArgumentCaptor<OttDiscountVO> captor = ArgumentCaptor.forClass(OttDiscountVO.class);
        verify(ottDiscountService).createDiscount(captor.capture());
        assertEquals("UNKNOWN", captor.getValue().getPlatformName());
    }

    @Test
    void registerExceptionShouldReturnSafeMessageAndSkipBlankFilters() {
        when(ottDiscountService.createDiscount(any(OttDiscountVO.class)))
                .thenThrow(new IllegalArgumentException("invalid"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String result = controller.register(
                "TVING", "CARD", "제목", 10000, 9000, "요약",
                null, null, null, null, null, null,
                " ", "", "   ", redirect);

        assertEquals("redirect:/admin/discount/list", result);
        assertEquals(
                "등록 중 오류가 발생했습니다. 입력값을 확인해 주세요.",
                redirect.getFlashAttributes().get("message"));
    }

    @Test
    void updateShouldCoverSuccessFailureAndExceptionMessages() {
        RedirectAttributesModelMap successRedirect = new RedirectAttributesModelMap();
        when(ottDiscountService.updateDiscount(any(OttDiscountVO.class)))
                .thenReturn(true);

        String success = callUpdate(successRedirect, "ALL", "CARD", "N");

        assertEquals(
                "redirect:/admin/discount/list?platform=ALL&category=CARD&status=N",
                success);
        assertEquals("할인 정보를 수정했습니다.", successRedirect.getFlashAttributes().get("message"));

        ArgumentCaptor<OttDiscountVO> captor = ArgumentCaptor.forClass(OttDiscountVO.class);
        verify(ottDiscountService).updateDiscount(captor.capture());
        assertEquals(7L, captor.getValue().getDiscountId());
        assertEquals("티빙", captor.getValue().getPlatformName());

        RedirectAttributesModelMap failureRedirect = new RedirectAttributesModelMap();
        when(ottDiscountService.updateDiscount(any(OttDiscountVO.class)))
                .thenReturn(false);

        String failure = callUpdate(failureRedirect, null, " ", null);

        assertEquals("redirect:/admin/discount/list", failure);
        assertEquals("수정 대상을 찾을 수 없습니다.", failureRedirect.getFlashAttributes().get("message"));

        RedirectAttributesModelMap exceptionRedirect = new RedirectAttributesModelMap();
        when(ottDiscountService.updateDiscount(any(OttDiscountVO.class)))
                .thenThrow(new IllegalStateException("db"));

        String exception = callUpdate(exceptionRedirect, "", null, " ");

        assertEquals("redirect:/admin/discount/list", exception);
        assertEquals(
                "수정 중 오류가 발생했습니다. 입력값을 확인해 주세요.",
                exceptionRedirect.getFlashAttributes().get("message"));
    }

    @Test
    void deactivateAndActivateShouldCoverBothResultBranches() {
        RedirectAttributesModelMap first = new RedirectAttributesModelMap();
        when(ottDiscountService.deleteDiscount(10L)).thenReturn(true);

        String deactivateSuccess = controller.deactivate(10L, "NETFLIX", "", "Y", first);

        assertEquals(
                "redirect:/admin/discount/list?platform=NETFLIX&status=Y",
                deactivateSuccess);
        assertEquals("할인 정보를 비활성화했습니다.", first.getFlashAttributes().get("message"));

        RedirectAttributesModelMap second = new RedirectAttributesModelMap();
        when(ottDiscountService.deleteDiscount(11L)).thenReturn(false);

        String deactivateFailure = controller.deactivate(11L, null, null, null, second);

        assertEquals("redirect:/admin/discount/list", deactivateFailure);
        assertEquals("비활성화 대상을 찾을 수 없습니다.", second.getFlashAttributes().get("message"));

        RedirectAttributesModelMap third = new RedirectAttributesModelMap();
        when(ottDiscountService.activateDiscount(20L)).thenReturn(true);

        String activateSuccess = controller.activate(20L, "", "CARD", "", third);

        assertEquals(
                "redirect:/admin/discount/list?category=CARD",
                activateSuccess);
        assertEquals("할인 정보를 다시 활성화했습니다.", third.getFlashAttributes().get("message"));

        RedirectAttributesModelMap fourth = new RedirectAttributesModelMap();
        when(ottDiscountService.activateDiscount(21L)).thenReturn(false);

        String activateFailure = controller.activate(21L, null, " ", null, fourth);

        assertEquals("redirect:/admin/discount/list", activateFailure);
        assertEquals("재활성화 대상을 찾을 수 없습니다.", fourth.getFlashAttributes().get("message"));
    }

    private String callUpdate(
            RedirectAttributesModelMap redirect,
            String filterPlatform,
            String filterCategory,
            String filterStatus) {

        return controller.update(
                7L,
                "TVING",
                "CARD",
                "티빙 할인",
                14000,
                10000,
                "할인 요약",
                "설명",
                "카드사",
                "https://example.com/tving",
                "추천",
                "2026-08-10",
                "2026-09-10",
                filterPlatform,
                filterCategory,
                filterStatus,
                redirect);
    }
}
