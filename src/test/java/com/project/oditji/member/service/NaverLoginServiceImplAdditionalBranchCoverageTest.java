package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.dao.MemberSocialDAO;
import com.project.oditji.member.vo.MemberSocialJoinVO;

/**
 * NaverLoginServiceImpl의 입력 검증과 회원 상태/오류 메시지 분기를 추가 보완합니다.
 */
class NaverLoginServiceImplAdditionalBranchCoverageTest {

    private NaverLoginServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new NaverLoginServiceImpl(
                mock(MemberDAO.class),
                mock(MemberSocialDAO.class));
    }

    @Test
    void loginUrlShouldRejectNullAndBlankState() {
        IllegalArgumentException nullState =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getNaverLoginUrl(null));

        assertEquals(
                "네이버 로그인 state 값이 없습니다.",
                nullState.getMessage());

        IllegalArgumentException blankState =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.getNaverLoginUrl("   "));

        assertEquals(
                "네이버 로그인 state 값이 없습니다.",
                blankState.getMessage());
    }

    @Test
    void loginShouldCoverNullAndBlankCodeAndState() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.naverLogin(null, "state"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.naverLogin(" ", "state"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.naverLogin("code", null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.naverLogin("code", " "));
    }

    @Test
    void validateMemberStatusShouldCoverBlockedWithdrawnAndNormalStates() {
        MemberSocialJoinVO blocked = member("BLOCKED");
        MemberSocialJoinVO withdrawn = member("WITHDRAWN");
        MemberSocialJoinVO active = member("ACTIVE");
        MemberSocialJoinVO noStatus = member(null);

        assertThrows(
                RuntimeException.class,
                () -> invokeValidate(blocked));

        assertThrows(
                RuntimeException.class,
                () -> invokeValidate(withdrawn));

        assertDoesNotThrow(
                () -> invokeValidate(active));

        assertDoesNotThrow(
                () -> invokeValidate(noStatus));
    }

    @Test
    void errorMessageHelperShouldKeepDefaultForAllDescriptionShapes() {
        assertEquals(
                "기본",
                invokeErrorMessage("기본", null));
        assertEquals(
                "기본",
                invokeErrorMessage("기본", " "));
        assertEquals(
                "기본",
                invokeErrorMessage("기본", "provider detail"));
        assertTrue(
                invokeErrorMessage("기본", "provider detail")
                        .startsWith("기본"));
    }

    private MemberSocialJoinVO member(String status) {
        MemberSocialJoinVO member = new MemberSocialJoinVO();
        member.setStatus(status);
        return member;
    }

    private void invokeValidate(MemberSocialJoinVO member) {
        ReflectionTestUtils.invokeMethod(
                service,
                "validateMemberStatus",
                member);
    }

    private String invokeErrorMessage(String message, String detail) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "buildNaverErrorMessage",
                message,
                detail);
    }
}
