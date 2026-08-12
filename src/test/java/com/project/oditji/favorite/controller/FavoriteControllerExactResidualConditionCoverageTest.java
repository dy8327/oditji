package com.project.oditji.favorite.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

/** 찜 컨트롤러의 아직 평가되지 않은 null 피연산자를 직접 보완합니다. */
class FavoriteControllerExactResidualConditionCoverageTest {

    private FavoriteService favoriteService;
    private ContentService contentService;
    private VerifyService verifyService;
    private FavoriteController controller;

    @BeforeEach
    void setUp() {
        favoriteService = mock(FavoriteService.class);
        contentService = mock(ContentService.class);
        verifyService = mock(VerifyService.class);
        controller = new FavoriteController(
                favoriteService,
                contentService,
                mock(WishService.class),
                verifyService);
    }

    @Test
    void tmdbToggleShouldRejectNullTmdbIdAfterLogin() {
        HttpSession session = mock(HttpSession.class);
        MemberVO member = new MemberVO();
        member.setMemberNo(10L);
        when(session.getAttribute("loginMember")).thenReturn(member);

        FavoriteVO request = new FavoriteVO();
        request.setTmdbId(null);
        request.setContentType("MOVIE");

        ResponseEntity<?> response = controller.toggleFavoriteByTmdb(request, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(contentService);
    }

    @Test
    void adultAccessValidationShouldReturnImmediatelyForNullFavoriteRequest() {
        MemberVO member = new MemberVO();
        member.setMemberNo(11L);

        ReflectionTestUtils.invokeMethod(
                controller,
                "validateAdultFavoriteAccess",
                null,
                member);

        verifyNoInteractions(favoriteService, contentService, verifyService);
    }

    @Test
    void loginMemberHelperShouldRejectNonMemberSessionObject() {
        HttpSession session = mock(HttpSession.class);
        when(session.getAttribute("loginMember")).thenReturn("not-a-member");

        MemberVO result = ReflectionTestUtils.invokeMethod(
                controller,
                "getLoginMember",
                session);

        assertNull(result);
    }
}
