<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<aside class="admin-sidebar">

    <div class="sidebar-logo">
        ODITJI ADMIN
    </div>

    <!-- 모바일 전용 메뉴 펼치기 버튼 (데스크톱에서는 숨김) -->
    <button type="button"
            class="sidebar-mobile-toggle"
            data-mobile-filter-toggle
            aria-expanded="false"
            aria-controls="adminSidebarMenu">
        <span>전체 메뉴</span>
        <span class="sidebar-mobile-toggle-arrow" aria-hidden="true">⌄</span>
    </button>


    <nav class="sidebar-menu" id="adminSidebarMenu">


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

        <!--
            이벤트 관리 (회원 관리와 동일한 방식)

            [정리됨] 예전에는 이벤트 등록/수정/연장 "요청 유형"별로 하위 메뉴 3개를 나누고
            각각 tab=register/update/extend 파라미터로 구분했었다. 하지만 지금 컨트롤러
            (AdminController#eventList)와 eventManage.jsp의 tab 파라미터는 요청 유형이 아니라
            "상태"(승인 대기/승인 완료/종료) 값을 쓰고 있어 더 이상 이 하위 메뉴들과 맞지 않았고,
            그 결과 tab이 비어있을 때(전체 이벤트를 보고 있을 때도) 항상 "이벤트 등록"만
            눌린 것처럼 표시되는 문제가 있었다. EVENT 테이블에는 요청 유형을 구분하는 컬럼이
            아직 없어 하위 메뉴로 나눌 근거도 없으므로, 회원 관리처럼 activeMenu만으로
            판단하는 단일 링크로 정리한다.
        -->
        <div class="menu-group">

            <h3>이벤트 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/event/list"
               class="${activeMenu == 'event' ? 'active' : ''}">
                이벤트 관리
            </a>

        </div>

        <!--
            상품 관리 (사업자 등록/수정/삭제 요청 처리)

            [정리됨] 이벤트 관리와 동일한 이유로 정리했다. 예전에는 요청 유형별로
            등록/수정/삭제 하위 메뉴 3개를 나누고 tab=register/update/delete로
            구분했지만, PRODUCT 테이블에는 요청 유형을 구분하는 컬럼이 없어
            등록/수정 탭이 실제로는 같은 WAITING 목록을 보여주고 있었다.
            지금은 productManage.jsp의 tab 파라미터가 "상태"(승인 대기/승인 완료/
            삭제 요청) 값을 쓰므로, 회원·이벤트 관리처럼 activeMenu만으로
            판단하는 단일 링크로 정리한다.
        -->
        <div class="menu-group">

            <h3>상품 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/product/list"
               class="${activeMenu == 'product' ? 'active' : ''}">
                상품 관리
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

        <!--
            사업자 관리 (이벤트/상품 관리와 동일한 방식)

            [정리됨] 예전에는 tab=info/approval 하위 메뉴 2개로 나눠져 있었지만,
            businessManage.jsp에 상단 통계 카드(입점 완료/승인 대기)와 페이지 내
            tab-menu(사업자 목록/사업자 승인 관리)가 이미 같은 전환을 제공하고 있어
            사이드바에서까지 나누면 같은 기능이 중복된다. 이벤트/상품 관리처럼
            activeMenu만으로 판단하는 단일 링크로 정리한다.
        -->
        <div class="menu-group">

            <h3>사업자 관리</h3>

            <a href="${pageContext.request.contextPath}/admin/business/list"
               class="${activeMenu == 'business' ? 'active' : ''}">
                사업자 관리
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

        <!-- 사업자 공지 채팅 -->
        <div class="menu-group">

            <h3>사업자 커뮤니티</h3>

            <a href="${pageContext.request.contextPath}/chat/list"
               class="${activeMenu == 'chat' ? 'active' : ''}">
                공지 채팅방
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
