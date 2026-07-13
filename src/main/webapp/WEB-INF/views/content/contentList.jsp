<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<title>ODITJI | 콘텐츠</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content.css">

<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/content.js"></script>

<script defer
        src="${pageContext.request.contextPath}/js/favorite.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-list-container">

<section class="list-header">

    <h1 class="list-title">
        <c:choose>
            <c:when test="${type eq 'new'}">신규 콘텐츠</c:when>
            <c:when test="${type eq 'popular'}">인기 콘텐츠</c:when>
            <c:otherwise>콘텐츠 리스트</c:otherwise>
        </c:choose>
    </h1>

    <p class="result-count">
        총 <strong>${totalCount}</strong>개
    </p>

</section>

<c:if test="${empty contentList}">
    <section class="empty-state">
        <p>등록된 콘텐츠가 없습니다.</p>
    </section>
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
                                 alt="${c.title}"
                                 loading="lazy"/>
                        </c:when>
                        <c:otherwise>
                            <div class="no-img">NO IMAGE</div>
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

            <button
                type="button"
                class="fav-btn"
                data-type="content"
                data-content-no="${c.contentNo}">
                ♡
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

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>