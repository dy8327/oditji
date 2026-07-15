package com.project.oditji.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.exception.MemberBlockedException;
import com.project.oditji.member.exception.MemberWithdrawnException;
import com.project.oditji.member.vo.MemberVO;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberDAO memberDAO;

    public MemberServiceImpl(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }

    @Override
    @Transactional
    public void joinMember(MemberVO memberVO, List<String> ottList) {

        validateMember(memberVO);
        validateOttList(ottList);

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

        memberDAO.insertMember(memberVO);

        Long memberNo = memberVO.getMemberNo();

        System.out.println("생성된 MEMBER_NO = " + memberNo);

        if (memberNo == null) {
            throw new IllegalStateException("회원 번호 생성에 실패했습니다.");
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

    private void validateMember(MemberVO memberVO) {
        if (memberVO.getMemberName() == null || memberVO.getMemberName().isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        if (memberVO.getMemberId() == null || memberVO.getMemberId().isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }

        if (memberVO.getMemberPw() == null || memberVO.getMemberPw().isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        if (memberVO.getNickname() == null || memberVO.getNickname().isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        if (memberVO.getEmail() == null || memberVO.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일을 입력해주세요.");
        }
    }

    private void validateOttList(List<String> ottList) {
        if (ottList == null || ottList.isEmpty()) {
            throw new IllegalArgumentException("사용 중인 OTT를 1개 이상 선택해주세요.");
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

        if (loginMember == null) {
            return null;
        }

        /*
         * 이 시점에는 이미 MEMBER_ID + MEMBER_PW가 일치하는 회원이 조회된 상태이므로,
         * 정지/탈퇴 여부만 STATUS로 분기한다.
         */
        if ("BLOCKED".equals(loginMember.getStatus())) {
            throw new MemberBlockedException("정지된 계정입니다. 고객센터로 문의해주세요.");
        }

        if ("WITHDRAWN".equals(loginMember.getStatus())) {
            throw new MemberWithdrawnException(
                    "탈퇴한 계정입니다.",
                    loginMember.getMemberNo(),
                    loginMember.getWithdrawnAt());
        }

        return loginMember;
    }

    @Override
    @Transactional
    public void updateMember(MemberVO memberVO) {
        memberDAO.updateMember(memberVO);
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
        return memberDAO.checkPassword(memberNo, password) > 0;
    }

    @Override
    public MemberVO getMemberByNo(Long memberNo) {
        return memberDAO.getMemberByNo(memberNo);
    }

    @Override
    public boolean checkUpdateNickname(Long memberNo, String nickname) {

        return memberDAO.countByNicknameExceptMe(
                nickname,
                memberNo
        ) == 0;

    }

    @Override
    public MemberVO findId(MemberVO memberVO) {
        return memberDAO.findId(memberVO);
    }

    @Override
    public MemberVO findPw(MemberVO memberVO) {
        return memberDAO.findPw(memberVO);
    }
}
