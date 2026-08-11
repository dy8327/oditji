package com.project.oditji.member.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** 네이버 이름/닉네임 fallback의 null과 blank 조합을 개별적으로 보완합니다. */
class NaverUserInfoVOFinalFallbackCoverageTest {

    @Test
    void displayNameShouldCoverNullNameWithNicknameAndBlankNicknameFallback() {
        NaverUserInfoVO.Response response = new NaverUserInfoVO.Response();
        NaverUserInfoVO value = new NaverUserInfoVO();
        value.setResponse(response);

        response.setName(null);
        response.setNickname("닉네임");
        assertEquals("닉네임", value.getDisplayName());

        response.setName(" ");
        response.setNickname(" ");
        assertEquals("네이버회원", value.getDisplayName());
    }

    @Test
    void safeNicknameShouldCoverNullNicknameWithNameAndBlankNameFallback() {
        NaverUserInfoVO.Response response = new NaverUserInfoVO.Response();
        NaverUserInfoVO value = new NaverUserInfoVO();
        value.setResponse(response);

        response.setNickname(null);
        response.setName("이름");
        assertEquals("이름", value.getSafeNickname());

        response.setNickname(" ");
        response.setName(" ");
        assertEquals("네이버회원", value.getSafeNickname());
    }

    @Test
    void gettersShouldReturnNullWhenResponseIsAbsent() {
        NaverUserInfoVO value = new NaverUserInfoVO();
        assertNull(value.getProviderUserId());
        assertNull(value.getName());
        assertNull(value.getNickname());
        assertNull(value.getEmail());
        assertNull(value.getMobile());
        assertNull(value.getProfileImage());
    }
}
