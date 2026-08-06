package com.project.oditji.common.util;

import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpSession;

/**
 * 컨트롤러에서 세션의 로그인 회원을 동일한 기준으로 조회합니다.
 */
public final class LoginMemberUtil {

    private LoginMemberUtil() {
        // 인스턴스 생성 방지
    }

    public static MemberVO getLoginMember(HttpSession session) {
        Object sessionMember = session.getAttribute("loginMember");

        if (!(sessionMember instanceof MemberVO loginMember)
                || loginMember.getMemberNo() == null
                || loginMember.getMemberNo() <= 0) {
            return null;
        }

        return loginMember;
    }

    public static Long getLoginMemberNo(HttpSession session) {
        MemberVO loginMember = getLoginMember(session);
        return loginMember == null ? null : loginMember.getMemberNo();
    }
}
