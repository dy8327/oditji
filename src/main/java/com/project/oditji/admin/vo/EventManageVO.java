package com.project.oditji.admin.vo;

import java.util.Calendar;
import java.util.Date;

/**
 * 이벤트 관리 VO (테이블: EVENT + EVENT_PRODUCT + PRODUCT + BUSINESS)
 *
 * EVENT 테이블에는 사업자 연결(BUSINESS_NO), 요청 내용(DESCRIPTION),
 * 요청 유형(등록/수정/연장 구분) 컬럼이 여전히 없다.
 * 대신 EVENT -> EVENT_PRODUCT -> PRODUCT -> BUSINESS 경로로 조인하여
 * businessName / productName / price / discountRate를 실제 값으로 채운다.
 * (요청 유형 구분이 필요해지면 REQUEST_TYPE 컬럼 추가를 별도로 검토한다.)
 */
public class EventManageVO {

    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private Long eventNo; // = EVENT.EVENT_NO (이벤트 번호)

    private String businessName; // = BUSINESS.BUSINESS_NAME (사업자명)

    private String title; // = EVENT.TITLE (이벤트 제목)

    private String bannerImage; // = EVENT.BANNER_IMAGE (사업자가 등록한 이벤트 배너 이미지 경로)

    private Date startDate; // = EVENT.START_DATE (이벤트 시작일)

    private Date endDate; // = EVENT.END_DATE (이벤트 종료일)

    private Date createdAt; // = EVENT.CREATED_AT (이벤트 등록일)

    private String status; // = EVENT.STATUS (WAITING/APPROVED/END/REJECTED/DELETED)

    private String description; // 이벤트 설명 (현재 EVENT 테이블 미지원)

    // ===================== 연결 상품 및 할인 정보 =====================

    private Long productNo; // = EVENT_PRODUCT.PRODUCT_NO (연결 상품 번호)

    private String productName; // = PRODUCT.PRODUCT_NAME (상품명)

    private Long price; // = PRODUCT.PRICE (상품 가격)

    private Integer eventDiscountRate; // = EVENT_PRODUCT.EVENT_DISCOUNT_RATE (이벤트 할인율 %)

    /*
     * 관리자 목록/상세보기 전용
     *
     * 이벤트 하나에 여러 상품이 연결될 수 있어
     * 목록 조회 시에는 이벤트 단위로 GROUP BY 하여
     * productName / price / eventDiscountRate에는 대표값(집계값)만 담긴다.
     *
     * 상세보기 팝업에서 상품별 개별 할인율을 그대로 보여주기 위해
     * "상품명|할인율|가격" 을 상품 단위 구분자 ";;"로 이어붙인
     * 원본 데이터를 별도로 담아 화면(JS)에서 다시 분리해 사용한다.
     */
    private String productDetail;

    // ===================== Getter / Setter =====================

    public Long getEventNo() {
        return eventNo;
    }

    public void setEventNo(Long eventNo) {
        this.eventNo = eventNo;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProductNo() {
        return productNo;
    }

    public void setProductNo(Long productNo) {
        this.productNo = productNo;
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

    public Integer getEventDiscountRate() {
        return eventDiscountRate;
    }

    public void setEventDiscountRate(Integer eventDiscountRate) {
        this.eventDiscountRate = eventDiscountRate;
    }

    public String getProductDetail() {
        return productDetail;
    }

    public void setProductDetail(String productDetail) {
        this.productDetail = productDetail;
    }

    /**
     * 이벤트 할인 적용 후 판매 가격 계산
     *
     * 예:
     * price = 10000
     * eventDiscountRate = 20
     *
     * 결과:
     * 8000원
     */
    public Long getDiscountedPrice() {

        if (price == null || eventDiscountRate == null) {
            return null;
        }

        return Math.round(
                price * (100 - eventDiscountRate) / 100.0);
    }

    /*
     * =========================================================
     * 승인상태 / 현재상태 (화면 표시용 계산 필드)
     *
     * 지금까지는 EVENT.STATUS 하나(WAITING/APPROVED/END/REJECTED)를
     * 그대로 "상태" 뱃지 하나로 보여주고 있었는데, 이 값에는 서로 다른
     * 두 가지 의미가 섞여 있다.
     *
     *  1) 승인상태 : 관리자가 이 이벤트 요청을 대기/승인/반려 중
     *                어떻게 처리했는가 (대기, 승인, 반려)
     *  2) 현재상태 : 승인된 이벤트가 START_DATE/END_DATE 기준으로
     *                지금 시점에 진행중인지, 아직 시작 전(예정)인지,
     *                끝났는지 (진행중, 예정, 종료)
     *
     * STATUS='END'는 "승인됐다가 종료된" 것이므로 승인상태 관점에서는
     * 여전히 승인(APPROVED)이다. 반대로 현재상태는 STATUS 값을 그대로
     * 믿지 않고 START_DATE/END_DATE를 오늘 날짜와 직접 비교해서 계산한다.
     * EventStatusScheduler가 하루 한 번(00:05)만 STATUS를 END로 갱신하기
     * 때문에, 배치가 돌기 전까지는 STATUS만으로는 종료일이 지난 이벤트를
     * "진행중"으로 잘못 보여줄 수 있기 때문이다.
     * =========================================================
     */

    /**
     * 승인상태 코드: WAITING(대기) / APPROVED(승인) / REJECTED(반려)
     */
    public String getApprovalStatus() {

        if (STATUS_REJECTED.equals(status)) {
            return STATUS_REJECTED;
        }

        if (STATUS_APPROVED.equals(status) || "END".equals(status)) {
            return STATUS_APPROVED;
        }

        return "WAITING";
    }

    /** 승인상태 한글 라벨 (대기 / 승인 / 반려) */
    public String getApprovalStatusLabel() {

        switch (getApprovalStatus()) {
            case STATUS_APPROVED:
                return "승인";
            case STATUS_REJECTED:
                return "반려";
            default:
                return "대기";
        }
    }

    /**
     * 현재상태 코드: ONGOING(진행중) / UPCOMING(예정) / ENDED(종료)
     * 아직 승인되지 않은(대기/반려) 이벤트는 노출 대상이 아니므로 null.
     */
    public String getProgressStatus() {

        if (!STATUS_APPROVED.equals(getApprovalStatus())) {
            return null;
        }

        if (startDate == null || endDate == null) {
            return null;
        }

        Date today = truncateToDate(new Date());
        Date start = truncateToDate(startDate);
        Date end = truncateToDate(endDate);

        if (today.before(start)) {
            return "UPCOMING";
        }

        if (today.after(end)) {
            return "ENDED";
        }

        return "ONGOING";
    }

    /** 현재상태 한글 라벨 (진행중 / 예정 / 종료), 대상이 아니면 "-" */
    public String getProgressStatusLabel() {

        String progress = getProgressStatus();

        if (progress == null) {
            return "-";
        }

        switch (progress) {
            case "ONGOING":
                return "진행중";
            case "UPCOMING":
                return "예정";
            default:
                return "종료";
        }
    }

    // 시/분/초를 0으로 잘라 날짜(일 단위)만 비교할 수 있게 한다. (Oracle TRUNC(SYSDATE)와 동일한 개념)
    private Date truncateToDate(Date date) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

}