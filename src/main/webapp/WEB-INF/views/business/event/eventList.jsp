<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 이벤트 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
<script defer src="${pageContext.request.contextPath}/js/business.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>

        <h1 class="page-title">
            이벤트 관리
        </h1>

        <!-- 처리 결과 메시지 -->
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <section class="content-panel">

            <div class="product-control-row">

                <div></div>

                <form action="${pageContext.request.contextPath}/business/event/list"
                      method="get"
                      class="product-search-form">

                    <%--
                        검색 입력창에 고유 id를 부여하고 label의 for와 연결한다.
                        label은 화면 배치에 영향을 주지 않도록 시각적으로만 숨긴다.
                    --%>
                    <label for="eventKeyword"
                           style="position:absolute;
                                  width:1px;
                                  height:1px;
                                  padding:0;
                                  margin:-1px;
                                  overflow:hidden;
                                  clip:rect(0, 0, 0, 0);
                                  white-space:nowrap;
                                  border:0;">
                        이벤트명, 상태 및 상품명 검색
                    </label>

                    <input type="text"
                           id="eventKeyword"
                           name="keyword"
                           value="<c:out value='${keyword}'/>"
                           placeholder="이벤트명, 상태, 상품명 검색">

                    <button type="submit">
                        검색
                    </button>

                </form>

                <button type="button"
                        class="product-register-btn"
                        onclick="location.href='${pageContext.request.contextPath}/business/event/register'">
                    등록 요청
                </button>

            </div>

            <table class="data-table mobile-fit-table">

                <thead>

                    <tr>
                        <th class="col-hide-mobile">번호</th>
                        <th>이벤트명</th>
                        <th>연결 상품</th>
                        <th class="col-hide-mobile">할인 적용가</th>
                        <th class="col-hide-mobile">이벤트 기간</th>
                        <th class="col-hide-mobile">상태</th>
                        <th class="col-hide-mobile">등록일</th>
                        <th>관리</th>
                    </tr>

                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty eventList}">

                            <c:forEach var="event"
                                       items="${eventList}">

                                <tr>

                                    <td class="col-hide-mobile">
                                        <c:out value="${event.eventNo}"/>
                                    </td>

                                    <%--
                                        [2단계] 모바일에서는 상태 뱃지 컬럼을 숨기는 대신,
                                        이벤트명 텍스트 색상으로 상태를 표시한다(mobile-status-text
                                        는 max-width:768px 미디어쿼리 안에서만 색을 입히므로
                                        데스크톱 표시는 그대로 유지된다).
                                    --%>
                                    <c:set var="eventStatusClass">
                                        <c:choose>
                                            <c:when test="${event.status eq 'APPROVED'}">st-ok</c:when>
                                            <c:when test="${event.status eq 'WAITING'}">st-waiting</c:when>
                                            <c:otherwise>st-reject</c:otherwise>
                                        </c:choose>
                                    </c:set>

                                    <td>
                                        <span class="mobile-status-text ${fn:trim(eventStatusClass)}">
                                            <c:out value="${event.title}"/>
                                        </span>
                                    </td>

                                    <td>
                                        <c:out value="${event.productName}"/>
                                    </td>

                                    <td class="col-hide-mobile">

                                        <c:choose>

                                            <c:when test="${event.eventDiscountRate > 0 and not empty event.price}">

                                                <span class="discount-rate">
                                                    <c:out value="${event.eventDiscountRate}"/>%
                                                </span>

                                                <br>

                                                <s>
                                                    <fmt:formatNumber value="${event.price}"
                                                                      pattern="#,###"/>원
                                                </s>

                                                →

                                                <strong>
                                                    <fmt:formatNumber value="${event.discountedPrice}"
                                                                      pattern="#,###"/>원
                                                </strong>

                                            </c:when>

                                            <c:otherwise>
                                                할인 없음
                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td class="col-hide-mobile">
                                        <c:out value="${event.startDate}"/>
                                        ~
                                        <c:out value="${event.endDate}"/>
                                    </td>

                                    <td class="col-hide-mobile">

                                        <c:choose>

                                            <c:when test="${event.status eq 'APPROVED'}">

                                                <span class="status ok">
                                                    승인 완료
                                                </span>

                                            </c:when>

                                            <c:when test="${event.status eq 'WAITING'}">

                                                <span class="status waiting">
                                                    승인 대기
                                                </span>

                                            </c:when>

                                            <c:when test="${event.status eq 'REJECTED'}">

                                                <span class="status">
                                                    승인 반려
                                                </span>

                                            </c:when>

                                            <c:when test="${event.status eq 'END'}">

                                                <span class="status">
                                                    종료
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="status">
                                                    <c:out value="${event.status}"/>
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td class="col-hide-mobile">
                                        ${dt:format(event.createdAt, 'yyyy-MM-dd')}
                                    </td>

                                    <td>

                                        <%--
                                            [2단계] 데스크톱 마크업/동작은 그대로 유지하고,
                                            desktop-only-el로 감싼다.
                                        --%>
                                        <div class="desktop-only-el">

                                            <!--
                                                관리자 승인이 완료된 APPROVED 이벤트만
                                                즉시 수정 또는 연장할 수 있다.
                                            -->
                                            <c:choose>

                                                <c:when test="${event.status eq 'APPROVED'}">

                                                    <%--
                                                        [리팩터링] 페이지 이동 대신 공용 수정 모달(#eventUpdateModal)을
                                                        연다. 이벤트마다 모달을 복제하지 않는 이유는 상품 수정 모달과
                                                        동일하다 - 상품 검색 팝업/날짜 최소값 제한/이미지 파일명 표시
                                                        로직(business.js)이 고정된 element id를 기준으로 동작하기
                                                        때문이다. 제목/설명/기간처럼 값이 하나뿐인 필드는 이 버튼의
                                                        data-* 값으로 싣고, openEventUpdateModal(this)이 그 값을
                                                        폼에 채운 뒤 모달을 연다. 연결 상품처럼 개수가 정해지지 않은
                                                        값은 아래 숨김 template(#eventProductData_${event.eventNo})에서
                                                        읽어온다.
                                                    --%>
                                                    <button type="button"
                                                            class="btn btn-dark"
                                                            data-event-no="${event.eventNo}"
                                                            data-title="${fn:escapeXml(event.title)}"
                                                            data-description="${fn:escapeXml(event.description)}"
                                                            data-start-date="${event.startDate}"
                                                            data-end-date="${event.endDate}"
                                                            data-banner-image="${fn:escapeXml(event.bannerImage)}"
                                                            onclick="openEventUpdateModal(this)">
                                                        수정
                                                    </button>

                                                    <%-- [리팩터링] 페이지 이동 대신 모달을 연다. --%>
                                                    <button type="button"
                                                            class="btn btn-dark"
                                                            onclick="openModal('eventExtendModal_${event.eventNo}')">
                                                        연장
                                                    </button>

                                                </c:when>

                                                <c:when test="${event.status eq 'WAITING'}">
                                                    승인 대기 중
                                                </c:when>

                                                <c:when test="${event.status eq 'REJECTED'}">
                                                    승인 반려
                                                </c:when>

                                                <c:otherwise>
                                                    처리 불가
                                                </c:otherwise>

                                            </c:choose>

                                        </div>

                                        <%--
                                            [2단계] 모바일에서는 상태별 여러 버튼/문구 대신
                                            "상세보기" 버튼 하나로 통합한다. 숨겨진 컬럼(번호/할인
                                            적용가/이벤트 기간/상태/등록일)과 상태별 조치는
                                            #eventDetailModal_${event.eventNo}에서 확인/수행한다.
                                        --%>
                                        <button type="button"
                                                class="btn btn-dark mobile-only-el"
                                                onclick="openModal('eventDetailModal_${event.eventNo}')">
                                            상세보기
                                        </button>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>
                                <td colspan="8">
                                    등록된 이벤트가 없습니다.
                                </td>
                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <a href="#">
                    ‹
                </a>

                <c:forEach var="p"
                           begin="1"
                           end="${empty pagination.totalPages
                               ? 1
                               : pagination.totalPages}">

                    <a href="?page=${p}"
                       class="${pagination.currentPage == p
                           ? 'active'
                           : ''}">
                        ${p}
                    </a>

                </c:forEach>

                <a href="#">
                    ›
                </a>

            </div>

        </section>

        <%--
            [리팩터링 추가] 이벤트 연장 모달
            페이지 이동 방식이던 구 eventExtend.jsp를 없애고, 그 폼을
            연장 가능한(APPROVED) 이벤트마다 별도 모달로 미리 렌더링해 둔다.
            폼 action/파라미터는 기존 컨트롤러(POST /business/event/extend)를
            그대로 재사용한다. 성공/실패 모두 이 목록(/business/event/list)으로
            리다이렉트되며(구 GET /business/event/extend 페이지는 컨트롤러에서도
            제거함), 위쪽 알림(.alert-success/.alert-error)으로 결과가 노출된다.
        --%>
        <c:forEach var="event" items="${eventList}">

            <c:if test="${event.status eq 'APPROVED'}">

                <div class="modal-overlay" id="eventExtendModal_${event.eventNo}">

                    <div class="modal-box">

                        <div class="modal-header">
                            <h3>이벤트 연장</h3>
                            <button type="button"
                                    class="modal-close"
                                    onclick="closeModal('eventExtendModal_${event.eventNo}')"
                                    aria-label="닫기">
                                &times;
                            </button>
                        </div>

                        <form action="${pageContext.request.contextPath}/business/event/extend"
                              method="post">

                            <input type="hidden"
                                   name="eventNo"
                                   value="<c:out value='${event.eventNo}'/>">

                            <div class="form-group">

                                <label class="form-label"
                                       for="eventTitleDisplay_${event.eventNo}">
                                    이벤트명
                                </label>

                                <input class="form-input"
                                       type="text"
                                       id="eventTitleDisplay_${event.eventNo}"
                                       value="<c:out value='${event.title}'/>"
                                       readonly>

                            </div>

                            <div class="form-group">

                                <label class="form-label"
                                       for="currentEventPeriod_${event.eventNo}">
                                    현재 이벤트 기간
                                </label>

                                <input class="form-input"
                                       type="text"
                                       id="currentEventPeriod_${event.eventNo}"
                                       value="<c:out value='${event.startDate}'/> ~ <c:out value='${event.endDate}'/>"
                                       readonly>

                            </div>

                            <div class="form-group">

                                <label class="form-label"
                                       for="extendEndDate_${event.eventNo}">
                                    연장 종료일
                                </label>

                                <input class="form-input extend-end-date-input"
                                       type="date"
                                       id="extendEndDate_${event.eventNo}"
                                       name="extendEndDate"
                                       min="<c:out value='${event.endDate}'/>"
                                       data-current-end-date="<c:out value='${event.endDate}'/>"
                                       required>

                            </div>

                            <div class="form-group">

                                <label class="form-label"
                                       for="extendReason_${event.eventNo}">
                                    연장 사유
                                </label>

                                <%-- 현재 EVENT 테이블에는 연장 사유 컬럼이 없으므로 입력한 사유는 서버 콘솔 로그로만 확인한다. --%>
                                <textarea class="form-textarea"
                                          id="extendReason_${event.eventNo}"
                                          name="extendReason"
                                          maxlength="1000"
                                          placeholder="이벤트 연장 사유를 입력해주세요."
                                          required></textarea>

                            </div>

                            <div class="form-group">

                                <label class="form-label"
                                       for="requestStatus_${event.eventNo}">
                                    요청 상태
                                </label>

                                <input class="form-input"
                                       type="text"
                                       id="requestStatus_${event.eventNo}"
                                       value="승인 대기"
                                       readonly>

                            </div>

                            <div class="modal-footer">

                                <button class="btn btn-primary"
                                        type="submit">
                                    연장 요청
                                </button>

                                <button class="btn btn-outline"
                                        type="button"
                                        onclick="closeModal('eventExtendModal_${event.eventNo}')">
                                    취소
                                </button>

                            </div>

                        </form>

                    </div>

                </div>

            </c:if>

        </c:forEach>

        <%--
            [2단계 모바일 반응형 추가] 이벤트 상세보기 모달 (모바일 전용 진입점)

            모바일 화면에서는 관리 컬럼의 여러 버튼/문구 대신 "상세보기"
            버튼 하나만 노출한다(business.css의 .mobile-only-el 참고).
            이 모달은 모바일에서 숨겨진 컬럼(번호/할인 적용가/이벤트
            기간/상태/등록일) 정보를 읽기 전용으로 보여주고, APPROVED
            이벤트라면 기존 수정/연장 모달을 그대로 여는 버튼을 제공한다.
            새로운 서버 호출이나 데이터는 필요 없다 - eventList에 이미
            들어있는 값만 사용한다. 데스크톱 마크업/동작은 건드리지 않고,
            이 모달 자체가 .mobile-only-el 트리거로만 열린다.
        --%>
        <c:forEach var="event" items="${eventList}">

            <div class="modal-overlay" id="eventDetailModal_${event.eventNo}">

                <div class="modal-box">

                    <div class="modal-header">
                        <h3>이벤트 상세 정보</h3>
                        <button type="button"
                                class="modal-close"
                                onclick="closeModal('eventDetailModal_${event.eventNo}')"
                                aria-label="닫기">
                            &times;
                        </button>
                    </div>

                    <div class="detail-grid">

                        <div>
                            <span class="detail-label">번호</span>
                            <p><c:out value="${event.eventNo}"/></p>
                        </div>

                        <div>
                            <span class="detail-label">상태</span>
                            <p>
                                <c:choose>
                                    <c:when test="${event.status eq 'APPROVED'}">
                                        <span class="status ok">승인 완료</span>
                                    </c:when>
                                    <c:when test="${event.status eq 'WAITING'}">
                                        <span class="status waiting">승인 대기</span>
                                    </c:when>
                                    <c:when test="${event.status eq 'REJECTED'}">
                                        <span class="status">승인 반려</span>
                                    </c:when>
                                    <c:when test="${event.status eq 'END'}">
                                        <span class="status">종료</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status"><c:out value="${event.status}"/></span>
                                    </c:otherwise>
                                </c:choose>
                            </p>
                        </div>

                        <div>
                            <span class="detail-label">이벤트명</span>
                            <p><c:out value="${event.title}"/></p>
                        </div>

                        <div>
                            <span class="detail-label">연결 상품</span>
                            <p><c:out value="${event.productName}"/></p>
                        </div>

                        <div>
                            <span class="detail-label">할인 적용가</span>
                            <p>
                                <c:choose>
                                    <c:when test="${event.eventDiscountRate > 0 and not empty event.price}">
                                        <span class="discount-rate"><c:out value="${event.eventDiscountRate}"/>%</span>
                                        <s><fmt:formatNumber value="${event.price}" pattern="#,###"/>원</s>
                                        →
                                        <strong><fmt:formatNumber value="${event.discountedPrice}" pattern="#,###"/>원</strong>
                                    </c:when>
                                    <c:otherwise>할인 없음</c:otherwise>
                                </c:choose>
                            </p>
                        </div>

                        <div>
                            <span class="detail-label">이벤트 기간</span>
                            <p><c:out value="${event.startDate}"/> ~ <c:out value="${event.endDate}"/></p>
                        </div>

                        <div>
                            <span class="detail-label">등록일</span>
                            <p>${dt:format(event.createdAt, 'yyyy-MM-dd')}</p>
                        </div>

                    </div>

                    <div class="modal-footer">

                        <c:choose>

                            <c:when test="${event.status eq 'APPROVED'}">

                                <%--
                                    데스크톱 "수정" 버튼과 동일한 data-* 값을 실어 두고,
                                    상세 모달을 닫은 뒤 그대로 openEventUpdateModal(this)를
                                    호출한다(같은 함수가 openModal('eventUpdateModal')까지
                                    처리하므로 별도 호출이 필요 없다).
                                --%>
                                <button type="button"
                                        class="btn btn-dark"
                                        data-event-no="${event.eventNo}"
                                        data-title="${fn:escapeXml(event.title)}"
                                        data-description="${fn:escapeXml(event.description)}"
                                        data-start-date="${event.startDate}"
                                        data-end-date="${event.endDate}"
                                        data-banner-image="${fn:escapeXml(event.bannerImage)}"
                                        onclick="closeModal('eventDetailModal_${event.eventNo}'); openEventUpdateModal(this)">
                                    수정
                                </button>

                                <button type="button"
                                        class="btn btn-dark"
                                        onclick="closeModal('eventDetailModal_${event.eventNo}'); openModal('eventExtendModal_${event.eventNo}')">
                                    연장
                                </button>

                            </c:when>

                            <c:when test="${event.status eq 'WAITING'}">
                                <span class="form-help">승인 대기 중인 이벤트입니다.</span>
                            </c:when>

                            <c:when test="${event.status eq 'REJECTED'}">
                                <span class="form-help">승인 반려된 이벤트입니다.</span>
                            </c:when>

                            <c:otherwise>
                                <span class="form-help">처리할 수 없는 이벤트입니다.</span>
                            </c:otherwise>

                        </c:choose>

                        <button type="button"
                                class="btn btn-outline"
                                onclick="closeModal('eventDetailModal_${event.eventNo}')">
                            닫기
                        </button>

                    </div>

                </div>

            </div>

        </c:forEach>

        <%--
            [리팩터링 추가] 이벤트별 연결 상품 데이터 (숨김 template)

            공용 수정 모달(#eventUpdateModal)은 이벤트마다 복제되지 않으므로,
            제목/기간처럼 값이 하나뿐인 필드는 "수정" 버튼의 data-* 값으로
            충분하지만, 연결 상품은 이벤트마다 개수가 다르다.

            그래서 이벤트별 연결 상품만 <template>(브라우저가 렌더링하지
            않는 비활성 콘텐츠) 안에, 상품 검색 결과 행과 동일하게 c:out으로
            이스케이프한 data-* 속성을 가진 요소로 미리 렌더링해 둔다.
            "수정" 버튼을 누르면 openEventUpdateModal(this)가 같은 eventNo의
            template을 찾아 그 안의 데이터로 연결 상품 입력 행을 다시 그린다.
        --%>
        <c:forEach var="event" items="${eventList}">

            <c:if test="${event.status eq 'APPROVED'}">

                <template id="eventProductData_${event.eventNo}">

                    <c:forEach var="connectedProduct"
                               items="${event.connectedProducts}">

                        <div class="event-product-data"
                             data-product-no="<c:out value='${connectedProduct.productNo}'/>"
                             data-product-name="<c:out value='${connectedProduct.productName}'/>"
                             data-price="<c:out value='${connectedProduct.price}'/>"
                             data-discount-rate="<c:out value='${connectedProduct.discountRate}'/>">
                        </div>

                    </c:forEach>

                </template>

            </c:if>

        </c:forEach>

        <%--
            [리팩터링 추가] 이벤트 수정 요청 모달 (공용 1개)

            페이지 이동 방식이던 구 eventUpdate.jsp를 없애고, 그 폼을 이벤트
            목록 전체가 공유하는 모달 1개로 통합했다. 상품 수정 모달과 동일한
            이유(상품 검색 팝업/날짜 최소값 제한/이미지 파일명 표시 로직이
            고정된 element id를 기준으로 동작)로 이벤트마다 모달을 복제하지
            않는다.

            "수정" 버튼을 누르면 openEventUpdateModal(this)가 버튼의 data-*
            값과 위 숨김 template의 연결 상품 데이터를 이 폼에 채워 넣고
            모달을 연다. 폼 action/파라미터는 기존 컨트롤러
            (POST /business/event/update)를 그대로 재사용한다. 성공/실패
            모두 이 목록(/business/event/list)으로 리다이렉트되며(구
            GET /business/event/update 페이지는 컨트롤러에서도 제거함),
            위쪽 알림(.alert-success/.alert-error)으로 결과가 노출된다.
        --%>
        <div class="modal-overlay" id="eventUpdateModal">

            <div class="modal-box">

                <div class="modal-header">

                    <h3>이벤트 수정 요청</h3>

                    <button type="button"
                            class="modal-close"
                            onclick="closeModal('eventUpdateModal')"
                            aria-label="닫기">
                        &times;
                    </button>

                </div>

                <form action="${pageContext.request.contextPath}/business/event/update"
                      method="post"
                      enctype="multipart/form-data"
                      id="eventForm">

                    <input type="hidden"
                           id="updateEventNo"
                           name="eventNo"
                           value="">

                    <!-- 이벤트명 -->
                    <div class="form-group">

                        <label class="form-label"
                               for="eventTitle">
                            이벤트명
                        </label>

                        <input class="form-input"
                               type="text"
                               id="eventTitle"
                               name="eventTitle"
                               maxlength="200"
                               required>

                    </div>

                    <!-- 이벤트 설명 -->
                    <div class="form-group">

                        <label class="form-label"
                               for="description">
                            이벤트 설명
                        </label>

                        <textarea class="form-textarea"
                                  id="description"
                                  name="description"
                                  placeholder="이벤트에 대한 설명을 입력하세요."></textarea>

                    </div>

                    <!-- 이벤트 기간 -->
                    <div class="form-group">

                        <span class="form-label">
                            이벤트 기간
                        </span>

                        <div class="event-date-row">

                            <%--
                                두 날짜 입력창이 하나의 공통 제목 아래에 있으므로
                                aria-label을 통해 각 입력창의 역할을 구분합니다.
                            --%>
                            <input class="form-input"
                                   type="date"
                                   id="startDate"
                                   aria-label="이벤트 시작일"
                                   name="startDate"
                                   required>

                            <span class="event-date-separator">
                                -
                            </span>

                            <input class="form-input"
                                   type="date"
                                   id="endDate"
                                   aria-label="이벤트 종료일"
                                   name="endDate"
                                   required>

                        </div>

                    </div>

                    <!-- 연결 상품: 여러 개 연결 가능 -->
                    <%--
                        연결 상품은 여러 입력 행을 포함하는 하나의 입력 그룹입니다.
                        ARIA 그룹 역할 대신 네이티브 fieldset/legend를 사용하여
                        브라우저와 보조기기에서 동일한 의미로 해석되도록 합니다.
                    --%>
                    <fieldset class="form-group event-product-fieldset">

                        <legend class="form-label">
                            연결 상품
                        </legend>

                        <div id="productList"></div>

                    </fieldset>

                    <!-- 요청 상태 -->
                    <div class="form-group">

                        <%--
                            이벤트 상태는 사업자가 직접 지정할 수 없습니다.

                            수정 요청이 접수되면 EVENT.STATUS는 WAITING으로 변경되고,
                            관리자 승인 전에는 사용자 화면에 노출되지 않습니다.
                        --%>
                        <label class="form-label"
                               for="requestStatus">
                            요청 상태
                        </label>

                        <input class="form-input"
                               type="text"
                               id="requestStatus"
                               value="승인 대기"
                               readonly>

                    </div>

                    <!-- 이벤트 이미지 -->
                    <div class="form-group">

                        <label class="form-label"
                               for="eventImage">
                            이벤트 이미지
                        </label>

                        <div class="file-box">

                            <input type="file"
                                   id="eventImage"
                                   name="eventImage"
                                   accept=".jpg,.jpeg,.png,.gif,.webp">

                            <span id="eventImageFileName">
                                선택된 파일 없음
                            </span>

                        </div>

                        <p class="form-help">새 이미지를 선택하지 않으면 기존 이미지가 그대로 유지됩니다.</p>

                    </div>

                    <div class="modal-footer">

                        <button class="btn btn-primary"
                                type="submit">
                            이벤트 수정 요청
                        </button>

                        <button class="btn btn-outline"
                                type="button"
                                onclick="closeModal('eventUpdateModal')">
                            취소
                        </button>

                    </div>

                </form>

            </div>

        </div>

        <%--
            [리팩터링 추가] 이벤트 연결 상품 검색 모달

            구 eventUpdate.jsp(및 eventRegister.jsp)에 있던 상품 검색 모달과
            동일한 마크업이다. eventList.jsp에서도 동일한 상품 검색 기능이
            필요해 이 화면으로 옮겨왔다. business.js의 상품 검색 로직은
            고정된 id(productSearchModal 등)를 기준으로 동작하므로 페이지당
            1개만 존재한다.
        --%>
        <dialog class="product-search-modal"
                id="productSearchModal"
                open
                aria-modal="true"
                aria-labelledby="productSearchModalTitle">

            <%--
                네이티브 dialog 요소를 사용해 보조 기술과 브라우저가
                상품 검색 모달을 일관되게 인식하도록 합니다.
            --%>
            <div class="product-search-modal-panel">

                <div class="product-search-modal-header">

                    <h2 class="product-search-modal-title"
                        id="productSearchModalTitle">
                        이벤트 연결 상품 검색
                    </h2>

                    <button class="product-search-modal-close"
                            type="button"
                            id="productSearchModalClose"
                            aria-label="상품 검색 창 닫기">
                        ×
                    </button>

                </div>

                <!-- 상품 검색 입력 영역 -->
                <div class="product-search-bar">

                    <%--
                        검색창의 시각적 디자인은 유지하면서
                        화면 낭독기용 label을 제공합니다.
                    --%>
                    <label for="productSearchKeyword"
                           class="sr-only">
                        상품 검색어
                    </label>

                    <input class="form-input"
                           type="text"
                           id="productSearchKeyword"
                           placeholder="상품명, 작품명, 배우명, 상품 종류를 검색하세요.">

                    <button class="btn btn-dark"
                            type="button"
                            id="productSearchResetButton">
                        초기화
                    </button>

                </div>

                <!-- 상품 검색 결과 -->
                <div class="product-search-result">

                    <table class="product-search-table">

                        <thead>

                            <tr>
                                <th>상품명</th>
                                <th>작품</th>
                                <th>배우</th>
                                <th>가격</th>
                                <th>상태</th>
                                <th>선택</th>
                            </tr>

                        </thead>

                        <tbody>

                            <c:forEach var="product"
                                       items="${productList}">

                                <!-- 삭제 요청 중인 상품은 연결 대상에서 제외합니다. -->
                                <c:if test="${product.status ne 'DELETE_REQUESTED'}">

                                    <tr class="product-search-row"
                                        data-search-text="<c:out value='${product.productName} ${product.contentTitle} ${product.actorName} ${product.productType}'/>">

                                        <td>
                                            <c:out value="${product.productName}"/>
                                        </td>

                                        <td>
                                            <c:out value="${product.contentTitle}"/>
                                        </td>

                                        <td>
                                            <c:out value="${product.actorName}"/>
                                        </td>

                                        <td>

                                            <fmt:formatNumber
                                                    value="${product.price}"
                                                    pattern="#,###"/>원

                                        </td>

                                        <td>
                                            <c:out value="${product.status}"/>
                                        </td>

                                        <td>

                                            <button class="btn btn-primary product-select-button"
                                                    type="button"
                                                    data-product-no="<c:out value='${product.productNo}'/>"
                                                    data-product-name="<c:out value='${product.productName}'/>"
                                                    data-product-price="<c:out value='${product.price}'/>">
                                                선택
                                            </button>

                                        </td>

                                    </tr>

                                </c:if>

                            </c:forEach>

                        </tbody>

                    </table>

                    <div class="product-search-no-result"
                         id="productSearchNoResult">
                        검색 결과가 없습니다.
                    </div>

                </div>

            </div>

        </dialog>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>