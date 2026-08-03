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

<%--
    전체 및 OTT별 랭킹 패널이 공유하는 콘텐츠 카드이다.
--%>
<c:url var="rankingDetailUrl"
       value="/content/prepare">
    <c:param name="tmdbId" value="${content.tmdbId}" />
    <c:param name="contentType" value="${content.contentType}" />
</c:url>

<li class="ranking-card">

    <a href="${rankingDetailUrl}"
       class="ranking-link">

        <div class="ranking-poster-wrap">

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

            <span class="ranking-number ${rank le 3 ? 'top-rank' : ''}">
                ${rank}
            </span>

        </div>

        <div class="ranking-info">

            <h3 class="ranking-content-title">
                <c:out value="${content.title}" />
            </h3>

            <div class="ranking-meta">

                <span class="ranking-type">
                    <c:choose>
                        <c:when test="${content.contentType eq 'MOVIE'}">
                            영화
                        </c:when>

                        <c:when test="${content.contentType eq 'TV'}">
                            TV
                        </c:when>

                        <c:otherwise>
                            <c:out value="${content.contentType}" />
                        </c:otherwise>
                    </c:choose>
                </span>

                <c:if test="${not empty content.releaseDate}">
                    <span>
                        <c:out value="${content.releaseDate}" />
                    </span>
                </c:if>

                <c:if test="${not empty content.tmdbScore}">
                    <span class="ranking-score">
                        ★ <c:out value="${content.tmdbScore}" />
                    </span>
                </c:if>

            </div>

        </div>

    </a>

</li>
