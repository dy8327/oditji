<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<%@ taglib prefix="fmt"
           uri="jakarta.tags.fmt" %>

<%@ taglib prefix="oditjiGoods"
           tagdir="/WEB-INF/tags/goods" %>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">

<title>ODITJI | 상품</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/goods.css">
  <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/component.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/favorite.css">

<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/goods.js">
</script>

<script defer
        src="${pageContext.request.contextPath}/js/favorite.js">
</script>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pagination-common.css?v=1">
    <script defer src="${pageContext.request.contextPath}/js/pagination.js?v=1"></script>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="goods-list-container goods-page-container">


<div class="goods-layout">

    <!-- =================================================
         LEFT FILTER SIDEBAR
    ================================================== -->
    <aside class="goods-left-sidebar" id="goods-category-filter" aria-label="상품 카테고리 필터">

        <!-- 모바일 전용 필터 펼치기 버튼 (데스크톱에서는 숨김) -->
        <button type="button"
                class="goods-mobile-filter-toggle"
                data-mobile-filter-toggle
                aria-expanded="false"
                aria-controls="goodsMobileFilterPanel">
            <span>필터</span>
            <span class="mobile-filter-toggle-arrow" aria-hidden="true">⌄</span>
        </button>

        <div class="goods-mobile-filter-panel"
             id="goodsMobileFilterPanel"
             data-mobile-filter-panel>

            <jsp:include
                page="/WEB-INF/views/common/goodsLeftSidebar.jsp"/>

        </div>

    </aside>

    <!-- =================================================
         RESULT AREA
    ================================================== -->
    <div class="goods-result-area">

        <!-- =================================================
             PAGE HEADER (contentList.jsp와 동일한 구조로 통일)
        ================================================== -->
        <header class="goods-list-header">

            <div class="goods-list-heading">

                <span class="goods-list-eyebrow">GOODS LIBRARY</span>

                <div class="goods-list-title-row">

                    <h1 class="list-title">

                        <c:choose>

                            <c:when test="${type eq 'popular'}">
                                인기 상품
                            </c:when>

                            <c:when test="${type eq 'category'}">
                                <%-- [수정] 선택한 세부 카테고리명을 목록 제목으로 표시합니다. --%>
                                <c:out value="${selectedCategoryName}"/>
                            </c:when>

                            <c:otherwise>
                                전체 상품
                            </c:otherwise>

                        </c:choose>

                    </h1>

                    <span class="goods-list-result-count">

                        총
                        <strong>
                            <fmt:formatNumber
                                value="${totalCount}"
                                pattern="#,###"/>
                        </strong>개

                    </span>

                </div>

                <p>
                    현재 구매 가능한 상품을 둘러보세요.
                </p>

            </div>

        </header>

        <!-- =================================================
             SELECTED FILTERS + SORT (contentList.jsp의 toolbar와 동일한 구조)
        ================================================== -->
        <section class="goods-list-toolbar"
                 aria-label="굿즈 목록 도구">

            <div class="goods-selected-filter-area">

                <c:choose>

                    <c:when test="${not empty productTypes
                                  or not empty priceRanges
                                  or not empty stockStatus}">

                        <span class="goods-selected-filter-label">
                            선택된 조건
                        </span>

                        <div class="goods-selected-filter-list">

                            <c:forEach var="selectedType"
                                       items="${productTypes}">

                                <button type="button"
                                        class="goods-selected-filter-chip"
                                        data-goods-filter-chip
                                        data-filter-name="productTypes"
                                        data-filter-value="${selectedType}">

                                    <span>
                                        <c:choose>
                                            <c:when test="${selectedType eq 'CLOTHES'}">의상</c:when>
                                            <c:when test="${selectedType eq 'SHOES'}">신발</c:when>
                                            <c:when test="${selectedType eq 'PROP'}">소품</c:when>
                                            <c:when test="${selectedType eq 'GOODS'}">굿즈</c:when>
                                            <c:when test="${selectedType eq 'OST'}">OST</c:when>
                                            <c:when test="${selectedType eq 'BOOK'}">도서</c:when>
                                            <c:when test="${selectedType eq 'FIGURE'}">피규어</c:when>
                                            <c:when test="${selectedType eq 'POSTER'}">포스터</c:when>
                                            <c:when test="${selectedType eq 'ETC'}">기타</c:when>
                                            <c:otherwise><c:out value="${selectedType}"/></c:otherwise>
                                        </c:choose>
                                    </span>

                                    <span aria-hidden="true">×</span>
                                </button>

                            </c:forEach>

                            <c:forEach var="selectedRange"
                                       items="${priceRanges}">

                                <button type="button"
                                        class="goods-selected-filter-chip"
                                        data-goods-filter-chip
                                        data-filter-name="priceRanges"
                                        data-filter-value="${selectedRange}">

                                    <span>
                                        <c:choose>
                                            <c:when test="${selectedRange eq 'UNDER_10000'}">1만원 미만</c:when>
                                            <c:when test="${selectedRange eq 'RANGE_10000_30000'}">1만 ~ 3만원</c:when>
                                            <c:when test="${selectedRange eq 'RANGE_30000_50000'}">3만 ~ 5만원</c:when>
                                            <c:when test="${selectedRange eq 'RANGE_50000_100000'}">5만 ~ 10만원</c:when>
                                            <c:when test="${selectedRange eq 'OVER_100000'}">10만원 이상</c:when>
                                            <c:otherwise><c:out value="${selectedRange}"/></c:otherwise>
                                        </c:choose>
                                    </span>

                                    <span aria-hidden="true">×</span>
                                </button>

                            </c:forEach>

                            <c:forEach var="selectedStock"
                                       items="${stockStatus}">

                                <button type="button"
                                        class="goods-selected-filter-chip"
                                        data-goods-filter-chip
                                        data-filter-name="stockStatus"
                                        data-filter-value="${selectedStock}">

                                    <span>
                                        <c:choose>
                                            <c:when test="${selectedStock eq 'IN_STOCK'}">판매중</c:when>
                                            <c:when test="${selectedStock eq 'SOLD_OUT'}">품절 포함</c:when>
                                            <c:otherwise><c:out value="${selectedStock}"/></c:otherwise>
                                        </c:choose>
                                    </span>

                                    <span aria-hidden="true">×</span>
                                </button>

                            </c:forEach>

                        </div>

                        <c:url var="clearGoodsFilterUrl"
                               value="/goods/list">
                            <c:param name="type" value="${type}"/>
                            <c:param name="keyword" value="${keyword}"/>
                            <c:param name="sort" value="${sort}"/>
                        </c:url>

                        <a class="goods-selected-filter-clear"
                           href="${clearGoodsFilterUrl}">
                            전체 해제
                        </a>

                    </c:when>

                    <c:otherwise>
                        <span class="goods-selected-filter-empty">
                            현재 전체 상품을 표시하고 있습니다.
                        </span>
                    </c:otherwise>

                </c:choose>

            </div>

            <form class="goods-list-sort-form"
                  action="${pageContext.request.contextPath}/goods/list"
                  method="get">

                <input type="hidden" name="type" value="${type}">
                <input type="hidden" name="keyword" value="${keyword}">
                <input type="hidden" name="page" value="1">

                <c:forEach var="selectedType"
                           items="${productTypes}">
                    <input type="hidden"
                           name="productTypes"
                           value="${selectedType}">
                </c:forEach>

                <c:forEach var="selectedRange"
                           items="${priceRanges}">
                    <input type="hidden"
                           name="priceRanges"
                           value="${selectedRange}">
                </c:forEach>

                <c:forEach var="selectedStock"
                           items="${stockStatus}">
                    <input type="hidden"
                           name="stockStatus"
                           value="${selectedStock}">
                </c:forEach>

                <label for="goodsSortSelect">
                    정렬
                </label>

                <select id="goodsSortSelect"
                        name="sort"
                        data-goods-sort-select>
                    <option value="popular"
                            <c:if test="${sort eq 'popular'}">selected</c:if>>
                        인기순
                    </option>
                    <option value="latest"
                            <c:if test="${sort eq 'latest'}">selected</c:if>>
                        최신순
                    </option>
                    <option value="price_asc"
                            <c:if test="${sort eq 'price_asc'}">selected</c:if>>
                        낮은 가격순
                    </option>
                    <option value="price_desc"
                            <c:if test="${sort eq 'price_desc'}">selected</c:if>>
                        높은 가격순
                    </option>
                    <option value="title"
                            <c:if test="${sort eq 'title'}">selected</c:if>>
                        가나다순
                    </option>
                </select>

            </form>

        </section>

        <!-- =================================================
             EMPTY
        ================================================== -->
        <c:if test="${empty goodsList}">

            <section class="empty-state">

                <p>등록된 상품이 없습니다.</p>

            </section>

        </c:if>

        <!-- =================================================
            GOODS CARD LIST
        ================================================== -->
        <c:if test="${not empty goodsList}">

            <section class="card-list">

                <c:forEach var="g" items="${goodsList}">

                    <oditjiGoods:goodsCard goods="${g}"
                                           imageFallback="true" />

                </c:forEach>

            </section>

        </c:if>

        <!-- =================================================
             PAGINATION URL
        ================================================== -->
        <c:url var="previousPageUrl"
               value="/goods/list">

            <c:param name="type"
                     value="${type}"/>

            <c:param name="keyword"
                     value="${keyword}"/>

            <c:param name="sort"
                     value="${sort}"/>

            <c:param name="page"
                     value="${page - 1}"/>

            <c:if test="${not empty minPrice}">
                <c:param name="minPrice"
                         value="${minPrice}"/>
            </c:if>

            <c:if test="${not empty maxPrice}">
                <c:param name="maxPrice"
                         value="${maxPrice}"/>
            </c:if>

            <c:if test="${discountOnly}">
                <c:param name="discountOnly"
                         value="true"/>
            </c:if>

            <c:if test="${inStockOnly}">
                <c:param name="inStockOnly"
                         value="true"/>
            </c:if>

            <c:forEach var="selectedType"
                       items="${productTypes}">

                <c:param name="productTypes"
                         value="${selectedType}"/>

            </c:forEach>

            <c:forEach var="selectedRange"
                       items="${priceRanges}">

                <c:param name="priceRanges"
                         value="${selectedRange}"/>

            </c:forEach>

            <c:forEach var="selectedStock"
                       items="${stockStatus}">

                <c:param name="stockStatus"
                         value="${selectedStock}"/>

            </c:forEach>

        </c:url>

        <c:url var="nextPageUrl"
               value="/goods/list">

            <c:param name="type"
                     value="${type}"/>

            <c:param name="keyword"
                     value="${keyword}"/>

            <c:param name="sort"
                     value="${sort}"/>

            <c:param name="page"
                     value="${page + 1}"/>

            <c:if test="${not empty minPrice}">
                <c:param name="minPrice"
                         value="${minPrice}"/>
            </c:if>

            <c:if test="${not empty maxPrice}">
                <c:param name="maxPrice"
                         value="${maxPrice}"/>
            </c:if>

            <c:if test="${discountOnly}">
                <c:param name="discountOnly"
                         value="true"/>
            </c:if>

            <c:if test="${inStockOnly}">
                <c:param name="inStockOnly"
                         value="true"/>
            </c:if>

            <c:forEach var="selectedType"
                       items="${productTypes}">

                <c:param name="productTypes"
                         value="${selectedType}"/>

            </c:forEach>

            <c:forEach var="selectedRange"
                       items="${priceRanges}">

                <c:param name="priceRanges"
                         value="${selectedRange}"/>

            </c:forEach>

            <c:forEach var="selectedStock"
                       items="${stockStatus}">

                <c:param name="stockStatus"
                         value="${selectedStock}"/>

            </c:forEach>

        </c:url>

        <!-- =================================================
             [수정] 콘텐츠 목록과 동일한 공통 페이지네이션
        ================================================== -->
        <c:if test="${totalCount > 0}">
            <nav class="oditji-pagination"
                 data-pagination
                 data-current-page="${page}"
                 data-total-page="${totalPage}"
                 data-page-param="page"
                 aria-label="상품 목록 페이지"></nav>
        </c:if>

    </div>

    <!-- =================================================
         RIGHT RECOMMEND SIDEBAR
    ================================================== -->
    <aside class="goods-right-sidebar" aria-label="추천 상품">

        <jsp:include
            page="/WEB-INF/views/common/goodsRightSidebar.jsp"/>

    </aside>

</div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>