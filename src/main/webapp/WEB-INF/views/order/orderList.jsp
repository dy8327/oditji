<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">

    <title>ODITJI | 주문 내역</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/order.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/review.css">

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-list-container">

    <h1>주문 내역</h1>

    <!-- 주문 내역 없음 -->
    <c:if test="${empty orderList}">

        <div class="empty-box">
            주문 내역이 없습니다.
        </div>

    </c:if>

    <!-- 주문 목록 -->
    <c:forEach var="o" items="${orderList}">

        <section class="order-card">

            <!-- 주문 기본 정보 -->
            <div class="order-card-header">

                <div>

                    <strong>주문번호</strong>

                    <span>
                        ${o.orderNo}
                    </span>

                </div>

                <div>

                    <fmt:formatDate
                        value="${o.createdAt}"
                        pattern="yyyy.MM.dd HH:mm"/>

                </div>

                <div class="order-status">

                    <c:choose>

                        <c:when test="${o.orderStatus eq 'ORDERED'}">
                            주문 완료
                        </c:when>

                        <c:when test="${o.orderStatus eq 'PAID'}">
                            결제 완료
                        </c:when>

                        <c:when test="${o.orderStatus eq 'PREPARING'}">
                            상품 준비 중
                        </c:when>

                        <c:when test="${o.orderStatus eq 'SHIPPING'}">
                            배송 중
                        </c:when>

                        <c:when test="${o.orderStatus eq 'DELIVERED'}">
                            배송 완료
                        </c:when>

                        <c:when test="${o.orderStatus eq 'CANCEL_REQUEST'}">
                            취소 요청
                        </c:when>

                        <c:when test="${o.orderStatus eq 'CANCELED'}">
                            주문 취소
                        </c:when>

                        <c:otherwise>
                            ${o.orderStatus}
                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

            <!-- 주문 상품 목록 -->
            <div class="order-items">

                <c:forEach var="i" items="${o.items}">

                    <div class="order-item">

                        <a href="${pageContext.request.contextPath}/goods/goodsDetail/${i.productNo}">

                            <c:choose>

                                <c:when test="${empty i.mainImage}">

                                    <div class="no-image">
                                        NO IMAGE
                                    </div>

                                </c:when>

                                <c:otherwise>

                                    <img src="${pageContext.request.contextPath}${i.mainImage}"
                                         alt="${i.productName}">

                                </c:otherwise>

                            </c:choose>

                            <div class="order-item-info">

                                <div>
                                    ${i.productName}
                                </div>

                                <div>
                                    수량: ${i.quantity}
                                </div>

                                <div>

                                    ₩

                                    <fmt:formatNumber
                                        value="${i.itemTotalPrice}"
                                        pattern="#,###"/>

                                </div>

                            </div>

                        </a>

                        <!-- 상품 링크 밖에 배치 -->
                        <button
                            type="button"
                            class="review-btn"
                            data-order-item="${i.orderItemNo}"
                            data-product="${i.productNo}">
                            리뷰 작성
                        </button>

                    </div>

                </c:forEach>

            </div>

            <!-- 주문자 및 배송 정보 -->
            <div class="order-info-section">

                <h2 class="order-info-title">
                    주문 정보
                </h2>

                <div class="order-info-list">

                    <div class="order-info-row">

                        <div class="order-info-label">
                            주문 상태
                        </div>

                        <div class="order-info-value">

                            <span class="order-info-status">

                                <c:choose>

                                    <c:when test="${o.orderStatus eq 'ORDERED'}">
                                        주문 완료
                                    </c:when>

                                    <c:when test="${o.orderStatus eq 'PAID'}">
                                        결제 완료
                                    </c:when>

                                    <c:when test="${o.orderStatus eq 'PREPARING'}">
                                        상품 준비 중
                                    </c:when>

                                    <c:when test="${o.orderStatus eq 'SHIPPING'}">
                                        배송 중
                                    </c:when>

                                    <c:when test="${o.orderStatus eq 'DELIVERED'}">
                                        배송 완료
                                    </c:when>

                                    <c:when test="${o.orderStatus eq 'CANCEL_REQUEST'}">
                                        취소 요청
                                    </c:when>

                                    <c:when test="${o.orderStatus eq 'CANCELED'}">
                                        주문 취소
                                    </c:when>

                                    <c:otherwise>
                                        ${o.orderStatus}
                                    </c:otherwise>

                                </c:choose>

                            </span>

                        </div>

                    </div>

                    <div class="order-info-row">

                        <div class="order-info-label">
                            주문자 이름
                        </div>

                        <div class="order-info-value">

                            <c:choose>

                                <c:when test="${empty o.receiverName}">
                                    -
                                </c:when>

                                <c:otherwise>
                                    <c:out value="${o.receiverName}"/>
                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                    <div class="order-info-row">

                        <div class="order-info-label">
                            주문자 전화번호
                        </div>

                        <div class="order-info-value">

                            <c:choose>

                                <c:when test="${empty o.receiverPhone}">
                                    -
                                </c:when>

                                <c:otherwise>
                                    <c:out value="${o.receiverPhone}"/>
                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                    <div class="order-info-row">

                        <div class="order-info-label">
                            배송지 주소
                        </div>

                        <div class="order-info-value">

                            <c:choose>

                                <c:when test="${empty o.address}">
                                    -
                                </c:when>

                                <c:otherwise>
                                    <c:out value="${o.address}"/>
                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                    <div class="order-info-row">

                        <div class="order-info-label">
                            결제 금액
                        </div>

                        <div class="order-info-value order-info-price">

                            ₩

                            <fmt:formatNumber
                                value="${o.totalAmount}"
                                pattern="#,###"/>

                        </div>

                    </div>

                </div>

            </div>

            <!-- 티켓 구분선 -->
            <div class="ticket-divider"
                 aria-hidden="true">
            </div>

            <!-- 총 결제금액 -->
            <div class="order-total">

                <span>
                    총 결제금액
                </span>

                <span class="order-total-price">

                    ₩

                    <fmt:formatNumber
                        value="${o.totalAmount}"
                        pattern="#,###"/>

                </span>

            </div>

        </section>

    </c:forEach>

    <!-- 리뷰 작성 모달 -->
    <div id="reviewModal"
         class="review-modal"
         style="display:none;">

        <div class="review-modal-content">

            <h2>상품 리뷰 작성</h2>

            <form
                action="${pageContext.request.contextPath}/review/writeProductReview"
                method="post">

                <input
                    type="hidden"
                    id="orderItemNo"
                    name="orderItemNo">

                <input
                    type="hidden"
                    id="productNo"
                    name="productNo">

                <div>

                    <label for="rating">
                        평점
                    </label>

                    <select
                        id="rating"
                        name="rating">

                        <option value="5">
                            ★★★★★
                        </option>

                        <option value="4">
                            ★★★★☆
                        </option>

                        <option value="3">
                            ★★★☆☆
                        </option>

                        <option value="2">
                            ★★☆☆☆
                        </option>

                        <option value="1">
                            ★☆☆☆☆
                        </option>

                    </select>

                </div>

                <div>

                    <textarea
                        name="content"
                        rows="6"
                        placeholder="리뷰를 작성해주세요."></textarea>

                </div>

                <button type="submit">
                    등록
                </button>

                <button
                    type="button"
                    id="closeReviewModal">
                    취소
                </button>

            </form>

        </div>

    </div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script src="${pageContext.request.contextPath}/js/orderReview.js"></script>

</body>

</html>