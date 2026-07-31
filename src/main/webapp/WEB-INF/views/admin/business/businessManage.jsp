<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="business"/>
<c:set var="currentTab" value="${empty param.tab ? 'info' : param.tab}"/>

<%-- 검색 기준(searchType)의 현재 선택값. memberManage.jsp의 searchType 필터와 동일한 방식. --%>
<c:set var="currentSearchType" value="${empty param.searchType ? '' : param.searchType}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 사업자 관리</title>
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

            <h1 class="admin-page-title">사업자 관리</h1>

            <p class="admin-page-desc">
                사업자 정보와 등급을 관리하고, 사업자의 승인 요청을 처리할 수 있습니다.
            </p>

        </div>

        <%--
            상단 통계 카드. 각 카드는 해당 탭으로 바로 이동하는 링크이며,
            현재 선택된 탭과 일치하는 카드에는 active 클래스를 준다.
            memberManage.jsp / reviewManage.jsp와 동일한 .member-stat-grid 컴포넌트를 재사용한다.
        --%>
        <div class="member-stat-grid">

            <a class="stat-card ${currentTab == 'info' ? 'active' : ''}"
               href="?tab=info">
                <span>입점 완료 사업자</span>
                <strong>${businessStats.approvedCount}명</strong>
            </a>

            <a class="stat-card ${currentTab == 'approval' ? 'active' : ''}"
               href="?tab=approval">
                <span>승인 대기</span>
                <strong>${businessStats.waitingCount}명</strong>
            </a>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">
                <a href="?tab=info&searchType=${currentSearchType}&keyword=${param.keyword}" class="${currentTab == 'info' ? 'active' : ''}">사업자 목록</a>
                <a href="?tab=approval&searchType=${currentSearchType}&keyword=${param.keyword}" class="${currentTab == 'approval' ? 'active' : ''}">사업자 승인 관리</a>
            </nav>

            <div class="toolbar">

                <form method="get" action="${pageContext.request.contextPath}/admin/business/list">

                    <%-- 상태 필터. 위쪽 탭 메뉴와 같은 값(tab)을 다루지만, 검색창 옆에서도
                         memberManage.jsp / eventManage.jsp와 동일하게 select로 전환할 수 있도록 제공한다. --%>
                    <label for="businessStatusFilter"
                        style="position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0, 0, 0, 0);white-space:nowrap;border:0;">
                        상태 필터
                    </label>

                    <select id="businessStatusFilter" name="tab" class="filter-select">
                        <option value="info"     ${currentTab == 'info' ? 'selected' : ''}>사업자 목록</option>
                        <option value="approval" ${currentTab == 'approval' ? 'selected' : ''}>사업자 승인 관리</option>
                    </select>

                    <%-- 검색 기준 필터. memberManage.jsp의 searchType 필터와 동일한 구성(이름/아이디/이메일). --%>
                    <label for="businessSearchTypeFilter"
                        style="position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0, 0, 0, 0);white-space:nowrap;border:0;">
                        검색 기준
                    </label>

                    <select id="businessSearchTypeFilter" name="searchType" class="filter-select">
                        <option value=""         ${empty currentSearchType ? 'selected' : ''}>전체</option>
                        <option value="name"     ${currentSearchType == 'name' ? 'selected' : ''}>이름</option>
                        <option value="id"       ${currentSearchType == 'id' ? 'selected' : ''}>아이디</option>
                        <option value="email"    ${currentSearchType == 'email' ? 'selected' : ''}>이메일</option>
                    </select>

                    <label for="businessKeyword"
                        style="position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0, 0, 0, 0);white-space:nowrap;border:0;">
                        사업자 검색어
                    </label>

                    <input type="text"
                        id="businessKeyword"
                        class="page-search"
                        name="keyword"
                        value="${param.keyword}"
                        placeholder="이름, 아이디, 이메일 검색">

                    <button type="submit" class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

            </div>

            <%-- 상품/이벤트/정산 요청 승인은 각각 상품 관리, 이벤트 관리, 정산 관리 메뉴에서 처리합니다. --%>

            <c:choose>

                <c:when test="${currentTab == 'info'}">

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>이름</th>
                                <th class="col-mobile-hide">아이디</th>
                                <th class="col-mobile-hide">이메일</th>
                                <%-- [사업자 자동 등급 관리 추가] 누적 실매출 표시 --%>
                                <th class="col-mobile-hide">누적 실매출</th>
                                <th>등급</th>
                                <th>상태</th>
                                <th>관리</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty businessList}">

                                    <c:forEach var="business" items="${businessList}">

                                        <tr>
                                            <td>${business.businessName}</td>
                                            <td class="col-mobile-hide">${business.memberId}</td>
                                            <td class="col-mobile-hide">${business.email}</td>
                                            <%-- [사업자 자동 등급 관리 추가] 환불 승인 금액을 제외한 누적 실매출 --%>
                                            <td class="col-mobile-hide"><fmt:formatNumber value="${business.totalSales}" pattern="#,##0" />원</td>
                                            <td>${business.gradeName}</td>
                                            <td>

                                                <c:choose>
                                                    <c:when test="${business.status == 'APPROVED'}">
                                                        <span class="status-ok">승인</span>
                                                    </c:when>
                                                    <c:when test="${business.status == 'WAITING'}">
                                                        <span class="status-waiting">대기</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-reject">반려</span>
                                                    </c:otherwise>
                                                </c:choose>

                                            </td>
                                            <%--
                                                이 버튼은 모바일 전용 트리거가 아니라 원래부터 화면의 유일한
                                                관리 액션이라, 숨겨진 컬럼(아이디/이메일/누적 실매출)까지 포함한
                                                전체 정보와 등급 변경 폼을 gradeModal이 그대로 보여준다.
                                                별도 모바일 상세보기 모달을 새로 만들 필요가 없다.
                                            --%>
                                            <td>
                                                <button type="button" class="btn btn-dark"
                                                        onclick="openGradeModal(
                                                            '${business.businessNo}',
                                                            '${business.businessName}',
                                                            '${business.memberId}',
                                                            '${business.email}',
                                                            '${business.gradeName}',
                                                            '${business.totalSales}'
                                                        )">
                                                    등급 관리
                                                </button>
                                            </td>
                                        </tr>

                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr><td colspan="7">조회된 사업자가 없습니다.</td></tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:when>

                <c:otherwise>

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>사업자명</th>
                                <th class="col-mobile-hide">아이디</th>
                                <th class="col-mobile-hide">이메일</th>
                                <th>사업자등록번호</th>
                                <th class="col-mobile-hide">정산 계좌</th>
                                <th>관리</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty approvalList}">

                                    <c:forEach var="req" items="${approvalList}">

                                        <tr>
                                            <td>${req.businessName}</td>
                                            <td class="col-mobile-hide">${req.memberId}</td>
                                            <td class="col-mobile-hide">${req.email}</td>
                                            <td>${req.businessNumber}</td>
                                            <td class="col-mobile-hide">${req.bankName} ${req.accountNumber} (${req.accountHolder})</td>
                                            <td>
                                                <button type="button" class="btn btn-dark"
                                                        onclick="openApprovalModal(
                                                            '${req.businessNo}',
                                                            '${req.businessName}',
                                                            '${req.memberId}',
                                                            '${req.email}',
                                                            '${req.businessNumber}',
                                                            '${req.bankName} ${req.accountNumber} (${req.accountHolder})'
                                                        )">
                                                    상세보기
                                                </button>
                                            </td>
                                        </tr>

                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr><td colspan="6">승인 요청 내역이 없습니다.</td></tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:otherwise>

            </c:choose>

            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>


                <!-- 이전 페이지 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>


                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <!-- 다음 페이지 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>


                <!-- 다음 블록 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>


        </section>

    </main>

</div>

<%-- 사업자 등급 관리 팝업 --%>
<div class="modal-overlay" id="gradeModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>사업자 등급 관리</h3>
            <%--
                클릭 가능한 span 대신 기본 키보드 동작을 제공하는 button을 사용한다.
                기존 .modal-close 디자인을 유지하도록 버튼 기본 스타일을 제거한다.
            --%>
            <button type="button"
                    class="modal-close"
                    aria-label="사업자 등급 관리 팝업 닫기"
                    style="padding:0;border:0;background:transparent;font-family:inherit;"
                    onclick="closeModal('gradeModal')">
                &times;
            </button>
        </div>

        <form action="${pageContext.request.contextPath}/admin/business/grade" method="post">

            <input type="hidden" name="businessNo" id="gradeBusinessNo">
            <input type="hidden" name="tab" value="${currentTab}">
            <input type="hidden" name="searchType" value="${currentSearchType}">
            <input type="hidden" name="keyword" value="${param.keyword}">
            <input type="hidden" name="page" value="${pagination.currentPage}">

            <div class="target-info-box">
                <p><span>이름</span><strong id="gradeBusinessName"></strong></p>
                <p><span>아이디</span><strong id="gradeBusinessId"></strong></p>
                <p><span>이메일</span><strong id="gradeBusinessEmail"></strong></p>
                <p><span>누적 실매출</span><strong id="gradeBusinessSales"></strong></p>
                <p><span>사업자 등급</span><strong id="gradeBusinessCurrent"></strong></p>
            </div>

            <div class="grade-select-box">

                <div class="grade-select-title">등급 변경하기</div>

                <div class="grade-option-list">
                    <label><input type="radio" name="gradeName" value="BRONZE"><span class="grade-dot grade-dot-bronze"></span>Bronze</label>
                    <label><input type="radio" name="gradeName" value="SILVER"><span class="grade-dot grade-dot-silver"></span>Silver</label>
                    <label><input type="radio" name="gradeName" value="GOLD"><span class="grade-dot grade-dot-gold"></span>Gold</label>
                    <label><input type="radio" name="gradeName" value="PLATINUM"><span class="grade-dot grade-dot-platinum"></span>Platinum</label>
                    <label><input type="radio" name="gradeName" value="VIP"><span class="grade-dot grade-dot-vip"></span>VIP</label>
                </div>

            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">저장하기</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('gradeModal')">닫기</button>
            </div>

        </form>

    </div>

</div>

<div class="modal-overlay" id="approvalModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>사업자 승인</h3>
            <%--
                마우스 클릭뿐 아니라 키보드 Enter/Space 입력도 기본 지원하도록
                닫기 요소를 button으로 변경한다.
            --%>
            <button type="button"
                    class="modal-close"
                    aria-label="사업자 승인 팝업 닫기"
                    style="padding:0;border:0;background:transparent;font-family:inherit;"
                    onclick="closeModal('approvalModal')">
                &times;
            </button>
        </div>

        <div class="target-info-box">
            <p><span>사업자명</span><strong id="approvalBusinessName"></strong></p>
            <p><span>아이디</span><strong id="approvalMemberId"></strong></p>
            <p><span>이메일</span><strong id="approvalEmail"></strong></p>
            <p><span>사업자등록번호</span><strong id="approvalBusinessNumber"></strong></p>
            <p><span>정산 계좌</span><strong id="approvalAccount"></strong></p>
        </div>

        <form id="approvalForm" action="${pageContext.request.contextPath}/admin/business/approve" method="post">

            <input type="hidden" name="businessNo" id="approvalBusinessNo">
            <input type="hidden" name="tab" value="${currentTab}">
            <input type="hidden" name="searchType" value="${currentSearchType}">
            <input type="hidden" name="keyword" value="${param.keyword}">
            <input type="hidden" name="page" value="${pagination.currentPage}">

            <div class="modal-footer">
                <button type="submit" class="btn btn-success">승인</button>
                <button type="submit" formaction="${pageContext.request.contextPath}/admin/business/reject"
                        class="btn btn-danger">반려</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('approvalModal')">닫기</button>
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