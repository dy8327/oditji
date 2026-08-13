package com.project.oditji.content.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.view.RedirectView;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.holiday.service.HolidayService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.verify.service.VerifyService;

/** 콘텐츠 상세/OTT 이동의 null 입력과 성인인증 false 분기를 보완합니다. */
class ContentControllerResidualInputCoverageTest {

    private ContentService contentService;
    private VerifyService verifyService;
    private GoodsService goodsService;
    private ContentController controller;

    @BeforeEach
    void setUp() {
        contentService = mock(ContentService.class);
        verifyService = mock(VerifyService.class);
        goodsService = mock(GoodsService.class);

        controller = new ContentController(
                contentService,
                mock(ReviewService.class),
                mock(FavoriteService.class),
                mock(TmdbDAO.class),
                verifyService,
                goodsService,
                mock(HolidayService.class));
    }

    @Test
    void ottSearchShouldNormalizeNullPlatformAndNullTitle() {
        RedirectView bothNull = controller.redirectOttSearch(null, null);
        RedirectView nullTitle = controller.redirectOttSearch("Netflix", null);

        assertTrue(bothNull.getUrl().startsWith("https://www.google.com/search?q="));
        assertEquals("https://www.netflix.com/search?q=", nullTitle.getUrl());
    }

    @Test
    void adultContentShouldRedirectLoggedInMemberWhenVerificationReturnsFalse() {
        ContentVO content = new ContentVO();
        content.setContentNo(31);
        content.setAgeRating("청소년 관람불가");
        when(contentService.getContentDetail(31)).thenReturn(content);
        when(verifyService.isAdultVerified(9L)).thenReturn(false);

        MemberVO member = new MemberVO();
        member.setMemberNo(9L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);

        String view = controller.detail(
                31,
                session,
                new ExtendedModelMap());

        assertTrue(view.startsWith("redirect:/verify/adult?returnUrl="));
        verify(verifyService).isAdultVerified(9L);
        verify(contentService, never()).recordContentViewHistory(9L, 31);
        verify(goodsService, never()).getGoodsByContentNo(31, 8);
    }
}
