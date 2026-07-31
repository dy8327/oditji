<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    추천 콘텐츠 카드 공통 JSP

    중요:
    - 이 파일은 recommendContent.jsp에서 정적 include로 포함됩니다.
    - 연령등급 비교에 한글 문자열을 직접 사용하므로
      이 JSP 조각 자체에도 UTF-8 pageEncoding을 명시합니다.
    - content.ageRating 원본 값은 data-age-rating에 남겨
      개발자 도구에서 실제 전달값을 확인할 수 있습니다.
--%>

<c:set var="ageBadgeLabel" value="?"/>
<c:set var="ageBadgeClass" value="unknown"/>
<c:set var="ageBadgeTitle" value="등급 정보 없음"/>

<c:choose>
    <c:when test="${content.ageRating eq '전체 관람가'}">
        <c:set var="ageBadgeLabel" value="ALL"/>
        <c:set var="ageBadgeClass" value="all"/>
        <c:set var="ageBadgeTitle" value="전체 관람가"/>
    </c:when>

    <c:when test="${content.ageRating eq '7세 이상 관람가'}">
        <c:set var="ageBadgeLabel" value="7"/>
        <c:set var="ageBadgeClass" value="age7"/>
        <c:set var="ageBadgeTitle" value="7세 이상 관람가"/>
    </c:when>

    <c:when test="${content.ageRating eq '12세 이상 관람가'}">
        <c:set var="ageBadgeLabel" value="12"/>
        <c:set var="ageBadgeClass" value="age12"/>
        <c:set var="ageBadgeTitle" value="12세 이상 관람가"/>
    </c:when>

    <c:when test="${content.ageRating eq '15세 이상 관람가'}">
        <c:set var="ageBadgeLabel" value="15"/>
        <c:set var="ageBadgeClass" value="age15"/>
        <c:set var="ageBadgeTitle" value="15세 이상 관람가"/>
    </c:when>

    <c:when test="${content.ageRating eq '청소년 관람불가'}">
        <c:set var="ageBadgeLabel" value="19"/>
        <c:set var="ageBadgeClass" value="adult"/>
        <c:set var="ageBadgeTitle" value="청소년 관람불가"/>
    </c:when>
</c:choose>

<a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&contentType=${content.contentType}"
   class="recommend-card">

    <div class="recommend-card-thumb">

        <c:choose>

            <c:when test="${not empty content.posterPath}">
                <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                     alt="${content.title}"
                     loading="lazy">
            </c:when>

            <c:otherwise>
                <div class="recommend-card-no-image">
                    NO IMAGE
                </div>
            </c:otherwise>

        </c:choose>

        <span class="recommend-card-type">

            <c:choose>

                <c:when test="${content.contentType eq 'MOVIE'}">
                    &#50689;&#54868;
                </c:when>

                <c:otherwise>
                    TV
                </c:otherwise>

            </c:choose>

        </span>

        <span class="recommend-card-age-rating"
              title="${ageBadgeTitle}"
              data-age-rating="${fn:escapeXml(content.ageRating)}">

            <span class="content-age-rating-badge is-${ageBadgeClass}"
                  aria-label="${ageBadgeTitle}">
                ${ageBadgeLabel}
            </span>

        </span>

    </div>

    <div class="recommend-card-info">

        <h3>
            ${content.title}
        </h3>

        <div class="recommend-card-meta">

            <span>

                <c:choose>

                    <c:when test="${showRecentEpisodeDate
                                  and content.contentType eq 'TV'
                                  and not empty content.lastAirDate}">
                        &#52572;&#44540; &#54924;&#52264; <br>
                        ${content.lastAirDate}
                    </c:when>

                    <c:when test="${not empty content.releaseDate}">
                        ${content.releaseDate}
                    </c:when>

                    <c:otherwise>
                        공개일 미정
                    </c:otherwise>

                </c:choose>

            </span>

            <c:if test="${not empty content.tmdbScore}">

                <span class="recommend-card-score">
                    <span aria-hidden="true">&#11088;</span>
                    ${content.tmdbScore}
                </span>

            </c:if>

        </div>

        <c:if test="${not empty content.platformList}">

            <div class="recommend-card-platforms"
                 aria-label="시청 가능한 OTT">

                <c:forEach var="platform"
                           items="${content.platformList}"
                           begin="0"
                           end="2">

                    <img src="${platform.logoImage}"
                         alt="${platform.platformName}"
                         title="${platform.platformName}"
                         loading="lazy">

                </c:forEach>

                <c:if test="${content.platformList.size() > 3}">
                    <span>
                        +${content.platformList.size() - 3}
                    </span>
                </c:if>

            </div>

        </c:if>

    </div>

</a>