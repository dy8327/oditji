<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>ODITJI | 검색 결과</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/search.css?v=3">

    <script defer
            src="${pageContext.request.contextPath}/js/search.js?v=13"></script>
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<%--
    검색어와 필터가 모두 없는 경우에는 일반 검색 결과가 아니라
    사용자가 콘텐츠를 발견할 수 있는 인기 콘텐츠/상품 화면으로 표시합니다.
--%>
<c:set var="discoveryMode"
       value="${empty keyword
                and empty contentCategories
                and empty genreCodes
                and empty providerIds
                and empty ageRatings
                and empty productTypes
                and empty minPrice
                and empty maxPrice
                and not discountOnly
                and not inStockOnly}"/>

<c:set var="hasActiveFilter"
       value="${not empty contentCategories
                or not empty genreCodes
                or not empty providerIds
                or not empty ageRatings
                or not empty productTypes
                or not empty minPrice
                or not empty maxPrice
                or discountOnly
                or inStockOnly}"/>

<main class="search-page-container">
    <div class="search-layout">

        <aside class="search-left-sidebar"
               aria-label="검색 필터">

            <!-- 모바일 전용 필터 펼치기 버튼 (데스크톱에서는 숨김) -->
            <button type="button"
                    class="search-mobile-filter-toggle"
                    data-mobile-filter-toggle
                    aria-expanded="false"
                    aria-controls="searchMobileFilterPanel">
                <span>필터</span>
                <span class="mobile-filter-toggle-arrow" aria-hidden="true">⌄</span>
            </button>

            <div class="search-mobile-filter-panel"
                 id="searchMobileFilterPanel"
                 data-mobile-filter-panel>
                <jsp:include page="/WEB-INF/views/common/leftSidebar.jsp"/>
            </div>

        </aside>

        <section class="search-result-area"
                 data-search-root
                 data-initial-tab="${empty searchTab ? 'ALL' : searchTab}">

            <%-- 검색 결과 제목과 설명 --%>
            <header class="search-result-hero">
                <div class="search-result-hero-copy">
                    <span class="search-result-eyebrow">
                        <c:choose>
                            <c:when test="${discoveryMode}">DISCOVER</c:when>
                            <c:otherwise>SEARCH RESULT</c:otherwise>
                        </c:choose>
                    </span>

                    <h1><c:out value="${searchTitle}"/></h1>

                    <p>
                        <c:choose>
                            <c:when test="${discoveryMode}">
                                ODITJI에서 지금 주목받는 콘텐츠와 판매 중인 상품을 확인해 보세요.
                            </c:when>
                            <c:when test="${not empty keyword}">
                                '<c:out value="${keyword}"/>'와 관련된 콘텐츠와 상품을 모아 보여드려요.
                            </c:when>
                            <c:otherwise>
                                선택한 필터 조건에 맞는 콘텐츠와 상품을 보여드려요.
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>

                <div class="search-result-summary"
                     aria-label="현재 표시 결과 수">
                    <span>현재 표시</span>
                    <strong><fmt:formatNumber value="${combinedTotalCount}"/></strong>
                </div>
            </header>

            <%-- 본문에만 탭을 두어 사이드바의 중복 탭을 제거합니다. --%>
            <nav class="search-result-tabs"
                 aria-label="검색 결과 유형"
                 role="tablist">

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="ALL"
                        role="tab"
                        aria-selected="false">
                    <span>전체</span>
                    <b><fmt:formatNumber value="${combinedTotalCount}"/></b>
                </button>

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="CONTENT"
                        role="tab"
                        aria-selected="false">
                    <span>콘텐츠</span>
                    <b><fmt:formatNumber value="${contentTotalCount}"/></b>
                </button>

                <button type="button"
                        class="search-result-tab"
                        data-search-tab="GOODS"
                        role="tab"
                        aria-selected="false">
                    <span>상품</span>
                    <b><fmt:formatNumber value="${goodsTotalCount}"/></b>
                </button>
            </nav>

            <%-- 선택한 필터를 결과 상단에서 바로 확인하고 한 개씩 제거할 수 있습니다. --%>
            <c:if test="${hasActiveFilter}">
                <section class="active-filter-bar"
                         aria-label="선택된 필터">
                    <div class="active-filter-title">
                        <span>선택된 필터</span>
                    </div>

                    <div class="active-filter-chip-list">
                        <c:forEach var="category" items="${contentCategories}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="contentCategories"
                                    data-filter-value="${category}">
                                <c:choose>
                                    <c:when test="${category eq 'MOVIE'}">영화</c:when>
                                    <c:when test="${category eq 'DRAMA'}">드라마</c:when>
                                    <c:when test="${category eq 'ANIMATION'}">애니메이션</c:when>
                                    <c:when test="${category eq 'VARIETY'}">예능</c:when>
                                    <c:otherwise>다큐멘터리</c:otherwise>
                                </c:choose>
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:forEach>

                        <c:forEach var="genre" items="${genreCodes}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="genreCodes"
                                    data-filter-value="${genre}">
                                <c:choose>
                                    <c:when test="${genre eq 'ACTION'}">액션</c:when>
                                    <c:when test="${genre eq 'COMEDY'}">코미디</c:when>
                                    <c:when test="${genre eq 'THRILLER'}">스릴러</c:when>
                                    <c:when test="${genre eq 'ROMANCE'}">로맨스</c:when>
                                    <c:when test="${genre eq 'CRIME'}">범죄</c:when>
                                    <c:when test="${genre eq 'ADVENTURE'}">모험</c:when>
                                    <c:when test="${genre eq 'FAMILY'}">가족</c:when>
                                    <c:when test="${genre eq 'FANTASY'}">판타지</c:when>
                                    <c:when test="${genre eq 'HISTORY'}">역사</c:when>
                                    <c:when test="${genre eq 'HORROR'}">공포</c:when>
                                    <c:when test="${genre eq 'MUSIC'}">음악</c:when>
                                    <c:when test="${genre eq 'MYSTERY'}">미스터리</c:when>
                                    <c:when test="${genre eq 'SCI_FI'}">SF</c:when>
                                    <c:when test="${genre eq 'WAR'}">전쟁</c:when>
                                    <c:otherwise>서부</c:otherwise>
                                </c:choose>
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:forEach>

                        <c:forEach var="ageRating" items="${ageRatings}">
                            <c:set var="filterAgeBadgeLabel" value="?"/>
                            <c:set var="filterAgeBadgeClass" value="unknown"/>

                            <c:choose>
                                <c:when test="${ageRating eq '전체 관람가'}">
                                    <c:set var="filterAgeBadgeLabel" value="ALL"/>
                                    <c:set var="filterAgeBadgeClass" value="all"/>
                                </c:when>
                                <c:when test="${ageRating eq '7세 이상 관람가'}">
                                    <c:set var="filterAgeBadgeLabel" value="7"/>
                                    <c:set var="filterAgeBadgeClass" value="age7"/>
                                </c:when>
                                <c:when test="${ageRating eq '12세 이상 관람가'}">
                                    <c:set var="filterAgeBadgeLabel" value="12"/>
                                    <c:set var="filterAgeBadgeClass" value="age12"/>
                                </c:when>
                                <c:when test="${ageRating eq '15세 이상 관람가'}">
                                    <c:set var="filterAgeBadgeLabel" value="15"/>
                                    <c:set var="filterAgeBadgeClass" value="age15"/>
                                </c:when>
                                <c:when test="${ageRating eq '청소년 관람불가'}">
                                    <c:set var="filterAgeBadgeLabel" value="19"/>
                                    <c:set var="filterAgeBadgeClass" value="adult"/>
                                </c:when>
                            </c:choose>

                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="ageRatings"
                                    data-filter-value="${ageRating}">
                                <span class="age-rating-badge is-${filterAgeBadgeClass}"
                                      aria-hidden="true">
                                    <c:out value="${filterAgeBadgeLabel}"/>
                                </span>
                                <c:out value="${ageRating}"/>
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:forEach>

                        <c:forEach var="provider" items="${providerIds}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="providerIds"
                                    data-filter-value="${provider}">
                                <c:choose>
                                    <c:when test="${provider eq '8'}">Netflix</c:when>
                                    <c:when test="${provider eq '1883'}">Tving</c:when>
                                    <c:when test="${provider eq '356'}">Wavve</c:when>
                                    <c:when test="${provider eq '337'}">Disney+</c:when>
                                    <c:when test="${provider eq '97'}">Watcha</c:when>
                                    <c:otherwise>Coupangplay</c:otherwise>
                                </c:choose>
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:forEach>

                        <c:forEach var="productType" items="${productTypes}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="productTypes"
                                    data-filter-value="<c:out value='${productType}'/>">
                                <c:out value="${productType}"/>
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:forEach>

                        <c:if test="${not empty minPrice}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="minPrice">
                                최소 <fmt:formatNumber value="${minPrice}"/>원
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:if>

                        <c:if test="${not empty maxPrice}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="maxPrice">
                                최대 <fmt:formatNumber value="${maxPrice}"/>원
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:if>

                        <c:if test="${discountOnly}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="discountOnly">
                                할인 상품
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:if>

                        <c:if test="${inStockOnly}">
                            <button type="button"
                                    class="active-filter-chip"
                                    data-filter-chip
                                    data-filter-name="inStockOnly">
                                품절 제외
                                <span aria-hidden="true">×</span>
                            </button>
                        </c:if>
                    </div>

                    <c:url var="activeFilterResetUrl" value="/search">
                        <c:param name="keyword" value="${keyword}"/>
                        <c:param name="searchTab" value="${empty searchTab ? 'ALL' : searchTab}"/>
                        <c:param name="contentPage" value="1"/>
                        <c:param name="goodsPage" value="1"/>
                    </c:url>

                    <a class="active-filter-reset"
                       href="${activeFilterResetUrl}">
                        전체 초기화
                    </a>
                </section>
            </c:if>

            <%-- =====================================================
                 전체 탭
            ====================================================== --%>
            <section class="search-tab-panel"
                     data-search-panel="ALL"
                     role="tabpanel">

                <section class="search-all-section">
                    <div class="search-section-heading">
                        <div>
                            <span class="search-section-kicker">CONTENT</span>
                            <h2>
                                <c:choose>
                                    <c:when test="${discoveryMode}">지금 인기 있는 콘텐츠</c:when>
                                    <c:otherwise>콘텐츠</c:otherwise>
                                </c:choose>
                            </h2>
                        </div>

                        <button type="button"
                                class="search-more-button"
                                data-search-move-tab="CONTENT">
                            콘텐츠 전체보기
                            <span aria-hidden="true">→</span>
                        </button>
                    </div>

                    <c:choose>
                        <c:when test="${empty allContentResults}">
                            <section class="empty-state">
                                <strong>검색된 콘텐츠가 없습니다.</strong>
                                <p>다른 검색어나 필터 조건으로 다시 확인해 주세요.</p>
                            </section>
                        </c:when>

                        <%--
                            전체 탭은 검색어와 필터 선택 여부와 관계없이
                            포스터 중심의 발견형 카드 UI를 유지합니다.

                            실제 카드 데이터는 allContentResults를 사용하므로
                            검색어와 콘텐츠 필터가 적용된 결과만 표시됩니다.
                        --%>
                        <c:otherwise>
                            <div class="search-discovery-content-grid">
                                <c:forEach var="content" items="${allContentResults}">
                                    <c:url var="allContentDetailUrl" value="/content/prepare">
                                        <c:param name="tmdbId" value="${content.tmdbId}"/>
                                        <c:param name="contentType" value="${content.contentType}"/>
                                    </c:url>

                                    <%--
                                        콘텐츠의 정규화된 연령등급을 카드용 숫자 배지로 변환합니다.
                                        빈 값이나 알 수 없는 값은 등급 정보 없음(?)으로 처리합니다.
                                    --%>
                                    <c:set var="ageBadgeLabel" value="?"/>
                                    <c:set var="ageBadgeClass" value="unknown"/>
                                    <c:set var="ageBadgeTitle" value="등급 정보 없음"/>

                                    <c:choose>
                                        <c:when test="${content.ageRating eq '전체 관람가'}">
                                            <c:set var="ageBadgeLabel" value="ALL"/>
                                            <c:set var="ageBadgeClass" value="all"/>
                                            <c:set var="ageBadgeTitle" value="전체 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '7세 이상 관람가'}">
                                            <c:set var="ageBadgeLabel" value="7"/>
                                            <c:set var="ageBadgeClass" value="age7"/>
                                            <c:set var="ageBadgeTitle" value="7세 이상 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '12세 이상 관람가'}">
                                            <c:set var="ageBadgeLabel" value="12"/>
                                            <c:set var="ageBadgeClass" value="age12"/>
                                            <c:set var="ageBadgeTitle" value="12세 이상 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '15세 이상 관람가'}">
                                            <c:set var="ageBadgeLabel" value="15"/>
                                            <c:set var="ageBadgeClass" value="age15"/>
                                            <c:set var="ageBadgeTitle" value="15세 이상 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '청소년 관람불가'}">
                                            <c:set var="ageBadgeLabel" value="19"/>
                                            <c:set var="ageBadgeClass" value="adult"/>
                                            <c:set var="ageBadgeTitle" value="청소년 관람불가"/>
                                        </c:when>
                                    </c:choose>

                                    <article class="search-discovery-card">
                                        <a href="${allContentDetailUrl}">
                                            <div class="search-discovery-poster">
                                                <c:choose>
                                                    <c:when test="${not empty content.posterPath}">
                                                        <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                                             alt="<c:out value='${content.title}'/>"
                                                             loading="lazy">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="search-content-no-image">NO IMAGE</div>
                                                    </c:otherwise>
                                                </c:choose>

                                                <span class="search-discovery-age-rating"
                                                      title="<c:out value='${ageBadgeTitle}'/>">
                                                    <span class="age-rating-badge is-${ageBadgeClass}"
                                                          aria-label="<c:out value='${ageBadgeTitle}'/>">
                                                        <c:out value="${ageBadgeLabel}"/>
                                                    </span>
                                                </span>

                                                <c:if test="${not empty content.tmdbScore and content.tmdbScore > 0}">
                                                    <span class="search-discovery-score">
                                                        ★ <fmt:formatNumber value="${content.tmdbScore}" pattern="0.0"/>
                                                    </span>
                                                </c:if>
                                            </div>

                                            <div class="search-discovery-info">
                                                <div class="search-discovery-badge-row">
                                                    <span class="search-content-type-badge">
                                                        <c:choose>
                                                            <c:when test="${content.contentType eq 'MOVIE'}">영화</c:when>
                                                            <c:when test="${fn:contains(content.genreText, '애니메이션')}">애니메이션</c:when>
                                                            <c:when test="${fn:contains(content.genreText, '다큐멘터리')}">다큐멘터리</c:when>
                                                            <c:when test="${fn:contains(content.genreText, '리얼리티') or fn:contains(content.genreText, '토크')}">예능</c:when>
                                                            <c:when test="${fn:contains(content.genreText, '드라마')}">드라마</c:when>
                                                            <c:otherwise>TV</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                </div>

                                                <h3><c:out value="${content.title}"/></h3>

                                                <p>
                                                    <c:choose>
                                                        <c:when test="${not empty content.releaseDate}">
                                                            <c:out value="${content.releaseDate}"/>
                                                        </c:when>
                                                        <c:otherwise>공개일 정보 없음</c:otherwise>
                                                    </c:choose>
                                                </p>
                                            </div>
                                        </a>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>

                <section class="search-all-section search-goods-section">
                    <div class="search-section-heading">
                        <div>
                            <span class="search-section-kicker">GOODS</span>
                            <h2>
                                <c:choose>
                                    <c:when test="${discoveryMode}">지금 판매 중인 상품</c:when>
                                    <c:otherwise>상품</c:otherwise>
                                </c:choose>
                            </h2>
                        </div>

                        <button type="button"
                                class="search-more-button"
                                data-search-move-tab="GOODS">
                            상품 전체보기
                            <span aria-hidden="true">→</span>
                        </button>
                    </div>

                    <c:choose>
                        <c:when test="${empty allGoodsResults}">
                            <section class="empty-state compact-empty-state">
                                <strong>현재 표시할 상품이 없습니다.</strong>
                                <p>새로운 상품이 등록되면 이곳에서 확인할 수 있어요.</p>
                            </section>
                        </c:when>
                        <c:otherwise>
                            <div class="search-goods-grid search-all-goods-grid">
                                <c:forEach var="goods" items="${allGoodsResults}">
                                    <article class="search-goods-card">
                                        <a href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">
                                            <div class="search-goods-image">
                                                <c:choose>
                                                    <c:when test="${not empty goods.mainImage}">
                                                        <img src="${pageContext.request.contextPath}${goods.mainImage}"
                                                             alt="<c:out value='${goods.productName}'/>"
                                                             loading="lazy">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="search-goods-no-image">NO IMAGE</div>
                                                    </c:otherwise>
                                                </c:choose>

                                                <c:if test="${goods.stock <= 0}">
                                                    <span class="search-goods-soldout">품절</span>
                                                </c:if>
                                            </div>

                                            <div class="search-goods-info">
                                                <p class="search-goods-business">
                                                    <c:out value="${goods.businessName}"/>
                                                </p>
                                                <h3><c:out value="${goods.productName}"/></h3>

                                                <c:choose>
                                                    <c:when test="${goods.discountRate > 0}">
                                                        <p class="search-goods-price">
                                                            <span class="search-goods-rate">${goods.discountRate}%</span>
                                                            <strong><fmt:formatNumber value="${goods.discountPrice}"/>원</strong>
                                                        </p>
                                                        <p class="search-goods-original">
                                                            <fmt:formatNumber value="${goods.price}"/>원
                                                        </p>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <p class="search-goods-price">
                                                            <strong><fmt:formatNumber value="${goods.price}"/>원</strong>
                                                        </p>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                        </a>
                                    </article>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </section>
            </section>

            <%-- =====================================================
                 콘텐츠 탭
            ====================================================== --%>
            <section class="search-tab-panel"
                     data-search-panel="CONTENT"
                     role="tabpanel"
                     hidden>

                <div class="search-section-heading search-list-heading">
                    <div>
                        <span class="search-section-kicker">CONTENT RESULT</span>
                        <h2>콘텐츠 검색 결과</h2>
                    </div>
                    <span class="search-section-count">
                        현재 <fmt:formatNumber value="${contentTotalCount}"/>개 표시
                    </span>
                </div>

                <c:choose>
                    <c:when test="${empty contentResults}">
                        <section class="empty-state">
                            <strong>검색된 콘텐츠가 없습니다.</strong>
                            <p>검색어를 바꾸거나 선택한 콘텐츠 필터를 줄여보세요.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <div class="search-content-list">
                            <c:forEach var="content" items="${contentResults}">
                                <c:url var="contentDetailUrl" value="/content/prepare">
                                    <c:param name="tmdbId" value="${content.tmdbId}"/>
                                    <c:param name="contentType" value="${content.contentType}"/>
                                </c:url>

                                <%--
                                    콘텐츠의 정규화된 연령등급을 카드용 숫자 배지로 변환합니다.
                                    빈 값이나 알 수 없는 값은 등급 정보 없음(?)으로 처리합니다.
                                --%>
                                <c:set var="ageBadgeLabel" value="?"/>
                                <c:set var="ageBadgeClass" value="unknown"/>
                                <c:set var="ageBadgeTitle" value="등급 정보 없음"/>

                                <c:choose>
                                    <c:when test="${content.ageRating eq '전체 관람가'}">
                                        <c:set var="ageBadgeLabel" value="ALL"/>
                                        <c:set var="ageBadgeClass" value="all"/>
                                        <c:set var="ageBadgeTitle" value="전체 관람가"/>
                                    </c:when>
                                    <c:when test="${content.ageRating eq '7세 이상 관람가'}">
                                        <c:set var="ageBadgeLabel" value="7"/>
                                        <c:set var="ageBadgeClass" value="age7"/>
                                        <c:set var="ageBadgeTitle" value="7세 이상 관람가"/>
                                    </c:when>
                                    <c:when test="${content.ageRating eq '12세 이상 관람가'}">
                                        <c:set var="ageBadgeLabel" value="12"/>
                                        <c:set var="ageBadgeClass" value="age12"/>
                                        <c:set var="ageBadgeTitle" value="12세 이상 관람가"/>
                                    </c:when>
                                    <c:when test="${content.ageRating eq '15세 이상 관람가'}">
                                        <c:set var="ageBadgeLabel" value="15"/>
                                        <c:set var="ageBadgeClass" value="age15"/>
                                        <c:set var="ageBadgeTitle" value="15세 이상 관람가"/>
                                    </c:when>
                                    <c:when test="${content.ageRating eq '청소년 관람불가'}">
                                        <c:set var="ageBadgeLabel" value="19"/>
                                        <c:set var="ageBadgeClass" value="adult"/>
                                        <c:set var="ageBadgeTitle" value="청소년 관람불가"/>
                                    </c:when>
                                </c:choose>

                                <article class="search-content-item">
                                    <a class="search-content-link" href="${contentDetailUrl}">
                                        <div class="search-content-poster">
                                            <c:choose>
                                                <c:when test="${not empty content.posterPath}">
                                                    <img src="https://image.tmdb.org/t/p/w300${content.posterPath}"
                                                         alt="<c:out value='${content.title}'/>"
                                                         loading="lazy">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="search-content-no-image">NO IMAGE</div>
                                                </c:otherwise>
                                            </c:choose>

                                            <span class="search-content-age-rating"
                                                  title="<c:out value='${ageBadgeTitle}'/>">
                                                <span class="age-rating-badge is-${ageBadgeClass}"
                                                      aria-label="<c:out value='${ageBadgeTitle}'/>">
                                                    <c:out value="${ageBadgeLabel}"/>
                                                </span>
                                            </span>
                                        </div>

                                        <div class="search-content-info">
                                            <div class="search-content-title-row">
                                                <div>
                                                    <div class="search-content-badge-row">
                                                        <span class="search-content-type-badge">
                                                            <c:choose>
                                                                <c:when test="${content.contentType eq 'MOVIE'}">영화</c:when>
                                                                <c:when test="${fn:contains(content.genreText, '애니메이션')}">애니메이션</c:when>
                                                                <c:when test="${fn:contains(content.genreText, '다큐멘터리')}">다큐멘터리</c:when>
                                                                <c:when test="${fn:contains(content.genreText, '리얼리티') or fn:contains(content.genreText, '토크')}">예능</c:when>
                                                                <c:when test="${fn:contains(content.genreText, '드라마')}">드라마</c:when>
                                                                <c:otherwise>TV</c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                    </div>
                                                    <h3 class="search-content-title"><c:out value="${content.title}"/></h3>
                                                </div>

                                                <c:if test="${not empty content.tmdbScore and content.tmdbScore > 0}">
                                                    <span class="search-content-score">
                                                        ★ <fmt:formatNumber value="${content.tmdbScore}" pattern="0.0"/>
                                                    </span>
                                                </c:if>
                                            </div>

                                            <c:if test="${content.matchType eq 'PERSON'}">
                                                <p class="search-person-match">
                                                    <c:out value="${content.matchedPersonRole}"/> 검색 결과 ·
                                                    <c:out value="${content.matchedPersonName}"/>
                                                    <c:choose>
                                                        <c:when test="${content.matchedPersonRole eq '감독'}"> 연출</c:when>
                                                        <c:otherwise> 출연</c:otherwise>
                                                    </c:choose>
                                                </p>
                                            </c:if>

                                            <div class="search-content-meta">
                                                <c:if test="${not empty content.releaseDate}">
                                                    <span><c:out value="${content.releaseDate}"/></span>
                                                </c:if>
                                                <c:if test="${not empty content.genreText}">
                                                    <span><c:out value="${content.genreText}"/></span>
                                                </c:if>
                                            </div>

                                            <c:if test="${not empty content.overview}">
                                                <p class="search-content-overview">
                                                    <c:out value="${content.overview}"/>
                                                </p>
                                            </c:if>

                                            <div class="search-content-bottom-row">
                                                <div class="search-content-platform-list">
                                                    <c:forEach var="platform"
                                                               items="${content.platformList}"
                                                               varStatus="platformStatus">
                                                        <c:if test="${platformStatus.index < 4}">
                                                            <c:choose>
                                                                <c:when test="${not empty platform.logoImage}">
                                                                    <img class="search-content-platform-logo"
                                                                         src="${platform.logoImage}"
                                                                         alt="<c:out value='${platform.platformName}'/>"
                                                                         title="<c:out value='${platform.platformName}'/>"
                                                                         loading="lazy">
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="search-platform-name">
                                                                        <c:out value="${platform.platformName}"/>
                                                                    </span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </c:if>
                                                    </c:forEach>

                                                    <c:if test="${content.platformList.size() > 4}">
                                                        <span class="search-platform-more">
                                                            +${content.platformList.size() - 4}
                                                        </span>
                                                    </c:if>
                                                </div>

                                                <span class="search-detail-label">상세보기 →</span>
                                            </div>
                                        </div>
                                    </a>
                                </article>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>

                <%-- 현재 페이지 기준 앞뒤 2페이지를 표시하는 콘텐츠 페이징 --%>
                <c:if test="${contentTotalPages > 1}">
                    <c:set var="contentStartPage" value="${contentCurrentPage - 2}"/>
                    <c:set var="contentEndPage" value="${contentCurrentPage + 2}"/>
                    <c:if test="${contentStartPage < 1}">
                        <c:set var="contentStartPage" value="1"/>
                    </c:if>
                    <c:if test="${contentEndPage > contentTotalPages}">
                        <c:set var="contentEndPage" value="${contentTotalPages}"/>
                    </c:if>

                    <nav class="search-pagination" aria-label="콘텐츠 페이지 이동">
                        <c:if test="${contentCurrentPage > 1}">
                            <c:url var="contentFirstUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="1"/>
                                <c:param name="goodsPage" value="${goodsCurrentPage}"/>
                                <c:param name="searchTab" value="CONTENT"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${contentFirstUrl}" aria-label="첫 페이지">«</a>

                            <c:url var="contentPrevUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="${contentCurrentPage - 1}"/>
                                <c:param name="goodsPage" value="${goodsCurrentPage}"/>
                                <c:param name="searchTab" value="CONTENT"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${contentPrevUrl}" aria-label="이전 페이지">‹</a>
                        </c:if>

                        <c:forEach var="pageNumber"
                                   begin="${contentStartPage}"
                                   end="${contentEndPage}">
                            <c:choose>
                                <c:when test="${pageNumber == contentCurrentPage}">
                                    <span class="page-now" aria-current="page">${pageNumber}</span>
                                </c:when>
                                <c:otherwise>
                                    <c:url var="contentPageUrl" value="/search">
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="contentPage" value="${pageNumber}"/>
                                        <c:param name="goodsPage" value="${goodsCurrentPage}"/>
                                        <c:param name="searchTab" value="CONTENT"/>
                                    </c:url>
                                    <a class="page-btn" href="${contentPageUrl}">${pageNumber}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${contentCurrentPage < contentTotalPages}">
                            <c:url var="contentNextUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="${contentCurrentPage + 1}"/>
                                <c:param name="goodsPage" value="${goodsCurrentPage}"/>
                                <c:param name="searchTab" value="CONTENT"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${contentNextUrl}" aria-label="다음 페이지">›</a>

                            <c:url var="contentLastUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="${contentTotalPages}"/>
                                <c:param name="goodsPage" value="${goodsCurrentPage}"/>
                                <c:param name="searchTab" value="CONTENT"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${contentLastUrl}" aria-label="마지막 페이지">»</a>
                        </c:if>
                    </nav>
                </c:if>
            </section>

            <%-- =====================================================
                 상품 탭
            ====================================================== --%>
            <section class="search-tab-panel"
                     data-search-panel="GOODS"
                     role="tabpanel"
                     hidden>

                <div class="search-section-heading search-list-heading">
                    <div>
                        <span class="search-section-kicker">GOODS RESULT</span>
                        <h2>상품 검색 결과</h2>
                    </div>
                    <span class="search-section-count">
                        현재 <fmt:formatNumber value="${goodsTotalCount}"/>개 표시
                    </span>
                </div>

                <c:choose>
                    <c:when test="${empty goodsResults}">
                        <section class="empty-state">
                            <strong>검색된 상품이 없습니다.</strong>
                            <p>검색어를 바꾸거나 선택한 상품 필터를 줄여보세요.</p>
                        </section>
                    </c:when>
                    <c:otherwise>
                        <div class="search-goods-grid">
                            <c:forEach var="goods" items="${goodsResults}">
                                <article class="search-goods-card">
                                    <a href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">
                                        <div class="search-goods-image">
                                            <c:choose>
                                                <c:when test="${not empty goods.mainImage}">
                                                    <img src="${pageContext.request.contextPath}${goods.mainImage}"
                                                         alt="<c:out value='${goods.productName}'/>"
                                                         loading="lazy">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="search-goods-no-image">NO IMAGE</div>
                                                </c:otherwise>
                                            </c:choose>

                                            <c:if test="${goods.stock <= 0}">
                                                <span class="search-goods-soldout">품절</span>
                                            </c:if>
                                        </div>

                                        <div class="search-goods-info">
                                            <p class="search-goods-business"><c:out value="${goods.businessName}"/></p>
                                            <h3><c:out value="${goods.productName}"/></h3>

                                            <c:choose>
                                                <c:when test="${goods.discountRate > 0}">
                                                    <p class="search-goods-price">
                                                        <span class="search-goods-rate">${goods.discountRate}%</span>
                                                        <strong><fmt:formatNumber value="${goods.discountPrice}"/>원</strong>
                                                    </p>
                                                    <p class="search-goods-original">
                                                        <fmt:formatNumber value="${goods.price}"/>원
                                                    </p>
                                                </c:when>
                                                <c:otherwise>
                                                    <p class="search-goods-price">
                                                        <strong><fmt:formatNumber value="${goods.price}"/>원</strong>
                                                    </p>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </a>
                                </article>
                            </c:forEach>
                        </div>
                    </c:otherwise>
                </c:choose>

                <%-- 상품 페이지 이동 --%>
                <c:if test="${goodsTotalPages > 1}">
                    <c:set var="goodsStartPage" value="${goodsCurrentPage - 2}"/>
                    <c:set var="goodsEndPage" value="${goodsCurrentPage + 2}"/>
                    <c:if test="${goodsStartPage < 1}">
                        <c:set var="goodsStartPage" value="1"/>
                    </c:if>
                    <c:if test="${goodsEndPage > goodsTotalPages}">
                        <c:set var="goodsEndPage" value="${goodsTotalPages}"/>
                    </c:if>

                    <nav class="search-pagination" aria-label="상품 페이지 이동">
                        <c:if test="${goodsCurrentPage > 1}">
                            <c:url var="goodsFirstUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="${contentCurrentPage}"/>
                                <c:param name="goodsPage" value="1"/>
                                <c:param name="searchTab" value="GOODS"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${goodsFirstUrl}" aria-label="첫 페이지">«</a>

                            <c:url var="goodsPrevUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="${contentCurrentPage}"/>
                                <c:param name="goodsPage" value="${goodsCurrentPage - 1}"/>
                                <c:param name="searchTab" value="GOODS"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${goodsPrevUrl}" aria-label="이전 페이지">‹</a>
                        </c:if>

                        <c:forEach var="pageNumber"
                                   begin="${goodsStartPage}"
                                   end="${goodsEndPage}">
                            <c:choose>
                                <c:when test="${pageNumber == goodsCurrentPage}">
                                    <span class="page-now" aria-current="page">${pageNumber}</span>
                                </c:when>
                                <c:otherwise>
                                    <c:url var="goodsPageUrl" value="/search">
                                        <c:param name="keyword" value="${keyword}"/>
                                        <c:param name="contentPage" value="${contentCurrentPage}"/>
                                        <c:param name="goodsPage" value="${pageNumber}"/>
                                        <c:param name="searchTab" value="GOODS"/>
                                    </c:url>
                                    <a class="page-btn" href="${goodsPageUrl}">${pageNumber}</a>
                                </c:otherwise>
                            </c:choose>
                        </c:forEach>

                        <c:if test="${goodsCurrentPage < goodsTotalPages}">
                            <c:url var="goodsNextUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="${contentCurrentPage}"/>
                                <c:param name="goodsPage" value="${goodsCurrentPage + 1}"/>
                                <c:param name="searchTab" value="GOODS"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${goodsNextUrl}" aria-label="다음 페이지">›</a>

                            <c:url var="goodsLastUrl" value="/search">
                                <c:param name="keyword" value="${keyword}"/>
                                <c:param name="contentPage" value="${contentCurrentPage}"/>
                                <c:param name="goodsPage" value="${goodsTotalPages}"/>
                                <c:param name="searchTab" value="GOODS"/>
                            </c:url>
                            <a class="page-btn page-arrow" href="${goodsLastUrl}" aria-label="마지막 페이지">»</a>
                        </c:if>
                    </nav>
                </c:if>
            </section>
        </section>
    </div>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>