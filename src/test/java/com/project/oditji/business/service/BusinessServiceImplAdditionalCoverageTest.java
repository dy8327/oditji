package com.project.oditji.business.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.EventProductVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.goods.vo.ProductOptionVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.tmdb.service.TmdbService;

/** 배송·상품 목록·이벤트 처리의 서비스 핵심 분기를 추가로 검증합니다. */
@ExtendWith(MockitoExtension.class)
class BusinessServiceImplAdditionalCoverageTest {

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
        void deliveryListShouldNormalizeFiltersAndReturnSafeCollections() {
                List<DeliveryManageVO> deliveries = List.of(new DeliveryManageVO());
                when(businessDAO.selectBusinessDeliveryList(
                                10L,
                                "SHIPPING",
                                "상품",
                                5,
                                5)).thenReturn(deliveries);
                when(businessDAO.selectBusinessDeliveryListCount(
                                10L,
                                null,
                                null)).thenReturn(7);

                assertSame(
                                deliveries,
                                service.getBusinessDeliveryList(
                                                10L,
                                                " shipping ",
                                                " 상품 ",
                                                2,
                                                5));
                assertEquals(
                                7,
                                service.getBusinessDeliveryListCount(
                                                10L,
                                                "ALL",
                                                " "));

                when(businessDAO.selectBusinessDeliveryList(
                                11L,
                                null,
                                null,
                                0,
                                5)).thenReturn(null);
                assertTrue(service.getBusinessDeliveryList(
                                11L,
                                null,
                                null,
                                1,
                                5).isEmpty());

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.getBusinessDeliveryList(
                                                0L,
                                                null,
                                                null,
                                                1,
                                                5));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.getBusinessDeliveryListCount(
                                                10L,
                                                "INVALID",
                                                null));
        }

        @Test
        void deliveryUpdateShouldSaveStatusAndNotifyMember() {
                DeliveryManageVO current = deliveryItem(
                                "PREPARING",
                                100L,
                                200L,
                                300L,
                                "테스트 상품");
                when(businessDAO.selectBusinessDeliveryItem(10L, 200L))
                                .thenReturn(current);
                when(businessDAO.mergeDelivery(any(DeliveryManageVO.class)))
                                .thenReturn(1);
                when(businessDAO.updateOrderItemDeliveryStatus(
                                10L,
                                200L,
                                "SHIPPING")).thenReturn(1);

                service.updateBusinessDelivery(
                                10L,
                                200L,
                                " CJ대한통운 ",
                                " 1234567890 ",
                                " shipping ");

                ArgumentCaptor<DeliveryManageVO> deliveryCaptor = ArgumentCaptor.forClass(DeliveryManageVO.class);
                verify(businessDAO).mergeDelivery(deliveryCaptor.capture());
                DeliveryManageVO saved = deliveryCaptor.getValue();
                assertEquals(10L, saved.getBusinessNo());
                assertEquals(200L, saved.getOrderItemNo());
                assertEquals(100L, saved.getOrderNo());
                assertEquals("CJ대한통운", saved.getCourier());
                assertEquals("1234567890", saved.getTrackingNumber());
                assertEquals("SHIPPING", saved.getStatus());
                verify(businessDAO).updateOrderStatusByOrderItem(100L);
                verify(notificationService).createForMember(
                                300L,
                                "DELIVERY_SHIPPED",
                                "상품 발송",
                                "주문하신 상품이 발송되었습니다. 상품명: 테스트 상품",
                                "/order/list",
                                "ORDER_ITEM",
                                200L);
        }

        @Test
        void deliveryUpdateShouldRejectInvalidOwnershipStateAndTransition() {
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.updateBusinessDelivery(
                                                0L,
                                                1L,
                                                null,
                                                null,
                                                "PREPARING"));

                when(businessDAO.selectBusinessDeliveryItem(10L, 1L))
                                .thenReturn(null);
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.updateBusinessDelivery(
                                                10L,
                                                1L,
                                                null,
                                                null,
                                                "PREPARING"));

                when(businessDAO.selectBusinessDeliveryItem(10L, 2L))
                                .thenReturn(deliveryItem(
                                                "CANCELED",
                                                100L,
                                                2L,
                                                300L,
                                                null));
                assertThrows(
                                IllegalStateException.class,
                                () -> service.updateBusinessDelivery(
                                                10L,
                                                2L,
                                                null,
                                                null,
                                                "PREPARING"));

                when(businessDAO.selectBusinessDeliveryItem(10L, 3L))
                                .thenReturn(deliveryItem(
                                                "DELIVERED",
                                                100L,
                                                3L,
                                                300L,
                                                null));
                assertThrows(
                                IllegalStateException.class,
                                () -> service.updateBusinessDelivery(
                                                10L,
                                                3L,
                                                "택배",
                                                "123",
                                                "SHIPPING"));

                when(businessDAO.selectBusinessDeliveryItem(10L, 4L))
                                .thenReturn(deliveryItem(
                                                "PREPARING",
                                                100L,
                                                4L,
                                                300L,
                                                null));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.updateBusinessDelivery(
                                                10L,
                                                4L,
                                                null,
                                                "123",
                                                "SHIPPING"));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.updateBusinessDelivery(
                                                10L,
                                                4L,
                                                "택배",
                                                null,
                                                "SHIPPING"));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.updateBusinessDelivery(
                                                10L,
                                                4L,
                                                "택배",
                                                "123",
                                                "UNKNOWN"));
        }

        @Test
        void productListShouldLoadOptionsOnlyForClothesAndShoes() {
                GoodsManageVO clothes = product(1L, "CLOTHES");
                GoodsManageVO shoes = product(2L, "SHOES");
                GoodsManageVO goods = product(3L, "GOODS");
                List<GoodsManageVO> products = List.of(clothes, shoes, goods);
                List<ProductOptionVO> clothesOptions = List.of(new ProductOptionVO());
                List<ProductOptionVO> shoesOptions = List.of(new ProductOptionVO());

                when(businessDAO.selectProductListByBusinessNo(
                                10L,
                                "검색",
                                null,
                                null,
                                null,
                                0,
                                5)).thenReturn(products);
                when(businessDAO.selectProductOptionsByProductNo(1L))
                                .thenReturn(clothesOptions);
                when(businessDAO.selectProductOptionsByProductNo(2L))
                                .thenReturn(shoesOptions);
                when(businessDAO.selectProductListCountByBusinessNo(10L, null, null, null, null))
                                .thenReturn(3);

                assertSame(
                                products,
                                service.getProductListByBusinessNo(
                                                10L,
                                                " 검색 ",
                                                null,
                                                null,
                                                null,
                                                1,
                                                5));
                assertSame(clothesOptions, clothes.getOptionList());
                assertSame(shoesOptions, shoes.getOptionList());
                verify(businessDAO, never()).selectProductOptionsByProductNo(3L);
                assertEquals(
                                3,
                                service.getProductListCountByBusinessNo(10L, " ", null, null, null));

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.getProductForUpdate(0L, 10L));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.getProductForUpdate(1L, 0L));
                when(businessDAO.selectProductForUpdate(1L, 10L)).thenReturn(goods);
                assertSame(goods, service.getProductForUpdate(1L, 10L));
                when(businessDAO.selectProductForUpdate(2L, 10L)).thenReturn(null);
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.getProductForUpdate(2L, 10L));
        }

        @Test
        void productDeleteRequestShouldValidateStateAndNotifyAdmins() {
                GoodsManageVO product = product(5L, "GOODS");
                product.setProductName("삭제 대상");
                product.setStatus("APPROVED");
                when(businessDAO.selectProductForUpdate(5L, 10L))
                                .thenReturn(product);
                when(businessDAO.updateProductDeleteRequest(5L, 10L))
                                .thenReturn(1);

                service.requestProductDelete(5L, 10L, " 판매 종료 ");

                verify(notificationService).createForAdmins(
                                "PRODUCT_DELETE_REQUEST",
                                "상품 삭제 승인 요청",
                                "삭제 대상 상품의 삭제 요청이 접수되었습니다.",
                                "/admin/product/list?tab=delete",
                                "PRODUCT",
                                5L);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.requestProductDelete(0L, 10L, "사유"));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.requestProductDelete(5L, 0L, "사유"));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.requestProductDelete(5L, 10L, " "));

                product.setStatus("DELETE_REQUESTED");
                assertThrows(
                                IllegalStateException.class,
                                () -> service.requestProductDelete(5L, 10L, "사유"));
        }

        @Test
        void eventRegisterShouldValidateInsertRelationsAndNotifyAdmins() {
                EventManageVO event = validEvent();
                when(businessDAO.countProductByBusinessNo(1L, 10L, null))
                                .thenReturn(1);
                when(businessDAO.countProductByBusinessNo(2L, 10L, null))
                                .thenReturn(1);
                when(businessDAO.insertEvent(event)).thenAnswer(invocation -> {
                        event.setEventNo(100L);
                        return 1;
                });
                when(businessDAO.insertEventProduct(100L, 1L, 10))
                                .thenReturn(1);
                when(businessDAO.insertEventProduct(100L, 2L, 20))
                                .thenReturn(1);

                assertEquals(100L, service.registerEvent(event, null));
                assertEquals("WAITING", event.getStatus());
                assertEquals("여름 이벤트", event.getTitle());
                verify(notificationService).createForAdmins(
                                "EVENT_REQUEST",
                                "이벤트 승인 요청",
                                "여름 이벤트 이벤트의 등록 승인 요청이 접수되었습니다.",
                                "/admin/event/list?tab=waiting",
                                "EVENT",
                                100L);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.registerEvent(null, null));

                EventManageVO duplicate = validEvent();
                duplicate.setProductNoList(List.of(1L, 1L));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.registerEvent(duplicate, null));

                EventManageVO invalidRate = validEvent();
                invalidRate.setDiscountRateList(Arrays.asList(10, null));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.registerEvent(invalidRate, null));
        }

        @Test
        void eventQueriesAndExtensionShouldCoverSuccessAndValidation() {
                EventManageVO event = validEvent();
                event.setEventNo(200L);
                event.setTitle("승인 이벤트");
                LocalDate originalEndDate = LocalDate.of(2026, Month.AUGUST, 31);
                LocalDate extendedEndDate = LocalDate.of(2026, Month.SEPTEMBER, 30);
                event.setEndDate(originalEndDate);
                List<EventProductVO> connected = List.of(new EventProductVO());
                when(businessDAO.selectApprovedEventForBusiness(200L, 10L))
                                .thenReturn(event);
                when(businessDAO.selectEventProductListByEventNo(200L))
                                .thenReturn(connected);

                EventManageVO result = service.getApprovedEventForBusiness(200L, 10L);
                assertSame(event, result);
                assertEquals(10L, result.getBusinessNo());
                assertSame(connected, result.getConnectedProducts());

                when(businessDAO.extendApprovedEvent(
                                200L,
                                10L,
                                extendedEndDate)).thenReturn(1);
                service.extendApprovedEvent(
                                200L,
                                10L,
                                extendedEndDate,
                                " 기간 연장 ");
                verify(notificationService).createForAdmins(
                                "EVENT_REQUEST",
                                "이벤트 연장 승인 요청",
                                "승인 이벤트 이벤트의 연장 승인 요청이 접수되었습니다.",
                                "/admin/event/list?tab=waiting",
                                "EVENT",
                                200L);

                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.extendApprovedEvent(
                                                200L,
                                                10L,
                                                null,
                                                "사유"));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.extendApprovedEvent(
                                                200L,
                                                10L,
                                                originalEndDate,
                                                "사유"));
                assertThrows(
                                IllegalArgumentException.class,
                                () -> service.extendApprovedEvent(
                                                200L,
                                                10L,
                                                extendedEndDate,
                                                " "));

                when(businessDAO.selectEventListByBusinessNo(
                                10L,
                                null,
                                null,
                                null,
                                null,
                                0,
                                10)).thenReturn(null);
                assertTrue(service.getEventListByBusinessNo(
                                10L,
                                " ",
                                null,
                                null,
                                null,
                                1,
                                10).isEmpty());
                when(businessDAO.selectEventListCountByBusinessNo(10L, "검색", null, null, null))
                                .thenReturn(4);
                assertEquals(
                                4,
                                service.getEventListCountByBusinessNo(10L, " 검색 ", null, null, null));
        }

        private DeliveryManageVO deliveryItem(
                        String status,
                        Long orderNo,
                        Long orderItemNo,
                        Long memberNo,
                        String productName) {
                DeliveryManageVO item = new DeliveryManageVO();
                item.setStatus(status);
                item.setOrderNo(orderNo);
                item.setOrderItemNo(orderItemNo);
                item.setMemberNo(memberNo);
                item.setProductName(productName);
                return item;
        }

        private GoodsManageVO product(Long productNo, String type) {
                GoodsManageVO product = new GoodsManageVO();
                product.setProductNo(productNo);
                product.setProductType(type);
                return product;
        }

        private EventManageVO validEvent() {
                EventManageVO event = new EventManageVO();
                event.setBusinessNo(10L);
                event.setTitle(" 여름 이벤트 ");
                event.setDescription("설명");
                event.setStartDate(LocalDate.of(2026, Month.AUGUST, 10));
                event.setEndDate(LocalDate.of(2026, Month.AUGUST, 31));
                event.setStatus("APPROVED");
                event.setProductNoList(List.of(1L, 2L));
                event.setDiscountRateList(List.of(10, 20));
                return event;
        }
}