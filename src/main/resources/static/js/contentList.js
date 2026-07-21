document.addEventListener("DOMContentLoaded", function () {

    initializeContentFilters();
    initializeContentGenreToggle();
    initializeContentOttPlatformModal();
    initializeRecommendCarousel();
    initializeContentListFavorites();

});


function initializeContentFilters() {

    const form =
        document.getElementById("contentFilterForm");

    if (!form) {
        return;
    }

    const allCheckboxes =
        form.querySelectorAll(
            "input[data-content-filter-all]"
        );

    const itemCheckboxes =
        form.querySelectorAll(
            "input[data-content-filter-item]"
        );

    itemCheckboxes.forEach(function (checkbox) {

        checkbox.addEventListener(
            "change",
            function () {

                updateContentFilterAllState(
                    form,
                    checkbox.dataset.contentFilterGroup
                );
            }
        );
    });

    allCheckboxes.forEach(function (allCheckbox) {

        allCheckbox.addEventListener(
            "change",
            function () {

                const groupName =
                    allCheckbox.dataset.contentFilterGroup;

                const groupItems =
                    form.querySelectorAll(
                        "input[data-content-filter-item]"
                        + "[data-content-filter-group='"
                        + groupName
                        + "']"
                    );

                if (allCheckbox.checked) {

                    groupItems.forEach(function (item) {
                        item.checked = false;
                    });

                    if (groupName === "provider") {
                        updateContentOttSelectedSummary();
                    }

                    return;
                }

                const checkedItems =
                    form.querySelectorAll(
                        "input[data-content-filter-item]"
                        + "[data-content-filter-group='"
                        + groupName
                        + "']:checked"
                    );

                if (checkedItems.length === 0) {
                    allCheckbox.checked = true;
                }
            }
        );
    });

    form.addEventListener("submit", function () {

        const pageInput =
            form.querySelector(
                "input[name='page']"
            );

        if (pageInput) {
            pageInput.value = "1";
        }
    });
}


function updateContentFilterAllState(
        form,
        groupName) {

    const allCheckbox =
        form.querySelector(
            "input[data-content-filter-all]"
            + "[data-content-filter-group='"
            + groupName
            + "']"
        );

    if (!allCheckbox) {
        return;
    }

    const checkedItems =
        form.querySelectorAll(
            "input[data-content-filter-item]"
            + "[data-content-filter-group='"
            + groupName
            + "']:checked"
        );

    allCheckbox.checked =
        checkedItems.length === 0;

    if (groupName === "provider") {
        updateContentOttSelectedSummary();
    }
}


function initializeContentGenreToggle() {

    const button =
        document.getElementById(
            "contentGenreToggleButton"
        );

    const extraGenres =
        document.getElementById(
            "contentExtraGenres"
        );

    if (!button || !extraGenres) {
        return;
    }

    button.addEventListener(
        "click",
        function () {

            const expanded =
                button.getAttribute(
                    "aria-expanded"
                ) === "true";

            extraGenres.hidden =
                expanded;

            button.setAttribute(
                "aria-expanded",
                expanded ? "false" : "true"
            );

            button.textContent =
                expanded
                    ? "장르 전체보기"
                    : "장르 접기";
        }
    );
}


function initializeRecommendCarousel() {

    const carousels =
        document.querySelectorAll(
            "[data-recommend-carousel]"
        );

    carousels.forEach(function (carousel) {

        const viewport =
            carousel.querySelector(
                "[data-recommend-viewport]"
            );

        const slides =
            Array.from(
                carousel.querySelectorAll(
                    "[data-recommend-slide]"
                )
            );

        const previousButton =
            carousel.querySelector(
                "[data-recommend-previous]"
            );

        const nextButton =
            carousel.querySelector(
                "[data-recommend-next]"
            );

        const currentElement =
            carousel.querySelector(
                "[data-recommend-current]"
            );

        const totalElement =
            carousel.querySelector(
                "[data-recommend-total]"
            );

        if (!viewport || slides.length === 0) {
            return;
        }

        let currentIndex = 0;
        let scrollTimer = null;

        if (totalElement) {
            totalElement.textContent =
                String(slides.length);
        }

        function updateCurrentSlide(index) {

            currentIndex =
                Math.max(
                    0,
                    Math.min(
                        index,
                        slides.length - 1
                    )
                );

            slides.forEach(
                function (slide, slideIndex) {

                    const isActive =
                        slideIndex === currentIndex;

                    slide.setAttribute(
                        "aria-hidden",
                        isActive ? "false" : "true"
                    );

                    const link =
                        slide.querySelector("a");

                    if (link) {
                        link.tabIndex =
                            isActive ? 0 : -1;
                    }
                }
            );

            if (currentElement) {
                currentElement.textContent =
                    String(currentIndex + 1);
            }
        }

        function scrollToSlide(index) {

            let nextIndex = index;

            if (nextIndex < 0) {
                nextIndex =
                    slides.length - 1;
            }

            if (nextIndex >= slides.length) {
                nextIndex = 0;
            }

            viewport.scrollTo({
                left: viewport.clientWidth
                        * nextIndex,
                behavior: "smooth"
            });

            updateCurrentSlide(nextIndex);
        }

        if (previousButton) {

            previousButton.addEventListener(
                "click",
                function () {

                    scrollToSlide(
                        currentIndex - 1
                    );
                }
            );
        }

        if (nextButton) {

            nextButton.addEventListener(
                "click",
                function () {

                    scrollToSlide(
                        currentIndex + 1
                    );
                }
            );
        }

        viewport.addEventListener(
            "scroll",
            function () {

                window.clearTimeout(
                    scrollTimer
                );

                scrollTimer =
                    window.setTimeout(
                        function () {

                            const width =
                                viewport.clientWidth;

                            if (width <= 0) {
                                return;
                            }

                            const index =
                                Math.round(
                                    viewport.scrollLeft
                                    / width
                                );

                            updateCurrentSlide(index);
                        },
                        80
                    );
            }
        );

        carousel.addEventListener(
            "keydown",
            function (event) {

                if (event.key === "ArrowLeft") {

                    event.preventDefault();

                    scrollToSlide(
                        currentIndex - 1
                    );

                } else if (event.key === "ArrowRight") {

                    event.preventDefault();

                    scrollToSlide(
                        currentIndex + 1
                    );
                }
            }
        );

        updateCurrentSlide(0);
    });
}


function initializeContentListFavorites() {

    const buttons =
        Array.from(
            document.querySelectorAll(
                "[data-content-list-favorite]"
            )
        );

    if (buttons.length === 0) {
        return;
    }

    buttons.forEach(function (button) {

        button.addEventListener(
            "click",
            function (event) {

                event.preventDefault();
                event.stopPropagation();

                toggleContentListFavorite(
                    button
                );
            }
        );

        loadContentListFavoriteStatus(
                button
        );
    });
}


async function loadContentListFavoriteStatus(
        button) {

    const tmdbId =
        button.dataset.tmdbId;

    const contentType =
        button.dataset.contentType;

    if (!tmdbId || !contentType) {
        return;
    }

    const query =
        new URLSearchParams({
            tmdbId: tmdbId,
            contentType: contentType
        });

    try {

        const response =
            await fetch(
                contextPath
                + "/favorite/status-by-tmdb?"
                + query.toString(),
                {
                    method: "GET",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

        if (!response.ok) {
            return;
        }

        const result =
            await response.json();

        setContentListFavoriteState(
            button,
            result.active === true
        );

    } catch (error) {

        console.error(
            "찜 상태 조회 실패:",
            error
        );
    }
}


async function toggleContentListFavorite(
        button) {

    if (button.dataset.loading === "true") {
        return;
    }

    const tmdbId =
        button.dataset.tmdbId;

    const contentType =
        button.dataset.contentType;

    if (!tmdbId || !contentType) {

        alert(
            "콘텐츠 정보를 확인할 수 없습니다."
        );

        return;
    }

    button.dataset.loading = "true";
    button.disabled = true;

    try {

        const response =
            await fetch(
                contextPath
                + "/favorite/toggle-by-tmdb",
                {
                    method: "POST",
                    headers: {
                        "Content-Type":
                            "application/json",
                        "Accept":
                            "application/json"
                    },
                    body: JSON.stringify({
                        tmdbId: Number(tmdbId),
                        contentType: contentType
                    })
                }
            );

        if (response.status === 401) {

            alert("로그인이 필요합니다.");
            return;
        }

        const result =
            await response.json()
                .catch(function () {
                    return {};
                });

        if (!response.ok) {

            throw new Error(
                result.message
                || "찜 처리에 실패했습니다."
            );
        }

        if (result.contentNo) {
            button.dataset.contentNo =
                String(result.contentNo);
        }

        setContentListFavoriteState(
            button,
            result.active === true
        );

    } catch (error) {

        console.error(error);

        alert(
            error.message
            || "찜 처리 중 오류가 발생했습니다."
        );

    } finally {

        button.dataset.loading = "false";
        button.disabled = false;
    }
}


function setContentListFavoriteState(
        button,
        active) {

    const icon =
        button.querySelector("span");

    button.classList.toggle(
        "is-active",
        active
    );

    button.setAttribute(
        "aria-pressed",
        active ? "true" : "false"
    );

    button.setAttribute(
        "title",
        active ? "찜 해제" : "찜하기"
    );

    if (icon) {
        icon.textContent =
            active ? "♥" : "♡";
    }
}

/**
 * 콘텐츠 목록 전용 OTT 선택 모달을 초기화합니다.
 *
 * 검색 결과 화면의 search.js를 직접 불러오지 않고,
 * 콘텐츠 목록에서 필요한 OTT 선택 기능만 독립적으로 처리합니다.
 */
function initializeContentOttPlatformModal() {

    const modal = document.getElementById("contentOttPlatformModal");
    const openButton = document.getElementById("contentOttModalOpenButton");
    const cancelButton = document.getElementById("contentOttModalCancelButton");
    const confirmButton = document.getElementById("contentOttModalConfirmButton");

    if (!modal || !openButton || !cancelButton || !confirmButton) {
        return;
    }

    const closeElements = modal.querySelectorAll("[data-content-ott-modal-close]");
    let selectionSnapshot = createContentOttSelectionSnapshot();

    openButton.addEventListener("click", function () {
        selectionSnapshot = createContentOttSelectionSnapshot();
        openContentOttPlatformModal(modal);
    });

    confirmButton.addEventListener("click", function () {
        updateContentOttSelectedSummary();
        closeContentOttPlatformModal(modal, openButton);
    });

    cancelButton.addEventListener("click", function () {
        restoreContentOttSelectionSnapshot(selectionSnapshot);
        updateContentOttSelectedSummary();
        closeContentOttPlatformModal(modal, openButton);
    });

    closeElements.forEach(function (element) {
        element.addEventListener("click", function () {
            restoreContentOttSelectionSnapshot(selectionSnapshot);
            updateContentOttSelectedSummary();
            closeContentOttPlatformModal(modal, openButton);
        });
    });

    document.addEventListener("keydown", function (event) {
        if (event.key !== "Escape" || modal.hidden) {
            return;
        }

        restoreContentOttSelectionSnapshot(selectionSnapshot);
        updateContentOttSelectedSummary();
        closeContentOttPlatformModal(modal, openButton);
    });

    updateContentOttSelectedSummary();
}

function openContentOttPlatformModal(modal) {
    modal.hidden = false;
    document.body.classList.add("content-ott-modal-open");

    const firstCheckbox = modal.querySelector("input[type='checkbox']");

    if (firstCheckbox) {
        window.requestAnimationFrame(function () {
            firstCheckbox.focus();
        });
    }
}

function closeContentOttPlatformModal(modal, focusTarget) {
    modal.hidden = true;
    document.body.classList.remove("content-ott-modal-open");

    if (focusTarget) {
        focusTarget.focus();
    }
}

function createContentOttSelectionSnapshot() {
    const providerCheckboxes = document.querySelectorAll(
        "[data-content-provider-checkbox]"
    );

    const snapshot = {};

    providerCheckboxes.forEach(function (checkbox) {
        snapshot[checkbox.value] = checkbox.checked;
    });

    return snapshot;
}

function restoreContentOttSelectionSnapshot(snapshot) {
    const providerCheckboxes = document.querySelectorAll(
        "[data-content-provider-checkbox]"
    );

    providerCheckboxes.forEach(function (checkbox) {
        checkbox.checked = Boolean(snapshot[checkbox.value]);
    });

    const form = document.getElementById("contentFilterForm");

    if (form) {
        updateContentFilterAllState(form, "provider");
    }
}

function updateContentOttSelectedSummary() {
    const summary = document.getElementById("contentOttSelectedSummary");

    if (!summary) {
        return;
    }

    const checkedProviders = Array.from(
        document.querySelectorAll("[data-content-provider-checkbox]:checked")
    );

    if (checkedProviders.length === 0) {
        summary.textContent = "전체 플랫폼";
        return;
    }

    const selectedNames = checkedProviders.map(function (checkbox) {
        return checkbox.dataset.providerName;
    });

    if (selectedNames.length === 1) {
        summary.textContent = selectedNames[0];
        return;
    }

    summary.textContent = selectedNames[0]
        + " 외 "
        + (selectedNames.length - 1)
        + "개";
}

