package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberSocialVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.SocialLoginResultVO;

/** Google OAuth 설정, 회원 상태, 신규 가입, 닉네임 및 API 오류 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class GoogleLoginServiceImplCoverageTest {

    @Mock
    private MemberDAO memberDAO;
    @Mock
    private MemberSocialDAO memberSocialDAO;
    @Mock
    private Environment environment;

    private GoogleLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new GoogleLoginServiceImpl(memberDAO, memberSocialDAO, environment);
        resetServer();
    }

    @Test
    void loginUrlShouldUsePrimaryConfigAndDefaultRedirect() {
        stubProperties(Map.of(
                "google.client-id",
                " primary-client "));

        String url = service.getGoogleLoginUrl("state-value");

        assertTrue(url.startsWith("https://accounts.google.com/o/oauth2/v2/auth"));
        assertTrue(url.contains("client_id=primary-client"));
        assertTrue(url.contains("redirect_uri=http://localhost:8080/oditji/member/google/callback"));
        assertTrue(url.contains("scope=openid%20profile"));
        assertTrue(url.contains("state=state-value"));
        assertTrue(url.contains("prompt=select_account"));
    }

    @Test
    void loginUrlShouldUseSpringFallbackConfigAndValidateRequiredValues() {
        stubProperties(Map.of(
                "spring.security.oauth2.client.registration.google.client-id",
                "fallback-client",
                "google.redirect-uri",
                " http://localhost/custom/callback "));

        String url = service.getGoogleLoginUrl("state");
        assertTrue(url.contains("client_id=fallback-client"));
        assertTrue(url.contains("redirect_uri=http://localhost/custom/callback"));

        assertThrows(IllegalStateException.class, () -> service.getGoogleLoginUrl(" "));
    }

    @Test
    void missingClientIdAndCodeShouldFailClearly() {
        IllegalStateException missingClient = assertThrows(
                IllegalStateException.class,
                () -> service.getGoogleLoginUrl("state"));
        assertTrue(missingClient.getMessage().contains("Client ID"));

        IllegalStateException missingCode = assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin(null));
        assertTrue(missingCode.getMessage().contains("인증 코드"));
    }

    @Test
    void existingActiveMemberShouldLoginWithoutInsert() {
        stubConfig();
        expectTokenAndUser("active-sub", "기존 회원", "/profile.jpg");
        MemberSocialJoinVO existing = joined(10L, "ACTIVE");
        when(memberSocialDAO.selectMemberBySocial("GOOGLE", "active-sub"))
                .thenReturn(existing);

        SocialLoginResultVO result = service.googleLogin("code");

        assertFalse(result.isNewMember());
        assertEquals(existing, result.getMember());
        server.verify();
    }

    @Test
    void blockedAndWithdrawnMembersShouldThrowDomainExceptions() {
        stubConfig();
        expectTokenAndUser("blocked-sub", "정지 회원", null);
        when(memberSocialDAO.selectMemberBySocial("GOOGLE", "blocked-sub"))
                .thenReturn(joined(20L, "BLOCKED"));
        assertThrows(MemberBlockedException.class, () -> service.googleLogin("blocked"));
        server.verify();

        resetServer();
        expectTokenAndUser("withdrawn-sub", "탈퇴 회원", null);
        MemberSocialJoinVO withdrawn = joined(21L, "WITHDRAWN");
        withdrawn.setWithdrawnAt(new Date());
        when(memberSocialDAO.selectMemberBySocial("GOOGLE", "withdrawn-sub"))
                .thenReturn(withdrawn);

        MemberWithdrawnException exception = assertThrows(
                MemberWithdrawnException.class,
                () -> service.googleLogin("withdrawn"));
        assertEquals(21L, exception.getMemberNo());
        server.verify();
    }

    @Test
    void newMemberShouldStoreProfileAndUniqueNickname() {
        stubConfig();
        expectTokenAndUser("new-sub", " Google User ", " https://image.test/google.jpg ");
        when(memberSocialDAO.selectMemberBySocial("GOOGLE", "new-sub"))
                .thenReturn(null)
                .thenReturn(joined(30L, "ACTIVE"));
        when(memberDAO.countByNickname(anyString()))
                .thenReturn(1)
                .thenReturn(0);
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(30L);
            return 1;
        }).when(memberDAO).insertKakaoMember(any(MemberVO.class));

        SocialLoginResultVO result = service.googleLogin("new-code");

        assertTrue(result.isNewMember());
        ArgumentCaptor<MemberVO> memberCaptor = ArgumentCaptor.forClass(MemberVO.class);
        verify(memberDAO).insertKakaoMember(memberCaptor.capture());
        MemberVO saved = memberCaptor.getValue();
        assertTrue(saved.getMemberId().startsWith("google_"));
        assertEquals(39, saved.getMemberId().length());
        assertNull(saved.getMemberPw());
        assertNull(saved.getMemberName());
        assertTrue(saved.getNickname().startsWith("Google User_"));
        assertTrue(saved.getNickname().length() <= 30);
        assertNull(saved.getEmail());
        assertNull(saved.getPhone());
        assertEquals("https://image.test/google.jpg", saved.getProfileImage());
        assertEquals("USER", saved.getRole());
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals("N", saved.getAdultVerified());

        ArgumentCaptor<MemberSocialVO> socialCaptor = ArgumentCaptor.forClass(MemberSocialVO.class);
        verify(memberSocialDAO).insertMemberSocial(socialCaptor.capture());
        assertEquals(30L, socialCaptor.getValue().getMemberNo());
        assertEquals("GOOGLE", socialCaptor.getValue().getProvider());
        assertEquals("new-sub", socialCaptor.getValue().getProviderUserId());
        server.verify();
    }

    @Test
    void exhaustedNicknameCandidatesShouldUseFallback() {
        stubConfig();
        expectTokenAndUser("fallback-sub", null, null);
        when(memberSocialDAO.selectMemberBySocial("GOOGLE", "fallback-sub"))
                .thenReturn(null)
                .thenReturn(joined(40L, "ACTIVE"));
        when(memberDAO.countByNickname(anyString())).thenReturn(1);
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(40L);
            return 1;
        }).when(memberDAO).insertKakaoMember(any(MemberVO.class));

        service.googleLogin("fallback-code");

        ArgumentCaptor<MemberVO> captor = ArgumentCaptor.forClass(MemberVO.class);
        verify(memberDAO).insertKakaoMember(captor.capture());
        assertTrue(captor.getValue().getNickname().startsWith("구글회원_"));
        assertNull(captor.getValue().getProfileImage());
        server.verify();
    }

    @Test
    void missingGeneratedMemberNumberOrJoinedLookupShouldFail() {
        stubConfig();
        expectTokenAndUser("no-number", "회원", null);
        when(memberSocialDAO.selectMemberBySocial("GOOGLE", "no-number"))
                .thenReturn(null);
        when(memberDAO.countByNickname("회원")).thenReturn(0);

        IllegalStateException noNumber = assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("no-number-code"));
        assertTrue(noNumber.getMessage().contains("회원번호"));
        server.verify();

        resetServer();
        expectTokenAndUser("no-joined", "회원2", null);
        when(memberSocialDAO.selectMemberBySocial("GOOGLE", "no-joined"))
                .thenReturn(null);
        when(memberDAO.countByNickname("회원2")).thenReturn(0);
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(51L);
            return 1;
        }).when(memberDAO).insertKakaoMember(any(MemberVO.class));

        IllegalStateException noJoined = assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("no-joined-code"));
        assertTrue(noJoined.getMessage().contains("저장 후 조회"));
        server.verify();
    }

    @Test
    void tokenAndUserApiFailuresShouldBeWrapped() {
        stubConfig();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\"}"));
        IllegalStateException tokenFailure = assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("bad-code"));
        assertTrue(tokenFailure.getMessage().contains("로그인 인증"));
        server.verify();

        resetServer();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withSuccess("{\"access_token\":\"token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"unauthorized\"}"));
        IllegalStateException userFailure = assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("bad-user"));
        assertTrue(userFailure.getMessage().contains("로그인 처리"));
        server.verify();
    }

    @Test
    void missingTokenOrUserIdentifierShouldFailClearly() {
        stubConfig();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        IllegalStateException missingToken = assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("missing-token"));
        assertTrue(missingToken.getMessage().contains("access_token"));
        server.verify();

        resetServer();
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withSuccess("{\"access_token\":\"token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        IllegalStateException missingUser = assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("missing-user"));
        assertTrue(missingUser.getMessage().contains("사용자정보 조회"));
        server.verify();
    }

    private void stubConfig() {
        stubProperties(Map.of(
                "google.client-id",
                "client-id",
                "google.client-secret",
                "client-secret",
                "google.redirect-uri",
                "http://localhost/callback"));
    }

    /**
     * Environment#getProperty 호출을 단일 Answer로 처리해 Mockito strict stubbing의
     * 속성 키별 인자 불일치 오탐을 방지합니다.
     */
    private void stubProperties(Map<String, String> properties) {
        when(environment.getProperty(anyString()))
                .thenAnswer(invocation -> properties.get(invocation.getArgument(0)));
    }

    private void expectTokenAndUser(String sub, String name, String picture) {
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"access-token\"}",
                        MediaType.APPLICATION_JSON));

        String userJson = "{\"sub\":\"" + sub + "\"," 
                + jsonField("name", name) + ","
                + jsonField("picture", picture)
                + "}";
        server.expect(requestTo("https://openidconnect.googleapis.com/v1/userinfo"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(userJson, MediaType.APPLICATION_JSON));
    }

    private String jsonField(String name, String value) {
        return "\"" + name + "\":"
                + (value == null ? "null" : "\"" + value + "\"");
    }

    private MemberSocialJoinVO joined(long memberNo, String status) {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setMemberNo(memberNo);
        member.setStatus(status);
        return member;
    }

    private void resetServer() {
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(
                service,
                "restTemplate");
        server = MockRestServiceServer.createServer(restTemplate);
    }
}
