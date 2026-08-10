package com.project.oditji.member.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * 카카오 사용자 정보 VO의 프로필 누락, 기본 이미지 및 실제 이미지 분기를 검증합니다.
 */
class KakaoUserInfoVOCoverageTest {

    @Test
    void missingAccountOrProfileShouldUseSafeDefaults() {
        KakaoUserInfoVO user = new KakaoUserInfoVO();
        user.setId(100L);

        assertEquals(100L, user.getId());
        assertEquals("카카오회원", user.getNickname());
        assertNull(user.getProfileImageUrl());
        assertNull(user.getThumbnailImageUrl());
        assertTrue(user.isDefaultImage());

        KakaoUserInfoVO.KakaoAccount account =
                new KakaoUserInfoVO.KakaoAccount();
        user.setKakaoAccount(account);

        assertSame(account, user.getKakaoAccount());
        assertNull(account.getProfile());
        assertEquals("카카오회원", user.getNickname());
        assertTrue(user.isDefaultImage());
    }

    @Test
    void nullBlankAndNormalNicknamesShouldCoverFallbackConditions() {
        KakaoUserInfoVO user = userWithProfile();
        KakaoUserInfoVO.Profile profile =
                user.getKakaoAccount().getProfile();

        profile.setNickname(null);
        assertEquals("카카오회원", user.getNickname());

        profile.setNickname("   ");
        assertEquals("카카오회원", user.getNickname());

        profile.setNickname(" 카카오닉네임 ");
        assertEquals(" 카카오닉네임 ", user.getNickname());
    }

    @Test
    void defaultImageStatesShouldControlProfileImageExposure() {
        KakaoUserInfoVO user = userWithProfile();
        KakaoUserInfoVO.Profile profile =
                user.getKakaoAccount().getProfile();

        profile.setProfileImageUrl("https://image.example/profile.png");
        profile.setThumbnailImageUrl("https://image.example/thumb.png");

        profile.setDefaultImage(null);
        assertTrue(user.isDefaultImage());
        assertNull(user.getProfileImageUrl());

        profile.setDefaultImage(Boolean.TRUE);
        assertTrue(user.isDefaultImage());
        assertNull(user.getProfileImageUrl());

        profile.setDefaultImage(Boolean.FALSE);
        assertFalse(user.isDefaultImage());
        assertEquals(
                "https://image.example/profile.png",
                user.getProfileImageUrl());
        assertEquals(
                "https://image.example/thumb.png",
                user.getThumbnailImageUrl());

        assertEquals(Boolean.FALSE, profile.getDefaultImage());
        assertEquals(
                "https://image.example/profile.png",
                profile.getProfileImageUrl());
        assertEquals(
                "https://image.example/thumb.png",
                profile.getThumbnailImageUrl());
    }

    private KakaoUserInfoVO userWithProfile() {
        KakaoUserInfoVO.Profile profile = new KakaoUserInfoVO.Profile();
        KakaoUserInfoVO.KakaoAccount account =
                new KakaoUserInfoVO.KakaoAccount();
        account.setProfile(profile);

        KakaoUserInfoVO user = new KakaoUserInfoVO();
        user.setKakaoAccount(account);
        return user;
    }
}
