
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


    // ================= FAVORITE TOGGLE =================
    const buttons = document.querySelectorAll(".fav-btn");

    buttons.forEach(btn => {

        let loading = false;

        btn.addEventListener("click", async () => {

            if (loading) return;
            loading = true;

            const contentNo = btn.dataset.contentNo;

            try {

                const res = await fetch("/favorite/toggle", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({ contentNo })
                });

                const data = await res.json();

                if (data.result === "added") {
                    btn.classList.add("active");
                    btn.innerText = "♥";
                    updateCount(+1);
                } else {
                    btn.classList.remove("active");
                    btn.innerText = "♡";
                    updateCount(-1);
                    btn.closest(".favorite-card").remove();
                }

            } catch (err) {
                console.error(err);
            } finally {
                loading = false;
            }

        });

    });


    // ================= COUNT UPDATE =================
    function updateCount(diff) {

        const badge = document.getElementById("favCount");

        if (!badge) return;

        let count = parseInt(badge.innerText || "0");

        count += diff;

        if (count < 0) count = 0;

        badge.innerText = count;
    }

});