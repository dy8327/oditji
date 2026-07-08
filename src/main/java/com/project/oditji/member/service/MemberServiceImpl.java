package com.project.oditji.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.oditji.member.dao.MemberDAO;
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
        return memberDAO.loginMember(memberVO);
    }
}