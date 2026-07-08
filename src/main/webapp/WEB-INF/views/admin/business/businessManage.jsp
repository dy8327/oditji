<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="business"/>
<c:set var="currentTab" value="${empty param.tab ? 'info' : param.tab}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
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
                입점 사업자 정보와 등급을 관리하고, 사업자가 요청한 승인 내역을 처리할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <nav class="tab-menu">
                <a href="?tab=info" class="${currentTab == 'info' ? 'active' : ''}">사업자 정보</a>
                <a href="?tab=approval" class="${currentTab == 'approval' ? 'active' : ''}">사업자 승인</a>
            </nav>

            <div class="toolbar">

                <form method="get" action="${pageContext.request.contextPath}/admin/business/list">
                    <input type="hidden" name="tab" value="${currentTab}">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="이름, 아이디, 이메일 검색">
                </form>

                <c:if test="${currentTab == 'approval'}">
                    <div class="filter-chip-row">
                        <a class="filter-chip ${empty param.reqType ? 'active' : ''}"
                           href="?tab=approval">전체</a>
                        <a class="filter-chip ${param.reqType == 'PRODUCT' ? 'active' : ''}"
                           href="?tab=approval&reqType=PRODUCT">상품</a>
                        <a class="filter-chip ${param.reqType == 'EVENT' ? 'active' : ''}"
                           href="?tab=approval&reqType=EVENT">이벤트</a>
                        <a class="filter-chip ${param.reqType == 'SETTLEMENT' ? 'active' : ''}"
                           href="?tab=approval&reqType=SETTLEMENT">정산</a>
                    </div>
                </c:if>

            </div>

            <c:choose>

                <%-- 사업자 정보 탭 --%>
                <c:when test="${currentTab == 'info'}">

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>이름</th>
                                <th>아이디</th>
                                <th>이메일</th>
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
                                            <td>${business.memberId}</td>
                                            <td>${business.email}</td>
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
                                            <td>
                                                <button type="button" class="btn btn-dark"
                                                        onclick="openGradeModal(
                                                            '${business.businessNo}',
                                                            '${business.businessName}',
                                                            '${business.memberId}',
                                                            '${business.email}',
                                                            '${business.gradeName}'
                                                        )">
                                                    등급 관리
                                                </button>
                                            </td>
                                        </tr>

                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr><td colspan="6">조회된 사업자가 없습니다.</td></tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:when>

                <%-- 사업자 승인 탭 --%>
                <c:otherwise>

                    <table class="data-table">

                        <thead>
                            <tr>
                                <th>이름</th>
                                <th>아이디</th>
                                <th>이메일</th>
                                <th>요청 유형</th>
                                <th>관리</th>
                            </tr>
                        </thead>

                        <tbody>

                            <c:choose>

                                <c:when test="${not empty approvalList}">

                                    <c:forEach var="req" items="${approvalList}">

                                        <tr>
                                            <td>${req.businessName}</td>
                                            <td>${req.memberId}</td>
                                            <td>${req.email}</td>
                                            <td>${req.requestType}</td>
                                            <td>
                                                <button type="button" class="btn btn-dark"
                                                        onclick="openApprovalModal(
                                                            '${req.requestNo}',
                                                            '${req.businessName}',
                                                            '${req.requestType}',
                                                            '${req.targetName}',
                                                            '${req.settlementAmount}',
                                                            '${req.content}'
                                                        )">
                                                    상세보기
                                                </button>
                                            </td>
                                        </tr>

                                    </c:forEach>

                                </c:when>

                                <c:otherwise>
                                    <tr><td colspan="5">승인 요청 내역이 없습니다.</td></tr>
                                </c:otherwise>

                            </c:choose>

                        </tbody>

                    </table>

                </c:otherwise>

            </c:choose>

            <div class="pagination">

                <a href="?tab=${currentTab}&page=${pagination.currentPage-1}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?tab=${currentTab}&page=${p}"
                       class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?tab=${currentTab}&page=${pagination.currentPage+1}">›</a>

            </div>

        </section>

    </main>

</div>

<%-- 사업자 등급 관리 팝업 --%>
<div class="modal-overlay" id="gradeModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>사업자 등급 관리</h3>
            <span class="modal-close" onclick="closeModal('gradeModal')">&times;</span>
        </div>

        <form action="${pageContext.request.contextPath}/admin/business/grade" method="post">

            <input type="hidden" name="businessNo" id="gradeBusinessNo">

            <div class="target-info-box">
                <p><span>이름</span><strong id="gradeBusinessName"></strong></p>
                <p><span>아이디</span><strong id="gradeBusinessId"></strong></p>
                <p><span>이메일</span><strong id="gradeBusinessEmail"></strong></p>
                <p><span>사업자 등급</span><strong id="gradeBusinessCurrent"></strong></p>
            </div>

            <div class="grade-select-box">

                <div class="grade-select-title">등급 변경하기</div>

                <div class="grade-option-list">
                    <label><input type="radio" name="gradeName" value="BRONZE">Bronze</label>
                    <label><input type="radio" name="gradeName" value="SILVER">Silver</label>
                    <label><input type="radio" name="gradeName" value="GOLD">Gold</label>
                    <label><input type="radio" name="gradeName" value="PLATINUM">Platinum</label>
                    <label><input type="radio" name="gradeName" value="VIP">VIP</label>
                </div>

            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">저장하기</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('gradeModal')">닫기</button>
            </div>

        </form>

    </div>

</div>

<%-- 사업자 승인 요청 팝업 --%>
<div class="modal-overlay" id="approvalModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>사업자 승인 요청</h3>
            <span class="modal-close" onclick="closeModal('approvalModal')">&times;</span>
        </div>

        <div class="target-info-box">
            <p><span>사업자명</span><strong id="approvalBusinessName"></strong></p>
            <p id="approvalTargetRow"><span id="approvalTargetLabel">상품(이벤트) 명</span><strong id="approvalTargetName"></strong></p>
            <p id="approvalAmountRow" style="display:none;"><span>정산 금액</span><strong id="approvalAmount"></strong></p>
        </div>

        <div class="form-group">
            <label class="form-label">내용</label>
            <textarea class="form-textarea" id="approvalContent" readonly></textarea>
        </div>

        <form id="approvalForm" action="${pageContext.request.contextPath}/admin/business/approve" method="post">

            <input type="hidden" name="requestNo" id="approvalRequestNo">

            <div class="modal-footer">
                <button type="submit" class="btn btn-success">승인</button>
                <button type="submit" formaction="${pageContext.request.contextPath}/admin/business/reject"
                        class="btn btn-danger">반려</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

function openGradeModal(businessNo, name, memberId, email, currentGrade) {
    document.getElementById('gradeBusinessNo').value = businessNo;
    document.getElementById('gradeBusinessName').textContent = name;
    document.getElementById('gradeBusinessId').textContent = memberId;
    document.getElementById('gradeBusinessEmail').textContent = email;
    document.getElementById('gradeBusinessCurrent').textContent = currentGrade;

    var radios = document.getElementsByName('gradeName');
    for (var i = 0; i < radios.length; i++) {
        radios[i].checked = (radios[i].value === currentGrade);
    }

    document.getElementById('gradeModal').classList.add('open');
}

function openApprovalModal(requestNo, businessName, requestType, targetName, settlementAmount, content) {
    document.getElementById('approvalRequestNo').value = requestNo;
    document.getElementById('approvalBusinessName').textContent = businessName;
    document.getElementById('approvalContent').value = content;

    var amountRow = document.getElementById('approvalAmountRow');
    var targetRow = document.getElementById('approvalTargetRow');

    if (requestType === 'SETTLEMENT') {
        targetRow.style.display = 'none';
        amountRow.style.display = 'flex';
        document.getElementById('approvalAmount').textContent = settlementAmount + '원';
    } else {
        targetRow.style.display = 'flex';
        amountRow.style.display = 'none';
        document.getElementById('approvalTargetName').textContent = targetName;
    }

    document.getElementById('approvalModal').classList.add('open');
}
</script>

</body>
</html>
