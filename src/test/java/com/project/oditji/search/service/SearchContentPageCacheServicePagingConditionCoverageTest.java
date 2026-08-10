package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.content.vo.ContentListPageVO;
import com.project.oditji.tmdb.dao.TmdbDAO;

/**
 * 콘텐츠 목록 page clamp와 신규 콘텐츠 날짜 필터의 양쪽 조건을 검증합니다.
 */
class SearchContentPageCacheServicePagingConditionCoverageTest {

    private SearchContentStore store;
    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        store = mock(SearchContentStore.class);

        service = new SearchContentPageCacheService(
                store,
                mock(TmdbDAO.class));
    }

    @Test
    void emptyResultShouldKeepRequestedPageNormalizedToOneAndTotalPagesZero() {
        when(store.getAll())
                .thenReturn(List.of());

        ContentListPageVO page =
                service.getContentListPage(
                        "all",
                        "popular",
                        0,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of());

        assertEquals(1, page.getCurrentPage());
        assertEquals(0, page.getTotalPages());
        assertTrue(page.getContentList().isEmpty());
    }

    @Test
    void oversizedPageShouldClampToLastAvailablePage() {
        List<CachedContentVO> contents =
                new ArrayList<CachedContentVO>();

        for (int index = 1;
                index <= 21;
                index++) {

            CachedContentVO content =
                    new CachedContentVO();

            content.setTmdbId((long) index);
            content.setContentType("MOVIE");
            content.setTitle("콘텐츠" + index);
            content.setGenreText("드라마");
            content.setAgeRating("15세 이상 관람가");
            content.setPlatformKeys(
                    List.of("netflix"));
            content.setSearchText(
                    "콘텐츠" + index);
            content.setPopularity(
                    (double) index);

            contents.add(content);
        }

        when(store.getAll())
                .thenReturn(contents);

        ContentListPageVO page =
                service.getContentListPage(
                        "all",
                        "popular",
                        99,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of());

        assertEquals(2, page.getCurrentPage());
        assertEquals(2, page.getTotalPages());
        assertEquals(1, page.getContentList().size());
    }
}
