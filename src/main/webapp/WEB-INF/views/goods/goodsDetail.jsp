<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 상세</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/goods.css">

<script defer
        src="${pageContext.request.contextPath}/js/goods.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="goods-detail-container">

<div class="back-area">
    <a href="${pageContext.request.contextPath}/goods/list"
       class="back-btn">← 목록으로</a>
</div>

<section class="detail-header">

    <div class="detail-poster">

        <c:choose>
            <c:when test="${not empty goods.mainImage}">
                <img id="mainImage"
                     src="${goods.mainImage}"
                     alt="${goods.productName}">
            </c:when>
            <c:otherwise>
                <div class="no-img">NO IMAGE</div>
            </c:otherwise>
        </c:choose>

        <c:if test="${not empty imageList}">
            <div class="image-gallery">
                <c:forEach var="img" items="${imageList}">
                    <img src="${img.imagePath}"
                         alt="${goods.productName}"
                         class="${img.isMain eq 'Y' ? 'is-active' : ''}"
                         data-full="${img.imagePath}">
                </c:forEach>
            </div>
        </c:if>

    </div>

    <div class="detail-info">

        <p class="detail-brand">${goods.businessName}</p>

        <h1 class="detail-title">${goods.productName}</h1>

        <div class="detail-meta">

            <span>${goods.productType}</span>

            <span class="divider">|</span>

            <c:choose>
                <c:when test="${goods.stock <= 0}">
                    <span class="status-badge sold-out">품절</span>
                </c:when>
                <c:when test="${goods.status eq 'ON_SALE'}">
                    <span class="status-badge on-sale">판매중</span>
                </c:when>
                <c:otherwise>
                    <span class="status-badge waiting">${goods.status}</span>
                </c:otherwise>
            </c:choose>

            <span class="divider">|</span>

            <span>재고 ${goods.stock}개</span>

        </div>

        <div class="price-box">
        <c:choose>
            <c:when test="${goods.discountRate > 0}">

                <span class="price-original">
                    ₩ <fmt:formatNumber value="${goods.price}" pattern="#,###"/>
                </span>

                <span class="rate">${goods.discountRate}%</span>

                <span class="price-final">
                    ₩ <fmt:formatNumber
                            value="${(g.price - (g.price * g.discountRate / 100))}"
                            pattern="#,###"/>
                </span>

            </c:when>

            <c:otherwise>

                <span class="price-final">
                    ₩ <fmt:formatNumber value="${goods.price}" pattern="#,###"/>
                </span>

            </c:otherwise>
        </c:choose>
    </div>

        <p class="detail-desc">
            ${goods.description}
        </p>

        <div class="action-box">

            <a href="${pageContext.request.contextPath}/goods/list"
               class="btn">
                목록
            </a>

            <button type="button"
                    class="btn cart-btn"
                    data-product-no="${goods.productNo}"
                    ${goods.stock <= 0 ? 'disabled' : ''}>
                🛒 장바구니
            </button>

            <button type="button"
                    class="btn btn-primary buy-btn"
                    data-product-no="${goods.productNo}"
                    ${goods.stock <= 0 ? 'disabled' : ''}>
                바로 구매
            </button>

        </div>

    </div>

</section>

<c:if test="${not empty content}">

<section class="detail-section">

    <h2>원작 콘텐츠</h2>

    <div class="content-grid">

        <article class="content-card">

            <a class="content-card__link"
               href="${pageContext.request.contextPath}/content/contentDetail/${content.contentNo}">

                <div class="content-card__poster">
                    <c:choose>
                        <c:when test="${not empty content.posterPath}">
                            <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                 alt="${content.title}">
                        </c:when>
                        <c:otherwise>
                            <div class="no-img">NO IMAGE</div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="content-card__info">

                    <h3 class="content-card__title">${content.title}</h3>

                    <div class="content-card__meta">
                        <span>${content.contentType}</span>
                        <span>⭐ ${content.tmdbScore}</span>
                    </div>

                </div>

            </a>

        </article>

    </div>

    <c:if test="${not empty actor}">
        <p class="card-sub" style="margin-top:12px;">
            출연 : ${actor.actorName}
        </p>
    </c:if>

</section>

</c:if>

<section class="detail-section">

    <h2>상품 리뷰</h2>

    <div class="score-box">

        <c:choose>
            <c:when test="${not empty avgRating}">
                <span class="score-user">⭐ ${avgRating} (${reviewCount}건)</span>
            </c:when>
            <c:otherwise>
                <span class="score-user">아직 등록된 리뷰가 없습니다</span>
            </c:otherwise>
        </c:choose>

    </div>

    <div class="review-list">

        <c:choose>

            <c:when test="${not empty reviewList}">

                <c:forEach var="r" items="${reviewList}">

                    <div class="review-item">

                        <div class="review-meta">

                            <span class="writer">${r.writer}</span>

                            <span class="rating">
                                ⭐ ${r.rating}
                            </span>

                            <span class="date">
                                ${r.createdAt}
                            </span>

                        </div>

                        <p class="review-content">
                            ${r.content}
                        </p>

                    </div>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <div class="empty-state">
                    구매 후 첫 리뷰를 남겨보세요
                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
