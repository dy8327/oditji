package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.notification.service.NotificationService;

/** MemberServiceImpl 후반부의 남은 단락 조건과 성공/실패 경계값을 검증합니다. */
class MemberServiceImplTailConditionCoverageTest {

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
    void checkPasswordShouldCoverEveryGuardAndEncodedPasswordBranch() {
        assertFalse(service.checkPassword(null, "Password1!"));
        assertFalse(service.checkPassword(1L, null));
        assertFalse(service.checkPassword(1L, "   "));

        when(memberDAO.selectPasswordByMemberNo(2L)).thenReturn(null);
        assertFalse(service.checkPassword(2L, "Password1!"));

        when(memberDAO.selectPasswordByMemberNo(3L)).thenReturn("encoded");
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
        assertFalse(service.checkPassword(3L, "wrong"));

        when(memberDAO.selectPasswordByMemberNo(4L)).thenReturn("encoded2");
        when(passwordEncoder.matches("Password1!", "encoded2")).thenReturn(true);
        assertTrue(service.checkPassword(4L, "Password1!"));
    }

    @Test
    void updatePasswordShouldCoverBlankInvalidFailureAndSuccess() {
        assertThrows(IllegalArgumentException.class, () -> service.updatePassword(null, "Password1!"));
        assertThrows(IllegalArgumentException.class, () -> service.updatePassword(1L, null));
        assertThrows(IllegalArgumentException.class, () -> service.updatePassword(1L, "   "));
        assertThrows(IllegalArgumentException.class, () -> service.updatePassword(1L, "password"));

        when(passwordEncoder.encode("Password1!")).thenReturn("encoded");
        when(memberDAO.updatePassword(1L, "encoded")).thenReturn(0, 1);
        assertThrows(IllegalStateException.class, () -> service.updatePassword(1L, " Password1! "));

        service.updatePassword(1L, " Password1! ");
        verify(memberDAO, times(2)).updatePassword(1L, "encoded");
    }

    @Test
    void updateSnsEmailShouldCoverNullInvalidDuplicateAndSuccess() {
        assertThrows(IllegalArgumentException.class, () -> service.updateSnsMemberEmail(null, "a@test.com"));
        assertThrows(IllegalArgumentException.class, () -> service.updateSnsMemberEmail(1L, null));
        assertThrows(IllegalArgumentException.class, () -> service.updateSnsMemberEmail(1L, "bad-email"));

        when(memberDAO.countByEmailExceptMe("dup@test.com", 1L)).thenReturn(1);
        assertThrows(IllegalArgumentException.class, () -> service.updateSnsMemberEmail(1L, " dup@test.com "));

        when(memberDAO.countByEmailExceptMe("ok@test.com", 1L)).thenReturn(0);
        when(memberDAO.updateMemberEmail(1L, "ok@test.com")).thenReturn(1);
        service.updateSnsMemberEmail(1L, " ok@test.com ");
        verify(memberDAO).updateMemberEmail(1L, "ok@test.com");
    }
}
