document.addEventListener("DOMContentLoaded", () => {

    let isProcessing = false;

    document.addEventListener("click", async (e) => {

        const btn = e.target.closest(".fav-btn");
        if (!btn) return;

        if (isProcessing) return;
        isProcessing = true;

        const contentNo = btn.dataset.contentNo;

        try {

            const res = await fetch("/favorite/toggle", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    targetType: "content",
                    targetId: contentNo
                })
            });

            if (!res.ok) throw new Error("서버 오류");

            const data = await res.json();

            // ======================
            // TOGGLE STATE (표준화)
            // ======================
            setFavoriteUI(btn, data.active);

        } catch (err) {
            console.error(err);
            alert("찜 처리 실패");
        } finally {
            isProcessing = false;
        }
    });

    // ======================
    // UI UPDATE (표준 함수)
    // ======================
    function setFavoriteUI(btn, isActive) {

        if (isActive) {
            btn.classList.add("active");
            btn.innerText = "♥";
        } else {
            btn.classList.remove("active");
            btn.innerText = "♡";
        }
    }

});