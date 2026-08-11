package com.project.oditji.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.notification.dao.RestockRequestDAO;
import com.project.oditji.notification.vo.RestockRequestVO;

/**
 * 품절 상태를 확인한 뒤 사용자별 재입고 알림 신청을 등록/취소합니다.
 */
@Service
public class RestockRequestServiceImpl implements RestockRequestService {

    private static final String STATUS_WAITING = "WAITING";

    private final RestockRequestDAO restockRequestDAO;

    /*
     * [재입고 신청 사업자 알림 추가]
     * 사용자가 재입고 알림을 신청하면
     * 해당 상품을 등록한 사업자에게 알림을 생성하기 위해 사용합니다.
     */
    private final NotificationService notificationService;

    public RestockRequestServiceImpl(
            RestockRequestDAO restockRequestDAO,
            NotificationService notificationService) {

        this.restockRequestDAO = restockRequestDAO;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRequested(
            Long memberNo,
            Long productNo,
            Long optionNo) {

        validateMemberNo(memberNo);
        validateProductNo(productNo);

        /*
         * [옵션별 재입고 알림 추가]
         * optionNo가 없으면 상품 전체 신청,
         * 값이 있으면 특정 옵션 신청 여부를 확인합니다.
         */
        if (optionNo == null) {
            return restockRequestDAO.countActiveProductRequest(
                    memberNo,
                    productNo) > 0;
        }

        validateOptionNo(optionNo);

        return restockRequestDAO.countActiveOptionRequest(
                memberNo,
                productNo,
                optionNo) > 0;
    }

    @Override
    @Transactional
    public void requestRestockNotification(
            Long memberNo,
            Long productNo,
            Long optionNo) {

        validateMemberNo(memberNo);
        validateProductNo(productNo);

        /*
         * 상품 전체/옵션별 재고 검증과 중복 신청 확인을 분리해
         * 신청 메서드의 인지 복잡도를 낮춥니다.
         */
        if (validateStockAndCheckDuplicateRequest(
                memberNo,
                productNo,
                optionNo)) {

            return;
        }

        insertRestockRequest(memberNo, productNo, optionNo);
        createProductOwnerRestockRequestNotification(productNo, optionNo);
    }

    private boolean validateStockAndCheckDuplicateRequest(
            Long memberNo,
            Long productNo,
            Long optionNo) {

        if (optionNo == null) {
            return validateProductStockAndCheckDuplicate(memberNo, productNo);
        }

        return validateOptionStockAndCheckDuplicate(memberNo, productNo, optionNo);
    }

    private boolean validateProductStockAndCheckDuplicate(
            Long memberNo,
            Long productNo) {

        Integer currentStock = restockRequestDAO.selectProductStock(productNo);

        if (currentStock == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 상품입니다.");
        }

        if (currentStock > 0) {
            throw new IllegalStateException(
                    "현재 재고가 있는 상품은 재입고 알림을 신청할 수 없습니다.");
        }

        return restockRequestDAO.countActiveProductRequest(
                memberNo,
                productNo) > 0;
    }

    private boolean validateOptionStockAndCheckDuplicate(
            Long memberNo,
            Long productNo,
            Long optionNo) {

        validateOptionNo(optionNo);

        Integer optionStock = restockRequestDAO.selectOptionStock(
                productNo,
                optionNo);

        if (optionStock == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 상품 옵션입니다.");
        }

        if (optionStock > 0) {
            throw new IllegalStateException(
                    "현재 재고가 있는 옵션은 재입고 알림을 신청할 수 없습니다.");
        }

        return restockRequestDAO.countActiveOptionRequest(
                memberNo,
                productNo,
                optionNo) > 0;
    }

    private void insertRestockRequest(
            Long memberNo,
            Long productNo,
            Long optionNo) {

        RestockRequestVO restockRequestVO = new RestockRequestVO();
        restockRequestVO.setMemberNo(memberNo);
        restockRequestVO.setProductNo(productNo);
        restockRequestVO.setOptionNo(optionNo);
        restockRequestVO.setStatus(STATUS_WAITING);

        if (restockRequestDAO.insertRestockRequest(
                restockRequestVO) != 1) {

            throw new IllegalStateException(
                    "재입고 알림 신청에 실패했습니다.");
        }
    }

    private void createProductOwnerRestockRequestNotification(
            Long productNo,
            Long optionNo) {

        String productName = getNotificationProductName(productNo);

        if (optionNo == null) {
            createProductRestockRequestNotification(productNo, productName);
            return;
        }

        createOptionRestockRequestNotification(productNo, optionNo, productName);
    }

    private String getNotificationProductName(Long productNo) {

        String productName = restockRequestDAO.selectProductName(productNo);

        if (productName == null
                || productName.isBlank()) {

            return "상품";
        }

        return productName;
    }

    private void createProductRestockRequestNotification(
            Long productNo,
            String productName) {

        int waitingCount = restockRequestDAO.countWaitingProductRequests(
                productNo);

        String title = "[재입고 요청] " + productName;
        String message = "'" + productName
                + "' 상품에 재입고 알림 신청이 들어왔습니다. "
                + "현재 재입고 대기 "
                + waitingCount
                + "명";

        notificationService.createForProductOwner(
                productNo,
                "RESTOCK_REQUEST",
                title,
                message,
                "/business/product/list",
                "PRODUCT",
                productNo);
    }

    private void createOptionRestockRequestNotification(
            Long productNo,
            Long optionNo,
            String productName) {

        String colorName = normalizeOptionLabel(
                restockRequestDAO.selectOptionColorName(productNo, optionNo),
                "색상 미지정");
        String sizeName = normalizeOptionLabel(
                restockRequestDAO.selectOptionSizeName(productNo, optionNo),
                "사이즈 미지정");
        int waitingCount = restockRequestDAO.countWaitingOptionRequests(
                productNo,
                optionNo);

        String title = "[재입고 요청] " + productName;
        String message = "'" + productName
                + "'의 "
                + colorName
                + " / "
                + sizeName
                + " 옵션에 재입고 알림 신청이 들어왔습니다. "
                + "(현재 재입고 대기: "
                + waitingCount
                + "명)";

        notificationService.createForProductOwner(
                productNo,
                "RESTOCK_OPTION_REQUEST",
                title,
                message,
                "/business/product/list",
                "PRODUCT",
                productNo);
    }

    private String normalizeOptionLabel(
            String value,
            String defaultValue) {

        return value == null || value.isBlank()
                ? defaultValue
                : value;
    }

    @Override
    @Transactional
    public void cancelRestockNotification(
            Long memberNo,
            Long productNo,
            Long optionNo) {

        validateMemberNo(memberNo);
        validateProductNo(productNo);

        /*
         * [옵션별 재입고 알림 추가]
         * optionNo가 없으면 상품 전체 신청을 취소합니다.
         */
        if (optionNo == null) {

            restockRequestDAO.cancelProductRestockRequest(
                    memberNo,
                    productNo);

            return;
        }

        /*
         * optionNo가 있으면 해당 옵션 신청만 취소합니다.
         */
        validateOptionNo(optionNo);

        restockRequestDAO.cancelOptionRestockRequest(
                memberNo,
                productNo,
                optionNo);
    }

    /*
     * [옵션별 재입고 알림 추가]
     * 상품 옵션 번호의 기본 유효성을 검사합니다.
     */
    private void validateOptionNo(Long optionNo) {

        if (optionNo == null || optionNo <= 0L) {
            throw new IllegalArgumentException(
                    "올바르지 않은 상품 옵션 번호입니다.");
        }
    }

    private void validateMemberNo(Long memberNo) {

        if (memberNo == null || memberNo <= 0L) {
            throw new IllegalArgumentException(
                    "올바르지 않은 회원 번호입니다.");
        }
    }

    private void validateProductNo(Long productNo) {

        if (productNo == null || productNo <= 0L) {
            throw new IllegalArgumentException(
                    "올바르지 않은 상품 번호입니다.");
        }
    }
}
