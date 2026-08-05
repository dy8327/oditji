package com.project.oditji.favorite.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.goods.vo.GoodsVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.wish.service.WishService;

import jakarta.servlet.http.HttpSession;

/** 콘텐츠 찜 목록, 토글, TMDB 토글, 성인 제한과 상태 조회를 검증합니다. */
@ExtendWith(MockitoExtension.class)
class FavoriteControllerCoverageTest {

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private ContentService contentService;

    @Mock
    private WishService wishService;

    @Mock
    private VerifyService verifyService;

    @Mock
    private HttpSession session;

    private final Map<String, Object> sessionValues = new HashMap<String, Object>();

    private FavoriteController controller;

    @BeforeEach
    void setUp() {
        controller = new FavoriteController(
                favoriteService,
                contentService,
                wishService,
                verifyService);
        when(session.getAttribute(anyString()))
                .thenAnswer(invocation -> sessionValues.get(invocation.getArgument(0)));
    }

    @Test
    void listShouldRedirectAnonymousMember() {
        assertEquals(
                "redirect:/member/login",
                controller.favoriteList(session, new ExtendedModelMap()));
    }

    @Test
    void listShouldExposeContentGoodsAndCounts() {
        login(1L);
        List<ContentVO> contents = List.of(new ContentVO(), new ContentVO());
        List<GoodsVO> goods = List.of(new GoodsVO());
        when(favoriteService.selectFavoriteList(1L)).thenReturn(contents);
        when(wishService.selectWishList(1L)).thenReturn(goods);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("favorite/favoriteList", controller.favoriteList(session, model));
        assertEquals(contents, model.get("contentFavoriteList"));
        assertEquals(2, model.get("contentCount"));
        assertEquals(goods, model.get("goodsFavoriteList"));
        assertEquals(1, model.get("goodsCount"));
        assertEquals(3, model.get("totalFavoriteCount"));
    }

    @Test
    void toggleShouldRejectAnonymousMember() {
        ResponseEntity<Map<String, Object>> response = controller.toggleFavorite(
                favorite(10L, null, null),
                session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("로그인이 필요합니다.", response.getBody().get("message"));
    }

    @Test
    void existingFavoriteShouldToggleWithoutAdultLookup() {
        login(2L);
        FavoriteVO request = favorite(10L, null, null);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(true);
        when(favoriteService.toggleFavorite(any(FavoriteVO.class))).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.toggleFavorite(request, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.FALSE, response.getBody().get("active"));
        assertEquals(10L, response.getBody().get("contentNo"));
        assertEquals(2L, request.getMemberNo());
        verify(contentService, never()).getContentDetail(anyInt());
    }

    @Test
    void normalContentShouldBeAddedWithoutAdultVerification() {
        login(3L);
        FavoriteVO request = favorite(11L, null, null);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(false);
        when(contentService.getContentDetail(11)).thenReturn(content("15세 이상 관람가"));
        when(favoriteService.toggleFavorite(any(FavoriteVO.class))).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.toggleFavorite(request, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("active"));
        verify(verifyService, never()).isAdultVerified(anyLong());
    }

    @Test
    void missingContentShouldReturnBadRequest() {
        login(4L);
        FavoriteVO request = favorite(12L, null, null);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(false);
        when(contentService.getContentDetail(12)).thenReturn(null);

        ResponseEntity<Map<String, Object>> response = controller.toggleFavorite(request, session);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("존재하지 않는 콘텐츠입니다.", response.getBody().get("message"));
    }

    @Test
    void adultContentShouldReturnForbiddenWhenMemberIsNotVerified() {
        login(5L);
        FavoriteVO request = favorite(13L, null, null);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(false);
        when(contentService.getContentDetail(13)).thenReturn(content("청소년 관람불가"));
        when(verifyService.isAdultVerified(5L)).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.toggleFavorite(request, session);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(
                "성인인증이 필요한 콘텐츠입니다. 성인인증 후 찜해 주세요.",
                response.getBody().get("message"));
    }

    @Test
    void verifiedMemberShouldAddAdultContent() {
        login(6L);
        FavoriteVO request = favorite(14L, null, null);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(false);
        when(contentService.getContentDetail(14)).thenReturn(content(" 19세 "));
        when(verifyService.isAdultVerified(6L)).thenReturn(true);
        when(favoriteService.toggleFavorite(any(FavoriteVO.class))).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.toggleFavorite(request, session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("active"));
    }

    @Test
    void toggleServiceFailuresShouldReturnMappedResponses() {
        login(7L);
        FavoriteVO invalid = favorite(null, null, null);
        when(favoriteService.toggleFavorite(invalid))
                .thenThrow(new IllegalArgumentException("콘텐츠 번호가 필요합니다."));

        ResponseEntity<Map<String, Object>> badRequest = controller.toggleFavorite(invalid, session);
        assertEquals(HttpStatus.BAD_REQUEST, badRequest.getStatusCode());

        FavoriteVO failed = favorite(15L, null, null);
        when(favoriteService.isFavorite(failed)).thenReturn(true);
        when(favoriteService.toggleFavorite(failed)).thenThrow(new IllegalStateException("db"));

        ResponseEntity<Map<String, Object>> error = controller.toggleFavorite(failed, session);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.getStatusCode());
        assertEquals("찜 처리 중 오류가 발생했습니다.", error.getBody().get("message"));
    }

    @Test
    void tmdbToggleShouldValidateLoginIdAndContentType() {
        ResponseEntity<Map<String, Object>> anonymous = controller.toggleFavoriteByTmdb(
                favorite(null, 100L, "movie"),
                session);
        assertEquals(HttpStatus.UNAUTHORIZED, anonymous.getStatusCode());

        login(8L);
        ResponseEntity<Map<String, Object>> invalidId = controller.toggleFavoriteByTmdb(
                favorite(null, 0L, "movie"),
                session);
        assertEquals(HttpStatus.BAD_REQUEST, invalidId.getStatusCode());
        assertEquals("TMDB 콘텐츠 번호가 올바르지 않습니다.", invalidId.getBody().get("message"));

        ResponseEntity<Map<String, Object>> invalidType = controller.toggleFavoriteByTmdb(
                favorite(null, 100L, "documentary"),
                session);
        assertEquals(HttpStatus.BAD_REQUEST, invalidType.getStatusCode());
        assertEquals("올바르지 않은 콘텐츠 유형입니다.", invalidType.getBody().get("message"));
    }

    @Test
    void tmdbToggleShouldPrepareNormalizeAndReturnIdentifiers() {
        login(9L);
        when(contentService.prepareContentDetail(101L, "MOVIE")).thenReturn(21);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(true);
        when(favoriteService.toggleFavorite(any(FavoriteVO.class))).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.toggleFavoriteByTmdb(
                favorite(null, 101L, " movie "),
                session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Boolean.TRUE, response.getBody().get("active"));
        assertEquals(21, response.getBody().get("contentNo"));
        assertEquals(101L, response.getBody().get("tmdbId"));
        assertEquals("MOVIE", response.getBody().get("contentType"));
    }

    @Test
    void tmdbToggleShouldMapAdultAndStorageFailures() {
        login(10L);
        when(contentService.prepareContentDetail(102L, "TV")).thenReturn(22);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(false);
        when(contentService.getContentDetail(22)).thenReturn(content("등급 정보 없음"));
        when(verifyService.isAdultVerified(10L)).thenReturn(false);

        ResponseEntity<Map<String, Object>> restricted = controller.toggleFavoriteByTmdb(
                favorite(null, 102L, "tv"),
                session);
        assertEquals(HttpStatus.FORBIDDEN, restricted.getStatusCode());

        when(contentService.prepareContentDetail(103L, "TV"))
                .thenThrow(new IllegalStateException("save failure"));
        ResponseEntity<Map<String, Object>> stateError = controller.toggleFavoriteByTmdb(
                favorite(null, 103L, "tv"),
                session);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stateError.getStatusCode());

        when(contentService.prepareContentDetail(104L, "TV"))
                .thenThrow(new RuntimeException("unexpected"));
        ResponseEntity<Map<String, Object>> genericError = controller.toggleFavoriteByTmdb(
                favorite(null, 104L, "tv"),
                session);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genericError.getStatusCode());
        assertEquals(
                "콘텐츠 저장 및 찜 처리 중 오류가 발생했습니다.",
                genericError.getBody().get("message"));
    }

    @Test
    void statusShouldSupportAnonymousLoggedInAndInvalidType() {
        ResponseEntity<Map<String, Object>> anonymous = controller.favoriteStatusByTmdb(
                200L,
                "movie",
                session);
        assertEquals(HttpStatus.OK, anonymous.getStatusCode());
        assertEquals(Boolean.FALSE, anonymous.getBody().get("login"));
        assertEquals(Boolean.FALSE, anonymous.getBody().get("active"));

        login(11L);
        when(favoriteService.isFavoriteByTmdb(any(FavoriteVO.class))).thenReturn(true);
        ResponseEntity<Map<String, Object>> active = controller.favoriteStatusByTmdb(
                200L,
                " tv ",
                session);
        assertEquals(Boolean.TRUE, active.getBody().get("login"));
        assertEquals(Boolean.TRUE, active.getBody().get("active"));

        ResponseEntity<Map<String, Object>> invalid = controller.favoriteStatusByTmdb(
                200L,
                " ",
                session);
        assertEquals(HttpStatus.BAD_REQUEST, invalid.getStatusCode());
        assertEquals("콘텐츠 유형이 없습니다.", invalid.getBody().get("message"));
    }

    @Test
    void ratingAliasesShouldExerciseAdditionalRestrictedFormats() {
        login(12L);
        String[] ratings = {null, " ", "NOT RATED", "Unrated", "NR"};

        for (int i = 0; i < ratings.length; i++) {
            long contentNo = 30L + i;
            FavoriteVO request = favorite(contentNo, null, null);
            when(favoriteService.isFavorite(request)).thenReturn(false);
            when(contentService.getContentDetail((int) contentNo)).thenReturn(content(ratings[i]));
            when(verifyService.isAdultVerified(12L)).thenReturn(false);

            ResponseEntity<Map<String, Object>> response = controller.toggleFavorite(request, session);
            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        }
        assertTrue(ratings.length > 0);
    }

    private void login(Long memberNo) {
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        sessionValues.put("loginMember", member);
    }

    private FavoriteVO favorite(Long contentNo, Long tmdbId, String contentType) {
        FavoriteVO favorite = new FavoriteVO();
        favorite.setContentNo(contentNo);
        favorite.setTmdbId(tmdbId);
        favorite.setContentType(contentType);
        return favorite;
    }

    private ContentVO content(String ageRating) {
        ContentVO content = new ContentVO();
        content.setAgeRating(ageRating);
        return content;
    }
}
