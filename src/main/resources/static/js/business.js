document.addEventListener("DOMContentLoaded", function () {
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

  const extendEndDateInput = document.getElementById("extendEndDate");

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

    /*
     * 연결 상품 제목 label은 첫 번째 상품명 input과 연결합니다.
     *
     * 수정된 JSP에서는 이미 연결되어 있지만, 기존 화면과의 호환성을
     * 위해 JavaScript에서도 한 번 더 보완합니다.
     */
    const firstProductNameInput = productList.querySelector(".productName");

    const productSectionLabel = productList.previousElementSibling;

    if (firstProductNameInput && productSectionLabel && productSectionLabel.tagName === "LABEL") {
      productSectionLabel.htmlFor = firstProductNameInput.id;
    }

    /*
     * 기존 행에 규칙에 맞는 id가 없었던 경우에도,
     * 다음 동적 행이 기존 행과 같은 id를 사용하지 않도록 보정합니다.
     */
    productRowSequence = Math.max(productRowSequence, existingProductItems.length);
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
    alert(successMessage);
  }

  const errorMessage = document.body.dataset.errorMessage;

  if (errorMessage) {
    alert(errorMessage);
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
  if (extendEndDateInput) {
    const currentEndDate = extendEndDateInput.dataset.currentEndDate;

    if (currentEndDate) {
      const minimumDate = new Date(currentEndDate + "T00:00:00");

      minimumDate.setDate(minimumDate.getDate() + 1);

      const year = minimumDate.getFullYear();

      const month = String(minimumDate.getMonth() + 1).padStart(2, "0");

      const day = String(minimumDate.getDate()).padStart(2, "0");

      extendEndDateInput.min = year + "-" + month + "-" + day;
    }
  }

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
   * 상품 행 추가
   *
   * 동적으로 생성되는 각 입력 요소에 고유한 id를 부여합니다.
   * 상품명 input은 aria-label로 접근 가능한 이름을 제공하고,
   * 할인율 input은 label의 for 속성과 id를 연결합니다.
   */
  document.addEventListener("click", function (event) {
    const target = event.target;

    if (!(target instanceof Element) || !target.classList.contains("addProductButton")) {
      return;
    }

    /*
     * 이벤트 등록·수정 화면이 아닌 페이지에서도
     * business.js가 로드될 수 있으므로 대상 목록을 확인합니다.
     */
    if (!productList) {
      return;
    }

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
                           value="0">


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

    productList.appendChild(productItem);
  });

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

        alert("이벤트에 연결할 상품을 선택해주세요.");
      }
    });
  }

  /*
   * 관리자 계좌번호 복사 기능
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
        alert("계좌번호가 복사되었습니다.");
      })
      .catch(function () {
        alert("계좌번호를 복사하지 못했습니다.");
      });
  };

  window.chooseContent = function (button) {

    const mode = button.dataset.mode;
    const title = button.dataset.title;

    if (!window.opener
            || window.opener.closed) {

        alert("상품 등록 또는 수정 화면을 찾을 수 없습니다.");
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

            alert("올바른 콘텐츠 정보가 아닙니다.");
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

        alert("올바른 콘텐츠 번호가 아닙니다.");
        return;
    }

    window.opener.selectContent(
        contentNo,
        title
    );

    window.close();
  };
});
