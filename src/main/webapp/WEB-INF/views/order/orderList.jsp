<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>ODITJI | 주문 내역</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/order.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/review.css">

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/payment.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pagination-common.css?v=1">
    <script defer src="${pageContext.request.contextPath}/js/pagination.js?v=1"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-list-container"
      data-context-path="${pageContext.request.contextPath}"
      data-portone-test-mode="${portOneTestMode}"
      data-active-tab="${activeTab}">

    <%--
        [추가] 상품 리뷰 작성 실패 메시지를 외부 JavaScript에서 읽을 수 있도록 저장한다.
        화면에는 표시하지 않으며, order.js에서 alert 메시지로 사용한다.
    --%>
    <c:if test="${not empty reviewMessage}">

        <div id="reviewMessageData"
             data-message="<c:out value='${reviewMessage}'/>"
             hidden>
        </div>

    </c:if>

    <%--
        =========================================================
        [주문/환불 탭 UI 추가]

        orderList.jsp 안에서 "주문 내역"과 "환불 내역"을 탭으로
        전환하는 UI만 구성한다. 실제 데이터 조회 로직은 그대로
        유지하며(orderList 모델은 기존과 동일), 환불 내역 탭은
        기존에 함께 내려오는 주문/취소 요청 데이터를 화면에서
        다시 정리해서 보여준다.
        =========================================================
    --%>
    <div class="order-tabs"
         role="tablist"
         aria-label="주문/환불 내역 탭">

        <button type="button"
                class="order-tab-btn ${activeTab eq 'history' ? '' : 'active'}"
                data-tab-target="orderTabPanel"
                role="tab"
                aria-selected="${activeTab eq 'history' ? 'false' : 'true'}">
            구매내역
        </button>

        <button type="button"
                class="order-tab-btn ${activeTab eq 'history' ? 'active' : ''}"
                data-tab-target="refundTabPanel"
                role="tab"
                aria-selected="${activeTab eq 'history' ? 'true' : 'false'}">
            취소/환불 내역
        </button>

    </div>

    <!-- 주문 내역 탭 패널 -->
    <div id="orderTabPanel"
         class="order-tab-panel ${activeTab eq 'history' ? '' : 'active'}"
         role="tabpanel"
         ${activeTab eq 'history' ? 'hidden' : ''}>

    <h1>구매내역</h1>

    <!-- 주문 내역 없음 -->
    <c:if test="${empty orderList}">

        <div class="empty-box">
            주문 내역이 없습니다.
        </div>

    </c:if>

    <!-- 주문 목록 -->
    <c:forEach var="o"
               items="${orderList}">

        <section class="order-card">

            <div class="order-card-main">

                <!-- 왼쪽: 주문번호, 주문일시, 상품 목록 -->
                <div class="order-product-section">

                    <div class="order-card-header">

                        <div>

                            <strong>주문번호</strong>

                            <span>
                                ${o.orderNo}
                            </span>

                        </div>

                        <div>

                            ${dt:format(o.createdAt, 'yyyy.MM.dd HH:mm')}

                        </div>

                    </div>

                    <div class="order-items">

                        <c:forEach var="i"
                                   items="${o.items}">

                            <div class="order-item">

                                <%--
                                    =========================================================
                                    [상품 선택 체크박스 추가]

                                    취소 요청이 가능한 상태(주문 완료/결제 완료/상품 준비 중/
                                    배송 중)의 상품에만 체크박스를 노출하여, 상단의
                                    "선택 상품 주문 취소" 버튼과 함께 여러 상품을 한 번에
                                    선택할 수 있는 UI를 구성한다. 체크박스 선택 값 처리와
                                    실제 취소 로직은 이번 작업 범위에 포함하지 않는다.
                                    =========================================================
                                --%>
                                <c:if test="${i.cancelEligible || i.refundEligible}">

                                    <%-- [수정] 주문 확인중은 취소, 배송 완료는 환불 선택 가능 --%>
                                    <label class="order-item-select-label">
                                        <input type="checkbox"
                                               class="order-item-select"
                                               value="${i.orderItemNo}"
                                               data-product-name="${fn:escapeXml(i.productName)}"
                                               data-action-type="${i.refundEligible ? 'REFUND' : 'CANCEL'}"
                                               <%-- [추가] 잘못된 취소/환불 버튼 클릭 시 배송 상태에 맞는 안내를 표시한다. --%>
                                               data-delivery-status="${i.deliveryStatus}"
                                               aria-label="${fn:escapeXml(i.productName)} 선택">
                                    </label>

                                </c:if>

                                <a href="${pageContext.request.contextPath}/goods/goodsDetail/${i.productNo}">

                                    <c:choose>

                                        <c:when test="${empty i.mainImage}">

                                            <div class="no-image">
                                                NO IMAGE
                                            </div>

                                        </c:when>

                                        <c:otherwise>

                                            <img src="${pageContext.request.contextPath}${i.mainImage}"
                                                 alt="${i.productName}">

                                        </c:otherwise>

                                    </c:choose>

                                    <div class="order-item-info">

                                        <div>
                                            <c:out value="${i.productName}"/>
                                        </div>

                                        <%--
                                            =========================================================
                                            [상품 옵션 정보 표시 추가]

                                            구매 당시 선택한 상품 옵션이 존재하는 경우에만
                                            색상과 사이즈 정보를 주문 내역에 표시한다.
                                            =========================================================
                                        --%>
                                        <c:if test="${not empty i.optionNo}">
                                            <div class="order-item-option">

                                                <span class="option-label">옵션</span>

                                                <c:if test="${not empty i.colorName}">
                                                    <span>
                                                        색상:
                                                        <c:out value="${i.colorName}"/>
                                                    </span>
                                                </c:if>

                                                <c:if test="${not empty i.sizeName}">
                                                    <span>
                                                        사이즈:
                                                        <c:out value="${i.sizeName}"/>
                                                    </span>
                                                </c:if>

                                            </div>
                                        </c:if>

                                        <div>
                                            수량: ${i.quantity}
                                        </div>

                                        <div>

                                            ₩

                                            <fmt:formatNumber
                                                value="${i.itemTotalPrice}"
                                                pattern="#,###"/>

                                        </div>

                                    </div>
                                </a>

                                <div class="order-item-actions">

                                    <%--
                                        =========================================================
                                        [상품별 배송조회 버튼 추가]

                                        취소가 완료된 상품이 아니면 배송조회 탭으로 전환하여
                                        해당 주문상품의 배송 정보를 볼 수 있도록 한다.
                                        =========================================================
                                    --%>
                                    <c:if test="${i.status eq 'DELIVERED'}">

                                        <button type="button"
                                                class="delivery-detail-btn"
                                                data-order-item-no="${i.orderItemNo}">
                                            배송조회
                                        </button>

                                    </c:if>

                                    <c:if test="${o.orderStatus ne 'CANCELED'}">

                                        <button type="button"
                                                class="review-btn"
                                                data-order-item="${i.orderItemNo}"
                                                data-product="${i.productNo}">
                                            리뷰 작성
                                        </button>

                                    </c:if>

                                    <%--
                                        =========================================================
                                        [상품별 액션 버튼 정리]

                                        상품별 액션 영역에는 "배송조회"와 "리뷰 작성"만 남기고,
                                        상태별 "주문 취소" / "환불 신청" 버튼은 제거한다.
                                        상품 취소·환불은 체크박스로 선택한 뒤 주문 카드 하단의
                                        "선택 상품 주문 취소" 버튼, 환불 내역 탭의
                                        "선택 상품 환불" 버튼으로 처리하는 방식으로 통일한다.

                                        취소 진행 상태는 버튼이 아닌 안내 문구로만 표시한다.
                                        =========================================================
                                    --%>
                                    <c:if test="${i.status eq 'CANCEL_REQUEST'}">

                                        <span class="item-cancel-state">
                                            취소 승인 대기
                                        </span>

                                    </c:if>

                                    <c:if test="${i.status eq 'CANCELED'}">

                                        <span class="item-cancel-state canceled">
                                            취소 완료
                                        </span>

                                    </c:if>

                                </div>

                            </div>

                        </c:forEach>

                    </div>

                </div>

                <!-- 오른쪽: 주문 상태와 주문 정보 -->
                <aside class="order-info-section">

                    <h2 class="order-info-title">
                        주문 정보
                    </h2>

                    <div class="order-info-list">

                        <div class="order-info-row">

                            <div class="order-info-label">
                                주문 상태
                            </div>

                            <div class="order-info-value">

                                <span class="order-info-status">

                                    <c:choose>

                                        <c:when test="${o.orderStatus eq 'ORDERED'}">주문 완료</c:when>
                                        <c:when test="${o.orderStatus eq 'PAID'}">결제 완료</c:when>
                                        <c:when test="${o.orderStatus eq 'PREPARING'}">상품 준비 중</c:when>
                                        <c:when test="${o.orderStatus eq 'SHIPPING'}">배송 중</c:when>
                                        <c:when test="${o.orderStatus eq 'DELIVERED'}">배송 완료</c:when>
                                        <c:when test="${o.orderStatus eq 'CANCEL_REQUEST'}">취소 요청</c:when>
                                        <c:when test="${o.orderStatus eq 'PARTIAL_CANCELED'}">부분 취소</c:when>
                                        <c:when test="${o.orderStatus eq 'CANCELED'}">주문 취소</c:when>
                                        <c:otherwise>${o.orderStatus}</c:otherwise>

                                    </c:choose>

                                </span>

                            </div>

                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">
                                주문자 이름
                            </div>

                            <div class="order-info-value">
                                <c:out value="${empty o.receiverName ? '-' : o.receiverName}"/>
                            </div>

                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">
                                주문자 전화번호
                            </div>

                            <div class="order-info-value">
                                <c:out value="${empty o.receiverPhone ? '-' : o.receiverPhone}"/>
                            </div>

                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">
                                배송지 주소
                            </div>

                            <div class="order-info-value">
                                <c:out value="${empty o.address ? '-' : o.address}"/>
                            </div>

                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">
                                결제 금액
                            </div>

                            <div class="order-info-value order-info-price">

                                ₩

                                <fmt:formatNumber
                                    value="${o.totalAmount}"
                                    pattern="#,###"/>

                            </div>

                        </div>

                    </div>

                </aside>

            </div>

            <%--
                =========================================================
                [주문 전체 취소 처리 상태 및 반려 사유 표시 추가]

                가장 최근 FULL 취소 그룹의 처리 상태를 주문 단위로
                표시한다.

                WAITING
                - 전체 취소 승인 대기 상태를 표시한다.
                - 전체 취소 버튼은 숨긴다.

                REJECTED
                - 전체 취소 반려 상태와 반려 사유를 표시한다.
                - 전체 취소 버튼은 숨긴다.

                APPROVED
                - 전체 취소 완료 상태를 표시한다.

                취소 요청 없음
                - 기존 주문 전체 취소 버튼을 표시한다.
                =========================================================
            --%>
            <c:choose>

                <c:when test="${o.fullCancelStatus eq 'WAITING'}">

                    <div class="full-cancel-status-box waiting">

                        <strong class="full-cancel-status-title">
                            전체 주문 취소 승인 대기
                        </strong>

                        <p class="full-cancel-status-message">
                            주문에 포함된 사업자의 승인을 기다리고 있습니다.
                        </p>

                    </div>

                </c:when>

                <c:when test="${o.fullCancelStatus eq 'REJECTED'}">

                    <div class="full-cancel-status-box rejected">

                        <strong class="full-cancel-status-title">
                            전체 주문 취소 반려
                        </strong>

                        <p class="full-cancel-status-message">

                            <span class="full-cancel-reject-label">
                                반려 사유:
                            </span>

                            <c:choose>

                                <c:when test="${not empty o.fullCancelRejectReason}">
                                    <c:out value="${o.fullCancelRejectReason}"/>
                                </c:when>

                                <c:otherwise>
                                    사업자가 전체 취소/환불 요청을 반려했습니다.
                                </c:otherwise>

                            </c:choose>

                        </p>

                    </div>

                </c:when>

                <c:when test="${o.fullCancelStatus eq 'APPROVED'}">

                    <div class="full-cancel-status-box approved">

                        <strong class="full-cancel-status-title">
                            전체 주문 취소 완료
                        </strong>

                        <p class="full-cancel-status-message">
                            주문 전체 취소와 결제 환불 처리가 완료되었습니다.
                        </p>

                    </div>

                </c:when>

            </c:choose>

            <div class="ticket-divider"
                 aria-hidden="true">
            </div>

            <div class="order-card-footer">

                <div class="order-total">

                    <span>
                        총 결제금액
                    </span>

                    <span class="order-total-price">

                        ₩

                        <fmt:formatNumber
                            value="${o.totalAmount}"
                            pattern="#,###"/>

                    </span>

                </div>

                <%--
                    =========================================================
                    [전체 취소 반려 후 재요청 버튼 유지]

                    전체 취소 요청이 반려됐더라도 주문상품 상태가
                    취소 가능한 상태라면 사용자가 다시 전체 취소를
                    요청할 수 있도록 기존 버튼을 표시한다.
                    =========================================================
                --%>
                <c:if test="${empty o.fullCancelStatus || o.fullCancelStatus eq 'REJECTED'}">
                    <div class="order-cancel-action">
                        <%-- [추가] 상품 선택 없이 주문 전체 취소 --%>
                        <%-- [수정] 전체 상품이 모두 취소 가능할 때만 전체 취소 버튼 노출 --%>
                        <c:if test="${o.allCancelEligible}">
                            <button type="button"
                                    class="bulk-action-btn payment-cancel-btn full-order-cancel-btn"
                                    data-order-no="${o.orderNo}">
                                전체 상품 주문 취소
                            </button>
                        </c:if>

                        <%-- [수정] 취소 가능한 상품이 하나라도 남아 있으면 선택 취소 버튼 유지 --%>
                        <c:if test="${o.anyCancelEligible}">
                            <button type="button"
                                    class="bulk-action-btn order-select-cancel-btn"
                                    data-action-type="CANCEL"
                                    data-order-no="${o.orderNo}"
                                    <%-- [추가] 테스트 채널 간편결제의 부분 취소 가능 여부를 JavaScript에서 확인한다. --%>
                                    data-pay-method="${fn:escapeXml(o.payMethod)}">
                                선택 상품 주문 취소
                            </button>
                        </c:if>

                        <%-- [수정] 모든 상품이 배송 완료 상태일 때만 전체 환불 버튼 노출 --%>
                        <c:if test="${o.allRefundEligible}">
                            <button type="button"
                                    class="bulk-action-btn payment-cancel-btn full-order-refund-btn"
                                    data-order-no="${o.orderNo}"
                                    data-request-kind="REFUND">
                                전체 상품 환불
                            </button>
                        </c:if>

                        <%-- [수정] 환불 가능한 상품이 하나라도 남아 있으면 선택 환불 버튼 유지 --%>
                        <c:if test="${o.anyRefundEligible}">
                            <button type="button"
                                    class="bulk-action-btn order-select-cancel-btn selected-refund-btn"
                                    data-action-type="REFUND"
                                    data-order-no="${o.orderNo}"
                                    <%-- [추가] 테스트 채널 간편결제의 부분 환불 가능 여부를 JavaScript에서 확인한다. --%>
                                    data-pay-method="${fn:escapeXml(o.payMethod)}">
                                선택 상품 환불
                            </button>
                        </c:if>
                    </div>
                </c:if>

            </div>

        </section>

    </c:forEach>

    <!-- [수정] 콘텐츠 목록과 동일한 주문내역 페이지네이션 -->
    <c:if test="${not empty orderList}">
        <nav class="oditji-pagination"
             data-pagination
             data-current-page="${pageVO.currentPage}"
             data-total-page="${pageVO.totalPage}"
             data-page-param="page"
             data-fixed-param-name="tab"
             data-fixed-param-value="order"
             aria-label="주문내역 페이지"></nav>
    </c:if>

    </div>
    <!-- // 주문 내역 탭 패널 -->

    <%--
        =========================================================
        [환불 내역 탭 패널 추가]

        Controller/Service/Mapper 수정 없이, 이미 orderList 모델에
        포함되어 있는 주문 전체 취소(o.fullCancelStatus)와 상품
        부분 취소(i.cancelRequestStatus) 데이터를 화면에서 다시
        모아 표(table) 형태로 보여준다.

        "신청일" 컬럼은 취소 요청 시각을 담는 필드가 현재
        OrderVO/OrderItemVO에 없어 주문일(createdAt)로 대신 표시했다.
        실제 취소/환불 신청일을 보여주려면 서버에서 해당 값을
        내려주는 작업이 추후 필요하다.
        =========================================================
    --%>
    <div id="refundTabPanel"
         class="order-tab-panel ${activeTab eq 'history' ? 'active' : ''}"
         role="tabpanel"
         ${activeTab eq 'history' ? '' : 'hidden'}>

        <h1>취소/환불 내역</h1>

        <%-- =========================================================
             [수정] 취소/환불 유형·처리상태·기간 서버 조회 조건
             달력 입력값은 GET 파라미터로 Controller/Mapper까지 전달된다.

             [수정] 조회 기간 기본값 개선
             기존에는 시작일 입력칸에 항상 "오늘 날짜"가 채워져 있어,
             사용자가 아무 것도 고르지 않고 조회를 눌러도 오늘 하루치
             데이터만 조회되는 것처럼 보이는 문제가 있었다. 이제는
             사용자가 실제로 조회에 사용한 값(param.startDate/endDate)만
             그대로 되돌려 보여주므로, 처음 들어왔을 때나 "초기화" 후에는
             두 칸 모두 비어 있고 이는 곧 "전체 기간 조회"를 의미한다.

             대신 실제 쇼핑몰 주문내역 화면처럼 1주일/1개월/3개월/6개월
             빠른 선택 버튼을 눌러 기간을 즉시 채우고 바로 조회할 수
             있게 했다(동작은 orderTabs.js 참고).
        ========================================================== --%>
        <form class="refund-search-form"
              id="refundSearchForm"
              action="${pageContext.request.contextPath}/order/list"
              method="get">
            <input type="hidden" name="tab" value="history">

            <div class="refund-search-field">
                <label for="historyTypeFilter">구분</label>
                <select id="historyTypeFilter" name="historyType">
                    <option value="ALL" ${historyType eq 'ALL' ? 'selected' : ''}>전체</option>
                    <option value="REFUND" ${historyType eq 'REFUND' ? 'selected' : ''}>환불</option>
                    <option value="CANCEL" ${historyType eq 'CANCEL' ? 'selected' : ''}>취소</option>
                </select>
            </div>

            <div class="refund-search-field">
                <label for="historyStatusFilter">처리 상태</label>
                <select id="historyStatusFilter" name="historyStatus">
                    <option value="ALL" ${historyStatus eq 'ALL' ? 'selected' : ''}>전체</option>
                    <option value="WAITING" ${historyStatus eq 'WAITING' ? 'selected' : ''}>승인 대기</option>
                    <option value="APPROVED" ${historyStatus eq 'APPROVED' ? 'selected' : ''}>승인</option>
                    <option value="REJECTED" ${historyStatus eq 'REJECTED' ? 'selected' : ''}>반려</option>
                </select>
            </div>

            <div class="refund-search-field refund-date-field">
                <label for="historyStartDate">조회 기간</label>

                <%-- [SonarQube 접근성] role="group" 대신 의미가 명확한 fieldset을 사용합니다. --%>
                <fieldset class="refund-quick-range"
                          aria-label="조회 기간 빠른 선택">
                    <button type="button" class="refund-quick-btn" data-range="all">전체</button>
                    <button type="button" class="refund-quick-btn" data-range="7">1주일</button>
                    <button type="button" class="refund-quick-btn" data-range="30">1개월</button>
                    <button type="button" class="refund-quick-btn" data-range="90">3개월</button>
                    <button type="button" class="refund-quick-btn" data-range="180">6개월</button>
                </fieldset>

                <div class="refund-date-range">
                    <input id="historyStartDate" type="date" name="startDate" value="${param.startDate}">
                    <span>부터</span>
                    <input id="historyEndDate" type="date" name="endDate" value="${param.endDate}" aria-label="조회 종료일">
                    <span>까지</span>
                </div>
            </div>

            <div class="refund-search-actions">
                <button type="submit" class="history-search-btn">조회</button>
                <a class="history-reset-btn" href="${pageContext.request.contextPath}/order/list?tab=history">초기화</a>
            </div>
        </form>

        <c:choose>
            <c:when test="${empty cancelRefundHistory}">
                <div class="empty-box">조회된 취소/환불 내역이 없습니다.</div>
            </c:when>
            <c:otherwise>
                <div class="refund-table-wrap">
                    <table class="refund-table">
                        <thead>
                            <tr>
                                <th>구분</th>
                                <th>주문번호</th>
                                <th>상품명</th>
                                <th>신청일</th>
                                <th>금액</th>
                                <th>처리 상태</th>
                                <th>반려 사유</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="history" items="${cancelRefundHistory}">
                                <tr class="refund-row">
                                    <td data-label="구분">
                                        <span class="refund-type-badge ${history.historyType eq 'REFUND' ? 'refund' : 'cancel'}">
                                            ${history.historyType eq 'REFUND' ? '환불' : '취소'}
                                        </span>
                                    </td>
                                    <td data-label="주문번호">${history.orderNo}</td>
                                    <td data-label="상품명"><c:out value="${history.productName}"/></td>
                                    <td data-label="신청일">${dt:format(history.createdAt, 'yyyy.MM.dd')}</td>
                                    <td data-label="금액">₩ <fmt:formatNumber value="${history.refundAmount}" pattern="#,###"/></td>
                                    <td data-label="처리 상태">
                                        <c:choose>
                                            <c:when test="${history.status eq 'APPROVED'}"><span class="refund-badge completed">승인</span></c:when>
                                            <c:when test="${history.status eq 'REJECTED'}"><span class="refund-badge rejected">반려</span></c:when>
                                            <c:otherwise><span class="refund-badge waiting">승인 대기</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td data-label="반려 사유"><c:out value="${empty history.rejectReason ? '-' : history.rejectReason}"/></td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:otherwise>
        </c:choose>

        <c:if test="${historyPageVO.totalCount > 0}">
            <nav class="oditji-pagination"
                 data-pagination
                 data-current-page="${historyPageVO.currentPage}"
                 data-total-page="${historyPageVO.totalPage}"
                 data-page-param="historyPage"
                 data-fixed-param-name="tab"
                 data-fixed-param-value="history"
                 aria-label="취소 환불 내역 페이지"></nav>
        </c:if>
    </div>
    <!-- // 취소/환불 내역 탭 패널 -->

    <%--
        =========================================================
        [배송조회 모달 추가]

        기존에는 배송조회가 같은 페이지 안의 별도 탭(패널)으로
        전환되는 방식이었지만, 불필요하게 화면을 분리하고 있어
        주문 목록에서 "배송조회" 버튼을 누르면 모달로 바로
        보여주도록 리팩토링했다. 내용 구성(배송 단계 / 주문상품
        정보 / 배송 정보)은 기존과 동일하며, orderDeliveryModal.js가
        /order/delivery AJAX 응답으로 아래 요소들의 내용을 채워
        넣는다.
        =========================================================
    --%>
    <dialog id="deliveryDetailModal"
            class="delivery-detail-modal"
            aria-hidden="true"
            aria-modal="true"
            aria-labelledby="deliveryDetailModalTitle">

        <div class="delivery-detail-modal-content">

            <div class="delivery-detail-modal-header">

                <h2 id="deliveryDetailModalTitle">
                    배송 조회
                </h2>

                <button type="button"
                        id="deliveryDetailCloseBtn"
                        class="delivery-detail-modal-close"
                        aria-label="닫기">
                    &times;
                </button>

            </div>

            <!-- 배송 정보를 불러오는 중 -->
            <div id="deliveryDetailLoading"
                 class="empty-box"
                 style="display:none;">
                배송 정보를 불러오는 중입니다...
            </div>

            <!-- 배송 정보 조회 실패 -->
            <div id="deliveryDetailError"
                 class="empty-box"
                 style="display:none;">
            </div>

            <!-- 배송 정보 -->
            <div id="deliveryDetailContent"
                 style="display:none;">

                <div id="deliveryProgress"
                     class="delivery-progress"
                     data-status="CONFIRMED">

                    <div class="delivery-progress-labels">

                        <span class="delivery-progress-label"
                              data-step="CONFIRMED">
                            주문 확인중
                        </span>

                        <span class="delivery-progress-label"
                              data-step="PREPARING">
                            배송 준비 중
                        </span>

                        <span class="delivery-progress-label"
                              data-step="SHIPPING">
                            배송 중
                        </span>

                        <span class="delivery-progress-label"
                              data-step="DELIVERED">
                            배송 완료
                        </span>

                    </div>

                </div>

                <div class="delivery-product delivery-product-without-image">

                    <%-- [수정] 배송조회 화면에서는 상품 이미지를 표시하지 않는다. --%>
                    <div class="order-info-list delivery-product-info">

                        <div class="order-info-row">
                            <div class="order-info-label">상품명</div>
                            <div class="order-info-value" id="deliveryProductName"></div>
                        </div>

                        <div class="order-info-row">
                            <div class="order-info-label">수량</div>
                            <div class="order-info-value" id="deliveryQuantity"></div>
                        </div>

                        <div class="order-info-row">
                            <div class="order-info-label">결제 금액</div>
                            <div class="order-info-value">₩ <span id="deliveryItemTotalPrice"></span></div>
                        </div>

                    </div>

                </div>

                <div class="ticket-divider"
                     aria-hidden="true">
                </div>

                <div class="order-info-section">

                    <h2 class="order-info-title">배송 정보</h2>

                    <div class="order-info-list">

                        <div class="order-info-row">
                            <div class="order-info-label">받는 사람</div>
                            <div class="order-info-value" id="deliveryReceiverName"></div>
                        </div>

                        <div class="order-info-row">
                            <div class="order-info-label">연락처</div>
                            <div class="order-info-value" id="deliveryReceiverPhone"></div>
                        </div>

                        <div class="order-info-row">
                            <div class="order-info-label">배송지 주소</div>
                            <div class="order-info-value" id="deliveryAddress"></div>
                        </div>

                        <div class="order-info-row">
                            <div class="order-info-label">택배사</div>
                            <div class="order-info-value" id="deliveryCourier"></div>
                        </div>

                        <div class="order-info-row">

                            <div class="order-info-label">운송장 번호</div>

                            <div class="order-info-value"
                                 id="deliveryTrackingWrap">
                                <span class="delivery-empty-note">아직 운송장 번호가 등록되지 않았습니다.</span>
                            </div>

                        </div>

                        <div class="order-info-row">
                            <div class="order-info-label">주문일</div>
                            <div class="order-info-value" id="deliveryOrderCreatedAt"></div>
                        </div>

                    </div>

                </div>

            </div>

            <div class="delivery-detail-modal-footer">

                <button type="button"
                        id="deliveryDetailFooterCloseBtn"
                        class="delivery-detail-modal-footer-close">
                    닫기
                </button>

            </div>

        </div>

    </dialog>

    <!-- 리뷰 작성 모달 -->
    <div id="reviewModal"
         class="review-modal"
         style="display:none;">

        <div class="review-modal-content">

            <h2>상품 리뷰 작성</h2>

            <form action="${pageContext.request.contextPath}/review/writeProductReview"
                  method="post">

                <input type="hidden"
                       id="orderItemNo"
                       name="orderItemNo">

                <input type="hidden"
                       id="productNo"
                       name="productNo">

                <%--
                    [추가] 리뷰 작성 성공 또는 실패 후
                    사용자가 보고 있던 주문내역 페이지를 유지한다.
                --%>
                <input type="hidden"
                       name="page"
                       value="${pageVO.currentPage}">

                <div>

                    <label for="rating">
                        평점
                    </label>

                    <select id="rating"
                            name="rating">

                        <option value="5">
                            ★★★★★
                        </option>

                        <option value="4">
                            ★★★★☆
                        </option>

                        <option value="3">
                            ★★★☆☆
                        </option>

                        <option value="2">
                            ★★☆☆☆
                        </option>

                        <option value="1">
                            ★☆☆☆☆
                        </option>

                    </select>

                </div>

                <div>

                    <%--
                        상품 리뷰 내용 입력창에 고유 id를 부여하고
                        label의 for 속성과 연결한다.
                    --%>
                    <label for="productReviewContent"
                           class="review-accessibility-label">
                        상품 리뷰 내용
                    </label>

                    <textarea id="productReviewContent"
                              name="content"
                              rows="6"
                              placeholder="리뷰를 작성해주세요."></textarea>

                </div>

                <button type="submit">
                    등록
                </button>

                <button type="button"
                        id="closeReviewModal">
                    취소
                </button>

            </form>

        </div>

    </div>

    <!-- 취소/환불 요청 모달 -->
    <dialog id="orderCancelModal"
            class="payment-cancel-modal"
            open
            aria-hidden="true"
            aria-modal="true"
            aria-labelledby="orderCancelModalTitle">

        <div class="payment-cancel-modal-content">

            <h2 id="orderCancelModalTitle"
                class="payment-cancel-modal-title">
                취소/환불 요청
            </h2>

            <p id="orderCancelModalDescription"
               class="payment-cancel-modal-desc">
            </p>

            <label for="orderCancelReason"
                   class="payment-cancel-label">
                취소/환불 사유
            </label>

            <textarea id="orderCancelReason"
                      class="payment-cancel-reason"
                      maxlength="500"
                      placeholder="취소/환불 사유를 입력해주세요."></textarea>

            <p id="orderCancelError"
               class="payment-cancel-error"
               style="display:none;">
            </p>

            <div class="payment-cancel-modal-actions">

                <button type="button"
                        id="orderCancelCloseBtn"
                        class="payment-cancel-close-btn">
                    닫기
                </button>

                <button type="button"
                        id="orderCancelSubmitBtn"
                        class="payment-cancel-submit-btn">
                    요청하기
                </button>

            </div>

        </div>

    </dialog>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script src="${pageContext.request.contextPath}/js/orderReview.js"></script>
<script src="${pageContext.request.contextPath}/js/orderCancel.js"></script>
<script src="${pageContext.request.contextPath}/js/orderDeliveryModal.js"></script>
<script src="${pageContext.request.contextPath}/js/orderTabs.js"></script>
<script src="${pageContext.request.contextPath}/js/order.js"></script>

</body>

</html>