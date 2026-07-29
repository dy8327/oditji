package com.project.oditji.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.notification.service.NotificationService;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberDAO memberDAO;
    private final PasswordEncoder passwordEncoder;
    private final BusinessDAO businessDAO;
    private final NotificationService notificationService;

    public MemberServiceImpl(
            MemberDAO memberDAO,
            BusinessDAO businessDAO,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {

        this.memberDAO = memberDAO;
        this.businessDAO = businessDAO;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public void joinMember(MemberVO memberVO, List<String> ottList, String noOtt) {

        validateCommonMember(memberVO);
        validateUserMember(memberVO);
        validateOttSelection(ottList, noOtt);

        if (memberDAO.countByMemberId(memberVO.getMemberId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (memberDAO.countByEmail(memberVO.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (memberDAO.countByNickname(memberVO.getNickname()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        memberVO.setRole("USER");
        memberVO.setStatus("ACTIVE");
        memberVO.setAdultVerified("N");
        memberVO.setMemberPw(passwordEncoder.encode(memberVO.getMemberPw()));

        memberDAO.insertMember(memberVO);

        Long memberNo = memberVO.getMemberNo();

        System.out.println("생성된 MEMBER_NO = " + memberNo);

        if (memberNo == null) {
            throw new IllegalStateException("회원 번호 생성에 실패했습니다.");
        }

        /*
         * =========================================================
         * 일반회원 OTT 없음 처리
         *
         * OTT 없음이 선택된 경우 ottList가 비어 있으므로
         * MEMBER_PLATFORM에는 별도 데이터를 저장하지 않는다.
         * =========================================================
         */
        if (ottList == null || ottList.isEmpty()) {
            return;
        }

        for (String platformCode : ottList) {
            System.out.println("선택된 OTT 코드 = [" + platformCode + "]");

            Long platformNo = memberDAO.selectPlatformNoByCode(platformCode);

            System.out.println("조회된 PLATFORM_NO = " + platformNo);

            if (platformNo == null) {
                throw new IllegalArgumentException("존재하지 않는 OTT 플랫폼입니다: " + platformCode);
            }

            memberDAO.insertMemberPlatform(memberNo, platformNo);
        }
    }

    /* 사업자 가입 */
    @Override
    @Transactional
    public void joinBusinessMember(MemberVO memberVO, BusinessVO businessVO) {

        /* 사업자는 이름, 닉네임, OTT 검증을 하지 않는다. */
        validateCommonMember(memberVO);

        if (businessVO == null) {
            throw new IllegalArgumentException("사업자 정보가 없습니다.");
        }

        if (businessVO.getBusinessName() == null || businessVO.getBusinessName().isBlank()) {
            throw new IllegalArgumentException("상호명을 입력해주세요.");
        }

        if (businessVO.getBusinessNumber() == null || businessVO.getBusinessNumber().isBlank()) {
            throw new IllegalArgumentException("사업자등록번호를 입력해주세요.");
        }

        if (businessVO.getRepresentativeName() == null || businessVO.getRepresentativeName().isBlank()) {
            throw new IllegalArgumentException("대표자명을 입력해주세요.");
        }

        if (businessVO.getOpenDate() == null || !businessVO.getOpenDate().matches("\\d{8}")) {
            throw new IllegalArgumentException("개업일 형식이 올바르지 않습니다.");
        }

        /* 회원정보 중복 재검사 */
        if (memberDAO.countByMemberId(memberVO.getMemberId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        if (memberDAO.countByEmail(memberVO.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        /*
         * 사업자는 닉네임을 사용하지 않으므로
         * countByNickname() 검사하지 않음
         */

        /* 사업자등록번호 중복 재검사 */
        if (businessDAO.countByBusinessNumber(businessVO.getBusinessNumber()) > 0) {
            throw new IllegalArgumentException("이미 등록된 사업자등록번호입니다.");
        }

        /* MEMBER 등록 */
        memberVO.setRole("BUSINESS");
        memberVO.setStatus("ACTIVE");
        memberVO.setAdultVerified("N");

        /* 사업자는 일반회원용 이름/닉네임을 사용하지 않음 */
        memberVO.setMemberName(null);
        memberVO.setNickname(null);
        memberVO.setMemberPw(passwordEncoder.encode(memberVO.getMemberPw()));

        memberDAO.insertMember(memberVO);

        Long memberNo = memberVO.getMemberNo();

        if (memberNo == null) {
            throw new IllegalStateException("회원 번호 생성에 실패했습니다.");
        }

        /* MEMBER와 BUSINESS 연결 */
        businessVO.setMemberNo(memberNo);

        /*
         * 서버에서 국세청 검증 후 설정할 값
         *
         * 최종적으로는 Controller에서 받은 값을 믿지 않고
         * 서버 검증 결과를 사용한다.
         */
        businessVO.setGradeName("BRONZE");
        businessVO.setStatus("WAITING");

        int inserted = businessDAO.insertBusiness(businessVO);

        if (inserted != 1) {
            throw new IllegalStateException("사업자 정보 등록에 실패했습니다.");
        }

        /* 사업자 가입 승인 요청을 관리자 업무 알림으로 등록합니다. */
        notificationService.createForAdmins(
                "BUSINESS_REQUEST",
                "사업자 승인 요청",
                businessVO.getBusinessName()
                        + "의 사업자 가입 승인 요청이 접수되었습니다.",
                "/admin/business/list?tab=approval",
                "BUSINESS",
                businessVO.getBusinessNo());
    }

    /* 일반회원 / 사업자회원 공통 검증 */
    private void validateCommonMember(MemberVO memberVO) {

        if (memberVO == null) {
            throw new IllegalArgumentException("회원 정보가 없습니다.");
        }

        if (memberVO.getMemberId() == null || memberVO.getMemberId().isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (memberVO.getMemberPw() == null || memberVO.getMemberPw().isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (memberVO.getEmail() == null || memberVO.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
    }

    /* 일반회원 전용 검증 */
    private void validateUserMember(MemberVO memberVO) {

        if (memberVO.getMemberName() == null || memberVO.getMemberName().isBlank()) {

            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (memberVO.getNickname() == null || memberVO.getNickname().isBlank()) {

            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }
    }

    /*
     * =========================================================
     * 일반회원 OTT 선택 검증
     *
     * 실제 OTT를 1개 이상 선택하거나 OTT 없음을 선택해야 한다.
     * =========================================================
     */
    private void validateOttSelection(List<String> ottList, String noOtt) {

        boolean hasOtt = ottList != null && !ottList.isEmpty();
        boolean selectedNoOtt = "Y".equalsIgnoreCase(noOtt);

        if (!hasOtt && !selectedNoOtt) {
            throw new IllegalArgumentException(
                    "사용 중인 OTT를 선택하거나 OTT 없음을 선택해주세요.");
        }

        if (hasOtt && selectedNoOtt) {
            throw new IllegalArgumentException(
                    "OTT 없음과 다른 OTT는 동시에 선택할 수 없습니다.");
        }
    }

    @Override
    public boolean isDuplicateId(String memberId) {
        return memberDAO.countByMemberId(memberId) > 0;
    }

    @Override
    public boolean isDuplicateEmail(String email) {
        return memberDAO.countByEmail(email) > 0;
    }

    @Override
    public boolean isDuplicateNickname(String nickname) {
        return memberDAO.countByNickname(nickname) > 0;
    }

   @Override
    public MemberVO loginMember(MemberVO memberVO) {

        MemberVO loginMember = memberDAO.loginMember(memberVO);

        if (loginMember == null
                || !passwordEncoder.matches(memberVO.getMemberPw(), loginMember.getMemberPw())) {
            return null;
        }

        if ("BLOCKED".equals(loginMember.getStatus())) {
            throw new MemberBlockedException("정지된 계정입니다. 고객센터로 문의해주세요.");
        }

        if ("WITHDRAWN".equals(loginMember.getStatus())) {
            throw new MemberWithdrawnException(
                    "탈퇴한 계정입니다.",
                    loginMember.getMemberNo(),
                    loginMember.getWithdrawnAt());
        }

        loginMember.setMemberPw(null);

        return loginMember;
    }

    @Override
    @Transactional
    public void updateMember(MemberVO memberVO) {

        if (memberVO == null || memberVO.getMemberNo() == null) {
            throw new IllegalArgumentException("회원 정보가 올바르지 않습니다.");
        }

        String nickname = memberVO.getNickname();

        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        nickname = nickname.trim();

        if (!nickname.matches("^[a-zA-Z0-9가-힣]{2,10}$")) {
            throw new IllegalArgumentException("닉네임은 한글, 영문, 숫자 2~10자로 입력해주세요.");
        }

        memberVO.setNickname(nickname);

        if (memberDAO.countByNicknameExceptMe(nickname, memberVO.getMemberNo()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        String email = memberVO.getEmail();

        if (email != null) {
            email = email.trim();

            if (email.isBlank()) {
                throw new IllegalArgumentException("이메일을 입력해주세요.");
            }

            if (!email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("올바른 이메일 형식으로 입력해주세요.");
            }

            memberVO.setEmail(email);

            if (memberDAO.countByEmailExceptMe(email, memberVO.getMemberNo()) > 0) {
                throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
            }
        }

        String newPassword = memberVO.getMemberPw();

        if (newPassword != null && !newPassword.isBlank()) {
            memberVO.setMemberPw(passwordEncoder.encode(newPassword));
        } else {
            memberVO.setMemberPw(null);
        }

        int updated = memberDAO.updateMember(memberVO);


        if (updated != 1) {
            throw new IllegalStateException("회원정보 수정에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public void updateMemberOtt(Long memberNo, List<String> ottList) {

        // 기존 OTT 삭제
        memberDAO.deleteMemberPlatform(memberNo);

        // OTT 없으면 종료
        if (ottList == null || ottList.isEmpty()) {
            return;
        }

        for (String platformCode : ottList) {

            if (platformCode == null || platformCode.isBlank()) {
                continue;
            }

            Long platformNo = memberDAO.selectPlatformNoByCode(platformCode);

            if (platformNo == null) {
                throw new IllegalArgumentException("존재하지 않는 OTT: " + platformCode);
            }

            memberDAO.insertMemberPlatform(memberNo, platformNo);
        }
    }

    @Override
    @Transactional
    public void withdrawMember(Long memberNo) {

        // 즉시 삭제하지 않고 상태만 WITHDRAWN으로 변경한다.
        // 실제 데이터 삭제는 MemberDeleteScheduler가 7일 경과 후 처리한다.
        memberDAO.withdrawMember(memberNo);
    }

    @Override
    @Transactional
    public void restoreMember(Long memberNo) {

        if (memberNo == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        int updated = memberDAO.restoreMember(memberNo);

        if (updated == 0) {
            throw new IllegalStateException("복구 가능한 기간이 지났거나 이미 처리된 계정입니다.");
        }
    }

    @Override
    public boolean checkPassword(Long memberNo, String password) {
        if (memberNo == null || password == null || password.isBlank()) {
            return false;
        }

    String encodedPassword = memberDAO.selectPasswordByMemberNo(memberNo);

    return encodedPassword != null
            && passwordEncoder.matches(password, encodedPassword);
}

    @Override
    public MemberVO getMemberByNo(Long memberNo) {
        return memberDAO.getMemberByNo(memberNo);
    }

    @Override
    public boolean checkUpdateNickname(Long memberNo, String nickname) {

        return memberDAO.countByNicknameExceptMe(
                nickname,
                memberNo) == 0;

    }

    @Override
    public boolean checkUpdateEmail(Long memberNo, String email) {

        return memberDAO.countByEmailExceptMe(
                email,
                memberNo) == 0;
    }

    @Override
    public MemberVO findId(MemberVO memberVO) {
        return memberDAO.findId(memberVO);
    }

    @Override
    public MemberVO findPw(MemberVO memberVO) {
        return memberDAO.findPw(memberVO);
    }

    @Override
    @Transactional
    public void updatePassword(Long memberNo, String password) {

        /* =========================================================
        * 비밀번호 변경 유효성 검사
        * 회원 번호와 새 비밀번호가 정상적으로 전달되었는지 확인한다.
        * =========================================================
        */
        if (memberNo == null) {
            throw new IllegalArgumentException("회원 정보가 올바르지 않습니다.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }

        String trimmedPassword = password.trim();

        /*
        * =========================================================
        * 새 비밀번호 형식 검사
        *
        * 회원가입과 동일하게 영문, 숫자, 특수문자를 포함한
        * 8~20자의 비밀번호만 허용한다.
        * =========================================================
        */
        if (!trimmedPassword.matches("^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$")) {

            throw new IllegalArgumentException("비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
        }

        /*
        * =========================================================
        * 비밀번호 BCrypt 암호화
        *
        * 입력받은 평문 비밀번호를 그대로 DB에 저장하지 않고,
        * 회원가입과 동일하게 PasswordEncoder로 암호화하여 저장한다.
        * =========================================================
        */
        String encodedPassword = passwordEncoder.encode(trimmedPassword);

        int updatedCount = memberDAO.updatePassword(memberNo, encodedPassword);

        /*
        * 회원 정보가 존재하지 않아 수정되지 않은 경우를 방지한다.
        */
        if (updatedCount != 1) {
            throw new IllegalStateException("비밀번호 변경에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public void updateSnsMemberEmail(Long memberNo, String email) {

        if (memberNo == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }

        String trimmedEmail = email.trim();

        if (!trimmedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("올바른 이메일 형식으로 입력해주세요.");
        }

        if (memberDAO.countByEmailExceptMe(trimmedEmail, memberNo) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        int updated = memberDAO.updateMemberEmail(memberNo, trimmedEmail);

        if (updated != 1) {
            throw new IllegalStateException("이메일 등록에 실패했습니다.");
        }
    }
}
