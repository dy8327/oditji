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

<style>
/*
 * =========================================================
 * 이벤트 수정 화면 상품 검색 모달
 *
 * 기존 business.css 레이아웃을 유지하면서
 * 현재 페이지에서만 사용하는 추가 스타일이다.
 * =========================================================
 */
.product-search-modal {
    display: none;
    position: fixed;
    inset: 0;
    z-index: 1000;
    align-items: center;
    justify-content: center;
    padding: 30px;
    box-sizing: border-box;
    background: rgba(0, 0, 0, 0.72);
}

.product-search-modal.active {
    display: flex;
}

.product-search-modal-panel {
    width: 100%;
     max-width: 1000px;
    max-height: 82vh;
    padding: 28px;
    border: 1px solid #3b3e46;
    border-radius: 12px;
    box-sizing: border-box;
    overflow: hidden;
    background: #25272b;
}

.product-search-modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;
}

.product-search-modal-title {
    margin: 0;
    color: #ffffff;
    font-size: 24px;
}

.product-search-modal-close {
    border: 0;
    background: transparent;
    color: #ffffff;
    font-size: 28px;
    cursor: pointer;
}

.product-search-bar {
    display: flex;
    gap: 10px;
    margin-bottom: 20px;
}

.product-search-bar .form-input {
    flex: 1;
    min-width: 0;
}

.product-search-result {
    max-height: 52vh;
    overflow-y: auto;
    border: 1px solid #3b3e46;
    border-radius: 10px;
}

.product-search-table {
    width: 100%;
    border-collapse: collapse;
    background: #202125;
}

.product-search-table th,
.product-search-table td {
    padding: 13px 12px;
    border-bottom: 1px solid #343740;
    text-align: left;
}

.product-search-table th {
    position: sticky;
    top: 0;
    background: #292b30;
    color: #c8ccd4;
}

.product-search-table td {
    color: #f1f2f4;
}

.product-search-no-result {
    display: none;
    padding: 40px 20px;
    color: #a7acb5;
    text-align: center;
}

/*
 * =========================================================
 * 연결 상품 / 할인율 행 레이아웃
 *
 * 상품 검색 행과 할인율 행의 입력창·버튼 박스 크기를
 * 동일하게 맞추고, +/- 버튼을 할인율 입력 오른쪽에 배치한다.
 * 화면에 미리 그려지는 행과 business.js가 동적으로
 * 추가하는 행이 항상 같은 구조/크기를 갖도록 한다.
 * =========================================================
 */
.product-item {
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #3b3e46;
}

.product-item:last-child {
    margin-bottom: 0;
    padding-bottom: 0;
    border-bottom: 0;
}

.input-with-btn,
.discount-controls {
    display: flex;
    align-items: center;
    gap: 10px;
}

.input-with-btn {
    margin-bottom: 10px;
}

.input-with-btn .productName {
    flex: 1;
    min-width: 0;
}

.discount-row .form-label {
    flex-shrink: 0;
}

.discount-controls .productDiscountRate {
    flex: 1;
    min-width: 0;
}

.input-with-btn .btn,
.discount-controls .btn {
    flex-shrink: 0;
    height: 44px;
    padding: 0 16px;
    box-sizing: border-box;
}

.addProductButton,
.removeProductButton {
    width: 44px;
    padding: 0;
    text-align: center;
}

.discount-row .form-hint {
    margin: 8px 0 0;
    color: #a7acb5;
    font-size: 13px;
}

@media (max-width: 760px) {
    .input-with-btn,
    .discount-controls {
        flex-wrap: wrap;
    }
}
</style>
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

                            <!-- 기존에 연결되어 있던 상품이 있으면 그대로 다시 그려준다 -->
                            <c:when test="${not empty event.connectedProducts}">

                                <c:forEach var="connectedProduct"
                                           items="${event.connectedProducts}">

                                    <div class="product-item">

                                        <input type="hidden"
                                            name="productNoList"
                                            class="productNo"
                                            value="<c:out value='${connectedProduct.productNo}'/>">

                                        <div class="input-with-btn">

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

                                        </div>

                                        <div class="discount-row">

                                            <div class="discount-controls">

                                                <label class="form-label">
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

                                    </div>

                                </c:forEach>

                            </c:when>

                            <!-- 연결된 상품이 없는 경우 빈 행을 하나 보여준다 -->
                            <c:otherwise>

                                <div class="product-item">

                                    <input type="hidden"
                                        name="productNoList"
                                        class="productNo">

                                    <div class="input-with-btn">

                                        <input class="form-input productName"
                                            type="text"
                                            name="productNameList"
                                            placeholder="연결할 상품을 선택하세요."
                                            readonly>

                                        <button class="btn btn-dark productSearchButton"
                                                type="button">
                                            상품 검색
                                        </button>

                                    </div>

                                    <div class="discount-row">

                                        <div class="discount-controls">

                                            <label class="form-label">
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
