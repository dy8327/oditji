package com.project.oditji.member.service;

import java.util.List;

import com.project.oditji.member.vo.MemberVO;

public interface MemberService {

    void joinMember(MemberVO memberVO, List<String> ottList);

    boolean isDuplicateId(String memberId);

    boolean isDuplicateEmail(String email);

    boolean isDuplicateNickname(String nickname);

    MemberVO loginMember(MemberVO memberVO);

    void updateMember(MemberVO memberVO);

    void updateMemberOtt(Long memberNo, List<String> ottList);

    void deleteMember(Long memberNo);

    boolean checkPassword(Long memberNo, String password);

    MemberVO getMemberByNo(Long memberNo);

    boolean checkUpdateNickname(Long memberNo, String nickname);
}