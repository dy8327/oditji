package com.project.oditji.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class BusinessCheckInterceptor implements HandlerInterceptor {

    private static final long ADMIN_MEMBER_NO = 1L;
    private static final String ROLE_ADMIN = "ADMIN";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        MemberVO loginMember = session == null
                ? null
                : (MemberVO) session.getAttribute("loginMember");

        /* 비로그인 접근 차단 */
        if (loginMember == null) {
            response.sendRedirect(request.getContextPath() + "/member/login");
            return false;
        }

        /*
         * /chat 및 /chat/** 요청은 승인된 사업자뿐 아니라 관리자도 허용합니다.
         * 관리자가 자유방까지 접근하지 못하도록 하는 세부 권한은
         * ChatController와 ChatApiController에서 방 유형별로 다시 검사합니다.
         */
        if (isChatRequest(request) && isAdmin(loginMember)) {
            return true;
        }

        Object businessNo = session.getAttribute("businessNo");
        String businessStatus = (String) session.getAttribute("businessStatus");

       /* 승인된 사업자 외 접근 차단 */
        if (businessNo == null || !"APPROVED".equals(businessStatus)) {
            // 필터·인터셉터 단계의 오류도 동일한 안내 화면을 사용하도록 /error로 위임합니다.
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    /**
     * 현재 요청이 채팅 기능 요청인지 확인합니다.
     */
    private boolean isChatRequest(HttpServletRequest request) {

        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        return "/chat".equals(path) || path.startsWith("/chat/");
    }

    /**
     * 관리자 고정 계정 여부를 확인합니다.
     */
    private boolean isAdmin(MemberVO loginMember) {

        Long memberNo = loginMember.getMemberNo();

        return memberNo != null
                && memberNo == ADMIN_MEMBER_NO
                && ROLE_ADMIN.equals(loginMember.getRole());
    }
}
