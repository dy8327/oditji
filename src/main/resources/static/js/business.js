document.addEventListener("DOMContentLoaded", function () {

  /*
   * [신규] 공용 모달 - openModal / closeModal
   *
   * 관리자 페이지(admin.js)의 .modal-overlay/.modal-box + openModal/closeModal
   * 패턴을 "아이디어와 함수 네이밍 규칙"만 참고해서 business.js 안에
   * 독립적으로 새로 작성했다. admin.js를 import하거나 참조하지 않는다.
   *
   * 지금까지 페이지 이동(JSP) 방식이던 상품 수정/삭제, 이벤트 수정/연장,
   * 주문 상세 화면이 이 함수 위에서 모달로 열린다. onclick 속성에서 바로
   * 호출할 수 있도록 window에 노출한다.
   *
   * 사용법: <button onclick="openModal('xxxModal')">
   *        <button class="modal-close" onclick="closeModal('xxxModal')">
   */
  window.openModal = function (id) {

    const modal = document.getElementById(id);

    if (!modal) {
      return;
    }

    modal.classList.add("open");
  };

  window.closeModal = function (id) {

    const modal = document.getElementById(id);

    if (!modal) {
      return;
    }

    modal.classList.remove("open");
  };

  const eventImageInput = document.getElementById("eventImage");

  const eventImageFileName = document.getElementById("eventImageFileName");

  const productList = document.getElementById("productList");

  const productSearchModal = document.getElementById("productSearchModal");

  const productSearchModalClose = document.getElementById("productSearchModalClose");

  const productSearchKeyword = document.getElementById("productSearchKeyword");

  const productSearchResetButton = document.getElementById("productSearchResetButton");

  const productSearchRows = document.querySelectorAll(".product-search-row");

  const productSelectButtons = document.querySelectorAll(".product-select-button");

  const productSearchNoResult = document.getElementById("productSearchNoResult");

  const startDateInput = document.getElementById("startDate");

  const endDateInput = document.getElementById("endDate");

  /*
   * [상품 옵션 기능 추가] 의상/신발 색상-사이즈별 재고 입력 영역.
   *
   * 상품 등록 화면(productRegister.jsp)과 상품 수정 요청 모달
   * (productList.jsp의 #productUpdateModal)이 동일한 id를 사용하므로,
   * 아래 코드는 두 화면 모두에서 공통으로 동작한다.
   */
  const productTypeSelect = document.getElementById("productType");

  const productOptionSection = document.getElementById("productOptionSection");

  const productOptionRows = document.getElementById("productOptionRows");

  const addProductOptionBtn = document.getElementById("addProductOptionBtn");

  const stockInput = document.getElementById("stock");

  /* 수정 모달에만 있는 안내 문구. 등록 화면에는 없으므로 null일 수 있다. */
  const stockOptionHint = document.getElementById("stockOptionHint");

  /*
   * [리팩터링] eventList.jsp에 이벤트 연장 모달을 이벤트마다 여러 개
   * 렌더링하면서 id="extendEndDate"가 페이지에 중복될 수 있어
   * 클래스 선택자로 바꿨다.
   */
  const extendEndDateInputs = document.querySelectorAll(".extend-end-date-input");

  /*
   * 이벤트 등록/수정 화면에서 공통으로 사용하는 폼
   *
   * 두 화면 모두 id="eventForm"을 사용하므로
   * 어느 페이지에서 로드되어도 동일하게 동작합니다.
   */
  const eventRegisterForm = document.getElementById("eventForm");

  /*
   * 상품 검색 모달에서 선택한 상품을 입력할 현재 상품 행입니다.
   */
  let currentProductItem = null;

  /*
   * 동적으로 추가되는 연결 상품 행에 사용할 순번입니다.
   *
   * 각 행의 상품명 input과 할인율 input에 서로 다른 id를 부여하여
   * label의 for 속성과 정확하게 연결하고, 중복 id가 생기지 않도록 합니다.
   */
  let productRowSequence = 0;

  /*
   * JSP에서 최초 출력된 연결 상품 행의 접근성 속성을 정리합니다.
   *
   * 이벤트 등록 화면뿐 아니라 이벤트 수정 화면에서도 business.js를
   * 공통으로 사용하므로, 이미 여러 행이 출력되어 있는 경우까지 고려합니다.
   */
  function initializeProductRowAccessibility() {
    if (!productList) {
      return;
    }

    const existingProductItems = productList.querySelectorAll(".event-product-item");

    existingProductItems.forEach(function (productItem, index) {
      const productNameInput = productItem.querySelector(".productName");

      const discountInput = productItem.querySelector(".productDiscountRate");

      const discountLabel = productItem.querySelector(".discount-label");

      /*
       * 기존 JSP에 id가 이미 있으면 그 값을 유지하고,
       * id가 없을 때만 현재 행 순번을 이용하여 생성합니다.
       */
      if (productNameInput) {
        if (!productNameInput.id) {
          productNameInput.id = "productName_" + index;
        }

        /*
         * 두 번째 행부터는 화면에 별도의 상품명 label이 없으므로
         * aria-label을 통해 접근 가능한 이름을 제공합니다.
         */
        productNameInput.setAttribute("aria-label", "연결 상품 " + (index + 1));

        const productNameIndexMatch = productNameInput.id.match(/^productName_(\d+)$/);

        /*
         * 기존에 부여된 번호보다 큰 번호부터
         * 새로운 상품 행 번호를 생성합니다.
         */
        if (productNameIndexMatch) {
          productRowSequence = Math.max(productRowSequence, Number(productNameIndexMatch[1]) + 1);
        }
      }

      if (discountInput) {
        if (!discountInput.id) {
          discountInput.id = "productDiscountRate_" + index;
        }

        const discountIndexMatch = discountInput.id.match(/^productDiscountRate_(\d+)$/);

        if (discountIndexMatch) {
          productRowSequence = Math.max(productRowSequence, Number(discountIndexMatch[1]) + 1);
        }
      }

      /*
       * 할인율 label이 존재하면 해당 행의 할인율 input과
       * for/id로 연결하여 화면 낭독기가 입력 목적을 알 수 있게 합니다.
       */
      if (discountLabel && discountInput) {
        discountLabel.htmlFor = discountInput.id;
      }
    });

    linkProductSectionLabel();

    /*
     * 기존 행에 규칙에 맞는 id가 없었던 경우에도,
     * 다음 동적 행이 기존 행과 같은 id를 사용하지 않도록 보정합니다.
     */
    productRowSequence = Math.max(productRowSequence, existingProductItems.length);
  }

  /*
   * 연결 상품 제목 label을 현재 #productList의 첫 번째 상품명 input과
   * 연결합니다. 최초 로드 시(initializeProductRowAccessibility)뿐 아니라,
   * 이벤트 수정 모달이 열릴 때 상품 행이 통째로 새로 채워진 뒤에도
   * 다시 호출해서 label-for 연결이 끊어지지 않게 합니다.
   */
  function linkProductSectionLabel() {
    if (!productList) {
      return;
    }

    const firstProductNameInput = productList.querySelector(".productName");

    const productSectionLabel = productList.previousElementSibling;

    if (firstProductNameInput && productSectionLabel && productSectionLabel.tagName === "LABEL") {
      productSectionLabel.htmlFor = firstProductNameInput.id;
    }
  }

  /*
   * 화면이 처음 로드되었을 때 기존 상품 행의
   * id와 label 연결 상태를 먼저 정리합니다.
   */
  initializeProductRowAccessibility();

  /*
   * 등록/수정 처리 결과 알림
   *
   * JSP에서 <script>alert("${successMessage}")</script> 형태로
   * 직접 스크립트를 출력하던 부분을 외부 JavaScript로 분리한 것입니다.
   *
   * 서버는 body 태그의 data-success-message와
   * data-error-message 속성에 메시지를 전달합니다.
   */
  const successMessage = document.body.dataset.successMessage;

  if (successMessage) {
    showAlert(successMessage, "success");
  }

  const errorMessage = document.body.dataset.errorMessage;

  if (errorMessage) {
    showAlert(errorMessage, "error");
  }

  /*
   * 이미지 파일명 출력
   */
  if (eventImageInput) {
    eventImageInput.addEventListener("change", function () {
      if (eventImageInput.files.length === 0) {
        if (eventImageFileName) {
          eventImageFileName.textContent = "선택된 파일 없음";
        }

        return;
      }

      if (eventImageFileName) {
        eventImageFileName.textContent = eventImageInput.files[0].name;
      }
    });
  }

  /*
   * 이벤트 시작일 변경 시 종료일의 최소 선택 날짜를 설정합니다.
   */
  if (startDateInput && endDateInput) {
    startDateInput.addEventListener("change", function () {
      endDateInput.min = startDateInput.value;

      /*
       * 이미 선택된 종료일이 새 시작일보다 빠르면
       * 잘못된 기간이 제출되지 않도록 종료일을 초기화합니다.
       */
      if (endDateInput.value && endDateInput.value < startDateInput.value) {
        endDateInput.value = "";
      }
    });
  }

  /*
   * 이벤트 연장 화면의 최소 선택 날짜를 설정합니다.
   *
   * 현재 종료일은 input의 data-current-end-date 속성으로 전달받고,
   * 현재 종료일 다음 날부터 연장 종료일을 선택할 수 있게 합니다.
   */
  extendEndDateInputs.forEach(function (extendEndDateInput) {
    const currentEndDate = extendEndDateInput.dataset.currentEndDate;

    if (currentEndDate) {
      const minimumDate = new Date(currentEndDate + "T00:00:00");

      minimumDate.setDate(minimumDate.getDate() + 1);

      const year = minimumDate.getFullYear();

      const month = String(minimumDate.getMonth() + 1).padStart(2, "0");

      const day = String(minimumDate.getDate()).padStart(2, "0");

      extendEndDateInput.min = year + "-" + month + "-" + day;
    }
  });

  /*
   * 상품 검색 버튼 클릭 시
   * 현재 상품 행을 저장하고 상품 검색 모달을 엽니다.
   */
  document.addEventListener("click", function (event) {
    const target = event.target;

    if (!(target instanceof Element) || !target.classList.contains("productSearchButton")) {
      return;
    }

    /*
     * 상품 검색 모달이 없는 화면에서는
     * 이후 로직을 실행하지 않습니다.
     */
    if (!productSearchModal) {
      return;
    }

    currentProductItem = target.closest(".event-product-item");

    productSearchModal.classList.add("active");

    document.body.style.overflow = "hidden";

    if (productSearchKeyword) {
      productSearchKeyword.focus();
    }
  });

  /*
   * 연결 상품 행 하나를 생성합니다.
   *
   * addProductButton 클릭(빈 행 추가)과, 이벤트 수정 모달을 열 때
   * 기존 연결 상품으로 행을 다시 채우는 두 경우 모두 이 함수를
   * 공유합니다. 두 경우 모두 각 입력 요소에 고유한 id를 부여해야
   * 하므로(상품명 input의 aria-label, 할인율 input과 label의 for/id
   * 연결) 템플릿을 이 함수 하나로 통합했습니다.
   *
   * productName은 신뢰할 수 없는 값(상품명에 특수문자가 포함될 수 있음)
   * 이므로 innerHTML 템플릿에 직접 넣지 않고, DOM 생성 후 .value로
   * 대입합니다(id/순번처럼 안전한 값만 템플릿 문자열에 사용).
   */
  function createProductRow(productNo, productName, discountRate, price) {

    /*
     * 현재 순번을 이번 행에 사용한 다음,
     * 다음 추가 행을 위해 순번을 증가시킵니다.
     *
     * 삭제된 행의 번호를 다시 사용하지 않으므로
     * 동일 화면에서 id가 중복될 가능성을 방지합니다.
     */
    const currentRowSequence = productRowSequence;

    productRowSequence++;

    const productNameId = "productName_" + currentRowSequence;

    const discountRateId = "productDiscountRate_" + currentRowSequence;

    const productRowNumber = currentRowSequence + 1;

    const safeDiscountRate =
      (discountRate === null || discountRate === undefined || discountRate === "")
        ? 0
        : discountRate;

    const productItem = document.createElement("div");

    productItem.className = "event-product-item";

    productItem.innerHTML = `

                <input type="hidden"
                       name="productNoList"
                       class="productNo">


                <div class="product-row">

                    <input class="form-input productName"
                           type="text"
                           id="${productNameId}"
                           name="productNameList"
                           aria-label="연결 상품 ${productRowNumber}"
                           placeholder="연결할 상품을 선택하세요."
                           readonly>


                    <button class="btn btn-dark productSearchButton"
                            type="button">
                        상품 검색
                    </button>


                    <label class="form-label discount-label"
                           for="${discountRateId}">
                        할인율 (%)
                    </label>


                    <input class="form-input productDiscountRate"
                           type="number"
                           id="${discountRateId}"
                           name="discountRateList"
                           min="0"
                           max="100"
                           value="${safeDiscountRate}">


                    <button class="btn btn-dark removeProductButton"
                            type="button"
                            aria-label="연결 상품 ${productRowNumber} 삭제">
                        -
                    </button>

                </div>


                <p class="form-hint productDiscountPreview">
                    상품을 선택하면 할인 적용가가 표시됩니다.
                </p>

            `;

    /*
     * 상품 번호/상품명은 신뢰할 수 없는 텍스트이므로
     * 프로퍼티 대입(.value)만으로 채워서 마크업으로 해석되지 않게 합니다.
     */
    const productNoInput = productItem.querySelector(".productNo");

    if (productNoInput) {
      productNoInput.value = productNo || "";
    }

    const productNameInput = productItem.querySelector(".productName");

    if (productNameInput) {
      productNameInput.value = productName || "";
    }

    if (price) {
      productItem.dataset.price = price;
    }

    const discountInput = productItem.querySelector(".productDiscountRate");

    const preview = productItem.querySelector(".productDiscountPreview");

    updateDiscountPreview(Number(price), discountInput, preview);

    return productItem;
  }

  // 연결 상품 버튼 상태 갱신
  function refreshProductButtons() {

      if (!productList) {
          return;
      }

      const items = productList.querySelectorAll(".event-product-item");

      items.forEach(function(item, index) {

          const button = item.querySelector(".addProductButton, .removeProductButton");

          if (!button) {
              return;
          }

          // 첫 번째 상품은 항상 추가 버튼
          if (index === 0) {

              button.className = "btn btn-primary addProductButton";
              button.textContent = "+";
              button.setAttribute("aria-label", "연결 상품 추가");

          } else {

              button.className = "btn btn-dark removeProductButton";
              button.textContent = "-";
              button.setAttribute("aria-label", "연결 상품 삭제");

          }

      });

  }


  // 연결 상품 행 추가
  function addProductRow() {

      if (!productList) {
          return;
      }


      const newItem = createProductRow(null, "", 0, null);


      productList.appendChild(newItem);


      refreshProductButtons();

      linkProductSectionLabel();

  }


  // 연결 상품 행 삭제
  function removeProductRow(button) {

      const item = button.closest(".event-product-item");

      if (!item) {
          return;
      }


      item.remove();


      refreshProductButtons();

  }


  // 상품 추가/삭제 이벤트
  if (productList) {

      productList.addEventListener("click", function(e) {


          // + 버튼 클릭
          if (e.target.classList.contains("addProductButton")) {

              addProductRow();

          }


          // - 버튼 클릭
          if (e.target.classList.contains("removeProductButton")) {

              removeProductRow(e.target);

          }

      });

  }

  /*
   * [상품 옵션 기능 추가]
   *
   * 의상/신발은 색상-사이즈 조합별로 재고를 등록/수정한다. 상품 등록
   * 화면과 상품 수정 요청 모달이 동일한 id(#productOptionSection,
   * #productOptionRows, #addProductOptionBtn, #productType, #stock)를
   * 쓰므로 아래 로직 전체를 공유한다.
   *
   * 서버(BusinessServiceImpl)는 PRODUCT.STOCK에 옵션별 재고의 합을
   * 저장하므로, 옵션 모드일 때는 #stock을 읽기 전용으로 바꾸고 옵션
   * 입력값이 바뀔 때마다 합계를 다시 계산해 보여준다.
   *
   * 각 옵션 행의 input name은 optionList[N].colorName / .sizeName /
   * .stock 형태로 맞춘다. GoodsManageVO.optionList가
   * List<ProductOptionVO>이므로 Spring이 이 인덱스 표기를 그대로
   * List<ProductOptionVO>로 바인딩한다. 행을 삭제했을 때 인덱스가
   * 중간에 비면 바인딩이 꼬일 수 있으므로, 추가/삭제할 때마다
   * reindexProductOptionRows()로 0부터 다시 매긴다.
   */
  function productTypeRequiresOption(productTypeValue) {
    return productTypeValue === "CLOTHES" || productTypeValue === "SHOES";
  }

  /*
   * [수정] 상품 등록 화면(productRegister.js)은 사이즈를 자유 입력이 아니라
   * 신발/의상 종류별 고정 목록에서 고르는 드롭다운으로 제공하는데, 이
   * 수정 모달(business.js)만 자유 텍스트 입력이라 두 화면이 서로 달라
   * 보였다. 동일한 목록으로 통일한다.
   */
  function productOptionSizeChoices(productTypeValue) {
    return productTypeValue === "SHOES"
      ? ["220", "225", "230", "235", "240", "245", "250", "255", "260", "265", "270", "275", "280", "285", "290"]
      : ["XS", "S", "M", "L", "XL", "2XL", "3XL", "FREE"];
  }

  function buildProductOptionSizeChoicesHtml(productTypeValue) {
    return productOptionSizeChoices(productTypeValue)
      .map(function (value) {
        return "<option value=\"" + value + "\">" + value + "</option>";
      })
      .join("");
  }

  function createProductOptionRow(colorName, sizeName, stock) {

    const row = document.createElement("div");

    /*
     * business.css의 [상품 옵션 기능 추가] 블록이 .product-option-row를
     * grid-template-columns: 1.2fr 1fr 0.8fr auto (색상/사이즈/재고/삭제
     * 버튼 4열)로 이미 정의해두었으므로, 그 4개 요소를 이 div의 직계
     * 자식으로 둔다(중첩 wrapper를 두지 않는다).
     */
    row.className = "product-option-row";

    const sizeChoicesHtml = buildProductOptionSizeChoicesHtml(productTypeSelect ? productTypeSelect.value : "");

    row.innerHTML = `

                <input class="form-input optionColorName"
                       type="text"
                       placeholder="색상 (예: 블랙)"
                       maxlength="50">

                <select class="form-input optionSizeName">
                    <option value="">사이즈 선택</option>
                    ${sizeChoicesHtml}
                </select>

                <input class="form-input optionStock"
                       type="number"
                       min="0"
                       placeholder="재고">

                <button class="option-remove-btn"
                        type="button">
                    삭제
                </button>

            `;

    /* 상품명과 마찬가지로 특수문자가 들어갈 수 있으므로 .value로 대입한다. */
    const colorInput = row.querySelector(".optionColorName");
    const sizeInput = row.querySelector(".optionSizeName");
    const stockField = row.querySelector(".optionStock");

    if (colorInput) {
      colorInput.value = colorName || "";
    }

    if (sizeInput) {

      sizeInput.value = sizeName || "";

      /*
       * 기존에 저장된 사이즈 값이 현재 상품 종류의 드롭다운 목록에
       * 없는 경우(예: 예전 자유 입력 데이터, 상품 종류가 바뀐 경우)
       * .value 대입이 조용히 실패해 값이 사라지므로, 목록에 없는
       * 값이면 옵션을 하나 추가해 데이터가 유실되지 않게 한다.
       */
      if (sizeName && sizeInput.value !== sizeName) {

        const extraOption = document.createElement("option");

        extraOption.value = sizeName;
        extraOption.textContent = sizeName;
        extraOption.selected = true;

        sizeInput.appendChild(extraOption);
      }
    }

    if (stockField) {
      stockField.value = (stock === null || stock === undefined || stock === "") ? "" : stock;
    }

    return row;
  }

  /* 옵션 행들의 input name(optionList[N].xxx)과 aria-label을 0부터 다시 매긴다. */
  function reindexProductOptionRows() {

    if (!productOptionRows) {
      return;
    }

    const rows = productOptionRows.querySelectorAll(".product-option-row");

    rows.forEach(function (row, index) {

      const rowNumber = index + 1;

      const colorInput = row.querySelector(".optionColorName");
      const sizeInput = row.querySelector(".optionSizeName");
      const stockField = row.querySelector(".optionStock");
      const removeButton = row.querySelector(".option-remove-btn");

      if (colorInput) {
        colorInput.name = "optionList[" + index + "].colorName";
        colorInput.setAttribute("aria-label", "옵션 " + rowNumber + " 색상");
      }

      if (sizeInput) {
        sizeInput.name = "optionList[" + index + "].sizeName";
        sizeInput.setAttribute("aria-label", "옵션 " + rowNumber + " 사이즈");
      }

      if (stockField) {
        stockField.name = "optionList[" + index + "].stock";
        stockField.setAttribute("aria-label", "옵션 " + rowNumber + " 재고");
      }

      if (removeButton) {
        removeButton.setAttribute("aria-label", "옵션 " + rowNumber + " 삭제");
      }
    });
  }

  function refreshProductOptionRemoveButtons() {

      if (!productOptionRows) {
          return;
      }

      const rows = productOptionRows.querySelectorAll(".product-option-row");

      rows.forEach(function (row, index) {

          const removeButton = row.querySelector(".option-remove-btn");

          if (!removeButton) {
              return;
          }

          removeButton.style.display = index === 0 ? "none" : "inline-flex";

      });
  }

  /*
   * [상품 옵션 기능 추가] 상품 종류(의상/신발)가 바뀌면 이미 그려진
   * 옵션 행들의 사이즈 드롭다운 목록도 그 종류에 맞게 다시 그린다.
   * 기존에 골라둔 값이 새 목록에도 있으면 그대로 유지한다.
   */
  function refreshProductOptionSizeChoices() {

    if (!productOptionRows || !productTypeSelect) {
      return;
    }

    const sizeChoicesHtml = buildProductOptionSizeChoicesHtml(productTypeSelect.value);

    productOptionRows.querySelectorAll(".optionSizeName").forEach(function (select) {

      const oldValue = select.value;

      select.innerHTML = "<option value=\"\">사이즈 선택</option>" + sizeChoicesHtml;

      select.value = oldValue;

      if (oldValue && select.value !== oldValue) {

        const extraOption = document.createElement("option");

        extraOption.value = oldValue;
        extraOption.textContent = oldValue;
        extraOption.selected = true;

        select.appendChild(extraOption);
      }
    });
  }

  /* 옵션 모드일 때 #stock 표시값을 옵션별 재고의 합으로 다시 계산한다. */
  function refreshProductOptionStockTotal() {

    if (!stockInput || !productOptionRows || !productTypeSelect) {
      return;
    }

    if (!productTypeRequiresOption(productTypeSelect.value)) {
      return;
    }

    const stockFields = productOptionRows.querySelectorAll(".optionStock");

    let total = 0;

    stockFields.forEach(function (field) {

      const value = Number(field.value);

      if (Number.isFinite(value) && value > 0) {
        total += value;
      }
    });

    stockInput.value = total;
  }

  /*
   * [상품 옵션 기능 추가]
   * 상품 수정 요청 모달을 열 때, 이 상품의 기존 옵션 조합으로 옵션
   * 행을 다시 채운다. productList.jsp의 숨김 template
   * (#productOptionData_{productNo})에서 데이터를 읽어온다 - 이벤트
   * 수정 모달이 연결 상품을 채우는 방식과 동일하다.
   */
  function fillProductOptionRows(productNo) {

    if (!productOptionRows) {
      return;
    }

    productOptionRows.innerHTML = "";

    const optionTemplate = document.getElementById("productOptionData_" + productNo);

    const existingOptionRows = optionTemplate
      ? optionTemplate.content.querySelectorAll(".product-option-data")
      : [];

    if (existingOptionRows.length === 0) {

      productOptionRows.appendChild(createProductOptionRow(null, "", null));

    } else {

      existingOptionRows.forEach(function (optionData) {

        productOptionRows.appendChild(createProductOptionRow(
          optionData.dataset.colorName,
          optionData.dataset.sizeName,
          optionData.dataset.stock));
      });
    }

    reindexProductOptionRows();
    refreshProductOptionRemoveButtons();
  }

  /*
   * 상품 종류에 맞춰 옵션 영역을 보이거나 숨기고, #stock 읽기 전용
   * 여부를 맞춘다. productNoForPrefill을 주면(수정 모달을 열 때) 빈
   * 행 하나 대신 그 상품의 기존 옵션으로 채운다.
   */
  function updateProductOptionSectionVisibility(productNoForPrefill) {

    if (!productOptionSection || !productTypeSelect || !productOptionRows) {
      return;
    }

    const needsOption = productTypeRequiresOption(productTypeSelect.value);

    productOptionSection.hidden = !needsOption;

    if (stockOptionHint) {
      stockOptionHint.hidden = !needsOption;
    }

    if (stockInput) {
      stockInput.readOnly = needsOption;
    }

    if (needsOption) {

      if (productNoForPrefill) {

        fillProductOptionRows(productNoForPrefill);

      } else if (!productOptionRows.querySelector(".product-option-row")) {

        productOptionRows.appendChild(createProductOptionRow(null, "", null));
        reindexProductOptionRows();

      } else {

        /*
         * 이미 옵션 행이 있는 상태에서 상품 종류(의상<->신발)만 바뀐
         * 경우 - 새 행을 만들지 않고 기존 행들의 사이즈 드롭다운
         * 목록만 새 종류에 맞게 다시 그린다.
         */
        refreshProductOptionSizeChoices();
      }

      refreshProductOptionStockTotal();

    } else {

      productOptionRows.innerHTML = "";
    }
  }

  if (productTypeSelect) {

    productTypeSelect.addEventListener("change", updateProductOptionSectionVisibility);

    /* 상품 수정 모달을 열 때 openProductUpdateModal이 값을 채운 뒤에도 호출하지만, 페이지 최초 진입 시 상태도 맞춰둔다. */
    updateProductOptionSectionVisibility();
  }

  if (addProductOptionBtn) {

    addProductOptionBtn.addEventListener("click", function () {

      if (!productOptionRows) {
        return;
      }

      productOptionRows.appendChild(createProductOptionRow(null, "", null));

      reindexProductOptionRows();
      refreshProductOptionRemoveButtons();
      refreshProductOptionStockTotal();
    });
  }

  if (productOptionRows) {

    /* 옵션 행 삭제(이벤트 위임) */
    productOptionRows.addEventListener("click", function (e) {

      if (!e.target.classList.contains("option-remove-btn")) {
        return;
      }

      const row = e.target.closest(".product-option-row");

      if (!row) {
        return;
      }

      /* 옵션은 의상/신발 상품에서 최소 1개 이상 필요하므로 마지막 한 행은 지우지 못하게 한다. */
      if (productOptionRows.querySelectorAll(".product-option-row").length <= 1) {
        return;
      }

      row.remove();

      reindexProductOptionRows();
      refreshProductOptionRemoveButtons();
      refreshProductOptionStockTotal();
    });

    /* 옵션 재고 입력이 바뀔 때마다 #stock 합계를 다시 계산한다(이벤트 위임). */
    productOptionRows.addEventListener("input", function (e) {

      if (!e.target.classList.contains("optionStock")) {
        return;
      }

      refreshProductOptionStockTotal();
    });
  }

  /*
   * 동적으로 추가한 상품 행을 삭제합니다.
   */
  document.addEventListener("click", function (event) {
    const target = event.target;

    if (!(target instanceof Element) || !target.classList.contains("removeProductButton")) {
      return;
    }

    const item = target.closest(".event-product-item");

    if (item) {
      item.remove();
    }
  });

  /*
   * 상품 검색 모달을 닫고
   * 페이지 스크롤 잠금을 해제합니다.
   */
  function closeProductSearchModal() {
    if (!productSearchModal) {
      return;
    }

    productSearchModal.classList.remove("active");

    document.body.style.overflow = "";
  }

  /*
   * 모달 우측 상단 닫기 버튼 처리입니다.
   */
  if (productSearchModalClose) {
    productSearchModalClose.addEventListener("click", closeProductSearchModal);
  }

  /*
   * 모달 배경을 직접 클릭한 경우 모달을 닫습니다.
   */
  if (productSearchModal) {
    productSearchModal.addEventListener("click", function (event) {
      if (event.target === productSearchModal) {
        closeProductSearchModal();
      }
    });
  }

  /*
   * 상품 검색 모달이 열린 상태에서
   * Escape 키를 누르면 모달을 닫습니다.
   */
  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && productSearchModal && productSearchModal.classList.contains("active")) {
      closeProductSearchModal();
    }
  });

  /*
   * 상품 검색어가 입력될 때
   * 상품명, 작품명, 배우명, 상품 종류를 포함한 행 전체를 검색합니다.
   */
  if (productSearchKeyword) {
    productSearchKeyword.addEventListener("input", function () {
      const keyword = productSearchKeyword.value.trim().toLowerCase();

      let visibleCount = 0;

      productSearchRows.forEach(function (row) {
        const rowText = row.innerText.toLowerCase();

        const matched = keyword === "" || rowText.includes(keyword);

        row.style.display = matched ? "" : "none";

        if (matched) {
          visibleCount++;
        }
      });

      if (productSearchNoResult) {
        productSearchNoResult.style.display = visibleCount === 0 ? "block" : "none";
      }
    });
  }

  /*
   * 상품 검색 조건을 초기화하고
   * 숨겨진 모든 상품 행을 다시 표시합니다.
   */
  if (productSearchResetButton && productSearchKeyword) {
    productSearchResetButton.addEventListener("click", function () {
      productSearchKeyword.value = "";

      productSearchRows.forEach(function (row) {
        row.style.display = "";
      });

      if (productSearchNoResult) {
        productSearchNoResult.style.display = "none";
      }

      productSearchKeyword.focus();
    });
  }

  /*
   * 상품 검색 결과에서 선택 버튼을 누르면
   * 현재 상품 행에 상품 번호, 상품명, 가격을 저장합니다.
   */
  productSelectButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      if (!currentProductItem) {
        return;
      }

      const productNoInput = currentProductItem.querySelector(".productNo");

      const productNameInput = currentProductItem.querySelector(".productName");

      const discountInput = currentProductItem.querySelector(".productDiscountRate");

      const preview = currentProductItem.querySelector(".productDiscountPreview");

      if (productNoInput) {
        productNoInput.value = button.dataset.productNo || "";
      }

      if (productNameInput) {
        productNameInput.value = button.dataset.productName || "";
      }

      /*
       * 할인 미리보기 계산에 사용할 상품 원가를
       * 현재 상품 행의 data-price 속성에 저장합니다.
       */
      currentProductItem.dataset.price = button.dataset.productPrice || "";

      if (discountInput && preview) {
        updateDiscountPreview(Number(currentProductItem.dataset.price), discountInput, preview);
      }

      closeProductSearchModal();
    });
  });

  /*
   * 선택한 상품의 원가와 할인율을 이용하여
   * 할인 적용가 안내 문구를 갱신합니다.
   */
  function updateDiscountPreview(price, discountInput, preview) {
    if (!discountInput || !preview) {
      return;
    }

    const rate = Number(discountInput.value);

    if (!price || Number.isNaN(rate)) {
      preview.textContent = "상품을 선택하면 할인 적용가가 표시됩니다.";

      return;
    }

    const discountedPrice = Math.round((price * (100 - rate)) / 100);

    preview.textContent = "원가 " + price.toLocaleString() + "원 → 할인 적용가 " + discountedPrice.toLocaleString() + "원 (할인율 " + rate + "%)";
  }

  /*
   * 할인율 입력값이 변경될 때
   * 해당 상품 행의 할인 적용가를 다시 계산합니다.
   */
  document.addEventListener("input", function (event) {
    const target = event.target;

    if (!(target instanceof Element) || !target.classList.contains("productDiscountRate")) {
      return;
    }

    const item = target.closest(".event-product-item");

    if (!item) {
      return;
    }

    const price = item.dataset.price;

    const preview = item.querySelector(".productDiscountPreview");

    updateDiscountPreview(Number(price), target, preview);
  });

  /*
   * 이벤트 등록 또는 수정 요청 전
   * 하나 이상의 상품이 선택되었는지 확인합니다.
   */
  if (eventRegisterForm) {
    eventRegisterForm.addEventListener("submit", function (event) {
      const productNos = eventRegisterForm.querySelectorAll(".productNo");

      let selected = false;

      productNos.forEach(function (input) {
        if (input.value) {
          selected = true;
        }
      });

      if (!selected) {
        event.preventDefault();

        showAlert("이벤트에 연결할 상품을 선택해주세요.", "warning");
      }
    });
  }

  /*
   * 정산 지급계좌번호 복사 기능
   */
  window.copyAccountNumber = function () {
    const accountNumberElement = document.querySelector(".account-number");

    if (!accountNumberElement) {
      return;
    }

    const accountNumber = accountNumberElement.textContent.trim();

    navigator.clipboard
      .writeText(accountNumber)
      .then(function () {
        showAlert("계좌번호가 복사되었습니다.", "success");
      })
      .catch(function () {
        showAlert("계좌번호를 복사하지 못했습니다.", "error");
      });
  };

  window.chooseContent = function (button) {

    const mode = button.dataset.mode;
    const title = button.dataset.title;

    if (!window.opener
            || window.opener.closed) {

        showAlert("상품 등록 또는 수정 화면을 찾을 수 없습니다.", "warning");
        return;
    }

    /*
     * 상품 등록 화면은 JSONL의 TMDB 식별값을 전달합니다.
     * 이 단계에서는 CONTENT 테이블에 저장하지 않습니다.
     */
    if (mode === "register") {

        const tmdbId = Number(button.dataset.tmdbId);
        const contentType = button.dataset.contentType;

        if (!Number.isFinite(tmdbId)
                || tmdbId <= 0
                || !contentType) {

            showAlert("올바른 콘텐츠 정보가 아닙니다.", "warning");
            return;
        }

        window.opener.selectCachedContent(
            tmdbId,
            contentType,
            title
        );

        window.close();
        return;
    }

    /* 상품 수정 화면은 기존 CONTENT_NO 방식을 유지합니다. */
    const contentNo = Number(button.dataset.contentNo);

    if (!Number.isFinite(contentNo)
            || contentNo <= 0) {

        showAlert("올바른 콘텐츠 번호가 아닙니다.", "warning");
        return;
    }

    window.opener.selectContent(
        contentNo,
        title
    );

    window.close();
  };

  /*
   * =========================================================
   * [신규] 상품 수정 모달 (구 productUpdate.js를 이 파일로 흡수)
   *
   * productList.jsp의 상품마다 있는 "수정 요청" 버튼은 페이지 이동 대신
   * 공용 모달(#productUpdateModal) 1개를 연다. 콘텐츠 검색 팝업 연동,
   * 배우 목록 AJAX 조회, 이미지 파일명 표시, 폼 검증 로직은 상품마다
   * 새로 만들지 않고 고정된 element id를 그대로 사용한다(구
   * productUpdate.jsp가 페이지 하나당 상품 하나만 다루던 것과 동일한
   * 전제). 아이디어와 함수 구성은 admin.js의 상세보기 팝업 population
   * 방식(row 버튼의 data-* 값을 읽어 폼에 채우는 것)만 참고했다.
   * =========================================================
   */

  /* 콘텐츠 검색 팝업 */
  window.openContentSearch = function () {

    window.open(
      contextPath + "/business/content/search",
      "contentSearchPopup",
      "width=900,height=720,scrollbars=yes,resizable=yes"
    );
  };

  /*
   * 콘텐츠 검색 팝업에서 콘텐츠를 선택하면
   * contentSearch.jsp -> business.js의 chooseContent()가
   * window.opener.selectContent(contentNo, title)을 호출한다.
   */
  window.selectContent = function (contentNo, title) {

    const convertedContentNo = Number(contentNo);

    if (!Number.isFinite(convertedContentNo) || convertedContentNo <= 0) {
      showAlert("올바른 콘텐츠 번호가 아닙니다.", "warning");
      return;
    }

    document.getElementById("contentNo").value = convertedContentNo;
    document.getElementById("contentTitle").value = title;

    /* 콘텐츠가 바뀌었으므로 새 콘텐츠에 연결된 배우 목록을 다시 조회한다. */
    loadActorsByContent(convertedContentNo, null);
  };

  /* 배우 선택 영역을 "콘텐츠를 먼저 선택하세요" 상태로 되돌린다. */
  function resetActorSelect() {

    const actorSelect = document.getElementById("actorNo");
    const actorLoadMessage = document.getElementById("actorLoadMessage");

    if (!actorSelect || !actorLoadMessage) {
      return;
    }

    actorSelect.innerHTML = "<option value=\"\">콘텐츠를 먼저 선택해주세요.</option>";
    actorSelect.disabled = true;
    actorLoadMessage.textContent = "콘텐츠를 선택하면 해당 작품에 연결된 배우가 표시됩니다.";
  }

  /* 배우 목록을 select 옵션으로 렌더링하고, 필요하면 기존 선택값을 복원한다. */
  function renderActorList(actorList, actorNoToRestore) {

    const actorSelect = document.getElementById("actorNo");
    const actorLoadMessage = document.getElementById("actorLoadMessage");

    actorSelect.innerHTML = "";

    /* PRODUCT.ACTOR_NO는 NULL 허용이므로 "선택 안 함" 옵션을 둔다. */
    const emptyOption = document.createElement("option");
    emptyOption.value = "";
    emptyOption.textContent = "관련 배우 선택 안 함";
    actorSelect.appendChild(emptyOption);

    if (!Array.isArray(actorList) || actorList.length === 0) {

      const noActorOption = document.createElement("option");
      noActorOption.value = "";
      noActorOption.textContent = "해당 콘텐츠에 연결된 배우가 없습니다.";
      actorSelect.appendChild(noActorOption);

      actorSelect.disabled = false;
      actorLoadMessage.textContent = "CONTENT_ACTOR에 연결된 배우 정보가 없습니다.";
      return;
    }

    actorList.forEach(function (actor) {

      const option = document.createElement("option");

      /* 서버에 전송되는 값은 ACTOR_NO */
      option.value = actor.actorNo;

      let optionText = actor.actorName;

      if (actor.characterName) {
        optionText += " / 배역: " + actor.characterName;
      }

      option.textContent = optionText;

      /* 기존 상품에 저장된 배우 또는 수정 실패 후 유지된 배우를 선택 상태로 복원한다. */
      if (actorNoToRestore && String(actor.actorNo) === String(actorNoToRestore)) {
        option.selected = true;
      }

      actorSelect.appendChild(option);
    });

    actorSelect.disabled = false;
    actorLoadMessage.textContent = actorList.length + "명의 배우가 조회되었습니다.";
  }

  /* 선택한 콘텐츠(contentNo)에 연결된 배우를 조회한다. */
  async function loadActorsByContent(contentNo, actorNoToRestore) {

    const actorSelect = document.getElementById("actorNo");
    const actorLoadMessage = document.getElementById("actorLoadMessage");

    const convertedContentNo = Number(contentNo);

    if (!Number.isFinite(convertedContentNo) || convertedContentNo <= 0) {
      resetActorSelect();
      return;
    }

    actorSelect.disabled = true;
    actorSelect.innerHTML = "<option value=\"\">배우 목록을 불러오는 중입니다.</option>";
    actorLoadMessage.textContent = "선택한 콘텐츠의 배우를 조회하고 있습니다.";

    try {

      const requestUrl =
        contextPath
        + "/business/api/actor/list"
        + "?contentNo="
        + encodeURIComponent(convertedContentNo);

      const response = await fetch(requestUrl, {
        method: "GET",
        headers: { "Accept": "application/json" }
      });

      if (!response.ok) {
        throw new Error("배우 조회 실패: " + response.status);
      }

      const actorList = await response.json();

      renderActorList(actorList, actorNoToRestore);

    } catch (error) {

      console.error("콘텐츠별 배우 조회 오류:", error);

      actorSelect.innerHTML = "<option value=\"\">배우 목록 조회 실패</option>";
      actorSelect.disabled = true;
      actorLoadMessage.textContent = "배우 정보를 불러오지 못했습니다.";
    }
  }

  /* 이미지 파일명 출력 */
  window.updateFileName = function (input) {

    const fileNameElement = document.getElementById("selectedFileName");

    if (input.files && input.files.length > 0) {

      fileNameElement.textContent = input.files[0].name;

    } else {

      const existingImagePath = document.getElementById("existingImagePath").value;

      fileNameElement.textContent = existingImagePath ? existingImagePath : "선택된 파일 없음";
    }
  };

  /* 상품 수정 폼 검증 */
  window.validateProductForm = function (event) {

    const productName = document.getElementById("productName").value.trim();
    const productType = document.getElementById("productType").value;
    const price = Number(document.getElementById("price").value);
    const discountRate = Number(document.getElementById("discountRate").value);
    const stock = Number(document.getElementById("stock").value);
    const contentNo = document.getElementById("contentNo").value;

    if (!productName) {
      showAlert("상품명을 입력해주세요.", "warning");
      return false;
    }

    if (!productType) {
      showAlert("상품 종류를 선택해주세요.", "warning");
      return false;
    }

    if (!Number.isFinite(price) || price <= 0) {
      showAlert("가격은 1원 이상 입력해주세요.", "warning");
      return false;
    }

    if (!Number.isFinite(discountRate) || discountRate < 0 || discountRate > 100) {
      showAlert("할인율은 0부터 100 사이로 입력해주세요.", "warning");
      return false;
    }

    if (!Number.isFinite(stock) || stock < 0) {
      showAlert("재고는 0개 이상 입력해주세요.", "warning");
      return false;
    }

    if (!contentNo) {
      showAlert("관련 콘텐츠를 선택해주세요.", "warning");
      return false;
    }

    /*
     * [상품 옵션 기능 추가] 의상/신발은 색상-사이즈 옵션을 1개 이상,
     * 중복 없이 올바르게 입력해야 한다. BusinessServiceImpl의 서버
     * 검증과 동일한 규칙을 화면에서 먼저 확인해 왕복 없이 알려준다.
     */
    if (productTypeRequiresOption(productType)) {

      const optionValidationMessage = validateProductOptionRows();

      if (optionValidationMessage) {
        showAlert(optionValidationMessage, "warning");
        return false;
      }

      refreshProductOptionStockTotal();
    }

    return confirmAndSubmit(event, "상품 수정을 요청하시겠습니까?");
  };

  /*
   * [상품 옵션 기능 추가]
   * 옵션 행들을 검사해 문제가 있으면 사용자에게 보여줄 안내 문구를,
   * 문제가 없으면 null을 반환한다.
   */
  function validateProductOptionRows() {

    if (!productOptionRows) {
      return null;
    }

    const rows = productOptionRows.querySelectorAll(".product-option-row");

    if (rows.length === 0) {
      return "의상과 신발은 색상, 사이즈, 재고 옵션을 1개 이상 등록해야 합니다.";
    }

    const duplicateCheck = new Set();

    for (const row of rows) {

      const colorName = (row.querySelector(".optionColorName") || {}).value || "";
      const sizeName = (row.querySelector(".optionSizeName") || {}).value || "";
      const stockValue = Number((row.querySelector(".optionStock") || {}).value);

      if (!colorName.trim() || !sizeName.trim() || !Number.isFinite(stockValue) || stockValue < 0) {
        return "모든 옵션의 색상, 사이즈, 재고를 올바르게 입력해주세요.";
      }

      const key = colorName.trim().toUpperCase() + "|" + sizeName.trim().toUpperCase();

      if (duplicateCheck.has(key)) {
        return "동일한 색상과 사이즈 조합은 중복 등록할 수 없습니다.";
      }

      duplicateCheck.add(key);
    }

    return null;
  }

  /*
   * productList.jsp의 "수정 요청" 버튼(data-* 속성 보유)을 클릭했을 때
   * 공용 수정 모달의 폼 필드를 채우고 모달을 연다.
   */
  window.openProductUpdateModal = function (button) {

    document.getElementById("updateProductNo").value = button.dataset.productNo || "";
    document.getElementById("productName").value = button.dataset.productName || "";
    document.getElementById("productType").value = button.dataset.productType || "";
    document.getElementById("price").value = button.dataset.price || "";
    document.getElementById("discountRate").value = button.dataset.discountRate || 0;
    document.getElementById("stock").value = button.dataset.stock || 0;
    document.getElementById("contentNo").value = button.dataset.contentNo || "";
    document.getElementById("contentTitle").value = button.dataset.contentTitle || "";
    document.getElementById("description").value = button.dataset.description || "";
    document.getElementById("existingImagePath").value = button.dataset.imagePath || "";

    /*
     * [상품 옵션 기능 추가] 등록 화면과 동일하게 상품 종류에 맞춰
     * 옵션 영역을 열고, 이 상품의 기존 색상-사이즈 옵션으로 채운다.
     */
    updateProductOptionSectionVisibility(button.dataset.productNo);

    /* 새 모달을 열 때마다 이전에 선택했던 이미지 파일 입력값은 비운다. */
    const productImageInput = document.getElementById("productImage");
    if (productImageInput) {
      productImageInput.value = "";
    }
    updateFileName(productImageInput);

    const contentNo = button.dataset.contentNo;

    if (contentNo && Number(contentNo) > 0) {
      loadActorsByContent(contentNo, button.dataset.actorNo);
    } else {
      resetActorSelect();
    }

    openModal("productUpdateModal");
  };

  /*
   * =========================================================
   * [신규] 이벤트 수정 모달 (구 eventUpdate.jsp를 공용 모달 1개로 통합)
   *
   * eventList.jsp의 이벤트마다 있는 "수정" 버튼은 페이지 이동 대신
   * 공용 모달(#eventUpdateModal) 1개를 연다. 상품 수정 모달과 마찬가지로
   * 상품 검색 팝업/날짜 최소값 제한/이미지 파일명 표시 로직(고정된
   * element id 기준으로 동작)이 이벤트마다 모달을 복제하면 깨지기
   * 때문에, 제목·기간처럼 값이 하나뿐인 필드는 버튼의 data-* 값으로
   * 채우고, 개수가 정해지지 않은 연결 상품 목록만 이벤트별로 미리
   * 렌더링해 둔 숨김 <template id="eventProductData_{eventNo}">에서
   * 읽어와 createProductRow()로 다시 그린다.
   * =========================================================
   */
  window.openEventUpdateModal = function (button) {

    document.getElementById("updateEventNo").value = button.dataset.eventNo || "";
    document.getElementById("eventTitle").value = button.dataset.title || "";
    document.getElementById("description").value = button.dataset.description || "";

    const startDateField = document.getElementById("startDate");
    const endDateField = document.getElementById("endDate");

    startDateField.value = button.dataset.startDate || "";
    endDateField.value = button.dataset.endDate || "";

    /* 시작일 변경 리스너와 동일한 규칙으로 종료일의 최소 선택 날짜를 맞춘다. */
    endDateField.min = button.dataset.startDate || "";

    /* 새 모달을 열 때마다 이전에 선택했던 이미지 파일 입력값은 비운다. */
    const eventImageInputField = document.getElementById("eventImage");

    if (eventImageInputField) {
      eventImageInputField.value = "";
    }

    /*
     * 새 이미지를 선택하지 않으면 서버가 기존 배너 이미지를 그대로
     * 유지하므로(BusinessServiceImpl.updateApprovedEvent), 화면에는
     * 기존 이미지 유지 여부만 안내한다.
     */
    const eventImageFileNameField = document.getElementById("eventImageFileName");

    if (eventImageFileNameField) {
      eventImageFileNameField.textContent = button.dataset.bannerImage
        ? "기존 이미지 유지"
        : "선택된 파일 없음";
    }

    /*
     * 연결 상품 행을 이 이벤트의 기존 연결 상품으로 다시 채운다.
     * 숨김 template에 상품이 하나도 없으면(이론상 발생하지 않지만
     * 방어적으로) 등록 화면과 동일하게 빈 행 하나를 보여준다.
     */
    if (productList) {

      productList.innerHTML = "";

      const eventProductTemplate = document.getElementById(
        "eventProductData_" + button.dataset.eventNo);

      const connectedProductRows = eventProductTemplate
        ? eventProductTemplate.content.querySelectorAll(".event-product-data")
        : [];

      if (connectedProductRows.length === 0) {

        productList.appendChild(createProductRow(null, "", 0, null));

      } else {

        connectedProductRows.forEach(function (connectedProduct) {

          productList.appendChild(createProductRow(
            connectedProduct.dataset.productNo,
            connectedProduct.dataset.productName,
            connectedProduct.dataset.discountRate,
            connectedProduct.dataset.price));
        });
      }

      /*
       * createProductRow()는 모든 행을 일단 "-"(removeProductButton,
       * btn-dark)로 생성한다. 연결 상품 등록 화면(eventRegister.jsp)처럼
       * 첫 번째 행은 "+"(addProductButton, btn-primary), 나머지 행은
       * "-"가 되도록 버튼 상태를 다시 계산한다.
       */
      refreshProductButtons();

      linkProductSectionLabel();
    }

    openModal("eventUpdateModal");
  };
});
