package com.project.oditji.content.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.view.RedirectView;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.goods.service.GoodsService;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.review.service.ReviewService;
import com.project.oditji.tmdb.dao.TmdbDAO;
import com.project.oditji.tmdb.vo.OttPlatformVO;
import com.project.oditji.verify.service.VerifyService;

/** 콘텐츠 목록, 상세 접근 제한, OTT 이동 및 인물 페이지 분기를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class ContentControllerCoverageTest {

    @Mock
    private ContentService contentService;
    @Mock
    private ReviewService reviewService;
    @Mock
    private FavoriteService favoriteService;
    @Mock
    private TmdbDAO tmdbDAO;
    @Mock
    private VerifyService verifyService;
    @Mock
    private GoodsService goodsService;

    private ContentController controller;

    @BeforeEach
    void setUp() {
        controller = new ContentController(
                contentService,
                reviewService,
                favoriteService,
                tmdbDAO,
                verifyService,
                goodsService);
    }

    @Test
    void prepareShouldRedirectToStoredContentNumber() {
        when(contentService.prepareContentDetail(100L, "MOVIE")).thenReturn(77);

        String view = controller.prepareDetail(100L, "MOVIE");

        assertEquals("redirect:/content/contentDetail/77", view);
    }

    @Test
    void listShouldNormalizeInvalidInputsAndBuildLogoMap() {
        ContentListPageVO pageVO = page(1, 3, 21);
        when(contentService.getContentListByType(
                eq("all"), eq("popular"), eq(1),
                anyList(), anyList(), anyList(), anyList()))
                .thenReturn(pageVO);
        when(contentService.getContentRecommendedList(
                anyList(), anyList(), anyList(), anyList()))
                .thenReturn(List.of());

        OttPlatformVO netflix = platform("Netflix", "/netflix.png");
        OttPlatformVO blankLogo = platform("TVING", " ");
        when(tmdbDAO.selectActivePlatformList())
                .thenReturn(List.of(netflix, blankLogo));

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.list(
                "invalid",
                "invalid",
                0,
                null,
                null,
                null,
                null,
                model);

        assertEquals("content/contentList", view);
        assertEquals("all", model.get("type"));
        assertEquals("popular", model.get("sort"));
        assertEquals("영화·시리즈", model.get("pageTitle"));
        assertEquals(1, model.get("page"));
        assertEquals(3, model.get("totalPage"));
        assertEquals(21, model.get("totalCount"));
        assertTrue(((List<?>) model.get("genreCodes")).isEmpty());

        @SuppressWarnings("unchecked")
        Map<String, String> logoMap = (Map<String, String>) model.get("ottLogoMap");
        assertEquals(Map.of("netflix", "/netflix.png"), logoMap);
    }

    @Test
    void listShouldUseLatestForNewTypeAndKeepFilters() {
        ContentListPageVO pageVO = page(2, 2, 12);
        List<String> categories = List.of("MOVIE");
        List<String> genres = List.of("28");
        List<String> providers = List.of("netflix");
        List<String> ratings = List.of("15세 이상 관람가");
        when(contentService.getContentListByType(
                "new", "latest", 2,
                categories, genres, providers, ratings))
                .thenReturn(pageVO);
        when(contentService.getContentRecommendedList(
                categories, genres, providers, ratings))
                .thenReturn(List.of());

        ExtendedModelMap model = new ExtendedModelMap();
        String view = controller.list(
                " NEW ",
                null,
                2,
                categories,
                genres,
                providers,
                ratings,
                model);

        assertEquals("content/contentList", view);
        assertEquals("new", model.get("type"));
        assertEquals("latest", model.get("sort"));
        assertEquals("신규 콘텐츠", model.get("pageTitle"));
    }

    @Test
    void detailShouldRejectMissingContent() {
        when(contentService.getContentDetail(10)).thenReturn(null);

        MockHttpSession session = new MockHttpSession();
        ExtendedModelMap model = new ExtendedModelMap();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.detail(10, session, model));

        assertTrue(exception.getMessage().contains("존재하지 않는"));
    }

    @Test
    void adultContentShouldRedirectGuestBeforeLoadingRelatedData() {
        ContentVO content = content(10, "청소년 관람불가");
        when(contentService.getContentDetail(10)).thenReturn(content);

        String view = controller.detail(
                10,
                new MockHttpSession(),
                new ExtendedModelMap());

        assertTrue(view.startsWith("redirect:/verify/adult?returnUrl="));
        assertTrue(view.contains("contentDetail"));
        verify(contentService, never()).recordContentViewHistory(10L, 10);
        verify(goodsService, never()).getGoodsByContentNo(10, 8);
    }

    @Test
    void normalContentShouldRenderForGuestWithoutHistoryOrFavorite() {
        ContentVO content = content(11, "15세 이상 관람가");
        when(contentService.getContentDetail(11)).thenReturn(content);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.detail(11, new MockHttpSession(), model);

        assertEquals("content/contentDetail", view);
        assertEquals(content, model.get("content"));
        assertEquals(Boolean.TRUE, model.get("loginRequired"));
        assertEquals(Boolean.FALSE, model.get("favoriteActive"));
        verify(contentService, never()).recordContentViewHistory(
                org.mockito.ArgumentMatchers.anyLong(), eq(11));
        verify(favoriteService, never()).isFavorite(
                org.mockito.ArgumentMatchers.any(FavoriteVO.class));
    }

    @Test
    void verifiedMemberShouldRecordHistoryAndResolveFavorite() {
        ContentVO content = content(12, "등급 정보 없음");
        when(contentService.getContentDetail(12)).thenReturn(content);
        when(verifyService.isAdultVerified(50L)).thenReturn(true);
        when(favoriteService.isFavorite(
                org.mockito.ArgumentMatchers.any(FavoriteVO.class)))
                .thenReturn(true);

        MemberVO member = new MemberVO();
        member.setMemberNo(50L);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loginMember", member);
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.detail(12, session, model);

        assertEquals("content/contentDetail", view);
        assertEquals(Boolean.FALSE, model.get("loginRequired"));
        assertEquals(Boolean.TRUE, model.get("favoriteActive"));
        verify(contentService).recordContentViewHistory(50L, 12);
        verify(verifyService).isAdultVerified(50L);
    }

    @Test
    void ottSearchShouldBuildOfficialAndFallbackUrls() {
        assertUrlContains(controller.redirectOttSearch("Netflix", "오징어 게임"),
                "netflix.com/search", "q=");
        assertUrlContains(controller.redirectOttSearch("TVING", "드라마"),
                "tving.com/search", "keyword=");
        assertUrlContains(controller.redirectOttSearch("wavve", "예능"),
                "wavve.com/search", "searchWord=");
        assertUrlContains(controller.redirectOttSearch("Watcha", "영화"),
                "watcha.com/search", "query=");
        assertUrlContains(controller.redirectOttSearch("Coupang Play", "스포츠"),
                "coupangplay.com/query", "keyword=");

        RedirectView disney = controller.redirectOttSearch("Disney+", "작품");
        assertEquals("https://www.disneyplus.com/", disney.getUrl());

        RedirectView fallback = controller.redirectOttSearch("Unknown", "제목");
        assertTrue(fallback.getUrl().startsWith("https://www.google.com/search?q="));
    }

    @Test
    void personFilmographyShouldAddPersonAndRelatedGoods() {
        PersonFilmographyVO person = new PersonFilmographyVO();
        when(contentService.getPersonFilmography(300L, "DIRECTOR"))
                .thenReturn(person);
        when(goodsService.getGoodsByTmdbActorId(300L, 8))
                .thenReturn(List.of());
        ExtendedModelMap model = new ExtendedModelMap();

        String view = controller.personFilmography(300L, "DIRECTOR", model);

        assertEquals("content/personFilmography", view);
        assertEquals(person, model.get("person"));
        assertEquals(List.of(), model.get("relatedGoodsList"));
    }

    private ContentListPageVO page(int current, int totalPages, int totalResults) {
        ContentListPageVO page = new ContentListPageVO();
        page.setContentList(List.of());
        page.setCurrentPage(current);
        page.setTotalPages(totalPages);
        page.setTotalResults(totalResults);
        return page;
    }

    private ContentVO content(int contentNo, String ageRating) {
        ContentVO content = new ContentVO();
        content.setContentNo(contentNo);
        content.setAgeRating(ageRating);
        return content;
    }

    private OttPlatformVO platform(String name, String logo) {
        OttPlatformVO platform = new OttPlatformVO();
        platform.setPlatformName(name);
        platform.setLogoImage(logo);
        return platform;
    }

    private void assertUrlContains(
            RedirectView view,
            String hostPath,
            String queryKey) {
        assertTrue(view.getUrl().contains(hostPath));
        assertTrue(view.getUrl().contains(queryKey));
    }
}
