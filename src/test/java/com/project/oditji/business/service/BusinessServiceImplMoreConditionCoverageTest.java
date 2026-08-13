package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/**
 * BusinessServiceImpl의 이벤트/상품 검증 OR 조건을 추가로 보완합니다.
 */
class BusinessServiceImplMoreConditionCoverageTest {

    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BusinessServiceImpl(
                mock(BusinessDAO.class),
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                mock(NotificationService.class),
                "uploads/product",
                "uploads/event");
    }

    @Test
    void eventProductListsShouldCoverNullEmptyMismatchedAndValidLists() {
        // [SonarQube] assertThrows 람다 안에는 실제 검증 대상 호출만 남깁니다.
        List<Long> emptyProductNoList = List.of();
        List<Integer> emptyDiscountRateList = List.of();
        List<Long> singleProductNoList = List.of(1L);
        List<Long> multipleProductNoList = List.of(1L, 2L);
        List<Integer> singleDiscountRateList = List.of(10);
        List<Integer> multipleDiscountRateList = List.of(10, 20);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProductLists",
                        null,
                        emptyDiscountRateList));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProductLists",
                        emptyProductNoList,
                        emptyDiscountRateList));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProductLists",
                        singleProductNoList,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProductLists",
                        multipleProductNoList,
                        singleDiscountRateList));

        assertDoesNotThrow(
                () -> invoke(
                        "validateEventProductLists",
                        multipleProductNoList,
                        multipleDiscountRateList));
    }

    @Test
    void eventProductValidatorShouldCoverNullZeroNegativeAndOverHundredDiscounts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProduct",
                        null,
                        10,
                        1L,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProduct",
                        0L,
                        10,
                        1L,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProduct",
                        1L,
                        null,
                        1L,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProduct",
                        1L,
                        -1,
                        1L,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventProduct",
                        1L,
                        101,
                        1L,
                        null));
    }

    @Test
    void eventImageValidationShouldCoverNullBlankFilenameSizeExtensionAndContentType() {
        MockMultipartFile blankName =
                new MockMultipartFile(
                        "eventImage",
                        "",
                        "image/png",
                        new byte[] { 1 });

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventImage",
                        blankName));

        MockMultipartFile badExtension =
                new MockMultipartFile(
                        "eventImage",
                        "event.txt",
                        "text/plain",
                        new byte[] { 1 });

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventImage",
                        badExtension));

        MockMultipartFile badContentType =
                new MockMultipartFile(
                        "eventImage",
                        "event.png",
                        "text/plain",
                        new byte[] { 1 });

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateEventImage",
                        badContentType));
    }

    @Test
    void productImageValidationShouldCoverNullEmptyBlankFilenameAndNonImageContentType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateProductImage",
                        (Object) null));

        MockMultipartFile empty =
                new MockMultipartFile(
                        "image",
                        "a.png",
                        "image/png",
                        new byte[0]);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateProductImage",
                        empty));

        MockMultipartFile blankName =
                new MockMultipartFile(
                        "image",
                        "",
                        "image/png",
                        new byte[] { 1 });

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateProductImage",
                        blankName));

        MockMultipartFile badContentType =
                new MockMultipartFile(
                        "image",
                        "a.png",
                        "text/plain",
                        new byte[] { 1 });

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke(
                        "validateProductImage",
                        badContentType));
    }

    @Test
    void detailImageCounterShouldIgnoreNullAndEmptyEntries() {
        MockMultipartFile empty =
                new MockMultipartFile(
                        "detail",
                        new byte[0]);

        MockMultipartFile selected =
                new MockMultipartFile(
                        "detail",
                        "a.png",
                        "image/png",
                        new byte[] { 1 });

        Integer count =
                ReflectionTestUtils.invokeMethod(
                        service,
                        "countSelectedDetailImages",
                        (Object)
                                new org.springframework.web.multipart.MultipartFile[] {
                                        null,
                                        empty,
                                        selected
                                });

        assertEquals(1, count.intValue());

        assertNull(
                ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveProductImagePhysicalPath",
                        "/uploads/product/sub/a.png"));
    }

    private void invoke(
            String methodName,
            Object... arguments) {

        ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
