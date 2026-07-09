<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 하위 메뉴 활성화 표시용 (각 페이지의 currentTab 유무와 무관하게 param.tab을 그대로 사용) --%>
<c:set var="curTab" value="${empty param.tab ? '' : param.tab}"/>

<aside class="admin-sidebar">

    <div class="sidebar-logo">
        ODITJI ADMIN
    </div>


    <nav class="sidebar-menu">


        <!-- 관리자 홈 -->
        <div class="menu-group">

            <h3>관리자 홈</h3>

            <a href="${pageContext.request.contextPath}/admin/main"
               class="${activeMenu == 'main' ? 'active' : ''}">
                대시보드
            </a>

        </div>

        <!-- 회원 관리 (기존 방식 유지) -->
        <div class="menu-group">

            <h3>회원 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/member/list"
               class="${activeMenu == 'member' ? 'active' : ''}">
                회원 관리
            </a>

        </div>

        <!-- 리뷰 관리 -->
        <div class="menu-group">

            <h3>리뷰 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/review/list"
               class="${activeMenu == 'review' ? 'active' : ''}">
                콘텐츠 리뷰 관리
            </a>

            <a href="${pageContext.request.contextPath}/admin/productReview/list"
               class="${activeMenu == 'productReview' ? 'active' : ''}">
                상품 리뷰 관리
            </a>

        </div>

        <!-- 이벤트 관리 (사업자 등록/수정/연장 요청 처리) -->
        <div class="menu-group">

            <h3>이벤트 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/event/list?tab=register"
               class="${activeMenu == 'event' && (curTab == 'register' || empty curTab) ? 'active' : ''}">
                이벤트 등록
            </a>

            <a href="${pageContext.request.contextPath}/admin/event/list?tab=update"
               class="${activeMenu == 'event' && curTab == 'update' ? 'active' : ''}">
                이벤트 수정
            </a>

            <a href="${pageContext.request.contextPath}/admin/event/list?tab=extend"
               class="${activeMenu == 'event' && curTab == 'extend' ? 'active' : ''}">
                이벤트 연장
            </a>

        </div>

        <!-- 상품 관리 (사업자 등록/수정/삭제 요청 처리) -->
        <div class="menu-group">

            <h3>상품 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/product/list?tab=register"
               class="${activeMenu == 'product' && (curTab == 'register' || empty curTab) ? 'active' : ''}">
                상품 등록
            </a>

            <a href="${pageContext.request.contextPath}/admin/product/list?tab=update"
               class="${activeMenu == 'product' && curTab == 'update' ? 'active' : ''}">
                상품 수정
            </a>

            <a href="${pageContext.request.contextPath}/admin/product/list?tab=delete"
               class="${activeMenu == 'product' && curTab == 'delete' ? 'active' : ''}">
                상품 삭제
            </a>

        </div>

        <!-- 주문 관리 (기존 방식 유지) -->
        <div class="menu-group">

            <h3>주문 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/order/list"
               class="${activeMenu == 'order' ? 'active' : ''}">
                주문 관리
            </a>

        </div>

        <!-- 사업자 관리 -->
        <div class="menu-group">

            <h3>사업자 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/business/list?tab=info"
               class="${activeMenu == 'business' && (curTab == 'info' || empty curTab) ? 'active' : ''}">
                사업자 목록
            </a>

            <a href="${pageContext.request.contextPath}/admin/business/list?tab=approval"
               class="${activeMenu == 'business' && curTab == 'approval' ? 'active' : ''}">
                사업자 승인 관리
            </a>

        </div>

        <!-- 정산 관리 (사업자 입금 확인 요청 처리) -->
        <div class="menu-group">

            <h3>정산 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/settlement/main"
               class="${activeMenu == 'settlement' ? 'active' : ''}">
                사업자 입금 확인
            </a>

        </div>

        <!-- 시스템 관리 -->
        <div class="menu-group">

            <h3>시스템 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/monitoring"
               class="${activeMenu == 'monitoring' ? 'active' : ''}">
                모니터링
            </a>

        </div>


    </nav>


</aside>
