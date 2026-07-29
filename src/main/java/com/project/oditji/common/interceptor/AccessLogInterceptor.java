package com.project.oditji.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.common.dao.AccessLogDAO;
import com.project.oditji.common.vo.AccessLogVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 요청마다 ACCESS_LOG에 접속 기록을 남긴다.
 *
 * - 로그인 여부와 무관하게 기록한다. 비로그인 요청은 MEMBER_NO를 NULL로 저장한다
 *   (ACCESS_LOG.MEMBER_NO는 NULL 허용 컬럼).
 * - 정적 리소스/에러 페이지는 WebConfig의 excludePathPatterns에서 제외한다.
 * - 로그 적재 중 예외가 나더라도 실제 화면 요청 처리를 막으면 안 되므로
 *   항상 true를 반환한다.
 */
@Component
public class AccessLogInterceptor implements HandlerInterceptor {

    private final AccessLogDAO accessLogDAO;
    private static final Logger log = LoggerFactory.getLogger(AccessLogInterceptor.class);

    public AccessLogInterceptor(AccessLogDAO accessLogDAO) {
        this.accessLogDAO = accessLogDAO;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        try {

            HttpSession session = request.getSession(false);

            MemberVO loginMember =
                    session == null
                            ? null
                            : (MemberVO) session.getAttribute("loginMember");

            AccessLogVO accessLog = new AccessLogVO();

            accessLog.setMemberNo(
                    loginMember == null ? null : loginMember.getMemberNo());
            accessLog.setAccessIp(resolveClientIp(request));
            accessLog.setUserAgent(request.getHeader("User-Agent"));
            accessLog.setAccessUrl(request.getRequestURI());

            accessLogDAO.insertAccessLog(accessLog);

        } catch (Exception e) {
            // 접속 로그 적재 실패가 실제 요청 흐름을 막지 않도록 로그만 남긴다.
            if (log.isWarnEnabled()) {
                log.warn("접속 로그 저장 실패 - {} {}", request.getMethod(), request.getRequestURI(), e);
            }
        }

        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
