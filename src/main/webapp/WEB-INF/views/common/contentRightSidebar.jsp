<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<section class="recommend-sidebar"
         data-recommend-carousel>

    <div class="recommend-sidebar-header">

        <h2>추천 콘텐츠</h2>

        <c:if test="${not empty recommendedList}">

            <div class="recommend-carousel-controls">

                <button type="button"
                        class="recommend-carousel-button"
                        data-recommend-previous
                        aria-label="이전 추천 콘텐츠">
                    ‹
                </button>

                <button type="button"
                        class="recommend-carousel-button"
                        data-recommend-next
                        aria-label="다음 추천 콘텐츠">
                    ›
                </button>

            </div>

        </c:if>

    </div>

    <c:choose>

        <c:when test="${empty recommendedList}">

            <div class="recommend-empty">
                추천 콘텐츠가 없습니다.
            </div>

        </c:when>

        <c:otherwise>

            <div class="recommend-carousel-viewport"
                 data-recommend-viewport>

                <div class="recommend-carousel-track"
                     data-recommend-track>

                    <c:forEach var="recommend"
                               items="${recommendedList}"
                               begin="0"
                               end="4"
                               varStatus="status">

                        <c:url var="recommendDetailUrl"
                               value="/content/prepare">

                            <c:param name="tmdbId"
                                     value="${recommend.tmdbId}"/>

                            <c:param name="contentType"
                                     value="${recommend.contentType}"/>

                        </c:url>

                        <article class="recommend-slide"
                                 data-recommend-slide
                                 aria-hidden="${status.first ? 'false' : 'true'}">

                            <a class="recommend-item"
                               href="${recommendDetailUrl}">

                                <div class="recommend-poster">

                                    <c:choose>

                                        <c:when test="${not empty recommend.posterPath}">

                                            <img src="https://image.tmdb.org/t/p/w342${recommend.posterPath}"
                                                 alt="<c:out value='${recommend.title}'/>"
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
                                        <c:out value="${recommend.title}"/>
                                    </strong>

                                    <div class="recommend-meta">

                                        <span>

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
                                            <span>⭐ ${recommend.tmdbScore}</span>
                                        </c:if>

                                    </div>

                                </div>

                            </a>

                        </article>

                    </c:forEach>

                </div>

            </div>

            <div class="recommend-carousel-status"
                 aria-live="polite">

                <span data-recommend-current>1</span>
                <span aria-hidden="true">/</span>
                <span data-recommend-total>1</span>

            </div>

        </c:otherwise>

    </c:choose>

</section>
