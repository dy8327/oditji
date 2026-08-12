package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 관련 콘텐츠 추천 이유 helper의 우선순위와 문자열 short-circuit 분기를 집중 보완합니다. */
class SearchContentPageCacheServiceRelatedHelperGapCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void reasonTypeShouldCoverNullDirectorCastMainGenreGenreAndCategory() {
        ContentVO current = new ContentVO();
        SearchResultVO candidate = new SearchResultVO();

        assertEquals("CATEGORY", reasonType(null, candidate, "액션"));
        assertEquals("CATEGORY", reasonType(current, null, "액션"));

        current.setDirector("감독A");
        candidate.setDirector("감독A");
        assertEquals("DIRECTOR", reasonType(current, candidate, ""));

        current.setDirector(null);
        candidate.setDirector(null);
        current.setCastNames("배우A");
        candidate.setCastNames("배우A");
        assertEquals("CAST", reasonType(current, candidate, ""));

        current.setCastNames(null);
        candidate.setCastNames(null);
        current.setGenreText("액션, 코미디");
        candidate.setGenreText("액션, 드라마");
        assertEquals("MAIN_GENRE", reasonType(current, candidate, "액션"));

        candidate.setGenreText("코미디, 드라마");
        assertEquals("GENRE", reasonType(current, candidate, ""));

        candidate.setGenreText("멜로");
        assertEquals("CATEGORY", reasonType(current, candidate, ""));
    }

    @Test
    void matchedValueHelpersShouldCoverEmptyBlankMatchAndMiss() {
        assertEquals("", invokeString("findFirstOriginalMatchedValue", null, "액션"));
        assertEquals("", invokeString("findFirstOriginalMatchedValue", "액션", null));
        assertEquals("", invokeString("findFirstOriginalMatchedValue", "액션", "   "));
        assertEquals("코미디", invokeString(
                "findFirstOriginalMatchedValue",
                "액션, 코미디",
                "드라마, 코미디"));
        assertEquals("", invokeString(
                "findFirstOriginalMatchedValue",
                "액션",
                "드라마"));

        assertEquals("", invokeString("findOriginalValue", null, "액션"));
        assertEquals("", invokeString("findOriginalValue", "액션", null));
        assertEquals("", invokeString("findOriginalValue", "액션", "   "));
        assertEquals("코미디", invokeString(
                "findOriginalValue",
                "액션, 코미디",
                "코미디"));
        assertEquals("", invokeString(
                "findOriginalValue",
                "액션, 코미디",
                "드라마"));
    }

    @Test
    void relatedGenreHelpersShouldCoverEveryCategoryMainGenreAndSplitBoundary() {
        assertEquals("ANIMATION", invokeString("resolveRelatedCategory", "TV", "애니메이션"));
        assertEquals("DOCUMENTARY", invokeString("resolveRelatedCategory", "TV", "다큐멘터리"));
        assertEquals("VARIETY", invokeString("resolveRelatedCategory", "TV", "리얼리티"));
        assertEquals("VARIETY", invokeString("resolveRelatedCategory", "TV", "토크"));
        assertEquals("MOVIE", invokeString("resolveRelatedCategory", "MOVIE", "액션"));
        assertEquals("DRAMA", invokeString("resolveRelatedCategory", "TV", "액션"));

        assertEquals("", invokeString("resolveRelatedMainGenre", (Object) null));
        assertEquals("", invokeString("resolveRelatedMainGenre", List.of()));
        assertEquals("액션", invokeString("resolveRelatedMainGenre", List.of("드라마", "액션")));
        assertEquals("드라마", invokeString("resolveRelatedMainGenre", List.of("드라마", "토크")));

        List<?> nullSplit = ReflectionTestUtils.invokeMethod(
                service,
                "splitRelatedValues",
                (Object) null);
        List<?> blankSplit = ReflectionTestUtils.invokeMethod(
                service,
                "splitRelatedValues",
                "   ");
        List<?> values = ReflectionTestUtils.invokeMethod(
                service,
                "splitRelatedValues",
                " 액션, 액션, 코미디,  ");

        assertTrue(nullSplit.isEmpty());
        assertTrue(blankSplit.isEmpty());
        assertEquals(List.of("액션", "코미디"), values);
    }

    @Test
    void relatedScoreShouldAccumulateMainGenreGenreDirectorAndCastMatches() {
        SearchResultVO candidate = new SearchResultVO();
        candidate.setGenreText("액션, 코미디");
        candidate.setDirector("감독A, 감독B");
        candidate.setCastNames("배우A, 배우B");

        Integer score = ReflectionTestUtils.invokeMethod(
                service,
                "calculateRelatedScore",
                candidate,
                Set.of("액션", "코미디"),
                "액션",
                Set.of("감독a"),
                Set.of("배우b"));

        assertEquals(1140, score.intValue());
    }

    private String reasonType(
            ContentVO current,
            SearchResultVO candidate,
            String mainGenre) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "resolveRelatedRecommendationReasonType",
                current,
                candidate,
                mainGenre);
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
