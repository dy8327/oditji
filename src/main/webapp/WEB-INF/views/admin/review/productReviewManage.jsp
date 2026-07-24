<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="productReview"/>
<c:set var="currentTab" value="${empty param.tab ? 'all' : param.tab}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 리뷰 관리</title>
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
                상품 리뷰 관리
            </h1>

            <p class="admin-page-desc">
                구매 상품에 등록된 전체 리뷰를 조회하고, 신고 접수된 리뷰를 확인하여
                승인(리뷰 삭제) 또는 반려 처리할 수 있습니다.
            </p>

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
                      action="${pageContext.request.contextPath}/admin/productReview/list">

                    <input type="hidden"
                           name="tab"
                           value="${currentTab}">

                    <%--
                        검색 input에 id를 추가하고 label의 for와 연결한다.
                        label은 화면에는 표시하지 않고 보조 기술에만 제공한다.
                    --%>
                    <label for="productReviewKeyword"
                           style="position:absolute;
                                  width:1px;
                                  height:1px;
                                  padding:0;
                                  margin:-1px;
                                  overflow:hidden;
                                  clip:rect(0, 0, 0, 0);
                                  white-space:nowrap;
                                  border:0;">
                        작성자 및 상품명 검색
                    </label>

                    <input type="text"
                           id="productReviewKeyword"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="작성자, 상품명 검색">

                </form>

            </div>

            <div class="card-list">

                <c:choose>

                    <c:when test="${not empty productReviewList}">

                        <c:forEach var="review"
                                   items="${productReviewList}">

                            <article class="item-card">

                                <div class="thumb">
                                    상품
                                </div>

                                <div class="item-info">

                                    <h3>

                                        ${review.nickname}

                                        <span class="meta"
                                              style="display:inline;">

                                            |

                                            <fmt:formatDate value="${review.createdAt}"
                                                            pattern="yyyy-MM-dd"/>

                                        </span>

                                    </h3>

                                    <div class="meta">

                                        <span>
                                            상품명 ${review.productName}
                                        </span>

                                        <span>
                                            별점 ${review.rating}점
                                        </span>

                                        <c:if test="${currentTab == 'report'}">

                                            <span class="badge badge-yellow">
                                                신고 ${review.reportCount}건
                                            </span>

                                        </c:if>

                                    </div>

                                    <p style="margin-top:10px;
                                              color:var(--adm-text-sub);">
                                        ${review.content}
                                    </p>

                                </div>

                                <div class="item-actions">

                                    <c:choose>

                                        <c:when test="${currentTab == 'report'}">

                                            <form action="${pageContext.request.contextPath}/admin/productReview/report/approve"
                                                  method="post"
                                                  style="display:inline;">

                                                <input type="hidden"
                                                       name="reviewNo"
                                                       value="${review.reviewNo}">

                                                <input type="hidden"
                                                       name="tab"
                                                       value="${currentTab}">

                                                <button type="submit"
                                                        class="btn btn-danger">
                                                    승인 (리뷰 삭제)
                                                </button>

                                            </form>

                                            <form action="${pageContext.request.contextPath}/admin/productReview/report/reject"
                                                  method="post"
                                                  style="display:inline;">

                                                <input type="hidden"
                                                       name="reviewNo"
                                                       value="${review.reviewNo}">

                                                <input type="hidden"
                                                       name="tab"
                                                       value="${currentTab}">

                                                <button type="submit"
                                                        class="btn btn-secondary">
                                                    반려
                                                </button>

                                            </form>

                                        </c:when>

                                        <c:otherwise>

                                            <form action="${pageContext.request.contextPath}/admin/productReview/delete"
                                                  method="post">

                                                <input type="hidden"
                                                       name="reviewNo"
                                                       value="${review.reviewNo}">

                                                <input type="hidden"
                                                       name="tab"
                                                       value="${currentTab}">

                                                <button type="submit"
                                                        class="btn btn-danger">
                                                    리뷰 삭제
                                                </button>

                                            </form>

                                        </c:otherwise>

                                    </c:choose>

                                </div>

                            </article>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <article class="item-card empty">
                            ${currentTab == 'report'
                                ? '신고 접수된 리뷰가 없습니다.'
                                : '등록된 리뷰가 없습니다.'}
                        </article>

                    </c:otherwise>

                </c:choose>

            </div>

            <div class="pagination">

                <a href="?tab=${currentTab}&page=${pagination.currentPage - 1}">
                    ‹
                </a>

                <c:forEach var="p"
                           begin="1"
                           end="${empty pagination.totalPages
                               ? 1
                               : pagination.totalPages}">

                    <a href="?tab=${currentTab}&page=${p}"
                       class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>

                <a href="?tab=${currentTab}&page=${pagination.currentPage + 1}">
                    ›
                </a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>