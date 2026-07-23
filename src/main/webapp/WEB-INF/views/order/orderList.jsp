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

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/payment.css">

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-list-container"
      data-context-path="${pageContext.request.contextPath}">

    <h1>주문 내역</h1>

    <!-- 주문 내역 없음 -->
    <c:if test="${empty orderList}">

        <div class="empty-box">
            주문 내역이 없습니다.
        </div>

    </c:if>

    <!-- 주문 목록 -->
    <c:forEach var="o"
               items="${orderList}">

        <section class="order-card">

            <div class="order-card-main">

                <!-- 왼쪽: 주문번호, 주문일시, 상품 목록 -->
                <div class="order-product-section">

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

                    </div>

                    <div class="order-items">

                        <c:forEach var="i"
                                   items="${o.items}">

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
                                            <c:out value="${i.productName}"/>
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

                                <div class="order-item-actions">

                                    <c:if test="${o.orderStatus ne 'CANCELED'}">

                                        <button type="button"
                                                class="review-btn"
                                                data-order-item="${i.orderItemNo}"
                                                data-product="${i.productNo}">
                                            리뷰 작성
                                        </button>

                                    </c:if>

                                    <%--
                                        =========================================================
                                        [상품별 부분 취소 버튼 추가]

                                        주문상품이 결제 완료 또는 상품 준비 중 상태일 때만
                                        해당 상품 한 건에 대한 부분 취소 요청을 등록한다.
                                        기존 상품 카드 레이아웃은 유지한다.
                                        =========================================================
                                    --%>
                                    <c:if test="${i.status eq 'PAID'
                                            || i.status eq 'ORDERED'
                                            || i.status eq 'PREPARING'}">

                                        <button type="button"
                                                class="item-cancel-btn"
                                                data-order-item-no="${i.orderItemNo}"
                                                data-product-name="${i.productName}">
                                            상품 부분 취소
                                        </button>

                                    </c:if>

                                    <c:if test="${i.status eq 'CANCEL_REQUEST'}">

                                        <span class="item-cancel-state">
                                            취소 승인 대기
                                        </span>

                                    </c:if>

                                    <c:if test="${i.status eq 'CANCELED'}">

                                        <span class="item-cancel-state canceled">
                                            취소 완료
                                        </span>

                                    </c:if>

                                </div>

                            </div>

                        </c:forEach>

                    </div>

                </div>

                <!-- 오른쪽: 주문 상태와 주문 정보 -->
                <aside class="order-info-section">

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

                                        <c:when test="${o.orderStatus eq 'ORDERED'}">주문 완료</c:when>
                                        <c:when test="${o.orderStatus eq 'PAID'}">결제 완료</c:when>
                                        <c:when test="${o.orderStatus eq 'PREPARING'}">상품 준비 중</c:when>
                                        <c:when test="${o.orderStatus eq 'SHIPPING'}">배송 중</c:when>
                                        <c:when test="${o.orderStatus eq 'DELIVERED'}">배송 완료</c:when>
                                        <c:when test="${o.orderStatus eq 'CANCEL_REQUEST'}">취소 요청</c:when>
                                        <c:when test="${o.orderStatus eq 'PARTIAL_CANCELED'}">부분 취소</c:when>
                                        <c:when test="${o.orderStatus eq 'CANCELED'}">주문 취소</c:when>
                                        <c:otherwise>${o.orderStatus}</c:otherwise>

                                    </c:choose>

                                </span>

                            </div>

                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">
                                주문자 이름
                            </div>

                            <div class="order-info-value">
                                <c:out value="${empty o.receiverName ? '-' : o.receiverName}"/>
                            </div>

                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">
                                주문자 전화번호
                            </div>

                            <div class="order-info-value">
                                <c:out value="${empty o.receiverPhone ? '-' : o.receiverPhone}"/>
                            </div>

                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">
                                배송지 주소
                            </div>

                            <div class="order-info-value">
                                <c:out value="${empty o.address ? '-' : o.address}"/>
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

                </aside>

            </div>

            <%--
                =========================================================
                [주문 전체 취소 처리 상태 및 반려 사유 표시 추가]

                가장 최근 FULL 취소 그룹의 처리 상태를 주문 단위로
                표시한다.

                WAITING
                - 전체 취소 승인 대기 상태를 표시한다.
                - 전체 취소 버튼은 숨긴다.

                REJECTED
                - 전체 취소 반려 상태와 반려 사유를 표시한다.
                - 전체 취소 버튼은 숨긴다.

                APPROVED
                - 전체 취소 완료 상태를 표시한다.

                취소 요청 없음
                - 기존 주문 전체 취소 버튼을 표시한다.
                =========================================================
            --%>
            <c:choose>

                <c:when test="${o.fullCancelStatus eq 'WAITING'}">

                    <div class="full-cancel-status-box waiting">

                        <strong class="full-cancel-status-title">
                            전체 주문 취소 승인 대기
                        </strong>

                        <p class="full-cancel-status-message">
                            주문에 포함된 사업자의 승인을 기다리고 있습니다.
                        </p>

                    </div>

                </c:when>

                <c:when test="${o.fullCancelStatus eq 'REJECTED'}">

                    <div class="full-cancel-status-box rejected">

                        <strong class="full-cancel-status-title">
                            전체 주문 취소 반려
                        </strong>

                        <p class="full-cancel-status-message">

                            <span class="full-cancel-reject-label">
                                반려 사유:
                            </span>

                            <c:choose>

                                <c:when test="${not empty o.fullCancelRejectReason}">
                                    <c:out value="${o.fullCancelRejectReason}"/>
                                </c:when>

                                <c:otherwise>
                                    사업자가 전체 주문 취소 요청을 반려했습니다.
                                </c:otherwise>

                            </c:choose>

                        </p>

                    </div>

                </c:when>

                <c:when test="${o.fullCancelStatus eq 'APPROVED'}">

                    <div class="full-cancel-status-box approved">

                        <strong class="full-cancel-status-title">
                            전체 주문 취소 완료
                        </strong>

                        <p class="full-cancel-status-message">
                            주문 전체 취소와 결제 환불 처리가 완료되었습니다.
                        </p>

                    </div>

                </c:when>

            </c:choose>

            <div class="ticket-divider"
                 aria-hidden="true">
            </div>

            <div class="order-card-footer">

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

                <%--
                    =========================================================
                    [전체 취소 반려 후 재요청 버튼 유지]

                    전체 취소 요청이 반려됐더라도 주문상품 상태가
                    취소 가능한 상태라면 사용자가 다시 전체 취소를
                    요청할 수 있도록 기존 버튼을 표시한다.
                    =========================================================
                --%>
                <c:if test="${(empty o.fullCancelStatus || o.fullCancelStatus eq 'REJECTED')
                        && (o.orderStatus eq 'PAID'
                        || o.orderStatus eq 'ORDERED'
                        || o.orderStatus eq 'PREPARING')}">

                    <div class="order-cancel-action">

                        <button type="button"
                                class="payment-cancel-btn"
                                data-order-no="${o.orderNo}">
                            주문 전체 취소
                        </button>

                    </div>

                </c:if>

            </div>

        </section>

    </c:forEach>

    <!-- 주문 3건 단위 페이지 번호 -->
    <c:if test="${not empty orderList && pageVO.totalPage > 1}">

        <nav class="order-pagination"
             aria-label="주문내역 페이지">

            <c:if test="${pageVO.prev}">

                <a class="order-page-link order-page-arrow"
                   href="${pageContext.request.contextPath}/order/list?page=${pageVO.startPage - 1}"
                   aria-label="이전 페이지 묶음">
                    ‹
                </a>

            </c:if>

            <c:forEach var="pageNo"
                       begin="${pageVO.startPage}"
                       end="${pageVO.endPage}">

                <c:choose>

                    <c:when test="${pageNo eq pageVO.currentPage}">

                        <span class="order-page-link active"
                              aria-current="page">
                            ${pageNo}
                        </span>

                    </c:when>

                    <c:otherwise>

                        <a class="order-page-link"
                           href="${pageContext.request.contextPath}/order/list?page=${pageNo}">
                            ${pageNo}
                        </a>

                    </c:otherwise>

                </c:choose>

            </c:forEach>

            <c:if test="${pageVO.next}">

                <a class="order-page-link order-page-arrow"
                   href="${pageContext.request.contextPath}/order/list?page=${pageVO.endPage + 1}"
                   aria-label="다음 페이지 묶음">
                    ›
                </a>

            </c:if>

        </nav>

    </c:if>

    <!-- 리뷰 작성 모달 -->
    <div id="reviewModal"
         class="review-modal"
         style="display:none;">

        <div class="review-modal-content">

            <h2>상품 리뷰 작성</h2>

            <form action="${pageContext.request.contextPath}/review/writeProductReview"
                  method="post">

                <input type="hidden"
                       id="orderItemNo"
                       name="orderItemNo">

                <input type="hidden"
                       id="productNo"
                       name="productNo">

                <div>

                    <label for="rating">
                        평점
                    </label>

                    <select id="rating"
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

                    <textarea name="content"
                              rows="6"
                              placeholder="리뷰를 작성해주세요."></textarea>

                </div>

                <button type="submit">
                    등록
                </button>

                <button type="button"
                        id="closeReviewModal">
                    취소
                </button>

            </form>

        </div>

    </div>

    <!-- 주문 취소 요청 모달 -->
    <div id="orderCancelModal"
         class="payment-cancel-modal"
         aria-hidden="true">

        <div class="payment-cancel-modal-content"
             role="dialog"
             aria-modal="true"
             aria-labelledby="orderCancelModalTitle">

            <h2 id="orderCancelModalTitle"
                class="payment-cancel-modal-title">
                주문 취소 요청
            </h2>

            <p id="orderCancelModalDescription"
               class="payment-cancel-modal-desc">
            </p>

            <label for="orderCancelReason"
                   class="payment-cancel-label">
                취소 사유
            </label>

            <textarea id="orderCancelReason"
                      class="payment-cancel-reason"
                      maxlength="500"
                      placeholder="취소 사유를 입력해주세요."></textarea>

            <p id="orderCancelError"
               class="payment-cancel-error"
               style="display:none;">
            </p>

            <div class="payment-cancel-modal-actions">

                <button type="button"
                        id="orderCancelCloseBtn"
                        class="payment-cancel-close-btn">
                    닫기
                </button>

                <button type="button"
                        id="orderCancelSubmitBtn"
                        class="payment-cancel-submit-btn">
                    결제 취소 요청
                </button>

            </div>

        </div>

    </div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script src="${pageContext.request.contextPath}/js/orderReview.js"></script>
<script src="${pageContext.request.contextPath}/js/orderCancel.js"></script>

</body>

</html>