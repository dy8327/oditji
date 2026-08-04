<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:set var="activeMenu" value="settlement" />
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 납부 내역</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
<script defer src="${pageContext.request.contextPath}/js/business.js"></script>
</head>
<body>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp" />
    <main id="mainContent" class="main-content">
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">수수료 관리</h1>
        <section class="content-panel">
            <%-- [수정] 기존 수수료 관리 화면과 동일한 탭 레이아웃 유지 --%>
            <nav class="tab-menu">
                <a href="${pageContext.request.contextPath}/business/settlement/main">이번달 수수료</a>
                <a class="active" href="${pageContext.request.contextPath}/business/settlement/complete">납부 내역</a>
                <a href="${pageContext.request.contextPath}/business/settlement/account">계좌 정보 관리</a>
            </nav>
            <div class="admin-account-box">
                <h3>월별 수수료 납부 내역</h3>
                <table class="data-table mobile-fit-table">
                    <thead>
                        <tr>
                            <th>정산 월</th>
                            <th class="col-hide-mobile">월 매출</th>
                            <th class="col-hide-mobile">주문 수</th>
                            <th class="col-hide-mobile">수수료율</th>
                            <th>수수료 금액</th>
                            <th>상태</th>
                            <th class="col-hide-mobile">처리일</th>
                            <%-- [2단계] 신규 관리 컬럼 - 읽기 전용 상세보기(새 매퍼 쿼리 불필요, settlementHistory에 이미 있는 값만 사용) --%>
                            <th>관리</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${not empty settlementHistory}">
                            <c:forEach var="item" items="${settlementHistory}" varStatus="settlementStatus">
                                <tr>
                                    <td>${item.settlementMonth}</td>
                                    <td class="col-hide-mobile"><fmt:formatNumber value="${item.monthSales}" pattern="#,##0"/>원</td>
                                    <td class="col-hide-mobile">${item.monthOrderCount}건</td>
                                    <td class="col-hide-mobile">${item.feeRate}%</td>
                                    <td><fmt:formatNumber value="${item.feeAmount}" pattern="#,##0"/>원</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${item.status eq 'APPROVED'}">납부 완료</c:when>
                                            <c:when test="${item.status eq 'REQUESTED'}">관리자 확인중</c:when>
                                            <c:when test="${item.status eq 'REJECTED'}">반려</c:when>
                                            <c:otherwise>납부 가능</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="col-hide-mobile">${empty item.settledAt ? '-' : item.settledAt}</td>
                                    <td>
                                        <%-- [2단계] 모바일에서 숨겨진 컬럼(월 매출/주문 수/수수료율/처리일)을
                                             확인할 수 있도록 읽기 전용 상세보기 모달을 연다. 데스크톱에서도
                                             동일하게 노출된다(이 화면은 기존에 관리 컬럼 자체가 없었으므로
                                             desktop-only-el / mobile-only-el 분기 없이 그대로 추가). --%>
                                        <button type="button"
                                                class="btn btn-dark"
                                                onclick="openModal('settlementDetailModal_${settlementStatus.index}')">
                                            상세보기
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise><tr><td colspan="8">수수료 납부 내역이 없습니다.</td></tr></c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <%--
            [2단계] 납부 내역 상세보기 모달 (읽기 전용)
            새 매퍼 쿼리 없이 settlementHistory에 이미 담겨 있는 값만 사용한다.
        --%>
        <c:forEach var="item" items="${settlementHistory}" varStatus="settlementStatus">

            <div class="modal-overlay" id="settlementDetailModal_${settlementStatus.index}">

                <div class="modal-box">

                    <div class="modal-header">
                        <h3>납부 내역 상세</h3>
                        <button type="button"
                                class="modal-close"
                                onclick="closeModal('settlementDetailModal_${settlementStatus.index}')"
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
                            <span class="detail-label">상태</span>
                            <p>
                                <c:choose>
                                    <c:when test="${item.status eq 'APPROVED'}">납부 완료</c:when>
                                    <c:when test="${item.status eq 'REQUESTED'}">관리자 확인중</c:when>
                                    <c:when test="${item.status eq 'REJECTED'}">반려</c:when>
                                    <c:otherwise>납부 가능</c:otherwise>
                                </c:choose>
                            </p>
                        </div>

                        <div>
                            <span class="detail-label">월 매출</span>
                            <p><fmt:formatNumber value="${item.monthSales}" pattern="#,##0"/>원</p>
                        </div>

                        <div>
                            <span class="detail-label">주문 수</span>
                            <p>${item.monthOrderCount}건</p>
                        </div>

                        <div>
                            <span class="detail-label">수수료율</span>
                            <p>${item.feeRate}%</p>
                        </div>

                        <div>
                            <span class="detail-label">수수료 금액</span>
                            <p><fmt:formatNumber value="${item.feeAmount}" pattern="#,##0"/>원</p>
                        </div>

                        <div>
                            <span class="detail-label">처리일</span>
                            <p>${empty item.settledAt ? '-' : item.settledAt}</p>
                        </div>

                    </div>

                    <div class="modal-footer">
                        <button type="button"
                                class="btn btn-outline"
                                onclick="closeModal('settlementDetailModal_${settlementStatus.index}')">
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