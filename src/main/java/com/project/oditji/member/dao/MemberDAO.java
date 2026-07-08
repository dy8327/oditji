package com.project.oditji.member.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.project.oditji.member.vo.MemberVO;

@Mapper
public interface MemberDAO {

    int insertMember(MemberVO memberVO);

    int countByMemberId(@Param("memberId") String memberId);

    int countByEmail(@Param("email") String email);

    int countByNickname(@Param("nickname") String nickname);

    Long selectPlatformNoByCode(@Param("platformCode") String platformCode);

    int insertMemberPlatform(@Param("memberNo") Long memberNo,
                             @Param("platformNo") Long platformNo);

    MemberVO loginMember(MemberVO memberVO);

    int insertKakaoMember(MemberVO memberVO);

    int updateMember(MemberVO memberVO);

    int deleteMember(@Param("memberNo") Long memberNo);

    int deleteMemberPlatform(@Param("memberNo") Long memberNo);

    int checkPassword(@Param("memberNo") Long memberNo,
                    @Param("password") String password);

    MemberVO getMemberByNo(@Param("memberNo") Long memberNo);

    int countByNicknameExceptMe(
            @Param("nickname") String nickname,
            @Param("memberNo") Long memberNo
    );
}