<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <title>ODITJI | 찜 목록</title>

    <link rel="stylesheet"
  <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/component.css">
          href="${pageContext.request.contextPath}/css/favorite.css">

    <script>
        const contextPath = "${pageContext.request.contextPath}";
    </script>

    <script defer
            src="${pageContext.request.contextPath}/js/favorite.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="favorite-container">

    <!-- HEADER -->
    <section class="favorite-header">
        <h1>
            찜 목록
            <span class="fav-count-badge" id="favCount">
                ${totalFavoriteCount}
            </span>
        </h1>
    </section>

    <!-- TAB -->
    <section class="favorite-tab">

        <button class="tab-btn active"
                data-tab="content"
                data-count-target="contentTabCount">
            콘텐츠 (<span id="contentTabCount">${contentCount}</span>)
        </button>

        <button class="tab-btn"
                data-tab="goods"
                data-count-target="goodsTabCount">
            상품 (<span id="goodsTabCount">${goodsCount}</span>)
        </button>

    </section>

    <!-- CONTENT TAB -->
    <section class="tab-content active" id="content-tab">

        <c:if test="${empty contentFavoriteList}">
            <div class="empty-state">찜한 콘텐츠가 없습니다.</div>
        </c:if>

        <div class="favorite-grid">

            <c:forEach var="c" items="${contentFavoriteList}">

                <article class="favorite-card">

                    <a class="favorite-card-link"
                       href="${pageContext.request.contextPath}/content/contentDetail/${c.contentNo}">

                        <div class="favorite-poster">
                            <c:choose>
                                <c:when test="${not empty c.posterPath}">
                                    <img src="https://image.tmdb.org/t/p/w500${c.posterPath}"
                                         alt="${c.title}"
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div class="no-img">NO IMAGE</div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="favorite-info">
                            <h3 class="favorite-title">${c.title}</h3>
                            <div class="favorite-meta">
                                <span>${c.contentType}</span>
                                <span>⭐ ${c.tmdbScore}</span>
                            </div>
                        </div>

                    </a>

                    <button type="button"
                            class="fav-btn active"
                            data-type="content"
                            data-content-no="${c.contentNo}">
                        ♥
                    </button>

                </article>

            </c:forEach>

        </div>

    </section>

    <!-- GOODS TAB -->
    <section class="tab-content" id="goods-tab">

        <c:if test="${empty goodsFavoriteList}">
            <div class="empty-state">찜한 상품이 없습니다.</div>
        </c:if>

        <div class="favorite-grid">

            <c:forEach var="g" items="${goodsFavoriteList}">

                <article class="favorite-card">

                    <a class="favorite-card-link"
                       href="${pageContext.request.contextPath}/goods/goodsDetail/${g.productNo}">

                        <div class="favorite-poster">
                            <c:choose>
                                <c:when test="${not empty g.mainImage}">
                                    <img src="${g.mainImage}"
                                         alt="${g.productName}"
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div class="no-img">NO IMAGE</div>
                                </c:otherwise>
                            </c:choose>

                            <c:if test="${g.discountRate > 0}">
                                <span class="discount-badge">${g.discountRate}%</span>
                            </c:if>
                        </div>

                        <div class="favorite-info">

                            <p class="favorite-brand">${g.businessName}</p>

                            <h3 class="favorite-title">${g.productName}</h3>

                            <div class="favorite-meta">
                                <c:choose>
                                    <c:when test="${g.discountRate > 0}">
                                        <span class="price-original">₩ ${g.price}</span>
                                        <span class="price-final">
                                            ₩ ${g.price - (g.price * g.discountRate / 100)}
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="price-final">₩ ${g.price}</span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                        </div>

                    </a>

                    <button type="button"
                            class="fav-btn active"
                            data-type="goods"
                            data-product-no="${g.productNo}">
                        ♥
                    </button>

                </article>

            </c:forEach>

        </div>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>
