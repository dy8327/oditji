<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="oditji" tagdir="/WEB-INF/tags/content" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>오늘의 콘텐츠 | ODITJI</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content-more.css?v=2">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="content-more-page">

    <section class="content-more-hero">

        <div>

            <p class="content-more-kicker">
                TODAY
            </p>

            <h1>
                오늘의 콘텐츠
            </h1>

            <p class="content-more-description">
                최근 30일 이내 공개된 인기 영화와 TV 콘텐츠를 확인해보세요.
            </p>

        </div>

        <div class="content-more-count">
            총 ${contentCount}개
        </div>

    </section>

    <section class="content-more-grid-section">

        <c:choose>

            <c:when test="${not empty todayContentList}">

                <div class="content-more-grid">

                    <c:forEach var="content"
                               items="${todayContentList}">

                        <oditji:contentCard content="${content}" variant="more" />

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>

                <div class="content-more-empty">
                    조건에 맞는 오늘의 콘텐츠가 없습니다.
                </div>

            </c:otherwise>

        </c:choose>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>