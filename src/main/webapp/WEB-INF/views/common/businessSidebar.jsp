<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>


<aside class="business-sidebar">

    <div class="sidebar-logo">
        ODITJI BUSINESS
    </div>

    <!-- 모바일 전용 메뉴 펼치기 버튼 (데스크톱에서는 숨김) -->
    <button type="button"
            class="sidebar-mobile-toggle"
            data-mobile-filter-toggle
            aria-expanded="false"
            aria-controls="businessSidebarMenu">
        <span>전체 메뉴</span>
        <span class="sidebar-mobile-toggle-arrow" aria-hidden="true">⌄</span>
    </button>


    <nav class="sidebar-menu" id="businessSidebarMenu">


        <!-- 사업자 홈 -->
        <div class="menu-group">

            <h3>사업자 홈</h3>

            <a href="${pageContext.request.contextPath}/business/main"
               class="${activeMenu == 'main' ? 'active' : ''}">
                대시보드
            </a>

        </div>



        <%--
            [미승인 사업자 마이페이지 제한]
            WAITING/REJECTED 상태에서는 대시보드만 노출하고,
            실제 사업 운영 메뉴는 APPROVED 상태에서만 표시합니다.
        --%>
        <c:choose>
            <c:when test="${sessionScope.businessStatus == 'APPROVED'}">

        <!-- 상품 관리 -->
        <div class="menu-group">

            <h3>상품 관리</h3>

            <a href="${pageContext.request.contextPath}/business/product/list"
               class="${activeMenu == 'product' ? 'active' : ''}">
                상품 목록
            </a>


            <a href="${pageContext.request.contextPath}/business/product/register"
               class="${activeMenu == 'productRegister' ? 'active' : ''}">
                상품 등록
            </a>

        </div>



        <!-- 이벤트 관리 -->
        <div class="menu-group">

            <h3>이벤트 관리</h3>

            <a href="${pageContext.request.contextPath}/business/event/list"
               class="${activeMenu == 'event' ? 'active' : ''}">
                이벤트 목록
            </a>


            <a href="${pageContext.request.contextPath}/business/event/register"
               class="${activeMenu == 'eventRegister' ? 'active' : ''}">
                이벤트 등록
            </a>

        </div>



        <!-- 주문 관리 -->
        <div class="menu-group">

            <h3>주문 관리</h3>


            <a href="${pageContext.request.contextPath}/business/order/list"
               class="${activeMenu == 'order' ? 'active' : ''}">
                주문 현황
            </a>


            <!--
                [배송 관리 기능 수정]
                기존 주문 상세 URL이 아니라 사업자 배송 관리 전용 목록으로 이동한다.
            -->
            <a href="${pageContext.request.contextPath}/business/delivery/list"
               class="${activeMenu == 'delivery' ? 'active' : ''}">
                배송 관리
            </a>


            <a href="${pageContext.request.contextPath}/business/cancel/list"
               class="${activeMenu == 'cancel' ? 'active' : ''}">
                취소/환불 관리
            </a>

        </div>



        <!-- 정산 관리 -->
        <div class="menu-group">

            <h3>정산 관리</h3>


            <a href="${pageContext.request.contextPath}/business/settlement/sales"
               class="${activeMenu == 'sales' ? 'active' : ''}">
                판매 현황
            </a>


            <a href="${pageContext.request.contextPath}/business/settlement/main"
               class="${activeMenu == 'settlement' ? 'active' : ''}">
                정산 관리
            </a>

        </div>



        <!-- 커뮤니티 -->
        <div class="menu-group">

            <h3>커뮤니티</h3>


            <a href="${pageContext.request.contextPath}/chat/list"
               class="${activeMenu == 'chat' ? 'active' : ''}">
                실시간 채팅
            </a>

        </div>


            </c:when>

            <c:otherwise>
                <div class="menu-group business-approval-menu">
                    <h3>사업자 승인</h3>

                    <span class="business-restricted-menu-text">
                        <c:choose>
                            <c:when test="${sessionScope.businessStatus == 'REJECTED'}">
                                승인 반려 상태
                            </c:when>
                            <c:otherwise>
                                승인 대기 상태
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
            </c:otherwise>
        </c:choose>

    </nav>


</aside>