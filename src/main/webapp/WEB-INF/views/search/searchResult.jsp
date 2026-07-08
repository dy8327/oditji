<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 검색 결과</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content.css">

    <script defer
            src="${pageContext.request.contextPath}/js/content.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<!-- ================================
     SEARCH RESULT WRAP
================================ -->
<main class="content-container">

    <!-- HEADER -->
    <section class="content-header">

        <h1>
            검색 결과
            <span style="font-size:14px; color:#aaa;">
                "${keyword}"
            </span>
        </h1>

    </section>

    <!-- EMPTY STATE -->
    <c:if test="${empty resultList}">
        <section class="empty-state">
            <p>검색 결과가 없습니다.</p>
        </section>
    </c:if>

    <!-- RESULT GRID -->
    <section class="content-grid">

        <c:forEach var="c" items="${resultList}">

            <article class="content-card">

                <!-- CARD LINK -->
                <a class="content-card__link"
                   href="${pageContext.request.contextPath}/content/detail?contentNo=${c.contentNo}">

                    <!-- POSTER -->
                    <div class="content-card__poster">

                        <img src="${c.thumbnail}"
                             alt="${c.title}"/>

                    </div>

                    <!-- INFO -->
                    <div class="content-card__info">

                        <h3 class="content-card__title">
                            ${c.title}
                        </h3>

                        <div class="content-card__meta">

                            <span class="content-type">
                                ${c.contentType}
                            </span>

                            <span class="content-rating">
                                ⭐ ${c.rating}
                            </span>

                        </div>

                    </div>

                </a>

                <!-- FAVORITE BUTTON -->
                <button type="button"
                        class="content-card__fav-btn"
                        data-content-no="${c.contentNo}">
                    ♡
                </button>

            </article>

        </c:forEach>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>