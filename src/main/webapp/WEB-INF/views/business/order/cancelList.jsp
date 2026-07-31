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
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>ODITJI | 취소/환불 관리</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
    </head>

    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <div class="business-wrap">

            <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

            <main id="mainContent" class="main-content">

                <div class="business-page-header">
                    <h1 class="business-page-title">취소/환불 관리</h1>
                    <p class="business-page-desc">고객의 요청을 확인하고 승인 또는 반려 처리하세요.</p>
                </div>

                <%--
                    =========================================================
                    [수정] 취소/환불 처리 결과 메시지
                    성공 메시지와 오류 메시지를 공통 알림 박스 형태로 표시한다.
                    기존 successMessage, errorMessage 처리 로직은 그대로 유지한다.
                    =========================================================
                --%>
                <c:if test="${not empty successMessage or not empty errorMessage}">
                    <div class="cancel-alert-area">

                        <c:if test="${not empty successMessage}">
                            <div class="cancel-alert cancel-alert-success"
                                role="alert">
                                <span class="cancel-alert-icon" aria-hidden="true">✓</span>

                                <div class="cancel-alert-content">
                                    <strong class="cancel-alert-title">처리가 완료되었습니다.</strong>
                                    <p class="cancel-alert-message">
                                        <c:out value="${successMessage}" />
                                    </p>
                                </div>
                            </div>
                        </c:if>

                        <c:if test="${not empty errorMessage}">
                            <div class="cancel-alert cancel-alert-error"
                                role="alert">
                                <span class="cancel-alert-icon" aria-hidden="true">!</span>

                                <div class="cancel-alert-content">
                                    <strong class="cancel-alert-title">처리하지 못했습니다.</strong>
                                    <p class="cancel-alert-message">
                                        <c:out value="${errorMessage}" />
                                    </p>
                                </div>
                            </div>
                        </c:if>

                    </div>
                </c:if>

                <%--
                    =========================================================
                    [취소/환불 관리 화면 정렬 개선]

                    기존 조회·승인·반려 로직은 그대로 유지하고,
                    필터와 요청 카드의 정렬을 위한 전용 클래스를 추가한다.
                    =========================================================
                --%>
                <section class="business-content-box cancel-manage-page">

                    <%-- 상태별 필터 탭 --%>
                    <nav class="tab-menu cancel-filter-tabs">
                        <a href="?status=ALL"
                           class="${empty param.status || param.status == 'ALL' ? 'active' : ''}">
                            전체
                        </a>

                        <a href="?status=WAITING"
                           class="${param.status == 'WAITING' ? 'active' : ''}">
                            처리 대기
                        </a>

                        <a href="?status=APPROVED"
                           class="${param.status == 'APPROVED' ? 'active' : ''}">
                            승인 완료
                        </a>

                        <a href="?status=REJECTED"
                           class="${param.status == 'REJECTED' ? 'active' : ''}">
                            반려
                        </a>
                    </nav>

                    <div class="card-list cancel-card-list">

                        <c:choose>

                            <c:when test="${not empty cancelList}">

                                <c:forEach var="item"
                                           items="${cancelList}">

                                    <article class="item-card cancel-request-card">

                                        <%-- 수정: 카드 제목과 상태를 같은 기준선에 배치한다. --%>
                                        <header class="cancel-card-header">
                                            <div class="cancel-card-title-area">
                                                <span class="cancel-request-type">
                                                    <c:choose>
                                                        <c:when test="${item.cancelType == 'FULL'}">
                                                            전체 주문 취소
                                                        </c:when>
                                                        <c:otherwise>
                                                            상품 부분 취소
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>

                                                <h3 class="cancel-card-title">
                                                    <c:choose>
                                                        <c:when test="${item.cancelType == 'FULL'}">
                                                            주문 전체 취소 요청
                                                        </c:when>
                                                        <c:otherwise>
                                                            <c:out value="${item.productName}"/>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </h3>
                                            </div>

                                            <span class="badge cancel-status-badge ${item.status == 'WAITING' ? 'badge-yellow' : 'badge-gray'}">
                                                <c:choose>
                                                    <c:when test="${item.status == 'WAITING'}">
                                                        처리 대기
                                                    </c:when>
                                                    <c:when test="${item.status == 'APPROVED'}">
                                                        승인 완료
                                                    </c:when>
                                                    <c:when test="${item.status == 'REJECTED'}">
                                                        반려
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${item.status}
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </header>

                                        <%-- 수정: 정보 항목을 동일한 열과 간격으로 정렬한다. --%>
                                        <div class="cancel-card-body">
                                            <dl class="cancel-info-grid">
                                                <div class="cancel-info-item">
                                                    <dt>주문번호</dt>
                                                    <dd>${item.orderNo}</dd>
                                                </div>

                                                <div class="cancel-info-item">
                                                    <dt>요청 유형</dt>
                                                    <dd>
                                                        <c:choose>
                                                            <c:when test="${item.cancelType == 'FULL'}">
                                                                전체 취소
                                                            </c:when>
                                                            <c:otherwise>
                                                                상품 부분 취소
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </dd>
                                                </div>

                                                <c:if test="${item.cancelType == 'FULL'}">
                                                    <div class="cancel-info-item">
                                                        <dt>처리 대상</dt>
                                                        <dd>내 사업자 상품 ${item.itemCount}건</dd>
                                                    </div>
                                                </c:if>

                                                <div class="cancel-info-item cancel-info-product">
                                                    <dt>상품</dt>
                                                    <dd><c:out value="${item.productName}"/></dd>
                                                </div>

                                                <div class="cancel-info-item">
                                                    <dt>수량</dt>
                                                    <dd>${item.quantity}개</dd>
                                                </div>

                                                <div class="cancel-info-item cancel-info-amount">
                                                    <dt>환불 예정 금액</dt>
                                                    <dd>
                                                        <fmt:formatNumber
                                                                value="${item.cancelAmount}"
                                                                pattern="#,###"/>원
                                                    </dd>
                                                </div>

                                                <div class="cancel-info-item cancel-info-reason">
                                                    <dt>요청 사유</dt>
                                                    <dd><c:out value="${item.reason}"/></dd>
                                                </div>

                                                <div class="cancel-info-item cancel-info-date">
                                                    <dt>요청일</dt>
                                                    <dd>
                                                        <fmt:formatDate
                                                                value="${item.createdAt}"
                                                                pattern="yyyy-MM-dd HH:mm"/>
                                                    </dd>
                                                </div>

                                                <c:if test="${not empty item.rejectReason}">
                                                    <div class="cancel-info-item cancel-info-reject-reason">
                                                        <dt>반려 사유</dt>
                                                        <dd><c:out value="${item.rejectReason}"/></dd>
                                                    </div>
                                                </c:if>
                                            </dl>
                                        </div>

                                        <c:if test="${item.status == 'WAITING'}">
                                            <%-- 수정: 승인과 반려 영역을 카드 하단의 독립된 작업 영역으로 정렬한다. --%>
                                            <footer class="cancel-card-actions">

                                                <%--
                                                    =========================================================
                                                    [취소 승인 기능 추가]

                                                    승인 시 서버에서 포트원 부분 환불을 실행한다.
                                                    =========================================================
                                                --%>
                                                <form action="${pageContext.request.contextPath}/business/cancel/approve"
                                                      method="post"
                                                      class="cancel-approve-form"
                                                      onsubmit="return confirmAndSubmit(event, '${item.cancelType == 'FULL' ? '내 사업자 상품 전체를 승인하시겠습니까? 모든 사업자의 승인 완료 후 전액 환불됩니다.' : '부분 취소 요청을 승인하고 환불하시겠습니까?'}');">

                                                    <input type="hidden"
                                                           name="cancelNo"
                                                           value="${item.cancelNo}">

                                                    <button type="submit"
                                                            class="btn btn-dark cancel-approve-button">
                                                        <c:choose>
                                                            <c:when test="${item.cancelType == 'FULL'}">
                                                                전체 취소 승인
                                                            </c:when>
                                                            <c:otherwise>
                                                                승인 및 부분 환불
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </button>
                                                </form>

                                                <%--
                                                    =========================================================
                                                    [취소 반려 사유 추가]

                                                    반려 사유를 CANCEL_REQUEST.REJECT_REASON에 저장한다.
                                                    =========================================================
                                                --%>
                                                <form action="${pageContext.request.contextPath}/business/cancel/reject"
                                                      method="post"
                                                      class="cancel-reject-form"
                                                      onsubmit="return confirmAndSubmit(event, '취소 요청을 반려하시겠습니까?');">

                                                    <input type="hidden"
                                                           name="cancelNo"
                                                           value="${item.cancelNo}">

                                                    <%--
                                                        SonarQube 접근성 이슈 대응:

                                                        반복 출력되는 반려 사유 입력창마다
                                                        취소 번호를 사용한 고유 id를 부여하고
                                                        label의 for 속성과 연결합니다.
                                                    --%>
                                                    <label for="rejectReason_${item.cancelNo}"
                                                           class="sr-only">
                                                        주문 ${item.orderNo} 취소 반려 사유
                                                    </label>

                                                    <input type="text"
                                                           id="rejectReason_${item.cancelNo}"
                                                           name="rejectReason"
                                                           maxlength="500"
                                                           placeholder="반려 사유를 입력하세요."
                                                           class="cancel-reject-input"
                                                           required>

                                                    <button type="submit"
                                                            class="btn btn-danger cancel-reject-button">
                                                        반려
                                                    </button>
                                                </form>
                                            </footer>
                                        </c:if>

                                    </article>

                                </c:forEach>

                            </c:when>

                            <c:otherwise>
                                <article class="item-card empty cancel-empty-card">
                                    취소/환불 요청 내역이 없습니다.
                                </article>
                            </c:otherwise>

                        </c:choose>

                    </div>

                </section>

            </main>

        </div>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>

</html>
