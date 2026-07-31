<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>ODITJI | 이벤트 등록 요청</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<script defer
        src="${pageContext.request.contextPath}/js/business.js">
</script>

</head>

<body data-success-message="<c:out value='${successMessage}'/>"
      data-error-message="<c:out value='${errorMessage}'/>">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>

        <section class="form-panel">

            <h1 class="form-title">
                이벤트 등록 요청
            </h1>

            <form action="${pageContext.request.contextPath}/business/event/register"
                  method="post"
                  enctype="multipart/form-data"
                  id="eventForm">

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
                           placeholder="이벤트명을 입력하세요."
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
                              placeholder="이벤트에 대한 설명을 입력하세요."></textarea>

                </div>

                <!-- 이벤트 기간 -->
                <div class="form-group">

                    <span class="form-label">
                        이벤트 기간
                    </span>

                    <div class="event-date-row">

                        <!-- 이벤트 시작일 -->
                        <input class="form-input"
                               type="date"
                               id="startDate"
                               name="startDate"
                               aria-label="이벤트 시작일"
                               required>

                        <span class="event-date-separator">
                            -
                        </span>

                        <!-- 이벤트 종료일 -->
                        <input class="form-input"
                               type="date"
                               id="endDate"
                               name="endDate"
                               aria-label="이벤트 종료일"
                               required>

                    </div>

                </div>

                <!-- 연결 상품 -->
                <div class="form-group">

                    <label class="form-label">
                        연결 상품
                    </label>

                    <div id="productList">

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

                    </div>

                </div>

                <!--
                    이벤트 상태는 사업자가 직접 지정할 수 없다.
                    등록/수정 요청은 항상 서버에서 WAITING(관리자 승인 대기)으로
                    저장되고, 관리자가 승인/반려/종료 처리를 한다.
                    (EVENT.STATUS CHECK 제약: WAITING/APPROVED/END/REJECTED/DELETED)
                -->

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
                            선택된 파일 없음
                        </span>

                    </div>

                </div>

                <!-- 버튼 -->
                <div class="submit-stack">

                    <button class="btn btn-primary"
                            type="submit">
                        이벤트 등록 요청
                    </button>

                    <a class="btn btn-dark"
                       href="${pageContext.request.contextPath}/business/main">
                        취소
                    </a>

                </div>

            </form>

        </section>

    </main>

</div>

<!-- =========================================================
     이벤트 연결 상품 검색 모달

     신규 JSP를 만들지 않고 현재 이벤트 등록 화면 내부에서
     로그인 사업자가 등록한 상품을 검색하고 선택한다.
========================================================= -->
<div class="product-search-modal"
     id="productSearchModal">

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

        <!-- 상품 검색어 -->
        <div class="product-search-bar">

            <%--
                상품 검색 input의 id와 label을 명시적으로 연결한다.
                모달 디자인은 유지하면서 보조 기술에 검색 목적을 제공한다.
            --%>
            <label for="productSearchKeyword"
                   style="position:absolute;
                          width:1px;
                          height:1px;
                          padding:0;
                          margin:-1px;
                          overflow:hidden;
                          clip:rect(0, 0, 0, 0);
                          white-space:nowrap;
                          border:0;">
                이벤트 연결 상품 검색어
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

        <!-- 사업자 상품 목록 -->
        <div class="product-search-result">

            <c:choose>

                <c:when test="${empty productList}">

                    <div class="product-search-empty">
                        등록된 상품이 없습니다.
                    </div>

                </c:when>

                <c:otherwise>

                    <table class="product-search-table">

                        <thead>
                            <tr>
                                <th>상품명</th>
                                <th>작품</th>
                                <th>배우</th>
                                <th>종류</th>
                                <th>가격</th>
                                <th>재고</th>
                                <th>상태</th>
                                <th>선택</th>
                            </tr>
                        </thead>

                        <tbody id="productSearchTableBody">

                            <c:forEach var="product"
                                       items="${productList}">

                                <!--
                                    삭제 요청 중인 상품은
                                    이벤트 연결 대상에서 제외한다.
                                -->
                                <c:if test="${product.status ne 'DELETE_REQUESTED'}">

                                    <tr class="product-search-row"
                                        data-product-name="<c:out value='${product.productName}'/>"
                                        data-content-title="<c:out value='${product.contentTitle}'/>"
                                        data-actor-name="<c:out value='${product.actorName}'/>"
                                        data-product-type="<c:out value='${product.productType}'/>">

                                        <!-- 상품명 -->
                                        <td>

                                            <span class="product-search-name">
                                                <c:out value="${product.productName}"/>
                                            </span>

                                        </td>

                                        <!-- 작품명 -->
                                        <td>

                                            <c:choose>

                                                <c:when test="${not empty product.contentTitle}">
                                                    <c:out value="${product.contentTitle}"/>
                                                </c:when>

                                                <c:otherwise>
                                                    -
                                                </c:otherwise>

                                            </c:choose>

                                        </td>

                                        <!-- 배우명 -->
                                        <td>

                                            <c:choose>

                                                <c:when test="${not empty product.actorName}">
                                                    <c:out value="${product.actorName}"/>
                                                </c:when>

                                                <c:otherwise>
                                                    -
                                                </c:otherwise>

                                            </c:choose>

                                        </td>

                                        <!-- 상품 종류 -->
                                        <td>
                                            <c:out value="${product.productType}"/>
                                        </td>

                                        <!-- 가격 -->
                                        <td>
                                            <fmt:formatNumber value="${product.price}"
                                                              pattern="#,###"/>원
                                        </td>

                                        <!-- 재고 -->
                                        <td>
                                            <fmt:formatNumber value="${product.stock}"
                                                              pattern="#,###"/>개
                                        </td>

                                        <!-- 상태 -->
                                        <td>

                                            <c:choose>

                                                <c:when test="${product.status eq 'WAITING'}">
                                                    승인 대기
                                                </c:when>

                                                <c:when test="${product.status eq 'APPROVED'}">
                                                    승인 완료
                                                </c:when>

                                                <c:when test="${product.status eq 'REJECTED'}">
                                                    승인 반려
                                                </c:when>

                                                <c:when test="${product.status eq 'SOLD_OUT'}">
                                                    품절
                                                </c:when>

                                                <c:when test="${product.status eq 'STOPPED'}">
                                                    판매 중지
                                                </c:when>

                                                <c:otherwise>
                                                    <c:out value="${product.status}"/>
                                                </c:otherwise>

                                            </c:choose>

                                        </td>

                                        <!-- 선택 -->
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

                    <!-- 검색 결과가 없는 경우 -->
                    <div class="product-search-no-result"
                         id="productSearchNoResult">
                        검색 결과가 없습니다.
                    </div>

                </c:otherwise>

            </c:choose>

        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>