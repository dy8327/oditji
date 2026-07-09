<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 연장 요청</title>
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
                이벤트 연장 요청
            </h1>

            <form action="${pageContext.request.contextPath}/business/event/extend"
                  method="post">

                <input type="hidden"
                       name="eventNo"
                       value="${event.eventNo}">

                <div class="form-group">

                    <label class="form-label">
                        이벤트명
                    </label>

                    <input class="form-input"
                           type="text"
                           value="${event.title}"
                           readonly>

                </div>

                <div class="form-group">

                    <label class="form-label">
                        현재 이벤트 기간
                    </label>

                    <input class="form-input"
                           type="text"
                           value="${event.startDate} ~ ${event.endDate}"
                           readonly>

                </div>

                <div class="form-group">

                    <label class="form-label">
                        연장 종료일
                    </label>

                    <input class="form-input"
                           type="date"
                           name="extendEndDate">

                </div>

                <div class="form-group">

                    <label class="form-label">
                        연장 사유
                    </label>

                    <textarea class="form-textarea"
                              name="extendReason"
                              placeholder="이벤트 연장 사유를 입력해주세요."></textarea>

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

</body>
</html>