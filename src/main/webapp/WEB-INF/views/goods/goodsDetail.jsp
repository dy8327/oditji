<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 굿즈 상세</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content.css">

    <script defer
            src="${pageContext.request.contextPath}/js/goods.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="detail-container">

    <!-- ================= GOODS HEADER ================= -->
    <section class="detail-header">

        <div class="detail-poster">
            <img src="${goods.image}" alt="${goods.name}">
        </div>

        <div class="detail-info">

            <h1 class="detail-title">
                ${goods.name}
            </h1>

            <div class="detail-meta">
                <span>₩ ${goods.price}</span>
                <span>재고 ${goods.stock}</span>
                <span>${goods.category}</span>
            </div>

            <p class="detail-desc">
                ${goods.description}
            </p>

            <!-- ACTION -->
            <div class="detail-actions">

                <a class="btn"
                   href="${pageContext.request.contextPath}/goods/list">
                    목록
                </a>

                <button type="button"
                        class="btn fav-btn"
                        data-type="goods"
                        data-goods-id="${goods.goodsNo}">
                    🛒 장바구니
                </button>

            </div>

        </div>

    </section>

    <!-- ================= RELATED CONTENT ================= -->
    <section class="detail-section">

        <h2>관련 콘텐츠</h2>

        <div class="content-grid">

            <c:forEach var="c" items="${contentList}">

                <article class="content-card">

                    <a class="content-card__link"
                       href="${pageContext.request.contextPath}/content/detail?contentNo=${c.contentNo}">

                        <div class="content-card__poster">
                            <img src="${c.thumbnail}" alt="${c.title}">
                        </div>

                        <div class="content-card__info">

                            <h3 class="content-card__title">
                                ${c.title}
                            </h3>

                            <div class="content-card__meta">
                                <span>${c.contentType}</span>
                                <span>⭐ ${c.rating}</span>
                            </div>

                        </div>

                    </a>

                </article>

            </c:forEach>

        </div>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>