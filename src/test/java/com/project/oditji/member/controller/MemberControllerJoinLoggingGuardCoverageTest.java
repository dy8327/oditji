package com.project.oditji.member.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.project.oditji.business.service.BusinessService;
import com.project.oditji.business.service.NtsBusinessService;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.mail.service.MailService;
import com.project.oditji.member.service.MemberPlatformService;
import com.project.oditji.member.service.MemberService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.order.service.OrderService;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.wish.service.WishService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/** 회원가입 예외 처리의 WARN/ERROR 로그 비활성 분기를 보완합니다. */
class MemberControllerJoinLoggingGuardCoverageTest {

    private MemberService memberService;
    private MemberController controller;

    @BeforeEach
    void setUp() {
        memberService = mock(MemberService.class);
        controller = new MemberController(
                memberService,
                mock(MemberPlatformService.class),
                mock(BusinessService.class),
                mock(NtsBusinessService.class),
                mock(MailService.class),
                mock(FavoriteService.class),
                mock(WishService.class),
                mock(OrderService.class),
                mock(ReviewService.class),
                "build/test-profile",
                "build/test-license");
    }

    @Test
    void validationFailureShouldCoverDisabledWarnLogGuard() {
        doThrow(new IllegalArgumentException("invalid"))
                .when(memberService)
                .joinMember(any(MemberVO.class), anyList(), anyString());

        String view = withLoggingDisabled(() -> controller.join(
                new MemberVO(),
                new BusinessVO(),
                "USER",
                java.util.List.of("netflix"),
                "N",
                null,
                null,
                new ExtendedModelMap(),
                new RedirectAttributesModelMap()));

        assertEquals("member/join", view);
    }

    @Test
    void unexpectedFailureShouldCoverDisabledErrorLogGuard() {
        doThrow(new RuntimeException("boom"))
                .when(memberService)
                .joinMember(any(MemberVO.class), anyList(), anyString());

        String view = withLoggingDisabled(() -> controller.join(
                new MemberVO(),
                new BusinessVO(),
                "USER",
                java.util.List.of("netflix"),
                "N",
                null,
                null,
                new ExtendedModelMap(),
                new RedirectAttributesModelMap()));

        assertEquals("member/join", view);
    }

    private String withLoggingDisabled(java.util.function.Supplier<String> action) {
        Logger logger = (Logger) LoggerFactory.getLogger(MemberController.class);
        Level originalLevel = logger.getLevel();

        try {
            logger.setLevel(Level.OFF);
            return action.get();
        } finally {
            logger.setLevel(originalLevel);
        }
    }
}
