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
         * =========================================================
         * [상품 전체 재입고 알림]
         * optionNo가 NULL이면 기존처럼 PRODUCT.STOCK을 검사합니다.
         * =========================================================
         */
        if (optionNo == null) {

            Integer currentStock = restockRequestDAO.selectProductStock(productNo);

            if (currentStock == null) {
                throw new IllegalArgumentException(
                        "존재하지 않는 상품입니다.");
            }

            if (currentStock > 0) {
                throw new IllegalStateException(
                        "현재 재고가 있는 상품은 재입고 알림을 신청할 수 없습니다.");
            }

            /*
             * 이미 상품 전체 재입고 신청이 있으면
             * 중복 등록하지 않습니다.
             */
            if (restockRequestDAO.countActiveProductRequest(
                    memberNo,
                    productNo) > 0) {

                return;
            }

        } else {

            /*
             * =========================================================
             * [옵션별 재입고 알림 추가]
             * 특정 옵션 신청이면 PRODUCT_OPTION.STOCK을 검사합니다.
             * =========================================================
             */
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

            /*
             * 같은 옵션에 이미 신청되어 있으면
             * 중복 행을 생성하지 않습니다.
             */
            if (restockRequestDAO.countActiveOptionRequest(
                    memberNo,
                    productNo,
                    optionNo) > 0) {

                return;
            }
        }

        RestockRequestVO restockRequestVO = new RestockRequestVO();

        restockRequestVO.setMemberNo(memberNo);
        restockRequestVO.setProductNo(productNo);

        /*
         * [옵션별 재입고 알림 추가]
         * NULL이면 상품 전체,
         * 값이 있으면 특정 옵션 신청입니다.
         */
        restockRequestVO.setOptionNo(optionNo);

        restockRequestVO.setStatus(STATUS_WAITING);

        if (restockRequestDAO.insertRestockRequest(
                restockRequestVO) != 1) {

            throw new IllegalStateException(
                    "재입고 알림 신청에 실패했습니다.");
        }

        /*
         * =========================================================
         * [사업자 재입고 신청 알림 추가]
         *
         * 재입고 신청 DB 저장이 정상적으로 완료된 뒤
         * 해당 상품을 등록한 사업자에게 알림을 생성합니다.
         *
         * 알림에는:
         * - 상품명
         * - 옵션 상품이면 색상 / 사이즈
         * - 현재 재입고 대기 인원
         * 을 표시합니다.
         * =========================================================
         */

        String productName = restockRequestDAO.selectProductName(productNo);

        /*
         * 혹시 상품명이 조회되지 않는 예외 상황에서도
         * 알림 생성 때문에 전체 신청 트랜잭션이 깨지지 않도록
         * 기본 문구를 사용합니다.
         */
        if (productName == null
                || productName.isBlank()) {

            productName = "상품";
        }

        /*
         * =========================================================
         * 상품 전체 재입고 신청
         * =========================================================
         */
        if (optionNo == null) {

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

                    /*
                     * 현재 프로젝트는 GET /business/product/update 페이지가
                     * 제거되어 있으므로 상품 목록 화면으로 이동시킵니다.
                     */
                    "/business/product/list",

                    "PRODUCT",
                    productNo);

            return;
        }

        /*
         * =========================================================
         * 특정 옵션 재입고 신청
         * =========================================================
         */
        String colorName = restockRequestDAO.selectOptionColorName(
                productNo,
                optionNo);

        String sizeName = restockRequestDAO.selectOptionSizeName(
                productNo,
                optionNo);

        /*
         * NULL 값이 그대로 알림에 출력되지 않도록 처리합니다.
         */
        if (colorName == null
                || colorName.isBlank()) {

            colorName = "색상 미지정";
        }

        if (sizeName == null
                || sizeName.isBlank()) {

            sizeName = "사이즈 미지정";
        }

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
