/*
 * =========================================================
 * [수정] 주문 내역 / 취소·환불 내역 탭 및 조회 필터
 *
 * 실제 취소·환불 요청 전송은 orderCancel.js가 담당한다.
 * 이 파일에서는 버튼 요청을 중복 처리하지 않고 탭 전환과
 * 취소·환불 내역의 유형/상태/기간 조회만 담당한다.
 * =========================================================
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  const container = document.querySelector(".order-list-container");
  if (!container) return;

  const tabButtons = document.querySelectorAll(".order-tab-btn");
  const tabPanels = document.querySelectorAll(".order-tab-panel");

  function activateTab(targetId) {
    tabButtons.forEach(function (button) {
      const active = button.dataset.tabTarget === targetId;
      button.classList.toggle("active", active);
      button.setAttribute("aria-selected", active ? "true" : "false");
    });
    tabPanels.forEach(function (panel) {
      const active = panel.id === targetId;
      panel.classList.toggle("active", active);
      if (active) panel.removeAttribute("hidden");
      else panel.setAttribute("hidden", "");
    });
  }

  tabButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      if (button.dataset.tabTarget) activateTab(button.dataset.tabTarget);
    });
  });

  /* [수정] 조회 필터는 GET 요청으로 Controller/Mapper에서 처리한다. */
  const activeTab = container.dataset.activeTab === "history" ? "refundTabPanel" : "orderTabPanel";
  activateTab(activeTab);
});
