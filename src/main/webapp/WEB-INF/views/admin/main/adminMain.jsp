<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 관리자</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main id="mainContent" class="main-content">

        <section class="admin-hero">

            <h1>관리자 대시보드</h1>

            <p>ODITJI 서비스 전반의 회원, 콘텐츠, 사업자 현황을 한눈에 확인하세요.</p>

            <div class="stat-grid">

                <a href="${pageContext.request.contextPath}/admin/member/list"
                   class="stat-card">
                    <span>전체 회원 수</span>
                    <strong>${adminMain.memberCount-1}명</strong>
                </a>

                <a href="${pageContext.request.contextPath}/admin/review/list"
                   class="stat-card">
                    <span>콘텐츠리뷰 수</span>
                    <strong>${adminMain.contentReviewCount}개</strong>
                </a>

                <a href="${pageContext.request.contextPath}/admin/productReview/list"
                   class="stat-card">
                    <span>상품리뷰 수</span>
                    <strong>${adminMain.productReviewCount}개</strong>
                </a>

                <a href="${pageContext.request.contextPath}/admin/review/list?tab=report"
                   class="stat-card">
                    <span>신고 수</span>
                    <strong>${adminMain.reportCount}건</strong>
                </a>

                <a href="${pageContext.request.contextPath}/admin/business/list?tab=approval"
                   class="stat-card">
                    <span>사업자 승인 대기</span>
                    <strong>${adminMain.businessRequestCount}건</strong>
                </a>

                <a href="${pageContext.request.contextPath}/admin/product/list?tab=waiting"
                   class="stat-card">
                    <span>상품 등록 요청</span>
                    <strong>${adminMain.productRequestCount}건</strong>
                </a>

                <a href="${pageContext.request.contextPath}/admin/event/list?tab=waiting"
                   class="stat-card">
                    <span>이벤트 요청</span>
                    <strong>${adminMain.eventRequestCount}건</strong>
                </a>

                <%--
                    [수정] 관리자 대시보드의 정산 요청 대기 카드를 누르면
                    정산 관리 화면의 REQUESTED, 즉 지급 대기 상태만 조회한다.
                --%>
                <a href="${pageContext.request.contextPath}/admin/settlement/main?status=REQUESTED"
                class="stat-card">
                    <span>정산 요청 대기</span>
                    <strong>${adminMain.settlementWaitingCount}건</strong>
                </a>

                <a href="${pageContext.request.contextPath}/admin/monitoring"
                   class="stat-card">
                    <span>방문자 수</span>
                    <strong>${adminMain.visitorCount}명</strong>
                </a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
