package com.project.oditji.member.service;

import com.project.oditji.member.vo.SocialLoginResultVO;

/**
 * Google OAuth 로그인 서비스 인터페이스입니다.
 *
 * ODITJI의 기존 카카오/네이버 수동 OAuth 구조와 동일하게
 * 로그인 URL 생성과 인증 코드 처리 책임을 분리합니다.
 */
public interface GoogleLoginService {

    /**
     * Google 로그인 동의 화면으로 이동할 URL을 생성합니다.
     *
     * @param state CSRF 방지를 위해 세션에 저장한 일회성 상태값
     * @return Google OAuth 인증 요청 URL
     */
    String getGoogleLoginUrl(String state);

    /**
     * Google에서 전달한 인증 코드를 처리하고
     * ODITJI 회원 및 소셜 계정 정보를 조회하거나 생성합니다.
     *
     * @param code Google OAuth 인증 코드
     * @return 신규 회원 여부와 ODITJI 회원정보
     */
    SocialLoginResultVO googleLogin(String code);
}
