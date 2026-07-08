<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 수정 요청</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
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
                이벤트 수정 요청
            </h1>

            <form action="${pageContext.request.contextPath}/business/event/update"
                  method="post"
                  enctype="multipart/form-data">

                <input type="hidden"
                       name="eventNo"
                       value="${event.eventNo}">

                <div class="form-group">

                    <label class="form-label">
                        이벤트명
                    </label>

                    <input class="form-input"
                           type="text"
                           name="eventTitle"
                           value="${event.title}">

                </div>


                <div class="form-group">

                    <label class="form-label">
                        이벤트 설명
                    </label>

                    <textarea class="form-textarea"
                              name="eventContent">${event.content}</textarea>

                </div>


                <div class="form-group">

                    <label class="form-label">
                        이벤트 기간
                    </label>

                    <input class="form-input"
                           type="text"
                           name="eventPeriod"
                           value="${event.startDate} ~ ${event.endDate}">

                </div>


                <div class="form-group">

                    <label class="form-label">
                        연결 상품
                    </label>

                    <div class="input-with-btn">

                        <input class="form-input"
                               type="text"
                               name="productName"
                               value="${event.productName}"
                               readonly>

                        <button class="btn btn-dark"
                                type="button">
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


                <div class="form-group">

                    <label class="form-label">
                        이벤트 이미지
                    </label>

                    <div class="file-box">

                        <input type="file"
                               name="eventImage">

                        <span>
                            ${empty event.bannerImage ? "선택된 파일 없음" : event.bannerImage}
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

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>