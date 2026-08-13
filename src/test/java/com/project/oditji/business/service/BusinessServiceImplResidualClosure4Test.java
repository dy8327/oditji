package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

/** 상품 이미지 저장/교체 helper의 잔여 정상·실패 분기를 보완합니다. */
class BusinessServiceImplResidualClosure4Test {

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
    void mainImageUpdateShouldCoverNullEmptyUpdateInsertAndInsertFailurePaths() {
        GoodsManageVO ignored = goods(10L);
        List<Path> ignoredSavedPaths = new ArrayList<>();

        invoke("updateMainProductImageIfSelected", ignored, null, ignoredSavedPaths);
        invoke(
                "updateMainProductImageIfSelected",
                ignored,
                new MockMultipartFile("image", "empty.png", "image/png", new byte[0]),
                ignoredSavedPaths);

        verify(businessDAO, never()).updateProductMainImage(ignored);

        GoodsManageVO updated = goods(20L);
        List<Path> updatedSavedPaths = new ArrayList<>();
        when(businessDAO.updateProductMainImage(updated)).thenReturn(1);

        invoke(
                "updateMainProductImageIfSelected",
                updated,
                image("updated.png"),
                updatedSavedPaths);

        assertNotNull(updated.getImagePath());
        verify(businessDAO).updateProductMainImage(updated);
        verify(businessDAO, never()).insertProductImage(updated);

        GoodsManageVO inserted = goods(30L);
        List<Path> insertedSavedPaths = new ArrayList<>();
        when(businessDAO.updateProductMainImage(inserted)).thenReturn(0);
        when(businessDAO.insertProductImage(inserted)).thenReturn(1);

        invoke(
                "updateMainProductImageIfSelected",
                inserted,
                image("inserted.png"),
                insertedSavedPaths);

        verify(businessDAO).insertProductImage(inserted);

        GoodsManageVO failed = goods(40L);
        List<Path> failedSavedPaths = new ArrayList<>();
        when(businessDAO.updateProductMainImage(failed)).thenReturn(0);
        when(businessDAO.insertProductImage(failed)).thenReturn(0);

        MockMultipartFile failedImage = image("failed.png");
        assertThrows(
                IllegalStateException.class,
                () -> invoke(
                        "updateMainProductImageIfSelected",
                        failed,
                        failedImage,
                        failedSavedPaths));
    }

    @Test
    void imageSaveHelpersShouldCoverSuccessfulWritesBlankNamesAndIoFailure() throws Exception {
        Object productSaved = invoke("saveProductImage", image("product.png"));
        Object eventSaved = invoke("saveEventImage", image("event.png"));

        assertNotNull(productSaved);
        assertNotNull(eventSaved);

        MockMultipartFile blankName = new MockMultipartFile(
                "image",
                "",
                "image/png",
                new byte[] { 1 });

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("saveProductImage", blankName));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("saveEventImage", blankName));

        MultipartFile brokenProductImage = mock(MultipartFile.class);
        when(brokenProductImage.getOriginalFilename()).thenReturn("broken.png");
        when(brokenProductImage.getInputStream()).thenThrow(new IOException("broken"));

        assertThrows(
                IllegalStateException.class,
                () -> invoke("saveProductImage", brokenProductImage));

        MultipartFile brokenEventImage = mock(MultipartFile.class);
        when(brokenEventImage.getOriginalFilename()).thenReturn("broken.png");
        when(brokenEventImage.getInputStream()).thenThrow(new IOException("broken"));

        assertThrows(
                IllegalStateException.class,
                () -> invoke("saveEventImage", brokenEventImage));
    }

    @Test
    void deleteSavedFilesShouldDeleteEveryExistingPathAndIgnoreMissingFile() throws Exception {
        Path first = tempDirectory.resolve("first.png");
        Path second = tempDirectory.resolve("second.png");
        Path missing = tempDirectory.resolve("missing.png");
        Files.write(first, new byte[] { 1 });
        Files.write(second, new byte[] { 2 });

        invoke("deleteSavedFiles", List.of(first, second, missing));

        assertFalse(Files.exists(first));
        assertFalse(Files.exists(second));
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
