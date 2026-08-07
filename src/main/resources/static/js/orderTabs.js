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

  /*
   * =========================================================
   * [추가] 취소·환불 내역 조회 기간 빠른 선택
   *
   * 기존에는 시작일 입력칸에 항상 오늘 날짜가 채워져 있어
   * 사용자가 매번 직접 지우고 다시 골라야 했다. 실제 쇼핑몰
   * 주문내역 화면처럼 "1주일/1개월/3개월/6개월/전체" 버튼을
   * 누르면 기간을 즉시 계산해 채우고 바로 조회되도록 한다.
   *
   * 날짜를 직접 입력하면 빠른 선택 버튼은 선택 해제되고,
   * 두 칸이 모두 비어 있으면 "전체" 버튼이 선택된 것으로 본다.
   * =========================================================
   */
  const refundForm = document.getElementById("refundSearchForm");

  if (refundForm) {
    const startInput = document.getElementById("historyStartDate");
    const endInput = document.getElementById("historyEndDate");
    const quickButtons = document.querySelectorAll(".refund-quick-btn");

    function toDateInputValue(date) {
      const yyyy = date.getFullYear();
      const mm = String(date.getMonth() + 1).padStart(2, "0");
      const dd = String(date.getDate()).padStart(2, "0");
      return yyyy + "-" + mm + "-" + dd;
    }

    function clearActiveButtons() {
      quickButtons.forEach(function (btn) {
        btn.classList.remove("active");
      });
    }

    /* 현재 입력된 기간이 어떤 빠른 선택 버튼과 일치하는지 찾아 표시한다. */
    function syncActiveButtonFromInputs() {
      clearActiveButtons();

      const startValue = startInput.value;
      const endValue = endInput.value;

      if (!startValue && !endValue) {
        const allBtn = document.querySelector('.refund-quick-btn[data-range="all"]');
        if (allBtn) allBtn.classList.add("active");
        return;
      }

      if (!startValue || !endValue) return;
      if (endValue !== toDateInputValue(new Date())) return;

      quickButtons.forEach(function (btn) {
        const range = btn.dataset.range;
        if (range === "all") return;

        const expectedStart = new Date();
        expectedStart.setDate(expectedStart.getDate() - Number(range));

        if (startValue === toDateInputValue(expectedStart)) {
          btn.classList.add("active");
        }
      });
    }

    quickButtons.forEach(function (btn) {
      btn.addEventListener("click", function () {
        const range = btn.dataset.range;

        if (range === "all") {
          startInput.value = "";
          endInput.value = "";
        } else {
          const end = new Date();
          const start = new Date();
          start.setDate(start.getDate() - Number(range));

          startInput.value = toDateInputValue(start);
          endInput.value = toDateInputValue(end);
        }

        clearActiveButtons();
        btn.classList.add("active");

        refundForm.submit();
      });
    });

    /* 날짜를 직접 고치면 더 이상 빠른 선택과 일치하지 않으므로 선택을 해제한다. */
    [startInput, endInput].forEach(function (input) {
      input.addEventListener("change", clearActiveButtons);
    });

    /* [추가] 시작일이 종료일보다 늦으면 조회 전에 안내하고 요청을 막는다. */
    refundForm.addEventListener("submit", function (e) {
      if (startInput.value && endInput.value && startInput.value > endInput.value) {
        e.preventDefault();
        alert("조회 시작일은 종료일보다 늦을 수 없습니다.");
      }
    });

    syncActiveButtonFromInputs();
  }
});
