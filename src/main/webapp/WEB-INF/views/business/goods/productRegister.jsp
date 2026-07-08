<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="product"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 등록 요청</title>
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


        <section class="form-panel">

            <h1 class="form-title">
                상품 등록 요청
            </h1>


            <form action="${pageContext.request.contextPath}/business/product/register"
                  method="post"
                  enctype="multipart/form-data">


                <div class="form-group">

                    <label class="form-label">
                        상품명
                    </label>

                    <input class="form-input"
                           type="text"
                           name="productName"
                           placeholder="상품명을 입력하세요">

                </div>


                <div class="form-group">

                    <label class="form-label">
                        가격
                    </label>

                    <input class="form-input"
                           type="number"
                           name="price"
                           placeholder="가격을 입력하세요">

                </div>


                <div class="form-group">

                    <label class="form-label">
                        재고
                    </label>

                    <input class="form-input"
                           type="number"
                           name="stock"
                           placeholder="재고 수량을 입력하세요">

                </div>


                <div class="form-group">

                    <label class="form-label">
                        관련 콘텐츠
                    </label>

                    <div class="input-with-btn">

                        <input class="form-input"
                               type="text"
                               name="contentTitle"
                               placeholder="콘텐츠를 검색하세요"
                               readonly>

                        <button class="btn btn-dark"
                                type="button">
                            콘텐츠 검색
                        </button>

                    </div>

                </div>


                <div class="form-group">

                    <label class="form-label">
                        상품 설명
                    </label>

                    <textarea class="form-textarea"
                              name="description"
                              placeholder="상품 설명을 입력하세요"></textarea>

                </div>


                <div class="form-group">

                    <label class="form-label">
                        사업자명
                    </label>

                    <input class="form-input"
                           type="text"
                           name="businessName"
                           value="${businessMain.business.businessName}"
                           readonly>

                </div>


                <div class="form-group">

                    <label class="form-label">
                        상품 이미지
                    </label>

                    <div class="file-box">

                        <input type="file"
                               name="productImage">

                        <span>
                            선택된 파일 없음
                        </span>

                    </div>

                </div>


                <div class="submit-stack">

                    <button class="btn btn-primary"
                            type="submit">
                        상품 등록 요청
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