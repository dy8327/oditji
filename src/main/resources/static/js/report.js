/**
 * 리뷰 신고 모달 공통 스크립트.
 *
 * contentDetail.jsp, goodsDetail.jsp 양쪽에서 동일한 마크업(#reportModal 등)과
 * 동일한 트리거(.report-btn, data-review-type, data-review-no)를 사용하므로
 * 이 스크립트 하나로 두 페이지 모두 처리한다.
 *
 * data-review-type 값:
 *   - "CONTENT" : 콘텐츠 리뷰(REVIEW) 신고
 *   - "PRODUCT" : 상품 리뷰(PRODUCT_REVIEW) 신고
 */
document.addEventListener("DOMContentLoaded", () => {

    const reportModal =
        document.getElementById("reportModal");

    // 이 페이지에 신고 모달 자체가 없으면 아무 것도 하지 않는다.
    if (!reportModal) {
        return;
    }

    const contextPath = resolveContextPath();

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

    reportModal.addEventListener(
        "click",
        (event) => {

            if (event.target === reportModal) {
                closeReportModal();
            }
        }
    );

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

                    await showAlert(
                        "신고 대상 리뷰를 찾을 수 없습니다.",
                        "warning"
                    );

                    return;
                }

                if (!reason) {

                    await showAlert(
                        "신고 사유를 선택해주세요.",
                        "warning"
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
                            `${contextPath}/report/submit`,
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

                    if (response.status === 401) {

                        closeReportModal();

                        const moveLogin =
                            await showConfirm(
                                "신고는 로그인 후 이용할 수 있습니다.\n로그인 페이지로 이동하시겠습니까?",
                                "info"
                            );

                        if (moveLogin) {

                            const currentUrl =
                                window.location.pathname
                                + window.location.search;

                            window.location.href =
                                `${contextPath}/member/login`
                                + `?redirect=${encodeURIComponent(currentUrl)}`;
                        }

                        return;
                    }

                    if (response.status === 409) {

                        await showAlert(
                            "이미 신고한 리뷰입니다.",
                            "warning"
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

                        await showAlert(
                            "신고가 접수되었습니다.",
                            "success"
                        );

                        markAsReported();
                        closeReportModal();

                    } else {

                        await showAlert(
                            data.message
                            || "신고 처리에 실패했습니다.",
                            "error"
                        );
                    }

                } catch (error) {

                    console.error(error);
                    await showAlert("신고 처리 실패", "error");

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

    function resolveContextPath() {

        // body[data-context-path] (goodsDetail.jsp) 또는
        // #contentPageData[data-context-path] (contentDetail.jsp) 등
        // 페이지 어디에 있든 첫 번째로 발견되는 값을 사용한다.
        const element =
            document.querySelector("[data-context-path]");

        return element
            ? (element.dataset.contextPath || "")
            : "";
    }
});
