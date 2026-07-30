<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:set var="activeMenu" value="event"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ODITJI | 이벤트 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>

        <h1 class="page-title">
            이벤트 관리
        </h1>

        <!-- 처리 결과 메시지 -->
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success">
                <c:out value="${successMessage}"/>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error">
                <c:out value="${errorMessage}"/>
            </div>
        </c:if>

        <section class="content-panel">

            <div class="product-control-row">

                <div></div>

                <form action="${pageContext.request.contextPath}/business/event/list"
                      method="get"
                      class="product-search-form">

                    <%--
                        검색 입력창에 고유 id를 부여하고 label의 for와 연결한다.
                        label은 화면 배치에 영향을 주지 않도록 시각적으로만 숨긴다.
                    --%>
                    <label for="eventKeyword"
                           style="position:absolute;
                                  width:1px;
                                  height:1px;
                                  padding:0;
                                  margin:-1px;
                                  overflow:hidden;
                                  clip:rect(0, 0, 0, 0);
                                  white-space:nowrap;
                                  border:0;">
                        이벤트명, 상태 및 상품명 검색
                    </label>

                    <input type="text"
                           id="eventKeyword"
                           name="keyword"
                           value="<c:out value='${keyword}'/>"
                           placeholder="이벤트명, 상태, 상품명 검색">

                    <button type="submit">
                        검색
                    </button>

                </form>

                <button type="button"
                        class="product-register-btn"
                        onclick="location.href='${pageContext.request.contextPath}/business/event/register'">
                    등록 요청
                </button>

            </div>

            <table class="data-table">

                <thead>

                    <tr>
                        <th>번호</th>
                        <th>이벤트명</th>
                        <th>연결 상품</th>
                        <th>할인 적용가</th>
                        <th>이벤트 기간</th>
                        <th>상태</th>
                        <th>등록일</th>
                        <th>관리</th>
                    </tr>

                </thead>

                <tbody>

                    <c:choose>

                        <c:when test="${not empty eventList}">

                            <c:forEach var="event"
                                       items="${eventList}">

                                <tr>

                                    <td>
                                        <c:out value="${event.eventNo}"/>
                                    </td>

                                    <td>
                                        <c:out value="${event.title}"/>
                                    </td>

                                    <td>
                                        <c:out value="${event.productName}"/>
                                    </td>

                                    <td>

                                        <c:choose>

                                            <c:when test="${event.eventDiscountRate > 0 and not empty event.price}">

                                                <span class="discount-rate">
                                                    <c:out value="${event.eventDiscountRate}"/>%
                                                </span>

                                                <br>

                                                <s>
                                                    <fmt:formatNumber value="${event.price}"
                                                                      pattern="#,###"/>원
                                                </s>

                                                →

                                                <strong>
                                                    <fmt:formatNumber value="${event.discountedPrice}"
                                                                      pattern="#,###"/>원
                                                </strong>

                                            </c:when>

                                            <c:otherwise>
                                                할인 없음
                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td>
                                        <c:out value="${event.startDate}"/>
                                        ~
                                        <c:out value="${event.endDate}"/>
                                    </td>

                                    <td>

                                        <c:choose>

                                            <c:when test="${event.status eq 'APPROVED'}">

                                                <span class="status ok">
                                                    승인 완료
                                                </span>

                                            </c:when>

                                            <c:when test="${event.status eq 'WAITING'}">

                                                <span class="status waiting">
                                                    승인 대기
                                                </span>

                                            </c:when>

                                            <c:when test="${event.status eq 'REJECTED'}">

                                                <span class="status">
                                                    승인 반려
                                                </span>

                                            </c:when>

                                            <c:when test="${event.status eq 'END'}">

                                                <span class="status">
                                                    종료
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="status">
                                                    <c:out value="${event.status}"/>
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td>
                                        <fmt:formatDate value="${event.createdAt}"
                                                        pattern="yyyy-MM-dd"/>
                                    </td>

                                    <td>

                                        <!--
                                            관리자 승인이 완료된 APPROVED 이벤트만
                                            즉시 수정 또는 연장할 수 있다.
                                        -->
                                        <c:choose>

                                            <c:when test="${event.status eq 'APPROVED'}">

                                                <a class="btn btn-dark"
                                                   href="${pageContext.request.contextPath}/business/event/update?eventNo=${event.eventNo}">
                                                    수정
                                                </a>

                                                <a class="btn btn-dark"
                                                   href="${pageContext.request.contextPath}/business/event/extend?eventNo=${event.eventNo}">
                                                    연장
                                                </a>

                                            </c:when>

                                            <c:when test="${event.status eq 'WAITING'}">
                                                승인 대기 중
                                            </c:when>

                                            <c:when test="${event.status eq 'REJECTED'}">
                                                승인 반려
                                            </c:when>

                                            <c:otherwise>
                                                처리 불가
                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>
                                <td colspan="8">
                                    등록된 이벤트가 없습니다.
                                </td>
                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>

            <div class="pagination">

                <a href="#">
                    ‹
                </a>

                <c:forEach var="p"
                           begin="1"
                           end="${empty pagination.totalPages
                               ? 1
                               : pagination.totalPages}">

                    <a href="?page=${p}"
                       class="${pagination.currentPage == p
                           ? 'active'
                           : ''}">
                        ${p}
                    </a>

                </c:forEach>

                <a href="#">
                    ›
                </a>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>