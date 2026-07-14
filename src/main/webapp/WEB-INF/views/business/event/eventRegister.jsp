<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">

<title>ODITJI | 이벤트 등록 요청</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">
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
                  enctype="multipart/form-data">

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

                <!-- 상태 -->
                <div class="form-group">

                    <span class="form-label">
                        상태
                    </span>

                    <div class="btn-row">

                        <label>
                            <input type="radio"
                                   name="status"
                                   value="WAITING"
                                   checked>
                            예정
                        </label>

                        <label>
                            <input type="radio"
                                   name="status"
                                   value="ACTIVE">
                            진행 중
                        </label>

                        <label>
                            <input type="radio"
                                   name="status"
                                   value="ENDED">
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

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
document.addEventListener("DOMContentLoaded", function () {

    const eventImageInput =
        document.getElementById("eventImage");

    const eventImageFileName =
        document.getElementById("eventImageFileName");

    const productSearchButton =
        document.getElementById("productSearchButton");

    const startDateInput =
        document.getElementById("startDate");

    const endDateInput =
        document.getElementById("endDate");

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
     * 상품 검색 기능 연결 위치
     */
    productSearchButton.addEventListener("click", function () {

        alert(
            "상품 검색 팝업 기능은 상품 목록 조회 기능과 연결해야 합니다."
        );

        /*
         * 상품 검색 팝업을 구현한 이후에는 예를 들어
         * 다음과 같이 연결할 수 있습니다.
         *
         * window.open(
         *     contextPath + "/business/product/search-popup",
         *     "productSearch",
         *     "width=900,height=700"
         * );
         */
    });

});
</script>

</body>
</html>