(function () {
    "use strict";

    function initFilmographyTabs() {

        const tabContainer =
                document.querySelector("[data-filmography-tabs]");

        if (!tabContainer
                || tabContainer.dataset.tabsInitialized === "true") {
            return;
        }

        const tabButtons = Array.from(
                tabContainer.querySelectorAll("[data-tab-target]")
        );

        const tabPanels = Array.from(
                tabContainer.querySelectorAll("[data-tab-panel]")
        );

        if (tabButtons.length === 0 || tabPanels.length === 0) {
            return;
        }

        tabContainer.dataset.tabsInitialized = "true";

        function activateTab(tabName, moveFocus) {

            const targetButton = tabButtons.find(function (button) {
                return button.dataset.tabTarget === tabName;
            });

            const targetPanel = tabPanels.find(function (panel) {
                return panel.dataset.tabPanel === tabName;
            });

            if (!targetButton || !targetPanel) {
                return;
            }

            tabButtons.forEach(function (button) {

                const isActive = button === targetButton;

                button.classList.toggle("is-active", isActive);

                button.setAttribute(
                        "aria-selected",
                        String(isActive)
                );

                button.tabIndex = isActive ? 0 : -1;
            });

            tabPanels.forEach(function (panel) {

                const isActive = panel === targetPanel;

                panel.hidden = !isActive;
                panel.classList.toggle("is-active", isActive);
            });

            if (moveFocus) {
                targetButton.focus();
            }
        }

        tabButtons.forEach(function (button, index) {

            button.addEventListener("click", function () {

                activateTab(
                        button.dataset.tabTarget,
                        false
                );
            });

            button.addEventListener("keydown", function (event) {

                let nextIndex = index;

                if (event.key === "ArrowRight") {

                    nextIndex =
                            (index + 1) % tabButtons.length;

                } else if (event.key === "ArrowLeft") {

                    nextIndex =
                            (index - 1 + tabButtons.length)
                            % tabButtons.length;

                } else if (event.key === "Home") {

                    nextIndex = 0;

                } else if (event.key === "End") {

                    nextIndex = tabButtons.length - 1;

                } else {

                    return;
                }

                event.preventDefault();

                activateTab(
                        tabButtons[nextIndex].dataset.tabTarget,
                        true
                );
            });
        });

        const initiallyActive = tabButtons.find(
                function (button) {

                    return button.classList.contains(
                            "is-active"
                    );
                }
        );

        const initialTabName = initiallyActive
                ? initiallyActive.dataset.tabTarget
                : tabButtons[0].dataset.tabTarget;

        activateTab(initialTabName, false);
    }

    if (document.readyState === "loading") {

        document.addEventListener(
                "DOMContentLoaded",
                initFilmographyTabs
        );

    } else {

        initFilmographyTabs();
    }

})();