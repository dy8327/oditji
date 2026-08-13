package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;

/** 네이버 MEMBER_ID SHA-256 생성 예외 잔여 라인을 보완합니다. */
class NaverLoginServiceImplDigestGapCoverageTest {

    @Test
    void memberIdHashShouldWrapMissingSha256Algorithm() {
        NaverLoginServiceImpl service = new NaverLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class));

        try (MockedStatic<MessageDigest> digestMock =
                     mockStatic(MessageDigest.class)) {

            digestMock.when(
                    () -> MessageDigest.getInstance("SHA-256"))
                    .thenThrow(new NoSuchAlgorithmException("forced-for-coverage"));

            IllegalStateException exception = assertThrows(
                    IllegalStateException.class,
                    () -> ReflectionTestUtils.invokeMethod(
                            service,
                            "buildNaverMemberId",
                            "provider-id"));

            assertInstanceOf(
                    NoSuchAlgorithmException.class,
                    exception.getCause());
        }
    }
}
