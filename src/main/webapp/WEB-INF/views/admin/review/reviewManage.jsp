<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    탭(all/report)의 현재 선택값. param이 없으면 '전체'를 의미한다.
    eventManage.jsp / productManage.jsp와 동일하게 currentTab을 기준으로
    통계 카드 / 탭 메뉴 / 목록을 함께 다룬다.
--%>
<c:set var="currentTab" value="${empty param.tab ? 'all' : param.tab}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 콘텐츠 리뷰 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main class="main-content">

        <div class="admin-page-header">

            <a href="${pageContext.request.contextPath}/admin/main"
               class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="admin-page-title">
                콘텐츠 리뷰 관리
            </h1>

            <p class="admin-page-desc">
                작품(콘텐츠)에 등록된 전체 리뷰를 조회하고, 신고 접수된 리뷰를 확인하여
                승인(리뷰 삭제) 또는 반려 처리할 수 있습니다. 여러 건을 한 번에 선택해
                일괄 처리할 수도 있습니다.
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
                <strong>${reviewStats.totalCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'report' ? 'active' : ''}"
               href="?tab=report">
                <span>신고 접수</span>
                <strong>${reviewStats.reportCount}건</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">

                <a href="?tab=all"
                   class="${currentTab == 'all' ? 'active' : ''}">
                    전체 리뷰
                </a>

                <a href="?tab=report"
                   class="${currentTab == 'report' ? 'active' : ''}">
                    신고 내역
                </a>

            </nav>

            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}/admin/review/list">

                    <input type="hidden"
                           name="tab"
                           value="${currentTab}">

                    <label for="contentReviewKeyword"
                           class="sr-only">
                        작성자 및 콘텐츠명 검색
                    </label>

                    <input type="text"
                           id="contentReviewKeyword"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="작성자, 콘텐츠명 검색">

                    <button type="submit"
                            class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

            </div>

            <%-- 체크박스로 선택한 리뷰를 삭제(전체 리뷰 탭) 또는 신고 승인/반려(신고 내역 탭) 처리하는 폼.
                 목록 테이블 전체를 감싼다. (memberManage.jsp의 memberBulkForm과 동일한 패턴) --%>
            <form id="reviewBulkForm" method="post" action="${pageContext.request.contextPath}/admin/review/bulk">

                <input type="hidden" name="keyword" value="${param.keyword}">
                <input type="hidden" name="tab" value="${currentTab}">

                <div class="bulk-action-bar">

                    <span class="bulk-label">
                        선택한 리뷰 : <strong id="selectedReviewCount">0</strong>건
                    </span>

                    <c:choose>

                        <c:when test="${currentTab == 'report'}">

                            <button type="submit" name="action" value="approve"
                                    id="bulkApproveReviewBtn" class="btn btn-danger" disabled
                                    onclick="return confirmReviewBulkAction('승인');">
                                선택 승인 (리뷰 삭제)
                            </button>

                            <button type="submit" name="action" value="reject"
                                    id="bulkRejectReviewBtn" class="btn btn-secondary" disabled
                                    onclick="return confirmReviewBulkAction('반려');">
                                선택 반려
                            </button>

                        </c:when>

                        <c:otherwise>

                            <button type="submit" name="action" value="delete"
                                    id="bulkDeleteReviewBtn" class="btn btn-danger" disabled
                                    onclick="return confirmReviewBulkAction('삭제');">
                                선택 삭제
                            </button>

                        </c:otherwise>

                    </c:choose>

                </div>

                <table class="data-table">

                    <thead>

                        <tr>
                            <th class="checkbox-col">
                                <input type="checkbox" id="reviewCheckAll"
                                       aria-label="전체 선택"
                                       onclick="toggleAllReviews(this)">
                            </th>
                            <th>번호</th>
                            <th>작성자</th>
                            <th>콘텐츠</th>
                            <th>별점</th>
                            <th>내용</th>
                            <th>작성일</th>
                            <c:if test="${currentTab == 'report'}">
                                <th>신고 건수</th>
                            </c:if>
                            <th>관리</th>
                        </tr>

                    </thead>

                    <tbody>

                        <c:choose>

                            <c:when test="${not empty reviewList}">

                                <c:forEach var="review" items="${reviewList}">

                                    <fmt:formatDate var="reviewCreatedAtStr"
                                                    value="${review.createdAt}"
                                                    pattern="yyyy-MM-dd"/>

                                    <tr>

                                        <td class="checkbox-col">
                                            <input type="checkbox"
                                                   class="review-check"
                                                   name="reviewNos"
                                                   value="${review.reviewNo}"
                                                   aria-label="${review.reviewNo}번 리뷰 선택"
                                                   onclick="updateSelectedReviewCount()">
                                        </td>

                                        <td>${review.reviewNo}</td>
                                        <td>${review.nickname}</td>
                                        <td>${review.contentTitle}</td>
                                        <td>${review.rating}점</td>

                                        <td>
                                            <div class="table-text-clamp">
                                                ${review.content}
                                            </div>
                                        </td>

                                        <td>${reviewCreatedAtStr}</td>

                                        <c:if test="${currentTab == 'report'}">
                                            <td>
                                                <span class="badge badge-yellow">
                                                    ${review.reportCount}건
                                                </span>
                                            </td>
                                        </c:if>

                                        <td>

                                            <div class="item-actions">

                                                <%-- 리뷰 내용이 길어도 팝업으로 전체 내용을 그대로 확인할 수 있다.
                                                     따옴표/줄바꿈이 섞여도 안전하도록 onclick 인라인 문자열이 아닌
                                                     data-* 속성으로 값을 전달한다. --%>
                                                <button type="button"
                                                        class="btn btn-outline"
                                                        data-writer="${fn:escapeXml(review.nickname)}"
                                                        data-target="${fn:escapeXml(review.contentTitle)}"
                                                        data-rating="${review.rating}"
                                                        data-created-at="${reviewCreatedAtStr}"
                                                        data-content="${fn:escapeXml(review.content)}"
                                                        onclick="openReviewContentModal(this)">
                                                    내용보기
                                                </button>

                                                <c:choose>

                                                    <c:when test="${currentTab == 'report'}">

                                                        <form action="${pageContext.request.contextPath}/admin/review/report/approve"
                                                              method="post"
                                                              style="display:inline;">

                                                            <input type="hidden" name="reviewNo" value="${review.reviewNo}">
                                                            <input type="hidden" name="tab" value="${currentTab}">

                                                            <button type="submit"
                                                                    class="btn btn-danger"
                                                                    onclick="return confirm('신고를 승인하여 리뷰를 삭제하시겠습니까?');">
                                                                승인 (리뷰 삭제)
                                                            </button>

                                                        </form>

                                                        <form action="${pageContext.request.contextPath}/admin/review/report/reject"
                                                              method="post"
                                                              style="display:inline;">

                                                            <input type="hidden" name="reviewNo" value="${review.reviewNo}">
                                                            <input type="hidden" name="tab" value="${currentTab}">

                                                            <button type="submit"
                                                                    class="btn btn-secondary"
                                                                    onclick="return confirm('신고를 반려하시겠습니까?');">
                                                                반려
                                                            </button>

                                                        </form>

                                                    </c:when>

                                                    <c:otherwise>

                                                        <form action="${pageContext.request.contextPath}/admin/review/delete"
                                                              method="post"
                                                              style="display:inline;">

                                                            <input type="hidden" name="reviewNo" value="${review.reviewNo}">
                                                            <input type="hidden" name="tab" value="${currentTab}">

                                                            <button type="submit"
                                                                    class="btn btn-danger"
                                                                    onclick="return confirm('이 리뷰를 삭제하시겠습니까?');">
                                                                리뷰 삭제
                                                            </button>

                                                        </form>

                                                    </c:otherwise>

                                                </c:choose>

                                            </div>

                                        </td>

                                    </tr>

                                </c:forEach>

                            </c:when>

                            <c:otherwise>

                                <tr>
                                    <td colspan="${currentTab == 'report' ? 9 : 8}">
                                        ${currentTab == 'report'
                                            ? '신고 접수된 리뷰가 없습니다.'
                                            : '등록된 리뷰가 없습니다.'}
                                    </td>
                                </tr>

                            </c:otherwise>

                        </c:choose>

                    </tbody>

                </table>

            </form>

            <div class="pagination">

                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.currentPage - 1}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${p}"
                       class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.currentPage + 1}">›</a>

            </div>

        </section>

    </main>

</div>

<%-- 리뷰 내용 상세 팝업 (조회 전용). 표에서 2줄로 잘린 리뷰 내용을 줄바꿈까지 그대로 살려 전체 보여준다. --%>
<div class="modal-overlay" id="reviewContentModal">

    <div class="modal-box modal-box-lg">

        <div class="modal-header">
            <h3>리뷰 내용</h3>
            <button type="button"
                    class="modal-close"
                    aria-label="리뷰 내용 팝업 닫기"
                    onclick="closeModal('reviewContentModal')">
                &times;
            </button>
        </div>

        <div class="target-info-box">
            <p><span>작성자</span><strong id="reviewContentWriter"></strong></p>
            <p><span>콘텐츠</span><strong id="reviewContentTarget"></strong></p>
            <p><span>별점</span><strong id="reviewContentRating"></strong></p>
            <p><span>작성일</span><strong id="reviewContentDate"></strong></p>
        </div>

        <div class="detail-section-title">리뷰 내용</div>
        <div class="review-content-full" id="reviewContentBody"></div>

        <div class="modal-footer">
            <button type="button" class="btn btn-outline" onclick="closeModal('reviewContentModal')">닫기</button>
        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
