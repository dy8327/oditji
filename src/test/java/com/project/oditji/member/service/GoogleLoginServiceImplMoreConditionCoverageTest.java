package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

/**
 * Google 로그인 helper의 null/blank 단축평가와 길이 경계값을 보완합니다.
 */
class GoogleLoginServiceImplMoreConditionCoverageTest {

    private GoogleLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GoogleLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class),
                mock(Environment.class));
    }

    @Test
    void loginAndUserInfoShouldCoverBlankSecondOperands() {
        assertThrows(
                IllegalStateException.class,
                () -> service.googleLogin("   "));

        assertThrows(
                IllegalStateException.class,
                () -> invoke(
                        "requestUserInfo",
                        "   "));
    }

    @Test
    void firstNonBlankShouldCoverNullArrayNullEntryBlankEntryAndTrimmedValue() {
        assertNull(
                invoke(
                        "firstNonBlank",
                        (Object) null));

        assertNull(
                invoke(
                        "firstNonBlank",
                        (Object)
                                new String[] {
                                        null,
                                        " ",
                                        "   "
                                }));

        assertEquals(
                "value",
                invoke(
                        "firstNonBlank",
                        (Object)
                                new String[] {
                                        null,
                                        " ",
                                        " value "
                                }));
    }

    @Test
    void nicknameNormalizerShouldUseDefaultForNullAndBlankAndCollapseSpaces() {
        assertEquals(
                "구글회원",
                invoke(
                        "normalizeNickname",
                        (Object) null));

        assertEquals(
                "구글회원",
                invoke(
                        "normalizeNickname",
                        "   "));

        assertEquals(
                "Google User",
                invoke(
                        "normalizeNickname",
                        "  Google   User  "));
    }

    @Test
    void limitLengthShouldCoverNullShortExactAndLongValues() {
        assertNull(
                invoke(
                        "limitLength",
                        null,
                        5));

        assertEquals(
                "abc",
                invoke(
                        "limitLength",
                        "abc",
                        5));

        assertEquals(
                "abcde",
                invoke(
                        "limitLength",
                        "abcde",
                        5));

        assertEquals(
                "abcde",
                invoke(
                        "limitLength",
                        "abcdef",
                        5));
    }

    @Test
    void appendSuffixShouldProtectBaseLengthWhenSuffixConsumesMostCapacity() {
        String result = invoke(
                "appendNicknameSuffix",
                "아주아주아주아주아주아주아주아주긴닉네임",
                "_" + "x".repeat(28));

        assertEquals(30, result.length());
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(
            String methodName,
            Object... arguments) {

        return (T)
                ReflectionTestUtils.invokeMethod(
                        service,
                        methodName,
                        arguments);
    }
}
