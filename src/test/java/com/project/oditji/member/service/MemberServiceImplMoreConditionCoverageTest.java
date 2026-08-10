package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * MemberServiceImpl의 남은 null/blank AND·OR 조건을 집중 보완합니다.
 */
class MemberServiceImplMoreConditionCoverageTest {

    private MemberDAO memberDAO;
    private PasswordEncoder passwordEncoder;
    private MemberServiceImpl service;

    @BeforeEach
    void setUp() {
        memberDAO = mock(MemberDAO.class);
        passwordEncoder = mock(PasswordEncoder.class);

        service = new MemberServiceImpl(
                memberDAO,
                mock(BusinessDAO.class),
                passwordEncoder,
                mock(NotificationService.class));
    }

    @Test
    void commonMemberValidationShouldCoverNullAndBlankSecondOperandsSeparately() {
        MemberVO nullMemberIdMember =
                validMember();
        nullMemberIdMember.setMemberId(null);

        assertEquals(
                "아이디를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> invoke(
                                "validateCommonMember",
                                nullMemberIdMember))
                        .getMessage());

        MemberVO blankMemberIdMember =
                validMember();
        blankMemberIdMember.setMemberId("   ");

        assertEquals(
                "아이디를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> invoke(
                                "validateCommonMember",
                                blankMemberIdMember))
                        .getMessage());

        MemberVO blankPasswordMember =
                validMember();
        blankPasswordMember.setMemberPw(" ");

        assertEquals(
                "비밀번호를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> invoke(
                                "validateCommonMember",
                                blankPasswordMember))
                        .getMessage());

        MemberVO nullEmailMember =
                validMember();
        nullEmailMember.setEmail(null);

        assertEquals(
                "이메일을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> invoke(
                                "validateCommonMember",
                                nullEmailMember))
                        .getMessage());
    }

    @Test
    void userMemberAndOttSelectionShouldCoverSecondOperands() {
        MemberVO blankMemberNameMember =
                validMember();
        blankMemberNameMember.setMemberName(" ");

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateUserMember",
                        blankMemberNameMember));

        MemberVO nullNicknameMember =
                validMember();
        nullNicknameMember.setNickname(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateUserMember",
                        nullNicknameMember));

        List<String> emptyOttList =
                List.of();

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateOttSelection",
                        emptyOttList,
                        "N"));

        List<String> selectedOttList =
                List.of("NETFLIX");

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateOttSelection",
                        selectedOttList,
                        "Y"));

        assertDoesNotThrow(
                () -> invoke(
                        "validateOttSelection",
                        selectedOttList,
                        "N"));
    }

    @Test
    void checkPasswordShouldCoverEachGuardAndEncodedPasswordNullBranch() {
        assertFalse(service.checkPassword(null, "Password1!"));
        assertFalse(service.checkPassword(1L, null));
        assertFalse(service.checkPassword(1L, " "));

        when(memberDAO.selectPasswordByMemberNo(1L))
                .thenReturn(null);

        assertFalse(
                service.checkPassword(
                        1L,
                        "Password1!"));

        when(memberDAO.selectPasswordByMemberNo(2L))
                .thenReturn("encoded");
        when(passwordEncoder.matches(
                "Password1!",
                "encoded"))
                .thenReturn(true);

        assertTrue(
                service.checkPassword(
                        2L,
                        "Password1!"));
    }

    @Test
    void updateMemberOttShouldSkipNullAndBlankCodesAndRejectUnknownCode() {
        when(memberDAO.selectPlatformNoByCode("NETFLIX"))
                .thenReturn(10L);

        service.updateMemberOtt(
                1L,
                Arrays.asList(
                        null,
                        " ",
                        "NETFLIX"));

        verify(memberDAO)
                .insertMemberPlatform(
                        1L,
                        10L);

        when(memberDAO.selectPlatformNoByCode("UNKNOWN"))
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateMemberOtt(
                        1L,
                        List.of("UNKNOWN")));
    }

    @Test
    void newPasswordEncoderShouldCoverNullBlankAndPresentValues() {
        MemberVO member = validMember();

        member.setMemberPw(null);
        invoke("encodeNewPassword", member);
        assertNull(member.getMemberPw());

        member.setMemberPw("   ");
        invoke("encodeNewPassword", member);
        assertNull(member.getMemberPw());

        member.setMemberPw("Password1!");
        when(passwordEncoder.encode("Password1!"))
                .thenReturn("encoded");

        invoke("encodeNewPassword", member);

        assertEquals(
                "encoded",
                member.getMemberPw());
    }

    @Test
    void snsEmailUpdateShouldCoverBlankSecondOperandAndFailureCount() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateSnsMemberEmail(
                        1L,
                        "   "));

        when(memberDAO.countByEmailExceptMe(
                "new@test.com",
                1L))
                .thenReturn(0);
        when(memberDAO.updateMemberEmail(
                1L,
                "new@test.com"))
                .thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> service.updateSnsMemberEmail(
                        1L,
                        " new@test.com "));

        verify(memberDAO, never())
                .updateMemberEmail(
                        1L,
                        "   ");
    }

    private MemberVO validMember() {
        MemberVO member = new MemberVO();
        member.setMemberNo(1L);
        member.setMemberId("member");
        member.setMemberPw("Password1!");
        member.setEmail("member@test.com");
        member.setMemberName("회원");
        member.setNickname("닉네임");
        return member;
    }

    private void invoke(
            String methodName,
            Object... arguments) {

        ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
