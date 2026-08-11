package com.project.oditji.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.notification.dao.RestockRequestDAO;
import com.project.oditji.notification.vo.RestockRequestVO;

/**
 * 재입고 알림 신청 서비스의 유효성 검사, 품절/중복 여부,
 * 상품/옵션별 등록·취소 및 사업자 알림 분기를 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class RestockRequestServiceImplCoverageTest {

    private static final Long MEMBER_NO = 10L;
    private static final Long PRODUCT_NO = 20L;
    private static final Long OPTION_NO = 30L;
    private static final String PRODUCT_NAME = "테스트 상품";
    private static final String INVALID_MEMBER_MESSAGE = "올바르지 않은 회원 번호입니다.";
    private static final String INVALID_PRODUCT_MESSAGE = "올바르지 않은 상품 번호입니다.";
    private static final String INVALID_OPTION_MESSAGE = "올바르지 않은 상품 옵션 번호입니다.";
    private static final String REQUEST_FAILED_MESSAGE = "재입고 알림 신청에 실패했습니다.";

    @Mock
    private RestockRequestDAO restockRequestDAO;

    @Mock
    private NotificationService notificationService;

    private RestockRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RestockRequestServiceImpl(
                restockRequestDAO,
                notificationService);
    }

    @Test
    void isRequestedShouldRejectNullAndNonPositiveMemberNumbers() {
        IllegalArgumentException nullException = assertThrows(
                IllegalArgumentException.class,
                () -> service.isRequested(null, PRODUCT_NO, null));
        assertEquals(INVALID_MEMBER_MESSAGE, nullException.getMessage());

        IllegalArgumentException zeroException = assertThrows(
                IllegalArgumentException.class,
                () -> service.isRequested(0L, PRODUCT_NO, null));
        assertEquals(INVALID_MEMBER_MESSAGE, zeroException.getMessage());

        verifyNoInteractions(restockRequestDAO, notificationService);
    }

    @Test
    void isRequestedShouldRejectNullAndNonPositiveProductNumbers() {
        IllegalArgumentException nullException = assertThrows(
                IllegalArgumentException.class,
                () -> service.isRequested(MEMBER_NO, null, null));
        assertEquals(INVALID_PRODUCT_MESSAGE, nullException.getMessage());

        IllegalArgumentException zeroException = assertThrows(
                IllegalArgumentException.class,
                () -> service.isRequested(MEMBER_NO, 0L, null));
        assertEquals(INVALID_PRODUCT_MESSAGE, zeroException.getMessage());

        verifyNoInteractions(restockRequestDAO, notificationService);
    }

    @Test
    void isRequestedShouldReturnBothProductRequestStates() {
        when(restockRequestDAO.countActiveProductRequest(MEMBER_NO, PRODUCT_NO))
                .thenReturn(1)
                .thenReturn(0);

        assertTrue(service.isRequested(MEMBER_NO, PRODUCT_NO, null));
        assertFalse(service.isRequested(MEMBER_NO, PRODUCT_NO, null));

        verify(restockRequestDAO, times(2))
                .countActiveProductRequest(MEMBER_NO, PRODUCT_NO);
    }

    @Test
    void isRequestedShouldRejectInvalidOptionNumber() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.isRequested(MEMBER_NO, PRODUCT_NO, 0L));

        assertEquals(INVALID_OPTION_MESSAGE, exception.getMessage());
        verifyNoInteractions(restockRequestDAO, notificationService);
    }

    @Test
    void isRequestedShouldReturnBothOptionRequestStates() {
        when(restockRequestDAO.countActiveOptionRequest(
                MEMBER_NO, PRODUCT_NO, OPTION_NO))
                .thenReturn(1)
                .thenReturn(0);

        assertTrue(service.isRequested(MEMBER_NO, PRODUCT_NO, OPTION_NO));
        assertFalse(service.isRequested(MEMBER_NO, PRODUCT_NO, OPTION_NO));

        verify(restockRequestDAO, times(2))
                .countActiveOptionRequest(MEMBER_NO, PRODUCT_NO, OPTION_NO);
    }

    @Test
    void requestProductShouldRejectMissingProduct() {
        when(restockRequestDAO.selectProductStock(PRODUCT_NO)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, null));

        assertEquals("존재하지 않는 상품입니다.", exception.getMessage());
        verify(restockRequestDAO, never()).insertRestockRequest(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestProductShouldRejectProductThatIsInStock() {
        when(restockRequestDAO.selectProductStock(PRODUCT_NO)).thenReturn(1);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, null));

        assertEquals(
                "현재 재고가 있는 상품은 재입고 알림을 신청할 수 없습니다.",
                exception.getMessage());
        verify(restockRequestDAO, never()).insertRestockRequest(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestProductShouldReturnWithoutInsertWhenAlreadyRequested() {
        when(restockRequestDAO.selectProductStock(PRODUCT_NO)).thenReturn(0);
        when(restockRequestDAO.countActiveProductRequest(MEMBER_NO, PRODUCT_NO))
                .thenReturn(1);

        service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, null);

        verify(restockRequestDAO, never()).insertRestockRequest(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestProductShouldThrowWhenInsertDoesNotAffectOneRow() {
        stubNewProductRequest();
        when(restockRequestDAO.insertRestockRequest(any(RestockRequestVO.class)))
                .thenReturn(0);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, null));

        assertEquals(REQUEST_FAILED_MESSAGE, exception.getMessage());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestProductShouldInsertWaitingRequestAndNotifyOwner() {
        stubNewProductRequest();
        when(restockRequestDAO.insertRestockRequest(any(RestockRequestVO.class)))
                .thenReturn(1);
        when(restockRequestDAO.selectProductName(PRODUCT_NO))
                .thenReturn(PRODUCT_NAME);
        when(restockRequestDAO.countWaitingProductRequests(PRODUCT_NO))
                .thenReturn(3);

        service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, null);

        ArgumentCaptor<RestockRequestVO> captor =
                ArgumentCaptor.forClass(RestockRequestVO.class);
        verify(restockRequestDAO).insertRestockRequest(captor.capture());

        RestockRequestVO saved = captor.getValue();
        assertEquals(MEMBER_NO, saved.getMemberNo());
        assertEquals(PRODUCT_NO, saved.getProductNo());
        assertNull(saved.getOptionNo());
        assertEquals("WAITING", saved.getStatus());

        verify(notificationService).createForProductOwner(
                PRODUCT_NO,
                "RESTOCK_REQUEST",
                "[재입고 요청] " + PRODUCT_NAME,
                "'테스트 상품' 상품에 재입고 알림 신청이 들어왔습니다. 현재 재입고 대기 3명",
                "/business/product/list",
                "PRODUCT",
                PRODUCT_NO);
    }

    @Test
    void requestProductShouldUseDefaultNameWhenNameIsNullOrBlank() {
        stubNewProductRequest();
        when(restockRequestDAO.insertRestockRequest(any(RestockRequestVO.class)))
                .thenReturn(1);
        when(restockRequestDAO.selectProductName(PRODUCT_NO))
                .thenReturn(null)
                .thenReturn("   ");
        when(restockRequestDAO.countWaitingProductRequests(PRODUCT_NO))
                .thenReturn(1);

        service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, null);
        service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, null);

        verify(notificationService, times(2))
                .createForProductOwner(
                        PRODUCT_NO,
                        "RESTOCK_REQUEST",
                        "[재입고 요청] 상품",
                        "'상품' 상품에 재입고 알림 신청이 들어왔습니다. 현재 재입고 대기 1명",
                        "/business/product/list",
                        "PRODUCT",
                        PRODUCT_NO);
    }

    @Test
    void requestOptionShouldRejectMissingOption() {
        when(restockRequestDAO.selectOptionStock(PRODUCT_NO, OPTION_NO))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.requestRestockNotification(
                        MEMBER_NO, PRODUCT_NO, OPTION_NO));

        assertEquals("존재하지 않는 상품 옵션입니다.", exception.getMessage());
        verify(restockRequestDAO, never()).insertRestockRequest(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestOptionShouldRejectOptionThatIsInStock() {
        when(restockRequestDAO.selectOptionStock(PRODUCT_NO, OPTION_NO))
                .thenReturn(2);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.requestRestockNotification(
                        MEMBER_NO, PRODUCT_NO, OPTION_NO));

        assertEquals(
                "현재 재고가 있는 옵션은 재입고 알림을 신청할 수 없습니다.",
                exception.getMessage());
        verify(restockRequestDAO, never()).insertRestockRequest(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestOptionShouldReturnWithoutInsertWhenAlreadyRequested() {
        when(restockRequestDAO.selectOptionStock(PRODUCT_NO, OPTION_NO))
                .thenReturn(0);
        when(restockRequestDAO.countActiveOptionRequest(
                MEMBER_NO, PRODUCT_NO, OPTION_NO))
                .thenReturn(1);

        service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, OPTION_NO);

        verify(restockRequestDAO, never()).insertRestockRequest(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestOptionShouldInsertAndUseNormalizedOptionLabels() {
        stubNewOptionRequest();
        when(restockRequestDAO.insertRestockRequest(any(RestockRequestVO.class)))
                .thenReturn(1);
        when(restockRequestDAO.selectProductName(PRODUCT_NO))
                .thenReturn(PRODUCT_NAME);
        when(restockRequestDAO.selectOptionColorName(PRODUCT_NO, OPTION_NO))
                .thenReturn(null);
        when(restockRequestDAO.selectOptionSizeName(PRODUCT_NO, OPTION_NO))
                .thenReturn("   ");
        when(restockRequestDAO.countWaitingOptionRequests(PRODUCT_NO, OPTION_NO))
                .thenReturn(2);

        service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, OPTION_NO);

        ArgumentCaptor<RestockRequestVO> captor =
                ArgumentCaptor.forClass(RestockRequestVO.class);
        verify(restockRequestDAO).insertRestockRequest(captor.capture());
        assertEquals(OPTION_NO, captor.getValue().getOptionNo());

        verify(notificationService).createForProductOwner(
                PRODUCT_NO,
                "RESTOCK_OPTION_REQUEST",
                "[재입고 요청] " + PRODUCT_NAME,
                "'테스트 상품'의 색상 미지정 / 사이즈 미지정 옵션에 재입고 알림 신청이 들어왔습니다. (현재 재입고 대기: 2명)",
                "/business/product/list",
                "PRODUCT",
                PRODUCT_NO);
    }

    @Test
    void requestOptionShouldKeepNonBlankOptionLabels() {
        stubNewOptionRequest();
        when(restockRequestDAO.insertRestockRequest(any(RestockRequestVO.class)))
                .thenReturn(1);
        when(restockRequestDAO.selectProductName(PRODUCT_NO))
                .thenReturn(PRODUCT_NAME);
        when(restockRequestDAO.selectOptionColorName(PRODUCT_NO, OPTION_NO))
                .thenReturn("Black");
        when(restockRequestDAO.selectOptionSizeName(PRODUCT_NO, OPTION_NO))
                .thenReturn("L");

        service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, OPTION_NO);

        verify(notificationService).createForProductOwner(
                PRODUCT_NO,
                "RESTOCK_OPTION_REQUEST",
                "[재입고 요청] " + PRODUCT_NAME,
                "'테스트 상품'의 Black / L 옵션에 재입고 알림 신청이 들어왔습니다. (현재 재입고 대기: 0명)",
                "/business/product/list",
                "PRODUCT",
                PRODUCT_NO);
    }

    @Test
    void requestOptionShouldThrowWhenInsertDoesNotAffectOneRow() {
        stubNewOptionRequest();
        when(restockRequestDAO.insertRestockRequest(any(RestockRequestVO.class)))
                .thenReturn(2);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.requestRestockNotification(
                        MEMBER_NO, PRODUCT_NO, OPTION_NO));

        assertEquals(REQUEST_FAILED_MESSAGE, exception.getMessage());
        verifyNoInteractions(notificationService);
    }

    @Test
    void requestShouldRejectInvalidNumbersBeforeDaoAccess() {
        IllegalArgumentException memberException = assertThrows(
                IllegalArgumentException.class,
                () -> service.requestRestockNotification(0L, PRODUCT_NO, null));
        assertEquals(INVALID_MEMBER_MESSAGE, memberException.getMessage());

        IllegalArgumentException productException = assertThrows(
                IllegalArgumentException.class,
                () -> service.requestRestockNotification(MEMBER_NO, -1L, null));
        assertEquals(INVALID_PRODUCT_MESSAGE, productException.getMessage());

        IllegalArgumentException optionException = assertThrows(
                IllegalArgumentException.class,
                () -> service.requestRestockNotification(MEMBER_NO, PRODUCT_NO, -1L));
        assertEquals(INVALID_OPTION_MESSAGE, optionException.getMessage());

        verifyNoInteractions(restockRequestDAO, notificationService);
    }

    @Test
    void cancelShouldCancelProductRequestWhenOptionIsAbsent() {
        service.cancelRestockNotification(MEMBER_NO, PRODUCT_NO, null);

        verify(restockRequestDAO)
                .cancelProductRestockRequest(MEMBER_NO, PRODUCT_NO);
        verify(restockRequestDAO, never())
                .cancelOptionRestockRequest(any(), any(), any());
    }

    @Test
    void cancelShouldCancelOnlyRequestedOption() {
        service.cancelRestockNotification(MEMBER_NO, PRODUCT_NO, OPTION_NO);

        verify(restockRequestDAO)
                .cancelOptionRestockRequest(MEMBER_NO, PRODUCT_NO, OPTION_NO);
        verify(restockRequestDAO, never())
                .cancelProductRestockRequest(any(), any());
    }

    @Test
    void cancelShouldRejectInvalidMemberProductAndOptionNumbers() {
        IllegalArgumentException memberException = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelRestockNotification(null, PRODUCT_NO, null));
        assertEquals(INVALID_MEMBER_MESSAGE, memberException.getMessage());

        IllegalArgumentException productException = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelRestockNotification(MEMBER_NO, 0L, null));
        assertEquals(INVALID_PRODUCT_MESSAGE, productException.getMessage());

        IllegalArgumentException optionException = assertThrows(
                IllegalArgumentException.class,
                () -> service.cancelRestockNotification(MEMBER_NO, PRODUCT_NO, 0L));
        assertEquals(INVALID_OPTION_MESSAGE, optionException.getMessage());

        verifyNoInteractions(restockRequestDAO, notificationService);
    }

    private void stubNewProductRequest() {
        when(restockRequestDAO.selectProductStock(PRODUCT_NO)).thenReturn(0);
        when(restockRequestDAO.countActiveProductRequest(MEMBER_NO, PRODUCT_NO))
                .thenReturn(0);
    }

    private void stubNewOptionRequest() {
        when(restockRequestDAO.selectOptionStock(PRODUCT_NO, OPTION_NO))
                .thenReturn(0);
        when(restockRequestDAO.countActiveOptionRequest(
                MEMBER_NO, PRODUCT_NO, OPTION_NO))
                .thenReturn(0);
    }
}
