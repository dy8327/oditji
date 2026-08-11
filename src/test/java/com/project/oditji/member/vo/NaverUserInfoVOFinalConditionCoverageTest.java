package com.project.oditji.member.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** 네이버 이름/닉네임 fallback의 남은 AND 단락 조건을 검증합니다. */
class NaverUserInfoVOFinalConditionCoverageTest {

    @Test
    void displayNameShouldFallBackWhenNameIsBlankAndNicknameIsNull() {
        NaverUserInfoVO.Response response = new NaverUserInfoVO.Response();
        response.setName("   ");
        response.setNickname(null);

        NaverUserInfoVO userInfo = new NaverUserInfoVO();
        userInfo.setResponse(response);

        assertEquals("네이버회원", userInfo.getDisplayName());
    }

    @Test
    void safeNicknameShouldFallBackWhenNicknameIsBlankAndNameIsNull() {
        NaverUserInfoVO.Response response = new NaverUserInfoVO.Response();
        response.setNickname("   ");
        response.setName(null);

        NaverUserInfoVO userInfo = new NaverUserInfoVO();
        userInfo.setResponse(response);

        assertEquals("네이버회원", userInfo.getSafeNickname());
    }
}
