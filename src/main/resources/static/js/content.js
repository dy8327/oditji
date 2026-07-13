document.addEventListener("DOMContentLoaded", () => {

    // ======================
    // 리뷰 신고 모달
    // ======================
    const reportModal = document.getElementById("reportModal");
    const reportReviewType = document.getElementById("reportReviewType");
    const reportReviewNo = document.getElementById("reportReviewNo");
    const reportReason = document.getElementById("reportReason");
    const reportDetail = document.getElementById("reportDetail");
    const reportCancelBtn = document.getElementById("reportCancelBtn");
    const reportSubmitBtn = document.getElementById("reportSubmitBtn");

    let reportProcessing = false;
    let reportTargetBtn = null;

    // 신고 버튼 클릭 -> 모달 오픈
    document.addEventListener("click", (e) => {

        const btn = e.target.closest(".report-btn");
        if (!btn || btn.classList.contains("reported")) return;

        if (!reportModal) return;

        reportTargetBtn = btn;
        openReportModal(btn.dataset.reviewType, btn.dataset.reviewNo);

    });

    // 취소 버튼
    if (reportCancelBtn) {
        reportCancelBtn.addEventListener("click", closeReportModal);
    }

    // 배경 클릭 시 닫기
    if (reportModal) {
        reportModal.addEventListener("click", (e) => {
            if (e.target === reportModal) {
                closeReportModal();
            }
        });
    }

    // 신고하기
    if (reportSubmitBtn) {

        reportSubmitBtn.addEventListener("click", async () => {

            if (reportProcessing) return;

            const reviewType = reportReviewType.value;
            const reviewNo = reportReviewNo.value;
            const reason = reportReason.value;
            const detail = reportDetail.value.trim();

            if (!reviewNo) {
                alert("신고 대상 리뷰를 찾을 수 없습니다.");
                return;
            }

            if (!reason) {
                alert("신고 사유를 선택해주세요.");
                return;
            }

            const payload = {
                reviewType,
                reason,
                detail
            };

            if (reviewType === "PRODUCT") {
                payload.productReviewNo = reviewNo;
            } else {
                payload.contentReviewNo = reviewNo;
            }

            reportProcessing = true;
            reportSubmitBtn.disabled = true;

            try {

                const res = await fetch(`${contextPath}/review/report`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(payload)
                });

                if (res.status === 409) {
                    alert("이미 신고한 리뷰입니다.");
                    markAsReported();
                    closeReportModal();
                    return;
                }

                if (!res.ok) {
                    throw new Error("서버 오류");
                }

                const data = await res.json();

                if (data.success) {

                    alert("신고가 접수되었습니다.");
                    markAsReported();
                    closeReportModal();

                } else {

                    alert(data.message || "신고 처리에 실패했습니다.");

                }

            } catch (err) {

                console.error(err);
                alert("신고 처리 실패");

            } finally {

                reportProcessing = false;
                reportSubmitBtn.disabled = false;

            }

        });

    }

    function openReportModal(reviewType, reviewNo) {

        reportReviewType.value = reviewType;
        reportReviewNo.value = reviewNo;
        reportReason.selectedIndex = 0;
        reportDetail.value = "";
        reportModal.hidden = false;

    }

    function closeReportModal() {

        reportModal.hidden = true;
        reportReviewType.value = "";
        reportReviewNo.value = "";
        reportDetail.value = "";
        reportTargetBtn = null;

    }

    function markAsReported() {

        if (!reportTargetBtn) return;

        reportTargetBtn.outerHTML =
            '<span class="report-btn reported" aria-disabled="true">신고완료</span>';

    }

});