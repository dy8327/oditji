<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

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

    <%-- [추가] 현재 목록 유형을 필터 적용 후에도 유지합니다. --%>
    <input type="hidden"
           name="type"
           value="<c:out value='${type}'/>">

    <%--
        [수정] 세부 카테고리를 선택한 화면에서는 상품 종류 체크박스를
        숨기고, 선택한 카테고리 값을 hidden으로 계속 전달합니다.
    --%>
    <c:if test="${type eq 'category'}">
        <c:forEach var="selectedType" items="${productTypes}">
            <input type="hidden"
                   name="productTypes"
                   value="<c:out value='${selectedType}'/>">
        </c:forEach>
    </c:if>

    <!-- =====================================
         PRODUCT TYPE

         availableProductTypes에는 승인된 상품 중
         실제로 등록되어 있는 상품 종류만 들어온다.
         따라서 DB에 상품이 존재하는 종류만 화면에 표시한다.
    ====================================== -->
    <%-- [수정] 카테고리 화면에서는 가격대/재고 상태 필터만 표시합니다. --%>
    <c:if test="${type ne 'category'}">
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

        <c:forEach var="availableType"
                   items="${availableProductTypes}">

            <c:set var="productTypeChecked"
                   value="false"/>

            <c:forEach var="selectedType"
                       items="${productTypes}">

                <c:if test="${selectedType eq availableType}">
                    <c:set var="productTypeChecked"
                           value="true"/>
                </c:if>

            </c:forEach>

            <label class="filter-option">

                <input type="checkbox"
                       name="productTypes"
                       value="<c:out value='${availableType}'/>"
                       data-filter-checkbox
                       data-filter-group="productType"
                       <c:if test="${productTypeChecked}">
                           checked
                       </c:if>>

                <span>
                    <c:choose>

                        <c:when test="${availableType eq 'CLOTHES'}">
                            의상
                        </c:when>

                        <c:when test="${availableType eq 'SHOES'}">
                            신발
                        </c:when>

                        <c:when test="${availableType eq 'PROP'}">
                            소품
                        </c:when>

                        <c:when test="${availableType eq 'GOODS'}">
                            굿즈
                        </c:when>

                        <c:when test="${availableType eq 'OST'}">
                            OST
                        </c:when>

                        <c:when test="${availableType eq 'BOOK'}">
                            도서
                        </c:when>

                        <c:when test="${availableType eq 'FIGURE'}">
                            피규어
                        </c:when>

                        <c:when test="${availableType eq 'POSTER'}">
                            포스터
                        </c:when>

                        <c:when test="${availableType eq 'ETC'}">
                            기타
                        </c:when>

                        <c:otherwise>
                            <c:out value="${availableType}"/>
                        </c:otherwise>

                    </c:choose>
                </span>

            </label>

        </c:forEach>

    </section>

    </c:if>

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

    <div class="filter-action-box">

        <button type="submit"
                class="filter-submit-btn">
            필터 적용
        </button>

        <c:url var="resetFilterUrl"
               value="/goods/list">

            <c:param name="keyword"
                     value="${keyword}"/>

            <%-- [수정] 초기화 시에도 현재 목록 유형을 유지합니다. --%>
            <c:param name="type"
                     value="${type}"/>

            <%-- [수정] 카테고리 화면에서는 선택한 세부 카테고리도 유지합니다. --%>
            <c:if test="${type eq 'category'}">
                <c:forEach var="selectedType" items="${productTypes}">
                    <c:param name="productTypes"
                             value="${selectedType}"/>
                </c:forEach>
            </c:if>

        </c:url>

        <a class="filter-reset-btn"
           href="${resetFilterUrl}">
            필터 초기화
        </a>

    </div>

</form>