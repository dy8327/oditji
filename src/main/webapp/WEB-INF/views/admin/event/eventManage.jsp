<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 이벤트 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main class="main-content">

        <div class="admin-page-header">

            <a href="${pageContext.request.contextPath}/admin/main" class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="admin-page-title">이벤트 관리</h1>

            <p class="admin-page-desc">
                진행 중인 이벤트를 등록, 수정, 삭제하고 진행 상태를 관리할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="toolbar">

                <form method="get" action="${pageContext.request.contextPath}/admin/event/list">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="이벤트명 검색">
                </form>

                <button type="button" class="btn btn-primary" onclick="openEventModal('register')">
                    등록
                </button>

            </div>

            <table class="data-table">

                <thead>
                    <tr>
                        <th>번호</th>
                        <th>이벤트명</th>
                        <th>이벤트 기간</th>
                        <th>상태</th>
                        <th>등록일</th>
                        <th>관리</th>
                    </tr>
                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty eventList}">

                            <c:forEach var="event" items="${eventList}">

                                <tr>
                                    <td>${event.eventNo}</td>
                                    <td>${event.title}</td>
                                    <td>${event.startDate} ~ ${event.endDate}</td>
                                    <td>

                                        <c:choose>
                                            <c:when test="${event.status == 'READY'}">
                                                <span class="status-waiting">예정</span>
                                            </c:when>
                                            <c:when test="${event.status == 'ACTIVE'}">
                                                <span class="status-ok">진행중</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-reject">종료</span>
                                            </c:otherwise>
                                        </c:choose>

                                    </td>
                                    <td>${event.createdAt}</td>
                                    <td>

                                        <div class="item-actions" style="justify-content:center;">

                                            <button type="button" class="btn btn-dark"
                                                    onclick="openEventModal(
                                                        'update',
                                                        '${event.eventNo}',
                                                        '${event.title}',
                                                        '${event.description}',
                                                        '${event.startDate} ~ ${event.endDate}',
                                                        '${event.status}'
                                                    )">
                                                수정
                                            </button>

                                            <form action="${pageContext.request.contextPath}/admin/event/delete" method="post">
                                                <input type="hidden" name="eventNo" value="${event.eventNo}">
                                                <button type="submit" class="btn btn-danger">삭제</button>
                                            </form>

                                        </div>

                                    </td>
                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>
                            <tr><td colspan="6">등록된 이벤트가 없습니다.</td></tr>
                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <a href="?page=${pagination.currentPage-1}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?page=${p}" class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?page=${pagination.currentPage+1}">›</a>

            </div>

        </section>

    </main>

</div>

<%-- 이벤트 등록 / 수정 팝업 --%>
<div class="modal-overlay" id="eventModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3 id="eventModalTitle">이벤트 등록</h3>
            <span class="modal-close" onclick="closeModal('eventModal')">&times;</span>
        </div>

        <form id="eventForm" action="${pageContext.request.contextPath}/admin/event/register" method="post"
              enctype="multipart/form-data">

            <input type="hidden" name="eventNo" id="eventNo">

            <div class="form-group">
                <label class="form-label">이벤트명</label>
                <input type="text" name="title" id="eventTitle" class="form-input" required>
            </div>

            <div class="form-group">
                <label class="form-label">이벤트 설명</label>
                <textarea name="description" id="eventDescription" class="form-textarea"></textarea>
            </div>

            <div class="form-group">
                <label class="form-label">이벤트 기간</label>
                <input type="text" name="eventPeriod" id="eventPeriod" class="form-input"
                       placeholder="2026-07-01 ~ 2026-07-31">
            </div>

            <div class="form-group">
                <label class="form-label">연결 상품</label>
                <input type="text" name="linkedProduct" class="form-input" placeholder="상품명 검색">
            </div>

            <div class="form-group">
                <label class="form-label">상태</label>
                <div class="radio-group" id="eventStatusGroup">
                    <label><input type="radio" name="status" value="READY">예정</label>
                    <label><input type="radio" name="status" value="ACTIVE">진행중</label>
                    <label><input type="radio" name="status" value="ENDED">종료</label>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">이벤트 이미지</label>
                <div class="file-box">
                    <input type="file" name="bannerImage" accept="image/*">
                </div>
            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary" id="eventSubmitBtn">이벤트 등록</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('eventModal')">취소</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

function openEventModal(mode, eventNo, title, description, period, status) {

    var form = document.getElementById('eventForm');
    var modalTitle = document.getElementById('eventModalTitle');
    var submitBtn = document.getElementById('eventSubmitBtn');

    if (mode === 'update') {
        modalTitle.textContent = '이벤트 수정';
        submitBtn.textContent = '이벤트 수정';
        form.action = '${pageContext.request.contextPath}/admin/event/update';

        document.getElementById('eventNo').value = eventNo;
        document.getElementById('eventTitle').value = title;
        document.getElementById('eventDescription').value = description;
        document.getElementById('eventPeriod').value = period;

        var radios = document.getElementsByName('status');
        for (var i = 0; i < radios.length; i++) {
            radios[i].checked = (radios[i].value === status);
        }

    } else {
        modalTitle.textContent = '이벤트 등록';
        submitBtn.textContent = '이벤트 등록';
        form.action = '${pageContext.request.contextPath}/admin/event/register';
        form.reset();
    }

    document.getElementById('eventModal').classList.add('open');
}
</script>

</body>
</html>
