document.addEventListener("DOMContentLoaded", () => {

    const tabs = document.querySelectorAll(".review-tab");
    const cards = document.querySelectorAll(".mypage-review-card");

    if (tabs.length === 0) return;

    const emptyBox = document.querySelector(".mypage-review-filter-empty");
    const emptyTitle = document.querySelector(".mypage-filter-empty-title");
    const emptyMessage = document.querySelector(".mypage-filter-empty-message");

    // 개수 계산
    const allCount = cards.length;
    const contentCount = document.querySelectorAll('.mypage-review-card[data-type="CONTENT"]').length;
    const productCount = document.querySelectorAll('.mypage-review-card[data-type="PRODUCT"]').length;

    document.getElementById("allCount").textContent = allCount;
    document.getElementById("contentCount").textContent = contentCount;
    document.getElementById("productCount").textContent = productCount;

    tabs.forEach(tab => {

        tab.addEventListener("click", () => {

            tabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");

            const type = tab.dataset.type;

            let visibleCount = 0;

            cards.forEach(card => {

                if (type === "ALL" || card.dataset.type === type) {
                    card.style.display = "";
                    visibleCount++;
                } else {
                    card.style.display = "none";
                }

            });

            if (type === "ALL") {
                emptyBox.style.display = "none";
                return;
            }

            if (visibleCount === 0) {

                emptyBox.style.display = "block";

                if (type === "CONTENT") {
                    emptyTitle.textContent = "아직 작성한 콘텐츠 리뷰가 없습니다.";
                    emptyMessage.textContent = "마음에 드는 콘텐츠에 첫 리뷰를 작성해보세요.";
                } else {
                    emptyTitle.textContent = "아직 작성한 상품 리뷰가 없습니다.";
                    emptyMessage.textContent = "구매한 상품에 첫 리뷰를 작성해보세요.";
                }

            } else {
                emptyBox.style.display = "none";
            }

        });

    });

});