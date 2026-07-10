package com.project.oditji.business.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(
            BusinessService businessService) {

        this.businessService = businessService;
    }

    // 사업자 메인
    @GetMapping("/main")
    public String main(Model model) {

        model.addAttribute("activeMenu", "main");

        return "business/main/businessMain";
    }

    // 상품 목록
    @GetMapping("/product/list")
    public String productList(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long memberNo = getMemberNo(session);

        if (memberNo == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "로그인이 필요한 서비스입니다.");

            return "redirect:/member/login";
        }

        BusinessVO business =
                businessService.getBusinessByMemberNo(
                        memberNo);

        if (business == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "등록된 사업자 정보가 없습니다.");

            return "redirect:/business/main";
        }

        List<GoodsManageVO> productList =
                businessService.getProductList(
                        business.getBusinessNo());

        model.addAttribute("business", business);
        model.addAttribute("productList", productList);
        model.addAttribute("activeMenu", "product");

        return "business/goods/productList";
    }

    // 상품 등록 화면
    @GetMapping("/product/register")
    public String productRegister(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long memberNo = getMemberNo(session);

        if (memberNo == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "로그인이 필요한 서비스입니다.");

            return "redirect:/member/login";
        }

        BusinessVO business =
                businessService.getBusinessByMemberNo(
                        memberNo);

        if (business == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "등록된 사업자 정보가 없습니다.");

            return "redirect:/business/main";
        }

        if (!"APPROVED".equals(business.getStatus())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "승인된 사업자만 상품을 등록할 수 있습니다.");

            return "redirect:/business/main";
        }

        List<ActorSearchVO> actorList =
                businessService.getActorList();

        model.addAttribute("business", business);
        model.addAttribute("actorList", actorList);
        model.addAttribute(
                "activeMenu",
                "productRegister");

        return "business/goods/productRegister";
    }

    // 상품 등록 처리
    @PostMapping("/product/register")
    public String productRegisterProcess(
            @ModelAttribute GoodsManageVO goodsManageVO,
            @RequestParam("productImage")
            MultipartFile productImage,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Long memberNo = getMemberNo(session);

        if (memberNo == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "로그인이 필요한 서비스입니다.");

            return "redirect:/member/login";
        }

        BusinessVO business =
                businessService.getBusinessByMemberNo(
                        memberNo);

        if (business == null) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "등록된 사업자 정보가 없습니다.");

            return "redirect:/business/main";
        }

        if (!"APPROVED".equals(business.getStatus())) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "승인된 사업자만 상품을 등록할 수 있습니다.");

            return "redirect:/business/main";
        }

        /*
         * BUSINESS_NO는 사용자가 전송한 값을 사용하지 않고
         * 로그인 회원과 연결된 사업자 번호를 서버에서 설정한다.
         */
        goodsManageVO.setBusinessNo(
                business.getBusinessNo());

        try {
            long productNo =
                    businessService.registerProduct(
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

    // 콘텐츠 검색 팝업
    @GetMapping("/content/search")
    public String contentSearch(
            @RequestParam(
                    value = "keyword",
                    required = false)
            String keyword,
            Model model) {

        List<ContentSearchVO> contentList =
                businessService.searchContentList(
                        keyword);

        model.addAttribute("keyword", keyword);
        model.addAttribute(
                "contentList",
                contentList);

        return "business/goods/contentSearch";
    }

    // 상품 수정
    @GetMapping("/product/update")
    public String productUpdate(Model model) {

        model.addAttribute(
                "activeMenu",
                "productUpdate");

        return "business/goods/productUpdate";
    }

    // 상품 삭제 요청
    @GetMapping("/product/delete")
    public String productDelete(Model model) {

        model.addAttribute(
                "activeMenu",
                "productDelete");

        return "business/goods/productDelete";
    }

    // 이벤트 목록
    @GetMapping("/event/list")
    public String eventList(Model model) {

        model.addAttribute("activeMenu", "event");

        return "business/event/eventList";
    }

    // 이벤트 등록
    @GetMapping("/event/register")
    public String eventRegister(Model model) {

        model.addAttribute(
                "activeMenu",
                "eventRegister");

        return "business/event/eventRegister";
    }

    // 이벤트 수정
    @GetMapping("/event/update")
    public String eventUpdate(Model model) {

        model.addAttribute(
                "activeMenu",
                "eventUpdate");

        return "business/event/eventUpdate";
    }

    // 이벤트 연장 요청
    @GetMapping("/event/extend")
    public String eventExtend(Model model) {

        model.addAttribute(
                "activeMenu",
                "eventExtend");

        return "business/event/eventExtend";
    }

    // 승인 관리
    @GetMapping("/approval")
    public String approvalList(
            @RequestParam(defaultValue = "product")
            String type,
            Model model) {

        model.addAttribute("currentType", type);
        model.addAttribute(
                "activeMenu",
                "approval");

        return "business/approval/approvalList";
    }

    // 정산 관리
    @GetMapping("/settlement/main")
    public String settlement(Model model) {

        model.addAttribute(
                "activeMenu",
                "settlement");

        return "business/settlement/settlementMain";
    }

    // 판매 현황
    @GetMapping("/settlement/sales")
    public String sales(Model model) {

        model.addAttribute(
                "activeMenu",
                "sales");

        return "business/settlement/salesStatus";
    }

    // 채팅
    @GetMapping("/chat")
    public String chat(Model model) {

        model.addAttribute(
                "activeMenu",
                "chat");

        return "business/community/businessChat";
    }

    // 주문 현황
    @GetMapping("/order/list")
    public String orderList(Model model) {

        model.addAttribute(
                "activeMenu",
                "order");

        return "business/order/orderList";
    }

    // 배송 관리
    @GetMapping("/order/detail")
    public String orderDetail(Model model) {

        model.addAttribute(
                "activeMenu",
                "delivery");

        return "business/order/orderDetail";
    }

    // 취소/환불 관리
    @GetMapping("/cancel/list")
    public String cancelList(Model model) {

        model.addAttribute(
                "activeMenu",
                "cancel");

        return "business/order/cancelList";
    }

    private Long getMemberNo(HttpSession session) {

        Object memberNo =
                session.getAttribute("memberNo");

        if (memberNo instanceof Number number) {
            return number.longValue();
        }

        return null;
    }
}