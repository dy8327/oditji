<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="productUpdate" scope="request"/>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>ODITJI | 상품 수정 요청</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<script>
const contextPath =
        "${pageContext.request.contextPath}";

/*
 * 기존 상품에 저장된 배우 번호 또는
 * 수정 실패 후 유지된 배우 번호
 */
const savedActorNo =
        "${empty productForm.actorNo
            ? ''
            : productForm.actorNo}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/productUpdate.js">
</script>

</head>

<body>

<jsp:include
    page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include
        page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>


        <section class="form-panel">

            <h1 class="form-title">
                상품 수정 요청
            </h1>


            <c:if test="${not empty errorMessage}">

                <div class="form-message error-message">
                    <c:out value="${errorMessage}"/>
                </div>

            </c:if>


            <form action="${pageContext.request.contextPath}/business/product/update"
                  method="post"
                  enctype="multipart/form-data"
                  onsubmit="return validateProductForm(event);">


                <input type="hidden"
                       name="productNo"
                       value="${productForm.productNo}">


                <input type="hidden"
                       id="existingImagePath"
                       name="imagePath"
                       value="<c:out value='${productForm.imagePath}'/>">


                <div class="form-group">

                    <label class="form-label"
                           for="productName">
                        상품명
                    </label>

                    <input class="form-input"
                           type="text"
                           id="productName"
                           name="productName"
                           maxlength="200"
                           value="<c:out value='${productForm.productName}'/>"
                           required>

                </div>


                <div class="form-group">

                    <label class="form-label"
                           for="productType">
                        상품 종류
                    </label>

                    <select class="form-input"
                            id="productType"
                            name="productType"
                            required>

                        <option value="">
                            상품 종류를 선택하세요
                        </option>

                        <option value="CLOTHES"
                            ${productForm.productType == 'CLOTHES'
                                ? 'selected' : ''}>
                            의상
                        </option>

                        <option value="PROP"
                            ${productForm.productType == 'PROP'
                                ? 'selected' : ''}>
                            소품
                        </option>

                        <option value="GOODS"
                            ${productForm.productType == 'GOODS'
                                ? 'selected' : ''}>
                            굿즈
                        </option>

                        <option value="OST"
                            ${productForm.productType == 'OST'
                                ? 'selected' : ''}>
                            OST
                        </option>

                        <option value="BOOK"
                            ${productForm.productType == 'BOOK'
                                ? 'selected' : ''}>
                            도서
                        </option>

                        <option value="FIGURE"
                            ${productForm.productType == 'FIGURE'
                                ? 'selected' : ''}>
                            피규어
                        </option>

                        <option value="ETC"
                            ${productForm.productType == 'ETC'
                                ? 'selected' : ''}>
                            기타
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label class="form-label"
                           for="price">
                        가격
                    </label>

                    <input class="form-input"
                           type="number"
                           id="price"
                           name="price"
                           min="1"
                           value="${productForm.price}"
                           required>

                </div>


                <div class="form-group">

                    <label class="form-label"
                           for="discountRate">
                        할인율
                    </label>

                    <input class="form-input"
                           type="number"
                           id="discountRate"
                           name="discountRate"
                           min="0"
                           max="100"
                           value="${productForm.discountRate}">

                </div>


                <div class="form-group">

                    <label class="form-label"
                           for="stock">
                        재고
                    </label>

                    <input class="form-input"
                           type="number"
                           id="stock"
                           name="stock"
                           min="0"
                           value="${productForm.stock}"
                           required>

                </div>


                <div class="form-group">

                    <%--
                        관련 콘텐츠 설명을 실제 화면에 표시되는 읽기 전용
                        콘텐츠명 입력창과 연결하여 접근 가능한 이름을 제공한다.
                    --%>
                    <label class="form-label"
                           for="contentTitle">
                        관련 콘텐츠
                    </label>


                    <input type="hidden"
                           id="contentNo"
                           name="contentNo"
                           value="${productForm.contentNo > 0
                               ? productForm.contentNo
                               : ''}">


                    <div class="input-with-btn">

                        <input class="form-input"
                               type="text"
                               id="contentTitle"
                               name="contentTitle"
                               value="<c:out value='${productForm.contentTitle}'/>"
                               readonly>


                        <button class="btn btn-dark"
                                type="button"
                                onclick="openContentSearch();">
                            콘텐츠 검색
                        </button>

                    </div>

                </div>


                <div class="form-group">

                    <label class="form-label"
                           for="actorNo">
                        관련 배우
                    </label>


                    <select class="form-input"
                            id="actorNo"
                            name="actorNo"
                            disabled>

                        <option value="">
                            콘텐츠를 먼저 선택해주세요.
                        </option>

                    </select>


                    <p class="form-help"
                       id="actorLoadMessage">

                        콘텐츠를 선택하면 해당 작품에 연결된
                        배우가 표시됩니다.

                    </p>

                </div>


                <div class="form-group">

                    <label class="form-label"
                           for="description">
                        상품 설명
                    </label>


                    <textarea class="form-textarea"
                              id="description"
                              name="description"><c:out value="${productForm.description}"/></textarea>

                </div>


                <div class="form-group">

                    <%--
                        사업자명 입력창에 고유 id를 부여하고 label과 연결한다.
                        읽기 전용 필드도 보조 기술에서 의미를 알 수 있어야 한다.
                    --%>
                    <label class="form-label"
                           for="businessName">
                        사업자명
                    </label>


                    <input class="form-input"
                           type="text"
                           id="businessName"
                           value="<c:out value='${business.businessName}'/>"
                           readonly>

                </div>


                <div class="form-group">

                    <label class="form-label"
                           for="productImage">
                        상품 이미지
                    </label>


                    <div class="file-box">

                        <input type="file"
                               id="productImage"
                               name="productImage"
                               accept=".jpg,.jpeg,.png,.gif,.webp,image/*"
                               onchange="updateFileName(this);">


                        <span id="selectedFileName">

                            <c:choose>

                                <c:when test="${not empty productForm.imagePath}">
                                    <c:out value="${productForm.imagePath}"/>
                                </c:when>

                                <c:otherwise>
                                    선택된 파일 없음
                                </c:otherwise>

                            </c:choose>

                        </span>

                    </div>


                    <p class="form-help">

                        새 이미지를 선택하지 않으면 기존 이미지가
                        그대로 유지됩니다.

                    </p>

                </div>


                <div class="submit-stack">


                    <button class="btn btn-primary"
                            type="submit">
                        상품 수정 요청
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

<jsp:include
    page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>