<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<title>ODITJI | 인기 콘텐츠</title>

<link rel="stylesheet"
  <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/component.css">
      href="${pageContext.request.contextPath}/css/content.css">

<script defer
        src="${pageContext.request.contextPath}/js/content.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-list-container">

<section class="list-header">
    <h1>인기 콘텐츠</h1>

    <p class="result-count">
        총 <strong>${totalCount}</strong>개의 콘텐츠
    </p>
</section>

<c:if test="${empty contentList}">
    <div class="empty-state">
        인기 콘텐츠가 없습니다.
    </div>
</c:if>

<section class="card-list">

    <c:forEach var="c" items="${contentList}">

        <article class="card-item">

            <a class="card-link"
               href="${pageContext.request.contextPath}/content/contentDetail/${c.contentNo}">

                <div class="card-poster">

                    <c:choose>
                        <c:when test="${not empty c.posterPath}">
                            <img src="https://image.tmdb.org/t/p/w500${c.posterPath}"
                                 alt="${c.title}">
                        </c:when>
                        <c:otherwise>
                            <div class="no-img"></div>
                        </c:otherwise>
                    </c:choose>

                </div>

                <div class="card-info">

                    <h3 class="card-title">${c.title}</h3>

                    <div class="card-meta">
                        <span>${c.contentType}</span>
                        <span>⭐ ${c.tmdbScore}</span>
                    </div>

                    <div class="card-sub">
                        ${c.genreText}
                    </div>

                </div>

            </a>

            <button type="button"
                    class="fav-btn"
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