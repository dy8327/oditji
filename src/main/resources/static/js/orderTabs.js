/*
 * =========================================================
 * 주문 내역 / 환불 내역 탭 전환 + 상품 선택 UI
 *
 * orderList.jsp에서 사용하는 화면(UI) 전용 스크립트이다.
 *
 * 이 파일이 담당하는 범위:
 * 1) "주문 내역" / "환불 내역" 탭 전환
 * 2) 주문 카드별 상품 체크박스 선택 + "선택 상품 주문 취소" 버튼
 *    (기존 "주문 전체 취소" 버튼 자리에 위치하며, 해당 주문
 *    카드 안에서 체크한 상품만 대상으로 한다)
 * 3) 환불 내역 표의 체크박스 선택 + "선택 상품 환불" 버튼
 * 4) 환불 내역 표가 비어있을 때 빈 상태 문구 표시
 *
 * 상품별 액션 영역에는 "배송조회"와 "리뷰 작성"만 남겨두고,
 * 상태별 개별 "주문 취소" / "환불 신청" 버튼은 두지 않는다.
 * 취소/환불은 모두 체크박스로 선택한 뒤 버튼 한 번으로 요청하는
 * 방식으로 통일한다.
 *
 * 위 2), 3)은 현재 화면(UI)만 구현한 상태이며, 실제 서버 요청은
 * 발생하지 않는다. 이후 Java 로직과 연동할 때 fetch 호출부만
 * 추가하면 되도록 함수를 분리해두었다.
 * =========================================================
 */

document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  const orderListContainer = document.querySelector(".order-list-container");

  if (!orderListContainer) {
    return;
  }

  /*
   * =========================================================
   * 1) 주문 내역 / 환불 내역 탭 전환
   * =========================================================
   */

  const tabButtons = document.querySelectorAll(".order-tab-btn");
  const tabPanels = document.querySelectorAll(".order-tab-panel");

  function activateTab(targetId) {
    tabButtons.forEach(function (btn) {
      const isActive = btn.dataset.tabTarget === targetId;

      btn.classList.toggle("active", isActive);
      btn.setAttribute("aria-selected", isActive ? "true" : "false");
    });

    tabPanels.forEach(function (panel) {
      const isActive = panel.id === targetId;

      panel.classList.toggle("active", isActive);

      if (isActive) {
        panel.removeAttribute("hidden");
      } else {
        panel.setAttribute("hidden", "");
      }
    });
  }

  tabButtons.forEach(function (btn) {
    btn.addEventListener("click", function () {
      const targetId = btn.dataset.tabTarget;

      if (!targetId) {
        return;
      }

      activateTab(targetId);
    });
  });

  /*
   * =========================================================
   * 2) 주문 카드별 상품 선택 체크박스 + 선택 상품 주문 취소
   *
   * 버튼은 기존 "주문 전체 취소" 자리(order-cancel-action)에
   * 있으며, 해당 주문 카드(.order-card) 안에서 체크한 상품만
   * 대상으로 한다. 현재는 화면(UI)만 구현하며, 선택한 상품
   * 정보를 알림으로만 보여준다. 실제 취소 요청 전송은 하지 않는다.
   * =========================================================
   */

  document.querySelectorAll(".order-select-cancel-btn").forEach(function (button) {
    button.addEventListener("click", function () {
      const orderCard = button.closest(".order-card");

      if (!orderCard) {
        return;
      }

      const checkedBoxes = orderCard.querySelectorAll(".order-item-select:checked");

      if (checkedBoxes.length === 0) {
        alert("취소할 상품을 선택해주세요.");
        return;
      }

      const productNames = Array.from(checkedBoxes).map(function (checkbox) {
        return checkbox.dataset.productName || "선택한 상품";
      });

      alert(
        "주문번호 " +
          (button.dataset.orderNo || "") +
          "에서 선택한 " +
          checkedBoxes.length +
          "개 상품의 주문 취소를 요청합니다.\n\n" +
          productNames.join(", ") +
          "\n\n(현재는 화면 UI만 제공되며, 실제 취소 처리는 추후 연동됩니다.)"
      );
    });
  });

  /*
   * =========================================================
   * 3) 환불 내역 - 상품 선택 체크박스 + 선택 상품 환불
   *
   * 현재는 화면(UI)만 구현하며, 실제 환불 요청 전송은 하지 않는다.
   * =========================================================
   */

  const bulkRefundBtn = document.getElementById("bulkRefundBtn");

  if (bulkRefundBtn) {
    bulkRefundBtn.addEventListener("click", function () {
      const checkedBoxes = document.querySelectorAll(".refund-select:checked");

      if (checkedBoxes.length === 0) {
        alert("환불을 요청할 상품을 선택해주세요.");
        return;
      }

      alert(
        "선택한 " +
          checkedBoxes.length +
          "건의 환불을 요청합니다.\n\n(현재는 화면 UI만 제공되며, 실제 환불 처리는 추후 연동됩니다.)"
      );
    });
  }

  /*
   * =========================================================
   * 4) 환불 내역 표 빈 상태 처리
   * =========================================================
   */

  const refundTableBody = document.getElementById("refundTableBody");
  const refundEmptyBox = document.getElementById("refundEmptyBox");
  const refundTableWrap = document.querySelector(".refund-table-wrap");

  if (refundTableBody && refundEmptyBox) {
    const hasRefundRows = refundTableBody.querySelectorAll(".refund-row").length > 0;

    if (!hasRefundRows) {
      refundEmptyBox.style.display = "block";

      if (refundTableWrap) {
        refundTableWrap.style.display = "none";
      }
    }
  }
});
