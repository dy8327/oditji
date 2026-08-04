<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="activeMenu" value="settlement"/>

<%--
    상태(status) 필터의 현재 선택값. param이 없으면 '전체'를 의미하는 빈 문자열로 취급한다.
    eventManage.jsp와 동일한 방식.
--%>
<c:set var="currentStatus" value="${empty param.status ? '' : param.status}"/>

<%--
    기간(period) 필터의 현재 선택값. param이 없으면 '전체'를 의미하는 빈 문자열로 취급한다.
    eventManage.jsp의 기간 필터와 동일한 방식(신청일 기준).
--%>
<c:set var="currentPeriod" value="${empty param.period ? '' : param.period}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 정산 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main id="mainContent" class="main-content">

        <div class="admin-page-header">

            <a href="${pageContext.request.contextPath}/admin/main"
               class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="admin-page-title">
                정산 관리
            </h1>

            <p class="admin-page-desc">
                사업자가 요청한 정산 내역을 확인하고 지급 완료 또는 반려 처리할 수 있습니다.
            </p>

        </div>

        <%--
            상단 통계 카드. 각 카드는 해당 상태로 바로 필터링된 목록으로 이동하는 링크이며,
            현재 선택된 상태(status)와 일치하는 카드에는 active 클래스를 준다.
            memberManage.jsp / eventManage.jsp와 동일한 .member-stat-grid 컴포넌트를 재사용한다.
        --%>
        <div class="member-stat-grid">

            <a class="stat-card ${empty currentStatus ? 'active' : ''}"
               href="?status=">
                <span>전체 정산</span>
                <strong>${settlementStats.totalCount}건</strong>
            </a>

            <a class="stat-card ${currentStatus == 'REQUESTED' ? 'active' : ''}"
               href="?status=REQUESTED">
                <span>지급 대기</span>
                <strong>${settlementStats.requestedCount}건</strong>
            </a>

            <a class="stat-card ${currentStatus == 'DONE' ? 'active' : ''}"
               href="?status=DONE">
                <span>지급 완료</span>
                <strong>${settlementStats.doneCount}건</strong>
            </a>

            <a class="stat-card ${currentStatus == 'REJECTED' ? 'active' : ''}"
               href="?status=REJECTED">
                <span>반려</span>
                <strong>${settlementStats.rejectedCount}건</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <%--
                상태 탭. memberManage.jsp의 회원 유형 탭(.tab-menu)과 동일한 컴포넌트로,
                위쪽 통계 카드와 같은 상태(status) 값을 다루지만 목록 바로 위에서도
                탭 형태로 빠르게 전환할 수 있도록 제공한다. 검색어는 그대로 유지한다.
            --%>
            <div class="tab-menu">

                <a class="${empty currentStatus ? 'active' : ''}"
                   href="?status=&period=${currentPeriod}&keyword=${param.keyword}">
                    전체
                </a>

                <a class="${currentStatus == 'REQUESTED' ? 'active' : ''}"
                   href="?status=REQUESTED&period=${currentPeriod}&keyword=${param.keyword}">
                    지급 대기
                </a>

                <a class="${currentStatus == 'DONE' ? 'active' : ''}"
                   href="?status=DONE&period=${currentPeriod}&keyword=${param.keyword}">
                    지급 완료
                </a>

                <a class="${currentStatus == 'REJECTED' ? 'active' : ''}"
                   href="?status=REJECTED&period=${currentPeriod}&keyword=${param.keyword}">
                    반려
                </a>

            </div>

            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}/admin/settlement/main">

                    <%-- 상태 필터. 위쪽 통계 카드/탭 메뉴와 같은 값(status)을 다루지만, 검색창 옆에서도
                         memberManage.jsp / eventManage.jsp와 동일하게 select로 전환할 수 있도록 제공한다. --%>
                    <label for="settlementStatusFilter" class="sr-only">
                        상태 필터
                    </label>

                    <select id="settlementStatusFilter" name="status" class="filter-select">
                        <option value=""          ${empty currentStatus ? 'selected' : ''}>상태 전체</option>
                        <option value="REQUESTED" ${currentStatus == 'REQUESTED' ? 'selected' : ''}>지급 대기</option>
                        <option value="DONE"      ${currentStatus == 'DONE' ? 'selected' : ''}>지급 완료</option>
                        <option value="REJECTED"  ${currentStatus == 'REJECTED' ? 'selected' : ''}>반려</option>
                    </select>

                    <%-- 기간 필터. 정산 신청일(신청일) 기준으로 최근 건만 좁혀 볼 때 사용한다. eventManage.jsp와 동일한 구성. --%>
                    <label for="settlementPeriodFilter" class="sr-only">
                        기간 필터
                    </label>

                    <select id="settlementPeriodFilter" name="period" class="filter-select">
                        <option value=""      ${empty currentPeriod ? 'selected' : ''}>기간 전체</option>
                        <option value="today" ${currentPeriod == 'today' ? 'selected' : ''}>오늘</option>
                        <option value="week"  ${currentPeriod == 'week' ? 'selected' : ''}>최근 7일</option>
                        <option value="month" ${currentPeriod == 'month' ? 'selected' : ''}>최근 30일</option>
                    </select>

                    <%--
                        검색 input에 고유 id를 부여하고
                        label의 for 속성과 연결한다.
                    --%>
                    <label for="settlementKeyword"
                           style="position:absolute;
                                  width:1px;
                                  height:1px;
                                  padding:0;
                                  margin:-1px;
                                  overflow:hidden;
                                  clip:rect(0, 0, 0, 0);
                                  white-space:nowrap;
                                  border:0;">
                        사업자명 검색
                    </label>

                    <input type="text"
                        id="settlementKeyword"
                        class="page-search"
                        name="keyword"
                        value="${param.keyword}"
                        placeholder="사업자명 검색">

                    <button type="submit" class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

            </div>

            <table class="data-table">

                <thead>

                    <tr>
                        <th>사업자명</th>
                        <%--
                            [모바일 리팩토링] 정산 월 / 신청일 / 지급 계좌는 모바일에서는 숨기고,
                            "상세보기" 버튼을 누르면 열리는 settlementModal 안에서 확인하도록 한다.
                        --%>
                        <th class="col-mobile-hide">정산 월</th>
                        <th class="col-mobile-hide">신청일</th>
                        <th>정산 예정금</th>
                        <th class="col-mobile-hide">지급 계좌</th>
                        <%-- [수정] 모바일에서는 이 컬럼을 숨기고, 대신 사업자명 글자 색으로
                             지급 대기/완료/반려 상태를 표현한다(.approval-name-*, 아래 사업자명 td 참고).
                             businessManage.jsp와 동일한 방식. 데스크톱은 기존과 동일하게 상태 뱃지 컬럼이 그대로 보인다. --%>
                        <th class="col-mobile-hide">정산 상태</th>
                        <th>관리</th>
                    </tr>

                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty settlementList}">

                            <c:forEach var="settlement"
                                       items="${settlementList}">

                                <%--
                                    상세보기 모달로 넘겨줄 값들을 미리 변수로 정리해 둔다.
                                    (신청일 문자열, 상태 한글 라벨. eventManage.jsp의 reqCreatedAtStr /
                                    reqStatusLabel 등과 동일한 방식)
                                --%>
                                <fmt:formatDate var="settlementCreatedAtStr" value="${settlement.requestedAt}" pattern="yyyy-MM-dd"/>

                                <c:choose>
                                    <c:when test="${settlement.status == 'DONE'}">
                                        <c:set var="settlementStatusLabel" value="지급 완료"/>
                                    </c:when>
                                    <c:when test="${settlement.status == 'REJECTED'}">
                                        <c:set var="settlementStatusLabel" value="반려"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="settlementStatusLabel" value="대기"/>
                                    </c:otherwise>
                                </c:choose>

                                <%-- 모바일 상세보기 모달에 그대로 넘겨줄 정산 예정금 표시 문자열 --%>
                                <fmt:formatNumber var="settlementAmountStr" value="${settlement.settledAmount}" pattern="#,##0"/>

                                <%-- [수정] 모바일에서 정산 상태 컬럼을 숨기는 대신 사업자명 글자 색으로 상태를
                                     표현하기 위한 클래스. businessManage.jsp의 approval-name-* 와 동일한 방식이며,
                                     이 색은 768px 이하에서만 적용되므로 데스크톱에서는 기존 사업자명 색과 동일하게 보인다. --%>
                                <c:choose>
                                    <c:when test="${settlement.status == 'DONE'}">
                                        <c:set var="settlementNameStatusClass" value="approval-name-approved"/>
                                    </c:when>
                                    <c:when test="${settlement.status == 'REJECTED'}">
                                        <c:set var="settlementNameStatusClass" value="approval-name-rejected"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="settlementNameStatusClass" value="approval-name-waiting"/>
                                    </c:otherwise>
                                </c:choose>

                                <tr>

                                    <td class="approval-name-cell">
                                        <div class="approval-name-wrapper">
                                            <div class="mobile-approval-name">
                                                <span class="approval-name-text ${settlementNameStatusClass}">${settlement.businessName}</span>
                                            </div>
                                            <span class="pc-approval-name">${settlement.businessName}</span>
                                        </div>
                                    </td>

                                    <%-- [수정] 월별로 묶인 정산 기준 월 표시. 모바일에서는 숨기고 모달에서 확인한다. --%>
                                    <td class="col-mobile-hide">${settlement.settlementMonth}</td>

                                    <td class="col-mobile-hide">
                                        ${settlementRequestedAtStr}
                                    </td>

                                    <td>
                                        <fmt:formatNumber value="${settlement.settledAmount}" pattern="#,##0"/>원
                                    </td>

                                    <td class="col-mobile-hide">
                                        ${settlement.bankName}
                                        ${settlement.accountNumber}
                                        (${settlement.accountHolder})
                                    </td>

                                    <td class="col-mobile-hide">

                                        <c:choose>

                                            <c:when test="${settlement.status == 'DONE'}">

                                                <span class="status-ok">
                                                    지급 완료
                                                </span>

                                            </c:when>

                                            <c:when test="${settlement.status == 'REJECTED'}">

                                                <span class="status-reject">
                                                    반려
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="status-waiting">
                                                    대기
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td>

                                        <div class="item-actions"
                                             style="justify-content:center;">

                                            <%--
                                                [모바일 리팩토링] 모바일 전용 상세보기 트리거. 768px 이하에서는
                                                아래 입금확인/반려 버튼이 전부 숨겨지고 이 버튼만 남는다. 눌렀을 때
                                                settlementModal에 이 행의 전체 정보(정산월/신청일/입금계좌 등 모바일에서
                                                숨겨진 컬럼 포함)와 지금과 동일한 입금확인/반려 버튼을 모달 안에서 그대로
                                                보여준다. memberManage.jsp의 row-detail-trigger와 동일한 방식이며,
                                                데스크톱(768px 초과)에서는 이 버튼이 보이지 않고 기존 버튼이 그대로 노출된다.
                                            --%>
                                            <button type="button" class="btn btn-outline row-detail-trigger"
                                                    aria-label="${fn:escapeXml(settlement.businessName)} 정산 상세보기"
                                                    data-request-no="${settlement.requestNo}"
                                                    data-business-name="${fn:escapeXml(settlement.businessName)}"
                                                    data-settlement-month="${settlement.settlementMonth}"
                                                    data-requested-at="${settlementRequestedAtStr}"
                                                    data-settled-amount="${settlementAmountStr}원"
                                                    data-account="${fn:escapeXml(settlement.bankName)} ${fn:escapeXml(settlement.accountNumber)} (${fn:escapeXml(settlement.accountHolder)})"
                                                    data-status="${settlement.status}"
                                                    data-status-label="${settlementStatusLabel}"
                                                    onclick="openRowDetailModal('settlementModal', this)">
                                                상세
                                            </button>

                                            <%-- [수정] 관리자 처리는 지급 완료 요청 상태에서만 가능 --%>
                                            <c:if test="${settlement.status == 'REQUESTED'}">

                                            <form action="${pageContext.request.contextPath}/admin/settlement/confirm"
                                                  method="post">

                                                <%-- [수정] 주문상품 1건이 아닌 사업자/정산 월 전체를 처리 --%>
                                                <input type="hidden" name="requestNo" value="${settlement.requestNo}">
                                                <input type="hidden" name="keyword" value="${param.keyword}">
                                                <input type="hidden" name="status" value="${currentStatus}">
                                                <input type="hidden" name="period" value="${currentPeriod}">
                                                <input type="hidden" name="page" value="${pagination.currentPage}">

                                                <button type="submit" class="btn btn-success">
                                                    지급 완료
                                                </button>

                                            </form>

                                            <form action="${pageContext.request.contextPath}/admin/settlement/reject"
                                                  method="post">

                                                <%-- [수정] 주문상품 1건이 아닌 사업자/정산 월 전체를 처리 --%>
                                                <input type="hidden" name="requestNo" value="${settlement.requestNo}">
                                                <input type="text" name="rejectReason" class="page-search" placeholder="반려 사유"
                                                    maxlength="500" required>
                                                <input type="hidden" name="keyword" value="${param.keyword}">
                                                <input type="hidden" name="status" value="${currentStatus}">
                                                <input type="hidden" name="period" value="${currentPeriod}">
                                                <input type="hidden" name="page" value="${pagination.currentPage}">

                                                <button type="submit"
                                                        class="btn btn-danger">
                                                    반려
                                                </button>

                                            </form>

                                            </c:if>

                                            <%-- [수정] 데스크톱에서는 상세보기 버튼(.row-detail-trigger)이 숨겨져 있어
                                                 입금확인/반려 버튼도 없는 행(REQUESTED가 아닌 상태)은 관리 컬럼이
                                                 완전히 비어 보이므로 처리할 게 없다는 뜻으로 '-'를 보여준다.
                                                 모바일에서는 상세보기 버튼만 남기고 이 span도 함께 숨겨야 하므로
                                                 (텍스트 노드 그대로 두면 .item-actions > *:not(.row-detail-trigger)
                                                 숨김 규칙이 적용되지 않아 버튼 옆에 '-'가 그대로 남아 있었다) 반드시
                                                 span으로 감싸서 다른 관리 버튼들과 동일하게 처리한다. --%>
                                            <c:if test="${settlement.status != 'REQUESTED'}">
                                                <span>-</span>
                                            </c:if>

                                        </div>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>
                                <td colspan="7">
                                    정산 요청 내역이 없습니다.
                                </td>
                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>


                <!-- 이전 페이지 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>


                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <!-- 다음 페이지 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>


                <!-- 다음 블록 -->
                <a href="?status=${currentStatus}&period=${currentPeriod}&keyword=${param.keyword}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>

        </section>

    </main>

</div>

<%--
    [모바일 리팩토링] 정산 상세보기 모달.
    768px 이하에서 각 행의 "상세" 버튼을 누르면 열리며, 표에서 숨겨진 컬럼
    (정산월/신청일/입금계좌)과 입금확인/반려 버튼을 desktop 행과 동일하게 보여준다.
    값은 JS의 openRowDetailModal()이 row-detail-trigger 버튼의 data-* 값을 그대로
    채워 넣으므로, 이 화면 전용 JS 함수를 따로 만들 필요가 없다(memberManage.jsp와 동일한 방식).
--%>
<div class="modal-overlay" id="settlementModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>정산 상세정보</h3>
            <button type="button"
                    class="modal-close"
                    aria-label="정산 상세정보 팝업 닫기"
                    onclick="closeModal('settlementModal')">
                &times;
            </button>
        </div>

        <div class="row-detail-list">

            <div class="row-detail-item">
                <span class="row-detail-label">사업자명</span>
                <span class="row-detail-value" data-detail-field="businessName"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">정산 월</span>
                <span class="row-detail-value" data-detail-field="settlementMonth"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">신청일</span>
                <span class="row-detail-value" data-detail-field="requestedAt"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">정산 예정금</span>
                <span class="row-detail-value" data-detail-field="settledAmount"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">지급 계좌</span>
                <span class="row-detail-value" data-detail-field="account"></span>
            </div>

            <div class="row-detail-item">
                <span class="row-detail-label">정산 상태</span>
                <span class="row-detail-value" data-detail-field="statusLabel"></span>
            </div>

        </div>

        <%-- 데스크톱 행의 입금확인/반려 폼과 완전히 동일한 값을 그대로 제출한다.
             data-detail-toggle으로 현재 정산 상태(REQUESTED)에 맞을 때만 버튼을 보여준다. --%>
        <form action="${pageContext.request.contextPath}/admin/settlement/confirm" method="post"
                        data-detail-toggle="status:REQUESTED">

            <input type="hidden" name="requestNo" data-detail-field="requestNo">
            <input type="hidden" name="keyword" value="${param.keyword}">
            <input type="hidden" name="status" value="${currentStatus}">
            <input type="hidden" name="period" value="${currentPeriod}">
            <input type="hidden" name="page" value="${pagination.currentPage}">

            <div class="row-detail-actions">
                <button type="submit" class="btn btn-success">지급 완료</button>
            </div>
        </form>

        <form action="${pageContext.request.contextPath}/admin/settlement/reject" method="post"
                        data-detail-toggle="status:REQUESTED">

            <input type="hidden" name="requestNo" data-detail-field="requestNo">
            <input type="hidden" name="keyword" value="${param.keyword}">
            <input type="hidden" name="status" value="${currentStatus}">
            <input type="hidden" name="period" value="${currentPeriod}">
            <input type="hidden" name="page" value="${pagination.currentPage}">

            <div class="row-detail-actions">
                <input type="text" name="rejectReason" class="page-search" placeholder="반려 사유를 입력하세요"
                    maxlength="500" required>

                <button type="submit" class="btn btn-danger">반려</button>
            </div>
        </form>

        <div class="row-detail-actions">
            <span class="status-waiting" data-detail-toggle="status:DONE,REJECTED">
                이미 처리된 정산 요청입니다.
            </span>
        </div>
        <div class="modal-footer">
            <button type="button" class="btn btn-outline" onclick="closeModal('settlementModal')">닫기</button>
        </div>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>