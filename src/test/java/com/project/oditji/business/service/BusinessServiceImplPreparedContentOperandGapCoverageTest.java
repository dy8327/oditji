package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;

/** 상품 등록의 JSONL/레거시 선택과 배우 short-circuit 잔여 조건을 보완합니다. */
class BusinessServiceImplPreparedContentOperandGapCoverageTest {

    @TempDir
    Path tempDirectory;

    private BusinessDAO businessDAO;
    private ContentService contentService;
    private SearchContentStore searchContentStore;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        contentService = mock(ContentService.class);
        searchContentStore = mock(SearchContentStore.class);

        service = new BusinessServiceImpl(
                businessDAO,
                contentService,
                searchContentStore,
                mock(TmdbService.class),
                mock(NotificationService.class),
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void prepareProductContentShouldCoverEachJsonlOperandBeforeLegacyFallback() {
        ContentSearchVO legacyContent = new ContentSearchVO();
        when(businessDAO.selectContentByNo(9L)).thenReturn(legacyContent);

        GoodsManageVO zeroTmdb = legacyGoods();
        zeroTmdb.setTmdbId(0L);
        zeroTmdb.setContentType("MOVIE");
        assertDoesNotThrow(() -> invoke("prepareProductContent", zeroTmdb));

        GoodsManageVO nullType = legacyGoods();
        nullType.setTmdbId(1L);
        nullType.setContentType(null);
        assertDoesNotThrow(() -> invoke("prepareProductContent", nullType));

        GoodsManageVO blankType = legacyGoods();
        blankType.setTmdbId(1L);
        blankType.setContentType("   ");
        assertDoesNotThrow(() -> invoke("prepareProductContent", blankType));

        CachedContentVO cached = new CachedContentVO();
        cached.setTmdbId(2L);
        cached.setContentType("MOVIE");
        when(searchContentStore.findByTmdbIdAndContentType(2L, "MOVIE"))
                .thenReturn(cached);
        when(contentService.ensureContentStored(2L, " movie "))
                .thenReturn(22);

        GoodsManageVO jsonl = new GoodsManageVO();
        jsonl.setTmdbId(2L);
        jsonl.setContentType(" movie ");

        assertDoesNotThrow(() -> invoke("prepareProductContent", jsonl));
        assertEquals(22L, jsonl.getContentNo());
    }

    @Test
    void resolveProductActorShouldCoverZeroIdNullListInvalidRowsAndMatch() {
        GoodsManageVO zeroActor = new GoodsManageVO();
        zeroActor.setTmdbActorId(0L);
        zeroActor.setActorNo(7L);
        assertDoesNotThrow(() -> invoke("resolveProductActor", zeroActor));
        assertEquals(Long.valueOf(7L), zeroActor.getActorNo());

        GoodsManageVO missing = new GoodsManageVO();
        missing.setContentNo(10L);
        missing.setTmdbActorId(100L);
        when(businessDAO.selectActorListByContentNo(10L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("resolveProductActor", missing));

        ActorSearchVO other = new ActorSearchVO();
        other.setActorNo(3L);
        other.setTmdbActorId(300L);

        ActorSearchVO matched = new ActorSearchVO();
        matched.setActorNo(4L);
        matched.setTmdbActorId(100L);

        when(businessDAO.selectActorListByContentNo(11L))
                .thenReturn(Arrays.asList(null, other, matched));

        GoodsManageVO selected = new GoodsManageVO();
        selected.setContentNo(11L);
        selected.setTmdbActorId(100L);

        assertDoesNotThrow(() -> invoke("resolveProductActor", selected));
        assertEquals(Long.valueOf(4L), selected.getActorNo());
    }

    private GoodsManageVO legacyGoods() {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setContentNo(9L);
        return goods;
    }

    private void invoke(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
