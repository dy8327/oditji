package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/**
 * 전체 커버리지에서 비중이 큰 BusinessServiceImpl의 상품 수정/이미지 조건을
 * 우선 보강합니다.
 */
@ExtendWith(MockitoExtension.class)
class BusinessServiceImplPriorityCoverageTest {

    @TempDir
    Path tempDirectory;

    @Mock
    private BusinessDAO businessDAO;

    @Mock
    private ContentService contentService;

    @Mock
    private SearchContentStore searchContentStore;

    @Mock
    private TmdbService tmdbService;

    @Mock
    private NotificationService notificationService;

    private BusinessServiceImpl service;
    private Path productDirectory;

    @BeforeEach
    void setUp() {
        productDirectory = tempDirectory.resolve("products");
        service = new BusinessServiceImpl(
                businessDAO,
                contentService,
                searchContentStore,
                tmdbService,
                notificationService,
                productDirectory.toString(),
                tempDirectory.resolve("events").toString());
    }

    @Test
    void popularProductsShouldValidateAndReturnSafeCollection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getPopularProducts(0L));

        when(businessDAO.selectPopularProductsByBusinessNo(10L, 5))
                .thenReturn(null);
        assertTrue(service.getPopularProducts(10L).isEmpty());

        List<GoodsManageVO> products = List.of(new GoodsManageVO());
        when(businessDAO.selectPopularProductsByBusinessNo(11L, 5))
                .thenReturn(products);
        assertSame(products, service.getPopularProducts(11L));
    }

    @Test
    void updateProductShouldRejectMoreThanTenRemainingDetailImages() {
        GoodsManageVO input = validProduct(700L, "ETC");
        GoodsManageVO existing = approvedExistingProduct();

        List<String> existingDetailImages = new ArrayList<>();
        for (int index = 0; index < 10; index++) {
            existingDetailImages.add("/uploads/product/old-" + index + ".png");
        }

        when(businessDAO.selectProductForUpdate(700L, 10L)).thenReturn(existing);
        when(businessDAO.selectProductDetailImagePathList(700L))
                .thenReturn(existingDetailImages);

        MultipartFile[] newDetailImagesForLimit = new MultipartFile[] {
                image("new.png", "image/png")
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(
                        input,
                        null,
                        newDetailImagesForLimit,
                        false,
                        null));

        verify(businessDAO, never()).updateProduct(input);
    }

    @Test
    void updateProductShouldDeleteOnlyRegisteredImagesAndAddNewDetailImage()
            throws Exception {

        Files.createDirectories(productDirectory);
        Path oldMainImage = Files.writeString(
                productDirectory.resolve("old-main.png"),
                "old-main",
                StandardCharsets.UTF_8);
        Path oldDetailImage = Files.writeString(
                productDirectory.resolve("old-detail.jpg"),
                "old-detail",
                StandardCharsets.UTF_8);

        GoodsManageVO input = validProduct(701L, "ETC");
        GoodsManageVO existing = approvedExistingProduct();
        existing.setImagePath("/uploads/product/old-main.png");

        when(businessDAO.selectProductForUpdate(701L, 10L)).thenReturn(existing);
        when(businessDAO.selectProductDetailImagePathList(701L)).thenReturn(List.of(
                "/uploads/product/old-detail.jpg",
                "/uploads/product/keep.jpg"));
        when(businessDAO.updateProduct(input)).thenReturn(1);
        when(businessDAO.deleteProductDetailImageByPath(
                701L,
                "/uploads/product/old-detail.jpg"))
                .thenReturn(1);
        when(businessDAO.insertProductImage(any(GoodsManageVO.class)))
                .thenReturn(1);

        MultipartFile[] newDetailImages = new MultipartFile[] {
                null,
                new MockMultipartFile(
                        "detailImages",
                        "",
                        "application/octet-stream",
                        new byte[0]),
                image("new-detail.webp", "image/webp")
        };

        service.updateProduct(
                input,
                null,
                newDetailImages,
                true,
                new String[] {
                        "/uploads/product/old-detail.jpg",
                        "/uploads/product/old-detail.jpg",
                        "/uploads/product/not-owned.jpg",
                        null
                });

        verify(businessDAO).deleteProductMainImageByProductNo(701L);
        verify(businessDAO).deleteProductDetailImageByPath(
                701L,
                "/uploads/product/old-detail.jpg");
        verify(businessDAO, never()).deleteProductDetailImageByPath(
                701L,
                "/uploads/product/not-owned.jpg");

        assertFalse(Files.exists(oldMainImage));
        assertFalse(Files.exists(oldDetailImage));
        assertEquals("WAITING", input.getStatus());

        try (var files = Files.list(productDirectory)) {
            assertEquals(1L, files.count());
        }
    }

    @Test
    void updateProductShouldReplaceMainImageAndDeletePreviousFile() throws Exception {
        Files.createDirectories(productDirectory);
        Path oldMainImage = Files.writeString(
                productDirectory.resolve("replace-main.png"),
                "old-main",
                StandardCharsets.UTF_8);

        GoodsManageVO input = validProduct(702L, "ETC");
        GoodsManageVO existing = approvedExistingProduct();
        existing.setImagePath("/uploads/product/replace-main.png");

        when(businessDAO.selectProductForUpdate(702L, 10L)).thenReturn(existing);
        when(businessDAO.selectProductDetailImagePathList(702L)).thenReturn(List.of());
        when(businessDAO.updateProduct(input)).thenReturn(1);
        when(businessDAO.updateProductMainImage(input)).thenReturn(1);

        service.updateProduct(
                input,
                image("replacement.JPG", "image/jpeg"),
                null,
                false,
                null);

        assertFalse(Files.exists(oldMainImage));
        assertTrue(input.getImagePath().startsWith("/uploads/product/"));
        assertTrue(input.getImagePath().endsWith(".jpg"));
        verify(businessDAO, never()).insertProductImage(input);
    }

    @Test
    void updateProductShouldCleanSavedMainImageWhenImageRowCannotBeInserted()
            throws Exception {

        GoodsManageVO input = validProduct(703L, "ETC");
        GoodsManageVO existing = approvedExistingProduct();
        existing.setImagePath("/outside/not-managed.png");

        when(businessDAO.selectProductForUpdate(703L, 10L)).thenReturn(existing);
        when(businessDAO.selectProductDetailImagePathList(703L)).thenReturn(List.of());
        when(businessDAO.updateProduct(input)).thenReturn(1);
        when(businessDAO.updateProductMainImage(input)).thenReturn(0);
        when(businessDAO.insertProductImage(input)).thenReturn(0);

        MultipartFile failedMainImage = image("failed-main.png", "image/png");

        assertThrows(
                IllegalStateException.class,
                () -> service.updateProduct(
                        input,
                        failedMainImage,
                        null,
                        false,
                        null));

        assertDirectoryHasNoFiles(productDirectory);
    }

    @Test
    void updateProductShouldCleanSavedDetailImageWhenDatabaseInsertFails()
            throws Exception {

        GoodsManageVO input = validProduct(704L, "ETC");
        GoodsManageVO existing = approvedExistingProduct();

        when(businessDAO.selectProductForUpdate(704L, 10L)).thenReturn(existing);
        when(businessDAO.selectProductDetailImagePathList(704L)).thenReturn(List.of());
        when(businessDAO.updateProduct(input)).thenReturn(1);
        when(businessDAO.insertProductImage(any(GoodsManageVO.class)))
                .thenReturn(0);

        MultipartFile[] failedDetailImages = new MultipartFile[] {
                image("failed-detail.gif", "image/gif")
        };

        assertThrows(
                IllegalStateException.class,
                () -> service.updateProduct(
                        input,
                        null,
                        failedDetailImages,
                        false,
                        null));

        assertDirectoryHasNoFiles(productDirectory);
    }

    @Test
    void updateProductShouldValidateConnectedActor() {
        GoodsManageVO input = validProduct(705L, "ETC");
        input.setContentNo(100L);
        input.setActorNo(50L);

        GoodsManageVO existing = approvedExistingProduct();
        when(businessDAO.selectProductForUpdate(705L, 10L)).thenReturn(existing);
        when(businessDAO.selectProductDetailImagePathList(705L)).thenReturn(List.of());
        when(businessDAO.countContentActor(100L, 50L)).thenReturn(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(input, null, null, false, null));

        when(businessDAO.countContentActor(100L, 50L)).thenReturn(1);
        when(businessDAO.updateProduct(input)).thenReturn(1);

        service.updateProduct(input, null, null, false, null);
        assertEquals(50L, input.getActorNo());
    }

    @Test
    void updateProductShouldCoverProductValidationConditions() {
        GoodsManageVO existing = approvedExistingProduct();
        when(businessDAO.selectProductForUpdate(anyLong(), anyLong()))
                .thenReturn(existing);
        when(businessDAO.selectProductDetailImagePathList(anyLong()))
                .thenReturn(List.of());

        GoodsManageVO invalidBusiness = validProduct(710L, "ETC");
        invalidBusiness.setBusinessNo(0L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(invalidBusiness, null, null, false, null));

        GoodsManageVO nullName = validProduct(711L, "ETC");
        nullName.setProductName(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(nullName, null, null, false, null));

        GoodsManageVO blankName = validProduct(712L, "ETC");
        blankName.setProductName("   ");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(blankName, null, null, false, null));

        GoodsManageVO longName = validProduct(713L, "ETC");
        longName.setProductName("가".repeat(201));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(longName, null, null, false, null));

        GoodsManageVO nullType = validProduct(714L, "ETC");
        nullType.setProductType(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(nullType, null, null, false, null));

        GoodsManageVO blankType = validProduct(715L, "ETC");
        blankType.setProductType("   ");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(blankType, null, null, false, null));

        GoodsManageVO invalidType = validProduct(716L, "ETC");
        invalidType.setProductType("UNKNOWN");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(invalidType, null, null, false, null));

        GoodsManageVO invalidPrice = validProduct(717L, "ETC");
        invalidPrice.setPrice(0L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(invalidPrice, null, null, false, null));

        GoodsManageVO negativeDiscount = validProduct(718L, "ETC");
        negativeDiscount.setDiscountRate(-1);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(negativeDiscount, null, null, false, null));

        GoodsManageVO excessiveDiscount = validProduct(719L, "ETC");
        excessiveDiscount.setDiscountRate(101);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(excessiveDiscount, null, null, false, null));

        GoodsManageVO invalidStock = validProduct(720L, "ETC");
        invalidStock.setStock(-1);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.updateProduct(invalidStock, null, null, false, null));

        GoodsManageVO normalized = validProduct(721L, " goods ");
        normalized.setDescription("   ");
        when(businessDAO.updateProduct(normalized)).thenReturn(1);

        service.updateProduct(normalized, null, null, false, null);

        assertEquals("GOODS", normalized.getProductType());
        assertEquals("테스트 상품", normalized.getProductName());
        assertNull(normalized.getDescription());
    }

    @Test
    void registerProductShouldRejectInvalidMainImageConditions() {
        GoodsManageVO product = newProductForRegistration();

        MultipartFile emptyImage = new MockMultipartFile(
                "productImage",
                "empty.png",
                "image/png",
                new byte[0]);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(product, emptyImage, null));

        GoodsManageVO noExtensionProduct = newProductForRegistration();
        MultipartFile noExtension = image("image", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(noExtensionProduct, noExtension, null));

        GoodsManageVO invalidExtensionProduct = newProductForRegistration();
        MultipartFile invalidExtension = image("image.exe", "image/png");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(invalidExtensionProduct, invalidExtension, null));

        GoodsManageVO invalidContentTypeProduct = newProductForRegistration();
        MultipartFile invalidContentType = image("image.png", "text/plain");
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(invalidContentTypeProduct, invalidContentType, null));

        GoodsManageVO oversizedProduct = newProductForRegistration();
        MultipartFile oversizedImage = mock(MultipartFile.class);
        when(oversizedImage.isEmpty()).thenReturn(false);
        when(oversizedImage.getSize()).thenReturn(10L * 1024L * 1024L + 1L);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(oversizedProduct, oversizedImage, null));
    }

    @Test
    void registerProductShouldRejectMoreThanTenSelectedDetailImages() {
        MultipartFile[] detailImages = new MultipartFile[12];
        detailImages[0] = null;
        detailImages[1] = new MockMultipartFile(
                "detailImages",
                "empty.png",
                "image/png",
                new byte[0]);

        for (int index = 2; index < detailImages.length; index++) {
            detailImages[index] = image("detail-" + index + ".png", "image/png");
        }

        // 실제 선택 파일은 10장이므로 허용 범위입니다. 콘텐츠 검증까지 진행되는지 확인합니다.
        GoodsManageVO tenImages = newProductForRegistration();
        MultipartFile tenImagesMainImage = image("main.png", "image/png");
        when(businessDAO.selectContentByNo(100L)).thenReturn(null);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(
                        tenImages,
                        tenImagesMainImage,
                        detailImages));

        MultipartFile[] elevenImages = new MultipartFile[11];
        for (int index = 0; index < elevenImages.length; index++) {
            elevenImages[index] = image("too-many-" + index + ".jpg", "image/jpeg");
        }

        GoodsManageVO elevenImagesProduct = newProductForRegistration();
        MultipartFile elevenImagesMainImage = image("main.jpg", "image/jpeg");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registerProduct(
                        elevenImagesProduct,
                        elevenImagesMainImage,
                        elevenImages));
    }

    private GoodsManageVO validProduct(long productNo, String productType) {
        GoodsManageVO product = new GoodsManageVO();
        product.setProductNo(productNo);
        product.setBusinessNo(10L);
        product.setProductName(" 테스트 상품 ");
        product.setProductType(productType);
        product.setPrice(10000L);
        product.setDiscountRate(10);
        product.setStock(5);
        product.setDescription(" 설명 ");
        return product;
    }

    private GoodsManageVO newProductForRegistration() {
        GoodsManageVO product = validProduct(0L, "ETC");
        product.setContentNo(100L);
        return product;
    }

    private GoodsManageVO approvedExistingProduct() {
        GoodsManageVO existing = new GoodsManageVO();
        existing.setStatus("APPROVED");
        return existing;
    }

    private MockMultipartFile image(String filename, String contentType) {
        return new MockMultipartFile(
                "image",
                filename,
                contentType,
                "image-data".getBytes(StandardCharsets.UTF_8));
    }

    private void assertDirectoryHasNoFiles(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }

        try (var files = Files.list(directory)) {
            assertEquals(0L, files.count());
        }
    }
}
