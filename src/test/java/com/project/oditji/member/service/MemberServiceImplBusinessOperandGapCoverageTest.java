package com.project.oditji.member.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.member.dao.MemberDAO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.notification.service.NotificationService;

/** 사업자 가입 검증의 null/blank short-circuit 잔여 조건을 보완합니다. */
class MemberServiceImplBusinessOperandGapCoverageTest {

    private MemberServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MemberServiceImpl(
                mock(MemberDAO.class),
                mock(BusinessDAO.class),
                mock(PasswordEncoder.class),
                mock(NotificationService.class));
    }

    @Test
    void businessValidationShouldCoverOppositeNullAndBlankOperands() {
        BusinessVO nullName = validBusiness();
        nullName.setBusinessName(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(validMember(), nullName));

        BusinessVO blankNumber = validBusiness();
        blankNumber.setBusinessNumber("   ");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(validMember(), blankNumber));

        BusinessVO nullRepresentative = validBusiness();
        nullRepresentative.setRepresentativeName(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(validMember(), nullRepresentative));

        BusinessVO nullOpenDate = validBusiness();
        nullOpenDate.setOpenDate(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(validMember(), nullOpenDate));
    }

    private MemberVO validMember() {
        MemberVO member = new MemberVO();
        member.setMemberId("business-member");
        member.setMemberPw("Password1!");
        member.setEmail("business@test.com");
        return member;
    }

    private BusinessVO validBusiness() {
        BusinessVO business = new BusinessVO();
        business.setBusinessName("테스트상점");
        business.setBusinessNumber("123-45-67890");
        business.setRepresentativeName("대표자");
        business.setOpenDate("20260101");
        return business;
    }
}
