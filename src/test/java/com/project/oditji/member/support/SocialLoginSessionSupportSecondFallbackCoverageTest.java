package com.project.oditji.member.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.project.oditji.member.vo.MemberSocialJoinVO;

/** 소셜 로그인 표시명 fallback의 nicknameFirst=false 반대쪽 ternary 분기를 보완합니다. */
class SocialLoginSessionSupportSecondFallbackCoverageTest {

    @Test
    void displayNameShouldUseNicknameWhenMemberNameIsBlankAndNicknameIsPreferredSecond() {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setMemberName("   ");
        member.setNickname("소셜닉네임");

        assertEquals(
                "소셜닉네임",
                SocialLoginSessionSupport.resolveDisplayName(
                        member,
                        false,
                        "기본값"));
    }
}
