package com.project.oditji.member.vo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Google UserInfo VO의 null/blank 정규화와 기본 getter/setter를 검증합니다.
 */
class GoogleUserInfoVOCoverageTest {

    @Test
    void scalarFieldsShouldRoundTripAndTrimDerivedValues() {
        GoogleUserInfoVO user =
                new GoogleUserInfoVO();

        user.setSub("google-sub");
        user.setName("  Google User  ");
        user.setPicture("  https://image.example/google.png  ");

        assertEquals(
                "google-sub",
                user.getSub());
        assertEquals(
                "  Google User  ",
                user.getName());
        assertEquals(
                "  https://image.example/google.png  ",
                user.getPicture());

        assertEquals(
                "Google User",
                user.getNickname());
        assertEquals(
                "https://image.example/google.png",
                user.getProfileImageUrl());
    }

    @Test
    void nullAndBlankNameShouldUseDefaultNickname() {
        GoogleUserInfoVO user =
                new GoogleUserInfoVO();

        user.setName(null);
        assertEquals(
                "구글회원",
                user.getNickname());

        user.setName("   ");
        assertEquals(
                "구글회원",
                user.getNickname());
    }

    @Test
    void nullAndBlankPictureShouldReturnNull() {
        GoogleUserInfoVO user =
                new GoogleUserInfoVO();

        user.setPicture(null);
        assertNull(
                user.getProfileImageUrl());

        user.setPicture("   ");
        assertNull(
                user.getProfileImageUrl());
    }
}
