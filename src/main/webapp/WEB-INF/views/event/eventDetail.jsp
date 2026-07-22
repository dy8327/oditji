<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html>

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

                <div class="event-header">

                    <h1>
                        ${event.title}
                    </h1>

                    <p>
                        <fmt:formatDate value="${event.startDate}" pattern="yyyy.MM.dd"/>
                        ~
                        <fmt:formatDate value="${event.endDate}" pattern="yyyy.MM.dd"/>
                    </p>

                </div>

                <div class="detail-banner">

                    <c:choose>

                        <c:when test="${not empty event.bannerImage}">

                            <img src="${pageContext.request.contextPath}${event.bannerImage}"
                            alt="${event.title}">

                        </c:when>

                        <c:otherwise>

                            <div class="no-img">
                                NO IMAGE
                            </div>

                        </c:otherwise>

                    </c:choose>

                </div>

                <section class="product-section">

                    <h2>
                        이벤트 상품
                    </h2>

                    <div class="product-grid">

                        <c:forEach var="product" items="${event.products}">

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

                                </div>

                                <div class="product-info">

                                    <h3>
                                        ${product.productName}
                                    </h3>

                                    <c:choose>

                                        <c:when test="${product.eventDiscountRate > 0}">

                                            <p class="price-original">
                                                <fmt:formatNumber value="${product.price}" pattern="#,###"/>원
                                            </p>

                                            <p class="price-discounted">
                                                <fmt:formatNumber value="${product.discountPrice}" pattern="#,###"/>원
                                                <span class="discount-rate">
                                                    ${product.eventDiscountRate}%
                                                </span>
                                            </p>

                                        </c:when>

                                        <c:otherwise>

                                            <p>
                                                <fmt:formatNumber value="${product.price}" pattern="#,###"/>원
                                            </p>

                                        </c:otherwise>

                                    </c:choose>

                                </div>

                            </a>

                        </c:forEach>

                    </div>

                </section>

            </section>

        </main>

        <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

    </body>

</html>