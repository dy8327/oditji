package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** SonarQube에 남은 BusinessServiceImpl의 실제 진입 가능한 분기를 보완합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessServiceImplResidualClosure8Test {

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

    @BeforeEach
    void setUp() {
        service = new BusinessServiceImpl(
                businessDAO,
                contentService,
                searchContentStore,
                tmdbService,
                notificationService,
                tempDirectory.resolve("product").toString(),
                tempDirectory.resolve("event").toString());
    }

    @Test
    void publicListAndSettlementMethodsShouldRejectNonPositiveBusinessNumbers() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBusinessOrderList(0L, 1, 10));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestSettlementConfirmation(0L));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getBusinessDeliveryListCount(0L, null, null));
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getProductListByBusinessNo(
                        0L,
                        null,
                        null,
                        null,
                        null,
                        1,
                        10));
    }

    @Test
    void deliveryShouldCoverAlreadyDeliveredShortCircuitWithoutRefreshingSettlement() {
        DeliveryManageVO currentItem = new DeliveryManageVO();
        currentItem.setOrderNo(700L);
        currentItem.setOrderItemNo(701L);
        currentItem.setStatus("DELIVERED");

        when(businessDAO.selectBusinessDeliveryItem(10L, 701L))
                .thenReturn(currentItem);
        when(businessDAO.mergeDelivery(any(DeliveryManageVO.class)))
                .thenReturn(1);
        when(businessDAO.updateOrderItemDeliveryStatus(10L, 701L, "DELIVERED"))
                .thenReturn(1);

        service.updateBusinessDelivery(
                10L,
                701L,
                "CJ대한통운",
                "1234567890",
                "DELIVERED");

        verify(businessDAO, never())
                .refreshPreRequestedSettlementAmount(10L);
    }

    @Test
    void legacyOptionValidationShouldCoverEmptyOptionListOperand() {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setProductNo(10L);
        goods.setProductType("CLOTHES");
        goods.setOptionList(Collections.emptyList());

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("validateAndInsertProductOptions", goods));
    }

    @Test
    void productDeleteShouldCoverNullLongReasonAndMissingProduct() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestProductDelete(10L, 20L, null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestProductDelete(
                        10L,
                        20L,
                        "가".repeat(1001)));

        when(businessDAO.selectProductForUpdate(10L, 20L))
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.requestProductDelete(
                        10L,
                        20L,
                        "정상 길이의 삭제 요청 사유"));
    }

    @Test
    void registerEventShouldCoverEmptyImageOperand() {
        EventManageVO event = validEvent();
        MockMultipartFile emptyImage = new MockMultipartFile(
                "eventImage",
                "empty.png",
                "image/png",
                new byte[0]);

        when(businessDAO.countProductByBusinessNo(1L, 10L, null))
                .thenReturn(1);
        when(businessDAO.insertEvent(event))
                .thenAnswer(invocation -> {
                    EventManageVO target = invocation.getArgument(0);
                    target.setEventNo(900L);
                    return 1;
                });
        when(businessDAO.insertEventProduct(900L, 1L, 10))
                .thenReturn(1);

        assertEquals(900L, service.registerEvent(event, emptyImage));
        assertNull(event.getBannerImage());
    }

    @Test
    void registerEventShouldRejectZeroGeneratedEventNumber() {
        EventManageVO event = validEvent();

        when(businessDAO.countProductByBusinessNo(1L, 10L, null))
                .thenReturn(1);
        when(businessDAO.insertEvent(event))
                .thenAnswer(invocation -> {
                    EventManageVO target = invocation.getArgument(0);
                    target.setEventNo(0L);
                    return 1;
                });

        assertThrows(
                IllegalStateException.class,
                () -> service.registerEvent(event, null));
    }

    @Test
    void updateApprovedEventShouldCoverEmptyImageOperand() {
        EventManageVO existingEvent = validEvent();
        existingEvent.setEventNo(1000L);
        existingEvent.setTitle("기존 이벤트");
        existingEvent.setBannerImage("/uploads/event/old.png");

        EventManageVO updateEvent = validEvent();
        updateEvent.setEventNo(1000L);

        MockMultipartFile emptyImage = new MockMultipartFile(
                "eventImage",
                "empty.png",
                "image/png",
                new byte[0]);

        when(businessDAO.selectApprovedEventForBusiness(1000L, 10L))
                .thenReturn(existingEvent);
        when(businessDAO.selectEventProductListByEventNo(1000L))
                .thenReturn(Collections.emptyList());
        when(businessDAO.countProductByBusinessNo(1L, 10L, 1000L))
                .thenReturn(1);
        when(businessDAO.updateApprovedEvent(updateEvent))
                .thenReturn(1);
        when(businessDAO.deleteEventProductByEventNo(1000L))
                .thenReturn(1);
        when(businessDAO.insertEventProduct(1000L, 1L, 10))
                .thenReturn(1);

        assertDoesNotThrow(
                () -> service.updateApprovedEvent(updateEvent, emptyImage));
        assertEquals("/uploads/event/old.png", updateEvent.getBannerImage());
    }

    @Test
    void extendEventShouldCoverNullAndOverlongReasons() {
        EventManageVO existingEvent = validEvent();
        existingEvent.setEventNo(1100L);
        existingEvent.setEndDate(LocalDate.of(2026, Month.AUGUST, 31));

        when(businessDAO.selectApprovedEventForBusiness(1100L, 10L))
                .thenReturn(existingEvent);
        when(businessDAO.selectEventProductListByEventNo(1100L))
                .thenReturn(Collections.emptyList());

        LocalDate extendedEndDate = LocalDate.of(2026, Month.SEPTEMBER, 30);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.extendApprovedEvent(
                        1100L,
                        10L,
                        extendedEndDate,
                        null));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.extendApprovedEvent(
                        1100L,
                        10L,
                        extendedEndDate,
                        "연".repeat(1001)));
    }

    @Test
    void imageSaveHelpersShouldCoverNullOriginalFilenames() {
        MultipartFile eventImage = org.mockito.Mockito.mock(MultipartFile.class);
        MultipartFile productImage = org.mockito.Mockito.mock(MultipartFile.class);

        when(eventImage.getOriginalFilename()).thenReturn(null);
        when(productImage.getOriginalFilename()).thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("saveEventImage", eventImage));
        assertThrows(
                IllegalArgumentException.class,
                () -> invoke("saveProductImage", productImage));
    }

    @Test
    void productValidationShouldCoverNullDescription() {
        GoodsManageVO goods = new GoodsManageVO();
        goods.setBusinessNo(10L);
        goods.setProductName("상품");
        goods.setProductType("ETC");
        goods.setPrice(10000L);
        goods.setDiscountRate(10);
        goods.setStock(1);
        goods.setDescription(null);

        assertDoesNotThrow(() -> invoke("validateProduct", goods));
        assertNull(goods.getDescription());
    }

    @Test
    void deletionHelpersShouldCoverEmptyListAndIOExceptionPath() throws Exception {
        assertDoesNotThrow(
                () -> invoke(
                        "registerProductImageFilesForDeletionAfterCommit",
                        (Object) null));
        assertDoesNotThrow(
                () -> invoke(
                        "registerProductImageFilesForDeletionAfterCommit",
                        Collections.emptyList()));

        Path nonEmptyDirectory = Files.createDirectory(
                tempDirectory.resolve("cannot-delete-directly"));
        Files.writeString(
                nonEmptyDirectory.resolve("child.txt"),
                "child");

        assertDoesNotThrow(
                () -> invoke(
                        "deleteSavedFileQuietly",
                        nonEmptyDirectory));
        assertTrue(Files.exists(nonEmptyDirectory));
    }

    private EventManageVO validEvent() {
        EventManageVO event = new EventManageVO();
        event.setBusinessNo(10L);
        event.setTitle("테스트 이벤트");
        event.setDescription("설명");
        event.setStartDate(LocalDate.of(2026, Month.AUGUST, 20));
        event.setEndDate(LocalDate.of(2026, Month.AUGUST, 31));
        event.setStatus("APPROVED");
        event.setProductNoList(List.of(1L));
        event.setDiscountRateList(List.of(10));
        return event;
    }

    private Object invoke(String methodName, Object... arguments) {
        return ReflectionTestUtils.invokeMethod(
                service,
                methodName,
                arguments);
    }
}
