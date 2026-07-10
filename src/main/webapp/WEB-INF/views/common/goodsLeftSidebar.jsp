<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- =====================================
     SELECTED PRODUCT TYPES
====================================== -->

<c:set var="apparelChecked"
       value="false"/>

<c:set var="figureChecked"
       value="false"/>

<c:set var="albumChecked"
       value="false"/>

<c:set var="posterChecked"
       value="false"/>

<c:set var="accessoryChecked"
       value="false"/>

<c:set var="etcChecked"
       value="false"/>

<c:forEach var="selectedType"
           items="${productTypes}">

    <c:if test="${selectedType eq 'APPAREL'}">
        <c:set var="apparelChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedType eq 'FIGURE'}">
        <c:set var="figureChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedType eq 'ALBUM'}">
        <c:set var="albumChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedType eq 'POSTER'}">
        <c:set var="posterChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedType eq 'ACCESSORY'}">
        <c:set var="accessoryChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedType eq 'ETC'}">
        <c:set var="etcChecked"
               value="true"/>
    </c:if>

</c:forEach>

<!-- =====================================
     SELECTED PRICE RANGES
====================================== -->

<c:set var="under10000Checked"
       value="false"/>

<c:set var="range10to30Checked"
       value="false"/>

<c:set var="range30to50Checked"
       value="false"/>

<c:set var="range50to100Checked"
       value="false"/>

<c:set var="over100000Checked"
       value="false"/>

<c:forEach var="selectedRange"
           items="${priceRanges}">

    <c:if test="${selectedRange eq 'UNDER_10000'}">
        <c:set var="under10000Checked"
               value="true"/>
    </c:if>

    <c:if test="${selectedRange eq 'RANGE_10000_30000'}">
        <c:set var="range10to30Checked"
               value="true"/>
    </c:if>

    <c:if test="${selectedRange eq 'RANGE_30000_50000'}">
        <c:set var="range30to50Checked"
               value="true"/>
    </c:if>

    <c:if test="${selectedRange eq 'RANGE_50000_100000'}">
        <c:set var="range50to100Checked"
               value="true"/>
    </c:if>

    <c:if test="${selectedRange eq 'OVER_100000'}">
        <c:set var="over100000Checked"
               value="true"/>
    </c:if>

</c:forEach>

<!-- =====================================
     SELECTED STOCK STATUS
====================================== -->

<c:set var="inStockChecked"
       value="false"/>

<c:set var="soldOutChecked"
       value="false"/>

<c:forEach var="selectedStock"
           items="${stockStatus}">

    <c:if test="${selectedStock eq 'IN_STOCK'}">
        <c:set var="inStockChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedStock eq 'SOLD_OUT'}">
        <c:set var="soldOutChecked"
               value="true"/>
    </c:if>

</c:forEach>

<!-- =====================================
     SELECTED GENRES (원작 콘텐츠 장르)
====================================== -->

<c:set var="actionChecked"
       value="false"/>

<c:set var="comedyChecked"
       value="false"/>

<c:set var="dramaChecked"
       value="false"/>

<c:set var="thrillerChecked"
       value="false"/>

<c:set var="romanceChecked"
       value="false"/>

<c:set var="animationChecked"
       value="false"/>

<c:set var="documentaryChecked"
       value="false"/>

<c:forEach var="selectedGenre"
           items="${genreCodes}">

    <c:if test="${selectedGenre eq 'ACTION'}">
        <c:set var="actionChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'COMEDY'}">
        <c:set var="comedyChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'DRAMA'}">
        <c:set var="dramaChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'THRILLER'}">
        <c:set var="thrillerChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'ROMANCE'}">
        <c:set var="romanceChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'ANIMATION'}">
        <c:set var="animationChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'DOCUMENTARY'}">
        <c:set var="documentaryChecked"
               value="true"/>
    </c:if>

</c:forEach>

<form id="goodsFilterForm"
      class="search-filter-form"
      action="${pageContext.request.contextPath}/goods/list"
      method="get">

    <input type="hidden"
           name="keyword"
           value="<c:out value='${keyword}'/>">

    <input type="hidden"
           name="page"
           value="1">

    <!-- =====================================
         PRODUCT TYPE
    ====================================== -->
    <section class="filter-group">

        <h2 class="filter-group-title">
            상품 종류
        </h2>

        <label class="filter-option">

            <input type="checkbox"
                   data-filter-all
                   data-filter-group="productType"
                   <c:if test="${empty productTypes}">
                       checked
                   </c:if>>

            <span>전체</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="productTypes"
                   value="APPAREL"
                   data-filter-checkbox
                   data-filter-group="productType"
                   <c:if test="${apparelChecked}">
                       checked
                   </c:if>>

            <span>의류</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="productTypes"
                   value="FIGURE"
                   data-filter-checkbox
                   data-filter-group="productType"
                   <c:if test="${figureChecked}">
                       checked
                   </c:if>>

            <span>피규어</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="productTypes"
                   value="ALBUM"
                   data-filter-checkbox
                   data-filter-group="productType"
                   <c:if test="${albumChecked}">
                       checked
                   </c:if>>

            <span>음반</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="productTypes"
                   value="POSTER"
                   data-filter-checkbox
                   data-filter-group="productType"
                   <c:if test="${posterChecked}">
                       checked
                   </c:if>>

            <span>포스터</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="productTypes"
                   value="ACCESSORY"
                   data-filter-checkbox
                   data-filter-group="productType"
                   <c:if test="${accessoryChecked}">
                       checked
                   </c:if>>

            <span>액세서리</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="productTypes"
                   value="ETC"
                   data-filter-checkbox
                   data-filter-group="productType"
                   <c:if test="${etcChecked}">
                       checked
                   </c:if>>

            <span>기타</span>

        </label>

    </section>

    <!-- =====================================
         PRICE RANGE
    ====================================== -->
    <section class="filter-group">

        <h2 class="filter-group-title">
            가격대
        </h2>

        <label class="filter-option">

            <input type="checkbox"
                   data-filter-all
                   data-filter-group="priceRange"
                   <c:if test="${empty priceRanges}">
                       checked
                   </c:if>>

            <span>전체</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="priceRanges"
                   value="UNDER_10000"
                   data-filter-checkbox
                   data-filter-group="priceRange"
                   <c:if test="${under10000Checked}">
                       checked
                   </c:if>>

            <span>1만원 미만</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="priceRanges"
                   value="RANGE_10000_30000"
                   data-filter-checkbox
                   data-filter-group="priceRange"
                   <c:if test="${range10to30Checked}">
                       checked
                   </c:if>>

            <span>1만 ~ 3만원</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="priceRanges"
                   value="RANGE_30000_50000"
                   data-filter-checkbox
                   data-filter-group="priceRange"
                   <c:if test="${range30to50Checked}">
                       checked
                   </c:if>>

            <span>3만 ~ 5만원</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="priceRanges"
                   value="RANGE_50000_100000"
                   data-filter-checkbox
                   data-filter-group="priceRange"
                   <c:if test="${range50to100Checked}">
                       checked
                   </c:if>>

            <span>5만 ~ 10만원</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="priceRanges"
                   value="OVER_100000"
                   data-filter-checkbox
                   data-filter-group="priceRange"
                   <c:if test="${over100000Checked}">
                       checked
                   </c:if>>

            <span>10만원 이상</span>

        </label>

    </section>

    <!-- =====================================
         STOCK STATUS
    ====================================== -->
    <section class="filter-group">

        <h2 class="filter-group-title">
            재고 상태
        </h2>

        <label class="filter-option">

            <input type="checkbox"
                   data-filter-all
                   data-filter-group="stockStatus"
                   <c:if test="${empty stockStatus}">
                       checked
                   </c:if>>

            <span>전체</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="stockStatus"
                   value="IN_STOCK"
                   data-filter-checkbox
                   data-filter-group="stockStatus"
                   <c:if test="${inStockChecked}">
                       checked
                   </c:if>>

            <span>판매중</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="stockStatus"
                   value="SOLD_OUT"
                   data-filter-checkbox
                   data-filter-group="stockStatus"
                   <c:if test="${soldOutChecked}">
                       checked
                   </c:if>>

            <span>품절 포함</span>

        </label>

    </section>

    <!-- =====================================
         GENRE (원작 콘텐츠 장르)
    ====================================== -->
    <section class="filter-group">

        <h2 class="filter-group-title">
            원작 장르
        </h2>

        <label class="filter-option">

            <input type="checkbox"
                   data-filter-all
                   data-filter-group="genre"
                   <c:if test="${empty genreCodes}">
                       checked
                   </c:if>>

            <span>전체</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="genreCodes"
                   value="ACTION"
                   data-filter-checkbox
                   data-filter-group="genre"
                   <c:if test="${actionChecked}">
                       checked
                   </c:if>>

            <span>액션</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="genreCodes"
                   value="COMEDY"
                   data-filter-checkbox
                   data-filter-group="genre"
                   <c:if test="${comedyChecked}">
                       checked
                   </c:if>>

            <span>코미디</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="genreCodes"
                   value="DRAMA"
                   data-filter-checkbox
                   data-filter-group="genre"
                   <c:if test="${dramaChecked}">
                       checked
                   </c:if>>

            <span>드라마</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="genreCodes"
                   value="THRILLER"
                   data-filter-checkbox
                   data-filter-group="genre"
                   <c:if test="${thrillerChecked}">
                       checked
                   </c:if>>

            <span>스릴러</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="genreCodes"
                   value="ROMANCE"
                   data-filter-checkbox
                   data-filter-group="genre"
                   <c:if test="${romanceChecked}">
                       checked
                   </c:if>>

            <span>로맨스</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="genreCodes"
                   value="ANIMATION"
                   data-filter-checkbox
                   data-filter-group="genre"
                   <c:if test="${animationChecked}">
                       checked
                   </c:if>>

            <span>애니메이션</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="genreCodes"
                   value="DOCUMENTARY"
                   data-filter-checkbox
                   data-filter-group="genre"
                   <c:if test="${documentaryChecked}">
                       checked
                   </c:if>>

            <span>다큐멘터리</span>

        </label>

    </section>

    <div class="filter-action-box">

        <button type="submit"
                class="filter-submit-btn">
            필터 적용
        </button>

        <c:url var="resetFilterUrl"
               value="/goods/list">

            <c:param name="keyword"
                     value="${keyword}"/>

        </c:url>

        <a class="filter-reset-btn"
           href="${resetFilterUrl}">
            필터 초기화
        </a>

    </div>

</form>
