package com.project.oditji.event.controller;

import com.project.oditji.common.util.PaginationUtil;
import com.project.oditji.common.vo.PageVO;
import com.project.oditji.event.service.OttDiscountService;
import com.project.oditji.event.vo.OttDiscountVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discount")
public class OttDiscountApiController {

    /** [수정] ottDiscount.jsp와 동일하게 9개 단위 페이징 */
    private static final int DISCOUNT_PAGE_SIZE = 9;

    private final OttDiscountService ottDiscountService;

    public OttDiscountApiController(OttDiscountService ottDiscountService) {
        this.ottDiscountService = ottDiscountService;
    }

    /**
     * 1. 할인 정보 목록 조회 (GET)
     * URL: /api/discount?platform=NETFLIX&category=CARD&page=1
     * [수정] 플랫폼/카테고리 필터를 AJAX로 바꿀 때도 서버와 동일한 9개 단위 페이징을
     * 적용해야 하므로 page 파라미터를 추가하고, 프론트에서 페이지네이션 내비게이션을
     * 다시 그릴 수 있도록 currentPage/totalPage/totalCount를 응답에 함께 내려준다.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDiscountList(
            @RequestParam(value = "platform", required = false, defaultValue = "ALL") String platform,
            @RequestParam(value = "category", required = false, defaultValue = "ALL") String category,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page) {

        Map<String, Object> response = new HashMap<String, Object>();

        int totalCount = ottDiscountService.getDiscountListCount(platform, category);
        PageVO pagination = PaginationUtil.createPage(page, DISCOUNT_PAGE_SIZE, totalCount);

        List<OttDiscountVO> list =
                ottDiscountService.getDiscountList(platform, category, pagination.getCurrentPage(), DISCOUNT_PAGE_SIZE);

        response.put("status", "success");
        response.put("count", list.size());
        response.put("data", list);
        response.put("currentPage", pagination.getCurrentPage());
        response.put("totalPage", pagination.getTotalPage());
        response.put("totalCount", pagination.getTotalCount());

        return ResponseEntity.ok(response);
    }

    /**
     * 2. 할인 정보 단건 상세 조회 (GET)
     * URL: /api/discount/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDiscountDetail(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<String, Object>();
        OttDiscountVO discount = ottDiscountService.getDiscountDetail(id);

        if (discount == null) {
            response.put("status", "error");
            response.put("message", "해당 할인 정보를 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("status", "success");
        response.put("data", discount);
        return ResponseEntity.ok(response);
    }

    /**
     * 3. 할인 정보 신규 등록 (POST)
     * URL: /api/discount
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDiscount(@RequestBody OttDiscountVO discountVO) {
        Map<String, Object> response = new HashMap<String, Object>();

        boolean result = ottDiscountService.createDiscount(discountVO);
        if (result) {
            response.put("status", "success");
            response.put("message", "할인 정보가 정상적으로 등록되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("status", "error");
            response.put("message", "등록에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 4. 할인 정보 수정 (PUT)
     * URL: /api/discount/1
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDiscount(
            @PathVariable("id") Long id,
            @RequestBody OttDiscountVO discountVO) {

        Map<String, Object> response = new HashMap<String, Object>();
        discountVO.setDiscountId(id);

        boolean result = ottDiscountService.updateDiscount(discountVO);
        if (result) {
            response.put("status", "success");
            response.put("message", "할인 정보가 수정되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "수정에 실패했거나 대상을 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 5. 할인 정보 삭제 (DELETE)
     * URL: /api/discount/1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteDiscount(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<String, Object>();

        boolean result = ottDiscountService.deleteDiscount(id);
        if (result) {
            response.put("status", "success");
            response.put("message", "할인 정보가 비활성화(삭제)되었습니다.");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "삭제 대상을 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
