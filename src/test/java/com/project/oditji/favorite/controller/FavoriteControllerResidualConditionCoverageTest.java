package com.project.oditji.favorite.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.service.ContentService;
import com.project.oditji.favorite.service.FavoriteService;
import com.project.oditji.favorite.vo.FavoriteVO;
import com.project.oditji.member.vo.MemberVO;
import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.wish.service.WishService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** 찜 컨트롤러의 성인등급 OR 체인, 콘텐츠 유형, logger false 분기를 보완합니다. */
class FavoriteControllerResidualConditionCoverageTest {

    private FavoriteService favoriteService;
    private ContentService contentService;
    private FavoriteController controller;

    @BeforeEach
    void setUp() {
        favoriteService = mock(FavoriteService.class);
        contentService = mock(ContentService.class);
        controller = new FavoriteController(
                favoriteService,
                contentService,
                mock(WishService.class),
                mock(VerifyService.class));
    }

    @Test
    void adultRatingHelperShouldCoverEveryAlternative() {
        assertTrue(restricted(null));
        assertTrue(restricted("   "));
        assertTrue(restricted("청소년 관람불가"));
        assertTrue(restricted("19세 이상"));
        assertTrue(restricted("등급 정보 없음"));
        assertTrue(restricted("Not Rated"));
        assertTrue(restricted("UNRATED"));
        assertTrue(restricted("NR"));
        assertFalse(restricted("15세 이상 관람가"));
    }

    @Test
    void normalizeContentTypeShouldCoverNullBlankMovieTvAndInvalid() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(controller, "normalizeContentType", (Object) null));
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(controller, "normalizeContentType", "   "));
        assertEquals("MOVIE", normalizeType(" movie "));
        assertEquals("TV", normalizeType(" tv "));
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(controller, "normalizeContentType", "OTHER"));
    }

    @Test
    void loggerDisabledShouldCoverGenericToggleFailureWithoutLogging() {
        HttpSession session = loginSession(1L);
        FavoriteVO request = new FavoriteVO();
        request.setContentNo(10L);
        when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(true);
        when(favoriteService.toggleFavorite(any(FavoriteVO.class))).thenThrow(new RuntimeException("boom"));

        Logger logger = (Logger) LoggerFactory.getLogger(FavoriteController.class);
        Level original = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);
            ResponseEntity<?> response = controller.toggleFavorite(request, session);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        } finally {
            logger.setLevel(original);
        }
    }

    @Test
    void loggerDisabledShouldCoverTmdbStateAndGenericFailures() {
        HttpSession session = loginSession(2L);
        FavoriteVO request = new FavoriteVO();
        request.setTmdbId(100L);
        request.setContentType("MOVIE");

        Logger logger = (Logger) LoggerFactory.getLogger(FavoriteController.class);
        Level original = logger.getLevel();
        try {
            logger.setLevel(Level.OFF);

            when(contentService.prepareContentDetail(100L, "MOVIE"))
                    .thenThrow(new IllegalStateException("save"));
            ResponseEntity<?> stateFailure = controller.toggleFavoriteByTmdb(request, session);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, stateFailure.getStatusCode());

            FavoriteVO second = new FavoriteVO();
            second.setTmdbId(101L);
            second.setContentType("TV");
            when(contentService.prepareContentDetail(101L, "TV")).thenReturn(20);
            when(favoriteService.isFavorite(any(FavoriteVO.class))).thenReturn(true);
            when(favoriteService.toggleFavorite(any(FavoriteVO.class))).thenThrow(new RuntimeException("db"));

            ResponseEntity<?> genericFailure = controller.toggleFavoriteByTmdb(second, session);
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genericFailure.getStatusCode());
        } finally {
            logger.setLevel(original);
        }
    }

    private boolean restricted(String rating) {
        Boolean result = ReflectionTestUtils.invokeMethod(controller, "isAdultRestrictedContent", rating);
        return Boolean.TRUE.equals(result);
    }

    private String normalizeType(String value) {
        return ReflectionTestUtils.invokeMethod(controller, "normalizeContentType", value);
    }

    private HttpSession loginSession(Long memberNo) {
        HttpSession session = mock(HttpSession.class);
        MemberVO member = new MemberVO();
        member.setMemberNo(memberNo);
        when(session.getAttribute("loginMember")).thenReturn(member);
        return session;
    }
}
