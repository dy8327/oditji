<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ODITJI | 찜 목록</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/favorite.css">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/component.css">
    <%-- 콘텐츠 탭: 콘텐츠리스트(content-list-card) 카드 디자인 재사용 --%>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content-list-modern.css">
    <%-- 상품 탭: 굿즈리스트(card-item) 카드 디자인 재사용 --%>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/goods.css">

    <script>
        const contextPath = "${pageContext.request.contextPath}";
    </script>

    <script defer
            src="${pageContext.request.contextPath}/js/favorite.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pagination-common.css?v=1">
    <script defer src="${pageContext.request.contextPath}/js/pagination.js?v=1"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="favorite-container">

    <!-- HEADER -->
    <section class="favorite-header">
        <h1>
            찜 목록
            <span class="fav-count-badge" id="favCount">
                ${totalFavoriteCount}
            </span>
        </h1>
    </section>

    <!-- TAB -->
    <section class="favorite-tab">

        <button type="button"
                class="tab-btn ${activeTab eq 'content' ? 'active' : ''}"
                data-tab="content"
                data-count-target="contentTabCount">
            <span>콘텐츠</span>
            <b id="contentTabCount">${contentCount}</b>
        </button>

        <button type="button"
                class="tab-btn ${activeTab eq 'goods' ? 'active' : ''}"
                data-tab="goods"
                data-count-target="goodsTabCount">
            <span>상품</span>
            <b id="goodsTabCount">${goodsCount}</b>
        </button>

    </section>

    <!-- CONTENT TAB -->
    <section class="tab-content ${activeTab eq 'content' ? 'active' : ''}" id="content-tab">

        <c:if test="${empty contentFavoriteList}">
            <div class="empty-state">찜한 콘텐츠가 없습니다.</div>
        </c:if>

        <div class="content-list-card-grid">

            <c:forEach var="c" items="${contentFavoriteList}">

                <%-- 장르 텍스트를 기준으로 사용자용 콘텐츠 분류를 표시합니다 (콘텐츠리스트와 동일 로직). --%>
                <c:set var="contentBadgeLabel" value="드라마"/>
                <c:set var="contentBadgeClass" value="drama"/>

                <c:choose>
                    <c:when test="${fn:contains(c.genreText, '애니메이션')}">
                        <c:set var="contentBadgeLabel" value="애니메이션"/>
                        <c:set var="contentBadgeClass" value="animation"/>
                    </c:when>
                    <c:when test="${fn:contains(c.genreText, '다큐멘터리')}">
                        <c:set var="contentBadgeLabel" value="다큐멘터리"/>
                        <c:set var="contentBadgeClass" value="documentary"/>
                    </c:when>
                    <c:when test="${c.contentType eq 'TV'
                                  and (fn:contains(c.genreText, '리얼리티')
                                       or fn:contains(c.genreText, '토크'))}">
                        <c:set var="contentBadgeLabel" value="예능"/>
                        <c:set var="contentBadgeClass" value="variety"/>
                    </c:when>
                    <c:when test="${c.contentType eq 'MOVIE'}">
                        <c:set var="contentBadgeLabel" value="영화"/>
                        <c:set var="contentBadgeClass" value="movie"/>
                    </c:when>
                </c:choose>

                <%-- 관람등급을 이미지 없이 CSS 배지로 표시합니다 (콘텐츠리스트와 동일 로직). --%>
                <c:set var="ageBadgeLabel" value="?"/>
                <c:set var="ageBadgeClass" value="unknown"/>
                <c:set var="ageBadgeTitle" value="등급 정보 없음"/>

                <c:choose>
                    <c:when test="${c.ageRating eq '전체 관람가'}">
                        <c:set var="ageBadgeLabel" value="ALL"/>
                        <c:set var="ageBadgeClass" value="all"/>
                        <c:set var="ageBadgeTitle" value="전체 관람가"/>
                    </c:when>
                    <c:when test="${c.ageRating eq '7세 이상 관람가'}">
                        <c:set var="ageBadgeLabel" value="7"/>
                        <c:set var="ageBadgeClass" value="age7"/>
                        <c:set var="ageBadgeTitle" value="7세 이상 관람가"/>
                    </c:when>
                    <c:when test="${c.ageRating eq '12세 이상 관람가'}">
                        <c:set var="ageBadgeLabel" value="12"/>
                        <c:set var="ageBadgeClass" value="age12"/>
                        <c:set var="ageBadgeTitle" value="12세 이상 관람가"/>
                    </c:when>
                    <c:when test="${c.ageRating eq '15세 이상 관람가'}">
                        <c:set var="ageBadgeLabel" value="15"/>
                        <c:set var="ageBadgeClass" value="age15"/>
                        <c:set var="ageBadgeTitle" value="15세 이상 관람가"/>
                    </c:when>
                    <c:when test="${c.ageRating eq '청소년 관람불가'}">
                        <c:set var="ageBadgeLabel" value="19"/>
                        <c:set var="ageBadgeClass" value="adult"/>
                        <c:set var="ageBadgeTitle" value="청소년 관람불가"/>
                    </c:when>
                </c:choose>

                <article class="content-list-card favorite-card">

                    <a class="content-list-card-link"
                       href="${pageContext.request.contextPath}/content/contentDetail/${c.contentNo}">

                        <div class="content-list-card-poster">
                            <c:choose>
                                <c:when test="${not empty c.posterPath}">
                                    <img src="https://image.tmdb.org/t/p/w500${c.posterPath}"
                                         alt="${c.title}"
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div class="no-img">NO IMAGE</div>
                                </c:otherwise>
                            </c:choose>

                            <span class="content-list-type-badge is-${contentBadgeClass}">
                                <c:out value="${contentBadgeLabel}"/>
                            </span>

                            <span class="content-list-poster-age-rating"
                                  title="<c:out value='${ageBadgeTitle}'/>">
                                <span class="age-rating-badge is-${ageBadgeClass}"
                                      aria-label="<c:out value='${ageBadgeTitle}'/>">
                                    <c:out value="${ageBadgeLabel}"/>
                                </span>
                            </span>

                            <c:if test="${not empty c.tmdbScore}">
                                <span class="content-list-score-badge">
                                    <span aria-hidden="true">★</span>
                                    ${c.tmdbScore}
                                </span>
                            </c:if>
                        </div>

                        <div class="content-list-card-info">
                            <h2><c:out value="${c.title}"/></h2>
                            <div class="content-list-card-meta">
                                <span>
                                    <c:choose>
                                        <c:when test="${not empty c.releaseDate}">
                                            ${fn:substring(c.releaseDate, 0, 4)}
                                        </c:when>
                                        <c:otherwise>
                                            공개일 미정
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                                <span><c:out value="${contentBadgeLabel}"/></span>
                            </div>

                            <c:if test="${not empty c.genreText}">
                                <p class="content-list-card-genre">
                                    <c:out value="${c.genreText}"/>
                                </p>
                            </c:if>
                        </div>

                    </a>

                    <div class="content-list-card-bottom">
                        <button type="button"
                                class="content-list-favorite-btn fav-btn active"
                                data-type="content"
                                data-content-no="${c.contentNo}"
                                aria-pressed="true"
                                aria-label="<c:out value='${c.title}'/> 찜 해제"
                                title="찜 해제">
                            ♥
                        </button>
                    </div>

                </article>

            </c:forEach>

        </div>

        <c:if test="${contentCount > 0}">
            <nav class="oditji-pagination"
                 data-pagination
                 data-current-page="${contentPageVO.currentPage}"
                 data-total-page="${contentPageVO.totalPage}"
                 data-page-param="contentPage"
                 data-fixed-param-name="tab"
                 data-fixed-param-value="content"
                 aria-label="찜한 콘텐츠 페이지"></nav>
        </c:if>

    </section>

    <!-- GOODS TAB -->
    <section class="tab-content ${activeTab eq 'goods' ? 'active' : ''}" id="goods-tab">

        <c:if test="${empty goodsFavoriteList}">
            <div class="empty-state">찜한 상품이 없습니다.</div>
        </c:if>

        <div class="card-list">

            <c:forEach var="g" items="${goodsFavoriteList}">

                <article class="card-item favorite-card${g.stock <= 0 ? ' is-soldout' : ''}">

                    <button type="button"
                            class="fav-btn card-favorite-btn active"
                            data-type="goods"
                            data-product-no="${g.productNo}"
                            aria-pressed="true"
                            aria-label="<c:out value='${g.productName}'/> 찜 해제"
                            title="찜 해제">
                        ♥
                    </button>

                    <a class="card-link"
                       href="${pageContext.request.contextPath}/goods/goodsDetail/${g.productNo}">

                        <div class="card-poster">
                            <!-- ⭕ [수정 1] 할인 뱃지를 썸네일 포스터 내부 좌측 상단으로 이동 -->
                            <c:if test="${g.discountRate > 0}">
                                <span class="discount-badge">할인</span>
                            </c:if>

                            <c:choose>
                                <c:when test="${not empty g.mainImage}">
                                    <img src="${pageContext.request.contextPath}${g.mainImage}"
                                         alt="${g.productName}"
                                         loading="lazy">
                                </c:when>
                                <c:otherwise>
                                    <div class="no-img">NO IMAGE</div>
                                </c:otherwise>
                            </c:choose>

                            <c:if test="${g.stock <= 0}">
                                <div class="soldout-badge">SOLD OUT</div>
                            </c:if>
                        </div>

                        <div class="card-info">

                            <p class="card-brand">
                                <c:choose>
                                    <c:when test="${not empty g.businessName}">
                                        <c:out value="${g.businessName}"/>
                                    </c:when>
                                    <c:otherwise>
                                        판매자 정보 없음
                                    </c:otherwise>
                                </c:choose>
                            </p>

                            <!-- ⭕ [수정 2] 제목 영역 100% 활용 (뱃지 제거) -->
                            <div class="card-title-wrap">
                                <h3 class="card-title">
                                    <c:out value="${g.productName}"/>
                                </h3>
                            </div>

                            <!-- ⭕ [수정 3] 모바일 수직 배치를 위한 가격 계층 구조 적용 -->
                            <div class="card-meta">
                                <c:choose>
                                    <c:when test="${g.discountRate > 0}">
                                        <span class="price-original">
                                            ₩ <fmt:formatNumber value="${g.price}" pattern="#,###"/>
                                        </span>
                                        <div class="price-sale-row">
                                            <span class="rate">${g.discountRate}%</span>
                                            <span class="price-final">
                                                ₩ <fmt:formatNumber value="${g.discountPrice}" pattern="#,###"/>
                                            </span>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="price-final">
                                            ₩ <fmt:formatNumber value="${g.price}" pattern="#,###"/>
                                        </span>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="card-sub">
                                <c:choose>
                                    <c:when test="${g.stock <= 0}">
                                        <span class="stock-warning">품절</span>
                                    </c:when>
                                    <c:when test="${g.stock <= 5}">
                                        <span class="stock-warning">
                                            재고 ${g.stock}개 남음
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        재고 ${g.stock}개
                                    </c:otherwise>
                                </c:choose>
                            </div>

                        </div>

                    </a>

                </article>

            </c:forEach>

        </div>

        <c:if test="${goodsCount > 0}">
            <nav class="oditji-pagination"
                 data-pagination
                 data-current-page="${goodsPageVO.currentPage}"
                 data-total-page="${goodsPageVO.totalPage}"
                 data-page-param="goodsPage"
                 data-fixed-param-name="tab"
                 data-fixed-param-value="goods"
                 aria-label="찜한 상품 페이지"></nav>
        </c:if>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>