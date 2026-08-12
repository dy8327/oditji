package com.project.oditji.common.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.member.vo.MemberVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class BusinessCheckInterceptor implements HandlerInterceptor {

    private static final long ADMIN_MEMBER_NO = 1L;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_BUSINESS = "BUSINESS";
    private static final String STATUS_APPROVED = "APPROVED";

    private final BusinessService businessService;

    public BusinessCheckInterceptor(BusinessService businessService) {
        this.businessService = businessService;
    }

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

        /* 사업자 역할이 아닌 회원은 사업자 영역에 접근할 수 없습니다. */
        if (!ROLE_BUSINESS.equals(loginMember.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        /*
         * [미승인 사업자 제한 적용]
         * 세션의 businessStatus는 관리자 승인/반려 직후 오래된 값일 수 있으므로
         * 매 요청마다 DB의 현재 BUSINESS 상태를 조회해 권한을 판단합니다.
         */
        BusinessVO business = businessService.getBusinessByMemberNo(loginMember.getMemberNo());
        if (business == null || business.getBusinessNo() == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        refreshBusinessSession(session, business);

        /*
         * WAITING/REJECTED 사업자도 사업자 마이페이지 홈에서는
         * 현재 승인 상태와 반려 사유를 확인할 수 있습니다.
         */
        if (isBusinessMainRequest(request)) {
            return true;
        }

        /* 그 외 상품/이벤트/주문/정산/채팅 기능은 승인된 사업자만 허용합니다. */
        if (!STATUS_APPROVED.equals(business.getStatus())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    private void refreshBusinessSession(HttpSession session, BusinessVO business) {
        session.setAttribute("businessNo", business.getBusinessNo());
        session.setAttribute("businessName", business.getBusinessName());
        session.setAttribute("businessStatus", business.getStatus());
    }

    /**
     * 현재 요청이 사업자 마이페이지 홈인지 확인합니다.
     */
    private boolean isBusinessMainRequest(HttpServletRequest request) {
        return "/business/main".equals(getRequestPath(request));
    }

    /**
     * 현재 요청이 채팅 기능 요청인지 확인합니다.
     */
    private boolean isChatRequest(HttpServletRequest request) {
        String path = getRequestPath(request);

        return "/chat".equals(path) || path.startsWith("/chat/");
    }

    private String getRequestPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();

        return requestUri.substring(contextPath.length());
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
