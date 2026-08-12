package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/**
 * 관련 콘텐츠 추천 helper의 방어성 조건과 후반 short-circuit를 집중 보완합니다.
 */
class SearchContentPageCacheServiceLargeGapClosureCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void recommendationReasonShouldCoverNullCandidateFallbackAndDefensiveMembershipOperands() {
        ContentVO current = new ContentVO();
        current.setGenreText(null);
        current.setDirector("Same Director");
        current.setCastNames("Same Cast");

        assertEquals(
                "비슷한 콘텐츠로 추천했어요.",
                invokeString(
                        "createRelatedRecommendationReason",
                        current,
                        null,
                        Collections.emptySet(),
                        "action",
                        Collections.emptySet(),
                        Collections.emptySet()));

        SearchResultVO candidate = new SearchResultVO();
        candidate.setGenreText("Action");
        candidate.setDirector("Same Director");
        candidate.setCastNames("Same Cast");

        String reason = invokeString(
                "createRelatedRecommendationReason",
                current,
                candidate,
                Collections.emptySet(),
                "action",
                Collections.emptySet(),
                Collections.emptySet());

        assertEquals("같은 action 장르의 작품이에요.", reason);
    }

    @Test
    void recommendationReasonShouldCoverCandidateGenresPresentButCurrentGenresEmpty() {
        ContentVO current = new ContentVO();
        current.setGenreText(null);
        current.setDirector(null);
        current.setCastNames(null);

        SearchResultVO candidate = new SearchResultVO();
        candidate.setGenreText("Action");
        candidate.setDirector(null);
        candidate.setCastNames(null);

        assertEquals(
                "같은 콘텐츠 분류에서 추천한 작품이에요.",
                invokeString(
                        "createRelatedRecommendationReason",
                        current,
                        candidate,
                        Collections.emptySet(),
                        "",
                        Collections.emptySet(),
                        Collections.emptySet()));
    }

    @Test
    void reasonTypeShouldCoverEachNullAndMainGenreShortCircuitPosition() {
        ContentVO current = new ContentVO();
        current.setGenreText("Action");
        current.setDirector(null);
        current.setCastNames(null);

        SearchResultVO candidate = new SearchResultVO();
        candidate.setGenreText("Action");
        candidate.setDirector(null);
        candidate.setCastNames(null);

        assertEquals(
                "CATEGORY",
                invokeString(
                        "resolveRelatedRecommendationReasonType",
                        null,
                        candidate,
                        "action"));
        assertEquals(
                "CATEGORY",
                invokeString(
                        "resolveRelatedRecommendationReasonType",
                        current,
                        null,
                        "action"));
        assertEquals(
                "GENRE",
                invokeString(
                        "resolveRelatedRecommendationReasonType",
                        current,
                        candidate,
                        null));
        assertEquals(
                "GENRE",
                invokeString(
                        "resolveRelatedRecommendationReasonType",
                        current,
                        candidate,
                        ""));
        assertEquals(
                "GENRE",
                invokeString(
                        "resolveRelatedRecommendationReasonType",
                        current,
                        candidate,
                        "comedy"));
        assertEquals(
                "MAIN_GENRE",
                invokeString(
                        "resolveRelatedRecommendationReasonType",
                        current,
                        candidate,
                        "action"));
    }

    @Test
    void matchedValueHelpersShouldCoverNullAndBlankTargetsAfterEarlierOperandsPass() {
        assertEquals(
                "",
                invokeString(
                        "findFirstOriginalMatchedValue",
                        "Action",
                        null));
        assertEquals(
                "",
                invokeString(
                        "findFirstOriginalMatchedValue",
                        "Action",
                        "   "));
        assertEquals(
                "",
                invokeString(
                        "findOriginalValue",
                        "Action, Drama",
                        null));
        assertEquals(
                "",
                invokeString(
                        "findOriginalValue",
                        "Action, Drama",
                        "   "));
    }

    @Test
    void relatedScoreShouldCoverNonEmptyMainGenreMismatchAndUnmatchedPeople() {
        SearchResultVO candidate = new SearchResultVO();
        candidate.setGenreText("Action");
        candidate.setDirector("Candidate Director");
        candidate.setCastNames("Candidate Cast");

        Integer score = ReflectionTestUtils.invokeMethod(
                service,
                "calculateRelatedScore",
                candidate,
                Set.of("action"),
                "comedy",
                Set.of("other director"),
                Set.of("other cast"));

        assertEquals(50, score.intValue());
    }

    @Test
    void relatedCategoryShouldReachTalkOperandAfterRealityMisses() {
        assertEquals(
                "VARIETY",
                invokeString(
                        "resolveRelatedCategory",
                        "TV",
                        "토크"));
        assertEquals(
                "DRAMA",
                invokeString(
                        "resolveRelatedCategory",
                        "TV",
                        "로맨스"));
        java.util.List<?> values = ReflectionTestUtils.invokeMethod(
                service,
                "splitRelatedValues",
                "Action, action, , Drama");
        assertEquals(2, values.size());
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
