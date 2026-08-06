<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<c:set var="activeMenu" value="order"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 주문 현황</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
<script defer src="${pageContext.request.contextPath}/js/business.js"></script>
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">주문 현황</h1>

        <section class="content-panel">
            <table class="data-table mobile-fit-table">
                <thead>
                    <tr>
                        <th class="col-hide-mobile">주문번호</th>
                        <th class="col-hide-mobile">주문일</th>
                        <th>상품</th>
                        <th>수량</th>
                        <th>판매금액</th>
                        <th class="col-hide-mobile">주문상태</th>
                        <th>관리</th>
                    </tr>
                </thead>

                <tbody>
                    <c:choose>
                        <c:when test="${not empty orderList}">
                            <c:forEach var="order" items="${orderList}">
                                <c:set var="totalQuantity" value="0"/>
                                <c:forEach var="item" items="${order.items}">
                                    <c:set var="totalQuantity" value="${totalQuantity + item.quantity}"/>
                                </c:forEach>

                                <%--
                                    [2단계] 모바일에서는 주문상태 뱃지 컬럼을 숨기는 대신
                                    상품명 텍스트 색상으로 상태를 표시한다(mobile-status-text는
                                    max-width:768px 미디어쿼리 안에서만 색을 입히므로 데스크톱
                                    표시는 그대로 유지된다).
                                --%>
                                <c:set var="orderStatusClass">
                                    <c:choose>
                                        <c:when test="${order.orderStatus eq 'PAID' or order.orderStatus eq 'DELIVERED'}">st-ok</c:when>
                                        <c:when test="${order.orderStatus eq 'CANCELED'}">st-reject</c:when>
                                        <c:otherwise>st-waiting</c:otherwise>
                                    </c:choose>
                                </c:set>

                                <tr>
                                    <td class="col-hide-mobile"><c:out value="${order.orderNo}"/></td>
                                    <td class="col-hide-mobile">${dt:format(order.createdAt, 'yyyy-MM-dd HH:mm')}</td>
                                    <td>
                                        <span class="mobile-status-text ${fn:trim(orderStatusClass)}">
                                            <c:choose>
                                                <c:when test="${not empty order.items}">
                                                    <c:out value="${order.items[0].productName}"/>
                                                    <c:if test="${fn:length(order.items) > 1}">
                                                        외 ${fn:length(order.items) - 1}건
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>-</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td><c:out value="${totalQuantity}"/>개</td>
                                    <td><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>원</td>
                                    <td class="col-hide-mobile">
                                        <c:choose>
                                            <c:when test="${order.orderStatus eq 'ORDERED'}">
                                                <span class="status waiting">주문 완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'PAID'}">
                                                <span class="status ok">결제 완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'PREPARING'}">
                                                <span class="status waiting">상품 준비 중</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'SHIPPING'}">
                                                <span class="status waiting">배송 중</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'DELIVERED'}">
                                                <span class="status ok">배송 완료</span>
                                            </c:when>
                                            <c:when test="${order.orderStatus eq 'CANCELED'}">
                                                <span class="status">주문 취소</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status"><c:out value="${order.orderStatus}"/></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <%-- [리팩터링] 페이지 이동 대신 모달을 연다. 목록 조회 시점에 이미
                                             주문 상세와 동일한 데이터(배송지/상품 목록 포함)를 들고 있으므로
                                             별도 서버 호출 없이 아래에서 order 단위로 미리 렌더링해 둔
                                             #orderDetailModal_해당주문번호 를 그대로 연다. --%>
                                        <button type="button"
                                                class="btn btn-dark"
                                                onclick="openModal('orderDetailModal_${order.orderNo}')">
                                            상세보기
                                        </button>
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

            <%-- [페이징 리팩터링 추가] 관리자 목록 화면과 동일한 블록 네비게이션 방식 --%>
            <c:if test="${not empty pagination
                        and pagination.totalPage > 0}">

                <div class="pagination">

                    <!-- 이전 블록 -->
                    <a href="?page=${pagination.startPage - 1}"
                       class="${!pagination.prev ? 'disabled' : ''}">
                        &laquo;
                    </a>

                    <!-- 이전 페이지 -->
                    <a href="?page=${pagination.currentPage - 1}"
                       class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                        &lsaquo;
                    </a>

                    <!-- 페이지 번호 -->
                    <c:forEach var="p"
                               begin="${pagination.startPage}"
                               end="${pagination.endPage}">

                        <a href="?page=${p}"
                           class="${pagination.currentPage == p
                               ? 'active'
                               : ''}">
                            ${p}
                        </a>

                    </c:forEach>

                    <!-- 다음 페이지 -->
                    <a href="?page=${pagination.currentPage + 1}"
                       class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                        &rsaquo;
                    </a>

                    <!-- 다음 블록 -->
                    <a href="?page=${pagination.endPage + 1}"
                       class="${!pagination.next ? 'disabled' : ''}">
                        &raquo;
                    </a>

                </div>

            </c:if>

        </section>

        <%--
            [리팩터링 추가] 주문 상세보기 모달
            orderDetail.jsp(페이지 이동 방식)에서 쓰던 마크업을 그대로 옮겨,
            주문마다 별도 모달로 미리 렌더링해 둔다. orderList 조회 시점에
            order.items / receiverName / receiverPhone / address / totalAmount가
            이미 채워져 있으므로(getBusinessOrderList == getBusinessOrderDetail과
            동일한 데이터 구성) 추가 컨트롤러 호출 없이 그대로 사용한다.
        --%>
        <c:forEach var="order" items="${orderList}">

            <div class="modal-overlay" id="orderDetailModal_${order.orderNo}">

                <div class="modal-box modal-box-lg">

                    <div class="modal-header">
                        <h3>주문 상세 정보</h3>
                        <button type="button"
                                class="modal-close"
                                onclick="closeModal('orderDetailModal_${order.orderNo}')"
                                aria-label="닫기">
                            &times;
                        </button>
                    </div>

                    <article class="item-card">
                        <div class="item-info">
                            <h3>주문번호 : ${order.orderNo}</h3>
                            <div class="meta">
                                <span>주문일 : ${dt:format(order.createdAt, 'yyyy-MM-dd HH:mm')}</span>
                            </div>
                        </div>
                    </article>

                    <%--
                        [2단계 보완] 모바일에서는 표 대신 항목별 카드로 세로 나열한다
                        (business.css의 .order-item-table 참고, thead 숨김 + 각 td
                        앞에 data-label 라벨을 붙여 표시). 데스크톱은 기존 표 그대로.
                    --%>
                    <table class="data-table order-item-table">
                        <thead>
                            <tr>
                                <th>상품명</th>
                                <th>상품번호</th>
                                <th>수량</th>
                                <th>판매단가</th>
                                <th>판매금액</th>
                                <th>상태</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${order.items}">
                                <tr>
                                    <td data-label="상품명"><c:out value="${item.productName}"/></td>
                                    <td data-label="상품번호">${item.productNo}</td>
                                    <td data-label="수량">${item.quantity}개</td>
                                    <td data-label="판매단가"><fmt:formatNumber value="${item.productPrice}" pattern="#,###"/>원</td>
                                    <td data-label="판매금액"><fmt:formatNumber value="${item.productPrice * item.quantity}" pattern="#,###"/>원</td>
                                    <td data-label="상태">
                                        <c:choose>
                                            <c:when test="${item.status eq 'PAID'}">
                                                <span class="status waiting">결제 완료</span>
                                            </c:when>
                                            <c:when test="${item.status eq 'PREPARING'}">
                                                <span class="status waiting">상품 준비 중</span>
                                            </c:when>
                                            <c:when test="${item.status eq 'SHIPPING'}">
                                                <span class="status waiting">배송 중</span>
                                            </c:when>
                                            <c:when test="${item.status eq 'DELIVERED'}">
                                                <span class="status ok">배송 완료</span>
                                            </c:when>
                                            <c:when test="${item.status eq 'CANCELED'}">
                                                <span class="status reject">취소</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status">${item.status}</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>

                    <article class="item-card">
                        <div class="item-info">
                            <h3>배송지 정보</h3>
                            <div class="meta">
                                <span>수령인 : <c:out value="${order.receiverName}"/></span>
                                <span>연락처 : <c:out value="${order.receiverPhone}"/></span>
                                <span>주소 : <c:out value="${order.address}"/></span>
                            </div>
                        </div>
                    </article>

                    <div class="summary-box">
                        <p>
                            <span>내 상품 판매금액</span>
                            <strong><fmt:formatNumber value="${order.totalAmount}" pattern="#,###"/>원</strong>
                        </p>
                    </div>

                    <div class="modal-footer">
                        <button type="button"
                                class="btn btn-outline"
                                onclick="closeModal('orderDetailModal_${order.orderNo}')">
                            닫기
                        </button>
                    </div>

                </div>

            </div>

        </c:forEach>

        <%--
            [리팩터링 추가] 알림 딥링크로 들어온 경우(openOrderNo 파라미터) 목록이
            로드된 뒤 해당 주문의 상세 모달을 자동으로 연다.
            (구 orderDetail.jsp 진입 링크를 대체 - BusinessController 참고)
        --%>
        <c:if test="${not empty param.openOrderNo}">
            <script>
                document.addEventListener("DOMContentLoaded", function () {
                    if (typeof window.openModal === "function") {
                        window.openModal("orderDetailModal_<c:out value='${param.openOrderNo}'/>");
                    }
                });
            </script>
        </c:if>

    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>