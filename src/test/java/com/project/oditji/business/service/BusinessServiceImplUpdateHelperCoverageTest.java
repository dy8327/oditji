package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.goods.vo.ProductOptionVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/**
 * SonarQube 복잡도 개선 과정에서 분리된 상품 수정 helper들의 남은 분기를 보완합니다.
 */
class BusinessServiceImplUpdateHelperCoverageTest {

    private BusinessDAO businessDAO;
    private NotificationService notificationService;
    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        businessDAO = mock(BusinessDAO.class);
        notificationService = mock(NotificationService.class);
        service = new BusinessServiceImpl(
                businessDAO,
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                notificationService,
                "uploads/product",
                "uploads/event");
    }

    @Test
    void updateRequestAndExistingProductValidatorsShouldCoverAllBranches() {
        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateProductUpdateRequest", (Object) null));

        GoodsManageVO invalidNo = new GoodsManageVO();
        invalidNo.setProductNo(0L);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("validateProductUpdateRequest", invalidNo));

        GoodsManageVO valid = goods(10L, 20L, "GOODS");
        assertDoesNotThrow(() -> invoke("validateProductUpdateRequest", valid));

        when(businessDAO.selectProductForUpdate(10L, 20L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> invoke("getExistingProductForUpdate", valid));

        GoodsManageVO deleting = goods(10L, 20L, "GOODS");
        deleting.setStatus("DELETE_REQUESTED");
        when(businessDAO.selectProductForUpdate(10L, 20L)).thenReturn(deleting);
        assertThrows(IllegalStateException.class,
                () -> invoke("getExistingProductForUpdate", valid));

        GoodsManageVO existing = goods(10L, 20L, "GOODS");
        existing.setStatus("APPROVED");
        when(businessDAO.selectProductForUpdate(10L, 20L)).thenReturn(existing);
        GoodsManageVO result = ReflectionTestUtils.invokeMethod(
                service, "getExistingProductForUpdate", valid);
        assertSame(existing, result);
    }

    @Test
    void deletedDetailImageFilteringAndCountShouldCoverBoundaries() {
        Set<String> none = ReflectionTestUtils.invokeMethod(
                service,
                "filterExistingDetailImagePaths",
                List.of("/uploads/product/a.png"),
                null);
        assertTrue(none.isEmpty());

        String[] requested = {
                null,
                "/uploads/product/missing.png",
                "/uploads/product/a.png",
                "/uploads/product/a.png"
        };

        Set<String> filtered = ReflectionTestUtils.invokeMethod(
                service,
                "filterExistingDetailImagePaths",
                List.of("/uploads/product/a.png", "/uploads/product/b.png"),
                requested);

        assertEquals(Set.of("/uploads/product/a.png"), filtered);

        MultipartFile selected = new MockMultipartFile(
                "detailImages",
                "new.png",
                "image/png",
                new byte[] { 1 });

        assertDoesNotThrow(() -> invoke(
                "validateDetailImageCountAfterUpdate",
                10,
                1,
                new MultipartFile[] { selected }));

        assertThrows(IllegalArgumentException.class, () -> invoke(
                "validateDetailImageCountAfterUpdate",
                10,
                0,
                new MultipartFile[] { selected }));
    }

    @Test
    void resolveDeletedPathsShouldUseDaoAndSelectedImageCount() {
        when(businessDAO.selectProductDetailImagePathList(30L))
                .thenReturn(List.of(
                        "/uploads/product/a.png",
                        "/uploads/product/b.png"));

        String[] requested = { "/uploads/product/a.png", "/outside.png" };

        Set<String> result = ReflectionTestUtils.invokeMethod(
                service,
                "resolveDeletedDetailImagePaths",
                30L,
                null,
                requested);

        assertEquals(Set.of("/uploads/product/a.png"), result);
    }

    @Test
    void updateProductAndOptionsShouldCoverUpdateFailureAndNonOptionSuccess() {
        GoodsManageVO failed = goods(40L, 50L, "GOODS");
        when(businessDAO.updateProduct(failed)).thenReturn(0);

        assertThrows(IllegalStateException.class,
                () -> invoke("updateProductAndOptions", failed));

        GoodsManageVO success = goods(41L, 50L, "GOODS");
        ProductOptionVO oldOption = new ProductOptionVO();
        oldOption.setOptionNo(410L);
        oldOption.setColorName("RED");
        oldOption.setSizeName("M");
        oldOption.setStock(1);

        when(businessDAO.updateProduct(success)).thenReturn(1);
        when(businessDAO.selectProductOptionsByProductNo(41L))
                .thenReturn(List.of(oldOption));

        assertDoesNotThrow(() -> invoke("updateProductAndOptions", success));
        verify(businessDAO).deleteProductOptionByOptionNo(410L);
    }

    @Test
    void mainAndDetailImageHelpersShouldCoverDeleteAndNoSelectionPaths() {
        GoodsManageVO goods = goods(60L, 70L, "GOODS");
        GoodsManageVO existing = goods(60L, 70L, "GOODS");
        existing.setImagePath("/uploads/product/old.png");

        List<Path> saved = new ArrayList<>();
        List<Path> oldToDelete = new ArrayList<>();

        invoke(
                "processMainProductImageUpdate",
                goods,
                existing,
                null,
                false,
                saved,
                oldToDelete);

        verify(businessDAO, never()).deleteProductMainImageByProductNo(60L);
        assertTrue(oldToDelete.isEmpty());

        invoke(
                "processMainProductImageUpdate",
                goods,
                existing,
                null,
                true,
                saved,
                oldToDelete);

        verify(businessDAO).deleteProductMainImageByProductNo(60L);
        assertEquals(1, oldToDelete.size());

        when(businessDAO.deleteProductDetailImageByPath(
                60L, "/uploads/product/detail.png"))
                .thenReturn(1);

        invoke(
                "processDetailProductImageUpdate",
                60L,
                null,
                Set.of("/uploads/product/detail.png"),
                saved,
                oldToDelete);

        verify(businessDAO).deleteProductDetailImageByPath(
                60L, "/uploads/product/detail.png");
        assertEquals(2, oldToDelete.size());
    }

    @Test
    void reapprovalNotificationHelperShouldDelegateToNotificationService() {
        GoodsManageVO goods = goods(80L, 90L, "GOODS");
        goods.setProductName("ODITJI 굿즈");

        invoke("createProductReapprovalNotification", goods);

        verify(notificationService).createForAdmins(
                "PRODUCT_REQUEST",
                "상품 재승인 요청",
                "ODITJI 굿즈 상품의 수정 승인 요청이 접수되었습니다.",
                "/admin/product/list?tab=waiting",
                "PRODUCT",
                80L);
    }

    private GoodsManageVO goods(long productNo, long businessNo, String type) {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(productNo);
        goods.setBusinessNo(businessNo);
        goods.setProductType(type);
        return goods;
    }

    private void invoke(String methodName, Object... arguments) {
        ReflectionTestUtils.invokeMethod(service, methodName, arguments);
    }
}
