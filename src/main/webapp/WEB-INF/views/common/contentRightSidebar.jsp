<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!--
    기존 우측 세로 캐러셀을 본문 하단의 가로형 추천 섹션으로 변경했습니다.
    목록 폭을 넓게 유지하면서도 추천 콘텐츠 5개를 한 번에 비교할 수 있습니다.
-->
<section class="content-recommend-section"
         aria-labelledby="contentRecommendTitle">

    <header class="content-recommend-header">

        <div>
            <span class="content-recommend-eyebrow">
                RECOMMEND
            </span>

            <h2 id="contentRecommendTitle">
                함께 보기 좋은 콘텐츠
            </h2>

            <p>
                현재 선택한 콘텐츠 종류, 장르, OTT 조건을 반영했습니다.
            </p>
        </div>

    </header>

    <c:choose>

        <c:when test="${empty recommendedList}">

            <div class="content-recommend-empty">
                현재 조건으로 추천할 콘텐츠가 없습니다.
            </div>

        </c:when>

        <c:otherwise>

            <div class="content-recommend-grid">

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

                    <article class="content-recommend-card">

                        <a href="${recommendDetailUrl}"
                           class="content-recommend-link">

                            <div class="content-recommend-poster">

                                <c:choose>
                                    <c:when test="${not empty recommend.posterPath}">
                                        <img src="https://image.tmdb.org/t/p/w342${recommend.posterPath}"
                                             alt="<c:out value='${recommend.title}'/>"
                                             loading="lazy">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="content-recommend-no-image">
                                            NO IMAGE
                                        </div>
                                    </c:otherwise>
                                </c:choose>

                                <span class="content-recommend-type">
                                    <c:choose>
                                        <c:when test="${recommend.contentType eq 'MOVIE'}">
                                            영화
                                        </c:when>
                                        <c:otherwise>
                                            시리즈
                                        </c:otherwise>
                                    </c:choose>
                                </span>

                                <c:if test="${not empty recommend.tmdbScore}">
                                    <span class="content-recommend-score">
                                        <span aria-hidden="true">★</span>
                                        <fmt:formatNumber value="${recommend.tmdbScore}"
                                                          pattern="0.0"/>
                                    </span>
                                </c:if>

                            </div>

                            <div class="content-recommend-info">

                                <strong>
                                    <c:out value="${recommend.title}"/>
                                </strong>

                                <c:if test="${not empty recommend.genreText}">
                                    <span>
                                        <c:out value="${recommend.genreText}"/>
                                    </span>
                                </c:if>

                            </div>

                        </a>

                    </article>

                </c:forEach>

            </div>

        </c:otherwise>

    </c:choose>

</section>
