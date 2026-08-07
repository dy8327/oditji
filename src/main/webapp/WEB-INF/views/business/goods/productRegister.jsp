<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_header" content="${_csrf.headerName}">
<meta name="_csrf_parameter" content="${_csrf.parameterName}">

<title>ODITJI | 상품 등록 요청</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<script>
const contextPath =
        "${pageContext.request.contextPath}";

/*
 * 상품 등록 실패 후 화면으로 돌아왔을 때
 * JSONL에서 선택했던 TMDB 배우 ID를 복원합니다.
 */
const savedTmdbActorId =
        "${empty productForm.tmdbActorId
            ? ''
            : productForm.tmdbActorId}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/productRegister.js">
</script>

</head>

<body>

<jsp:include
    page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include
        page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">

        <a href="${pageContext.request.contextPath}/business/main"
           class="back-link">
            ← 뒤로가기
        </a>

        <section class="form-panel">

            <h1 class="form-title">
                상품 등록 요청
            </h1>

            <c:if test="${not empty errorMessage}">

                <div class="form-message error-message">
                    <c:out value="${errorMessage}"/>
                </div>

            </c:if>

            <%-- multipart POST는 Security Filter 단계에서 CSRF를 확인할 수 있도록 토큰을 URL 파라미터로 전달한다. --%>
            <c:url var="productRegisterAction" value="/business/product/register">
                <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
            </c:url>

            <form action="${productRegisterAction}"
                  method="post"
                  enctype="multipart/form-data"
                  onsubmit="return validateProductForm(event);">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">

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
                           placeholder="상품명을 입력하세요"
                           required>

                </div>

                <div class="form-group">

                    <label class="form-label"
                           for="productType">
                        상품 종류
                    </label>

                    <select class="form-input" id="productType" name="productType" required>

                        <option value="" disabled hidden ${productForm.productType == null ? 'selected' : ''}>
                            상품 종류를 선택하세요
                        </option>

                        <option value="CLOTHES"
                            ${productForm.productType == 'CLOTHES' ? 'selected' : ''}>
                            의상
                        </option>

                        <option value="SHOES"
                            ${productForm.productType == 'SHOES' ? 'selected' : ''}>
                            신발
                        </option>

                        <option value="PROP"
                            ${productForm.productType == 'PROP' ? 'selected' : ''}>
                            소품
                        </option>

                        <option value="GOODS"
                            ${productForm.productType == 'GOODS' ? 'selected' : ''}>
                            굿즈
                        </option>

                        <option value="OST"
                            ${productForm.productType == 'OST' ? 'selected' : ''}>
                            OST
                        </option>

                        <option value="BOOK"
                            ${productForm.productType == 'BOOK' ? 'selected' : ''}>
                            도서
                        </option>

                        <option value="FIGURE"
                            ${productForm.productType == 'FIGURE' ? 'selected' : ''}>
                            피규어
                        </option>

                        <option value="POSTER"
                            ${productForm.productType == 'POSTER' ? 'selected' : ''}>
                            포스터
                        </option>

                        <option value="ETC"
                            ${productForm.productType == 'ETC' ? 'selected' : ''}>
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
                           placeholder="가격을 입력하세요"
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
                           value="${empty productForm.discountRate
                               ? 0
                               : productForm.discountRate}"
                           placeholder="할인율을 입력하세요">

                </div>

                <%-- [상품 옵션 기능 추가] 의상/신발은 색상-사이즈 조합별 재고를 등록합니다. --%>
                <div id="productOptionSection" class="form-group" hidden>
                    <span class="form-label">색상 · 사이즈별 재고</span>
                    <div id="productOptionRows"></div>
                    <button type="button" id="addProductOptionBtn" class="option-add-btn">+ 옵션 조합 추가</button>
                    <p class="form-help">의상 예: 블랙 / M, 신발 예: 화이트 / 250. 같은 조합은 한 번만 등록하세요.</p>
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
                           placeholder="재고 수량을 입력하세요"
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

                    <%--
                        CONTENT_NO는 등록 요청 처리 중 서버가 생성하거나 조회합니다.
                        JSONL에서 선택한 TMDB 식별값을 hidden 필드로 전달합니다.
                    --%>
                    <%--
                        등록 화면에서는 CONTENT_NO를 클라이언트가 전송하지 않습니다.
                        상품 등록 처리 중 tmdbId와 contentType을 이용하여 서버에서
                        CONTENT_NO를 조회하거나 생성한 뒤 GoodsManageVO에 설정합니다.

                        name 속성을 넣으면 빈 문자열이 기본형 long인 contentNo에
                        바인딩되면서 컨트롤러 진입 전에 500 오류가 발생할 수 있으므로,
                        이 필드는 JavaScript 화면 제어용 id만 유지합니다.
                    --%>
                    <input type="hidden"
                           id="contentNo"
                           value="${productForm.contentNo > 0
                               ? productForm.contentNo
                               : ''}">

                    <input type="hidden"
                           id="tmdbId"
                           name="tmdbId"
                           value="${empty productForm.tmdbId
                               ? ''
                               : productForm.tmdbId}">

                    <input type="hidden"
                           id="contentType"
                           name="contentType"
                           value="<c:out value='${productForm.contentType}'/>">

                    <div class="input-with-btn">

                        <input class="form-input"
                               type="text"
                               id="contentTitle"
                               name="contentTitle"
                               value="<c:out value='${productForm.contentTitle}'/>"
                               placeholder="콘텐츠를 검색하세요"
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
                           for="tmdbActorId">
                        관련 배우
                    </label>

                    <select class="form-input"
                            id="tmdbActorId"
                            name="tmdbActorId"
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
                              name="description"
                              placeholder="상품 설명을 입력하세요"><c:out value="${productForm.description}"/></textarea>

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

                <%-- [수정] 대표 이미지와 선택 세부 이미지를 명확히 분리한다. --%>
                <div class="form-group">

                    <label class="form-label"
                           for="productImage">
                        상품 기본 이미지
                    </label>

                    <div class="file-box product-image-file-box">

                        <input type="file"
                               id="productImage"
                               name="productImage"
                               accept=".jpg,.jpeg,.png,.gif,.webp,image/*"
                               required>

                        <span id="productImageFileName">
                            선택된 파일 없음
                        </span>

                    </div>

                    <p class="form-help">
                        상품 목록과 상세 페이지의 대표 이미지로 사용됩니다.
                        JPG, JPEG, PNG, GIF, WEBP 형식의 10MB 이하 이미지를 등록해주세요.
                    </p>

                </div>

                <div class="form-group">

                    <div class="detail-image-label-row">
                        <%-- [SonarQube 접근성] 첫 번째 파일 입력에 id를 부여하고 라벨과 연결합니다. --%>
                        <label for="detailImage0" class="form-label detail-image-label">
                            세부 이미지 등록 (선택)
                        </label>

                        <%-- [추가] + 버튼을 누르면 독립된 파일 입력창을 한 줄씩 추가한다. --%>
                        <button type="button"
                                id="addDetailImageBtn"
                                class="detail-image-add-btn"
                                aria-label="세부 이미지 입력 추가">
                            +
                        </button>
                    </div>

                    <div id="detailImageRows" class="detail-image-rows">

                        <div class="detail-image-row">
                            <input type="file"
                                   id="detailImage0"
                                   class="detail-image-input"
                                   name="detailImages"
                                   accept=".jpg,.jpeg,.png,.gif,.webp,image/*">

                            <span class="detail-image-file-name">
                                선택된 파일 없음
                            </span>

                            <button type="button"
                                    class="detail-image-remove-btn"
                                    aria-label="세부 이미지 입력 삭제"
                                    hidden>
                                삭제
                            </button>
                        </div>

                    </div>

                    <p class="form-help">
                        필요한 만큼 + 버튼으로 입력창을 추가할 수 있으며 최대 10장까지 등록할 수 있습니다.
                    </p>

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

<jsp:include
    page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>