<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport"
      content="width=device-width, initial-scale=1.0">

<title>추천 콘텐츠 | ODITJI</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content-more.css">

<script defer
        src="${pageContext.request.contextPath}/js/recommend-content.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-more-page recommend-page">

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

    <c:choose>

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
                                ${platform.platformName}
                            </span>

                        </div>

                    </c:forEach>

                </div>

            </section>

        </c:when>

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

                            <%@ include file="/WEB-INF/views/recommend/recommendCard.jsp" %>

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

                            <%@ include file="/WEB-INF/views/recommend/recommendCard.jsp" %>

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
                    최근 공개된 영화와 TV 콘텐츠예요.
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

                        <c:forEach var="content"
                                   items="${newContentList}">

                            <%@ include file="/WEB-INF/views/recommend/recommendCard.jsp" %>

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
                                ${platformSection.platformName}에서 볼 수 있는 추천
                            </h2>

                            <p>
                                ${platformSection.platformName}에서 지금 인기 있는 작품이에요.
                            </p>

                        </div>

                    </div>

                </div>

                <div class="recommend-slider">

                    <button type="button"
                            class="recommend-slider-btn"
                            data-slider-direction="left"
                            data-slider-target="platformRecommendTrack${sectionStatus.index}"
                            aria-label="${platformSection.platformName} 추천 이전 목록">
                        ‹
                    </button>

                    <div class="recommend-track"
                         id="platformRecommendTrack${sectionStatus.index}">

                        <c:forEach var="content"
                                   items="${platformSection.contentList}">

                            <%@ include file="/WEB-INF/views/recommend/recommendCard.jsp" %>

                        </c:forEach>

                    </div>

                    <button type="button"
                            class="recommend-slider-btn"
                            data-slider-direction="right"
                            data-slider-target="platformRecommendTrack${sectionStatus.index}"
                            aria-label="${platformSection.platformName} 추천 다음 목록">
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
