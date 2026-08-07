package com.project.oditji.business.service;

import java.io.IOException;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessDashboardVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.EventProductVO;
import com.project.oditji.business.vo.DeliveryManageVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.SettlementManageVO;
import com.project.oditji.order.vo.OrderItemVO;
import com.project.oditji.order.vo.OrderVO;
import com.project.oditji.notification.service.NotificationService;
import com.project.oditji.content.service.ContentService;
import com.project.oditji.search.service.SearchContentStore;
import com.project.oditji.search.vo.CachedContentVO;
import com.project.oditji.tmdb.service.TmdbService;
import com.project.oditji.tmdb.vo.ActorVO;
import com.project.oditji.common.vo.SettlementRequestVO;
import com.project.oditji.common.util.PaginationUtil;

@Service
public class BusinessServiceImpl
                implements BusinessService {

        private static final Logger log = LoggerFactory.getLogger(BusinessServiceImpl.class);

        private static final long MAX_IMAGE_SIZE = 10L * 1024L * 1024L;

        /* 마이페이지 대시보드에 노출할 인기 상품 개수 */
        private static final int POPULAR_PRODUCT_LIMIT = 5;

        /* JSONL 콘텐츠 검색 팝업에 한 번에 표시할 최대 건수 */
        private static final int CONTENT_SEARCH_LIMIT = 100;

        private static final String PRODUCT_TYPE_CLOTHES = "CLOTHES";
        private static final String PRODUCT_TYPE_SHOES = "SHOES";
        private static final String DELIVERY_STATUS_PREPARING = "PREPARING";
        private static final String DELIVERY_STATUS_SHIPPING = "SHIPPING";
        private static final String DELIVERY_STATUS_DELIVERED = "DELIVERED";
        private static final String STATUS_WAITING = "WAITING";
        private static final String REFERENCE_TYPE_PRODUCT = "PRODUCT";
        private static final String NOTIFICATION_TYPE_EVENT_REQUEST = "EVENT_REQUEST";
        private static final String ADMIN_EVENT_WAITING_URL = "/admin/event/list?tab=waiting";
        private static final String REFERENCE_TYPE_EVENT = "EVENT";

        private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

        private static final Set<String> ALLOWED_PRODUCT_TYPES = Set.of(PRODUCT_TYPE_CLOTHES, "PROP", "GOODS", "OST",
                        "BOOK",
                        "FIGURE", PRODUCT_TYPE_SHOES, "POSTER", "ETC");

        private final BusinessDAO businessDAO;
        private final ContentService contentService;
        private final SearchContentStore searchContentStore;
        private final TmdbService tmdbService;
        private final NotificationService notificationService;
        private final Path productUploadDirectory;

        /*
         * application.properties를 추가로 수정하지 않고
         * 기존 ODITJI 외부 업로드 폴더 구조를 그대로 사용한다.
         */
        private final Path eventUploadDirectory;

        public BusinessServiceImpl(
                        BusinessDAO businessDAO,
                        ContentService contentService,
                        SearchContentStore searchContentStore,
                        TmdbService tmdbService,
                        NotificationService notificationService,
                        @Value("${oditji.upload.product-path:"
                                        + "uploads/product}") String productUploadPath,
                        @Value("${oditji.upload.event-path:uploads/event}") String eventUploadPath) {

                this.businessDAO = businessDAO;
                this.contentService = contentService;
                this.searchContentStore = searchContentStore;
                this.tmdbService = tmdbService;
                this.notificationService = notificationService;

                this.productUploadDirectory = Paths.get(
                                productUploadPath)
                                .toAbsolutePath()
                                .normalize();
                this.eventUploadDirectory = Paths.get(eventUploadPath)
                                .toAbsolutePath()
                                .normalize();
        }

        /*
         * =========================================================
         * 로그인 회원과 연결된 사업자 조회
         * =========================================================
         */
        @Override
        public BusinessVO getBusinessByMemberNo(long memberNo) {

                if (memberNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 회원 번호입니다.");
                }

                return businessDAO.selectBusinessByMemberNo(memberNo);
        }

        /*
         * =========================================================
         * 마이페이지 대시보드 통계 조회
         *
         * 오늘 매출/판매량/구매 고객 수/클릭 수/지급 대기 정산액/
         * 승인 대기 상품 수와 인기 상품 TOP N을 한 번에 모아서 내려준다.
         *
         * [구매전환율 계산 기준 수정]
         * purchaseRate(구매전환율)는 별도 저장 컬럼이 없어
         * "오늘 정상 판매 수량 / 오늘 클릭 수 * 100"으로 계산한다.
         * 취소 요청 및 결제 취소 완료 상품은 Mapper 조회에서 제외한다.
         * 클릭 수가 0이면 나눗셈이 불가능하므로 0.0으로 처리한다.
         * =========================================================
         */
        @Override
        public BusinessDashboardVO getBusinessDashboard(long businessNo) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                long todaySales = businessDAO.selectTodaySalesByBusinessNo(businessNo);
                int todayOrderCount = businessDAO.selectTodayOrderCountByBusinessNo(businessNo);

                /* [오늘 구매 고객 수 조회 추가] */
                int todayCustomerCount = businessDAO.selectTodayCustomerCountByBusinessNo(businessNo);

                int clickCount = businessDAO.selectTodayClickCountByBusinessNo(businessNo);
                long waitingSettlement = businessDAO.selectWaitingSettlementAmountByBusinessNo(businessNo);
                int waitingProductCount = businessDAO.selectWaitingProductCountByBusinessNo(businessNo);

                /*
                 * [구매전환율 계산]
                 *
                 * todayOrderCount에는 정상 판매 상태의 수량만 포함되므로
                 * 취소 완료된 상품은 구매전환율에 포함되지 않는다.
                 */
                double purchaseRate = clickCount <= 0 ? 0.0 : todayOrderCount * 100.0 / clickCount;

                BusinessDashboardVO dashboard = new BusinessDashboardVO();

                dashboard.setTodaySales(todaySales);
                dashboard.setTodayOrderCount(todayOrderCount);

                /* [오늘 구매 고객 수 설정 추가] */
                dashboard.setTodayCustomerCount(todayCustomerCount);

                dashboard.setClickCount(clickCount);
                dashboard.setPurchaseRate(purchaseRate);
                dashboard.setWaitingSettlement(waitingSettlement);
                dashboard.setWaitingProductCount(waitingProductCount);
                dashboard.setPopularProducts(getPopularProducts(businessNo));

                return dashboard;
        }

        /*
         * =========================================================
         * 인기 상품 목록 조회 (클릭수 내림차순 TOP N)
         * =========================================================
         */
        @Override
        public List<GoodsManageVO> getPopularProducts(long businessNo) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                List<GoodsManageVO> popularProducts = businessDAO.selectPopularProductsByBusinessNo(
                                businessNo,
                                POPULAR_PRODUCT_LIMIT);

                return popularProducts == null
                                ? Collections.emptyList()
                                : popularProducts;
        }

        /*
         * =========================================================
         * 사업자 주문 현황 - 주문 목록 조회
         * 해당 사업자의 주문과 주문 상품을 조회한 뒤
         * ORDER_NO 기준으로 상품 목록을 각 주문에 묶어서 반환.
         * =========================================================
         */
        @Override
        public List<OrderVO> getBusinessOrderList(long businessNo, int currentPage, int pageSize) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                int offset = PaginationUtil.offset(currentPage, pageSize);
                List<OrderVO> orderList = businessDAO.selectBusinessOrderList(businessNo, offset, pageSize);
                if (orderList == null || orderList.isEmpty()) {

                        return Collections.emptyList();
                }

                /*
                 * [페이징 리팩터링] 상품 매핑용 전체 주문상품 목록은 페이지와 무관하게
                 * 그대로 전체 조회한다. 현재 페이지에 표시되는 주문에만 연결되므로
                 * 데이터 정확성에는 영향이 없다.
                 */
                List<OrderItemVO> itemList = getBusinessOrderItemList(businessNo);

                /* ORDER_NO별 주문 상품 묶기 */
                Map<Long, List<OrderItemVO>> itemMap = new HashMap<>();
                for (OrderItemVO item : itemList) {

                        itemMap.computeIfAbsent(item.getOrderNo(), key -> new ArrayList<>())
                                        .add(item);
                }

                /* 각 주문에 해당 상품 목록 연결 */
                for (OrderVO order : orderList) {
                        order.setItems(itemMap.getOrDefault(order.getOrderNo(), Collections.emptyList()));
                }

                return orderList;
        }

        /* [페이징 리팩터링 추가] 사업자 주문 목록 전체 건수 */
        @Override
        public int getBusinessOrderListCount(long businessNo) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                return businessDAO.selectBusinessOrderListCount(businessNo);
        }

        /* 사업자 주문 현황 - 주문 상품 목록 조회 */
        @Override
        public List<OrderItemVO> getBusinessOrderItemList(long businessNo) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                List<OrderItemVO> itemList = businessDAO.selectBusinessOrderItemList(businessNo);

                return itemList == null ? Collections.emptyList() : itemList;
        }

        /*
         * [리팩터링] 사업자 주문 상세 조회(getBusinessOrderDetail)는 제거했다.
         * 주문 상세는 이제 orderList.jsp 모달에서 getBusinessOrderList가 이미
         * 채워주는 데이터(주문별 배송지/상품 목록 포함)를 그대로 사용한다.
         */

        /*
         * =========================================================
         * 사업자 판매 현황 요약 조회
         *
         * 조회 기간과 사업자 번호를 검증한 뒤 매출 합계,
         * 판매량 1위 상품, 주문 건수를 반환한다.
         * =========================================================
         */
        @Override
        public SettlementManageVO getBusinessSalesStatus(
                        long businessNo,
                        LocalDate startDate,
                        LocalDate endDate) {

                validateSalesSearchCondition(businessNo, startDate, endDate);

                SettlementManageVO salesStatus = businessDAO.selectBusinessSalesStatus(
                                businessNo, startDate, endDate);

                if (salesStatus == null) {
                        salesStatus = new SettlementManageVO();
                        salesStatus.setProductName("판매 상품 없음");
                } else if (salesStatus.getProductName() == null
                                || salesStatus.getProductName().isBlank()) {
                        salesStatus.setProductName("판매 상품 없음");
                }

                return salesStatus;
        }

        /* [페이징 리팩터링] 날짜별 판매 내역 조회. currentPage/pageSize로 페이지 단위 조회한다. */
        @Override
        public List<SettlementManageVO> getBusinessSalesHistory(
                        long businessNo,
                        LocalDate startDate,
                        LocalDate endDate,
                        int currentPage,
                        int pageSize) {

                validateSalesSearchCondition(businessNo, startDate, endDate);

                int offset = PaginationUtil.offset(currentPage, pageSize);
                List<SettlementManageVO> salesHistory = businessDAO.selectBusinessSalesHistory(
                                businessNo, startDate, endDate, offset, pageSize);

                return salesHistory == null ? Collections.emptyList() : salesHistory;
        }

        /* [페이징 리팩터링 추가] 판매 내역 전체 건수 (조회 기간 내 판매가 발생한 날짜 수) */
        @Override
        public int getBusinessSalesHistoryCount(
                        long businessNo,
                        LocalDate startDate,
                        LocalDate endDate) {

                validateSalesSearchCondition(businessNo, startDate, endDate);

                return businessDAO.selectBusinessSalesHistoryCount(businessNo, startDate, endDate);
        }

        /* 판매 현황 검색 기간과 사업자 번호 공통 검증. */
        private void validateSalesSearchCondition(
                        long businessNo,
                        LocalDate startDate,
                        LocalDate endDate) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                if (startDate == null || endDate == null) {
                        throw new IllegalArgumentException("조회 시작일과 종료일을 입력해주세요.");
                }

                if (startDate.isAfter(endDate)) {
                        throw new IllegalArgumentException("조회 시작일은 종료일보다 늦을 수 없습니다.");
                }
        }

        /*
         * =========================================================
         * [수정] 이번 달 수수료 요약 조회
         * SETTLEMENT에 생성된 결제 완료 주문상품을 월 단위로 집계한다.
         * =========================================================
         */
        @Override
        public SettlementManageVO getMonthlySettlementSummary(long businessNo) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                SettlementManageVO summary = businessDAO.selectMonthlySettlementSummary(businessNo);
                return summary == null ? new SettlementManageVO() : summary;
        }

        /* 사업자의 정산 요청 내역 조회 */
        @Override
        public List<SettlementRequestVO> getSettlementPaymentHistory(long businessNo) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                List<SettlementRequestVO> history = businessDAO.selectSettlementPaymentHistory(businessNo);
                return history == null ? Collections.emptyList() : history;
        }

        /* 사업자 정산 요청 */
        @Override
        @Transactional
        public void requestSettlementConfirmation(long businessNo) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                SettlementRequestVO settlementRequest = businessDAO.selectSettlementRequestTarget(businessNo);

                if (settlementRequest == null
                                || settlementRequest.getOrderCount() == null
                                || settlementRequest.getOrderCount() <= 0
                                || settlementRequest.getSettledAmount() == null
                                || settlementRequest.getSettledAmount() <= 0) {
                        throw new IllegalStateException("정산을 요청할 수 있는 배송 완료 내역이 없습니다.");
                }

                if (settlementRequest.getBankName() == null
                                || settlementRequest.getBankName().isBlank()
                                || settlementRequest.getAccountNumber() == null
                                || settlementRequest.getAccountNumber().isBlank()
                                || settlementRequest.getAccountHolder() == null
                                || settlementRequest.getAccountHolder().isBlank()) {
                        throw new IllegalStateException("정산 요청 전에 정산 계좌 정보를 등록해주세요.");
                }

                int insertedCount = businessDAO.insertSettlementRequest(settlementRequest);
                if (insertedCount != 1) {
                        throw new IllegalStateException("정산 요청 정보를 생성하지 못했습니다.");
                }

                int linkedCount = businessDAO.updateSettlementRequestNo(
                                businessNo, settlementRequest.getRequestNo());

                if (linkedCount <= 0) {
                        throw new IllegalStateException("정산 요청에 포함할 판매 내역이 없습니다.");
                }

                notificationService.createForAdmins(
                                "SETTLEMENT_REQUEST",
                                "사업자 정산 요청",
                                "사업자가 정산금 지급을 요청했습니다.",
                                "/admin/settlement/main",
                                "SETTLEMENT_REQUEST",
                                settlementRequest.getRequestNo());
        }

        /* [수정] 사업자 정산 계좌 조회 */
        @Override
        public SettlementManageVO getSettlementAccount(long businessNo) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                return businessDAO.selectSettlementAccount(businessNo);
        }

        /* [수정] 사업자 정산 계좌 수정 */
        @Override
        public void updateSettlementAccount(long businessNo, String bankName, String accountNumber,
                        String accountHolder) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                String normalizedBankName = bankName == null ? "" : bankName.trim();
                String normalizedAccountNumber = accountNumber == null ? "" : accountNumber.trim();
                String normalizedAccountHolder = accountHolder == null ? "" : accountHolder.trim();

                if (normalizedBankName.isEmpty() || normalizedAccountNumber.isEmpty()
                                || normalizedAccountHolder.isEmpty()) {
                        throw new IllegalArgumentException("은행명, 계좌번호, 예금주를 모두 입력해주세요.");
                }

                if (!normalizedAccountNumber.matches("[0-9-]{5,50}")) {
                        throw new IllegalArgumentException("계좌번호는 숫자와 하이픈(-)만 입력할 수 있습니다.");
                }

                int updatedCount = businessDAO.updateSettlementAccount(
                                businessNo, normalizedBankName, normalizedAccountNumber, normalizedAccountHolder);

                if (updatedCount <= 0) {
                        throw new IllegalStateException("정산 계좌 정보를 수정하지 못했습니다.");
                }
        }

        /*
         * =========================================================
         * 사업자 배송 관리 목록 조회
         *
         * 허용된 상태값만 Mapper에 전달하여 임의 문자열이 SQL 조건으로
         * 사용되지 않게 하고, 검색어 앞뒤 공백도 제거한다.
         * =========================================================
         */
        @Override
        public List<DeliveryManageVO> getBusinessDeliveryList(
                        long businessNo,
                        String status,
                        String keyword,
                        int currentPage,
                        int pageSize) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                String normalizedStatus = normalizeDeliveryStatusFilter(status);
                String normalizedKeyword = normalizeKeyword(keyword);
                int offset = PaginationUtil.offset(currentPage, pageSize);

                List<DeliveryManageVO> deliveryList = businessDAO.selectBusinessDeliveryList(
                                businessNo, normalizedStatus, normalizedKeyword, offset, pageSize);

                return deliveryList == null ? Collections.emptyList() : deliveryList;
        }

        /* [페이징 리팩터링 추가] 배송 목록 전체 건수 (검색 조건 동일 적용) */
        @Override
        public int getBusinessDeliveryListCount(long businessNo, String status, String keyword) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                String normalizedStatus = normalizeDeliveryStatusFilter(status);
                String normalizedKeyword = normalizeKeyword(keyword);

                return businessDAO.selectBusinessDeliveryListCount(businessNo, normalizedStatus, normalizedKeyword);
        }

        /* [페이징 리팩터링 추가] 검색어 앞뒤 공백 제거 후 빈 문자열이면 null로 취급하는 공용 헬퍼 */
        private String normalizeKeyword(String keyword) {
                String normalizedKeyword = keyword == null ? null : keyword.trim();
                return (normalizedKeyword != null && normalizedKeyword.isEmpty()) ? null : normalizedKeyword;
        }

        /*
         * =========================================================
         * 사업자 운송장/배송 상태 변경
         *
         * 1. ORDER_ITEM_NO와 BUSINESS_NO로 소유권을 확인한다.
         * 2. 취소 관련 상태의 주문상품은 배송 처리하지 않는다.
         * 3. 배송 중/배송 완료는 택배사와 운송장 번호를 필수로 검사한다.
         * 4. DELIVERY 저장, ORDER_ITEM 상태 변경, ORDERS 상태 재계산을
         * 하나의 트랜잭션으로 처리한다.
         * =========================================================
         */
        @Override
        @Transactional
        public void updateBusinessDelivery(
                        long businessNo,
                        long orderItemNo,
                        String courier,
                        String trackingNumber,
                        String status) {

                validateDeliveryRequestNumbers(businessNo, orderItemNo);

                DeliveryManageVO currentItem = getBusinessDeliveryItem(businessNo, orderItemNo);
                String currentStatus = currentItem.getStatus();
                validateDeliveryChangeAllowed(currentStatus);

                String normalizedStatus = normalizeDeliveryUpdateStatus(status);
                validateDeliveryStatusTransition(currentStatus, normalizedStatus);
                String normalizedCourier = courier == null ? null : courier.trim();
                String normalizedTrackingNumber = trackingNumber == null ? null : trackingNumber.trim();
                validateDeliveryDetails(normalizedStatus, normalizedCourier, normalizedTrackingNumber);

                DeliveryManageVO delivery = createDelivery(
                                businessNo,
                                orderItemNo,
                                currentItem.getOrderNo(),
                                normalizedCourier,
                                normalizedTrackingNumber,
                                normalizedStatus);

                saveDelivery(delivery, businessNo, orderItemNo, normalizedStatus);
                businessDAO.updateOrderStatusByOrderItem(currentItem.getOrderNo());

                if (!normalizedStatus.equals(currentStatus)) {
                        createDeliveryStatusNotification(
                                        currentItem,
                                        normalizedStatus);
                }
        }

        private void validateDeliveryRequestNumbers(long businessNo, long orderItemNo) {
                if (businessNo <= 0 || orderItemNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 배송 정보입니다.");
                }
        }

        private DeliveryManageVO getBusinessDeliveryItem(long businessNo, long orderItemNo) {
                DeliveryManageVO currentItem = businessDAO.selectBusinessDeliveryItem(
                                businessNo, orderItemNo);

                if (currentItem == null) {
                        throw new IllegalArgumentException("해당 주문상품을 확인할 수 없습니다.");
                }

                return currentItem;
        }

        private void validateDeliveryChangeAllowed(String currentStatus) {
                if ("CANCEL_REQUEST".equals(currentStatus)
                                || "CANCELED".equals(currentStatus)
                                || "REFUNDED".equals(currentStatus)) {
                        throw new IllegalStateException("취소 또는 환불 처리 중인 상품은 배송 상태를 변경할 수 없습니다.");
                }
        }

        private void validateDeliveryDetails(
                        String status,
                        String courier,
                        String trackingNumber) {

                boolean trackingRequired = DELIVERY_STATUS_SHIPPING.equals(status)
                                || DELIVERY_STATUS_DELIVERED.equals(status);

                if (trackingRequired && (courier == null || courier.isEmpty())) {
                        throw new IllegalArgumentException("배송 중 또는 배송 완료 처리 시 택배사를 선택해주세요.");
                }

                if (trackingRequired && (trackingNumber == null || trackingNumber.isEmpty())) {
                        throw new IllegalArgumentException("배송 중 또는 배송 완료 처리 시 운송장 번호를 입력해주세요.");
                }

                if (trackingNumber != null && trackingNumber.length() > 100) {
                        throw new IllegalArgumentException("운송장 번호는 100자 이하로 입력해주세요.");
                }

                if (courier != null && courier.length() > 50) {
                        throw new IllegalArgumentException("택배사명은 50자 이하로 입력해주세요.");
                }
        }

        private DeliveryManageVO createDelivery(
                        long businessNo,
                        long orderItemNo,
                        Long orderNo,
                        String courier,
                        String trackingNumber,
                        String status) {

                DeliveryManageVO delivery = new DeliveryManageVO();
                delivery.setBusinessNo(businessNo);
                delivery.setOrderItemNo(orderItemNo);
                delivery.setOrderNo(orderNo);
                delivery.setCourier(courier);
                delivery.setTrackingNumber(trackingNumber);
                delivery.setStatus(status);

                return delivery;
        }

        private void saveDelivery(
                        DeliveryManageVO delivery,
                        long businessNo,
                        long orderItemNo,
                        String status) {

                int deliveryResult = businessDAO.mergeDelivery(delivery);
                if (deliveryResult != 1) {
                        throw new IllegalStateException("배송 정보 저장에 실패했습니다.");
                }

                int itemResult = businessDAO.updateOrderItemDeliveryStatus(
                                businessNo, orderItemNo, status);
                if (itemResult != 1) {
                        throw new IllegalStateException("주문상품 배송 상태 변경에 실패했습니다.");
                }
        }

        /**
         * 배송 상태가 실제로 다음 단계로 변경된 경우 주문 회원에게 알림을 생성합니다.
         * 동일 상태 재저장은 알림 생성 대상에서 제외합니다.
         */
        private void createDeliveryStatusNotification(
                        DeliveryManageVO deliveryItem,
                        String deliveryStatus) {

                if (deliveryItem == null || deliveryStatus == null) {
                        return;
                }

                String productName = deliveryItem.getProductName();
                String productMessage = productName == null
                                || productName.isBlank()
                                                ? ""
                                                : " 상품명: " + productName.trim();

                String notificationType;
                String title;
                String message;

                switch (deliveryStatus) {
                        case DELIVERY_STATUS_PREPARING -> {
                                notificationType = "DELIVERY_PREPARING";
                                title = "배송 준비 시작";
                                message = "주문하신 상품의 배송 준비가 시작되었습니다."
                                                + productMessage;
                        }
                        case DELIVERY_STATUS_SHIPPING -> {
                                notificationType = "DELIVERY_SHIPPED";
                                title = "상품 발송";
                                message = "주문하신 상품이 발송되었습니다."
                                                + productMessage;
                        }
                        case DELIVERY_STATUS_DELIVERED -> {
                                notificationType = "DELIVERY_DELIVERED";
                                title = "배송 완료";
                                message = "주문하신 상품의 배송이 완료되었습니다."
                                                + productMessage;
                        }
                        default -> {
                                return;
                        }
                }

                notificationService.createForMember(
                                deliveryItem.getMemberNo(),
                                notificationType,
                                title,
                                message,
                                "/order/list",
                                "ORDER_ITEM",
                                deliveryItem.getOrderItemNo());
        }

        /* 배송 목록 검색에 사용할 상태값을 검증하고 정규화. */
        private String normalizeDeliveryStatusFilter(String status) {
                if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
                        return null;
                }

                String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
                if (!Set.of("CONFIRMED", DELIVERY_STATUS_PREPARING, DELIVERY_STATUS_SHIPPING, DELIVERY_STATUS_DELIVERED)
                                .contains(normalizedStatus)) {
                        throw new IllegalArgumentException("올바르지 않은 배송 상태 검색 조건입니다.");
                }

                return normalizedStatus;
        }

        /* 화면에서 변경 가능한 배송 상태만 허용. */
        private String normalizeDeliveryUpdateStatus(String status) {
                if (status == null || status.isBlank()) {
                        throw new IllegalArgumentException("변경할 배송 상태를 선택해주세요.");
                }

                String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
                if (!Set.of(DELIVERY_STATUS_PREPARING, DELIVERY_STATUS_SHIPPING, DELIVERY_STATUS_DELIVERED)
                                .contains(normalizedStatus)) {
                        throw new IllegalArgumentException("변경할 수 없는 배송 상태입니다.");
                }

                return normalizedStatus;
        }

        /* 배송 상태가 이전 단계로 돌아가지 않도록 검사 */
        private void validateDeliveryStatusTransition(String currentStatus, String nextStatus) {
                Map<String, Integer> statusOrder = Map.of(
                                "PAID", 0,
                                "CONFIRMED", 0,
                                DELIVERY_STATUS_PREPARING, 1,
                                DELIVERY_STATUS_SHIPPING, 2,
                                DELIVERY_STATUS_DELIVERED, 3);

                Integer currentStep = statusOrder.get(currentStatus);
                Integer nextStep = statusOrder.get(nextStatus);

                if (currentStep != null && nextStep != null && nextStep < currentStep) {
                        throw new IllegalStateException("배송 상태는 이전 단계로 되돌릴 수 없습니다.");
                }
        }

        /* 사업자등록번호 사용 가능 여부 확인 */
        @Override
        public boolean isBusinessNumberAvailable(String businessNumber) {

                if (businessNumber == null || businessNumber.isBlank()) {
                        throw new IllegalArgumentException("사업자등록번호를 입력해주세요.");
                }

                String normalizedBusinessNumber = businessNumber.trim();

                if (!normalizedBusinessNumber.matches("\\d{3}-\\d{2}-\\d{5}")) {
                        throw new IllegalArgumentException("사업자등록번호 형식이 올바르지 않습니다.");
                }

                int count = businessDAO.countByBusinessNumber(normalizedBusinessNumber);

                return count == 0;
        }

        /*
         * =========================================================
         * 상품 등록
         * =========================================================
         */
        @Override
        @Transactional
        public long registerProduct(
                        GoodsManageVO goodsManageVO,
                        MultipartFile productImage,
                        MultipartFile[] detailImages) {

                if (goodsManageVO == null) {
                        throw new IllegalArgumentException("상품 등록 정보가 없습니다.");
                }

                validateProduct(goodsManageVO);

                /*
                 * [상품 옵션 기능 추가]
                 * 의상/신발은 옵션별 재고의 합계를 PRODUCT.STOCK에 저장합니다.
                 */
                applyOptionTotalStock(goodsManageVO);

                validateProductImage(productImage);
                validateDetailImages(detailImages);

                /*
                 * JSONL에서 선택한 콘텐츠를 실제 상품 저장 직전에 DB에 준비합니다.
                 */
                prepareProductContent(goodsManageVO);
                resolveProductActor(goodsManageVO);
                validateContentActor(goodsManageVO);

                List<Path> savedPhysicalPathList = new ArrayList<Path>();

                try {
                        SavedFileInfo savedFileInfo = saveProductImage(productImage);

                        savedPhysicalPathList.add(savedFileInfo.physicalPath());

                        goodsManageVO.setImagePath(savedFileInfo.webPath());
                        goodsManageVO.setIsMain("Y");
                        goodsManageVO.setStatus(STATUS_WAITING);

                        int productResult = businessDAO.insertProduct(goodsManageVO);

                        if (productResult != 1) {
                                throw new IllegalStateException("상품 등록에 실패했습니다.");
                        }

                        if (goodsManageVO.getProductNo() <= 0) {
                                throw new IllegalStateException("등록된 상품 번호를 확인할 수 없습니다.");
                        }

                        // [상품 옵션 기능 추가] 옵션 상품은 조합별 재고를 저장하고 PRODUCT.STOCK에는 총재고를 유지합니다.
                        saveProductOptions(goodsManageVO);

                        int imageResult = businessDAO.insertProductImage(goodsManageVO);

                        if (imageResult != 1) {
                                throw new IllegalStateException("상품 대표 이미지 등록에 실패했습니다.");
                        }

                        /* [추가] 세부 이미지 여러 장 저장 */
                        saveDetailProductImages(goodsManageVO.getProductNo(), detailImages, savedPhysicalPathList);

                        notificationService.createForAdmins(
                                        "PRODUCT_REQUEST",
                                        "상품 승인 요청",
                                        goodsManageVO.getProductName()
                                                        + " 상품의 등록 승인 요청이 접수되었습니다.",
                                        "/admin/product/list?tab=waiting",
                                        REFERENCE_TYPE_PRODUCT,
                                        goodsManageVO.getProductNo());

                        return goodsManageVO.getProductNo();

                } catch (RuntimeException e) {
                        for (Path path : savedPhysicalPathList) {
                                deleteSavedFileQuietly(path);
                        }

                        throw e;
                }
        }

        /*
         * =========================================================
         * 세부 이미지 검증
         * =========================================================
         */
        private void validateDetailImages(MultipartFile[] detailImages) {
                if (detailImages == null || detailImages.length == 0) {
                        return;
                }

                int validImageCount = 0;

                for (MultipartFile detailImage : detailImages) {
                        if (detailImage == null || detailImage.isEmpty()) {
                                continue;
                        }

                        validImageCount++;

                        if (validImageCount > 10) {
                                throw new IllegalArgumentException("상품 세부 이미지는 최대 10장까지 등록할 수 있습니다.");
                        }

                        validateProductImage(detailImage);
                }
        }

        /*
         * =========================================================
         * [상품 세부 이미지 수정 추가]
         * 수정 요청에서 실제 선택된 세부 이미지가 있는지 확인합니다.
         * 비어 있는 file input도 배열 요소로 전달될 수 있으므로
         * 배열 길이뿐 아니라 MultipartFile.isEmpty()까지 검사합니다.
         * =========================================================
         */
        private boolean hasSelectedDetailImage(
                        MultipartFile[] detailImages) {

                if (detailImages == null
                                || detailImages.length == 0) {

                        return false;
                }

                for (MultipartFile detailImage : detailImages) {

                        if (detailImage != null
                                        && !detailImage.isEmpty()) {

                                return true;
                        }
                }

                return false;
        }

        /*
         * =========================================================
         * 세부 이미지 저장
         * - PRODUCT_IMAGE에 대표 이미지 외 추가 이미지들을 IS_MAIN='N'으로 저장
         * =========================================================
         */
        private void saveDetailProductImages(
                        long productNo,
                        MultipartFile[] detailImages,
                        List<Path> savedPhysicalPathList) {

                if (detailImages == null || detailImages.length == 0) {
                        return;
                }

                for (MultipartFile detailImage : detailImages) {
                        if (detailImage == null || detailImage.isEmpty()) {
                                continue;
                        }

                        SavedFileInfo savedFileInfo = saveProductImage(detailImage);

                        savedPhysicalPathList.add(savedFileInfo.physicalPath());

                        GoodsManageVO detailImageVO = new GoodsManageVO();
                        detailImageVO.setProductNo(productNo);
                        detailImageVO.setImagePath(savedFileInfo.webPath());
                        detailImageVO.setIsMain("N");

                        int imageInsertResult = businessDAO.insertProductImage(detailImageVO);

                        if (imageInsertResult != 1) {
                                throw new IllegalStateException("상품 세부 이미지 등록에 실패했습니다.");
                        }
                }
        }

        /*
         * [상품 옵션 기능 추가]
         * 의상/신발 상품의 옵션별 재고 합계를 PRODUCT.STOCK에 반영합니다.
         *
         * 등록과 수정에서 같은 계산을 사용하도록 분리하여
         * updateProduct 메서드의 인지 복잡도를 낮춥니다.
         */
        private void applyOptionTotalStock(
                        GoodsManageVO goodsManageVO) {

                String productType = goodsManageVO.getProductType();

                boolean optionProduct = PRODUCT_TYPE_CLOTHES.equals(
                                productType)
                                || PRODUCT_TYPE_SHOES.equals(
                                                productType);

                if (!optionProduct
                                || goodsManageVO.getOptionList() == null) {
                        return;
                }

                int optionTotalStock = goodsManageVO.getOptionList()
                                .stream()
                                .filter(java.util.Objects::nonNull)
                                .map(com.project.oditji.goods.vo.ProductOptionVO::getStock)
                                .filter(java.util.Objects::nonNull)
                                .mapToInt(Integer::intValue)
                                .sum();

                goodsManageVO.setStock(
                                optionTotalStock);
        }

        /** [상품 옵션 기능 추가] 의상/신발 옵션 검증 및 저장(등록 시 사용) */
        private void saveProductOptions(GoodsManageVO goodsManageVO) {
                validateAndInsertProductOptions(goodsManageVO);
        }

        /*
         * [상품 옵션 기능 추가]
         * 수정 요청 처리 시 사용. 기존 옵션 조합을 전부 지우고, 화면에서
         * 넘어온 조합으로 다시 채워 넣는다(전체 교체 방식).
         *
         * 상품 종류가 의상/신발이 아닌 다른 종류로 바뀐 경우에는(원래
         * 의상/신발이었다가 수정하면서 종류를 바꾼 경우 포함) 기존에
         * 남아있는 옵션 조합이 없도록 항상 먼저 삭제한다.
         */
        private void replaceProductOptions(GoodsManageVO goodsManageVO) {
                businessDAO.deleteProductOptionsByProductNo(goodsManageVO.getProductNo());
                validateAndInsertProductOptions(goodsManageVO);
        }

        /** [상품 옵션 기능 추가] 의상/신발 옵션 검증 및 저장 (등록/수정 공용) */
        private void validateAndInsertProductOptions(GoodsManageVO goodsManageVO) {
                String type = goodsManageVO.getProductType();
                if (!PRODUCT_TYPE_CLOTHES.equals(type) && !PRODUCT_TYPE_SHOES.equals(type)) {
                        return;
                }
                if (goodsManageVO.getOptionList() == null || goodsManageVO.getOptionList().isEmpty()) {
                        throw new IllegalArgumentException("의상과 신발은 색상, 사이즈, 재고 옵션을 1개 이상 등록해야 합니다.");
                }
                Set<String> duplicateCheck = new HashSet<String>();
                int totalStock = 0;
                for (com.project.oditji.goods.vo.ProductOptionVO option : goodsManageVO.getOptionList()) {
                        if (option == null || option.getColorName() == null || option.getColorName().isBlank()
                                        || option.getSizeName() == null || option.getSizeName().isBlank()
                                        || option.getStock() == null || option.getStock() < 0) {
                                throw new IllegalArgumentException("모든 옵션의 색상, 사이즈, 재고를 올바르게 입력해주세요.");
                        }
                        option.setColorName(option.getColorName().trim());
                        option.setSizeName(option.getSizeName().trim());
                        String key = option.getColorName().toUpperCase() + "|" + option.getSizeName().toUpperCase();
                        if (!duplicateCheck.add(key)) {
                                throw new IllegalArgumentException("동일한 색상과 사이즈 조합은 중복 등록할 수 없습니다.");
                        }
                        option.setProductNo(goodsManageVO.getProductNo());
                        totalStock += option.getStock();
                        if (businessDAO.insertProductOption(option) != 1) {
                                throw new IllegalStateException("상품 옵션 저장에 실패했습니다.");
                        }
                }
                goodsManageVO.setStock(totalStock);
        }

        /*
         * =========================================================
         * 콘텐츠 검색
         * =========================================================
         */
        @Override
        public List<ContentSearchVO> getContentList(
                        String keyword) {

                String searchKeyword = keyword;

                if (searchKeyword != null) {

                        searchKeyword = searchKeyword.trim();

                        if (searchKeyword.isEmpty()) {
                                searchKeyword = null;
                        }
                }

                List<ContentSearchVO> contentList = businessDAO.selectBusinessContentList(
                                searchKeyword);

                if (contentList == null) {
                        return Collections.emptyList();
                }

                return contentList;
        }

        /*
         * =========================================================
         * 상품 등록 화면용 JSONL 콘텐츠 검색
         *
         * DB의 CONTENT 테이블은 조회하지 않습니다.
         * 검색어가 없는 경우 3만 건 이상의 전체 캐시가 화면에 노출되지
         * 않도록 빈 목록을 반환합니다.
         * =========================================================
         */
        @Override
        public List<ContentSearchVO> getCachedContentList(
                        String keyword) {

                if (keyword == null
                                || keyword.isBlank()) {
                        return Collections.emptyList();
                }

                String normalizedKeyword = keyword.trim()
                                .toLowerCase(Locale.ROOT);

                List<ContentSearchVO> resultList = new ArrayList<ContentSearchVO>();

                for (CachedContentVO cachedContent : searchContentStore.getAll()) {

                        boolean validContent = cachedContent != null
                                        && cachedContent.getTmdbId() != null
                                        && cachedContent.getContentType() != null;

                        if (validContent
                                        && matchesCachedContent(
                                                        cachedContent,
                                                        normalizedKeyword)) {
                                resultList.add(
                                                convertToContentSearchVO(
                                                                cachedContent));

                                if (resultList.size() >= CONTENT_SEARCH_LIMIT) {
                                        break;
                                }
                        }
                }

                return resultList;
        }

        /*
         * =========================================================
         * JSONL 콘텐츠 선택 후 TMDB 배우 미리보기 조회
         *
         * 이 단계에서는 ACTOR와 CONTENT_ACTOR에 저장하지 않습니다.
         * 상품 등록 요청이 성공적으로 처리될 때 DB 저장이 수행됩니다.
         * =========================================================
         */
        @Override
        public List<ActorSearchVO> getActorPreview(
                        Long tmdbId,
                        String contentType) {

                validateCachedContentSelection(
                                tmdbId,
                                contentType);

                List<ActorVO> actorList = tmdbService.getContentActorPreview(
                                tmdbId,
                                contentType);

                if (actorList == null
                                || actorList.isEmpty()) {
                        return Collections.emptyList();
                }

                List<ActorSearchVO> resultList = new ArrayList<ActorSearchVO>();

                for (ActorVO actor : actorList) {

                        if (actor == null
                                        || actor.getTmdbActorId() == null) {
                                continue;
                        }

                        ActorSearchVO result = new ActorSearchVO();

                        result.setTmdbActorId(
                                        actor.getTmdbActorId());
                        result.setActorName(
                                        actor.getActorName());
                        result.setProfilePath(
                                        actor.getProfilePath());
                        result.setCharacterName(
                                        actor.getCharacterName());
                        result.setDisplayOrder(
                                        actor.getDisplayOrder());

                        resultList.add(result);
                }

                return resultList;
        }

        /*
         * =========================================================
         * 콘텐츠 단건 조회
         * =========================================================
         */
        @Override
        public ContentSearchVO getContentByNo(
                        long contentNo) {

                if (contentNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 콘텐츠 번호입니다.");
                }

                ContentSearchVO content = businessDAO.selectContentByNo(
                                contentNo);

                if (content == null) {

                        throw new IllegalArgumentException(
                                        "존재하지 않는 콘텐츠입니다.");
                }

                return content;
        }

        /*
         * =========================================================
         * 선택한 콘텐츠에 연결된 배우 목록 조회
         * =========================================================
         */
        @Override
        public List<ActorSearchVO> getActorListByContentNo(
                        long contentNo) {

                if (contentNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 콘텐츠 번호입니다.");
                }

                ContentSearchVO content = businessDAO.selectContentByNo(
                                contentNo);

                if (content == null) {

                        throw new IllegalArgumentException(
                                        "존재하지 않는 콘텐츠입니다.");
                }

                List<ActorSearchVO> actorList = businessDAO.selectActorListByContentNo(
                                contentNo);

                if (actorList == null) {
                        return Collections.emptyList();
                }

                return actorList;
        }

        /*
         * =========================================================
         * 사업자가 등록한 상품 목록 조회 (승인된 상품만)
         *
         * 이벤트 등록/수정 화면의 상품 검색 모달 전용이다.
         * =========================================================
         */
        @Override
        public List<GoodsManageVO> getApprovedProductListByBusinessNo(
                        long businessNo) {

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                List<GoodsManageVO> productList = businessDAO.selectApprovedProductListByBusinessNo(
                                businessNo);

                if (productList == null) {
                        return Collections.emptyList();
                }

                return productList;
        }

        /*
         * =========================================================
         * [페이징 리팩터링] 사업자가 등록한 상품 목록 조회
         * 관리자 목록 화면과 동일하게 currentPage/pageSize로 페이지 단위 조회한다.
         * =========================================================
         */
        @Override
        public List<GoodsManageVO> getProductListByBusinessNo(
                        long businessNo, String keyword, int currentPage, int pageSize) {

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                String normalizedKeyword = normalizeKeyword(keyword);
                int offset = PaginationUtil.offset(currentPage, pageSize);

                List<GoodsManageVO> productList = businessDAO.selectProductListByBusinessNo(
                                businessNo, normalizedKeyword, offset, pageSize);

                if (productList == null) {
                        return Collections.emptyList();
                }

                /*
                 * [상품 옵션 기능 추가] 의상/신발 상품은 기존 색상-사이즈
                 * 옵션을 함께 내려보내, 상품 수정 요청 모달을 열었을 때
                 * 화면(productList.jsp의 숨김 template)에서 다시 채워
                 * 넣을 수 있게 한다.
                 */
                for (GoodsManageVO product : productList) {

                        if (PRODUCT_TYPE_CLOTHES.equals(product.getProductType())
                                        || PRODUCT_TYPE_SHOES.equals(product.getProductType())) {

                                product.setOptionList(
                                                businessDAO.selectProductOptionsByProductNo(
                                                                product.getProductNo()));
                        }

                        /*
                         * [상품 세부 이미지 수정 추가]
                         * 상품 수정 모달에서 기존 세부 이미지 파일명을 표시할 수 있도록
                         * 대표 이미지 외 이미지 경로 목록을 함께 조회합니다.
                         */
                        product.setDetailImagePathList(
                                        businessDAO.selectProductDetailImagePathList(
                                                        product.getProductNo()));
                }

                return productList;
        }

        /* [페이징 리팩터링 추가] 사업자가 등록한 상품 목록 전체 건수 (검색 조건 동일 적용) */
        @Override
        public int getProductListCountByBusinessNo(long businessNo, String keyword) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                return businessDAO.selectProductListCountByBusinessNo(businessNo, normalizeKeyword(keyword));
        }

        /*
         * =========================================================
         * 상품 수정 화면용 상품 단건 조회
         * =========================================================
         */
        @Override
        public GoodsManageVO getProductForUpdate(
                        long productNo,
                        long businessNo) {

                if (productNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 상품 번호입니다.");
                }

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                GoodsManageVO product = businessDAO.selectProductForUpdate(
                                productNo,
                                businessNo);

                if (product == null) {

                        throw new IllegalArgumentException(
                                        "상품이 존재하지 않거나 "
                                                        + "수정 권한이 없습니다.");
                }

                return product;
        }

        /*
         * =========================================================
         * 상품 수정 요청
         *
         * 새 이미지가 없으면 기존 상품 이미지를 유지한다.
         * 새 이미지가 있으면 기존 대표 이미지의 경로를 변경한다.
         * 대표 이미지 행이 없다면 새 대표 이미지 행을 등록한다.
         * =========================================================
         */
        @Override
        @Transactional
        public void updateProduct(
                        GoodsManageVO goodsManageVO,
                        MultipartFile productImage,
                        MultipartFile[] detailImages,
                        boolean deleteMainImage,
                        String[] deletedDetailImagePaths) {

                validateProductUpdateRequest(goodsManageVO);

                GoodsManageVO existingProduct = getExistingProductForUpdate(goodsManageVO);

                Set<String> requestedDeletedDetailImagePathSet = resolveDeletedDetailImagePaths(
                                goodsManageVO.getProductNo(),
                                detailImages,
                                deletedDetailImagePaths);

                prepareProductForUpdate(goodsManageVO);

                List<Path> savedPhysicalPathList = new ArrayList<Path>();
                List<Path> oldPhysicalPathListToDelete = new ArrayList<Path>();

                try {
                        updateProductAndOptions(goodsManageVO);

                        processMainProductImageUpdate(
                                        goodsManageVO,
                                        existingProduct,
                                        productImage,
                                        deleteMainImage,
                                        savedPhysicalPathList,
                                        oldPhysicalPathListToDelete);

                        processDetailProductImageUpdate(
                                        goodsManageVO.getProductNo(),
                                        detailImages,
                                        requestedDeletedDetailImagePathSet,
                                        savedPhysicalPathList,
                                        oldPhysicalPathListToDelete);

                } catch (RuntimeException e) {
                        deleteSavedFiles(savedPhysicalPathList);
                        throw e;
                }

                registerProductImageFilesForDeletionAfterCommit(
                                oldPhysicalPathListToDelete);

                createProductReapprovalNotification(goodsManageVO);
        }

        /*
         * [SonarQube Brain Method / 인지 복잡도 개선]
         * 상품 수정 진입 검증을 분리하여 updateProduct의 분기와 지역변수 수를 줄입니다.
         */
        private void validateProductUpdateRequest(
                        GoodsManageVO goodsManageVO) {

                if (goodsManageVO == null) {
                        throw new IllegalArgumentException(
                                        "상품 수정 정보가 없습니다.");
                }

                if (goodsManageVO.getProductNo() <= 0) {
                        throw new IllegalArgumentException(
                                        "올바르지 않은 상품 번호입니다.");
                }
        }

        /* [SonarQube Brain Method / 인지 복잡도 개선] 수정 대상 조회와 상태 검증을 분리합니다. */
        private GoodsManageVO getExistingProductForUpdate(
                        GoodsManageVO goodsManageVO) {

                GoodsManageVO existingProduct = businessDAO.selectProductForUpdate(
                                goodsManageVO.getProductNo(),
                                goodsManageVO.getBusinessNo());

                if (existingProduct == null) {
                        throw new IllegalArgumentException(
                                        "상품이 존재하지 않거나 "
                                                        + "수정 권한이 없습니다.");
                }

                if ("DELETE_REQUESTED".equals(existingProduct.getStatus())) {
                        throw new IllegalStateException(
                                        "삭제 요청 중인 상품은 수정할 수 없습니다.");
                }

                return existingProduct;
        }

        /*
         * [상품 이미지 개별 삭제 / SonarQube 개선]
         * 요청 경로는 DB의 현재 세부 이미지 경로와 대조하고, 수정 후 총 이미지 수를 검증합니다.
         */
        private Set<String> resolveDeletedDetailImagePaths(
                        long productNo,
                        MultipartFile[] detailImages,
                        String[] deletedDetailImagePaths) {

                List<String> existingDetailImagePaths = businessDAO.selectProductDetailImagePathList(
                                productNo);

                Set<String> requestedDeletedDetailImagePathSet = filterExistingDetailImagePaths(
                                existingDetailImagePaths,
                                deletedDetailImagePaths);

                validateDetailImageCountAfterUpdate(
                                existingDetailImagePaths.size(),
                                requestedDeletedDetailImagePathSet.size(),
                                detailImages);

                return requestedDeletedDetailImagePathSet;
        }

        private Set<String> filterExistingDetailImagePaths(
                        List<String> existingDetailImagePaths,
                        String[] deletedDetailImagePaths) {

                Set<String> requestedDeletedDetailImagePathSet = new HashSet<String>();

                if (deletedDetailImagePaths == null) {
                        return requestedDeletedDetailImagePathSet;
                }

                for (String deletedDetailImagePath : deletedDetailImagePaths) {
                        if (deletedDetailImagePath != null
                                        && existingDetailImagePaths.contains(deletedDetailImagePath)) {
                                requestedDeletedDetailImagePathSet.add(deletedDetailImagePath);
                        }
                }

                return requestedDeletedDetailImagePathSet;
        }

        private void validateDetailImageCountAfterUpdate(
                        int existingDetailImageCount,
                        int deletedDetailImageCount,
                        MultipartFile[] detailImages) {

                int remainingDetailImageCount = existingDetailImageCount
                                - deletedDetailImageCount
                                + countSelectedDetailImages(detailImages);

                if (remainingDetailImageCount > 10) {
                        throw new IllegalArgumentException(
                                        "상품 세부 이미지는 삭제 후 새로 추가한 이미지를 포함해 최대 10장까지 등록할 수 있습니다.");
                }
        }

        /* [SonarQube Brain Method / 인지 복잡도 개선] 상품 값 검증과 수정 전 정규화를 묶어 분리합니다. */
        private void prepareProductForUpdate(
                        GoodsManageVO goodsManageVO) {

                validateProduct(goodsManageVO);
                applyOptionTotalStock(goodsManageVO);
                normalizeActorNo(goodsManageVO);
                validateContentActor(goodsManageVO);
                goodsManageVO.setStatus(STATUS_WAITING);
        }

        private void updateProductAndOptions(
                        GoodsManageVO goodsManageVO) {

                int updateResult = businessDAO.updateProduct(goodsManageVO);

                if (updateResult != 1) {
                        throw new IllegalStateException(
                                        "상품 수정에 실패했습니다.");
                }

                replaceProductOptions(goodsManageVO);
        }

        /*
         * [상품 기본 이미지 개별 삭제 / SonarQube 개선]
         * 새 대표 이미지 교체와 대표 이미지 삭제 예약을 한 메서드에서 처리합니다.
         */
        private void processMainProductImageUpdate(
                        GoodsManageVO goodsManageVO,
                        GoodsManageVO existingProduct,
                        MultipartFile productImage,
                        boolean deleteMainImage,
                        List<Path> savedPhysicalPathList,
                        List<Path> oldPhysicalPathListToDelete) {

                if (productImage != null && !productImage.isEmpty()) {
                        updateMainProductImageIfSelected(
                                        goodsManageVO,
                                        productImage,
                                        savedPhysicalPathList);
                        addProductImagePathForDeletion(
                                        existingProduct.getImagePath(),
                                        oldPhysicalPathListToDelete);
                        return;
                }

                if (deleteMainImage) {
                        businessDAO.deleteProductMainImageByProductNo(
                                        goodsManageVO.getProductNo());
                        addProductImagePathForDeletion(
                                        existingProduct.getImagePath(),
                                        oldPhysicalPathListToDelete);
                }
        }

        /*
         * [상품 세부 이미지 개별 삭제 / SonarQube 개선]
         * 기존 선택 이미지 삭제와 새 세부 이미지 추가를 분리된 흐름으로 처리합니다.
         */
        private void processDetailProductImageUpdate(
                        long productNo,
                        MultipartFile[] detailImages,
                        Set<String> requestedDeletedDetailImagePathSet,
                        List<Path> savedPhysicalPathList,
                        List<Path> oldPhysicalPathListToDelete) {

                deleteRequestedDetailProductImages(
                                productNo,
                                requestedDeletedDetailImagePathSet,
                                oldPhysicalPathListToDelete);

                if (!hasSelectedDetailImage(detailImages)) {
                        return;
                }

                validateDetailImages(detailImages);
                saveDetailProductImages(
                                productNo,
                                detailImages,
                                savedPhysicalPathList);
        }

        private void deleteRequestedDetailProductImages(
                        long productNo,
                        Set<String> requestedDeletedDetailImagePathSet,
                        List<Path> oldPhysicalPathListToDelete) {

                for (String deletedDetailImagePath : requestedDeletedDetailImagePathSet) {
                        int deletedImageCount = businessDAO.deleteProductDetailImageByPath(
                                        productNo,
                                        deletedDetailImagePath);

                        if (deletedImageCount == 1) {
                                addProductImagePathForDeletion(
                                                deletedDetailImagePath,
                                                oldPhysicalPathListToDelete);
                        }
                }
        }

        private void addProductImagePathForDeletion(
                        String imageWebPath,
                        List<Path> oldPhysicalPathListToDelete) {

                Path physicalPath = resolveProductImagePhysicalPath(imageWebPath);

                if (physicalPath != null) {
                        oldPhysicalPathListToDelete.add(physicalPath);
                }
        }

        private void deleteSavedFiles(
                        List<Path> savedPhysicalPathList) {

                for (Path savedPhysicalPath : savedPhysicalPathList) {
                        deleteSavedFileQuietly(savedPhysicalPath);
                }
        }

        private void createProductReapprovalNotification(
                        GoodsManageVO goodsManageVO) {

                notificationService.createForAdmins(
                                "PRODUCT_REQUEST",
                                "상품 재승인 요청",
                                goodsManageVO.getProductName()
                                                + " 상품의 수정 승인 요청이 접수되었습니다.",
                                "/admin/product/list?tab=waiting",
                                REFERENCE_TYPE_PRODUCT,
                                goodsManageVO.getProductNo());
        }

        /*
         * [SonarQube 인지 복잡도 개선]
         * 상품 수정 중 대표 이미지 교체 처리만 분리한다.
         */
        private void updateMainProductImageIfSelected(
                        GoodsManageVO goodsManageVO,
                        MultipartFile productImage,
                        List<Path> savedPhysicalPathList) {

                if (productImage == null
                                || productImage.isEmpty()) {
                        return;
                }

                validateProductImage(
                                productImage);

                SavedFileInfo savedFileInfo = saveProductImage(
                                productImage);

                savedPhysicalPathList.add(
                                savedFileInfo.physicalPath());

                goodsManageVO.setImagePath(
                                savedFileInfo.webPath());

                goodsManageVO.setIsMain(
                                "Y");

                int imageUpdateResult = businessDAO.updateProductMainImage(
                                goodsManageVO);

                /*
                 * 기존 대표 이미지가 없는 상품이면
                 * 새로운 대표 이미지 행을 등록한다.
                 */
                if (imageUpdateResult == 0) {

                        int imageInsertResult = businessDAO.insertProductImage(
                                        goodsManageVO);

                        if (imageInsertResult != 1) {

                                throw new IllegalStateException(
                                                "상품 대표 이미지 수정에 실패했습니다.");
                        }
                }
        }

        /*
         * =========================================================
         * 상품 삭제 요청
         *
         * 실제 상품과 이미지를 즉시 삭제하지 않고
         * PRODUCT.STATUS를 DELETE_REQUESTED로 변경한다.
         *
         * 현재 PRODUCT 테이블에는 삭제 사유 컬럼이 없으므로
         * 삭제 사유는 검증 후 개발 로그에만 출력한다.
         * =========================================================
         */
        @Override
        @Transactional
        public void requestProductDelete(
                        long productNo,
                        long businessNo,
                        String reason) {

                if (productNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 상품 번호입니다.");
                }

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                if (reason == null
                                || reason.isBlank()) {

                        throw new IllegalArgumentException(
                                        "삭제 요청 사유를 입력해주세요.");
                }

                String normalizedReason = reason.trim();

                if (normalizedReason.length() > 1000) {

                        throw new IllegalArgumentException(
                                        "삭제 요청 사유는 1000자 이하로 입력해주세요.");
                }

                /*
                 * PRODUCT_NO와 BUSINESS_NO를 함께 조회하여
                 * 로그인한 사업자가 등록한 상품인지 검증한다.
                 */
                GoodsManageVO existingProduct = businessDAO.selectProductForUpdate(
                                productNo,
                                businessNo);

                if (existingProduct == null) {

                        throw new IllegalArgumentException(
                                        "상품이 존재하지 않거나 "
                                                        + "삭제 요청 권한이 없습니다.");
                }

                if ("DELETE_REQUESTED".equals(
                                existingProduct.getStatus())) {

                        throw new IllegalStateException(
                                        "이미 삭제 요청이 접수된 상품입니다.");
                }

                /*
                 * PRODUCT 테이블에 삭제 사유를 저장할 컬럼이 없으므로
                 * 현재 단계에서는 서버 로그로 확인한다.
                 */
                if (log.isInfoEnabled()) {
                        log.info("===== 상품 삭제 요청 =====");
                        log.info("상품 번호: {}", productNo);
                        log.info("사업자 번호: {}", businessNo);
                        log.info("상품명: {}", existingProduct.getProductName());
                        log.info("삭제 사유: {}", normalizedReason);
                }
                int updateResult = businessDAO.updateProductDeleteRequest(
                                productNo,
                                businessNo);

                if (updateResult != 1) {

                        throw new IllegalStateException(
                                        "상품 삭제 요청 처리에 실패했습니다.");
                }

                notificationService.createForAdmins(
                                "PRODUCT_DELETE_REQUEST",
                                "상품 삭제 승인 요청",
                                existingProduct.getProductName()
                                                + " 상품의 삭제 요청이 접수되었습니다.",
                                "/admin/product/list?tab=delete",
                                REFERENCE_TYPE_PRODUCT,
                                productNo);
        }

        /*
         * =========================================================
         * 이벤트 등록
         *
         * EVENT 테이블에 이벤트 기본 정보를 먼저 저장하고,
         * 상품이 선택된 경우 EVENT_PRODUCT 테이블에도 연결 정보를 저장한다.
         *
         * 두 DB 작업은 하나의 트랜잭션으로 처리한다.
         * =========================================================
         */
        @Override
        @Transactional
        public long registerEvent(
                        EventManageVO eventManageVO,
                        MultipartFile eventImage) {

                if (eventManageVO == null) {

                        throw new IllegalArgumentException(
                                        "이벤트 등록 정보가 없습니다.");
                }

                /*
                 * 이벤트 등록 요청은 반드시 관리자 승인을 거치므로
                 * 화면 전달값과 무관하게 WAITING 상태로 저장한다.
                 */
                eventManageVO.setStatus(
                                STATUS_WAITING);

                validateEvent(
                                eventManageVO,
                                null);

                Path savedPhysicalPath = null;

                try {

                        /*
                         * 이벤트 이미지는 선택 항목이다.
                         * 선택한 경우에만 검증 후 외부 폴더에 저장한다.
                         */
                        if (eventImage != null
                                        && !eventImage.isEmpty()) {

                                validateEventImage(
                                                eventImage);

                                SavedFileInfo savedFileInfo = saveEventImage(
                                                eventImage);

                                savedPhysicalPath = savedFileInfo.physicalPath();

                                eventManageVO.setBannerImage(
                                                savedFileInfo.webPath());
                        }

                        int eventResult = businessDAO.insertEvent(
                                        eventManageVO);

                        if (eventResult != 1) {

                                throw new IllegalStateException(
                                                "이벤트 등록에 실패했습니다.");
                        }

                        if (eventManageVO.getEventNo() == null
                                        || eventManageVO.getEventNo() <= 0) {

                                throw new IllegalStateException(
                                                "등록된 이벤트 번호를 확인할 수 없습니다.");
                        }

                        /*
                         * 선택된 상품 수만큼 EVENT_PRODUCT에 연결 정보를 저장한다.
                         * productNoList / discountRateList는 validateEvent에서
                         * 이미 같은 길이로 검증되었다.
                         */
                        List<Long> productNoList = eventManageVO.getProductNoList();
                        List<Integer> discountRateList = eventManageVO.getDiscountRateList();

                        for (int i = 0; i < productNoList.size(); i++) {

                                int eventProductResult = businessDAO.insertEventProduct(
                                                eventManageVO.getEventNo(),
                                                productNoList.get(i),
                                                discountRateList.get(i));

                                if (eventProductResult != 1) {

                                        throw new IllegalStateException(
                                                        "이벤트 상품 연결 등록에 실패했습니다.");
                                }
                        }

                        notificationService.createForAdmins(
                                        NOTIFICATION_TYPE_EVENT_REQUEST,
                                        "이벤트 승인 요청",
                                        eventManageVO.getTitle()
                                                        + " 이벤트의 등록 승인 요청이 접수되었습니다.",
                                        ADMIN_EVENT_WAITING_URL,
                                        REFERENCE_TYPE_EVENT,
                                        eventManageVO.getEventNo());

                        return eventManageVO.getEventNo();

                } catch (RuntimeException e) {

                        /*
                         * DB 작업이 실패하면 이번 등록 과정에서
                         * 새로 저장한 이벤트 이미지 파일을 삭제한다.
                         */
                        deleteSavedFileQuietly(
                                        savedPhysicalPath);

                        throw e;
                }
        }

        /*
         * =========================================================
         * [페이징 리팩터링] 사업자 이벤트 목록 조회
         * currentPage/pageSize로 페이지 단위 조회한다.
         * =========================================================
         */
        @Override
        public List<EventManageVO> getEventListByBusinessNo(
                        long businessNo,
                        String keyword,
                        int currentPage,
                        int pageSize) {

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                String searchKeyword = normalizeKeyword(keyword);
                int offset = PaginationUtil.offset(currentPage, pageSize);

                List<EventManageVO> eventList = businessDAO.selectEventListByBusinessNo(
                                businessNo,
                                searchKeyword,
                                offset,
                                pageSize);

                if (eventList == null) {
                        return Collections.emptyList();
                }

                return eventList;
        }

        /* [페이징 리팩터링 추가] 사업자 이벤트 목록 전체 건수 (검색 조건 동일 적용) */
        @Override
        public int getEventListCountByBusinessNo(long businessNo, String keyword) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                return businessDAO.selectEventListCountByBusinessNo(businessNo, normalizeKeyword(keyword));
        }

        /*
         * =========================================================
         * 승인된 이벤트 단건 조회
         * =========================================================
         */
        @Override
        public EventManageVO getApprovedEventForBusiness(
                        long eventNo,
                        long businessNo) {

                if (eventNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 이벤트 번호입니다.");
                }

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                EventManageVO event = businessDAO.selectApprovedEventForBusiness(
                                eventNo,
                                businessNo);

                if (event == null) {

                        throw new IllegalArgumentException(
                                        "승인된 이벤트가 존재하지 않거나 "
                                                        + "접근 권한이 없습니다.");
                }

                event.setBusinessNo(
                                businessNo);

                List<EventProductVO> connectedProducts = businessDAO.selectEventProductListByEventNo(
                                eventNo);

                event.setConnectedProducts(
                                connectedProducts == null
                                                ? Collections.emptyList()
                                                : connectedProducts);

                return event;
        }

        /*
         * =========================================================
         * 승인된 이벤트 수정 요청
         *
         * 별도 수정 요청 테이블이 없으므로
         * EVENT와 EVENT_PRODUCT의 값을 먼저 변경하고
         * EVENT.STATUS를 WAITING으로 변경한다.
         *
         * 관리자 승인 전에는 사용자 화면에서 노출되지 않는다.
         * =========================================================
         */
        @Override
        @Transactional
        public void updateApprovedEvent(
                        EventManageVO eventManageVO,
                        MultipartFile eventImage) {

                if (eventManageVO == null) {

                        throw new IllegalArgumentException(
                                        "이벤트 수정 정보가 없습니다.");
                }

                EventManageVO existingEvent = getApprovedEventForBusiness(
                                eventManageVO.getEventNo(),
                                eventManageVO.getBusinessNo());

                /*
                 * 상품 소유권 검증 시 "지금 수정 중인 이벤트 자신과의 연결"은
                 * 중복 연결로 취급하지 않도록 eventNo를 함께 전달한다.
                 * (이 값을 안 넘기면 기존에 연결돼 있던 상품을 그대로 두고
                 * 저장하는 것만으로도 항상 검증에 실패하는 버그가 있었다.)
                 */
                validateEvent(
                                eventManageVO,
                                eventManageVO.getEventNo());

                /*
                 * 수정된 내용은 관리자 재승인을 받아야 하므로
                 * 승인 대기 상태로 변경한다.
                 */
                eventManageVO.setStatus(
                                STATUS_WAITING);

                /*
                 * 새 이미지를 선택하지 않은 경우 기존 이미지를 유지한다.
                 */
                eventManageVO.setBannerImage(
                                existingEvent.getBannerImage());

                Path savedPhysicalPath = null;

                try {

                        if (eventImage != null
                                        && !eventImage.isEmpty()) {

                                validateEventImage(
                                                eventImage);

                                SavedFileInfo savedFileInfo = saveEventImage(
                                                eventImage);

                                savedPhysicalPath = savedFileInfo.physicalPath();

                                eventManageVO.setBannerImage(
                                                savedFileInfo.webPath());
                        }

                        int updateResult = businessDAO.updateApprovedEvent(
                                        eventManageVO);

                        if (updateResult != 1) {

                                throw new IllegalStateException(
                                                "이벤트 수정에 실패했습니다.");
                        }

                        /*
                         * 연결 상품을 개별적으로 수정/추가/삭제하지 않고
                         * 기존 연결을 모두 지운 뒤 새로 선택된 목록을
                         * 다시 등록하는 방식으로 처리한다.
                         * 등록 화면과 동일한 +버튼 다중 선택 UI를 그대로
                         * 재사용할 수 있고, 상품 추가/삭제 케이스를
                         * 따로 분기하지 않아도 되어 단순하다.
                         */
                        int deletedCount = businessDAO.deleteEventProductByEventNo(
                                        eventManageVO.getEventNo());

                        if (deletedCount == 0) {

                                throw new IllegalStateException(
                                                "기존 연결 상품 삭제에 실패했습니다.");
                        }

                        List<Long> productNoList = eventManageVO.getProductNoList();
                        List<Integer> discountRateList = eventManageVO.getDiscountRateList();

                        for (int i = 0; i < productNoList.size(); i++) {

                                int eventProductResult = businessDAO.insertEventProduct(
                                                eventManageVO.getEventNo(),
                                                productNoList.get(i),
                                                discountRateList.get(i));

                                if (eventProductResult != 1) {

                                        throw new IllegalStateException(
                                                        "이벤트 연결 상품 등록에 실패했습니다.");
                                }
                        }

                } catch (RuntimeException e) {

                        deleteSavedFileQuietly(
                                        savedPhysicalPath);

                        throw e;
                }

                notificationService.createForAdmins(
                                NOTIFICATION_TYPE_EVENT_REQUEST,
                                "이벤트 재승인 요청",
                                eventManageVO.getTitle()
                                                + " 이벤트의 수정 승인 요청이 접수되었습니다.",
                                ADMIN_EVENT_WAITING_URL,
                                REFERENCE_TYPE_EVENT,
                                eventManageVO.getEventNo());
        }

        /*
         * =========================================================
         * 승인된 이벤트 연장 요청
         *
         * 별도 연장 요청 테이블이 없으므로
         * EVENT.END_DATE를 먼저 변경하고
         * EVENT.STATUS를 WAITING으로 변경한다.
         *
         * 관리자 승인 전에는 사용자 화면에서 노출되지 않는다.
         * 연장 사유는 서버 로그로만 확인한다.
         * =========================================================
         */
        @Override
        @Transactional
        public void extendApprovedEvent(
                        long eventNo,
                        long businessNo,
                        LocalDate extendEndDate,
                        String extendReason) {

                EventManageVO existingEvent = getApprovedEventForBusiness(
                                eventNo,
                                businessNo);

                if (extendEndDate == null) {

                        throw new IllegalArgumentException(
                                        "연장 종료일을 선택해주세요.");
                }

                if (!extendEndDate.isAfter(
                                existingEvent.getEndDate())) {

                        throw new IllegalArgumentException(
                                        "연장 종료일은 현재 종료일보다 이후여야 합니다.");
                }

                if (extendReason == null
                                || extendReason.isBlank()) {

                        throw new IllegalArgumentException(
                                        "이벤트 연장 사유를 입력해주세요.");
                }

                String normalizedReason = extendReason.trim();

                if (normalizedReason.length() > 1000) {

                        throw new IllegalArgumentException(
                                        "이벤트 연장 사유는 1000자 이하로 입력해주세요.");
                }
                if (log.isInfoEnabled()) {
                        log.info("===== 이벤트 연장 요청 =====");
                        log.info("이벤트 번호: {}", eventNo);
                        log.info("사업자 번호: {}", businessNo);
                        log.info("기존 종료일: {}", existingEvent.getEndDate());
                        log.info("연장 종료일: {}", extendEndDate);
                        log.info("연장 사유: {}", normalizedReason);
                }
                int updateResult = businessDAO.extendApprovedEvent(
                                eventNo,
                                businessNo,
                                extendEndDate);

                if (updateResult != 1) {

                        throw new IllegalStateException(
                                        "이벤트 연장 처리에 실패했습니다.");
                }

                notificationService.createForAdmins(
                                NOTIFICATION_TYPE_EVENT_REQUEST,
                                "이벤트 연장 승인 요청",
                                existingEvent.getTitle()
                                                + " 이벤트의 연장 승인 요청이 접수되었습니다.",
                                ADMIN_EVENT_WAITING_URL,
                                REFERENCE_TYPE_EVENT,
                                eventNo);
        }

        /*
         * =========================================================
         * 이벤트 입력값 검증
         * =========================================================
         */
        private void validateEvent(
                        EventManageVO eventManageVO,
                        Long excludeEventNo) {

                validateEventBusinessNo(eventManageVO.getBusinessNo());
                validateAndNormalizeEventTitle(eventManageVO);
                validateEventDates(eventManageVO);
                normalizeEventStatus(eventManageVO);
                validateEventProducts(eventManageVO, excludeEventNo);
        }

        private void validateEventBusinessNo(long businessNo) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException(
                                        "사업자 정보가 올바르지 않습니다.");
                }
        }

        private void validateAndNormalizeEventTitle(EventManageVO eventManageVO) {
                String title = eventManageVO.getTitle();

                if (title == null || title.isBlank()) {
                        throw new IllegalArgumentException(
                                        "이벤트명을 입력해주세요.");
                }

                title = title.trim();
                if (title.length() > 200) {
                        throw new IllegalArgumentException(
                                        "이벤트명은 200자 이하로 입력해주세요.");
                }

                eventManageVO.setTitle(title);
        }

        private void validateEventDates(EventManageVO eventManageVO) {
                if (eventManageVO.getStartDate() == null) {
                        throw new IllegalArgumentException(
                                        "이벤트 시작일을 선택해주세요.");
                }

                if (eventManageVO.getEndDate() == null) {
                        throw new IllegalArgumentException(
                                        "이벤트 종료일을 선택해주세요.");
                }

                if (eventManageVO.getEndDate().isBefore(eventManageVO.getStartDate())) {
                        throw new IllegalArgumentException(
                                        "이벤트 종료일은 시작일보다 빠를 수 없습니다.");
                }
        }

        private void normalizeEventStatus(EventManageVO eventManageVO) {
                if (!STATUS_WAITING.equals(eventManageVO.getStatus())) {
                        eventManageVO.setStatus(STATUS_WAITING);
                }
        }

        private void validateEventProducts(
                        EventManageVO eventManageVO,
                        Long excludeEventNo) {

                List<Long> productNoList = eventManageVO.getProductNoList();
                List<Integer> discountRateList = eventManageVO.getDiscountRateList();
                validateEventProductLists(productNoList, discountRateList);

                for (int i = 0; i < productNoList.size(); i++) {
                        validateEventProduct(
                                        productNoList.get(i),
                                        discountRateList.get(i),
                                        eventManageVO.getBusinessNo(),
                                        excludeEventNo);
                }

                validateDistinctEventProducts(productNoList);
        }

        private void validateEventProductLists(
                        List<Long> productNoList,
                        List<Integer> discountRateList) {

                if (productNoList == null || productNoList.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "이벤트에 연결할 상품을 선택해주세요.");
                }

                if (discountRateList == null || discountRateList.size() != productNoList.size()) {
                        throw new IllegalArgumentException(
                                        "상품별 할인율 입력값이 올바르지 않습니다.");
                }
        }

        private void validateEventProduct(
                        Long productNo,
                        Integer discountRate,
                        long businessNo,
                        Long excludeEventNo) {

                if (productNo == null || productNo <= 0) {
                        throw new IllegalArgumentException(
                                        "이벤트에 연결할 상품을 선택해주세요.");
                }

                if (discountRate == null || discountRate < 0 || discountRate > 100) {
                        throw new IllegalArgumentException(
                                        "이벤트 할인율은 0부터 100 사이여야 합니다.");
                }

                int productCount = businessDAO.countProductByBusinessNo(
                                productNo,
                                businessNo,
                                excludeEventNo);
                log.debug("productCount = {}", productCount);

                if (productCount == 0) {
                        throw new IllegalArgumentException(
                                        "선택한 상품이 존재하지 않거나 "
                                                        + "이벤트에 연결할 권한이 없습니다.");
                }
        }

        private void validateDistinctEventProducts(List<Long> productNoList) {
                long distinctProductCount = productNoList.stream()
                                .distinct()
                                .count();

                if (distinctProductCount != productNoList.size()) {
                        throw new IllegalArgumentException(
                                        "같은 상품을 중복해서 연결할 수 없습니다.");
                }
        }

        /*
         * =========================================================
         * 이벤트 이미지 검증
         * =========================================================
         */
        private void validateEventImage(
                        MultipartFile eventImage) {

                if (eventImage.getSize() > MAX_IMAGE_SIZE) {

                        throw new IllegalArgumentException(
                                        "이벤트 이미지는 10MB 이하만 "
                                                        + "업로드할 수 있습니다.");
                }

                String originalFilename = eventImage.getOriginalFilename();

                if (originalFilename == null
                                || originalFilename.isBlank()) {

                        throw new IllegalArgumentException(
                                        "이벤트 이미지 파일명이 올바르지 않습니다.");
                }

                String extension = getFileExtension(
                                originalFilename);

                if (!ALLOWED_EXTENSIONS.contains(
                                extension)) {

                        throw new IllegalArgumentException(
                                        "이벤트 이미지는 JPG, JPEG, PNG, GIF, "
                                                        + "WEBP 형식만 업로드할 수 있습니다.");
                }

                String contentType = eventImage.getContentType();

                if (contentType != null
                                && !contentType.startsWith(
                                                "image/")) {

                        throw new IllegalArgumentException(
                                        "이미지 파일만 업로드할 수 있습니다.");
                }
        }

        /*
         * =========================================================
         * 이벤트 이미지 저장
         * =========================================================
         */
        private SavedFileInfo saveEventImage(
                        MultipartFile eventImage) {

                String originalFilename = eventImage.getOriginalFilename();

                if (originalFilename == null
                                || originalFilename.isBlank()) {

                        throw new IllegalArgumentException(
                                        "이벤트 이미지 파일명이 없습니다.");
                }

                String extension = getFileExtension(
                                originalFilename);

                String savedFilename = UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                + "."
                                + extension;

                try {

                        Files.createDirectories(
                                        eventUploadDirectory);

                        Path targetPath = eventUploadDirectory
                                        .resolve(savedFilename)
                                        .normalize();

                        if (!targetPath.startsWith(
                                        eventUploadDirectory)) {

                                throw new IllegalArgumentException(
                                                "올바르지 않은 파일 경로입니다.");
                        }

                        try (var inputStream = eventImage.getInputStream()) {

                                Files.copy(
                                                inputStream,
                                                targetPath,
                                                StandardCopyOption.REPLACE_EXISTING);
                        }

                        String webPath = "/uploads/event/"
                                        + savedFilename;

                        return new SavedFileInfo(
                                        webPath,
                                        targetPath);

                } catch (IOException e) {

                        throw new IllegalStateException(
                                        "이벤트 이미지 저장 중 오류가 발생했습니다.",
                                        e);
                }
        }

        /*
         * =========================================================
         * 상품 입력값 검증
         * =========================================================
         */
        private void validateProduct(
                        GoodsManageVO goodsManageVO) {

                if (goodsManageVO.getBusinessNo() <= 0) {

                        throw new IllegalArgumentException(
                                        "사업자 정보가 올바르지 않습니다.");
                }

                String productName = goodsManageVO.getProductName();

                if (productName == null
                                || productName.isBlank()) {

                        throw new IllegalArgumentException(
                                        "상품명을 입력해주세요.");
                }

                productName = productName.trim();

                if (productName.length() > 200) {

                        throw new IllegalArgumentException(
                                        "상품명은 200자 이하로 입력해주세요.");
                }

                goodsManageVO.setProductName(
                                productName);

                String productType = goodsManageVO.getProductType();

                if (productType == null
                                || productType.isBlank()) {

                        throw new IllegalArgumentException(
                                        "상품 종류를 선택해주세요.");
                }

                productType = productType.trim()
                                .toUpperCase(Locale.ROOT);

                if (!ALLOWED_PRODUCT_TYPES.contains(
                                productType)) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 상품 종류입니다.");
                }

                goodsManageVO.setProductType(
                                productType);

                if (goodsManageVO.getPrice() <= 0) {

                        throw new IllegalArgumentException(
                                        "상품 가격은 1원 이상이어야 합니다.");
                }

                if (goodsManageVO.getDiscountRate() < 0
                                || goodsManageVO.getDiscountRate() > 100) {

                        throw new IllegalArgumentException(
                                        "할인율은 0부터 100 사이여야 합니다.");
                }

                if (goodsManageVO.getStock() < 0) {

                        throw new IllegalArgumentException(
                                        "재고는 0개 이상이어야 합니다.");
                }

                String description = goodsManageVO.getDescription();

                if (description != null) {

                        description = description.trim();

                        if (description.isEmpty()) {

                                goodsManageVO.setDescription(
                                                null);

                        } else {

                                goodsManageVO.setDescription(
                                                description);
                        }
                }
        }

        /*
         * =========================================================
         * JSONL 선택 콘텐츠를 상품 저장용 DB 데이터로 준비
         * =========================================================
         */
        private void prepareProductContent(
                        GoodsManageVO goodsManageVO) {

                Long tmdbId = goodsManageVO.getTmdbId();
                String contentType = goodsManageVO.getContentType();

                /*
                 * 신규 JSONL 검색 방식입니다.
                 */
                if (tmdbId != null
                                && tmdbId > 0
                                && contentType != null
                                && !contentType.isBlank()) {

                        validateCachedContentSelection(
                                        tmdbId,
                                        contentType);

                        int contentNo = contentService.ensureContentStored(
                                        tmdbId,
                                        contentType);

                        goodsManageVO.setContentNo(
                                        contentNo);

                        return;
                }

                /*
                 * 기존 CONTENT_NO 방식으로 되돌아온 등록 요청도 허용하여
                 * 브라우저 뒤로가기나 이전 화면과의 호환성을 유지합니다.
                 */
                if (goodsManageVO.getContentNo() <= 0) {
                        throw new IllegalArgumentException(
                                        "콘텐츠를 선택해주세요.");
                }

                ContentSearchVO content = businessDAO.selectContentByNo(
                                goodsManageVO.getContentNo());

                if (content == null) {
                        throw new IllegalArgumentException(
                                        "선택한 콘텐츠가 존재하지 않습니다.");
                }
        }

        /*
         * =========================================================
         * TMDB 배우 ID를 DB의 ACTOR_NO로 변환
         * =========================================================
         */
        private void resolveProductActor(
                        GoodsManageVO goodsManageVO) {

                Long tmdbActorId = goodsManageVO.getTmdbActorId();

                if (tmdbActorId == null
                                || tmdbActorId <= 0) {

                        normalizeActorNo(goodsManageVO);
                        return;
                }

                List<ActorSearchVO> actorList = businessDAO.selectActorListByContentNo(
                                goodsManageVO.getContentNo());

                if (actorList == null) {
                        actorList = Collections.emptyList();
                }

                for (ActorSearchVO actor : actorList) {

                        if (actor != null
                                        && tmdbActorId.equals(actor.getTmdbActorId())) {

                                goodsManageVO.setActorNo(
                                                actor.getActorNo());
                                return;
                        }
                }

                throw new IllegalArgumentException(
                                "선택한 배우를 저장된 콘텐츠 배우 목록에서 찾을 수 없습니다.");
        }

        /*
         * =========================================================
         * JSONL 콘텐츠 선택값 검증
         * =========================================================
         */
        private void validateCachedContentSelection(
                        Long tmdbId,
                        String contentType) {

                if (tmdbId == null
                                || tmdbId <= 0
                                || contentType == null
                                || contentType.isBlank()) {

                        throw new IllegalArgumentException(
                                        "올바른 콘텐츠를 선택해주세요.");
                }

                String normalizedType = contentType.trim()
                                .toUpperCase(Locale.ROOT);

                if (!"MOVIE".equals(normalizedType)
                                && !"TV".equals(normalizedType)) {

                        throw new IllegalArgumentException(
                                        "지원하지 않는 콘텐츠 유형입니다.");
                }

                CachedContentVO cachedContent = searchContentStore
                                .findByTmdbIdAndContentType(
                                                tmdbId,
                                                normalizedType);

                if (cachedContent == null) {
                        throw new IllegalArgumentException(
                                        "JSONL 공용 저장소에서 선택한 콘텐츠를 찾을 수 없습니다.");
                }
        }

        private boolean matchesCachedContent(
                        CachedContentVO cachedContent,
                        String normalizedKeyword) {

                return containsIgnoreCase(
                                cachedContent.getTitle(),
                                normalizedKeyword)
                                || containsIgnoreCase(
                                                cachedContent.getOriginalTitle(),
                                                normalizedKeyword)
                                || containsIgnoreCase(
                                                cachedContent.getSearchText(),
                                                normalizedKeyword);
        }

        private boolean containsIgnoreCase(
                        String value,
                        String normalizedKeyword) {

                return value != null
                                && value.toLowerCase(Locale.ROOT)
                                                .contains(normalizedKeyword);
        }

        private ContentSearchVO convertToContentSearchVO(
                        CachedContentVO cachedContent) {

                ContentSearchVO result = new ContentSearchVO();

                result.setTmdbId(
                                cachedContent.getTmdbId());
                result.setContentType(
                                cachedContent.getContentType());
                result.setTitle(
                                cachedContent.getTitle());
                result.setOriginalTitle(
                                cachedContent.getOriginalTitle());
                result.setPosterPath(
                                cachedContent.getPosterPath());
                result.setGenreText(
                                cachedContent.getGenreText());
                result.setAgeRating(
                                cachedContent.getAgeRating());

                return result;
        }

        /*
         * =========================================================
         * 배우 번호 정규화
         * =========================================================
         */
        private void normalizeActorNo(
                        GoodsManageVO goodsManageVO) {

                Long actorNo = goodsManageVO.getActorNo();

                if (actorNo != null
                                && actorNo <= 0) {

                        goodsManageVO.setActorNo(
                                        null);
                }
        }

        /*
         * =========================================================
         * 콘텐츠와 배우 연결 관계 검증
         * =========================================================
         */
        private void validateContentActor(
                        GoodsManageVO goodsManageVO) {

                Long actorNo = goodsManageVO.getActorNo();

                if (actorNo == null) {
                        return;
                }

                int contentActorCount = businessDAO.countContentActor(
                                goodsManageVO.getContentNo(),
                                actorNo);

                if (contentActorCount == 0) {

                        throw new IllegalArgumentException(
                                        "선택한 배우는 해당 콘텐츠에 "
                                                        + "연결된 배우가 아닙니다.");
                }
        }

        /*
         * =========================================================
         * 상품 이미지 검증
         * =========================================================
         */
        private void validateProductImage(
                        MultipartFile productImage) {

                if (productImage == null
                                || productImage.isEmpty()) {

                        throw new IllegalArgumentException(
                                        "상품 대표 이미지를 선택해주세요.");
                }

                if (productImage.getSize() > MAX_IMAGE_SIZE) {

                        throw new IllegalArgumentException(
                                        "상품 이미지는 10MB 이하만 "
                                                        + "업로드할 수 있습니다.");
                }

                String originalFilename = productImage.getOriginalFilename();

                if (originalFilename == null
                                || originalFilename.isBlank()) {

                        throw new IllegalArgumentException(
                                        "상품 이미지 파일명이 올바르지 않습니다.");
                }

                String extension = getFileExtension(
                                originalFilename);

                if (!ALLOWED_EXTENSIONS.contains(
                                extension)) {

                        throw new IllegalArgumentException(
                                        "상품 이미지는 JPG, JPEG, PNG, GIF, "
                                                        + "WEBP 형식만 업로드할 수 있습니다.");
                }

                String contentType = productImage.getContentType();

                if (contentType != null
                                && !contentType.startsWith(
                                                "image/")) {

                        throw new IllegalArgumentException(
                                        "이미지 파일만 업로드할 수 있습니다.");
                }
        }

        /*
         * =========================================================
         * 상품 이미지 저장
         * =========================================================
         */
        private SavedFileInfo saveProductImage(
                        MultipartFile productImage) {

                String originalFilename = productImage.getOriginalFilename();

                if (originalFilename == null
                                || originalFilename.isBlank()) {

                        throw new IllegalArgumentException(
                                        "상품 이미지 파일명이 없습니다.");
                }

                String extension = getFileExtension(
                                originalFilename);

                String savedFilename = UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                + "."
                                + extension;

                try {

                        Files.createDirectories(
                                        productUploadDirectory);

                        Path targetPath = productUploadDirectory
                                        .resolve(savedFilename)
                                        .normalize();

                        if (!targetPath.startsWith(
                                        productUploadDirectory)) {

                                throw new IllegalArgumentException(
                                                "올바르지 않은 파일 경로입니다.");
                        }

                        try (var inputStream = productImage.getInputStream()) {

                                Files.copy(
                                                inputStream,
                                                targetPath,
                                                StandardCopyOption.REPLACE_EXISTING);
                        }

                        String webPath = "/uploads/product/"
                                        + savedFilename;

                        return new SavedFileInfo(
                                        webPath,
                                        targetPath);

                } catch (IOException e) {

                        throw new IllegalStateException(
                                        "상품 이미지 저장 중 오류가 발생했습니다.",
                                        e);
                }
        }

        /*
         * =========================================================
         * 파일 확장자 추출
         * =========================================================
         */
        private String getFileExtension(
                        String filename) {

                int dotIndex = filename.lastIndexOf('.');

                if (dotIndex < 0
                                || dotIndex == filename.length() - 1) {

                        return "";
                }

                return filename
                                .substring(dotIndex + 1)
                                .toLowerCase(Locale.ROOT);
        }

        /*
         * =========================================================
         * 상품 등록 또는 수정 실패 시 저장된 이미지 삭제
         * =========================================================
         */
        /*
         * [상품 이미지 개별 삭제 추가]
         * 기존 이미지 파일은 DB 변경이 커밋된 뒤에만 삭제합니다.
         */
        private void registerProductImageFilesForDeletionAfterCommit(
                        List<Path> physicalPathList) {

                if (physicalPathList == null || physicalPathList.isEmpty()) {
                        return;
                }

                List<Path> deletionTargetList = new ArrayList<Path>(physicalPathList);

                if (!TransactionSynchronizationManager.isSynchronizationActive()) {
                        for (Path deletionTarget : deletionTargetList) {
                                deleteSavedFileQuietly(deletionTarget);
                        }
                        return;
                }

                TransactionSynchronizationManager.registerSynchronization(
                                new TransactionSynchronization() {
                                        @Override
                                        public void afterCommit() {
                                                for (Path deletionTarget : deletionTargetList) {
                                                        deleteSavedFileQuietly(deletionTarget);
                                                }
                                        }
                                });
        }

        /*
         * [상품 이미지 개별 삭제 추가]
         * 실제 선택된 새 세부 이미지 수를 계산합니다.
         */
        private int countSelectedDetailImages(
                        MultipartFile[] detailImages) {

                if (detailImages == null) {
                        return 0;
                }

                int selectedCount = 0;

                for (MultipartFile detailImage : detailImages) {
                        if (detailImage != null && !detailImage.isEmpty()) {
                                selectedCount++;
                        }
                }

                return selectedCount;
        }

        /*
         * [상품 이미지 개별 삭제 추가]
         * /uploads/product/파일명 형태의 웹 경로를 실제 상품 업로드 경로로 변환합니다.
         * 경로 순회 문자열이나 상품 업로드 폴더 밖의 경로는 삭제하지 않습니다.
         */
        private Path resolveProductImagePhysicalPath(
                        String imageWebPath) {

                if (imageWebPath == null || imageWebPath.isBlank()) {
                        return null;
                }

                String normalizedWebPath = imageWebPath.replace('\\', '/');
                String productWebPrefix = "/uploads/product/";

                if (!normalizedWebPath.startsWith(productWebPrefix)) {
                        return null;
                }

                String fileName = normalizedWebPath.substring(productWebPrefix.length());

                if (fileName.isBlank() || fileName.contains("/")) {
                        return null;
                }

                Path physicalPath = productUploadDirectory.resolve(fileName).normalize();

                if (!physicalPath.startsWith(productUploadDirectory)) {
                        return null;
                }

                return physicalPath;
        }

        private void deleteSavedFileQuietly(
                        Path savedPhysicalPath) {

                if (savedPhysicalPath == null) {
                        return;
                }

                try {

                        Files.deleteIfExists(
                                        savedPhysicalPath);

                } catch (IOException e) {

                        log.warn("상품 이미지 파일 삭제 실패: {}", savedPhysicalPath, e);
                }
        }

        /*
         * =========================================================
         * 이미지 저장 결과
         * =========================================================
         */
        private record SavedFileInfo(
                        String webPath,
                        Path physicalPath) {
        }
}