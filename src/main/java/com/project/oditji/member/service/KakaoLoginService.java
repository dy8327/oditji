package com.project.oditji.member.service;

import com.project.oditji.member.vo.KakaoLoginResultVO;

public interface KakaoLoginService {

    String getKakaoLoginUrl();

    KakaoLoginResultVO kakaoLogin(String code);
}