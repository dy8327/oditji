<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="oditji" tagdir="/WEB-INF/tags/content" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">

<title>인기 랭킹 | ODITJI</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/oditji.css">
<%--
    [리팩터링] 연령등급 배지(.age-rating-badge)의 원형/색상 스타일은
    content-list-modern.css에 공용으로 정의돼 있다. 랭킹 카드에서도
    common:ageRatingBadge 태그로 같은 배지를 재사용하기 위해 이 CSS를 같이 불러온다.
    (랭킹 페이지 전용 클래스는 여전히 oditji.css에 둔다)
--%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content-list-modern.css">
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body>

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main id="mainContent" class="ranking-page">

    <section class="ranking-header">
        <h1>한국 OTT 콘텐츠 인기 랭킹</h1>

        <p>
            한국에서 정액제로 제공되는 영화와 TV 콘텐츠를
            JSONL에 저장된 인기도와 평점 기준으로 정렬한 랭킹입니다.
        </p>
    </section>

    <nav class="ranking-tabs"
         aria-label="OTT 인기 랭킹 선택">

        <c:forEach var="panel"
                   items="${rankingPanels}">

            <button type="button"
                    class="${panel.active ? 'ranking-tab-button active' : 'ranking-tab-button'}"
                    data-ranking-tab="${panel.tabId}">

                <c:if test="${not empty panel.tabLogoImage}">

                    <img class="ranking-tab-logo"
                         src="${panel.tabLogoImage}"
                         alt="">

                </c:if>

                <span>
                    <common:platformDisplayName platformName="${panel.tabLabel}" />
                </span>

            </button>

        </c:forEach>

    </nav>

    <c:forEach var="panel"
               items="${rankingPanels}">

        <section class="${panel.active ? 'ranking-panel active' : 'ranking-panel'}"
                 id="ranking-panel-${panel.tabId}">

            <div class="ranking-panel-header">
                <div>
                    <h2 class="ranking-panel-title">
                        <span class="section-eyebrow section-eyebrow--rank">LIVE</span>
                        <c:out value="${panel.title}" />
                    </h2>

                    <p class="ranking-panel-description">
                        <c:out value="${panel.description}" />
                    </p>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty panel.rankingList}">

                    <ol class="ranking-list">

                        <c:forEach var="content"
                                   items="${panel.rankingList}"
                                   varStatus="status">

                            <oditji:rankingCard content="${content}"
                                                rank="${status.count}" />

                        </c:forEach>

                    </ol>

                </c:when>

                <c:otherwise>
                    <div class="ranking-empty">
                        <c:out value="${panel.emptyMessage}" />
                    </div>
                </c:otherwise>
            </c:choose>

        </section>

    </c:forEach>

</main>

<script defer
        src="${pageContext.request.contextPath}/js/contentRanking.js"></script>

</body>
</html>
