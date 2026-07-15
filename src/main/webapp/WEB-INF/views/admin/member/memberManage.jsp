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
                가입한 회원의 정보를 조회하고 정지, 복구, 탈퇴(즉시 완전삭제)를 관리할 수 있습니다.
                회원이 마이페이지에서 직접 탈퇴한 경우 7일 후 자동으로 삭제됩니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/member/list">
                    <input type="text"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="이름, 아이디, 닉네임, 이메일 검색">
                </form>
            </div>

            <table class="data-table">

                <thead>
                    <tr>
                        <th>이름</th>
                        <th>아이디</th>
                        <th>닉네임</th>
                        <th>이메일</th>
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

                                <td>

                                <div class="item-actions">

                                    <c:choose>

                                        <c:when test="${member.status == 'ACTIVE'}">

                                            <form action="${pageContext.request.contextPath}/admin/member/suspend"
                                                method="post">

                                                <input type="hidden"
                                                    name="memberNo"
                                                    value="${member.memberNo}">

                                                <button type="submit"
                                                        class="btn btn-outline">
                                                    정지
                                                </button>

                                            </form>


                                            <form action="${pageContext.request.contextPath}/admin/member/withdraw"
                                                method="post"
                                                onsubmit="return confirm('해당 회원 데이터를 완전히 삭제하시겠습니까?\\n삭제 후 복구할 수 없습니다.');">

                                                <input type="hidden"
                                                    name="memberNo"
                                                    value="${member.memberNo}">

                                                <button type="submit"
                                                        class="btn btn-danger">
                                                    탈퇴(완전삭제)
                                                </button>

                                            </form>

                                        </c:when>

                                        <c:when test="${member.status == 'BLOCKED'}">

                                            <form action="${pageContext.request.contextPath}/admin/member/restore"
                                                method="post">

                                                <input type="hidden"
                                                    name="memberNo"
                                                    value="${member.memberNo}">

                                                <button type="submit"
                                                        class="btn btn-primary">
                                                    복구
                                                </button>

                                            </form>


                                            <form action="${pageContext.request.contextPath}/admin/member/withdraw"
                                                method="post"
                                                onsubmit="return confirm('해당 회원 데이터를 완전히 삭제하시겠습니까?\\n삭제 후 복구할 수 없습니다.');">

                                                <input type="hidden"
                                                    name="memberNo"
                                                    value="${member.memberNo}">

                                                <button type="submit"
                                                        class="btn btn-danger">
                                                    탈퇴(완전삭제)
                                                </button>

                                            </form>

                                        </c:when>

                                        <c:when test="${member.status == 'WITHDRAWN'}">

                                            <span class="status-waiting">
                                                탈퇴 처리됨 (7일 후 자동 삭제)
                                            </span>

                                        </c:when>

                                        <c:otherwise>

                                            <span class="status-waiting">
                                                알 수 없는 상태
                                            </span>

                                        </c:otherwise>


                                    </c:choose>

                                </div>

                            </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>
                                <td colspan="6">조회된 회원이 없습니다.</td>
                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <a href="?page=${pagination.currentPage-1}&keyword=${param.keyword}">‹</a>

                <c:forEach var="p"
                           begin="1"
                           end="${empty pagination.totalPages ? 1 : pagination.totalPages}">

                    <a href="?page=${p}&keyword=${param.keyword}"
                       class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>

                <a href="?page=${pagination.currentPage+1}&keyword=${param.keyword}">›</a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>