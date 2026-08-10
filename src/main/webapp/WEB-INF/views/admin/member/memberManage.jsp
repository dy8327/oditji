<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="activeMenu" value="member"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 회원 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main id="mainContent" class="main-content">

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
                                       onchange="toggleAllMembers(this)">
                            </th>
                            <th>회원번호</th>
                            <th>아이디</th>
                            <th class="col-mobile-hide">닉네임</th>
                            <th class="col-mobile-hide">이름</th>
                            <th class="col-mobile-hide">이메일</th>
                            <%-- [수정] 모바일에서는 이 컬럼을 숨기고, 대신 아이디 글자 색으로
                                 정상/정지/탈퇴 상태를 표현한다(.id-status-*, 아래 아이디 td 참고).
                                 데스크톱은 기존과 동일하게 상태 뱃지 컬럼이 그대로 보인다. --%>
                            <th class="col-mobile-hide">상태</th>
                            <th>관리</th>
                        </tr>
                    </thead>

                    <tbody>

                        <c:choose>

                            <c:when test="${not empty memberList}">

                                <c:forEach var="member" items="${memberList}">

                                    <%-- 상세보기 모달에 그대로 넘겨줄 상태 한글 라벨 (표의 상태 뱃지와 동일한 문구) --%>
                                    <c:choose>
                                        <c:when test="${member.status == 'ACTIVE'}">
                                            <c:set var="memberStatusLabel" value="정상"/>
                                        </c:when>
                                        <c:when test="${member.status == 'BLOCKED'}">
                                            <c:set var="memberStatusLabel" value="정지"/>
                                        </c:when>
                                        <c:when test="${member.status == 'WITHDRAWN'}">
                                            <c:set var="memberStatusLabel" value="탈퇴 (자동삭제 예정)"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="memberStatusLabel" value="알 수 없음"/>
                                        </c:otherwise>
                                    </c:choose>

                                    <%-- [수정] 모바일에서 상태 컬럼을 숨기는 대신 아이디 글자 색으로 상태를
                                         표현하기 위한 클래스. 데스크톱에서는 .id-status-*에 아무 스타일도
                                         없어(768px 이하에서만 색이 적용됨) 기존 아이디 색과 동일하게 보인다. --%>
                                    <c:choose>
                                        <c:when test="${member.status == 'ACTIVE'}">
                                            <c:set var="memberIdStatusClass" value="id-status-active"/>
                                        </c:when>
                                        <c:when test="${member.status == 'BLOCKED'}">
                                            <c:set var="memberIdStatusClass" value="id-status-blocked"/>
                                        </c:when>
                                        <c:when test="${member.status == 'WITHDRAWN'}">
                                            <c:set var="memberIdStatusClass" value="id-status-withdrawn"/>
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="memberIdStatusClass" value=""/>
                                        </c:otherwise>
                                    </c:choose>

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
                                                           onchange="updateSelectedMemberCount()">
                                                </c:otherwise>
                                            </c:choose>
                                        </td>

                                        <td>${member.memberNo}</td>

                                        <%--
                                            [수정] SNS 로그인 회원의 아이디를 모바일에서 provider 로고 아이콘으로
                                            대체하던 방식을 걷어내고, 모바일에서도 데스크톱과 동일하게 전체 아이디
                                            문자열을 그대로 보여준다. 대신 "상태" 컬럼을 모바일에서 숨기는 만큼
                                            (위 <th class="col-mobile-hide">상태</th> 참고) 아이디 글자 자체에
                                            memberIdStatusClass(.id-status-active/blocked/withdrawn)를 입혀 정상/
                                            정지/탈퇴 상태를 표현한다. 이 색은 768px 이하에서만 적용되므로 데스크톱
                                            에서는 상태 뱃지 컬럼이 있는 기존 모습 그대로다.
                                        --%>
                                        <td class="member-id-cell">

                                            <button type="button"
                                                    class="member-id-text ${memberIdStatusClass}"
                                                    title="${member.memberId}"
                                                    aria-haspopup="dialog">
                                                ${member.memberId}
                                            </button>

                                            <c:if test="${member.snsYn == 'Y'}">
                                                <span class="badge badge-gray">SNS</span>
                                            </c:if>

                                            <c:if test="${member.role == 'BUSINESS'}">
                                                <span class="badge badge-gray">사업자</span>
                                            </c:if>

                                        </td>

                                        <td class="col-mobile-hide">${member.nickname}</td>
                                        <td class="col-mobile-hide">${member.memberName}</td>
                                        <td class="col-mobile-hide">${member.email}</td>

                                        <td class="col-mobile-hide">

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

                                                <%--
                                                    모바일 전용 상세보기 트리거. 768px 이하에서는 아래의 정지/복구/탈퇴
                                                    버튼이 전부 숨겨지고 이 버튼만 남는다. 눌렀을 때 memberDetailModal에
                                                    이 행의 전체 정보(닉네임/이름/이메일 등 숨겨진 컬럼 포함)와 지금과
                                                    동일한 정지/복구/탈퇴 버튼을 모달 안에서 그대로 보여준다.
                                                    [수정] "관리" 컬럼 전체 폭을 채우던 버튼을 최소화된 크기로 줄이고
                                                    (admin.css .row-detail-trigger 참고), 표시 글자도 "상세"로 줄인
                                                    대신 aria-label로 회원을 특정할 수 있는 전체 문구를 제공한다.
                                                --%>
                                                <button type="button" class="btn btn-outline row-detail-trigger"
                                                        aria-label="${member.memberId} 상세보기"
                                                        data-member-no="${member.memberNo}"
                                                        data-member-id="${member.memberId}"
                                                        data-nickname="${member.nickname}"
                                                        data-member-name="${member.memberName}"
                                                        data-email="${member.email}"
                                                        data-status="${member.status}"
                                                        data-status-label="${memberStatusLabel}"
                                                        onclick="openRowDetailModal('memberDetailModal', this)">
                                                    상세
                                                </button>

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

<%--
    [모바일 리팩토링] 회원 상세보기 모달.
    768px 이하에서 각 행의 "상세보기" 버튼을 누르면 열리며, 표에서 숨겨진 컬럼
    (닉네임/이름/이메일)과 정지/복구/탈퇴 버튼을 desktop 행과 동일하게 보여준다.
    값은 JS의 openRowDetailModal()이 row-detail-trigger 버튼의 data-* 값을 그대로
    채워 넣으므로, 이 화면 전용 JS 함수를 따로 만들 필요가 없다.
--%>
<div class="modal-overlay" id="memberDetailModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>회원 상세정보</h3>
            <button type="button"
                    class="modal-close"
                    aria-label="회원 상세정보 팝업 닫기"
                    onclick="closeModal('memberDetailModal')">
                &times;
            </button>
        </div>

        <input type="hidden" id="detailMemberNo" data-detail-field="memberNo">

        <div class="row-detail-list">

            <div class="row-detail-item">
                <span class="row-detail-label">아이디</span>
                <span class="row-detail-value" data-detail-field="memberId"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">닉네임</span>
                <span class="row-detail-value" data-detail-field="nickname"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">이름</span>
                <span class="row-detail-value" data-detail-field="memberName"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">이메일</span>
                <span class="row-detail-value" data-detail-field="email"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">상태</span>
                <span class="row-detail-value" data-detail-field="statusLabel"></span>
            </div>

        </div>

        <%-- 데스크톱 행의 정지/복구/탈퇴 버튼과 완전히 동일한 함수를 그대로 호출한다.
             data-detail-toggle으로 현재 회원 상태에 맞는 버튼만 보여준다. --%>
        <div class="row-detail-actions">

            <button type="button" class="btn btn-outline"
                    data-detail-toggle="status:ACTIVE"
                    onclick="memberSuspend(document.getElementById('detailMemberNo').value)">
                정지
            </button>

            <button type="button" class="btn btn-primary"
                    data-detail-toggle="status:BLOCKED"
                    onclick="memberRestore(document.getElementById('detailMemberNo').value)">
                복구
            </button>

            <button type="button" class="btn btn-danger"
                    data-detail-toggle="status:ACTIVE,BLOCKED"
                    onclick="memberWithdraw(document.getElementById('detailMemberNo').value)">
                탈퇴
            </button>

            <span class="status-waiting" data-detail-toggle="status:WITHDRAWN">
                탈퇴 처리됨 (자동삭제 예정)
            </span>

        </div>

        <div class="modal-footer">
            <button type="button" class="btn btn-outline" onclick="closeModal('memberDetailModal')">닫기</button>
        </div>

    </div>

</div>

<%--
    [신규] 아이디 전체보기 모달.
    768px 이하에서 말줄임(...)된 아이디(.member-id-text)를 클릭하면 열리며,
    admin.js의 openMemberIdModal()이 값을 채워 넣는다. monitoring.jsp에도
    동일한 id로 하나씩 둔다(페이지당 하나만 렌더링되므로 id 충돌 없음).
    이 화면은 닉네임을 별도로 보여주지 않으므로 닉네임 줄은 항상 숨겨진다.
--%>
<dialog class="modal-overlay" id="memberIdModal" open aria-modal="true"
        aria-labelledby="memberIdModalTitle">

    <div class="modal-box">

        <div class="modal-header">
            <h3 id="memberIdModalTitle">아이디</h3>
            <button type="button" class="modal-close" aria-label="아이디 팝업 닫기"
                    onclick="closeModal('memberIdModal')">
                &times;
            </button>
        </div>

        <p class="member-id-modal-value" id="memberIdModalValue"></p>
        <p class="member-id-modal-nickname" id="memberIdModalNickname" style="display:none"></p>

        <div class="modal-footer">
            <button type="button" class="btn btn-outline" onclick="closeModal('memberIdModal')">닫기</button>
        </div>

    </div>

</dialog>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
