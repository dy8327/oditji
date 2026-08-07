<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="content"
              required="true"
              type="java.lang.Object" %>
<%@ attribute name="showRecentEpisodeDate"
              required="false"
              type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>

<%--
    추천 콘텐츠 카드 공통 태그.

    - recommendContent.jsp의 4개 추천 섹션(인기/오늘의 추천/최근 방영/맞춤 추천)이
      공유한다. 기존에는 정적 include(recommendCard.jsp)였던 것을 태그로 전환했다.
    - showRecentEpisodeDate: true면 TV + lastAirDate가 있을 때 "최근 회차"를
      우선 표시한다(최근 방영 섹션 전용, 기본값 false).
    - content.ageRating 원본 값은 data-age-rating에 남겨
      개발자 도구에서 실제 전달값을 확인할 수 있다(common:ageRatingBadge 내부 처리).
--%>
<c:set var="resolvedShowRecentEpisodeDate" value="${empty showRecentEpisodeDate ? false : showRecentEpisodeDate}" />

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

        <common:ageRatingBadge ageRating="${content.ageRating}"
                               outerClass="recommend-card-age-rating"
                               includeDataAgeRating="true" />

    </div>

    <div class="recommend-card-info">

        <h3>
            ${content.title}
        </h3>

        <div class="recommend-card-meta">

            <span>

                <c:choose>

                    <c:when test="${resolvedShowRecentEpisodeDate
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
