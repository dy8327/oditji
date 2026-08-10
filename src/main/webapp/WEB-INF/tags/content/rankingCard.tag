<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="content"
              required="true"
              type="java.lang.Object" %>
<%@ attribute name="rank"
              required="true"
              type="java.lang.Integer" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/common" %>
<%@ taglib prefix="content" tagdir="/WEB-INF/tags/content" %>

<%--
    전체 및 OTT별 랭킹 패널이 공유하는 콘텐츠 카드이다.

    [리팩터링] 실제 OTT 랭킹 페이지처럼 보이도록 두 가지를 보강한다.
    1) 평점을 contentCard.tag/recommendCard.tag와 동일하게 fmt:formatNumber로
       소수 첫째 자리까지 통일한다. (8.175 / 9.43 처럼 자릿수가 들쭉날쭉하던 것을
       8.2 / 9.4 처럼 사이트 전체와 같은 규칙으로 맞춘다)
    2) "전체" 탭처럼 여러 OTT가 섞여 나오는 패널에서 어느 플랫폼에서 볼 수 있는지
       바로 보이도록, 목록/추천 카드에 이미 쓰던 것과 같은 플랫폼 로고 줄을
       common:platformDisplayName 태그로 접근성 텍스트를 붙여 추가한다.
    3) 1~3위는 카드에 is-top3 클래스를 붙여 포스터 둘레에 은은한 골드 링을 두르고,
       순위 숫자는 메인화면 "실시간 인기 콘텐츠"(.rank-number)와 완전히 같은 방식
       (윤곽선 숫자가 포스터 왼쪽 바깥으로 살짝 걸치는 넷플릭스 Top10 스타일)을
       그대로 재사용해서 1/2/3위는 금/은/동 색으로 구분한다. 포스터 자체를 클립하는
       레이어(.ranking-poster-frame)를 숫자와 분리해서, 숫자만 카드 밖으로
       자연스럽게 삐져나오게 한다.
--%>
<c:url var="rankingDetailUrl"
       value="/content/prepare">
    <c:param name="tmdbId" value="${content.tmdbId}" />
    <c:param name="contentType" value="${content.contentType}" />
</c:url>

<li class="ranking-card ${rank le 3 ? 'is-top3' : ''}">

    <a href="${rankingDetailUrl}"
       class="ranking-link">

        <div class="ranking-poster-wrap">

            <div class="ranking-poster-frame">

                <c:choose>
                    <c:when test="${not empty content.posterPath}">
                        <img class="ranking-poster"
                             src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                             alt="<c:out value='${content.title}' /> 포스터"
                             loading="lazy">
                    </c:when>

                    <c:otherwise>
                        <div class="ranking-no-poster">
                            포스터 이미지가 없습니다.
                        </div>
                    </c:otherwise>
                </c:choose>

            </div>

            <span class="ranking-number"
                  aria-hidden="true">
                ${rank}
            </span>

            <common:ageRatingBadge ageRating="${content.ageRating}"
                                   outerClass="ranking-age-rating"
                                   innerClass="age-rating-badge" />

        </div>

        <div class="ranking-info">

            <h3 class="ranking-content-title">
                <c:out value="${content.title}" />
            </h3>

            <div class="ranking-meta">

                <content:contentTypeBadge content="${content}"
                                         outerClass="ranking-type" />

                <c:if test="${not empty content.releaseDate}">
                    <span>
                        <c:out value="${content.releaseDate}" />
                    </span>
                </c:if>

                <c:if test="${not empty content.tmdbScore}">
                    <span class="ranking-score">
                        <span aria-hidden="true">★</span>
                        <fmt:formatNumber value="${content.tmdbScore}" pattern="0.0" />
                    </span>
                </c:if>

            </div>

            <c:if test="${not empty content.platformList}">

                <div class="ranking-platforms"
                     aria-label="시청 가능한 OTT 플랫폼">

                    <c:forEach var="platform"
                               items="${content.platformList}"
                               begin="0"
                               end="2">

                        <c:if test="${not empty platform.logoImage}">
                            <img class="ranking-platform-logo"
                                 src="${platform.logoImage}"
                                 alt="<common:platformDisplayName platformName='${platform.platformName}' />"
                                 title="<common:platformDisplayName platformName='${platform.platformName}' />"
                                 loading="lazy">
                        </c:if>

                    </c:forEach>

                    <c:if test="${fn:length(content.platformList) > 3}">
                        <span class="ranking-platform-more">
                            +${fn:length(content.platformList) - 3}
                        </span>
                    </c:if>

                </div>

            </c:if>

        </div>

    </a>

</li>
