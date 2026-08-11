<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>OTT 할인 혜택 - ODITJI</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/ott-discount.css">
    <%-- [수정] 8개 단위 페이징 추가: 다른 목록 화면(eventList.jsp 등)과 동일한 공용
         페이지네이션 위젯을 재사용한다. ottDiscount.js보다 먼저 로드되어야
         renderPaginationNav()를 AJAX 갱신 시에도 재사용할 수 있다. --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pagination-common.css?v=1">
    <script defer src="${pageContext.request.contextPath}/js/pagination.js?v=1"></script>
    <script defer src="${pageContext.request.contextPath}/js/ottDiscount.js"></script>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="main">
<section class="ott-discount-section"
         id="ottDiscountSection"
         data-api-url="${pageContext.request.contextPath}/api/discount"
         data-page-url="${pageContext.request.contextPath}/discount/ott"
         data-selected-platform="${selectedPlatform}"
         data-selected-category="${selectedCategory}">

    <%-- [수정] 상단 이달의 대표 혜택 배너: BEST 뱃지가 붙은 항목, 없으면 최신 항목을 노출.
         목록이 8개 단위로 페이징되면서 discountList를 직접 훑던 기존 방식은 2페이지
         이후에 있는 BEST 항목을 놓치므로, 컨트롤러에서 페이징과 무관하게 별도 조회한
         heroItem을 그대로 쓴다.
         [수정] UI/UX 강화: 기존에는 "플랫폼 · 제목 — 요약"을 한 줄 텍스트로만 보여줘
         그냥 DB에서 꺼낸 항목 하나를 밋밋하게 나열하는 느낌이었다. 태그(+아이콘)/뱃지/
         가격 비교/CTA 버튼을 갖춘 카드 형태로 바꿔 "이달의 대표 혜택"다운 임팩트를 준다. --%>
    <div class="ott-discount-hero">
        <p class="ott-discount-hero__eyebrow">ODITJI PICK</p>
        <h1 class="ott-discount-hero__title">이달의 OTT 할인 혜택</h1>
        <%-- [수정] 히어로 배너 실시간 갱신: 플랫폼/혜택 종류 탭을 클릭해도 새로고침
             전까지 첫 진입 값이 그대로 남아있던 문제를 고친다. 동적으로 바뀌는
             부분만 id="ottDiscountHeroBody"로 감싸, 필터를 바꿀 때 ottDiscount.js가
             /api/discount 응답의 heroItem으로 이 영역만 다시 그리고 부드럽게
             페이드 전환한다(전체 페이지 새로고침 불필요). --%>
        <div class="ott-discount-hero__body" id="ottDiscountHeroBody">
            <c:choose>
                <c:when test="${not empty heroItem}">
                    <div class="ott-discount-hero__card">
                        <div class="ott-discount-hero__card-top">
                            <span class="ott-platform-tag ott-platform-tag--lg ott-platform-tag--${heroItem.platformCode}">
                                <c:set var="heroLogoUrl" value="${ottLogoMap[fn:toLowerCase(heroItem.platformCode)]}"/>
                                <c:choose>
                                    <c:when test="${not empty heroLogoUrl}">
                                        <img class="ott-platform-tag__logo" src="${heroLogoUrl}" alt="" aria-hidden="true">
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ott-platform-tag__icon" aria-hidden="true">${fn:substring(heroItem.platformName, 0, 1)}</span>
                                    </c:otherwise>
                                </c:choose>
                                ${heroItem.platformName}
                            </span>
                            <c:if test="${not empty heroItem.badgeText}">
                                <span class="ott-badge ott-badge--hero">${heroItem.badgeText}</span>
                            </c:if>
                        </div>

                        <p class="ott-discount-hero__card-title">${heroItem.title}</p>
                        <p class="ott-discount-hero__card-summary">${heroItem.discountSummary}</p>

                        <c:if test="${not empty heroItem.regularPrice and not empty heroItem.discountPrice}">
                            <div class="ott-price-row ott-discount-hero__price-row">
                                <span class="ott-price-regular"><fmt:formatNumber value="${heroItem.regularPrice}" pattern="#,##0"/>원</span>
                                <span class="ott-price-arrow">→</span>
                                <span class="ott-price-discount"><fmt:formatNumber value="${heroItem.discountPrice}" pattern="#,##0"/>원</span>
                                <c:if test="${not empty heroItem.discountRate}">
                                    <span class="ott-discount-rate">${heroItem.discountRate}% 할인</span>
                                </c:if>
                            </div>
                        </c:if>

                        <c:if test="${not empty heroItem.targetUrl}">
                            <a href="${heroItem.targetUrl}" target="_blank" rel="noopener" class="ott-discount-hero__cta">혜택 보러가기</a>
                        </c:if>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="ott-discount-hero__desc">카드사·통신사·멤버십 혜택을 한 곳에서 비교해 보세요.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- OTT 플랫폼 선택 탭 --%>
    <div class="ott-filter-section">
        <p class="ott-filter-label">OTT 플랫폼</p>
        <div class="ott-filter-group" id="platformFilterGroup" data-filter-type="platform">
            <button type="button" class="ott-filter-btn ${selectedPlatform == 'ALL' ? 'active' : ''}" data-value="ALL">전체</button>
            <button type="button" class="ott-filter-btn ${selectedPlatform == 'NETFLIX' ? 'active' : ''}" data-value="NETFLIX">넷플릭스</button>
            <button type="button" class="ott-filter-btn ${selectedPlatform == 'TVING' ? 'active' : ''}" data-value="TVING">티빙</button>
            <button type="button" class="ott-filter-btn ${selectedPlatform == 'WAVVE' ? 'active' : ''}" data-value="WAVVE">웨이브</button>
            <button type="button" class="ott-filter-btn ${selectedPlatform == 'DISNEY' ? 'active' : ''}" data-value="DISNEY">디즈니+</button>
            <button type="button" class="ott-filter-btn ${selectedPlatform == 'WATCHA' ? 'active' : ''}" data-value="WATCHA">왓챠</button>
            <button type="button" class="ott-filter-btn ${selectedPlatform == 'COUPANG' ? 'active' : ''}" data-value="COUPANG">쿠팡플레이</button>
        </div>

        <%-- 할인 종류 칩 버튼 --%>
        <p class="ott-filter-label">혜택 종류</p>
        <div class="ott-filter-group ott-filter-group--chip" id="categoryFilterGroup" data-filter-type="category">
            <button type="button" class="ott-filter-btn ott-filter-btn--chip ${selectedCategory == 'ALL' ? 'active' : ''}" data-value="ALL">전체 혜택</button>
            <button type="button" class="ott-filter-btn ott-filter-btn--chip ${selectedCategory == 'CARD' ? 'active' : ''}" data-value="CARD">카드사</button>
            <button type="button" class="ott-filter-btn ott-filter-btn--chip ${selectedCategory == 'TELECOM' ? 'active' : ''}" data-value="TELECOM">통신사</button>
            <button type="button" class="ott-filter-btn ott-filter-btn--chip ${selectedCategory == 'MEMBERSHIP' ? 'active' : ''}" data-value="MEMBERSHIP">멤버십/포인트</button>
        </div>
    </div>

    <%-- 카드 리스트 영역 : 최초 진입은 서버 사이드 렌더링, 이후 필터 변경은 ottDiscount.js가 /api/discount로 갱신 --%>
    <div class="ott-discount-grid" id="discountCardGrid">
        <c:choose>
            <c:when test="${empty discountList}">
                <p class="ott-discount-empty">조건에 맞는 할인 혜택이 없습니다.</p>
            </c:when>
            <c:otherwise>
                <c:forEach var="item" items="${discountList}">
                    <div class="ott-discount-card" data-id="${item.discountId}">
                        <div class="ott-discount-card__top">
                            <span class="ott-platform-tag ott-platform-tag--${item.platformCode}">
                                <c:set var="cardLogoUrl" value="${ottLogoMap[fn:toLowerCase(item.platformCode)]}"/>
                                <c:choose>
                                    <c:when test="${not empty cardLogoUrl}">
                                        <img class="ott-platform-tag__logo" src="${cardLogoUrl}" alt="" aria-hidden="true">
                                    </c:when>
                                    <c:otherwise>
                                        <span class="ott-platform-tag__icon" aria-hidden="true">${fn:substring(item.platformName, 0, 1)}</span>
                                    </c:otherwise>
                                </c:choose>
                                ${item.platformName}
                            </span>
                            <c:if test="${not empty item.badgeText}">
                                <span class="ott-badge">${item.badgeText}</span>
                            </c:if>
                        </div>

                        <h3 class="ott-discount-card__title">${item.title}</h3>
                        <p class="ott-discount-card__summary">${item.discountSummary}</p>

                        <c:if test="${not empty item.regularPrice and not empty item.discountPrice}">
                            <div class="ott-price-row">
                                <span class="ott-price-regular"><fmt:formatNumber value="${item.regularPrice}" pattern="#,##0"/>원</span>
                                <span class="ott-price-arrow">→</span>
                                <span class="ott-price-discount"><fmt:formatNumber value="${item.discountPrice}" pattern="#,##0"/>원</span>
                                <c:if test="${not empty item.discountRate}">
                                    <span class="ott-discount-rate">${item.discountRate}% 할인</span>
                                </c:if>
                            </div>
                        </c:if>

                        <c:if test="${not empty item.description}">
                            <p class="ott-discount-card__desc">${item.description}</p>
                        </c:if>

                        <div class="ott-discount-card__bottom">
                            <span class="ott-discount-card__company">${item.cardOrCompany}</span>
                            <span class="ott-discount-card__date">
                                <c:choose>
                                    <c:when test="${not empty item.startDate}">${fn:substring(item.startDate, 0, 10)}</c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                                ~
                                <c:choose>
                                    <c:when test="${not empty item.endDate}">${fn:substring(item.endDate, 0, 10)}</c:when>
                                    <c:otherwise>-</c:otherwise>
                                </c:choose>
                            </span>
                        </div>

                        <div class="ott-discount-card__actions">
                            <button type="button"
                                    class="ott-discount-card__calc-btn"
                                    data-discount-id="${item.discountId}"
                                    data-title="${item.title}"
                                    data-platform-name="${item.platformName}"
                                    data-regular-price="${item.regularPrice}"
                                    data-discount-rate="${item.discountRate}">
                                계산하기
                            </button>
                            <c:if test="${not empty item.targetUrl}">
                                <a href="${item.targetUrl}" target="_blank" rel="noopener" class="ott-discount-card__link">혜택 자세히 보기</a>
                            </c:if>
                        </div>
                    </div>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </div>

    <%-- [수정] 8개 단위 페이징 내비게이션. totalPage가 1 이하면 pagination.js가
         자동으로 숨긴다(nav.hidden=true). 필터를 AJAX로 바꾸면 ottDiscount.js가
         data-current-page/data-total-page를 새 결과 기준으로 갱신하고
         renderPaginationNav()를 다시 호출해 같은 위젯을 재사용한다. --%>
    <nav class="oditji-pagination"
        id="ottDiscountPagination"
        data-pagination
        data-current-page="${pageVO.currentPage}"
        data-total-page="${pageVO.totalPage}"
        data-page-param="page"
        aria-label="OTT 할인 정보 페이지 이동">
    </nav>

    <%-- [수정] AJAX로 카드를 다시 그릴 때도 서버 렌더링과 동일한 OTT_PLATFORM 로고 이미지를
         쓰기 위해 ottLogoMap을 JSON으로 내려준다. 플랫폼 목록 자체는 필터/페이지와 무관하게
         항상 동일하므로 /api/discount 응답에 매번 실어 보낼 필요 없이 최초 1회만 내려준다. --%>
    <script type="application/json" id="ottPlatformLogoData">{<c:forEach var="logoEntry" items="${ottLogoMap}" varStatus="logoStatus">"${logoEntry.key}":"${fn:escapeXml(logoEntry.value)}"<c:if test="${not logoStatus.last}">,</c:if></c:forEach>}</script>
</section>
</main>

<%-- OTT 할인 계산기 모달 (서버 요청 없이 JavaScript에서 즉시 계산) --%>
<div class="ott-calc-modal" id="ottCalcModal" aria-hidden="true">
    <div class="ott-calc-modal__dim" data-close-modal></div>
    <div class="ott-calc-modal__panel" role="dialog" aria-modal="true" aria-labelledby="ottCalcModalTitle">
        <button type="button" class="ott-calc-modal__close" data-close-modal aria-label="닫기">×</button>
        <h2 class="ott-calc-modal__title" id="ottCalcModalTitle">OTT 할인 계산기</h2>
        <p class="ott-calc-modal__subtitle" id="ottCalcModalSubtitle"></p>

        <div class="ott-calc-modal__field">
            <label for="ottCalcRegularPrice">원래 가격</label>
            <input type="number" id="ottCalcRegularPrice" min="0" step="1" inputmode="numeric" placeholder="예: 17000">
        </div>

        <div class="ott-calc-modal__field">
            <label for="ottCalcRate">할인율(%)</label>
            <input type="number" id="ottCalcRate" min="0" max="100" step="1" inputmode="numeric" placeholder="예: 20">
        </div>

        <div class="ott-calc-modal__field">
            <label for="ottCalcMonths">이용 개월 수</label>
            <select id="ottCalcMonths">
                <option value="1" selected>1개월</option>
                <option value="3">3개월</option>
                <option value="6">6개월</option>
                <option value="12">12개월</option>
            </select>
        </div>

        <div class="ott-calc-modal__result-label">실시간 계산 결과</div>
        <div class="ott-calc-modal__result" id="ottCalcResult">
            <p class="ott-calc-modal__placeholder">원래 가격과 할인율을 입력하면 바로 계산해 드려요.</p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
