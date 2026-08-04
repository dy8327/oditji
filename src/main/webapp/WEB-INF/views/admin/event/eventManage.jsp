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

    <main id="mainContent" class="main-content">

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

            [추가] "승인상태"(대기/승인/반려)와 "현재상태"(진행중/예정/종료)를
            컬럼으로 분리한 뒤에도 카드는 예전 그대로(전체/대기/승인/종료)라
            반려·진행중·예정 이벤트는 카드로 바로 필터링해 볼 수 없었다.
            세 카드를 추가한다(tab=rejected/ongoing/upcoming, adminMapper.xml의
            selectEventStats/selectAdminEventList(Count) 참고).
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

            <a class="stat-card ${currentTab == 'rejected' ? 'active' : ''}"
               href="?tab=rejected">
                <span>반려</span>
                <strong>${eventStats.rejectedCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'ongoing' ? 'active' : ''}"
               href="?tab=ongoing">
                <span>진행중인 이벤트</span>
                <strong>${eventStats.ongoingCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'upcoming' ? 'active' : ''}"
               href="?tab=upcoming">
                <span>예정 이벤트</span>
                <strong>${eventStats.upcomingCount}건</strong>
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

                <a class="${currentTab == 'rejected' ? 'active' : ''}"
                   href="?tab=rejected&period=${currentPeriod}&keyword=${param.keyword}">
                    반려
                </a>

                <a class="${currentTab == 'ongoing' ? 'active' : ''}"
                   href="?tab=ongoing&period=${currentPeriod}&keyword=${param.keyword}">
                    진행중
                </a>

                <a class="${currentTab == 'upcoming' ? 'active' : ''}"
                   href="?tab=upcoming&period=${currentPeriod}&keyword=${param.keyword}">
                    예정
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
                        <option value="rejected" ${currentTab == 'rejected' ? 'selected' : ''}>반려</option>
                        <option value="ongoing"  ${currentTab == 'ongoing' ? 'selected' : ''}>진행중</option>
                        <option value="upcoming" ${currentTab == 'upcoming' ? 'selected' : ''}>예정</option>
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
                        <%--
                            [수정] 하나의 "상태" 컬럼에 승인 여부(대기/승인/반려)와
                            진행 상태(진행중/예정/종료)가 섞여 있어 뱃지 하나만 보고는
                            승인은 됐는데 아직 시작 전인지, 이미 끝난 이벤트인지 구분이
                            안 됐다. 두 컬럼으로 나눠서 보여준다.
                        --%>
                        <th class="col-mobile-hide">승인상태</th>
                        <th class="col-mobile-hide">현재상태</th>
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

                                <%--
                                    승인상태(대기/승인/반려)·현재상태(진행중/예정/종료) 한글 라벨은
                                    더 이상 여기서 계산하지 않는다. EventManageVO의
                                    getApprovalStatusLabel()/getProgressStatusLabel()이 STATUS와
                                    START_DATE/END_DATE를 기준으로 직접 계산해 주므로
                                    ${req.approvalStatusLabel} / ${req.progressStatusLabel}로 바로 쓴다.
                                --%>

                                <tr>

                                    <td class="col-mobile-hide">
                                        ${req.eventNo}
                                    </td>


                                    <td>
                                        ${req.businessName}
                                    </td>


                                    <td class="event-title-cell">

                                        <div class="event-title-wrapper">

                                            <%--
                                                [리팩토링] 색을 원본 EVENT.STATUS(WAITING/APPROVED/REJECTED/END)
                                                4가지 대신, 데스크톱 "승인상태" 컬럼과 동일한 req.approvalStatus
                                                (WAITING/APPROVED/REJECTED) 3가지 기준으로 맞춘다. END는 승인상태
                                                관점에서 여전히 승인(APPROVED)이므로(EventManageVO.getApprovalStatus()
                                                참고) 별도 회색이 아니라 승인과 같은 초록으로 보여준다.
                                            --%>
                                            <!-- 모바일용 -->
                                            <div class="mobile-event-title">

                                                <c:choose>

                                                    <c:when test="${req.approvalStatus == 'APPROVED'}">
                                                        <span class="event-title-text event-title-approved">
                                                            ${req.title}
                                                        </span>
                                                    </c:when>

                                                    <c:when test="${req.approvalStatus == 'REJECTED'}">
                                                        <span class="event-title-text event-title-rejected">
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


                                    <!-- 승인상태(대기/승인/반려) : 모바일 숨김 -->
                                    <td class="col-mobile-hide">

                                        <c:choose>

                                            <c:when test="${req.approvalStatus == 'APPROVED'}">
                                                <span class="status-ok">
                                                    승인
                                                </span>
                                            </c:when>

                                            <c:when test="${req.approvalStatus == 'REJECTED'}">
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

                                    <%--
                                        현재상태(진행중/예정/종료) : 모바일 숨김.
                                        대기·반려 이벤트는 아직 노출되는 이벤트가 아니므로 진행 상태 자체가
                                        없다는 의미로 "-"만 보여준다(progressStatus가 null인 경우).
                                    --%>
                                    <td class="col-mobile-hide">

                                        <c:choose>

                                            <c:when test="${req.progressStatus == 'ONGOING'}">
                                                <span class="status-progress-ongoing">
                                                    진행중
                                                </span>
                                            </c:when>

                                            <c:when test="${req.progressStatus == 'UPCOMING'}">
                                                <span class="status-progress-upcoming">
                                                    예정
                                                </span>
                                            </c:when>

                                            <c:when test="${req.progressStatus == 'ENDED'}">
                                                <span class="status-progress-ended">
                                                    종료
                                                </span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="progress-status-none">-</span>
                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td>

                                        <%--
                                            [버그 수정] businessName/title/productDetail 등에 따옴표나
                                            줄바꿈이 섞여도 onclick 인라인 문자열이 깨지지 않도록 data-* 속성으로
                                            값을 전달할 의도였으나, 실제로는 openEventDetailModal(button)이
                                            button.dataset.eventNo 처럼 버튼 요소의 data-*를 읽도록 되어있는데
                                            여기서는 문자열 9개를 그냥 함수 인자로 넘기고 있었다. 그 결과 button
                                            자리에 eventNo 문자열이 들어가 button.dataset이 undefined가 되어
                                            모달이 열리지 않았다(데스크톱/모바일 공용 버튼이라 양쪽 다 발생).
                                            data-* 속성 + openEventDetailModal(this)로 수정한다.
                                        --%>
                                        <button type="button"
                                                class="btn btn-dark"
                                                data-event-no="${req.eventNo}"
                                                data-business-name="${fn:escapeXml(req.businessName)}"
                                                data-title="${fn:escapeXml(req.title)}"
                                                data-period="${reqStartDateStr} ~ ${reqEndDateStr}"
                                                data-created-at="${reqCreatedAtStr}"
                                                data-status="${req.status}"
                                                data-approval-label="${req.approvalStatusLabel}"
                                                data-progress-label="${req.progressStatusLabel}"
                                                data-product-detail="${fn:escapeXml(req.productDetail)}"
                                                data-banner-image="${fn:escapeXml(reqBannerImageUrl)}"
                                                onclick="openEventDetailModal(this)">

                                            상세보기

                                        </button>

                                    </td>


                                </tr>

                            </c:forEach>

                        </c:when>


                        <c:otherwise>

                            <tr>

                                <td colspan="9">

                                    <c:choose>

                                        <c:when test="${currentTab == 'approved'}">
                                            승인 완료된 이벤트가 없습니다.
                                        </c:when>

                                        <c:when test="${currentTab == 'rejected'}">
                                            반려된 이벤트가 없습니다.
                                        </c:when>

                                        <c:when test="${currentTab == 'ongoing'}">
                                            진행중인 이벤트가 없습니다.
                                        </c:when>

                                        <c:when test="${currentTab == 'upcoming'}">
                                            예정된 이벤트가 없습니다.
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
<dialog class="modal-overlay" id="eventRequestModal" open aria-modal="true"
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
                <span>승인상태</span>
                <strong id="reqApprovalStatus"></strong>
            </p>

            <p>
                <span>현재상태</span>
                <strong id="reqProgressStatus"></strong>
            </p>

        </div>

        <div class="detail-section-title">
            적용 상품
        </div>

        <%--
            [수정] 이 표는 항상 좁은 모달(.modal-box-lg) 안에서만 쓰이는데, 다른 관리자
            화면의 .data-table 모바일 규칙(컬럼 숨김 + 상세보기 모달)은 이 표에는
            적용되지 않아 768px 이하에서 5개 컬럼이 그대로 좁아진 폭에 눌려 글자가
            제멋대로 줄바꿈됐다. product-detail-table 클래스로 이 표만 따로 지정해
            모바일에서 행 단위 세로 카드로 바뀌도록 한다(admin.css 참고).
        --%>
        <table class="data-table product-detail-table">

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
</dialog>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
