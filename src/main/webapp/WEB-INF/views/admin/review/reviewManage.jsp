<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="review"/>
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

            <a href="${pageContext.request.contextPath}/admin/main" class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="admin-page-title">콘텐츠 리뷰 관리</h1>

            <p class="admin-page-desc">
                작품(콘텐츠)에 등록된 전체 리뷰를 조회하고, 신고 접수된 리뷰를 확인하여 삭제 처리할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">
                <a href="?tab=all" class="${currentTab == 'all' ? 'active' : ''}">전체 리뷰</a>
                <a href="?tab=report" class="${currentTab == 'report' ? 'active' : ''}">신고 내역</a>
            </nav>

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/review/list">
                    <input type="hidden" name="tab" value="${currentTab}">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="작성자, 콘텐츠명 검색">
                </form>
            </div>

            <div class="card-list">

                <c:choose>

                    <c:when test="${not empty reviewList}">

                        <c:forEach var="review" items="${reviewList}">

                            <article class="item-card">

                                <div class="item-info">

                                    <h3>
                                        ${review.nickname}
                                        <span class="meta" style="display:inline;">| ${review.createdAt}</span>
                                    </h3>

                                    <div class="meta">
                                        <span>별점 ${review.rating}점</span>
                                        <c:if test="${currentTab == 'report'}">
                                            <span class="badge badge-yellow">신고 ${review.reportCount}건</span>
                                        </c:if>
                                    </div>

                                    <p style="margin-top:10px; color:var(--adm-text-sub);">
                                        ${review.content}
                                    </p>

                                    <div class="meta" style="margin-top:10px;">
                                        <span>👍 추천 ${review.likeCount}</span>
                                        <span>💬 댓글 ${review.commentCount}</span>
                                    </div>

                                </div>

                                <div class="item-actions">
                                    <form action="${pageContext.request.contextPath}/admin/review/delete" method="post">
                                        <input type="hidden" name="reviewNo" value="${review.reviewNo}">
                                        <input type="hidden" name="tab" value="${currentTab}">
                                        <button type="submit" class="btn btn-danger">리뷰 삭제</button>
                                    </form>
                                </div>

                            </article>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>
                        <article class="item-card empty">
                            ${currentTab == 'report' ? '신고 접수된 리뷰가 없습니다.' : '등록된 리뷰가 없습니다.'}
                        </article>
                    </c:otherwise>

                </c:choose>

            </div>

            <div class="pagination">

                <a href="?tab=${currentTab}&page=${pagination.currentPage-1}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?tab=${currentTab}&page=${p}"
                       class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?tab=${currentTab}&page=${pagination.currentPage+1}">›</a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
