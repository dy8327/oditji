<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    필터(플랫폼/카테고리/상태)의 현재 선택값.
    param이 없으면 컨트롤러 기본값(ALL)과 동일하게 취급한다.
--%>
<c:set var="currentPlatform" value="${empty param.platform ? 'ALL' : param.platform}"/>
<c:set var="currentCategory" value="${empty param.category ? 'ALL' : param.category}"/>
<c:set var="currentStatus" value="${empty param.status ? 'ALL' : param.status}"/>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">
<title>ODITJI | OTT 할인 관리</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">

<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
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

            <h1 class="admin-page-title">
                OTT 할인 관리
            </h1>

            <p class="admin-page-desc">
                OTT 구독료 할인 정보(카드사·통신사·멤버십 혜택)를 직접 등록·수정·비활성화합니다.
                비활성화한 항목은 사용자 화면(/discount/ott)에서 바로 노출이 중단되며, 언제든 다시 활성화할 수 있습니다.
            </p>

        </div>

        <%-- 등록/수정/비활성화/재활성화 처리 결과 안내 (RedirectAttributes flash message) --%>
        <c:if test="${not empty message}">
            <div class="admin-flash-message">${message}</div>
        </c:if>

        <section class="admin-content-box">

            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}/admin/discount/list">

                    <label for="discountPlatformFilter" class="sr-only">
                        플랫폼 필터
                    </label>

                    <select id="discountPlatformFilter" name="platform" class="filter-select">
                        <option value="ALL"     ${currentPlatform == 'ALL' ? 'selected' : ''}>플랫폼 전체</option>
                        <option value="NETFLIX" ${currentPlatform == 'NETFLIX' ? 'selected' : ''}>넷플릭스</option>
                        <option value="TVING"   ${currentPlatform == 'TVING' ? 'selected' : ''}>티빙</option>
                        <option value="WAVVE"   ${currentPlatform == 'WAVVE' ? 'selected' : ''}>웨이브</option>
                        <option value="DISNEY"  ${currentPlatform == 'DISNEY' ? 'selected' : ''}>디즈니+</option>
                        <option value="WATCHA"  ${currentPlatform == 'WATCHA' ? 'selected' : ''}>왓챠</option>
                        <option value="COUPANG" ${currentPlatform == 'COUPANG' ? 'selected' : ''}>쿠팡플레이</option>
                    </select>

                    <label for="discountCategoryFilter" class="sr-only">
                        카테고리 필터
                    </label>

                    <select id="discountCategoryFilter" name="category" class="filter-select">
                        <option value="ALL"        ${currentCategory == 'ALL' ? 'selected' : ''}>혜택 유형 전체</option>
                        <option value="CARD"       ${currentCategory == 'CARD' ? 'selected' : ''}>카드사 할인</option>
                        <option value="TELECOM"    ${currentCategory == 'TELECOM' ? 'selected' : ''}>통신사 제휴</option>
                        <option value="MEMBERSHIP" ${currentCategory == 'MEMBERSHIP' ? 'selected' : ''}>멤버십/포인트</option>
                    </select>

                    <label for="discountStatusFilter" class="sr-only">
                        상태 필터
                    </label>

                    <select id="discountStatusFilter" name="status" class="filter-select">
                        <option value="ALL" ${currentStatus == 'ALL' ? 'selected' : ''}>상태 전체</option>
                        <option value="Y"   ${currentStatus == 'Y' ? 'selected' : ''}>활성</option>
                        <option value="N"   ${currentStatus == 'N' ? 'selected' : ''}>비활성</option>
                    </select>

                    <button type="submit" class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

                <button type="button" class="btn btn-primary"
                        onclick="openDiscountRegisterModal()">
                    + 할인 정보 등록
                </button>

            </div>

            <table class="data-table">

                <thead>

                    <tr>
                        <th class="col-mobile-hide">번호</th>
                        <th>플랫폼</th>
                        <th class="col-mobile-hide">혜택 유형</th>
                        <th>제목</th>
                        <th class="col-mobile-hide">정가 / 할인가</th>
                        <th class="col-mobile-hide">핵심 혜택</th>
                        <th class="col-mobile-hide">기간</th>
                        <%-- [수정] 모바일에서는 상태 컬럼을 숨기고 그 정보를 제목 글자색으로 대신
                             표현한다(memberManage.jsp의 id-status-* 패턴과 동일). 데스크톱에서는
                             col-mobile-hide가 적용되지 않아 이 컬럼이 그대로 보인다. --%>
                        <th class="col-mobile-hide">상태</th>
                        <th>관리</th>
                    </tr>

                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty discountList}">

                            <c:forEach var="item" items="${discountList}">

                                <tr>

                                    <td class="col-mobile-hide">
                                        ${item.discountId}
                                    </td>

                                    <td>
                                        ${item.platformName}
                                    </td>

                                    <td class="col-mobile-hide">
                                        <c:choose>
                                            <c:when test="${item.category == 'CARD'}">카드사</c:when>
                                            <c:when test="${item.category == 'TELECOM'}">통신사</c:when>
                                            <c:when test="${item.category == 'MEMBERSHIP'}">멤버십/포인트</c:when>
                                            <c:otherwise>${item.category}</c:otherwise>
                                        </c:choose>
                                    </td>

                                    <%-- [수정] 모바일에서 숨겨지는 "상태" 컬럼 정보를 제목 글자색으로 대신
                                         표현하기 위한 클래스. 데스크톱에서는 .id-status-*에 아무 스타일도
                                         없어(768px 이하 미디어쿼리에서만 색이 붙는다) 기존과 동일하게 보인다. --%>
                                    <c:set var="discountTitleStatusClass"
                                           value="${item.isActive == 'Y' ? 'id-status-active' : 'id-status-blocked'}"/>

                                    <td class="${discountTitleStatusClass}">
                                        ${item.title}
                                    </td>

                                    <%-- 정가 및 할인가 표시 컬럼 추가 --%>
                                    <td class="col-mobile-hide">
                                        <c:choose>
                                            <c:when test="${not empty item.regularPrice and not empty item.discountPrice}">
                                                <del style="color: #888;"><fmt:formatNumber value="${item.regularPrice}" pattern="#,##0"/>원</del>
                                                → <strong><fmt:formatNumber value="${item.discountPrice}" pattern="#,##0"/>원</strong>
                                            </c:when>
                                            <c:when test="${not empty item.regularPrice}">
                                                <fmt:formatNumber value="${item.regularPrice}" pattern="#,##0"/>원
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td class="col-mobile-hide">
                                        ${item.discountSummary}
                                    </td>

                                    <td class="col-mobile-hide">
                                        <c:choose>
                                            <c:when test="${not empty item.startDate}">
                                                ${fn:substring(item.startDate, 0, 10)}
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                        ~
                                        <c:choose>
                                            <c:when test="${not empty item.endDate}">
                                                ${fn:substring(item.endDate, 0, 10)}
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td class="col-mobile-hide">
                                        <c:choose>
                                            <c:when test="${item.isActive == 'Y'}">
                                                <span class="status-ok">활성</span>
                                            </c:when>
                                            <%-- [수정] 비활성 뱃지를 중립색(status-end)에서 위험색(status-reject,
                                                 빨간색)으로 변경. status-end는 eventManage.jsp의 "종료"처럼
                                                 반려와 의미가 다른 화면에서 쓰는 색이라 그대로 두고, 여기서는
                                                 이미 빨간색인 status-reject를 재사용한다. --%>
                                            <c:otherwise>
                                                <span class="status-reject">비활성</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>

                                        <div class="item-actions">

                                            <%--
                                                [수정] 이 "수정" 버튼은 다른 화면의 row-detail-trigger와 달리
                                                단순 상세보기가 아니라 유일한 수정 진입점이다(모바일에서만
                                                보이면 데스크톱에서는 수정할 방법이 없어진다). row-detail-trigger
                                                클래스는 openRowDetailModal의 데이터 채움 방식을 그대로 쓰기 위해
                                                유지하되, discount-edit-trigger를 추가해 admin.css에서 데스크톱
                                                기본 숨김(.data-table .row-detail-trigger{display:none})을
                                                덮어쓰고 항상 노출되도록 한다.
                                            --%>
                                            <button type="button" class="btn btn-outline row-detail-trigger discount-edit-trigger"
                                                    data-discount-id="${item.discountId}"
                                                    data-platform-code="${item.platformCode}"
                                                    data-category="${item.category}"
                                                    data-title="${item.title}"
                                                    data-regular-price="${item.regularPrice}"
                                                    data-discount-price="${item.discountPrice}"
                                                    data-discount-summary="${item.discountSummary}"
                                                    data-description="${item.description}"
                                                    data-card-or-company="${item.cardOrCompany}"
                                                    data-target-url="${item.targetUrl}"
                                                    data-badge-text="${item.badgeText}"
                                                    data-start-date="${item.startDate}"
                                                    data-end-date="${item.endDate}"
                                                    data-is-active="${item.isActive}"
                                                    onclick="openDiscountEditModal(this)">
                                                수정
                                            </button>

                                            <c:choose>

                                                <c:when test="${item.isActive == 'Y'}">
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/admin/discount/deactivate">
                                                        <input type="hidden" name="discountId" value="${item.discountId}">
                                                        <input type="hidden" name="filterPlatform" value="${currentPlatform}">
                                                        <input type="hidden" name="filterCategory" value="${currentCategory}">
                                                        <input type="hidden" name="filterStatus" value="${currentStatus}">
                                                        <input type="hidden" name="page" value="${pagination.currentPage}">
                                                        <button type="submit" class="btn btn-danger">
                                                            비활성화
                                                        </button>
                                                    </form>
                                                </c:when>

                                                <c:otherwise>
                                                    <form method="post"
                                                          action="${pageContext.request.contextPath}/admin/discount/activate">
                                                        <input type="hidden" name="discountId" value="${item.discountId}">
                                                        <input type="hidden" name="filterPlatform" value="${currentPlatform}">
                                                        <input type="hidden" name="filterCategory" value="${currentCategory}">
                                                        <input type="hidden" name="filterStatus" value="${currentStatus}">
                                                        <input type="hidden" name="page" value="${pagination.currentPage}">
                                                        <button type="submit" class="btn btn-success">
                                                            재활성화
                                                        </button>
                                                    </form>
                                                </c:otherwise>

                                            </c:choose>

                                        </div>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>
                                <td colspan="9" class="empty">
                                    조건에 맞는 할인 정보가 없습니다.
                                </td>
                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <%-- 페이징 nav. 다른 관리자 화면(memberManage.jsp 등)과 동일한 마크업/클래스를 그대로 쓰고,
                 이 화면의 필터(platform/category/status) 3종만 쿼리스트링에 함께 실어 나른다. --%>
            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?platform=${currentPlatform}&category=${currentCategory}&status=${currentStatus}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>

                <!-- 이전 페이지 -->
                <a href="?platform=${currentPlatform}&category=${currentCategory}&status=${currentStatus}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>

                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?platform=${currentPlatform}&category=${currentCategory}&status=${currentStatus}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>

                <!-- 다음 페이지 -->
                <a href="?platform=${currentPlatform}&category=${currentCategory}&status=${currentStatus}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>

                <!-- 다음 블록 -->
                <a href="?platform=${currentPlatform}&category=${currentCategory}&status=${currentStatus}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>

        </section>
    </main>
</div>

<%--
    OTT 할인 등록/수정 공용 모달.
    등록·수정 모두 이 하나의 모달과 폼을 재사용한다(admin.js의
    openDiscountRegisterModal()/openDiscountEditModal() 참고).
    form action은 두 함수가 data-register-url/data-update-url 값으로 바꿔치기한다.
--%>
<dialog class="modal-overlay" id="discountFormModal" open aria-modal="true"
        aria-labelledby="discountFormTitle">

    <div class="modal-box">

        <div class="modal-header">

            <h3 id="discountFormTitle">
                OTT 할인 등록
            </h3>

            <button type="button" class="modal-close" aria-label="OTT 할인 등록·수정 팝업 닫기"
                    onclick="closeModal('discountFormModal')">
                &times;
            </button>

        </div>

        <form id="discountForm" method="post"
              action="${pageContext.request.contextPath}/admin/discount/register"
              data-register-url="${pageContext.request.contextPath}/admin/discount/register"
              data-update-url="${pageContext.request.contextPath}/admin/discount/update"
              onsubmit="return validateDiscountForm()">

            <%-- 수정 모드에서만 채워지는 대상 식별자. 등록 모드에서는 빈 값으로 전송된다. --%>
            <input type="hidden" name="discountId" data-detail-field="discountId">

            <%-- 목록 화면으로 돌아갈 때 방금 보고 있던 필터와 페이지를 그대로 유지하기 위한 값 --%>
            <input type="hidden" name="filterPlatform" value="${currentPlatform}">
            <input type="hidden" name="filterCategory" value="${currentCategory}">
            <input type="hidden" name="filterStatus" value="${currentStatus}">
            <input type="hidden" name="page" value="${pagination.currentPage}">

            <div class="form-group">
                <label class="form-label" for="discountPlatformCode">OTT 플랫폼</label>
                <select id="discountPlatformCode" name="platformCode" class="form-select" required
                        data-detail-field="platformCode">
                    <option value="NETFLIX">넷플릭스</option>
                    <option value="TVING">티빙</option>
                    <option value="WAVVE">웨이브</option>
                    <option value="DISNEY">디즈니+</option>
                    <option value="WATCHA">왓챠</option>
                    <option value="COUPANG">쿠팡플레이</option>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label" for="discountCategory">혜택 유형</label>
                <select id="discountCategory" name="category" class="form-select" required
                        data-detail-field="category">
                    <option value="CARD">카드사 할인</option>
                    <option value="TELECOM">통신사 제휴</option>
                    <option value="MEMBERSHIP">멤버십/포인트</option>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label" for="discountTitle">할인 혜택 제목</label>
                <input type="text" id="discountTitle" name="title" class="form-input" required
                       maxlength="200" placeholder="예: KB국민 톡톡O 카드"
                       data-detail-field="title">
            </div>

            <%-- 정가 / 할인가 입력 필드 (한 행 배치) --%>
            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="discountRegularPrice">정가 (원)</label>
                    <input type="number" id="discountRegularPrice" name="regularPrice" class="form-input"
                           min="0" step="1" placeholder="예: 17000"
                           data-detail-field="regularPrice">
                </div>

                <div class="form-group">
                    <label class="form-label" for="discountDiscountPrice">할인가 (원)</label>
                    <input type="number" id="discountDiscountPrice" name="discountPrice" class="form-input"
                           min="0" step="1" placeholder="예: 8500"
                           data-detail-field="discountPrice">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="discountSummary">핵심 혜택 요약</label>
                <input type="text" id="discountSummary" name="discountSummary" class="form-input" required
                       maxlength="200" placeholder="예: 구독료 50% 청구할인"
                       data-detail-field="discountSummary">
            </div>

            <div class="form-group">
                <label class="form-label" for="discountDescription">상세 설명</label>
                <textarea id="discountDescription" name="description" class="form-textarea"
                          maxlength="1000" placeholder="적용 조건, 실적 기준 등 상세 내용"
                          data-detail-field="description"></textarea>
            </div>

            <div class="form-group">
                <label class="form-label" for="discountCardOrCompany">카드사/통신사명</label>
                <input type="text" id="discountCardOrCompany" name="cardOrCompany" class="form-input"
                       maxlength="100" placeholder="예: KB국민카드, SKT"
                       data-detail-field="cardOrCompany">
            </div>

            <div class="form-group">
                <label class="form-label" for="discountTargetUrl">바로가기 URL</label>
                <input type="url" id="discountTargetUrl" name="targetUrl" class="form-input"
                       maxlength="500" placeholder="https://"
                       data-detail-field="targetUrl">
            </div>

            <div class="form-group">
                <label class="form-label" for="discountBadgeText">뱃지 문구</label>
                <input type="text" id="discountBadgeText" name="badgeText" class="form-input"
                       maxlength="30" placeholder="예: BEST, 인기, 단독, NEW"
                       data-detail-field="badgeText">
            </div>

            <div class="form-row">

                <div class="form-group">
                    <label class="form-label" for="discountStartDate">시작일</label>
                    <input type="date" id="discountStartDate" name="startDate" class="form-input"
                           data-detail-field="startDate">
                </div>

                <div class="form-group">
                    <label class="form-label" for="discountEndDate">종료일</label>
                    <input type="date" id="discountEndDate" name="endDate" class="form-input"
                           data-detail-field="endDate">
                </div>

            </div>

            <div class="modal-footer">

                <button type="submit" class="btn btn-primary">
                    저장
                </button>
                <button type="button" class="btn btn-outline" onclick="closeModal('discountFormModal')">
                    취소
                </button>

            </div>

        </form>

        <%--
            모바일 전용 활성화/비활성화 진입점.
            768px 이하에서는 표의 .item-actions 안 비활성화/활성화 폼이 CSS로 숨겨지고
            .row-detail-trigger("수정")만 남아 이 모달로 들어오므로, 같은 동작을 모달
            안에도 둔다. discountId는 위 "수정" 폼과 마찬가지로 data-detail-field로
            채워지고, data-detail-toggle="isActive:Y|N"로 현재 상태에 맞는 버튼만 보인다.
            등록 모드(신규 등록)에서는 대상 discountId가 없으므로 admin.js의
            openDiscountRegisterModal()이 이 영역 전체를 숨긴다.
        --%>
        <div class="modal-footer" id="discountStatusActions">

            <form method="post"
                  action="${pageContext.request.contextPath}/admin/discount/deactivate"
                  data-detail-toggle="isActive:Y">
                <input type="hidden" name="discountId" data-detail-field="discountId">
                <input type="hidden" name="filterPlatform" value="${currentPlatform}">
                <input type="hidden" name="filterCategory" value="${currentCategory}">
                <input type="hidden" name="filterStatus" value="${currentStatus}">
                <input type="hidden" name="page" value="${pagination.currentPage}">
                <button type="submit" class="btn btn-danger">
                    비활성화
                </button>
            </form>

            <form method="post"
                  action="${pageContext.request.contextPath}/admin/discount/activate"
                  data-detail-toggle="isActive:N">
                <input type="hidden" name="discountId" data-detail-field="discountId">
                <input type="hidden" name="filterPlatform" value="${currentPlatform}">
                <input type="hidden" name="filterCategory" value="${currentCategory}">
                <input type="hidden" name="filterStatus" value="${currentStatus}">
                <input type="hidden" name="page" value="${pagination.currentPage}">
                <button type="submit" class="btn btn-success">
                    재활성화
                </button>
            </form>

        </div>

    </div>

</dialog>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>