package com.project.oditji.business.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.refund.service.OrderCancelRefundService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.business.vo.BusinessDashboardVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.common.vo.PageVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/business")
public class BusinessController {

        private final BusinessService businessService;
        private final OrderCancelRefundService orderCancelRefundService;
        private static final Logger log = LoggerFactory.getLogger(BusinessController.class);
        private static final String ATTR_ERROR_MESSAGE = "errorMessage";
        private static final String ATTR_SUCCESS_MESSAGE = "successMessage";
        private static final String LOGIN_MEMBER_ERROR_MESSAGE = "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.";
        private static final String BUSINESS_NOT_FOUND_MESSAGE = "로그인 회원과 연결된 사업자 정보가 없습니다.";
        private static final String REDIRECT_MEMBER_LOGIN = "redirect:/member/login";
        private static final String REDIRECT_HOME = "redirect:/";
        private static final String REDIRECT_BUSINESS_MAIN = "redirect:/business/main";
        private static final String REDIRECT_PRODUCT_LIST = "redirect:/business/product/list";
        private static final String REDIRECT_EVENT_REGISTER = "redirect:/business/event/register";
        private static final String REDIRECT_EVENT_LIST = "redirect:/business/event/list";
        private static final String MODEL_BUSINESS = "business";
        private static final String MODEL_ACTIVE_MENU = "activeMenu";
        private static final String MODEL_PRODUCT_LIST = "productList";
        private static final String MODEL_PRODUCT_FORM = "productForm";
        private static final String MODEL_EVENT = "event";
        private static final String MODEL_ORDER = "order";
        private static final String PARAM_KEYWORD = "keyword";
        private static final String STATUS_APPROVED = "APPROVED";
        private static final String ACTIVE_MENU_SETTLEMENT = "settlement";
        private static final String SESSION_LOGIN_MEMBER_NO = "loginMemberNo";
        private static final String ATTR_PAGINATION = "pagination";

        /*
         * [페이징 리팩터링 추가] 사업자 목록 화면 공용 페이징 설정.
         * 관리자 목록 화면(AdminController의 ADMIN_PAGE_SIZE/ADMIN_PAGE_BLOCK_SIZE)과
         * 동일하게 한 페이지 10건, 페이지 번호 5개 단위 블록으로 맞춘다.
         */
        private static final int BUSINESS_PAGE_SIZE = 10;
        private static final int BUSINESS_PAGE_BLOCK_SIZE = 5;

        /*
         * [페이징 리팩터링] 상품 목록처럼 항목 하나당 이미지/상세정보가 큰
         * 카드형 화면은 한 페이지에 너무 많이 나오면 스크롤이 길어지므로
         * 5건 단위로 별도 관리한다. (상품 목록/배송 관리/취소·환불 관리)
         * 표 형태로 한 줄씩 나오는 화면(이벤트 목록/주문 현황/판매 현황)은
         * 기존 BUSINESS_PAGE_SIZE(10건)를 그대로 사용한다.
         */
        private static final int BUSINESS_CARD_PAGE_SIZE = 5;

        public BusinessController(
                        BusinessService businessService,
                        OrderCancelRefundService orderCancelRefundService) {

                this.businessService = businessService;
                this.orderCancelRefundService = orderCancelRefundService;
        }

        /* 사업자 메인 */
        @GetMapping("/main")
        public String businessMain(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);
                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }
                model.addAttribute(MODEL_BUSINESS, business);
                /* 사업자 메인 대시보드 통계 */
                BusinessDashboardVO businessMain = businessService.getBusinessDashboard(business.getBusinessNo());
                /* 사업자 인기 상품 */
                businessMain.setPopularProducts(businessService.getPopularProducts(business.getBusinessNo()));
                model.addAttribute("businessMain", businessMain);
                model.addAttribute(MODEL_ACTIVE_MENU, "main");

                return "business/main/businessMain";
        }

        /*
         * 상품 목록
         *
         * [페이징 리팩터링] 관리자 목록 화면과 동일한 방식(PaginationUtil/PageVO)으로
         * 검색어(keyword) + 페이지(page)를 함께 처리한다.
         */
        @GetMapping("/product/list")
        public String productList(
                        @RequestParam(value = PARAM_KEYWORD, required = false) String keyword,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                int totalCount = businessService.getProductListCountByBusinessNo(business.getBusinessNo(), keyword);
                PageVO pagination = PaginationUtil.build(page, totalCount, BUSINESS_CARD_PAGE_SIZE,
                                BUSINESS_PAGE_BLOCK_SIZE);

                List<GoodsManageVO> productList = businessService.getProductListByBusinessNo(
                                business.getBusinessNo(), keyword, pagination.getCurrentPage(), BUSINESS_CARD_PAGE_SIZE);

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute(MODEL_PRODUCT_LIST, productList);
                model.addAttribute(PARAM_KEYWORD, keyword);
                model.addAttribute(ATTR_PAGINATION, pagination);
                model.addAttribute(MODEL_ACTIVE_MENU, "product");

                return "business/goods/productList";
        }

        /* 상품 등록 화면 */
        @GetMapping("/product/register")
        public String productRegister(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                if (!STATUS_APPROVED.equals(business.getStatus())) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "승인된 사업자만 상품을 등록할 수 있습니다.");

                        return REDIRECT_BUSINESS_MAIN;
                }

                /* 상품 등록 실패 후 다시 진입한 경우 RedirectAttributes로 전달된 productForm을 유지. */
                if (!model.containsAttribute(MODEL_PRODUCT_FORM)) {

                        model.addAttribute(MODEL_PRODUCT_FORM, new GoodsManageVO());
                }

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute(MODEL_ACTIVE_MENU, "productRegister");

                return "business/goods/productRegister";
        }

        /* 상품 등록 처리 */
        @PostMapping("/product/register")
        public String productRegisterProcess(
                        @ModelAttribute(MODEL_PRODUCT_FORM) GoodsManageVO goodsManageVO,

                        @RequestParam(value = "productImage", required = false) MultipartFile productImage,
                        HttpSession session, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                if (!STATUS_APPROVED.equals(business.getStatus())) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "승인된 사업자만 상품을 등록할 수 있습니다.");

                        return REDIRECT_BUSINESS_MAIN;
                }

                /*
                 * 화면에서 BUSINESS_NO가 전달되더라도 사용하지 않음.
                 * 현재 로그인 회원과 연결된 사업자 번호를 서버에서 설정.
                 */
                goodsManageVO.setBusinessNo(business.getBusinessNo());

                /* 배우 선택값이 없거나 0 이하이면 PRODUCT.ACTOR_NO에 NULL이 저장되도록 처리. */
                if (goodsManageVO.getActorNo() != null && goodsManageVO.getActorNo() <= 0) {

                        goodsManageVO.setActorNo(null);
                }

                try {
                        long productNo = businessService.registerProduct(goodsManageVO, productImage);

                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "상품 등록 요청이 완료되었습니다. "
                                        + "관리자 승인 후 판매됩니다.");
                        redirectAttributes.addFlashAttribute("registeredProductNo", productNo);

                        return REDIRECT_PRODUCT_LIST;

                } catch (IllegalArgumentException | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                        redirectAttributes.addFlashAttribute(MODEL_PRODUCT_FORM, goodsManageVO);

                        return "redirect:/business/product/register";

                } catch (Exception e) {
                if (log.isErrorEnabled()) {
                        log.error("상품 등록 처리 중 오류 발생", e);
                }
                redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "상품 등록 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                redirectAttributes.addFlashAttribute(MODEL_PRODUCT_FORM, goodsManageVO);

                return "redirect:/business/product/register";
                }
        }

        /*
         * 콘텐츠 검색 팝업 화면
         *
         * mode=register: JSONL 공용 저장소 검색
         * 그 외: 기존 DB CONTENT 검색
         */
        @GetMapping("/content/search")
        public String contentSearch(
                        @RequestParam(value = PARAM_KEYWORD, required = false) String keyword,
                        @RequestParam(value = "mode", required = false, defaultValue = "database") String mode,
                        Model model) {

                boolean registerMode =
                                "register".equalsIgnoreCase(mode);

                List<ContentSearchVO> contentList =
                                registerMode
                                                ? businessService.getCachedContentList(keyword)
                                                : businessService.getContentList(keyword);

                model.addAttribute(PARAM_KEYWORD, keyword);
                model.addAttribute("mode", registerMode
                                ? "register"
                                : "database");
                model.addAttribute("contentList", contentList);

                return "business/goods/contentSearch";
        }

        /*
         * =========================================================
         * 콘텐츠 검색 JSON API
         *
         * 요청 주소:
         * GET /business/api/content/list?keyword=검색어
         * =========================================================
         */
        @GetMapping("/api/content/list")
        @ResponseBody
        public List<ContentSearchVO> contentListApi(
                        @RequestParam(value = PARAM_KEYWORD, required = false, defaultValue = "") String keyword) {
                List<ContentSearchVO> contentList = businessService.getContentList(keyword);

                if (contentList == null) {
                        return Collections.emptyList();
                }

                return contentList;
        }

        /*
         * =========================================================
         * JSONL 콘텐츠의 배우 미리보기 JSON API
         *
         * 콘텐츠 선택 단계에서는 DB에 저장하지 않고 TMDB 정보만 반환합니다.
         * 실제 ACTOR/CONTENT_ACTOR 저장은 상품 등록 요청 시 수행됩니다.
         * =========================================================
         */
        @GetMapping("/api/content/actor-preview")
        @ResponseBody
        public List<ActorSearchVO> contentActorPreviewApi(
                        @RequestParam("tmdbId") Long tmdbId,
                        @RequestParam("contentType") String contentType) {

                List<ActorSearchVO> actorList =
                                businessService.getActorPreview(
                                                tmdbId,
                                                contentType);

                return actorList == null
                                ? Collections.emptyList()
                                : actorList;
        }

        /*
         * =========================================================
         * 콘텐츠 단건 조회 JSON API
         *
         * 요청 주소:
         * GET /business/api/content/detail?contentNo=1
         * =========================================================
         */
        @GetMapping("/api/content/detail")
        @ResponseBody
        public ContentSearchVO contentDetailApi(@RequestParam("contentNo") long contentNo) {

                return businessService.getContentByNo(contentNo);
        }

        /*
         * =========================================================
         * 선택 콘텐츠의 배우 목록 JSON API
         *
         * 요청 주소:
         * GET /business/api/actor/list?contentNo=1
         * =========================================================
         */
        @GetMapping("/api/actor/list")
        @ResponseBody
        public List<ActorSearchVO> actorListByContentApi(@RequestParam("contentNo") long contentNo) {


                if (contentNo <= 0) {
                        return Collections.emptyList();
                }

                return businessService.getActorListByContentNo(contentNo);
        }

        /*
         * =========================================================
         * 상품 수정 화면
         *
         * 상품 번호 없이 직접 접근한 경우에는
         * 상품 목록으로 이동.
         * =========================================================
         */
        /*
         * [리팩터링] 상품 수정 요청 페이지(GET /business/product/update)는 제거했다.
         * 상품 수정은 이제 productList.jsp 안의 단일 공용 수정 모달로만 진입한다.
         * 모달은 "수정 요청" 버튼의 data-* 속성(상품 목록 조회 시점에 이미 내려간
         * GoodsManageVO 값)을 JS로 읽어 채우므로, 클릭 시점에 별도로 상품 단건을
         * 다시 조회할 필요가 없다.
         */

        /*
         * =========================================================
         * 상품 수정 처리
         * =========================================================
         */
        @PostMapping("/product/update")
        public String productUpdateProcess(
                        @ModelAttribute(MODEL_PRODUCT_FORM) GoodsManageVO goodsManageVO,

                        @RequestParam(value = "productImage", required = false) MultipartFile productImage,

                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        ATTR_ERROR_MESSAGE,
                                        LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        ATTR_ERROR_MESSAGE,
                                        BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                if (!STATUS_APPROVED.equals(
                                business.getStatus())) {

                        redirectAttributes.addFlashAttribute(
                                        ATTR_ERROR_MESSAGE,
                                        "승인된 사업자만 상품을 수정할 수 있습니다.");

                        return REDIRECT_BUSINESS_MAIN;
                }

                /*
                 * 화면에서 넘어온 BUSINESS_NO는 신뢰하지 않고
                 * 로그인 회원과 연결된 사업자 번호로 다시 설정한다.
                 */
                goodsManageVO.setBusinessNo(
                                business.getBusinessNo());

                /*
                 * 배우 선택값이 없거나 0 이하이면
                 * PRODUCT.ACTOR_NO에 NULL이 저장되도록 처리한다.
                 */
                if (goodsManageVO.getActorNo() != null
                                && goodsManageVO.getActorNo() <= 0) {

                        goodsManageVO.setActorNo(
                                        null);
                }

                try {

                        businessService.updateProduct(
                                        goodsManageVO,
                                        productImage);

                        redirectAttributes.addFlashAttribute(
                                        ATTR_SUCCESS_MESSAGE,
                                        "상품 수정 요청이 완료되었습니다. "
                                                        + "관리자 승인 후 변경 내용이 반영됩니다.");

                        return REDIRECT_PRODUCT_LIST;

                } catch (IllegalArgumentException | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());

                        /*
                         * [리팩터링] 수정 폼이 productList.jsp 모달로 통합되면서
                         * 별도 GET /product/update 재표시 페이지가 없어졌다.
                         * 오류 시에도 목록으로 돌아가 상단 알림으로 안내한다.
                         * (입력값 유지 대신 모달을 다시 열어 값을 채워야 하므로,
                         *  입력값은 유지하지 않는다 - eventExtend와 동일한 처리 방식)
                         */
                        return REDIRECT_PRODUCT_LIST;

                } catch (Exception e) {
                if (log.isErrorEnabled()) {
                        log.error("상품 수정 처리 중 오류 - productNo: {}", goodsManageVO.getProductNo(), e);
                }
                redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "상품 수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

                return REDIRECT_PRODUCT_LIST;
                }
        }

        /*
         * =========================================================
         * 상품 삭제 요청 화면
         *
         * 상품 목록에서 전달받은 PRODUCT_NO로 상품 정보를 조회한다.
         * 로그인한 사업자가 등록한 상품만 조회할 수 있다.
         *
         * 요청 예:
         * /business/product/delete?productNo=1
         * =========================================================
         */
        /*
         * [리팩터링] 상품 삭제 요청 페이지(GET /business/product/delete)는 제거했다.
         * 삭제 요청은 이제 productList.jsp 안에서 상품마다 미리 렌더링된
         * 모달로만 진입한다. (아래 productList()에서 내려주는 productList
         * 데이터에 삭제 모달 표시에 필요한 값이 이미 모두 포함되어 있다.)
         */

        /*
         * =========================================================
         * 상품 삭제 요청 처리
         *
         * productList.jsp의 상품별 삭제 모달에서 전송되는
         * POST /business/product/delete 요청을 처리한다.
         *
         * 실제 상품 데이터를 바로 삭제하지 않고
         * PRODUCT.STATUS를 DELETE_REQUESTED로 변경한다.
         * =========================================================
         */
        @PostMapping("/product/delete")
        public String productDeleteProcess(@RequestParam("productNo") long productNo, @RequestParam("reason") String reason,
                        HttpSession session, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                if (!STATUS_APPROVED.equals(business.getStatus())) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "승인된 사업자만 상품 삭제를 요청할 수 있습니다.");

                        return REDIRECT_BUSINESS_MAIN;
                }

                try {
                        businessService.requestProductDelete(productNo, business.getBusinessNo(), reason);
                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "상품 삭제 요청이 완료되었습니다. " + "관리자 승인 후 최종 처리됩니다.");

                        return REDIRECT_PRODUCT_LIST;

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());

                        /*
                         * [리팩터링] 삭제 폼이 productList.jsp 모달로 통합되면서
                         * 별도 GET /product/delete 재표시 페이지가 없어졌다.
                         * 오류 시에도 목록으로 돌아가 상단 알림으로 안내한다.
                         */
                        return REDIRECT_PRODUCT_LIST;

                } catch (Exception e) {
                if (log.isErrorEnabled()) {
                        log.error("상품 삭제 요청 처리 중 오류 - productNo: {}", productNo, e);
                }
                redirectAttributes.addFlashAttribute(
                        ATTR_ERROR_MESSAGE,
                        "상품 삭제 요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

                return REDIRECT_PRODUCT_LIST;
                }
        }

        /*
         * =========================================================
         * 이벤트 목록
         *
         * 현재 로그인한 사업자가 상품을 연결하여 등록한 이벤트만 조회한다.
         * EVENT 테이블에는 BUSINESS_NO가 없으므로
         * EVENT_PRODUCT -> PRODUCT 경로로 사업자 소유권을 확인한다.
         * =========================================================
         */
        @GetMapping("/event/list")
        public String eventList(@RequestParam(value = PARAM_KEYWORD, required = false) String keyword,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);
                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                /* [페이징 리팩터링] 관리자 목록 화면과 동일한 방식으로 페이지 계산 후 조회한다. */
                int totalCount = businessService.getEventListCountByBusinessNo(business.getBusinessNo(), keyword);
                PageVO pagination = PaginationUtil.build(page, totalCount, BUSINESS_PAGE_SIZE, BUSINESS_PAGE_BLOCK_SIZE);

                List<EventManageVO> eventList = businessService.getEventListByBusinessNo(
                                business.getBusinessNo(), keyword, pagination.getCurrentPage(), BUSINESS_PAGE_SIZE);

                /*
                 * [리팩터링 추가] 이벤트 수정 요청 모달(공용 1개, #eventUpdateModal)이
                 * "수정" 버튼의 data-* 값으로 채워지는데, 목록 조회 쿼리(LISTAGG로
                 * 상품명을 한 줄로만 합쳐서 보여주는 요약용)에는 DESCRIPTION과
                 * 상품별 상세 연결 정보가 없다.
                 *
                 * 그래서 APPROVED 이벤트에 대해서만, 구 GET /business/event/update
                 * 화면 진입 시 쓰던 것과 동일한 조회(getApprovedEventForBusiness)를
                 * 재사용해서 description/connectedProducts만 보강해 넣는다.
                 * (신규 매퍼 쿼리를 추가하지 않고 기존 서비스 메서드만 재사용)
                 */
                for (EventManageVO event : eventList) {

                        if (STATUS_APPROVED.equals(event.getStatus())) {

                                EventManageVO eventDetail = businessService.getApprovedEventForBusiness(
                                                event.getEventNo(), business.getBusinessNo());

                                event.setDescription(eventDetail.getDescription());
                                event.setConnectedProducts(eventDetail.getConnectedProducts());
                        }
                }

                /*
                 * [리팩터링 추가] 수정 모달 안의 "이벤트 연결 상품 검색" 팝업에서 사용할
                 * 상품 목록. eventUpdate.jsp/eventRegister.jsp에서 쓰던 것과 동일한
                 * 서비스 메서드를 그대로 재사용한다.
                 */
                List<GoodsManageVO> productList = businessService.getApprovedProductListByBusinessNo(business.getBusinessNo());

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute(PARAM_KEYWORD, keyword);
                model.addAttribute("eventList", eventList);
                model.addAttribute(MODEL_PRODUCT_LIST, productList);
                model.addAttribute(ATTR_PAGINATION, pagination);
                model.addAttribute(MODEL_ACTIVE_MENU, MODEL_EVENT);

                return "business/event/eventList";
        }

        /*
         * =========================================================
         * 이벤트 등록
         *
         * 현재 로그인한 사업자가 등록한 상품 목록을 함께 조회하여
         * eventRegister.jsp 내부 상품 검색 모달에서 사용한다.
         * =========================================================
         */
        @GetMapping("/event/register")
        public String eventRegister(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                if (!STATUS_APPROVED.equals(business.getStatus())) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "승인된 사업자만 이벤트 등록을 요청할 수 있습니다.");

                        return REDIRECT_BUSINESS_MAIN;
                }

                /*
                 * 현재 로그인한 사업자가 등록한 상품만 조회한다.
                 * 별도 검색 JSP를 추가하지 않고 현재 이벤트 등록 화면의
                 * 상품 검색 모달에서 이 목록을 사용한다.
                 */
                List<GoodsManageVO> productList = businessService.getApprovedProductListByBusinessNo(business.getBusinessNo());

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute(MODEL_PRODUCT_LIST, productList);
                model.addAttribute(MODEL_ACTIVE_MENU, "eventRegister");

                return "business/event/eventRegister";
        }

        /*
         * =========================================================
         * 이벤트 등록 처리
         *
         * eventRegister.jsp의 form에서 전송되는
         * POST /business/event/register 요청을 처리한다.
         *
         * EVENT 테이블에 WAITING 상태로 이벤트 기본 정보를 저장하고,
         * EVENT_PRODUCT 테이블에 선택 상품 연결 정보를 저장한다.
         *
         * EVENT 테이블에는 BUSINESS_NO가 없으므로
         * 사업자별 조회를 위해 연결 상품 선택은 필수로 처리한다.
         *
         * [수정] EVENT 테이블에 DESCRIPTION 컬럼이 추가되어
         * description은 이제 EVENT.DESCRIPTION에 그대로 저장된다.
         * =========================================================
         */
        @PostMapping("/event/register")
        public String eventRegisterProcess(

                        @RequestParam("eventTitle") String eventTitle,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam("startDate") LocalDate startDate,
                        @RequestParam("endDate") LocalDate endDate,

                        // 선택한 연결 상품 목록
                        @RequestParam(value = "productNoList", required = false) List<Long> productNoList,
                        // 상품별 할인율 목록
                        @RequestParam(value = "discountRateList", required = false) List<Integer> discountRateList,
                        @RequestParam(value = "eventImage", required = false) MultipartFile eventImage,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                if (!STATUS_APPROVED.equals(business.getStatus())) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE,"승인된 사업자만 이벤트 등록을 요청할 수 있습니다.");

                        return REDIRECT_BUSINESS_MAIN;
                }

                if (hasInvalidDiscountRate(discountRateList)) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "이벤트 할인율은 0~100 사이로 입력해주세요.");

                        return REDIRECT_EVENT_REGISTER;
                }

                EventManageVO eventManageVO = new EventManageVO();

                eventManageVO.setBusinessNo(business.getBusinessNo());
                eventManageVO.setTitle(eventTitle);
                eventManageVO.setDescription(description);
                eventManageVO.setStartDate(startDate);
                eventManageVO.setEndDate(endDate);

                /*
                 * 이벤트 등록 요청은 반드시 관리자 승인을 거치므로
                 * 화면 전달값과 무관하게 WAITING 상태로 저장한다.
                 */
                eventManageVO.setStatus("WAITING");
                eventManageVO.setProductNoList(productNoList);
                eventManageVO.setDiscountRateList(discountRateList);

                try {
                        long eventNo = businessService.registerEvent(eventManageVO, eventImage);

                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "이벤트 등록 요청이 접수되었습니다.");
                        redirectAttributes.addFlashAttribute("registeredEventNo", eventNo);

                        return REDIRECT_EVENT_LIST;

                } catch (IllegalArgumentException | IllegalStateException e) {
                        if (log.isWarnEnabled()) {
                                log.warn("이벤트 등록 검증 실패: {}", e.getMessage());
                        }
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());

                        return REDIRECT_EVENT_REGISTER;

                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("이벤트 등록 처리 중 오류", e);
                        }
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "이벤트 등록 요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

                        return REDIRECT_EVENT_REGISTER;
                }
        }

        /*
         * =========================================================
         * [리팩터링] 이벤트 수정 화면(GET /business/event/update)은 제거했다.
         * 이벤트 수정은 이제 eventList.jsp 안의 공용 모달(#eventUpdateModal)
         * 하나로만 진입한다. "수정" 버튼의 data-* 값을 openEventUpdateModal(this)가
         * 채워 넣으므로 별도 화면 이동이 필요 없다.
         * =========================================================
         */

        /*
         * =========================================================
         * 이벤트 수정 처리
         *
         * APPROVED 상태 이벤트의 내용을 수정하고
         * EVENT.STATUS를 WAITING으로 변경한다.
         *
         * 관리자 승인 전에는 사용자 화면에서 노출되지 않고,
         * 승인 후 수정된 정보가 다시 노출된다.
         * =========================================================
         */
        @PostMapping("/event/update")
        public String eventUpdateProcess(
                        @RequestParam("eventNo") long eventNo,
                        @RequestParam("eventTitle") String eventTitle,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam("startDate") LocalDate startDate,
                        @RequestParam("endDate") LocalDate endDate,
                        @RequestParam(value = "productNoList", required = false) List<Long> productNoList,

                        //상품별 할인율 목록
                        @RequestParam(value = "discountRateList", required = false) List<Integer> discountRateList,
                        @RequestParam(value = "eventImage", required = false) MultipartFile eventImage,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                if (discountRateList != null) {
                        for (Integer rate : discountRateList) {
                                if (rate ==null || rate < 0 || rate > 100) {
                                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "이벤트 할인율은 0~100 사이로 입력해주세요.");

                                        return REDIRECT_EVENT_LIST;
                                }
                        }
                }

                EventManageVO eventManageVO = new EventManageVO();

                eventManageVO.setEventNo(eventNo);
                eventManageVO.setBusinessNo(business.getBusinessNo());
                eventManageVO.setTitle(eventTitle);
                eventManageVO.setDescription(description);
                eventManageVO.setStartDate(startDate);
                eventManageVO.setEndDate(endDate);

                /*
                 * 이벤트 수정 요청은 반드시 관리자 재승인을 거치므로
                 * 화면 전달값과 무관하게 WAITING 상태로 저장한다.
                 */
                eventManageVO.setStatus("WAITING");
                eventManageVO.setProductNoList(productNoList);
                eventManageVO.setDiscountRateList(discountRateList);

                try {
                        businessService.updateApprovedEvent(eventManageVO, eventImage);
                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "이벤트 수정 요청이 접수되었습니다. " + "관리자 승인 전까지 사용자 화면에 노출되지 않습니다.");

                        return REDIRECT_EVENT_LIST;

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute( ATTR_ERROR_MESSAGE, e.getMessage());

                        /*
                         * [리팩터링] 수정 폼이 eventList.jsp 모달로 통합되면서
                         * 별도 GET /event/update 재표시 페이지가 없어졌다.
                         * 오류 시에도 목록으로 돌아가 상단 알림으로 안내한다.
                         */
                        return REDIRECT_EVENT_LIST;

               } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("이벤트 수정 처리 중 오류 - eventNo: {}", eventNo, e);
                        }
                        redirectAttributes.addFlashAttribute(
                                ATTR_ERROR_MESSAGE,
                                "이벤트 수정 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

                        return REDIRECT_EVENT_LIST;
                }
        }

        /*
         * =========================================================
         * 이벤트 연장 화면
         *
         * 관리자 승인이 완료된 APPROVED 이벤트만 연장 요청할 수 있다.
         * =========================================================
         */
        /*
         * [리팩터링] 이벤트 연장 페이지(GET /business/event/extend)는 제거했다.
         * 이벤트 연장은 이제 eventList.jsp 안에서 APPROVED 이벤트마다
         * 미리 렌더링된 모달로만 진입한다.
         */

        /*
         * =========================================================
         * 이벤트 연장 처리
         *
         * APPROVED 상태 이벤트의 END_DATE를 변경하고
         * EVENT.STATUS를 WAITING으로 변경한다.
         *
         * 현재 연장 요청 테이블과 사유 컬럼이 없으므로
         * 연장 사유는 서버 콘솔 로그로만 확인한다.
         *
         * 관리자 승인 전에는 사용자 화면에서 노출되지 않고,
         * 승인 후 연장된 종료일로 다시 노출된다.
         * =========================================================
         */
        @PostMapping("/event/extend")
        public String eventExtendProcess(
                        @RequestParam("eventNo") long eventNo,
                        @RequestParam("extendEndDate") LocalDate extendEndDate,
                        @RequestParam("extendReason") String extendReason,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                try {
                        businessService.extendApprovedEvent(eventNo, business.getBusinessNo(), extendEndDate, extendReason);
                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "이벤트 연장 요청이 접수되었습니다. " + "관리자 승인 전까지 사용자 화면에 노출되지 않습니다.");

                        return REDIRECT_EVENT_LIST;

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());

                        /*
                         * [리팩터링] 연장 폼이 eventList.jsp 모달로 통합되면서
                         * 별도 GET /event/extend 재표시 페이지가 없어졌다.
                         * 오류 시에도 목록으로 돌아가 상단 알림으로 안내한다.
                         */
                        return REDIRECT_EVENT_LIST;

               } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("이벤트 연장 처리 중 오류 - eventNo: {}", eventNo, e);
                        }
                        redirectAttributes.addFlashAttribute(
                                ATTR_ERROR_MESSAGE,
                                "이벤트 연장 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");

                        return REDIRECT_EVENT_LIST;
                }
        }

        /*
         * =========================================================
         * 정산 관리
         * =========================================================
         */
        @GetMapping("/settlement/main")
        public String settlement(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                // 로그인 사업자의 정산 예정 정보를 조회한다.
                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return REDIRECT_MEMBER_LOGIN;
                }

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute("settlementSummary", businessService.getMonthlySettlementSummary(business.getBusinessNo()));
                model.addAttribute("settlementAccount", businessService.getSettlementAccount(business.getBusinessNo()));
                model.addAttribute(MODEL_ACTIVE_MENU, ACTIVE_MENU_SETTLEMENT);

                return "business/settlement/settlementMain";
        }

        // 사업자 정산 요청 처리
        @PostMapping("/settlement/request")
        public String requestSettlement(HttpSession session, RedirectAttributes redirectAttributes) {

                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return REDIRECT_MEMBER_LOGIN;
                }

                try {
                        businessService.requestSettlementConfirmation(business.getBusinessNo());
                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "정산 요청이 완료되었습니다.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                }

                return "redirect:/business/settlement/main";
        }

        /* [수정] 월별 플랫폼 수수료 내역 화면 */
        @GetMapping("/settlement/complete")
        public String settlementComplete(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return REDIRECT_MEMBER_LOGIN;
                }

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute("settlementHistory", businessService.getSettlementPaymentHistory(business.getBusinessNo()));
                model.addAttribute(MODEL_ACTIVE_MENU, ACTIVE_MENU_SETTLEMENT);

                return "business/settlement/settlementComplete";
        }

        /* [수정] 사업자 정산 계좌 정보 관리 화면 */
        @GetMapping("/settlement/account")
        public String settlementAccount(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return REDIRECT_MEMBER_LOGIN;
                }

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute("settlementAccount", businessService.getSettlementAccount(business.getBusinessNo()));
                model.addAttribute(MODEL_ACTIVE_MENU, ACTIVE_MENU_SETTLEMENT);

                return "business/settlement/settlementAccount";
        }

        /* [수정] 사업자 정산 계좌 정보 저장 */
        @PostMapping("/settlement/account")
        public String updateSettlementAccount(
                        @RequestParam String bankName,
                        @RequestParam String accountNumber,
                        @RequestParam String accountHolder,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return REDIRECT_MEMBER_LOGIN;
                }

                try {
                        businessService.updateSettlementAccount(business.getBusinessNo(), bankName, accountNumber, accountHolder);
                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "정산 계좌 정보가 저장되었습니다.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                }

                return "redirect:/business/settlement/account";
        }

        /*
         * =========================================================
         * 판매 현황
         * =========================================================
         */
        @GetMapping("/settlement/sales")
        public String sales(
                        @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);
                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);
                        return REDIRECT_HOME;
                }

                /*
                 * 날짜를 입력하지 않고 처음 진입하면 이번 달 1일부터 오늘까지를
                 * 기본 조회 기간으로 사용한다. 한쪽 날짜만 전달된 경우에도
                 * 누락된 날짜만 기본값으로 보완한다.
                 */
                LocalDate today = LocalDate.now(DateTimeUtil.KOREA_ZONE);
                LocalDate resolvedStartDate = startDate == null ? today.withDayOfMonth(1) : startDate;
                LocalDate resolvedEndDate = endDate == null ? today : endDate;

                try {
                        SettlementManageVO salesStatus = businessService.getBusinessSalesStatus(
                                        business.getBusinessNo(), resolvedStartDate, resolvedEndDate);

                        /* [페이징 리팩터링] 관리자 목록 화면과 동일한 방식으로 페이지 계산 후 조회한다. */
                        int totalCount = businessService.getBusinessSalesHistoryCount(
                                        business.getBusinessNo(), resolvedStartDate, resolvedEndDate);
                        PageVO pagination = PaginationUtil.build(page, totalCount, BUSINESS_PAGE_SIZE,
                                        BUSINESS_PAGE_BLOCK_SIZE);

                        List<SettlementManageVO> salesHistory = businessService.getBusinessSalesHistory(
                                        business.getBusinessNo(), resolvedStartDate, resolvedEndDate,
                                        pagination.getCurrentPage(), BUSINESS_PAGE_SIZE);

                        model.addAttribute(MODEL_BUSINESS, business);
                        model.addAttribute("salesStatus", salesStatus);
                        model.addAttribute("salesHistory", salesHistory);
                        model.addAttribute("startDate", resolvedStartDate);
                        model.addAttribute("endDate", resolvedEndDate);
                        model.addAttribute(ATTR_PAGINATION, pagination);
                        model.addAttribute(MODEL_ACTIVE_MENU, "sales");

                        return "business/settlement/salesStatus";

                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());

                        return "redirect:/business/settlement/sales";
                }
        }

        /*
         * =========================================================
         * 채팅
         * =========================================================
         */
        @GetMapping("/chat")
        public String chat(Model model) {
                model.addAttribute(MODEL_ACTIVE_MENU, "chat");

                return "business/community/businessChat";
        }

        /*
         * 사업자 주문 현황
         *
         * [페이징 리팩터링] 관리자 목록 화면과 동일한 방식으로 페이지(page)를 처리한다.
         * 알림 딥링크(openOrderNo)로 들어온 주문은 최신순 정렬 기준 대부분 1페이지에
         * 위치하지만, 다른 페이지에 있는 경우까지는 별도로 찾아가지 않는다(기존 동작 유지).
         */
        @GetMapping("/order/list")
        public String orderList(
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);

                        return REDIRECT_MEMBER_LOGIN;
                }

                /* 로그인 회원과 연결된 사업자 조회 */
                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);

                        return REDIRECT_HOME;
                }

                int totalCount = businessService.getBusinessOrderListCount(business.getBusinessNo());
                PageVO pagination = PaginationUtil.build(page, totalCount, BUSINESS_PAGE_SIZE, BUSINESS_PAGE_BLOCK_SIZE);

                /* 해당 사업자의 주문 목록 조회 */
                List<OrderVO> orderList = businessService.getBusinessOrderList(
                                business.getBusinessNo(), pagination.getCurrentPage(), BUSINESS_PAGE_SIZE);

                model.addAttribute(MODEL_BUSINESS, business);
                model.addAttribute("orderList", orderList);
                model.addAttribute(ATTR_PAGINATION, pagination);
                model.addAttribute(MODEL_ACTIVE_MENU, MODEL_ORDER);

                return "business/order/orderList";
        }

        /*
         * [리팩터링] 주문 상세 페이지(GET /business/order/detail)는 제거했다.
         * 주문 상세는 이제 orderList.jsp 안에서 모달로만 보여준다.
         * 알림에서 들어오는 딥링크는 NotificationServiceImpl(주문 알림 생성 쪽)에서
         * "/business/order/list?openOrderNo=" + orderNo 로 직접 목록 페이지를
         * 가리키도록 바꿨고, 목록 페이지가 로드된 뒤 business.js가 해당 주문의
         * 상세 모달을 자동으로 연다.
         */

        /*
         * =========================================================
         * 사업자 배송 관리 목록
         *
         * 현재 로그인 회원과 연결된 사업자를 확인한 뒤 해당 사업자의
         * 주문상품만 조회한다. 상태와 주문번호/상품명 검색을 지원한다.
         * =========================================================
         */
        @GetMapping("/delivery/list")
        public String deliveryList(
                        @RequestParam(name = "status", required = false) String status,
                        @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);
                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);
                        return REDIRECT_HOME;
                }

                try {
                        /* [페이징 리팩터링] 관리자 목록 화면과 동일한 방식으로 페이지 계산 후 조회한다. */
                        int totalCount = businessService.getBusinessDeliveryListCount(
                                        business.getBusinessNo(), status, keyword);
                        PageVO pagination = PaginationUtil.build(page, totalCount, BUSINESS_CARD_PAGE_SIZE,
                                        BUSINESS_PAGE_BLOCK_SIZE);

                        List<DeliveryManageVO> deliveryList = businessService.getBusinessDeliveryList(
                                        business.getBusinessNo(), status, keyword,
                                        pagination.getCurrentPage(), BUSINESS_CARD_PAGE_SIZE);

                        model.addAttribute(MODEL_BUSINESS, business);
                        model.addAttribute("deliveryList", deliveryList);
                        model.addAttribute("selectedStatus", status);
                        model.addAttribute(PARAM_KEYWORD, keyword);
                        model.addAttribute(ATTR_PAGINATION, pagination);
                        model.addAttribute(MODEL_ACTIVE_MENU, "delivery");

                        return "business/order/deliveryList";

                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                        return "redirect:/business/delivery/list";
                }
        }

        /*
         * =========================================================
         * 사업자 운송장 및 배송 상태 저장
         *
         * BUSINESS_NO는 화면에서 받지 않고 로그인 세션을 기준으로
         * 서버에서 결정하여 다른 사업자의 주문상품 수정을 차단한다.
         * =========================================================
         */
        @PostMapping("/delivery/update")
        public String updateDelivery(
                        @RequestParam("orderItemNo") long orderItemNo,
                        @RequestParam(name = "courier", required = false) String courier,
                        @RequestParam(name = "trackingNumber", required = false) String trackingNumber,
                        @RequestParam("status") String status,
                        @RequestParam(name = "returnStatus", required = false) String returnStatus,
                        @RequestParam(name = "returnKeyword", required = false) String returnKeyword,
                        @RequestParam(name = "returnPage", required = false, defaultValue = "1") int returnPage,
                        HttpSession session, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);
                        return REDIRECT_MEMBER_LOGIN;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);
                        return REDIRECT_HOME;
                }

                try {
                        businessService.updateBusinessDelivery(business.getBusinessNo(), orderItemNo, courier, trackingNumber, status);

                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "배송 정보가 저장되었습니다.");

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("배송 정보 저장 중 오류 - orderItemNo: {}", orderItemNo, e);
                        }
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "배송 정보 저장 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
                }

                /* 목록에서 사용하던 검색 조건과 페이지를 유지하여 같은 화면으로 돌아간다. */
                if (returnStatus != null && !returnStatus.isBlank()) {
                        redirectAttributes.addAttribute("status", returnStatus);
                }
                if (returnKeyword != null && !returnKeyword.isBlank()) {
                        redirectAttributes.addAttribute(PARAM_KEYWORD, returnKeyword);
                }
                if (returnPage > 1) {
                        redirectAttributes.addAttribute("page", returnPage);
                }

                return "redirect:/business/delivery/list";
        }

        /* 취소 및 환불 관리 */
        @GetMapping("/cancel/list")
        public String cancelList(@RequestParam(name = "status", required = false) String status,
                        @RequestParam(required = false, defaultValue = "1") int page,
                        HttpSession session,
                        Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "로그인 회원 정보를 확인할 수 없습니다.");
                        return REDIRECT_MEMBER_LOGIN;
                }

                try {
                        /* [페이징 리팩터링] 관리자 목록 화면과 동일한 방식으로 페이지 계산 후 조회한다. */
                        int totalCount = orderCancelRefundService.getBusinessCancelListCount(memberNo, status);
                        PageVO pagination = PaginationUtil.build(page, totalCount, BUSINESS_CARD_PAGE_SIZE,
                                        BUSINESS_PAGE_BLOCK_SIZE);

                        model.addAttribute("cancelList", orderCancelRefundService.getBusinessCancelList(
                                        memberNo, status, pagination.getCurrentPage(), BUSINESS_CARD_PAGE_SIZE));
                        model.addAttribute(ATTR_PAGINATION, pagination);
                        model.addAttribute(MODEL_ACTIVE_MENU, "cancel");
                        return "business/order/cancelList";

                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                        return REDIRECT_BUSINESS_MAIN;
                }
        }

        /*
         * =========================================================
         * [취소 요청 승인 기능 추가]
         * 사업자가 승인하면 포트원 부분 환불 후 재고와 상태를 변경한다.
         * =========================================================
         */
        @PostMapping("/cancel/approve")
        public String approveCancel(@RequestParam("cancelNo") Long cancelNo,
                        @RequestParam(name = "returnStatus", required = false) String returnStatus,
                        @RequestParam(name = "returnPage", required = false, defaultValue = "1") int returnPage,
                        HttpSession session, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                try {
                        orderCancelRefundService.approveCancel(memberNo, cancelNo);
                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "취소 요청을 승인하고 환불을 완료했습니다.");

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("취소 요청 승인 처리 중 오류 - memberNo: {}, cancelNo: {}", memberNo, cancelNo, e);
                        }
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "취소 요청 승인 처리 중 오류가 발생했습니다.");
                }

                /* [페이징 리팩터링 추가] 목록에서 보던 상태 필터와 페이지를 유지하여 같은 화면으로 돌아간다. */
                if (returnStatus != null && !returnStatus.isBlank()) {
                        redirectAttributes.addAttribute("status", returnStatus);
                }
                if (returnPage > 1) {
                        redirectAttributes.addAttribute("page", returnPage);
                }

                return "redirect:/business/cancel/list";
        }

        /*
         * =========================================================
         * [취소 요청 반려 기능 추가]
         * 반려 사유를 저장하고 주문상품 상태를 결제 완료로 복구한다.
         * =========================================================
         */
        @PostMapping("/cancel/reject")
        public String rejectCancel(
                        @RequestParam("cancelNo") Long cancelNo,
                        @RequestParam(name = "rejectReason", required = false) String rejectReason,
                        @RequestParam(name = "returnStatus", required = false) String returnStatus,
                        @RequestParam(name = "returnPage", required = false, defaultValue = "1") int returnPage,
                        HttpSession session, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                try {
                        orderCancelRefundService.rejectCancel(memberNo, cancelNo, rejectReason);
                        redirectAttributes.addFlashAttribute(ATTR_SUCCESS_MESSAGE, "취소 요청을 반려했습니다.");

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, e.getMessage());
                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("취소 요청 반려 처리 중 오류 - memberNo: {}, cancelNo: {}", memberNo, cancelNo, e);
                        }
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, "취소 요청 반려 처리 중 오류가 발생했습니다.");
                }

                if (returnStatus != null && !returnStatus.isBlank()) {
                        redirectAttributes.addAttribute("status", returnStatus);
                }
                if (returnPage > 1) {
                        redirectAttributes.addAttribute("page", returnPage);
                }

                return "redirect:/business/cancel/list";
        }

        /* 이벤트 상품별 할인율 유효성 검사 */
        private static boolean hasInvalidDiscountRate(List<Integer> discountRateList) {

                if (discountRateList == null) {
                        return false;
                }

                for (Integer rate : discountRateList) {
                        if (rate < 0 || rate > 100) {
                                return true;
                        }
                }

                return false;
        }

        /*
         * =========================================================
         * 로그인 회원 번호 조회
         *
         * 조회 순서:
         * 1. loginMemberNo
         * 2. memberNo
         * 3. loginMember 객체
         *
         * 프로젝트 세션 키가 완전히 통일되기 전까지
         * 기존 로그인 방식도 함께 지원한다.
         * =========================================================
         */
        /*
         * [수정] 정산 화면에서 공통으로 사용하는 로그인 사업자 조회.
         * 세션 회원번호와 BUSINESS 연결 여부를 한 곳에서 확인한다.
         */
        private BusinessVO getLoginBusiness(HttpSession session, RedirectAttributes redirectAttributes) {
                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, LOGIN_MEMBER_ERROR_MESSAGE);
                        return null;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);
                if (business == null) {
                        redirectAttributes.addFlashAttribute(ATTR_ERROR_MESSAGE, BUSINESS_NOT_FOUND_MESSAGE);
                }
                return business;
        }

        private Long getLoginMemberNo(
                        HttpSession session) {


                // 현재 ODITJI 표준 세션 키
                Object loginMemberNo = session.getAttribute(SESSION_LOGIN_MEMBER_NO);

                Long convertedLoginMemberNo = convertToLong(loginMemberNo);

                if (convertedLoginMemberNo != null) {
                        return convertedLoginMemberNo;
                }

                // 기존 코드에서 사용했을 가능성이 있는 세션 키
                Object memberNo = session.getAttribute("memberNo");

                Long convertedMemberNo = convertToLong(memberNo);

                if (convertedMemberNo != null) {

                        /*
                         * 이후 요청부터 표준 키를 사용하도록
                         * loginMemberNo에도 다시 저장한다.
                         */
                        session.setAttribute(SESSION_LOGIN_MEMBER_NO, convertedMemberNo);

                        return convertedMemberNo;
                }

                // MemberVO 전체 객체가 세션에 저장된 경우
                Object loginMemberObject = session.getAttribute("loginMember");

                if (loginMemberObject instanceof MemberVO loginMember) {
                        long loginMemberObjectNo = loginMember.getMemberNo();

                        if (loginMemberObjectNo > 0) {
                                session.setAttribute(SESSION_LOGIN_MEMBER_NO, loginMemberObjectNo);

                                return loginMemberObjectNo;
                        }
                }

                return null;
        }

        /*
         * =========================================================
         * Object 값을 Long으로 변환
         *
         * 세션에 Long, Integer 또는 숫자 문자열로 저장된 경우를
         * 모두 처리한다.
         * =========================================================
         */
        private Long convertToLong(Object value) {

                if (value == null) {
                        return null;
                }

                if (value instanceof Number number) {
                        long convertedValue = number.longValue();

                        return convertedValue > 0 ? convertedValue : null;
                }

                if (value instanceof String stringValue) {
                        try {
                                long convertedValue = Long.parseLong(stringValue.trim());

                                return convertedValue > 0 ? convertedValue : null;

                        } catch (NumberFormatException e) {

                                return null;
                        }
                }

                return null;
        }

}