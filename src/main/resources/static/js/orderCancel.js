/*
 * =========================================================
 * 주문 전체 취소 / 상품 부분 취소 요청
 *
 * orderList.jsp에서 사용하는 취소 요청 전용 스크립트이다.
 *
 * 전체 취소:
 * POST /order/payment/cancel
 *
 * 상품 부분 취소:
 * POST /order/payment/cancel/item
 * =========================================================
 */

document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  /*
   * =========================================================
   * 기본 요소 조회
   * =========================================================
   */

  const orderListContainer = document.querySelector(".order-list-container");

  if (!orderListContainer) {
    return;
  }

  const contextPath = orderListContainer.dataset.contextPath || "";

  /*
   * =========================================================
   * [포트원 테스트 채널 여부 확인 추가]
   * =========================================================
   */
  const portOneTestMode = orderListContainer.dataset.portoneTestMode === "true";

  const modal = document.getElementById("orderCancelModal");

  const modalTitle = document.getElementById("orderCancelModalTitle");

  const modalDescription = document.getElementById("orderCancelModalDescription");

  const reasonInput = document.getElementById("orderCancelReason");

  const errorBox = document.getElementById("orderCancelError");

  const closeButton = document.getElementById("orderCancelCloseBtn");

  const submitButton = document.getElementById("orderCancelSubmitBtn");

  if (!modal || !modalTitle || !modalDescription || !reasonInput || !errorBox || !closeButton || !submitButton) {
    console.error("주문 취소 모달을 구성하는 HTML 요소를 찾을 수 없습니다.");

    return;
  }

  /*
   * =========================================================
   * 선택된 취소 요청 정보
   *
   * cancelType:
   * ORDER = 주문 전체 취소
   * ITEM  = 상품 부분 취소
   * =========================================================
   */

  let cancelType = null;
  let selectedOrderNo = null;
  let selectedOrderItemNo = null;
  // [신규] 주문 카드에서 체크한 여러 상품을 한 번에 요청하기 위한 목록
  let selectedOrderItemNos = [];

  /*
   * =========================================================
   * 오류 메시지
   * =========================================================
   */

  function showError(message) {
    errorBox.textContent = message || "취소 요청 중 오류가 발생했습니다.";

    errorBox.style.display = "block";
  }

  function clearError() {
    errorBox.textContent = "";
    errorBox.style.display = "none";
  }

  /*
   * =========================================================
   * 버튼 상태
   * =========================================================
   */

  function disableButtons() {
    submitButton.disabled = true;
    closeButton.disabled = true;

    submitButton.textContent = "요청 처리 중...";
  }

  function restoreButtons() {
    submitButton.disabled = false;
    closeButton.disabled = false;

    submitButton.textContent = "취소 요청";
  }

  /*
   * =========================================================
   * 전체 취소 모달 열기
   * =========================================================
   */

  function openOrderCancelModal(orderNo, requestKind) {
    const parsedOrderNo = Number(orderNo);

    if (!Number.isInteger(parsedOrderNo) || parsedOrderNo <= 0) {
      showAlert("취소할 주문 번호가 올바르지 않습니다.", "warning");
      return;
    }

    cancelType = "ORDER";
    const isRefund = requestKind === "REFUND";
    selectedOrderNo = parsedOrderNo;
    selectedOrderItemNo = null;

    modalTitle.textContent = isRefund ? "전체 상품 환불 요청" : "전체 상품 주문 취소 요청";

    modalDescription.textContent = isRefund
      ? "배송 완료된 전체 상품의 환불을 요청합니다. 모든 사업자가 승인하면 전액 환불됩니다."
      : "주문 확인중인 전체 상품의 주문 취소를 요청합니다. 모든 사업자가 승인하면 전액 환불됩니다.";

    reasonInput.placeholder = isRefund ? "전체 상품 환불 사유를 입력해주세요." : "전체 주문 취소 사유를 입력해주세요.";

    reasonInput.value = "";

    clearError();
    restoreButtons();

    modal.classList.add("open");

    modal.setAttribute("aria-hidden", "false");

    document.body.style.overflow = "hidden";

    window.setTimeout(function () {
      reasonInput.focus();
    }, 0);
  }

  /*
   * =========================================================
   * [테스트 채널 간편결제 부분 취소 사전 안내 추가]
   *
   * 서버에서도 동일하게 검증하지만, 사용자가 요청을 제출하기 전에
   * 테스트 채널의 간편결제 제한을 바로 확인할 수 있도록 한다.
   * =========================================================
   */

  function isEasyPay(payMethod) {
    const normalized = String(payMethod || "")
      .trim()
      .toUpperCase()
      .replace(/[\s_-]/g, "");

    return (
      normalized.includes("EASYPAY") ||
      normalized.includes("KAKAOPAY") ||
      normalized.includes("NAVERPAY") ||
      normalized.includes("TOSSPAY") ||
      normalized.includes("PAYCO") ||
      normalized.includes("SAMSUNGPAY") ||
      normalized.includes("SSGPAY") ||
      normalized.includes("LPAY")
    );
  }

  /*
   * =========================================================
   * 상품 부분 취소 모달 열기
   * =========================================================
   */

  function openItemCancelModal(orderItemNo, productName, payMethod) {
    if (portOneTestMode && isEasyPay(payMethod)) {
      showAlert("테스트 채널의 간편결제 주문은 상품 부분 취소를 지원하지 않습니다.\n" + "주문 전체 취소를 이용해주세요.", "warning");
      return;
    }
    const parsedOrderItemNo = Number(orderItemNo);

    if (!Number.isInteger(parsedOrderItemNo) || parsedOrderItemNo <= 0) {
      showAlert("취소할 주문 상품 번호가 올바르지 않습니다.", "warning");
      return;
    }

    const safeProductName = productName && productName.trim() ? productName.trim() : "선택한 상품";

    cancelType = "ITEM";
    selectedOrderNo = null;
    selectedOrderItemNo = parsedOrderItemNo;

    modalTitle.textContent = "상품 부분 취소 요청";

    modalDescription.textContent = "'" + safeProductName + "' 상품을 부분 취소 요청합니다. " + "해당 상품을 판매한 사업자가 승인하면 " + "부분 환불됩니다.";

    reasonInput.placeholder = safeProductName + "의 취소 사유를 입력해주세요.";

    reasonInput.value = "";

    clearError();
    restoreButtons();

    modal.classList.add("open");

    modal.setAttribute("aria-hidden", "false");

    document.body.style.overflow = "hidden";

    window.setTimeout(function () {
      reasonInput.focus();
    }, 0);
  }

  /*
   * =========================================================
   * [신규] 체크한 여러 상품 취소/환불 모달 열기
   * 배송 준비중/배송중 상품은 JSP에서 체크박스 자체를 숨긴다.
   * =========================================================
   */
  function openBulkItemCancelModal(button) {
    const actionType = button.dataset.actionType || "CANCEL";
    const card = button.closest(".order-card");
    const checked = card
      ? Array.from(card.querySelectorAll(".order-item-select:checked")).filter(function (checkbox) {
          return checkbox.dataset.actionType === actionType;
        })
      : [];

    if (checked.length === 0) {
      showAlert(actionType === "REFUND" ? "환불할 상품을 선택해주세요." : "취소할 상품을 선택해주세요.", "warning");
      return;
    }

    selectedOrderItemNos = checked
      .map(function (checkbox) {
        return Number(checkbox.value);
      })
      .filter(function (value) {
        return Number.isInteger(value) && value > 0;
      });

    if (selectedOrderItemNos.length !== checked.length) {
      showAlert("선택한 주문상품 정보가 올바르지 않습니다.", "warning");
      return;
    }

    cancelType = "BULK_ITEM";
    selectedOrderNo = null;
    selectedOrderItemNo = null;
    modalTitle.textContent = actionType === "REFUND" ? "선택 상품 환불 요청" : "선택 상품 주문 취소 요청";
    modalDescription.textContent = "선택한 " + selectedOrderItemNos.length + "개 상품의 " + (actionType === "REFUND" ? "환불" : "주문 취소") + "를 요청합니다.";
    reasonInput.placeholder = "선택 상품의 " + (actionType === "REFUND" ? "환불" : "취소") + " 사유를 입력해주세요.";
    reasonInput.value = "";
    clearError();
    restoreButtons();
    modal.classList.add("open");
    modal.setAttribute("aria-hidden", "false");
    document.body.style.overflow = "hidden";
    window.setTimeout(function () {
      reasonInput.focus();
    }, 0);
  }

  /*
   * =========================================================
   * 모달 닫기
   * =========================================================
   */

  function closeCancelModal() {
    if (submitButton.disabled) {
      return;
    }

    modal.classList.remove("open");

    modal.setAttribute("aria-hidden", "true");

    document.body.style.overflow = "";

    cancelType = null;
    selectedOrderNo = null;
    selectedOrderItemNo = null;
    selectedOrderItemNos = [];

    reasonInput.value = "";

    clearError();
    restoreButtons();
  }

  /*
   * =========================================================
   * 서버 JSON 응답 읽기
   * =========================================================
   */

  async function readJsonResponse(response) {
    let data;

    try {
      data = await response.json();
    } catch (error) {
      throw new Error("서버 응답을 읽을 수 없습니다.");
    }

    if (!response.ok) {
      throw new Error(data.message || "HTTP 오류가 발생했습니다. 상태 코드: " + response.status);
    }

    return data;
  }

  /*
   * =========================================================
   * 취소 요청 API 호출
   * =========================================================
   */

  async function sendCancelRequest(url, payload) {
    const response = await fetch(contextPath + url, {
      method: "POST",

      headers: {
        "Content-Type": "application/json",
      },

      body: JSON.stringify(payload),
    });

    return readJsonResponse(response);
  }

  /*
   * =========================================================
   * 취소 요청 제출
   * =========================================================
   */

  async function submitCancelRequest() {
    clearError();

    const reason = reasonInput.value.trim();

    if (!reason) {
      showError("취소 사유를 입력해주세요.");

      reasonInput.focus();
      return;
    }

    if (reason.length > 500) {
      showError("취소 사유는 500자 이하로 입력해주세요.");

      reasonInput.focus();
      return;
    }

    let url;
    let payload;
    let confirmMessage;

    if (cancelType === "ORDER") {
      if (!Number.isInteger(selectedOrderNo) || selectedOrderNo <= 0) {
        showError("취소할 주문 번호가 올바르지 않습니다.");

        return;
      }

      url = "/order/payment/cancel";

      payload = {
        orderNo: selectedOrderNo,
        reason: reason,
      };

      confirmMessage = "주문 전체 취소를 요청하시겠습니까?\n" + "모든 사업자가 승인하면 전액 환불됩니다.";
    } else if (cancelType === "ITEM") {
      if (!Number.isInteger(selectedOrderItemNo) || selectedOrderItemNo <= 0) {
        showError("취소할 주문 상품 번호가 올바르지 않습니다.");

        return;
      }

      url = "/order/payment/cancel/item";

      payload = {
        orderItemNo: selectedOrderItemNo,
        reason: reason,
      };

      confirmMessage = "선택한 상품의 부분 취소를 요청하시겠습니까?";
    } else if (cancelType === "BULK_ITEM") {
      if (!Array.isArray(selectedOrderItemNos) || selectedOrderItemNos.length === 0) {
        showError("선택한 주문상품이 없습니다.");
        return;
      }

      const selectedAction = document.querySelector(".order-select-cancel-btn[data-active-request='true']");
      const requestText = selectedAction && selectedAction.dataset.actionType === "REFUND" ? "환불" : "주문 취소";
      const confirmed = await showConfirm("선택한 " + selectedOrderItemNos.length + "개 상품의 " + requestText + "를 요청하시겠습니까?", "warning");
      if (!confirmed) return;

      disableButtons();
      try {
        const data = await sendCancelRequest("/order/payment/cancel/items", {
          orderItemNos: selectedOrderItemNos,
          reason: reason,
        });
        if (!data.success) throw new Error(data.message || "선택 상품 요청에 실패했습니다.");
        await showAlert(data.message || "선택한 상품의 취소/환불 요청이 접수되었습니다.", "success");
        window.location.href = contextPath + (data.redirectUrl || "/order/list");
      } catch (error) {
        console.error("선택 상품 일괄 요청 오류:", error);
        showError(error.message || "선택 상품 요청 중 오류가 발생했습니다.");
        restoreButtons();
      }
      return;
    } else {
      showError("취소 요청 유형이 올바르지 않습니다.");

      return;
    }

    const confirmed = await showConfirm(confirmMessage, "warning");

    if (!confirmed) {
      return;
    }

    disableButtons();

    try {
      const data = await sendCancelRequest(url, payload);

      if (!data.success) {
        if (data.loginRequired) {
          window.location.href = contextPath + "/member/login" + "?redirect=/order/list";

          return;
        }

        throw new Error(data.message || "취소 요청에 실패했습니다.");
      }

      await showAlert(data.message || "취소 요청이 등록되었습니다.", "success");

      window.location.href = contextPath + (data.redirectUrl || "/order/list");
    } catch (error) {
      console.error("주문 취소 요청 오류:", error);

      showError(error.message || "취소 요청 중 오류가 발생했습니다.");

      restoreButtons();
    }
  }

  document.querySelectorAll(".order-select-cancel-btn").forEach(function (button) {
    button.addEventListener("click", function () {
      document.querySelectorAll(".order-select-cancel-btn").forEach(function (item) {
        delete item.dataset.activeRequest;
      });
      button.dataset.activeRequest = "true";
      openBulkItemCancelModal(button);
    });
  });

  /*
   * =========================================================
   * 주문 전체 취소 버튼
   * =========================================================
   */

  document.querySelectorAll(".payment-cancel-btn").forEach(function (button) {
    button.addEventListener("click", function () {
      openOrderCancelModal(button.dataset.orderNo, button.dataset.requestKind || "CANCEL");
    });
  });

  /*
   * =========================================================
   * 상품 부분 취소 버튼
   * =========================================================
   */

  document.querySelectorAll(".item-cancel-btn").forEach(function (button) {
    button.addEventListener("click", function () {
      openItemCancelModal(button.dataset.orderItemNo, button.dataset.productName, button.dataset.payMethod);
    });
  });

  /*
   * =========================================================
   * 모달 버튼 이벤트
   * =========================================================
   */

  closeButton.addEventListener("click", closeCancelModal);

  submitButton.addEventListener("click", submitCancelRequest);

  /*
   * =========================================================
   * 키보드 이벤트
   *
   * ESC: 모달 닫기
   * Ctrl + Enter: 취소 요청 제출
   * =========================================================
   */

  document.addEventListener("keydown", function (event) {
    if (!modal.classList.contains("open")) {
      return;
    }

    if (event.key === "Escape") {
      event.preventDefault();

      closeCancelModal();
      return;
    }

    if (event.key === "Enter" && event.ctrlKey) {
      event.preventDefault();

      submitCancelRequest();
    }
  });

  /*
   * =========================================================
   * 모달 바깥 영역 클릭 시 닫기
   * =========================================================
   */

  modal.addEventListener("click", function (event) {
    if (event.target === modal) {
      closeCancelModal();
    }
  });
});
