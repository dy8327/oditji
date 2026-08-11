package com.project.oditji.admin.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.common.vo.PageVO;
import com.project.oditji.event.service.OttDiscountService;
import com.project.oditji.event.vo.OttDiscountVO;

/**
 * 관리자 전용 OTT 할인 정보 관리 화면.
 *
 * event.controller.OttDiscountController(사용자용)/OttDiscountApiController와 달리,
 * 이 컨트롤러는 사업자 승인 플로우가 없는 단순 CRUD다. 관리자가 직접
 * 등록/수정/비활성화/재활성화를 수행하며, 기존 OttDiscountService를 그대로 재사용한다.
 * God Controller인 AdminController에 얹지 않고 별도 컨트롤러로 분리했다.
 */
@Controller
@RequestMapping("/admin/discount")
public class AdminOttDiscountController {

    private static final String DEFAULT_FILTER = "ALL";
    private static final String REDIRECT_PREFIX = "redirect:";
    private static final String FLASH_MESSAGE = "message";

    // 관리자 목록 화면 공용 페이징 설정 (AdminController의 ADMIN_PAGE_SIZE/ADMIN_PAGE_BLOCK_SIZE와 동일:
    // 한 페이지 10건, 페이지 번호 5개 단위 블록)
    private static final int ADMIN_PAGE_SIZE = 10;
    private static final int ADMIN_PAGE_BLOCK_SIZE = 5;
    private static final String ATTR_PAGINATION = "pagination";

    /**
     * PLATFORM_CODE -> 표기명(PLATFORM_NAME) 고정 매핑.
     * ottDiscount.jsp(사용자용) 필터 버튼에 나열된 플랫폼과 동일한 집합이며,
     * 관리자가 직접 표기명을 입력하지 않고 코드 선택만으로 정확한 이름이 채워지도록 서버에서 고정한다.
     */
    private static final Map<String, String> PLATFORM_NAMES = Map.of(
            "NETFLIX", "넷플릭스",
            "TVING", "티빙",
            "WAVVE", "웨이브",
            "DISNEY", "디즈니+",
            "WATCHA", "왓챠",
            "COUPANG", "쿠팡플레이"
    );

    private final OttDiscountService ottDiscountService;

    public AdminOttDiscountController(OttDiscountService ottDiscountService) {
        this.ottDiscountService = ottDiscountService;
    }

    /**
     * OTT 할인 관리 목록 화면
     * URL: GET /admin/discount/list
     * 공개용 목록과 달리 비활성화(IS_ACTIVE='N')된 항목도 함께 조회한다.
     */
    @GetMapping("/list")
    public String discountList(
            @RequestParam(value = "platform", required = false, defaultValue = DEFAULT_FILTER) String platform,
            @RequestParam(value = "category", required = false, defaultValue = DEFAULT_FILTER) String category,
            @RequestParam(value = "status", required = false, defaultValue = DEFAULT_FILTER) String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        int totalCount = ottDiscountService.getAdminDiscountListCount(platform, category, status);
        PageVO pagination = PaginationUtil.build(page, totalCount, ADMIN_PAGE_SIZE, ADMIN_PAGE_BLOCK_SIZE);

        List<OttDiscountVO> discountList = ottDiscountService.getAdminDiscountList(
                platform, category, status, pagination.getCurrentPage(), ADMIN_PAGE_SIZE);

        model.addAttribute("activeMenu", "discount");
        model.addAttribute("discountList", discountList);
        model.addAttribute("selectedPlatform", platform);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStatus", status);
        model.addAttribute(ATTR_PAGINATION, pagination);

        return "admin/discount/discountManage";
    }

    /**
     * 할인 정보 신규 등록
     * URL: POST /admin/discount/register
     */
    @PostMapping("/register")
    public String register(
            @RequestParam String platformCode,
            @RequestParam String category,
            @RequestParam String title,
            @RequestParam(required = false) Integer regularPrice,
            @RequestParam(required = false) Integer discountPrice,
            @RequestParam String discountSummary,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String cardOrCompany,
            @RequestParam(required = false) String targetUrl,
            @RequestParam(required = false) String badgeText,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(value = "filterPlatform", required = false, defaultValue = DEFAULT_FILTER) String filterPlatform,
            @RequestParam(value = "filterCategory", required = false, defaultValue = DEFAULT_FILTER) String filterCategory,
            @RequestParam(value = "filterStatus", required = false, defaultValue = DEFAULT_FILTER) String filterStatus,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            RedirectAttributes redirectAttributes) {

        OttDiscountVO discountVO = createDiscountVO(
                null,
                platformCode,
                category,
                title,
                regularPrice,
                discountPrice);
        applyDiscountDetails(
                discountVO,
                discountSummary,
                description,
                cardOrCompany,
                targetUrl,
                badgeText);
        applyDiscountPeriod(discountVO, startDate, endDate);

        try {
            boolean result = ottDiscountService.createDiscount(discountVO);
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE,
                    result ? "할인 정보를 등록했습니다." : "등록에 실패했습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "등록 중 오류가 발생했습니다. 입력값을 확인해 주세요.");
        }

        return REDIRECT_PREFIX + listRedirectUrl(filterPlatform, filterCategory, filterStatus, page);
    }

    /**
     * 할인 정보 수정
     * URL: POST /admin/discount/update
     */
    @PostMapping("/update")
    public String update(
            @RequestParam Long discountId,
            @RequestParam String platformCode,
            @RequestParam String category,
            @RequestParam String title,
            @RequestParam(required = false) Integer regularPrice,
            @RequestParam(required = false) Integer discountPrice,
            @RequestParam String discountSummary,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String cardOrCompany,
            @RequestParam(required = false) String targetUrl,
            @RequestParam(required = false) String badgeText,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(value = "filterPlatform", required = false, defaultValue = DEFAULT_FILTER) String filterPlatform,
            @RequestParam(value = "filterCategory", required = false, defaultValue = DEFAULT_FILTER) String filterCategory,
            @RequestParam(value = "filterStatus", required = false, defaultValue = DEFAULT_FILTER) String filterStatus,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            RedirectAttributes redirectAttributes) {

        OttDiscountVO discountVO = createDiscountVO(
                discountId,
                platformCode,
                category,
                title,
                regularPrice,
                discountPrice);
        applyDiscountDetails(
                discountVO,
                discountSummary,
                description,
                cardOrCompany,
                targetUrl,
                badgeText);
        applyDiscountPeriod(discountVO, startDate, endDate);

        try {
            boolean result = ottDiscountService.updateDiscount(discountVO);
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE,
                    result ? "할인 정보를 수정했습니다." : "수정 대상을 찾을 수 없습니다.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute(FLASH_MESSAGE, "수정 중 오류가 발생했습니다. 입력값을 확인해 주세요.");
        }

        return REDIRECT_PREFIX + listRedirectUrl(filterPlatform, filterCategory, filterStatus, page);
    }

    /**
     * 할인 정보 비활성화 (소프트 삭제)
     * URL: POST /admin/discount/deactivate
     */
    @PostMapping("/deactivate")
    public String deactivate(
            @RequestParam Long discountId,
            @RequestParam(value = "filterPlatform", required = false, defaultValue = DEFAULT_FILTER) String filterPlatform,
            @RequestParam(value = "filterCategory", required = false, defaultValue = DEFAULT_FILTER) String filterCategory,
            @RequestParam(value = "filterStatus", required = false, defaultValue = DEFAULT_FILTER) String filterStatus,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            RedirectAttributes redirectAttributes) {

        boolean result = ottDiscountService.deleteDiscount(discountId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE,
                result ? "할인 정보를 비활성화했습니다." : "비활성화 대상을 찾을 수 없습니다.");

        return REDIRECT_PREFIX + listRedirectUrl(filterPlatform, filterCategory, filterStatus, page);
    }

    /**
     * 비활성화된 할인 정보 재활성화
     * URL: POST /admin/discount/activate
     */
    @PostMapping("/activate")
    public String activate(
            @RequestParam Long discountId,
            @RequestParam(value = "filterPlatform", required = false, defaultValue = DEFAULT_FILTER) String filterPlatform,
            @RequestParam(value = "filterCategory", required = false, defaultValue = DEFAULT_FILTER) String filterCategory,
            @RequestParam(value = "filterStatus", required = false, defaultValue = DEFAULT_FILTER) String filterStatus,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            RedirectAttributes redirectAttributes) {

        boolean result = ottDiscountService.activateDiscount(discountId);
        redirectAttributes.addFlashAttribute(FLASH_MESSAGE,
                result ? "할인 정보를 다시 활성화했습니다." : "재활성화 대상을 찾을 수 없습니다.");

        return REDIRECT_PREFIX + listRedirectUrl(filterPlatform, filterCategory, filterStatus, page);
    }

    /** 등록/수정 폼의 핵심 값을 OttDiscountVO로 변환한다. platformName은 platformCode로부터 서버에서 고정 매핑한다. */
    private OttDiscountVO createDiscountVO(
            Long discountId,
            String platformCode,
            String category,
            String title,
            Integer regularPrice,
            Integer discountPrice) {

        OttDiscountVO discountVO = new OttDiscountVO();
        discountVO.setDiscountId(discountId);
        discountVO.setPlatformCode(platformCode);
        discountVO.setPlatformName(PLATFORM_NAMES.getOrDefault(platformCode, platformCode));
        discountVO.setCategory(category);
        discountVO.setTitle(title);
        discountVO.setRegularPrice(regularPrice);
        discountVO.setDiscountPrice(discountPrice);
        return discountVO;
    }

    /** Sonar 파라미터 수 제한을 지키면서 등록/수정 공통 상세값을 분리해 설정한다. */
    private void applyDiscountDetails(
            OttDiscountVO discountVO,
            String discountSummary,
            String description,
            String cardOrCompany,
            String targetUrl,
            String badgeText) {

        discountVO.setDiscountSummary(discountSummary);
        discountVO.setDescription(description);
        discountVO.setCardOrCompany(cardOrCompany);
        discountVO.setTargetUrl(targetUrl);
        discountVO.setBadgeText(badgeText);
    }

    private void applyDiscountPeriod(
            OttDiscountVO discountVO,
            String startDate,
            String endDate) {

        discountVO.setStartDate(startDate);
        discountVO.setEndDate(endDate);
    }

    /** 등록/수정/비활성화/재활성화 처리 후 방금 보고 있던 필터(platform/category/status)와 페이지 그대로 목록으로 돌아가기 위한 리다이렉트 URL. */
    private String listRedirectUrl(String platform, String category, String status, int page) {

        // [병합 해결] 기존 경로 생성 방식은 유지하고, develop의 page 유지 기능을 함께 반영합니다.
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .pathSegment("admin", "discount", "list")
                .queryParam("page", page);

        if (platform != null && !platform.isBlank()) {
            builder.queryParam("platform", platform);
        }
        if (category != null && !category.isBlank()) {
            builder.queryParam("category", category);
        }
        if (status != null && !status.isBlank()) {
            builder.queryParam("status", status);
        }

        return builder.build().toUriString();
    }
}