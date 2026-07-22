package com.project.oditji.business.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project.oditji.business.dao.BusinessDAO;
import com.project.oditji.business.vo.ActorSearchVO;
import com.project.oditji.business.vo.BusinessVO;
import com.project.oditji.business.vo.ContentSearchVO;
import com.project.oditji.business.vo.EventManageVO;
import com.project.oditji.business.vo.EventProductVO;
import com.project.oditji.business.vo.GoodsManageVO;
import com.project.oditji.business.vo.BusinessDashboardVO;

@Service
public class BusinessServiceImpl
                implements BusinessService {

        private static final long MAX_IMAGE_SIZE = 10L * 1024L * 1024L;

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

        /* 로그인 회원과 연결된 사업자 조회 */
        @Override
        public BusinessVO getBusinessByMemberNo(long memberNo) {

                if (memberNo <= 0) {
                        throw new IllegalArgumentException("올바르지 않은 회원 번호입니다.");
                }

                return businessDAO.selectBusinessByMemberNo(memberNo);
        }

        /* 사업자 대쉬보드 */
        @Override
        public BusinessDashboardVO getBusinessDashboard(long businessNo) {
        return businessDAO.selectBusinessDashboard(businessNo);
        }

        /* 사업자 인기 상품 조회 */
        @Override
        public List<GoodsManageVO> getPopularProducts(long businessNo) {
        return businessDAO.selectPopularProducts(businessNo);
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

        /* 상품 등록 */
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

        /* 콘텐츠 검색 */
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

        /* 콘텐츠 단건 조회 */
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

        /* 선택한 콘텐츠에 연결된 배우 목록 조회 */
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
         * 이벤트 등록/수정 화면의 상품 검색 모달 전용
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

        /* 사업자가 등록한 상품 목록 조회 */
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

        /* 상품 수정 화면용 상품 단건 조회 */
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
         * 새 이미지가 없으면 기존 상품 이미지를 유지
         * 새 이미지가 있으면 기존 대표 이미지의 경로를 변경
         * 대표 이미지 행이 없다면 새 대표 이미지 행을 등록
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

                /* 수정 요청이 들어오면 관리자 재승인을 받을 수 있도록 승인 상태를 WAITING으로 변경 */
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

                        /* 새 이미지가 선택된 경우에만 이미지 정보를 변경 */
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

                                /* 기존 대표 이미지가 없는 상품이면 새로운 대표 이미지 행을 등록 */
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

                        /* DB 작업이 실패하면 이번 수정 과정에서 새로 저장한 이미지 파일만 삭제 */
                        deleteSavedFileQuietly(
                                        savedPhysicalPath);

                        throw e;
                }
        }

        /*
         * =========================================================
         * 상품 삭제 요청
         * 실제 상품과 이미지를 즉시 삭제하지 않고
         * PRODUCT.STATUS를 DELETE_REQUESTED로 변경
         * 현재 PRODUCT 테이블에는 삭제 사유 컬럼이 없으므로
         * 삭제 사유는 검증 후 개발 로그에만 출력
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

                /* PRODUCT_NO와 BUSINESS_NO를 함께 조회하여 로그인한 사업자가 등록한 상품인지 검증 */
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

                /* PRODUCT 테이블에 삭제 사유를 저장할 컬럼이 없으므로 현재 단계에서는 서버 로그로 확인 */
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
         * EVENT 테이블에 이벤트 기본 정보를 먼저 저장하고,
         * 상품이 선택된 경우 EVENT_PRODUCT 테이블에도 연결 정보를 저장
         * 두 DB 작업은 하나의 트랜잭션으로 처리
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

                /* 이벤트 등록 요청은 반드시 관리자 승인을 거치므로 화면 전달값과 무관하게 WAITING 상태로 저장 */
                eventManageVO.setStatus(
                                "WAITING");

                validateEvent(
                                eventManageVO);

                Path savedPhysicalPath = null;

                try {

                        /* 이벤트 이미지는 선택 항목. 선택한 경우에만 검증 후 외부 폴더에 저장 */
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
                         * 선택된 상품 수만큼 EVENT_PRODUCT에 연결 정보를 저장.
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

                        /* DB 작업이 실패하면 이번 등록 과정에서 새로 저장한 이벤트 이미지 파일을 삭제 */
                        deleteSavedFileQuietly(
                                        savedPhysicalPath);

                        throw e;
                }
        }

        /* 사업자 이벤트 목록 조회 */
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

        /* 승인된 이벤트 단건 조회 */
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
         * 승인된 이벤트 수정 요청.
         * 별도 수정 요청 테이블이 없으므로
         * EVENT와 EVENT_PRODUCT의 값을 먼저 변경하고
         * EVENT.STATUS를 WAITING으로 변경.
         * 관리자 승인 전에는 사용자 화면에서 노출 금지.
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

                validateEvent(
                                eventManageVO);

                /* 수정된 내용은 관리자 재승인을 받아야 하므로 승인 대기 상태로 변경. */
                eventManageVO.setStatus(
                                "WAITING");

                /* 새 이미지를 선택하지 않은 경우 기존 이미지를 유지. */
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
                         * 다시 등록하는 방식으로 처리.
                         * 등록 화면과 동일한 +버튼 다중 선택 UI를 그대로
                         * 재사용할 수 있고, 상품 추가/삭제 케이스를
                         * 따로 분기하지 않아도 되어 단순.
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
         * 별도 연장 요청 테이블이 없으므로
         * EVENT.END_DATE를 먼저 변경하고
         * EVENT.STATUS를 WAITING으로 변경.
         * 관리자 승인 전에는 사용자 화면에서 노출 금지.
         * 연장 사유는 서버 로그로만 확인.
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

        /* 이벤트 입력값 검증 */
        private void validateEvent(
                        EventManageVO eventManageVO) {

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
                 * 이벤트 등록/수정은 항상 관리자 승인 대기 상태로 저장되어야 함.
                 * registerEvent/updateApprovedEvent에서 이미 "WAITING"으로
                 * 강제 설정하지만, 화면이나 다른 호출 경로에서 잘못된 값이
                 * 넘어오는 경우를 대비해 여기서도 한 번 더 방어.
                 * EVENT.STATUS는 CK_EVENT_STATUS 제약조건에 의해
                 * WAITING / APPROVED / END / REJECTED / DELETED 값만 허용되며,
                 * 사업자가 직접 지정할 수 있는 값은 WAITING뿐임.
                 */
                if (!"WAITING".equals(
                                eventManageVO.getStatus())) {

                        eventManageVO.setStatus(
                                        "WAITING");
                }

                /*
                 * EVENT 테이블에는 BUSINESS_NO가 없으므로 사업자별 이벤트 소유권 확인을 위해 연결 상품은 최소 1개 필수.
                 * 화면(business.js)에서 addProductButton으로 여러 개의
                 * 상품 행을 추가하므로 productNoList / discountRateList가
                 * 여러 건 전달될 수 있음.
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

                        /* 화면에서 전달된 PRODUCT_NO를 그대로 신뢰하지 않고 로그인한 사업자가 등록한 상품인지 서버에서 다시 확인. */
                        int productCount = businessDAO.countProductByBusinessNo(
                                        productNo,
                                        eventManageVO.getBusinessNo());
                        System.out.println("productCount = " + productCount);

                        if (productCount == 0) {

                                throw new IllegalArgumentException(
                                                "선택한 상품이 존재하지 않거나 "
                                                                + "이벤트에 연결할 권한이 없습니다.");
                        }
                }

                /* 같은 상품을 중복 선택한 경우 방지. */
                long distinctProductCount = productNoList.stream()
                                .distinct()
                                .count();

                if (distinctProductCount != productNoList.size()) {

                        throw new IllegalArgumentException(
                                        "같은 상품을 중복해서 연결할 수 없습니다.");
                }
        }

        /* 이벤트 이미지 검증 */
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

        /* 이벤트 이미지 저장 */
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

        /* 상품 입력값 검증 */
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
                                .toUpperCase();

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

        /* 배우 번호 정규화 */
        private void normalizeActorNo(
                        GoodsManageVO goodsManageVO) {

                Long actorNo = goodsManageVO.getActorNo();

                if (actorNo != null
                                && actorNo <= 0) {

                        goodsManageVO.setActorNo(
                                        null);
                }
        }

        /* 콘텐츠와 배우 연결 관계 검증 */
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

        /* 상품 이미지 검증 */
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

        /* 상품 이미지 저장 */
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

        /* 파일 확장자 추출 */
        private String getFileExtension(
                        String filename) {

                int dotIndex = filename.lastIndexOf('.');

                if (dotIndex < 0
                                || dotIndex == filename.length() - 1) {

                        return "";
                }

                return filename
                                .substring(dotIndex + 1)
                                .toLowerCase();
        }

        /* 상품 등록 또는 수정 실패 시 저장된 이미지 삭제 */
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

        /* 이미지 저장 결과 */
        private record SavedFileInfo(
                        String webPath,
                        Path physicalPath) {
        }
}