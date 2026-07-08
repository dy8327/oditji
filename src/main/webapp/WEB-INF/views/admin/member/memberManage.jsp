<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="member"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 회원 관리</title>
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

            <h1 class="admin-page-title">회원 관리</h1>

            <p class="admin-page-desc">
                가입한 회원의 정보를 조회하고 정지, 탈퇴, 등급을 관리할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/member/list">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="이름, 아이디, 닉네임, 이메일 검색">
                </form>
            </div>

            <table class="data-table">

                <thead>
                    <tr>
                        <th>이름</th>
                        <th>아이디</th>
                        <th>닉네임</th>
                        <th>이메일</th>
                        <th>등급</th>
                        <th>상태</th>
                        <th>관리</th>
                    </tr>
                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty memberList}">

                            <c:forEach var="member" items="${memberList}">

                                <tr>
                                    <td>${member.memberName}</td>
                                    <td>${member.memberId}</td>
                                    <td>${member.nickname}</td>
                                    <td>${member.email}</td>
                                    <td>${empty member.grade ? 'BRONZE' : member.grade}</td>
                                    <td>

                                        <c:choose>
                                            <c:when test="${member.status == 'ACTIVE'}">
                                                <span class="status-ok">활성</span>
                                            </c:when>
                                            <c:when test="${member.status == 'BLOCKED'}">
                                                <span class="status-reject">정지</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-waiting">탈퇴</span>
                                            </c:otherwise>
                                        </c:choose>

                                    </td>
                                    <td>

                                        <div class="item-actions">

                                            <form action="${pageContext.request.contextPath}/admin/member/suspend" method="post">
                                                <input type="hidden" name="memberNo" value="${member.memberNo}">
                                                <button type="submit" class="btn btn-outline">정지</button>
                                            </form>

                                            <form action="${pageContext.request.contextPath}/admin/member/withdraw" method="post">
                                                <input type="hidden" name="memberNo" value="${member.memberNo}">
                                                <button type="submit" class="btn btn-danger">탈퇴</button>
                                            </form>

                                            <button type="button" class="btn btn-dark"
                                                    onclick="openGradeModal(
                                                        '${member.memberNo}',
                                                        '${member.memberName}',
                                                        '${member.memberId}',
                                                        '${member.nickname}',
                                                        '${empty member.grade ? 'BRONZE' : member.grade}'
                                                    )">
                                                등급 관리
                                            </button>

                                        </div>

                                    </td>
                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>
                            <tr><td colspan="7">조회된 회원이 없습니다.</td></tr>
                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <a href="?page=${pagination.currentPage-1}&keyword=${param.keyword}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?page=${p}&keyword=${param.keyword}"
                       class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?page=${pagination.currentPage+1}&keyword=${param.keyword}">›</a>

            </div>

        </section>

    </main>

</div>

<%-- 회원 등급 관리 팝업 --%>
<div class="modal-overlay" id="gradeModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>회원 등급 관리</h3>
            <span class="modal-close" onclick="closeGradeModal()">&times;</span>
        </div>

        <form id="gradeForm" action="${pageContext.request.contextPath}/admin/member/grade" method="post">

            <input type="hidden" name="memberNo" id="gradeMemberNo">

            <div class="target-info-box">
                <p><span>이름</span><strong id="gradeMemberName"></strong></p>
                <p><span>아이디</span><strong id="gradeMemberId"></strong></p>
                <p><span>닉네임</span><strong id="gradeMemberNickname"></strong></p>
                <p><span>현재 회원 등급</span><strong id="gradeMemberCurrent"></strong></p>
            </div>

            <div class="grade-select-box">

                <div class="grade-select-title">등급 변경하기</div>

                <div class="grade-option-list">
                    <label><input type="radio" name="grade" value="BRONZE">Bronze</label>
                    <label><input type="radio" name="grade" value="SILVER">Silver</label>
                    <label><input type="radio" name="grade" value="GOLD">Gold</label>
                    <label><input type="radio" name="grade" value="PLATINUM">Platinum</label>
                    <label><input type="radio" name="grade" value="VIP">VIP</label>
                </div>

            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">저장하기</button>
                <button type="button" class="btn btn-outline" onclick="closeGradeModal()">닫기</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
function openGradeModal(memberNo, memberName, memberId, nickname, currentGrade) {
    document.getElementById('gradeMemberNo').value = memberNo;
    document.getElementById('gradeMemberName').textContent = memberName;
    document.getElementById('gradeMemberId').textContent = memberId;
    document.getElementById('gradeMemberNickname').textContent = nickname;
    document.getElementById('gradeMemberCurrent').textContent = currentGrade;

    var radios = document.getElementsByName('grade');
    for (var i = 0; i < radios.length; i++) {
        radios[i].checked = (radios[i].value === currentGrade);
    }

    document.getElementById('gradeModal').classList.add('open');
}

function closeGradeModal() {
    document.getElementById('gradeModal').classList.remove('open');
}
</script>

</body>
</html>
