package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/** 관련 콘텐츠 추천 문구와 동일 콘텐츠 판별의 잔여 short-circuit를 보완합니다. */
class SearchContentPageCacheServiceResidualClosure7Test {

    private SearchContentStore searchContentStore;
    private TmdbDAO tmdbDAO;
    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        searchContentStore = mock(SearchContentStore.class);
        tmdbDAO = mock(TmdbDAO.class);
        service = new SearchContentPageCacheService(searchContentStore, tmdbDAO);
        when(tmdbDAO.selectActivePlatformList()).thenReturn(List.of());
    }

    @Test
    void recommendationReasonShouldCoverGenreDirectorCastAndCompositionBranches() {
        ContentVO current = current("액션, 코미디", "감독A", "배우A");

        SearchResultVO directorCandidate = candidate(
                "코미디, 스릴러",
                "감독A",
                "다른배우");

        String directorReason = invokeString(
                "createRelatedRecommendationReason",
                current,
                directorCandidate,
                Set.of("액션", "코미디"),
                "액션",
                Set.of("감독a"),
                Set.of("배우a"));

        assertTrue(directorReason.contains("코미디 장르"));
        assertTrue(directorReason.contains("감독A 감독"));

        SearchResultVO castCandidate = candidate(
                null,
                "다른감독",
                "배우A");

        String castReason = invokeString(
                "createRelatedRecommendationReason",
                current(null, "감독A", "배우A"),
                castCandidate,
                Set.of(),
                "",
                Set.of("감독a"),
                Set.of("배우a"));

        assertTrue(castReason.contains("출연진 배우A"));

        SearchResultVO compositionCandidate = candidate(
                "코미디",
                null,
                null);

        String compositionReason = invokeString(
                "createRelatedRecommendationReason",
                current("액션", null, null),
                compositionCandidate,
                Set.of("액션"),
                "",
                Set.of(),
                Set.of());

        assertTrue(compositionReason.contains("비슷한 장르 구성"));
    }

    @Test
    void recommendationReasonShouldUseOriginalMainGenreWhenItCanBeResolved() {
        ContentVO current = current("액션, 드라마", null, null);
        SearchResultVO candidate = candidate("액션, 코미디", null, null);

        String reason = invokeString(
                "createRelatedRecommendationReason",
                current,
                candidate,
                Set.of("액션", "드라마"),
                "액션",
                Set.of(),
                Set.of());

        assertTrue(reason.startsWith("같은 액션 장르"));
    }

    @Test
    void originalValueHelperShouldCoverBlankOriginalValuesAfterNullGuardPasses() {
        assertEquals(
                "",
                invokeString(
                        "findOriginalValue",
                        "   ",
                        "액션"));
    }

    @Test
    void relatedListShouldCoverSameIdTypeComparisonAndNullCurrentId() {
        CachedContentVO same = cached(10L, "MOVIE", "동일 작품", "액션");
        CachedContentVO sameIdDifferentType = cached(10L, "TV", "동일 ID 다른 유형", "드라마");
        CachedContentVO other = cached(11L, "MOVIE", "다른 작품", "액션");

        when(searchContentStore.getAll())
                .thenReturn(List.of(same, sameIdDifferentType, other));

        ContentVO current = new ContentVO();
        current.setTmdbId(10L);
        current.setContentType("MOVIE");
        current.setGenreText("액션");

        List<SearchResultVO> related = service.getRelatedContentList(current, 10);
        assertEquals(1, related.size());
        assertEquals(11L, related.get(0).getTmdbId());

        when(searchContentStore.getAll()).thenReturn(List.of(other));

        ContentVO currentWithoutId = new ContentVO();
        currentWithoutId.setTmdbId(null);
        currentWithoutId.setContentType("MOVIE");
        currentWithoutId.setGenreText("액션");

        List<SearchResultVO> nullIdRelated = service.getRelatedContentList(
                currentWithoutId,
                10);

        assertEquals(1, nullIdRelated.size());
        assertEquals(11L, nullIdRelated.get(0).getTmdbId());
    }

    private ContentVO current(String genre, String director, String castNames) {
        ContentVO content = new ContentVO();
        content.setGenreText(genre);
        content.setDirector(director);
        content.setCastNames(castNames);
        return content;
    }

    private SearchResultVO candidate(String genre, String director, String castNames) {
        SearchResultVO result = new SearchResultVO();
        result.setGenreText(genre);
        result.setDirector(director);
        result.setCastNames(castNames);
        return result;
    }

    private CachedContentVO cached(
            Long tmdbId,
            String contentType,
            String title,
            String genreText) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle(title);
        content.setGenreText(genreText);
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }

    private String invokeString(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
