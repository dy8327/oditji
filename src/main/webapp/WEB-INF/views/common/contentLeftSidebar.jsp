<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="movieChecked" value="false"/>
<c:set var="dramaChecked" value="false"/>
<c:set var="animationChecked" value="false"/>
<c:set var="varietyChecked" value="false"/>
<c:set var="documentaryChecked" value="false"/>

<c:forEach var="selectedCategory"
           items="${contentCategories}">

    <c:if test="${selectedCategory eq 'MOVIE'}">
        <c:set var="movieChecked" value="true"/>
    </c:if>

    <c:if test="${selectedCategory eq 'DRAMA'}">
        <c:set var="dramaChecked" value="true"/>
    </c:if>

    <c:if test="${selectedCategory eq 'ANIMATION'}">
        <c:set var="animationChecked" value="true"/>
    </c:if>

    <c:if test="${selectedCategory eq 'VARIETY'}">
        <c:set var="varietyChecked" value="true"/>
    </c:if>

    <c:if test="${selectedCategory eq 'DOCUMENTARY'}">
        <c:set var="documentaryChecked" value="true"/>
    </c:if>

</c:forEach>

<c:set var="categorySelected"
       value="${movieChecked
               or dramaChecked
               or animationChecked
               or varietyChecked
               or documentaryChecked}"/>

<c:set var="actionChecked" value="false"/>
<c:set var="comedyChecked" value="false"/>
<c:set var="thrillerChecked" value="false"/>
<c:set var="romanceChecked" value="false"/>
<c:set var="crimeChecked" value="false"/>
<c:set var="adventureChecked" value="false"/>
<c:set var="familyChecked" value="false"/>
<c:set var="fantasyChecked" value="false"/>
<c:set var="historyChecked" value="false"/>
<c:set var="horrorChecked" value="false"/>
<c:set var="musicChecked" value="false"/>
<c:set var="mysteryChecked" value="false"/>
<c:set var="sciFiChecked" value="false"/>
<c:set var="warChecked" value="false"/>
<c:set var="westernChecked" value="false"/>

<c:forEach var="selectedGenre"
           items="${genreCodes}">

    <c:if test="${selectedGenre eq 'ACTION'}">
        <c:set var="actionChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'COMEDY'}">
        <c:set var="comedyChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'THRILLER'}">
        <c:set var="thrillerChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'ROMANCE'}">
        <c:set var="romanceChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'CRIME'}">
        <c:set var="crimeChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'ADVENTURE'}">
        <c:set var="adventureChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'FAMILY'}">
        <c:set var="familyChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'FANTASY'}">
        <c:set var="fantasyChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'HISTORY'}">
        <c:set var="historyChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'HORROR'}">
        <c:set var="horrorChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'MUSIC'}">
        <c:set var="musicChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'MYSTERY'}">
        <c:set var="mysteryChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'SCI_FI'}">
        <c:set var="sciFiChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'WAR'}">
        <c:set var="warChecked" value="true"/>
    </c:if>

    <c:if test="${selectedGenre eq 'WESTERN'}">
        <c:set var="westernChecked" value="true"/>
    </c:if>

</c:forEach>

<c:set var="extraGenreSelected"
       value="${adventureChecked
               or familyChecked
               or fantasyChecked
               or historyChecked
               or horrorChecked
               or musicChecked
               or mysteryChecked
               or sciFiChecked
               or warChecked
               or westernChecked}"/>

<c:set var="netflixChecked" value="false"/>
<c:set var="tvingChecked" value="false"/>
<c:set var="wavveChecked" value="false"/>
<c:set var="disneyChecked" value="false"/>
<c:set var="watchaChecked" value="false"/>
<c:set var="coupangChecked" value="false"/>

<c:forEach var="selectedProvider"
           items="${providerIds}">

    <c:if test="${selectedProvider eq 'Netflix'}">
        <c:set var="netflixChecked" value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq 'Tving'}">
        <c:set var="tvingChecked" value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq 'Wavve'}">
        <c:set var="wavveChecked" value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq 'Disney+'}">
        <c:set var="disneyChecked" value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq 'Watcha'}">
        <c:set var="watchaChecked" value="true"/>
    </c:if>

    <c:if test="${selectedProvider eq 'Coupangplay'}">
        <c:set var="coupangChecked" value="true"/>
    </c:if>

</c:forEach>

<form id="contentFilterForm"
      class="content-filter-form"
      action="${pageContext.request.contextPath}/content/list"
      method="get">

    <input type="hidden"
           name="type"
           value="${type}">

    <input type="hidden"
           name="page"
           value="1">

    <section class="content-filter-group">

        <h2>콘텐츠 종류</h2>

        <label class="content-filter-option">
            <input type="checkbox"
                   data-content-filter-all
                   data-content-filter-group="category"
                   <c:if test="${not categorySelected}">
                       checked
                   </c:if>>
            <span>전체</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="contentCategories"
                   value="MOVIE"
                   data-content-filter-item
                   data-content-filter-group="category"
                   <c:if test="${movieChecked}">checked</c:if>>
            <span>영화</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="contentCategories"
                   value="DRAMA"
                   data-content-filter-item
                   data-content-filter-group="category"
                   <c:if test="${dramaChecked}">checked</c:if>>
            <span>드라마</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="contentCategories"
                   value="ANIMATION"
                   data-content-filter-item
                   data-content-filter-group="category"
                   <c:if test="${animationChecked}">checked</c:if>>
            <span>애니메이션</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="contentCategories"
                   value="VARIETY"
                   data-content-filter-item
                   data-content-filter-group="category"
                   <c:if test="${varietyChecked}">checked</c:if>>
            <span>예능</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="contentCategories"
                   value="DOCUMENTARY"
                   data-content-filter-item
                   data-content-filter-group="category"
                   <c:if test="${documentaryChecked}">checked</c:if>>
            <span>다큐멘터리</span>
        </label>

    </section>

    <section class="content-filter-group">

        <h2>장르</h2>

        <label class="content-filter-option">
            <input type="checkbox"
                   data-content-filter-all
                   data-content-filter-group="genre"
                   <c:if test="${empty genreCodes}">
                       checked
                   </c:if>>
            <span>전체</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="genreCodes"
                   value="ACTION"
                   data-content-filter-item
                   data-content-filter-group="genre"
                   <c:if test="${actionChecked}">checked</c:if>>
            <span>액션</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="genreCodes"
                   value="COMEDY"
                   data-content-filter-item
                   data-content-filter-group="genre"
                   <c:if test="${comedyChecked}">checked</c:if>>
            <span>코미디</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="genreCodes"
                   value="THRILLER"
                   data-content-filter-item
                   data-content-filter-group="genre"
                   <c:if test="${thrillerChecked}">checked</c:if>>
            <span>스릴러</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="genreCodes"
                   value="ROMANCE"
                   data-content-filter-item
                   data-content-filter-group="genre"
                   <c:if test="${romanceChecked}">checked</c:if>>
            <span>로맨스</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="genreCodes"
                   value="CRIME"
                   data-content-filter-item
                   data-content-filter-group="genre"
                   <c:if test="${crimeChecked}">checked</c:if>>
            <span>범죄</span>
        </label>

        <div id="contentExtraGenres"
             <c:if test="${not extraGenreSelected}">hidden</c:if>>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="ADVENTURE"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${adventureChecked}">checked</c:if>>
                <span>모험</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="FAMILY"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${familyChecked}">checked</c:if>>
                <span>가족</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="FANTASY"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${fantasyChecked}">checked</c:if>>
                <span>판타지</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="HISTORY"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${historyChecked}">checked</c:if>>
                <span>역사</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="HORROR"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${horrorChecked}">checked</c:if>>
                <span>공포</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="MUSIC"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${musicChecked}">checked</c:if>>
                <span>음악</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="MYSTERY"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${mysteryChecked}">checked</c:if>>
                <span>미스터리</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="SCI_FI"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${sciFiChecked}">checked</c:if>>
                <span>SF</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="WAR"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${warChecked}">checked</c:if>>
                <span>전쟁</span>
            </label>

            <label class="content-filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="WESTERN"
                       data-content-filter-item
                       data-content-filter-group="genre"
                       <c:if test="${westernChecked}">checked</c:if>>
                <span>서부</span>
            </label>

        </div>

        <button type="button"
                id="contentGenreToggleButton"
                class="content-filter-secondary-btn"
                aria-controls="contentExtraGenres"
                aria-expanded="${extraGenreSelected ? 'true' : 'false'}">

            <c:choose>
                <c:when test="${extraGenreSelected}">
                    장르 접기
                </c:when>
                <c:otherwise>
                    장르 전체보기
                </c:otherwise>
            </c:choose>

        </button>

    </section>

    <section class="content-filter-group">

        <h2>OTT 플랫폼</h2>

        <label class="content-filter-option">
            <input type="checkbox"
                   data-content-filter-all
                   data-content-filter-group="provider"
                   <c:if test="${empty providerIds}">
                       checked
                   </c:if>>
            <span>전체</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="providerIds"
                   value="Netflix"
                   data-content-filter-item
                   data-content-filter-group="provider"
                   <c:if test="${netflixChecked}">checked</c:if>>
            <span>Netflix</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="providerIds"
                   value="Tving"
                   data-content-filter-item
                   data-content-filter-group="provider"
                   <c:if test="${tvingChecked}">checked</c:if>>
            <span>Tving</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="providerIds"
                   value="Wavve"
                   data-content-filter-item
                   data-content-filter-group="provider"
                   <c:if test="${wavveChecked}">checked</c:if>>
            <span>Wavve</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="providerIds"
                   value="Disney+"
                   data-content-filter-item
                   data-content-filter-group="provider"
                   <c:if test="${disneyChecked}">checked</c:if>>
            <span>Disney+</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="providerIds"
                   value="Watcha"
                   data-content-filter-item
                   data-content-filter-group="provider"
                   <c:if test="${watchaChecked}">checked</c:if>>
            <span>Watcha</span>
        </label>

        <label class="content-filter-option">
            <input type="checkbox"
                   name="providerIds"
                   value="Coupangplay"
                   data-content-filter-item
                   data-content-filter-group="provider"
                   <c:if test="${coupangChecked}">checked</c:if>>
            <span>Coupangplay</span>
        </label>

    </section>

    <div class="content-filter-actions">

        <button type="submit"
                class="content-filter-submit-btn">
            필터 적용
        </button>

        <c:url var="resetUrl"
               value="/content/list">

            <c:param name="type"
                     value="${type}"/>

        </c:url>

        <a class="content-filter-reset-btn"
           href="${resetUrl}">
            필터 초기화
        </a>

    </div>

</form>
