package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.tmdb.dao.TmdbDAO;

/** 헤더 자동완성 공개 메서드의 입력 방어와 정상 위임 경로를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class SearchContentPageCacheServiceAutocompleteCoverageTest {

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private TmdbDAO tmdbDAO;

    private SearchContentPageCacheService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentPageCacheService(
                searchContentStore,
                tmdbDAO);
    }

    @Test
    void autocompleteShouldCoverEveryInvalidInputCondition() {
        assertTrue(service.getAutocompletePreview(null, 5).isEmpty());
        assertTrue(service.getAutocompletePreview("   ", 5).isEmpty());
        assertTrue(service.getAutocompletePreview("검색어", 0).isEmpty());
        assertTrue(service.getAutocompletePreview("검색어", -1).isEmpty());
    }

    @Test
    void autocompleteShouldDelegateToFirstPagePreviewForValidInput() {
        when(searchContentStore.getAll())
                .thenReturn(Collections.emptyList());
        when(tmdbDAO.selectActivePlatformList())
                .thenReturn(Collections.emptyList());

        assertTrue(service.getAutocompletePreview("검색어", 5).isEmpty());
    }
}
