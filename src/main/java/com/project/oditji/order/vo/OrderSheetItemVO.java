package com.project.oditji.order.vo;

import com.project.oditji.common.vo.ProductOptionSelectionVO;

/**
 * 주문서(order.jsp) 작성 단계에서 사용하는 임시 주문 품목 정보.
 *
 * 장바구니에서 선택한 상품 또는 상품 상세의 "바로 구매"로 담긴 상품을
 * 결제 전까지 세션(OrderSheetVO)에 보관하기 위한 용도이며,
 * ORDER_ITEM 테이블과 1:1로 대응되지 않는다 (주문 완료 전 단계).
 */
public class OrderSheetItemVO extends ProductOptionSelectionVO{

    private static final long serialVersionUID = 1L;

    private Integer businessNo;

    /**
     * 장바구니에서 담겨온 경우에만 값이 존재합니다.
     * 주문 완료 시 해당 CART_ITEM 삭제에 사용하며,
     * 바로 구매로 담긴 경우에는 null입니다.
     */
    private Long cartItemNo;

    private String productName;
    private String productType;
    private Integer price;
    private Integer discountRate;
    private Integer stock;
    private String status;
    private String businessName;
    private String mainImage;

    public Integer getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(Integer businessNo) {
        this.businessNo = businessNo;
    }

    public Long getCartItemNo() {
        return cartItemNo;
    }

    public void setCartItemNo(Long cartItemNo) {
        this.cartItemNo = cartItemNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getMainImage() {
        return mainImage;
    }

    public void setMainImage(String mainImage) {
        this.mainImage = mainImage;
    }

    public int getDiscountPrice() {

        int originalPrice = getPrice() == null ? 0 : getPrice();
        int rate = getDiscountRate() == null ? 0 : getDiscountRate();

        if (rate < 0) {
            rate = 0;
        }

        if (rate > 100) {
            rate = 100;
        }

        return originalPrice * (100 - rate) / 100;
    }

    public long getItemTotalPrice() {

        int itemQuantity = getQuantity() == null ? 0 : getQuantity();

        return (long) getDiscountPrice() * itemQuantity;
    }

    /**
     * 주문 시점 기준 정상 주문 가능 여부.
     * 상품 승인 상태(APPROVED)와 재고 수량을 함께 확인한다.
     */
    public boolean isAvailable() {

        int currentStock = getStock() == null ? 0 : getStock();
        int currentQuantity = getQuantity() == null ? 0 : getQuantity();

        return "APPROVED".equals(getStatus())
                && currentStock > 0
                && currentQuantity >= 1
                && currentQuantity <= currentStock;
    }
}
