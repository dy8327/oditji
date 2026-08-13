package com.project.oditji.member.service;

import com.project.oditji.member.vo.SocialLoginResultVO;

public interface KakaoLoginService {

    String getKakaoLoginUrl();

    SocialLoginResultVO kakaoLogin(String code);
}