package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.project.oditji.search.vo.CachedContentVO;

/**
 * SearchContentStore의 null/blank 조회와 노출정책 OR 조건을 보완합니다.
 */
class SearchContentStoreAdditionalConditionCoverageTest {

    private SearchContentPolicyService policyService;
    private SearchContentStore store;

    @BeforeEach
    void setUp() {
        policyService = mock(SearchContentPolicyService.class);
        store = new SearchContentStore(policyService);
    }

    @Test
    void lookupShouldCoverNullTypeBlankTypeAndNormalizedSuccessfulLookup() {
        assertNull(store.findByTmdbIdAndContentType(1L, null));
        assertNull(store.findByTmdbIdAndContentType(1L, "   "));

        CachedContentVO content = content(10L, "MOVIE");

        when(policyService.shouldExcludeContent(content))
                .thenReturn(false);

        store.replaceAll(List.of(content));

        assertSame(
                content,
                store.findByTmdbIdAndContentType(
                        10L,
                        " movie "));
    }

    @Test
    void replaceAllShouldRemoveNullAndPolicyExcludedRowsButKeepAllowedRows() {
        CachedContentVO excluded = content(1L, "MOVIE");
        CachedContentVO allowed = content(2L, "TV");

        when(policyService.shouldExcludeContent(excluded))
                .thenReturn(true);
        when(policyService.shouldExcludeContent(allowed))
                .thenReturn(false);

        store.replaceAll(
                Arrays.asList(
                        null,
                        excluded,
                        allowed));

        assertEquals(1, store.size());
        assertSame(allowed, store.getAll().get(0));
        assertTrue(store.getLastUpdatedAt() != null);
    }

    @Test
    void replaceAllShouldIgnoreContentWithEmptyContentKey() {
        CachedContentVO noType =
                new CachedContentVO();
        noType.setTmdbId(3L);

        when(policyService.shouldExcludeContent(noType))
                .thenReturn(false);

        store.replaceAll(List.of(noType));

        assertEquals(1, store.size());
        assertNull(
                store.findByTmdbIdAndContentType(
                        3L,
                        "MOVIE"));
    }

    private CachedContentVO content(
            Long id,
            String type) {

        CachedContentVO content =
                new CachedContentVO();
        content.setTmdbId(id);
        content.setContentType(type);
        content.setTitle("제목");
        return content;
    }
}
