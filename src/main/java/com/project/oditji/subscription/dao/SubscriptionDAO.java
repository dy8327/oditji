package com.project.oditji.subscription.dao;

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
}
