<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="product"/>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">

<title>ODITJI | 상품 관리</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

</head>

<body>

<jsp:include
    page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include
        page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <div class="business-page-header">

            <a href="${pageContext.request.contextPath}/business/main"
               class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="business-page-title">
                상품 관리
            </h1>

            <p class="business-page-desc">
                등록한 상품을 조회하고 등록, 수정, 삭제,
                판매 현황, 승인 상태를 확인할 수 있습니다.
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
                           value="<c:out value='${param.keyword}'/>"
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


            <c:if test="${not empty successMessage}">

                <div class="form-message success-message">
                    <c:out value="${successMessage}"/>
                </div>

            </c:if>


            <c:if test="${not empty errorMessage}">

                <div class="form-message error-message">
                    <c:out value="${errorMessage}"/>
                </div>

            </c:if>


            <div class="product-list-frame">

                <c:choose>

                    <c:when test="${not empty productList}">

                        <c:forEach var="product"
                                   items="${productList}">

                            <div class="product-item">

                                <div class="product-thumb">

                                    <c:choose>

                                        <%--
                                            Mapper의 IMAGE_PATH 별칭은
                                            GoodsManageVO.imagePath에 매핑된다.

                                            기존 product.thumbnailPath는
                                            GoodsManageVO에 존재하지 않으므로
                                            사용하면 PropertyNotFoundException이 발생한다.
                                        --%>
                                        <c:when test="${not empty product.imagePath}">

                                            <img src="${pageContext.request.contextPath}${product.imagePath}"
                                                 alt="<c:out value='${product.productName}'/>">

                                        </c:when>

                                        <c:otherwise>

                                            <span>
                                                상품
                                            </span>

                                        </c:otherwise>

                                    </c:choose>

                                </div>


                                <div class="product-info">

                                    <div class="product-header">

                                        <div class="product-name">
                                            <c:out value="${product.productName}"/>
                                        </div>

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

                                            <c:when test="${product.status == 'REJECTED'}">

                                                <span class="status-reject">
                                                    반려
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="status-reject">
                                                    <c:out value="${product.status}"/>
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </div>


                                    <div class="product-type">

                                        <c:choose>

                                            <c:when test="${product.productType == 'CLOTHES'}">
                                                의상
                                            </c:when>

                                            <c:when test="${product.productType == 'PROP'}">
                                                소품
                                            </c:when>

                                            <c:when test="${product.productType == 'GOODS'}">
                                                굿즈
                                            </c:when>

                                            <c:when test="${product.productType == 'OST'}">
                                                OST
                                            </c:when>

                                            <c:when test="${product.productType == 'BOOK'}">
                                                도서
                                            </c:when>

                                            <c:when test="${product.productType == 'FIGURE'}">
                                                피규어
                                            </c:when>

                                            <c:otherwise>
                                                기타
                                            </c:otherwise>

                                        </c:choose>

                                    </div>


                                    <div class="product-meta">

                                        <div class="meta-row">
                                            관련 콘텐츠 :
                                            <c:out value="${product.contentTitle}"/>
                                        </div>

                                        <c:if test="${not empty product.actorName}">

                                            <div class="meta-row">
                                                관련 배우 :
                                                <c:out value="${product.actorName}"/>
                                            </div>

                                        </c:if>

                                        <div class="meta-row">

                                            <span>
                                                수량 :
                                                <c:out value="${product.stock}"/>개
                                            </span>

                                            <span>
                                                가격 :
                                                <c:out value="${product.price}"/>원
                                            </span>

                                        </div>

                                        <c:if test="${product.discountRate > 0}">

                                            <div class="meta-row">
                                                할인율 :
                                                <c:out value="${product.discountRate}"/>%
                                            </div>

                                        </c:if>

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
                                                onclick="location.href='${pageContext.request.contextPath}/business/settlement/sales?productNo=${product.productNo}'">
                                            판매 현황
                                        </button>

                                    </div>

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


                <c:if test="${not empty pagination
                            and pagination.totalPages > 0}">

                    <div class="pagination">

                        <c:choose>

                            <c:when test="${pagination.currentPage > 1}">

                                <a href="?page=${pagination.currentPage - 1}&keyword=${param.keyword}">
                                    &lt;
                                </a>

                            </c:when>

                            <c:otherwise>

                                <a href="#"
                                   onclick="return false;">
                                    &lt;
                                </a>

                            </c:otherwise>

                        </c:choose>


                        <c:forEach var="p"
                                   begin="1"
                                   end="${pagination.totalPages}">

                            <a href="?page=${p}&keyword=${param.keyword}"
                               class="${pagination.currentPage == p
                                   ? 'active'
                                   : ''}">

                                <c:out value="${p}"/>

                            </a>

                        </c:forEach>


                        <c:choose>

                            <c:when test="${pagination.currentPage
                                            < pagination.totalPages}">

                                <a href="?page=${pagination.currentPage + 1}&keyword=${param.keyword}">
                                    &gt;
                                </a>

                            </c:when>

                            <c:otherwise>

                                <a href="#"
                                   onclick="return false;">
                                    &gt;
                                </a>

                            </c:otherwise>

                        </c:choose>

                    </div>

                </c:if>

            </div>

        </section>

    </main>

</div>

<jsp:include
    page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>