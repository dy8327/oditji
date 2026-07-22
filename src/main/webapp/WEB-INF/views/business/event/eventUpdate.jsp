<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 수정</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
<script defer src="${pageContext.request.contextPath}/js/business.js"></script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <a href="${pageContext.request.contextPath}/business/event/list"
           class="back-link">
            ← 뒤로가기
        </a>

        <section class="form-panel">

            <h1 class="form-title">
                이벤트 수정
            </h1>

            <!-- 수정 실패 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/business/event/update"
                  method="post"
                  enctype="multipart/form-data"
                  id="eventForm">

                <input type="hidden"
                       name="eventNo"
                       value="<c:out value='${event.eventNo}'/>">

                <div class="form-group">

                    <label class="form-label"
                           for="eventTitle">
                        이벤트명
                    </label>

                    <input class="form-input"
                           type="text"
                           id="eventTitle"
                           name="eventTitle"
                           maxlength="200"
                           value="<c:out value='${event.title}'/>"
                           required>

                </div>

                <div class="form-group">

                    <label class="form-label"
                           for="eventContent">
                        이벤트 설명
                    </label>

                    <!--
                        현재 EVENT 테이블에는 이벤트 설명 컬럼이 없으므로
                        입력값은 수정 처리 로그 확인용으로만 전달된다.
                    -->
                    <textarea class="form-textarea"
                              id="eventContent"
                              name="eventContent"
                              placeholder="이벤트에 대한 설명을 입력하세요."></textarea>

                </div>

                <div class="form-group">

                    <span class="form-label">
                        이벤트 기간
                    </span>

                    <div class="event-date-row">

                        <input class="form-input"
                               type="date"
                               id="startDate"
                               name="startDate"
                               value="<c:out value='${event.startDate}'/>"
                               required>

                        <span class="event-date-separator">
                            -
                        </span>

                        <input class="form-input"
                               type="date"
                               id="endDate"
                               name="endDate"
                               value="<c:out value='${event.endDate}'/>"
                               required>

                    </div>

                </div>

                <!-- 연결 상품 (여러 개 연결 가능) -->
                <div class="form-group">

                    <label class="form-label">
                        연결 상품
                    </label>


                    <div id="productList">

                        <c:choose>

                            <%-- 기존에 연결되어 있던 상품이 있으면 그대로 다시 그려준다 --%>
                            <c:when test="${not empty event.connectedProducts}">

                                <c:forEach var="connectedProduct"
                                           items="${event.connectedProducts}">

                                    <div class="event-product-item">

                                        <input type="hidden"
                                            name="productNoList"
                                            class="productNo"
                                            value="<c:out value='${connectedProduct.productNo}'/>">

                                        <div class="product-row">

                                            <input class="form-input productName"
                                                type="text"
                                                name="productNameList"
                                                value="<c:out value='${connectedProduct.productName}'/>"
                                                placeholder="연결할 상품을 선택하세요."
                                                readonly>

                                            <button class="btn btn-dark productSearchButton"
                                                    type="button">
                                                상품 검색
                                            </button>

                                            <label class="form-label discount-label">
                                                할인율 (%)
                                            </label>

                                            <input class="form-input productDiscountRate"
                                                type="number"
                                                name="discountRateList"
                                                min="0"
                                                max="100"
                                                value="<c:out value='${connectedProduct.discountRate}'/>">

                                            <button class="btn btn-primary addProductButton"
                                                    type="button">
                                                +
                                            </button>

                                            <button class="btn btn-dark removeProductButton"
                                                    type="button">
                                                -
                                            </button>

                                        </div>

                                        <p class="form-hint productDiscountPreview">
                                            <fmt:formatNumber value="${connectedProduct.price}" pattern="#,###"/>원 →
                                            할인율 <c:out value="${connectedProduct.discountRate}"/>%
                                        </p>

                                    </div>

                                </c:forEach>

                            </c:when>

                            <%-- 연결된 상품이 없는 경우 빈 행을 하나 보여준다 --%>
                            <c:otherwise>

                                <div class="event-product-item">

                                    <input type="hidden"
                                        name="productNoList"
                                        class="productNo">

                                    <div class="product-row">

                                        <input class="form-input productName"
                                            type="text"
                                            name="productNameList"
                                            placeholder="연결할 상품을 선택하세요."
                                            readonly>

                                        <button class="btn btn-dark productSearchButton"
                                                type="button">
                                            상품 검색
                                        </button>

                                        <label class="form-label discount-label">
                                            할인율 (%)
                                        </label>

                                        <input class="form-input productDiscountRate"
                                            type="number"
                                            name="discountRateList"
                                            min="0"
                                            max="100"
                                            value="0">

                                        <button class="btn btn-primary addProductButton"
                                                type="button">
                                            +
                                        </button>

                                    </div>

                                    <p class="form-hint productDiscountPreview">
                                        상품을 선택하면 할인 적용가가 표시됩니다.
                                    </p>

                                </div>

                            </c:otherwise>

                        </c:choose>

                    </div>

                </div>

                <div class="form-group">

                    <label class="form-label">
                        요청 상태
                    </label>

                    <!--
                        이벤트 상태는 사업자가 직접 지정할 수 없다.
                        수정 요청이 접수되면 EVENT.STATUS는 WAITING으로
                        변경되고, 관리자 승인 전에는 사용자 화면에
                        노출되지 않는다.
                        (EVENT.STATUS CHECK 제약: WAITING/APPROVED/END/REJECTED/DELETED)
                    -->
                    <input class="form-input"
                           type="text"
                           value="승인 대기"
                           readonly>

                </div>

                <!-- 이벤트 이미지 -->
                <div class="form-group">

                    <label class="form-label"
                           for="eventImage">
                        이벤트 이미지
                    </label>

                    <div class="file-box">

                        <input type="file"
                               id="eventImage"
                               name="eventImage"
                               accept=".jpg,.jpeg,.png,.gif,.webp">

                        <span id="eventImageFileName">
                            <c:choose>
                                <c:when test="${empty event.bannerImage}">
                                    선택된 파일 없음
                                </c:when>
                                <c:otherwise>
                                    기존 이미지 유지
                                </c:otherwise>
                            </c:choose>
                        </span>

                    </div>

                </div>

                <div class="submit-stack">

                    <button class="btn btn-primary"
                            type="submit">
                        이벤트 수정 요청
                    </button>

                    <a class="btn btn-dark"
                       href="${pageContext.request.contextPath}/business/event/list">
                        취소
                    </a>

                </div>

            </form>

        </section>

    </main>

</div>

<!-- =========================================================
     이벤트 연결 상품 검색 모달
========================================================= -->
<div class="product-search-modal"
     id="productSearchModal">

    <div class="product-search-modal-panel">

        <div class="product-search-modal-header">

            <h2 class="product-search-modal-title">
                이벤트 연결 상품 검색
            </h2>

            <button class="product-search-modal-close"
                    type="button"
                    id="productSearchModalClose">
                ×
            </button>

        </div>

        <div class="product-search-bar">
            <input class="form-input"
                   type="text"
                   id="productSearchKeyword"
                   placeholder="상품명, 작품명, 배우명, 상품 종류를 검색하세요.">

            <button class="btn btn-dark"
                    type="button"
                    id="productSearchResetButton">
                초기화
            </button>
        </div>

        <div class="product-search-result">

            <table class="product-search-table">

                <thead>
                    <tr>
                        <th>상품명</th>
                        <th>작품</th>
                        <th>배우</th>
                        <th>가격</th>
                        <th>상태</th>
                        <th>선택</th>
                    </tr>
                </thead>

                <tbody>

                    <c:forEach var="product"
                               items="${productList}">

                        <c:if test="${product.status ne 'DELETE_REQUESTED'}">

                            <tr class="product-search-row"
                                data-search-text="<c:out value='${product.productName} ${product.contentTitle} ${product.actorName} ${product.productType}'/>">

                                <td>
                                    <c:out value="${product.productName}"/>
                                </td>

                                <td>
                                    <c:out value="${product.contentTitle}"/>
                                </td>

                                <td>
                                    <c:out value="${product.actorName}"/>
                                </td>

                                <td>
                                    <fmt:formatNumber value="${product.price}"
                                                      pattern="#,###"/>원
                                </td>

                                <td>
                                    <c:out value="${product.status}"/>
                                </td>

                                <td>
                                    <button class="btn btn-primary product-select-button"
                                            type="button"
                                            data-product-no="<c:out value='${product.productNo}'/>"
                                            data-product-name="<c:out value='${product.productName}'/>"
                                            data-product-price="<c:out value='${product.price}'/>">
                                        선택
                                    </button>
                                </td>

                            </tr>

                        </c:if>

                    </c:forEach>

                </tbody>

            </table>

            <div class="product-search-no-result"
                 id="productSearchNoResult">
                검색 결과가 없습니다.
            </div>

        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
