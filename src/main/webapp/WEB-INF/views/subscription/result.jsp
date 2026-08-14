<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>OTT 구독 조합 계산 결과 - ODITJI</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/subscription-calculator.css">
    <script defer src="${pageContext.request.contextPath}/js/subscriptionCalculator.js"></script>
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>
<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main id="mainContent" class="main">
<section class="sub-calc-section" id="subCalcResultSection">

    <div class="sub-calc-hero">
        <p class="sub-calc-hero__eyebrow">ODITJI TOOL</p>
        <h1 class="sub-calc-hero__title">공유된 구독 조합 계산 결과</h1>
        <p class="sub-calc-hero__desc">
            다른 사람이 공유한 OTT 구독 조합 계산 결과예요. 나에게 맞는 조건으로
            다시 계산하고 싶다면 계산기에서 새로 시작해보세요.
        </p>
    </div>

    <c:choose>
        <c:when test="${resultNotFound}">
            <div class="sub-calc-result-panel">
                <p class="sub-calc-result-placeholder">
                    존재하지 않거나 만료된 공유 링크예요.
                </p>
                <a class="sub-calc-calculate-btn" href="${pageContext.request.contextPath}/subscription/calculator">
                    계산기로 이동하기
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <div class="sub-calc-result">
                <p class="sub-calc-result__eyebrow">최저가 구독 조합 결과</p>

                <ul class="sub-calc-result-platform-list">
                    <c:forEach var="platform" items="${result.selectedPlatformList}" varStatus="platformStatus">
                        <li class="sub-calc-result-platform">
                            <button type="button" class="sub-calc-result-platform__toggle"
                                    aria-expanded="false"
                                    aria-controls="resultPlatformBody-${platformStatus.index}">
                                <span class="sub-calc-result-platform__head">
                                    <span class="sub-calc-result-platform__name">${platform.platformName}</span>
                                    <span>
                                        <c:if test="${platform.discountSource != null && platform.bestPrice < platform.regularPrice}">
                                            <span class="sub-calc-result-platform__regular">
                                                <fmt:formatNumber value="${platform.regularPrice}" type="number"/>원
                                            </span>
                                        </c:if>
                                        <span class="sub-calc-result-platform__price">
                                            <fmt:formatNumber value="${platform.bestPrice}" type="number"/>원
                                        </span>
                                        <c:if test="${platform.discountSource != null && platform.discountRate != null}">
                                            <span class="sub-calc-result-platform__rate">(${platform.discountRate}% 할인)</span>
                                        </c:if>
                                    </span>
                                </span>
                                <span class="sub-calc-result-platform__chevron" aria-hidden="true"></span>
                            </button>
                            <div class="sub-calc-result-platform__body" id="resultPlatformBody-${platformStatus.index}" inert>
                            <div class="sub-calc-result-platform__body-inner">
                                <span class="sub-calc-result-platform__source">
                                    <c:choose>
                                        <c:when test="${platform.discountSource != null}">${platform.discountSource} 적용 시</c:when>
                                        <c:otherwise>(기본 정가 적용)</c:otherwise>
                                    </c:choose>
                                </span>

                                <c:if test="${not empty platform.contentList}">
                                    <ul class="sub-calc-result-platform__content-list">
                                        <c:forEach var="content" items="${platform.contentList}">
                                            <li class="sub-calc-result-platform__content-item">
                                                <c:choose>
                                                    <c:when test="${not empty content.tmdbId && not empty content.contentType}">
                                                        <c:url var="platformContentDetailUrl" value="/content/prepare">
                                                            <c:param name="tmdbId" value="${content.tmdbId}"/>
                                                            <c:param name="contentType" value="${content.contentType}"/>
                                                        </c:url>
                                                        <a class="sub-calc-result-platform__content-link" href="${platformContentDetailUrl}">
                                                            <c:if test="${not empty content.posterPath}">
                                                                <img class="sub-calc-result-platform__content-poster"
                                                                     src="https://image.tmdb.org/t/p/w92${content.posterPath}"
                                                                     alt="" loading="lazy">
                                                            </c:if>
                                                            <span class="sub-calc-result-platform__content-name">${content.title}</span>
                                                        </a>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="sub-calc-result-platform__content-link">
                                                            <c:if test="${not empty content.posterPath}">
                                                                <img class="sub-calc-result-platform__content-poster"
                                                                     src="https://image.tmdb.org/t/p/w92${content.posterPath}"
                                                                     alt="" loading="lazy">
                                                            </c:if>
                                                            <span class="sub-calc-result-platform__content-name">${content.title}</span>
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                </c:if>
                            </div>
                            </div>
                        </li>
                    </c:forEach>
                </ul>

                <div class="sub-calc-result-total">
                    <span>월 정가 합계</span>
                    <span><fmt:formatNumber value="${result.totalRegularMonthlyPrice}" type="number"/>원</span>
                </div>
                <div class="sub-calc-result-total sub-calc-result-total--main">
                    <span>할인 적용 총 예상 금액</span>
                    <strong><fmt:formatNumber value="${result.totalMonthlyPrice}" type="number"/>원</strong>
                </div>

                <c:if test="${result.totalRegularMonthlyPrice - result.totalMonthlyPrice > 0}">
                    <p class="sub-calc-result-savings">
                        월 <fmt:formatNumber value="${result.totalRegularMonthlyPrice - result.totalMonthlyPrice}" type="number"/>원 절감 효과!
                    </p>
                </c:if>

                <c:if test="${not empty result.unresolvedItemList}">
                    <div class="sub-calc-result-unresolved">
                        <p class="sub-calc-result-unresolved__title">가격 정보가 없어 제외된 작품</p>
                        <ul>
                            <c:forEach var="item" items="${result.unresolvedItemList}">
                                <li>${item.title}</li>
                            </c:forEach>
                        </ul>
                    </div>
                </c:if>
            </div>

            <div class="sub-calc-result-actions">
                <a class="sub-calc-calculate-btn" href="${pageContext.request.contextPath}/subscription/calculator">
                    나도 계산해보기
                </a>
            </div>
        </c:otherwise>
    </c:choose>

</section>
</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
