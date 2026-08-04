package com.project.oditji.admin.vo;

import java.util.Date;

import com.project.oditji.common.vo.LongProductSummaryVO;

/**
 * 상품 관리 VO (테이블: PRODUCT, BUSINESS/CONTENT/PRODUCT_IMAGE 조인)
 *
 * 업로드된 VO 목록에는 없었으나 productManage.jsp 화면 구현을 위해 신규 작성.
 * PRODUCT 테이블에는 등록/수정/삭제 요청을 구분하는 별도 컬럼이 없어
 * tab(register/update/delete) 구분 없이 STATUS 기준으로 동일하게 조회한다.
 */
public class ProductManageVO extends LongProductSummaryVO {

    private Long businessNo;
    private String businessName;  // BUSINESS 조인
    private Long contentNo;
    private String contentTitle;  // CONTENT 조인
    private String productType;
    private Integer discountRate;
    private Integer stock;
    private String description;
    private String status;        // WAITING, APPROVED, REJECTED
    private String mainImage;     // PRODUCT_IMAGE 중 IS_MAIN='Y' 조인
    private Date createdAt;

    public Long getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Long businessNo) {
        this.businessNo = businessNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public Long getContentNo() {
        return contentNo;
    }

    public void setContentNo(Long contentNo) {
        this.contentNo = contentNo;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Integer discountRate) {
        this.discountRate = discountRate;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
