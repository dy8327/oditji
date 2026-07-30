<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

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
            ${event.title}
        </title>

        <link rel="stylesheet"
              href="${pageContext.request.contextPath}/css/event.css">

    </head>

    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <main class="main">

            <section class="event-detail">

                <a href="${pageContext.request.contextPath}/event/list"
                   class="event-back-link">
                    &larr; 이벤트 목록
                </a>

                <div class="event-hero event-hero--${period}">

                    <c:choose>

                        <c:when test="${not empty event.bannerImage}">

                            <img class="event-hero-image"
                                 src="${pageContext.request.contextPath}${event.bannerImage}"
                                 alt="${event.title}">

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
                        </span>

                        <h1 class="event-hero-title">
                            ${event.title}
                        </h1>

                        <p class="event-period">
                            <fmt:formatDate
                                    value="${event.startDate}"
                                    pattern="yyyy.MM.dd"/>
                            ~
                            <fmt:formatDate
                                    value="${event.endDate}"
                                    pattern="yyyy.MM.dd"/>
                        </p>

                    </div>

                </div>

                <c:if test="${not empty event.description}">

                    <section class="event-description-section">

                        <h2>이벤트 안내</h2>

                        <p class="event-description-text">
                            <c:out value="${event.description}"/>
                        </p>

                    </section>

                </c:if>

                <section class="product-section">

                    <h2>
                        이벤트 상품
                        <span class="product-count">
                            <c:out value="${fn:length(event.products)}"/>개
                        </span>
                    </h2>

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
                                이벤트에 등록된 상품이 없습니다.
                            </div>

                        </c:otherwise>

                    </c:choose>

                </section>

            </section>

        </main>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>

</html>