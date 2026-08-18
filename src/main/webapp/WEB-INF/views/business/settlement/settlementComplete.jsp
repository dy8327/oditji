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
<script defer src="${pageContext.request.contextPath}/js/business.js"></script>
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
                <table class="data-table mobile-fit-table">
                    <thead>
                        <tr>
                            <th>정산 월</th>
                            <th class="col-hide-mobile">요청일</th>
                            <th class="col-hide-mobile">판매금액</th>
                            <th class="col-hide-mobile">수수료</th>
                            <th>정산금</th>
                            <th class="col-hide-mobile">주문 수</th>
                            <th>상태</th>
                            <%--
                            =========================================================
                            [수정] 정산 반려 사유 칼럼 추가
                            지급 완료 건은 '-'를 표시하고,
                            반려된 정산 요청은 관리자 반려 사유를 표시한다.
                            =========================================================
                            --%>
                            <th class="col-hide-mobile">반려 사유</th>
                            <th class="col-hide-mobile">처리일</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty settlementHistory}">
                            <c:forEach var="item" items="${settlementHistory}">
                                <tr>
                                    <td>${item.settlementMonth}</td>
                                    <td class="col-hide-mobile">
                                        ${dt:format(item.requestedAt, 'yyyy-MM-dd')}
                                    </td>
                                    <td class="col-hide-mobile">
                                        <fmt:formatNumber value="${item.totalAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td class="col-hide-mobile">
                                        <fmt:formatNumber value="${item.feeAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.settledAmount}" pattern="#,##0"/>원
                                    </td>
                                    <td class="col-hide-mobile">${item.orderCount}건</td>
                                    <td>
                                        <c:choose>

                                            <%--
                                                =========================================================
                                                [사전 정산 요청 상태 표시 수정]

                                                PRE_REQUESTED / REQUESTED 상태는
                                                사업자 정산 내역 화면에서 모두 "정산 요청"으로 표시한다.

                                                기존 사업자 페이지의 대기 상태와 동일한
                                                노란색 status waiting 스타일을 그대로 사용한다.
                                                =========================================================
                                            --%>
                                            <c:when test="${item.status eq 'PRE_REQUESTED'
                                                    or item.status eq 'REQUESTED'}">
                                                <span class="status waiting">
                                                    정산 요청
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

                                        <%--
                                            [모바일 반응형] 요청일/판매금액/수수료/주문 수/반려
                                            사유/처리일 컬럼은 모바일에서 숨기는 대신, 이 상세보기
                                            버튼으로 #settlementDetailModal_${item.requestNo}를 연다.
                                        --%>
                                        <button type="button"
                                                class="btn btn-dark mobile-only-el"
                                                onclick="openModal('settlementDetailModal_${item.requestNo}')">
                                            상세보기
                                        </button>
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
                                    <td class="col-hide-mobile">
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
                                    <td class="col-hide-mobile">
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
                                <td colspan="9">정산 요청 내역이 없습니다.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <%--
            [모바일 반응형] 정산 요청 내역 상세보기 모달
            모바일에서 숨긴 컬럼(요청일/판매금액/수수료/주문 수/반려 사유/처리일)을
            읽기 전용으로 보여준다. 새로운 서버 호출 없이 settlementHistory에
            이미 담긴 값만 사용한다.
        --%>
        <c:forEach var="item" items="${settlementHistory}">
            <div class="modal-overlay" id="settlementDetailModal_${item.requestNo}">
                <div class="modal-box">
                    <div class="modal-header">
                        <h3>정산 요청 상세 정보</h3>
                        <button type="button"
                                class="modal-close"
                                onclick="closeModal('settlementDetailModal_${item.requestNo}')"
                                aria-label="닫기">
                            &times;
                        </button>
                    </div>

                    <div class="detail-grid">
                        <div>
                            <span class="detail-label">정산 월</span>
                            <p>${item.settlementMonth}</p>
                        </div>

                        <div>
                            <span class="detail-label">요청일</span>
                            <p>${dt:format(item.requestedAt, 'yyyy-MM-dd')}</p>
                        </div>

                        <div>
                            <span class="detail-label">판매금액</span>
                            <p><fmt:formatNumber value="${item.totalAmount}" pattern="#,##0"/>원</p>
                        </div>

                        <div>
                            <span class="detail-label">수수료</span>
                            <p><fmt:formatNumber value="${item.feeAmount}" pattern="#,##0"/>원</p>
                        </div>

                        <div>
                            <span class="detail-label">정산금</span>
                            <p><fmt:formatNumber value="${item.settledAmount}" pattern="#,##0"/>원</p>
                        </div>

                        <div>
                            <span class="detail-label">주문 수</span>
                            <p>${item.orderCount}건</p>
                        </div>

                        <div>
                            <span class="detail-label">상태</span>
                            <p>
                                <c:choose>
                                    <c:when test="${item.status eq 'PRE_REQUESTED'
                                            or item.status eq 'REQUESTED'}">
                                        <span class="status waiting">정산 요청</span>
                                    </c:when>
                                    <c:when test="${item.status eq 'DONE'}">
                                        <span class="status ok">지급 완료</span>
                                    </c:when>
                                    <c:when test="${item.status eq 'REJECTED'}">
                                        <span class="status reject">반려</span>
                                    </c:when>
                                    <c:otherwise>
                                        <c:out value="${item.status}"/>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>

                        <div>
                            <span class="detail-label">반려 사유</span>
                            <p>
                                <c:choose>
                                    <c:when test="${item.status eq 'REJECTED'
                                                    and not empty item.rejectReason}">
                                        <c:out value="${item.rejectReason}"/>
                                    </c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </p>
                        </div>

                        <div>
                            <span class="detail-label">처리일</span>
                            <p>
                                <c:choose>
                                    <c:when test="${not empty item.processedAt}">
                                        ${dt:format(item.processedAt, 'yyyy-MM-dd')}
                                    </c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </p>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button"
                                class="btn btn-outline"
                                onclick="closeModal('settlementDetailModal_${item.requestNo}')">
                            닫기
                        </button>
                    </div>
                </div>
            </div>
        </c:forEach>

    </main>
</div>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
</body>
</html>