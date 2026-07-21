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

<c:set var="providerSelectedCount" value="0"/>

<c:if test="${netflixChecked}">
    <c:set var="providerSelectedCount" value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${tvingChecked}">
    <c:set var="providerSelectedCount" value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${wavveChecked}">
    <c:set var="providerSelectedCount" value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${disneyChecked}">
    <c:set var="providerSelectedCount" value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${watchaChecked}">
    <c:set var="providerSelectedCount" value="${providerSelectedCount + 1}"/>
</c:if>

<c:if test="${coupangChecked}">
    <c:set var="providerSelectedCount" value="${providerSelectedCount + 1}"/>
</c:if>

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

    <section class="content-filter-group content-provider-group">

        <h2>OTT 플랫폼</h2>

        <button type="button"
                id="contentOttModalOpenButton"
                class="content-provider-select-btn"
                aria-haspopup="dialog"
                aria-controls="contentOttPlatformModal">
            OTT 플랫폼 선택
        </button>

        <p id="contentOttSelectedSummary"
           class="content-provider-summary"
           aria-live="polite">

            <c:choose>
                <c:when test="${providerSelectedCount == 0}">
                    전체 플랫폼
                </c:when>
                <c:when test="${providerSelectedCount == 1 and netflixChecked}">Netflix</c:when>
                <c:when test="${providerSelectedCount == 1 and tvingChecked}">Tving</c:when>
                <c:when test="${providerSelectedCount == 1 and wavveChecked}">Wavve</c:when>
                <c:when test="${providerSelectedCount == 1 and disneyChecked}">Disney+</c:when>
                <c:when test="${providerSelectedCount == 1 and watchaChecked}">Watcha</c:when>
                <c:when test="${providerSelectedCount == 1 and coupangChecked}">Coupangplay</c:when>
                <c:otherwise>
                    <c:out value="${providerSelectedCount}"/>개 플랫폼 선택됨
                </c:otherwise>
            </c:choose>

        </p>

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

    <div id="contentOttPlatformModal"
         class="content-ott-modal"
         role="dialog"
         aria-modal="true"
         aria-labelledby="contentOttModalTitle"
         hidden>

        <div class="content-ott-modal-backdrop"
             data-content-ott-modal-close></div>

        <div class="content-ott-modal-dialog"
             role="document">

            <header class="content-ott-modal-header">
                <h2 id="contentOttModalTitle">OTT 플랫폼 선택</h2>
                <button type="button"
                        class="content-ott-modal-close-btn"
                        data-content-ott-modal-close
                        aria-label="닫기">×</button>
            </header>

            <div class="content-ott-modal-body">

                <label class="content-ott-modal-option content-ott-modal-all-option">
                    <input type="checkbox"
                           id="contentOttProviderAll"
                           data-content-filter-all
                           data-content-filter-group="provider"
                           <c:if test="${providerSelectedCount == 0}">checked</c:if>>
                    <span class="content-ott-modal-option-text">전체 플랫폼</span>
                </label>

                <div class="content-ott-modal-option-grid">
                    <label class="content-ott-modal-option">
                        <input type="checkbox" name="providerIds" value="Netflix"
                               data-content-provider-checkbox
                               data-content-filter-item data-content-filter-group="provider"
                               data-provider-name="Netflix"
                               <c:if test="${netflixChecked}">checked</c:if>>
                        <c:if test="${not empty ottLogoMap['netflix']}">
                            <img class="content-ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["netflix"]}'/>"
                                 alt="Netflix 로고"
                                 loading="lazy">
                        </c:if>
                        <span class="content-ott-modal-option-text">Netflix</span>
                    </label>
                    <label class="content-ott-modal-option">
                        <input type="checkbox" name="providerIds" value="Tving"
                               data-content-provider-checkbox
                               data-content-filter-item data-content-filter-group="provider"
                               data-provider-name="Tving"
                               <c:if test="${tvingChecked}">checked</c:if>>
                        <c:if test="${not empty ottLogoMap['tving']}">
                            <img class="content-ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["tving"]}'/>"
                                 alt="Tving 로고"
                                 loading="lazy">
                        </c:if>
                        <span class="content-ott-modal-option-text">Tving</span>
                    </label>
                    <label class="content-ott-modal-option">
                        <input type="checkbox" name="providerIds" value="Wavve"
                               data-content-provider-checkbox
                               data-content-filter-item data-content-filter-group="provider"
                               data-provider-name="Wavve"
                               <c:if test="${wavveChecked}">checked</c:if>>
                        <c:if test="${not empty ottLogoMap['wavve']}">
                            <img class="content-ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["wavve"]}'/>"
                                 alt="Wavve 로고"
                                 loading="lazy">
                        </c:if>
                        <span class="content-ott-modal-option-text">Wavve</span>
                    </label>
                    <label class="content-ott-modal-option">
                        <input type="checkbox" name="providerIds" value="Disney+"
                               data-content-provider-checkbox
                               data-content-filter-item data-content-filter-group="provider"
                               data-provider-name="Disney+"
                               <c:if test="${disneyChecked}">checked</c:if>>
                        <c:if test="${not empty ottLogoMap['disney']}">
                            <img class="content-ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["disney"]}'/>"
                                 alt="Disney+ 로고"
                                 loading="lazy">
                        </c:if>
                        <span class="content-ott-modal-option-text">Disney+</span>
                    </label>
                    <label class="content-ott-modal-option">
                        <input type="checkbox" name="providerIds" value="Watcha"
                               data-content-provider-checkbox
                               data-content-filter-item data-content-filter-group="provider"
                               data-provider-name="Watcha"
                               <c:if test="${watchaChecked}">checked</c:if>>
                        <c:if test="${not empty ottLogoMap['watcha']}">
                            <img class="content-ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["watcha"]}'/>"
                                 alt="Watcha 로고"
                                 loading="lazy">
                        </c:if>
                        <span class="content-ott-modal-option-text">Watcha</span>
                    </label>
                    <label class="content-ott-modal-option">
                        <input type="checkbox" name="providerIds" value="Coupangplay"
                               data-content-provider-checkbox
                               data-content-filter-item data-content-filter-group="provider"
                               data-provider-name="Coupangplay"
                               <c:if test="${coupangChecked}">checked</c:if>>
                        <c:if test="${not empty ottLogoMap['coupang']}">
                            <img class="content-ott-modal-option-logo"
                                 src="<c:out value='${ottLogoMap["coupang"]}'/>"
                                 alt="Coupangplay 로고"
                                 loading="lazy">
                        </c:if>
                        <span class="content-ott-modal-option-text">Coupangplay</span>
                    </label>
                </div>
            </div>

            <footer class="content-ott-modal-footer">
                <button type="button" id="contentOttModalCancelButton"
                        class="content-ott-modal-cancel-btn">취소</button>
                <button type="button" id="contentOttModalConfirmButton"
                        class="content-ott-modal-confirm-btn">선택 완료</button>
            </footer>

        </div>
    </div>

</form>
