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
    // 리뷰 신고 모달 로직은 goodsDetail.jsp 와 공용으로 쓰기 위해
    // report.js 로 분리했다. (data-review-type/.report-btn 등은 그대로 유지)

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
});
