package com.project.oditji.member.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Batch01 이후 남은 카카오 중첩 VO getter/setter와 썸네일 반환 분기를 보완합니다.
 */
class KakaoUserInfoVOAdditionalCoverageTest {

    @Test
    void nestedProfileAccessorsShouldRoundTripEveryField() {
        KakaoUserInfoVO.Profile profile =
                new KakaoUserInfoVO.Profile();

        profile.setNickname("닉네임");
        profile.setProfileImageUrl("profile");
        profile.setThumbnailImageUrl("thumbnail");
        profile.setDefaultImage(Boolean.FALSE);

        assertEquals("닉네임", profile.getNickname());
        assertEquals("profile", profile.getProfileImageUrl());
        assertEquals("thumbnail", profile.getThumbnailImageUrl());
        assertEquals(Boolean.FALSE, profile.getDefaultImage());

        KakaoUserInfoVO.KakaoAccount account =
                new KakaoUserInfoVO.KakaoAccount();
        account.setProfile(profile);

        assertSame(profile, account.getProfile());

        KakaoUserInfoVO user = new KakaoUserInfoVO();
        user.setId(10L);
        user.setKakaoAccount(account);

        assertEquals(10L, user.getId());
        assertSame(account, user.getKakaoAccount());
        assertEquals("닉네임", user.getNickname());
        assertEquals("profile", user.getProfileImageUrl());
        assertEquals("thumbnail", user.getThumbnailImageUrl());
        assertFalse(user.isDefaultImage());
    }

    @Test
    void thumbnailShouldRemainAvailableEvenWhenProfileIsDefaultImage() {
        KakaoUserInfoVO.Profile profile =
                new KakaoUserInfoVO.Profile();
        profile.setThumbnailImageUrl("default-thumbnail");
        profile.setProfileImageUrl("default-profile");
        profile.setDefaultImage(Boolean.TRUE);

        KakaoUserInfoVO.KakaoAccount account =
                new KakaoUserInfoVO.KakaoAccount();
        account.setProfile(profile);

        KakaoUserInfoVO user = new KakaoUserInfoVO();
        user.setKakaoAccount(account);

        assertTrue(user.isDefaultImage());
        assertNull(user.getProfileImageUrl());
        assertEquals(
                "default-thumbnail",
                user.getThumbnailImageUrl());
    }

    @Test
    void falseDefaultFlagWithNullImageShouldReturnNullImageValueDirectly() {
        KakaoUserInfoVO.Profile profile =
                new KakaoUserInfoVO.Profile();
        profile.setDefaultImage(Boolean.FALSE);
        profile.setProfileImageUrl(null);
        profile.setThumbnailImageUrl(null);

        KakaoUserInfoVO.KakaoAccount account =
                new KakaoUserInfoVO.KakaoAccount();
        account.setProfile(profile);

        KakaoUserInfoVO user = new KakaoUserInfoVO();
        user.setKakaoAccount(account);

        assertFalse(user.isDefaultImage());
        assertNull(user.getProfileImageUrl());
        assertNull(user.getThumbnailImageUrl());
    }
}
