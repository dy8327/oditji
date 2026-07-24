<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>ODITJI | ${pageTitle}</title>

    <!-- 콘텐츠 상세 등 기존 공용 스타일을 먼저 불러옵니다. -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content.css?v=22">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/component.css">

    <!-- 콘텐츠 목록 화면 전용 레이아웃과 카드 디자인입니다. -->
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/content-list-modern.css?v=2">

    <script>
        const contextPath = "${pageContext.request.contextPath}";
    </script>

    <script defer
            src="${pageContext.request.contextPath}/js/content.js"></script>

    <script defer
            src="${pageContext.request.contextPath}/js/favorite.js"></script>

    <script defer
            src="${pageContext.request.contextPath}/js/contentList.js?v=4"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="content-list-page">

    <div class="content-list-layout">

        <!-- 왼쪽 영역은 검색 조건만 담당하도록 유지합니다. -->
        <aside class="content-list-left-sidebar">
            <jsp:include page="/WEB-INF/views/common/contentLeftSidebar.jsp"/>
        </aside>

        <section class="content-list-main">

            <!-- 제목과 전체 건수를 같은 시각적 그룹에 배치합니다. -->
            <header class="content-list-header">

                <div class="content-list-heading">
                    <span class="content-list-eyebrow">CONTENT LIBRARY</span>

                    <div class="content-list-title-row">
                        <h1>${pageTitle}</h1>

                        <span class="content-list-result-count">
                            총
                            <strong>
                                <fmt:formatNumber value="${totalCount}"
                                                  pattern="#,###"/>
                            </strong>개
                        </span>
                    </div>

                    <p>
                        한국에서 정액제로 시청 가능한 콘텐츠만 표시합니다.
                    </p>
                </div>

            </header>

            <!-- 선택된 필터와 정렬 기능을 목록 바로 위에서 확인할 수 있습니다. -->
            <section class="content-list-toolbar"
                     aria-label="콘텐츠 목록 도구">

                <div class="content-selected-filter-area">

                    <c:choose>
                        <c:when test="${not empty contentCategories
                                      or not empty genreCodes
                                      or not empty providerIds}">

                            <span class="content-selected-filter-label">
                                선택된 조건
                            </span>

                            <div class="content-selected-filter-list">

                                <c:forEach var="category"
                                           items="${contentCategories}">

                                    <button type="button"
                                            class="content-selected-filter-chip"
                                            data-content-filter-chip
                                            data-filter-name="contentCategories"
                                            data-filter-value="${category}">

                                        <span>
                                            <c:choose>
                                                <c:when test="${category eq 'MOVIE'}">영화</c:when>
                                                <c:when test="${category eq 'DRAMA'}">드라마</c:when>
                                                <c:when test="${category eq 'ANIMATION'}">애니메이션</c:when>
                                                <c:when test="${category eq 'VARIETY'}">예능</c:when>
                                                <c:when test="${category eq 'DOCUMENTARY'}">다큐멘터리</c:when>
                                                <c:otherwise><c:out value="${category}"/></c:otherwise>
                                            </c:choose>
                                        </span>

                                        <span aria-hidden="true">×</span>
                                    </button>

                                </c:forEach>

                                <c:forEach var="genre"
                                           items="${genreCodes}">

                                    <button type="button"
                                            class="content-selected-filter-chip"
                                            data-content-filter-chip
                                            data-filter-name="genreCodes"
                                            data-filter-value="${genre}">

                                        <span>
                                            <c:choose>
                                                <c:when test="${genre eq 'ACTION'}">액션</c:when>
                                                <c:when test="${genre eq 'COMEDY'}">코미디</c:when>
                                                <c:when test="${genre eq 'THRILLER'}">스릴러</c:when>
                                                <c:when test="${genre eq 'ROMANCE'}">로맨스</c:when>
                                                <c:when test="${genre eq 'CRIME'}">범죄</c:when>
                                                <c:when test="${genre eq 'ADVENTURE'}">모험</c:when>
                                                <c:when test="${genre eq 'FAMILY'}">가족</c:when>
                                                <c:when test="${genre eq 'FANTASY'}">판타지</c:when>
                                                <c:when test="${genre eq 'HISTORY'}">역사</c:when>
                                                <c:when test="${genre eq 'HORROR'}">공포</c:when>
                                                <c:when test="${genre eq 'MUSIC'}">음악</c:when>
                                                <c:when test="${genre eq 'MYSTERY'}">미스터리</c:when>
                                                <c:when test="${genre eq 'SCI_FI'}">SF</c:when>
                                                <c:when test="${genre eq 'WAR'}">전쟁</c:when>
                                                <c:when test="${genre eq 'WESTERN'}">서부</c:when>
                                                <c:otherwise><c:out value="${genre}"/></c:otherwise>
                                            </c:choose>
                                        </span>

                                        <span aria-hidden="true">×</span>
                                    </button>

                                </c:forEach>

                                <c:forEach var="provider"
                                           items="${providerIds}">

                                    <button type="button"
                                            class="content-selected-filter-chip"
                                            data-content-filter-chip
                                            data-filter-name="providerIds"
                                            data-filter-value="${provider}">
                                        <span><c:out value="${provider}"/></span>
                                        <span aria-hidden="true">×</span>
                                    </button>

                                </c:forEach>

                            </div>

                            <c:url var="clearFilterUrl"
                                   value="/content/list">
                                <c:param name="type" value="${type}"/>
                                <c:param name="sort" value="${sort}"/>
                            </c:url>

                            <a class="content-selected-filter-clear"
                               href="${clearFilterUrl}">
                                전체 해제
                            </a>

                        </c:when>

                        <c:otherwise>
                            <span class="content-selected-filter-empty">
                                현재 전체 콘텐츠를 표시하고 있습니다.
                            </span>
                        </c:otherwise>
                    </c:choose>

                </div>

                <form class="content-list-sort-form"
                      action="${pageContext.request.contextPath}/content/list"
                      method="get">

                    <input type="hidden" name="type" value="${type}">
                    <input type="hidden" name="page" value="1">

                    <c:forEach var="category"
                               items="${contentCategories}">
                        <input type="hidden"
                               name="contentCategories"
                               value="${category}">
                    </c:forEach>

                    <c:forEach var="genre"
                               items="${genreCodes}">
                        <input type="hidden"
                               name="genreCodes"
                               value="${genre}">
                    </c:forEach>

                    <c:forEach var="provider"
                               items="${providerIds}">
                        <input type="hidden"
                               name="providerIds"
                               value="${provider}">
                    </c:forEach>

                    <label for="contentSortSelect">
                        정렬
                    </label>

                    <select id="contentSortSelect"
                            name="sort"
                            data-content-sort-select>
                        <option value="popular"
                                <c:if test="${sort eq 'popular'}">selected</c:if>>
                            인기순
                        </option>
                        <option value="rating"
                                <c:if test="${sort eq 'rating'}">selected</c:if>>
                            평점순
                        </option>
                        <option value="latest"
                                <c:if test="${sort eq 'latest'}">selected</c:if>>
                            최신순
                        </option>
                        <option value="title"
                                <c:if test="${sort eq 'title'}">selected</c:if>>
                            가나다순
                        </option>
                    </select>

                </form>

            </section>

            <c:choose>

                <c:when test="${empty contentList}">

                    <section class="empty-state content-list-empty-state">
                        <strong>조건에 맞는 콘텐츠가 없습니다.</strong>
                        <p>필터 조건을 줄이거나 전체 초기화를 이용해 주세요.</p>
                    </section>

                </c:when>

                <c:otherwise>

                    <section class="content-list-card-grid"
                             aria-label="콘텐츠 목록">

                        <c:forEach var="content"
                                   items="${contentList}">

                            <!-- 장르 텍스트를 기준으로 사용자용 콘텐츠 분류를 표시합니다. -->
                            <c:set var="contentBadgeLabel" value="드라마"/>
                            <c:set var="contentBadgeClass" value="drama"/>

                            <c:choose>
                                <c:when test="${fn:contains(content.genreText, '애니메이션')}">
                                    <c:set var="contentBadgeLabel" value="애니메이션"/>
                                    <c:set var="contentBadgeClass" value="animation"/>
                                </c:when>
                                <c:when test="${fn:contains(content.genreText, '다큐멘터리')}">
                                    <c:set var="contentBadgeLabel" value="다큐멘터리"/>
                                    <c:set var="contentBadgeClass" value="documentary"/>
                                </c:when>
                                <c:when test="${content.contentType eq 'TV'
                                              and (fn:contains(content.genreText, '리얼리티')
                                                   or fn:contains(content.genreText, '토크'))}">
                                    <c:set var="contentBadgeLabel" value="예능"/>
                                    <c:set var="contentBadgeClass" value="variety"/>
                                </c:when>
                                <c:when test="${content.contentType eq 'MOVIE'}">
                                    <c:set var="contentBadgeLabel" value="영화"/>
                                    <c:set var="contentBadgeClass" value="movie"/>
                                </c:when>
                            </c:choose>

                            <article class="content-list-card">

                                <c:url var="detailUrl"
                                       value="/content/prepare">
                                    <c:param name="tmdbId"
                                             value="${content.tmdbId}"/>
                                    <c:param name="contentType"
                                             value="${content.contentType}"/>
                                </c:url>

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

                                        <span class="content-list-type-badge is-${contentBadgeClass}">
                                            <c:out value="${contentBadgeLabel}"/>
                                        </span>

                                        <c:if test="${not empty content.tmdbScore}">
                                            <span class="content-list-score-badge">
                                                <span aria-hidden="true">★</span>
                                                <fmt:formatNumber value="${content.tmdbScore}"
                                                                  pattern="0.0"/>
                                            </span>
                                        </c:if>

                                    </div>

                                    <div class="content-list-card-info">

                                        <h2>
                                            <c:out value="${content.title}"/>
                                        </h2>

                                        <div class="content-list-card-meta">
                                            <span>
                                                <c:choose>
                                                    <c:when test="${not empty content.releaseDate}">
                                                        ${fn:substring(content.releaseDate, 0, 4)}
                                                    </c:when>
                                                    <c:otherwise>
                                                        공개일 미정
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>

                                            <span><c:out value="${contentBadgeLabel}"/></span>
                                        </div>

                                        <c:if test="${not empty content.genreText}">
                                            <p class="content-list-card-genre">
                                                <c:out value="${content.genreText}"/>
                                            </p>
                                        </c:if>

                                        <c:if test="${not empty content.platformList}">
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

                                    </div>

                                </a>

                            </article>

                        </c:forEach>

                    </section>

                </c:otherwise>

            </c:choose>

            <c:if test="${totalPage > 1}">

                <nav class="content-list-pagination"
                     aria-label="콘텐츠 목록 페이지">

                    <c:set var="startPage"
                           value="${page - 2 > 1 ? page - 2 : 1}"/>

                    <c:set var="endPage"
                           value="${page + 2 < totalPage ? page + 2 : totalPage}"/>

                    <c:if test="${page > 1}">

                        <c:url var="firstPageUrl"
                               value="/content/list">
                            <c:param name="type" value="${type}"/>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="1"/>
                            <c:forEach var="category" items="${contentCategories}">
                                <c:param name="contentCategories" value="${category}"/>
                            </c:forEach>
                            <c:forEach var="genre" items="${genreCodes}">
                                <c:param name="genreCodes" value="${genre}"/>
                            </c:forEach>
                            <c:forEach var="provider" items="${providerIds}">
                                <c:param name="providerIds" value="${provider}"/>
                            </c:forEach>
                        </c:url>

                        <c:url var="previousPageUrl"
                               value="/content/list">
                            <c:param name="type" value="${type}"/>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="${page - 1}"/>
                            <c:forEach var="category" items="${contentCategories}">
                                <c:param name="contentCategories" value="${category}"/>
                            </c:forEach>
                            <c:forEach var="genre" items="${genreCodes}">
                                <c:param name="genreCodes" value="${genre}"/>
                            </c:forEach>
                            <c:forEach var="provider" items="${providerIds}">
                                <c:param name="providerIds" value="${provider}"/>
                            </c:forEach>
                        </c:url>

                        <a class="page-btn page-edge-btn"
                           href="${firstPageUrl}"
                           aria-label="첫 페이지">«</a>

                        <a class="page-btn"
                           href="${previousPageUrl}"
                           aria-label="이전 페이지">‹</a>

                    </c:if>

                    <c:forEach var="pageNumber"
                               begin="${startPage}"
                               end="${endPage}">

                        <c:url var="pageUrl"
                               value="/content/list">
                            <c:param name="type" value="${type}"/>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="${pageNumber}"/>
                            <c:forEach var="category" items="${contentCategories}">
                                <c:param name="contentCategories" value="${category}"/>
                            </c:forEach>
                            <c:forEach var="genre" items="${genreCodes}">
                                <c:param name="genreCodes" value="${genre}"/>
                            </c:forEach>
                            <c:forEach var="provider" items="${providerIds}">
                                <c:param name="providerIds" value="${provider}"/>
                            </c:forEach>
                        </c:url>

                        <c:choose>
                            <c:when test="${pageNumber == page}">
                                <span class="page-now"
                                      aria-current="page">
                                    ${pageNumber}
                                </span>
                            </c:when>
                            <c:otherwise>
                                <a class="page-btn"
                                   href="${pageUrl}">
                                    ${pageNumber}
                                </a>
                            </c:otherwise>
                        </c:choose>

                    </c:forEach>

                    <c:if test="${page < totalPage}">

                        <c:url var="nextPageUrl"
                               value="/content/list">
                            <c:param name="type" value="${type}"/>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="${page + 1}"/>
                            <c:forEach var="category" items="${contentCategories}">
                                <c:param name="contentCategories" value="${category}"/>
                            </c:forEach>
                            <c:forEach var="genre" items="${genreCodes}">
                                <c:param name="genreCodes" value="${genre}"/>
                            </c:forEach>
                            <c:forEach var="provider" items="${providerIds}">
                                <c:param name="providerIds" value="${provider}"/>
                            </c:forEach>
                        </c:url>

                        <c:url var="lastPageUrl"
                               value="/content/list">
                            <c:param name="type" value="${type}"/>
                            <c:param name="sort" value="${sort}"/>
                            <c:param name="page" value="${totalPage}"/>
                            <c:forEach var="category" items="${contentCategories}">
                                <c:param name="contentCategories" value="${category}"/>
                            </c:forEach>
                            <c:forEach var="genre" items="${genreCodes}">
                                <c:param name="genreCodes" value="${genre}"/>
                            </c:forEach>
                            <c:forEach var="provider" items="${providerIds}">
                                <c:param name="providerIds" value="${provider}"/>
                            </c:forEach>
                        </c:url>

                        <a class="page-btn"
                           href="${nextPageUrl}"
                           aria-label="다음 페이지">›</a>

                        <a class="page-btn page-edge-btn"
                           href="${lastPageUrl}"
                           aria-label="마지막 페이지">»</a>

                    </c:if>

                </nav>

            </c:if>

            <!-- 기존 우측 세로 추천 영역을 본문 하단 가로형 섹션으로 이동했습니다. -->
            <jsp:include page="/WEB-INF/views/common/contentRightSidebar.jsp"/>

        </section>

    </div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
