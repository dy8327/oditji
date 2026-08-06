<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- 검색 결과의 전체/콘텐츠 탭이 공통으로 사용하는 콘텐츠 카드 목록입니다. --%>
<div class="content-list-card-grid">
                                <c:forEach var="content" items="${searchContentItems}">
                                    <c:url var="ottCardDetailUrl" value="/content/prepare">
                                        <c:param name="tmdbId" value="${content.tmdbId}"/>
                                        <c:param name="contentType" value="${content.contentType}"/>
                                    </c:url>

                                    <c:set var="ottAgeBadgeLabel" value="?"/>
                                    <c:set var="ottAgeBadgeClass" value="unknown"/>
                                    <c:set var="ottAgeBadgeTitle" value="등급 정보 없음"/>

                                    <c:choose>
                                        <c:when test="${content.ageRating eq '전체 관람가'}">
                                            <c:set var="ottAgeBadgeLabel" value="ALL"/>
                                            <c:set var="ottAgeBadgeClass" value="all"/>
                                            <c:set var="ottAgeBadgeTitle" value="전체 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '7세 이상 관람가'}">
                                            <c:set var="ottAgeBadgeLabel" value="7"/>
                                            <c:set var="ottAgeBadgeClass" value="age7"/>
                                            <c:set var="ottAgeBadgeTitle" value="7세 이상 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '12세 이상 관람가'}">
                                            <c:set var="ottAgeBadgeLabel" value="12"/>
                                            <c:set var="ottAgeBadgeClass" value="age12"/>
                                            <c:set var="ottAgeBadgeTitle" value="12세 이상 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '15세 이상 관람가'}">
                                            <c:set var="ottAgeBadgeLabel" value="15"/>
                                            <c:set var="ottAgeBadgeClass" value="age15"/>
                                            <c:set var="ottAgeBadgeTitle" value="15세 이상 관람가"/>
                                        </c:when>
                                        <c:when test="${content.ageRating eq '청소년 관람불가'}">
                                            <c:set var="ottAgeBadgeLabel" value="19"/>
                                            <c:set var="ottAgeBadgeClass" value="adult"/>
                                            <c:set var="ottAgeBadgeTitle" value="청소년 관람불가"/>
                                        </c:when>
                                    </c:choose>

                                    <c:set var="ottTypeLabel" value="드라마"/>
                                    <c:set var="ottTypeBadgeClass" value="drama"/>
                                    <c:choose>
                                        <c:when test="${fn:contains(content.genreText, '애니메이션')}">
                                            <c:set var="ottTypeLabel" value="애니메이션"/>
                                            <c:set var="ottTypeBadgeClass" value="animation"/>
                                        </c:when>
                                        <c:when test="${fn:contains(content.genreText, '다큐멘터리')}">
                                            <c:set var="ottTypeLabel" value="다큐멘터리"/>
                                            <c:set var="ottTypeBadgeClass" value="documentary"/>
                                        </c:when>
                                        <c:when test="${content.contentType eq 'TV' and (fn:contains(content.genreText, '리얼리티') or fn:contains(content.genreText, '토크'))}">
                                            <c:set var="ottTypeLabel" value="예능"/>
                                            <c:set var="ottTypeBadgeClass" value="variety"/>
                                        </c:when>
                                        <c:when test="${content.contentType eq 'MOVIE'}">
                                            <c:set var="ottTypeLabel" value="영화"/>
                                            <c:set var="ottTypeBadgeClass" value="movie"/>
                                        </c:when>
                                    </c:choose>

                                    <article class="content-list-card">
                                        <a class="content-list-card-link" href="${ottCardDetailUrl}">
                                            <div class="content-list-card-poster">
                                                <c:choose>
                                                    <c:when test="${not empty content.posterPath}">
                                                        <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                                                             alt="<c:out value='${content.title}'/>"
                                                             loading="lazy">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="no-img">NO IMAGE</div>
                                                    </c:otherwise>
                                                </c:choose>

                                                <span class="content-list-type-badge is-${ottTypeBadgeClass}">
                                                    <c:out value="${ottTypeLabel}"/>
                                                </span>

                                                <span class="content-list-poster-age-rating" title="<c:out value='${ottAgeBadgeTitle}'/>">
                                                    <span class="age-rating-badge is-${ottAgeBadgeClass}"
                                                          aria-label="<c:out value='${ottAgeBadgeTitle}'/>">
                                                        <c:out value="${ottAgeBadgeLabel}"/>
                                                    </span>
                                                </span>

                                                <c:if test="${not empty content.tmdbScore and content.tmdbScore > 0}">
                                                    <span class="content-list-score-badge">
                                                        <span aria-hidden="true">★</span>
                                                        <fmt:formatNumber value="${content.tmdbScore}" pattern="0.0"/>
                                                    </span>
                                                </c:if>
                                            </div>

                                            <div class="content-list-card-info">
                                                <h2><c:out value="${content.title}"/></h2>

                                                <div class="content-list-card-meta">
                                                    <span>
                                                        <c:choose>
                                                            <c:when test="${not empty content.releaseDate}">
                                                                <c:out value="${content.releaseDate}"/>
                                                            </c:when>
                                                            <c:otherwise>공개일 미정</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    <span><c:out value="${ottTypeLabel}"/></span>
                                                </div>

                                                <c:if test="${not empty content.genreText}">
                                                    <p class="content-list-card-genre"><c:out value="${content.genreText}"/></p>
                                                </c:if>
                                            </div>
                                        </a>

                                        <div class="content-list-card-bottom">
                                            <c:if test="${not empty content.platformList}">
                                                <div class="content-list-platform-row" aria-label="시청 가능한 OTT 플랫폼">
                                                    <c:forEach var="platform" items="${content.platformList}" begin="0" end="2">
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

                                            <button type="button"
                                                    class="content-list-favorite-btn"
                                                    data-content-list-favorite
                                                    data-tmdb-id="${content.tmdbId}"
                                                    data-content-type="${content.contentType}"
                                                    aria-pressed="false"
                                                    aria-label="<c:out value='${content.title}'/> 찜하기"
                                                    title="찜하기">
                                                <span aria-hidden="true">♡</span>
                                            </button>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
