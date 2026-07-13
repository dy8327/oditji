document.addEventListener("DOMContentLoaded", function () {

    initializeContentFilters();
    initializeContentGenreToggle();
    initializeRecommendCarousel();

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
