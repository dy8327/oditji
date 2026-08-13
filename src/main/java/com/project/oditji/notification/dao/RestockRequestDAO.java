package com.project.oditji.notification.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.notification.vo.RestockRequestVO;

/**
 * 재입고 알림 신청 관련 DAO
 */
@Mapper
public interface RestockRequestDAO {

        /*
         * [상품 전체 재입고 알림]
         * 현재 상품 전체 재고를 조회합니다.
         */
        Integer selectProductStock(@Param("productNo") Long productNo);

        /*
         * [옵션별 재입고 알림 추가]
         * 특정 상품 옵션의 현재 재고를 조회합니다.
         */
        Integer selectOptionStock(
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo);

        /*
         * [상품 전체 재입고 알림 신청]
         * OPTION_NO가 NULL인 대기 중 신청 여부를 확인합니다.
         */
        int countActiveProductRequest(
                        @Param("memberNo") Long memberNo,
                        @Param("productNo") Long productNo);

        /*
         * [옵션별 재입고 알림 신청]
         * 특정 OPTION_NO에 대한 대기 중 신청 여부를 확인합니다.
         */
        int countActiveOptionRequest(
                        @Param("memberNo") Long memberNo,
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo);

        /*
         * [재입고 알림 신청]
         * 상품 전체 또는 특정 옵션 재입고 신청을 저장합니다.
         */
        int insertRestockRequest(
                        RestockRequestVO restockRequestVO);

        /*
         * [상품 전체 재입고 알림 신청 취소]
         * OPTION_NO가 NULL인 상품 전체 신청만 취소합니다.
         */
        int cancelProductRestockRequest(
                        @Param("memberNo") Long memberNo,
                        @Param("productNo") Long productNo);

        /*
         * [옵션별 재입고 알림 신청 취소]
         * 해당 OPTION_NO의 신청만 취소합니다.
         */
        int cancelOptionRestockRequest(
                        @Param("memberNo") Long memberNo,
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo);

        /*
         * =========================================================
         * [사업자 재입고 신청 알림 추가]
         * 사업자 알림 메시지에 표시할 상품명을 조회합니다.
         * =========================================================
         */
        String selectProductName(
                        @Param("productNo") Long productNo);

        /*
         * =========================================================
         * [사업자 옵션 재입고 신청 알림 추가]
         * 선택한 옵션의 색상명을 조회합니다.
         * =========================================================
         */
        String selectOptionColorName(
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo);

        /*
         * 선택한 옵션의 사이즈명을 조회합니다.
         */
        String selectOptionSizeName(
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo);

        /*
         * =========================================================
         * [상품 전체 재입고 대기 인원]
         * OPTION_NO가 NULL인 WAITING 신청 인원을 조회합니다.
         * =========================================================
         */
        int countWaitingProductRequests(
                        @Param("productNo") Long productNo);

        /*
         * =========================================================
         * [옵션별 재입고 대기 인원]
         * 해당 OPTION_NO의 WAITING 신청 인원을 조회합니다.
         * =========================================================
         */
        int countWaitingOptionRequests(
                        @Param("productNo") Long productNo,
                        @Param("optionNo") Long optionNo);
}