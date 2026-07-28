package com.project.oditji.business.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Enumeration;
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

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/business")
public class BusinessController {

        private final BusinessService businessService;
        private final OrderCancelRefundService orderCancelRefundService;
        private static final Logger log = LoggerFactory.getLogger(BusinessController.class);

        public BusinessController(
                        BusinessService businessService,
                        OrderCancelRefundService orderCancelRefundService) {

                this.businessService = businessService;
                this.orderCancelRefundService = orderCancelRefundService;
        }

        /* 사업자 메인 */
        @GetMapping("/main")
        public String main(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원 정보를 확인할 수 없습니다. "
                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);
                if (business == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }
                model.addAttribute("business", business);
                /* 사업자 메인 대시보드 통계 */
                BusinessDashboardVO businessMain = businessService.getBusinessDashboard(business.getBusinessNo());
                /* 사업자 인기 상품 */
                businessMain.setPopularProducts(businessService.getPopularProducts(business.getBusinessNo()));
                model.addAttribute("businessMain", businessMain);
                model.addAttribute("activeMenu", "main");

                return "business/main/businessMain";
        }

        /* 상품 목록 */
        @GetMapping("/product/list")
        public String productList(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원 정보를 확인할 수 없습니다. "
                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                List<GoodsManageVO> productList = businessService.getProductListByBusinessNo(business.getBusinessNo());
                model.addAttribute("business", business);
                model.addAttribute("productList", productList);
                model.addAttribute("activeMenu", "product");

                return "business/goods/productList";
        }

        /* 상품 등록 화면 */
        @GetMapping("/product/register")
        public String productRegister(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원 정보를 확인할 수 없습니다. "
                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(business.getStatus())) {
                        redirectAttributes.addFlashAttribute("errorMessage", "승인된 사업자만 상품을 등록할 수 있습니다.");

                        return "redirect:/business/main";
                }

                /* 상품 등록 실패 후 다시 진입한 경우 RedirectAttributes로 전달된 productForm을 유지. */
                if (!model.containsAttribute("productForm")) {

                        model.addAttribute("productForm", new GoodsManageVO());
                }

                model.addAttribute("business", business);
                model.addAttribute("activeMenu", "productRegister");

                return "business/goods/productRegister";
        }

        /* 상품 등록 처리 */
        @PostMapping("/product/register")
        public String productRegisterProcess(
                        @ModelAttribute("productForm") GoodsManageVO goodsManageVO,

                        @RequestParam(value = "productImage", required = false) MultipartFile productImage,
                        HttpSession session, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원 정보를 확인할 수 없습니다. "
                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(business.getStatus())) {
                        redirectAttributes.addFlashAttribute("errorMessage", "승인된 사업자만 상품을 등록할 수 있습니다.");

                        return "redirect:/business/main";
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

                        redirectAttributes.addFlashAttribute("successMessage", "상품 등록 요청이 완료되었습니다. "
                                        + "관리자 승인 후 판매됩니다.");
                        redirectAttributes.addFlashAttribute("registeredProductNo", productNo);

                        return "redirect:/business/product/list";

                } catch (IllegalArgumentException | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                        redirectAttributes.addFlashAttribute("productForm", goodsManageVO);

                        return "redirect:/business/product/register";

                } catch (Exception e) {
                        e.printStackTrace();
                        redirectAttributes.addFlashAttribute("errorMessage", "상품 등록 중 오류가 발생했습니다.");
                        redirectAttributes.addFlashAttribute("productForm", goodsManageVO);

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
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "mode", required = false, defaultValue = "database") String mode,
                        Model model) {

                boolean registerMode =
                                "register".equalsIgnoreCase(mode);

                List<ContentSearchVO> contentList =
                                registerMode
                                                ? businessService.getCachedContentList(keyword)
                                                : businessService.getContentList(keyword);

                model.addAttribute("keyword", keyword);
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
                        @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword) {
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

                System.out.println("===== 콘텐츠별 배우 조회 API =====");
                System.out.println("contentNo: " + contentNo);

                if (contentNo <= 0) {
                        return Collections.emptyList();
                }

                List<ActorSearchVO> actorList = businessService.getActorListByContentNo(contentNo);
                System.out.println("조회된 배우 수: " + actorList.size());

                for (ActorSearchVO actor : actorList) {

                        System.out.println("배우: " + actor.getActorName() + ", 배역: " + actor.getCharacterName());
                }

                return actorList;
        }

        /*
         * =========================================================
         * 상품 수정 화면
         *
         * 상품 번호 없이 직접 접근한 경우에는
         * 상품 목록으로 이동.
         * =========================================================
         */
        @GetMapping("/product/update")
        public String productUpdate(@RequestParam(value = "productNo", required = false) Long productNo,
                        HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                if (productNo == null || productNo <= 0) {
                        redirectAttributes.addFlashAttribute("errorMessage", "수정할 상품을 선택해주세요.");

                        return "redirect:/business/product/list";
                }

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원 정보를 확인할 수 없습니다. "
                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(
                                business.getStatus())) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "승인된 사업자만 상품을 수정할 수 있습니다.");

                        return "redirect:/business/main";
                }

                try {

                        /*
                         * 수정 처리 실패 후 다시 돌아온 경우에는
                         * 사용자가 입력했던 productForm을 유지한다.
                         */
                        if (!model.containsAttribute(
                                        "productForm")) {

                                GoodsManageVO product = businessService.getProductForUpdate(
                                                productNo,
                                                business.getBusinessNo());

                                model.addAttribute(
                                                "productForm",
                                                product);
                        }

                        model.addAttribute(
                                        "business",
                                        business);

                        /*
                         * 상품 수정 메뉴 활성화
                         */
                        model.addAttribute(
                                        "activeMenu",
                                        "productUpdate");

                        return "business/goods/productUpdate";

                } catch (IllegalArgumentException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/product/list";
                }
        }

        /*
         * =========================================================
         * 상품 수정 처리
         * =========================================================
         */
        @PostMapping("/product/update")
        public String productUpdateProcess(
                        @ModelAttribute("productForm") GoodsManageVO goodsManageVO,

                        @RequestParam(value = "productImage", required = false) MultipartFile productImage,

                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(
                                business.getStatus())) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "승인된 사업자만 상품을 수정할 수 있습니다.");

                        return "redirect:/business/main";
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
                                        "successMessage",
                                        "상품 수정 요청이 완료되었습니다. "
                                                        + "관리자 승인 후 변경 내용이 반영됩니다.");

                        return "redirect:/business/product/list";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        redirectAttributes.addFlashAttribute(
                                        "productForm",
                                        goodsManageVO);

                        return "redirect:/business/product/update"
                                        + "?productNo="
                                        + goodsManageVO.getProductNo();

                } catch (Exception e) {

                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "상품 수정 중 오류가 발생했습니다.");

                        redirectAttributes.addFlashAttribute(
                                        "productForm",
                                        goodsManageVO);

                        return "redirect:/business/product/update"
                                        + "?productNo="
                                        + goodsManageVO.getProductNo();
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
        @GetMapping("/product/delete")
        public String productDelete(
                        @RequestParam(value = "productNo", required = false) Long productNo,

                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                /*
                 * 상품 번호 없이 삭제 페이지에 직접 접근한 경우
                 */
                if (productNo == null
                                || productNo <= 0) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "삭제 요청할 상품을 선택해주세요.");

                        return "redirect:/business/product/list";
                }

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(
                                business.getStatus())) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "승인된 사업자만 상품 삭제를 요청할 수 있습니다.");

                        return "redirect:/business/main";
                }

                try {

                        /*
                         * PRODUCT_NO와 BUSINESS_NO를 함께 조회하므로
                         * 다른 사업자의 상품에는 접근할 수 없다.
                         */
                        GoodsManageVO product = businessService.getProductForUpdate(
                                        productNo,
                                        business.getBusinessNo());

                        model.addAttribute(
                                        "product",
                                        product);

                        model.addAttribute(
                                        "business",
                                        business);

                        model.addAttribute(
                                        "activeMenu",
                                        "productDelete");

                        return "business/goods/productDelete";

                } catch (IllegalArgumentException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/product/list";
                }
        }

        /*
         * =========================================================
         * 상품 삭제 요청 처리
         *
         * productDelete.jsp에서 전송되는
         * POST /business/product/delete 요청을 처리한다.
         *
         * 실제 상품 데이터를 바로 삭제하지 않고
         * PRODUCT.STATUS를 DELETE_REQUESTED로 변경한다.
         * =========================================================
         */
        @PostMapping("/product/delete")
        public String productDeleteProcess(
                        @RequestParam("productNo") long productNo,

                        @RequestParam("reason") String reason,

                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(
                                business.getStatus())) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "승인된 사업자만 상품 삭제를 요청할 수 있습니다.");

                        return "redirect:/business/main";
                }

                try {

                        businessService.requestProductDelete(
                                        productNo,
                                        business.getBusinessNo(),
                                        reason);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "상품 삭제 요청이 완료되었습니다. "
                                                        + "관리자 승인 후 최종 처리됩니다.");

                        return "redirect:/business/product/list";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/product/delete"
                                        + "?productNo="
                                        + productNo;

                } catch (Exception e) {

                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "상품 삭제 요청 처리 중 오류가 발생했습니다.");

                        return "redirect:/business/product/delete"
                                        + "?productNo="
                                        + productNo;
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
        public String eventList(
                        @RequestParam(value = "keyword", required = false) String keyword,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                List<EventManageVO> eventList = businessService.getEventListByBusinessNo(
                                business.getBusinessNo(),
                                keyword);

                model.addAttribute(
                                "business",
                                business);

                model.addAttribute(
                                "keyword",
                                keyword);

                model.addAttribute(
                                "eventList",
                                eventList);

                model.addAttribute(
                                "activeMenu",
                                "event");

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
        public String eventRegister(
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(
                                business.getStatus())) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "승인된 사업자만 이벤트 등록을 요청할 수 있습니다.");

                        return "redirect:/business/main";
                }

                /*
                 * 현재 로그인한 사업자가 등록한 상품만 조회한다.
                 * 별도 검색 JSP를 추가하지 않고 현재 이벤트 등록 화면의
                 * 상품 검색 모달에서 이 목록을 사용한다.
                 */
                List<GoodsManageVO> productList = businessService.getApprovedProductListByBusinessNo(
                                business.getBusinessNo());

                model.addAttribute(
                                "business",
                                business);

                model.addAttribute(
                                "productList",
                                productList);

                model.addAttribute(
                                "activeMenu",
                                "eventRegister");

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
         * EVENT 테이블에는 이벤트 설명 컬럼이 없으므로
         * eventContent는 현재 서버 로그 확인용으로만 사용한다.
         * =========================================================
         */
        @PostMapping("/event/register")
        public String eventRegisterProcess(

                        @RequestParam("eventTitle") String eventTitle,

                        @RequestParam(value = "eventContent", required = false) String eventContent,

                        @RequestParam("startDate") LocalDate startDate,

                        @RequestParam("endDate") LocalDate endDate,

                        /*
                         * 선택한 연결 상품 목록
                         */
                        @RequestParam(value = "productNoList", required = false) List<Long> productNoList,

                        /*
                         * 상품별 할인율 목록
                         */
                        @RequestParam(value = "discountRateList", required = false) List<Integer> discountRateList,

                        @RequestParam(value = "eventImage", required = false) MultipartFile eventImage,

                        HttpSession session,

                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (!"APPROVED".equals(
                                business.getStatus())) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "승인된 사업자만 이벤트 등록을 요청할 수 있습니다.");

                        return "redirect:/business/main";
                }

                if (discountRateList != null) {

                        for (Integer rate : discountRateList) {

                                if (rate < 0 || rate > 100) {

                                        redirectAttributes.addFlashAttribute(
                                                        "errorMessage",
                                                        "이벤트 할인율은 0~100 사이로 입력해주세요.");

                                        return "redirect:/business/event/register";
                                }

                        }

                }

                EventManageVO eventManageVO = new EventManageVO();

                eventManageVO.setBusinessNo(
                                business.getBusinessNo());

                eventManageVO.setTitle(
                                eventTitle);

                eventManageVO.setStartDate(
                                startDate);

                eventManageVO.setEndDate(
                                endDate);

                /*
                 * 이벤트 등록 요청은 반드시 관리자 승인을 거치므로
                 * 화면 전달값과 무관하게 WAITING 상태로 저장한다.
                 */
                eventManageVO.setStatus(
                                "WAITING");

                eventManageVO.setProductNoList(
                                productNoList);

                eventManageVO.setDiscountRateList(
                                discountRateList);

                try {

                        System.out.println(
                                        "===== 이벤트 등록 요청 =====");

                        System.out.println(
                                        "사업자 번호: "
                                                        + business.getBusinessNo());

                        System.out.println(
                                        "이벤트명: "
                                                        + eventTitle);

                        System.out.println(
                                        "이벤트 설명: "
                                                        + eventContent);

                        System.out.println(
                                        "이벤트 시작일: "
                                                        + startDate);

                        System.out.println(
                                        "이벤트 종료일: "
                                                        + endDate);

                        System.out.println(
                                        "연결 상품 번호 목록: "
                                                        + productNoList);

                        System.out.println(
                                        "상품별 할인율 목록: "
                                                        + discountRateList);

                        System.out.println(
                                        "이벤트 상태: WAITING");

                        long eventNo = businessService.registerEvent(
                                        eventManageVO,
                                        eventImage);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "이벤트 등록 요청이 접수되었습니다.");

                        redirectAttributes.addFlashAttribute(
                                        "registeredEventNo",
                                        eventNo);

                        return "redirect:/business/event/list";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        System.out.println("===== catch 실행 =====");
                        System.out.println("에러 메시지 : " + e.getMessage());
                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/event/register";

                } catch (Exception e) {

                        System.out.println("===== Exception catch 실행 =====");
                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트 등록 요청 처리 중 오류가 발생했습니다.");

                        return "redirect:/business/event/register";
                }
        }

        /*
         * =========================================================
         * 이벤트 수정 화면
         *
         * 관리자 승인이 완료된 APPROVED 이벤트만 수정 요청할 수 있다.
         * 현재 로그인한 사업자의 상품과 연결된 이벤트인지 함께 확인한다.
         * =========================================================
         */
        @GetMapping("/event/update")
        public String eventUpdate(
                        @RequestParam(value = "eventNo", required = false) Long eventNo,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                if (eventNo == null
                                || eventNo <= 0) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "수정할 이벤트를 선택해주세요.");

                        return "redirect:/business/event/list";
                }

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                try {

                        EventManageVO event = businessService.getApprovedEventForBusiness(
                                        eventNo,
                                        business.getBusinessNo());

                        List<GoodsManageVO> productList = businessService.getApprovedProductListByBusinessNo(
                                        business.getBusinessNo());

                        model.addAttribute(
                                        "business",
                                        business);

                        model.addAttribute(
                                        "event",
                                        event);

                        model.addAttribute(
                                        "productList",
                                        productList);

                        model.addAttribute(
                                        "activeMenu",
                                        "eventUpdate");

                        return "business/event/eventUpdate";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/event/list";
                }
        }

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

                        @RequestParam(value = "eventContent", required = false) String eventContent,

                        @RequestParam("startDate") LocalDate startDate,

                        @RequestParam("endDate") LocalDate endDate,

                        /*
                         * 선택한 연결 상품 목록 (수정 화면도 등록 화면과 동일하게
                         * +버튼으로 여러 개의 상품을 연결할 수 있다.)
                         */
                        @RequestParam(value = "productNoList", required = false) List<Long> productNoList,

                        /*
                         * 상품별 할인율 목록
                         */
                        @RequestParam(value = "discountRateList", required = false) List<Integer> discountRateList,

                        @RequestParam(value = "eventImage", required = false) MultipartFile eventImage,

                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                if (discountRateList != null) {

                        for (Integer rate : discountRateList) {

                                if (rate < 0 || rate > 100) {

                                        redirectAttributes.addFlashAttribute(
                                                        "errorMessage",
                                                        "이벤트 할인율은 0~100 사이로 입력해주세요.");

                                        return "redirect:/business/event/update?eventNo=" + eventNo;
                                }
                        }
                }

                EventManageVO eventManageVO = new EventManageVO();

                eventManageVO.setEventNo(
                                eventNo);

                eventManageVO.setBusinessNo(
                                business.getBusinessNo());

                eventManageVO.setTitle(
                                eventTitle);

                eventManageVO.setStartDate(
                                startDate);

                eventManageVO.setEndDate(
                                endDate);

                /*
                 * 이벤트 수정 요청은 반드시 관리자 재승인을 거치므로
                 * 화면 전달값과 무관하게 WAITING 상태로 저장한다.
                 */
                eventManageVO.setStatus(
                                "WAITING");

                eventManageVO.setProductNoList(
                                productNoList);

                eventManageVO.setDiscountRateList(
                                discountRateList);

                try {

                        System.out.println(
                                        "===== 이벤트 수정 요청 =====");

                        System.out.println(
                                        "이벤트 번호: "
                                                        + eventNo);

                        System.out.println(
                                        "사업자 번호: "
                                                        + business.getBusinessNo());

                        System.out.println(
                                        "이벤트 설명: "
                                                        + eventContent);

                        System.out.println(
                                        "연결 상품 번호 목록: "
                                                        + productNoList);

                        System.out.println(
                                        "상품별 할인율 목록: "
                                                        + discountRateList);

                        businessService.updateApprovedEvent(
                                        eventManageVO,
                                        eventImage);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "이벤트 수정 요청이 접수되었습니다. "
                                                        + "관리자 승인 전까지 사용자 화면에 노출되지 않습니다.");

                        return "redirect:/business/event/list";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/event/update"
                                        + "?eventNo="
                                        + eventNo;

                } catch (Exception e) {

                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트 수정 처리 중 오류가 발생했습니다.");

                        return "redirect:/business/event/update"
                                        + "?eventNo="
                                        + eventNo;
                }
        }

        /*
         * =========================================================
         * 이벤트 연장 화면
         *
         * 관리자 승인이 완료된 APPROVED 이벤트만 연장 요청할 수 있다.
         * =========================================================
         */
        @GetMapping("/event/extend")
        public String eventExtend(
                        @RequestParam(value = "eventNo", required = false) Long eventNo,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                if (eventNo == null
                                || eventNo <= 0) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "연장할 이벤트를 선택해주세요.");

                        return "redirect:/business/event/list";
                }

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                try {

                        EventManageVO event = businessService.getApprovedEventForBusiness(
                                        eventNo,
                                        business.getBusinessNo());

                        model.addAttribute(
                                        "business",
                                        business);

                        model.addAttribute(
                                        "event",
                                        event);

                        model.addAttribute(
                                        "activeMenu",
                                        "eventExtend");

                        return "business/event/eventExtend";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/event/list";
                }
        }

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

                Long memberNo = getLoginMemberNo(
                                session);

                if (memberNo == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. "
                                                        + "다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(
                                memberNo);

                if (business == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                try {

                        businessService.extendApprovedEvent(
                                        eventNo,
                                        business.getBusinessNo(),
                                        extendEndDate,
                                        extendReason);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "이벤트 연장 요청이 접수되었습니다. "
                                                        + "관리자 승인 전까지 사용자 화면에 노출되지 않습니다.");

                        return "redirect:/business/event/list";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        return "redirect:/business/event/extend"
                                        + "?eventNo="
                                        + eventNo;

                } catch (Exception e) {

                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트 연장 처리 중 오류가 발생했습니다.");

                        return "redirect:/business/event/extend"
                                        + "?eventNo="
                                        + eventNo;
                }
        }

        /*
         * =========================================================
         * 정산 관리
         * =========================================================
         */
        @GetMapping("/settlement/main")
        public String settlement(
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                /* [수정] 로그인 사업자의 이번 달 수수료 정보를 실제 DB에서 조회한다. */
                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return "redirect:/member/login";
                }

                model.addAttribute("business", business);
                model.addAttribute("settlementSummary",
                                businessService.getMonthlySettlementSummary(business.getBusinessNo()));
                model.addAttribute("activeMenu", "settlement");

                return "business/settlement/settlementMain";
        }

        /* [수정] 사업자 수수료 입금 확인 요청 처리 */
        @PostMapping("/settlement/request")
        public String requestSettlement(
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return "redirect:/member/login";
                }

                try {
                        businessService.requestSettlementConfirmation(business.getBusinessNo());
                        redirectAttributes.addFlashAttribute("successMessage", "입금 확인 요청이 완료되었습니다.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                }

                return "redirect:/business/settlement/main";
        }

        /* [수정] 월별 수수료 납부 내역 화면 */
        @GetMapping("/settlement/complete")
        public String settlementComplete(
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return "redirect:/member/login";
                }

                model.addAttribute("business", business);
                model.addAttribute("settlementHistory",
                                businessService.getSettlementPaymentHistory(business.getBusinessNo()));
                model.addAttribute("activeMenu", "settlement");

                return "business/settlement/settlementComplete";
        }

        /* [수정] 사업자 정산 계좌 정보 관리 화면 */
        @GetMapping("/settlement/account")
        public String settlementAccount(
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                BusinessVO business = getLoginBusiness(session, redirectAttributes);
                if (business == null) {
                        return "redirect:/member/login";
                }

                model.addAttribute("business", business);
                model.addAttribute("settlementAccount",
                                businessService.getSettlementAccount(business.getBusinessNo()));
                model.addAttribute("activeMenu", "settlement");

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
                        return "redirect:/member/login";
                }

                try {
                        businessService.updateSettlementAccount(
                                        business.getBusinessNo(), bankName, accountNumber, accountHolder);
                        redirectAttributes.addFlashAttribute("successMessage", "정산 계좌 정보가 저장되었습니다.");
                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
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
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);
                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);
                if (business == null) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                /*
                 * 날짜를 입력하지 않고 처음 진입하면 이번 달 1일부터 오늘까지를
                 * 기본 조회 기간으로 사용한다. 한쪽 날짜만 전달된 경우에도
                 * 누락된 날짜만 기본값으로 보완한다.
                 */
                LocalDate today = LocalDate.now();
                LocalDate resolvedStartDate = startDate == null
                                ? today.withDayOfMonth(1)
                                : startDate;
                LocalDate resolvedEndDate = endDate == null
                                ? today
                                : endDate;

                try {
                        SettlementManageVO salesStatus = businessService.getBusinessSalesStatus(
                                        business.getBusinessNo(), resolvedStartDate, resolvedEndDate);
                        List<SettlementManageVO> salesHistory = businessService.getBusinessSalesHistory(
                                        business.getBusinessNo(), resolvedStartDate, resolvedEndDate);

                        model.addAttribute("business", business);
                        model.addAttribute("salesStatus", salesStatus);
                        model.addAttribute("salesHistory", salesHistory);
                        model.addAttribute("startDate", resolvedStartDate);
                        model.addAttribute("endDate", resolvedEndDate);
                        model.addAttribute("activeMenu", "sales");

                        return "business/settlement/salesStatus";

                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

                        return "redirect:/business/settlement/sales";
                }
        }

        /*
         * =========================================================
         * 채팅
         * =========================================================
         */
        @GetMapping("/chat")
        public String chat(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "chat");

                return "business/community/businessChat";
        }

        /* 사업자 주문 현황 */
        @GetMapping("/order/list")
        public String orderList(HttpSession session, Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.");

                        return "redirect:/member/login";
                }

                /* 로그인 회원과 연결된 사업자 조회 */
                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원과 연결된 사업자 정보가 없습니다.");

                        return "redirect:/";
                }

                /* 해당 사업자의 주문 목록 조회 */
                List<OrderVO> orderList = businessService.getBusinessOrderList(business.getBusinessNo());

                model.addAttribute("business", business);
                model.addAttribute("orderList", orderList);
                model.addAttribute("activeMenu", "order");

                return "business/order/orderList";
        }

        // 사업자 주문 상세
        @GetMapping("/order/detail")
        public String orderDetail(@RequestParam("orderNo") long orderNo, HttpSession session, Model model,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인이 필요합니다.");
                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "사업자 정보를 확인할 수 없습니다.");
                        return "redirect:/";
                }

                OrderVO order = businessService.getBusinessOrderDetail(business.getBusinessNo(), orderNo);

                if (order == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "해당 주문을 확인할 수 없습니다.");
                        return "redirect:/business/order/list";
                }

                model.addAttribute("business", business);
                model.addAttribute("order", order);
                model.addAttribute("activeMenu", "order");

                return "business/order/orderDetail";
        }

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
                        @RequestParam(name = "keyword", required = false) String keyword,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.");
                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");
                        return "redirect:/";
                }

                try {
                        List<DeliveryManageVO> deliveryList = businessService.getBusinessDeliveryList(
                                        business.getBusinessNo(), status, keyword);

                        model.addAttribute("business", business);
                        model.addAttribute("deliveryList", deliveryList);
                        model.addAttribute("selectedStatus", status);
                        model.addAttribute("keyword", keyword);
                        model.addAttribute("activeMenu", "delivery");

                        return "business/order/deliveryList";

                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
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
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.");
                        return "redirect:/member/login";
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);

                if (business == null) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");
                        return "redirect:/";
                }

                try {
                        businessService.updateBusinessDelivery(
                                        business.getBusinessNo(),
                                        orderItemNo,
                                        courier,
                                        trackingNumber,
                                        status);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "배송 정보가 저장되었습니다.");

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                } catch (Exception e) {
                        e.printStackTrace();
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "배송 정보 저장 중 오류가 발생했습니다.");
                }

                /* 목록에서 사용하던 검색 조건을 유지하여 같은 화면으로 돌아간다. */
                if (returnStatus != null && !returnStatus.isBlank()) {
                        redirectAttributes.addAttribute("status", returnStatus);
                }
                if (returnKeyword != null && !returnKeyword.isBlank()) {
                        redirectAttributes.addAttribute("keyword", returnKeyword);
                }

                return "redirect:/business/delivery/list";
        }

        /* 취소 및 환불 관리 */
        @GetMapping("/cancel/list")
        public String cancelList(@RequestParam(name = "status", required = false) String status, HttpSession session,
                        Model model, RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                if (memberNo == null) {
                        redirectAttributes.addFlashAttribute("errorMessage", "로그인 회원 정보를 확인할 수 없습니다.");
                        return "redirect:/member/login";
                }

                try {
                        model.addAttribute("cancelList",
                                        orderCancelRefundService.getBusinessCancelList(memberNo, status));
                        model.addAttribute("activeMenu", "cancel");
                        return "business/order/cancelList";

                } catch (IllegalArgumentException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                        return "redirect:/business/main";
                }
        }

        /*
         * =========================================================
         * [취소 요청 승인 기능 추가]
         * 사업자가 승인하면 포트원 부분 환불 후 재고와 상태를 변경한다.
         * =========================================================
         */
        @PostMapping("/cancel/approve")
        public String approveCancel(
                        @RequestParam("cancelNo") Long cancelNo,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                try {
                        orderCancelRefundService.approveCancel(memberNo, cancelNo);
                        redirectAttributes.addFlashAttribute("successMessage", "취소 요청을 승인하고 환불을 완료했습니다.");

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("취소 요청 승인 처리 중 오류 - memberNo: {}, cancelNo: {}", memberNo, cancelNo, e);
                        }
                        redirectAttributes.addFlashAttribute("errorMessage", "취소 요청 승인 처리 중 오류가 발생했습니다.");
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
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

                Long memberNo = getLoginMemberNo(session);

                try {
                        orderCancelRefundService.rejectCancel(memberNo, cancelNo, rejectReason);
                        redirectAttributes.addFlashAttribute("successMessage", "취소 요청을 반려했습니다.");

                } catch (IllegalArgumentException | IllegalStateException e) {
                        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                                log.error("취소 요청 반려 처리 중 오류 - memberNo: {}, cancelNo: {}", memberNo, cancelNo, e);
                        }
                        redirectAttributes.addFlashAttribute("errorMessage", "취소 요청 반려 처리 중 오류가 발생했습니다.");
                }

                return "redirect:/business/cancel/list";
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
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원 정보를 확인할 수 없습니다. 다시 로그인해주세요.");
                        return null;
                }

                BusinessVO business = businessService.getBusinessByMemberNo(memberNo);
                if (business == null) {
                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "로그인 회원과 연결된 사업자 정보가 없습니다.");
                }
                return business;
        }

        private Long getLoginMemberNo(
                        HttpSession session) {

                System.out.println(
                                "===== 사업자 페이지 세션 확인 =====");

                System.out.println(
                                "세션 ID: "
                                                + session.getId());

                /*
                 * 현재 ODITJI 표준 세션 키
                 */
                Object loginMemberNo = session.getAttribute(
                                "loginMemberNo");

                System.out.println(
                                "loginMemberNo: "
                                                + loginMemberNo);

                Long convertedLoginMemberNo = convertToLong(
                                loginMemberNo);

                if (convertedLoginMemberNo != null) {
                        return convertedLoginMemberNo;
                }

                /*
                 * 기존 코드에서 사용했을 가능성이 있는 세션 키
                 */
                Object memberNo = session.getAttribute(
                                "memberNo");

                System.out.println(
                                "memberNo: "
                                                + memberNo);

                Long convertedMemberNo = convertToLong(
                                memberNo);

                if (convertedMemberNo != null) {

                        /*
                         * 이후 요청부터 표준 키를 사용하도록
                         * loginMemberNo에도 다시 저장한다.
                         */
                        session.setAttribute(
                                        "loginMemberNo",
                                        convertedMemberNo);

                        return convertedMemberNo;
                }

                /*
                 * MemberVO 전체 객체가 세션에 저장된 경우
                 */
                Object loginMemberObject = session.getAttribute(
                                "loginMember");

                System.out.println(
                                "loginMember 객체: "
                                                + loginMemberObject);

                if (loginMemberObject instanceof MemberVO loginMember) {

                        long loginMemberObjectNo = loginMember.getMemberNo();

                        if (loginMemberObjectNo > 0) {

                                session.setAttribute(
                                                "loginMemberNo",
                                                loginMemberObjectNo);

                                return loginMemberObjectNo;
                        }
                }

                /*
                 * 현재 저장된 전체 세션 키를 콘솔에 출력한다.
                 * 세션 속성명 불일치 확인용이다.
                 */
                printSessionAttributes(
                                session);

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
        private Long convertToLong(
                        Object value) {

                if (value == null) {
                        return null;
                }

                if (value instanceof Number number) {

                        long convertedValue = number.longValue();

                        return convertedValue > 0
                                        ? convertedValue
                                        : null;
                }

                if (value instanceof String stringValue) {

                        try {

                                long convertedValue = Long.parseLong(
                                                stringValue.trim());

                                return convertedValue > 0
                                                ? convertedValue
                                                : null;

                        } catch (NumberFormatException e) {

                                return null;
                        }
                }

                return null;
        }

        /*
         * =========================================================
         * 현재 세션 속성 전체 출력
         *
         * 개발 중 세션 속성명 확인을 위한 로그이다.
         * 로그인 세션 키가 통일된 후에는 삭제해도 된다.
         * =========================================================
         */
        private void printSessionAttributes(
                        HttpSession session) {

                System.out.println(
                                "===== 현재 세션 속성 전체 목록 =====");

                Enumeration<String> attributeNames = session.getAttributeNames();

                if (!attributeNames.hasMoreElements()) {

                        System.out.println(
                                        "저장된 세션 속성이 없습니다.");

                        return;
                }

                while (attributeNames.hasMoreElements()) {

                        String attributeName = attributeNames.nextElement();

                        Object attributeValue = session.getAttribute(
                                        attributeName);

                        System.out.println(
                                        attributeName
                                                        + " = "
                                                        + attributeValue);
                }
        }
}