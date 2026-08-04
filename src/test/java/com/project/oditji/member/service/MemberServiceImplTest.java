package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.NotificationService;

/**
 * 회원 가입, 로그인, 회원정보 수정, 탈퇴·복구 및 비밀번호 변경의
 * 정상·실패 분기를 DB 연결 없이 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceImplTest {

    private static final String RAW_PASSWORD = "Password1!";
    private static final String ENCODED_PASSWORD = "encoded-password";

    @Mock
    private MemberDAO memberDAO;

    @Mock
    private BusinessDAO businessDAO;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberServiceImpl(
                memberDAO,
                businessDAO,
                passwordEncoder,
                notificationService);
    }

    @Test
    void joinMemberShouldRegisterUserWithoutOtt() {

        MemberVO member = createUserMember(1L);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        memberService.joinMember(member, null, "Y");

        assertEquals("USER", member.getRole());
        assertEquals("ACTIVE", member.getStatus());
        assertEquals("N", member.getAdultVerified());
        assertEquals(ENCODED_PASSWORD, member.getMemberPw());
        verify(memberDAO).insertMember(member);
        verify(memberDAO, never()).insertMemberPlatform(anyLong(), anyLong());
    }

    @Test
    void joinMemberShouldRegisterEverySelectedOtt() {

        MemberVO member = createUserMember(2L);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(memberDAO.selectPlatformNoByCode("NETFLIX")).thenReturn(10L);
        when(memberDAO.selectPlatformNoByCode("TVING")).thenReturn(20L);

        memberService.joinMember(
                member,
                List.of("NETFLIX", "TVING"),
                "N");

        verify(memberDAO).insertMemberPlatform(2L, 10L);
        verify(memberDAO).insertMemberPlatform(2L, 20L);
    }

    @Test
    void joinMemberShouldRejectMissingMemberInformation() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> memberService.joinMember(null, null, "Y"));

        assertEquals("회원 정보가 없습니다.", exception.getMessage());
    }

    @Test
    void joinMemberShouldRejectBlankRequiredMemberFields() {

        MemberVO missingId = createUserMember(1L);
        missingId.setMemberId(" ");
        assertEquals(
                "아이디를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(missingId, null, "Y"))
                        .getMessage());

        MemberVO missingPassword = createUserMember(1L);
        missingPassword.setMemberPw(null);
        assertEquals(
                "비밀번호를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(missingPassword, null, "Y"))
                        .getMessage());

        MemberVO missingEmail = createUserMember(1L);
        missingEmail.setEmail(" ");
        assertEquals(
                "이메일을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(missingEmail, null, "Y"))
                        .getMessage());
    }

    @Test
    void joinMemberShouldRejectMissingNameOrNickname() {

        MemberVO missingName = createUserMember(1L);
        missingName.setMemberName(null);
        assertEquals(
                "이름을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(missingName, null, "Y"))
                        .getMessage());

        MemberVO missingNickname = createUserMember(1L);
        missingNickname.setNickname(" ");
        assertEquals(
                "닉네임을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(missingNickname, null, "Y"))
                        .getMessage());
    }

    @Test
    void joinMemberShouldRejectInvalidOttSelection() {

        MemberVO member = createUserMember(1L);
        assertEquals(
                "사용 중인 OTT를 선택하거나 OTT 없음을 선택해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(member, null, "N"))
                        .getMessage());

        assertEquals(
                "OTT 없음과 다른 OTT는 동시에 선택할 수 없습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(
                                member,
                                List.of("NETFLIX"),
                                "Y"))
                        .getMessage());
    }

    @Test
    void joinMemberShouldRejectDuplicateMemberValues() {

        MemberVO duplicateId = createUserMember(1L);
        when(memberDAO.countByMemberId(duplicateId.getMemberId())).thenReturn(1);
        assertEquals(
                "이미 사용 중인 아이디입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(duplicateId, null, "Y"))
                        .getMessage());

        MemberVO duplicateEmail = createUserMember(1L);
        when(memberDAO.countByMemberId(duplicateEmail.getMemberId())).thenReturn(0);
        when(memberDAO.countByEmail(duplicateEmail.getEmail())).thenReturn(1);
        assertEquals(
                "이미 사용 중인 이메일입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(duplicateEmail, null, "Y"))
                        .getMessage());

        MemberVO duplicateNickname = createUserMember(1L);
        when(memberDAO.countByMemberId(duplicateNickname.getMemberId())).thenReturn(0);
        when(memberDAO.countByEmail(duplicateNickname.getEmail())).thenReturn(0);
        when(memberDAO.countByNickname(duplicateNickname.getNickname())).thenReturn(1);
        assertEquals(
                "이미 사용 중인 닉네임입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(duplicateNickname, null, "Y"))
                        .getMessage());
    }

    @Test
    void joinMemberShouldRejectMissingGeneratedMemberNo() {

        MemberVO member = createUserMember(null);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        assertEquals(
                "회원 번호 생성에 실패했습니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> memberService.joinMember(member, null, "Y"))
                        .getMessage());
    }

    @Test
    void joinMemberShouldRejectUnknownOttCode() {

        MemberVO member = createUserMember(1L);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(memberDAO.selectPlatformNoByCode("UNKNOWN")).thenReturn(null);

        assertEquals(
                "존재하지 않는 OTT 플랫폼입니다: UNKNOWN",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinMember(
                                member,
                                List.of("UNKNOWN"),
                                "N"))
                        .getMessage());
    }

    @Test
    void joinBusinessMemberShouldRegisterWaitingBusinessAndNotifyAdmins() {

        MemberVO member = createBusinessMember(5L);
        BusinessVO business = createBusiness(30L);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(businessDAO.insertBusiness(business)).thenReturn(1);

        memberService.joinBusinessMember(member, business);

        assertEquals("BUSINESS", member.getRole());
        assertEquals("ACTIVE", member.getStatus());
        assertEquals("N", member.getAdultVerified());
        assertNull(member.getMemberName());
        assertNull(member.getNickname());
        assertEquals(ENCODED_PASSWORD, member.getMemberPw());
        assertEquals(5L, business.getMemberNo().longValue());
        assertEquals("BRONZE", business.getGradeName());
        assertEquals("WAITING", business.getStatus());
        verify(notificationService).createForAdmins(
                "BUSINESS_REQUEST",
                "사업자 승인 요청",
                "테스트상점의 사업자 가입 승인 요청이 접수되었습니다.",
                "/admin/business/list?tab=approval",
                "BUSINESS",
                30L);
    }

    @Test
    void joinBusinessMemberShouldRejectMissingOrInvalidBusinessInformation() {

        MemberVO member = createBusinessMember(5L);
        assertEquals(
                "사업자 정보가 없습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(member, null))
                        .getMessage());

        BusinessVO missingName = createBusiness(30L);
        missingName.setBusinessName(" ");
        assertEquals(
                "상호명을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(member, missingName))
                        .getMessage());

        BusinessVO missingNumber = createBusiness(30L);
        missingNumber.setBusinessNumber(null);
        assertEquals(
                "사업자등록번호를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(member, missingNumber))
                        .getMessage());

        BusinessVO missingRepresentative = createBusiness(30L);
        missingRepresentative.setRepresentativeName(" ");
        assertEquals(
                "대표자명을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(
                                member,
                                missingRepresentative))
                        .getMessage());

        BusinessVO invalidOpenDate = createBusiness(30L);
        invalidOpenDate.setOpenDate("2026-01-01");
        assertEquals(
                "개업일 형식이 올바르지 않습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(
                                member,
                                invalidOpenDate))
                        .getMessage());
    }

    @Test
    void joinBusinessMemberShouldRejectDuplicateValues() {

        MemberVO member = createBusinessMember(5L);
        BusinessVO business = createBusiness(30L);
        when(memberDAO.countByMemberId(member.getMemberId())).thenReturn(1);
        assertEquals(
                "이미 사용 중인 아이디입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(member, business))
                        .getMessage());

        when(memberDAO.countByMemberId(member.getMemberId())).thenReturn(0);
        when(memberDAO.countByEmail(member.getEmail())).thenReturn(1);
        assertEquals(
                "이미 사용 중인 이메일입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(member, business))
                        .getMessage());

        when(memberDAO.countByEmail(member.getEmail())).thenReturn(0);
        when(businessDAO.countByBusinessNumber(
                business.getBusinessNumber())).thenReturn(1);
        assertEquals(
                "이미 등록된 사업자등록번호입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.joinBusinessMember(member, business))
                        .getMessage());
    }

    @Test
    void joinBusinessMemberShouldRejectInsertFailures() {

        MemberVO memberWithoutNo = createBusinessMember(null);
        BusinessVO business = createBusiness(30L);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        assertEquals(
                "회원 번호 생성에 실패했습니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> memberService.joinBusinessMember(
                                memberWithoutNo,
                                business))
                        .getMessage());

        MemberVO member = createBusinessMember(5L);
        when(businessDAO.insertBusiness(business)).thenReturn(0);
        assertEquals(
                "사업자 정보 등록에 실패했습니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> memberService.joinBusinessMember(member, business))
                        .getMessage());
        verify(notificationService, never()).createForAdmins(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyLong());
    }

    @Test
    void duplicateChecksShouldUseDaoCounts() {

        when(memberDAO.countByMemberId("user01")).thenReturn(1);
        when(memberDAO.countByEmail("user@test.com")).thenReturn(0);
        when(memberDAO.countByNickname("닉네임")).thenReturn(2);

        assertTrue(memberService.isDuplicateId("user01"));
        assertFalse(memberService.isDuplicateEmail("user@test.com"));
        assertTrue(memberService.isDuplicateNickname("닉네임"));
    }

    @Test
    void loginMemberShouldReturnNullForMissingMemberOrWrongPassword() {

        MemberVO request = createLoginRequest();
        when(memberDAO.loginMember(request)).thenReturn(null);
        assertNull(memberService.loginMember(request));

        MemberVO stored = createStoredLoginMember("ACTIVE");
        when(memberDAO.loginMember(request)).thenReturn(stored);
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                .thenReturn(false);
        assertNull(memberService.loginMember(request));
    }

    @Test
    void loginMemberShouldRejectBlockedAndWithdrawnMembers() {

        MemberVO request = createLoginRequest();
        MemberVO blocked = createStoredLoginMember("BLOCKED");
        when(memberDAO.loginMember(request)).thenReturn(blocked);
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        assertThrows(
                MemberBlockedException.class,
                () -> memberService.loginMember(request));

        Date withdrawnAt = new Date();
        MemberVO withdrawn = createStoredLoginMember("WITHDRAWN");
        withdrawn.setWithdrawnAt(withdrawnAt);
        when(memberDAO.loginMember(request)).thenReturn(withdrawn);

        MemberWithdrawnException exception = assertThrows(
                MemberWithdrawnException.class,
                () -> memberService.loginMember(request));
        assertEquals(9L, exception.getMemberNo().longValue());
        assertSame(withdrawnAt, exception.getWithdrawnAt());
    }

    @Test
    void loginMemberShouldClearPasswordForActiveMember() {

        MemberVO request = createLoginRequest();
        MemberVO stored = createStoredLoginMember("ACTIVE");
        when(memberDAO.loginMember(request)).thenReturn(stored);
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);

        MemberVO result = memberService.loginMember(request);

        assertSame(stored, result);
        assertNull(result.getMemberPw());
    }

    @Test
    void updateMemberShouldTrimValuesAndEncodeNewPassword() {

        MemberVO member = new MemberVO();
        member.setMemberNo(1L);
        member.setNickname("  새닉네임  ");
        member.setEmail("  new@test.com  ");
        member.setMemberPw(RAW_PASSWORD);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(memberDAO.updateMember(member)).thenReturn(1);

        memberService.updateMember(member);

        assertEquals("새닉네임", member.getNickname());
        assertEquals("new@test.com", member.getEmail());
        assertEquals(ENCODED_PASSWORD, member.getMemberPw());
    }

    @Test
    void updateMemberShouldAllowNullEmailAndBlankNewPassword() {

        MemberVO member = new MemberVO();
        member.setMemberNo(1L);
        member.setNickname("닉네임");
        member.setEmail(null);
        member.setMemberPw(" ");
        when(memberDAO.updateMember(member)).thenReturn(1);

        memberService.updateMember(member);

        assertNull(member.getEmail());
        assertNull(member.getMemberPw());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateMemberShouldRejectInvalidTargetOrNickname() {

        assertEquals(
                "회원 정보가 올바르지 않습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMember(null))
                        .getMessage());

        MemberVO member = new MemberVO();
        member.setMemberNo(1L);
        member.setNickname(" ");
        assertEquals(
                "닉네임을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMember(member))
                        .getMessage());

        member.setNickname("a!");
        assertEquals(
                "닉네임은 한글, 영문, 숫자 2~10자로 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMember(member))
                        .getMessage());

        member.setNickname("중복닉네임");
        when(memberDAO.countByNicknameExceptMe("중복닉네임", 1L))
                .thenReturn(1);
        assertEquals(
                "이미 사용 중인 닉네임입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMember(member))
                        .getMessage());
    }

    @Test
    void updateMemberShouldRejectInvalidOrDuplicateEmail() {

        MemberVO member = createUpdateMember();
        member.setEmail(" ");
        assertEquals(
                "이메일을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMember(member))
                        .getMessage());

        member.setEmail("invalid-email");
        assertEquals(
                "올바른 이메일 형식으로 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMember(member))
                        .getMessage());

        member.setEmail("duplicate@test.com");
        when(memberDAO.countByEmailExceptMe("duplicate@test.com", 1L))
                .thenReturn(1);
        assertEquals(
                "이미 사용 중인 이메일입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMember(member))
                        .getMessage());
    }

    @Test
    void updateMemberShouldRejectDaoUpdateFailure() {

        MemberVO member = createUpdateMember();
        when(memberDAO.updateMember(member)).thenReturn(0);

        assertEquals(
                "회원정보 수정에 실패했습니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> memberService.updateMember(member))
                        .getMessage());
    }

    @Test
    void updateMemberOttShouldReplaceValidOttList() {

        when(memberDAO.selectPlatformNoByCode("NETFLIX")).thenReturn(10L);
        when(memberDAO.selectPlatformNoByCode("TVING")).thenReturn(20L);

        memberService.updateMemberOtt(
                1L,
                List.of("NETFLIX", " ", "TVING"));

        verify(memberDAO).deleteMemberPlatform(1L);
        verify(memberDAO).insertMemberPlatform(1L, 10L);
        verify(memberDAO).insertMemberPlatform(1L, 20L);
    }

    @Test
    void updateMemberOttShouldOnlyDeleteWhenNoOttSelected() {

        memberService.updateMemberOtt(1L, null);

        verify(memberDAO).deleteMemberPlatform(1L);
        verify(memberDAO, never()).selectPlatformNoByCode(anyString());
    }

    @Test
    void updateMemberOttShouldRejectUnknownOtt() {

        when(memberDAO.selectPlatformNoByCode("UNKNOWN")).thenReturn(null);

        assertEquals(
                "존재하지 않는 OTT: UNKNOWN",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateMemberOtt(
                                1L,
                                List.of("UNKNOWN")))
                        .getMessage());
    }

    @Test
    void withdrawAndRestoreShouldDelegateToDao() {

        when(memberDAO.restoreMember(1L)).thenReturn(1);

        memberService.withdrawMember(1L);
        memberService.restoreMember(1L);

        verify(memberDAO).withdrawMember(1L);
        verify(memberDAO).restoreMember(1L);
    }

    @Test
    void restoreMemberShouldRejectInvalidOrExpiredRequest() {

        assertEquals(
                "잘못된 요청입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.restoreMember(null))
                        .getMessage());

        when(memberDAO.restoreMember(1L)).thenReturn(0);
        assertEquals(
                "복구 가능한 기간이 지났거나 이미 처리된 계정입니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> memberService.restoreMember(1L))
                        .getMessage());
    }

    @Test
    void checkPasswordShouldCoverInvalidMissingAndMatchingPasswords() {

        assertFalse(memberService.checkPassword(null, RAW_PASSWORD));
        assertFalse(memberService.checkPassword(1L, " "));

        when(memberDAO.selectPasswordByMemberNo(1L)).thenReturn(null);
        assertFalse(memberService.checkPassword(1L, RAW_PASSWORD));

        when(memberDAO.selectPasswordByMemberNo(1L))
                .thenReturn(ENCODED_PASSWORD);
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                .thenReturn(true);
        assertTrue(memberService.checkPassword(1L, RAW_PASSWORD));
    }

    @Test
    void simpleLookupMethodsShouldReturnDaoResults() {

        MemberVO expected = new MemberVO();
        MemberVO request = new MemberVO();
        when(memberDAO.getMemberByNo(1L)).thenReturn(expected);
        when(memberDAO.countByNicknameExceptMe("닉네임", 1L)).thenReturn(0);
        when(memberDAO.countByEmailExceptMe("user@test.com", 1L)).thenReturn(1);
        when(memberDAO.findId(request)).thenReturn(expected);
        when(memberDAO.findPw(request)).thenReturn(expected);

        assertSame(expected, memberService.getMemberByNo(1L));
        assertTrue(memberService.checkUpdateNickname(1L, "닉네임"));
        assertFalse(memberService.checkUpdateEmail(1L, "user@test.com"));
        assertSame(expected, memberService.findId(request));
        assertSame(expected, memberService.findPw(request));
    }

    @Test
    void updatePasswordShouldEncodeTrimmedValidPassword() {

        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(memberDAO.updatePassword(1L, ENCODED_PASSWORD)).thenReturn(1);

        memberService.updatePassword(1L, "  " + RAW_PASSWORD + "  ");

        verify(passwordEncoder).encode(RAW_PASSWORD);
        verify(memberDAO).updatePassword(1L, ENCODED_PASSWORD);
    }

    @Test
    void updatePasswordShouldRejectInvalidInputAndDaoFailure() {

        assertEquals(
                "회원 정보가 올바르지 않습니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updatePassword(null, RAW_PASSWORD))
                        .getMessage());

        assertEquals(
                "새 비밀번호를 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updatePassword(1L, " "))
                        .getMessage());

        assertEquals(
                "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updatePassword(1L, "password"))
                        .getMessage());

        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(memberDAO.updatePassword(1L, ENCODED_PASSWORD)).thenReturn(0);
        assertEquals(
                "비밀번호 변경에 실패했습니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> memberService.updatePassword(1L, RAW_PASSWORD))
                        .getMessage());
    }

    @Test
    void updateSnsMemberEmailShouldTrimAndUpdateValidEmail() {

        when(memberDAO.updateMemberEmail(1L, "sns@test.com")).thenReturn(1);

        memberService.updateSnsMemberEmail(1L, "  sns@test.com  ");

        verify(memberDAO).updateMemberEmail(1L, "sns@test.com");
    }

    @Test
    void updateSnsMemberEmailShouldRejectInvalidDuplicateOrFailedUpdate() {

        assertEquals(
                "잘못된 요청입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateSnsMemberEmail(
                                null,
                                "sns@test.com"))
                        .getMessage());

        assertEquals(
                "이메일을 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateSnsMemberEmail(1L, " "))
                        .getMessage());

        assertEquals(
                "올바른 이메일 형식으로 입력해주세요.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateSnsMemberEmail(
                                1L,
                                "invalid"))
                        .getMessage());

        when(memberDAO.countByEmailExceptMe("sns@test.com", 1L))
                .thenReturn(1);
        assertEquals(
                "이미 사용 중인 이메일입니다.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> memberService.updateSnsMemberEmail(
                                1L,
                                "sns@test.com"))
                        .getMessage());

        when(memberDAO.countByEmailExceptMe("sns@test.com", 1L))
                .thenReturn(0);
        when(memberDAO.updateMemberEmail(1L, "sns@test.com")).thenReturn(0);
        assertEquals(
                "이메일 등록에 실패했습니다.",
                assertThrows(
                        IllegalStateException.class,
                        () -> memberService.updateSnsMemberEmail(
                                1L,
                                "sns@test.com"))
                        .getMessage());
    }

    private MemberVO createUserMember(Long memberNo) {

        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setMemberId("user01");
        member.setMemberPw(RAW_PASSWORD);
        member.setMemberName("사용자");
        member.setNickname("닉네임");
        member.setEmail("user@test.com");
        return member;
    }

    private MemberVO createBusinessMember(Long memberNo) {

        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        member.setMemberId("business01");
        member.setMemberPw(RAW_PASSWORD);
        member.setMemberName("입력된이름");
        member.setNickname("입력된닉네임");
        member.setEmail("business@test.com");
        return member;
    }

    private BusinessVO createBusiness(Long businessNo) {

        BusinessVO business = new BusinessVO();
        business.setBusinessNo(businessNo);
        business.setBusinessName("테스트상점");
        business.setBusinessNumber("123-45-67890");
        business.setRepresentativeName("대표자");
        business.setOpenDate("20260101");
        return business;
    }

    private MemberVO createLoginRequest() {

        MemberVO request = new MemberVO();
        request.setMemberId("user01");
        request.setMemberPw(RAW_PASSWORD);
        return request;
    }

    private MemberVO createStoredLoginMember(String status) {

        MemberVO member = new MemberVO();
        member.setMemberNo(9L);
        member.setMemberPw(ENCODED_PASSWORD);
        member.setStatus(status);
        return member;
    }

    private MemberVO createUpdateMember() {

        MemberVO member = new MemberVO();
        member.setMemberNo(1L);
        member.setNickname("닉네임");
        member.setEmail("user@test.com");
        member.setMemberPw(null);
        return member;
    }
}
