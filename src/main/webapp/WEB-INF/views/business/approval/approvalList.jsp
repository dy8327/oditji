<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="approval"/>
<c:set var="currentType" value="${empty param.type ? 'product' : param.type}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>ODITJI | 승인 상태 통합 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>
<body>

    <jsp:include page="/WEB-INF/views/common/header.jsp"/>

    <div class="business-wrap">

        <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

        <main class="main-content">

            <div class="business-page-header">
                <a href="${pageContext.request.contextPath}/business/main" class="back-link">
                    ← 뒤로가기
                </a>
                <h1 class="business-page-title">
                    승인 상태
                </h1>
                <p class="business-page-desc">
                    ${currentType eq 'product' ? '상품 등록 및 수정 요청 승인 진행 상태를 확인합니다.' : '이벤트 진행 요청 승인 상태를 확인합니다.'}
                </p>
            </div>

            <section class="business-content-box">

                <nav class="tab-menu">
                    <a href="?type=product" class="${currentType eq 'product' ? 'active' : ''}">상품</a>
                    <a href="?type=event" class="${currentType eq 'event' ? 'active' : ''}">이벤트</a>
                </nav>

                <div class="toolbar">
                    <form method="get" action="${pageContext.request.contextPath}/business/approval">
                        <input type="hidden" name="type" value="${currentType}">
                        <input type="text" class="page-search" name="keyword" value="${param.keyword}" placeholder="${currentType eq 'product' ? '상품명 검색' : '이벤트명 검색'}">
                        <button type="submit" class="btn btn-dark">검색</button>
                    </form>
                </div>

                <div class="list-container">
                    <c:choose>
                        <%-- 상품 목록 (카드형) --%>
                        <c:when test="${currentType eq 'product'}">
                            <div class="card-list">
                                <c:choose>
                                    <c:when test="${not empty approvalList}">
                                        <c:forEach var="item" items="${approvalList}">
                                            <article class="item-card">
                                                <div class="thumb">${item.productType}</div>
                                                <div class="item-info">
                                                    <h3>${item.productName}</h3>
                                                    <div class="meta">
                                                        <span>관련 콘텐츠 : ${item.contentTitle}</span>
                                                        <span>수량 : ${item.stock}개</span>
                                                        <span>가격 : ${item.price}원</span>
                                                        <span class="request-type">요청 : ${item.requestType}</span>
                                                    </div>
                                                </div>
                                                <div class="item-actions">
                                                    <a class="btn btn-dark" href="product-detail?productNo=${item.productNo}">상세보기</a>
                                                    <form action="${pageContext.request.contextPath}/business/approval/product-cancel" method="post">
                                                        <input type="hidden" name="productNo" value="${item.productNo}">
                                                        <button type="submit" class="btn btn-danger">요청 취소</button>
                                                    </form>
                                                    <a class="btn btn-outline" href="product-detail?productNo=${item.productNo}">승인 상태</a>
                                                </div>
                                            </article>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <article class="item-card empty">승인 요청 내역이 없습니다.</article>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </c:when>

                        <%-- 이벤트 목록 (테이블형) --%>
                        <c:otherwise>
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>번호</th>
                                        <th>이벤트명</th>
                                        <th>이벤트 기간</th>
                                        <th>상태</th>
                                        <th>등록일</th>
                                        <th>요청 유형</th>
                                        <th>관리</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty approvalList}">
                                            <c:forEach var="event" items="${approvalList}">
                                                <tr>
                                                    <td>${event.eventNo}</td>
                                                    <td>${event.title}</td>
                                                    <td>${event.startDate} ~ ${event.endDate}</td>
                                                    <td><span class="status waiting">승인 대기</span></td>
                                                    <td>${event.createdAt}</td>
                                                    <td>${event.requestType}</td>
                                                    <td>
                                                        <a class="btn btn-dark" href="event-detail?eventNo=${event.eventNo}">상세보기</a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr><td colspan="7">승인 요청 내역이 없습니다.</td></tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="pagination">
                    <a href="?type=${currentType}&page=${pagination.currentPage-1}&keyword=${param.keyword}">‹</a>
                    <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                        <a href="?type=${currentType}&page=${p}&keyword=${param.keyword}" class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                    </c:forEach>
                    <a href="?type=${currentType}&page=${pagination.currentPage+1}&keyword=${param.keyword}">›</a>
                </div>

            </section>

        </main>
    </div>

    <jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>