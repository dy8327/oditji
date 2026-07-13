package com.project.oditji.business.controller;

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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/business")
public class BusinessController {

        private final BusinessService businessService;

        public BusinessController(
                        BusinessService businessService) {

                this.businessService = businessService;
        }

        /*
         * =========================================================
         * 사업자 메인
         * =========================================================
         */
        @GetMapping("/main")
        public String main(
                        HttpSession session,
                        Model model,
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

                model.addAttribute(
                                "business",
                                business);

                model.addAttribute(
                                "activeMenu",
                                "main");

                return "business/main/businessMain";
        }

        /*
         * =========================================================
         * 상품 목록
         * =========================================================
         */
        @GetMapping("/product/list")
        public String productList(
                        HttpSession session,
                        Model model,
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

                List<GoodsManageVO> productList = businessService
                                .getProductListByBusinessNo(
                                                business.getBusinessNo());

                model.addAttribute("business", business);

                model.addAttribute("productList", productList);

                model.addAttribute("activeMenu", "product");

                return "business/goods/productList";
        }

        /*
         * =========================================================
         * 상품 등록 화면
         * =========================================================
         */
        @GetMapping("/product/register")
        public String productRegister(
                        HttpSession session,
                        Model model,
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
                                        "승인된 사업자만 상품을 등록할 수 있습니다.");

                        return "redirect:/business/main";
                }

                /*
                 * 상품 등록 실패 후 다시 진입한 경우에는
                 * RedirectAttributes로 전달된 productForm을 유지한다.
                 */
                if (!model.containsAttribute(
                                "productForm")) {

                        model.addAttribute(
                                        "productForm",
                                        new GoodsManageVO());
                }

                model.addAttribute("business", business);

                model.addAttribute("activeMenu", "productRegister");

                return "business/goods/productRegister";
        }

        /*
         * =========================================================
         * 상품 등록 처리
         * =========================================================
         */
        @PostMapping("/product/register")
        public String productRegisterProcess(
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
                                        "승인된 사업자만 상품을 등록할 수 있습니다.");

                        return "redirect:/business/main";
                }

                /*
                 * 화면에서 BUSINESS_NO가 전달되더라도 사용하지 않는다.
                 * 현재 로그인 회원과 연결된 사업자 번호를 서버에서 설정한다.
                 */
                goodsManageVO.setBusinessNo(
                                business.getBusinessNo());

                /*
                 * 배우 선택값이 없거나 0 이하이면
                 * PRODUCT.ACTOR_NO에 NULL이 저장되도록 처리한다.
                 */
                if (goodsManageVO.getActorNo() != null
                                && goodsManageVO.getActorNo() <= 0) {

                        goodsManageVO.setActorNo(null);
                }

                try {

                        long productNo = businessService.registerProduct(goodsManageVO, productImage);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "상품 등록 요청이 완료되었습니다. "
                                                        + "관리자 승인 후 판매됩니다.");

                        redirectAttributes.addFlashAttribute("registeredProductNo", productNo);

                        return "redirect:/business/product/list";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

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
         * =========================================================
         * 콘텐츠 검색 팝업 화면
         * =========================================================
         */
        @GetMapping("/content/search")
        public String contentSearch(
                        @RequestParam(value = "keyword", required = false) String keyword,
                        Model model) {

                List<ContentSearchVO> contentList = businessService.getContentList(
                                keyword);

                model.addAttribute(
                                "keyword",
                                keyword);

                model.addAttribute(
                                "contentList",
                                contentList);

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

                List<ContentSearchVO> contentList = businessService.getContentList(
                                keyword);

                if (contentList == null) {
                        return Collections.emptyList();
                }

                return contentList;
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
        public ContentSearchVO contentDetailApi(
                        @RequestParam("contentNo") long contentNo) {

                return businessService.getContentByNo(
                                contentNo);
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
        public List<ActorSearchVO> actorListByContentApi(
                        @RequestParam("contentNo") long contentNo) {

                System.out.println("===== 콘텐츠별 배우 조회 API =====");
                System.out.println("contentNo: " + contentNo);

                if (contentNo <= 0) {
                        return Collections.emptyList();
                }

                List<ActorSearchVO> actorList = businessService.getActorListByContentNo(
                                contentNo);

                System.out.println("조회된 배우 수: " + actorList.size());

                for (ActorSearchVO actor : actorList) {
                        System.out.println("배우: "
                                        + actor.getActorName()
                                        + ", 배역: "
                                        + actor.getCharacterName());
                }

                return actorList;
        }

        /*
         * =========================================================
         * 상품 수정
         * =========================================================
         */
        @GetMapping("/product/update")
        public String productUpdate(
                        Model model) {

                model.addAttribute("activeMenu", "productUpdate");

                return "business/goods/productUpdate";
        }

        /*
         * =========================================================
         * 상품 삭제 요청
         * =========================================================
         */
        @GetMapping("/product/delete")
        public String productDelete(
                        Model model) {

                model.addAttribute("activeMenu", "productDelete");

                return "business/goods/productDelete";
        }

        /*
         * =========================================================
         * 이벤트 목록
         * =========================================================
         */
        @GetMapping("/event/list")
        public String eventList(
                        Model model) {

                model.addAttribute("activeMenu", "event");

                return "business/event/eventList";
        }

        /*
         * =========================================================
         * 이벤트 등록
         * =========================================================
         */
        @GetMapping("/event/register")
        public String eventRegister(
                        Model model) {

                model.addAttribute("activeMenu", "eventRegister");

                return "business/event/eventRegister";
        }

        /*
         * =========================================================
         * 이벤트 수정
         * =========================================================
         */
        @GetMapping("/event/update")
        public String eventUpdate(
                        Model model) {

                model.addAttribute("activeMenu", "eventUpdate");

                return "business/event/eventUpdate";
        }

        /*
         * =========================================================
         * 이벤트 연장 요청
         * =========================================================
         */
        @GetMapping("/event/extend")
        public String eventExtend(
                        Model model) {

                model.addAttribute("activeMenu", "eventExtend");

                return "business/event/eventExtend";
        }

        /*
         * =========================================================
         * 승인 관리
         * =========================================================
         */
        @GetMapping("/approval")
        public String approvalList(
                        @RequestParam(defaultValue = "product") String type,
                        Model model) {

                model.addAttribute("currentType", type);

                model.addAttribute("activeMenu", "approval");

                return "business/approval/approvalList";
        }

        /*
         * =========================================================
         * 정산 관리
         * =========================================================
         */
        @GetMapping("/settlement/main")
        public String settlement(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "settlement");

                return "business/settlement/settlementMain";
        }

        /*
         * =========================================================
         * 판매 현황
         * =========================================================
         */
        @GetMapping("/settlement/sales")
        public String sales(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "sales");

                return "business/settlement/salesStatus";
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

        /*
         * =========================================================
         * 주문 현황
         * =========================================================
         */
        @GetMapping("/order/list")
        public String orderList(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "order");

                return "business/order/orderList";
        }

        /*
         * =========================================================
         * 배송 관리
         * =========================================================
         */
        @GetMapping("/order/detail")
        public String orderDetail(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "delivery");

                return "business/order/orderDetail";
        }

        /*
         * =========================================================
         * 취소 및 환불 관리
         * =========================================================
         */
        @GetMapping("/cancel/list")
        public String cancelList(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "cancel");

                return "business/order/cancelList";
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

                Long convertedLoginMemberNo = convertToLong(loginMemberNo);

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

                Long convertedMemberNo = convertToLong(memberNo);

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
                printSessionAttributes(session);

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