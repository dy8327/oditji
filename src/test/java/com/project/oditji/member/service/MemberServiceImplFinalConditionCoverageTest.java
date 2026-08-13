package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.NotificationService;

/** 회원 서비스의 null/blank short-circuit 잔여 조건을 집중 보완합니다. */
class MemberServiceImplFinalConditionCoverageTest {

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
    void commonValidationShouldCoverRemainingNullAndBlankOperands() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateCommonMember", new Object[] { null }));

        MemberVO nullPassword = validMember();
        nullPassword.setMemberPw(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateCommonMember", nullPassword));

        MemberVO blankEmail = validMember();
        blankEmail.setEmail("   ");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateCommonMember", blankEmail));

        assertDoesNotThrow(() -> invoke("validateCommonMember", validMember()));
    }

    @Test
    void userValidationShouldCoverNullNameAndBlankNickname() {
        MemberVO nullName = validMember();
        nullName.setMemberName(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateUserMember", nullName));

        MemberVO blankNickname = validMember();
        blankNickname.setNickname("   ");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateUserMember", blankNickname));
    }

    @Test
    void ottValidationShouldCoverNullListAndNoOttBooleanCombinations() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateOttSelection", null, "N"));

        assertDoesNotThrow(
                () -> invoke("validateOttSelection", null, "Y"));

        List<String> selected = List.of("NETFLIX");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateOttSelection", selected, "Y"));
        assertDoesNotThrow(
                () -> invoke("validateOttSelection", selected, null));
    }

    @Test
    void updateTargetNicknameAndEmailShouldCoverRemainingGuards() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateUpdateTarget", new Object[] { null }));

        MemberVO noMemberNo = validMember();
        noMemberNo.setMemberNo(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateUpdateTarget", noMemberNo));

        MemberVO nullNickname = validMember();
        nullNickname.setNickname(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndSetNickname", nullNickname));

        MemberVO blankNickname = validMember();
        blankNickname.setNickname("  ");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndSetNickname", blankNickname));

        MemberVO nullEmail = validMember();
        nullEmail.setEmail(null);
        assertDoesNotThrow(() -> invoke("validateAndSetEmail", nullEmail));
        assertNull(nullEmail.getEmail());

        MemberVO blankEmail = validMember();
        blankEmail.setEmail("  ");
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndSetEmail", blankEmail));

        MemberVO validUpdate = validMember();
        validUpdate.setNickname(" 새닉네임 ");
        validUpdate.setEmail(" new@test.com ");
        when(memberDAO.countByNicknameExceptMe("새닉네임", 1L)).thenReturn(0);
        when(memberDAO.countByEmailExceptMe("new@test.com", 1L)).thenReturn(0);
        invoke("validateAndSetNickname", validUpdate);
        invoke("validateAndSetEmail", validUpdate);
        assertEquals("새닉네임", validUpdate.getNickname());
        assertEquals("new@test.com", validUpdate.getEmail());
    }

    @Test
    void passwordEncodingShouldCoverNullBlankAndNonBlankSecondCondition() {
        MemberVO member = validMember();

        member.setMemberPw(null);
        invoke("encodeNewPassword", member);
        assertNull(member.getMemberPw());

        member.setMemberPw("   ");
        invoke("encodeNewPassword", member);
        assertNull(member.getMemberPw());

        member.setMemberPw("Password1!");
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded");
        invoke("encodeNewPassword", member);
        assertEquals("encoded", member.getMemberPw());
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

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
