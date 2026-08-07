<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="recommend" tagdir="/WEB-INF/tags/recommend" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>추천 콘텐츠 | ODITJI</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content-more.css?v=2">

<script defer
        src="${pageContext.request.contextPath}/js/recommend-content.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="content-more-page recommend-page">

    <section class="content-more-hero">

        <div>

            <p class="content-more-kicker">
                RECOMMEND
            </p>

            <h1>
                추천 콘텐츠
            </h1>

            <p class="content-more-description">

                <c:choose>

                    <c:when test="${personalizedRecommendation}">
                        회원님이 이용하는 OTT에서 시청 가능한 콘텐츠를 추천해드려요.
                    </c:when>

                    <c:otherwise>
                        전체 OTT에서 시청 가능한 인기·평점 기반 콘텐츠를 추천해드려요.
                    </c:otherwise>

                </c:choose>

            </p>

        </div>

    </section>

    <!-- =====================================================
         최근 관심 기록 기반 맞춤 OTT 추천

         추천 활성화 조건을 충족하면 단독 또는 공동 추천을 표시하고,
         아직 기록이 부족하면 RecommendService의 안내 문구를 표시합니다.
    ====================================================== -->
    <section class="ott-recommendation-section">

        <div class="ott-recommendation-heading">

            <div>
                <p class="ott-recommendation-kicker">
                    MY OTT MATCH
                </p>

                <h2>
                    나에게 맞는 OTT
                </h2>

                <p>
                    최근 둘러본 콘텐츠를 기준으로 분석했어요.
                </p>
            </div>

        </div>

        <c:choose>

            <%-- 추천 기준을 충족한 회원 --%>
            <c:when test="${ottRecommendation.recommendationAvailable}">

                <div class="ott-recommendation-result">

                    <div class="ott-recommendation-summary">

                        <span class="ott-recommendation-badge">
                            <c:choose>
                                <c:when test="${ottRecommendation.jointRecommendation}">
                                    공동 추천
                                </c:when>
                                <c:otherwise>
                                    1순위 추천
                                </c:otherwise>
                            </c:choose>
                        </span>

                        <h3>
                            ${ottRecommendation.statusMessage}
                        </h3>

                        <ul class="ott-recommendation-reasons">
                            <c:forEach var="reason"
                                       items="${ottRecommendation.recommendationReasons}">
                                <li>${reason}</li>
                            </c:forEach>
                        </ul>

                    </div>

                    <div class="ott-recommendation-platform-list">

                        <c:forEach var="platform"
                                   items="${ottRecommendation.recommendedPlatformList}"
                                   varStatus="status">

                            <article class="ott-recommendation-platform-card">

                                <div class="ott-recommendation-platform-rank">
                                    ${status.index + 1}
                                </div>

                                <div class="ott-recommendation-platform-logo">
                                    <c:choose>
                                        <c:when test="${not empty platform.logoImage}">
                                            <img src="${platform.logoImage}"
                                                 alt="${platform.platformName}">
                                        </c:when>
                                        <c:otherwise>
                                            <span>OTT</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="ott-recommendation-platform-info">
                                    <h4><common:platformDisplayName platformName="${platform.platformName}"/></h4>

                                    <p class="ott-recommendation-platform-caption">
                                        회원님의 최근 관심 흐름과 잘 맞는 OTT예요.
                                    </p>

                                    <div class="ott-recommendation-content-summary">

                                        <div class="ott-recommendation-content-row">
                                            <strong>찜한 콘텐츠</strong>

                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty platform.favoriteContentList}">
                                                        <c:forEach var="content"
                                                                   items="${platform.favoriteContentList}"
                                                                   varStatus="contentStatus">
                                                            <a href="${pageContext.request.contextPath}/content/contentDetail/${content.contentNo}">${content.title}</a><c:if test="${not contentStatus.last}">, </c:if>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>없음</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                        <div class="ott-recommendation-content-row">
                                            <strong>둘러본 콘텐츠</strong>

                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty platform.viewedContentList}">
                                                        <c:forEach var="content"
                                                                   items="${platform.viewedContentList}"
                                                                   varStatus="contentStatus">
                                                            <a href="${pageContext.request.contextPath}/content/contentDetail/${content.contentNo}">${content.title}</a><c:if test="${not contentStatus.last}">, </c:if>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>없음</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>

                                    </div>
                                </div>

                            </article>

                        </c:forEach>

                    </div>

                </div>

            </c:when>

            <%-- 비로그인 또는 추천 데이터가 아직 부족한 상태 --%>
            <c:otherwise>

                <div class="ott-recommendation-empty">

                    <div class="ott-recommendation-empty-icon">
                        ?
                    </div>

                    <div>
                        <h3>
                            아직 추천 결과를 준비하고 있어요
                        </h3>

                        <p>
                            최근 둘러본 콘텐츠가 조금 더 쌓이면
                            회원님에게 잘 맞는 OTT를 추천해드릴게요.
                        </p>
                    </div>

                    <c:if test="${not loggedIn}">
                        <a href="${pageContext.request.contextPath}/member/login">
                            로그인하기
                        </a>
                    </c:if>

                </div>

            </c:otherwise>

        </c:choose>

    </section>


    <c:choose>

        <%-- 로그인했고 OTT를 선택한 회원 --%>
        <c:when test="${personalizedRecommendation and not empty selectedPlatformList}">

            <section class="selected-platform-section">

                <h2>
                    나의 OTT
                </h2>

                <div class="selected-platform-list">

                    <c:forEach var="platform"
                               items="${selectedPlatformList}">

                        <div class="selected-platform-chip">

                            <c:if test="${not empty platform.logoImage}">

                                <img src="${platform.logoImage}"
                                     alt="${platform.platformName}">

                            </c:if>

                            <span>
                                <common:platformDisplayName platformName="${platform.platformName}"/>
                            </span>

                        </div>

                    </c:forEach>

                </div>

            </section>

        </c:when>

        <%-- 로그인했지만 OTT를 선택하지 않은 회원 --%>
        <c:when test="${loggedIn}">

            <section class="recommend-guide">

                <p>
                    이용 중인 OTT를 선택하면,
                    해당 OTT에서 시청 가능한 콘텐츠만 추천받을 수 있어요.
                </p>

                <a href="${pageContext.request.contextPath}/member/mypage?openOttModal=true">
                    OTT 선택
                </a>

            </section>

        </c:when>

        <%-- 비로그인 사용자 --%>
        <c:otherwise>

            <section class="recommend-guide">

                <p>
                    로그인 후 이용 중인 OTT를 선택하면,
                    해당 OTT에서 시청 가능한 콘텐츠만 추천받을 수 있어요.
                </p>

                <a href="${pageContext.request.contextPath}/member/login">
                    로그인하기
                </a>

            </section>

        </c:otherwise>

    </c:choose>


    <!-- =====================================================
         내 OTT 인기 콘텐츠
    ====================================================== -->
    <section class="recommend-slider-section">

        <div class="recommend-section-header">

            <div>

                <h2>

                    <c:choose>

                        <c:when test="${personalizedRecommendation}">
                            내 OTT에서 지금 인기 있는 콘텐츠
                        </c:when>

                        <c:otherwise>
                            지금 인기 있는 콘텐츠
                        </c:otherwise>

                    </c:choose>

                </h2>

                <p>
                    최근 많은 관심을 받고 있는 작품이에요.
                </p>

            </div>

        </div>

        <div class="recommend-slider">

            <button type="button"
                    class="recommend-slider-btn"
                    data-slider-direction="left"
                    data-slider-target="popularRecommendTrack"
                    aria-label="인기 추천 이전 목록">
                ‹
            </button>

            <div class="recommend-track"
                 id="popularRecommendTrack">

                <c:choose>

                    <c:when test="${not empty popularContentList}">

                        <c:forEach var="content"
                                   items="${popularContentList}">

                            <recommend:recommendCard content="${content}" />

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="recommend-section-empty">
                            인기 추천 콘텐츠가 없습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button type="button"
                    class="recommend-slider-btn"
                    data-slider-direction="right"
                    data-slider-target="popularRecommendTrack"
                    aria-label="인기 추천 다음 목록">
                ›
            </button>

        </div>

    </section>


    <!-- =====================================================
         내 OTT 평점 높은 콘텐츠
    ====================================================== -->
    <section class="recommend-slider-section">

        <div class="recommend-section-header">

            <div>

                <h2>

                    <c:choose>

                        <c:when test="${personalizedRecommendation}">
                            내 OTT에서 평점 높은 콘텐츠
                        </c:when>

                        <c:otherwise>
                            평점 높은 콘텐츠
                        </c:otherwise>

                    </c:choose>

                </h2>

                <p>
                    평점과 인기도를 기준으로 선정했어요.
                </p>

            </div>

        </div>

        <div class="recommend-slider">

            <button type="button"
                    class="recommend-slider-btn"
                    data-slider-direction="left"
                    data-slider-target="highRatedRecommendTrack"
                    aria-label="평점 추천 이전 목록">
                ‹
            </button>

            <div class="recommend-track"
                 id="highRatedRecommendTrack">

                <c:choose>

                    <c:when test="${not empty highRatedContentList}">

                        <c:forEach var="content"
                                   items="${highRatedContentList}">

                            <recommend:recommendCard content="${content}" />

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="recommend-section-empty">
                            평점 추천 콘텐츠가 없습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button type="button"
                    class="recommend-slider-btn"
                    data-slider-direction="right"
                    data-slider-target="highRatedRecommendTrack"
                    aria-label="평점 추천 다음 목록">
                ›
            </button>

        </div>

    </section>


    <!-- =====================================================
         내 OTT 신작
    ====================================================== -->
    <section class="recommend-slider-section">

        <div class="recommend-section-header">

            <div>

                <h2>

                    <c:choose>

                        <c:when test="${personalizedRecommendation}">
                            내 OTT 신작
                        </c:when>

                        <c:otherwise>
                            최근 공개된 콘텐츠
                        </c:otherwise>

                    </c:choose>

                </h2>

                <p>
                    영화는 최근 개봉일, TV는 최근 회차 공개일을 기준으로 보여드려요.
                </p>

            </div>

        </div>

        <div class="recommend-slider">

            <button type="button"
                    class="recommend-slider-btn"
                    data-slider-direction="left"
                    data-slider-target="newRecommendTrack"
                    aria-label="신작 추천 이전 목록">
                ‹
            </button>

            <div class="recommend-track"
                 id="newRecommendTrack">

                <c:choose>

                    <c:when test="${not empty newContentList}">

                        <%--
                            신작 영역에서만 TV 카드 날짜를 최근 회차 공개일로 표시합니다.
                            다른 추천 영역은 기존 최초 공개일 표시를 그대로 유지합니다.
                        --%>
                        <c:forEach var="content"
                                   items="${newContentList}">

                            <recommend:recommendCard content="${content}"
                                                      showRecentEpisodeDate="true" />

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="recommend-section-empty">
                            최근 공개된 추천 콘텐츠가 없습니다.
                        </div>

                    </c:otherwise>

                </c:choose>

            </div>

            <button type="button"
                    class="recommend-slider-btn"
                    data-slider-direction="right"
                    data-slider-target="newRecommendTrack"
                    aria-label="신작 추천 다음 목록">
                ›
            </button>

        </div>

    </section>


    <!-- =====================================================
         OTT별 추천
    ====================================================== -->
    <c:if test="${personalizedRecommendation
                  and not empty platformSectionList}">

        <c:forEach var="platformSection"
                   items="${platformSectionList}"
                   varStatus="sectionStatus">

            <section class="recommend-slider-section platform-recommend-section">

                <div class="recommend-section-header platform-section-header">

                    <div class="platform-section-title">

                        <c:if test="${not empty platformSection.logoImage}">

                            <img src="${platformSection.logoImage}"
                                 alt="${platformSection.platformName}">

                        </c:if>

                        <div>

                            <h2>
                                <common:platformDisplayName platformName="${platformSection.platformName}"/>에서 볼 수 있는 추천
                            </h2>

                            <p>
                                <common:platformDisplayName platformName="${platformSection.platformName}"/>에서 지금 인기 있는 작품이에요.
                            </p>

                        </div>

                    </div>

                </div>

                <div class="recommend-slider">

                    <button type="button"
                            class="recommend-slider-btn"
                            data-slider-direction="left"
                            data-slider-target="platformRecommendTrack${sectionStatus.index}"
                            aria-label="<common:platformDisplayName platformName='${platformSection.platformName}'/> 추천 이전 목록">
                        ‹
                    </button>

                    <div class="recommend-track"
                         id="platformRecommendTrack${sectionStatus.index}">

                        <c:forEach var="content"
                                   items="${platformSection.contentList}">

                            <recommend:recommendCard content="${content}" />

                        </c:forEach>

                    </div>

                    <button type="button"
                            class="recommend-slider-btn"
                            data-slider-direction="right"
                            data-slider-target="platformRecommendTrack${sectionStatus.index}"
                            aria-label="<common:platformDisplayName platformName='${platformSection.platformName}'/> 추천 다음 목록">
                        ›
                    </button>

                </div>

            </section>

        </c:forEach>

    </c:if>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>