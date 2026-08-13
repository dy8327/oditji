package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 사업자 서비스의 문자열·날짜·이미지 경로 helper 잔여 조건을 보완합니다. */
class BusinessServiceImplFinalHelperCoverageTest {

    @TempDir
    Path tempDir;

    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BusinessServiceImpl(
                mock(BusinessDAO.class),
                mock(ContentService.class),
                mock(SearchContentStore.class),
                mock(TmdbService.class),
                mock(NotificationService.class),
                tempDir.resolve("product").toString(),
                tempDir.resolve("event").toString());
    }

    @Test
    void salesSearchValidationShouldCoverBusinessDateNullAndReverseRange() {
        LocalDate start = LocalDate.of(2026, Month.AUGUST, 1);
        LocalDate end = LocalDate.of(2026, Month.AUGUST, 11);
        assertThrows(IllegalArgumentException.class, () -> invoke("validateSalesSearchCondition", 0L, start, end));
        assertThrows(IllegalArgumentException.class, () -> invoke("validateSalesSearchCondition", 1L, null, end));
        assertThrows(IllegalArgumentException.class, () -> invoke("validateSalesSearchCondition", 1L, start, null));
        assertThrows(IllegalArgumentException.class, () -> invoke("validateSalesSearchCondition", 1L, end, start));
        invoke("validateSalesSearchCondition", 1L, start, end);
    }

    @Test
    void normalizeHelpersShouldCoverNullBlankUnsupportedAndAllowedStatuses() {
        assertNull(invoke("normalizeKeyword", (Object) null));
        assertNull(invoke("normalizeKeyword", "  "));
        assertEquals("abc", invoke("normalizeKeyword", " abc "));

        assertNull(invoke("normalizeDeliveryStatusFilter", (Object) null));
        assertNull(invoke("normalizeDeliveryStatusFilter", "ALL"));
        assertThrows(IllegalArgumentException.class, () -> invoke("normalizeDeliveryStatusFilter", "UNKNOWN"));
        assertEquals("SHIPPING", invoke("normalizeDeliveryStatusFilter", " shipping "));

        assertThrows(IllegalArgumentException.class, () -> invoke("normalizeDeliveryUpdateStatus", (Object) null));
        assertThrows(IllegalArgumentException.class, () -> invoke("normalizeDeliveryUpdateStatus", "UNKNOWN"));
        assertEquals("DELIVERED", invoke("normalizeDeliveryUpdateStatus", " delivered "));
    }

    @Test
    void optionFileAndPhysicalPathHelpersShouldCoverCaseAndTraversalGuards() {
        assertFalse(booleanValue(invoke("isOptionProduct", (Object) null)));
        assertFalse(booleanValue(invoke("isOptionProduct", " clothes ")));
        assertTrue(booleanValue(invoke("isOptionProduct", "CLOTHES")));
        assertTrue(booleanValue(invoke("isOptionProduct", "SHOES")));
        assertFalse(booleanValue(invoke("isOptionProduct", "GOODS")));

        assertEquals("", invoke("getFileExtension", "filename"));
        assertEquals("", invoke("getFileExtension", "filename."));
        assertEquals("jpg", invoke("getFileExtension", "IMAGE.JPG"));

        assertNull(invoke("resolveProductImagePhysicalPath", (Object) null));
        assertNull(invoke("resolveProductImagePhysicalPath", " "));
        assertNull(invoke("resolveProductImagePhysicalPath", "/other/file.jpg"));
        assertNull(invoke("resolveProductImagePhysicalPath", "/uploads/product/"));
        assertNull(invoke("resolveProductImagePhysicalPath", "/uploads/product/sub/file.jpg"));
        Path physical = invoke("resolveProductImagePhysicalPath", "/uploads/product/file.jpg");
        assertTrue(physical.endsWith("file.jpg"));
    }

    @Test
    void selectedDetailImageCounterShouldIgnoreNullAndEmptyFiles() {
        MockMultipartFile empty = new MockMultipartFile("detail", "", "image/png", new byte[0]);
        MockMultipartFile selected = new MockMultipartFile("detail", "a.png", "image/png", new byte[] {1});
        assertEquals(0, ((Integer) invoke("countSelectedDetailImages", (Object) null)).intValue());
        assertEquals(
                1,
                ((Integer) invoke(
                        "countSelectedDetailImages",
                        (Object) new MockMultipartFile[] {null, empty, selected})).intValue());
    }

    @Test
    void productImageValidatorShouldCoverFilenameExtensionAndContentTypeConditions() {
        MockMultipartFile missingName = new MockMultipartFile("image", "", "image/png", new byte[] {1});
        MockMultipartFile wrongExtension = new MockMultipartFile("image", "a.exe", "image/png", new byte[] {1});
        MockMultipartFile wrongMime = new MockMultipartFile("image", "a.jpg", "text/plain", new byte[] {1});
        MockMultipartFile valid = new MockMultipartFile("image", "a.webp", null, new byte[] {1});

        assertThrows(IllegalArgumentException.class, () -> invoke("validateProductImage", missingName));
        assertThrows(IllegalArgumentException.class, () -> invoke("validateProductImage", wrongExtension));
        assertThrows(IllegalArgumentException.class, () -> invoke("validateProductImage", wrongMime));
        invoke("validateProductImage", valid);
    }


    private boolean booleanValue(Object value) {
        return Boolean.TRUE.equals(value);
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String method, Object... args) {
        return (T) ReflectionTestUtils.invokeMethod(service, method, args);
    }
}
