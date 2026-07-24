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

                    <%-- 검색 input에 id를 부여하고 숨김 label과 연결한다. --%>
                    <label for="orderManageKeyword"
                           style="position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0, 0, 0, 0);white-space:nowrap;border:0;">
                        주문 상품명 검색
                    </label>

                    <input type="text"
                           id="orderManageKeyword"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="상품명 검색">
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
            <%-- 클릭 가능한 span을 키보드 접근 가능한 button으로 변경한다. --%>
            <button type="button"
                    class="modal-close"
                    aria-label="주문 상태 변경 팝업 닫기"
                    style="padding:0;border:0;background:transparent;font-family:inherit;"
                    onclick="closeModal('orderStatusModal')">
                &times;
            </button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/order/status-update" method="post">

            <input type="hidden" name="orderNo" id="statusOrderNo">

            <div class="target-info-box">
                <p><span>주문 번호</span><strong id="statusOrderNoText"></strong></p>
                <p><span>상품 명</span><strong id="statusProductName"></strong></p>
                <p><span>현재 상태</span><strong id="statusCurrent"></strong></p>
            </div>

            <div class="form-group">
                <%-- 여러 라디오 버튼을 설명하는 제목이므로 label 대신 그룹 설명으로 연결한다. --%>
                <p class="form-label" id="orderStatusLabel">상태 변경</p>
                <div class="radio-group"
                     role="radiogroup"
                     aria-labelledby="orderStatusLabel">
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
            <%-- 환불 팝업 닫기 요소도 기본 button으로 변경한다. --%>
            <button type="button"
                    class="modal-close"
                    aria-label="환불 팝업 닫기"
                    style="padding:0;border:0;background:transparent;font-family:inherit;"
                    onclick="closeModal('refundModal')">
                &times;
            </button>
        </div>

        <div class="target-info-box">
            <p><span>주문 번호</span><strong id="refundOrderItemNo"></strong></p>
            <p><span>상품 명</span><strong id="refundProductName"></strong></p>
            <p><span>구매자 아이디</span><strong id="refundMemberId"></strong></p>
        </div>

        <div class="form-group">
            <label class="form-label" for="refundReason">환불 사유</label>
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

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>