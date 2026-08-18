package com.project.oditji.search.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.search.vo.CachedContentVO;

/** 중복 제거 guard의 CONTENT_TYPE null 피연산자를 직접 보완합니다. */
class SearchContentCollectorServiceResidualOperandClosure2Test {

    private SearchContentCollectorService service;

    @BeforeEach
    void setUp() {
        service = new SearchContentCollectorService(
                mock(SearchContentDiscoverService.class),
                mock(SearchContentEnrichmentService.class),
                mock(SearchContentManualOverrideService.class),
                mock(SearchContentAgeRatingService.class),
                mock(TmdbProviderService.class),
                mock(SearchContentPolicyService.class));
    }

    @Test
    void duplicateRemovalShouldRejectNullTypeAfterValidId() {
        CachedContentVO nullType = content(1L, null);
        CachedContentVO valid = content(2L, "MOVIE");

        @SuppressWarnings("unchecked")
        List<CachedContentVO> result =
                (List<CachedContentVO>) ReflectionTestUtils.invokeMethod(
                        service,
                        "removeDuplicate",
                        List.of(nullType, valid));

        assertEquals(1, result.size());
        assertSame(valid, result.get(0));
    }

    private CachedContentVO content(
            Long tmdbId,
            String contentType) {

        CachedContentVO content = new CachedContentVO();
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        content.setTitle("콘텐츠");
        content.setPopularity(1.0);
        content.setPlatformKeys(List.of("netflix"));
        return content;
    }
}
