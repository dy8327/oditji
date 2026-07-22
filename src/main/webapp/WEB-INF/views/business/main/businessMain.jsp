<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <title>ODITJI | 사업자 페이지</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
    </head>
    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <div class="business-wrap">

            <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

            <main class="business-main">

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

                        <!-- 클릭수 -->
                        <a href="${pageContext.request.contextPath}/business/goodsManage"
                        class="dashboard-card">

                            <span>상품 조회수</span>

                            <strong>
                                ${businessMain.clickCount}회
                            </strong>

                        </a>

                        <!-- 구매율 -->
                        <a href="${pageContext.request.contextPath}/business/goodsManage"
                        class="dashboard-card">

                            <span>상품 구매율</span>

                            <strong>
                                ${businessMain.purchaseRate}%
                            </strong>

                        </a>

                        <!-- 정산 -->
                        <a href="${pageContext.request.contextPath}/business/settlementManage"
                        class="dashboard-card">

                            <span>정산 예정</span>

                            <strong>
                                ${businessMain.waitingSettlement}원
                            </strong>

                        </a>

                        <!-- 승인 -->
                        <a href="${pageContext.request.contextPath}/business/approvalManage"
                        class="dashboard-card">

                            <span>승인 대기 상품</span>

                            <strong>
                                ${businessMain.waitingProductCount}
                            </strong>

                        </a>

                    </div>

                </section>

                <!-- 최근 현황 -->
                <section class="business-content">

                   <div class="panel">
                        <h3>최근 주문</h3>
                        <ul>
                          <%--  <c:forEach var="order" items="${businessMain.recentOrders}">
                                <li>
                                    <span>${order.productName}</span>
                                    <strong>${order.status}</strong>
                                </li>
                            </c:forEach>
                            <c:if test="${empty businessMain.recentOrders}">--%>
                                <li>최근 주문 내역 없음</li>
                          <%--  </c:if> --%>
                        </ul>
                    </div>

                    <div class="panel">
                        <h3>최근 리뷰</h3>
                        <ul>
                          <%--  <c:forEach var="review" items="${businessMain.recentReviews}">
                                <li>
                                    <span>${review.content}</span>
                                    <strong>${review.rating}점</strong>
                                </li>
                            </c:forEach>
                            <c:if test="${empty businessMain.recentReviews}"> --%>
                                <li>리뷰 없음</li>
                            <%--</c:if> --%>
                        </ul>
                    </div>

                </section>

                <!-- 분석 -->
                <section class="analysis-section">

                    <div class="panel">
                        <h3>상품 분석</h3>
                        <p>
                            총 조회수
                            <strong>${businessMain.clickCount}</strong>
                            회
                        </p>
                        <p>
                            평균 리뷰 점수
                            <%-- <strong>${businessMain.averageRating}</strong> --%>
                            점
                        </p>
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
