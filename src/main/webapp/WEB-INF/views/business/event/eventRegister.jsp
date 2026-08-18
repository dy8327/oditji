<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">

<title>ODITJI | 이벤트 등록 요청</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<script defer
        src="${pageContext.request.contextPath}/js/business.js">
</script>

<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
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

                    <label class="form-label" for="productName_0">
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
                                       id="productName_0"
                                       name="productNameList"
                                       placeholder="연결할 상품을 선택하세요."
                                       readonly>

                                <button class="btn btn-dark productSearchButton"
                                        type="button">
                                    상품 검색
                                </button>

                                <label class="form-label discount-label" for="productDiscountRate_0">
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

                    <%-- [SonarQube 접근성] 제목 라벨을 실제 파일 입력과 연결합니다. --%>
                    <label for="eventImage" class="form-label">
                        이벤트 이미지
                    </label>

                    <div class="file-box event-image-file-box">

                        <%-- =====================================================
                            [이벤트 이미지 파일 선택 영역 수정]
                            실제 file input은 숨기고 지정된 버튼 영역을
                            클릭했을 때만 파일 선택창이 열리도록 수정합니다.
                        ===================================================== --%>
                        <label for="eventImage"
                            class="event-image-select-button">
                            파일 선택
                        </label>

                        <input type="file"
                            id="eventImage"
                            name="eventImage"
                            class="event-image-hidden-input"
                            accept=".jpg,.jpeg,.png,.gif,.webp">

                        <span id="eventImageFileName"
                            class="event-image-file-name">
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
<dialog class="product-search-modal"
        id="productSearchModal"
        open
        aria-modal="true"
        aria-labelledby="productSearchModalTitle">

    <div class="product-search-modal-panel">

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
                                <th class="col-hide-mobile">작품</th>
                                <th class="col-hide-mobile">배우</th>
                                <th class="col-hide-mobile">종류</th>
                                <th>가격</th>
                                <th>재고</th>
                                <th class="col-hide-mobile">상태</th>
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

                                    <%--
                                        [모바일 반응형] 상태 뱃지 컬럼을 숨기는 대신, eventList.jsp와
                                        동일하게 상품명 텍스트 색상으로 상태를 표시한다.
                                    --%>
                                    <c:set var="productStatusClass">
                                        <c:choose>
                                            <c:when test="${product.status eq 'APPROVED'}">st-ok</c:when>
                                            <c:when test="${product.status eq 'WAITING'}">st-waiting</c:when>
                                            <c:otherwise>st-reject</c:otherwise>
                                        </c:choose>
                                    </c:set>

                                    <tr class="product-search-row"
                                        data-product-name="<c:out value='${product.productName}'/>"
                                        data-content-title="<c:out value='${product.contentTitle}'/>"
                                        data-actor-name="<c:out value='${product.actorName}'/>"
                                        data-product-type="<c:out value='${product.productType}'/>">

                                        <!-- 상품명 -->
                                        <td>

                                            <span class="product-search-name mobile-status-text ${fn:trim(productStatusClass)}">
                                                <c:out value="${product.productName}"/>
                                            </span>

                                        </td>

                                        <!-- 작품명 -->
                                        <td class="col-hide-mobile">

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
                                        <td class="col-hide-mobile">

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
                                        <td class="col-hide-mobile">
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
                                        <td class="col-hide-mobile">

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

                                            <%--
                                                [모바일 반응형] 숨겨진 컬럼(작품/배우/종류/가격/재고/상태)은
                                                가로 스크롤 대신 상세보기 모달에서 확인한다.
                                            --%>
                                            <button class="btn btn-dark mobile-only-el"
                                                    type="button"
                                                    onclick="openModal('productSearchDetailModal_${product.productNo}')">
                                                상세보기
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

</dialog>

<%--
    [모바일 반응형] 상품 검색 결과 행의 상세보기 모달.
    <table> 안에는 <div>를 둘 수 없어 <tbody> 밖, 별도의
    forEach로 상품마다 하나씩 렌더링한다(eventDetailModal과 동일한 방식).
--%>
<c:forEach var="product" items="${productList}">

    <c:if test="${product.status ne 'DELETE_REQUESTED'}">

        <div class="modal-overlay" id="productSearchDetailModal_${product.productNo}">

            <div class="modal-box">

                <div class="modal-header">
                    <h3>상품 상세 정보</h3>
                    <button type="button"
                            class="modal-close"
                            onclick="closeModal('productSearchDetailModal_${product.productNo}')"
                            aria-label="닫기">
                        &times;
                    </button>
                </div>

                <div class="detail-grid">

                    <div>
                        <span class="detail-label">상품명</span>
                        <p><c:out value="${product.productName}"/></p>
                    </div>

                    <div>
                        <span class="detail-label">작품</span>
                        <p>
                            <c:choose>
                                <c:when test="${not empty product.contentTitle}"><c:out value="${product.contentTitle}"/></c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </p>
                    </div>

                    <div>
                        <span class="detail-label">배우</span>
                        <p>
                            <c:choose>
                                <c:when test="${not empty product.actorName}"><c:out value="${product.actorName}"/></c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </p>
                    </div>

                    <div>
                        <span class="detail-label">종류</span>
                        <p><c:out value="${product.productType}"/></p>
                    </div>

                    <div>
                        <span class="detail-label">가격</span>
                        <p><fmt:formatNumber value="${product.price}" pattern="#,###"/>원</p>
                    </div>

                    <div>
                        <span class="detail-label">재고</span>
                        <p><fmt:formatNumber value="${product.stock}" pattern="#,###"/>개</p>
                    </div>

                    <div>
                        <span class="detail-label">상태</span>
                        <p>
                            <c:choose>
                                <c:when test="${product.status eq 'WAITING'}">승인 대기</c:when>
                                <c:when test="${product.status eq 'APPROVED'}">승인 완료</c:when>
                                <c:when test="${product.status eq 'REJECTED'}">승인 반려</c:when>
                                <c:when test="${product.status eq 'SOLD_OUT'}">품절</c:when>
                                <c:when test="${product.status eq 'STOPPED'}">판매 중지</c:when>
                                <c:otherwise><c:out value="${product.status}"/></c:otherwise>
                            </c:choose>
                        </p>
                    </div>

                </div>

                <div class="modal-footer">
                    <button type="button"
                            class="btn btn-dark"
                            onclick="closeModal('productSearchDetailModal_${product.productNo}')">
                        닫기
                    </button>
                </div>

            </div>

        </div>

    </c:if>

</c:forEach>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>