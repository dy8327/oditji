<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>ODITJI MAIN</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/main.css">
<script defer src="${pageContext.request.contextPath}/js/main.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="main">

<section class="hero">

    <div class="hero-left">
        <div class="hero-sub">OTT 통합 검색 서비스</div>

        <h1 class="hero-title">
            세상 모든 OTT<br/>
            오딧지?!
        </h1>

        <p class="hero-desc">
            영화, 드라마, 예능, 그리고 관련 굿즈까지<br/>
            OTT 정보를 한 번에 확인하세요.
        </p>
    </div>

    <div class="hero-right">

        <div class="hero-box">
            <div class="box-title">인기 콘텐츠</div>

            <div class="box-list">

                <c:choose>
                    <c:when test="${not empty contentList}">
                        <c:forEach var="content" items="${contentList}" begin="0" end="3">
                            <div class="box-item">
                                ${content.title}
                            </div>
                        </c:forEach>
                    </c:when>

                    <c:otherwise>
                        <div class="box-item">데이터 없음</div>
                    </c:otherwise>
                </c:choose>

            </div>

        </div>

    </div>

</section>

<section class="slider-section">

    <h2 class="section-title">오늘의 콘텐츠</h2>

    <div class="slider">

        <button class="btn" type="button" onclick="moveSlider('today','left')">‹</button>

        <div class="track" id="todaySlider">

            <c:choose>
                <c:when test="${not empty contentList}">
                    <c:forEach var="content" items="${contentList}">

                        <c:if test="${not empty content.contentNo}">

                            <a href="${pageContext.request.contextPath}/content/contentDetail/${content.contentNo}"
                               class="card">

                                <div class="thumb">

                                    <c:choose>
                                        <c:when test="${not empty content.posterPath}">
                                            <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                                 alt="${content.title}">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="no-img"></div>
                                        </c:otherwise>
                                    </c:choose>

                                </div>

                            </a>

                        </c:if>

                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <div class="card">데이터 없음</div>
                </c:otherwise>
            </c:choose>

        </div>

        <button class="btn" type="button" onclick="moveSlider('today','right')">›</button>

    </div>

</section>

<section class="slider-section">

    <h2 class="section-title">추천 콘텐츠</h2>

    <div class="slider">

        <button class="btn" type="button" onclick="moveSlider('rec','left')">‹</button>

        <div class="track" id="recSlider">

            <c:choose>
                <c:when test="${not empty contentList}">
                    <c:forEach var="content" items="${contentList}">

                        <c:if test="${not empty content.contentNo}">

                            <a href="${pageContext.request.contextPath}/content/contentDetail/${content.contentNo}"
                               class="card">

                                <div class="thumb">

                                    <c:choose>
                                        <c:when test="${not empty content.posterPath}">
                                            <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                                 alt="${content.title}">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="no-img"></div>
                                        </c:otherwise>
                                    </c:choose>

                                </div>

                            </a>

                        </c:if>

                    </c:forEach>
                </c:when>

                <c:otherwise>
                    <div class="card">데이터 없음</div>
                </c:otherwise>
            </c:choose>

        </div>

        <button class="btn" type="button" onclick="moveSlider('rec','right')">›</button>

    </div>

</section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>