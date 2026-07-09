<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="settlement"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 정산 관리</title>
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

            <h1 class="admin-page-title">정산 관리</h1>

            <p class="admin-page-desc">
                사업자가 입금 확인을 요청한 정산 건을 확인하고 확인 처리 또는 반려할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/settlement/main">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="사업자명 검색">
                </form>
            </div>

            <table class="data-table">

                <thead>
                    <tr>
                        <th>사업자명</th>
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

                            <c:forEach var="s" items="${settlementList}">
                                <tr>
                                    <td>${s.businessName}</td>
                                    <td>${s.createdAt}</td>
                                    <td>${s.settledAmount}원</td>
                                    <td>${s.bankName} ${s.accountNumber} (${s.accountHolder})</td>
                                    <td>

                                        <c:choose>
                                            <c:when test="${s.status == 'DONE'}">
                                                <span class="status-ok">입금 완료</span>
                                            </c:when>
                                            <c:when test="${s.status == 'REJECTED'}">
                                                <span class="status-reject">반려</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-waiting">대기</span>
                                            </c:otherwise>
                                        </c:choose>

                                    </td>
                                    <td>

                                        <div class="item-actions" style="justify-content:center;">

                                            <form action="${pageContext.request.contextPath}/admin/settlement/confirm" method="post">
                                                <input type="hidden" name="settlementNo" value="${s.settlementNo}">
                                                <button type="submit" class="btn btn-success">입금 확인</button>
                                            </form>

                                            <form action="${pageContext.request.contextPath}/admin/settlement/reject" method="post">
                                                <input type="hidden" name="settlementNo" value="${s.settlementNo}">
                                                <button type="submit" class="btn btn-danger">반려</button>
                                            </form>

                                        </div>

                                    </td>
                                </tr>
                            </c:forEach>

                        </c:when>

                        <c:otherwise>
                            <tr><td colspan="6">입금 확인 요청 내역이 없습니다.</td></tr>
                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <a href="?page=${pagination.currentPage-1}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?page=${p}" class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?page=${pagination.currentPage+1}">›</a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
