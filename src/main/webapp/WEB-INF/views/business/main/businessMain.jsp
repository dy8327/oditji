<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <title>ODITJI | 사업자 페이지</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
    <jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>
    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <div class="business-wrap">

            <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

            <main id="mainContent" class="business-main">

                <!-- 사업자 Hero -->
                <section class="business-hero">

                    <div class="hero-left">
                        <p class="hero-sub">ODITJI 사업자 전용 페이지</p>
                        <h1>
                            상품과 판매 현황을<br/>
                            한눈에 관리하세요
                        </h1>
                        <p>
                            상품 등록부터 주문, 정산, 리뷰까지<br/>
                            사업 운영 현황을 확인할 수 있습니다.
                        </p>
                    </div>

                    <div class="hero-right">
                        <div class="business-info">
                            <div>
                                사업자명
                                <strong>${business.businessName}</strong>
                            </div>
                            <div>
                                등급
                                <strong>${business.gradeName}</strong>
                            </div>
                            <div>
                                상태
                                <strong>${business.status}</strong>
                            </div>
                        </div>
                    </div>

                </section>

                <!-- 핵심 사업 지표 -->
                <section class="dashboard-section">

                    <h2>오늘의 사업 현황</h2>

                    <div class="dashboard-grid">

                        <!-- 오늘 매출 -->
                        <a href="${pageContext.request.contextPath}/business/order/list"
                        class="dashboard-card highlight">

                            <span>오늘 매출</span>

                            <strong>
                                ${businessMain.todaySales}원
                            </strong>

                        </a>

                        <!-- 판매량 -->
                        <a href="${pageContext.request.contextPath}/business/order/list"
                        class="dashboard-card">

                            <span>오늘 판매량</span>

                            <strong>
                                ${businessMain.todayOrderCount}건
                            </strong>

                        </a>

                        <!-- =====================================================
                            [수정] 상품 조회수
                            단순 현황 표시 카드이므로 페이지 이동 링크 제거
                        ===================================================== -->
                        <div class="dashboard-card">

                            <span>상품 조회수</span>

                            <strong>
                                ${businessMain.clickCount}회
                            </strong>

                        </div>

                        <!-- =====================================================
                            [수정] 상품 구매율
                            단순 현황 표시 카드이므로 페이지 이동 링크 제거
                            구매율은 소수점 첫째 자리까지만 표시
                        ===================================================== -->
                        <div class="dashboard-card">

                            <span>상품 구매율</span>

                            <strong>
                                <fmt:formatNumber value="${businessMain.purchaseRate}" pattern="0.0"/>%
                            </strong>

                        </div>

                        <!-- 정산 -->
                        <a href="${pageContext.request.contextPath}/business/settlement/main"
                        class="dashboard-card">

                            <span>정산 예정</span>

                            <strong>
                                ${businessMain.waitingSettlement}원
                            </strong>

                        </a>

                        <%--
                            =====================================================
                            [오늘 구매 고객 수 카드 추가]
                            기존 dashboard-card 구조와 CSS를 그대로 사용하여
                            페이지 레이아웃과 디자인을 변경하지 않는다.
                            =====================================================
                        --%>
                        <a href="${pageContext.request.contextPath}/business/order/list"
                        class="dashboard-card">

                            <span>오늘 구매 고객 수</span>

                            <strong>
                                ${businessMain.todayCustomerCount}명
                            </strong>

                        </a>

                    </div>

                </section>

                <!-- =====================================================
                    [사업자 대시보드 최근 현황 디자인 수정]
                    최근 주문 / 최근 리뷰 영역
                ====================================================== -->
                <section class="business-content dashboard-recent-section">

                    <!-- =========================
                        최근 주문
                    ========================== -->
                    <div class="panel dashboard-modern-panel">

                        <div class="dashboard-panel-header">
                            <div class="dashboard-panel-title">
                                <span class="dashboard-title-icon order-icon">
                                    ♧
                                </span>
                                <h3>최근 주문</h3>
                            </div>

                            <a href="${pageContext.request.contextPath}/business/order/list"
                            class="dashboard-more-link"
                            aria-label="주문 목록으로 이동">
                                ›
                            </a>
                        </div>

                        <div class="dashboard-order-list">

                            <c:forEach var="order"
                                    items="${businessMain.recentOrders}">

                                <div class="dashboard-order-item">

                                    <!-- 상품 대표 이미지 -->
                                    <div class="dashboard-order-thumb">

                                        <c:choose>

                                            <c:when test="${not empty order.mainImage}">
                                                <img
                                                    src="${pageContext.request.contextPath}${order.mainImage}"
                                                    alt="<c:out value='${order.productName}'/>">
                                            </c:when>

                                            <c:otherwise>
                                                <span>상품</span>
                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                    <!-- 상품명 -->
                                    <div class="dashboard-order-info">
                                        <strong class="dashboard-order-name">
                                            <c:out value="${order.productName}"/>
                                        </strong>
                                    </div>

                                    <!-- 수량 -->
                                    <div class="dashboard-order-quantity">
                                        <c:out value="${order.quantity}"/>개
                                    </div>

                                    <!-- 주문 상태 -->
                                    <div class="dashboard-order-status">

                                        <c:choose>

                                            <%-- ==============================
                                                주문완료로 묶어서 표시
                                                ============================== --%>
                                            <c:when test="${order.status eq 'PAID'
                                                        or order.status eq 'ORDERED'
                                                        or order.status eq 'DELIVERED'
                                                        or order.status eq 'DELIVERY_COMPLETED'
                                                        or order.status eq 'COMPLETED'
                                                        or order.status eq '결제완료'
                                                        or order.status eq '배송완료'
                                                        or order.status eq '주문완료'}">

                                                <span class="dashboard-status-badge dashboard-order-complete">
                                                    <span class="dashboard-status-icon">
                                                        ✓
                                                    </span>
                                                    <span class="dashboard-status-text">
                                                        주문완료
                                                    </span>
                                                </span>

                                            </c:when>


                                            <%-- ==============================
                                                주문취소로 묶어서 표시
                                                ============================== --%>
                                            <c:when test="${order.status eq 'CANCELED'
                                                        or order.status eq 'PARTIAL_CANCELED'
                                                        or order.status eq 'REFUND_COMPLETED'
                                                        or order.status eq 'DELIVERY_CANCELED'
                                                        or order.status eq '결제취소'
                                                        or order.status eq '환불완료'
                                                        or order.status eq '배송취소'
                                                        or order.status eq '주문취소'}">

                                                <span class="dashboard-status-badge dashboard-order-cancel">
                                                    <span class="dashboard-status-icon">
                                                        ×
                                                    </span>
                                                    <span class="dashboard-status-text">
                                                        주문취소
                                                    </span>
                                                </span>

                                            </c:when>


                                            <%-- ==============================
                                                그 외 상태는 일단 주문완료 스타일로 fallback
                                                필요하면 raw status로 보여줘도 됨
                                                ============================== --%>
                                            <c:otherwise>

                                                <span class="dashboard-status-badge status-order-complete">
                                                    <span class="status-icon-pill">
                                                        <span class="status-icon">✓</span>
                                                    </span>
                                                    <span class="status-text">주문완료</span>
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                </div>

                            </c:forEach>

                            <!-- 최근 주문 없음 -->
                            <c:if test="${empty businessMain.recentOrders}">
                                <div class="dashboard-empty">
                                    최근 주문 내역이 없습니다.
                                </div>
                            </c:if>

                        </div>

                    </div>


                    <!-- =========================
                        최근 리뷰
                    ========================== -->
                    <div class="panel dashboard-modern-panel">

                        <div class="dashboard-panel-header">

                            <div class="dashboard-panel-title">
                                <span class="dashboard-title-icon review-icon">
                                    ☆
                                </span>
                                <h3>최근 리뷰</h3>
                            </div>

                            <a href="${pageContext.request.contextPath}/business/review/list"
                            class="dashboard-more-link"
                            aria-label="리뷰 목록으로 이동">
                                ›
                            </a>

                        </div>

                        <div class="dashboard-review-list">

                            <c:forEach var="review"
                                    items="${businessMain.recentReviews}">

                                <div class="dashboard-review-item">

                                    <!-- 평점 숫자 -->
                                    <div class="dashboard-review-score">
                                        <c:out value="${review.rating}"/>점
                                    </div>

                                    <!-- 리뷰 내용 -->
                                    <div class="dashboard-review-content">

                                        <!-- 별점 -->
                                        <div class="dashboard-review-stars"
                                            aria-label="${review.rating}점">

                                            <c:forEach begin="1"
                                                    end="5"
                                                    var="star">

                                                <span class="${review.rating >= star
                                                                ? 'star-active'
                                                                : 'star-empty'}">
                                                    ★
                                                </span>

                                            </c:forEach>

                                        </div>

                                        <p class="dashboard-review-text">
                                            <c:out value="${review.content}"/>
                                        </p>

                                    </div>

                                </div>

                            </c:forEach>

                            <!-- 최근 리뷰 없음 -->
                            <c:if test="${empty businessMain.recentReviews}">
                                <div class="dashboard-empty">
                                    최근 리뷰가 없습니다.
                                </div>
                            </c:if>

                        </div>

                    </div>

                </section>

                <!-- 분석 -->
                <section class="analysis-section">

                    <!-- =====================================================
                        [사업자 대시보드 상품 분석 디자인 수정]
                        기존 clickCount / averageRating 값을 그대로 사용
                    ====================================================== -->
                    <div class="panel dashboard-modern-panel dashboard-analysis-panel">

                        <div class="dashboard-panel-header">

                            <div class="dashboard-panel-title">
                                <span class="dashboard-title-icon analysis-icon">
                                    ▥
                                </span>
                                <h3>상품 분석</h3>
                            </div>

                        </div>


                        <!-- 상단 분석 카드 -->
                        <div class="dashboard-analysis-summary">

                            <!-- 총 조회수 -->
                            <div class="analysis-stat-card views-card">

                                <div class="analysis-stat-content">

                                    <span class="analysis-stat-label">
                                        총 조회수
                                    </span>

                                    <strong class="analysis-stat-value">
                                        ${businessMain.clickCount}<small>회</small>
                                    </strong>

                                    <span class="analysis-stat-caption">
                                        등록 상품 누적 조회수
                                    </span>

                                </div>


                                <!-- 미니 그래프 장식 -->
                                <div class="analysis-mini-chart"
                                    aria-hidden="true">

                                    <span style="height: 36%;"></span>
                                    <span style="height: 58%;"></span>
                                    <span style="height: 43%;"></span>
                                    <span style="height: 72%;"></span>
                                    <span style="height: 54%;"></span>
                                    <span style="height: 86%;"></span>

                                </div>

                            </div>


                            <!-- 평균 리뷰 점수 -->
                            <div class="analysis-stat-card rating-card">

                                <span class="analysis-stat-label">
                                    평균 리뷰 점수
                                </span>

                                <div class="analysis-rating-row">

                                    <strong class="analysis-stat-value">
                                        ${businessMain.averageRating}<small>점</small>
                                    </strong>

                                    <!-- 평균 별점 -->
                                    <div class="analysis-stars"
                                        aria-label="평균 리뷰 점수 ${businessMain.averageRating}점">

                                        <c:forEach begin="1"
                                                end="5"
                                                var="star">

                                            <span class="${businessMain.averageRating >= star
                                                            ? 'star-active'
                                                            : 'star-empty'}">
                                                ★
                                            </span>

                                        </c:forEach>

                                    </div>

                                </div>


                                <!-- 5점 만점 진행 막대 -->
                                <div class="analysis-rating-progress">

                                    <div
                                        class="analysis-rating-progress-bar"
                                        style="width: ${businessMain.averageRating * 20}%;">
                                    </div>

                                </div>

                                <span class="analysis-stat-caption">
                                    전체 상품 리뷰 기준
                                </span>

                            </div>

                        </div>


                        <!-- 하단 분석 요약 -->
                        <div class="dashboard-analysis-detail">

                            <!-- 조회수 원형 지표 -->
                            <div class="analysis-circle-area">

                                <div class="analysis-circle">

                                    <div class="analysis-circle-inner">

                                        <strong>
                                            ${businessMain.clickCount}
                                        </strong>

                                        <span>
                                            총 조회수
                                        </span>

                                    </div>

                                </div>

                            </div>


                            <!-- 간단한 분석 정보 -->
                            <div class="analysis-summary-box">

                                <h4>분석 요약</h4>

                                <div class="analysis-summary-item">
                                    <span class="summary-check">✓</span>

                                    <p>
                                        현재 등록 상품의 누적 조회수는
                                        <strong>${businessMain.clickCount}회</strong>입니다.
                                    </p>
                                </div>

                                <div class="analysis-summary-item">
                                    <span class="summary-check">✓</span>

                                    <p>
                                        평균 리뷰 점수는
                                        <strong>${businessMain.averageRating}점</strong>입니다.
                                    </p>
                                </div>

                                <div class="analysis-summary-item">
                                    <span class="summary-check">✓</span>

                                    <p>
                                        인기 상품 순위는 조회수를 기준으로 집계됩니다.
                                    </p>
                                </div>

                            </div>

                        </div>

                    </div>

                    <div class="panel">
                        <h3>인기 상품</h3>
                        <ul>
                           <div>

                            <c:forEach var="product"
                                    items="${businessMain.popularProducts}"
                                    varStatus="status">

                                <div class="item-card">

                                    <!-- 순위 -->
                                    <span class="badge badge-blue">
                                        ${status.index + 1}위
                                    </span>

                                    <!-- 대표 이미지 -->
                                    <div class="product-thumb">

                                        <c:choose>

                                            <c:when test="${not empty product.imagePath}">
                                                <img src="${pageContext.request.contextPath}${product.imagePath}"
                                                    alt="<c:out value='${product.productName}'/>">
                                            </c:when>

                                            <c:otherwise>
                                                <span>상품</span>
                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                    <!-- 상품 정보 -->
                                    <div class="item-info">

                                        <div class="product-name">
                                            <c:out value="${product.productName}"/>
                                        </div>

                                        <div class="meta">
                                            <span>
                                                조회수 ${product.clickCount}회
                                            </span>
                                        </div>

                                    </div>

                                </div>

                            </c:forEach>

                            <c:if test="${empty businessMain.popularProducts}">
                                <p>인기 상품 데이터가 없습니다.</p>
                            </c:if>

                        </div>
                        </ul>
                    </div>

                </section>

            </main>

        </div>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>
</html>
