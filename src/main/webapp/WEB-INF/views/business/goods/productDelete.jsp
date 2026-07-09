<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="product"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 삭제 요청</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/business.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/productDelete.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <a href="${pageContext.request.contextPath}/business/product/list"
           class="back-link">
            ← 뒤로가기
        </a>


        <section class="form-panel">

            <h1 class="form-title">
                상품 삭제 요청
            </h1>


            <div class="delete-summary-box">

                <div class="delete-summary-thumb">

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


                <div class="delete-summary-info">

                    <div class="delete-summary-type">
                        ${product.productType}
                    </div>

                    <div class="delete-summary-name">
                        ${product.productName}
                    </div>

                    <div class="delete-summary-meta">

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

                    <div class="delete-summary-status">

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

            </div>


            <div class="delete-warning-box">

                <p class="delete-warning-title">
                    ⚠ 삭제 요청 시 유의사항
                </p>

                <ul class="delete-warning-list">

                    <li>
                        삭제 요청은 관리자 승인 후 최종 반영됩니다.
                    </li>

                    <li>
                        삭제가 승인되면 해당 상품은 목록에서 완전히 제거되며 복구할 수 없습니다.
                    </li>

                    <li>
                        이미 주문이 진행 중인 상품은 삭제 요청이 제한될 수 있습니다.
                    </li>

                </ul>

            </div>


            <form action="${pageContext.request.contextPath}/business/product/delete"
                  method="post">

                <input type="hidden"
                       name="productNo"
                       value="${product.productNo}">


                <div class="form-group">

                    <label class="form-label">
                        삭제 사유
                    </label>

                    <textarea class="form-textarea"
                              name="reason"
                              placeholder="삭제 요청 사유를 입력하세요"
                              required></textarea>

                </div>


                <div class="submit-stack">

                    <button class="btn btn-danger"
                            type="submit"
                            onclick="return confirm('정말 이 상품의 삭제를 요청하시겠습니까?');">
                        삭제 요청
                    </button>


                    <a class="btn btn-dark"
                       href="${pageContext.request.contextPath}/business/product/list">
                        취소
                    </a>

                </div>


            </form>


        </section>


    </main>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>
