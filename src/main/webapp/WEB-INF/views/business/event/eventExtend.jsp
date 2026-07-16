<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 연장</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
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
                이벤트 연장
            </h1>

            <!-- 연장 실패 메시지 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-error">
                    <c:out value="${errorMessage}"/>
                </div>
            </c:if>

            <form action="${pageContext.request.contextPath}/business/event/extend"
                  method="post">

                <input type="hidden"
                       name="eventNo"
                       value="<c:out value='${event.eventNo}'/>">

                <div class="form-group">

                    <label class="form-label">
                        이벤트명
                    </label>

                    <input class="form-input"
                           type="text"
                           value="<c:out value='${event.title}'/>"
                           readonly>

                </div>

                <div class="form-group">

                    <label class="form-label">
                        현재 이벤트 기간
                    </label>

                    <input class="form-input"
                           type="text"
                           value="<c:out value='${event.startDate}'/> ~ <c:out value='${event.endDate}'/>"
                           readonly>

                </div>

                <div class="form-group">

                    <label class="form-label"
                           for="extendEndDate">
                        연장 종료일
                    </label>

                    <input class="form-input"
                           type="date"
                           id="extendEndDate"
                           name="extendEndDate"
                           min="<c:out value='${event.endDate}'/>"
                           required>

                </div>

                <div class="form-group">

                    <label class="form-label"
                           for="extendReason">
                        연장 사유
                    </label>

                    <!--
                        현재 EVENT 테이블에는 연장 사유 컬럼이 없으므로
                        입력한 사유는 서버 콘솔 로그로만 확인한다.
                    -->
                    <textarea class="form-textarea"
                              id="extendReason"
                              name="extendReason"
                              maxlength="1000"
                              placeholder="이벤트 연장 사유를 입력해주세요."
                              required></textarea>

                </div>

                <div class="form-group">

                    <label class="form-label">
                        요청 상태
                    </label>

                    <input class="form-input"
                           type="text"
                           value="승인 대기"
                           readonly>

                </div>

                <div class="submit-stack">

                    <button class="btn btn-primary"
                            type="submit">
                        연장 요청
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

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
document.addEventListener("DOMContentLoaded", function () {

    const extendEndDateInput =
        document.getElementById("extendEndDate");

    const currentEndDate =
        "<c:out value='${event.endDate}'/>";

    /*
     * 현재 종료일과 같은 날짜가 아닌
     * 다음 날부터 선택할 수 있도록 설정한다.
     */
    if (currentEndDate) {

        const minimumDate =
            new Date(currentEndDate + "T00:00:00");

        minimumDate.setDate(
            minimumDate.getDate() + 1
        );

        const year = minimumDate.getFullYear();
        const month = String(minimumDate.getMonth() + 1).padStart(2, "0");
        const day = String(minimumDate.getDate()).padStart(2, "0");

        extendEndDateInput.min =
            year + "-" + month + "-" + day;
    }

});
</script>

</body>
</html>
