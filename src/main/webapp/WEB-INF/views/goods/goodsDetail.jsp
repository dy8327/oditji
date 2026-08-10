<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>ODITJI | 상품 상세</title>

<%-- [수정] goods.css 변경 내용이 브라우저 캐시에 막히지 않도록 버전값 추가 --%>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/goods.css?v=20260810-2">
<link rel="stylesheet"
    href="${pageContext.request.contextPath}/css/component.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/favorite.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/report.css">

<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/goods.js">
</script>

<script defer
        src="${pageContext.request.contextPath}/js/report.js">
</script>

<script defer
        src="${pageContext.request.contextPath}/js/favorite.js">
</script>

</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="goods-detail-container">

<div class="back-area">

    <a href="${pageContext.request.contextPath}/goods/list"
       class="back-btn">
        ← 목록으로
    </a>

</div>

<div class="goods-detail-layout">

<aside class="goods-detail-left-sidebar">

    <c:if test="${not empty content}">

        <section class="detail-side-card">

            <div class="detail-side-card-header">
                <h2>관련 콘텐츠</h2>
            </div>

            <div class="detail-side-card-body">

                <a class="side-content-item"
                   href="${pageContext.request.contextPath}/content/contentDetail/${content.contentNo}">

                    <div class="side-content-poster">

                        <c:choose>

                            <c:when test="${not empty content.posterPath}">

                                <img src="https://image.tmdb.org/t/p/w342${content.posterPath}"
                                     alt="<c:out value='${content.title}'/>">

                            </c:when>

                            <c:otherwise>

                                <div class="no-img">
                                    NO IMAGE
                                </div>

                            </c:otherwise>

                        </c:choose>

                    </div>

                    <div class="side-content-info">

                        <strong>
                            <c:out value="${content.title}"/>
                        </strong>

                        <span>

                            <c:out value="${content.contentType}"/>

                            <c:if test="${not empty content.tmdbScore}">
                                ·
                                <span class="rating">★</span>
                                <fmt:formatNumber
                                    value="${content.tmdbScore}"
                                    pattern="0.0"/>
                            </c:if>

                        </span>

                    </div>

                </a>

            </div>

        </section>

    </c:if>

    <c:if test="${not empty actor}">

        <section class="detail-side-card">

            <div class="detail-side-card-header">
                <h2>관련 배우</h2>
            </div>

            <div class="detail-side-card-body">

                <a class="actor-card"
                     href="${pageContext.request.contextPath}/content/person/${actor.tmdbActorId}">

                    <div class="actor-card__photo">

                        <c:choose>

                            <c:when test="${not empty actor.profilePath}">

                                <img src="https://image.tmdb.org/t/p/w185${actor.profilePath}"
                                     alt="<c:out value='${actor.actorName}'/>">

                            </c:when>

                            <c:otherwise>

                                <div class="actor-card__no-image">
                                    NO IMAGE
                                </div>

                            </c:otherwise>

                        </c:choose>

                    </div>

                    <div class="actor-card__info">

                        <strong>
                            <c:out value="${actor.actorName}"/>
                        </strong>

                        <span>
                            출연 배우
                        </span>

                    </div>

                </a>

            </div>

        </section>

    </c:if>

</aside>

<div class="goods-detail-main">

<%-- =====================================================
     [레이아웃 병합]
     기존 JSP 출력값과 상품 옵션/수량 처리 로직은 유지하고,
     오른쪽 수정본의 상품 상단 카드 레이아웃 구조만 반영한다.
====================================================== --%>
<section class="detail-header goods-detail-card">

    <%-- =====================================================
         [수정]
         왼쪽: 대표 이미지 + 세부 이미지 3개씩 슬라이드
         오른쪽: 4번째 시안처럼 상단 요약 레이아웃 구성
    ====================================================== --%>
    <div class="detail-poster">

        <div class="detail-main-image-wrap">

            <c:choose>

                <c:when test="${not empty goods.mainImage}">

                    <c:choose>

                        <c:when test="${fn:startsWith(goods.mainImage, 'http://')
                                        or fn:startsWith(goods.mainImage, 'https://')}">

                            <img id="mainImage"
                                 src="${goods.mainImage}"
                                 alt="<c:out value='${goods.productName}'/>">

                        </c:when>

                        <c:otherwise>

                            <img id="mainImage"
                                 src="${pageContext.request.contextPath}${goods.mainImage}"
                                 alt="<c:out value='${goods.productName}'/>">

                        </c:otherwise>

                    </c:choose>

                </c:when>

                <c:otherwise>

                    <div class="no-img">
                        NO IMAGE
                    </div>

                </c:otherwise>

            </c:choose>

        </div>

        <%-- [추가] 대표 이미지 제외한 세부 이미지만 아래 슬라이드 영역에 노출 --%>
        <c:set var="detailImageCount" value="0"/>

        <c:forEach var="img" items="${imageList}">
            <c:if test="${img.isMain ne 'Y'}">
                <c:set var="detailImageCount" value="${detailImageCount + 1}"/>
            </c:if>
        </c:forEach>

        <c:if test="${detailImageCount > 0}">

            <div class="detail-sub-gallery">

                <button type="button"
                        class="detail-gallery-nav prev"
                        aria-label="이전 세부 이미지"
                        disabled>
                    ‹
                </button>

                <div class="detail-gallery-viewport">

                    <div class="detail-gallery-track">

                        <c:forEach var="img" items="${imageList}">

                            <c:if test="${img.isMain ne 'Y'}">

                                <c:set var="imageUrl"
                                       value="${img.imagePath}"/>

                                <c:if test="${not fn:startsWith(imageUrl, 'http://')
                                            and not fn:startsWith(imageUrl, 'https://')}">

                                    <c:set var="imageUrl"
                                           value="${pageContext.request.contextPath}${imageUrl}"/>

                                </c:if>

                                <button type="button"
                                        class="detail-gallery-thumb"
                                        data-full="${imageUrl}"
                                        aria-label="세부 이미지 보기">

                                    <img src="${imageUrl}"
                                         alt="<c:out value='${goods.productName}'/> 세부 이미지">

                                </button>

                            </c:if>

                        </c:forEach>

                    </div>

                </div>

                <button type="button"
                        class="detail-gallery-nav next"
                        aria-label="다음 세부 이미지">
                    ›
                </button>

            </div>

        </c:if>

    </div>

    <div class="detail-info">

        <%-- =====================================================
             [수정] 상단 요약 레이아웃
        ====================================================== --%>
        <div class="detail-summary-top">

            <div class="detail-summary-heading">

                <div class="detail-brand-row">

                    <span class="detail-brand-label">
                        판매자
                    </span>

                    <p class="detail-brand">
                        <c:out value="${goods.businessName}"/>
                    </p>

                </div>

                <div class="detail-title-line">

                    <h1 class="detail-title">
                        <c:out value="${goods.productName}"/>
                    </h1>

                    <c:choose>

                        <c:when test="${goods.stock <= 0}">
                            <span class="status-badge sold-out">
                                품절
                            </span>
                        </c:when>

                        <c:otherwise>
                            <span class="status-badge on-sale">
                                판매중
                            </span>
                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

            <div class="price-box">

                <div class="price-value">

                    <c:choose>

                        <c:when test="${goods.discountRate > 0}">

                            <span class="price-original">
                                ₩
                                <fmt:formatNumber
                                    value="${goods.price}"
                                    pattern="#,###"/>
                            </span>

                            <span class="price-final">

                                <span class="rate">
                                    ${goods.discountRate}%
                                </span>

                                ₩
                                <fmt:formatNumber
                                    value="${goods.discountPrice}"
                                    pattern="#,###"/>

                            </span>

                        </c:when>

                        <c:otherwise>

                            <span class="price-final">
                                ₩
                                <fmt:formatNumber
                                    value="${goods.price}"
                                    pattern="#,###"/>
                            </span>

                        </c:otherwise>

                    </c:choose>

                </div>

            </div>

        </div>

        <div class="detail-meta">

            <span class="detail-meta-item">

                <span class="detail-meta-label">
                    상품 종류
                </span>

                <strong>
                    <c:out value="${goods.productType}"/>
                </strong>

            </span>

            <span class="detail-meta-item">

                <span class="detail-meta-label">
                    판매 상태
                </span>

                <c:choose>

                    <c:when test="${goods.stock <= 0}">
                        <span class="status-badge sold-out">
                            품절
                        </span>
                    </c:when>

                    <c:otherwise>
                        <span class="status-badge on-sale">
                            판매중
                        </span>
                    </c:otherwise>

                </c:choose>

            </span>

            <span class="detail-meta-item">

            <%-- =====================================================
                [수정]
                옵션 상품의 PRODUCT.STOCK은 각 옵션 재고의 합계이므로
                "남은 재고" 대신 "전체 재고"로 표시합니다.
                옵션이 없는 일반 상품은 기존처럼 "남은 재고"로 표시합니다.
            ====================================================== --%>
            <span class="detail-meta-label">
                <c:choose>
                    <c:when test="${not empty productOptionList}">
                        전체 재고
                    </c:when>
                    <c:otherwise>
                        남은 재고
                    </c:otherwise>
                </c:choose>
            </span>

            <strong>
                <fmt:formatNumber
                    value="${goods.stock}"
                    pattern="#,###"/>개
            </strong>

        </span>

        </div>

        <div class="detail-description-box">

            <span class="detail-description-title">
                상품 설명
            </span>

            <p class="detail-desc"><c:out value="${goods.description}"/></p>

        </div>

        <%-- [상품 옵션 기능 추가] 의상/신발 색상과 사이즈를 선택하고 조합 재고를 확인합니다. --%>
        <%-- =====================================================
            [상품 옵션 / 재입고 UI 수정]
            - 색상/사이즈 선택 영역을 2열 구조로 분리
            - 선택한 옵션의 실제 재고를 별도 영역에 표시
            - 선택 옵션이 품절일 경우에만 재입고 알림 영역 표시
        ====================================================== --%>
        <c:if test="${not empty productOptionList}">

            <div class="detail-product-options">

                <%-- 색상 / 사이즈 선택 영역 --%>
                <div class="detail-option-grid">

                    <div class="detail-option-field">

                        <label for="detailColor">
                            색상
                        </label>

                        <select id="detailColor"
                                class="detail-option-select">

                            <option value="">
                                색상 선택
                            </option>

                        </select>

                    </div>

                    <div class="detail-option-field">

                        <label for="detailSize">
                            사이즈
                        </label>

                        <select id="detailSize"
                                class="detail-option-select"
                                disabled>

                            <option value="">
                                사이즈 선택
                            </option>

                        </select>

                    </div>

                </div>

                <%-- =====================================================
                    [수정]
                    선택한 색상/사이즈 조합의 재고 표시
                ====================================================== --%>
                <div class="detail-option-status-row">

                    <span class="detail-option-status-label">
                        선택 옵션 재고
                    </span>

                    <strong id="detailOptionStock"
                            class="detail-option-stock">
                        색상과 사이즈를 선택해주세요.
                    </strong>

                </div>

                <%-- =====================================================
                    [옵션별 재입고 알림]
                    선택한 PRODUCT_OPTION.STOCK이 0일 때만
                    JS에서 이 영역을 표시합니다.
                ====================================================== --%>
                <div id="optionRestockArea"
                    class="option-restock-area"
                    style="display: none;">

                    <div class="option-restock-icon"
                        aria-hidden="true">
                        !
                    </div>

                    <div class="option-restock-info">

                        <strong id="optionRestockTitle"
                                class="option-restock-title">
                            선택한 옵션이 품절되었습니다.
                        </strong>

                        <span class="option-restock-description">
                            재고가 다시 들어오면 알림으로 알려드릴게요.
                        </span>

                    </div>

                    <button type="button"
                            id="optionRestockRequestBtn"
                            class="btn restock-request-btn"
                            data-product-no="${goods.productNo}"
                            data-option-no=""
                            aria-pressed="false">

                        선택 옵션 재입고 알림 신청

                    </button>

                    <span id="optionRestockRequestMessage"
                        class="restock-request-message"
                        aria-live="polite"></span>

                </div>

            </div>

            <%-- =====================================================
                optionNo / 색상 / 사이즈 / 재고 정보를 JS에 전달
            ====================================================== --%>
            <script type="application/json"
                    id="productOptionData">[<c:forEach var="opt" items="${productOptionList}" varStatus="st">{"optionNo":${opt.optionNo},"color":"${fn:escapeXml(opt.colorName)}","size":"${fn:escapeXml(opt.sizeName)}","stock":${opt.stock}}<c:if test="${not st.last}">,</c:if></c:forEach>]</script>

        </c:if>
        <div class="detail-purchase-option">

            <div class="detail-purchase-option-header">

                <div class="detail-purchase-option-title">

                    <strong>
                        구매 수량
                    </strong>

                    <%-- [유지] 옵션 상품은 옵션 선택 후 선택 옵션 재고를 안내한다. --%>
                    <span id="detailQuantityGuide">
                        <c:choose>

                            <c:when test="${not empty productOptionList}">
                                옵션을 선택해주세요.
                            </c:when>

                            <c:otherwise>
                                최대
                                <fmt:formatNumber
                                    value="${goods.stock}"
                                    pattern="#,###"/>개까지 선택할 수 있습니다.
                            </c:otherwise>

                        </c:choose>
                    </span>

                </div>

                <div class="detail-total-price">

                    <span>
                        총 상품 금액
                    </span>

                    <strong id="detailTotalPrice">
                        ₩
                        <fmt:formatNumber
                            value="${goods.discountRate > 0 ? goods.discountPrice : goods.price}"
                            pattern="#,###"/>
                    </strong>

                </div>

            </div>

            <div class="detail-quantity-control"
                 data-unit-price="${goods.discountRate > 0 ? goods.discountPrice : goods.price}"
                 data-stock="${goods.stock}">

                <button type="button"
                        id="detailQuantityMinus"
                        class="detail-quantity-button"
                        aria-label="수량 줄이기"
                        ${goods.stock <= 0 ? 'disabled' : ''}>
                    −
                </button>

                <input type="number"
                       id="detailQuantity"
                       class="detail-quantity-input"
                       value="${goods.stock > 0 ? 1 : 0}"
                       min="${goods.stock > 0 ? 1 : 0}"
                       max="${goods.stock}"
                       inputmode="numeric"
                       aria-label="구매 수량"
                       ${goods.stock <= 0 ? 'disabled' : ''}>

                <button type="button"
                        id="detailQuantityPlus"
                        class="detail-quantity-button"
                        aria-label="수량 늘리기"
                        ${goods.stock <= 0 ? 'disabled' : ''}>
                    +
                </button>

            </div>

        </div>

        <div class="action-box">

            <button type="button"
                    id="detailWishBtn"
                    class="btn fav-btn detail-favorite-btn${wishActive ? ' active' : ''}"
                    data-type="goods"
                    data-product-no="${goods.productNo}"
                    aria-pressed="${wishActive}"
                    aria-label="찜하기"
                    title="찜하기">

                <span class="fav-icon">

                    <c:choose>
                        <c:when test="${wishActive}">♥</c:when>
                        <c:otherwise>♡</c:otherwise>
                    </c:choose>

                </span>

                찜하기

            </button>

            <button type="button"
                    class="btn cart-btn"
                    data-product-no="${goods.productNo}"
                    data-stock="${goods.stock}"
                    ${goods.stock <= 0 ? 'disabled' : ''}>
                <span class="cart-icon" aria-hidden="true">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                        <path d="M3 4h2l2.4 12.2a2 2 0 0 0 2 1.6h7.6a2 2 0 0 0 2-1.6L21 8H6.2"
                              stroke="currentColor"
                              stroke-width="2"
                              stroke-linecap="round"
                              stroke-linejoin="round"/>
                        <circle cx="10" cy="20" r="1.4" fill="currentColor"/>
                        <circle cx="17" cy="20" r="1.4" fill="currentColor"/>
                    </svg>
                </span>
                장바구니
            </button>

            <button type="button"
                    class="btn btn-primary buy-btn"
                    data-product-no="${goods.productNo}"
                    data-stock="${goods.stock}"
                    ${goods.stock <= 0 ? 'disabled' : ''}>
                바로 구매
            </button>

        </div>

        <c:if test="${goods.stock <= 0}">

            <div class="sold-out-restock-box">

                <p class="sold-out-guide">
                    현재 품절된 상품입니다.
                </p>

                <%-- =====================================================
                    [상품 전체 재입고 알림]
                    PRODUCT.STOCK이 0일 경우 표시합니다.

                    OPTION_NO가 없는 신청이므로
                    data-option-no는 빈 값으로 전달합니다.
                ====================================================== --%>
                <button type="button"
                        id="restockRequestBtn"
                        class="btn restock-request-btn"
                        data-product-no="${goods.productNo}"
                        data-option-no=""
                        aria-pressed="false">
                    상품 재입고 알림 신청
                </button>

                <span id="restockRequestMessage"
                    class="restock-request-message"
                    aria-live="polite"></span>

            </div>

        </c:if>

    </div>

</section>

<%-- [수정] 상품 리뷰 영역을 독립된 카드로 표시한다. --%>
<section id="reviewSection"
         class="detail-section review-section-card">

    <div class="review-section-header">

        <div class="review-section-title">

            <span class="review-section-eyebrow">
                REVIEW
            </span>

            <h2>
                상품 리뷰
            </h2>

            <p>
                실제 구매 회원이 작성한 상품 후기를 확인해보세요.
            </p>

        </div>

        <div class="score-box">

            <c:choose>

                <c:when test="${reviewCount > 0 and not empty avgRating}">

                    <span class="review-score-star"
                          aria-hidden="true">
                        ★
                    </span>

                    <div class="review-score-info">

                        <strong>
                            <fmt:formatNumber
                                value="${avgRating}"
                                pattern="0.0"/>
                        </strong>

                        <span>
                            총
                            <fmt:formatNumber
                                value="${reviewCount}"
                                pattern="#,###"/>개의 리뷰
                        </span>

                    </div>

                </c:when>

                <c:otherwise>

                    <div class="review-score-empty">

                        <span class="review-score-star"
                              aria-hidden="true">
                            ☆
                        </span>

                        <span>
                            등록된 평점 없음
                        </span>

                    </div>

                </c:otherwise>

            </c:choose>

        </div>

    </div>

    <div class="review-list">

        <c:choose>

            <c:when test="${not empty reviewList}">

                <c:forEach var="r"
                           items="${reviewList}">

                    <div class="review-item">

                        <div class="review-meta">

                            <span class="writer">
                                <c:out value="${r.writer}"/>
                            </span>

                            <span class="rating">
                                ★
                                <fmt:formatNumber
                                    value="${r.rating}"
                                    pattern="0.0"/>
                            </span>

                            <span class="date">
                                ${dt:format(r.createdAt, 'yyyy-MM-dd')}
                            </span>

                            <c:choose>

                                <%-- [수정] 로그인 회원이 작성한 상품 리뷰에는 신고 버튼 대신 삭제 버튼을 표시한다. --%>
                                <c:when test="${not empty sessionScope.loginMember
                                                and sessionScope.loginMember.memberNo eq r.memberNo}">

                                    <div class="my-product-review-actions">

                                        <form action="${pageContext.request.contextPath}/review/deleteProductReview"
                                              method="post"
                                              class="product-review-delete-form"
                                              onsubmit="return confirmAndSubmit(event, '상품 리뷰를 삭제하시겠습니까?');">

                                            <input type="hidden"
                                                   name="reviewNo"
                                                   value="${r.reviewNo}">

                                            <%-- [수정] 상품 상세 화면에서 삭제한 경우 현재 상품 리뷰 영역으로 돌아가기 위해 전달한다. --%>
                                            <input type="hidden"
                                                   name="productNo"
                                                   value="${goods.productNo}">

                                            <button type="submit"
                                                    class="product-review-delete-btn">
                                                삭제
                                            </button>

                                        </form>

                                    </div>

                                </c:when>

                                <%-- [수정] 이미 신고한 다른 회원의 상품 리뷰에는 신고완료를 표시한다. --%>
                                <c:when test="${not empty reportedReviewSet
                                                and reportedReviewSet.contains(r.reviewNo)}">

                                    <span class="report-btn reported"
                                          aria-disabled="true">
                                        <span class="report-btn-icon" aria-hidden="true">
                                            <svg viewBox="0 0 24 24" fill="none">
                                                <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"
                                                      stroke="currentColor"
                                                      stroke-width="2"
                                                      stroke-linecap="round"
                                                      stroke-linejoin="round"/>
                                                <line x1="4" y1="22" x2="4" y2="3"
                                                      stroke="currentColor"
                                                      stroke-width="2"
                                                      stroke-linecap="round"/>
                                            </svg>
                                        </span>
                                        신고완료
                                    </span>

                                </c:when>

                                <%-- [수정] 본인 리뷰가 아니며 신고하지 않은 리뷰에만 신고 버튼을 표시한다. --%>
                                <c:otherwise>

                                    <button type="button"
                                            class="report-btn"
                                            data-review-type="PRODUCT"
                                            data-review-no="${r.reviewNo}"
                                            aria-label="<c:out value='${r.writer}'/>님의 리뷰 신고">
                                        <span class="report-btn-icon" aria-hidden="true">
                                            <svg viewBox="0 0 24 24" fill="none">
                                                <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"
                                                      stroke="currentColor"
                                                      stroke-width="2"
                                                      stroke-linecap="round"
                                                      stroke-linejoin="round"/>
                                                <line x1="4" y1="22" x2="4" y2="3"
                                                      stroke="currentColor"
                                                      stroke-width="2"
                                                      stroke-linecap="round"/>
                                            </svg>
                                        </span>
                                        신고
                                    </button>

                                </c:otherwise>

                            </c:choose>

                        </div>

                        <p class="review-content"><c:out value="${r.content}"/></p>

                    </div>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <%-- [수정] 리뷰가 없을 때 완성된 빈 상태 화면을 표시한다. --%>
                <div class="empty-state product-review-empty">

                    <div class="product-review-empty-icon"
                         aria-hidden="true">
                        ☆
                    </div>

                    <strong>
                        아직 등록된 상품 리뷰가 없습니다
                    </strong>

                    <p>
                        상품을 구매한 후 첫 번째 리뷰를 남겨보세요.
                    </p>

                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>

<div id="reportModal"
     class="modal-overlay"
     hidden>

    <div class="modal-box">

        <h3>리뷰 신고</h3>

        <input type="hidden"
               id="reportReviewType"
               value="">

        <input type="hidden"
               id="reportReviewNo"
               value="">

        <div class="modal-field">

            <label for="reportReason">
                신고 사유
            </label>

            <select id="reportReason">

                <option value="욕설/비방">
                    욕설/비방
                </option>

                <option value="스팸/광고">
                    스팸/광고
                </option>

                <option value="음란물/불법정보">
                    음란물/불법정보
                </option>

                <option value="기타">
                    기타
                </option>

            </select>

        </div>

        <div class="modal-field">

            <label for="reportDetail">
                상세 내용
            </label>

            <textarea id="reportDetail"
                      maxlength="1000"
                      placeholder="신고 사유를 자세히 적어주세요"></textarea>

        </div>

        <div class="modal-actions">

            <button type="button"
                    id="reportCancelBtn"
                    class="btn">
                취소
            </button>

            <button type="button"
                    id="reportSubmitBtn"
                    class="btn btn-danger">
                신고하기
            </button>

        </div>

    </div>

</div>

</div>

<aside class="goods-detail-sidebar">

    <jsp:include page="/WEB-INF/views/common/goodsRightSidebar.jsp"/>

</aside>

</div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>