<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="settlement"/>
<c:set var="currentTab" value="${empty param.tab ? 'fee' : param.tab}"/>

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
                수수료 등급, 사업자별 정산 현황, 지급 승인, 정산 통계를 확인할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">
                <a href="?tab=fee" class="${currentTab == 'fee' ? 'active' : ''}">수수료 관리</a>
                <a href="?tab=business" class="${currentTab == 'business' ? 'active' : ''}">사업자 정산</a>
                <a href="?tab=payout" class="${currentTab == 'payout' ? 'active' : ''}">지급 승인</a>
                <a href="?tab=stats" class="${currentTab == 'stats' ? 'active' : ''}">정산 통계</a>
            </nav>

            <c:choose>

                <%-- 1. 수수료 관리 --%>
                <c:when test="${currentTab == 'fee'}">

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>수수료 등급</th>
                                <th>적용 대상</th>
                                <th>수수료율</th>
                                <th>적용 시작일</th>
                                <th>상태</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty feePolicyList}">

                                    <c:forEach var="fee" items="${feePolicyList}">
                                        <tr>
                                            <td>${fee.gradeName}</td>
                                            <td>${fee.targetLabel}</td>
                                            <td>${fee.commissionRate}%</td>
                                            <td>${fee.appliedDate}</td>
                                            <td><span class="status-ok">적용중</span></td>
                                        </tr>
                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr><td colspan="5">등록된 수수료 정책이 없습니다.</td></tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:when>

                <%-- 2. 사업자 정산 --%>
                <c:when test="${currentTab == 'business'}">

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>사업자명</th>
                                <th>총 매출</th>
                                <th>수수료</th>
                                <th>정산 예정금</th>
                                <th>정산 상태</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty settlementList}">

                                    <c:forEach var="s" items="${settlementList}">
                                        <tr>
                                            <td>${s.businessName}</td>
                                            <td>${s.totalAmount}원</td>
                                            <td>${s.feeAmount}원</td>
                                            <td>${s.settledAmount}원</td>
                                            <td>

                                                <c:choose>
                                                    <c:when test="${s.status == 'DONE'}">
                                                        <span class="status-ok">완료</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-waiting">대기</span>
                                                    </c:otherwise>
                                                </c:choose>

                                            </td>
                                        </tr>
                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr><td colspan="5">정산 내역이 없습니다.</td></tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:when>

                <%-- 3. 지급 승인 --%>
                <c:when test="${currentTab == 'payout'}">

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>사업자명</th>
                                <th>신청일</th>
                                <th>정산 금액</th>
                                <th>계좌 확인</th>
                                <th>승인</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty payoutList}">

                                    <c:forEach var="p" items="${payoutList}">
                                        <tr>
                                            <td>${p.businessName}</td>
                                            <td>${p.requestedAt}</td>
                                            <td>${p.settledAmount}원</td>
                                            <td>${p.accountChecked ? '완료' : '대기'}</td>
                                            <td>

                                                <div class="item-actions" style="justify-content:center;">

                                                    <form action="${pageContext.request.contextPath}/admin/settlement/payout-approve" method="post">
                                                        <input type="hidden" name="settlementNo" value="${p.settlementNo}">
                                                        <button type="submit" class="btn btn-success">승인</button>
                                                    </form>

                                                    <form action="${pageContext.request.contextPath}/admin/settlement/payout-reject" method="post">
                                                        <input type="hidden" name="settlementNo" value="${p.settlementNo}">
                                                        <button type="submit" class="btn btn-danger">반려</button>
                                                    </form>

                                                </div>

                                            </td>
                                        </tr>
                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr><td colspan="5">지급 승인 대기 내역이 없습니다.</td></tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:when>

                <%-- 4. 정산 통계 --%>
                <c:otherwise>

                    <div class="stat-summary-row">

                        <div class="stat-card">
                            <span>총 매출</span>
                            <strong>${settlementStats.totalSales}원</strong>
                        </div>

                        <div class="stat-card">
                            <span>총 수수료</span>
                            <strong>${settlementStats.totalFee}원</strong>
                        </div>

                        <div class="stat-card">
                            <span>정산 완료</span>
                            <strong>${settlementStats.settledAmount}원</strong>
                        </div>

                        <div class="stat-card">
                            <span>정산 대기</span>
                            <strong>${settlementStats.waitingAmount}원</strong>
                        </div>

                    </div>

                </c:otherwise>

            </c:choose>

            <c:if test="${currentTab != 'stats'}">

                <div class="pagination">

                    <a href="?tab=${currentTab}&page=${pagination.currentPage-1}">‹</a>

                    <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                        <a href="?tab=${currentTab}&page=${p}"
                           class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                    </c:forEach>

                    <a href="?tab=${currentTab}&page=${pagination.currentPage+1}">›</a>

                </div>

            </c:if>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
