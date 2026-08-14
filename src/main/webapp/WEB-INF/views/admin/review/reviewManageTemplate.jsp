<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<%--
    탭(all/report)의 현재 선택값. param이 없으면 '전체'를 의미한다.
    eventManage.jsp / productManage.jsp와 동일하게 currentTab을 기준으로
    통계 카드 / 탭 메뉴 / 목록을 함께 다룬다.
--%>
<c:set var="currentTab" value="${empty param.tab ? 'all' : param.tab}"/>

<%-- 검색 기준(searchType)의 현재 선택값. memberManage.jsp의 searchType 필터와 동일한 방식. --%>
<c:set var="currentSearchType" value="${empty param.searchType ? '' : param.searchType}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | ${reviewPageTitle}</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main id="mainContent" class="main-content">

        <div class="admin-page-header">

            <a href="${pageContext.request.contextPath}/admin/main"
               class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="admin-page-title">
                ${reviewPageTitle}
            </h1>

            <p class="admin-page-desc">
                <c:out value="${reviewPageDescription}"/>
            </p>

        </div>

        <%-- 삭제/신고 승인·반려 처리 결과 안내 (RedirectAttributes flash message). memberManage.jsp와 동일한 컴포넌트를 사용한다. --%>
        <c:if test="${not empty message}">
            <div class="admin-flash-message">${message}</div>
        </c:if>

        <%--
            상단 통계 카드. 각 카드는 해당 탭으로 바로 이동하는 링크이며,
            현재 선택된 탭과 일치하는 카드에는 active 클래스를 준다.
            eventManage.jsp / productManage.jsp와 동일한 .member-stat-grid 컴포넌트를 재사용한다.
        --%>
        <div class="member-stat-grid">

            <a class="stat-card ${currentTab == 'all' ? 'active' : ''}"
               href="?tab=all">
                <span>전체 리뷰</span>
                <strong>${reviewPageStats.totalCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'report' ? 'active' : ''}"
               href="?tab=report">
                <span>신고 접수</span>
                <strong>${reviewPageStats.reportCount}건</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">

                <a href="?tab=all&searchType=${currentSearchType}&keyword=${param.keyword}"
                   class="${currentTab == 'all' ? 'active' : ''}">
                    전체 리뷰
                </a>

                <a href="?tab=report&searchType=${currentSearchType}&keyword=${param.keyword}"
                   class="${currentTab == 'report' ? 'active' : ''}">
                    신고 내역
                </a>

            </nav>

            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}${reviewBasePath}/list">

                    <%-- 상태 필터. 위쪽 탭 메뉴와 같은 값(tab)을 다루지만, 검색창 옆에서도
                         memberManage.jsp / eventManage.jsp와 동일하게 select로 전환할 수 있도록 제공한다. --%>
                    <label for="${reviewIdPrefix}StatusFilter"
                           class="sr-only">
                        상태 필터
                    </label>

                    <select id="${reviewIdPrefix}StatusFilter"
                            name="tab"
                            class="filter-select">
                        <option value="all"    ${currentTab == 'all' ? 'selected' : ''}>전체 리뷰</option>
                        <option value="report" ${currentTab == 'report' ? 'selected' : ''}>신고 내역</option>
                    </select>

                    <%-- 검색 기준 필터. --%>
                    <label for="${reviewIdPrefix}SearchTypeFilter"
                           class="sr-only">
                        검색 기준
                    </label>

                    <select id="${reviewIdPrefix}SearchTypeFilter"
                            name="searchType"
                            class="filter-select">
                        <option value=""        ${empty currentSearchType ? 'selected' : ''}>전체</option>
                        <option value="writer"  ${currentSearchType == 'writer' ? 'selected' : ''}>작성자</option>
                        <option value="${reviewTargetSearchValue}" ${currentSearchType == reviewTargetSearchValue ? 'selected' : ''}>${reviewTargetSearchLabel}</option>
                    </select>

                    <label for="${reviewIdPrefix}Keyword"
                           class="sr-only">
                        작성자 및 ${reviewTargetSearchLabel} 검색
                    </label>

                    <input type="text"
                           id="${reviewIdPrefix}Keyword"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="작성자, ${reviewTargetSearchLabel} 검색">

                    <button type="submit"
                            class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

            </div>

            <%-- 체크박스로 선택한 리뷰를 삭제(전체 리뷰 탭) 또는 신고 승인/반려(신고 내역 탭) 처리하는 폼.
                 목록 테이블 전체를 감싼다. (memberManage.jsp의 memberBulkForm과 동일한 패턴) --%>
            <form id="reviewBulkForm" method="post" action="${pageContext.request.contextPath}${reviewBasePath}/bulk">

                <input type="hidden" name="keyword" value="${param.keyword}">
                <input type="hidden" name="tab" value="${currentTab}">
                <input type="hidden" name="searchType" value="${currentSearchType}">
                <input type="hidden" name="page" value="${pagination.currentPage}">

                <div class="bulk-action-bar">

                    <span class="bulk-label">
                        선택한 리뷰 : <strong id="selectedReviewCount">0</strong>건
                    </span>

                    <c:choose>

                        <c:when test="${currentTab == 'report'}">

                            <button type="submit" name="action" value="approve"
                                    id="bulkApproveReviewBtn" class="btn btn-danger" disabled
                                    onclick="return confirmReviewBulkAction(event, '승인');">
                                선택 승인 (리뷰 삭제)
                            </button>

                            <button type="submit" name="action" value="reject"
                                    id="bulkRejectReviewBtn" class="btn btn-secondary" disabled
                                    onclick="return confirmReviewBulkAction(event, '반려');">
                                선택 반려
                            </button>

                        </c:when>

                        <c:otherwise>

                            <button type="submit" name="action" value="delete"
                                    id="bulkDeleteReviewBtn" class="btn btn-danger" disabled
                                    onclick="return confirmReviewBulkAction(event, '삭제');">
                                선택 삭제
                            </button>

                        </c:otherwise>

                    </c:choose>

                </div>
            </form>
                <table class="data-table">

                    <thead>

                        <tr>
                            <th class="checkbox-col">
                                <input type="checkbox" id="reviewCheckAll"
                                       aria-label="전체 선택"
                                       onchange="toggleAllReviews(this)">
                            </th>
                            <th class="col-mobile-hide">번호</th>
                            <th>작성자</th>
                            <th>${reviewTargetColumnLabel}</th>
                            <th class="col-mobile-hide">별점</th>
                            <th class="col-mobile-hide">리뷰내용</th>
                            <c:if test="${currentTab == 'report'}">
                                <th class="col-mobile-hide">신고내용</th>
                            </c:if>
                            <th class="col-mobile-hide">작성일</th>
                            <c:if test="${currentTab == 'report'}">
                                <th>신고 건수</th>
                            </c:if>
                            <th>관리</th>
                        </tr>

                    </thead>

                    <tbody>

                        <c:choose>

                            <c:when test="${not empty reviewItems}">

                                <c:forEach var="review" items="${reviewItems}">

                                    <c:set var="reviewCreatedAtStr" value="${dt:format(review.createdAt, 'yyyy-MM-dd')}"/>

                                    <c:set var="reviewTargetName"
                                           value="${reviewKind eq 'PRODUCT' ? review.productName : review.contentTitle}"/>

                                    <tr>

                                        <td class="checkbox-col">
                                            <input type="checkbox"
                                                   class="review-check"
                                                   name="reviewNos"
                                                   value="${review.reviewNo}"
                                                   form="reviewBulkForm"
                                                   aria-label="${review.reviewNo}번 리뷰 선택"
                                                   onchange="updateSelectedReviewCount()">
                                        </td>

                                        <td class="col-mobile-hide">${review.reviewNo}</td>
                                        <td><c:out value="${review.nickname}"/></td>
                                        <td><c:out value="${reviewTargetName}"/></td>
                                        <td class="col-mobile-hide">${review.rating}점</td>

                                        <td class="col-mobile-hide">
                                            <div class="table-text-clamp">
                                                <c:out value="${review.content}"/>
                                            </div>
                                        </td>

                                        <%-- 신고 탭에서는 원 리뷰와 신고 내용을 목록에서 바로 비교할 수 있게
                                             신고 사유 + 상세 내용을 별도 열로 보여준다. --%>
                                        <c:if test="${currentTab == 'report'}">
                                            <td class="col-mobile-hide">
                                                <div class="table-text-clamp report-text-clamp"
                                                     title="${fn:escapeXml(review.reportReason)}">
                                                    <c:out value="${review.reportReason}"/>
                                                </div>
                                            </td>
                                        </c:if>

                                        <td class="col-mobile-hide">${reviewCreatedAtStr}</td>

                                        <c:if test="${currentTab == 'report'}">
                                            <td>
                                                <%-- 신고 건수를 누르면 해당 리뷰의 신고 상세 모달을 바로 연다.
                                                     기존에는 단순 span이라 클릭해도 아무 동작이 없었다. --%>
                                                <button type="button"
                                                        class="badge badge-yellow report-count-trigger"
                                                        aria-label="${fn:escapeXml(review.nickname)} 리뷰 신고 ${review.reportCount}건 상세보기"
                                                        data-review-no="${review.reviewNo}"
                                                        data-writer="${fn:escapeXml(review.nickname)}"
                                                        data-target="${fn:escapeXml(reviewTargetName)}"
                                                        data-rating="${review.rating}"
                                                        data-created-at="${reviewCreatedAtStr}"
                                                        data-content="${fn:escapeXml(review.content)}"
                                                        data-report-reason="${fn:escapeXml(review.reportReason)}"
                                                        onclick="openReviewContentModal(this)">
                                                    ${review.reportCount}건
                                                </button>
                                            </td>
                                        </c:if>

                                        <td>

                                            <div class="item-actions">

                                                <%-- 신고 내역 탭에서는 목록에서 바로 승인/반려하지 않고
                                                     '내용 확인'으로 상세 모달을 연 뒤 리뷰 내용과 신고 내용을 비교하여
                                                     승인(리뷰 삭제) 또는 반려를 처리한다. 전체 리뷰 탭의 기존 삭제 흐름은 유지한다. --%>
                                                <c:choose>

                                                    <c:when test="${currentTab == 'report'}">
                                                        <button type="button"
                                                                class="btn btn-outline row-detail-trigger report-detail-trigger"
                                                                aria-label="${fn:escapeXml(review.nickname)} 리뷰 신고 내용 확인"
                                                                data-review-no="${review.reviewNo}"
                                                                data-writer="${fn:escapeXml(review.nickname)}"
                                                                data-target="${fn:escapeXml(reviewTargetName)}"
                                                                data-rating="${review.rating}"
                                                                data-created-at="${reviewCreatedAtStr}"
                                                                data-content="${fn:escapeXml(review.content)}"
                                                                data-report-reason="${fn:escapeXml(review.reportReason)}"
                                                                onclick="openReviewContentModal(this)">
                                                            내용 확인
                                                        </button>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <button type="button"
                                                                class="btn btn-outline row-detail-trigger"
                                                                aria-label="${fn:escapeXml(review.nickname)} 리뷰 내용보기"
                                                                data-review-no="${review.reviewNo}"
                                                                data-writer="${fn:escapeXml(review.nickname)}"
                                                                data-target="${fn:escapeXml(reviewTargetName)}"
                                                                data-rating="${review.rating}"
                                                                data-created-at="${reviewCreatedAtStr}"
                                                                data-content="${fn:escapeXml(review.content)}"
                                                                onclick="openReviewContentModal(this)">
                                                            내용
                                                        </button>

                                                        <button type="button"
                                                                class="btn btn-danger"
                                                                onclick="${reviewDeleteFunction}(${review.reviewNo})">
                                                            리뷰 삭제
                                                        </button>
                                                    </c:otherwise>

                                                </c:choose>

                                            </div>

                                        </td>

                                    </tr>

                                </c:forEach>

                            </c:when>

                            <c:otherwise>

                                <tr>
                                    <td colspan="${currentTab == 'report' ? 10 : 8}">
                                        ${currentTab == 'report'
                                            ? '신고 접수된 리뷰가 없습니다.'
                                            : '등록된 리뷰가 없습니다.'}
                                    </td>
                                </tr>

                            </c:otherwise>

                        </c:choose>

                    </tbody>

                </table>

            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>


                <!-- 이전 페이지 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>


                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <!-- 다음 페이지 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>


                <!-- 다음 블록 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>

        </section>

    </main>

</div>

<%-- 리뷰 내용 상세 팝업 (조회 전용). 표에서 2줄로 잘린 리뷰 내용을 줄바꿈까지 그대로 살려 전체 보여준다. --%>
<div class="modal-overlay" id="reviewContentModal">

    <div class="modal-box modal-box-lg">

        <div class="modal-header">
            <h3>${currentTab == 'report' ? '신고 상세' : '리뷰 내용'}</h3>
            <button type="button"
                    class="modal-close"
                    aria-label="${currentTab == 'report' ? '신고 상세' : '리뷰 내용'} 팝업 닫기"
                    onclick="closeModal('reviewContentModal')">
                &times;
            </button>
        </div>

        <div class="target-info-box">
            <p><span>작성자</span><strong id="reviewContentWriter"></strong></p>
            <p><span>${reviewTargetColumnLabel}</span><strong id="reviewContentTarget"></strong></p>
            <p><span>별점</span><strong id="reviewContentRating"></strong></p>
            <p><span>작성일</span><strong id="reviewContentDate"></strong></p>
        </div>

        <%-- 관리자가 신고 판단 시 원문과 신고 사유를 순서대로 비교할 수 있도록
             신고 탭에서는 '리뷰 내용'과 '신고 내용'을 명확히 분리한다. --%>
        <div class="detail-section-title">리뷰 내용</div>
        <div class="review-content-full" id="reviewContentBody"></div>

        <c:if test="${currentTab == 'report'}">
            <div class="detail-section-title">신고 내용</div>
            <div class="report-reason-list" id="reviewReportReasonBody"></div>
        </c:if>

        <%-- 내용보기 모달에서 바로 처리(삭제 / 신고 승인·반려)할 수 있도록
             폼으로 감싸고, 현재 탭에 맞는 처리 버튼만 보여준다.
             (row의 처리 버튼과 동일한 엔드포인트를 그대로 사용) --%>
        <form id="reviewContentForm"
              method="post"
              action="${pageContext.request.contextPath}${reviewBasePath}/delete">

            <input type="hidden" name="reviewNo" id="reviewContentReviewNo">
            <input type="hidden" name="tab" value="${currentTab}">
            <input type="hidden" name="searchType" value="${currentSearchType}">
            <input type="hidden" name="keyword" value="${param.keyword}">
            <input type="hidden" name="page" value="${pagination.currentPage}">

            <div class="modal-footer">

                <c:choose>

                    <c:when test="${currentTab == 'report'}">

                        <button type="submit"
                                formaction="${pageContext.request.contextPath}${reviewBasePath}/report/approve"
                                class="btn btn-danger"
                                onclick="return confirmAndSubmit(event, '신고를 승인하여 리뷰를 삭제하시겠습니까?');">
                            승인 (리뷰 삭제)
                        </button>

                        <button type="submit"
                                formaction="${pageContext.request.contextPath}${reviewBasePath}/report/reject"
                                class="btn btn-secondary"
                                onclick="return confirmAndSubmit(event, '신고를 반려하시겠습니까?');">
                            반려
                        </button>

                    </c:when>

                    <c:otherwise>

                        <button type="submit"
                                formaction="${pageContext.request.contextPath}${reviewBasePath}/delete"
                                class="btn btn-danger"
                                onclick="return confirmAndSubmit(event, '이 리뷰를 삭제하시겠습니까?');">
                            리뷰 삭제
                        </button>

                    </c:otherwise>

                </c:choose>

                <button type="button" class="btn btn-outline" onclick="closeModal('reviewContentModal')">닫기</button>

            </div>

        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
