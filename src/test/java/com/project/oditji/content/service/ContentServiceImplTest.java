package com.project.oditji.content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.content.dao.ContentDAO;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.content.vo.ContentViewHistoryVO;
import com.project.oditji.content.vo.PersonFilmographyVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.search.vo.SearchResultVO;
import com.project.oditji.tmdb.service.TmdbService;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.tmdb.vo.DirectorVO;
import com.project.oditji.tmdb.vo.OttPlatformVO;

/**
 * 콘텐츠 저장, 조회 이력, 상세 조회 및 JSONL 목록 위임 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceImplTest {

    @Mock
    private ContentDAO contentDAO;

    @Mock
    private TmdbService tmdbService;

    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;

    @Mock
    private SearchContentStore searchContentStore;

    private ContentServiceImpl contentService;

    @BeforeEach
    void setUp() {
        contentService = new ContentServiceImpl(
                contentDAO,
                tmdbService,
                searchContentPageCacheService,
                searchContentStore);
    }

    @Test
    void ensureContentStoredShouldRejectMissingValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> contentService.ensureContentStored(null, "MOVIE"));
        assertThrows(
                IllegalArgumentException.class,
                () -> contentService.ensureContentStored(0L, "MOVIE"));
        assertThrows(
                IllegalArgumentException.class,
                () -> contentService.ensureContentStored(10L, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> contentService.ensureContentStored(10L, " "));

        verify(contentDAO, never()).selectContentByTmdbId(any(), any());
    }

    @Test
    void ensureContentStoredShouldRejectUnsupportedContentType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> contentService.ensureContentStored(10L, "book"));

        assertTrue(exception.getMessage().contains("BOOK"));
        verify(contentDAO, never()).selectContentByTmdbId(any(), any());
    }

    @Test
    void ensureContentStoredShouldReturnExistingContentWithoutCacheUpdate() {
        ContentVO existing = createContent(15, 100L, "MOVIE");

        when(searchContentStore.findByTmdbIdAndContentType(100L, "MOVIE"))
                .thenReturn(null);
        when(contentDAO.selectContentByTmdbId(100L, "MOVIE"))
                .thenReturn(existing);

        int result = contentService.ensureContentStored(100L, " movie ");

        assertEquals(15, result);
        verify(contentDAO, never()).updateContentFromSearchCache(any());
        verify(tmdbService).saveContentPlatform(existing, Collections.emptyList());
        verify(tmdbService).saveContentPeople(existing);
    }

    @Test
    void ensureContentStoredShouldApplyAllUsableCacheValuesToExistingContent() {
        ContentVO existing = createContent(20, 200L, "TV");
        CachedContentVO cached = createFullCachedContent();

        when(searchContentStore.findByTmdbIdAndContentType(200L, "TV"))
                .thenReturn(cached);
        when(contentDAO.selectContentByTmdbId(200L, "TV"))
                .thenReturn(existing);

        int result = contentService.ensureContentStored(200L, "tv");

        assertEquals(20, result);
        assertEquals("캐시 제목", existing.getTitle());
        assertEquals("Cached Original", existing.getOriginalTitle());
        assertEquals("/poster.jpg", existing.getPosterPath());
        assertEquals(LocalDate.of(2026, Month.AUGUST, 1), existing.getReleaseDate());
        assertEquals("드라마, 코미디", existing.getGenreText());
        assertEquals(16, existing.getEpisodeCount().intValue());
        assertEquals("감독명", existing.getDirector());
        assertEquals("배우1, 배우2", existing.getCastNames());
        assertEquals("15세 이상 관람가", existing.getAgeRating());
        assertEquals(8.7, existing.getTmdbScore().doubleValue());
        verify(contentDAO).updateContentFromSearchCache(existing);
        verify(tmdbService).saveContentPlatform(existing, List.of("netflix", "tving"));
        verify(tmdbService).saveContentPeople(existing);
    }

    @Test
    void ensureContentStoredShouldIgnoreBlankAndInvalidCacheValues() {
        ContentVO existing = createContent(21, 201L, "MOVIE");
        existing.setReleaseDate(LocalDate.of(2020, Month.JANUARY, 1));

        CachedContentVO cached = new CachedContentVO();
        cached.setTitle(" ");
        cached.setOriginalTitle(null);
        cached.setPosterPath("");
        cached.setReleaseDate("not-a-date");
        cached.setGenreText(" ");
        cached.setDirector("");
        cached.setCastNames(null);
        cached.setAgeRating(" ");

        when(searchContentStore.findByTmdbIdAndContentType(201L, "MOVIE"))
                .thenReturn(cached);
        when(contentDAO.selectContentByTmdbId(201L, "MOVIE"))
                .thenReturn(existing);

        int result = contentService.ensureContentStored(201L, "MOVIE");

        assertEquals(21, result);
        assertEquals(LocalDate.of(2020, Month.JANUARY, 1), existing.getReleaseDate());
        verify(contentDAO, never()).updateContentFromSearchCache(any());
        verify(tmdbService).saveContentPlatform(existing, Collections.emptyList());
    }

    @Test
    void ensureContentStoredShouldInsertNewContentAndReturnGeneratedNumber() {
        CachedContentVO cached = createFullCachedContent();
        ContentVO detail = createContent(0, 300L, "MOVIE");
        ContentVO saved = createContent(77, 300L, "MOVIE");

        when(searchContentStore.findByTmdbIdAndContentType(300L, "MOVIE"))
                .thenReturn(cached);
        when(contentDAO.selectContentByTmdbId(300L, "MOVIE"))
                .thenReturn(null, saved);
        when(tmdbService.getDetailForSave(300L, "MOVIE"))
                .thenReturn(detail);

        int result = contentService.ensureContentStored(300L, "movie");

        assertEquals(77, result);
        assertEquals("캐시 제목", detail.getTitle());
        verify(contentDAO).insertContent(detail);
        verify(tmdbService).saveContentPlatform(saved, List.of("netflix", "tving"));
        verify(tmdbService).saveContentPeople(saved);
    }

    @Test
    void ensureContentStoredShouldFailWhenInsertedContentCannotBeRead() {
        ContentVO detail = createContent(0, 301L, "TV");

        when(searchContentStore.findByTmdbIdAndContentType(301L, "TV"))
                .thenReturn(null);
        when(contentDAO.selectContentByTmdbId(301L, "TV"))
                .thenReturn(null);
        when(tmdbService.getDetailForSave(301L, "TV"))
                .thenReturn(detail);

        assertThrows(
                IllegalStateException.class,
                () -> contentService.ensureContentStored(301L, "TV"));

        verify(contentDAO).insertContent(detail);
        verify(tmdbService, never()).saveContentPeople(any());
    }

    @Test
    void prepareContentDetailShouldIncreaseViewCount() {
        ContentVO existing = createContent(88, 400L, "MOVIE");
        when(searchContentStore.findByTmdbIdAndContentType(400L, "MOVIE"))
                .thenReturn(null);
        when(contentDAO.selectContentByTmdbId(400L, "MOVIE"))
                .thenReturn(existing);

        int result = contentService.prepareContentDetail(400L, "MOVIE");

        assertEquals(88, result);
        verify(contentDAO).increaseViewCount(88);
    }

    @Test
    void recordContentViewHistoryShouldIgnoreInvalidMember() {
        contentService.recordContentViewHistory(null, 10);
        contentService.recordContentViewHistory(0L, 10);

        verify(contentDAO, never()).mergeContentViewHistory(any());
    }

    @Test
    void recordContentViewHistoryShouldRejectInvalidContentNumber() {
        assertThrows(
                IllegalArgumentException.class,
                () -> contentService.recordContentViewHistory(1L, 0));

        verify(contentDAO, never()).mergeContentViewHistory(any());
    }

    @Test
    void recordContentViewHistoryShouldMergeMemberAndContent() {
        contentService.recordContentViewHistory(5L, 25);

        ArgumentCaptor<ContentViewHistoryVO> captor =
                ArgumentCaptor.forClass(ContentViewHistoryVO.class);
        verify(contentDAO).mergeContentViewHistory(captor.capture());

        assertEquals(5L, captor.getValue().getMemberNo().longValue());
        assertEquals(25, captor.getValue().getContentNo());
    }

    @Test
    void deleteExpiredHistoryShouldReturnDaoResult() {
        when(contentDAO.deleteExpiredContentViewHistory()).thenReturn(7);

        assertEquals(7, contentService.deleteExpiredContentViewHistory());
    }

    @Test
    void simpleDetailAndPeopleQueriesShouldReturnDaoValues() {
        ContentVO content = createContent(1, 1L, "MOVIE");
        List<ActorVO> actors = List.of(new ActorVO());
        List<DirectorVO> directors = List.of(new DirectorVO());
        List<OttPlatformVO> platforms = List.of(new OttPlatformVO());

        when(contentDAO.selectContentByContentNo(1)).thenReturn(content);
        when(contentDAO.selectActorListByContentNo(1)).thenReturn(actors);
        when(contentDAO.selectDirectorListByContentNo(1)).thenReturn(directors);
        when(contentDAO.selectOttPlatformListByContentNo(1)).thenReturn(platforms);

        assertSame(content, contentService.getContentDetail(1));
        assertSame(actors, contentService.getActorListByContentNo(1));
        assertSame(directors, contentService.getDirectorListByContentNo(1));
        assertSame(platforms, contentService.getOttPlatformListByContentNo(1));
    }

    @Test
    void peopleAndPlatformQueriesShouldConvertNullToEmptyLists() {
        when(contentDAO.selectActorListByContentNo(2)).thenReturn(null);
        when(contentDAO.selectDirectorListByContentNo(2)).thenReturn(null);
        when(contentDAO.selectOttPlatformListByContentNo(2)).thenReturn(null);

        assertTrue(contentService.getActorListByContentNo(2).isEmpty());
        assertTrue(contentService.getDirectorListByContentNo(2).isEmpty());
        assertTrue(contentService.getOttPlatformListByContentNo(2).isEmpty());
    }

    @Test
    void relatedContentShouldReturnEmptyWhenCurrentContentDoesNotExist() {
        when(contentDAO.selectContentByContentNo(99)).thenReturn(null);

        assertTrue(contentService.getRelatedContentList(99).isEmpty());
        verify(searchContentPageCacheService, never())
                .getRelatedContentList(any(), anyInt());
    }

    @Test
    void relatedContentShouldDelegateWithLimitThree() {
        ContentVO content = createContent(10, 10L, "TV");
        List<SearchResultVO> related = List.of(new SearchResultVO());
        when(contentDAO.selectContentByContentNo(10)).thenReturn(content);
        when(searchContentPageCacheService.getRelatedContentList(content, 3))
                .thenReturn(related);

        assertSame(related, contentService.getRelatedContentList(10));
    }

    @Test
    void personFilmographyAndMainListShouldDelegate() {
        PersonFilmographyVO filmography = new PersonFilmographyVO();
        List<ContentVO> mainList = List.of(createContent(3, 3L, "MOVIE"));
        when(tmdbService.getPersonFilmography(50L, "ACTOR"))
                .thenReturn(filmography);
        when(contentDAO.selectMainContentList()).thenReturn(mainList);

        assertSame(filmography, contentService.getPersonFilmography(50L, "ACTOR"));
        assertSame(mainList, contentService.getMainContentList());
    }

    @Test
    void threeFilterContentListShouldDelegateWithEmptyAdditionalFilter() {
        ContentListPageVO page = new ContentListPageVO();
        List<String> categories = List.of("MOVIE");
        List<String> genres = List.of("28");
        List<String> providers = List.of("netflix");

        when(searchContentPageCacheService.getContentListPage(
                "all", "popular", 2, categories, genres, providers, Collections.emptyList()))
                .thenReturn(page);

        assertSame(
                page,
                contentService.getContentListByType(
                        "all", "popular", 2, categories, genres, providers));
    }

    @Test
    void fourFilterContentListShouldDelegateEveryFilter() {
        ContentListPageVO page = new ContentListPageVO();
        List<String> categories = List.of("TV");
        List<String> genres = List.of("18");
        List<String> providers = List.of("tving");
        List<String> additional = List.of("adultExcluded");

        when(searchContentPageCacheService.getContentListPage(
                "new", "latest", 1, categories, genres, providers, additional))
                .thenReturn(page);

        assertSame(
                page,
                contentService.getContentListByType(
                        "new", "latest", 1, categories, genres, providers, additional));
    }

    @Test
    void recommendedContentOverloadsShouldDelegateWithLimitFive() {
        List<String> categories = List.of("MOVIE");
        List<String> genres = List.of("12");
        List<String> providers = List.of("watcha");
        List<String> additional = List.of("recent");
        List<SearchResultVO> basicResult = List.of(new SearchResultVO());
        List<SearchResultVO> fullResult = List.of(new SearchResultVO(), new SearchResultVO());

        when(searchContentPageCacheService.getContentRecommendedList(
                categories, genres, providers, Collections.emptyList(), 5))
                .thenReturn(basicResult);
        when(searchContentPageCacheService.getContentRecommendedList(
                categories, genres, providers, additional, 5))
                .thenReturn(fullResult);

        assertSame(
                basicResult,
                contentService.getContentRecommendedList(categories, genres, providers));
        assertSame(
                fullResult,
                contentService.getContentRecommendedList(
                        categories, genres, providers, additional));
    }

    private ContentVO createContent(int contentNo, Long tmdbId, String type) {
        ContentVO content = new ContentVO();
        content.setContentNo(contentNo);
        content.setTmdbId(tmdbId);
        content.setContentType(type);
        return content;
    }

    private CachedContentVO createFullCachedContent() {
        CachedContentVO cached = new CachedContentVO();
        cached.setTitle("캐시 제목");
        cached.setOriginalTitle("Cached Original");
        cached.setPosterPath("/poster.jpg");
        cached.setReleaseDate("2026-08-01");
        cached.setGenreText("드라마, 코미디");
        cached.setEpisodeCount(16);
        cached.setDirector("감독명");
        cached.setCastNames("배우1, 배우2");
        cached.setAgeRating("15세 이상 관람가");
        cached.setTmdbScore(8.7);
        cached.setPlatformKeys(List.of("netflix", "tving"));
        return cached;
    }
}
