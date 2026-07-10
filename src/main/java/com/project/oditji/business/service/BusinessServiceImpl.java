package com.project.oditji.business.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

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

    private static final String PRODUCT_UPLOAD_DIR =
            "C:/oditji/upload/product/";

    private static final long MAX_IMAGE_SIZE =
            10L * 1024L * 1024L;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "gif",
                    "webp"
            );

    /*
     * PRODUCT 테이블에는 PRODUCT_TYPE CHECK 제약조건이 없지만,
     * 화면과 서버에서 사용하는 상품 종류를 통일하기 위한 값이다.
     */
    private static final Set<String> ALLOWED_PRODUCT_TYPES =
            Set.of(
                    "CLOTHES",
                    "PROP",
                    "GOODS",
                    "OST",
                    "BOOK",
                    "FIGURE",
                    "ETC"
            );

    private final BusinessDAO businessDAO;

    public BusinessServiceImpl(BusinessDAO businessDAO) {
        this.businessDAO = businessDAO;
    }

    @Override
    public BusinessVO getBusinessByMemberNo(long memberNo) {
        return businessDAO.selectBusinessByMemberNo(memberNo);
    }

    @Override
    public List<ContentSearchVO> searchContentList(
            String keyword) {

        String searchKeyword = keyword;

        if (searchKeyword != null) {
            searchKeyword = searchKeyword.trim();
        }

        return businessDAO.selectContentList(searchKeyword);
    }

    @Override
    public List<ActorSearchVO> getActorList() {
        return businessDAO.selectActorList();
    }

    @Override
    public List<GoodsManageVO> getProductList(
            long businessNo) {

        return businessDAO.selectProductListByBusinessNo(
                businessNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long registerProduct(
            GoodsManageVO goodsManageVO,
            MultipartFile productImage) {

        validateProduct(goodsManageVO);
        validateRelatedData(goodsManageVO);
        validateProductImage(productImage);

        goodsManageVO.setStatus("WAITING");

        int productResult =
                businessDAO.insertProduct(goodsManageVO);

        if (productResult != 1) {
            throw new IllegalStateException(
                    "상품 정보 저장에 실패했습니다.");
        }

        /*
         * 파일명을 try문 실행 전에 생성한다.
         * 파일 저장 중 IOException이 발생해도
         * 삭제할 파일명을 알고 있기 때문에 정리할 수 있다.
         */
        String savedFileName =
                createSavedFileName(productImage);

        try {
            saveProductImage(
                    productImage,
                    savedFileName);

            goodsManageVO.setImagePath(savedFileName);
            goodsManageVO.setIsMain("Y");

            int imageResult =
                    businessDAO.insertProductImage(
                            goodsManageVO);

            if (imageResult != 1) {
                throw new IllegalStateException(
                        "상품 이미지 정보 저장에 실패했습니다.");
            }

            return goodsManageVO.getProductNo();

        } catch (IOException e) {

            deleteSavedFile(savedFileName);

            throw new IllegalStateException(
                    "상품 이미지 저장 중 오류가 발생했습니다.",
                    e);

        } catch (RuntimeException e) {

            deleteSavedFile(savedFileName);

            throw e;
        }
    }

    private void validateProduct(
            GoodsManageVO goodsManageVO) {

        if (goodsManageVO == null) {
            throw new IllegalArgumentException(
                    "상품 정보가 전달되지 않았습니다.");
        }

        if (goodsManageVO.getBusinessNo() <= 0) {
            throw new IllegalArgumentException(
                    "사업자 정보가 올바르지 않습니다.");
        }

        if (goodsManageVO.getContentNo() <= 0) {
            throw new IllegalArgumentException(
                    "관련 콘텐츠를 선택해주세요.");
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

        goodsManageVO.setProductName(productName);

        String productType =
                goodsManageVO.getProductType();

        if (productType == null
                || !ALLOWED_PRODUCT_TYPES.contains(
                        productType)) {

            throw new IllegalArgumentException(
                    "상품 종류를 올바르게 선택해주세요.");
        }

        if (goodsManageVO.getPrice() == null
                || goodsManageVO.getPrice() <= 0) {

            throw new IllegalArgumentException(
                    "가격은 1원 이상이어야 합니다.");
        }

        if (goodsManageVO.getDiscountRate() == null) {
            goodsManageVO.setDiscountRate(0);
        }

        if (goodsManageVO.getDiscountRate() < 0
                || goodsManageVO.getDiscountRate() > 100) {

            throw new IllegalArgumentException(
                    "할인율은 0부터 100 사이여야 합니다.");
        }

        if (goodsManageVO.getStock() == null
                || goodsManageVO.getStock() < 0) {

            throw new IllegalArgumentException(
                    "재고는 0개 이상이어야 합니다.");
        }

        if (goodsManageVO.getDescription() != null) {
            goodsManageVO.setDescription(
                    goodsManageVO
                            .getDescription()
                            .trim());
        }

        if (goodsManageVO.getActorNo() != null
                && goodsManageVO.getActorNo() <= 0) {

            goodsManageVO.setActorNo(null);
        }
    }

    private void validateRelatedData(
            GoodsManageVO goodsManageVO) {

        ContentSearchVO content =
                businessDAO.selectContentByNo(
                        goodsManageVO.getContentNo());

        if (content == null) {
            throw new IllegalArgumentException(
                    "선택한 콘텐츠가 존재하지 않습니다.");
        }

        Long actorNo =
                goodsManageVO.getActorNo();

        if (actorNo != null) {

            ActorSearchVO actor =
                    businessDAO.selectActorByNo(
                            actorNo);

            if (actor == null) {
                throw new IllegalArgumentException(
                        "선택한 배우가 존재하지 않습니다.");
            }
        }
    }

    private void validateProductImage(
            MultipartFile productImage) {

        if (productImage == null
                || productImage.isEmpty()) {

            throw new IllegalArgumentException(
                    "상품 대표 이미지를 선택해주세요.");
        }

        if (productImage.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    "상품 이미지는 10MB 이하만 등록할 수 있습니다.");
        }

        String extension =
                getFileExtension(
                        productImage.getOriginalFilename());

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "JPG, JPEG, PNG, GIF, WEBP 이미지만 등록할 수 있습니다.");
        }

        String contentType =
                productImage.getContentType();

        if (contentType == null
                || !contentType
                        .toLowerCase(Locale.ROOT)
                        .startsWith("image/")) {

            throw new IllegalArgumentException(
                    "이미지 파일만 등록할 수 있습니다.");
        }
    }

    /*
     * 저장할 파일명을 먼저 생성한다.
     */
    private String createSavedFileName(
            MultipartFile productImage) {

        String extension =
                getFileExtension(
                        productImage.getOriginalFilename());

        return UUID.randomUUID()
                + "."
                + extension;
    }

    /*
     * 전달받은 파일명으로 실제 파일을 저장한다.
     */
    private void saveProductImage(
            MultipartFile productImage,
            String savedFileName)
            throws IOException {

        File uploadDirectory =
                new File(PRODUCT_UPLOAD_DIR);

        if (!uploadDirectory.exists()
                && !uploadDirectory.mkdirs()) {

            throw new IOException(
                    "상품 이미지 저장 폴더를 생성할 수 없습니다.");
        }

        File savedFile =
                new File(
                        uploadDirectory,
                        savedFileName);

        productImage.transferTo(savedFile);
    }

    private String getFileExtension(
            String originalFileName) {

        if (originalFileName == null
                || originalFileName.isBlank()) {

            return "";
        }

        int dotIndex =
                originalFileName.lastIndexOf('.');

        if (dotIndex < 0
                || dotIndex
                == originalFileName.length() - 1) {

            return "";
        }

        return originalFileName
                .substring(dotIndex + 1)
                .toLowerCase(Locale.ROOT);
    }

    private void deleteSavedFile(
            String savedFileName) {

        if (savedFileName == null
                || savedFileName.isBlank()) {

            return;
        }

        File savedFile =
                new File(
                        PRODUCT_UPLOAD_DIR,
                        savedFileName);

        if (savedFile.exists()
                && !savedFile.delete()) {

            System.out.println(
                    "상품 이미지 삭제 실패: "
                    + savedFile.getAbsolutePath());
        }
    }
}