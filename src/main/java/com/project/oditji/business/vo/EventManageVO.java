package com.project.oditji.business.vo;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class EventManageVO {

    // EVENT
    private long eventNo;
    private String title;
    private String description;
    private String bannerImage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Date createdAt;


    // EVENT_PRODUCT
    private long eventProdNo;

    /*
     * 하나의 이벤트에 여러 상품 연결
     */
    private List<Long> productNoList;


    /*
     * 상품별 이벤트 할인율
     *
     * productNoList와 같은 순서로 매칭
     *
     * 예)
     * productNoList
     * [10, 20, 30]
     *
     * discountRateList
     * [10, 20, 30]
     *
     */
    private List<Integer> discountRateList;


    // 조회 화면 표시용 (이벤트 목록에서 대표 상품 1건 요약)
    private String productName;
    private Long price;
    private int eventDiscountRate;

    /*
     * 이벤트에 연결된 전체 상품 목록 (조회 전용)
     *
     * 이벤트 수정 화면에 진입할 때 기존에 연결되어 있던 상품들을
     * 그대로 화면에 다시 그려주기 위해 사용한다.
     *
     * productNoList / discountRateList는 화면 -> 서버로 보내는
     * "입력값" 전용이므로 서로 용도를 섞지 않는다.
     */
    private List<EventProductVO> connectedProducts;


    /*
     * 이벤트 등록 권한 및 연결 상품 소유 여부 검증용
     *
     * EVENT 테이블에는 BUSINESS_NO가 없으므로
     * 이벤트 등록/조회 시 사업자 확인용으로 사용
     */
    private long businessNo;




    public long getEventNo() {
        return eventNo;
    }


    public void setEventNo(long eventNo) {
        this.eventNo = eventNo;
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getDescription() {
        return description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public String getBannerImage() {
        return bannerImage;
    }


    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }


    public LocalDate getStartDate() {
        return startDate;
    }


    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }


    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }


    public Date getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }


    public long getEventProdNo() {
        return eventProdNo;
    }


    public void setEventProdNo(long eventProdNo) {
        this.eventProdNo = eventProdNo;
    }


    public List<Long> getProductNoList() {
        return productNoList;
    }


    public void setProductNoList(
            List<Long> productNoList) {

        this.productNoList = productNoList;
    }


    public List<Integer> getDiscountRateList() {
        return discountRateList;
    }


    public void setDiscountRateList(
            List<Integer> discountRateList) {

        this.discountRateList = discountRateList;
    }


    public String getProductName() {
        return productName;
    }


    public void setProductName(String productName) {
        this.productName = productName;
    }


    public Long getPrice() {
        return price;
    }


    public void setPrice(Long price) {
        this.price = price;
    }


    public int getEventDiscountRate() {
        return eventDiscountRate;
    }


    public void setEventDiscountRate(int eventDiscountRate) {
        this.eventDiscountRate = eventDiscountRate;
    }


    public List<EventProductVO> getConnectedProducts() {
        return connectedProducts;
    }


    public void setConnectedProducts(
            List<EventProductVO> connectedProducts) {

        this.connectedProducts = connectedProducts;
    }


    /*
     * 조회 화면용 할인 적용가
     *
     * 현재는 단일 조회용 유지
     *
     * 다중 상품 상세 조회 시에는
     * EVENT_PRODUCT 단위 VO로 분리 예정
     */
    public Long getDiscountedPrice() {

        if (price == null) {

            return null;
        }


        return price - (price * eventDiscountRate / 100);
    }


    public long getBusinessNo() {
        return businessNo;
    }


    public void setBusinessNo(long businessNo) {
        this.businessNo = businessNo;
    }


    @Override
    public String toString() {

        return "EventManageVO{" +
                "eventNo=" + eventNo +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", bannerImage='" + bannerImage + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", eventProdNo=" + eventProdNo +
                ", productNoList=" + productNoList +
                ", discountRateList=" + discountRateList +
                ", productName='" + productName + '\'' +
                ", price=" + price +
                ", eventDiscountRate=" + eventDiscountRate +
                ", connectedProducts=" + connectedProducts +
                ", businessNo=" + businessNo +
                '}';
    }
}