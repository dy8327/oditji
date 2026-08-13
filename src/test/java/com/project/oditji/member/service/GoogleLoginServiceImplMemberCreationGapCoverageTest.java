package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.vo.GoogleUserInfoVO;
import com.project.oditji.member.vo.MemberVO;

/** Google 신규 회원 생성과 닉네임 충돌 fallback 잔여 라인을 보완합니다. */
class GoogleLoginServiceImplMemberCreationGapCoverageTest {

    private MemberDAO memberDAO;
    private GoogleLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        memberDAO = mock(MemberDAO.class);
        service = new GoogleLoginServiceImpl(
                memberDAO,
                mock(MemberSocialDAO.class),
                mock(Environment.class));
    }

    @Test
    void createGoogleMemberShouldMapProfileAndGenerateInternalMemberId() {
        GoogleUserInfoVO googleUser = new GoogleUserInfoVO();
        googleUser.setName("Google User");
        googleUser.setPicture(" https://example.com/profile.png ");

        when(memberDAO.countByNickname("Google User")).thenReturn(0);

        MemberVO member = ReflectionTestUtils.invokeMethod(
                service,
                "createGoogleMember",
                googleUser,
                "provider-123");

        assertTrue(member.getMemberId().startsWith("google_"));
        assertEquals(39, member.getMemberId().length());
        assertNull(member.getMemberPw());
        assertNull(member.getMemberName());
        assertEquals("Google User", member.getNickname());
        assertNull(member.getEmail());
        assertNull(member.getPhone());
        assertEquals("https://example.com/profile.png", member.getProfileImage());
        assertEquals("USER", member.getRole());
        assertEquals("ACTIVE", member.getStatus());
        assertEquals("N", member.getAdultVerified());
    }

    @Test
    void duplicateBaseNicknameShouldUseShortHashSuffixCandidate() {
        String providerUserId = "provider-id";
        when(memberDAO.countByNickname("구글회원")).thenReturn(1);

        String hash = ReflectionTestUtils.invokeMethod(service, "sha256", providerUserId);
        String candidate = "구글회원_" + hash.substring(0, 6);
        when(memberDAO.countByNickname(candidate)).thenReturn(0);

        assertEquals(
                candidate,
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createAvailableNickname",
                        null,
                        providerUserId));
    }
}
