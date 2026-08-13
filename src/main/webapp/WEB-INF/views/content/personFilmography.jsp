<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="contentTag" tagdir="/WEB-INF/tags/content" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | ${person.personName} 필모그래피</title>

<c:url var="contentCssUrl" value="/css/content.css"/>
<c:url var="componentCssUrl" value="/css/component.css"/>
<c:url var="filmographyJsUrl" value="/js/personFilmography.js"/>

<link rel="stylesheet" href="${componentCssUrl}">
<link rel="stylesheet" href="${contentCssUrl}">
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="filmography-container">

    <div class="back-area">
        <button type="button"
                class="back-btn"
                onclick="history.back()">← 뒤로가기</button>
    </div>

    <%--
        [강화] 콘텐츠 상세페이지(contentDetail.jsp)에서 만든 히어로
        백드롭 + 글래스 카드 언어를 인물 상세페이지에도 동일하게
        적용해 사이트 전체의 시각적 일관성을 맞춘다.
        별도 백드롭 이미지 컬럼이 없으므로 프로필 이미지를 재사용하며,
        순수 장식용 레이어라 스크린리더에는 노출하지 않는다.
        아래 person-hero의 마크업·EL 로직은 전혀 건드리지 않았다.
    --%>
    <section class="content-hero">

        <c:if test="${not empty person.profilePath}">
            <div class="content-hero-backdrop"
                 style="background-image:url('https://image.tmdb.org/t/p/w1280${person.profilePath}');"
                 aria-hidden="true">
            </div>
        </c:if>

        <div class="content-hero-scrim" aria-hidden="true"></div>

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
                        <span class="meta-chip">생년월일 ${person.birthday}</span>
                    </c:if>

                    <c:if test="${not empty person.placeOfBirth}">
                        <span class="meta-chip">출생지 ${person.placeOfBirth}</span>
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
    </section>

    <section class="filmography-section"
             data-filmography-tabs>

        <div class="filmography-header">
            <div class="filmography-header-title">
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

            <contentTag:filmographyWorkPanel
                    panelKey="cast"
                    workList="${person.castList}"
                    active="${true}"
                    roleLabel="배역"
                    defaultRole="배역 정보 없음"
                    emptyMessage="표시할 출연작이 없습니다." />

            <contentTag:filmographyWorkPanel
                    panelKey="director"
                    workList="${person.directorList}"
                    active="${false}"
                    roleLabel="역할"
                    defaultRole="감독"
                    emptyMessage="표시할 감독 작품이 없습니다." />

            <contentTag:filmographyWorkPanel
                    panelKey="production"
                    workList="${person.productionList}"
                    active="${false}"
                    roleLabel="역할"
                    defaultRole="제작 참여"
                    emptyMessage="표시할 제작 참여 작품이 없습니다." />

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