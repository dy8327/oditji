<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 이벤트 수정</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<script defer
        src="${pageContext.request.contextPath}/js/business.js">
</script>

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">

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

                <!-- 이벤트명 -->
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

                <!-- 이벤트 설명 -->
                <div class="form-group">

                    <label class="form-label"
                           for="description">
                        이벤트 설명
                    </label>

                    <textarea class="form-textarea"
                              id="description"
                              name="description"
                              placeholder="이벤트에 대한 설명을 입력하세요."><c:out value='${event.description}'/></textarea>

                </div>

                <!-- 이벤트 기간 -->
                <div class="form-group">

                    <span class="form-label">
                        이벤트 기간
                    </span>

                    <div class="event-date-row">

                        <%--
                            두 날짜 입력창이 하나의 공통 제목 아래에 있으므로
                            aria-label을 통해 각 입력창의 역할을 구분합니다.
                        --%>
                        <input class="form-input"
                               type="date"
                               id="startDate"
                               aria-label="이벤트 시작일"
                               name="startDate"
                               value="<c:out value='${event.startDate}'/>"
                               required>

                        <span class="event-date-separator">
                            -
                        </span>

                        <input class="form-input"
                               type="date"
                               id="endDate"
                               aria-label="이벤트 종료일"
                               name="endDate"
                               value="<c:out value='${event.endDate}'/>"
                               required>

                    </div>

                </div>

                <!-- 연결 상품: 여러 개 연결 가능 -->
                <div class="form-group">

                    <%--
                        SonarQube 접근성 이슈 대응:

                        연결 상품 영역의 대표 label을 첫 번째 상품명 입력창과
                        for/id 속성으로 연결합니다.

                        기존 연결 상품이 있거나 빈 행이 표시되는 경우 모두
                        첫 번째 상품명 입력창은 productName_0을 사용합니다.
                    --%>
                    <label class="form-label"
                           for="productName_0">
                        연결 상품
                    </label>

                    <div id="productList">

                        <c:choose>

                            <%--
                                기존에 연결되어 있던 상품이 있으면
                                상품 정보를 유지한 상태로 다시 출력합니다.
                            --%>
                            <c:when test="${not empty event.connectedProducts}">

                                <c:forEach var="connectedProduct"
                                           items="${event.connectedProducts}"
                                           varStatus="connectedStatus">

                                    <div class="event-product-item">

                                        <input type="hidden"
                                               name="productNoList"
                                               class="productNo"
                                               value="<c:out value='${connectedProduct.productNo}'/>">

                                        <div class="product-row">

                                            <%--
                                                기존 연결 상품마다 반복문의 index를 사용하여
                                                productName_0, productName_1처럼
                                                서로 다른 id를 부여합니다.

                                                business.js에서도 동일한 ID 규칙을 사용하여
                                                추가되는 상품 행과 중복되지 않도록 합니다.
                                            --%>
                                            <input class="form-input productName"
                                                   type="text"
                                                   id="productName_${connectedStatus.index}"
                                                   name="productNameList"
                                                   aria-label="연결 상품 ${connectedStatus.count}"
                                                   value="<c:out value='${connectedProduct.productName}'/>"
                                                   placeholder="연결할 상품을 선택하세요."
                                                   readonly>

                                            <button class="btn btn-dark productSearchButton"
                                                    type="button">
                                                상품 검색
                                            </button>

                                            <%--
                                                각 행의 할인율 label과 input을
                                                동일한 반복문 index를 이용해 연결합니다.
                                            --%>
                                            <label class="form-label discount-label"
                                                   for="productDiscountRate_${connectedStatus.index}">
                                                할인율 (%)
                                            </label>

                                            <input class="form-input productDiscountRate"
                                                   type="number"
                                                   id="productDiscountRate_${connectedStatus.index}"
                                                   name="discountRateList"
                                                   min="0"
                                                   max="100"
                                                   value="<c:out value='${connectedProduct.discountRate}'/>">

                                            <%--
                                                기존 + 기능은 그대로 유지하면서
                                                화면 낭독기가 버튼 목적을 인식하도록
                                                aria-label만 추가합니다.
                                            --%>
                                            <button class="btn btn-primary addProductButton"
                                                    type="button"
                                                    aria-label="연결 상품 입력 행 추가">
                                                +
                                            </button>

                                            <%--
                                                기존 - 기능은 그대로 유지하면서
                                                삭제 대상 행을 알 수 있도록 설명을 추가합니다.
                                            --%>
                                            <button class="btn btn-dark removeProductButton"
                                                    type="button"
                                                    aria-label="연결 상품 ${connectedStatus.count} 삭제">
                                                -
                                            </button>

                                        </div>

                                        <p class="form-hint productDiscountPreview">

                                            <fmt:formatNumber
                                                    value="${connectedProduct.price}"
                                                    pattern="#,###"/>원 →

                                            할인율
                                            <c:out value="${connectedProduct.discountRate}"/>%

                                        </p>

                                    </div>

                                </c:forEach>

                            </c:when>

                            <%--
                                연결된 상품이 없는 경우
                                비어 있는 상품 입력 행을 하나 표시합니다.
                            --%>
                            <c:otherwise>

                                <div class="event-product-item">

                                    <input type="hidden"
                                           name="productNoList"
                                           class="productNo">

                                    <div class="product-row">

                                        <%--
                                            빈 행의 상품명 입력창은 첫 번째 행이므로
                                            대표 label의 for와 동일한 productName_0을 사용합니다.
                                        --%>
                                        <input class="form-input productName"
                                               type="text"
                                               id="productName_0"
                                               name="productNameList"
                                               aria-label="연결 상품 1"
                                               placeholder="연결할 상품을 선택하세요."
                                               readonly>

                                        <button class="btn btn-dark productSearchButton"
                                                type="button">
                                            상품 검색
                                        </button>

                                        <%--
                                            빈 행의 할인율 label과 input도
                                            for/id 속성으로 명시적으로 연결합니다.
                                        --%>
                                        <label class="form-label discount-label"
                                               for="productDiscountRate_0">
                                            할인율 (%)
                                        </label>

                                        <input class="form-input productDiscountRate"
                                               type="number"
                                               id="productDiscountRate_0"
                                               name="discountRateList"
                                               min="0"
                                               max="100"
                                               value="0">

                                        <button class="btn btn-primary addProductButton"
                                                type="button"
                                                aria-label="연결 상품 입력 행 추가">
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

                <!-- 요청 상태 -->
                <div class="form-group">

                    <%--
                        readonly 입력창도 form control에 해당하므로
                        label의 for와 input의 id를 연결합니다.
                    --%>
                    <label class="form-label"
                           for="requestStatus">
                        요청 상태
                    </label>

                    <!--
                        이벤트 상태는 사업자가 직접 지정할 수 없습니다.

                        수정 요청이 접수되면 EVENT.STATUS는 WAITING으로 변경되고,
                        관리자 승인 전에는 사용자 화면에 노출되지 않습니다.

                        EVENT.STATUS CHECK 제약:
                        WAITING / APPROVED / END / REJECTED / DELETED
                    -->
                    <input class="form-input"
                           type="text"
                           id="requestStatus"
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

                <!-- 수정 요청 및 취소 버튼 -->
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

    <%--
        모달 영역에 dialog 역할과 제목 연결을 추가하여
        화면 낭독기가 현재 영역을 대화상자로 인식하도록 합니다.
    --%>
    <div class="product-search-modal-panel"
         role="dialog"
         aria-modal="true"
         aria-labelledby="productSearchModalTitle">

        <div class="product-search-modal-header">

            <h2 class="product-search-modal-title"
                id="productSearchModalTitle">
                이벤트 연결 상품 검색
            </h2>

            <button class="product-search-modal-close"
                    type="button"
                    id="productSearchModalClose"
                    aria-label="상품 검색 창 닫기">
                ×
            </button>

        </div>

        <!-- 상품 검색 입력 영역 -->
        <div class="product-search-bar">

            <%--
                검색창의 시각적 디자인은 유지하면서
                화면 낭독기용 label을 제공합니다.
            --%>
            <label for="productSearchKeyword"
                   class="sr-only">
                상품 검색어
            </label>

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

        <!-- 상품 검색 결과 -->
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

                        <!-- 삭제 요청 중인 상품은 연결 대상에서 제외합니다. -->
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

                                    <fmt:formatNumber
                                            value="${product.price}"
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