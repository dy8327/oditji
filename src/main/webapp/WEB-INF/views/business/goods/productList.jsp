<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="product"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <div class="business-page-header">

            <a href="${pageContext.request.contextPath}/business/goodsManage"
               class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="business-page-title">
                상품 관리
            </h1>

            <p class="business-page-desc">
                등록한 상품을 조회하고 등록, 수정, 삭제, 판매 현황, 승인 상태를 확인할 수 있습니다.
            </p>

        </div>


        <section class="business-content-box">

            <div class="product-control-row">

                <div></div>

                <form action="${pageContext.request.contextPath}/business/product/list"
                      method="get"
                      class="product-search-form">

                    <input type="text"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="상품명, 관련 콘텐츠, 배우명 검색">

                    <button type="submit">
                        검색
                    </button>

                </form>

                <button type="button"
                        class="product-register-btn"
                        onclick="location.href='${pageContext.request.contextPath}/business/product/register'">
                    등록 요청
                </button>

            </div>

            <div class="product-list-frame">

                <c:choose>

                    <c:when test="${not empty productList}">

                        <c:forEach var="product" items="${productList}">

                            <div class="product-item">

                                <div class="product-thumb">

                                    <c:choose>

                                        <c:when test="${not empty product.thumbnailPath}">

                                            <img src="${product.thumbnailPath}"
                                                 alt="${product.productName}">

                                        </c:when>

                                        <c:otherwise>
                                            상품
                                        </c:otherwise>

                                    </c:choose>

                                </div>


                                <div class="product-info">

                                    <div class="product-type">
                                        ${product.productType}
                                    </div>

                                    <div class="product-name">
                                        ${product.productName}
                                    </div>

                                    <div class="product-meta">

                                        <span>
                                            관련 콘텐츠: ${product.contentTitle}
                                        </span>

                                        <span>
                                            수량: ${product.stock}개
                                        </span>

                                        <span>
                                            가격: ${product.price}원
                                        </span>

                                    </div>


                                    <div class="product-status-text">

                                        승인 상태:

                                        <c:choose>

                                            <c:when test="${product.status == 'APPROVED'}">

                                                <span class="status-ok">
                                                    승인 완료
                                                </span>

                                            </c:when>

                                            <c:when test="${product.status == 'WAITING'}">

                                                <span class="status-waiting">
                                                    승인 대기
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="status-reject">
                                                    반려
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                </div>


                                <div class="product-actions">

                                    <button type="button"
                                            class="product-action-btn"
                                            onclick="location.href='${pageContext.request.contextPath}/business/product/update?productNo=${product.productNo}'">
                                        수정 요청
                                    </button>

                                    <button type="button"
                                            class="product-action-btn delete"
                                            onclick="location.href='${pageContext.request.contextPath}/business/product/delete?productNo=${product.productNo}'">
                                        삭제 요청
                                    </button>

                                    <button type="button"
                                            class="product-action-btn"
                                            onclick="location.href='${pageContext.request.contextPath}/business/sales/status?productNo=${product.productNo}'">
                                        판매 현황
                                    </button>

                                    <button type="button"
                                            class="product-action-btn"
                                            onclick="location.href='${pageContext.request.contextPath}/business/approval/product-detail?productNo=${product.productNo}'">
                                        승인 상태
                                    </button>

                                </div>

                            </div>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>

                        <div class="product-item">
                            등록된 상품이 없습니다.
                        </div>

                    </c:otherwise>

                </c:choose>


                <div class="pagination">

                    <a href="#">
                        &lt;
                    </a>

                    <c:forEach var="p"
                               begin="1"
                               end="${empty pagination.totalPages ? 1 : pagination.totalPages}">

                        <a href="?page=${p}"
                           class="${pagination.currentPage == p ? 'active' : ''}">
                            ${p}
                        </a>

                    </c:forEach>

                    <a href="#">
                        &gt;
                    </a>

                </div>

            </div>

        </section>

    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>