<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<c:set var="activeMenu" value="monitoring"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 모니터링</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.1/dist/chart.umd.min.js"
        integrity="sha384-jb8JQMbMoBUzgWatfe6COACi2ljcDdZQ2OxczGA3bGNeWe+6DChMTBJemed7ZnvJ"
        crossorigin="anonymous"></script>
<script src="${pageContext.request.contextPath}/js/admin.js"></script>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main id="mainContent" class="main-content monitoring-main">

        <div class="admin-page-header monitoring-page-header">

    <a href="${pageContext.request.contextPath}/admin/main" class="back-link">
        ← 뒤로가기
    </a>

    <%--
        =========================================================
        [모니터링 공통 조회 기간]
        방문자 추이 내부에 있던 기간 선택을 페이지 제목 영역으로 이동한다.
        선택 기간은 방문 수 / 방문자 추이 / 주문 수 / 매출 /
        상품 클릭 TOP 5에 공통 적용한다.
        =========================================================
    --%>
    <div class="monitoring-header-row">

        <div class="monitoring-header-text">
            <h1 class="admin-page-title">모니터링</h1>

            <p class="admin-page-desc">
                회원과 방문, 주문, 매출 현황을 한눈에 확인하고 분석할 수 있습니다.
            </p>
        </div>

        <div class="monitoring-common-period">

            <span class="monitoring-common-period-label">조회 기간</span>

            <nav class="monitoring-period-tabs" aria-label="모니터링 공통 조회 기간">

                <a href="${pageContext.request.contextPath}/admin/monitoring?period=7d"
                   class="monitoring-period-tab ${period == '7d' ? 'active' : ''}">
                    최근 7일
                </a>

                <a href="${pageContext.request.contextPath}/admin/monitoring?period=3m"
                   class="monitoring-period-tab ${period == '3m' ? 'active' : ''}">
                    3개월
                </a>

                <a href="${pageContext.request.contextPath}/admin/monitoring?period=6m"
                   class="monitoring-period-tab ${period == '6m' ? 'active' : ''}">
                    6개월
                </a>

                <a href="${pageContext.request.contextPath}/admin/monitoring?period=1y"
                   class="monitoring-period-tab ${period == '1y' ? 'active' : ''}">
                    1년
                </a>

            </nav>

        </div>

    </div>

</div>

        <%--
            =========================================================
            [모니터링 화면 개편] 상단 요약 통계 카드
            기존 헤더/사이드바/푸터는 수정하지 않고 모니터링 본문에만 추가한다.
            아이콘은 별도 이미지 파일을 만들지 않고 인라인 SVG로 구성한다.
            =========================================================
        --%>
        <section class="monitoring-summary-grid" aria-label="모니터링 요약 통계">

            <article class="monitoring-summary-card monitoring-summary-member">
                <div class="monitoring-summary-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false">
                        <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z"/>
                        <path d="M4.5 21a7.5 7.5 0 0 1 15 0"/>
                    </svg>
                </div>
                <div class="monitoring-summary-body">
                    <span class="monitoring-summary-label">회원 수</span>
                    <strong class="monitoring-summary-value">
                        <fmt:formatNumber value="${monitoringSummary.memberCount}" pattern="#,#0"/>
                    </strong>
                    <span class="monitoring-summary-caption monitoring-summary-caption-positive">전체 회원 현황</span>
                </div>
            </article>

            <article class="monitoring-summary-card monitoring-summary-visitor">
                <div class="monitoring-summary-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false">
                        <path d="M4 19V5"/>
                        <path d="M4 19h16"/>
                        <path d="m7 15 4-4 3 2 4-5"/>
                        <path d="M16 8h2v2"/>
                    </svg>
                </div>

                <div class="monitoring-summary-body">
                    <span class="monitoring-summary-label">
                        방문 수

                        <%--
                            [모니터링 방문 수 기간 연동]
                            방문자 추이에서 선택한 기간과 동일한 기준으로
                            순 방문자 수를 집계한다.
                        --%>
                        <span class="monitoring-info-mark"
                            title="선택한 기간 동안 접속한 일반 회원의 중복을 제외한 순 방문자 수">i</span>
                    </span>

                    <strong class="monitoring-summary-value">
                        <fmt:formatNumber
                            value="${monitoringSummary.recentVisitorCount}"
                            pattern="#,#0"/>
                    </strong>

                    <%--
                        [모니터링 방문 수 기간 연동]
                        현재 선택된 방문자 추이 기간에 따라
                        방문 수 카드의 설명 문구도 함께 변경한다.
                    --%>
                    <span class="monitoring-summary-caption monitoring-summary-caption-positive">
                        <c:choose>
                            <c:when test="${period == '3m'}">
                                최근 3개월 순 방문자
                            </c:when>

                            <c:when test="${period == '6m'}">
                                최근 6개월 순 방문자
                            </c:when>

                            <c:when test="${period == '1y'}">
                                최근 1년 순 방문자
                            </c:when>

                            <c:otherwise>
                                최근 7일 순 방문자
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </article>

            <article class="monitoring-summary-card monitoring-summary-order">
                <div class="monitoring-summary-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false">
                        <path d="M6 8h12l-1 12H7L6 8Z"/>
                        <path d="M9 8V6a3 3 0 0 1 6 0v2"/>
                    </svg>
                </div>
                <div class="monitoring-summary-body">
                    <span class="monitoring-summary-label">주문 수</span>
                    <strong class="monitoring-summary-value">
                        <fmt:formatNumber value="${monitoringSummary.deliveredOrderCount}" pattern="#,#0"/>
                    </strong>
                    <span class="monitoring-summary-caption monitoring-summary-caption-positive">
                        <c:choose>
                            <c:when test="${period == '3m'}">최근 3개월 배송완료 주문</c:when>
                            <c:when test="${period == '6m'}">최근 6개월 배송완료 주문</c:when>
                            <c:when test="${period == '1y'}">최근 1년 배송완료 주문</c:when>
                            <c:otherwise>최근 7일 배송완료 주문</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </article>

            <article class="monitoring-summary-card monitoring-summary-sales">
                <div class="monitoring-summary-icon" aria-hidden="true">
                    <svg viewBox="0 0 24 24" focusable="false">
                        <ellipse cx="12" cy="6" rx="6" ry="3"/>
                        <path d="M6 6v6c0 1.7 2.7 3 6 3s6-1.3 6-3V6"/>
                        <path d="M6 12v6c0 1.7 2.7 3 6 3s6-1.3 6-3v-6"/>
                    </svg>
                </div>
                <div class="monitoring-summary-body">
                    <span class="monitoring-summary-label">매출</span>
                    <strong class="monitoring-summary-value monitoring-summary-value-money">
                        ₩<fmt:formatNumber value="${monitoringSummary.deliveredSalesAmount}" pattern="#,##0"/>
                    </strong>
                    <span class="monitoring-summary-caption monitoring-summary-caption-positive">
                        <c:choose>
                            <c:when test="${period == '3m'}">최근 3개월 배송완료 실매출</c:when>
                            <c:when test="${period == '6m'}">최근 6개월 배송완료 실매출</c:when>
                            <c:when test="${period == '1y'}">최근 1년 배송완료 실매출</c:when>
                            <c:otherwise>최근 7일 배송완료 실매출</c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </article>

        </section>

        <%--
            =========================================================
            [모니터링 화면 개편] 방문자 추이 / 상품 클릭 TOP 5
            방문자 추이는 7일·3개월·6개월·1년 탭으로 조회 기간을 전환한다.
            =========================================================
        --%>
        <section class="monitoring-chart-grid">

            <div class="monitoring-panel monitoring-visitor-panel <c:if test='${empty visitorTrend}'>empty</c:if>">
                <div class="monitoring-panel-header">
                    <h2 class="monitoring-panel-title">방문자 추이</h2>
                    <span class="monitoring-panel-unit">단위: 명</span>
                </div>

                <c:choose>
                    <c:when test="${not empty visitorTrend}">
                        <div class="monitoring-chart-canvas-wrap">
                            <canvas id="visitorTrendChart" data-chart='<c:out value="${visitorTrendJson}"/>'></canvas>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="monitoring-chart-empty">방문자 데이터가 없습니다.</div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="monitoring-panel monitoring-click-panel <c:if test='${empty popularClicks}'>empty</c:if>">
                <div class="monitoring-panel-header">
                    <h2 class="monitoring-panel-title">상품 클릭 TOP 5</h2>
                    <span class="monitoring-panel-unit">단위: 회</span>
                </div>

                <c:choose>
                    <c:when test="${not empty popularClicks}">
                        <div class="monitoring-chart-canvas-wrap">
                            <canvas id="popularClickChart" data-chart='<c:out value="${popularClicksJson}"/>'></canvas>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="monitoring-chart-empty">상품 클릭 데이터가 없습니다.</div>
                    </c:otherwise>
                </c:choose>
            </div>

        </section>

        <%--
            =========================================================
            [모니터링 화면 개편] 최근 접속 회원
            기존 모니터링 목록 데이터와 모바일 아이디 전체보기 기능은 그대로 유지한다.
            =========================================================
        --%>
        <section class="monitoring-panel monitoring-member-panel">
            <div class="monitoring-panel-header monitoring-member-header">
                <h2 class="monitoring-panel-title">최근 접속 회원</h2>
            </div>

            <div class="monitoring-table-wrap">
                <table class="data-table monitoring-table">

                    <thead>
                        <tr>
                            <th>아이디</th>
                            <th class="col-mobile-hide">닉네임</th>
                            <th>최근 접속일</th>
                            <th>상품 클릭 수</th>
                            <th>접속 IP</th>
                        </tr>
                    </thead>

                    <tbody>

                        <c:choose>

                            <c:when test="${not empty monitoringList}">

                                <c:forEach var="row" items="${monitoringList}">
                                    <tr>
                                        <%--
                                            [수정] 기존에는 이 컬럼에 닉네임(row.nickname)만 보여주고 있었는데,
                                            SNS 로그인 회원은 provider가 발급한 원본 아이디(row.memberId)가 길어서
                                            회원관리(memberManage.jsp)에서 보던 것과 같은 값을 여기서는 확인할 수
                                            없었다. memberManage.jsp와 동일하게 실제 로그인 아이디를 주 텍스트로
                                            보여주고, 닉네임은 그 아래 보조 텍스트로 함께 보여준다.

                                            .member-id-cell/.member-id-text는 memberManage.jsp와 공유하는 컴포넌트로,
                                            768px 이하에서만 말줄임(...)이 걸리고 클릭(혹은 포커스 후 Enter/Space)하면
                                            #memberIdModal 모달로 전체 값을 보여준다(닫기 버튼으로 닫음). 그 이상
                                            너비(데스크톱)에서는 항상 전체 아이디가 잘리지 않고 그대로 보인다.
                                        --%>
                                        <td class="member-id-cell">
                                            <button type="button"
                                                    class="member-id-text"
                                                    title="${row.memberId}"
                                                    aria-haspopup="dialog">
                                                ${row.memberId}
                                            </button>
                                            <%-- 모바일에서만 아이디 아래 보조 텍스트로 노출(admin.css 참고).
                                                 데스크톱에서는 바로 옆 닉네임 컬럼으로 대체된다. --%>
                                            <div class="monitoring-nickname">${row.nickname}</div>
                                        </td>
                                        <td class="col-mobile-hide">${row.nickname}</td>
                                        <%-- [모바일 리팩토링] 데스크톱은 기존 yyyy-MM-dd 그대로, 768px 이하에서는
                                             yyMMdd(예: 260730)로 짧게 표시한다. admin.css의 .pc-access-date/
                                             .mobile-access-date가 화면 폭에 따라 둘 중 하나만 보여준다. --%>
                                        <td>
                                            <span class="pc-access-date">${dt:format(row.lastAccessAt, 'yyyy-MM-dd')}</span>
                                            <span class="mobile-access-date">${dt:format(row.lastAccessAt, 'yyMMdd')}</span>
                                        </td>
                                        <td><fmt:formatNumber value="${row.productClickCount}" pattern="#,#0"/></td>
                                        <td>${row.accessIp}</td>
                                    </tr>
                                </c:forEach>

                            </c:when>

                            <c:otherwise>
                                <tr><td colspan="5">모니터링 데이터가 없습니다.</td></tr>
                            </c:otherwise>

                        </c:choose>

                    </tbody>

                </table>
            </div>

        </section>

    </main>

</div>

<%--
    [신규] 아이디 전체보기 모달.
    768px 이하에서 말줄임(...)된 아이디(.member-id-text)를 클릭하면 열리며,
    admin.js의 openMemberIdModal()이 값을 채워 넣는다. memberManage.jsp에도
    동일한 id로 하나씩 둔다(페이지당 하나만 렌더링되므로 id 충돌 없음).
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

</body>
</html>