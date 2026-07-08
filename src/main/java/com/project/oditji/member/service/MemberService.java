package com.project.oditji.member.service;

import java.util.List;

import com.project.oditji.member.vo.MemberVO;

public interface MemberService {

    void joinMember(MemberVO memberVO, List<String> ottList);

    boolean isDuplicateId(String memberId);

    boolean isDuplicateEmail(String email);

    boolean isDuplicateNickname(String nickname);

    MemberVO loginMember(MemberVO memberVO);
}