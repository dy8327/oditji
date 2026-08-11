package com.project.oditji.event.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.project.oditji.event.service.OttDiscountService;
import com.project.oditji.event.vo.OttDiscountVO;

/** OTT 할인 REST API의 목록, 상세 및 CRUD 성공/실패 응답을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class OttDiscountApiControllerCoverageTest {

    @Mock
    private OttDiscountService ottDiscountService;

    private OttDiscountApiController controller;

    @BeforeEach
    void setUp() {
        controller = new OttDiscountApiController(ottDiscountService);
    }

    @Test
    void listShouldClampPageAndReturnPaginationMetadata() {
        OttDiscountVO first = new OttDiscountVO();
        OttDiscountVO second = new OttDiscountVO();
        when(ottDiscountService.getDiscountListCount("NETFLIX", "CARD"))
                .thenReturn(10);
        when(ottDiscountService.getDiscountList("NETFLIX", "CARD", 2, 8))
                .thenReturn(List.of(first, second));

        ResponseEntity<Map<String, Object>> response =
                controller.getDiscountList("NETFLIX", "CARD", 99);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().get("status"));
        assertEquals(2, response.getBody().get("count"));
        assertEquals(List.of(first, second), response.getBody().get("data"));
        assertEquals(2, response.getBody().get("currentPage"));
        assertEquals(2, response.getBody().get("totalPage"));
        assertEquals(10, response.getBody().get("totalCount"));
    }

    @Test
    void detailShouldReturnSuccessOrNotFound() {
        OttDiscountVO discount = new OttDiscountVO();
        when(ottDiscountService.getDiscountDetail(1L)).thenReturn(discount);

        ResponseEntity<Map<String, Object>> found = controller.getDiscountDetail(1L);

        assertEquals(HttpStatus.OK, found.getStatusCode());
        assertEquals("success", found.getBody().get("status"));
        assertSame(discount, found.getBody().get("data"));

        when(ottDiscountService.getDiscountDetail(2L)).thenReturn(null);

        ResponseEntity<Map<String, Object>> missing = controller.getDiscountDetail(2L);

        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals("error", missing.getBody().get("status"));
        assertEquals("해당 할인 정보를 찾을 수 없습니다.", missing.getBody().get("message"));
    }

    @Test
    void createShouldCoverCreatedAndServerErrorResponses() {
        OttDiscountVO successRequest = new OttDiscountVO();
        when(ottDiscountService.createDiscount(successRequest)).thenReturn(true);

        ResponseEntity<Map<String, Object>> created = controller.createDiscount(successRequest);

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertEquals("success", created.getBody().get("status"));
        assertEquals("할인 정보가 정상적으로 등록되었습니다.", created.getBody().get("message"));

        OttDiscountVO failureRequest = new OttDiscountVO();
        when(ottDiscountService.createDiscount(failureRequest)).thenReturn(false);

        ResponseEntity<Map<String, Object>> failed = controller.createDiscount(failureRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, failed.getStatusCode());
        assertEquals("error", failed.getBody().get("status"));
        assertEquals("등록에 실패했습니다.", failed.getBody().get("message"));
    }

    @Test
    void updateShouldSetPathIdAndCoverSuccessAndNotFound() {
        OttDiscountVO successRequest = new OttDiscountVO();
        when(ottDiscountService.updateDiscount(any(OttDiscountVO.class))).thenReturn(true);

        ResponseEntity<Map<String, Object>> success = controller.updateDiscount(5L, successRequest);

        assertEquals(HttpStatus.OK, success.getStatusCode());
        assertEquals("success", success.getBody().get("status"));
        assertEquals("할인 정보가 수정되었습니다.", success.getBody().get("message"));
        assertEquals(5L, successRequest.getDiscountId());

        ArgumentCaptor<OttDiscountVO> captor = ArgumentCaptor.forClass(OttDiscountVO.class);
        verify(ottDiscountService).updateDiscount(captor.capture());
        assertSame(successRequest, captor.getValue());

        OttDiscountVO failureRequest = new OttDiscountVO();
        when(ottDiscountService.updateDiscount(failureRequest)).thenReturn(false);

        ResponseEntity<Map<String, Object>> failed = controller.updateDiscount(6L, failureRequest);

        assertEquals(HttpStatus.NOT_FOUND, failed.getStatusCode());
        assertEquals("error", failed.getBody().get("status"));
        assertEquals("수정에 실패했거나 대상을 찾을 수 없습니다.", failed.getBody().get("message"));
        assertEquals(6L, failureRequest.getDiscountId());
    }

    @Test
    void deleteShouldCoverSuccessAndNotFound() {
        when(ottDiscountService.deleteDiscount(7L)).thenReturn(true);

        ResponseEntity<Map<String, Object>> success = controller.deleteDiscount(7L);

        assertEquals(HttpStatus.OK, success.getStatusCode());
        assertEquals("success", success.getBody().get("status"));
        assertEquals("할인 정보가 비활성화(삭제)되었습니다.", success.getBody().get("message"));

        when(ottDiscountService.deleteDiscount(8L)).thenReturn(false);

        ResponseEntity<Map<String, Object>> failed = controller.deleteDiscount(8L);

        assertEquals(HttpStatus.NOT_FOUND, failed.getStatusCode());
        assertEquals("error", failed.getBody().get("status"));
        assertEquals("삭제 대상을 찾을 수 없습니다.", failed.getBody().get("message"));
    }
}