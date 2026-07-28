package com.project.oditji.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        MemberVO loginMember = session == null ? null : (MemberVO) session.getAttribute("loginMember");

        // 비로그인 접근 차단
        if (loginMember == null) {
            response.sendRedirect(request.getContextPath() + "/member/login");
            return false;
        }

        // 관리자 외 접근 차단
        if (!"ADMIN".equals(loginMember.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}