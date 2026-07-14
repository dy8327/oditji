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
import com.project.oditji.business.vo.GoodsManageVO;

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

        public BusinessServiceImpl(
                        BusinessDAO businessDAO,

                        @Value("${oditji.upload.product-path:"
                                        + "uploads/product}") String productUploadPath) {

                this.businessDAO = businessDAO;

                this.productUploadDirectory = Paths.get(productUploadPath)
                                .toAbsolutePath()
                                .normalize();
        }

        /*
         * =========================================================
         * 로그인 회원과 연결된 사업자 조회
         * =========================================================
         */
        @Override
        public BusinessVO getBusinessByMemberNo(
                        long memberNo) {

                if (memberNo <= 0) {

                        throw new IllegalArgumentException(
                                        "올바르지 않은 회원 번호입니다.");
                }

                return businessDAO
                                .selectBusinessByMemberNo(
                                                memberNo);
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
                        MultipartFile productImage) {

                if (goodsManageVO == null) {

                        throw new IllegalArgumentException(
                                        "상품 등록 정보가 없습니다.");
                }

                validateProduct(
                                goodsManageVO);

                normalizeActorNo(
                                goodsManageVO);

                validateContentActor(
                                goodsManageVO);

                validateProductImage(
                                productImage);

                Path savedPhysicalPath = null;

                try {

                        SavedFileInfo savedFileInfo = saveProductImage(
                                        productImage);

                        savedPhysicalPath = savedFileInfo.physicalPath();

                        goodsManageVO.setImagePath(
                                        savedFileInfo.webPath());

                        goodsManageVO.setIsMain(
                                        "Y");

                        goodsManageVO.setStatus(
                                        "WAITING");

                        int productResult = businessDAO.insertProduct(
                                        goodsManageVO);

                        if (productResult != 1) {

                                throw new IllegalStateException(
                                                "상품 등록에 실패했습니다.");
                        }

                        if (goodsManageVO.getProductNo() <= 0) {

                                throw new IllegalStateException(
                                                "등록된 상품 번호를 확인할 수 없습니다.");
                        }

                        int imageResult = businessDAO.insertProductImage(
                                        goodsManageVO);

                        if (imageResult != 1) {

                                throw new IllegalStateException(
                                                "상품 대표 이미지 등록에 실패했습니다.");
                        }

                        return goodsManageVO.getProductNo();

                } catch (RuntimeException e) {

                        deleteSavedFileQuietly(
                                        savedPhysicalPath);

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

                List<ContentSearchVO> contentList = businessDAO.selectContentList(
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
                                .toLowerCase();
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