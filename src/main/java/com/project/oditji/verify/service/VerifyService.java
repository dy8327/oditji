package com.project.oditji.verify.service;

import com.project.oditji.verify.vo.AdultVerifyCompleteVO;
import com.project.oditji.verify.vo.AdultVerifyReadyVO;
import jakarta.servlet.http.HttpSession;

public interface VerifyService {

    /**
     * 성인인증 준비 (포트원 본인인증 창 호출 전 사전 검증 및 데이터 준비)
     *
     * @param memberNo 현재 로그인한 회원의 번호
     * @return 성인인증 준비 결과 데이터 (요청 ID 등)
     */
    AdultVerifyReadyVO prepareVerification(long memberNo);

    /**
     * 성인인증 완료 처리 (포트원 인증 성공 후 서버단 최종 검증 및 세션/DB 반영)
     *
     * @param memberNo  현재 로그인한 회원의 번호
     * @param verifyId  포트원에서 발급받은 인증 고유 ID
     * @param returnUrl 인증 완료 후 돌아갈 페이지 경로
     * @param session   성인인증 완료 세션을 기록할 HTTP 세션 객체
     * @return 성인인증 최종 결과 데이터 (성공 여부, 리다이렉트 URL 등)
     */
    AdultVerifyCompleteVO completeVerification(
            long memberNo,
            String verifyId,
            String returnUrl,
            HttpSession session
    );

    /**
     * 회원의 성인인증 여부 확인 (DB 또는 세션 조회용)
     *
     * @param memberNo 조회할 회원의 번호
     * @return 성인인증을 완료한 회원이면 true, 아니면 false
     */
    boolean isAdultVerified(long memberNo);
}