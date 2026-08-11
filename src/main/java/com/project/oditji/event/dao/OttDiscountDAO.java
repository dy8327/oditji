package com.project.oditji.event.dao;

import com.project.oditji.event.vo.OttDiscountVO;
import com.project.oditji.subscription.vo.PlatformPriceVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OttDiscountDAO {

    // 할인 정보 목록 조회 (필터링, 공개용 - 활성화된 항목만, 페이징 적용)
    List<OttDiscountVO> selectDiscountList(@Param("platform") String platform, @Param("category") String category,
            @Param("offset") int offset, @Param("pageSize") int pageSize);

    // 할인 정보 목록 전체 건수 조회 (페이징 계산용, selectDiscountList와 동일한 조건)
    int selectDiscountListCount(@Param("platform") String platform, @Param("category") String category);

    /*
     * [수정] 목록 페이징 도입으로 discountList가 8개씩 잘리면서, 기존에 discountList
     * 전체를 훑어 BEST 뱃지를 찾던 상단 히어로 배너 로직이 2페이지 이후에 있는 BEST
     * 항목을 놓치게 됐다. 페이징과 무관하게 항상 정확한 항목을 보여주도록 DB에서
     * BEST 뱃지 우선 1건만 별도로 조회한다(없으면 최신 항목).
     */
    OttDiscountVO selectHeroDiscount(@Param("platform") String platform, @Param("category") String category);

    // 관리자용 전체 목록 조회 (IS_ACTIVE 필터 없이 조회, status로 활성/비활성 선택 조회 가능, 페이징 적용)
    List<OttDiscountVO> selectAdminDiscountList(@Param("platform") String platform, @Param("category") String category,
            @Param("status") String status, @Param("offset") int offset, @Param("pageSize") int pageSize);

    // 관리자용 전체 목록 건수 조회 (페이징 계산용, selectAdminDiscountList와 동일한 조건)
    int selectAdminDiscountListCount(@Param("platform") String platform, @Param("category") String category,
            @Param("status") String status);

    // 단건 상세 조회
    OttDiscountVO selectDiscountById(@Param("discountId") Long discountId);

    // 할인 정보 등록
    int insertDiscount(OttDiscountVO discountVO);

    // 할인 정보 수정
    int updateDiscount(OttDiscountVO discountVO);

    // 할인 정보 삭제 (상태 변경 N)
    int deleteDiscount(@Param("discountId") Long discountId);

    // 비활성화된 할인 정보 재활성화 (상태 변경 Y)
    int activateDiscount(@Param("discountId") Long discountId);

    // 만료된 할인 정보 일괄 비활성화
    int updateExpiredDiscounts();

    /*
     * [수정] 플랫폼별로 "사용자가 실제로 적용받을 수 있는" 최저가를 산출해 반환한다.
     * telecomCode/cardCompany/membershipName은 사용자가 화면에서 선택한 조건이며,
     * OTT_DISCOUNT_INFO.CARD_OR_COMPANY와 정확히 일치하는 할인만 적용 후보로 인정한다.
     * 셋 다 null/빈 값이면 어떤 조건부 할인도 적용하지 않고 정가를 반환한다.
     * 위시리스트에 담긴 콘텐츠들을 가장 저렴하게 커버하는 OTT 조합을 계산할 때
     * 가격 소스로 사용한다.
     */
    List<PlatformPriceVO> selectBestPriceByPlatform(@Param("telecomCode") String telecomCode,
            @Param("cardCompany") String cardCompany, @Param("membershipName") String membershipName);

    /*
     * [OTT 구독 조합 계산기 추가] 필터 드롭다운/라디오에 쓸 카테고리별
     * (TELECOM/CARD/MEMBERSHIP) CARD_OR_COMPANY 후보 목록을 조회한다.
     */
    List<String> selectDiscountProviderNames(@Param("category") String category);
}