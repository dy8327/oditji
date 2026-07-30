/*
 * =========================================================
 * 주문상품 배송조회 모달
 *
 * orderList.jsp에서 사용하는 스크립트이다.
 * 화면 이동 없이 선택한 주문상품의 배송 정보를
 * GET /order/delivery로 조회해서 모달(#deliveryDetailModal)
 * 안의 요소들에 채워 넣는다.
 * =========================================================
 */

document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  const orderListContainer = document.querySelector(".order-list-container");

  if (!orderListContainer) {
    return;
  }

  const contextPath = orderListContainer.dataset.contextPath || "";

  /*
   * =========================================================
   * 모달 / 상태 요소
   * =========================================================
   */

  const modal = document.getElementById("deliveryDetailModal");
  const closeButton = document.getElementById("deliveryDetailCloseBtn");
  const footerCloseButton = document.getElementById("deliveryDetailFooterCloseBtn");

  const deliveryDetailLoading = document.getElementById("deliveryDetailLoading");
  const deliveryDetailError = document.getElementById("deliveryDetailError");
  const deliveryDetailContent = document.getElementById("deliveryDetailContent");

  const deliveryProgress = document.getElementById("deliveryProgress");

  const deliveryProductName = document.getElementById("deliveryProductName");
  const deliveryQuantity = document.getElementById("deliveryQuantity");
  const deliveryItemTotalPrice = document.getElementById("deliveryItemTotalPrice");

  const deliveryReceiverName = document.getElementById("deliveryReceiverName");
  const deliveryReceiverPhone = document.getElementById("deliveryReceiverPhone");
  const deliveryAddress = document.getElementById("deliveryAddress");
  const deliveryCourier = document.getElementById("deliveryCourier");
  const deliveryTrackingWrap = document.getElementById("deliveryTrackingWrap");
  const deliveryOrderCreatedAt = document.getElementById("deliveryOrderCreatedAt");

  if (!modal || !deliveryDetailContent) {
    // 배송조회 모달 마크업이 없는 페이지에서는 아무 것도 하지 않는다.
    return;
  }

  /*
   * =========================================================
   * 모달 열기 / 닫기
   * =========================================================
   */

  function openModal() {
    modal.classList.add("open");

    modal.setAttribute("aria-hidden", "false");

    document.body.style.overflow = "hidden";
  }

  function closeModal() {
    modal.classList.remove("open");

    modal.setAttribute("aria-hidden", "true");

    document.body.style.overflow = "";
  }

  if (closeButton) {
    closeButton.addEventListener("click", closeModal);
  }

  if (footerCloseButton) {
    footerCloseButton.addEventListener("click", closeModal);
  }

  modal.addEventListener("click", function (event) {
    if (event.target === modal) {
      closeModal();
    }
  });

  document.addEventListener("keydown", function (event) {
    if (!modal.classList.contains("open")) {
      return;
    }

    if (event.key === "Escape") {
      event.preventDefault();

      closeModal();
    }
  });

  /*
   * =========================================================
   * 배송조회 패널 상태 전환 (loading / error / content)
   * =========================================================
   */

  function showDeliveryState(state, message) {
    if (state === "loading" && deliveryProgress) {
      deliveryProgress.dataset.status = "CONFIRMED";
    }

    if (deliveryDetailLoading) {
      deliveryDetailLoading.style.display = state === "loading" ? "block" : "none";
    }

    if (deliveryDetailError) {
      deliveryDetailError.style.display = state === "error" ? "block" : "none";

      if (state === "error") {
        deliveryDetailError.textContent = message || "배송 정보를 불러오지 못했습니다.";
      }
    }

    if (deliveryDetailContent) {
      deliveryDetailContent.style.display = state === "content" ? "block" : "none";
    }
  }

  /*
   * =========================================================
   * 배송 단계(PREPARING / SHIPPING / DELIVERED) 표시
   * =========================================================
   */

  function renderDeliverySteps(status) {
    const allowedStatuses = ["CONFIRMED", "PREPARING", "SHIPPING", "DELIVERED"];
    const normalizedStatus = allowedStatuses.includes(status) ? status : "CONFIRMED";

    if (deliveryProgress) {
      deliveryProgress.dataset.status = normalizedStatus;
    }
  }

  /*
   * =========================================================
   * 날짜 표시 (yyyy.MM.dd HH:mm)
   *
   * 서버가 java.util.Date를 JSON으로 내려줄 때 타임스탬프(숫자) 또는
   * ISO 문자열 어느 쪽으로 와도 Date 객체 생성이 가능하도록 처리한다.
   * =========================================================
   */

  function formatDateTime(value) {
    if (!value) {
      return "-";
    }

    const date = new Date(value);

    if (isNaN(date.getTime())) {
      return "-";
    }

    function pad(n) {
      return String(n).padStart(2, "0");
    }

    return date.getFullYear() + "." + pad(date.getMonth() + 1) + "." + pad(date.getDate()) + " " + pad(date.getHours()) + ":" + pad(date.getMinutes());
  }

  function formatPrice(value) {
    const amount = Number(value) || 0;
    return amount.toLocaleString("ko-KR");
  }

  /*
   * =========================================================
   * 운송장 번호 복사 버튼 (이벤트 위임 방식)
   *
   * 배송 정보를 조회할 때마다 트래킹 영역의 내용이 새로 그려지므로
   * 클릭 이벤트는 위임으로 한 번만 등록한다.
   * =========================================================
   */

  if (deliveryTrackingWrap) {
    deliveryTrackingWrap.addEventListener("click", function (event) {
      const copyBtn = event.target.closest(".tracking-copy-btn");

      if (!copyBtn) {
        return;
      }

      const trackingNumber = copyBtn.dataset.trackingNumber;

      if (!trackingNumber) {
        return;
      }

      navigator.clipboard.writeText(trackingNumber).then(function () {
        showAlert("운송장 번호가 복사되었습니다.", "success");
      });
    });
  }

  function renderTracking(trackingNumber) {
    if (!deliveryTrackingWrap) {
      return;
    }

    if (trackingNumber) {
      deliveryTrackingWrap.innerHTML =
        '<span class="tracking-value">' +
        escapeHtml(trackingNumber) +
        ' <button type="button" class="tracking-copy-btn" data-tracking-number="' +
        escapeHtml(trackingNumber) +
        '">복사</button></span>';
    } else {
      deliveryTrackingWrap.innerHTML = '<span class="delivery-empty-note">아직 운송장 번호가 등록되지 않았습니다.</span>';
    }
  }

  function escapeHtml(value) {
    return String(value).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
  }

  /*
   * =========================================================
   * 배송 정보 렌더링
   * =========================================================
   */

  function renderDelivery(delivery) {
    renderDeliverySteps(delivery.status);

    if (deliveryProductName) {
      deliveryProductName.textContent = delivery.productName || "-";
    }

    if (deliveryQuantity) {
      deliveryQuantity.textContent = delivery.quantity != null ? delivery.quantity : "-";
    }

    if (deliveryItemTotalPrice) {
      deliveryItemTotalPrice.textContent = formatPrice(delivery.itemTotalPrice);
    }

    if (deliveryReceiverName) {
      deliveryReceiverName.textContent = delivery.receiverName || "-";
    }

    if (deliveryReceiverPhone) {
      deliveryReceiverPhone.textContent = delivery.receiverPhone || "-";
    }

    if (deliveryAddress) {
      deliveryAddress.textContent = delivery.address || "-";
    }

    if (deliveryCourier) {
      deliveryCourier.textContent = delivery.courier || "-";
    }

    renderTracking(delivery.trackingNumber);

    if (deliveryOrderCreatedAt) {
      deliveryOrderCreatedAt.textContent = formatDateTime(delivery.orderCreatedAt);
    }

    showDeliveryState("content");
  }

  /*
   * =========================================================
   * 배송 정보 조회 API 호출
   * =========================================================
   */

  async function loadDeliveryDetail(orderItemNo) {
    showDeliveryState("loading");

    try {
      const response = await fetch(contextPath + "/order/delivery?orderItemNo=" + encodeURIComponent(orderItemNo), {
        method: "GET",
        headers: {
          Accept: "application/json",
        },
      });

      let data;

      try {
        data = await response.json();
      } catch (error) {
        throw new Error("서버 응답을 읽을 수 없습니다.");
      }

      if (!response.ok || !data.success) {
        if (data && data.loginRequired) {
          window.location.href = contextPath + "/member/login" + "?redirect=/order/list";
          return;
        }

        throw new Error((data && data.message) || "배송 정보를 불러오지 못했습니다.");
      }

      renderDelivery(data.delivery || {});
    } catch (error) {
      console.error("배송 조회 오류:", error);

      showDeliveryState("error", error.message);
    }
  }

  /*
   * =========================================================
   * 주문 목록의 "배송조회" 버튼
   * =========================================================
   */

  document.querySelectorAll(".delivery-detail-btn").forEach(function (button) {
    button.addEventListener("click", function () {
      const orderItemNo = button.dataset.orderItemNo;

      if (!orderItemNo) {
        return;
      }

      openModal();
      loadDeliveryDetail(orderItemNo);
    });
  });
});
