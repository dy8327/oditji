<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="order"/>
<c:set var="currentTab" value="${empty param.tab ? 'order' : param.tab}"/>
<c:set var="currentStatus" value="${empty param.status ? 'ALL' : param.status}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 주문 조회</title>
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

            <h1 class="admin-page-title">주문 조회</h1>

            <p class="admin-page-desc">
                전체 주문 및 배송 현황, 환불 요청 내역을 조회할 수 있습니다.
                배송 상태 변경, 주문 취소, 환불 승인/거절 처리는 사업자가 담당합니다.
            </p>

        </div>

        <%--
            상단 통계 카드. 각 카드는 해당 탭·상태로 바로 필터링된 목록으로 이동하는 링크이며,
            현재 선택된 탭/상태와 일치하는 카드에는 active 클래스를 준다.
            memberManage.jsp / eventManage.jsp와 동일한 .member-stat-grid 컴포넌트를 재사용한다.
        --%>
        <div class="member-stat-grid">

            <a class="stat-card ${currentTab == 'order' ? 'active' : ''}"
               href="?tab=order">
                <span>전체 주문</span>
                <strong>${orderStats.totalOrderCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'refund' && currentStatus == 'ALL' ? 'active' : ''}"
               href="?tab=refund&status=ALL">
                <span>전체 환불 요청</span>
                <strong>${orderStats.refundTotalCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'refund' && currentStatus == 'WAITING' ? 'active' : ''}"
               href="?tab=refund&status=WAITING">
                <span>환불 대기</span>
                <strong>${orderStats.refundWaitingCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'refund' && currentStatus == 'APPROVED' ? 'active' : ''}"
               href="?tab=refund&status=APPROVED">
                <span>환불 완료</span>
                <strong>${orderStats.refundApprovedCount}건</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">
                <a href="?tab=order" class="${currentTab == 'order' ? 'active' : ''}">주문 조회</a>
                <a href="?tab=refund" class="${currentTab == 'refund' ? 'active' : ''}">환불 조회</a>
            </nav>

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/order/list">
                    <input type="hidden" name="tab" value="${currentTab}">

                    <%-- 환불 조회 탭 전용 상태 필터. eventManage.jsp / productManage.jsp와 동일하게,
                         아래 탭 메뉴로도 이동할 수 있지만 키보드·스크린 리더 사용자를 위해 select로도 동일 기능을 제공한다. --%>
                    <c:if test="${currentTab == 'refund'}">
                        <label for="orderManageStatusFilter" class="sr-only">
                            처리 상태 필터
                        </label>

                        <select id="orderManageStatusFilter" name="status" class="filter-select">
                            <option value="ALL"      ${currentStatus == 'ALL' ? 'selected' : ''}>상태 전체</option>
                            <option value="WAITING"  ${currentStatus == 'WAITING' ? 'selected' : ''}>처리 대기</option>
                            <option value="APPROVED" ${currentStatus == 'APPROVED' ? 'selected' : ''}>승인 완료</option>
                            <option value="REJECTED" ${currentStatus == 'REJECTED' ? 'selected' : ''}>반려</option>
                        </select>
                    </c:if>

                    <label for="orderManageKeyword" class="sr-only">
                        주문 상품명 검색
                    </label>

                    <input type="text"
                           id="orderManageKeyword"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="상품명 검색">

                    <button type="submit" class="btn btn-dark search-btn">
                        검색
                    </button>
                </form>
            </div>

            <%-- 환불 조회 탭 전용 상태 필터. 실제 처리 결과(대기/승인/반려)만 확인하는 용도이다. --%>
            <c:if test="${currentTab == 'refund'}">
                <nav class="tab-menu" style="margin-top: 12px;">
                    <a href="?tab=refund&status=ALL&keyword=${param.keyword}"
                       class="${currentStatus == 'ALL' ? 'active' : ''}">전체</a>
                    <a href="?tab=refund&status=WAITING&keyword=${param.keyword}"
                       class="${currentStatus == 'WAITING' ? 'active' : ''}">처리 대기</a>
                    <a href="?tab=refund&status=APPROVED&keyword=${param.keyword}"
                       class="${currentStatus == 'APPROVED' ? 'active' : ''}">승인 완료</a>
                    <a href="?tab=refund&status=REJECTED&keyword=${param.keyword}"
                       class="${currentStatus == 'REJECTED' ? 'active' : ''}">반려</a>
                </nav>
            </c:if>

            <c:choose>

                <%-- 주문 조회 탭 --%>
                <c:when test="${currentTab == 'order'}">

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>주문번호</th>
                                <th>상품명</th>
                                <th>관련 콘텐츠</th>
                                <th>가격</th>
                                <th>주문상태</th>
                                <th>배송상태</th>
                                <th>관리</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty orderList}">

                                    <c:forEach var="order" items="${orderList}">

                                        <tr>

                                            <td>${order.orderNo}</td>
                                            <td>${order.productName}</td>
                                            <td>${order.contentTitle}</td>
                                            <td><fmt:formatNumber value="${order.productPrice}" pattern="#,###"/>원</td>

                                            <td>
                                                <span class="badge ${order.orderStatus == 'CANCEL_REQUEST' ? 'badge-yellow' : 'badge-gray'}">
                                                    <c:choose>
                                                        <c:when test="${order.orderStatus == 'ORDERED'}">주문 완료</c:when>
                                                        <c:when test="${order.orderStatus == 'PAID'}">결제 완료</c:when>
                                                        <c:when test="${order.orderStatus == 'PREPARING'}">상품 준비 중</c:when>
                                                        <c:when test="${order.orderStatus == 'SHIPPING'}">배송 중</c:when>
                                                        <c:when test="${order.orderStatus == 'DELIVERED'}">배송 완료</c:when>
                                                        <c:when test="${order.orderStatus == 'CONFIRMED'}">구매 확정</c:when>
                                                        <c:when test="${order.orderStatus == 'CANCELED'}">주문 취소</c:when>
                                                        <c:when test="${order.orderStatus == 'CANCEL_REQUEST'}">취소 요청 중</c:when>
                                                        <c:when test="${order.orderStatus == 'REFUNDED'}">환불 완료</c:when>
                                                        <c:otherwise>${order.orderStatus}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>

                                            <td>
                                                <span class="badge ${order.status == 'SHIPPING' ? 'badge-blue' : 'badge-gray'}">
                                                    <c:choose>
                                                        <c:when test="${order.status == 'PREPARING'}">상품 준비 중</c:when>
                                                        <c:when test="${order.status == 'SHIPPING'}">배송 중</c:when>
                                                        <c:when test="${order.status == 'DELIVERED'}">배송 완료</c:when>
                                                        <c:when test="${order.status == 'CONFIRMED'}">구매 확정</c:when>
                                                        <c:otherwise>${order.status}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>

                                            <td>
                                                <div class="item-actions">
                                                    <button type="button" class="btn btn-dark"
                                                            onclick="openOrderDetailModal(
                                                                '${order.orderNo}',
                                                                '${order.orderItemNo}',
                                                                '${order.productName}',
                                                                '${order.contentTitle}',
                                                                '${order.quantity}',
                                                                '${order.productPrice}',
                                                                '${order.totalAmount}',
                                                                '${order.orderStatus}',
                                                                '${order.status}',
                                                                '${order.receiverName}',
                                                                '${order.receiverPhone}',
                                                                '${order.address}',
                                                                '${order.trackingNumber}',
                                                                '${order.courier}',
                                                                '<fmt:formatDate value="${order.createdAt}" pattern="yyyy-MM-dd HH:mm"/>'
                                                            )">
                                                        상세보기
                                                    </button>
                                                </div>
                                            </td>

                                        </tr>

                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr>
                                        <td colspan="7">주문 내역이 없습니다.</td>
                                    </tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:when>

                <%-- 환불 조회 탭 --%>
                <c:otherwise>

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>주문번호</th>
                                <th>상품명</th>
                                <th>관련 콘텐츠</th>
                                <th>구매자</th>
                                <th>사유</th>
                                <th>처리상태</th>
                                <th>관리</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty refundList}">

                                    <c:forEach var="refund" items="${refundList}">

                                        <tr>

                                            <td>${refund.orderNo}</td>
                                            <td>${refund.productName}</td>
                                            <td>${refund.contentTitle}</td>
                                            <td>${refund.memberId}</td>
                                            <td>${refund.reason}</td>

                                            <td>
                                                <span class="badge ${refund.cancelStatus == 'WAITING' ? 'badge-yellow' : 'badge-gray'}">
                                                    <c:choose>
                                                        <c:when test="${refund.cancelStatus == 'WAITING'}">처리 대기</c:when>
                                                        <c:when test="${refund.cancelStatus == 'APPROVED'}">승인 완료</c:when>
                                                        <c:when test="${refund.cancelStatus == 'REJECTED'}">반려</c:when>
                                                        <c:otherwise>${refund.cancelStatus}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </td>

                                            <td>
                                                <div class="item-actions">
                                                    <button type="button" class="btn btn-dark"
                                                            onclick="openRefundDetailModal(
                                                                '${refund.cancelNo}',
                                                                '${refund.orderNo}',
                                                                '${refund.orderItemNo}',
                                                                '${refund.productName}',
                                                                '${refund.memberId}',
                                                                '${refund.cancelType}',
                                                                '${refund.quantity}',
                                                                '${refund.refundAmount}',
                                                                '${refund.reason}',
                                                                '${refund.cancelStatus}',
                                                                '${refund.rejectReason}',
                                                                '<fmt:formatDate value="${refund.createdAt}" pattern="yyyy-MM-dd HH:mm"/>',
                                                                '<fmt:formatDate value="${refund.processedAt}" pattern="yyyy-MM-dd HH:mm"/>'
                                                            )">
                                                        상세보기
                                                    </button>
                                                </div>
                                            </td>

                                        </tr>

                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr>
                                        <td colspan="7">환불 요청 내역이 없습니다.</td>
                                    </tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:otherwise>

            </c:choose>

            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>


                <!-- 이전 페이지 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>


                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <!-- 다음 페이지 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>


                <!-- 다음 블록 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>

        </section>

    </main>

</div>

<%-- 주문 상세 조회 팝업 (조회 전용) --%>
<div class="modal-overlay" id="orderDetailModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>주문 상세 정보</h3>
            <button type="button"
                    class="modal-close"
                    aria-label="주문 상세 팝업 닫기"
                    style="padding:0;border:0;background:transparent;font-family:inherit;"
                    onclick="closeModal('orderDetailModal')">
                &times;
            </button>
        </div>

        <div class="target-info-box">
            <p><span>주문 번호</span><strong id="detailOrderNo"></strong></p>
            <p><span>상품 명</span><strong id="detailProductName"></strong></p>
            <p><span>관련 콘텐츠</span><strong id="detailContentTitle"></strong></p>
            <p><span>수량</span><strong id="detailQuantity"></strong></p>
            <p><span>상품 가격</span><strong id="detailProductPrice"></strong></p>
            <p><span>주문 총액</span><strong id="detailTotalAmount"></strong></p>
            <p><span>주문 상태</span><strong id="detailOrderStatus"></strong></p>
            <p><span>배송 상태</span><strong id="detailDeliveryStatus"></strong></p>
        </div>

        <div class="target-info-box">
            <p><span>수령인</span><strong id="detailReceiverName"></strong></p>
            <p><span>연락처</span><strong id="detailReceiverPhone"></strong></p>
            <p><span>배송지</span><strong id="detailAddress"></strong></p>
            <p><span>택배사</span><strong id="detailCourier"></strong></p>
            <p><span>운송장 번호</span><strong id="detailTrackingNumber"></strong></p>
            <p><span>주문일</span><strong id="detailCreatedAt"></strong></p>
        </div>

        <p style="font-size: 13px; color: #888; margin-top: 8px;">
            ※ 배송 상태 변경 및 주문 취소는 사업자 페이지에서 처리됩니다.
        </p>

        <div class="modal-footer">
            <button type="button" class="btn btn-outline" onclick="closeModal('orderDetailModal')">닫기</button>
        </div>

    </div>

</div>

<%-- 환불 상세 조회 팝업 (조회 전용) --%>
<div class="modal-overlay" id="refundDetailModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>환불 상세 정보</h3>
            <button type="button"
                    class="modal-close"
                    aria-label="환불 상세 팝업 닫기"
                    style="padding:0;border:0;background:transparent;font-family:inherit;"
                    onclick="closeModal('refundDetailModal')">
                &times;
            </button>
        </div>

        <div class="target-info-box">
            <p><span>주문 번호</span><strong id="refundDetailOrderNo"></strong></p>
            <p><span>상품 명</span><strong id="refundDetailProductName"></strong></p>
            <p><span>구매자 아이디</span><strong id="refundDetailMemberId"></strong></p>
            <p><span>요청 유형</span><strong id="refundDetailType"></strong></p>
            <p><span>수량</span><strong id="refundDetailQuantity"></strong></p>
            <p><span>환불 예정 금액</span><strong id="refundDetailAmount"></strong></p>
            <p><span>처리 상태</span><strong id="refundDetailStatus"></strong></p>
            <p><span>요청일</span><strong id="refundDetailCreatedAt"></strong></p>
            <p><span>처리일</span><strong id="refundDetailProcessedAt"></strong></p>
        </div>

        <div class="form-group">
            <label class="form-label" for="refundDetailReason">환불 요청 사유</label>
            <textarea class="form-textarea" id="refundDetailReason" readonly></textarea>
        </div>

        <div class="form-group">
            <label class="form-label" for="refundDetailRejectReason">반려 사유</label>
            <textarea class="form-textarea" id="refundDetailRejectReason" readonly></textarea>
        </div>

        <p style="font-size: 13px; color: #888; margin-top: 8px;">
            ※ 환불 승인/거절 처리는 사업자 페이지에서 이루어집니다.
        </p>

        <div class="modal-footer">
            <button type="button" class="btn btn-outline" onclick="closeModal('refundDetailModal')">닫기</button>
        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
