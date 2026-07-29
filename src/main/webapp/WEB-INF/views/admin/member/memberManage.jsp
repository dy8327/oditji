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

        <%-- 정지/복구/삭제 처리 결과 안내 (RedirectAttributes flash message) --%>
        <c:if test="${not empty message}">
            <div class="admin-flash-message">${message}</div>
        </c:if>

        <%-- 회원 현황 통계 카드 (클릭 시 해당 상태로 자동 조회) --%>
        <div class="member-stat-grid">

            <a class="stat-card ${empty status ? 'active' : ''}"
               href="?memberType=${memberType}">
                <span>총 회원</span>
                <strong>${memberStats.totalCount}명</strong>
            </a>

            <a class="stat-card ${status == 'ACTIVE' ? 'active' : ''}"
               href="?memberType=${memberType}&status=ACTIVE">
                <span>정상 회원</span>
                <strong>${memberStats.activeCount}명</strong>
            </a>

            <a class="stat-card ${status == 'BLOCKED' ? 'active' : ''}"
               href="?memberType=${memberType}&status=BLOCKED">
                <span>정지 회원</span>
                <strong>${memberStats.blockedCount}명</strong>
            </a>

            <a class="stat-card ${status == 'WITHDRAWN' ? 'active' : ''}"
               href="?memberType=${memberType}&status=WITHDRAWN">
                <span>탈퇴 회원 (자동삭제 예정)</span>
                <strong>${memberStats.withdrawnCount}명</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <%-- 회원 유형 탭: 전체 유저 / 일반 회원 / SNS 로그인 유저 / 사업자 회원.
                 상태 필터/검색어는 유지한 채 회원 유형만 바꿔서 다시 조회한다.
                 (다른 관리자 화면과 동일한 .tab-menu 컴포넌트를 재사용) --%>
            <div class="tab-menu">

                <a class="${empty memberType || memberType == 'all' ? 'active' : ''}"
                   href="?memberType=all&status=${status}&searchType=${searchType}&keyword=${param.keyword}">
                    전체 유저
                </a>

                <a class="${memberType == 'general' ? 'active' : ''}"
                   href="?memberType=general&status=${status}&searchType=${searchType}&keyword=${param.keyword}">
                    일반 회원
                </a>

                <a class="${memberType == 'sns' ? 'active' : ''}"
                   href="?memberType=sns&status=${status}&searchType=${searchType}&keyword=${param.keyword}">
                    SNS 로그인 유저
                </a>

                <a class="${memberType == 'business' ? 'active' : ''}"
                   href="?memberType=business&status=${status}&searchType=${searchType}&keyword=${param.keyword}">
                    사업자 회원
                </a>

            </div>

            <div class="toolbar">

                <form method="get" action="${pageContext.request.contextPath}/admin/member/list">

                    <input type="hidden" name="memberType" value="${memberType}">

                    <label for="memberStatus" class="sr-only">상태 필터</label>
                    <select id="memberStatus" name="status" class="filter-select">
                        <option value="" ${empty status ? 'selected' : ''}>전체 상태</option>
                        <option value="ACTIVE" ${status == 'ACTIVE' ? 'selected' : ''}>정상</option>
                        <option value="BLOCKED" ${status == 'BLOCKED' ? 'selected' : ''}>정지</option>
                        <option value="WITHDRAWN" ${status == 'WITHDRAWN' ? 'selected' : ''}>탈퇴</option>
                    </select>

                    <label for="memberSearchType" class="sr-only">검색 기준</label>
                    <select id="memberSearchType" name="searchType" class="filter-select">
                        <option value="all" ${empty searchType || searchType == 'all' ? 'selected' : ''}>전체</option>
                        <option value="name" ${searchType == 'name' ? 'selected' : ''}>이름</option>
                        <option value="id" ${searchType == 'id' ? 'selected' : ''}>아이디</option>
                        <option value="nickname" ${searchType == 'nickname' ? 'selected' : ''}>닉네임</option>
                        <option value="email" ${searchType == 'email' ? 'selected' : ''}>이메일</option>
                    </select>

                    <%--
                        화면에는 검색 설명을 별도로 노출하지 않으면서
                        보조 기술에는 입력창의 목적이 전달되도록 한다.
                    --%>
                    <label for="memberKeyword" class="sr-only">
                        회원 검색어
                    </label>

                    <input type="text"
                        id="memberKeyword"
                        class="page-search"
                        name="keyword"
                        value="${param.keyword}"
                        placeholder="이름, 아이디, 닉네임, 이메일 검색">

                    <button type="submit" class="btn btn-dark search-btn" aria-label="검색">검색</button>

                </form>

            </div>

            <%-- 체크박스로 선택한 회원을 정지/복구/완전삭제 처리하는 폼. 목록 테이블 전체를 감싼다. --%>
            <form id="memberBulkForm" method="post" action="${pageContext.request.contextPath}/admin/member/bulk">

                <input type="hidden" name="keyword" value="${param.keyword}">
                <input type="hidden" name="searchType" value="${searchType}">
                <input type="hidden" name="status" value="${status}">
                <input type="hidden" name="memberType" value="${memberType}">
                <input type="hidden" name="page" value="${pagination.currentPage}">

                <div class="bulk-action-bar">

                    <span class="bulk-label">
                        선택한 회원 : <strong id="selectedMemberCount">0</strong>명
                    </span>

                    <button type="submit" name="action" value="suspend"
                            id="bulkSuspendBtn" class="btn btn-outline" disabled
                            onclick="return confirmMemberBulkAction(event, '정지');">
                        정지
                    </button>

                    <button type="submit" name="action" value="restore"
                            id="bulkRestoreBtn" class="btn btn-primary" disabled
                            onclick="return confirmMemberBulkAction(event, '복구');">
                        복구
                    </button>

                    <button type="submit" name="action" value="delete"
                            id="bulkDeleteBtn" class="btn btn-danger" disabled
                            onclick="return confirmMemberBulkAction(event, '완전삭제');">
                        탈퇴
                    </button>

                </div>

                <table class="data-table">

                    <thead>
                        <tr>
                            <th class="checkbox-col">
                                <input type="checkbox" id="memberCheckAll"
                                       aria-label="전체 선택"
                                       onclick="toggleAllMembers(this)">
                            </th>
                            <th>회원번호</th>
                            <th>아이디</th>
                            <th>닉네임</th>
                            <th>이름</th>
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

                                        <td class="checkbox-col">
                                            <c:choose>
                                                <%--
                                                    본인이 직접 탈퇴하여 7일 후 자동삭제되는 회원(WITHDRAWN)은
                                                    관리자가 정지/복구/탈퇴를 선택할 대상이 아니므로
                                                    체크박스 자체를 비활성화한다. name 속성을 두지 않아
                                                    혹시라도 submit 되지 않도록 이중으로 막는다.
                                                --%>
                                                <c:when test="${member.status == 'WITHDRAWN'}">
                                                    <input type="checkbox"
                                                           disabled
                                                           aria-label="${member.memberName}은(는) 자동삭제 예정 회원으로 선택할 수 없습니다."
                                                           title="자동삭제 예정 회원은 선택할 수 없습니다.">
                                                </c:when>
                                                <c:otherwise>
                                                    <input type="checkbox"
                                                           class="member-check"
                                                           name="memberNos"
                                                           value="${member.memberNo}"
                                                           aria-label="${member.memberName} 선택"
                                                           onclick="updateSelectedMemberCount()">
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <td>${member.memberNo}</td>

                                        <td>
                                            ${member.memberId}
                                            <c:if test="${member.snsYn == 'Y'}">
                                                <span class="badge badge-gray">SNS</span>
                                            </c:if>
                                            <c:if test="${member.role == 'BUSINESS'}">
                                                <span class="badge badge-gray">사업자</span>
                                            </c:if>
                                        </td>

                                        <td>${member.nickname}</td>
                                        <td>${member.memberName}</td>
                                        <td>${member.email}</td>

                                        <td>

                                            <c:choose>
                                                <c:when test="${member.status == 'ACTIVE'}">
                                                    <span class="status-ok">정상</span>
                                                </c:when>
                                                <c:when test="${member.status == 'BLOCKED'}">
                                                    <span class="status-reject">정지</span>
                                                </c:when>
                                                <c:when test="${member.status == 'WITHDRAWN'}">
                                                    <span class="status-waiting">탈퇴 (자동삭제 예정)</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-waiting">알 수 없음</span>
                                                </c:otherwise>
                                            </c:choose>

                                        </td>

                                        <td>

                                            <div class="item-actions">

                                                <c:choose>

                                                    <c:when test="${member.status == 'ACTIVE'}">

                                                        <button type="button" class="btn btn-outline"
                                                                onclick="memberSuspend(${member.memberNo})">
                                                            정지
                                                        </button>

                                                        <button type="button" class="btn btn-danger"
                                                                onclick="memberWithdraw(${member.memberNo})">
                                                            탈퇴
                                                        </button>

                                                    </c:when>

                                                    <c:when test="${member.status == 'BLOCKED'}">

                                                        <button type="button" class="btn btn-primary"
                                                                onclick="memberRestore(${member.memberNo})">
                                                            복구
                                                        </button>

                                                        <button type="button" class="btn btn-danger"
                                                                onclick="memberWithdraw(${member.memberNo})">
                                                            탈퇴
                                                        </button>

                                                    </c:when>

                                                    <c:when test="${member.status == 'WITHDRAWN'}">
                                                        <span class="status-waiting">
                                                            탈퇴 처리됨
                                                            <c:choose>
                                                                <c:when test="${not empty member.remainingDeleteDays}">
                                                                    (자동삭제까지 D-${member.remainingDeleteDays})
                                                                </c:when>
                                                                <c:otherwise>
                                                                    (7일 후 자동 삭제)
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="status-waiting">알 수 없는 상태</span>
                                                    </c:otherwise>

                                                </c:choose>

                                            </div>

                                        </td>

                                    </tr>

                                </c:forEach>

                            </c:when>

                            <c:otherwise>

                                <tr>
                                    <td colspan="8">조회된 회원이 없습니다.</td>
                                </tr>

                            </c:otherwise>

                        </c:choose>

                    </tbody>

                </table>

            </form>

            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>


                <!-- 이전 페이지 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>


                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <!-- 다음 페이지 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>


                <!-- 다음 블록 -->
                <a href="?tab=${currentTab}&keyword=${param.keyword}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>

        </section>

    </main>

</div>

<%-- admin.js(정적 파일)는 JSTL을 쓸 수 없어, 개별 처리 버튼이 현재 검색/필터/페이지 상태를
     그대로 유지한 채 다시 목록으로 돌아갈 수 있도록 현재 상태값을 data-* 속성으로 전달한다. --%>
<div id="memberListState"
     data-context="${pageContext.request.contextPath}"
     data-keyword="${param.keyword}"
     data-search-type="${searchType}"
     data-status="${status}"
     data-member-type="${memberType}"
     data-page="${pagination.currentPage}"
     style="display:none"></div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
