package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;

/**
 * BusinessServiceImpl의 배송/검색/파일 helper 단축평가를 집중 보완합니다.
 */
class BusinessServiceImplHelperConditionCoverageTest {

    private BusinessServiceImpl service;

    @BeforeEach
    void setUp() {
        SearchContentStore searchContentStore =
                mock(SearchContentStore.class);

        service = new BusinessServiceImpl(
                mock(BusinessDAO.class),
                mock(ContentService.class),
                searchContentStore,
                mock(TmdbService.class),
                mock(NotificationService.class),
                "uploads/product",
                "uploads/event");
    }

    @Test
    void keywordNormalizerShouldCoverNullBlankAndText() {
        assertNull(
                ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeKeyword",
                        (Object) null));

        assertNull(
                ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeKeyword",
                        "   "));

        assertEquals(
                "keyword",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "normalizeKeyword",
                        " keyword "));
    }

    @Test
    void deliveryRequestNumbersAndTrackingRequirementsShouldCoverBothOperands() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateDeliveryRequestNumbers",
                        0L,
                        1L));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateDeliveryRequestNumbers",
                        1L,
                        0L));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateDeliveryDetails",
                        "SHIPPING",
                        null,
                        "123"));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateDeliveryDetails",
                        "SHIPPING",
                        "CJ",
                        null));

        ReflectionTestUtils.invokeMethod(
                service,
                "validateDeliveryDetails",
                "PREPARING",
                null,
                null);
    }

    @Test
    void deliveryChangeAndTransitionShouldCoverEveryBlockedStatusAndUnknownSteps() {
        for (String status :
                new String[] {
                        "CANCEL_REQUEST",
                        "CANCELED",
                        "REFUNDED"
                }) {

            assertThrows(
                    IllegalStateException.class,
                    () -> ReflectionTestUtils.invokeMethod(
                            service,
                            "validateDeliveryChangeAllowed",
                            status));
        }

        ReflectionTestUtils.invokeMethod(
                service,
                "validateDeliveryStatusTransition",
                "UNKNOWN",
                "SHIPPING");

        ReflectionTestUtils.invokeMethod(
                service,
                "validateDeliveryStatusTransition",
                "PAID",
                "UNKNOWN");

        assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateDeliveryStatusTransition",
                        "DELIVERED",
                        "SHIPPING"));
    }

    @Test
    void cachedContentValidationShouldCoverEachInvalidArgumentAndUnsupportedType() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateCachedContentSelection",
                        null,
                        "MOVIE"));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateCachedContentSelection",
                        0L,
                        "MOVIE"));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateCachedContentSelection",
                        1L,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateCachedContentSelection",
                        1L,
                        " "));

        assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionTestUtils.invokeMethod(
                        service,
                        "validateCachedContentSelection",
                        1L,
                        "DOCUMENTARY"));
    }

    @Test
    void cachedContentMatcherShouldCoverTitleOriginalSearchTextAndNullValue() {
        CachedContentVO content =
                new CachedContentVO();

        content.setTitle("Movie Title");
        content.setOriginalTitle("Original");
        content.setSearchText("Actor Name");

        assertEquals(
                Boolean.TRUE,
                ReflectionTestUtils.invokeMethod(
                        service,
                        "matchesCachedContent",
                        content,
                        "movie"));

        assertEquals(
                Boolean.TRUE,
                ReflectionTestUtils.invokeMethod(
                        service,
                        "matchesCachedContent",
                        content,
                        "original"));

        assertEquals(
                Boolean.TRUE,
                ReflectionTestUtils.invokeMethod(
                        service,
                        "matchesCachedContent",
                        content,
                        "actor"));

        content.setTitle(null);
        content.setOriginalTitle(null);
        content.setSearchText(null);

        assertEquals(
                Boolean.FALSE,
                ReflectionTestUtils.invokeMethod(
                        service,
                        "matchesCachedContent",
                        content,
                        "missing"));
    }

    @Test
    void fileHelpersShouldCoverMissingDotTrailingDotDetailSelectionAndInvalidPaths() {
        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getFileExtension",
                        "filename"));

        assertEquals(
                "",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getFileExtension",
                        "filename."));

        assertEquals(
                "png",
                ReflectionTestUtils.invokeMethod(
                        service,
                        "getFileExtension",
                        "FILE.PNG"));

        assertEquals(
                0,
                ((Integer)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "countSelectedDetailImages",
                                (Object) null))
                        .intValue());

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

        assertEquals(
                1,
                ((Integer)
                        ReflectionTestUtils.invokeMethod(
                                service,
                                "countSelectedDetailImages",
                                (Object)
                                        new org.springframework.web.multipart.MultipartFile[] {
                                                null,
                                                empty,
                                                selected
                                        }))
                        .intValue());

        assertNull(resolvePath(null));
        assertNull(resolvePath(" "));
        assertNull(resolvePath("/uploads/event/a.png"));
        assertNull(resolvePath("/uploads/product/"));
        assertNull(resolvePath("/uploads/product/sub/a.png"));

        Path valid =
                resolvePath(
                        "/uploads/product/a.png");

        assertNotNull(valid);
        assertEquals(
                "a.png",
                valid.getFileName()
                        .toString());
    }

    private Path resolvePath(String value) {
        return ReflectionTestUtils.invokeMethod(
                service,
                "resolveProductImagePhysicalPath",
                value);
    }
}