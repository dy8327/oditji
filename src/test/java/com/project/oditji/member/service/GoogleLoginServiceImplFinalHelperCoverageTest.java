package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

/** Google 로그인 서비스의 nickname/property helper 잔여 조건을 보완합니다. */
class GoogleLoginServiceImplFinalHelperCoverageTest {

    private MemberDAO memberDAO;
    private Environment environment;
    private GoogleLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        memberDAO = mock(MemberDAO.class);
        environment = mock(Environment.class);
        service = new GoogleLoginServiceImpl(memberDAO, mock(MemberSocialDAO.class), environment);
    }

    @Test
    void nicknameHelpersShouldCoverNullBlankWhitespaceLengthAndSuffixTruncation() {
        assertEquals("구글회원", invoke("normalizeNickname", (Object) null));
        assertEquals("구글회원", invoke("normalizeNickname", "   "));
        assertEquals("홍 길동", invoke("normalizeNickname", "  홍   길동  "));
        String longNickname = "가".repeat(80);
        String normalized = invoke("normalizeNickname", longNickname);
        assertTrue(normalized.length() <= 50);

        assertNull(invoke("limitLength", null, 3));
        assertEquals("abc", invoke("limitLength", "abc", 3));
        assertEquals("abc", invoke("limitLength", "abcdef", 3));
        String suffixed = invoke("appendNicknameSuffix", "가".repeat(60), "_123456");
        assertTrue(suffixed.endsWith("_123456"));
        assertTrue(suffixed.length() <= 50);
    }

    @Test
    void firstNonBlankAndRedirectPropertyShouldCoverNullBlankAndDefaultFallback() {
        assertNull(invoke("firstNonBlank", (Object) null));
        assertNull(invoke("firstNonBlank", (Object) new String[] {null, " "}));
        assertEquals("value", invoke("firstNonBlank", (Object) new String[] {null, " value "}));

        when(environment.getProperty("google.redirect-uri")).thenReturn(" ");
        String redirect = invoke("getGoogleRedirectUri");
        assertTrue(redirect.contains("/member/google/callback"));
    }

    @Test
    void clientPropertiesShouldUseSecondaryKeyAndRejectMissingValues() {
        when(environment.getProperty("google.client-id")).thenReturn(" ");
        when(environment.getProperty("spring.security.oauth2.client.registration.google.client-id"))
                .thenReturn(" secondary-id ");
        assertEquals("secondary-id", invoke("getGoogleClientId"));

        when(environment.getProperty("google.client-secret")).thenReturn(null);
        when(environment.getProperty("spring.security.oauth2.client.registration.google.client-secret"))
                .thenReturn("secondary-secret");
        assertEquals("secondary-secret", invoke("getGoogleClientSecret"));

        org.mockito.Mockito.reset(environment);
        assertThrows(IllegalStateException.class, () -> invoke("getGoogleClientId"));
        assertThrows(IllegalStateException.class, () -> invoke("getGoogleClientSecret"));
    }

    @Test
    void availableNicknameShouldUseBaseThenHashSuffixWhenBaseIsTaken() {
        when(memberDAO.countByNickname("Google User")).thenReturn(0);
        assertEquals("Google User", invoke("createAvailableNickname", "Google User", "provider-1"));

        when(memberDAO.countByNickname("Taken")).thenReturn(1);
        when(memberDAO.countByNickname(org.mockito.ArgumentMatchers.startsWith("Taken_"))).thenReturn(0);
        String result = invoke("createAvailableNickname", "Taken", "provider-2");
        assertTrue(result.startsWith("Taken_"));
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
