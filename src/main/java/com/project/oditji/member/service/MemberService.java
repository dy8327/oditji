package com.project.oditji.member.service;

import java.util.List;

import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.business.vo.BusinessVO;

public interface MemberService {

    void joinMember(MemberVO memberVO, List<String> ottList);

    void joinBusinessMember(MemberVO memberVO, BusinessVO businessVO);

    boolean isDuplicateId(String memberId);

    boolean isDuplicateEmail(String email);

    boolean isDuplicateNickname(String nickname);

    /**
     * 로그인 처리.
     *
     * ID/PW가 일치하지 않으면 null을 반환한다.
     * 정지(BLOCKED) 회원이면 MemberBlockedException,
     * 탈퇴(WITHDRAWN) 회원이면 MemberWithdrawnException을 던진다.
     */
    MemberVO loginMember(MemberVO memberVO);

    void updateMember(MemberVO memberVO);

    void updateMemberOtt(Long memberNo, List<String> ottList);

    /**
     * 마이페이지 자진 탈퇴 (소프트 삭제)
     * 즉시 삭제하지 않고 STATUS를 WITHDRAWN으로 변경하며,
     * 7일 후 스케줄러에 의해 실제 데이터가 삭제된다.
     */
    void withdrawMember(Long memberNo);

    /**
     * 탈퇴 회원 복구.
     *
     * 이미 본인 인증(비밀번호 매칭 또는 SNS OAuth 인증)이 끝난 상태에서만
     * 호출되는 것을 전제로 하며, 별도의 비밀번호 재확인은 하지 않는다.
     * 탈퇴 후 7일이 지났거나 이미 처리된 경우 IllegalStateException을 던진다.
     */
    void restoreMember(Long memberNo);

    boolean checkPassword(Long memberNo, String password);

    MemberVO getMemberByNo(Long memberNo);

    boolean checkUpdateNickname(Long memberNo, String nickname);

    MemberVO findId(MemberVO memberVO);

    MemberVO findPw(MemberVO memberVO);
}
