<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="oditji" tagdir="/WEB-INF/tags/content" %>

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
          href="${pageContext.request.contextPath}/css/content-list-modern.css?v=5">

    <script>
        const contextPath = "${pageContext.request.contextPath}";
    </script>

    <script defer
            src="${pageContext.request.contextPath}/js/content.js"></script>

    <script defer
            src="${pageContext.request.contextPath}/js/favorite.js"></script>

    <script defer
            src="${pageContext.request.contextPath}/js/contentList.js?v=5"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="content-list-page">

    <div class="content-list-layout">

        <!-- 왼쪽 영역은 검색 조건만 담당하도록 유지합니다. -->
        <aside class="content-list-left-sidebar">

            <!-- 모바일 전용 필터 펼치기 버튼 (데스크톱에서는 숨김) -->
            <button type="button"
                    class="content-list-mobile-filter-toggle"
                    data-mobile-filter-toggle
                    aria-expanded="false"
                    aria-controls="contentListMobileFilterPanel">
                <span>필터</span>
                <span class="mobile-filter-toggle-arrow" aria-hidden="true">⌄</span>
            </button>

            <div class="content-list-mobile-filter-panel"
                 id="contentListMobileFilterPanel"
                 data-mobile-filter-panel>
                <jsp:include page="/WEB-INF/views/common/contentLeftSidebar.jsp"/>
            </div>

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
                                      or not empty providerIds
                                      or not empty ageRatings}">

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


                                <c:forEach var="ageRating"
                                           items="${ageRatings}">

                                    <%--
                                        관람등급 필터는 카드에서 사용하는 배지와 동일한 색상/숫자를 사용합니다.
                                        값 자체는 기존 ageRatings 값을 그대로 유지하므로 검색 조건에는 영향이 없습니다.
                                    --%>
                                    <c:set var="selectedAgeBadgeLabel" value="?"/>
                                    <c:set var="selectedAgeBadgeClass" value="unknown"/>

                                    <c:choose>
                                        <c:when test="${ageRating eq '전체 관람가'}">
                                            <c:set var="selectedAgeBadgeLabel" value="ALL"/>
                                            <c:set var="selectedAgeBadgeClass" value="all"/>
                                        </c:when>
                                        <c:when test="${ageRating eq '7세 이상 관람가'}">
                                            <c:set var="selectedAgeBadgeLabel" value="7"/>
                                            <c:set var="selectedAgeBadgeClass" value="age7"/>
                                        </c:when>
                                        <c:when test="${ageRating eq '12세 이상 관람가'}">
                                            <c:set var="selectedAgeBadgeLabel" value="12"/>
                                            <c:set var="selectedAgeBadgeClass" value="age12"/>
                                        </c:when>
                                        <c:when test="${ageRating eq '15세 이상 관람가'}">
                                            <c:set var="selectedAgeBadgeLabel" value="15"/>
                                            <c:set var="selectedAgeBadgeClass" value="age15"/>
                                        </c:when>
                                        <c:when test="${ageRating eq '청소년 관람불가'}">
                                            <c:set var="selectedAgeBadgeLabel" value="19"/>
                                            <c:set var="selectedAgeBadgeClass" value="adult"/>
                                        </c:when>
                                    </c:choose>

                                    <button type="button"
                                            class="content-selected-filter-chip"
                                            data-content-filter-chip
                                            data-filter-name="ageRatings"
                                            data-filter-value="${ageRating}">
                                        <span class="age-rating-badge is-${selectedAgeBadgeClass}"
                                              aria-hidden="true">
                                            <c:out value="${selectedAgeBadgeLabel}"/>
                                        </span>
                                        <span><c:out value="${ageRating}"/></span>
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

                    <c:forEach var="ageRating"
                               items="${ageRatings}">
                        <input type="hidden"
                               name="ageRatings"
                               value="${ageRating}">
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

                            <oditji:contentCard content="${content}"
                                                variant="grid" />

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

                    <%--
                        [리팩터링] type/sort/필터 조건은 페이지 번호만 바뀔 뿐 매번 동일하므로,
                        page 파라미터를 뺀 공통 쿼리를 한 번만 만들어 재사용합니다.
                        (기존에는 첫/이전/번호/다음/마지막 페이지마다 동일한 c:url + c:forEach 블록이
                        5번 반복되어 있었습니다.)
                    --%>
                    <c:url var="pageBaseUrl"
                           value="/content/list">
                        <c:param name="type" value="${type}"/>
                        <c:param name="sort" value="${sort}"/>
                        <c:forEach var="category" items="${contentCategories}">
                            <c:param name="contentCategories" value="${category}"/>
                        </c:forEach>
                        <c:forEach var="genre" items="${genreCodes}">
                            <c:param name="genreCodes" value="${genre}"/>
                        </c:forEach>
                        <c:forEach var="provider" items="${providerIds}">
                            <c:param name="providerIds" value="${provider}"/>
                        </c:forEach>
                        <c:forEach var="ageRating" items="${ageRatings}">
                            <c:param name="ageRatings" value="${ageRating}"/>
                        </c:forEach>
                    </c:url>

                    <c:set var="pageUrlPrefix"
                           value="${fn:contains(pageBaseUrl, '?') ? pageBaseUrl.concat('&page=') : pageBaseUrl.concat('?page=')}"/>

                    <c:if test="${page > 1}">

                        <a class="page-btn page-edge-btn"
                           href="${pageUrlPrefix}1"
                           aria-label="첫 페이지">«</a>

                        <a class="page-btn"
                           href="${pageUrlPrefix}${page - 1}"
                           aria-label="이전 페이지">‹</a>

                    </c:if>

                    <c:forEach var="pageNumber"
                               begin="${startPage}"
                               end="${endPage}">

                        <c:choose>
                            <c:when test="${pageNumber == page}">
                                <span class="page-now"
                                      aria-current="page">
                                    ${pageNumber}
                                </span>
                            </c:when>
                            <c:otherwise>
                                <a class="page-btn"
                                   href="${pageUrlPrefix}${pageNumber}">
                                    ${pageNumber}
                                </a>
                            </c:otherwise>
                        </c:choose>

                    </c:forEach>

                    <c:if test="${page < totalPage}">

                        <a class="page-btn"
                           href="${pageUrlPrefix}${page + 1}"
                           aria-label="다음 페이지">›</a>

                        <a class="page-btn page-edge-btn"
                           href="${pageUrlPrefix}${totalPage}"
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
