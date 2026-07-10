<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<section class="recommend-sidebar">

    <div class="recommend-sidebar-header">

        <h2>
            추천 콘텐츠
        </h2>

    </div>

    <c:choose>

        <c:when test="${empty recommendedList}">

            <div class="recommend-empty">

                <p>
                    추천 콘텐츠가 없습니다.
                </p>

            </div>

        </c:when>

        <c:otherwise>

            <div class="recommend-list">

                <c:forEach var="recommend"
                           items="${recommendedList}"
                           begin="0"
                           end="4">

                    <c:url var="recommendDetailUrl"
                           value="/content/prepare">

                        <c:param name="tmdbId"
                                 value="${recommend.tmdbId}"/>

                        <c:param name="contentType"
                                 value="${recommend.contentType}"/>

                    </c:url>

                    <a class="recommend-item"
                       href="${recommendDetailUrl}">

                        <div class="recommend-poster">

                            <c:choose>

                                <c:when test="${not empty recommend.posterPath}">

                                    <img src="https://image.tmdb.org/t/p/w185${recommend.posterPath}"
                                         alt="${recommend.title}"
                                         loading="lazy">

                                </c:when>

                                <c:otherwise>

                                    <div class="recommend-no-image">
                                        NO IMAGE
                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </div>

                        <div class="recommend-info">

                            <strong class="recommend-title">
                                ${recommend.title}
                            </strong>

                            <span class="recommend-type">

                                <c:choose>

                                    <c:when test="${recommend.contentType eq 'MOVIE'}">
                                        영화
                                    </c:when>

                                    <c:when test="${recommend.contentType eq 'TV'}">
                                        TV
                                    </c:when>

                                    <c:otherwise>
                                        콘텐츠
                                    </c:otherwise>

                                </c:choose>

                            </span>

                            <c:if test="${not empty recommend.tmdbScore}">

                                <span class="recommend-score">
                                    ⭐ ${recommend.tmdbScore}
                                </span>

                            </c:if>

                        </div>

                    </a>

                </c:forEach>

            </div>

        </c:otherwise>

    </c:choose>

</section>