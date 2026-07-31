<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="settlement"/>

<%--
    상태(status) 필터의 현재 선택값. param이 없으면 '전체'를 의미하는 빈 문자열로 취급한다.
    eventManage.jsp와 동일한 방식.
--%>
<c:set var="currentStatus" value="${empty param.status ? '' : param.status}"/>

<%--
    기간(period) 필터의 현재 선택값. param이 없으면 '전체'를 의미하는 빈 문자열로 취급한다.
    eventManage.jsp의 기간 필터와 동일한 방식(신청일 기준).
--%>
<c:set var="currentPeriod" value="${empty param.period ? '' : param.period}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 정산 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
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
                정산 관리
            </h1>

            <p class="admin-page-desc">
                사업자가 입금 확인을 요청한 정산 건을 확인하고 확인 처리 또는 반려할 수 있습니다.
            </p>

        </div>

        <%--
            상단 통계 카드. 각 카드는 해당 상태로 바로 필터링된 목록으로 이동하는 링크이며,
            현재 선택된 상태(status)와 일치하는 카드에는 active 클래스를 준다.
            memberManage.jsp / eventManage.jsp와 동일한 .member-stat-grid 컴포넌트를 재사용한다.
        --%>
        <div class="member-stat-grid">

            <a class="stat-card ${empty currentStatus ? 'active' : ''}"
               href="?status=">
                <span>전체 정산</span>
                <strong>${settlementStats.totalCount}건</strong>
            </a>

            <a class="stat-card ${currentStatus == 'REQUESTED' ? 'active' : ''}"
               href="?status=REQUESTED">
                <span>입금 대기</span>
                <strong>${settlementStats.requestedCount}건</strong>
            </a>

            <a class="stat-card ${currentStatus == 'DONE' ? 'active' : ''}"
               href="?status=DONE">
                <span>입금 완료</span>
                <strong>${settlementStats.doneCount}건</strong>
            </a>

            <a class="stat-card ${currentStatus == 'REJECTED' ? 'active' : ''}"
               href="?status=REJECTED">
                <span>반려</span>
                <strong>${settlementStats.rejectedCount}건</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <%--
                상태 탭. memberManage.jsp의 회원 유형 탭(.tab-menu)과 동일한 컴포넌트로,
                위쪽 통계 카드와 같은 상태(status) 값을 다루지만 목록 바로 위에서도
                탭 형태로 빠르게 전환할 수 있도록 제공한다. 검색어는 그대로 유지한다.
            --%>
            <div class="tab-menu">

                <a class="${empty currentStatus ? 'active' : ''}"
                   href="?status=&period=${currentPeriod}&keyword=${param.keyword}">
                    전체
                </a>

                <a class="${currentStatus == 'REQUESTED' ? 'active' : ''}"
                   href="?status=REQUESTED&period=${currentPeriod}&keyword=${param.keyword}">
                    입금 대기
                </a>

                <a class="${currentStatus == 'DONE' ? 'active' : ''}"
                   href="?status=DONE&period=${currentPeriod}&keyword=${param.keyword}">
                    입금 완료
                </a>

                <a class="${currentStatus == 'REJECTED' ? 'active' : ''}"
                   href="?status=REJECTED&period=${currentPeriod}&keyword=${param.keyword}">
                    반려
                </a>

            </div>

            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}/admin/settlement/main">

                    <%-- 상태 필터. 위쪽 통계 카드/탭 메뉴와 같은 값(status)을 다루지만, 검색창 옆에서도
                         memberManage.jsp / eventManage.jsp와 동일하게 select로 전환할 수 있도록 제공한다. --%>
                    <label for="settlementStatusFilter" class="sr-only">
                        상태 필터
                    </label>

                    <select id="settlementStatusFilter" name="status" class="filter-select">
                        <option value=""          ${empty currentStatus ? 'selected' : ''}>상태 전체</option>
                        <option value="REQUESTED" ${currentStatus == 'REQUESTED' ? 'selected' : ''}>입금 대기</option>
                        <option value="DONE"      ${currentStatus == 'DONE' ? 'selected' : ''}>입금 완료</option>
                        <option value="REJECTED"  ${currentStatus == 'REJECTED' ? 'selected' : ''}>반려</option>
                    </select>

                    <%-- 기간 필터. 정산 신청일(신청일) 기준으로 최근 건만 좁혀 볼 때 사용한다. eventManage.jsp와 동일한 구성. --%>
                    <label for="settlementPeriodFilter" class="sr-only">
                        기간 필터
                    </label>

                    <select id="settlementPeriodFilter" name="period" class="filter-select">
                        <option value=""      ${empty currentPeriod ? 'selected' : ''}>기간 전체</option>
                        <option value="today" ${currentPeriod == 'today' ? 'selected' : ''}>오늘</option>
                        <option value="week"  ${currentPeriod == 'week' ? 'selected' : ''}>최근 7일</option>
                        <option value="month" ${currentPeriod == 'month' ? 'selected' : ''}>최근 30일</option>
                    </select>

                    <%--
                        검색 input에 고유 id를 부여하고
                        label의 for 속성과 연결한다.
                    --%>
                    <label for="settlementKeyword"
                           style="position:absolute;
                                  width:1px;
                                  height:1px;
                                  padding:0;
                                  margin:-1px;
                                  overflow:hidden;
                                  clip:rect(0, 0, 0, 0);
                                  white-space:nowrap;
                                  border:0;">
                        사업자명 검색
                    </label>

                    <input type="text"
                        id="settlementKeyword"
                        class="page-search"
                        name="keyword"
                        value="${param.keyword}"
                        placeholder="사업자명 검색">

                    <button type="submit" class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

            </div>

            <table class="data-table">

                <thead>

                    <tr>
                        <th>사업자명</th>
                        <th>정산 월</th>
                        <th>신청일</th>
                        <th>정산 예정금</th>
                        <th>입금 계좌</th>
                        <th>정산 상태</th>
                        <th>관리</th>
                    </tr>

                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty settlementList}">

                            <c:forEach var="settlement"
                                       items="${settlementList}">

                                <tr>

                                    <td>
                                        ${settlement.businessName}
                                    </td>

                                    <%-- [수정] 월별로 묶인 정산 기준 월 표시 --%>
                                    <td>${settlement.settlementMonth}</td>

                                    <td>
                                        <fmt:formatDate value="${settlement.createdAt}"
                                                        pattern="yyyy-MM-dd"/>
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${settlement.settledAmount}" pattern="#,##0"/>원
                                    </td>

                                    <td>
                                        ${settlement.bankName}
                                        ${settlement.accountNumber}
                                        (${settlement.accountHolder})
                                    </td>

                                    <td>

                                        <c:choose>

                                            <c:when test="${settlement.status == 'DONE'}">

                                                <span class="status-ok">
                                                    입금 완료
                                                </span>

                                            </c:when>

                                            <c:when test="${settlement.status == 'REJECTED'}">

                                                <span class="status-reject">
                                                    반려
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="status-waiting">
                                                    대기
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td>

                                        <div class="item-actions"
                                             style="justify-content:center;">

                                            <%-- [수정] 관리자 처리는 입금 확인 요청 상태에서만 가능 --%>
                                            <c:if test="${settlement.status == 'REQUESTED'}">

                                            <form action="${pageContext.request.contextPath}/admin/settlement/confirm"
                                                  method="post">

                                                <%-- [수정] 주문상품 1건이 아닌 사업자/정산 월 전체를 처리 --%>
                                                <input type="hidden" name="businessNo" value="${settlement.businessNo}">
                                                <input type="hidden" name="settlementMonth" value="${settlement.settlementMonth}">
                                                <input type="hidden" name="keyword" value="${param.keyword}">
                                                <input type="hidden" name="status" value="${currentStatus}">
                                                <input type="hidden" name="period" value="${currentPeriod}">
                                                <input type="hidden" name="page" value="${pagination.currentPage}">

                                                <button type="submit"
                                                        class="btn btn-success">
                                                    입금 확인
                                                </button>

                                            </form>

                                            <form action="${pageContext.request.contextPath}/admin/settlement/reject"
                                                  method="post">

                                                <%-- [수정] 주문상품 1건이 아닌 사업자/정산 월 전체를 처리 --%>
                                                <input type="hidden" name="businessNo" value="${settlement.businessNo}">
                                                <input type="hidden" name="settlementMonth" value="${settlement.settlementMonth}">
                                                <input type="hidden" name="keyword" value="${param.keyword}">
                                                <input type="hidden" name="status" value="${currentStatus}">
                                                <input type="hidden" name="period" value="${currentPeriod}">
                                                <input type="hidden" name="page" value="${pagination.currentPage}">

                                                <button type="submit"
                                                        class="btn btn-danger">
                                                    반려
                                                </button>

                                            </form>

                                            </c:if>

                                            <c:if test="${settlement.status != 'REQUESTED'}">-</c:if>

                                        </div>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>
                                <td colspan="7">
                                    입금 확인 요청 내역이 없습니다.
                                </td>
                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>


                <!-- 이전 페이지 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>


                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <!-- 다음 페이지 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>


                <!-- 다음 블록 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>