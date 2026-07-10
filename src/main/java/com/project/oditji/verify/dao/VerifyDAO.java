package com.project.oditji.verify.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.project.oditji.verify.vo.IdentityVerifyLogVO;

@Mapper
public interface VerifyDAO {

    /**
     * 본인인증/성인인증 시도 및 결과 로그를 기록합니다.
     */
    int insertVerifyLog(IdentityVerifyLogVO log);

    /**
     * 성인인증 성공 시 해당 회원의 성인 여부 플래그를 업데이트합니다.
     */
    int updateMemberAdultVerified(@Param("memberNo") long memberNo);

    /**
     * 해당 회원이 이미 성인인증을 완료했는지 여부를 조회합니다 (예: "Y" 또는 "N").
     */
    String selectMemberAdultVerified(@Param("memberNo") long memberNo);

    /**
     * 중복 인증 요청을 방지하거나 유효성을 검증하기 위해 특정 verifyId의 존재 개수를 확인합니다.
     */
    int countVerifyId(@Param("verifyId") String verifyId);
}