package com.project.oditji.subscription.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.subscription.vo.SubscriptionShareVO;

/**
 * OTT 구독 조합 계산기 결과 저장/공유(SUBSCRIPTION_RESULT)를 담당합니다.
 */
@Mapper
public interface SubscriptionDAO {

    // 계산 결과 저장
    int insertResult(SubscriptionShareVO shareVO);

    // 공유 링크(resultId)로 저장된 계산 결과 조회 (만료된 비회원 결과는 조회되지 않음)
    SubscriptionShareVO selectResultById(@Param("resultId") String resultId);

    /*
     * [비회원 공유 링크 임시 보관 추가]
     * EXPIRES_AT이 지난(비회원) 결과를 정리합니다.
     * 회원이 저장한 결과는 EXPIRES_AT이 NULL이라 대상에서 제외됩니다.
     */
    int deleteExpiredResults();

    /*
     * [마이페이지 구독 계산 결과 모달 연동 추가]
     * 회원 번호(MEMBER_NO) 기준으로 저장된 결과를 최신순으로 조회합니다.
     */
    List<SubscriptionShareVO> selectResultsByMember(@Param("memberNo") Long memberNo);

    /*
     * [마이페이지 구독 계산 결과 모달 연동 추가]
     * resultId와 memberNo가 모두 일치하는 저장 결과만 삭제합니다(본인 검증).
     * 실제로 삭제된 행 수를 반환합니다.
     */
    int deleteResultByIdAndMember(
            @Param("resultId") String resultId,
            @Param("memberNo") Long memberNo);
}
