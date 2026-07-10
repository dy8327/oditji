
document.addEventListener("DOMContentLoaded", () => {

    // ================= TAB =================
    const tabs = document.querySelectorAll(".tab-btn");
    const contents = document.querySelectorAll(".tab-content");

    tabs.forEach(tab => {

        tab.addEventListener("click", () => {

            const target = tab.dataset.tab;

            tabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");

            contents.forEach(c => c.classList.remove("active"));
            document.getElementById(target + "-tab")
                .classList.add("active");

        });

    });


    // ================= FAVORITE / WISH TOGGLE =================
    // 콘텐츠 찜(FAVORITE 테이블)과 상품 찜(PRODUCT_WISH 테이블)은
    // 별도 테이블 / 엔드포인트이므로 data-type으로 분기한다.
    const buttons = document.querySelectorAll(".fav-btn");

    buttons.forEach(btn => {

        let loading = false;

        btn.addEventListener("click", async () => {

            if (loading) return;
            loading = true;

            const type = btn.dataset.type; // "content" | "goods"

            const endpoint =
                type === "goods"
                    ? "/wish/toggle"
                    : "/favorite/toggle";

            const body =
                type === "goods"
                    ? { productNo: btn.dataset.productNo }
                    : { contentNo: btn.dataset.contentNo };

            try {

                const res = await fetch(endpoint, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(body)
                });

                const data = await res.json();

                if (data.result === "added") {

                    btn.classList.add("active");
                    btn.innerText = "♥";
                    updateCount(type, +1);

                } else {

                    btn.classList.remove("active");
                    btn.innerText = "♡";
                    updateCount(type, -1);

                    const card = btn.closest(".favorite-card");
                    const grid = card ? card.closest(".favorite-grid") : null;

                    if (card) card.remove();

                    // 탭이 비면 empty-state 노출
                    if (grid && grid.children.length === 0) {

                        const emptyMessage =
                            type === "goods"
                                ? "찜한 상품이 없습니다."
                                : "찜한 콘텐츠가 없습니다.";

                        const empty = document.createElement("div");
                        empty.className = "empty-state";
                        empty.innerText = emptyMessage;

                        grid.parentElement.insertBefore(empty, grid);
                    }
                }

            } catch (err) {
                console.error(err);
            } finally {
                loading = false;
            }

        });

    });


    // ================= COUNT UPDATE =================
    function updateCount(type, diff) {

        const tabCountId =
            type === "goods" ? "goodsTabCount" : "contentTabCount";

        const tabCountEl = document.getElementById(tabCountId);
        const totalCountEl = document.getElementById("favCount");

        [tabCountEl, totalCountEl].forEach(el => {

            if (!el) return;

            let count = parseInt(el.innerText || "0", 10);
            count += diff;

            if (count < 0) count = 0;

            el.innerText = count;
        });
    }

});
