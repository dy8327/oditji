package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.tmdb.dao.TmdbDAO;

/** 관련 추천 문자열 helper의 후반 short-circuit와 fallback 분기를 추가로 보완합니다. */
class SearchContentPageCacheServiceRelatedOperandClosureCoverageTest {

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                mock(SearchContentStore.class),
                mock(TmdbDAO.class));
    }

    @Test
    void matchedValueHelpersShouldCoverNonEmptyCurrentWithBlankAndNonMatchingCandidateTokens() {
        assertEquals(
                "",
                invokeString(
                        "findFirstOriginalMatchedValue",
                        "액션",
                        " , 드라마"));

        assertEquals(
                "",
                invokeString(
                        "findOriginalValue",
                        "액션, 드라마",
                        "코미디"));
    }

    @Test
    void mainGenreShouldReturnFirstValueWhenEveryGenreIsExcluded() {
        assertEquals(
                "리얼리티",
                invokeString(
                        "resolveRelatedMainGenre",
                        List.of("리얼리티", "토크", "드라마")));
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
