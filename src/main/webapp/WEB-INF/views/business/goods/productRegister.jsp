<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">

<title>ODITJI | 상품 등록 요청</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<script>
const contextPath =
        "${pageContext.request.contextPath}";

/*
 * 상품 등록 실패 후 화면으로 돌아왔을 때
 * 이전에 선택한 배우 번호를 복원하기 위한 값
 */
const savedActorNo =
        "${empty productForm.actorNo
            ? ''
            : productForm.actorNo}";


/* =========================================================
   콘텐츠 검색 팝업
========================================================= */
function openContentSearch() {

    window.open(
        contextPath + "/business/content/search",
        "contentSearchPopup",
        "width=900,height=720,"
            + "scrollbars=yes,resizable=yes"
    );
}


/* =========================================================
   콘텐츠 검색 팝업에서 콘텐츠 선택

   contentSearch.jsp에서 다음 함수를 호출한다.

   window.opener.selectContent(contentNo, title);
========================================================= */
function selectContent(contentNo, title) {

    const convertedContentNo =
            Number(contentNo);

    if (!Number.isFinite(convertedContentNo)
            || convertedContentNo <= 0) {

        alert("올바른 콘텐츠 번호가 아닙니다.");
        return;
    }

    document.getElementById(
        "contentNo"
    ).value = convertedContentNo;

    document.getElementById(
        "contentTitle"
    ).value = title;

    /*
     * 선택한 CONTENT_NO로 해당 콘텐츠의 배우를 조회한다.
     */
    loadActorsByContent(
        convertedContentNo,
        null
    );
}


/* =========================================================
   선택한 콘텐츠에 연결된 배우 조회
========================================================= */
async function loadActorsByContent(
        contentNo,
        actorNoToRestore) {

    const actorSelect =
            document.getElementById(
                "actorNo"
            );

    const actorLoadMessage =
            document.getElementById(
                "actorLoadMessage"
            );

    const convertedContentNo =
            Number(contentNo);

    if (!Number.isFinite(convertedContentNo)
            || convertedContentNo <= 0) {

        resetActorSelect();
        return;
    }

    actorSelect.disabled = true;

    actorSelect.innerHTML =
            "<option value=\"\">"
            + "배우 목록을 불러오는 중입니다."
            + "</option>";

    actorLoadMessage.textContent =
            "선택한 콘텐츠의 배우를 조회하고 있습니다.";

    try {

        const requestUrl =
                contextPath
                + "/business/api/actor/list"
                + "?contentNo="
                + encodeURIComponent(
                    convertedContentNo
                );

        console.log(
            "배우 조회 요청:",
            requestUrl
        );

        const response =
                await fetch(
                    requestUrl,
                    {
                        method: "GET",
                        headers: {
                            "Accept": "application/json"
                        }
                    }
                );

        if (!response.ok) {

            throw new Error(
                "배우 조회 실패: "
                + response.status
            );
        }

        const actorList =
                await response.json();

        console.log(
            "배우 조회 결과:",
            actorList
        );

        renderActorList(
            actorList,
            actorNoToRestore
        );

    } catch (error) {

        console.error(
            "콘텐츠별 배우 조회 오류:",
            error
        );

        actorSelect.innerHTML =
                "<option value=\"\">"
                + "배우 목록 조회 실패"
                + "</option>";

        actorSelect.disabled = true;

        actorLoadMessage.textContent =
                "배우 정보를 불러오지 못했습니다.";
    }
}


/* =========================================================
   배우 목록 출력
========================================================= */
function renderActorList(
        actorList,
        actorNoToRestore) {

    const actorSelect =
            document.getElementById(
                "actorNo"
            );

    const actorLoadMessage =
            document.getElementById(
                "actorLoadMessage"
            );

    actorSelect.innerHTML = "";

    /*
     * PRODUCT.ACTOR_NO는 NULL 허용
     */
    const emptyOption =
            document.createElement(
                "option"
            );

    emptyOption.value = "";
    emptyOption.textContent =
            "관련 배우 선택 안 함";

    actorSelect.appendChild(
        emptyOption
    );

    if (!Array.isArray(actorList)
            || actorList.length === 0) {

        const noActorOption =
                document.createElement(
                    "option"
                );

        noActorOption.value = "";

        noActorOption.textContent =
                "해당 콘텐츠에 연결된 배우가 없습니다.";

        actorSelect.appendChild(
            noActorOption
        );

        actorSelect.disabled = false;

        actorLoadMessage.textContent =
                "CONTENT_ACTOR에 연결된 배우 정보가 없습니다.";

        return;
    }

    actorList.forEach(
        function(actor) {

            const option =
                    document.createElement(
                        "option"
                    );

            /*
             * 서버에 전송되는 값은 ACTOR_NO
             */
            option.value =
                    actor.actorNo;

            let optionText =
                    actor.actorName;

            if (actor.characterName) {

                optionText +=
                        " / 배역: "
                        + actor.characterName;
            }

            option.textContent =
                    optionText;

            if (actorNoToRestore
                    && String(actor.actorNo)
                        === String(
                            actorNoToRestore
                        )) {

                option.selected = true;
            }

            actorSelect.appendChild(
                option
            );
        }
    );

    actorSelect.disabled = false;

    actorLoadMessage.textContent =
            actorList.length
            + "명의 배우가 조회되었습니다.";
}


/* =========================================================
   배우 선택 영역 초기화
========================================================= */
function resetActorSelect() {

    const actorSelect =
            document.getElementById(
                "actorNo"
            );

    const actorLoadMessage =
            document.getElementById(
                "actorLoadMessage"
            );

    actorSelect.innerHTML =
            "<option value=\"\">"
            + "콘텐츠를 먼저 선택해주세요."
            + "</option>";

    actorSelect.disabled = true;

    actorLoadMessage.textContent =
            "콘텐츠를 선택하면 해당 작품에 연결된 "
            + "배우가 표시됩니다.";
}


/* =========================================================
   이미지 파일명 출력
========================================================= */
function updateFileName(input) {

    const fileNameElement =
            document.getElementById(
                "selectedFileName"
            );

    if (input.files
            && input.files.length > 0) {

        fileNameElement.textContent =
                input.files[0].name;

    } else {

        fileNameElement.textContent =
                "선택된 파일 없음";
    }
}


/* =========================================================
   상품 등록 폼 검증
========================================================= */
function validateProductForm() {

    const productName =
            document.getElementById(
                "productName"
            ).value.trim();

    const productType =
            document.getElementById(
                "productType"
            ).value;

    const price =
            Number(
                document.getElementById(
                    "price"
                ).value
            );

    const discountRate =
            Number(
                document.getElementById(
                    "discountRate"
                ).value
            );

    const stock =
            Number(
                document.getElementById(
                    "stock"
                ).value
            );

    const contentNo =
            document.getElementById(
                "contentNo"
            ).value;

    const productImage =
            document.getElementById(
                "productImage"
            );

    if (!productName) {
        alert("상품명을 입력해주세요.");
        return false;
    }

    if (!productType) {
        alert("상품 종류를 선택해주세요.");
        return false;
    }

    if (!Number.isFinite(price)
            || price <= 0) {

        alert("가격은 1원 이상 입력해주세요.");
        return false;
    }

    if (!Number.isFinite(discountRate)
            || discountRate < 0
            || discountRate > 100) {

        alert(
            "할인율은 0부터 100 사이로 입력해주세요."
        );

        return false;
    }

    if (!Number.isFinite(stock)
            || stock < 0) {

        alert("재고는 0개 이상 입력해주세요.");
        return false;
    }

    if (!contentNo) {

        alert("관련 콘텐츠를 선택해주세요.");
        return false;
    }

    if (!productImage.files
            || productImage.files.length === 0) {

        alert("상품 대표 이미지를 선택해주세요.");
        return false;
    }

    return confirm(
        "상품 등록을 요청하시겠습니까?"
    );
}


/* =========================================================
   화면 최초 진입

   등록 실패 후 CONTENT_NO가 남아 있으면
   배우 목록과 기존 배우 선택값을 복원한다.
========================================================= */
document.addEventListener(
    "DOMContentLoaded",
    function() {

        const contentNo =
                document.getElementById(
                    "contentNo"
                ).value;

        if (contentNo
                && Number(contentNo) > 0) {

            loadActorsByContent(
                contentNo,
                savedActorNo
            );

        } else {

            resetActorSelect();
        }
    }
);
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
                상품 등록 요청
            </h1>

            <c:if test="${not empty errorMessage}">

                <div class="form-message error-message">
                    <c:out value="${errorMessage}"/>
                </div>

            </c:if>

            <form action="${pageContext.request.contextPath}/business/product/register"
                  method="post"
                  enctype="multipart/form-data"
                  onsubmit="return validateProductForm();">

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

                    <label class="form-label">
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
                              name="description"
                              placeholder="상품 설명을 입력하세요"><c:out value="${productForm.description}"/></textarea>

                </div>

                <div class="form-group">

                    <label class="form-label">
                        사업자명
                    </label>

                    <input class="form-input"
                           type="text"
                           value="<c:out value='${business.businessName}'/>"
                           readonly>

                </div>

                <div class="form-group">

                    <label class="form-label"
                           for="productImage">
                        상품 대표 이미지
                    </label>

                    <div class="file-box">

                        <input type="file"
                               id="productImage"
                               name="productImage"
                               accept=".jpg,.jpeg,.png,.gif,.webp,image/*"
                               onchange="updateFileName(this);"
                               required>

                        <span id="selectedFileName">
                            선택된 파일 없음
                        </span>

                    </div>

                    <p class="form-help">
                        JPG, JPEG, PNG, GIF, WEBP 형식의
                        10MB 이하 이미지를 등록해주세요.
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