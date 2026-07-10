document.addEventListener("DOMContentLoaded", function () {

    initializeSearchFilter();
    initializeSearchTabs();
    initializeSearchMoreButtons();

});

/**
 * 왼쪽 검색 필터 동작을 초기화한다.
 */
function initializeSearchFilter() {

    const filterForm =
        document.getElementById("searchFilterForm");

    if (!filterForm) {
        return;
    }

    const allCheckboxes =
        filterForm.querySelectorAll(
            "input[type='checkbox'][data-filter-all]"
        );

    const filterCheckboxes =
        filterForm.querySelectorAll(
            "input[type='checkbox'][data-filter-checkbox]"
        );

    /*
     * 개별 항목을 선택하면 같은 그룹의 전체 체크를 해제한다.
     * 개별 항목이 하나도 없으면 전체를 다시 선택한다.
     */
    filterCheckboxes.forEach(function (checkbox) {

        checkbox.addEventListener("change", function () {

            const groupName =
                checkbox.dataset.filterGroup;

            const allCheckbox =
                filterForm.querySelector(
                    "input[data-filter-all]"
                    + "[data-filter-group='"
                    + groupName
                    + "']"
                );

            if (!allCheckbox) {
                return;
            }

            const checkedItems =
                filterForm.querySelectorAll(
                    "input[data-filter-checkbox]"
                    + "[data-filter-group='"
                    + groupName
                    + "']:checked"
                );

            allCheckbox.checked =
                checkedItems.length === 0;
        });
    });

    /*
     * 전체를 선택하면 같은 그룹의 개별 항목을 해제한다.
     */
    allCheckboxes.forEach(function (allCheckbox) {

        allCheckbox.addEventListener("change", function () {

            const groupName =
                allCheckbox.dataset.filterGroup;

            const groupItems =
                filterForm.querySelectorAll(
                    "input[data-filter-checkbox]"
                    + "[data-filter-group='"
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
                filterForm.querySelectorAll(
                    "input[data-filter-checkbox]"
                    + "[data-filter-group='"
                    + groupName
                    + "']:checked"
                );

            if (checkedItems.length === 0) {
                allCheckbox.checked = true;
            }
        });
    });

    /*
     * 필터 조건을 새로 적용할 때는
     * 콘텐츠와 상품 페이지를 모두 1페이지로 초기화한다.
     */
    filterForm.addEventListener("submit", function () {

        setFormPageValue(
            filterForm,
            "contentPage",
            "1"
        );

        setFormPageValue(
            filterForm,
            "goodsPage",
            "1"
        );

        /*
         * 기존 page 파라미터가 남아 있다면
         * 호환을 위해 같이 1로 초기화한다.
         */
        setExistingFormValue(
            filterForm,
            "page",
            "1"
        );
    });
}

/**
 * form에 입력값이 없으면 hidden input을 추가하고,
 * 이미 있으면 값을 변경한다.
 */
function setFormPageValue(
        form,
        name,
        value) {

    let input =
        form.querySelector(
            "input[name='"
            + name
            + "']"
        );

    if (!input) {

        input =
            document.createElement("input");

        input.type = "hidden";
        input.name = name;

        form.appendChild(input);
    }

    input.value = value;
}

/**
 * 기존 input이 존재할 때만 값을 변경한다.
 */
function setExistingFormValue(
        form,
        name,
        value) {

    const input =
        form.querySelector(
            "input[name='"
            + name
            + "']"
        );

    if (input) {
        input.value = value;
    }
}

/**
 * 전체 / 콘텐츠 / 상품 탭을 초기화한다.
 */
function initializeSearchTabs() {

    const searchRoot =
        document.querySelector(
            "[data-search-root]"
        );

    if (!searchRoot) {
        return;
    }

    const tabButtons =
        searchRoot.querySelectorAll(
            "[data-search-tab]"
        );

    const tabPanels =
        searchRoot.querySelectorAll(
            "[data-search-panel]"
        );

    if (tabButtons.length === 0
            || tabPanels.length === 0) {

        return;
    }

    tabButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const targetTab =
                normalizeSearchTab(
                    button.dataset.searchTab
                );

            activateSearchTab(
                searchRoot,
                targetTab,
                true
            );
        });
    });

    const initialTab =
        normalizeSearchTab(
            searchRoot.dataset.initialTab
        );

    activateSearchTab(
        searchRoot,
        initialTab,
        false
    );
}

/**
 * 전체 탭의 더보기 버튼을 처리한다.
 */
function initializeSearchMoreButtons() {

    const searchRoot =
        document.querySelector(
            "[data-search-root]"
        );

    if (!searchRoot) {
        return;
    }

    const moreButtons =
        searchRoot.querySelectorAll(
            "[data-search-move-tab]"
        );

    moreButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const targetTab =
                normalizeSearchTab(
                    button.dataset.searchMoveTab
                );

            activateSearchTab(
                searchRoot,
                targetTab,
                true
            );

            const tabs =
                searchRoot.querySelector(
                    ".search-result-tabs"
                );

            if (tabs) {

                tabs.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            }
        });
    });
}

/**
 * 선택한 탭만 표시한다.
 */
function activateSearchTab(
        searchRoot,
        targetTab,
        updateUrl) {

    const normalizedTarget =
        normalizeSearchTab(targetTab);

    const tabButtons =
        searchRoot.querySelectorAll(
            "[data-search-tab]"
        );

    const tabPanels =
        searchRoot.querySelectorAll(
            "[data-search-panel]"
        );

    tabButtons.forEach(function (button) {

        const buttonTab =
            normalizeSearchTab(
                button.dataset.searchTab
            );

        const active =
            buttonTab === normalizedTarget;

        button.classList.toggle(
            "is-active",
            active
        );

        button.setAttribute(
            "aria-selected",
            active ? "true" : "false"
        );
    });

    tabPanels.forEach(function (panel) {

        const panelTab =
            normalizeSearchTab(
                panel.dataset.searchPanel
            );

        const active =
            panelTab === normalizedTarget;

        panel.hidden = !active;

        panel.classList.toggle(
            "is-active",
            active
        );
    });

    searchRoot.dataset.initialTab =
        normalizedTarget;

    if (updateUrl) {

        updateSearchTabQuery(
            normalizedTarget
        );
    }
}

/**
 * 허용되지 않은 탭 값은 ALL로 처리한다.
 */
function normalizeSearchTab(
        tabValue) {

    const normalized =
        String(tabValue || "ALL")
            .trim()
            .toUpperCase();

    if (normalized === "CONTENT"
            || normalized === "GOODS") {

        return normalized;
    }

    return "ALL";
}

/**
 * 새로고침 후에도 현재 탭을 유지하도록
 * URL의 searchTab만 갱신한다.
 *
 * 콘텐츠 페이지와 상품 페이지 파라미터는 그대로 유지된다.
 */
function updateSearchTabQuery(
        searchTab) {

    if (!window.history
            || !window.history.replaceState) {

        return;
    }

    const currentUrl =
        new URL(
            window.location.href
        );

    currentUrl.searchParams.set(
        "searchTab",
        searchTab
    );

    window.history.replaceState(
        null,
        "",
        currentUrl.toString()
    );
}