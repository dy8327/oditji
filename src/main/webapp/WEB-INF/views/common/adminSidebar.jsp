<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>


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

        <!-- 회원 관리 -->
        <div class="menu-group">

            <h3>회원 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/member/list"
               class="${activeMenu == 'member' ? 'active' : ''}">
                회원 관리
            </a>

        </div>

        <!-- 콘텐츠 관리 -->
        <div class="menu-group">

            <h3>콘텐츠 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/content/list"
               class="${activeMenu == 'content' ? 'active' : ''}">
                콘텐츠 관리
            </a>


            <a href="${pageContext.request.contextPath}/admin/review/list"
               class="${activeMenu == 'review' ? 'active' : ''}">
                리뷰 관리
            </a>


            <a href="${pageContext.request.contextPath}/admin/event/list"
               class="${activeMenu == 'event' ? 'active' : ''}">
                이벤트 관리
            </a>

        </div>

        <!-- 주문 관리 -->
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

            <a href="${pageContext.request.contextPath}/admin/business/list"
               class="${activeMenu == 'business' ? 'active' : ''}">
                사업자 관리
            </a>

        </div>

        <!-- 정산 관리 -->
        <div class="menu-group">

            <h3>정산 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/settlement/main"
               class="${activeMenu == 'settlement' ? 'active' : ''}">
                정산 관리
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