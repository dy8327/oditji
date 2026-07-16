document.addEventListener("DOMContentLoaded", () => {

    const contextPathElement =
        document.getElementById("contentPageData");

    const pageContextPath =
        contextPathElement
            ? contextPathElement.dataset.contextPath || ""
            : "";

    const favoriteButton =
        document.getElementById("detailFavoriteBtn");

    let favoriteProcessing = false;

    // ======================
    // 콘텐츠 찜
    // ======================

    if (favoriteButton) {

        favoriteButton.addEventListener(
            "click",
            async () => {

                if (favoriteProcessing) {
                    return;
                }

                const loginRequired =
                    favoriteButton.dataset.loginRequired === "true";

                if (loginRequired) {

                    const moveLogin =
                        confirm(
                            "찜 기능은 로그인 후 이용할 수 있습니다.\n로그인 페이지로 이동하시겠습니까?"
                        );

                    if (moveLogin) {

                        const currentUrl =
                            window.location.pathname
                            + window.location.search;

                        window.location.href =
                            `${pageContextPath}/member/login`
                            + `?returnUrl=${encodeURIComponent(currentUrl)}`;
                    }

                    return;
                }

                const contentNo =
                    Number(favoriteButton.dataset.contentNo);

                if (!Number.isInteger(contentNo)
                        || contentNo <= 0) {

                    alert("콘텐츠 정보를 확인할 수 없습니다.");
                    return;
                }

                favoriteProcessing = true;
                favoriteButton.disabled = true;
                favoriteButton.classList.add("is-processing");

                try {

                    const response =
                        await fetch(
                            `${pageContextPath}/favorite/toggle`,
                            {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                body: JSON.stringify({
                                    contentNo: contentNo
                                })
                            }
                        );

                    if (response.status === 401) {

                        favoriteButton.dataset.loginRequired =
                            "true";

                        alert(
                            "로그인 정보가 만료되었습니다. 다시 로그인해주세요."
                        );

                        return;
                    }

                    if (!response.ok) {
                        throw new Error(
                            `찜 처리 실패: ${response.status}`
                        );
                    }

                    const result =
                        await response.json();

                    updateFavoriteButton(
                        favoriteButton,
                        result.active === true
                    );

                } catch (error) {

                    console.error(error);

                    alert(
                        "찜 처리 중 오류가 발생했습니다."
                    );

                } finally {

                    favoriteProcessing = false;
                    favoriteButton.disabled = false;
                    favoriteButton.classList.remove(
                        "is-processing"
                    );
                }
            }
        );
    }

    function updateFavoriteButton(
        button,
        active) {

        button.dataset.active =
            active ? "true" : "false";

        button.classList.toggle(
            "is-active",
            active
        );

        button.setAttribute(
            "aria-pressed",
            active ? "true" : "false"
        );

        button.textContent =
            active
                ? "♥ 찜 완료"
                : "♡ 찜하기";
    }

    // ======================
    // 리뷰 신고 모달
    // ======================

    const reportModal =
        document.getElementById("reportModal");

    const reportReviewType =
        document.getElementById("reportReviewType");

    const reportReviewNo =
        document.getElementById("reportReviewNo");

    const reportReason =
        document.getElementById("reportReason");

    const reportDetail =
        document.getElementById("reportDetail");

    const reportCancelBtn =
        document.getElementById("reportCancelBtn");

    const reportSubmitBtn =
        document.getElementById("reportSubmitBtn");

    let reportProcessing = false;
    let reportTargetBtn = null;

    document.addEventListener("click", (event) => {

        const button =
            event.target.closest(".report-btn");

        if (!button
                || button.classList.contains("reported")) {

            return;
        }

        if (!reportModal) {
            return;
        }

        reportTargetBtn = button;

        openReportModal(
            button.dataset.reviewType,
            button.dataset.reviewNo
        );
    });

    if (reportCancelBtn) {

        reportCancelBtn.addEventListener(
            "click",
            closeReportModal
        );
    }

    if (reportModal) {

        reportModal.addEventListener(
            "click",
            (event) => {

                if (event.target === reportModal) {
                    closeReportModal();
                }
            }
        );
    }

    if (reportSubmitBtn) {

        reportSubmitBtn.addEventListener(
            "click",
            async () => {

                if (reportProcessing) {
                    return;
                }

                const reviewType =
                    reportReviewType
                        ? reportReviewType.value
                        : "";

                const reviewNo =
                    reportReviewNo
                        ? reportReviewNo.value
                        : "";

                const reason =
                    reportReason
                        ? reportReason.value
                        : "";

                const detail =
                    reportDetail
                        ? reportDetail.value.trim()
                        : "";

                if (!reviewNo) {

                    alert(
                        "신고 대상 리뷰를 찾을 수 없습니다."
                    );

                    return;
                }

                if (!reason) {

                    alert(
                        "신고 사유를 선택해주세요."
                    );

                    return;
                }

                const payload = {
                    reviewType: reviewType,
                    reason: reason,
                    detail: detail
                };

                if (reviewType === "PRODUCT") {

                    payload.productReviewNo =
                        reviewNo;

                } else {

                    payload.contentReviewNo =
                        reviewNo;
                }

                reportProcessing = true;
                reportSubmitBtn.disabled = true;

                try {

                    const response =
                        await fetch(
                            `${pageContextPath}/review/report`,
                            {
                                method: "POST",
                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },
                                body: JSON.stringify(
                                    payload
                                )
                            }
                        );

                    if (response.status === 409) {

                        alert(
                            "이미 신고한 리뷰입니다."
                        );

                        markAsReported();
                        closeReportModal();

                        return;
                    }

                    if (!response.ok) {
                        throw new Error("서버 오류");
                    }

                    const data =
                        await response.json();

                    if (data.success) {

                        alert(
                            "신고가 접수되었습니다."
                        );

                        markAsReported();
                        closeReportModal();

                    } else {

                        alert(
                            data.message
                            || "신고 처리에 실패했습니다."
                        );
                    }

                } catch (error) {

                    console.error(error);
                    alert("신고 처리 실패");

                } finally {

                    reportProcessing = false;
                    reportSubmitBtn.disabled = false;
                }
            }
        );
    }

    function openReportModal(
        reviewType,
        reviewNo) {

        if (!reportModal
                || !reportReviewType
                || !reportReviewNo
                || !reportReason
                || !reportDetail) {

            return;
        }

        reportReviewType.value =
            reviewType || "";

        reportReviewNo.value =
            reviewNo || "";

        reportReason.selectedIndex = 0;
        reportDetail.value = "";
        reportModal.hidden = false;
    }

    function closeReportModal() {

        if (!reportModal) {
            return;
        }

        reportModal.hidden = true;

        if (reportReviewType) {
            reportReviewType.value = "";
        }

        if (reportReviewNo) {
            reportReviewNo.value = "";
        }

        if (reportDetail) {
            reportDetail.value = "";
        }

        reportTargetBtn = null;
    }

    function markAsReported() {

        if (!reportTargetBtn) {
            return;
        }

        const reportedElement =
            document.createElement("span");

        reportedElement.className =
            "report-btn reported";

        reportedElement.setAttribute(
            "aria-disabled",
            "true"
        );

        reportedElement.textContent =
            "🚨 신고완료";

        reportTargetBtn.replaceWith(
            reportedElement
        );
    }
});