<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="monitoring"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 모니터링</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.5.1/dist/chart.umd.min.js"
        integrity="sha384-jb8JQMbMoBUzgWatfe6COACi2ljcDdZQ2OxczGA3bGNeWe+6DChMTBJemed7ZnvJ"
        crossorigin="anonymous"></script>
<script src="${pageContext.request.contextPath}/js/admin.js"></script>
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

            <h1 class="admin-page-title">모니터링</h1>

            <p class="admin-page-desc">
                회원별 접속 현황과 상품 클릭 통계를 차트와 표로 확인할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="chart-row">

                <div class="chart-box <c:if test='${empty visitorTrend}'>empty</c:if>">
                    <c:choose>
                        <c:when test="${not empty visitorTrend}">
                            <h3 class="chart-box-title">최근 7일 방문자 추이</h3>
                            <canvas id="visitorTrendChart" data-chart='<c:out value="${visitorTrendJson}"/>'></canvas>
                        </c:when>
                        <c:otherwise>
                            최근 7일간 방문자 데이터가 없습니다.
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="chart-box <c:if test='${empty popularClicks}'>empty</c:if>">
                    <c:choose>
                        <c:when test="${not empty popularClicks}">
                            <h3 class="chart-box-title">상품 클릭 TOP 5</h3>
                            <canvas id="popularClickChart" data-chart='<c:out value="${popularClicksJson}"/>'></canvas>
                        </c:when>
                        <c:otherwise>
                            상품 클릭 데이터가 없습니다.
                        </c:otherwise>
                    </c:choose>
                </div>

            </div>

            <table class="data-table">

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
                                        <span class="member-id-text"
                                            title="${row.memberId}"
                                            tabindex="0"
                                            role="button"
                                            aria-haspopup="dialog">
                                            ${row.memberId}
                                        </span>
                                        <%-- 모바일에서만 아이디 아래 보조 텍스트로 노출(admin.css 참고).
                                             데스크톱에서는 바로 옆 닉네임 컬럼으로 대체된다. --%>
                                        <div class="monitoring-nickname">${row.nickname}</div>
                                    </td>
                                    <td class="col-mobile-hide">${row.nickname}</td>
                                    <%-- [모바일 리팩토링] 데스크톱은 기존 yyyy-MM-dd 그대로, 768px 이하에서는
                                         yyMMdd(예: 260730)로 짧게 표시한다. admin.css의 .pc-access-date/
                                         .mobile-access-date가 화면 폭에 따라 둘 중 하나만 보여준다. --%>
                                    <td>
                                        <span class="pc-access-date"><fmt:formatDate value="${row.lastAccessAt}" pattern="yyyy-MM-dd"/></span>
                                        <span class="mobile-access-date"><fmt:formatDate value="${row.lastAccessAt}" pattern="yyMMdd"/></span>
                                    </td>
                                    <td>${row.productClickCount}</td>
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

        </section>

    </main>

</div>

<%--
    [신규] 아이디 전체보기 모달.
    768px 이하에서 말줄임(...)된 아이디(.member-id-text)를 클릭하면 열리며,
    admin.js의 openMemberIdModal()이 값을 채워 넣는다. memberManage.jsp에도
    동일한 id로 하나씩 둔다(페이지당 하나만 렌더링되므로 id 충돌 없음).
--%>
<div class="modal-overlay" id="memberIdModal" role="dialog" aria-modal="true"
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

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
