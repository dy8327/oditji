<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<section class="recommend-sidebar">

    <div class="recommend-sidebar-header">

        <h2>
            추천 상품
        </h2>

    </div>

    <c:choose>

        <c:when test="${empty recommendedGoodsList}">

            <div class="recommend-empty">

                <p>
                    추천 상품이 없습니다.
                </p>

            </div>

        </c:when>

        <c:otherwise>

            <div class="recommend-list">

                <c:forEach var="recommend"
                           items="${recommendedGoodsList}"
                           begin="0"
                           end="4">

                    <c:url var="recommendDetailUrl"
                           value="/goods/goodsDetail/${recommend.productNo}"/>

                    <a class="recommend-item"
                       href="${recommendDetailUrl}">

                        <div class="recommend-poster">

                            <c:choose>

                                <c:when test="${not empty recommend.mainImage}">

                                    <img src="${pageContext.request.contextPath}${recommend.mainImage}"
                                                alt="<c:out value='${recommend.productName}'/>"
                                                loading="lazy">

                                </c:when>

                                <c:otherwise>

                                    <div class="recommend-no-image">
                                        NO IMAGE
                                    </div>

                                </c:otherwise>

                            </c:choose>

                        </div>

                        <div class="recommend-info">

                            <strong class="recommend-title">
                                ${recommend.productName}
                            </strong>

                            <span class="recommend-type">
                                ${recommend.businessName}
                            </span>

                            <span class="recommend-price">

                                <c:choose>

                                    <%-- 할인 상품: 소수점 없이 천 단위 쉼표 적용 --%>
                                    <c:when test="${recommend.discountRate > 0}">
                                        ₩ <fmt:formatNumber
                                            value="${recommend.price - (recommend.price * recommend.discountRate / 100)}"
                                            pattern="#,##0"/>
                                    </c:when>

                                    <%-- 일반 상품: 소수점 없이 천 단위 쉼표 적용 --%>
                                    <c:otherwise>
                                        ₩ <fmt:formatNumber
                                            value="${recommend.price}"
                                            pattern="#,##0"/>
                                    </c:otherwise>

                                </c:choose>

                            </span>

                        </div>

                    </a>

                </c:forEach>

            </div>

        </c:otherwise>

    </c:choose>

</section>
