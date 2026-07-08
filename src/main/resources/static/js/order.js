document.addEventListener("DOMContentLoaded", () => {

    const orderBtn = document.getElementById("orderBtn");

    orderBtn.addEventListener("click", async () => {

        const receiver = document.getElementById("receiver").value;
        const address = document.getElementById("address").value;
        const phone = document.getElementById("phone").value;

        if (!receiver || !address || !phone) {
            alert("배송 정보를 입력하세요");
            return;
        }

        const orderData = {
            receiver,
            address,
            phone
        };

        try {

            const res = await fetch("/order/submit", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(orderData)
            });

            if (!res.ok) throw new Error("order failed");

            const data = await res.json();

            // orderId 받기
            window.location.href = `/order/complete?orderId=${data.orderId}`;

        } catch (err) {
            console.error(err);
            alert("주문 실패");
        }

    });

});