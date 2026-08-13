package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 상품 수정 이미지 helper의 null/empty/selected 단축평가 잔여 분기를 보완합니다. */
class BusinessServiceImplResidualClosure5Test {

    @TempDir
    Path tempDirectory;

    private BusinessDAO businessDAO;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                mock(NotificationService.class),
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void detailImageGuardsShouldCoverNullEmptySkippedSelectedAndLimitBranches() {
        MultipartFile[] emptyArray = new MultipartFile[0];
        MockMultipartFile emptyFile = new MockMultipartFile(
                "detail",
                "empty.png",
                "image/png",
                new byte[0]);
        MockMultipartFile selected = image("selected.png");

        assertDoesNotThrow(() -> invoke("validateDetailImages", (Object) null));
        assertDoesNotThrow(() -> invoke("validateDetailImages", (Object) emptyArray));
        assertDoesNotThrow(() -> invoke(
                "validateDetailImages",
                (Object) new MultipartFile[] { null, emptyFile, selected }));

        assertEquals(Boolean.FALSE, invoke("hasSelectedDetailImage", (Object) null));
        assertEquals(Boolean.FALSE, invoke("hasSelectedDetailImage", (Object) emptyArray));
        assertEquals(
                Boolean.FALSE,
                invoke(
                        "hasSelectedDetailImage",
                        (Object) new MultipartFile[] { null, emptyFile }));
        assertEquals(
                Boolean.TRUE,
                invoke(
                        "hasSelectedDetailImage",
                        (Object) new MultipartFile[] { null, emptyFile, selected }));

        MultipartFile[] elevenImages = new MultipartFile[11];
        for (int index = 0; index < elevenImages.length; index++) {
            elevenImages[index] = image("detail-" + index + ".png");
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateDetailImages", (Object) elevenImages));
    }

    @Test
    void saveDetailImagesShouldCoverBothEmptyGuardsSkipAndInsertFailure() {
        List<Path> savedPaths = new ArrayList<>();
        MultipartFile[] emptyArray = new MultipartFile[0];

        assertDoesNotThrow(() -> invoke(
                "saveDetailProductImages",
                10L,
                null,
                savedPaths));
        assertDoesNotThrow(() -> invoke(
                "saveDetailProductImages",
                10L,
                emptyArray,
                savedPaths));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "detail",
                "empty.png",
                "image/png",
                new byte[0]);
        assertDoesNotThrow(() -> invoke(
                "saveDetailProductImages",
                10L,
                new MultipartFile[] { null, emptyFile },
                savedPaths));
        assertTrue(savedPaths.isEmpty());

        when(businessDAO.insertProductImage(any(GoodsManageVO.class))).thenReturn(0);
        MultipartFile failedDetailImage = image("failed-detail.png");
        assertThrows(
                IllegalStateException.class,
                () -> invoke(
                        "saveDetailProductImages",
                        10L,
                        new MultipartFile[] { failedDetailImage },
                        savedPaths));
        assertEquals(1, savedPaths.size());
        assertTrue(Files.exists(savedPaths.get(0)));
    }

    @Test
    void productImageUpdateOrchestratorsShouldCoverEmptyAndSelectedImageOperands() {
        GoodsManageVO goods = goods(20L);
        GoodsManageVO existing = goods(20L);
        existing.setImagePath("/uploads/product/old-main.png");

        List<Path> savedPaths = new ArrayList<>();
        List<Path> deletePaths = new ArrayList<>();
        MockMultipartFile emptyMain = new MockMultipartFile(
                "image",
                "empty.png",
                "image/png",
                new byte[0]);

        invoke(
                "processMainProductImageUpdate",
                goods,
                existing,
                emptyMain,
                false,
                savedPaths,
                deletePaths);
        assertTrue(savedPaths.isEmpty());
        assertTrue(deletePaths.isEmpty());

        when(businessDAO.updateProductMainImage(goods)).thenReturn(1);
        invoke(
                "processMainProductImageUpdate",
                goods,
                existing,
                image("new-main.png"),
                false,
                savedPaths,
                deletePaths);

        assertEquals(1, savedPaths.size());
        assertEquals(1, deletePaths.size());
        verify(businessDAO).updateProductMainImage(goods);

        when(businessDAO.insertProductImage(any(GoodsManageVO.class))).thenReturn(1);
        List<Path> detailSavedPaths = new ArrayList<>();
        List<Path> detailDeletePaths = new ArrayList<>();

        invoke(
                "processDetailProductImageUpdate",
                20L,
                new MultipartFile[0],
                Set.of(),
                detailSavedPaths,
                detailDeletePaths);
        assertTrue(detailSavedPaths.isEmpty());

        invoke(
                "processDetailProductImageUpdate",
                20L,
                new MultipartFile[] { image("new-detail.png") },
                Set.of(),
                detailSavedPaths,
                detailDeletePaths);
        assertEquals(1, detailSavedPaths.size());
    }

    @Test
    void quietDeleteShouldCoverNullAndExistingFile() throws Exception {
        assertDoesNotThrow(() -> invoke("deleteSavedFileQuietly", (Object) null));

        Path file = Files.writeString(tempDirectory.resolve("delete-me.png"), "image");
        assertTrue(Files.exists(file));

        invoke("deleteSavedFileQuietly", file);

        assertFalse(Files.exists(file));
    }

    private GoodsManageVO goods(long productNo) {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(productNo);
        return goods;
    }

    private MockMultipartFile image(String filename) {
        return new MockMultipartFile(
                "image",
                filename,
                "image/png",
                new byte[] { 1, 2, 3 });
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Object... arguments) {
        return (T) ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
