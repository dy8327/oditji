package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.common.util.DateTimeUtil;
import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberSocialVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.SocialLoginResultVO;

/** 카카오 OAuth URL, 기존 회원 상태 분기, 신규 가입과 API 오류 처리를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class KakaoLoginServiceImplTest {

    @Mock
    private MemberDAO memberDAO;

    @Mock
    private MemberSocialDAO memberSocialDAO;

    private KakaoLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new KakaoLoginServiceImpl(memberDAO, memberSocialDAO);
        ReflectionTestUtils.setField(service, "kakaoClientId", "client-id");
        ReflectionTestUtils.setField(service, "kakaoRedirectUri", "http://localhost/login/kakao");
        ReflectionTestUtils.setField(service, "kakaoClientSecret", "client-secret");

        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(
                service,
                "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void loginUrlShouldContainEncodedOAuthParameters() {
        String loginUrl = service.getKakaoLoginUrl();

        assertTrue(loginUrl.startsWith("https://kauth.kakao.com/oauth/authorize"));
        assertTrue(loginUrl.contains("response_type=code"));
        assertTrue(loginUrl.contains("client_id=client-id"));
        assertTrue(loginUrl.contains("redirect_uri=http://localhost/login/kakao"));
    }

    @Test
    void existingActiveMemberShouldLoginWithoutCreatingMember() {
        expectTokenAndUser(100L, "기존 회원", "/profile.jpg", false);
        MemberSocialJoinVO existing = joinedMember(10L, "ACTIVE");
        when(memberSocialDAO.selectMemberBySocial("KAKAO", "100"))
                .thenReturn(existing);

        SocialLoginResultVO result = service.kakaoLogin("authorization-code");

        assertFalse(result.isNewMember());
        assertEquals(existing, result.getMember());
        server.verify();
    }

    @Test
    void blockedAndWithdrawnMembersShouldThrowDomainExceptions() {
        expectTokenAndUser(200L, "정지 회원", null, true);
        when(memberSocialDAO.selectMemberBySocial("KAKAO", "200"))
                .thenReturn(joinedMember(20L, "BLOCKED"));

        assertThrows(
                MemberBlockedException.class,
                () -> service.kakaoLogin("blocked-code"));
        server.verify();

        setUpServerOnly();
        expectTokenAndUser(201L, "탈퇴 회원", null, true);
        MemberSocialJoinVO withdrawn = joinedMember(21L, "WITHDRAWN");
        withdrawn.setWithdrawnAt(LocalDateTime.now(DateTimeUtil.KOREA_ZONE));
        when(memberSocialDAO.selectMemberBySocial("KAKAO", "201"))
                .thenReturn(withdrawn);

        MemberWithdrawnException exception = assertThrows(
                MemberWithdrawnException.class,
                () -> service.kakaoLogin("withdrawn-code"));
        assertEquals(21L, exception.getMemberNo());
        server.verify();
    }

    @Test
    void newMemberShouldSaveKakaoProfileAndSocialRelation() {
        expectTokenAndUser(300L, "신규 회원", "https://image.test/profile.jpg", false);
        MemberSocialJoinVO joined = joinedMember(30L, "ACTIVE");
        when(memberSocialDAO.selectMemberBySocial("KAKAO", "300"))
                .thenReturn(null)
                .thenReturn(joined);
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(30L);
            return 1;
        }).when(memberDAO).insertKakaoMember(
                org.mockito.ArgumentMatchers.any(MemberVO.class));

        SocialLoginResultVO result = service.kakaoLogin("new-code");

        assertTrue(result.isNewMember());
        assertEquals(joined, result.getMember());

        ArgumentCaptor<MemberVO> memberCaptor = ArgumentCaptor.forClass(MemberVO.class);
        verify(memberDAO).insertKakaoMember(memberCaptor.capture());
        MemberVO savedMember = memberCaptor.getValue();
        assertEquals("kakao_300", savedMember.getMemberId());
        assertNull(savedMember.getMemberPw());
        assertNull(savedMember.getMemberName());
        assertEquals("신규 회원", savedMember.getNickname());
        assertNull(savedMember.getEmail());
        assertNull(savedMember.getPhone());
        assertEquals("https://image.test/profile.jpg", savedMember.getProfileImage());
        assertEquals("USER", savedMember.getRole());
        assertEquals("ACTIVE", savedMember.getStatus());
        assertEquals("N", savedMember.getAdultVerified());

        ArgumentCaptor<MemberSocialVO> socialCaptor = ArgumentCaptor.forClass(MemberSocialVO.class);
        verify(memberSocialDAO).insertMemberSocial(socialCaptor.capture());
        MemberSocialVO savedSocial = socialCaptor.getValue();
        assertEquals(30L, savedSocial.getMemberNo());
        assertEquals("KAKAO", savedSocial.getProvider());
        assertEquals("300", savedSocial.getProviderUserId());
        server.verify();
    }

    @Test
    void missingProfileShouldUseDefaultNicknameAndNoImage() {
        expectTokenAndRawUser(400L, "{\"id\":400}");
        MemberSocialJoinVO joined = joinedMember(40L, "ACTIVE");
        when(memberSocialDAO.selectMemberBySocial("KAKAO", "400"))
                .thenReturn(null)
                .thenReturn(joined);
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(40L);
            return 1;
        }).when(memberDAO).insertKakaoMember(
                org.mockito.ArgumentMatchers.any(MemberVO.class));

        service.kakaoLogin("no-profile-code");

        ArgumentCaptor<MemberVO> memberCaptor = ArgumentCaptor.forClass(MemberVO.class);
        verify(memberDAO).insertKakaoMember(memberCaptor.capture());
        assertEquals("카카오회원", memberCaptor.getValue().getNickname());
        assertNull(memberCaptor.getValue().getProfileImage());
        server.verify();
    }

    @Test
    void tokenAndUserApiFailuresShouldBeWrapped() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\"}"));

        IllegalStateException tokenFailure = assertThrows(
                IllegalStateException.class,
                () -> service.kakaoLogin("bad-code"));
        assertTrue(tokenFailure.getMessage().contains("카카오 로그인 인증"));
        server.verify();

        setUpServerOnly();
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withSuccess(
                        "{\"access_token\":\"access-token\"}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"msg\":\"unauthorized\"}"));

        IllegalStateException userFailure = assertThrows(
                IllegalStateException.class,
                () -> service.kakaoLogin("user-failure-code"));
        assertTrue(userFailure.getMessage().contains("사용자 정보 조회"));
        server.verify();
    }

    @Test
    void missingTokenOrUserBodyShouldFailClearly() {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        IllegalStateException missingToken = assertThrows(
                IllegalStateException.class,
                () -> service.kakaoLogin("missing-token"));
        assertTrue(missingToken.getMessage().contains("access_token"));
        server.verify();

        setUpServerOnly();
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andRespond(withSuccess(
                        "{\"access_token\":\"access-token\"}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        IllegalStateException missingUser = assertThrows(
                IllegalStateException.class,
                () -> service.kakaoLogin("missing-user"));
        assertTrue(missingUser.getMessage().contains("사용자 정보 조회"));
        server.verify();
    }

    private void expectTokenAndUser(
            long id,
            String nickname,
            String profileImage,
            boolean defaultImage) {
        String imageJson = profileImage == null
                ? "null"
                : "\"" + profileImage + "\"";
        String userJson = "{\"id\":" + id
                + ",\"kakao_account\":{\"profile\":{"
                + "\"nickname\":\"" + nickname + "\","
                + "\"profile_image_url\":" + imageJson + ","
                + "\"thumbnail_image_url\":\"/thumbnail.jpg\","
                + "\"is_default_image\":" + defaultImage
                + "}}}";
        expectTokenAndRawUser(id, userJson);
    }

    private void expectTokenAndRawUser(long id, String userJson) {
        server.expect(requestTo("https://kauth.kakao.com/oauth/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"access-token-" + id + "\"}",
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(userJson, MediaType.APPLICATION_JSON));
    }

    private MemberSocialJoinVO joinedMember(long memberNo, String status) {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setMemberNo(memberNo);
        member.setStatus(status);
        return member;
    }

    private void setUpServerOnly() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(
                service,
                "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
    }
}
