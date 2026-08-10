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

/**
 * 관련 콘텐츠 추천 이유 helper의 null/blank/분류/장르 조건을 집중 보완합니다.
 */
class SearchContentPageCacheRelatedConditionCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void recommendationReasonShouldReturnFallbackForNullCandidate() {
        ContentVO current = new ContentVO();

        assertEquals(
                "비슷한 콘텐츠로 추천했어요.",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "createRelatedRecommendationReason",
                        current,
                        null,
                        Set.of(),
                        "",
                        Set.of(),
                        Set.of()));
    }

    @Test
    void reasonTypeShouldCoverNullDirectorCastMainGenreGenreAndCategory() {
        ContentVO current = new ContentVO();
        SearchResultVO candidate = new SearchResultVO();

        assertEquals(
                "CATEGORY",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveRelatedRecommendationReasonType",
                        null,
                        candidate,
                        "action"));

        current.setDirector("감독A");
        candidate.setDirector("감독A");

        assertEquals(
                "DIRECTOR",
                reasonType(
                        current,
                        candidate,
                        "action"));

        current.setDirector(null);
        candidate.setDirector(null);
        current.setCastNames("배우A");
        candidate.setCastNames("배우A");

        assertEquals(
                "CAST",
                reasonType(
                        current,
                        candidate,
                        "action"));

        current.setCastNames(null);
        candidate.setCastNames(null);
        current.setGenreText("Action");
        candidate.setGenreText("Action");

        assertEquals(
                "MAIN_GENRE",
                reasonType(
                        current,
                        candidate,
                        "action"));

        assertEquals(
                "GENRE",
                reasonType(
                        current,
                        candidate,
                        "different"));

        current.setGenreText("Action");
        candidate.setGenreText("Drama");

        assertEquals(
                "CATEGORY",
                reasonType(
                        current,
                        candidate,
                        "different"));
    }

    @Test
    void matchedValueHelpersShouldCoverEmptyBlankMatchAndMiss() {
        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "findFirstOriginalMatchedValue",
                        null,
                        "A"));

        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "findFirstOriginalMatchedValue",
                        "A",
                        " "));

        assertEquals(
                "B",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "findFirstOriginalMatchedValue",
                        "a,b",
                        "C, B"));

        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "findFirstOriginalMatchedValue",
                        "A",
                        "B"));

        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "findOriginalValue",
                        null,
                        "a"));

        assertEquals(
                "Action",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "findOriginalValue",
                        "Drama, Action",
                        "action"));
    }

    @Test
    void categoryResolverShouldCoverAnimationDocumentaryVarietyMovieAndDrama() {
        assertEquals(
                "ANIMATION",
                resolveCategory(
                        "MOVIE",
                        "애니메이션"));
        assertEquals(
                "DOCUMENTARY",
                resolveCategory(
                        "MOVIE",
                        "다큐멘터리"));
        assertEquals(
                "VARIETY",
                resolveCategory(
                        "TV",
                        "토크"));
        assertEquals(
                "MOVIE",
                resolveCategory(
                        "MOVIE",
                        "액션"));
        assertEquals(
                "DRAMA",
                resolveCategory(
                        "TV",
                        "액션"));
    }

    @Test
    void mainGenreAndSplitHelpersShouldCoverNullExcludedDuplicateAndCustomGenres() {
        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveRelatedMainGenre",
                        (Object) null));

        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveRelatedMainGenre",
                        List.of()));

        assertEquals(
                "액션",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveRelatedMainGenre",
                        List.of(
                                "드라마",
                                "액션")));

        assertEquals(
                "드라마",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveRelatedMainGenre",
                        List.of(
                                "드라마",
                                "토크")));

        @SuppressWarnings("unchecked")
        List<String> split =
                (List<String>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "splitRelatedValues",
                                " Action , action, Drama ");

        assertEquals(
                List.of(
                        "action",
                        "drama"),
                split);

        assertTrue(
                ((List<?>)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "splitRelatedValues",
                                "   "))
                        .isEmpty());
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

    private String resolveCategory(
            String type,
            String genres) {

        return ReflectionTestUtils.invokeMethod(
                service,
                "resolveRelatedCategory",
                type,
                genres);
    }
}
