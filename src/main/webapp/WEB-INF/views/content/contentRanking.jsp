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

<title>인기 랭킹 | ODITJI</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/oditji.css">
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
                    <c:out value="${panel.tabLabel}" />
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
