package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

/**
 * Google 로그인 서비스의 설정 fallback과 문자열 helper 조건을 보완합니다.
 */
@ExtendWith(MockitoExtension.class)
class GoogleLoginServiceImplRemainingCoverageTest {

    @Mock
    private MemberDAO memberDAO;

    @Mock
    private MemberSocialDAO memberSocialDAO;

    @Mock
    private Environment environment;

    private GoogleLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoogleLoginServiceImpl(
                memberDAO,
                memberSocialDAO,
                environment);
    }

    @Test
    void clientSecretShouldUseSpringFallbackAndRejectMissingConfig() {
        stubProperties(Map.of(
                "spring.security.oauth2.client.registration.google.client-secret",
                " fallback-secret "));

        String secret = ReflectionTestUtils.invokeMethod(
                service,
                "getGoogleClientSecret");

        assertEquals("fallback-secret", secret);

        stubProperties(Map.of());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "getGoogleClientSecret"));

        assertTrue(exception.getMessage().contains("Client Secret"));
    }

    @Test
    void redirectUriShouldUseCustomValueOrDefaultValue() {
        stubProperties(Map.of(
                "google.redirect-uri",
                " http://localhost/custom "));

        assertEquals(
                "http://localhost/custom",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getGoogleRedirectUri"));

        stubProperties(Map.of());

        assertEquals(
                "http://localhost:8080/oditji/member/google/callback",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getGoogleRedirectUri"));
    }

    @Test
    void nicknameNormalizationShouldCoverNullBlankWhitespaceAndLengthLimit() {
        assertEquals(
                "구글회원",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeNickname",
                        (Object) null));

        assertEquals(
                "구글회원",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeNickname",
                        "   "));

        assertEquals(
                "Google User Name",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeNickname",
                        "  Google   User   Name  "));

        String longName = "가".repeat(40);
        String normalized = ReflectionTestUtils.invokeMethod(
                service,
                "normalizeNickname",
                longName);

        assertEquals(30, normalized.length());
    }

    @Test
    void lengthAndSuffixHelpersShouldCoverNullShortLongAndTinyBaseSpace() {
        assertNull(ReflectionTestUtils.invokeMethod(
                service,
                "limitLength",
                null,
                10));

        assertEquals(
                "short",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "limitLength",
                        "short",
                        10));

        assertEquals(
                "abc",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "limitLength",
                        "abcdef",
                        3));

        String longSuffix = "_" + "x".repeat(35);

        String appended = ReflectionTestUtils.invokeMethod(
                service,
                "appendNicknameSuffix",
                "very-long-base",
                longSuffix);

        assertTrue(appended.startsWith("v"));
        assertTrue(appended.endsWith(longSuffix));
    }

    @Test
    void firstNonBlankShouldCoverNullArrayNullElementsBlankAndTrim() throws Exception {
        Method method =
                GoogleLoginServiceImpl.class.getDeclaredMethod(
                        "firstNonBlank",
                        String[].class);
        method.setAccessible(true);

        assertNull(method.invoke(
                service,
                new Object[] { null }));

        assertNull(method.invoke(
                service,
                new Object[] {
                        new String[] {
                                null,
                                "   "
                        }
                }));

        assertEquals(
                "value",
                method.invoke(
                        service,
                        new Object[] {
                                new String[] {
                                        null,
                                        " ",
                                        " value "
                                }
                        }));
    }

    @Test
    void userInfoRequestShouldRejectNullAndBlankAccessTokensBeforeHttpCall() {
        IllegalStateException nullToken = assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "requestUserInfo",
                        (Object) null));

        assertTrue(nullToken.getMessage().contains("토큰"));

        IllegalStateException blankToken = assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "requestUserInfo",
                        " "));

        assertTrue(blankToken.getMessage().contains("토큰"));
    }

    private void stubProperties(Map<String, String> properties) {
        Map<String, String> copy =
                new HashMap<String, String>(properties);

        when(environment.getProperty(anyString()))
                .thenAnswer(invocation ->
                        copy.get(invocation.getArgument(0)));
    }
}
