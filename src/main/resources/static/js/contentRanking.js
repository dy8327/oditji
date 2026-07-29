document.addEventListener("DOMContentLoaded", function () {

    const tabButtons =
            document.querySelectorAll("[data-ranking-tab]");

    const panels =
            document.querySelectorAll(".ranking-panel");

    tabButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const selectedTab =
                    button.getAttribute("data-ranking-tab");

            tabButtons.forEach(function (targetButton) {
                targetButton.classList.remove("active");
            });

            panels.forEach(function (panel) {
                panel.classList.remove("active");
            });

            button.classList.add("active");

            const selectedPanel =
                    document.getElementById(
                            "ranking-panel-" + selectedTab);

            if (selectedPanel) {
                selectedPanel.classList.add("active");
            }
        });
    });
});
