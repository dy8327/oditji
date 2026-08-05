document.addEventListener("DOMContentLoaded", function () {
  const body = document.body;

  const contextPath = body.dataset.contextPath || "";

  function createJsonHeaders() {
    const headers = {
      "Content-Type": "application/json",
    };

    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    const csrfToken = csrfTokenMeta ? csrfTokenMeta.getAttribute("content") : "";
    const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute("content") : "";

    if (csrfToken && csrfHeader) {
      headers[csrfHeader] = csrfToken;
    }

    return headers;
  }

  initializeGoodsFilter();
  initializeGoodsSort();
  initializeGoodsSelectedFilterChips();
  initializeImageGallery();

  /*
   * [추가] 상품 상세 화면의 수량 선택과 총 상품 금액을 초기화한다.
   */
  initializeDetailQuantity();

  initializeCartButton();
  initializeBuyButton();

  /**
   * 콘텐츠 사이드바(contentList.js)와 동일한 방식으로 굿즈 필터를 초기화합니다.
   */
  function initializeGoodsFilter() {
    const form = document.getElementById("goodsFilterForm");

    if (!form) {
      return;
    }

    const allCheckboxes = form.querySelectorAll("input[data-goods-filter-all]");
    const itemCheckboxes = form.querySelectorAll("input[data-goods-filter-item]");

    itemCheckboxes.forEach(function (checkbox) {
      checkbox.addEventListener("change", function () {
        updateGoodsFilterAllState(
          form,
          checkbox.dataset.goodsFilterGroup
        );
      });
    });

    allCheckboxes.forEach(function (allCheckbox) {
      allCheckbox.addEventListener("change", function () {
        const groupName = allCheckbox.dataset.goodsFilterGroup;

        const groupItems = form.querySelectorAll(
          "input[data-goods-filter-item]"
          + "[data-goods-filter-group='"
          + groupName
          + "']"
        );

        if (allCheckbox.checked) {
          groupItems.forEach(function (item) {
            item.checked = false;
          });

          return;
        }

        const checkedItems = form.querySelectorAll(
          "input[data-goods-filter-item]"
          + "[data-goods-filter-group='"
          + groupName
          + "']:checked"
        );

        if (checkedItems.length === 0) {
          allCheckbox.checked = true;
        }
      });
    });

    form.addEventListener("submit", function () {
      const pageInput = form.querySelector("input[name='page']");

      if (pageInput) {
        pageInput.value = "1";
      }
    });
  }

  /**
   * 개별 항목 체크박스 상태에 따라 그룹 내 '전체' 체크박스 선택 여부를 갱신합니다.
   */
  function updateGoodsFilterAllState(form, groupName) {
    const allCheckbox = form.querySelector(
      "input[data-goods-filter-all]"
      + "[data-goods-filter-group='"
      + groupName
      + "']"
    );

    if (!allCheckbox) {
      return;
    }

    const checkedItems = form.querySelectorAll(
      "input[data-goods-filter-item]"
      + "[data-goods-filter-group='"
      + groupName
      + "']:checked"
    );

    allCheckbox.checked = checkedItems.length === 0;
  }

  /**
   * 정렬 선택값이 변경되면 현재 필터 조건을 유지한 채
   * 첫 페이지부터 다시 조회합니다. (contentList.js의 initializeContentSort와 동일)
   */
  function initializeGoodsSort() {
    const select = document.querySelector("[data-goods-sort-select]");

    if (!select || !select.form) {
      return;
    }

    select.addEventListener("change", function () {
      const pageInput = select.form.querySelector("input[name='page']");

      if (pageInput) {
        pageInput.value = "1";
      }

      select.form.submit();
    });
  }

  /**
   * 목록 상단의 선택 필터 칩을 누르면 해당 값만 URL에서 제거하고
   * 나머지 필터와 정렬 조건은 그대로 유지합니다. (contentList.js와 동일)
   */
  function initializeGoodsSelectedFilterChips() {
    const chips = document.querySelectorAll("[data-goods-filter-chip]");

    chips.forEach(function (chip) {
      chip.addEventListener("click", function () {
        const parameterName = chip.dataset.filterName;
        const parameterValue = chip.dataset.filterValue;

        if (!parameterName) {
          return;
        }

        const currentUrl = new URL(window.location.href);
        const nextParameters = new URLSearchParams();

        currentUrl.searchParams.forEach(function (value, name) {
          const isTarget = name === parameterName && value === parameterValue;

          if (!isTarget) {
            nextParameters.append(name, value);
          }
        });

        nextParameters.set("page", "1");

        const queryString = nextParameters.toString();

        window.location.href = currentUrl.pathname + (queryString ? "?" + queryString : "");
      });
    });
  }

  function initializeImageGallery() {
    const mainImage = document.getElementById("mainImage");

    const galleryImages = document.querySelectorAll(".image-gallery img");

    if (!mainImage || galleryImages.length === 0) {
      return;
    }

    galleryImages.forEach(function (galleryImage) {
      galleryImage.addEventListener("click", function () {
        const fullImage = galleryImage.dataset.full;

        if (!fullImage) {
          return;
        }

        mainImage.src = fullImage;

        galleryImages.forEach(function (item) {
          item.classList.remove("is-active");
        });

        galleryImage.classList.add("is-active");
      });
    });
  }

  /*
   * [추가] 상품 상세 화면의 구매 수량을 관리한다.
   * 수량은 최소 1개부터 현재 상품 재고까지만 선택할 수 있다.
   */
  function initializeDetailQuantity() {
    const quantityControl = document.querySelector(".detail-quantity-control");

    const quantityInput = document.getElementById("detailQuantity");

    const minusButton = document.getElementById("detailQuantityMinus");

    const plusButton = document.getElementById("detailQuantityPlus");

    const totalPriceElement = document.getElementById("detailTotalPrice");

    if (!quantityControl || !quantityInput || !minusButton || !plusButton) {
      return;
    }

    const stock = Number(quantityControl.dataset.stock);

    const unitPrice = Number(quantityControl.dataset.unitPrice);

    /* [추가] 입력한 수량을 1개부터 재고 범위 안으로 보정한다. */
    function normalizeQuantity() {
      if (!Number.isInteger(stock) || stock <= 0) {
        quantityInput.value = "0";
        return 0;
      }

      let quantity = Number(quantityInput.value);

      if (!Number.isInteger(quantity) || quantity < 1) {
        quantity = 1;
      }

      if (quantity > stock) {
        quantity = stock;
      }

      quantityInput.value = String(quantity);

      return quantity;
    }

    /* [추가] 선택 수량에 따라 총 상품 금액을 다시 계산한다. */
    function updateTotalPrice(quantity) {
      if (!totalPriceElement || !Number.isFinite(unitPrice)) {
        return;
      }

      const totalPrice = unitPrice * quantity;

      totalPriceElement.textContent = "₩ " + totalPrice.toLocaleString("ko-KR");
    }

    /* [추가] 최소·최대 수량에 따라 증감 버튼 상태를 조정한다. */
    function updateQuantityButtons(quantity) {
      if (!Number.isInteger(stock) || stock <= 0) {
        minusButton.disabled = true;
        plusButton.disabled = true;
        quantityInput.disabled = true;
        return;
      }

      minusButton.disabled = quantity <= 1;

      plusButton.disabled = quantity >= stock;
    }

    function refreshQuantityState() {
      const quantity = normalizeQuantity();

      updateTotalPrice(quantity);
      updateQuantityButtons(quantity);
    }

    minusButton.addEventListener("click", function () {
      const currentQuantity = normalizeQuantity();

      if (currentQuantity <= 1) {
        return;
      }

      quantityInput.value = String(currentQuantity - 1);

      refreshQuantityState();
    });

    plusButton.addEventListener("click", function () {
      const currentQuantity = normalizeQuantity();

      if (currentQuantity >= stock) {
        return;
      }

      quantityInput.value = String(currentQuantity + 1);

      refreshQuantityState();
    });

    quantityInput.addEventListener("input", function () {
      const quantity = Number(quantityInput.value);

      if (Number.isInteger(quantity) && quantity >= 1 && quantity <= stock) {
        updateTotalPrice(quantity);
        updateQuantityButtons(quantity);
      }
    });

    quantityInput.addEventListener("change", refreshQuantityState);

    quantityInput.addEventListener("blur", refreshQuantityState);

    refreshQuantityState();
  }

  function initializeCartButton() {
    document.addEventListener("click", async function (event) {
      const cartButton = event.target.closest(".cart-btn");

      if (!cartButton || cartButton.disabled) {
        return;
      }

      const productNo = Number(cartButton.dataset.productNo);

      if (!Number.isInteger(productNo) || productNo <= 0) {
        await Swal.fire({
          icon: "warning",
          text: "상품 정보가 올바르지 않습니다.",
          confirmButtonText: "확인",
        });

        return;
      }

      /*
       * [수정] 상품 상세 화면에서는 선택한 수량을 사용하고,
       * 상품 목록 화면에서는 기존처럼 1개를 장바구니에 담는다.
       */
      const quantityInput = document.getElementById("detailQuantity");

      const stock = Number(cartButton.dataset.stock);

      const quantity = quantityInput ? Number(quantityInput.value) : 1;
      const optionDataElement = document.getElementById("productOptionData");
      const selectedOption = window.getSelectedProductOption ? window.getSelectedProductOption() : null;
      if (optionDataElement && !selectedOption) {
        alert("색상과 사이즈를 선택해주세요.");
        return;
      }

      if (!Number.isInteger(quantity) || quantity <= 0) {
        await Swal.fire({
          icon: "warning",
          text: "구매 수량을 올바르게 입력해주세요.",
          confirmButtonText: "확인",
        });

        return;
      }

      if (quantityInput && Number.isInteger(stock) && stock > 0 && quantity > stock) {
        await Swal.fire({
          icon: "warning",
          text: "현재 재고는 " + stock.toLocaleString("ko-KR") + "개입니다.",
          confirmButtonText: "확인",
        });

        quantityInput.value = String(stock);

        quantityInput.dispatchEvent(new Event("change"));

        return;
      }

      cartButton.disabled = true;

      const originalText = cartButton.textContent;

      cartButton.textContent = "담는 중...";

      try {
        const response = await fetch(contextPath + "/cart/add", {
          method: "POST",

          headers: createJsonHeaders(),

          body: JSON.stringify({
            productNo: productNo,

            /* [수정] 상세 화면에서 선택한 수량을 전달한다. */
            quantity: quantity,
            optionNo: window.getSelectedProductOption && window.getSelectedProductOption() ? window.getSelectedProductOption().optionNo : null,
          }),
        });

        if (!response.ok) {
          throw new Error("HTTP " + response.status);
        }

        const result = await response.json();

        if (result.loginRequired) {
          const moveLogin = await Swal.fire({
            icon: "info",
            text: "장바구니는 로그인 후 이용할 수 있습니다.\n로그인 페이지로 이동하시겠습니까?",
            showCancelButton: true,
            confirmButtonText: "이동",
            cancelButtonText: "취소",
          });

          if (moveLogin.isConfirmed) {
            const currentUrl = window.location.pathname + window.location.search;

            window.location.href = contextPath + "/member/login?redirect=" + encodeURIComponent(currentUrl);
          }

          return;
        }

        if (!result.success) {
          await Swal.fire({
            icon: "error",
            text: result.message || "장바구니 처리에 실패했습니다.",
            confirmButtonText: "확인",
          });

          return;
        }

        const goToCart = await Swal.fire({
          icon: "success",
          text: "장바구니에 상품을 담았습니다.\n장바구니로 이동할까요?",
          showCancelButton: true,
          confirmButtonText: "이동",
          cancelButtonText: "계속 쇼핑",
        });

        if (goToCart.isConfirmed) {
          window.location.href = contextPath + "/cart";
        }
      } catch (error) {
        console.error(error);

        await Swal.fire({
          icon: "error",
          text: "장바구니 처리 중 오류가 발생했습니다.",
          confirmButtonText: "확인",
        });
      } finally {
        cartButton.disabled = false;

        cartButton.textContent = originalText;
      }
    });
  }

  function initializeBuyButton() {
    const buyButton = document.querySelector(".detail-info .buy-btn");

    if (!buyButton) {
      return;
    }

    buyButton.addEventListener("click", async function () {
      const productNo = Number(buyButton.dataset.productNo);

      if (!Number.isInteger(productNo) || productNo <= 0) {
        await Swal.fire({
          icon: "warning",
          text: "상품 정보가 올바르지 않습니다.",
          confirmButtonText: "확인",
        });

        return;
      }

      /* [추가] 상품 상세 화면에서 선택한 바로 구매 수량을 가져온다. */
      const quantityInput = document.getElementById("detailQuantity");

      const stock = Number(buyButton.dataset.stock);

      const quantity = quantityInput ? Number(quantityInput.value) : 1;
      const optionDataElement = document.getElementById("productOptionData");
      const selectedOption = window.getSelectedProductOption ? window.getSelectedProductOption() : null;
      if (optionDataElement && !selectedOption) {
        alert("색상과 사이즈를 선택해주세요.");
        return;
      }

      if (!Number.isInteger(quantity) || quantity <= 0) {
        await Swal.fire({
          icon: "warning",
          text: "구매 수량을 올바르게 입력해주세요.",
          confirmButtonText: "확인",
        });

        if (quantityInput) {
          quantityInput.focus();
        }

        return;
      }

      if (!Number.isInteger(stock) || stock <= 0) {
        await Swal.fire({
          icon: "warning",
          text: "현재 구매할 수 없는 상품입니다.",
          confirmButtonText: "확인",
        });

        return;
      }

      if (quantity > stock) {
        await Swal.fire({
          icon: "warning",
          text: "현재 재고는 " + stock.toLocaleString("ko-KR") + "개입니다.",
          confirmButtonText: "확인",
        });

        if (quantityInput) {
          quantityInput.value = String(stock);

          quantityInput.dispatchEvent(new Event("change"));
        }

        return;
      }

      buyButton.disabled = true;

      const originalText = buyButton.textContent;

      buyButton.textContent = "처리 중...";

      try {
        const response = await fetch(contextPath + "/order/direct", {
          method: "POST",

          headers: createJsonHeaders(),

          body: JSON.stringify({
            productNo: productNo,

            /* [수정] 선택한 수량으로 주문서를 생성한다. */
            quantity: quantity,
            optionNo: window.getSelectedProductOption && window.getSelectedProductOption() ? window.getSelectedProductOption().optionNo : null,
          }),
        });

        if (!response.ok) {
          throw new Error("HTTP " + response.status);
        }

        const result = await response.json();

        if (result.loginRequired) {
          const currentUrl = window.location.pathname + window.location.search;

          window.location.href = contextPath + "/member/login?redirect=" + encodeURIComponent(currentUrl);

          return;
        }

        if (!result.success) {
          await Swal.fire({
            icon: "error",
            text: result.message || "주문서 작성에 실패했습니다.",
            confirmButtonText: "확인",
          });

          return;
        }

        window.location.href = contextPath + (result.redirectUrl || "/order");
      } catch (error) {
        console.error(error);

        await Swal.fire({
          icon: "error",
          text: "바로 구매 처리 중 오류가 발생했습니다.",
          confirmButtonText: "확인",
        });
      } finally {
        buyButton.disabled = false;

        buyButton.textContent = originalText;
      }
    });
  }
});

/*
 * 상품 카드 이미지 로딩 실패 처리를 HTML의 onerror 속성에서 분리한다.
 * 비상호작용 요소인 img에 인라인 이벤트 속성을 지정하지 않으면서
 * 기존 NO IMAGE 대체 화면 동작은 그대로 유지한다.
 */
document.addEventListener("DOMContentLoaded", function () {
  const cardImages = document.querySelectorAll(".goods-card-image[data-fallback-target]");

  cardImages.forEach(function (image) {
    image.addEventListener(
      "error",
      function () {
        const fallbackId = image.dataset.fallbackTarget;

        const fallbackElement = document.getElementById(fallbackId);

        image.style.display = "none";

        if (fallbackElement) {
          fallbackElement.style.display = "flex";
        }
      },
      { once: true },
    );
  });
});

/* [상품 옵션 기능 추가] 색상 선택 후 가능한 사이즈와 조합 재고를 갱신합니다. */
document.addEventListener("DOMContentLoaded", function () {
  const dataEl = document.getElementById("productOptionData");
  const colorEl = document.getElementById("detailColor");
  const sizeEl = document.getElementById("detailSize");
  const stockText = document.getElementById("detailOptionStock");
  if (!dataEl || !colorEl || !sizeEl) {
    window.getSelectedProductOption = () => null;
    return;
  }
  let options = [];
  try {
    options = JSON.parse(dataEl.textContent);
  } catch (e) {
    options = [];
  }
  [...new Set(options.map((o) => o.color))].forEach((color) => colorEl.add(new Option(color, color)));
  function selected() {
    return options.find((o) => o.color === colorEl.value && o.size === sizeEl.value) || null;
  }
  function syncQuantity(option) {
    const control = document.querySelector(".detail-quantity-control");
    const input = document.getElementById("detailQuantity");
    const buttons = document.querySelectorAll(".cart-btn,.buy-btn");
    const stock = option ? Number(option.stock) : 0;
    if (control) control.dataset.stock = String(stock);
    if (input) {
      input.max = String(stock);
      input.value = stock > 0 ? "1" : "0";
      input.disabled = stock <= 0;
      input.dispatchEvent(new Event("change"));
    }
    buttons.forEach((btn) => {
      btn.dataset.stock = String(stock);
      btn.disabled = stock <= 0;
    });
    if (stockText) stockText.textContent = option ? `선택 옵션 재고: ${stock.toLocaleString()}개` : "색상과 사이즈를 선택해주세요.";
  }
  colorEl.addEventListener("change", () => {
    sizeEl.innerHTML = '<option value="">사이즈 선택</option>';
    options.filter((o) => o.color === colorEl.value).forEach((o) => sizeEl.add(new Option(o.size, o.size)));
    sizeEl.disabled = !colorEl.value;
    syncQuantity(null);
  });
  sizeEl.addEventListener("change", () => syncQuantity(selected()));
  window.getSelectedProductOption = selected;
});