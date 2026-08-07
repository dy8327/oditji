<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="content"
              required="true"
              type="java.lang.Object" %>
<%@ attribute name="variant"
              required="false"
              type="java.lang.String" %>

<%-- grid variant(콘텐츠 목록/검색결과/찜목록) 전용 선택 속성. main/more variant에는 영향 없음. --%>
<%@ attribute name="extraClass"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="detailUrl"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="releaseDateFormat"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="showPlatforms"
              required="false"
              type="java.lang.Boolean" %>
<%@ attribute name="scoreFormatted"
              required="false"
              type="java.lang.Boolean" %>
<%@ attribute name="scorePositiveOnly"
              required="false"
              type="java.lang.Boolean" %>
<%@ attribute name="showFavoriteButton"
              required="false"
              type="java.lang.Boolean" %>
<%@ attribute name="favoriteMode"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="favoriteTmdbId"
              required="false"
              type="java.lang.Object" %>
<%@ attribute name="favoriteContentType"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="favoriteContentNo"
              required="false"
              type="java.lang.Object" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>

<c:set var="resolvedVariant" value="${empty variant ? 'main' : variant}" />
<c:set var="moreVariant" value="${resolvedVariant eq 'more'}" />
<c:set var="gridVariant" value="${resolvedVariant eq 'grid'}" />
<c:set var="sidebarVariant" value="${resolvedVariant eq 'sidebar'}" />

<c:choose>
<c:when test="${gridVariant}">

    <c:set var="resolvedExtraClass" value="${empty extraClass ? '' : ' '.concat(extraClass)}" />
    <c:set var="resolvedReleaseDateFormat" value="${empty releaseDateFormat ? 'year' : releaseDateFormat}" />
    <c:set var="resolvedShowPlatforms" value="${empty showPlatforms ? true : showPlatforms}" />
    <c:set var="resolvedScoreFormatted" value="${empty scoreFormatted ? true : scoreFormatted}" />
    <c:set var="resolvedScorePositiveOnly" value="${empty scorePositiveOnly ? false : scorePositiveOnly}" />
    <c:set var="resolvedShowFavoriteButton" value="${empty showFavoriteButton ? true : showFavoriteButton}" />
    <c:set var="resolvedFavoriteMode" value="${empty favoriteMode ? 'toggle' : favoriteMode}" />
    <c:set var="resolvedFavTmdbId" value="${empty favoriteTmdbId ? content.tmdbId : favoriteTmdbId}" />
    <c:set var="resolvedFavContentType" value="${empty favoriteContentType ? content.contentType : favoriteContentType}" />
    <c:set var="resolvedFavContentNo" value="${empty favoriteContentNo ? content.contentNo : favoriteContentNo}" />

    <c:if test="${empty detailUrl}">
        <c:url var="detailUrl" value="/content/prepare">
            <c:param name="tmdbId" value="${content.tmdbId}" />
            <c:param name="contentType" value="${content.contentType}" />
        </c:url>
    </c:if>

    <c:set var="gridTypeLabel" value="드라마" />
    <c:set var="gridTypeClass" value="drama" />

    <c:choose>
        <c:when test="${fn:contains(content.genreText, '애니메이션')}">
            <c:set var="gridTypeLabel" value="애니메이션" />
            <c:set var="gridTypeClass" value="animation" />
        </c:when>
        <c:when test="${fn:contains(content.genreText, '다큐멘터리')}">
            <c:set var="gridTypeLabel" value="다큐멘터리" />
            <c:set var="gridTypeClass" value="documentary" />
        </c:when>
        <c:when test="${content.contentType eq 'TV'
                      and (fn:contains(content.genreText, '리얼리티')
                           or fn:contains(content.genreText, '토크'))}">
            <c:set var="gridTypeLabel" value="예능" />
            <c:set var="gridTypeClass" value="variety" />
        </c:when>
        <c:when test="${content.contentType eq 'MOVIE'}">
            <c:set var="gridTypeLabel" value="영화" />
            <c:set var="gridTypeClass" value="movie" />
        </c:when>
    </c:choose>

    <article class="content-list-card${resolvedExtraClass}">

        <a class="content-list-card-link"
           href="${detailUrl}">

            <div class="content-list-card-poster">

                <c:choose>
                    <c:when test="${not empty content.posterPath}">
                        <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                             alt="<c:out value='${content.title}'/>"
                             loading="lazy">
                    </c:when>
                    <c:otherwise>
                        <div class="no-img">
                            NO IMAGE
                        </div>
                    </c:otherwise>
                </c:choose>

                <span class="content-list-type-badge is-${gridTypeClass}">
                    <c:out value="${gridTypeLabel}" />
                </span>

                <common:ageRatingBadge ageRating="${content.ageRating}"
                                       outerClass="content-list-poster-age-rating"
                                       innerClass="age-rating-badge" />

                <c:if test="${not empty content.tmdbScore and (not resolvedScorePositiveOnly or content.tmdbScore > 0)}">
                    <span class="content-list-score-badge">
                        <span aria-hidden="true">★</span>
                        <c:choose>
                            <c:when test="${resolvedScoreFormatted}">
                                <fmt:formatNumber value="${content.tmdbScore}" pattern="0.0" />
                            </c:when>
                            <c:otherwise>
                                <c:out value="${content.tmdbScore}" />
                            </c:otherwise>
                        </c:choose>
                    </span>
                </c:if>

            </div>

            <div class="content-list-card-info">

                <h2>
                    <c:out value="${content.title}" />
                </h2>

                <div class="content-list-card-meta">

                    <span>
                        <c:choose>
                            <c:when test="${not empty content.releaseDate}">
                                <c:choose>
                                    <c:when test="${resolvedReleaseDateFormat eq 'full'}">
                                        <c:out value="${content.releaseDate}" />
                                    </c:when>
                                    <c:otherwise>
                                        ${fn:substring(content.releaseDate, 0, 4)}
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:otherwise>
                                공개일 미정
                            </c:otherwise>
                        </c:choose>
                    </span>

                    <span><c:out value="${gridTypeLabel}" /></span>

                </div>

                <c:if test="${not empty content.genreText}">
                    <p class="content-list-card-genre">
                        <c:out value="${content.genreText}" />
                    </p>
                </c:if>

            </div>

        </a>

        <div class="content-list-card-bottom">

            <c:if test="${resolvedShowPlatforms and not empty content.platformList}">

                <div class="content-list-platform-row"
                     aria-label="시청 가능한 OTT 플랫폼">

                    <c:forEach var="platform"
                               items="${content.platformList}"
                               begin="0"
                               end="2">

                        <c:if test="${not empty platform.logoImage}">
                            <img src="${platform.logoImage}"
                                 alt="<c:out value='${platform.platformName}'/>"
                                 title="<c:out value='${platform.platformName}'/>"
                                 loading="lazy">
                        </c:if>

                    </c:forEach>

                    <c:if test="${fn:length(content.platformList) > 3}">
                        <span class="content-list-platform-more">
                            +${fn:length(content.platformList) - 3}
                        </span>
                    </c:if>

                </div>

            </c:if>

            <c:if test="${resolvedShowFavoriteButton}">

                <c:choose>
                    <c:when test="${resolvedFavoriteMode eq 'remove'}">
                        <button type="button"
                                class="content-list-favorite-btn fav-btn active"
                                data-type="content"
                                data-content-no="${resolvedFavContentNo}"
                                aria-pressed="true"
                                aria-label="<c:out value='${content.title}'/> 찜 해제"
                                title="찜 해제">
                            ♥
                        </button>
                    </c:when>
                    <c:otherwise>
                        <button type="button"
                                class="content-list-favorite-btn"
                                data-content-list-favorite
                                data-tmdb-id="${resolvedFavTmdbId}"
                                data-content-type="${resolvedFavContentType}"
                                aria-pressed="false"
                                aria-label="<c:out value='${content.title}'/> 찜하기"
                                title="찜하기">
                            <span aria-hidden="true">♡</span>
                        </button>
                    </c:otherwise>
                </c:choose>

            </c:if>

        </div>

    </article>

</c:when>

<c:when test="${sidebarVariant}">

    <c:if test="${empty detailUrl}">
        <c:url var="detailUrl" value="/content/prepare">
            <c:param name="tmdbId" value="${content.tmdbId}" />
            <c:param name="contentType" value="${content.contentType}" />
        </c:url>
    </c:if>

    <%-- 좌측 상단 카테고리 뱃지 색상 판별. grid variant(gridTypeLabel/gridTypeClass)와
         동일한 기준(장르 텍스트 우선, 없으면 콘텐츠 타입)으로 맞춰서
         목록/추천 영역 어디서나 같은 색으로 보이게 한다. --%>
    <c:set var="sidebarTypeLabel" value="드라마" />
    <c:set var="sidebarTypeClass" value="drama" />

    <c:choose>
        <c:when test="${fn:contains(content.genreText, '애니메이션')}">
            <c:set var="sidebarTypeLabel" value="애니메이션" />
            <c:set var="sidebarTypeClass" value="animation" />
        </c:when>
        <c:when test="${fn:contains(content.genreText, '다큐멘터리')}">
            <c:set var="sidebarTypeLabel" value="다큐멘터리" />
            <c:set var="sidebarTypeClass" value="documentary" />
        </c:when>
        <c:when test="${content.contentType eq 'TV'
                      and (fn:contains(content.genreText, '리얼리티')
                           or fn:contains(content.genreText, '토크'))}">
            <c:set var="sidebarTypeLabel" value="예능" />
            <c:set var="sidebarTypeClass" value="variety" />
        </c:when>
        <c:when test="${content.contentType eq 'MOVIE' or content.contentType eq '영화'}">
            <c:set var="sidebarTypeLabel" value="영화" />
            <c:set var="sidebarTypeClass" value="movie" />
        </c:when>
    </c:choose>

    <article class="content-recommend-card">

        <a href="${detailUrl}"
           class="content-recommend-link">

            <div class="content-recommend-poster" style="position: relative;">

                <c:choose>
                    <c:when test="${not empty content.posterPath}">
                        <img src="https://image.tmdb.org/t/p/w342${content.posterPath}"
                             alt="<c:out value='${content.title}'/>"
                             loading="lazy">
                    </c:when>
                    <c:otherwise>
                        <div class="content-recommend-no-image">
                            NO IMAGE
                        </div>
                    </c:otherwise>
                </c:choose>

                <%-- 좌측 상단 카테고리 뱃지 --%>
                <span class="content-recommend-type is-${sidebarTypeClass}" style="position: absolute; top: 8px; left: 8px; z-index: 2;">
                    <c:out value="${sidebarTypeLabel}" />
                </span>

                <%-- 우측 상단 연령 등급 뱃지 --%>
                <common:ageRatingBadge ageRating="${content.ageRating}"
                                       outerClass="content-recommend-age-rating"
                                       innerClass="age-rating-badge" />

                <c:if test="${not empty content.tmdbScore}">
                    <span class="content-recommend-score">
                        <span aria-hidden="true">★</span>
                        <fmt:formatNumber value="${content.tmdbScore}" pattern="0.0"/>
                    </span>
                </c:if>

            </div>

            <div class="content-recommend-info">

                <strong class="card-title">
                    <c:out value="${content.title}"/>
                </strong>

                <div class="card-meta">
                    <c:if test="${not empty content.genreText}">
                        <span>
                            <c:out value="${content.genreText}"/>
                        </span>
                    </c:if>
                </div>

            </div>

        </a>

    </article>

</c:when>

<c:otherwise>

<c:choose>
    <c:when test="${moreVariant}">
        <c:set var="cardClass" value="content-more-card" />
        <c:set var="thumbClass" value="content-more-thumb" />
        <c:set var="noImageClass" value="content-more-no-image" />
        <c:set var="typeClass" value="content-more-type" />
        <c:set var="ageClass" value="content-more-age-rating" />
        <c:set var="infoClass" value="content-more-info" />
        <c:set var="metaClass" value="content-more-meta" />
        <c:set var="scoreClass" value="content-more-score" />
        <c:set var="platformListClass" value="content-more-platforms" />
        <c:set var="platformLogoClass" value="" />
        <c:set var="platformMoreClass" value="content-more-platform-more" />
        <c:set var="platformEndIndex" value="3" />
        <c:set var="platformDisplayLimit" value="4" />
    </c:when>

    <c:otherwise>
        <c:set var="cardClass" value="card" />
        <c:set var="thumbClass" value="thumb" />
        <c:set var="noImageClass" value="no-img" />
        <c:set var="typeClass" value="content-type-badge" />
        <c:set var="ageClass" value="content-age-rating" />
        <c:set var="infoClass" value="card-info" />
        <c:set var="metaClass" value="card-meta" />
        <c:set var="scoreClass" value="card-score" />
        <c:set var="platformListClass" value="card-platform-list" />
        <c:set var="platformLogoClass" value="card-platform-logo" />
        <c:set var="platformMoreClass" value="card-platform-more" />
        <c:set var="platformEndIndex" value="2" />
        <c:set var="platformDisplayLimit" value="3" />
    </c:otherwise>
</c:choose>

<c:url var="contentDetailUrl"
       value="/content/prepare">
    <c:param name="tmdbId" value="${content.tmdbId}" />
    <c:param name="contentType" value="${content.contentType}" />
</c:url>

<a href="${contentDetailUrl}"
   class="${cardClass}">

    <div class="${thumbClass}">

        <c:choose>
            <c:when test="${not empty content.posterPath}">
                <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                     alt="${content.title}"
                     loading="lazy">
            </c:when>

            <c:otherwise>
                <div class="${noImageClass}">
                    NO IMAGE
                </div>
            </c:otherwise>
        </c:choose>

        <span class="${typeClass}">
            <c:choose>
                <c:when test="${content.contentType eq 'MOVIE'}">
                    영화
                </c:when>

                <c:otherwise>
                    TV
                </c:otherwise>
            </c:choose>
        </span>

        <common:ageRatingBadge ageRating="${content.ageRating}"
                               outerClass="${ageClass}" />

    </div>

    <div class="${infoClass}">

        <c:choose>
            <c:when test="${moreVariant}">
                <h2>
                    <c:out value="${content.title}" />
                </h2>
            </c:when>

            <c:otherwise>
                <h3 class="card-title">
                    <c:out value="${content.title}" />
                </h3>
            </c:otherwise>
        </c:choose>

        <div class="${metaClass}">

            <span>
                <c:choose>
                    <c:when test="${not empty content.releaseDate}">
                        <c:out value="${content.releaseDate}" />
                    </c:when>

                    <c:otherwise>
                        공개일 미정
                    </c:otherwise>
                </c:choose>
            </span>

            <c:if test="${not empty content.tmdbScore}">
                <span class="${scoreClass}">
                    ⭐ <c:out value="${content.tmdbScore}" />
                </span>
            </c:if>

        </div>

        <c:if test="${not empty content.platformList}">

            <div class="${platformListClass}"
                 aria-label="시청 가능한 OTT">

                <c:forEach var="platform"
                           items="${content.platformList}"
                           begin="0"
                           end="${platformEndIndex}">

                    <img class="${platformLogoClass}"
                         src="${platform.logoImage}"
                         alt="${platform.platformName}"
                         title="${platform.platformName}"
                         loading="lazy">

                </c:forEach>

                <c:if test="${fn:length(content.platformList) > platformDisplayLimit}">
                    <span class="${platformMoreClass}">
                        +${fn:length(content.platformList) - platformDisplayLimit}
                    </span>
                </c:if>

            </div>

        </c:if>

    </div>

</a>

</c:otherwise>
</c:choose>