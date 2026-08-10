package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/**
 * SearchContentPageCacheService의 관련콘텐츠 점수/검색 category 조건을 추가 보완합니다.
 */
class SearchContentPageCacheServiceMoreConditionCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void relatedScoreShouldCoverMainGenreGenreDirectorAndCastMatches() {
        SearchResultVO candidate =
                new SearchResultVO();

        candidate.setGenreText(
                "액션, 코미디");
        candidate.setDirector(
                "감독A, 감독B");
        candidate.setCastNames(
                "배우A, 배우B");

        Integer score =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "calculateRelatedScore",
                        candidate,
                        Set.of(
                                "액션",
                                "드라마"),
                        "액션",
                        Set.of(
                                "감독a"),
                        Set.of(
                                "배우b"));

        assertEquals(
                1090,
                score.intValue());
    }

    @Test
    void relatedScoreShouldRemainZeroWhenNothingMatchesAndMainGenreIsBlank() {
        SearchResultVO candidate =
                new SearchResultVO();

        candidate.setGenreText("코미디");
        candidate.setDirector("감독B");
        candidate.setCastNames("배우B");

        Integer score =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "calculateRelatedScore",
                        candidate,
                        Set.of("액션"),
                        "",
                        Set.of("감독A"),
                        Set.of("배우A"));

        assertEquals(
                0,
                score.intValue());
    }

    @Test
    void relatedCategoryShouldCoverRealityAndTalkSecondOperand() {
        assertEquals(
                "VARIETY",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveRelatedCategory",
                        "TV",
                        "토크"));

        assertEquals(
                "VARIETY",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveRelatedCategory",
                        "TV",
                        "리얼리티"));
    }

    @Test
    void contentCategoryMatcherShouldCoverAnimationDocumentaryVarietyDramaAndMovieNegations() {
        CachedContentVO content =
                new CachedContentVO();

        content.setContentType("MOVIE");
        content.setGenreText("애니메이션");

        assertFalse(
                matchesCategory(
                        content,
                        "MOVIE"));
        assertTrue(
                matchesCategory(
                        content,
                        "ANIMATION"));

        content.setGenreText("다큐멘터리");

        assertTrue(
                matchesCategory(
                        content,
                        "DOCUMENTARY"));

        content.setContentType("TV");
        content.setGenreText("드라마, 토크");

        assertFalse(
                matchesCategory(
                        content,
                        "DRAMA"));
        assertTrue(
                matchesCategory(
                        content,
                        "VARIETY"));

        content.setGenreText("드라마");

        assertTrue(
                matchesCategory(
                        content,
                        "DRAMA"));
    }

    @Test
    void ageNormalizerShouldCoverEveryRestrictedAndUnknownAlias() {
        assertEquals(
                "등급 정보 없음",
                invokeString(
                        "normalizeAgeRating",
                        "Not Rated"));

        assertEquals(
                "등급 정보 없음",
                invokeString(
                        "normalizeAgeRating",
                        "UNRATED"));

        assertEquals(
                "등급 정보 없음",
                invokeString(
                        "normalizeAgeRating",
                        "NR"));

        assertEquals(
                "청소년 관람불가",
                invokeString(
                        "normalizeAgeRating",
                        "19세"));

        assertEquals(
                "청소년 관람불가",
                invokeString(
                        "normalizeAgeRating",
                        "18세"));
    }

    private boolean matchesCategory(
            CachedContentVO content,
            String category) {

        Boolean result =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "matchesContentCategory",
                        content,
                        category);

        return Boolean.TRUE.equals(result);
    }

    private String invokeString(
            String methodName,
            String value) {

        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                value);
    }
}
