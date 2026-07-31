<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    상태(tab) / 기간(period) 필터의 현재 선택값.
    param이 없으면 둘 다 '전체'를 의미하는 빈 문자열로 취급한다.

    [정리됨] 예전에는 미사용 기본값 'register'를 썼고, EVENT_PRODUCT/PRODUCT/BUSINESS와
    조인되는 목록 쿼리 특성상 tab이 비어 있으면 승인대기/승인완료/종료/반려 이벤트가
    구분 없이 한꺼번에 섞여 나왔는데도 화면에는 어떤 탭도 선택되지 않은 것처럼 보였다.
    지금은 '전체' 상태를 상단 통계 카드/상태 필터에서 명시적으로 선택할 수 있게 했다.
--%>
<c:set var="currentTab" value="${empty param.tab ? '' : param.tab}"/>
<c:set var="currentPeriod" value="${empty param.period ? '' : param.period}"/>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
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

            <h1 class="admin-page-title">
                이벤트 관리
            </h1>

            <p class="admin-page-desc">
                사업자가 요청한 이벤트 등록·수정 요청을 검토하고 승인 상태를 관리합니다. 
                이벤트는 종료일이 지나면 자동으로 종료 처리됩니다.
            </p>

        </div>

        <%--
            상단 통계 카드.
            각 카드는 해당 상태로 바로 필터링된 목록으로 이동하는 링크이며,
            현재 선택된 상태(tab)와 일치하는 카드에는 active 클래스를 준다.
            memberManage.jsp와 동일하게 admin-content-box 바깥(페이지 상단)에 별도로 배치한다.
        --%>
        <div class="member-stat-grid">

            <a class="stat-card ${empty currentTab ? 'active' : ''}"
               href="?tab=">
                <span>전체 이벤트</span>
                <strong>${eventStats.totalCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'waiting' ? 'active' : ''}"
               href="?tab=waiting">
                <span>승인 대기</span>
                <strong>${eventStats.waitingCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'approved' ? 'active' : ''}"
               href="?tab=approved">
                <span>승인 완료</span>
                <strong>${eventStats.approvedCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'end' ? 'active' : ''}"
               href="?tab=end">
                <span>종료 이벤트</span>
                <strong>${eventStats.endCount}건</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <%--
                상태 탭. memberManage.jsp의 회원 유형 탭(.tab-menu)과 동일한 컴포넌트로,
                위쪽 통계 카드와 같은 상태(tab) 값을 다루지만 목록 바로 위에서도
                탭 형태로 빠르게 전환할 수 있도록 제공한다. 기간/검색어는 그대로 유지한다.
            --%>
            <div class="tab-menu">

                <a class="${empty currentTab ? 'active' : ''}"
                   href="?tab=&period=${currentPeriod}&keyword=${param.keyword}">
                    전체
                </a>

                <a class="${currentTab == 'waiting' ? 'active' : ''}"
                   href="?tab=waiting&period=${currentPeriod}&keyword=${param.keyword}">
                    승인 대기
                </a>

                <a class="${currentTab == 'approved' ? 'active' : ''}"
                   href="?tab=approved&period=${currentPeriod}&keyword=${param.keyword}">
                    승인 완료
                </a>

                <a class="${currentTab == 'end' ? 'active' : ''}"
                   href="?tab=end&period=${currentPeriod}&keyword=${param.keyword}">
                    종료
                </a>

            </div>

            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}/admin/event/list">

                    <%--
                        상태 필터. 통계 카드로도 이동할 수 있지만,
                        키보드/스크린 리더 사용자를 위해 동일한 기능을 select로도 제공한다.
                    --%>
                    <label for="eventStatusFilter"
                           class="sr-only">
                        상태 필터
                    </label>

                    <select id="eventStatusFilter" name="tab" class="filter-select">
                        <option value=""         ${empty currentTab ? 'selected' : ''}>상태 전체</option>
                        <option value="waiting"  ${currentTab == 'waiting' ? 'selected' : ''}>승인 대기</option>
                        <option value="approved" ${currentTab == 'approved' ? 'selected' : ''}>승인 완료</option>
                        <option value="end"      ${currentTab == 'end' ? 'selected' : ''}>종료</option>
                    </select>

                    <%--
                        기간 필터. 이벤트의 요청일(등록일) 기준으로 최근 건만 좁혀 볼 때 사용한다.
                    --%>
                    <label for="eventPeriodFilter" class="sr-only">
                        기간 필터
                    </label>

                    <select id="eventPeriodFilter"
                            name="period"
                            class="filter-select">
                        <option value=""      ${empty currentPeriod ? 'selected' : ''}>기간 전체</option>
                        <option value="today" ${currentPeriod == 'today' ? 'selected' : ''}>오늘</option>
                        <option value="week"  ${currentPeriod == 'week' ? 'selected' : ''}>최근 7일</option>
                        <option value="month" ${currentPeriod == 'month' ? 'selected' : ''}>최근 30일</option>
                    </select>

                    <%--
                        검색창과 label을 for/id로 연결하여
                        키보드 사용자와 화면 낭독기가 검색 목적을 명확히 인식하도록 합니다.
                    --%>
                    <label for="eventKeyword" class="sr-only">
                        사업자명 또는 이벤트명 검색
                    </label>

                    <input type="text" id="eventKeyword" class="page-search" name="keyword"
                           value="${param.keyword}"
                           placeholder="사업자명, 이벤트명 검색">

                    <button type="submit" class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

            </div>

            <table class="data-table">

                <thead>

                    <tr>

                        <th class="col-mobile-hide">번호</th>
                        <th>사업자명</th>
                        <th>이벤트명</th>
                        <th class="col-mobile-hide">적용 상품</th>
                        <th class="col-mobile-hide">이벤트 기간</th>
                        <th class="col-mobile-hide">요청일</th>
                        <th class="col-mobile-hide">상태</th>
                        <th>관리</th>

                    </tr>

                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty eventRequestList}">

                            <c:forEach var="req"
                                       items="${eventRequestList}">

                                <%--
                                    상세보기 팝업에서 쓸 값들을 미리 변수로 정리해 둔다.
                                    (이벤트 기간 문자열, 상태 한글 라벨, JS로 넘길 때 따옴표가
                                    깨지지 않도록 처리한 값)
                                --%>
                                <fmt:formatDate var="reqStartDateStr" value="${req.startDate}" pattern="yyyy-MM-dd"/>
                                <fmt:formatDate var="reqEndDateStr" value="${req.endDate}" pattern="yyyy-MM-dd"/>
                                <fmt:formatDate var="reqCreatedAtStr" value="${req.createdAt}" pattern="yyyy-MM-dd"/>

                                <%--
                                    사업자가 등록한 이벤트 배너 이미지(EVENT.BANNER_IMAGE) URL.
                                    productManage.jsp의 reqMainImageUrl과 동일하게, DB에는 contextPath
                                    기준으로 바로 접근 가능한 상대경로가 저장되어 있다고 가정한다.
                                --%>
                                <c:choose>
                                    <c:when test="${not empty req.bannerImage}">
                                        <c:set var="reqBannerImageUrl"
                                               value="${pageContext.request.contextPath}${req.bannerImage}"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="reqBannerImageUrl" value=""/>
                                    </c:otherwise>
                                </c:choose>

                                <c:choose>
                                    <c:when test="${req.status == 'APPROVED'}">
                                        <c:set var="reqStatusLabel" value="승인"/>
                                    </c:when>
                                    <c:when test="${req.status == 'REJECTED'}">
                                        <c:set var="reqStatusLabel" value="반려"/>
                                    </c:when>
                                    <c:when test="${req.status == 'END'}">
                                        <c:set var="reqStatusLabel" value="종료"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="reqStatusLabel" value="대기"/>
                                    </c:otherwise>
                                </c:choose>

                                <tr>

                                    <td class="col-mobile-hide">
                                        ${req.eventNo}
                                    </td>


                                    <td>
                                        ${req.businessName}
                                    </td>


                                    <td class="event-title-cell">

                                        <div class="event-title-wrapper">

                                            <!-- 모바일용 -->
                                            <div class="mobile-event-title">

                                                <c:choose>

                                                    <c:when test="${req.status == 'APPROVED'}">
                                                        <span class="event-title-text event-title-approved">
                                                            ${req.title}
                                                        </span>
                                                    </c:when>

                                                    <c:when test="${req.status == 'REJECTED'}">
                                                        <span class="event-title-text event-title-rejected">
                                                            ${req.title}
                                                        </span>
                                                    </c:when>

                                                    <c:when test="${req.status == 'END'}">
                                                        <span class="event-title-text event-title-end">
                                                            ${req.title}
                                                        </span>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <span class="event-title-text event-title-waiting">
                                                            ${req.title}
                                                        </span>
                                                    </c:otherwise>


                                                </c:choose>

                                            </div>


                                            <!-- PC용 -->
                                            <span class="pc-event-title">
                                                ${req.title}
                                            </span>


                                        </div>

                                    </td>


                                    <%--
                                        연결 상품 개수
                                        모바일 숨김
                                    --%>
                                    <td class="col-mobile-hide">

                                        <c:choose>

                                            <c:when test="${not empty req.productDetail}">
                                                ${fn:length(fn:split(req.productDetail, ';'))}개 상품
                                            </c:when>

                                            <c:otherwise>
                                                -
                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <!-- 이벤트 기간 : 모바일 숨김 -->
                                    <td class="col-mobile-hide">

                                        ${reqStartDateStr} ~ ${reqEndDateStr}

                                    </td>


                                    <!-- 요청일 : 모바일 숨김 -->
                                    <td class="col-mobile-hide">

                                        ${reqCreatedAtStr}

                                    </td>


                                    <!-- 상태 : 모바일 숨김 -->
                                    <td class="col-mobile-hide">

                                        <c:choose>

                                            <c:when test="${req.status == 'APPROVED'}">
                                                <span class="status-ok">
                                                    승인
                                                </span>
                                            </c:when>

                                            <c:when test="${req.status == 'REJECTED'}">
                                                <span class="status-reject">
                                                    반려
                                                </span>
                                            </c:when>

                                            <c:when test="${req.status == 'END'}">
                                                <span class="status-end">
                                                    종료
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

                                        <%--
                                            businessName/title/productDetail 등에 따옴표나 줄바꿈이
                                            섞여도 onclick 인라인 문자열이 깨지지 않도록
                                            data-* 속성으로 값을 전달한다.
                                        --%>
                                        <button type="button"
                                                class="btn btn-dark"
                                                onclick="openEventDetailModal(
                                                    '${req.eventNo}',
                                                    '${fn:escapeXml(req.businessName)}',
                                                    '${fn:escapeXml(req.title)}',
                                                    '${reqStartDateStr} ~ ${reqEndDateStr}',
                                                    '${reqCreatedAtStr}',
                                                    '${req.status}',
                                                    '${reqStatusLabel}',
                                                    '${fn:escapeXml(req.productDetail)}',
                                                    '${fn:escapeXml(reqBannerImageUrl)}'
                                                )">

                                            상세보기

                                        </button>

                                    </td>


                                </tr>

                            </c:forEach>

                        </c:when>


                        <c:otherwise>

                            <tr>

                                <td colspan="8">

                                    <c:choose>

                                        <c:when test="${currentTab == 'approved'}">
                                            승인 완료된 이벤트가 없습니다.
                                        </c:when>

                                        <c:when test="${currentTab == 'end'}">
                                            종료된 이벤트가 없습니다.
                                        </c:when>

                                        <c:when test="${currentTab == 'waiting'}">
                                            승인 대기 이벤트가 없습니다.
                                        </c:when>

                                        <c:otherwise>
                                            조건에 맞는 이벤트가 없습니다.
                                        </c:otherwise>

                                    </c:choose>

                                </td>

                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

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

<%--
    이벤트 요청 상세 및 승인·반려 팝업입니다.

    role="dialog":
    현재 영역이 일반 콘텐츠가 아닌 대화상자임을 화면 낭독기에 알립니다.

    aria-modal="true":
    팝업이 열린 동안 배경 영역과 분리된 모달임을 알립니다.

    aria-labelledby:
    팝업 제목 요소와 모달을 연결합니다.
--%>
<div class="modal-overlay" id="eventRequestModal" role="dialog" aria-modal="true"
     aria-labelledby="eventRequestModalTitle">

    <div class="modal-box modal-box-lg">

        <div class="modal-header">

            <h3 id="eventRequestModalTitle">
                이벤트 상세
            </h3>

            <%--
                클릭 가능한 span 대신 기본 키보드 동작을 지원하는 button을 사용합니다.

                aria-label은 화면에 표시된 닫기 기호(×)의 목적을
                화면 낭독기 사용자에게 명확하게 전달합니다.
            --%>
            <button type="button" class="modal-close" aria-label="이벤트 상세 팝업 닫기"
                    onclick="closeModal('eventRequestModal')">
                &times;
            </button>

        </div>

        <div class="detail-section-title">
            이벤트 정보
        </div>

        <%--
            사업자가 등록한 이벤트 배너 이미지. openEventDetailModal()이 bannerImage 값을 보고
            이미지 또는 안내 문구 중 하나를 보여준다. productManage.jsp의 이미지 영역과 동일한 구조.
        --%>
        <div class="detail-image-box" id="reqEventImageBox">
            <img id="reqEventImage" src="" alt="이벤트 배너 이미지" style="display:none">
            <span id="reqEventImageEmpty" class="detail-image-empty">등록된 이미지가 없습니다.</span>
        </div>

        <div class="target-info-box">

            <p>
                <span>사업자명</span>
                <strong id="reqBusinessName"></strong>
            </p>

            <p>
                <span>이벤트명</span>
                <strong id="reqtitle"></strong>
            </p>

            <p>
                <span>기간</span>
                <strong id="reqEventPeriod"></strong>
            </p>

            <p>
                <span>요청일</span>
                <strong id="reqCreatedAt"></strong>
            </p>

            <p>
                <span>상태</span>
                <strong id="reqStatus"></strong>
            </p>

        </div>

        <div class="detail-section-title">
            적용 상품
        </div>

        <table class="data-table">

            <thead>
                <tr>
                    <th>상품명</th>
                    <th>판매자</th>
                    <th>기존가격</th>
                    <th>할인율</th>
                    <th>적용가격</th>
                </tr>
            </thead>

            <tbody id="reqProductTableBody">
                <%-- openEventDetailModal()이 상품 목록을 채워 넣는다 --%>
            </tbody>

        </table>

        <form id="eventRequestForm" action="${pageContext.request.contextPath}/admin/event/approve"
              method="post">

            <input type="hidden" name="eventNo" id="reqeventNo">
            <input type="hidden" name="tab" value="${currentTab}">
            <input type="hidden" name="period" value="${currentPeriod}">
            <input type="hidden" name="keyword"value="${param.keyword}">
            <input type="hidden" name="page" value="${pagination.currentPage}">


            <%--
                이미 승인/반려/종료 처리된 이벤트는 다시 승인·반려할 수 없다
                (adminMapper.xml의 updateEventStatus가 STATUS='WAITING'인 건만 갱신하며,
                이미 처리된 이벤트에 다시 승인/반려를 시도하면 0건 갱신으로 처리되어 오류가 발생한다).
                그래서 대기 상태가 아닐 때는 승인/반려 버튼 대신 안내 문구만 보여주고,
                실제 표시 여부는 openEventDetailModal()이 상태값을 보고 JS로 전환한다.
            --%>
            <p id="eventReadonlyNote" class="modal-readonly-note" style="display:none">
                이미 처리된 요청이라 승인·반려할 수 없습니다.
            </p>

            <div class="modal-footer" id="eventRequestActions">

                <button type="submit" id="eventApproveBtn" class="btn btn-success">
                    승인
                </button>
                <button type="submit" id="eventRejectBtn" formaction="${pageContext.request.contextPath}/admin/event/reject"
                        class="btn btn-danger">
                    반려
                </button>
                <button type="button" class="btn btn-outline" onclick="closeModal('eventRequestModal')">
                    닫기
                </button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
