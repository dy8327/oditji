<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="content"
              required="true"
              type="java.lang.Object" %>
<%@ attribute name="variant"
              required="true"
              type="java.lang.String" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>

<%--
    메인 슬라이더와 오늘의 콘텐츠 목록이 공유하는 콘텐츠 카드이다.
    variant가 more이면 콘텐츠 더보기 화면용 CSS 클래스를 사용한다.
--%>
<c:set var="moreVariant" value="${variant eq 'more'}" />

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
