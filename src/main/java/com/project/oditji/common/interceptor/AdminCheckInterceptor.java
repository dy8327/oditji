package com.project.oditji.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // 1. 로그인하지 않은 경우
        if (session == null
                || session.getAttribute("loginMember") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/member/login"
            );

            return false;
        }

        // 2. 로그인은 했지만 관리자가 아닌 경우
        String role =
                (String) session.getAttribute("role");

        if (!"ADMIN".equals(role)) {

            response.sendRedirect(
                    request.getContextPath() + "/"
            );

            return false;
        }

        // 3. 관리자만 통과
        return true;
    }
}