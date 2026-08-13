document.addEventListener("DOMContentLoaded", () => {
  const orderBtn = document.getElementById("orderBtn");

  // [수정] 주문 버튼이 있는 페이지에서만 주문 이벤트를 등록한다.
  if (orderBtn) {
    orderBtn.addEventListener("click", async () => {
      const receiver = document.getElementById("receiver").value;
      const address = document.getElementById("address").value;
      const phone = document.getElementById("phone").value;

      if (!receiver || !address || !phone) {
        await showAlert("배송 정보를 입력하세요", "warning");
        return;
      }

      const orderData = {
        receiver,
        address,
        phone,
      };

      try {
        const res = await fetch("/order/submit", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(orderData),
        });

        if (!res.ok) {
          throw new Error("order failed");
        }

        const data = await res.json();

        window.location.href = `/order/complete?orderId=${data.orderId}`;
      } catch (err) {
        console.error(err);
        await showAlert("주문 실패", "error");
      }
    });
  }

  /**
   * [추가] 리뷰 작성 실패 메시지를
   * 에러 페이지 대신 alert로 표시한다.
   */
  const reviewMessageData = document.getElementById("reviewMessageData");

  if (reviewMessageData) {
    const reviewMessage = reviewMessageData.dataset.message;

    if (reviewMessage) {
      showAlert(reviewMessage, "info");
    }
  }
});
