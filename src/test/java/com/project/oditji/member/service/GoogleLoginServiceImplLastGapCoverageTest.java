package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.vo.GoogleTokenVO;
import com.project.oditji.member.vo.GoogleUserInfoVO;

/** Google 로그인 서비스의 재검증 식별자와 SHA-256 예외 잔여 분기를 보완합니다. */
class GoogleLoginServiceImplLastGapCoverageTest {

    private MemberDAO memberDAO;
    private MemberSocialDAO memberSocialDAO;
    private Environment environment;
    private GoogleLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        memberDAO = mock(MemberDAO.class);
        memberSocialDAO = mock(MemberSocialDAO.class);
        environment = mock(Environment.class);
        service = new GoogleLoginServiceImpl(
                memberDAO,
                memberSocialDAO,
                environment);
    }

    @Test
    void loginShouldRejectNullProviderIdWhenValueChangesAfterUserInfoValidation() {
        replaceRestTemplateWithChangingUser("stable-sub", null);
        stubConfig();

        assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("code-null-provider"));
    }

    @Test
    void loginShouldRejectBlankProviderIdWhenValueChangesAfterUserInfoValidation() {
        replaceRestTemplateWithChangingUser("stable-sub", "   ");
        stubConfig();

        assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("code-blank-provider"));
    }

    @Test
    void sha256ShouldWrapMissingAlgorithmException() {
        try (MockedStatic<MessageDigest> digestMock =
                     mockStatic(MessageDigest.class)) {

            digestMock.when(
                    () -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(
                            new NoSuchAlgorithmException(
                                    "forced-for-coverage"));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> ReflectionTestUtils.invokeMethod(
                            service,
                            "sha256",
                            "provider-id"));

            assertInstanceOf(
                    NoSuchAlgorithmException.class,
                    exception.getCause());
        }
    }

    private void replaceRestTemplateWithChangingUser(
            String validatedProviderId,
            String providerIdAfterValidation) {

        RestTemplate restTemplate = mock(RestTemplate.class);

        GoogleTokenVO token = new GoogleTokenVO();
        token.setAccessToken("access-token");

        GoogleUserInfoVO googleUser = mock(GoogleUserInfoVO.class);
        when(googleUser.getSub())
                .thenReturn(
                        validatedProviderId,
                        validatedProviderId,
                        providerIdAfterValidation);

        when(restTemplate.exchange(
                eq("https://oauth2.googleapis.com/token"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(GoogleTokenVO.class)))
                .thenReturn(ResponseEntity.ok(token));

        when(restTemplate.exchange(
                any(RequestEntity.class),
                eq(GoogleUserInfoVO.class)))
                .thenReturn(ResponseEntity.ok(googleUser));

        ReflectionTestUtils.setField(
                service,
                "restTemplate",
                restTemplate);
    }

    private void stubConfig() {
        Map<String, String> properties = Map.of(
                "google.client-id", "client-id",
                "google.client-secret", "client-secret",
                "google.redirect-uri", "http://localhost/callback");

        when(environment.getProperty(anyString()))
                .thenAnswer(invocation ->
                        properties.get(
                                invocation.getArgument(0)));
    }
}
