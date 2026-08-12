package com.project.oditji.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.vo.EventFormVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.refund.service.OrderCancelRefundService;

import jakarta.servlet.http.HttpSession;

/** 사업자 컨트롤러 private helper의 단축평가와 경계값 조건을 보완합니다. */
class BusinessControllerHelperConditionClosureTest {

    private BusinessController controller;

    @BeforeEach
    void setUp() {
        controller = new BusinessController(
                mock(BusinessService.class),
                mock(OrderCancelRefundService.class));
    }

    @Test
    void discountValidationShouldCoverNullNegativeOverflowAndValidRates() {
        assertFalse(invalidDiscount(null));
        assertTrue(invalidDiscount(java.util.Arrays.asList((Integer) null)));
        assertTrue(invalidDiscount(List.of(-1)));
        assertTrue(invalidDiscount(List.of(101)));
        assertFalse(invalidDiscount(List.of(0, 50, 100)));
    }

    @Test
    void convertToLongShouldCoverNumberStringAndUnsupportedValues() {
        assertNull(convert(null));
        assertNull(convert(0));
        assertEquals(1L, convert(1));
        assertNull(convert("0"));
        assertEquals(2L, convert(" 2 "));
        assertNull(convert("not-a-number"));
        assertNull(convert(new Object()));
    }

    @Test
    void loginMemberResolutionShouldCoverFallbackKeysAndMemberObjectBoundary() {
        HttpSession standardSession = mock(HttpSession.class);
        when(standardSession.getAttribute("loginMemberNo")).thenReturn(10L);
        assertEquals(10L, loginMemberNo(standardSession));

        HttpSession legacySession = mock(HttpSession.class);
        when(legacySession.getAttribute("loginMemberNo")).thenReturn(0L);
        when(legacySession.getAttribute("memberNo")).thenReturn("20");
        assertEquals(20L, loginMemberNo(legacySession));
        verify(legacySession).setAttribute("loginMemberNo", 20L);

        MemberVO positiveMember = new MemberVO();
        positiveMember.setMemberNo(30L);
        HttpSession memberObjectSession = mock(HttpSession.class);
        when(memberObjectSession.getAttribute("loginMember"))
                .thenReturn(positiveMember);
        assertEquals(30L, loginMemberNo(memberObjectSession));
        verify(memberObjectSession).setAttribute("loginMemberNo", 30L);

        MemberVO zeroMember = new MemberVO();
        zeroMember.setMemberNo(0L);
        HttpSession zeroMemberSession = mock(HttpSession.class);
        when(zeroMemberSession.getAttribute("loginMember"))
                .thenReturn(zeroMember);
        assertNull(loginMemberNo(zeroMemberSession));

        HttpSession wrongObjectSession = mock(HttpSession.class);
        when(wrongObjectSession.getAttribute("loginMember"))
                .thenReturn("not-member");
        assertNull(loginMemberNo(wrongObjectSession));
    }

    @Test
    void eventFactoryShouldCoverNullAndPresentEventNumber() {
        EventFormVO form = new EventFormVO();
        form.setEventTitle("이벤트");
        form.setDiscountRateList(List.of(10));

        EventManageVO created = ReflectionTestUtils.invokeMethod(
                controller,
                "createEventManageVO",
                null,
                10L,
                form);
        EventManageVO updated = ReflectionTestUtils.invokeMethod(
                controller,
                "createEventManageVO",
                99L,
                10L,
                form);

        assertNull(created.getEventNo());
        assertEquals(99L, updated.getEventNo());
    }

    private boolean invalidDiscount(List<Integer> rates) {
        Boolean result = ReflectionTestUtils.invokeMethod(
                controller,
                "hasInvalidDiscountRate",
                rates);
        return Boolean.TRUE.equals(result);
    }

    private Long convert(Object value) {
        return ReflectionTestUtils.invokeMethod(
                controller,
                "convertToLong",
                value);
    }

    private Long loginMemberNo(HttpSession session) {
        return ReflectionTestUtils.invokeMethod(
                controller,
                "getLoginMemberNo",
                session);
    }
}
