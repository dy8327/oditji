package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Date;

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

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.MemberSocialJoinVO;
import com.project.oditji.member.vo.MemberSocialVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.member.vo.SocialLoginResultVO;

/** 네이버 OAuth URL, 회원 상태, 신규 가입 및 API 오류 처리를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class NaverLoginServiceImplCoverageTest {

    @Mock
    private MemberDAO memberDAO;
    @Mock
    private MemberSocialDAO memberSocialDAO;

    private NaverLoginServiceImpl service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        service = new NaverLoginServiceImpl(memberDAO, memberSocialDAO);
        ReflectionTestUtils.setField(service, "naverClientId", "naver-client");
        ReflectionTestUtils.setField(service, "naverClientSecret", "naver-secret");
        ReflectionTestUtils.setField(
                service,
                "naverRedirectUri",
                "http://localhost:8080/oditji/member/naver/callback");
        resetServer();
    }

    @Test
    void stateAndLoginUrlShouldBeGeneratedSafely() {
        String first = service.createState();
        String second = service.createState();

        assertEquals(43, first.length());
        assertNotEquals(first, second);

        String url = service.getNaverLoginUrl(first);
        assertTrue(url.startsWith("https://nid.naver.com/oauth2.0/authorize"));
        assertTrue(url.contains("response_type=code"));
        assertTrue(url.contains("client_id=naver-client"));
        assertTrue(url.contains("state=" + first));

        assertThrows(IllegalArgumentException.class, () -> service.getNaverLoginUrl(" "));
    }

    @Test
    void loginShouldRejectMissingCodeOrStateBeforeApiCall() {
        assertThrows(IllegalArgumentException.class, () -> service.naverLogin(null, "state"));
        assertThrows(IllegalArgumentException.class, () -> service.naverLogin("code", " "));
    }

    @Test
    void existingActiveMemberShouldLoginWithoutInsert() {
        expectTokenAndProfile("active-id", "기존닉네임", "기존이름", "/profile.jpg");
        MemberSocialJoinVO existing = joined(10L, "ACTIVE");
        when(memberSocialDAO.selectMemberBySocial("NAVER", "active-id"))
                .thenReturn(existing);

        SocialLoginResultVO result = service.naverLogin("code", "state");

        assertFalse(result.isNewMember());
        assertEquals(existing, result.getMember());
        server.verify();
    }

    @Test
    void blockedAndWithdrawnMembersShouldThrowDomainExceptions() {
        expectTokenAndProfile("blocked-id", "정지", null, null);
        when(memberSocialDAO.selectMemberBySocial("NAVER", "blocked-id"))
                .thenReturn(joined(20L, "BLOCKED"));
        assertThrows(
                MemberBlockedException.class,
                () -> service.naverLogin("blocked-code", "state"));
        server.verify();

        resetServer();
        expectTokenAndProfile("withdrawn-id", null, "탈퇴", null);
        MemberSocialJoinVO withdrawn = joined(21L, "WITHDRAWN");
        withdrawn.setWithdrawnAt(new Date());
        when(memberSocialDAO.selectMemberBySocial("NAVER", "withdrawn-id"))
                .thenReturn(withdrawn);

        MemberWithdrawnException exception = assertThrows(
                MemberWithdrawnException.class,
                () -> service.naverLogin("withdrawn-code", "state"));
        assertEquals(21L, exception.getMemberNo());
        server.verify();
    }

    @Test
    void newMemberShouldStoreSafeProfileAndSocialRelation() {
        expectTokenAndProfile("new-id", "신규닉네임", "신규이름", "https://image.test/naver.jpg");
        MemberSocialJoinVO joined = joined(30L, "ACTIVE");
        when(memberSocialDAO.selectMemberBySocial("NAVER", "new-id"))
                .thenReturn(null)
                .thenReturn(joined);
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(30L);
            return 1;
        }).when(memberDAO).insertMember(any(MemberVO.class));

        SocialLoginResultVO result = service.naverLogin("new-code", "state");

        assertTrue(result.isNewMember());
        assertEquals(joined, result.getMember());

        ArgumentCaptor<MemberVO> memberCaptor = ArgumentCaptor.forClass(MemberVO.class);
        verify(memberDAO).insertMember(memberCaptor.capture());
        MemberVO saved = memberCaptor.getValue();
        assertTrue(saved.getMemberId().startsWith("naver_"));
        assertEquals(46, saved.getMemberId().length());
        assertNull(saved.getMemberPw());
        assertEquals("신규이름", saved.getMemberName());
        assertEquals("신규닉네임", saved.getNickname());
        assertNull(saved.getEmail());
        assertNull(saved.getPhone());
        assertEquals("https://image.test/naver.jpg", saved.getProfileImage());
        assertEquals("USER", saved.getRole());
        assertEquals("ACTIVE", saved.getStatus());
        assertEquals("N", saved.getAdultVerified());

        ArgumentCaptor<MemberSocialVO> socialCaptor = ArgumentCaptor.forClass(MemberSocialVO.class);
        verify(memberSocialDAO).insertMemberSocial(socialCaptor.capture());
        assertEquals(30L, socialCaptor.getValue().getMemberNo());
        assertEquals("NAVER", socialCaptor.getValue().getProvider());
        assertEquals("new-id", socialCaptor.getValue().getProviderUserId());
        server.verify();
    }

    @Test
    void missingNicknameAndNameShouldUseDefaultNickname() {
        expectTokenAndProfile("default-id", null, null, null);
        when(memberSocialDAO.selectMemberBySocial("NAVER", "default-id"))
                .thenReturn(null)
                .thenReturn(joined(40L, "ACTIVE"));
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(40L);
            return 1;
        }).when(memberDAO).insertMember(any(MemberVO.class));

        service.naverLogin("default-code", "state");

        ArgumentCaptor<MemberVO> captor = ArgumentCaptor.forClass(MemberVO.class);
        verify(memberDAO).insertMember(captor.capture());
        assertEquals("네이버회원", captor.getValue().getNickname());
        assertNull(captor.getValue().getMemberName());
        server.verify();
    }

    @Test
    void missingJoinedMemberShouldFailAfterInsert() {
        expectTokenAndProfile("missing-joined", "회원", "회원", null);
        when(memberSocialDAO.selectMemberBySocial("NAVER", "missing-joined"))
                .thenReturn(null);
        doAnswer(invocation -> {
            MemberVO member = invocation.getArgument(0);
            member.setMemberNo(50L);
            return 1;
        }).when(memberDAO).insertMember(any(MemberVO.class));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin("code", "state"));

        assertTrue(exception.getMessage().contains("연동 정보 생성"));
        server.verify();
    }

    @Test
    void tokenAndProfileFailuresShouldBeWrappedOrReported() {
        server.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\"}"));
        IllegalStateException tokenFailure = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin("bad", "state"));
        assertTrue(tokenFailure.getMessage().contains("인증"));
        server.verify();

        resetServer();
        server.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
                .andRespond(withSuccess("{\"access_token\":\"token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"unauthorized\"}"));
        IllegalStateException profileFailure = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin("bad-profile", "state"));
        assertTrue(profileFailure.getMessage().contains("로그인 처리"));
        server.verify();
    }

    @Test
    void missingTokenOrInvalidProfileBodyShouldFailClearly() {
        server.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        IllegalStateException missingToken = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin("no-token", "state"));
        assertTrue(missingToken.getMessage().contains("접근 토큰"));
        server.verify();

        resetServer();
        server.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
                .andRespond(withSuccess("{\"access_token\":\"token\"}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andRespond(withSuccess(
                        "{\"resultcode\":\"99\",\"message\":\"failure\","
                                + "\"response\":{\"id\":\"id\"}}",
                        MediaType.APPLICATION_JSON));
        IllegalStateException badResult = assertThrows(
                IllegalStateException.class,
                () -> service.naverLogin("bad-result", "state"));
        assertTrue(badResult.getMessage().contains("사용자 정보 조회"));
        server.verify();
    }

    private void expectTokenAndProfile(
            String id,
            String nickname,
            String name,
            String profileImage) {
        server.expect(requestTo("https://nid.naver.com/oauth2.0/token"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"access_token\":\"access-token\"}",
                        MediaType.APPLICATION_JSON));

        String json = "{\"resultcode\":\"00\",\"message\":\"success\","
                + "\"response\":{\"id\":\"" + id + "\"," 
                + jsonField("nickname", nickname) + ","
                + jsonField("name", name) + ","
                + jsonField("profile_image", profileImage)
                + "}}";
        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
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
