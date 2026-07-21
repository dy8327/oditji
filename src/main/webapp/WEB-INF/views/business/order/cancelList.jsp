<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%--
    =========================================================
    [취소/환불 메뉴 활성화 수정]

    취소/환불 관리 페이지에서 사업자 사이드바의
    취소/환불 관리 메뉴가 활성화되도록 수정한다.
    =========================================================
--%>
<c:set var="activeMenu" value="cancel"/>

<!DOCTYPE html>
<html lang="ko">

    <head>
        <meta charset="UTF-8">
        <title>ODITJI | 취소/환불 관리</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
    </head>

    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <div class="business-wrap">

            <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

            <main class="main-content">

                <div class="business-page-header">
                    <h1 class="business-page-title">취소/환불 관리</h1>
                    <p class="business-page-desc">고객의 요청을 확인하고 승인 또는 반려 처리하세요.</p>
                </div>

                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success">${successMessage}</div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-error">${errorMessage}</div>
                </c:if>

                <section class="business-content-box">

                    <%-- 상태별 필터 탭 --%>
                    <nav class="tab-menu" style="margin-bottom: 24px;">
                        <a href="?status=ALL" class="${empty param.status || param.status == 'ALL' ? 'active' : ''}">전체</a>
                        <a href="?status=WAITING" class="${param.status == 'WAITING' ? 'active' : ''}">처리 대기</a>
                        <a href="?status=APPROVED" class="${param.status == 'APPROVED' ? 'active' : ''}">승인 완료</a>
                        <a href="?status=REJECTED" class="${param.status == 'REJECTED' ? 'active' : ''}">반려</a>
                    </nav>

                    <div class="card-list">
                        <c:choose>
                            <c:when test="${not empty cancelList}">
                                <c:forEach var="item" items="${cancelList}">
                                    <article class="item-card">
                                        <div class="item-info">
                                            <h3><c:out value="${item.productName}"/></h3>
                                            <div class="meta">
                                                <span>주문번호 : ${item.orderNo}</span>
                                                <span>수량 : ${item.quantity}개</span>
                                                <span>
                                                    환불 예정 금액 :
                                                    <fmt:formatNumber value="${item.cancelAmount}" pattern="#,###"/>원
                                                </span>
                                                <span>요청 사유 : <c:out value="${item.reason}"/></span>
                                                <span>요청일 : <fmt:formatDate value="${item.createdAt}" pattern="yyyy-MM-dd HH:mm"/></span>
                                                <c:if test="${not empty item.rejectReason}">
                                                    <span>반려 사유 : <c:out value="${item.rejectReason}"/></span>
                                                </c:if>
                                                <span class="badge ${item.status == 'WAITING' ? 'badge-yellow' : 'badge-gray'}">
                                                    <c:choose>
                                                        <c:when test="${item.status == 'WAITING'}">처리 대기</c:when>
                                                        <c:when test="${item.status == 'APPROVED'}">승인 완료</c:when>
                                                        <c:when test="${item.status == 'REJECTED'}">반려</c:when>
                                                        <c:otherwise>${item.status}</c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </div>

                                        <div class="item-actions">
                                            <c:if test="${item.status == 'WAITING'}">
                                                <%--
                                                    =========================================================
                                                    [취소 승인 기능 추가]
                                                    승인 시 서버에서 포트원 부분 환불을 실행한다.
                                                    =========================================================
                                                --%>
                                                <form action="${pageContext.request.contextPath}/business/cancel/approve"
                                                      method="post"
                                                      style="display:inline-block;"
                                                      onsubmit="return confirm('취소 요청을 승인하고 환불하시겠습니까?');">
                                                    <input type="hidden" name="cancelNo" value="${item.cancelNo}">
                                                    <button type="submit" class="btn btn-dark">승인 및 환불</button>
                                                </form>

                                                <%--
                                                    =========================================================
                                                    [취소 반려 사유 추가]
                                                    반려 사유를 CANCEL_REQUEST.REJECT_REASON에 저장한다.
                                                    =========================================================
                                                --%>
                                                <form action="${pageContext.request.contextPath}/business/cancel/reject"
                                                      method="post"
                                                      style="display:inline-block; margin-left: 8px;"
                                                      onsubmit="return confirm('취소 요청을 반려하시겠습니까?');">
                                                    <input type="hidden" name="cancelNo" value="${item.cancelNo}">
                                                    <input type="text"
                                                           name="rejectReason"
                                                           maxlength="500"
                                                           placeholder="반려 사유"
                                                           required
                                                           style="min-width: 220px; margin-right: 8px;">
                                                    <button type="submit" class="btn btn-danger">반려</button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </article>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <article class="item-card empty">취소/환불 요청 내역이 없습니다.</article>
                            </c:otherwise>
                        </c:choose>
                    </div>

                </section>

            </main>

        </div>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>
</html>
