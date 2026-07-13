<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="movieChecked" value="false"/>
<c:set var="dramaChecked" value="false"/>
<c:set var="animationChecked" value="false"/>
<c:set var="varietyChecked" value="false"/>
<c:set var="documentaryChecked" value="false"/>

<c:forEach var="category" items="${contentCategories}">

    <c:if test="${category eq 'MOVIE'}">
        <c:set var="movieChecked" value="true"/>
    </c:if>

    <c:if test="${category eq 'DRAMA'}">
        <c:set var="dramaChecked" value="true"/>
    </c:if>

    <c:if test="${category eq 'ANIMATION'}">
        <c:set var="animationChecked" value="true"/>
    </c:if>

    <c:if test="${category eq 'VARIETY'}">
        <c:set var="varietyChecked" value="true"/>
    </c:if>

    <c:if test="${category eq 'DOCUMENTARY'}">
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

<c:forEach var="genre" items="${genreCodes}">

    <c:if test="${genre eq 'ACTION'}">
        <c:set var="actionChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'COMEDY'}">
        <c:set var="comedyChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'THRILLER'}">
        <c:set var="thrillerChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'ROMANCE'}">
        <c:set var="romanceChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'CRIME'}">
        <c:set var="crimeChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'ADVENTURE'}">
        <c:set var="adventureChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'FAMILY'}">
        <c:set var="familyChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'FANTASY'}">
        <c:set var="fantasyChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'HISTORY'}">
        <c:set var="historyChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'HORROR'}">
        <c:set var="horrorChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'MUSIC'}">
        <c:set var="musicChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'MYSTERY'}">
        <c:set var="mysteryChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'SCI_FI'}">
        <c:set var="sciFiChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'WAR'}">
        <c:set var="warChecked" value="true"/>
    </c:if>

    <c:if test="${genre eq 'WESTERN'}">
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

<c:forEach var="provider" items="${providerIds}">

    <c:if test="${provider eq '8'}">
        <c:set var="netflixChecked" value="true"/>
    </c:if>

    <c:if test="${provider eq '1883'}">
        <c:set var="tvingChecked" value="true"/>
    </c:if>

    <c:if test="${provider eq '356'}">
        <c:set var="wavveChecked" value="true"/>
    </c:if>

    <c:if test="${provider eq '337'}">
        <c:set var="disneyChecked" value="true"/>
    </c:if>

    <c:if test="${provider eq '97'}">
        <c:set var="watchaChecked" value="true"/>
    </c:if>

    <c:if test="${provider eq '283'}">
        <c:set var="coupangChecked" value="true"/>
    </c:if>

</c:forEach>

<c:set var="providerSelectedCount" value="0"/>

<c:if test="${netflixChecked}">
    <c:set var="providerSelectedCount"
           value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${tvingChecked}">
    <c:set var="providerSelectedCount"
           value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${wavveChecked}">
    <c:set var="providerSelectedCount"
           value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${disneyChecked}">
    <c:set var="providerSelectedCount"
           value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${watchaChecked}">
    <c:set var="providerSelectedCount"
           value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${coupangChecked}">
    <c:set var="providerSelectedCount"
           value="${providerSelectedCount + 1}"/>
</c:if>

<c:set var="sidebarTab"
       value="${searchTab eq 'GOODS'
                ? 'GOODS'
                : 'CONTENT'}"/>

<form id="searchFilterForm"
      class="search-filter-form"
      action="${pageContext.request.contextPath}/search"
      method="get"
      data-initial-sidebar-tab="${sidebarTab}">

    <input type="hidden"
           name="keyword"
           value="<c:out value='${keyword}'/>"/>

    <input type="hidden"
           name="contentPage"
           value="1"/>

    <input type="hidden"
           name="goodsPage"
           value="1"/>

    <input type="hidden"
           name="searchTab"
           value="${empty searchTab ? 'ALL' : searchTab}"/>

    <div class="sidebar-filter-tabs"
         role="tablist"
         aria-label="검색 필터 종류">

        <button type="button"
                class="sidebar-filter-tab ${sidebarTab eq 'CONTENT' ? 'is-active' : ''}"
                data-sidebar-filter-tab="CONTENT"
                role="tab"
                aria-selected="${sidebarTab eq 'CONTENT' ? 'true' : 'false'}">
            콘텐츠
        </button>

        <button type="button"
                class="sidebar-filter-tab ${sidebarTab eq 'GOODS' ? 'is-active' : ''}"
                data-sidebar-filter-tab="GOODS"
                role="tab"
                aria-selected="${sidebarTab eq 'GOODS' ? 'true' : 'false'}">
            상품
        </button>

    </div>

    <div class="sidebar-filter-panel"
         data-sidebar-filter-panel="CONTENT"
         <c:if test="${sidebarTab ne 'CONTENT'}">hidden</c:if>>

        <section class="filter-group">

            <h2 class="filter-group-title">
                콘텐츠 종류
            </h2>

            <label class="filter-option">

                <input type="checkbox"
                       data-filter-all
                       data-filter-group="contentCategory"
                       <c:if test="${not categorySelected}">checked</c:if>/>

                <span>
                    전체
                </span>

            </label>

            <label class="filter-option">

                <input type="checkbox"
                       name="contentCategories"
                       value="MOVIE"
                       data-filter-checkbox
                       data-filter-group="contentCategory"
                       <c:if test="${movieChecked}">checked</c:if>/>

                <span>
                    영화
                </span>

            </label>

            <label class="filter-option">

                <input type="checkbox"
                       name="contentCategories"
                       value="DRAMA"
                       data-filter-checkbox
                       data-filter-group="contentCategory"
                       <c:if test="${dramaChecked}">checked</c:if>/>

                <span>
                    드라마
                </span>

            </label>

            <label class="filter-option">

                <input type="checkbox"
                       name="contentCategories"
                       value="ANIMATION"
                       data-filter-checkbox
                       data-filter-group="contentCategory"
                       <c:if test="${animationChecked}">checked</c:if>/>

                <span>
                    애니메이션
                </span>

            </label>

            <label class="filter-option">

                <input type="checkbox"
                       name="contentCategories"
                       value="VARIETY"
                       data-filter-checkbox
                       data-filter-group="contentCategory"
                       <c:if test="${varietyChecked}">checked</c:if>/>

                <span>
                    예능
                </span>

            </label>

            <label class="filter-option">

                <input type="checkbox"
                       name="contentCategories"
                       value="DOCUMENTARY"
                       data-filter-checkbox
                       data-filter-group="contentCategory"
                       <c:if test="${documentaryChecked}">checked</c:if>/>

                <span>
                    다큐멘터리
                </span>

            </label>

        </section>

        <section class="filter-group">

            <h2 class="filter-group-title">
                장르
            </h2>

            <label class="filter-option">

                <input type="checkbox"
                       data-filter-all
                       data-filter-group="genre"
                       <c:if test="${empty genreCodes}">checked</c:if>/>

                <span>
                    전체
                </span>

            </label>

            <label class="filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="ACTION"
                       data-filter-checkbox
                       data-filter-group="genre"
                       <c:if test="${actionChecked}">checked</c:if>/>
                <span>액션</span>
            </label>

            <label class="filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="COMEDY"
                       data-filter-checkbox
                       data-filter-group="genre"
                       <c:if test="${comedyChecked}">checked</c:if>/>
                <span>코미디</span>
            </label>

            <label class="filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="THRILLER"
                       data-filter-checkbox
                       data-filter-group="genre"
                       <c:if test="${thrillerChecked}">checked</c:if>/>
                <span>스릴러</span>
            </label>

            <label class="filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="ROMANCE"
                       data-filter-checkbox
                       data-filter-group="genre"
                       <c:if test="${romanceChecked}">checked</c:if>/>
                <span>로맨스</span>
            </label>

            <label class="filter-option">
                <input type="checkbox"
                       name="genreCodes"
                       value="CRIME"
                       data-filter-checkbox
                       data-filter-group="genre"
                       <c:if test="${crimeChecked}">checked</c:if>/>
                <span>범죄</span>
            </label>

            <div id="genreExtraOptions"
                 class="genre-extra-options"
                 <c:if test="${not extraGenreSelected}">hidden</c:if>>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="ADVENTURE"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${adventureChecked}">checked</c:if>/>
                    <span>모험</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="FAMILY"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${familyChecked}">checked</c:if>/>
                    <span>가족</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="FANTASY"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${fantasyChecked}">checked</c:if>/>
                    <span>판타지</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="HISTORY"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${historyChecked}">checked</c:if>/>
                    <span>역사</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="HORROR"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${horrorChecked}">checked</c:if>/>
                    <span>공포</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="MUSIC"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${musicChecked}">checked</c:if>/>
                    <span>음악</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="MYSTERY"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${mysteryChecked}">checked</c:if>/>
                    <span>미스터리</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="SCI_FI"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${sciFiChecked}">checked</c:if>/>
                    <span>SF</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="WAR"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${warChecked}">checked</c:if>/>
                    <span>전쟁</span>
                </label>

                <label class="filter-option">
                    <input type="checkbox"
                           name="genreCodes"
                           value="WESTERN"
                           data-filter-checkbox
                           data-filter-group="genre"
                           <c:if test="${westernChecked}">checked</c:if>/>
                    <span>서부</span>
                </label>

            </div>

        </section>

        <div class="filter-secondary-action-box">

            <button type="button"
                    id="genreToggleButton"
                    class="filter-secondary-action-btn"
                    aria-controls="genreExtraOptions"
                    aria-expanded="${extraGenreSelected ? 'true' : 'false'}"
                    data-expanded="${extraGenreSelected ? 'true' : 'false'}">

                <c:choose>

                    <c:when test="${extraGenreSelected}">
                        장르 접기
                    </c:when>

                    <c:otherwise>
                        장르 전체보기
                    </c:otherwise>

                </c:choose>

            </button>

        </div>

        <section class="filter-group filter-provider-group">

            <h2 class="filter-group-title">
                OTT 플랫폼
            </h2>

            <button type="button"
                    id="ottModalOpenButton"
                    class="filter-provider-select-btn"
                    aria-haspopup="dialog"
                    aria-controls="ottPlatformModal">
                OTT 플랫폼 선택
            </button>

            <p id="ottSelectedSummary"
               class="filter-provider-summary"
               aria-live="polite">

                <c:choose>

                    <c:when test="${providerSelectedCount == 0}">
                        전체 플랫폼
                    </c:when>

                    <c:when test="${providerSelectedCount == 1 and netflixChecked}">
                        Netflix
                    </c:when>

                    <c:when test="${providerSelectedCount == 1 and tvingChecked}">
                        Tving
                    </c:when>

                    <c:when test="${providerSelectedCount == 1 and wavveChecked}">
                        Wavve
                    </c:when>

                    <c:when test="${providerSelectedCount == 1 and disneyChecked}">
                        Disney+
                    </c:when>

                    <c:when test="${providerSelectedCount == 1 and watchaChecked}">
                        Watcha
                    </c:when>

                    <c:when test="${providerSelectedCount == 1 and coupangChecked}">
                        Coupangplay
                    </c:when>

                    <c:otherwise>
                        <c:out value="${providerSelectedCount}"/>개 플랫폼 선택됨
                    </c:otherwise>

                </c:choose>

            </p>

        </section>

    </div>

    <div class="sidebar-filter-panel"
         data-sidebar-filter-panel="GOODS"
         <c:if test="${sidebarTab ne 'GOODS'}">hidden</c:if>>

        <section class="filter-group">

            <h2 class="filter-group-title">
                상품 유형
            </h2>

            <c:choose>

                <c:when test="${empty availableProductTypes}">

                    <p class="filter-empty-message">
                        등록된 상품 유형이 없습니다.
                    </p>

                </c:when>

                <c:otherwise>

                    <c:forEach var="availableType"
                               items="${availableProductTypes}">

                        <c:set var="typeChecked"
                               value="false"/>

                        <c:forEach var="selectedType"
                                   items="${productTypes}">

                            <c:if test="${selectedType eq availableType}">
                                <c:set var="typeChecked"
                                       value="true"/>
                            </c:if>

                        </c:forEach>

                        <label class="filter-option">

                            <input type="checkbox"
                                   name="productTypes"
                                   value="<c:out value='${availableType}'/>"
                                   <c:if test="${typeChecked}">checked</c:if>/>

                            <span>
                                <c:out value="${availableType}"/>
                            </span>

                        </label>

                    </c:forEach>

                </c:otherwise>

            </c:choose>

        </section>

        <section class="filter-group">

            <h2 class="filter-group-title">
                가격 범위
            </h2>

            <div class="goods-price-filter">

                <label class="goods-price-field">

                    <span>
                        최소 가격
                    </span>

                    <input type="number"
                           name="minPrice"
                           min="0"
                           step="1000"
                           value="<c:out value='${minPrice}'/>"
                           placeholder="0"/>

                </label>

                <span class="goods-price-divider">
                    ~
                </span>

                <label class="goods-price-field">

                    <span>
                        최대 가격
                    </span>

                    <input type="number"
                           name="maxPrice"
                           min="0"
                           step="1000"
                           value="<c:out value='${maxPrice}'/>"
                           placeholder="제한 없음"/>

                </label>

            </div>

        </section>

        <section class="filter-group">

            <h2 class="filter-group-title">
                판매 조건
            </h2>

            <label class="filter-option">

                <input type="checkbox"
                       name="discountOnly"
                       value="true"
                       <c:if test="${discountOnly}">checked</c:if>/>

                <span>
                    할인 상품만
                </span>

            </label>

            <label class="filter-option">

                <input type="checkbox"
                       name="inStockOnly"
                       value="true"
                       <c:if test="${inStockOnly}">checked</c:if>/>

                <span>
                    품절 상품 제외
                </span>

            </label>

        </section>

    </div>

    <div class="filter-action-box">

        <button type="submit"
                class="filter-submit-btn">
            필터 적용
        </button>

        <c:url var="resetFilterUrl"
               value="/search">

            <c:param name="keyword"
                     value="${keyword}"/>

            <c:param name="contentPage"
                     value="1"/>

            <c:param name="goodsPage"
                     value="1"/>

            <c:param name="searchTab"
                     value="${sidebarTab}"/>

        </c:url>

        <a class="filter-reset-btn"
           href="${resetFilterUrl}">
            필터 초기화
        </a>

    </div>

    <div id="ottPlatformModal"
         class="ott-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="ottModalTitle"
         hidden>

        <div class="ott-modal-backdrop"
             data-ott-modal-close>
        </div>

        <div class="ott-modal-dialog"
             role="document">

            <header class="ott-modal-header">

                <h2 id="ottModalTitle">
                    OTT 플랫폼 선택
                </h2>

                <button type="button"
                        class="ott-modal-close-btn"
                        data-ott-modal-close
                        aria-label="닫기">
                    ×
                </button>

            </header>

            <div class="ott-modal-body">

                <label class="ott-modal-option ott-modal-all-option">

                    <input type="checkbox"
                           id="ottProviderAll"
                           data-filter-all
                           data-filter-group="provider"
                           <c:if test="${providerSelectedCount == 0}">checked</c:if>/>

                    <span class="ott-modal-option-text">
                        전체 플랫폼
                    </span>

                </label>

                <div class="ott-modal-option-grid">

                    <label class="ott-modal-option">

                        <input type="checkbox"
                               name="providerIds"
                               value="8"
                               data-provider-checkbox
                               data-filter-checkbox
                               data-filter-group="provider"
                               data-provider-name="Netflix"
                               <c:if test="${netflixChecked}">checked</c:if>/>

                        <c:if test="${not empty ottLogoMap['netflix']}">

                            <img class="ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["netflix"]}'/>"
                                 alt="Netflix 로고"
                                 loading="lazy">

                        </c:if>

                        <span class="ott-modal-option-text">
                            Netflix
                        </span>

                    </label>

                    <label class="ott-modal-option">

                        <input type="checkbox"
                               name="providerIds"
                               value="1883"
                               data-provider-checkbox
                               data-filter-checkbox
                               data-filter-group="provider"
                               data-provider-name="Tving"
                               <c:if test="${tvingChecked}">checked</c:if>/>

                        <c:if test="${not empty ottLogoMap['tving']}">

                            <img class="ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["tving"]}'/>"
                                 alt="Tving 로고"
                                 loading="lazy">

                        </c:if>

                        <span class="ott-modal-option-text">
                            Tving
                        </span>

                    </label>

                    <label class="ott-modal-option">

                        <input type="checkbox"
                               name="providerIds"
                               value="356"
                               data-provider-checkbox
                               data-filter-checkbox
                               data-filter-group="provider"
                               data-provider-name="Wavve"
                               <c:if test="${wavveChecked}">checked</c:if>/>

                        <c:if test="${not empty ottLogoMap['wavve']}">

                            <img class="ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["wavve"]}'/>"
                                 alt="Wavve 로고"
                                 loading="lazy">

                        </c:if>

                        <span class="ott-modal-option-text">
                            Wavve
                        </span>

                    </label>

                    <label class="ott-modal-option">

                        <input type="checkbox"
                               name="providerIds"
                               value="337"
                               data-provider-checkbox
                               data-filter-checkbox
                               data-filter-group="provider"
                               data-provider-name="Disney+"
                               <c:if test="${disneyChecked}">checked</c:if>/>

                        <c:if test="${not empty ottLogoMap['disney']}">

                            <img class="ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["disney"]}'/>"
                                 alt="Disney+ 로고"
                                 loading="lazy">

                        </c:if>

                        <span class="ott-modal-option-text">
                            Disney+
                        </span>

                    </label>

                    <label class="ott-modal-option">

                        <input type="checkbox"
                               name="providerIds"
                               value="97"
                               data-provider-checkbox
                               data-filter-checkbox
                               data-filter-group="provider"
                               data-provider-name="Watcha"
                               <c:if test="${watchaChecked}">checked</c:if>/>

                        <c:if test="${not empty ottLogoMap['watcha']}">

                            <img class="ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["watcha"]}'/>"
                                 alt="Watcha 로고"
                                 loading="lazy">

                        </c:if>

                        <span class="ott-modal-option-text">
                            Watcha
                        </span>

                    </label>

                    <label class="ott-modal-option">

                        <input type="checkbox"
                               name="providerIds"
                               value="283"
                               data-provider-checkbox
                               data-filter-checkbox
                               data-filter-group="provider"
                               data-provider-name="Coupangplay"
                               <c:if test="${coupangChecked}">checked</c:if>/>

                        <c:if test="${not empty ottLogoMap['coupang']}">

                            <img class="ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["coupang"]}'/>"
                                 alt="Coupangplay 로고"
                                 loading="lazy">

                        </c:if>

                        <span class="ott-modal-option-text">
                            Coupangplay
                        </span>

                    </label>

                </div>

            </div>

            <footer class="ott-modal-footer">

                <button type="button"
                        id="ottModalCancelButton"
                        class="ott-modal-cancel-btn">
                    취소
                </button>

                <button type="button"
                        id="ottModalConfirmButton"
                        class="ott-modal-confirm-btn">
                    선택 완료
                </button>

            </footer>

        </div>

    </div>

</form>