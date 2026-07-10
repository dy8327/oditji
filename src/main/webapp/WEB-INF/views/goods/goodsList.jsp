<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<title>ODITJI | 상품</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/goods.css">

<script defer
        src="${pageContext.request.contextPath}/js/goods.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="goods-list-container goods-page-container">

<div class="goods-layout">

    <!-- ================= LEFT FILTER SIDEBAR ================= -->
    <aside class="goods-left-sidebar">
        <jsp:include page="/WEB-INF/views/common/goodsLeftSidebar.jsp"/>
    </aside>

    <!-- ================= RESULT AREA ================= -->
    <div class="goods-result-area">

        <section class="list-header">

            <h1 class="list-title">
                <c:choose>
                    <c:when test="${type eq 'new'}">신상품</c:when>
                    <c:when test="${type eq 'best'}">베스트 상품</c:when>
                    <c:otherwise>상품 리스트</c:otherwise>
                </c:choose>
            </h1>

            <p class="result-count">
                총 <strong>${totalCount}</strong>개
            </p>

        </section>

        <c:if test="${empty goodsList}">
            <section class="empty-state">
                <p>등록된 상품이 없습니다.</p>
            </section>
        </c:if>

        <section class="card-list">

        <c:forEach var="g" items="${goodsList}">

            <article class="card-item ${g.stock <= 0 ? 'is-soldout' : ''}">

                <a class="card-link"
                href="${pageContext.request.contextPath}/goods/goodsDetail/${g.productNo}">

                    <div class="card-poster">

                        <c:choose>
                            <c:when test="${not empty g.mainImage}">
                                <img src="${g.mainImage}"
                                    alt="${g.productName}"
                                    loading="lazy"/>
                            </c:when>
                            <c:otherwise>
                                <div class="no-img">NO IMAGE</div>
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${g.stock <= 0}">
                            <div class="soldout-badge">SOLD OUT</div>
                        </c:if>

                    </div>

                    <div class="card-info">

                        <p class="card-brand">${g.businessName}</p>

                        <div class="card-title-wrap">

                            <h3 class="card-title">
                                ${g.productName}
                            </h3>

                            <c:if test="${g.discountRate > 0}">
                                <span class="discount-badge">할인</span>
                            </c:if>

                        </div>

                        <div class="card-meta">

                            <c:choose>

                                <c:when test="${g.discountRate > 0}">

                                    <span class="price-original">
                                        ₩ ${g.price}
                                    </span>

                                    <span class="price-final">
                                        <span class="rate">${g.discountRate}%</span>
                                        ₩ <fmt:formatNumber value="${g.discountPrice}" pattern="#,###"/>
                                    </span>

                                </c:when>

                                <c:otherwise>

                                    <span class="price-final">
                                        ₩ <fmt:formatNumber value="${g.price}" pattern="#,###"/>
                                    </span>

                                </c:otherwise>

                            </c:choose>

                        </div>

                        <div class="card-sub">

                            <c:choose>

                                <c:when test="${g.stock <= 0}">
                                    <span class="stock-warning">품절</span>
                                </c:when>

                                <c:when test="${g.stock <= 5}">
                                    <span class="stock-warning">
                                        재고 ${g.stock}개 남음
                                    </span>
                                </c:when>

                                <c:otherwise>
                                    재고 ${g.stock}개
                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                </a>

                <button type="button"
                        class="cart-btn"
                        data-product-no="${g.productNo}"
                        ${g.stock <= 0 ? 'disabled' : ''}>
                    🛒
                </button>

            </article>

        </c:forEach>

    </section>

        <section class="pagination">

            <c:if test="${page > 1}">
                <a class="page-btn"
                   href="?type=${type}&page=${page - 1}">‹</a>
            </c:if>

            <span class="page-now">${page}</span>

            <c:if test="${page < totalPage}">
                <a class="page-btn"
                   href="?type=${type}&page=${page + 1}">›</a>
            </c:if>

        </section>

    </div>

    <!-- ================= RIGHT RECOMMEND SIDEBAR ================= -->
    <aside class="goods-right-sidebar">
        <jsp:include page="/WEB-INF/views/common/goodsRightSidebar.jsp"/>
    </aside>

</div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
