document.addEventListener("DOMContentLoaded", () => {

    updateTotal();

    // =========================
    // 수량 증가 / 감소
    // =========================
    document.addEventListener("click", async (e) => {

        const btn = e.target.closest(".qty-btn");
        if (!btn) return;

        const cartId = btn.dataset.cartId;
        const price = parseInt(btn.dataset.price);

        const input = document.querySelector(
            `.qty-input[data-cart-id="${cartId}"]`
        );

        let qty = parseInt(input.value);

        if (btn.classList.contains("plus")) {
            qty++;
        }

        if (btn.classList.contains("minus")) {
            qty = Math.max(1, qty - 1);
        }

        input.value = qty;

        updateRow(cartId, qty, price);
        updateTotal();

        await syncCart(cartId, qty);
    });

    // =========================
    // 직접 입력
    // =========================
    document.addEventListener("input", (e) => {

        const input = e.target.closest(".qty-input");
        if (!input) return;

        const cartId = input.dataset.cartId;
        const price = parseInt(input.dataset.price);

        let qty = parseInt(input.value);
        if (isNaN(qty) || qty < 1) qty = 1;

        input.value = qty;

        updateRow(cartId, qty, price);
        updateTotal();
    });

    // =========================
    // 삭제
    // =========================
    document.addEventListener("click", async (e) => {

        const btn = e.target.closest(".cart-delete-btn");
        if (!btn) return;

        const cartId = btn.dataset.cartId;

        await fetch("/cart/delete", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({ cartId })
        });

        btn.closest(".cart-item").remove();

        updateTotal();
    });

    // =========================
    // ROW 계산
    // =========================
    function updateRow(cartId, qty, price) {

        const row = document.querySelector(
            `.cart-item__info .qty-input[data-cart-id="${cartId}"]`
        ).closest(".cart-item");

        const subtotal = row.querySelector(".subtotal");
        subtotal.innerText = qty * price;
    }

    // =========================
    // TOTAL 계산
    // =========================
    function updateTotal() {

        let total = 0;

        document.querySelectorAll(".cart-item").forEach(row => {

            const price = parseInt(
                row.querySelector(".unit-price").innerText
            );

            const qty = parseInt(
                row.querySelector(".qty-input").value
            );

            total += price * qty;
        });

        document.getElementById("totalPrice").innerText = total;
        document.getElementById("finalPrice").innerText = total;
    }

    // =========================
    // BACK SYNC
    // =========================
    async function syncCart(cartId, qty) {

        await fetch("/cart/update", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({
                cartId,
                quantity: qty
            })
        });
    }

});