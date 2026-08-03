<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="sales"/>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>
        ODITJI | 판매 현황
    </title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/business.css">
</head>

<body>

    <%-- 전체 화면에서 공통으로 사용하는 상단 헤더를 불러온다. --%>
    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <div class="business-wrap">

        <%-- 사업자 전용 메뉴 사이드바를 불러온다. --%>
        <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

        <main id="mainContent" class="main-content">

            <a href="${pageContext.request.contextPath}/business/main"
               class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="page-title">
                판매 현황
            </h1>

            <section class="content-panel">

                <%--
                    판매 현황을 조회하는 과정에서 오류가 발생한 경우
                    컨트롤러에서 전달된 오류 메시지를 화면에 표시한다.
                --%>
                <c:if test="${not empty errorMessage}">
                    <p class="form-message error-message">
                        ${errorMessage}
                    </p>
                </c:if>

                <%-- 선택한 조회 기간의 판매 요약 정보를 표시한다. --%>
                <div class="info-list">

                    <div class="info-row">
                        <strong>
                            조회 기간 매출 :
                        </strong>

                        <span>
                            <fmt:formatNumber
                                    value="${salesStatus.dailySales}"
                                    pattern="#,##0"/>원
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

                <%--
                    조회 시작일과 종료일을 GET 방식으로 전달한다.

                    id 속성은 label의 for 속성과 연결하여
                    날짜 입력 필드의 접근성을 보장한다.

                    value에는 컨트롤러가 전달한 startDate와 endDate를 사용하여
                    조회 후에도 선택한 날짜가 입력창에 유지되도록 한다.
                --%>
                <form class="search-form"
                      method="get">

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
                           value="${startDate}">

                    <span>
                        ~
                    </span>

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
                           value="${endDate}">

                    <button class="btn btn-primary"
                            type="submit">
                        조회
                    </button>

                </form>

                <h2 class="page-title"
                    style="font-size:20px;">
                    상품 판매 내역
                </h2>

                <%-- 조회 기간을 날짜별로 집계한 판매 내역을 표시한다. --%>
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

                            <%-- 판매 내역이 존재하는 경우 날짜별 집계 결과를 출력한다. --%>
                            <c:when test="${not empty salesHistory}">

                                <c:forEach var="row"
                                           items="${salesHistory}">

                                    <tr>
                                        <td>
                                            ${row.saleDate}
                                        </td>

                                        <td>
                                            ${row.quantity}
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                    value="${row.amount}"
                                                    pattern="#,##0"/>원
                                        </td>
                                    </tr>

                                </c:forEach>

                            </c:when>

                            <%-- 선택한 기간에 판매 내역이 없는 경우 안내 문구를 표시한다. --%>
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

            </section>

        </main>

    </div>

    <%-- 전체 화면에서 공통으로 사용하는 하단 푸터를 불러온다. --%>
    <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>