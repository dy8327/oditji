package com.project.oditji.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final String LOGIN_REDIRECT_SESSION_KEY = "redirectAfterLogin";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        MemberVO loginMember = session == null ? null : getLoginMember(session);

        if (loginMember != null) {
            return true;
        }

        // GET 요청은 로그인 후 돌아갈 경로를 저장한다.
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            HttpSession redirectSession = request.getSession();
            redirectSession.setAttribute(LOGIN_REDIRECT_SESSION_KEY, getRequestPath(request));
        }

        response.sendRedirect(request.getContextPath() + "/member/login");
        return false;
    }

    private MemberVO getLoginMember(HttpSession session) {
        Object sessionMember = session.getAttribute("loginMember");

        if (!(sessionMember instanceof MemberVO loginMember)) {
            return null;
        }

        return loginMember.getMemberNo() != null && loginMember.getMemberNo() > 0L
                ? loginMember
                : null;
    }

    private String getRequestPath(HttpServletRequest request) {
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        String queryString = request.getQueryString();

        return queryString == null || queryString.isBlank()
                ? requestPath
                : requestPath + "?" + queryString;
    }
}