<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="sales"/>

<!DOCTYPE html>
<html lang="ko">
    <head>
        <meta charset="UTF-8">
        <title>ODITJI | 판매 현황</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
    </head>
    <body>

        <jsp:include page="/WEB-INF/views/common/header.jsp"/>

        <div class="business-wrap">

            <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

            <main class="main-content">

                <a href="${pageContext.request.contextPath}/business/main" class="back-link">
                    ← 뒤로가기
                </a>


                <h1 class="page-title">
                    판매 현황
                </h1>


                <section class="content-panel">

                    <%-- 판매 현황 조회 실패 메시지를 기존 패널 안에서 표시. --%>
                    <c:if test="${not empty errorMessage}">
                        <p class="form-message error-message">
                            ${errorMessage}
                        </p>
                    </c:if>

                    <div class="info-list">

                        <div class="info-row">

                            <strong>
                                조회 기간 매출 :
                            </strong>

                            <span>
                                <fmt:formatNumber value="${salesStatus.dailySales}" pattern="#,##0"/>원
                            </span>

                        </div>


                        <div class="info-row">

                            <strong>
                                최다 판매 상품 :
                            </strong>

                            <span>
                                ${salesStatus.productName}
                            </span>

                        </div>


                        <div class="info-row">

                            <strong>
                                주문 건수 :
                            </strong>

                            <span>
                                ${salesStatus.orderCount}건
                            </span>

                        </div>

                    </div>


                <form class="search-form" method="get">

                    <input type="date"
                        name="startDate"
                        value="${startDate}">

                    <span>~</span>

                    <input type="date"
                        name="endDate"
                        value="${endDate}">

                    <button class="btn btn-primary"
                        type="submit">
                        조회
                    </button>

                </form>

                <h2 class="page-title" style="font-size:20px;">
                    상품 판매 내역
                </h2>


                <table class="data-table">

                    <thead>

                        <tr>
                            <th>날짜</th>
                            <th>판매 수량</th>
                            <th>매출액</th>
                        </tr>

                    </thead>


                    <tbody>

                        <c:choose>

                            <c:when test="${not empty salesHistory}">

                                <c:forEach var="row" items="${salesHistory}">

                                    <tr>

                                        <td>
                                            ${row.saleDate}
                                        </td>

                                        <td>
                                            ${row.quantity}
                                        </td>

                                        <td>
                                            <fmt:formatNumber value="${row.amount}" pattern="#,##0"/>원
                                        </td>

                                    </tr>

                                </c:forEach>

                            </c:when>


                            <c:otherwise>

                                <tr>
                                    <td colspan="3">
                                        판매 내역이 없습니다.
                                    </td>
                                </tr>

                            </c:otherwise>

                        </c:choose>

                    </tbody>

                </table>


                <div class="submit-stack">

                    <button class="btn btn-dark"
                    type="button"
                    onclick="history.back();">
                    닫기
                </button>

            </div>


        </section>


    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>