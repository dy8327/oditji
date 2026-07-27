package com.project.oditji.business.service;

import java.io.IOException;
import java.time.LocalDate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
public class BusinessServiceImpl
                implements BusinessService {

        private static final long MAX_IMAGE_SIZE = 10L * 1024L * 1024L;

        /* 마이페이지 대시보드에 노출할 인기 상품 개수 */
        private static final int POPULAR_PRODUCT_LIMIT = 5;

        private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
                        "jpg",
                        "jpeg",
                        "png",
                        "gif",
                        "webp");

        private static final Set<String> ALLOWED_PRODUCT_TYPES = Set.of(
                        "CLOTHES",
                        "PROP",
                        "GOODS",
                        "OST",
                        "BOOK",
                        "FIGURE",
                        "ETC");

        private final BusinessDAO businessDAO;
        private final Path productUploadDirectory;

        /*
         * application.properties를 추가로 수정하지 않고
         * 기존 ODITJI 외부 업로드 폴더 구조를 그대로 사용한다.
         */
        private final Path eventUploadDirectory = Paths.get(
                        "C:/oditji/uploads/event")
                        .toAbsolutePath()
                        .normalize();

        public BusinessServiceImpl(
                        BusinessDAO businessDAO,

                        @Value("${oditji.upload.product-path:"
                                        + "uploads/product}") String productUploadPath) {

                this.businessDAO = businessDAO;

                this.productUploadDirectory = Paths.get(
                                productUploadPath)
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
         * 오늘 매출/판매량/구매 고객 수/클릭 수/입금 대기 정산액/
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
        public List<OrderVO> getBusinessOrderList(long businessNo) {
                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                List<OrderVO> orderList = businessDAO.selectBusinessOrderList(businessNo);
                if (orderList == null || orderList.isEmpty()) {

                        return Collections.emptyList();
                }

                List<OrderItemVO> itemList = getBusinessOrderItemList(businessNo);

                /* ORDER_NO별 주문 상품 묶기 */
                Map<Long, List<OrderItemVO>> itemMap = new HashMap<>();
                for (OrderItemVO item : itemList) {

                        itemMap.computeIfAbsent(item.getOrderNo(), key -> new java.util.ArrayList<>())
                                        .add(item);
                }

                /* 각 주문에 해당 상품 목록 연결 */
                for (OrderVO order : orderList) {
                        order.setItems(itemMap.getOrDefault(order.getOrderNo(), Collections.emptyList()));
                }

                return orderList;
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

        // 사업자 주문 상세 조회
        @Override
        public OrderVO getBusinessOrderDetail(long businessNo, long orderNo) {
                if (businessNo <= 0 || orderNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 주문 정보입니다.");
                }

                OrderVO order = businessDAO.selectBusinessOrderDetail(businessNo, orderNo);

                if (order == null) {
                        return null;
                }

                List<OrderItemVO> itemList = businessDAO.selectBusinessOrderItemDetailList(businessNo, orderNo);
                order.setItems(itemList == null ? Collections.emptyList() : itemList);

                return order;
        }

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

        /* 날짜별 판매 내역 조회. */
        @Override
        public List<SettlementManageVO> getBusinessSalesHistory(
                        long businessNo,
                        LocalDate startDate,
                        LocalDate endDate) {

                validateSalesSearchCondition(businessNo, startDate, endDate);

                List<SettlementManageVO> salesHistory = businessDAO.selectBusinessSalesHistory(
                                businessNo, startDate, endDate);

                return salesHistory == null ? Collections.emptyList() : salesHistory;
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
                        String keyword) {

                if (businessNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 사업자 번호입니다.");
                }

                String normalizedStatus = normalizeDeliveryStatusFilter(status);
                String normalizedKeyword = keyword == null ? null : keyword.trim();

                if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
                        normalizedKeyword = null;
                }

                List<DeliveryManageVO> deliveryList = businessDAO.selectBusinessDeliveryList(
                                businessNo, normalizedStatus, normalizedKeyword);

                return deliveryList == null ? Collections.emptyList() : deliveryList;
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

                if (businessNo <= 0 || orderItemNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 배송 정보입니다.");
                }

                DeliveryManageVO currentItem = businessDAO.selectBusinessDeliveryItem(
                                businessNo, orderItemNo);

                if (currentItem == null) {
                        throw new IllegalArgumentException("해당 주문상품을 확인할 수 없습니다.");
                }

                String currentStatus = currentItem.getStatus();
                if ("CANCEL_REQUESTED".equals(currentStatus)
                                || "CANCELED".equals(currentStatus)
                                || "REFUNDED".equals(currentStatus)) {
                        throw new IllegalStateException("취소 또는 환불 처리 중인 상품은 배송 상태를 변경할 수 없습니다.");
                }

                String normalizedStatus = normalizeDeliveryUpdateStatus(status);
                validateDeliveryStatusTransition(currentStatus, normalizedStatus);
                String normalizedCourier = courier == null ? null : courier.trim();
                String normalizedTrackingNumber = trackingNumber == null ? null : trackingNumber.trim();

                if (("SHIPPING".equals(normalizedStatus) || "DELIVERED".equals(normalizedStatus))
                                && (normalizedCourier == null || normalizedCourier.isEmpty())) {
                        throw new IllegalArgumentException("배송 중 또는 배송 완료 처리 시 택배사를 선택해주세요.");
                }

                if (("SHIPPING".equals(normalizedStatus) || "DELIVERED".equals(normalizedStatus))
                                && (normalizedTrackingNumber == null || normalizedTrackingNumber.isEmpty())) {
                        throw new IllegalArgumentException("배송 중 또는 배송 완료 처리 시 운송장 번호를 입력해주세요.");
                }

                if (normalizedTrackingNumber != null && normalizedTrackingNumber.length() > 100) {
                        throw new IllegalArgumentException("운송장 번호는 100자 이하로 입력해주세요.");
                }

                if (normalizedCourier != null && normalizedCourier.length() > 50) {
                        throw new IllegalArgumentException("택배사명은 50자 이하로 입력해주세요.");
                }

                DeliveryManageVO delivery = new DeliveryManageVO();
                delivery.setBusinessNo(businessNo);
                delivery.setOrderItemNo(orderItemNo);
                delivery.setOrderNo(currentItem.getOrderNo());
                delivery.setCourier(normalizedCourier);
                delivery.setTrackingNumber(normalizedTrackingNumber);
                delivery.setStatus(normalizedStatus);

                int deliveryResult = businessDAO.mergeDelivery(delivery);
                if (deliveryResult != 1) {
                        throw new IllegalStateException("배송 정보 저장에 실패했습니다.");
                }

                int itemResult = businessDAO.updateOrderItemDeliveryStatus(
                                businessNo, orderItemNo, normalizedStatus);
                if (itemResult != 1) {
                        throw new IllegalStateException("주문상품 배송 상태 변경에 실패했습니다.");
                }

                businessDAO.updateOrderStatusByOrderItem(currentItem.getOrderNo());
        }

        /* 배송 목록 검색에 사용할 상태값을 검증하고 정규화. */
        private String normalizeDeliveryStatusFilter(String status) {
                if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
                        return null;
                }

                String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
                if (!Set.of("PREPARING", "SHIPPING", "DELIVERED").contains(normalizedStatus)) {
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
                if (!Set.of("PREPARING", "SHIPPING", "DELIVERED").contains(normalizedStatus)) {
                        throw new IllegalArgumentException("변경할 수 없는 배송 상태입니다.");
                }

                return normalizedStatus;
        }

        /* 배송 상태가 이전 단계로 돌아가지 않도록 검사 */
        private void validateDeliveryStatusTransition(String currentStatus, String nextStatus) {
                Map<String, Integer> statusOrder = Map.of(
                                "PAID", 0,
                                "PREPARING", 1,
                                "SHIPPING", 2,
                                "DELIVERED", 3);

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
        public long registerProduct(GoodsManageVO goodsManageVO, MultipartFile productImage) {

                if (goodsManageVO == null) {
                        throw new IllegalArgumentException("상품 등록 정보가 없습니다.");
                }

                validateProduct(goodsManageVO);
                normalizeActorNo(goodsManageVO);
                validateContentActor(goodsManageVO);
                validateProductImage(productImage);
                Path savedPhysicalPath = null;

                try {
                        SavedFileInfo savedFileInfo = saveProductImage(productImage);
                        savedPhysicalPath = savedFileInfo.physicalPath();
                        goodsManageVO.setImagePath(savedFileInfo.webPath());
                        goodsManageVO.setIsMain("Y");
                        goodsManageVO.setStatus("WAITING");

                        int productResult = businessDAO.insertProduct(goodsManageVO);
                        if (productResult != 1) {
                                throw new IllegalStateException("상품 등록에 실패했습니다.");
                        }
                        if (goodsManageVO.getProductNo() <= 0) {
                                throw new IllegalStateException("등록된 상품 번호를 확인할 수 없습니다.");
                        }

                        int imageResult = businessDAO.insertProductImage(goodsManageVO);

                        if (imageResult != 1) {
                                throw new IllegalStateException("상품 대표 이미지 등록에 실패했습니다.");
                        }
                        return goodsManageVO.getProductNo();

                } catch (RuntimeException e) {
                        deleteSavedFileQuietly(savedPhysicalPath);

                        throw e;
                }
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
         * 사업자가 등록한 상품 목록 조회
         * =========================================================
         */
        @Override
        public List<GoodsManageVO> getProductListByBusinessNo(
                        long businessNo) {

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                List<GoodsManageVO> productList = businessDAO.selectProductListByBusinessNo(
                                businessNo);

                if (productList == null) {
                        return Collections.emptyList();
                }

                return productList;
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
                        MultipartFile productImage) {

                if (goodsManageVO == null) {

                        throw new IllegalArgumentException(
                                        "상품 수정 정보가 없습니다.");
                }

                if (goodsManageVO.getProductNo() <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 상품 번호입니다.");
                }

                GoodsManageVO existingProduct = businessDAO.selectProductForUpdate(
                                goodsManageVO.getProductNo(),
                                goodsManageVO.getBusinessNo());

                if (existingProduct == null) {

                        throw new IllegalArgumentException(
                                        "상품이 존재하지 않거나 "
                                                        + "수정 권한이 없습니다.");
                }

                if ("DELETE_REQUESTED".equals(
                                existingProduct.getStatus())) {

                        throw new IllegalStateException(
                                        "삭제 요청 중인 상품은 수정할 수 없습니다.");
                }

                validateProduct(
                                goodsManageVO);

                normalizeActorNo(
                                goodsManageVO);

                validateContentActor(
                                goodsManageVO);

                /*
                 * 수정 요청이 들어오면 관리자 재승인을 받을 수 있도록
                 * 승인 상태를 WAITING으로 변경한다.
                 */
                goodsManageVO.setStatus(
                                "WAITING");

                Path savedPhysicalPath = null;

                try {

                        int updateResult = businessDAO.updateProduct(
                                        goodsManageVO);

                        if (updateResult != 1) {

                                throw new IllegalStateException(
                                                "상품 수정에 실패했습니다.");
                        }

                        /*
                         * 새 이미지가 선택된 경우에만 이미지 정보를 변경한다.
                         */
                        if (productImage != null
                                        && !productImage.isEmpty()) {

                                validateProductImage(
                                                productImage);

                                SavedFileInfo savedFileInfo = saveProductImage(
                                                productImage);

                                savedPhysicalPath = savedFileInfo.physicalPath();

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

                } catch (RuntimeException e) {

                        /*
                         * DB 작업이 실패하면 이번 수정 과정에서
                         * 새로 저장한 이미지 파일만 삭제한다.
                         */
                        deleteSavedFileQuietly(
                                        savedPhysicalPath);

                        throw e;
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
                System.out.println(
                                "===== 상품 삭제 요청 =====");

                System.out.println(
                                "상품 번호: "
                                                + productNo);

                System.out.println(
                                "사업자 번호: "
                                                + businessNo);

                System.out.println(
                                "상품명: "
                                                + existingProduct.getProductName());

                System.out.println(
                                "삭제 사유: "
                                                + normalizedReason);

                int updateResult = businessDAO.updateProductDeleteRequest(
                                productNo,
                                businessNo);

                if (updateResult != 1) {

                        throw new IllegalStateException(
                                        "상품 삭제 요청 처리에 실패했습니다.");
                }
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
                                "WAITING");

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

                        if (eventManageVO.getEventNo() <= 0) {

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
         * 사업자 이벤트 목록 조회
         * =========================================================
         */
        @Override
        public List<EventManageVO> getEventListByBusinessNo(
                        long businessNo,
                        String keyword) {

                if (businessNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 사업자 번호입니다.");
                }

                String searchKeyword = keyword;

                if (searchKeyword != null) {

                        searchKeyword = searchKeyword.trim();

                        if (searchKeyword.isEmpty()) {
                                searchKeyword = null;
                        }
                }

                List<EventManageVO> eventList = businessDAO.selectEventListByBusinessNo(
                                businessNo,
                                searchKeyword);

                if (eventList == null) {
                        return Collections.emptyList();
                }

                return eventList;
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
                                "WAITING");

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
                        java.time.LocalDate extendEndDate,
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

                System.out.println(
                                "===== 이벤트 연장 요청 =====");

                System.out.println(
                                "이벤트 번호: "
                                                + eventNo);

                System.out.println(
                                "사업자 번호: "
                                                + businessNo);

                System.out.println(
                                "기존 종료일: "
                                                + existingEvent.getEndDate());

                System.out.println(
                                "연장 종료일: "
                                                + extendEndDate);

                System.out.println(
                                "연장 사유: "
                                                + normalizedReason);

                int updateResult = businessDAO.extendApprovedEvent(
                                eventNo,
                                businessNo,
                                extendEndDate);

                if (updateResult != 1) {

                        throw new IllegalStateException(
                                        "이벤트 연장 처리에 실패했습니다.");
                }
        }

        /*
         * =========================================================
         * 이벤트 입력값 검증
         * =========================================================
         */
        private void validateEvent(
                        EventManageVO eventManageVO,
                        Long excludeEventNo) {

                if (eventManageVO.getBusinessNo() <= 0) {

                        throw new IllegalArgumentException(
                                        "사업자 정보가 올바르지 않습니다.");
                }

                String title = eventManageVO.getTitle();

                if (title == null
                                || title.isBlank()) {

                        throw new IllegalArgumentException(
                                        "이벤트명을 입력해주세요.");
                }

                title = title.trim();

                if (title.length() > 200) {

                        throw new IllegalArgumentException(
                                        "이벤트명은 200자 이하로 입력해주세요.");
                }

                eventManageVO.setTitle(
                                title);

                if (eventManageVO.getStartDate() == null) {

                        throw new IllegalArgumentException(
                                        "이벤트 시작일을 선택해주세요.");
                }

                if (eventManageVO.getEndDate() == null) {

                        throw new IllegalArgumentException(
                                        "이벤트 종료일을 선택해주세요.");
                }

                if (eventManageVO.getEndDate().isBefore(
                                eventManageVO.getStartDate())) {

                        throw new IllegalArgumentException(
                                        "이벤트 종료일은 시작일보다 빠를 수 없습니다.");
                }

                /*
                 * 이벤트 등록/수정은 항상 관리자 승인 대기 상태로 저장되어야 한다.
                 * registerEvent/updateApprovedEvent에서 이미 "WAITING"으로
                 * 강제 설정하지만, 화면이나 다른 호출 경로에서 잘못된 값이
                 * 넘어오는 경우를 대비해 여기서도 한 번 더 방어한다.
                 *
                 * EVENT.STATUS는 CK_EVENT_STATUS 제약조건에 의해
                 * WAITING / APPROVED / END / REJECTED / DELETED 값만 허용되며,
                 * 사업자가 직접 지정할 수 있는 값은 WAITING뿐이다.
                 */
                if (!"WAITING".equals(
                                eventManageVO.getStatus())) {

                        eventManageVO.setStatus(
                                        "WAITING");
                }

                /*
                 * EVENT 테이블에는 BUSINESS_NO가 없으므로
                 * 사업자별 이벤트 소유권 확인을 위해 연결 상품은 최소 1개 필수이다.
                 *
                 * 화면(business.js)에서 addProductButton으로 여러 개의
                 * 상품 행을 추가하므로 productNoList / discountRateList가
                 * 여러 건 전달될 수 있다.
                 */
                List<Long> productNoList = eventManageVO.getProductNoList();
                List<Integer> discountRateList = eventManageVO.getDiscountRateList();

                if (productNoList == null
                                || productNoList.isEmpty()) {

                        throw new IllegalArgumentException(
                                        "이벤트에 연결할 상품을 선택해주세요.");
                }

                if (discountRateList == null
                                || discountRateList.size() != productNoList.size()) {

                        throw new IllegalArgumentException(
                                        "상품별 할인율 입력값이 올바르지 않습니다.");
                }

                for (int i = 0; i < productNoList.size(); i++) {

                        Long productNo = productNoList.get(i);
                        Integer discountRate = discountRateList.get(i);

                        if (productNo == null
                                        || productNo <= 0) {

                                throw new IllegalArgumentException(
                                                "이벤트에 연결할 상품을 선택해주세요.");
                        }

                        if (discountRate == null
                                        || discountRate < 0
                                        || discountRate > 100) {

                                throw new IllegalArgumentException(
                                                "이벤트 할인율은 0부터 100 사이여야 합니다.");
                        }

                        /*
                         * 화면에서 전달된 PRODUCT_NO를 그대로 신뢰하지 않고
                         * 로그인한 사업자가 등록한 상품인지 서버에서 다시 확인한다.
                         */
                        int productCount = businessDAO.countProductByBusinessNo(
                                        productNo,
                                        eventManageVO.getBusinessNo(),
                                        excludeEventNo);
                        System.out.println("productCount = " + productCount);

                        if (productCount == 0) {

                                throw new IllegalArgumentException(
                                                "선택한 상품이 존재하지 않거나 "
                                                                + "이벤트에 연결할 권한이 없습니다.");
                        }
                }

                /*
                 * 같은 상품을 중복 선택한 경우도 방지한다.
                 */
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
        private void deleteSavedFileQuietly(
                        Path savedPhysicalPath) {

                if (savedPhysicalPath == null) {
                        return;
                }

                try {

                        Files.deleteIfExists(
                                        savedPhysicalPath);

                } catch (IOException e) {

                        System.err.println(
                                        "상품 이미지 파일 삭제 실패: "
                                                        + savedPhysicalPath);
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