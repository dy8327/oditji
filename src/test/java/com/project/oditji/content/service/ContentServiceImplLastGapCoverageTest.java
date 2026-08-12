package com.project.oditji.content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.content.dao.ContentDAO;
import com.project.oditji.content.vo.ContentVO;
import com.project.oditji.search.service.SearchContentPageCacheService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;

/** 콘텐츠 저장 서비스의 null target·빈 출시일·신규 캐시 없음 조건을 보완합니다. */
@ExtendWith(MockitoExtension.class)
class ContentServiceImplLastGapCoverageTest {

    @Mock
    private ContentDAO contentDAO;

    @Mock
    private TmdbService tmdbService;

    @Mock
    private SearchContentPageCacheService searchContentPageCacheService;

    @Mock
    private SearchContentStore searchContentStore;

    private ContentServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ContentServiceImpl(
                contentDAO,
                tmdbService,
                searchContentPageCacheService,
                searchContentStore);
    }

    @Test
    void cacheHelpersShouldCoverNullTargetAndReleaseDateWithoutText() {
        CachedContentVO cached = new CachedContentVO();

        Boolean nullTargetChanged = ReflectionTestUtils.invokeMethod(
                service,
                "applyCachedContent",
                null,
                cached);

        assertNotEquals(Boolean.TRUE, nullTargetChanged);

        ContentVO target = new ContentVO();
        Boolean blankCacheChanged = ReflectionTestUtils.invokeMethod(
                service,
                "applyCachedContent",
                target,
                cached);

        assertNotEquals(Boolean.TRUE, blankCacheChanged);

        Object parsedDate = ReflectionTestUtils.invokeMethod(
                service,
                "parseLocalDate",
                (Object) null);

        assertNull(parsedDate);
    }

    @Test
    void newContentWithoutCacheShouldSaveWithEmptyPlatformList() {
        ContentVO detail = createContent(0, 302L, "MOVIE");
        ContentVO saved = createContent(91, 302L, "MOVIE");

        when(searchContentStore.findByTmdbIdAndContentType(302L, "MOVIE"))
                .thenReturn(null);
        when(contentDAO.selectContentByTmdbId(302L, "MOVIE"))
                .thenReturn(null, saved);
        when(tmdbService.getDetailForSave(302L, "MOVIE"))
                .thenReturn(detail);

        int contentNo = service.ensureContentStored(302L, "MOVIE");

        assertEquals(91, contentNo);
        verify(contentDAO).insertContent(detail);
        verify(tmdbService).saveContentPlatform(
                saved,
                Collections.emptyList());
        verify(tmdbService).saveContentPeople(saved);
    }

    private ContentVO createContent(
            int contentNo,
            Long tmdbId,
            String contentType) {

        ContentVO content = new ContentVO();
        content.setContentNo(contentNo);
        content.setTmdbId(tmdbId);
        content.setContentType(contentType);
        return content;
    }
}
