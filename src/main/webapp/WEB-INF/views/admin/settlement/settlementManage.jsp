<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

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

        <section class="admin-content-box">

            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}/admin/settlement/main">

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

                <a href="?page=${pagination.currentPage - 1}">
                    ‹
                </a>

                <c:forEach var="p"
                           begin="1"
                           end="${empty pagination.totalPages
                               ? 1
                               : pagination.totalPages}">

                    <a href="?page=${p}"
                       class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>

                <a href="?page=${pagination.currentPage + 1}">
                    ›
                </a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>