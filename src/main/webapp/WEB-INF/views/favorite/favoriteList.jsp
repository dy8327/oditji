<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <title>ODITJI | 찜 목록</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/favorite.css">

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

        <button class="tab-btn active" data-tab="content">
            콘텐츠 (${contentCount})
        </button>

        <button class="tab-btn" data-tab="goods">
            상품 (${goodsCount})
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

                    <a href="/content/detail?contentNo=${c.contentNo}">
                        <img src="${c.thumbnail}">
                        <h3>${c.title}</h3>
                    </a>

                    <button class="fav-btn active"
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

                    <a href="/goods/detail?goodsNo=${g.goodsNo}">
                        <img src="${g.image}">
                        <h3>${g.name}</h3>
                    </a>

                    <button class="fav-btn active"
                            data-goods-no="${g.goodsNo}">
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