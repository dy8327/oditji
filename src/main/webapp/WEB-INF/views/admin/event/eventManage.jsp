<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="event"/>
<c:set var="currentTab" value="${empty param.tab ? 'register' : param.tab}"/>

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
                사업자가 요청한 이벤트 등록·수정·연장 건을 확인하고 승인 또는 반려할 수 있습니다.
                이벤트는 종료일이 지나면 자동으로 삭제됩니다.
            </p>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">
                <a href="?tab=register" class="${currentTab == 'register' ? 'active' : ''}">이벤트 등록</a>
                <a href="?tab=update" class="${currentTab == 'update' ? 'active' : ''}">이벤트 수정</a>
                <a href="?tab=extend" class="${currentTab == 'extend' ? 'active' : ''}">이벤트 연장</a>
            </nav>

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/event/list">
                    <input type="hidden" name="tab" value="${currentTab}">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="사업자명, 이벤트명 검색">
                </form>
            </div>

            <table class="data-table">

                <thead>
                    <tr>
                        <th>번호</th>
                        <th>사업자명</th>
                        <th>이벤트명</th>
                        <th>
                            <c:choose>
                                <c:when test="${currentTab == 'extend'}">연장 기간</c:when>
                                <c:otherwise>이벤트 기간</c:otherwise>
                            </c:choose>
                        </th>
                        <th>요청일</th>
                        <th>처리 상태</th>
                        <th>관리</th>
                    </tr>
                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty eventRequestList}">

                            <c:forEach var="req" items="${eventRequestList}">

                                <tr>
                                    <td>${req.requestNo}</td>
                                    <td>${req.businessName}</td>
                                    <td>${req.eventTitle}</td>
                                    <td>${req.startDate} ~ ${req.endDate}</td>
                                    <td>${req.requestedAt}</td>
                                    <td>

                                        <c:choose>
                                            <c:when test="${req.status == 'APPROVED'}">
                                                <span class="status-ok">승인</span>
                                            </c:when>
                                            <c:when test="${req.status == 'REJECTED'}">
                                                <span class="status-reject">반려</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-waiting">대기</span>
                                            </c:otherwise>
                                        </c:choose>

                                    </td>
                                    <td>

                                        <button type="button" class="btn btn-dark"
                                                onclick="openEventRequestModal(
                                                    '${req.requestNo}',
                                                    '${req.businessName}',
                                                    '${req.eventTitle}',
                                                    '${req.startDate} ~ ${req.endDate}',
                                                    '${req.description}'
                                                )">
                                            상세보기
                                        </button>

                                    </td>
                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>
                            <tr><td colspan="7">

                                <c:choose>
                                    <c:when test="${currentTab == 'update'}">수정 요청 내역이 없습니다.</c:when>
                                    <c:when test="${currentTab == 'extend'}">연장 요청 내역이 없습니다.</c:when>
                                    <c:otherwise>등록 요청 내역이 없습니다.</c:otherwise>
                                </c:choose>

                            </td></tr>
                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <a href="?tab=${currentTab}&page=${pagination.currentPage-1}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?tab=${currentTab}&page=${p}" class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?tab=${currentTab}&page=${pagination.currentPage+1}">›</a>

            </div>

        </section>

    </main>

</div>

<%-- 이벤트 요청 상세 / 승인·반려 팝업 --%>
<div class="modal-overlay" id="eventRequestModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3 id="eventRequestModalTitle">이벤트 요청 상세</h3>
            <span class="modal-close" onclick="closeModal('eventRequestModal')">&times;</span>
        </div>

        <div class="target-info-box">
            <p><span>사업자명</span><strong id="reqBusinessName"></strong></p>
            <p><span>이벤트명</span><strong id="reqEventTitle"></strong></p>
            <p><span>이벤트 기간</span><strong id="reqEventPeriod"></strong></p>
        </div>

        <div class="form-group">
            <label class="form-label">요청 내용</label>
            <textarea class="form-textarea" id="reqDescription" readonly></textarea>
        </div>

        <form id="eventRequestForm" action="${pageContext.request.contextPath}/admin/event/approve" method="post">

            <input type="hidden" name="requestNo" id="reqRequestNo">
            <input type="hidden" name="tab" value="${currentTab}">

            <div class="modal-footer">
                <button type="submit" class="btn btn-success">승인</button>
                <button type="submit" formaction="${pageContext.request.contextPath}/admin/event/reject"
                        class="btn btn-danger">반려</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('eventRequestModal')">닫기</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

function openEventRequestModal(requestNo, businessName, eventTitle, period, description) {
    document.getElementById('reqRequestNo').value = requestNo;
    document.getElementById('reqBusinessName').textContent = businessName;
    document.getElementById('reqEventTitle').textContent = eventTitle;
    document.getElementById('reqEventPeriod').textContent = period;
    document.getElementById('reqDescription').value = description;
    document.getElementById('eventRequestModal').classList.add('open');
}
</script>

</body>
</html>
