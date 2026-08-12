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
        MemberVO nullNameMember = validMember();
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(nullNameMember, nullName));

        BusinessVO blankNumber = validBusiness();
        blankNumber.setBusinessNumber("   ");
        MemberVO blankNumberMember = validMember();
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(blankNumberMember, blankNumber));

        BusinessVO nullRepresentative = validBusiness();
        nullRepresentative.setRepresentativeName(null);
        MemberVO nullRepresentativeMember = validMember();
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(nullRepresentativeMember, nullRepresentative));

        BusinessVO nullOpenDate = validBusiness();
        nullOpenDate.setOpenDate(null);
        MemberVO nullOpenDateMember = validMember();
        assertThrows(
                IllegalArgumentException.class,
                () -> service.joinBusinessMember(nullOpenDateMember, nullOpenDate));
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
