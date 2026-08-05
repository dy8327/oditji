<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<c:set var="activeMenu" value="delivery"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 배송 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">
    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">
        <a href="${pageContext.request.contextPath}/business/main" class="back-link">← 뒤로가기</a>
        <h1 class="page-title">배송 관리</h1>

        <!-- 처리 성공/실패 메시지 -->
        <c:if test="${not empty successMessage}">
            <div class="form-message success-message"><c:out value="${successMessage}"/></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="form-message error-message"><c:out value="${errorMessage}"/></div>
        </c:if>

        <!-- 배송 상태 및 주문/상품 검색 영역 -->
        <section class="content-panel">
            <form class="delivery-search-form"
                  action="${pageContext.request.contextPath}/business/delivery/list"
                  method="get">
                <div class="delivery-form-field">
                    <label for="delivery-status-filter">배송 상태</label>
                    <select id="delivery-status-filter" name="status" class="form-input">
                        <option value="">전체</option>
                        <%-- [추가] 결제 직후 사업자 확인 전 단계 --%>
                        <option value="CONFIRMED" ${selectedStatus eq 'CONFIRMED' ? 'selected' : ''}>주문 확인중</option>
                        <option value="PREPARING" ${selectedStatus eq 'PREPARING' ? 'selected' : ''}>배송 준비 중</option>
                        <option value="SHIPPING" ${selectedStatus eq 'SHIPPING' ? 'selected' : ''}>배송 중</option>
                        <option value="DELIVERED" ${selectedStatus eq 'DELIVERED' ? 'selected' : ''}>배송 완료</option>
                    </select>
                </div>

                <label for="delivery-keyword" class="sr-only">배송 검색어</label>
                <input type="text"
                       id="delivery-keyword"
                       name="keyword"
                       class="form-input delivery-keyword-input"
                       value="${fn:escapeXml(keyword)}"
                       placeholder="주문번호, 주문상품번호, 상품명, 수령인 검색">

                <button type="submit" class="btn btn-dark">검색</button>
                <button type="button" class="btn btn-dark" onclick="location.href='${pageContext.request.contextPath}/business/delivery/list'">초기화</button>
            </form>
        </section>

        <!-- 주문상품 단위 배송 목록 -->
        <section class="content-panel delivery-list-panel">
            <c:choose>
                <c:when test="${not empty deliveryList}">
                    <div class="delivery-card-list">
                        <c:forEach var="delivery" items="${deliveryList}">
                            <article class="delivery-card">
                                <div class="delivery-card-head">
                                    <div>
                                        <strong>주문번호 <c:out value="${delivery.orderNo}"/></strong>
                                        <span>주문상품번호 <c:out value="${delivery.orderItemNo}"/></span>
                                    </div>
                                    <c:choose>
                                        <c:when test="${delivery.status eq 'DELIVERED'}">
                                            <span class="status ok">배송 완료</span>
                                        </c:when>
                                        <c:when test="${delivery.status eq 'SHIPPING'}">
                                            <span class="status waiting">배송 중</span>
                                        </c:when>
                                        <c:when test="${delivery.status eq 'PREPARING'}">
                                            <span class="status waiting">배송 준비 중</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status waiting">주문 확인중</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="delivery-info-grid">
                                    <div><span>상품명</span><strong><c:out value="${delivery.productName}"/></strong></div>
                                    <div><span>수량</span><strong><c:out value="${delivery.quantity}"/>개</strong></div>
                                    <div><span>판매금액</span><strong><fmt:formatNumber value="${delivery.itemTotalPrice}" pattern="#,###"/>원</strong></div>
                                    <div><span>주문일</span><strong>${dt:format(delivery.orderCreatedAt, 'yyyy-MM-dd HH:mm')}</strong></div>
                                    <div><span>수령인</span><strong><c:out value="${delivery.receiverName}"/></strong></div>
                                    <div><span>연락처</span><strong><c:out value="${delivery.receiverPhone}"/></strong></div>
                                    <div class="delivery-address"><span>배송지</span><strong><c:out value="${delivery.address}"/></strong></div>
                                </div>

                                <!-- 운송장 및 배송 상태 변경 폼 -->
                                <form class="delivery-update-form"
                                      action="${pageContext.request.contextPath}/business/delivery/update"
                                      method="post">
                                    <input type="hidden" name="orderItemNo" value="${delivery.orderItemNo}">
                                    <input type="hidden" name="returnStatus" value="${fn:escapeXml(selectedStatus)}">
                                    <input type="hidden" name="returnKeyword" value="${fn:escapeXml(keyword)}">

                                    <div class="delivery-form-field">
                                        <label for="courier-${delivery.orderItemNo}">택배사</label>
                                        <select id="courier-${delivery.orderItemNo}" name="courier" class="form-input">
                                            <option value="">택배사 선택</option>
                                            <option value="CJ대한통운" ${delivery.courier eq 'CJ대한통운' ? 'selected' : ''}>CJ대한통운</option>
                                            <option value="한진택배" ${delivery.courier eq '한진택배' ? 'selected' : ''}>한진택배</option>
                                            <option value="롯데택배" ${delivery.courier eq '롯데택배' ? 'selected' : ''}>롯데택배</option>
                                            <option value="우체국택배" ${delivery.courier eq '우체국택배' ? 'selected' : ''}>우체국택배</option>
                                            <option value="로젠택배" ${delivery.courier eq '로젠택배' ? 'selected' : ''}>로젠택배</option>
                                            <option value="쿠팡로지스틱스" ${delivery.courier eq '쿠팡로지스틱스' ? 'selected' : ''}>쿠팡로지스틱스</option>
                                        </select>
                                    </div>

                                    <div class="delivery-form-field delivery-tracking-field">
                                        <label for="tracking-${delivery.orderItemNo}">운송장 번호</label>
                                        <input id="tracking-${delivery.orderItemNo}"
                                               type="text"
                                               name="trackingNumber"
                                               class="form-input"
                                               maxlength="100"
                                               value="${fn:escapeXml(delivery.trackingNumber)}"
                                               placeholder="운송장 번호 입력">
                                    </div>

                                    <div class="delivery-form-field">
                                        <label for="status-${delivery.orderItemNo}">배송 상태</label>
                                        <select id="status-${delivery.orderItemNo}" name="status" class="form-input">
                                            <option value="PREPARING" ${delivery.status eq 'PREPARING' ? 'selected' : ''}>배송 준비 중</option>
                                            <option value="SHIPPING" ${delivery.status eq 'SHIPPING' ? 'selected' : ''}>배송 중</option>
                                            <option value="DELIVERED" ${delivery.status eq 'DELIVERED' ? 'selected' : ''}>배송 완료</option>
                                        </select>
                                    </div>

                                    <button type="submit" class="btn btn-dark delivery-save-button">저장</button>
                                </form>
                            </article>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="delivery-empty">조회된 배송 대상 주문상품이 없습니다.</div>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>