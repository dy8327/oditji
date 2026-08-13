<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- =====================================
     SELECTED PRICE RANGES
====================================== -->
<c:set var="under10000Checked" value="false"/>
<c:set var="range10to30Checked" value="false"/>
<c:set var="range30to50Checked" value="false"/>
<c:set var="range50to100Checked" value="false"/>
<c:set var="over100000Checked" value="false"/>

<c:forEach var="selectedRange" items="${priceRanges}">
    <c:if test="${selectedRange eq 'UNDER_10000'}">
        <c:set var="under10000Checked" value="true"/>
    </c:if>
    <c:if test="${selectedRange eq 'RANGE_10000_30000'}">
        <c:set var="range10to30Checked" value="true"/>
    </c:if>
    <c:if test="${selectedRange eq 'RANGE_30000_50000'}">
        <c:set var="range30to50Checked" value="true"/>
    </c:if>
    <c:if test="${selectedRange eq 'RANGE_50000_100000'}">
        <c:set var="range50to100Checked" value="true"/>
    </c:if>
    <c:if test="${selectedRange eq 'OVER_100000'}">
        <c:set var="over100000Checked" value="true"/>
    </c:if>
</c:forEach>

<!-- =====================================
     SELECTED STOCK STATUS
====================================== -->
<c:set var="inStockChecked" value="false"/>
<c:set var="soldOutChecked" value="false"/>

<c:forEach var="selectedStock" items="${stockStatus}">
    <c:if test="${selectedStock eq 'IN_STOCK'}">
        <c:set var="inStockChecked" value="true"/>
    </c:if>
    <c:if test="${selectedStock eq 'SOLD_OUT'}">
        <c:set var="soldOutChecked" value="true"/>
    </c:if>
</c:forEach>

<c:url var="resetFilterUrl" value="/goods/list">
    <c:param name="keyword" value="${keyword}"/>
    <c:param name="type" value="${type}"/>
    <c:param name="sort" value="${sort}"/>
    <c:if test="${type eq 'category'}">
        <c:forEach var="selectedType" items="${productTypes}">
            <c:param name="productTypes" value="${selectedType}"/>
        </c:forEach>
    </c:if>
</c:url>

<form id="goodsFilterForm"
      class="goods-filter-form"
      action="${pageContext.request.contextPath}/goods/list"
      method="get">

    <input type="hidden"
           name="keyword"
           value="<c:out value='${keyword}'/>">

    <input type="hidden"
           name="page"
           value="1">

    <input type="hidden"
           name="type"
           value="<c:out value='${type}'/>">

    <input type="hidden"
           name="sort"
           value="<c:out value='${sort}'/>">

    <c:if test="${type eq 'category'}">
        <c:forEach var="selectedType" items="${productTypes}">
            <input type="hidden"
                   name="productTypes"
                   value="<c:out value='${selectedType}'/>">
        </c:forEach>
    </c:if>

    <!-- contentLeftSidebar 구조와 100% 동일한 Header 영역 -->
    <header class="goods-filter-header">

        <div class="goods-filter-title-area">
            <h2>굿즈 필터</h2>
            <span>GOODS FILTER</span>
        </div>

        <p>
            원하는 조건을 선택해 굿즈를 좁혀보세요.
        </p>

    </header>

    <!-- =====================================
         PRODUCT TYPE
    ====================================== -->
    <c:if test="${type ne 'category'}">
        <section class="goods-filter-group">

            <h2>상품 종류</h2>

            <label class="goods-filter-option">
                <input type="checkbox"
                       data-goods-filter-all
                       data-goods-filter-group="productType"
                       <c:if test="${empty productTypes}">checked</c:if>>
                <span>전체</span>
            </label>

            <c:forEach var="availableType" items="${availableProductTypes}">
                <c:set var="productTypeChecked" value="false"/>
                <c:forEach var="selectedType" items="${productTypes}">
                    <c:if test="${selectedType eq availableType}">
                        <c:set var="productTypeChecked" value="true"/>
                    </c:if>
                </c:forEach>

                <label class="goods-filter-option">
                    <input type="checkbox"
                           name="productTypes"
                           value="<c:out value='${availableType}'/>"
                           data-goods-filter-item
                           data-goods-filter-group="productType"
                           <c:if test="${productTypeChecked}">checked</c:if>>
                    <span>
                        <c:choose>
                            <c:when test="${availableType eq 'CLOTHES'}">의상</c:when>
                            <c:when test="${availableType eq 'SHOES'}">신발</c:when>
                            <c:when test="${availableType eq 'PROP'}">소품</c:when>
                            <c:when test="${availableType eq 'GOODS'}">굿즈</c:when>
                            <c:when test="${availableType eq 'OST'}">OST</c:when>
                            <c:when test="${availableType eq 'BOOK'}">도서</c:when>
                            <c:when test="${availableType eq 'FIGURE'}">피규어</c:when>
                            <c:when test="${availableType eq 'POSTER'}">포스터</c:when>
                            <c:when test="${availableType eq 'ETC'}">기타</c:when>
                            <c:otherwise><c:out value="${availableType}"/></c:otherwise>
                        </c:choose>
                    </span>
                </label>
            </c:forEach>

        </section>
    </c:if>

    <!-- =====================================
         PRICE RANGE
    ====================================== -->
    <section class="goods-filter-group">

        <h2>가격대</h2>

        <label class="goods-filter-option">
            <input type="checkbox"
                   data-goods-filter-all
                   data-goods-filter-group="priceRange"
                   <c:if test="${empty priceRanges}">checked</c:if>>
            <span>전체</span>
        </label>

        <label class="goods-filter-option">
            <input type="checkbox"
                   name="priceRanges"
                   value="UNDER_10000"
                   data-goods-filter-item
                   data-goods-filter-group="priceRange"
                   <c:if test="${under10000Checked}">checked</c:if>>
            <span>1만원 미만</span>
        </label>

        <label class="goods-filter-option">
            <input type="checkbox"
                   name="priceRanges"
                   value="RANGE_10000_30000"
                   data-goods-filter-item
                   data-goods-filter-group="priceRange"
                   <c:if test="${range10to30Checked}">checked</c:if>>
            <span>1만 ~ 3만원</span>
        </label>

        <label class="goods-filter-option">
            <input type="checkbox"
                   name="priceRanges"
                   value="RANGE_30000_50000"
                   data-goods-filter-item
                   data-goods-filter-group="priceRange"
                   <c:if test="${range30to50Checked}">checked</c:if>>
            <span>3만 ~ 5만원</span>
        </label>

        <label class="goods-filter-option">
            <input type="checkbox"
                   name="priceRanges"
                   value="RANGE_50000_100000"
                   data-goods-filter-item
                   data-goods-filter-group="priceRange"
                   <c:if test="${range50to100Checked}">checked</c:if>>
            <span>5만 ~ 10만원</span>
        </label>

        <label class="goods-filter-option">
            <input type="checkbox"
                   name="priceRanges"
                   value="OVER_100000"
                   data-goods-filter-item
                   data-goods-filter-group="priceRange"
                   <c:if test="${over100000Checked}">checked</c:if>>
            <span>10만원 이상</span>
        </label>

    </section>

    <!-- =====================================
         STOCK STATUS
    ====================================== -->
    <section class="goods-filter-group">

        <h2>재고 상태</h2>

        <label class="goods-filter-option">
            <input type="checkbox"
                   data-goods-filter-all
                   data-goods-filter-group="stockStatus"
                   <c:if test="${empty stockStatus}">checked</c:if>>
            <span>전체</span>
        </label>

        <label class="goods-filter-option">
            <input type="checkbox"
                   name="stockStatus"
                   value="IN_STOCK"
                   data-goods-filter-item
                   data-goods-filter-group="stockStatus"
                   <c:if test="${inStockChecked}">checked</c:if>>
            <span>판매중</span>
        </label>

        <label class="goods-filter-option">
            <input type="checkbox"
                   name="stockStatus"
                   value="SOLD_OUT"
                   data-goods-filter-item
                   data-goods-filter-group="stockStatus"
                   <c:if test="${soldOutChecked}">checked</c:if>>
            <span>품절 포함</span>
        </label>

    </section>

    <!-- contentLeftSidebar와 동일한 액션 영역 -->
    <div class="goods-filter-actions">

        <button type="submit"
                class="goods-filter-submit-btn">
            필터 적용
        </button>

        <a href="${resetFilterUrl}"
           class="goods-filter-reset-btn"
           aria-label="선택한 굿즈 필터 전체 초기화">
            <span class="goods-filter-reset-icon"
                  aria-hidden="true">↻</span>
            <span>전체 초기화</span>
        </a>

    </div>

</form>