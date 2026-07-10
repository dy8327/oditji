document.addEventListener("DOMContentLoaded", function () {

    initializeSearchFilter();
    initializeSearchTabs();
    initializeSearchMoreButtons();

});

/**
 * 왼쪽 사이드바 필터의 전체/개별 체크박스 동작과
 * 검색 제출 시 페이지 초기화를 처리한다.
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
     * 개별 항목을 선택하면 같은 그룹의 '전체'를 해제한다.
     * 개별 항목을 모두 해제하면 '전체'를 다시 선택한다.
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
     * '전체'를 선택하면 같은 그룹의 개별 항목을 모두 해제한다.
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
     * 필터를 적용하면 첫 페이지부터 다시 검색한다.
     */
    filterForm.addEventListener("submit", function () {

        const pageInput =
            filterForm.querySelector(
                "input[name='page']"
            );

        if (pageInput) {
            pageInput.value = "1";
        }
    });
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

    /*
     * 서버에서 전달한 초기 탭 값을 적용한다.
     * 값이 없거나 올바르지 않으면 ALL 탭으로 이동한다.
     */
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
 * 전체 탭의 더보기 버튼을 누르면
 * 콘텐츠 또는 상품 탭으로 이동한다.
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

            /*
             * 더보기 버튼으로 탭 이동 시
             * 탭 영역이 보이도록 부드럽게 스크롤한다.
             */
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
 * 전달된 탭을 활성화하고
 * 나머지 탭 패널은 숨긴다.
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

    /*
     * 탭 버튼 활성 상태 변경
     */
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

    /*
     * 선택한 탭의 패널만 표시
     */
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

    /*
     * 현재 활성 탭을 루트 요소에도 저장한다.
     */
    searchRoot.dataset.initialTab =
        normalizedTarget;

    /*
     * 사용자가 직접 탭을 이동한 경우에만
     * URL의 searchTab 파라미터를 변경한다.
     */
    if (updateUrl) {
        updateSearchTabQuery(
            normalizedTarget
        );
    }
}

/**
 * 허용되지 않은 탭 값은
 * 전체 탭으로 처리한다.
 */
function normalizeSearchTab(tabValue) {

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
 * 새로고침 후에도 현재 탭이 유지되도록
 * 현재 주소의 searchTab 파라미터를 갱신한다.
 */
function updateSearchTabQuery(searchTab) {

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