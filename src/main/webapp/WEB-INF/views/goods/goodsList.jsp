<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 굿즈 리스트</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content.css">

    <script defer
            src="${pageContext.request.contextPath}/js/content.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-container">

    <!-- HEADER -->
    <section class="content-header">
        <h1>굿즈 리스트</h1>
    </section>

    <!-- EMPTY -->
    <c:if test="${empty goodsList}">
        <div class="empty-state">
            등록된 굿즈가 없습니다.
        </div>
    </c:if>

    <!-- GRID -->
    <section class="content-grid">

        <c:forEach var="g" items="${goodsList}">

            <article class="content-card">

                <!-- DETAIL LINK -->
                <a class="content-card__link"
                   href="${pageContext.request.contextPath}/goods/detail?id=${g.goodsId}">

                    <!-- IMAGE -->
                    <div class="content-card__poster">
                        <img src="${empty g.imageUrl ? '/images/no-image.png' : g.imageUrl}"
                             alt="${g.goodsName}" />
                    </div>

                    <!-- INFO -->
                    <div class="content-card__info">

                        <h3 class="content-card__title">
                            ${g.goodsName}
                        </h3>

                        <div class="content-card__meta">

                            <span>₩ ${g.price}</span>

                            <span>재고 ${g.stock}</span>

                        </div>

                    </div>

                </a>

                <!-- CART / FAVORITE -->
                <button type="button"
                        class="content-card__fav-btn"
                        data-goods-id="${g.goodsId}"
                        data-type="goods">
                    🛒
                </button>

            </article>

        </c:forEach>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>