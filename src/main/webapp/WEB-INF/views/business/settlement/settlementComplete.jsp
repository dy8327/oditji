<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>
<c:set var="activeMenu" value="settlement" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 정산 내역</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp" />
    <main id="mainContent" class="main-content">
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">정산 관리</h1>
        <section class="content-panel">
            <nav class="tab-menu">
                <a href="${pageContext.request.contextPath}/business/settlement/main">정산 요청</a>
                <a class="active" href="${pageContext.request.contextPath}/business/settlement/complete">정산 내역</a>
                <a href="${pageContext.request.contextPath}/business/settlement/account">계좌 정보 관리</a>
            </nav>

            <div class="admin-account-box">
                <h3>정산 요청 내역</h3>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>정산 월</th>
                            <th>요청일</th>
                            <th>판매금액</th>
                            <th>수수료</th>
                            <th>정산금</th>
                            <th>주문 수</th>
                            <th>상태</th>
                            <%--
                            =========================================================
                            [수정] 정산 반려 사유 칼럼 추가
                            지급 완료 건은 '-'를 표시하고,
                            반려된 정산 요청은 관리자 반려 사유를 표시한다.
                            =========================================================
                            --%>
                            <th>반려 사유</th>
                            <th>처리일</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty settlementHistory}">
                            <c:forEach var="item" items="${settlementHistory}">
                                <tr>
                                    <td>${item.settlementMonth}</td>
                                    <td>
                                        ${dt:format(item.requestedAt, 'yyyy-MM-dd')}
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.totalAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.feeAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.settledAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td>${item.orderCount}건</td>
                                    <td>
                                        <c:choose>

                                            <%--
                                                =========================================================
                                                [수정] 정산 요청 처리 중 상태

                                                기존 상태 텍스트는 유지하고
                                                사업자 페이지 공통 대기 상태 스타일을 적용합니다.
                                                =========================================================
                                            --%>
                                            <c:when test="${item.status eq 'REQUESTED'}">
                                                <span class="status waiting">
                                                    처리 중
                                                </span>
                                            </c:when>

                                            <%--
                                                =========================================================
                                                [수정] 정산 지급 완료 상태

                                                주문 현황의 '배송 완료'와 동일한
                                                공통 성공 상태 스타일을 적용합니다.
                                                =========================================================
                                            --%>
                                            <c:when test="${item.status eq 'DONE'}">
                                                <span class="status ok">
                                                    지급 완료
                                                </span>
                                            </c:when>

                                            <%--
                                                =========================================================
                                                [수정] 정산 반려 상태

                                                주문 현황의 '주문 취소'와 동일한
                                                공통 실패 상태 스타일을 적용합니다.

                                                반려 사유가 존재하면 상태 뱃지 아래에
                                                기존 글자 크기를 유지한 채 표시합니다.
                                                =========================================================
                                            --%>
                                            <c:when test="${item.status eq 'REJECTED'}">

                                                <span class="status reject">
                                                    반려
                                                </span>

                                            </c:when>

                                            <%-- 기존 예외 상태 출력 유지 --%>
                                            <c:otherwise>
                                                <c:out value="${item.status}" />
                                            </c:otherwise>

                                        </c:choose>
                                    </td>
                                    <%--
                                        =========================================================
                                        [수정] 정산 반려 사유

                                        반려(REJECTED)된 정산 요청이고 반려 사유가 존재하면
                                        관리자에게 입력받은 반려 사유를 표시한다.

                                        지급 완료 또는 반려 사유가 없는 건은
                                        취소/환불 내역 화면과 동일하게 '-'로 표시한다.
                                        =========================================================
                                    --%>
                                    <td>
                                        <c:choose>

                                            <c:when test="${item.status eq 'REJECTED'
                                                            and not empty item.rejectReason}">
                                                <c:out value="${item.rejectReason}" />
                                            </c:when>

                                            <c:otherwise>
                                                -
                                            </c:otherwise>

                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty item.processedAt}">
                                                ${dt:format(item.processedAt, 'yyyy-MM-dd')}
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="8">정산 요청 내역이 없습니다.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>