package com.project.oditji.business.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
public class BusinessServiceImpl implements BusinessService {

    private static final long MAX_IMAGE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "gif",
                    "webp");

    private final BusinessDAO businessDAO;
    private final Path productUploadDirectory;

    public BusinessServiceImpl(
            BusinessDAO businessDAO,

            @Value(
                    "${oditji.upload.product-path:"
                            + "uploads/product}")
            String productUploadPath) {

        this.businessDAO = businessDAO;

        this.productUploadDirectory =
                Paths.get(productUploadPath)
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

        return businessDAO.selectBusinessByMemberNo(
                memberNo);
    }

    /*
     * =========================================================
     * 상품 등록
     *
     * 처리 순서
     * 1. 상품 입력값 검증
     * 2. 콘텐츠 존재 여부 확인
     * 3. 콘텐츠와 배우 연결 관계 확인
     * 4. 상품 대표 이미지 저장
     * 5. PRODUCT 등록
     * 6. PRODUCT_IMAGE 등록
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

        validateProduct(goodsManageVO);

        normalizeActorNo(goodsManageVO);

        validateContentActor(goodsManageVO);

        validateProductImage(productImage);

        String savedImagePath = null;
        Path savedPhysicalPath = null;

        try {

            /*
             * 상품 이미지는 필수로 사용할 경우 먼저 저장한다.
             *
             * DB 등록 중 문제가 생기면 아래 catch에서
             * 저장된 실제 파일을 삭제한다.
             */
            if (productImage != null
                    && !productImage.isEmpty()) {

                SavedFileInfo savedFile =
                        saveProductImage(productImage);

                savedImagePath =
                        savedFile.webPath();

                savedPhysicalPath =
                        savedFile.physicalPath();

                goodsManageVO.setImagePath(
                        savedImagePath);

                goodsManageVO.setIsMain("Y");
            }

            /*
             * 신규 상품은 관리자 승인 대기 상태로 등록한다.
             */
            goodsManageVO.setStatus("WAITING");

            int productResult =
                    businessDAO.insertProduct(
                            goodsManageVO);

            if (productResult != 1) {
                throw new IllegalStateException(
                        "상품 등록에 실패했습니다.");
            }

            /*
             * Mapper의 selectKey에 의해
             * goodsManageVO.productNo에 상품 번호가 저장된다.
             */
            if (goodsManageVO.getProductNo() <= 0) {
                throw new IllegalStateException(
                        "등록된 상품 번호를 확인할 수 없습니다.");
            }

            /*
             * 대표 이미지가 있는 경우에만
             * PRODUCT_IMAGE 테이블에 등록한다.
             */
            if (savedImagePath != null) {

                int imageResult =
                        businessDAO.insertProductImage(
                                goodsManageVO);

                if (imageResult != 1) {
                    throw new IllegalStateException(
                            "상품 대표 이미지 등록에 실패했습니다.");
                }
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
            searchKeyword =
                    searchKeyword.trim();
        }

        return businessDAO.selectContentList(
                searchKeyword);
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

        ContentSearchVO content =
                businessDAO.selectContentByNo(
                        contentNo);

        if (content == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 콘텐츠입니다.");
        }

        return content;
    }

    /*
     * =========================================================
     * 전체 배우 목록
     * =========================================================
     */
    @Override
    public List<ActorSearchVO> getActorList() {

        return businessDAO.selectActorList();
    }

    /*
     * =========================================================
     * 선택한 콘텐츠에 연결된 배우 목록
     *
     * CONTENT_ACTOR와 ACTOR를 조인한 결과를 반환한다.
     * =========================================================
     */
    @Override
    public List<ActorSearchVO> getActorListByContentNo(
            long contentNo) {

        if (contentNo <= 0) {
            throw new IllegalArgumentException(
                    "올바르지 않은 콘텐츠 번호입니다.");
        }

        ContentSearchVO content =
                businessDAO.selectContentByNo(
                        contentNo);

        if (content == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 콘텐츠입니다.");
        }

        return businessDAO
                .selectActorListByContentNo(
                        contentNo);
    }

    /*
     * =========================================================
     * 배우 단건 조회
     * =========================================================
     */
    @Override
    public ActorSearchVO getActorByNo(
            long actorNo) {

        if (actorNo <= 0) {
            throw new IllegalArgumentException(
                    "올바르지 않은 배우 번호입니다.");
        }

        ActorSearchVO actor =
                businessDAO.selectActorByNo(
                        actorNo);

        if (actor == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 배우입니다.");
        }

        return actor;
    }

    /*
     * =========================================================
     * 사업자가 등록한 상품 목록
     * =========================================================
     */
    @Override
    public List<GoodsManageVO>
            getProductListByBusinessNo(
                    long businessNo) {

        if (businessNo <= 0) {
            throw new IllegalArgumentException(
                    "올바르지 않은 사업자 번호입니다.");
        }

        return businessDAO
                .selectProductListByBusinessNo(
                        businessNo);
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

        ContentSearchVO content =
                businessDAO.selectContentByNo(
                        goodsManageVO.getContentNo());

        if (content == null) {
            throw new IllegalArgumentException(
                    "선택한 콘텐츠가 존재하지 않습니다.");
        }

        String productName =
                goodsManageVO.getProductName();

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

        String productType =
                goodsManageVO.getProductType();

        if (productType == null
                || productType.isBlank()) {

            throw new IllegalArgumentException(
                    "상품 유형을 선택해주세요.");
        }

        productType =
                productType.trim()
                        .toUpperCase();

        goodsManageVO.setProductType(
                productType);

        if (goodsManageVO.getPrice() < 0) {
            throw new IllegalArgumentException(
                    "상품 가격은 0원 이상이어야 합니다.");
        }

        if (goodsManageVO.getDiscountRate() < 0
                || goodsManageVO
                        .getDiscountRate() > 100) {

            throw new IllegalArgumentException(
                    "할인율은 0부터 100 사이여야 합니다.");
        }

        if (goodsManageVO.getStock() < 0) {
            throw new IllegalArgumentException(
                    "재고는 0개 이상이어야 합니다.");
        }

        String description =
                goodsManageVO.getDescription();

        if (description != null) {

            description =
                    description.trim();

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
     *
     * PRODUCT.ACTOR_NO는 NULL을 허용한다.
     * 배우 미선택 값이 0 이하이면 null로 변경한다.
     * =========================================================
     */
    private void normalizeActorNo(
            GoodsManageVO goodsManageVO) {

        Long actorNo =
                goodsManageVO.getActorNo();

        if (actorNo != null
                && actorNo <= 0) {

            goodsManageVO.setActorNo(null);
        }
    }

    /*
     * =========================================================
     * 콘텐츠와 배우 연결 관계 검증
     *
     * 사용자가 HTML 요청값을 조작하여
     * 다른 콘텐츠의 배우 번호를 전송하는 것을 방지한다.
     * =========================================================
     */
    private void validateContentActor(
            GoodsManageVO goodsManageVO) {

        Long actorNo =
                goodsManageVO.getActorNo();

        if (actorNo == null) {
            return;
        }

        ActorSearchVO actor =
                businessDAO.selectActorByNo(
                        actorNo);

        if (actor == null) {
            throw new IllegalArgumentException(
                    "선택한 배우가 존재하지 않습니다.");
        }

        int contentActorCount =
                businessDAO.countContentActor(
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

            /*
             * 이미지가 필수가 아니라면 그대로 반환한다.
             *
             * 이미지 등록을 필수로 만들고 싶다면
             * 여기서 예외를 발생시키면 된다.
             */
            return;
        }

        if (productImage.getSize()
                > MAX_IMAGE_SIZE) {

            throw new IllegalArgumentException(
                    "상품 이미지는 10MB 이하만 "
                            + "업로드할 수 있습니다.");
        }

        String originalFilename =
                productImage.getOriginalFilename();

        if (originalFilename == null
                || originalFilename.isBlank()) {

            throw new IllegalArgumentException(
                    "상품 이미지 파일명이 올바르지 않습니다.");
        }

        String extension =
                getFileExtension(
                        originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(
                extension)) {

            throw new IllegalArgumentException(
                    "상품 이미지는 JPG, JPEG, PNG, GIF, "
                            + "WEBP 형식만 업로드할 수 있습니다.");
        }

        String contentType =
                productImage.getContentType();

        if (contentType != null
                && !contentType.startsWith(
                        "image/")) {

            throw new IllegalArgumentException(
                    "이미지 파일만 업로드할 수 있습니다.");
        }
    }

    /*
     * =========================================================
     * 상품 이미지 실제 저장
     * =========================================================
     */
    private SavedFileInfo saveProductImage(
            MultipartFile productImage) {

        String originalFilename =
                productImage.getOriginalFilename();

        if (originalFilename == null) {
            throw new IllegalArgumentException(
                    "상품 이미지 파일명이 없습니다.");
        }

        String extension =
                getFileExtension(
                        originalFilename);

        String savedFilename =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        + "."
                        + extension;

        try {

            Files.createDirectories(
                    productUploadDirectory);

            Path targetPath =
                    productUploadDirectory
                            .resolve(savedFilename)
                            .normalize();

            /*
             * 설정된 상품 업로드 폴더 밖으로
             * 파일 경로가 이동하는 것을 방지한다.
             */
            if (!targetPath.startsWith(
                    productUploadDirectory)) {

                throw new IllegalArgumentException(
                        "올바르지 않은 파일 경로입니다.");
            }

            try (var inputStream =
                    productImage.getInputStream()) {

                Files.copy(
                        inputStream,
                        targetPath,
                        StandardCopyOption
                                .REPLACE_EXISTING);
            }

            /*
             * DB에는 실제 운영체제 경로가 아니라
             * 브라우저에서 사용할 웹 경로를 저장한다.
             */
            String webPath =
                    "/uploads/product/"
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

        int dotIndex =
                filename.lastIndexOf('.');

        if (dotIndex < 0
                || dotIndex
                        == filename.length() - 1) {

            return "";
        }

        return filename
                .substring(dotIndex + 1)
                .toLowerCase();
    }

    /*
     * =========================================================
     * 상품 등록 실패 시 이미 저장한 파일 삭제
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