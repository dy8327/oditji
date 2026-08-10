package com.project.oditji.event.service;

import java.util.List;

import com.project.oditji.event.vo.OttDiscountVO;

public interface OttDiscountService {

    /** 조건(플랫폼/카테고리)에 맞는 활성화된 할인 목록을 페이지 단위로 조회합니다. */
    List<OttDiscountVO> getDiscountList(String platform, String category, int page, int pageSize);

    /** 조건(플랫폼/카테고리)에 맞는 활성화된 할인 목록의 전체 건수를 조회합니다(페이징 계산용). */
    int getDiscountListCount(String platform, String category);

    /** 조건(플랫폼/카테고리)에 맞는 상단 히어로 배너용 대표 항목 1건을 조회합니다(페이징과 무관). */
    OttDiscountVO getHeroDiscount(String platform, String category);

    /** 할인 정보 단건을 조회합니다. */
    OttDiscountVO getDiscountDetail(Long discountId);

    /** 관리자용: 활성화 여부와 무관하게 조건(플랫폼/카테고리/상태)에 맞는 전체 할인 목록을 조회합니다. */
    List<OttDiscountVO> getAdminDiscountList(String platform, String category, String status);

    /** 할인 정보를 신규 등록합니다. */
    boolean createDiscount(OttDiscountVO discountVO);

    /** 할인 정보를 수정합니다. */
    boolean updateDiscount(OttDiscountVO discountVO);

    /** 할인 정보를 비활성화(소프트 삭제)합니다. */
    boolean deleteDiscount(Long discountId);

    /** 비활성화된 할인 정보를 다시 활성화합니다. */
    boolean activateDiscount(Long discountId);

    /** 종료일이 지난 할인 정보를 일괄 비활성화합니다. */
    int deactivateExpiredDiscounts();
}
