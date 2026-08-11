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
         data-image-base-url="https://image.tmdb.org/t/p/w92">

    <div class="sub-calc-hero">
        <p class="sub-calc-hero__eyebrow">ODITJI TOOL</p>
        <h1 class="sub-calc-hero__title">OTT 구독 조합 계산기</h1>
        <p class="sub-calc-hero__desc">
            보고 싶은 작품을 담아보세요. 그 작품들을 모두 볼 수 있는
            가장 저렴한 OTT 조합을 계산해 드려요.
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
