<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="order"/>
<c:set var="currentTab" value="${empty param.tab ? 'order' : param.tab}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 주문 관리</title>
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

            <h1 class="admin-page-title">주문 관리</h1>

            <p class="admin-page-desc">
                전체 주문 및 배송 현황을 관리하고, 접수된 환불 요청을 처리할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">
                <a href="?tab=order" class="${currentTab == 'order' ? 'active' : ''}">주문 관리</a>
                <a href="?tab=refund" class="${currentTab == 'refund' ? 'active' : ''}">환불 관리</a>
            </nav>

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/order/list">
                    <input type="hidden" name="tab" value="${currentTab}">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="상품명 검색">
                </form>
            </div>

            <div class="card-list">

                <c:choose>

                    <%-- 주문 관리 탭 --%>
                    <c:when test="${currentTab == 'order'}">

                        <c:choose>

                            <c:when test="${not empty orderList}">

                                <c:forEach var="order" items="${orderList}">

                                    <article class="item-card">

                                        <div class="thumb">상품</div>

                                        <div class="item-info">
                                            <h3>${order.productName}</h3>
                                            <div class="meta">
                                                <span>관련 콘텐츠: ${order.contentTitle}</span>
                                                <span>주문번호: ${order.orderNo}</span>
                                                <span>가격: ${order.productPrice}원</span>
                                                <span class="badge ${order.status == 'SHIPPING' ? 'badge-blue' : 'badge-gray'}">
                                                    ${order.status}
                                                </span>
                                            </div>
                                        </div>

                                        <div class="item-actions">

                                            <button type="button" class="btn btn-dark"
                                                    onclick="openOrderStatusModal(
                                                        '${order.orderNo}',
                                                        '${order.productName}',
                                                        '${order.status}'
                                                    )">
                                                배송 상태 변경
                                            </button>

                                            <form action="${pageContext.request.contextPath}/admin/order/cancel" method="post">
                                                <input type="hidden" name="orderItemNo" value="${order.orderItemNo}">
                                                <button type="submit" class="btn btn-danger">주문 취소</button>
                                            </form>

                                        </div>

                                    </article>

                                </c:forEach>

                            </c:when>

                            <c:otherwise>
                                <article class="item-card empty">주문 내역이 없습니다.</article>
                            </c:otherwise>

                        </c:choose>

                    </c:when>

                    <%-- 환불 관리 탭 --%>
                    <c:otherwise>

                        <c:choose>

                            <c:when test="${not empty refundList}">

                                <c:forEach var="refund" items="${refundList}">

                                    <article class="item-card">

                                        <div class="thumb">상품</div>

                                        <div class="item-info">
                                            <h3>${refund.productName}</h3>
                                            <div class="meta">
                                                <span>관련 콘텐츠: ${refund.contentTitle}</span>
                                                <span>구매자: ${refund.memberId}</span>
                                                <span>${refund.reason}</span>
                                            </div>
                                        </div>

                                        <div class="item-actions">

                                            <button type="button" class="btn btn-dark"
                                                    onclick="openRefundModal(
                                                        '${refund.cancelNo}',
                                                        '${refund.orderItemNo}',
                                                        '${refund.productName}',
                                                        '${refund.memberId}',
                                                        '${refund.reason}'
                                                    )">
                                                상세보기
                                            </button>

                                        </div>

                                    </article>

                                </c:forEach>

                            </c:when>

                            <c:otherwise>
                                <article class="item-card empty">환불 요청 내역이 없습니다.</article>
                            </c:otherwise>

                        </c:choose>

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

<%-- 주문 상태 변경 팝업 --%>
<div class="modal-overlay" id="orderStatusModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>주문 상태 변경</h3>
            <span class="modal-close" onclick="closeModal('orderStatusModal')">&times;</span>
        </div>

        <form action="${pageContext.request.contextPath}/admin/order/status-update" method="post">

            <input type="hidden" name="orderNo" id="statusOrderNo">

            <div class="target-info-box">
                <p><span>주문 번호</span><strong id="statusOrderNoText"></strong></p>
                <p><span>상품 명</span><strong id="statusProductName"></strong></p>
                <p><span>현재 상태</span><strong id="statusCurrent"></strong></p>
            </div>

            <div class="form-group">
                <label class="form-label">상태 변경</label>
                <div class="radio-group">
                    <label><input type="radio" name="orderStatus" value="PREPARING">상품준비중</label>
                    <label><input type="radio" name="orderStatus" value="SHIPPING">배송중</label>
                    <label><input type="radio" name="orderStatus" value="DELIVERED">배송완료</label>
                    <label><input type="radio" name="orderStatus" value="CONFIRMED">구매확정</label>
                </div>
            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">저장</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('orderStatusModal')">닫기</button>
            </div>

        </form>

    </div>

</div>

<%-- 환불 팝업 --%>
<div class="modal-overlay" id="refundModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>환불</h3>
            <span class="modal-close" onclick="closeModal('refundModal')">&times;</span>
        </div>

        <div class="target-info-box">
            <p><span>주문 번호</span><strong id="refundOrderItemNo"></strong></p>
            <p><span>상품 명</span><strong id="refundProductName"></strong></p>
            <p><span>구매자 아이디</span><strong id="refundMemberId"></strong></p>
        </div>

        <div class="form-group">
            <label class="form-label">환불 사유</label>
            <textarea class="form-textarea" id="refundReason" readonly></textarea>
        </div>

        <form id="refundForm" action="${pageContext.request.contextPath}/admin/order/refund-approve" method="post">

            <input type="hidden" name="cancelNo" id="refundCancelNo">

            <div class="modal-footer">
                <button type="submit" class="btn btn-success">환불 승인</button>
                <button type="submit" formaction="${pageContext.request.contextPath}/admin/order/refund-reject"
                        class="btn btn-danger">환불 거절</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

function openOrderStatusModal(orderNo, productName, status) {
    document.getElementById('statusOrderNo').value = orderNo;
    document.getElementById('statusOrderNoText').textContent = orderNo;
    document.getElementById('statusProductName').textContent = productName;
    document.getElementById('statusCurrent').textContent = status;

    var radios = document.getElementsByName('orderStatus');
    for (var i = 0; i < radios.length; i++) {
        radios[i].checked = (radios[i].value === status);
    }

    document.getElementById('orderStatusModal').classList.add('open');
}

function openRefundModal(cancelNo, orderItemNo, productName, memberId, reason) {
    document.getElementById('refundCancelNo').value = cancelNo;
    document.getElementById('refundOrderItemNo').textContent = orderItemNo;
    document.getElementById('refundProductName').textContent = productName;
    document.getElementById('refundMemberId').textContent = memberId;
    document.getElementById('refundReason').value = reason;
    document.getElementById('refundModal').classList.add('open');
}
</script>

</body>
</html>
