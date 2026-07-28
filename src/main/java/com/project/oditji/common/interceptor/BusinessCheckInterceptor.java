package com.project.oditji.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class BusinessCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);

        // 비로그인 접근 차단
        if (session == null || session.getAttribute("loginMember") == null) {
            response.sendRedirect(request.getContextPath() + "/member/login");
            return false;
        }

        Object businessNo = session.getAttribute("businessNo");
        String businessStatus = (String) session.getAttribute("businessStatus");

        // 승인된 사업자 외 접근 차단
        if (businessNo == null || !"APPROVED".equals(businessStatus)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }
}