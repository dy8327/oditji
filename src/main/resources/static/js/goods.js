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

  /**
   * [UX 강화] 재입고 알림 신청 결과 메시지 컨트롤러
   * 성공(success) 메시지는 확인 후 화면에 계속 남아있을 필요가 없으므로
   * 잠시 후 페이드아웃되며 자동으로 사라지고, 실패(error) 메시지는
   * 사용자가 원인을 놓치지 않도록 다음 조작이 있을 때까지 유지한다.
   */
  function createRestockMessageController(message) {
    let hideTimer = null;

    function clearHideTimer() {
      if (hideTimer) {
        clearTimeout(hideTimer);
        hideTimer = null;
      }
    }

    return function showMessage(text, type) {
      if (!message) {
        return;
      }

      clearHideTimer();
      message.classList.remove("is-fading");
      message.textContent = text || "";
      message.classList.remove("is-success", "is-error");

      if (!text || !type) {
        return;
      }

      message.classList.add(type === "error" ? "is-error" : "is-success");

      if (type === "success") {
        hideTimer = setTimeout(function () {
          message.classList.add("is-fading");
          hideTimer = setTimeout(function () {
            message.textContent = "";
            message.classList.remove("is-success", "is-fading");
          }, 250);
        }, 3200);
      }
    };
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

  /* [옵션별 재입고 알림 추가] 선택 옵션 재입고 신청 버튼을 초기화합니다. */
  initializeOptionRestockRequestButton();

  /* [재입고 알림 신청 추가] 품절 상품의 재입고 신청 버튼을 초기화합니다. */
  initializeRestockRequestButton();

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
        updateGoodsFilterAllState(form, checkbox.dataset.goodsFilterGroup);
      });
    });

    allCheckboxes.forEach(function (allCheckbox) {
      allCheckbox.addEventListener("change", function () {
        const groupName = allCheckbox.dataset.goodsFilterGroup;

        const groupItems = form.querySelectorAll("input[data-goods-filter-item]" + "[data-goods-filter-group='" + groupName + "']");

        if (allCheckbox.checked) {
          groupItems.forEach(function (item) {
            item.checked = false;
          });

          return;
        }

        const checkedItems = form.querySelectorAll("input[data-goods-filter-item]" + "[data-goods-filter-group='" + groupName + "']:checked");

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
    const allCheckbox = form.querySelector("input[data-goods-filter-all]" + "[data-goods-filter-group='" + groupName + "']");

    if (!allCheckbox) {
      return;
    }

    const checkedItems = form.querySelectorAll("input[data-goods-filter-item]" + "[data-goods-filter-group='" + groupName + "']:checked");

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
    const thumbButtons = Array.from(document.querySelectorAll(".detail-gallery-thumb"));
    const track = document.querySelector(".detail-gallery-track");
    const prevButton = document.querySelector(".detail-gallery-nav.prev");
    const nextButton = document.querySelector(".detail-gallery-nav.next");

    if (!mainImage) {
      return;
    }

    /* =====================================================
     * [상품 상세 이미지 동작 수정]
     * 큰 이미지는 등록된 기본 이미지로 고정합니다.
     * 세부 이미지에 마우스를 올린 동안만 해당 이미지를 미리 보여주고,
     * 마우스를 떼거나 포커스가 빠지면 기본 이미지로 되돌립니다.
     * ===================================================== */
    const registeredMainImageSrc = mainImage.currentSrc || mainImage.src;

    function previewDetailImage(thumbButton) {
      const fullImage = thumbButton.dataset.full;

      if (!fullImage) {
        return;
      }

      mainImage.src = fullImage;
      thumbButton.classList.add("is-active");
    }

    function restoreRegisteredMainImage(thumbButton) {
      mainImage.src = registeredMainImageSrc;
      thumbButton.classList.remove("is-active");
    }

    thumbButtons.forEach(function (thumbButton) {
      thumbButton.addEventListener("mouseenter", function () {
        previewDetailImage(thumbButton);
      });

      thumbButton.addEventListener("mouseleave", function () {
        restoreRegisteredMainImage(thumbButton);
      });

      /* 키보드 사용자도 동일하게 미리보기를 사용할 수 있게 합니다. */
      thumbButton.addEventListener("focus", function () {
        previewDetailImage(thumbButton);
      });

      thumbButton.addEventListener("blur", function () {
        restoreRegisteredMainImage(thumbButton);
      });

      /* 클릭해도 큰 이미지가 상세 이미지로 고정되지 않도록 기본 동작만 막습니다. */
      thumbButton.addEventListener("click", function (event) {
        event.preventDefault();
      });
    });

    /* =====================================================
     * [추가] 세부 이미지가 4장 이상일 때 3개씩 보이도록 슬라이드 처리
     * ===================================================== */
    if (!track || thumbButtons.length === 0 || !prevButton || !nextButton) {
      return;
    }

    const visibleCount = 3;
    let pageIndex = 0;

    function updateGallerySlider() {
      const maxPageIndex = Math.max(0, Math.ceil(thumbButtons.length / visibleCount) - 1);

      if (pageIndex > maxPageIndex) {
        pageIndex = maxPageIndex;
      }

      if (thumbButtons.length <= visibleCount) {
        track.style.transform = "translateX(0)";
        prevButton.disabled = true;
        nextButton.disabled = true;
        return;
      }

      const firstThumb = thumbButtons[0];
      const thumbWidth = firstThumb.getBoundingClientRect().width;
      const gapValue = window.getComputedStyle(track).gap || "0px";
      const gap = Number.parseFloat(gapValue) || 0;
      const moveWidth = (thumbWidth + gap) * visibleCount * pageIndex;

      track.style.transform = "translateX(-" + moveWidth + "px)";

      prevButton.disabled = pageIndex <= 0;
      nextButton.disabled = pageIndex >= maxPageIndex;
    }

    prevButton.addEventListener("click", function () {
      if (pageIndex <= 0) {
        return;
      }

      pageIndex -= 1;
      updateGallerySlider();
    });

    nextButton.addEventListener("click", function () {
      const maxPageIndex = Math.max(0, Math.ceil(thumbButtons.length / visibleCount) - 1);

      if (pageIndex >= maxPageIndex) {
        return;
      }

      pageIndex += 1;
      updateGallerySlider();
    });

    window.addEventListener("resize", updateGallerySlider);

    updateGallerySlider();
  }

  /*
   * [수정] 상품 상세 화면의 구매 수량을 관리한다.
   * 옵션 상품은 현재 선택한 옵션 재고까지만 수량을 선택할 수 있다.
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

    const unitPrice = Number(quantityControl.dataset.unitPrice);

    /*
     * [수정] 재고를 페이지 최초 로딩 시 한 번만 저장하지 않고,
     * 수량을 변경할 때마다 현재 data-stock 값을 다시 가져온다.
     *
     * 옵션 선택 시 syncQuantity()에서 data-stock이
     * 선택 옵션 재고로 변경되므로 해당 값을 기준으로 수량을 제한한다.
     */
    function getCurrentStock() {
      const currentStock = Number(quantityControl.dataset.stock);

      if (!Number.isInteger(currentStock) || currentStock < 0) {
        return 0;
      }

      return currentStock;
    }

    /*
     * [수정] 입력한 수량을 1개부터
     * 현재 선택 옵션 재고 범위 안으로 보정한다.
     */
    function normalizeQuantity() {
      const stock = getCurrentStock();

      if (stock <= 0) {
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

    /* [유지] 선택 수량에 따라 총 상품 금액을 다시 계산한다. */
    function updateTotalPrice(quantity) {
      if (!totalPriceElement || !Number.isFinite(unitPrice)) {
        return;
      }

      const totalPrice = unitPrice * quantity;

      totalPriceElement.textContent = "₩ " + totalPrice.toLocaleString("ko-KR");
    }

    /*
     * [수정] 현재 선택 옵션 재고를 기준으로
     * 수량 감소·증가 버튼의 활성화 상태를 변경한다.
     */
    function updateQuantityButtons(quantity) {
      const stock = getCurrentStock();

      if (stock <= 0) {
        minusButton.disabled = true;
        plusButton.disabled = true;
        quantityInput.disabled = true;

        return;
      }

      quantityInput.disabled = false;

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
      /*
       * [수정] 버튼을 클릭할 때마다 현재 선택 옵션 재고를 다시 확인한다.
       */
      const stock = getCurrentStock();

      const currentQuantity = normalizeQuantity();

      if (stock <= 0 || currentQuantity >= stock) {
        updateQuantityButtons(currentQuantity);

        return;
      }

      quantityInput.value = String(currentQuantity + 1);

      refreshQuantityState();
    });

    quantityInput.addEventListener("input", function () {
      const stock = getCurrentStock();

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

  /**
   * [재입고 알림 신청 추가]
   * 품절 상품 상세 화면에서 현재 신청 여부를 조회하고
   * 버튼 클릭 시 신청/취소 API를 호출합니다.
   */
  function initializeRestockRequestButton() {
    const button = document.getElementById("restockRequestBtn");
    const message = document.getElementById("restockRequestMessage");

    if (!button) {
      return;
    }

    const productNo = button.dataset.productNo;

    if (!productNo) {
      return;
    }

    function updateButton(requested) {
      button.dataset.requested = requested ? "true" : "false";
      button.setAttribute("aria-pressed", requested ? "true" : "false");
      button.textContent = requested ? "상품 재입고 알림 취소" : "상품 재입고 알림 신청";
      button.classList.toggle("active", requested);
    }

    const showMessage = createRestockMessageController(message);

    /*
     * [재입고 알림 신청 상태 조회]
     * 현재 로그인 사용자가 이 상품의 재입고 알림을 신청했는지 확인합니다.
     */
    fetch(contextPath + "/api/restock/" + encodeURIComponent(productNo), {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
    })
      .then(function (response) {
        if (!response.ok) {
          throw new Error("재입고 알림 신청 상태를 확인하지 못했습니다.");
        }

        return response.json();
      })
      .then(function (data) {
        if (data.login) {
          updateButton(Boolean(data.requested));
        }
      })
      .catch(function () {
        /*
         * 상태 조회에 실패하더라도
         * 상품 상세페이지 자체 사용은 막지 않습니다.
         */
      });

    /*
     * [재입고 알림 신청/취소]
     * 신청 전이면 POST,
     * 이미 신청한 상태이면 DELETE 요청을 보냅니다.
     */
    button.addEventListener("click", async function () {
      const requested = button.dataset.requested === "true";
      const method = requested ? "DELETE" : "POST";

      button.disabled = true;
      showMessage("");

      try {
        const response = await fetch(contextPath + "/api/restock/" + encodeURIComponent(productNo), {
          method: method,
          headers: createJsonHeaders(),
        });

        const data = await response.json();

        /*
         * 로그인하지 않은 사용자가 신청 버튼을 누르면
         * 로그인 페이지로 이동합니다.
         */
        if (response.status === 401) {
          alert(data.message || "로그인이 필요합니다.");
          window.location.href = contextPath + "/member/login";
          return;
        }

        if (!response.ok || data.success === false) {
          throw new Error(data.message || "재입고 알림 신청 처리에 실패했습니다.");
        }

        updateButton(Boolean(data.requested));
        showMessage(data.message || "처리되었습니다.", "success");
      } catch (error) {
        showMessage(error.message || "재입고 알림 신청 처리 중 오류가 발생했습니다.", "error");
      } finally {
        button.disabled = false;
      }
    });
  }

  /**
   * =========================================================
   * [옵션별 재입고 알림 추가]
   *
   * 색상/사이즈 옵션 상품에서 선택한 옵션의 재고가 0개일 경우
   * 해당 PRODUCT_OPTION에 대한 재입고 알림 신청/취소를 처리합니다.
   *
   * 상품 전체 재입고 신청과 구분하기 위해 OPTION_NO를
   * API 요청에 함께 전달합니다.
   * =========================================================
   */
  function initializeOptionRestockRequestButton() {
    const button = document.getElementById("optionRestockRequestBtn");

    const message = document.getElementById("optionRestockRequestMessage");

    /*
     * [UX 강화] 신청 완료 여부에 따라 품절 안내 박스 전체를
     * 빨간(품절) ↔ 초록(신청 완료) 톤으로 전환하기 위한 요소 참조.
     */
    const area = document.getElementById("optionRestockArea");
    const icon = document.getElementById("optionRestockIcon");
    const title = document.getElementById("optionRestockTitle");
    const description = document.getElementById("optionRestockDescription");

    /*
     * 옵션이 없는 상품 상세페이지에는
     * 버튼 자체가 존재하지 않으므로 종료합니다.
     */
    if (!button) {
      return;
    }

    const productNo = button.dataset.productNo;

    if (!productNo) {
      return;
    }

    /**
     * 현재 신청 여부에 따라 버튼 상태를 변경합니다.
     */
    function updateButton(requested) {
      button.dataset.requested = requested ? "true" : "false";

      button.setAttribute("aria-pressed", requested ? "true" : "false");

      /*
       * [UX 강화] "신청" -> "신청 취소"로 글자 수가 늘어나면 버튼 너비가
       * 상태마다 달라져 옆의 처리 메시지와의 줄바꿈/간격이 흔들렸다.
       * "신청" -> "취소"로 같은 글자 수만 바꿔 버튼 너비를 상태와 무관하게
       * 일정하게 유지한다.
       */
      button.textContent = requested ? "상품 선택 옵션 재입고 알림 취소" : "상품 선택 옵션 재입고 알림 신청";

      button.classList.toggle("active", requested);

      if (area) {
        area.classList.toggle("is-requested", requested);
      }

      if (icon) {
        icon.textContent = requested ? "✓" : "!";
      }

      if (title) {
        title.textContent = requested ? "재입고 알림 신청이 완료되었습니다." : "선택한 옵션이 품절되었습니다.";
      }

      if (description) {
        description.textContent = requested
          ? "재고가 다시 들어오면 알림으로 알려드릴게요."
          : "재입고 알림을 신청하면 입고 소식을 가장 먼저 알려드려요.";
      }
    }

    /**
     * 처리 결과 메시지를 표시합니다.
     */
    const showMessage = createRestockMessageController(message);

    /**
     * =========================================================
     * [옵션 재입고 신청 상태 조회]
     *
     * 사용자가 색상/사이즈를 변경할 때마다
     * 현재 OPTION_NO에 대한 신청 여부를 서버에서 조회합니다.
     * =========================================================
     */
    button.addEventListener("restockOptionChanged", async function () {
      const optionNo = Number(button.dataset.optionNo);

      /*
       * 정상적인 옵션 번호가 아니면
       * 조회하지 않습니다.
       */
      if (!Number.isInteger(optionNo) || optionNo <= 0) {
        updateButton(false);
        return;
      }

      showMessage("");

      try {
        const response = await fetch(contextPath + "/api/restock/" + encodeURIComponent(productNo) + "?optionNo=" + encodeURIComponent(optionNo), {
          method: "GET",

          headers: {
            Accept: "application/json",
          },
        });

        /*
         * 로그인하지 않은 상태에서의 조회 실패는
         * 상품 상세 화면 사용 자체를 막지 않습니다.
         */
        if (!response.ok) {
          updateButton(false);
          return;
        }

        const data = await response.json();

        if (data.login) {
          updateButton(Boolean(data.requested));
        }
      } catch (error) {
        /*
         * 상태 조회 실패가 상품 상세페이지의
         * 다른 기능에 영향을 주지 않도록 합니다.
         */
        updateButton(false);
      }
    });

    /**
     * =========================================================
     * [옵션 재입고 알림 신청/취소]
     *
     * 신청하지 않은 상태 → POST
     * 이미 신청한 상태 → DELETE
     *
     * OPTION_NO를 query parameter로 함께 전달합니다.
     * =========================================================
     */
    button.addEventListener("click", async function () {
      const optionNo = Number(button.dataset.optionNo);

      if (!Number.isInteger(optionNo) || optionNo <= 0) {
        showMessage("재입고 알림을 신청할 옵션을 선택해주세요.", "error");

        return;
      }

      const requested = button.dataset.requested === "true";

      const method = requested ? "DELETE" : "POST";

      button.disabled = true;

      showMessage("");

      try {
        const response = await fetch(contextPath + "/api/restock/" + encodeURIComponent(productNo) + "?optionNo=" + encodeURIComponent(optionNo), {
          method: method,

          headers: createJsonHeaders(),
        });

        const data = await response.json();

        /*
         * 로그인하지 않은 사용자는
         * 로그인 페이지로 이동시킵니다.
         */
        if (response.status === 401) {
          alert(data.message || "로그인이 필요합니다.");

          const currentUrl = window.location.pathname + window.location.search;

          window.location.href = contextPath + "/member/login?redirect=" + encodeURIComponent(currentUrl);

          return;
        }

        if (!response.ok || data.success === false) {
          throw new Error(data.message || "재입고 알림 신청 처리에 실패했습니다.");
        }

        /*
         * 처리 완료 후 서버에서 받은
         * 현재 신청 상태로 버튼을 갱신합니다.
         */
        updateButton(Boolean(data.requested));

        showMessage(data.message || "처리되었습니다.", "success");
      } catch (error) {
        showMessage(error.message || "재입고 알림 신청 처리 중 오류가 발생했습니다.", "error");
      } finally {
        button.disabled = false;
      }
    });
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
  /* [수정] 구매 수량 안내 문구도 선택 옵션 재고에 맞춰 변경한다. */
  const quantityGuide = document.getElementById("detailQuantityGuide");
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
    const minusButton = document.getElementById("detailQuantityMinus");
    const plusButton = document.getElementById("detailQuantityPlus");
    const buttons = document.querySelectorAll(".cart-btn,.buy-btn");
    /* [옵션별 재입고 알림 추가] JSP의 옵션 재입고 버튼을 가져옵니다. */
    const restockButton = document.getElementById("optionRestockRequestBtn");
    /*
     * [옵션별 재입고 알림 UI 추가]
     * 버튼뿐 아니라 품절 안내 영역 전체의 표시 여부를 관리합니다.
     */
    const restockArea = document.getElementById("optionRestockArea");
    const stock = option ? Number(option.stock) : 0;

    /* [유지] 수량 제어에 사용하는 재고를 선택 옵션 재고로 변경한다. */
    if (control) {
      control.dataset.stock = String(stock);
    }

    if (input) {
      input.max = String(stock);
      input.value = stock > 0 ? "1" : "0";
      input.disabled = stock <= 0;
      input.dispatchEvent(new Event("change"));
    }

    /*
     * [수정] 옵션을 다시 선택하거나 초기화했을 때
     * 수량 증가·감소 버튼 상태도 함께 변경한다.
     */
    if (minusButton) {
      minusButton.disabled = stock <= 0 || stock === 1;
    }

    if (plusButton) {
      plusButton.disabled = stock <= 1;
    }

    /* [유지] 장바구니와 바로 구매 버튼에 선택 옵션 재고를 저장한다. */
    buttons.forEach((btn) => {
      btn.dataset.stock = String(stock);
      btn.disabled = stock <= 0;
    });

    /*
     * =========================================================
     * [옵션별 재입고 알림 UI 수정]
     * =========================================================
     */
    if (restockButton) {
      if (!option) {
        if (restockArea) {
          restockArea.style.display = "none";
        }

        restockButton.dataset.optionNo = "";
        restockButton.dataset.requested = "false";
      } else if (stock <= 0) {
        if (restockArea) {
          restockArea.style.display = "flex";
        }

        restockButton.dataset.optionNo = String(option.optionNo);

        restockButton.dispatchEvent(
          new CustomEvent("restockOptionChanged", {
            detail: {
              optionNo: Number(option.optionNo),
            },
          }),
        );
      } else {
        if (restockArea) {
          restockArea.style.display = "none";
        }

        restockButton.dataset.optionNo = "";
        restockButton.dataset.requested = "false";
      }
    }

    /*
     * [수정] 선택 옵션 재고 상태 표시
     */
    if (stockText) {
      stockText.classList.remove("is-available", "is-soldout");

      if (!option) {
        stockText.textContent = "색상과 사이즈를 선택해주세요.";
      } else if (stock <= 0) {
        stockText.textContent = "품절";
        stockText.classList.add("is-soldout");
      } else {
        stockText.textContent = stock.toLocaleString("ko-KR") + "개";

        stockText.classList.add("is-available");
      }
    }

    /*
     * [수정] 구매 수량 안내도 전체 상품 재고가 아닌
     * 현재 선택한 옵션 재고를 기준으로 표시한다.
     */
    if (quantityGuide) {
      if (!option) {
        quantityGuide.textContent = "옵션을 선택해주세요.";
      } else if (stock <= 0) {
        quantityGuide.textContent = "선택한 옵션은 품절입니다.";
      } else {
        quantityGuide.textContent = `최대 ${stock.toLocaleString("ko-KR")}개까지 선택할 수 있습니다.`;
      }
    }
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
