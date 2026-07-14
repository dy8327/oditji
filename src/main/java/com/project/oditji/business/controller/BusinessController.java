package com.project.oditji.business.controller;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.time.LocalDate;

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

                List<GoodsManageVO> productList = businessService.getProductListByBusinessNo(
                                business.getBusinessNo());

                model.addAttribute(
                                "business",
                                business);

                model.addAttribute(
                                "productList",
                                productList);

                model.addAttribute(
                                "activeMenu",
                                "product");

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

                model.addAttribute(
                                "business",
                                business);

                model.addAttribute(
                                "activeMenu",
                                "productRegister");

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

                        goodsManageVO.setActorNo(
                                        null);
                }

                try {

                        long productNo = businessService.registerProduct(
                                        goodsManageVO,
                                        productImage);

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "상품 등록 요청이 완료되었습니다. "
                                                        + "관리자 승인 후 판매됩니다.");

                        redirectAttributes.addFlashAttribute(
                                        "registeredProductNo",
                                        productNo);

                        return "redirect:/business/product/list";

                } catch (IllegalArgumentException
                                | IllegalStateException e) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        e.getMessage());

                        redirectAttributes.addFlashAttribute(
                                        "productForm",
                                        goodsManageVO);

                        return "redirect:/business/product/register";

                } catch (Exception e) {

                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "상품 등록 중 오류가 발생했습니다.");

                        redirectAttributes.addFlashAttribute(
                                        "productForm",
                                        goodsManageVO);

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

                System.out.println(
                                "===== 콘텐츠별 배우 조회 API =====");

                System.out.println(
                                "contentNo: "
                                                + contentNo);

                if (contentNo <= 0) {
                        return Collections.emptyList();
                }

                List<ActorSearchVO> actorList = businessService.getActorListByContentNo(
                                contentNo);

                System.out.println(
                                "조회된 배우 수: "
                                                + actorList.size());

                for (ActorSearchVO actor : actorList) {

                        System.out.println(
                                        "배우: "
                                                        + actor.getActorName()
                                                        + ", 배역: "
                                                        + actor.getCharacterName());
                }

                return actorList;
        }

        /*
         * =========================================================
         * 상품 수정 화면
         *
         * 상품 번호 없이 직접 접근한 경우에는
         * 상품 목록으로 이동한다.
         * =========================================================
         */
        @GetMapping("/product/update")
        public String productUpdate(
                        @RequestParam(value = "productNo", required = false) Long productNo,

                        HttpSession session,
                        Model model,
                        RedirectAttributes redirectAttributes) {

                if (productNo == null
                                || productNo <= 0) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "수정할 상품을 선택해주세요.");

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
         * 이벤트 목록
         * =========================================================
         */
        @GetMapping("/event/list")
        public String eventList(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "event");

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
         * 현재 단계에서는 등록 요청값 검증과 파일 정보 확인까지 처리한다.
         * EVENT 테이블에 실제 저장하려면 이후 BusinessService에
         * 이벤트 등록 메서드와 MyBatis Mapper를 연결해야 한다.
         * =========================================================
         */
        @PostMapping("/event/register")
        public String eventRegisterProcess(
                        @RequestParam("eventTitle") String eventTitle,

                        @RequestParam(value = "eventContent", required = false) String eventContent,

                        @RequestParam("startDate") LocalDate startDate,

                        @RequestParam("endDate") LocalDate endDate,

                        @RequestParam(value = "productName", required = false) String productName,

                        /*
                         * 현재 JSP가 checkbox 형태이므로 여러 상태값이 전달될 수 있다.
                         * 이벤트 상태는 하나만 가져야 하므로 첫 번째 값을 사용한다.
                         *
                         * JSP의 status 입력을 radio로 수정하면
                         * String status 하나로 받아도 된다.
                         */
                        @RequestParam(value = "status", required = false) List<String> statusList,

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

                /*
                 * 필수 입력값 검증
                 */
                if (eventTitle == null
                                || eventTitle.trim().isEmpty()) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트명을 입력해주세요.");

                        return "redirect:/business/event/register";
                }

                /*
                 * 이벤트 날짜 입력값 검증
                 */
                if (startDate == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트 시작일을 선택해주세요.");

                        return "redirect:/business/event/register";
                }

                if (endDate == null) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트 종료일을 선택해주세요.");

                        return "redirect:/business/event/register";
                }

                /*
                 * 종료일이 시작일보다 빠른 경우 등록을 중단한다.
                 */
                if (endDate.isBefore(
                                startDate)) {

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트 종료일은 시작일보다 빠를 수 없습니다.");

                        return "redirect:/business/event/register";
                }

                /*
                 * checkbox에서 여러 상태값이 전달된 경우
                 * 첫 번째 상태값만 사용한다.
                 */
                String status = "WAITING";

                if (statusList != null
                                && !statusList.isEmpty()) {

                        status = statusList.get(0);
                }

                /*
                 * 허용하지 않는 상태값이 전달된 경우
                 * 기본값인 WAITING으로 처리한다.
                 */
                if (!"WAITING".equals(status)
                                && !"ACTIVE".equals(status)
                                && !"ENDED".equals(status)) {

                        status = "WAITING";
                }

                try {

                        System.out.println(
                                        "===== 이벤트 등록 요청 =====");

                        System.out.println(
                                        "사업자 번호: "
                                                        + business.getBusinessNo());

                        System.out.println(
                                        "이벤트명: "
                                                        + eventTitle.trim());

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
                                        "연결 상품명: "
                                                        + productName);

                        System.out.println(
                                        "이벤트 상태: "
                                                        + status);

                        if (eventImage != null
                                        && !eventImage.isEmpty()) {

                                System.out.println(
                                                "이벤트 이미지 파일명: "
                                                                + eventImage.getOriginalFilename());

                                System.out.println(
                                                "이벤트 이미지 크기: "
                                                                + eventImage.getSize());
                        }

                        /*
                         * EVENT 테이블 저장 기능 구현 시 아래 형태로
                         * BusinessService를 호출한다.
                         *
                         * businessService.registerEvent(
                         * business.getBusinessNo(),
                         * eventTitle,
                         * eventContent,
                         * startDate,
                         * endDate,
                         * productName,
                         * status,
                         * eventImage);
                         */

                        redirectAttributes.addFlashAttribute(
                                        "successMessage",
                                        "이벤트 등록 요청이 접수되었습니다.");

                        return "redirect:/business/event/list";

                } catch (Exception e) {

                        e.printStackTrace();

                        redirectAttributes.addFlashAttribute(
                                        "errorMessage",
                                        "이벤트 등록 요청 처리 중 오류가 발생했습니다.");

                        return "redirect:/business/event/register";
                }
        }

        /*
         * =========================================================
         * 이벤트 수정
         * =========================================================
         */
        @GetMapping("/event/update")
        public String eventUpdate(
                        Model model) {

                model.addAttribute(
                                "activeMenu",
                                "eventUpdate");

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

                model.addAttribute(
                                "activeMenu",
                                "eventExtend");

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

                model.addAttribute(
                                "currentType",
                                type);

                model.addAttribute(
                                "activeMenu",
                                "approval");

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