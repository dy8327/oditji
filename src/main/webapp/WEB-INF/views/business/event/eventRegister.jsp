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

<title>ODITJI | 이벤트 등록 요청</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<style>
/*
 * =========================================================
 * 이벤트 연결 상품 검색 모달
 *
 * 기존 business.css 레이아웃과 디자인을 유지하면서
 * 현재 이벤트 등록 화면 안에서만 사용하는 추가 스타일이다.
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
    gap: 20px;
    margin-bottom: 20px;
}

.product-search-modal-title {
    margin: 0;
    color: #ffffff;
    font-size: 24px;
    font-weight: 700;
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
    max-height: 55vh;
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
    vertical-align: middle;
}

.product-search-table th {
    position: sticky;
    top: 0;
    z-index: 1;
    background: #292b30;
    color: #c8ccd4;
    font-size: 13px;
    white-space: nowrap;
}

.product-search-table td {
    color: #f1f2f4;
    font-size: 14px;
}

.product-search-table tbody tr:last-child td {
    border-bottom: 0;
}

.product-search-table tbody tr:hover {
    background: #292b30;
}

.product-search-name {
    font-weight: 600;
}

.product-search-empty,
.product-search-no-result {
    padding: 50px 20px;
    color: #a7acb5;
    text-align: center;
}

.product-search-no-result {
    display: none;
}

@media (max-width: 760px) {
    .product-search-modal {
        padding: 15px;
    }

    .product-search-modal-panel {
        padding: 20px;
    }

    .product-search-bar {
        flex-direction: column;
    }
}
</style>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>

        <section class="form-panel">

            <h1 class="form-title">
                이벤트 등록 요청
            </h1>

            <!-- 등록 실패 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/business/event/register"
                  method="post"
                  enctype="multipart/form-data"
                  id="eventRegisterForm">

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
                           for="eventContent">
                        이벤트 설명
                    </label>

                    <!--
                        현재 EVENT 테이블에는 이벤트 설명 컬럼이 없으므로
                        입력값은 등록 요청 로그 확인용으로만 전달된다.
                    -->
                    <textarea class="form-textarea"
                              id="eventContent"
                              name="eventContent"
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

                    <label class="form-label"
                           for="productName">
                        연결 상품
                    </label>

                    <div class="input-with-btn">

                        <!-- 상품 검색 후 실제 상품 번호 저장 -->
                        <input type="hidden"
                               id="productNo"
                               name="productNo">

                        <!-- 사용자에게 표시되는 상품명 -->
                        <input class="form-input"
                               type="text"
                               id="productName"
                               name="productName"
                               placeholder="연결할 상품을 선택하세요."
                               readonly>

                        <button class="btn btn-dark"
                                type="button"
                                id="productSearchButton">
                            상품 검색
                        </button>

                    </div>

                </div>

                <div class="form-group">
                    <label class="form-label">
                            상태
                    </label>

                        <div class="btn-row">

                            <label>
                                <input type="checkbox"
                                    name="status"
                                    value="WAITING"
                                    ${event.status == 'WAITING' ? 'checked' : ''}>
                                예정
                            </label>

                            <label>
                                <input type="checkbox"
                                    name="status"
                                    value="ACTIVE"
                                    ${event.status == 'ACTIVE' ? 'checked' : ''}>
                                진행중
                            </label>

                            <label>
                                <input type="checkbox"
                                    name="status"
                                    value="ENDED"
                                    ${event.status == 'ENDED' ? 'checked' : ''}>
                                종료
                            </label>
                        </div>
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
                                                    data-product-name="<c:out value='${product.productName}'/>">
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

<script>
document.addEventListener("DOMContentLoaded", function () {

    const eventImageInput =
        document.getElementById("eventImage");

    const eventImageFileName =
        document.getElementById("eventImageFileName");

    const productSearchButton =
        document.getElementById("productSearchButton");

    const productSearchModal =
        document.getElementById("productSearchModal");

    const productSearchModalClose =
        document.getElementById("productSearchModalClose");

    const productSearchKeyword =
        document.getElementById("productSearchKeyword");

    const productSearchResetButton =
        document.getElementById("productSearchResetButton");

    const productSearchRows =
        document.querySelectorAll(".product-search-row");

    const productSelectButtons =
        document.querySelectorAll(".product-select-button");

    const productSearchNoResult =
        document.getElementById("productSearchNoResult");

    const productNoInput =
        document.getElementById("productNo");

    const productNameInput =
        document.getElementById("productName");

    const startDateInput =
        document.getElementById("startDate");

    const endDateInput =
        document.getElementById("endDate");

    const eventRegisterForm =
        document.getElementById("eventRegisterForm");

    /*
     * 이미지 파일명 출력
     */
    eventImageInput.addEventListener("change", function () {

        if (eventImageInput.files.length === 0) {
            eventImageFileName.textContent =
                "선택된 파일 없음";

            return;
        }

        eventImageFileName.textContent =
            eventImageInput.files[0].name;
    });

    /*
     * 이벤트 종료일 최소 날짜 설정
     */
    startDateInput.addEventListener("change", function () {

        endDateInput.min = startDateInput.value;

        if (
            endDateInput.value &&
            endDateInput.value < startDateInput.value
        ) {
            endDateInput.value = "";
        }
    });

    /*
     * 상품 검색 모달 열기
     */
    productSearchButton.addEventListener("click", function () {

        productSearchModal.classList.add("active");

        document.body.style.overflow =
            "hidden";

        if (productSearchKeyword) {
            productSearchKeyword.focus();
        }
    });

    /*
     * 상품 검색 모달 닫기
     */
    function closeProductSearchModal() {

        productSearchModal.classList.remove("active");

        document.body.style.overflow =
            "";
    }

    productSearchModalClose.addEventListener(
        "click",
        closeProductSearchModal
    );

    /*
     * 모달 배경을 누르면 닫는다.
     */
    productSearchModal.addEventListener(
        "click",
        function (event) {

            if (event.target === productSearchModal) {
                closeProductSearchModal();
            }
        }
    );

    /*
     * ESC 키로 모달 닫기
     */
    document.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Escape"
                && productSearchModal.classList.contains("active")
            ) {
                closeProductSearchModal();
            }
        }
    );

    /*
     * 상품 검색
     *
     * 서버를 다시 호출하지 않고
     * 현재 사업자의 상품 목록에서 바로 필터링한다.
     */
    if (productSearchKeyword) {

        productSearchKeyword.addEventListener(
            "input",
            function () {

                const keyword =
                    productSearchKeyword.value
                        .trim()
                        .toLowerCase();

                let visibleCount = 0;

                productSearchRows.forEach(
                    function (row) {

                        const productName =
                            (row.dataset.productName || "")
                                .toLowerCase();

                        const contentTitle =
                            (row.dataset.contentTitle || "")
                                .toLowerCase();

                        const actorName =
                            (row.dataset.actorName || "")
                                .toLowerCase();

                        const productType =
                            (row.dataset.productType || "")
                                .toLowerCase();

                        const matched =
                            keyword === ""
                            || productName.includes(keyword)
                            || contentTitle.includes(keyword)
                            || actorName.includes(keyword)
                            || productType.includes(keyword);

                        row.style.display =
                            matched
                                ? ""
                                : "none";

                        if (matched) {
                            visibleCount++;
                        }
                    }
                );

                if (productSearchNoResult) {

                    productSearchNoResult.style.display =
                        visibleCount === 0
                            ? "block"
                            : "none";
                }
            }
        );
    }

    /*
     * 상품 검색 초기화
     */
    if (productSearchResetButton) {

        productSearchResetButton.addEventListener(
            "click",
            function () {

                productSearchKeyword.value =
                    "";

                productSearchRows.forEach(
                    function (row) {
                        row.style.display = "";
                    }
                );

                if (productSearchNoResult) {
                    productSearchNoResult.style.display = "none";
                }

                productSearchKeyword.focus();
            }
        );
    }

    /*
     * 이벤트 연결 상품 선택
     *
     * 상품 번호는 hidden input에 저장하고
     * 상품명은 readonly input에 표시한다.
     */
    productSelectButtons.forEach(
        function (button) {

            button.addEventListener(
                "click",
                function () {

                    const productNo =
                        button.dataset.productNo;

                    const productName =
                        button.dataset.productName;

                    productNoInput.value =
                        productNo;

                    productNameInput.value =
                        productName;

                    closeProductSearchModal();
                }
            );
        }
    );

    /*
     * EVENT 테이블에는 BUSINESS_NO가 없으므로
     * 사업자별 이벤트 소유권 확인을 위해 연결 상품 선택은 필수이다.
     */
    eventRegisterForm.addEventListener("submit", function (event) {

        if (!productNoInput.value) {

            event.preventDefault();

            alert(
                "이벤트에 연결할 상품을 선택해주세요."
            );

            productSearchButton.focus();
        }
    });


});
</script>

</body>
</html>
