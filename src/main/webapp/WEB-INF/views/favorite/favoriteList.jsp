<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="oditji" tagdir="/WEB-INF/tags/content" %>
<%@ taglib prefix="oditjiGoods" tagdir="/WEB-INF/tags/goods" %>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ODITJI | 찜 목록</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/favorite.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/component.css">
    <%-- 콘텐츠 탭: 콘텐츠리스트(content-list-card) 카드 디자인 재사용 --%>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content-list-modern.css">
    <%-- 상품 탭: 굿즈리스트(card-item) 카드 디자인 재사용 --%>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/goods.css">

    <script>
        const contextPath = "${pageContext.request.contextPath}";
    </script>

    <script defer
            src="${pageContext.request.contextPath}/js/favorite.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pagination-common.css?v=1">
    <script defer src="${pageContext.request.contextPath}/js/pagination.js?v=1"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="favorite-container">

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

        <button type="button"
                class="tab-btn ${activeTab eq 'content' ? 'active' : ''}"
                data-tab="content"
                data-count-target="contentTabCount">
            <span>콘텐츠</span>
            <b id="contentTabCount">${contentCount}</b>
        </button>

        <button type="button"
                class="tab-btn ${activeTab eq 'goods' ? 'active' : ''}"
                data-tab="goods"
                data-count-target="goodsTabCount">
            <span>상품</span>
            <b id="goodsTabCount">${goodsCount}</b>
        </button>

    </section>

    <!-- CONTENT TAB -->
    <section class="tab-content ${activeTab eq 'content' ? 'active' : ''}" id="content-tab">

        <c:if test="${empty contentFavoriteList}">
            <div class="empty-state">찜한 콘텐츠가 없습니다.</div>
        </c:if>

        <div class="content-list-card-grid">

            <c:forEach var="c" items="${contentFavoriteList}">

                <oditji:contentCard content="${c}"
                                    variant="grid"
                                    extraClass="favorite-card"
                                    detailUrl="${pageContext.request.contextPath}/content/contentDetail/${c.contentNo}"
                                    showPlatforms="false"
                                    scoreFormatted="false"
                                    favoriteMode="remove" />

            </c:forEach>

        </div>

        <c:if test="${contentCount > 0}">
            <nav class="oditji-pagination"
                 data-pagination
                 data-current-page="${contentPageVO.currentPage}"
                 data-total-page="${contentPageVO.totalPage}"
                 data-page-param="contentPage"
                 data-fixed-param-name="tab"
                 data-fixed-param-value="content"
                 aria-label="찜한 콘텐츠 페이지"></nav>
        </c:if>

    </section>

    <!-- GOODS TAB -->
    <section class="tab-content ${activeTab eq 'goods' ? 'active' : ''}" id="goods-tab">

        <c:if test="${empty goodsFavoriteList}">
            <div class="empty-state">찜한 상품이 없습니다.</div>
        </c:if>

        <div class="card-list">

            <c:forEach var="g" items="${goodsFavoriteList}">

                <oditjiGoods:goodsCard goods="${g}"
                                       extraClass="favorite-card"
                                       favoriteMode="remove" />

            </c:forEach>

        </div>

        <c:if test="${goodsCount > 0}">
            <nav class="oditji-pagination"
                 data-pagination
                 data-current-page="${goodsPageVO.currentPage}"
                 data-total-page="${goodsPageVO.totalPage}"
                 data-page-param="goodsPage"
                 data-fixed-param-name="tab"
                 data-fixed-param-value="goods"
                 aria-label="찜한 상품 페이지"></nav>
        </c:if>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>