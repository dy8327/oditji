document.addEventListener("DOMContentLoaded", () => {

    const modal = document.getElementById("reviewModal");

    if (!modal) return;

    const orderItemNo = document.getElementById("orderItemNo");
    const productNo = document.getElementById("productNo");

    document.querySelectorAll(".review-btn").forEach(btn => {

        btn.addEventListener("click", (e) => {

            e.preventDefault();
            e.stopPropagation();

            orderItemNo.value = btn.dataset.orderItem;
            productNo.value = btn.dataset.product;

            modal.style.display = "flex";

        });

    });

    document.getElementById("closeReviewModal").addEventListener("click", () => {
        modal.style.display = "none";
    });

});