package com.project.oditji.member.service;

import com.project.oditji.member.vo.SocialLoginResultVO;

/**
 * 네이버 OAuth 로그인 처리 서비스입니다.
 *
 * 로그인 요청용 state 생성, 네이버 인증 URL 생성,
 * 인증 코드 기반 회원 로그인 처리를 담당합니다.
 */
public interface NaverLoginService {

    /**
     * 사이트 간 요청 위조 방지를 위한 임의의 state 값을 생성합니다.
     *
     * @return 네이버 로그인 요청에 사용할 state 값
     */
    String createState();

    /**
     * 네이버 로그인 인증 화면으로 이동할 URL을 생성합니다.
     *
     * @param state 세션에 저장한 OAuth state 값
     * @return 네이버 로그인 인증 URL
     */
    String getNaverLoginUrl(String state);

    /**
     * 네이버 콜백으로 전달된 인증 코드로 로그인 또는 자동가입을 처리합니다.
     *
     * @param code 네이버가 발급한 인증 코드
     * @param state 로그인 요청 때 사용한 state 값
     * @return 신규 가입 여부와 연동 회원 정보
     */
    SocialLoginResultVO naverLogin(String code, String state);
}
