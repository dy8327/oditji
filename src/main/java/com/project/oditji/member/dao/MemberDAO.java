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

    int deleteMemberPlatform(@Param("memberNo") Long memberNo);

    /**
     * 마이페이지 자진 탈퇴 처리 (소프트 삭제)
     * STATUS = 'WITHDRAWN', WITHDRAWN_AT = SYSDATE 로 갱신한다.
     * 실제 하드 삭제는 관리자 즉시 탈퇴처리 또는 7일 후 자동 삭제 스케줄러에서 처리한다.
     */
    int withdrawMember(@Param("memberNo") Long memberNo);

    /**
     * 탈퇴 회원 복구 처리.
     * STATUS = 'WITHDRAWN' 이고, 탈퇴 후 7일 이내인 경우에만 ACTIVE로 되돌린다.
     * (스케줄러의 하드 삭제와의 레이스 컨디션을 막기 위한 방어적 조건)
     *
     * @return 실제로 갱신된 row 수. 0이면 복구 불가 상태(기간 만료/이미 처리됨)를 의미한다.
     */
    int restoreMember(@Param("memberNo") Long memberNo);

    String selectPasswordByMemberNo(@Param("memberNo") Long memberNo);

    MemberVO getMemberByNo(@Param("memberNo") Long memberNo);

    int countByNicknameExceptMe(
            @Param("nickname") String nickname,
            @Param("memberNo") Long memberNo);

    int countByEmailExceptMe(
            @Param("email") String email,
            @Param("memberNo") Long memberNo);

    MemberVO findId(MemberVO memberVO);

    MemberVO findPw(MemberVO memberVO);

    /**
     * SNS(카카오) 자동가입 회원의 이메일을 등록/수정한다.
     * 카카오 가입 시 EMAIL이 NULL로 저장되므로,
     * OTT 선택 화면에서 이메일을 입력받아 이 메서드로 채워 넣는다.
     */
    int updateMemberEmail(@Param("memberNo") Long memberNo,
            @Param("email") String email);
}
