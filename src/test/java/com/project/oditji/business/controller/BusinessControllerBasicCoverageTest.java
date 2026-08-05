package com.project.oditji.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessDashboardVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.refund.service.OrderCancelRefundService;
import com.project.oditji.member.vo.MemberVO;

/** 사업자 기본 화면, 상품 등록 및 콘텐츠 검색 API의 주요 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessControllerBasicCoverageTest {

    @Mock
    private BusinessService businessService;

    @Mock
    private OrderCancelRefundService orderCancelRefundService;

    private BusinessController controller;

    @BeforeEach
    void setUp() {
        controller = new BusinessController(
                businessService,
                orderCancelRefundService);
    }

    @Test
    void mainShouldRedirectGuestAndMemberWithoutBusiness() {
        RedirectAttributesModelMap guestRedirect = new RedirectAttributesModelMap();
        String guestView = controller.businessMain(
                new MockHttpSession(),
                new ExtendedModelMap(),
                guestRedirect);

        assertEquals("redirect:/member/login", guestView);
        assertEquals(
                "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.",
                guestRedirect.getFlashAttributes().get("errorMessage"));

        MockHttpSession memberSession = sessionWith("loginMemberNo", 10L);
        when(businessService.getBusinessByMemberNo(10L)).thenReturn(null);
        RedirectAttributesModelMap memberRedirect = new RedirectAttributesModelMap();
        String memberView = controller.businessMain(
                memberSession,
                new ExtendedModelMap(),
                memberRedirect);

        assertEquals("redirect:/", memberView);
        assertEquals(
                "로그인 회원과 연결된 사업자 정보가 없습니다.",
                memberRedirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void mainShouldLoadDashboardAndPopularProducts() {
        BusinessVO business = business(20L, "APPROVED");
        BusinessDashboardVO dashboard = new BusinessDashboardVO();
        List<GoodsManageVO> popular = List.of(new GoodsManageVO());
        when(businessService.getBusinessByMemberNo(10L)).thenReturn(business);
        when(businessService.getBusinessDashboard(20L)).thenReturn(dashboard);
        when(businessService.getPopularProducts(20L)).thenReturn(popular);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.businessMain(
                sessionWith("loginMemberNo", 10L),
                model,
                new RedirectAttributesModelMap());

        assertEquals("business/main/businessMain", view);
        assertSame(business, model.get("business"));
        assertSame(dashboard, model.get("businessMain"));
        assertEquals(popular, dashboard.getPopularProducts());
        assertEquals("main", model.get("activeMenu"));
    }

    @Test
    void productListShouldSupportLegacyStringMemberNumberAndCacheStandardKey() {
        BusinessVO business = business(30L, "APPROVED");
        List<GoodsManageVO> products = List.of(new GoodsManageVO());
        when(businessService.getBusinessByMemberNo(11L)).thenReturn(business);
        when(businessService.getProductListByBusinessNo(30L)).thenReturn(products);
        MockHttpSession session = sessionWith("memberNo", " 11 ");
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.productList(
                session,
                model,
                new RedirectAttributesModelMap());

        assertEquals("business/goods/productList", view);
        assertEquals(11L, session.getAttribute("loginMemberNo"));
        assertEquals(products, model.get("productList"));
        assertEquals("product", model.get("activeMenu"));
    }

    @Test
    void productRegisterShouldRequireApprovedBusiness() {
        when(businessService.getBusinessByMemberNo(12L))
                .thenReturn(business(31L, "WAITING"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.productRegister(
                sessionWith("loginMemberNo", 12L),
                new ExtendedModelMap(),
                redirect);

        assertEquals("redirect:/business/main", view);
        assertEquals(
                "승인된 사업자만 상품을 등록할 수 있습니다.",
                redirect.getFlashAttributes().get("errorMessage"));
    }

    @Test
    void productRegisterShouldCreateFormOnlyWhenMissing() {
        BusinessVO business = business(32L, "APPROVED");
        when(businessService.getBusinessByMemberNo(13L)).thenReturn(business);
        ExtendedModelMap emptyModel = new ExtendedModelMap();

        String firstView = controller.productRegister(
                sessionWith("loginMemberNo", 13L),
                emptyModel,
                new RedirectAttributesModelMap());

        assertEquals("business/goods/productRegister", firstView);
        assertTrue(emptyModel.get("productForm") instanceof GoodsManageVO);

        GoodsManageVO existing = new GoodsManageVO();
        ExtendedModelMap existingModel = new ExtendedModelMap();
        existingModel.addAttribute("productForm", existing);
        String secondView = controller.productRegister(
                sessionWith("loginMemberNo", 13L),
                existingModel,
                new RedirectAttributesModelMap());

        assertEquals("business/goods/productRegister", secondView);
        assertSame(existing, existingModel.get("productForm"));
    }

    @Test
    void productRegisterProcessShouldUseAuthenticatedBusinessAndNormalizeActor() {
        BusinessVO business = business(40L, "APPROVED");
        when(businessService.getBusinessByMemberNo(14L)).thenReturn(business);
        GoodsManageVO form = new GoodsManageVO();
        form.setActorNo(0L);
        MockMultipartFile image = new MockMultipartFile(
                "productImage",
                "product.png",
                "image/png",
                new byte[] { 1 });
        when(businessService.registerProduct(form, image)).thenReturn(99L);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.productRegisterProcess(
                form,
                image,
                sessionWith("loginMemberNo", 14L),
                redirect);

        assertEquals("redirect:/business/product/list", view);
        assertEquals(40L, form.getBusinessNo());
        assertNull(form.getActorNo());
        assertEquals(99L, redirect.getFlashAttributes().get("registeredProductNo"));
        assertTrue(redirect.getFlashAttributes().get("successMessage").toString()
                .contains("상품 등록 요청이 완료"));
    }

    @Test
    void productRegisterProcessShouldPreserveFormForDomainFailure() {
        BusinessVO business = business(41L, "APPROVED");
        when(businessService.getBusinessByMemberNo(15L)).thenReturn(business);
        GoodsManageVO form = new GoodsManageVO();
        when(businessService.registerProduct(form, null))
                .thenThrow(new IllegalArgumentException("상품명이 필요합니다."));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.productRegisterProcess(
                form,
                null,
                sessionWith("loginMemberNo", 15L),
                redirect);

        assertEquals("redirect:/business/product/register", view);
        assertEquals("상품명이 필요합니다.", redirect.getFlashAttributes().get("errorMessage"));
        assertSame(form, redirect.getFlashAttributes().get("productForm"));
    }

    @Test
    void contentSearchShouldSelectCachedOrDatabaseSource() {
        List<ContentSearchVO> cached = List.of(new ContentSearchVO());
        List<ContentSearchVO> database = List.of(new ContentSearchVO());
        when(businessService.getCachedContentList("movie")).thenReturn(cached);
        when(businessService.getContentList("drama")).thenReturn(database);

        ExtendedModelMap cachedModel = new ExtendedModelMap();
        ExtendedModelMap databaseModel = new ExtendedModelMap();
        String cachedView = controller.contentSearch("movie", "REGISTER", cachedModel);
        String databaseView = controller.contentSearch("drama", "database", databaseModel);

        assertEquals("business/goods/contentSearch", cachedView);
        assertEquals("register", cachedModel.get("mode"));
        assertEquals(cached, cachedModel.get("contentList"));
        assertEquals("business/goods/contentSearch", databaseView);
        assertEquals("database", databaseModel.get("mode"));
        assertEquals(database, databaseModel.get("contentList"));
    }

    @Test
    void contentApisShouldReturnSafeCollectionsAndDelegateValidRequests() {
        when(businessService.getContentList("none")).thenReturn(null);
        when(businessService.getActorPreview(100L, "MOVIE")).thenReturn(null);
        ContentSearchVO detail = new ContentSearchVO();
        when(businessService.getContentByNo(3L)).thenReturn(detail);
        List<ActorSearchVO> actors = List.of(new ActorSearchVO());
        when(businessService.getActorListByContentNo(3L)).thenReturn(actors);

        assertEquals(List.of(), controller.contentListApi("none"));
        assertEquals(List.of(), controller.contentActorPreviewApi(100L, "MOVIE"));
        assertSame(detail, controller.contentDetailApi(3L));
        assertEquals(List.of(), controller.actorListByContentApi(0L));
        assertEquals(actors, controller.actorListByContentApi(3L));
        verify(businessService, never()).getActorListByContentNo(0L);
    }

    @Test
    void loginMemberObjectShouldBeAcceptedAsSessionFallback() {
        MemberVO member = new MemberVO();
        member.setMemberNo(16L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        when(businessService.getBusinessByMemberNo(16L)).thenReturn(null);

        String view = controller.productList(
                session,
                new ExtendedModelMap(),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/", view);
        assertEquals(16L, session.getAttribute("loginMemberNo"));
    }

    private BusinessVO business(Long businessNo, String status) {
        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        business.setStatus(status);
        return business;
    }

    private MockHttpSession sessionWith(String key, Object value) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(key, value);
        return session;
    }
}
