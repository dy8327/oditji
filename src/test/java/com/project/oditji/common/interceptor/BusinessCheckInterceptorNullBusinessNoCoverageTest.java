package com.project.oditji.common.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.member.vo.MemberVO;

/** 사업자 조회 결과는 존재하지만 BUSINESS_NO가 없는 방어 분기를 검증합니다. */
class BusinessCheckInterceptorNullBusinessNoCoverageTest {

    @Test
    void businessRecordWithoutBusinessNoShouldBeForbidden() throws Exception {
        BusinessService businessService = mock(BusinessService.class);
        BusinessCheckInterceptor interceptor =
                new BusinessCheckInterceptor(businessService);

        MemberVO loginMember = new MemberVO();
        loginMember.setMemberNo(31L);
        loginMember.setRole("BUSINESS");

        BusinessVO business = new BusinessVO();
        business.setBusinessNo(null);
        business.setBusinessName("번호없는사업자");
        business.setStatus("APPROVED");

        when(businessService.getBusinessByMemberNo(31L))
                .thenReturn(business);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/oditji");
        request.setRequestURI("/oditji/business/main");
        request.getSession().setAttribute("loginMember", loginMember);

        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }
}
