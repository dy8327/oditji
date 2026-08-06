<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="activeMenu" value="product"/>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>ODITJI | 상품 관리</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<%--
    [리팩터링 추가] 상품 수정 모달(콘텐츠 검색 팝업 연동, 배우 목록 AJAX 조회)이
    사용하는 contextPath 전역 변수. business.js보다 먼저 실행되어야 하므로
    defer 없이 둔다. (구 productUpdate.jsp에서 쓰던 것과 동일한 선언 방식)
--%>
<script>
const contextPath =
        "${pageContext.request.contextPath}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/business.js">
</script>

</head>

<body>

<jsp:include
    page="/WEB-INF/views/common/header.jsp"/>

<div class="business-wrap">

    <jsp:include
        page="/WEB-INF/views/common/businessSidebar.jsp"/>

    <main id="mainContent" class="main-content">

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

                    <%-- 상품 검색 input에 고유 id를 부여하고 숨김 label과 연결한다. --%>
                    <label for="businessProductKeyword"
                           style="position:absolute;width:1px;height:1px;padding:0;margin:-1px;overflow:hidden;clip:rect(0, 0, 0, 0);white-space:nowrap;border:0;">
                        상품명, 관련 콘텐츠 및 배우명 검색
                    </label>

                    <input type="text"
                           id="businessProductKeyword"
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

                                            <c:when test="${product.productType == 'SHOES'}">
                                                신발
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

                                            <c:when test="${product.productType == 'POSTER'}">
                                                포스터
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

                                        <%--
                                            [리팩터링] 페이지 이동 대신 공용 수정 모달(#productUpdateModal)을
                                            연다. 상품마다 모달을 새로 만들지 않고, 이미 화면에 내려온 이
                                            product 값을 data-* 속성으로 실어 openProductUpdateModal(this)가
                                            JS로 폼에 채워 넣는다(관리자 상세보기 팝업과 같은 방식, 다만 이
                                            폼은 읽기 전용이 아니라 그대로 수정 가능한 입력 폼이다).
                                        --%>
                                        <button type="button"
                                                class="product-action-btn"
                                                data-product-no="${product.productNo}"
                                                data-product-name="${fn:escapeXml(product.productName)}"
                                                data-product-type="${product.productType}"
                                                data-price="${product.price}"
                                                data-discount-rate="${product.discountRate}"
                                                data-stock="${product.stock}"
                                                data-content-no="${product.contentNo}"
                                                data-content-title="${fn:escapeXml(product.contentTitle)}"
                                                data-actor-no="${product.actorNo}"
                                                data-description="${fn:escapeXml(product.description)}"
                                                data-image-path="${fn:escapeXml(product.imagePath)}"
                                                onclick="openProductUpdateModal(this)">
                                            수정 요청
                                        </button>

                                        <%-- [리팩터링] 페이지 이동 대신 이 상품 전용 삭제 모달을 연다. --%>
                                        <button type="button"
                                                class="product-action-btn delete"
                                                onclick="openModal('productDeleteModal_${product.productNo}')">
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
                            and pagination.totalPage > 0}">

                    <div class="pagination">

                        <!-- 이전 블록 -->
                        <a href="?page=${pagination.startPage - 1}&keyword=${param.keyword}"
                           class="${!pagination.prev ? 'disabled' : ''}">
                            &laquo;
                        </a>

                        <!-- 이전 페이지 -->
                        <a href="?page=${pagination.currentPage - 1}&keyword=${param.keyword}"
                           class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                            &lsaquo;
                        </a>

                        <!-- 페이지 번호 -->
                        <c:forEach var="p"
                                   begin="${pagination.startPage}"
                                   end="${pagination.endPage}">

                            <a href="?page=${p}&keyword=${param.keyword}"
                               class="${pagination.currentPage == p
                                   ? 'active'
                                   : ''}">

                                <c:out value="${p}"/>

                            </a>

                        </c:forEach>

                        <!-- 다음 페이지 -->
                        <a href="?page=${pagination.currentPage + 1}&keyword=${param.keyword}"
                           class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                            &rsaquo;
                        </a>

                        <!-- 다음 블록 -->
                        <a href="?page=${pagination.endPage + 1}&keyword=${param.keyword}"
                           class="${!pagination.next ? 'disabled' : ''}">
                            &raquo;
                        </a>

                    </div>

                </c:if>

            </div>

        </section>

        <%--
            [상품 옵션 기능 추가] 상품별 색상-사이즈 옵션 데이터 (숨김 template)

            공용 수정 모달(#productUpdateModal)은 상품마다 복제되지 않으므로,
            값이 하나뿐인 필드는 "수정 요청" 버튼의 data-* 값으로 충분하지만,
            옵션은 상품마다 조합 개수가 다르다. 그래서 이벤트의 연결 상품과
            동일한 방식으로 <template> 안에 c:out으로 이스케이프한 data-*
            속성을 가진 요소로 미리 렌더링해 둔다. "수정 요청" 버튼을 누르면
            openProductUpdateModal(this)가 같은 productNo의 template을 찾아
            그 안의 데이터로 옵션 입력 행을 다시 그린다.
        --%>
        <c:forEach var="product" items="${productList}">

            <c:if test="${product.productType == 'CLOTHES' or product.productType == 'SHOES'}">

                <template id="productOptionData_${product.productNo}">

                    <c:forEach var="option" items="${product.optionList}">

                        <div class="product-option-data"
                             data-color-name="<c:out value='${option.colorName}'/>"
                             data-size-name="<c:out value='${option.sizeName}'/>"
                             data-stock="<c:out value='${option.stock}'/>">
                        </div>

                    </c:forEach>

                </template>

            </c:if>

        </c:forEach>

        <%--
            [리팩터링 추가] 상품 수정 요청 모달 (공용 1개)

            페이지 이동 방식이던 구 productUpdate.jsp를 없애고, 그 폼을
            상품 목록 전체가 공유하는 모달 1개로 통합했다. 상품마다 모달을
            복제하지 않는 이유는, 콘텐츠 검색 팝업 연동/배우 목록 AJAX 조회/
            이미지 파일 선택 같은 JS 로직(구 productUpdate.js, 지금은
            business.js로 흡수)이 고정된 element id(productName, contentNo,
            actorNo 등)를 기준으로 동작하기 때문이다. 상품마다 모달을 복제하면
            id가 중복되어 동작하지 않는다.

            "수정 요청" 버튼을 누르면 openProductUpdateModal(this)가 버튼의
            data-* 값을 이 폼에 채워 넣고 모달을 연다. 폼 action/파라미터는
            기존 컨트롤러(POST /business/product/update)를 그대로 재사용한다.
            성공/실패 모두 이 목록(/business/product/list)으로 리다이렉트되며
            (구 GET /business/product/update 페이지는 컨트롤러에서도 제거함),
            위쪽 알림(.form-message)으로 결과가 노출된다.
        --%>
        <div class="modal-overlay" id="productUpdateModal">

            <div class="modal-box">

                <div class="modal-header">

                    <h3>상품 수정 요청</h3>

                    <button type="button"
                            class="modal-close"
                            onclick="closeModal('productUpdateModal')"
                            aria-label="닫기">
                        &times;
                    </button>

                </div>

                <%-- multipart POST는 Security Filter 단계에서 CSRF를 확인할 수 있도록 토큰을 URL 파라미터로 전달한다. --%>
                <c:url var="productUpdateAction" value="/business/product/update">
                    <c:param name="${_csrf.parameterName}" value="${_csrf.token}"/>
                </c:url>

                <form action="${productUpdateAction}"
                      method="post"
                      enctype="multipart/form-data"
                      onsubmit="return validateProductForm(event);">

                    <input type="hidden"
                           id="updateProductNo"
                           name="productNo"
                           value="">

                    <input type="hidden"
                           id="existingImagePath"
                           name="imagePath"
                           value="">

                    <div class="form-group">

                        <label class="form-label" for="productName">상품명</label>

                        <input class="form-input"
                               type="text"
                               id="productName"
                               name="productName"
                               maxlength="200"
                               required>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="productType">상품 종류</label>

                        <select class="form-input"
                                id="productType"
                                name="productType"
                                required>

                            <option value="" disabled hidden>상품 종류를 선택하세요</option>
                            <option value="CLOTHES">의상</option>
                            <option value="SHOES">신발</option>
                            <option value="PROP">소품</option>
                            <option value="GOODS">굿즈</option>
                            <option value="OST">OST</option>
                            <option value="BOOK">도서</option>
                            <option value="FIGURE">피규어</option>
                            <option value="POSTER">포스터</option>
                            <option value="ETC">기타</option>

                        </select>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="price">가격</label>

                        <input class="form-input"
                               type="number"
                               id="price"
                               name="price"
                               min="1"
                               required>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="discountRate">할인율</label>

                        <input class="form-input"
                               type="number"
                               id="discountRate"
                               name="discountRate"
                               min="0"
                               max="100">

                    </div>

                    <%-- [상품 옵션 기능 추가] 의상/신발은 색상-사이즈 조합별 재고를 수정합니다. --%>
                    <div id="productOptionSection" class="form-group" hidden>
                        <span class="form-label">색상 · 사이즈별 재고</span>
                        <div id="productOptionRows"></div>
                        <button type="button" id="addProductOptionBtn" class="option-add-btn">+ 옵션 조합 추가</button>
                        <p class="form-help">의상 예: 블랙 / M, 신발 예: 화이트 / 250. 같은 조합은 한 번만 등록하세요.</p>
                    </div>

                    <div class="form-group">

                        <label class="form-label" for="stock">재고</label>

                        <input class="form-input"
                               type="number"
                               id="stock"
                               name="stock"
                               min="0"
                               required>

                        <%--
                            [상품 옵션 기능 추가] 의상/신발은 재고를 옵션별
                            수량의 합으로 서버가 다시 계산하므로, 이 필드는
                            읽기 전용으로 두고 참고용으로만 보여준다.
                        --%>
                        <p class="form-help" id="stockOptionHint" hidden>
                            의상 · 신발은 위 옵션별 재고의 합으로 자동 계산됩니다.
                        </p>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="contentTitle">관련 콘텐츠</label>

                        <input type="hidden" id="contentNo" name="contentNo" value="">

                        <div class="input-with-btn">

                            <input class="form-input"
                                   type="text"
                                   id="contentTitle"
                                   name="contentTitle"
                                   readonly>

                            <button class="btn btn-dark"
                                    type="button"
                                    onclick="openContentSearch();">
                                콘텐츠 검색
                            </button>

                        </div>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="actorNo">관련 배우</label>

                        <select class="form-input" id="actorNo" name="actorNo" disabled>
                            <option value="">콘텐츠를 먼저 선택해주세요.</option>
                        </select>

                        <p class="form-help" id="actorLoadMessage">
                            콘텐츠를 선택하면 해당 작품에 연결된 배우가 표시됩니다.
                        </p>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="description">상품 설명</label>

                        <textarea class="form-textarea" id="description" name="description"></textarea>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="updateBusinessName">사업자명</label>

                        <input class="form-input"
                               type="text"
                               id="updateBusinessName"
                               value="<c:out value='${business.businessName}'/>"
                               readonly>

                    </div>

                    <div class="form-group">

                        <label class="form-label" for="productImage">상품 이미지</label>

                        <div class="file-box">

                            <input type="file"
                                   id="productImage"
                                   name="productImage"
                                   accept=".jpg,.jpeg,.png,.gif,.webp,image/*"
                                   onchange="updateFileName(this);">

                            <span id="selectedFileName">선택된 파일 없음</span>

                        </div>

                        <p class="form-help">새 이미지를 선택하지 않으면 기존 이미지가 그대로 유지됩니다.</p>

                    </div>

                    <div class="modal-footer">

                        <button class="btn btn-primary" type="submit">상품 수정 요청</button>

                        <button class="btn btn-outline" type="button" onclick="closeModal('productUpdateModal')">취소</button>

                    </div>

                </form>

            </div>

        </div>

        <%--
            [리팩터링 추가] 상품 삭제 요청 모달
            페이지 이동 방식이던 구 productDelete.jsp를 없애고, 그 화면에서
            쓰던 삭제 요약/유의사항/사유 입력 폼을 상품마다 별도 모달로
            미리 렌더링해 둔다. 폼 action/파라미터는 기존 컨트롤러
            (POST /business/product/delete)를 그대로 재사용한다. 성공/실패
            모두 이 목록(/business/product/list)으로 리다이렉트되며(구
            GET /business/product/delete 페이지는 컨트롤러에서도 제거함),
            위쪽 알림(.form-message)으로 결과가 노출된다.
        --%>
        <c:forEach var="product"
                   items="${productList}">

            <div class="modal-overlay"
                 id="productDeleteModal_${product.productNo}">

                <div class="modal-box">

                    <div class="modal-header">

                        <h3>상품 삭제 요청</h3>

                        <button type="button"
                                class="modal-close"
                                onclick="closeModal('productDeleteModal_${product.productNo}')"
                                aria-label="닫기">
                            &times;
                        </button>

                    </div>

                    <div class="delete-summary-box">

                        <div class="delete-summary-thumb">

                            <c:choose>

                                <c:when test="${not empty product.imagePath}">

                                    <img src="${pageContext.request.contextPath}${product.imagePath}"
                                         alt="<c:out value='${product.productName}'/>">

                                </c:when>

                                <c:otherwise>
                                    상품
                                </c:otherwise>

                            </c:choose>

                        </div>

                        <div class="delete-summary-info">

                            <div class="delete-summary-type">

                                <c:choose>

                                    <c:when test="${product.productType == 'CLOTHES'}">의상</c:when>
                                    <c:when test="${product.productType == 'SHOES'}">신발</c:when>
                                    <c:when test="${product.productType == 'PROP'}">소품</c:when>
                                    <c:when test="${product.productType == 'GOODS'}">굿즈</c:when>
                                    <c:when test="${product.productType == 'OST'}">OST</c:when>
                                    <c:when test="${product.productType == 'BOOK'}">도서</c:when>
                                    <c:when test="${product.productType == 'FIGURE'}">피규어</c:when>
                                    <c:when test="${product.productType == 'POSTER'}">포스터</c:when>
                                    <c:otherwise>기타</c:otherwise>

                                </c:choose>

                            </div>

                            <div class="delete-summary-name">
                                <c:out value="${product.productName}"/>
                            </div>

                            <div class="delete-summary-meta">

                                <span>
                                    관련 콘텐츠:
                                    <c:out value="${product.contentTitle}"/>
                                </span>

                                <c:if test="${not empty product.actorName}">
                                    <span>
                                        관련 배우:
                                        <c:out value="${product.actorName}"/>
                                    </span>
                                </c:if>

                                <span>
                                    수량:
                                    <c:out value="${product.stock}"/>개
                                </span>

                                <span>
                                    가격:
                                    <c:out value="${product.price}"/>원
                                </span>

                                <c:if test="${product.discountRate > 0}">
                                    <span>
                                        할인율:
                                        <c:out value="${product.discountRate}"/>%
                                    </span>
                                </c:if>

                            </div>

                            <div class="delete-summary-status">

                                승인 상태:

                                <c:choose>

                                    <c:when test="${product.status == 'APPROVED'}">
                                        <span class="status-ok">승인 완료</span>
                                    </c:when>

                                    <c:when test="${product.status == 'WAITING'}">
                                        <span class="status-waiting">승인 대기</span>
                                    </c:when>

                                    <c:when test="${product.status == 'REJECTED'}">
                                        <span class="status-reject">반려</span>
                                    </c:when>

                                    <c:otherwise>
                                        <span class="status-reject">
                                            <c:out value="${product.status}"/>
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

                            <li>삭제 요청은 관리자 승인 후 최종 반영됩니다.</li>

                            <li>삭제가 승인되면 해당 상품은 목록에서 완전히 제거되며 복구할 수 없습니다.</li>

                            <li>이미 주문이 진행 중인 상품은 삭제 요청이 제한될 수 있습니다.</li>

                        </ul>

                    </div>

                    <form action="${pageContext.request.contextPath}/business/product/delete"
                          method="post">

                        <input type="hidden"
                               name="${_csrf.parameterName}"
                               value="${_csrf.token}">

                        <input type="hidden"
                               name="productNo"
                               value="${product.productNo}">

                        <div class="form-group">

                            <label class="form-label"
                                   for="reason_${product.productNo}">
                                삭제 사유
                            </label>

                            <textarea class="form-textarea"
                                      id="reason_${product.productNo}"
                                      name="reason"
                                      placeholder="삭제 요청 사유를 입력하세요"
                                      required></textarea>

                        </div>

                        <div class="modal-footer">

                            <button class="btn btn-danger"
                                    type="submit"
                                    onclick="return confirmAndSubmit(event, '정말 이 상품의 삭제를 요청하시겠습니까?');">
                                삭제 요청
                            </button>

                            <button class="btn btn-outline"
                                    type="button"
                                    onclick="closeModal('productDeleteModal_${product.productNo}')">
                                취소
                            </button>

                        </div>

                    </form>

                </div>

            </div>

        </c:forEach>

    </main>

</div>

<jsp:include
    page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>