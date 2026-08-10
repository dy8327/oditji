<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<!DOCTYPE html>

<%--
    SonarQube 접근성 이슈 대응:

    문서의 기본 언어가 한국어임을
    브라우저와 화면 낭독기에 알립니다.
--%>
<html lang="ko">

    <head>

        <meta charset="UTF-8">

        <title>
            <c:out value="${event.title}"/>
        </title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/css/event.css?v=2">

        <script>
            const contextPath = "${pageContext.request.contextPath}";
        </script>
        <script defer src="${pageContext.request.contextPath}/js/event.js?v=2"></script>

    <jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <main id="mainContent" class="main">

            <section class="event-detail">

                <a href="${pageContext.request.contextPath}/event/list?period=${period}"
                   class="event-back-link">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                        <path d="M15 6l-6 6 6 6" stroke="currentColor" stroke-width="2"
                              stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    이벤트 목록
                </a>

                <div class="event-hero event-hero--${period}"
                     data-event-timer
                     data-period="${period}"
                     data-start="${dt:format(event.startDate, 'yyyy-MM-dd')}"
                     data-end="${dt:format(event.endDate, 'yyyy-MM-dd')}">

                    <c:choose>

                        <c:when test="${not empty event.bannerImage}">

                            <img class="event-hero-image"
                                 src="${pageContext.request.contextPath}${event.bannerImage}"
                                 alt="${fn:escapeXml(event.title)}">

                        </c:when>

                        <c:otherwise>

                            <div class="event-hero-image no-img">
                                NO IMAGE
                            </div>

                        </c:otherwise>

                    </c:choose>

                    <div class="event-hero-scrim"></div>

                    <div class="event-hero-content">

                        <span class="event-hero-badge">
                            ${periodBadge} 이벤트
                            <span class="event-hero-dday" data-dday-text></span>
                        </span>

                        <h1 class="event-hero-title">
                            <c:out value="${event.title}"/>
                        </h1>

                        <p class="event-period">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <rect x="3" y="5" width="18" height="16" rx="2" stroke="currentColor" stroke-width="1.8"/>
                                <path d="M3 9h18" stroke="currentColor" stroke-width="1.8"/>
                                <path d="M8 3v4M16 3v4" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                            </svg>
                            ${dt:format(event.startDate, 'yyyy.MM.dd')}
                            ~
                            ${dt:format(event.endDate, 'yyyy.MM.dd')}
                        </p>

                        <div class="event-countdown event-countdown--hero" data-big-countdown aria-live="polite"></div>

                        <c:if test="${period eq 'ongoing' or period eq 'upcoming'}">
                            <span class="event-progress event-progress--hero" data-progress-bar aria-hidden="true"></span>
                        </c:if>

                    </div>

                    <a href="#eventProducts" class="event-hero-products-jump" data-event-quicknav>
                        <span class="event-hero-products-jump-icon">
                            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <path d="M20.5 12.7 12.7 20.5a1.7 1.7 0 0 1-2.4 0l-6.8-6.8a1.7 1.7 0 0 1 0-2.4L11.5 3.5a1.7 1.7 0 0 1 1.2-.5H19a1.5 1.5 0 0 1 1.5 1.5v6.3a1.7 1.7 0 0 1-.5 1.2Z"
                                      stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
                                <circle cx="16" cy="8" r="1.3" fill="currentColor"/>
                            </svg>
                        </span>
                        <span class="event-hero-products-jump-text">
                            이벤트 상품
                            <span class="event-hero-products-jump-count"><c:out value="${fn:length(event.products)}"/></span>
                        </span>
                        <svg class="event-hero-products-jump-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                            <path d="M9 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                                  stroke-linecap="round" stroke-linejoin="round"/>
                        </svg>
                    </a>

                </div>

                <c:if test="${not empty event.description}">

                    <section class="event-description-section event-description-section--${period}" id="eventInfo">

                        <div class="event-description-heading">

                            <span class="event-description-icon" aria-hidden="true">
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                    <circle cx="12" cy="12" r="9" stroke="currentColor" stroke-width="1.8"/>
                                    <path d="M12 11v5.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                                    <circle cx="12" cy="7.7" r="1.15" fill="currentColor"/>
                                </svg>
                            </span>

                            <div class="event-description-heading-text">
                                <h2>이벤트 안내</h2>
                                <p>참여 전 아래 내용을 꼭 확인해주세요</p>
                            </div>

                            <span class="event-description-badge">${periodBadge}</span>

                        </div>

                        <div class="event-description-body">
                            <p class="event-description-text">
                                <c:out value="${event.description}"/>
                            </p>
                        </div>

                    </section>

                </c:if>

                <section class="product-section product-section--${period}" id="eventProducts">

                    <div class="product-section-heading">

                        <span class="product-section-icon" aria-hidden="true">
                            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                <path d="M20.5 12.7 12.7 20.5a1.7 1.7 0 0 1-2.4 0l-6.8-6.8a1.7 1.7 0 0 1 0-2.4L11.5 3.5a1.7 1.7 0 0 1 1.2-.5H19a1.5 1.5 0 0 1 1.5 1.5v6.3a1.7 1.7 0 0 1-.5 1.2Z"
                                      stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/>
                                <circle cx="16" cy="8" r="1.3" fill="currentColor"/>
                            </svg>
                        </span>

                        <div class="product-section-heading-text">
                            <h2>이벤트 상품</h2>
                            <p>이번 이벤트에서 만나볼 수 있는 상품이에요</p>
                        </div>

                        <span class="product-count">
                            <c:out value="${fn:length(event.products)}"/>개
                        </span>

                    </div>

                    <c:choose>

                        <c:when test="${not empty event.products}">

                            <div class="product-grid">

                        <c:forEach var="product"
                                   items="${event.products}">

                            <a href="${pageContext.request.contextPath}/goods/goodsDetail/${product.productNo}"
                               class="product-card">

                                <div class="product-image">

                                    <c:choose>

                                        <c:when test="${not empty product.imagePath}">

                                            <img src="${pageContext.request.contextPath}${product.imagePath}"
                                                 alt="${product.productName}">

                                        </c:when>

                                        <c:otherwise>

                                            <div class="no-img">
                                                NO IMAGE
                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                    <c:if test="${product.eventDiscountRate > 0}">
                                        <span class="product-discount-badge">
                                            ${product.eventDiscountRate}%
                                        </span>
                                    </c:if>

                                    <span class="product-card-overlay">
                                        <span class="product-card-overlay-text">
                                            상품 보기
                                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                                <path d="M9 6l6 6-6 6" stroke="currentColor" stroke-width="2"
                                                      stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </span>
                                    </span>

                                </div>

                                <div class="product-info">

                                    <h3>
                                        ${product.productName}
                                    </h3>

                                    <c:choose>

                                        <c:when test="${product.eventDiscountRate > 0}">

                                            <p class="price-original">
                                                <fmt:formatNumber
                                                        value="${product.price}"
                                                        pattern="#,###"/>원
                                            </p>

                                            <p class="price-discounted">

                                                <fmt:formatNumber
                                                        value="${product.discountPrice}"
                                                        pattern="#,###"/>원

                                                <span class="discount-rate">
                                                    ${product.eventDiscountRate}%
                                                </span>

                                            </p>

                                        </c:when>

                                        <c:otherwise>

                                            <p>
                                                <fmt:formatNumber
                                                        value="${product.price}"
                                                        pattern="#,###"/>원

                                            </p>

                                        </c:otherwise>

                                    </c:choose>

                                </div>

                            </a>

                        </c:forEach>

                            </div>

                        </c:when>

                        <c:otherwise>

                            <div class="empty">
                                <svg width="40" height="40" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                                    <rect x="3" y="6" width="18" height="14" rx="2" stroke="currentColor" stroke-width="1.6"/>
                                    <path d="M3 10h18" stroke="currentColor" stroke-width="1.6"/>
                                    <path d="M8 3v4M16 3v4" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/>
                                </svg>
                                <p>이벤트에 등록된 상품이 없습니다.</p>
                            </div>

                        </c:otherwise>

                    </c:choose>

                </section>

            </section>

        </main>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>

</html>
