<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="settlement"/>

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


                    <div class="info-list">

                        <div class="info-row">

                            <strong>
                                일일 매출 :
                            </strong>

                            <span>
                                ${salesStatus.dailySales}원
                            </span>

                        </div>


                        <div class="info-row">

                            <strong>
                                판매 상품 :
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

                    <%--
                        날짜 검색 입력창에 고유 id와 label을 연결한다.
                        기존 화면 배치를 유지하기 위해 label은 시각적으로 숨기고
                        스크린 리더에는 조회 기간의 의미를 전달한다.
                    --%>
                    <label for="salesStartDate"
                           style="position:absolute;
                                  width:1px;
                                  height:1px;
                                  padding:0;
                                  margin:-1px;
                                  overflow:hidden;
                                  clip:rect(0, 0, 0, 0);
                                  white-space:nowrap;
                                  border:0;">
                        조회 시작일
                    </label>

                    <input type="date"
                           id="salesStartDate"
                           name="startDate"
                           value="${param.startDate}">

                    <span>~</span>

                    <label for="salesEndDate"
                           style="position:absolute;
                                  width:1px;
                                  height:1px;
                                  padding:0;
                                  margin:-1px;
                                  overflow:hidden;
                                  clip:rect(0, 0, 0, 0);
                                  white-space:nowrap;
                                  border:0;">
                        조회 종료일
                    </label>

                    <input type="date"
                           id="salesEndDate"
                           name="endDate"
                           value="${param.endDate}">

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
                                            ${row.amount}원
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