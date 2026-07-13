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

            const targetTab = document.getElementById(target + "-tab");
            if (targetTab) {
                targetTab.classList.add("active");
            }

        });

    });


    // ================= FAVORITE / WISH =================
    document.addEventListener("click", async (e) => {

        const btn = e.target.closest(".fav-btn");
        if (!btn) return;

        if (btn.dataset.loading === "true") return;
        btn.dataset.loading = "true";

        const type = btn.dataset.type;

        const endpoint =
            (type === "goods")
                ? `${contextPath}/wish/toggle`
                : `${contextPath}/favorite/toggle`;

        const body =
            (type === "goods")
                ? {
                    productNo: btn.dataset.productNo
                }
                : {
                    contentNo: btn.dataset.contentNo
                };

        try {

            const res = await fetch(endpoint, {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(body)

            });

            if (res.status === 401) {

                alert("로그인이 필요합니다.");
                return;

            }

            if (!res.ok) {
                throw new Error("서버 오류");
            }

            const data = await res.json();

            if (data.active) {

                btn.classList.add("active");
                btn.textContent = "♥";

                updateCount(type, 1);

            } else {

                btn.classList.remove("active");
                btn.textContent = "♡";

                updateCount(type, -1);

                // 마이페이지에서는 카드 제거
                const card = btn.closest(".favorite-card");

                if (card) {

                    const grid = card.parentElement;

                    card.remove();

                    if (grid &&
                        grid.querySelectorAll(".favorite-card").length === 0) {

                        const empty = document.createElement("div");

                        empty.className = "empty-state";

                        empty.textContent =
                            type === "goods"
                                ? "찜한 상품이 없습니다."
                                : "찜한 콘텐츠가 없습니다.";

                        grid.parentElement.insertBefore(empty, grid);
                    }
                }
            }

        } catch (err) {

            console.error(err);
            alert("찜 처리 중 오류가 발생했습니다.");

        } finally {

            btn.dataset.loading = "false";

        }

    });


    // ================= COUNT =================
    function updateCount(type, diff) {

        const tabCountId =
            type === "goods"
                ? "goodsTabCount"
                : "contentTabCount";

        const tabCount = document.getElementById(tabCountId);
        const totalCount = document.getElementById("favCount");

        [tabCount, totalCount].forEach(el => {

            if (!el) return;

            let count = parseInt(el.textContent || "0", 10);

            count += diff;

            if (count < 0) {
                count = 0;
            }

            el.textContent = count;

        });

    }

});