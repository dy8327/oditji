<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>OTT 구독 조합 계산기 - ODITJI</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/subscription-calculator.css">
    <script defer src="${pageContext.request.contextPath}/js/subscriptionCalculator.js"></script>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="main">
<section class="sub-calc-section"
         id="subCalcSection"
         data-search-url="${pageContext.request.contextPath}/api/subscription/search-content"
         data-calculate-url="${pageContext.request.contextPath}/api/subscription/calculate"
         data-save-url="${pageContext.request.contextPath}/api/subscription/save"
         data-result-base-url="${pageContext.request.contextPath}/subscription/result/"
         data-image-base-url="https://image.tmdb.org/t/p/w92">

    <div class="sub-calc-hero">
        <p class="sub-calc-hero__eyebrow">ODITJI TOOL</p>
        <h1 class="sub-calc-hero__title">OTT 구독 조합 계산기</h1>
        <p class="sub-calc-hero__desc">
            보고 싶은 작품을 담고, 나의 할인 조건을 체크해보세요.
            실제 이용 가능한 최저가 구독 조합과 절감 금액을 계산해 드립니다.
        </p>
    </div>

    <div class="sub-calc-filter-panel" id="subCalcFilterPanel">

        <p class="sub-calc-filter-panel__title">내 할인 조건 선택 (필터)</p>

        <div class="sub-calc-filter-row">
            <span class="sub-calc-filter-row__label">통신사</span>
            <div class="sub-calc-filter-radio-group" id="subCalcTelecomGroup">
                <label class="sub-calc-filter-radio">
                    <input type="radio" name="subCalcTelecom" value="" checked>
                    <span>전체</span>
                </label>
                <c:forEach var="telecom" items="${telecomList}">
                    <label class="sub-calc-filter-radio">
                        <input type="radio" name="subCalcTelecom" value="${telecom}">
                        <span>${telecom}</span>
                    </label>
                </c:forEach>
            </div>
        </div>

        <div class="sub-calc-filter-row">
            <label class="sub-calc-filter-row__label" for="subCalcCardSelect">카드사</label>
            <select class="sub-calc-filter-select" id="subCalcCardSelect">
                <option value="">선택 안 함</option>
                <c:forEach var="card" items="${cardList}">
                    <option value="${card}">${card}</option>
                </c:forEach>
            </select>
        </div>

        <div class="sub-calc-filter-row">
            <label class="sub-calc-filter-row__label" for="subCalcMembershipSelect">멤버십</label>
            <select class="sub-calc-filter-select" id="subCalcMembershipSelect">
                <option value="">선택 안 함</option>
                <c:forEach var="membership" items="${membershipList}">
                    <option value="${membership}">${membership}</option>
                </c:forEach>
            </select>
        </div>

        <p class="sub-calc-filter-panel__hint">
            체크한 조건에 해당하는 할인만 반영해서 계산해요. 아무 조건도 선택하지 않으면 정가로 계산돼요.
        </p>

    </div>

    <div class="sub-calc-layout">

        <div class="sub-calc-search-panel">

            <label for="subCalcSearchInput" class="sub-calc-search-label">
                보고 싶은 작품 검색
            </label>

            <div class="sub-calc-search-box">
                <input type="text"
                       id="subCalcSearchInput"
                       class="sub-calc-search-input"
                       placeholder="작품 제목을 입력하세요"
                       autocomplete="off">

                <div class="sub-calc-search-results"
                     id="subCalcSearchResults"
                     hidden></div>
            </div>

            <h2 class="sub-calc-wishlist-title">
                담은 작품
                <span class="sub-calc-wishlist-count" id="subCalcWishlistCount">0</span>
            </h2>

            <ul class="sub-calc-wishlist" id="subCalcWishlist">
                <li class="sub-calc-wishlist__empty" id="subCalcWishlistEmpty">
                    아직 담은 작품이 없어요. 검색해서 추가해보세요.
                </li>
            </ul>

            <button type="button"
                    class="sub-calc-calculate-btn"
                    id="subCalcCalculateBtn"
                    disabled>
                최저가 조합 계산하기
            </button>

        </div>

        <div class="sub-calc-result-panel"
             id="subCalcResultPanel">

            <p class="sub-calc-result-placeholder" id="subCalcResultPlaceholder">
                작품을 담고 계산하기를 누르면 결과가 여기에 표시돼요.
            </p>

        </div>

    </div>

</section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
