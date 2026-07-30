<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | ${person.personName} 필모그래피</title>

<c:url var="contentCssUrl" value="/css/content.css"/>
<c:url var="componentCssUrl" value="/css/component.css"/>
<c:url var="filmographyJsUrl" value="/js/personFilmography.js"/>

<link rel="stylesheet" href="${componentCssUrl}">
<link rel="stylesheet" href="${contentCssUrl}">
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="filmography-container">

    <div class="back-area">
        <button type="button"
                class="back-btn"
                onclick="history.back()">← 뒤로가기</button>
    </div>

    <section class="person-hero">

        <div class="person-hero-profile">
            <c:choose>
                <c:when test="${not empty person.profilePath}">
                    <img src="https://image.tmdb.org/t/p/h632${person.profilePath}"
                         alt="${person.personName}">
                </c:when>
                <c:otherwise>
                    <div class="person-hero-no-image">NO IMAGE</div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="person-hero-info">
            <span class="person-role-badge">
                <c:choose>
                    <c:when test="${person.role eq 'ACTOR'}">배우</c:when>
                    <c:when test="${person.role eq 'CREATOR'}">크리에이터</c:when>
                    <c:otherwise>감독</c:otherwise>
                </c:choose>
            </span>

            <h1>${person.personName}</h1>

            <div class="person-basic-info">
                <c:if test="${not empty person.birthday}">
                    <span>생년월일 ${person.birthday}</span>
                </c:if>

                <c:if test="${not empty person.placeOfBirth}">
                    <span>출생지 ${person.placeOfBirth}</span>
                </c:if>
            </div>

            <c:choose>
                <c:when test="${not empty person.biography}">
                    <p class="person-biography">${person.biography}</p>
                </c:when>
                <c:otherwise>
                    <p class="person-biography person-biography-empty">
                        등록된 인물 소개가 없습니다.
                    </p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <section class="filmography-section"
             data-filmography-tabs>

        <div class="filmography-header">
            <div>
                <h2>필모그래피</h2>
                <p class="filmography-summary">
                    출연, 감독 및 제작 참여 작품을 확인할 수 있습니다.
                </p>
            </div>

            <span class="filmography-total-count">
                총 ${person.totalCount}개
            </span>
        </div>

        <div class="filmography-tabs"
             role="tablist"
             aria-label="필모그래피 분류">

            <button type="button"
                    class="filmography-tab-button is-active"
                    id="filmography-tab-cast"
                    role="tab"
                    aria-controls="filmography-panel-cast"
                    aria-selected="true"
                    tabindex="0"
                    data-tab-target="cast">
                출연작
                <span>${person.castList.size()}</span>
            </button>

            <button type="button"
                    class="filmography-tab-button"
                    id="filmography-tab-director"
                    role="tab"
                    aria-controls="filmography-panel-director"
                    aria-selected="false"
                    tabindex="-1"
                    data-tab-target="director">
                감독
                <span>${person.directorList.size()}</span>
            </button>

            <button type="button"
                    class="filmography-tab-button"
                    id="filmography-tab-production"
                    role="tab"
                    aria-controls="filmography-panel-production"
                    aria-selected="false"
                    tabindex="-1"
                    data-tab-target="production">
                제작 참여
                <span>${person.productionList.size()}</span>
            </button>

            <%--
                관련 상품 탭은 상품 등록 시 이 인물이 관련 배우로
                연결되어 있고, 그 상품이 승인 완료된 경우에만 표출됩니다.
            --%>
            <c:if test="${not empty relatedGoodsList}">
                <button type="button"
                        class="filmography-tab-button"
                        id="filmography-tab-goods"
                        role="tab"
                        aria-controls="filmography-panel-goods"
                        aria-selected="false"
                        tabindex="-1"
                        data-tab-target="goods">
                    관련 상품
                    <span>${relatedGoodsList.size()}</span>
                </button>
            </c:if>
        </div>

        <div class="filmography-tab-panels">

            <section class="filmography-tab-panel is-active"
                     id="filmography-panel-cast"
                     role="tabpanel"
                     aria-labelledby="filmography-tab-cast"
                     data-tab-panel="cast">

                <c:choose>
                    <c:when test="${not empty person.castList}">
                        <div class="filmography-grid">
                            <c:forEach var="work"
                                       items="${person.castList}">

                                <c:url var="contentPrepareUrl"
                                       value="/content/prepare">
                                    <c:param name="tmdbId"
                                             value="${work.tmdbId}"/>
                                    <c:param name="contentType"
                                             value="${work.contentType}"/>
                                </c:url>

                                <a href="${contentPrepareUrl}"
                                   class="filmography-card">

                                    <div class="filmography-poster">
                                        <c:choose>
                                            <c:when test="${not empty work.posterPath}">
                                                <img src="https://image.tmdb.org/t/p/w342${work.posterPath}"
                                                     alt="${work.title}">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="filmography-no-image">
                                                    NO IMAGE
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <span class="filmography-type">
                                            ${work.contentType}
                                        </span>
                                    </div>

                                    <div class="filmography-info">
                                        <h3>${work.title}</h3>

                                        <div class="filmography-meta">
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty work.releaseDate}">
                                                        ${work.releaseDate}
                                                    </c:when>
                                                    <c:otherwise>
                                                        공개일 미정
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>

                                            <c:if test="${not empty work.tmdbScore}">
                                                <span>⭐ ${work.tmdbScore}</span>
                                            </c:if>
                                        </div>

                                        <div class="filmography-role">
                                            <span class="filmography-role-label">
                                                배역
                                            </span>
                                            <span class="filmography-role-value">
                                                <c:choose>
                                                    <c:when test="${not empty work.participationName}">
                                                        ${work.participationName}
                                                    </c:when>
                                                    <c:otherwise>
                                                        배역 정보 없음
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="empty-state">
                            표시할 출연작이 없습니다.
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="filmography-tab-panel"
                     id="filmography-panel-director"
                     role="tabpanel"
                     aria-labelledby="filmography-tab-director"
                     data-tab-panel="director"
                     hidden>

                <c:choose>
                    <c:when test="${not empty person.directorList}">
                        <div class="filmography-grid">
                            <c:forEach var="work"
                                       items="${person.directorList}">

                                <c:url var="contentPrepareUrl"
                                       value="/content/prepare">
                                    <c:param name="tmdbId"
                                             value="${work.tmdbId}"/>
                                    <c:param name="contentType"
                                             value="${work.contentType}"/>
                                </c:url>

                                <a href="${contentPrepareUrl}"
                                   class="filmography-card">

                                    <div class="filmography-poster">
                                        <c:choose>
                                            <c:when test="${not empty work.posterPath}">
                                                <img src="https://image.tmdb.org/t/p/w342${work.posterPath}"
                                                     alt="${work.title}">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="filmography-no-image">
                                                    NO IMAGE
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <span class="filmography-type">
                                            ${work.contentType}
                                        </span>
                                    </div>

                                    <div class="filmography-info">
                                        <h3>${work.title}</h3>

                                        <div class="filmography-meta">
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty work.releaseDate}">
                                                        ${work.releaseDate}
                                                    </c:when>
                                                    <c:otherwise>
                                                        공개일 미정
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>

                                            <c:if test="${not empty work.tmdbScore}">
                                                <span>⭐ ${work.tmdbScore}</span>
                                            </c:if>
                                        </div>

                                        <div class="filmography-role">
                                            <span class="filmography-role-label">
                                                역할
                                            </span>
                                            <span class="filmography-role-value">
                                                <c:choose>
                                                    <c:when test="${not empty work.participationName}">
                                                        ${work.participationName}
                                                    </c:when>
                                                    <c:otherwise>
                                                        감독
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="empty-state">
                            표시할 감독 작품이 없습니다.
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="filmography-tab-panel"
                     id="filmography-panel-production"
                     role="tabpanel"
                     aria-labelledby="filmography-tab-production"
                     data-tab-panel="production"
                     hidden>

                <c:choose>
                    <c:when test="${not empty person.productionList}">
                        <div class="filmography-grid">
                            <c:forEach var="work"
                                       items="${person.productionList}">

                                <c:url var="contentPrepareUrl"
                                       value="/content/prepare">
                                    <c:param name="tmdbId"
                                             value="${work.tmdbId}"/>
                                    <c:param name="contentType"
                                             value="${work.contentType}"/>
                                </c:url>

                                <a href="${contentPrepareUrl}"
                                   class="filmography-card">

                                    <div class="filmography-poster">
                                        <c:choose>
                                            <c:when test="${not empty work.posterPath}">
                                                <img src="https://image.tmdb.org/t/p/w342${work.posterPath}"
                                                     alt="${work.title}">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="filmography-no-image">
                                                    NO IMAGE
                                                </div>
                                            </c:otherwise>
                                        </c:choose>

                                        <span class="filmography-type">
                                            ${work.contentType}
                                        </span>
                                    </div>

                                    <div class="filmography-info">
                                        <h3>${work.title}</h3>

                                        <div class="filmography-meta">
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty work.releaseDate}">
                                                        ${work.releaseDate}
                                                    </c:when>
                                                    <c:otherwise>
                                                        공개일 미정
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>

                                            <c:if test="${not empty work.tmdbScore}">
                                                <span>⭐ ${work.tmdbScore}</span>
                                            </c:if>
                                        </div>

                                        <div class="filmography-role">
                                            <span class="filmography-role-label">
                                                역할
                                            </span>
                                            <span class="filmography-role-value">
                                                <c:choose>
                                                    <c:when test="${not empty work.participationName}">
                                                        ${work.participationName}
                                                    </c:when>
                                                    <c:otherwise>
                                                        제작 참여
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                </a>
                            </c:forEach>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="empty-state">
                            표시할 제작 참여 작품이 없습니다.
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

            <%--
                관련 상품 패널도 탭 버튼과 동일하게
                relatedGoodsList가 비어있지 않을 때만 렌더링합니다.
            --%>
            <c:if test="${not empty relatedGoodsList}">
                <section class="filmography-tab-panel"
                         id="filmography-panel-goods"
                         role="tabpanel"
                         aria-labelledby="filmography-tab-goods"
                         data-tab-panel="goods"
                         hidden>

                    <div class="filmography-grid">
                        <c:forEach var="goods"
                                   items="${relatedGoodsList}">

                            <c:url var="goodsDetailUrl"
                                   value="/goods/goodsDetail/${goods.productNo}"/>

                            <%--
                                mainImage는 "/uploads/product/파일명"처럼
                                컨텍스트 패스가 붙지 않은 루트 상대경로로 저장되어 있으므로,
                                c:url로 감싸 현재 배포 컨텍스트 패스를 자동으로 붙여줍니다.
                            --%>
                            <c:if test="${not empty goods.mainImage}">
                                <c:url var="goodsMainImageUrl"
                                       value="${goods.mainImage}"/>
                            </c:if>

                            <a href="${goodsDetailUrl}"
                               class="filmography-card">

                                <div class="filmography-poster">
                                    <c:choose>
                                        <c:when test="${not empty goods.mainImage}">
                                            <img src="${goodsMainImageUrl}"
                                                 alt="${goods.productName}">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="filmography-no-image">
                                                NO IMAGE
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                    <span class="filmography-type">
                                        ${goods.productType}
                                    </span>
                                </div>

                                <div class="filmography-info">
                                    <h3>${goods.productName}</h3>

                                    <div class="filmography-meta">
                                        <span>${goods.businessName}</span>

                                        <c:if test="${goods.stock <= 0}">
                                            <span>품절</span>
                                        </c:if>
                                    </div>

                                    <div class="filmography-role">
                                        <span class="filmography-role-label">
                                            가격
                                        </span>
                                        <span class="filmography-role-value">
                                            <c:choose>
                                                <c:when test="${goods.discountRate > 0}">
                                                    <fmt:formatNumber value="${goods.discountPrice}"
                                                                       type="number"/>원
                                                    (${goods.discountRate}% 할인)
                                                </c:when>
                                                <c:otherwise>
                                                    <fmt:formatNumber value="${goods.price}"
                                                                       type="number"/>원
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </div>
                                </div>
                            </a>
                        </c:forEach>
                    </div>
                </section>
            </c:if>
        </div>
    </section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script src="${filmographyJsUrl}?v=2"></script>
</body>
</html>