<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!-- =====================================
     SELECTED CONTENT TYPES
====================================== -->

<c:set var="movieChecked"
       value="false"/>

<c:set var="tvChecked"
       value="false"/>

<c:forEach var="selectedType"
           items="${contentTypes}">

    <c:if test="${selectedType eq 'MOVIE'}">
        <c:set var="movieChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedType eq 'TV'}">
        <c:set var="tvChecked"
               value="true"/>
    </c:if>

</c:forEach>

<!-- =====================================
     SELECTED GENRES
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

<!-- =====================================
     SELECTED PROVIDERS
====================================== -->

<c:set var="netflixChecked"
       value="false"/>

<c:set var="tvingChecked"
       value="false"/>

<c:set var="wavveChecked"
       value="false"/>

<c:set var="disneyChecked"
       value="false"/>

<c:set var="watchaChecked"
       value="false"/>

<c:set var="coupangChecked"
       value="false"/>

<c:forEach var="selectedProvider"
           items="${providerIds}">

    <c:if test="${selectedProvider eq '8'}">
        <c:set var="netflixChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq '1883'}">
        <c:set var="tvingChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq '356'}">
        <c:set var="wavveChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq '337'}">
        <c:set var="disneyChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq '97'}">
        <c:set var="watchaChecked"
               value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq '283'}">
        <c:set var="coupangChecked"
               value="true"/>
    </c:if>

</c:forEach>

<form id="searchFilterForm"
      class="search-filter-form"
      action="${pageContext.request.contextPath}/search"
      method="get">

    <input type="hidden"
           name="keyword"
           value="<c:out value='${keyword}'/>">

    <input type="hidden"
           name="page"
           value="1">

    <!-- =====================================
         CONTENT TYPE
    ====================================== -->
    <section class="filter-group">

        <h2 class="filter-group-title">
            콘텐츠 종류
        </h2>

        <label class="filter-option">

            <input type="checkbox"
                   data-filter-all
                   data-filter-group="contentType"
                   <c:if test="${not movieChecked and not tvChecked}">
                       checked
                   </c:if>>

            <span>전체</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="contentTypes"
                   value="MOVIE"
                   data-filter-checkbox
                   data-filter-group="contentType"
                   <c:if test="${movieChecked}">
                       checked
                   </c:if>>

            <span>영화</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="contentTypes"
                   value="TV"
                   data-filter-checkbox
                   data-filter-group="contentType"
                   <c:if test="${tvChecked}">
                       checked
                   </c:if>>

            <span>TV</span>

        </label>

    </section>

    <!-- =====================================
         GENRE
    ====================================== -->
    <section class="filter-group">

        <h2 class="filter-group-title">
            장르
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

    <!-- =====================================
         OTT PROVIDER
    ====================================== -->
    <section class="filter-group">

        <h2 class="filter-group-title">
            OTT 플랫폼
        </h2>

        <label class="filter-option">

            <input type="checkbox"
                   data-filter-all
                   data-filter-group="provider"
                   <c:if test="${empty providerIds}">
                       checked
                   </c:if>>

            <span>전체</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="providerIds"
                   value="8"
                   data-filter-checkbox
                   data-filter-group="provider"
                   <c:if test="${netflixChecked}">
                       checked
                   </c:if>>

            <span>Netflix</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="providerIds"
                   value="1883"
                   data-filter-checkbox
                   data-filter-group="provider"
                   <c:if test="${tvingChecked}">
                       checked
                   </c:if>>

            <span>Tving</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="providerIds"
                   value="356"
                   data-filter-checkbox
                   data-filter-group="provider"
                   <c:if test="${wavveChecked}">
                       checked
                   </c:if>>

            <span>Wavve</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="providerIds"
                   value="337"
                   data-filter-checkbox
                   data-filter-group="provider"
                   <c:if test="${disneyChecked}">
                       checked
                   </c:if>>

            <span>Disney+</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="providerIds"
                   value="97"
                   data-filter-checkbox
                   data-filter-group="provider"
                   <c:if test="${watchaChecked}">
                       checked
                   </c:if>>

            <span>Watcha</span>

        </label>

        <label class="filter-option">

            <input type="checkbox"
                   name="providerIds"
                   value="283"
                   data-filter-checkbox
                   data-filter-group="provider"
                   <c:if test="${coupangChecked}">
                       checked
                   </c:if>>

            <span>Coupangplay</span>

        </label>

    </section>

    <div class="filter-action-box">

        <button type="submit"
                class="filter-submit-btn">
            필터 적용
        </button>

        <c:url var="resetFilterUrl"
               value="/search">

            <c:param name="keyword"
                     value="${keyword}"/>

        </c:url>

        <a class="filter-reset-btn"
           href="${resetFilterUrl}">
            필터 초기화
        </a>

    </div>

</form>