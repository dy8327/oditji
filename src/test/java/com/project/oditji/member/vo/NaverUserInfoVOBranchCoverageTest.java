package com.project.oditji.member.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/** 네이버 프로필 응답의 null 처리와 이름·닉네임 대체 분기를 검증합니다. */
class NaverUserInfoVOBranchCoverageTest {

    @Test
    void missingResponseShouldReturnSafeDefaults() {
        NaverUserInfoVO userInfo = new NaverUserInfoVO();
        userInfo.setResultcode("00");
        userInfo.setMessage("success");

        assertEquals("00", userInfo.getResultcode());
        assertEquals("success", userInfo.getMessage());
        assertNull(userInfo.getResponse());
        assertNull(userInfo.getProviderUserId());
        assertNull(userInfo.getName());
        assertNull(userInfo.getNickname());
        assertNull(userInfo.getEmail());
        assertNull(userInfo.getMobile());
        assertNull(userInfo.getProfileImage());
        assertEquals("네이버회원", userInfo.getDisplayName());
        assertEquals("네이버회원", userInfo.getSafeNickname());
    }

    @Test
    void responsePropertiesAndPreferredNamesShouldRoundTrip() {
        NaverUserInfoVO.Response response = new NaverUserInfoVO.Response();
        response.setId("naver-100");
        response.setNickname("닉네임");
        response.setName("이름");
        response.setEmail("naver@example.com");
        response.setMobile("010-1234-5678");
        response.setProfileImage("profile.png");

        NaverUserInfoVO userInfo = new NaverUserInfoVO();
        userInfo.setResponse(response);

        assertSame(response, userInfo.getResponse());
        assertEquals("naver-100", response.getId());
        assertEquals("닉네임", response.getNickname());
        assertEquals("이름", response.getName());
        assertEquals("naver@example.com", response.getEmail());
        assertEquals("010-1234-5678", response.getMobile());
        assertEquals("profile.png", response.getProfileImage());
        assertEquals("naver-100", userInfo.getProviderUserId());
        assertEquals("이름", userInfo.getDisplayName());
        assertEquals("닉네임", userInfo.getSafeNickname());
        assertEquals("naver@example.com", userInfo.getEmail());
        assertEquals("010-1234-5678", userInfo.getMobile());
        assertEquals("profile.png", userInfo.getProfileImage());
    }

    @Test
    void displayNameAndSafeNicknameShouldUseFallbackOrder() {
        NaverUserInfoVO.Response response = new NaverUserInfoVO.Response();
        NaverUserInfoVO userInfo = new NaverUserInfoVO();
        userInfo.setResponse(response);

        response.setName(" ");
        response.setNickname("대체닉네임");
        assertEquals("대체닉네임", userInfo.getDisplayName());
        assertEquals("대체닉네임", userInfo.getSafeNickname());

        response.setName("대체이름");
        response.setNickname(" ");
        assertEquals("대체이름", userInfo.getDisplayName());
        assertEquals("대체이름", userInfo.getSafeNickname());

        response.setName(null);
        response.setNickname(null);
        assertEquals("네이버회원", userInfo.getDisplayName());
        assertEquals("네이버회원", userInfo.getSafeNickname());
    }
}
