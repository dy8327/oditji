document.addEventListener("DOMContentLoaded", function () {

    initializeSearchFilter();
    initializeSidebarFilterTabs();
    initializeHeaderSearchFilterPreservation();
    initializeGenreToggle();
    initializeOttPlatformModal();
    initializeSearchTabs();
    initializeSearchMoreButtons();
    initializePaginationFilterPreservation();

});

function initializeSearchFilter() {

    const filterForm = document.getElementById("searchFilterForm");

    if (!filterForm) {
        return;
    }

    const allCheckboxes = filterForm.querySelectorAll(
        "input[type='checkbox'][data-filter-all]"
    );

    const filterCheckboxes = filterForm.querySelectorAll(
        "input[type='checkbox'][data-filter-checkbox]"
    );

    filterCheckboxes.forEach(function (checkbox) {

        checkbox.addEventListener("change", function () {

            const groupName = checkbox.dataset.filterGroup;

            updateFilterAllCheckbox(
                filterForm,
                groupName
            );
        });
    });

    allCheckboxes.forEach(function (allCheckbox) {

        allCheckbox.addEventListener("change", function () {

            const groupName = allCheckbox.dataset.filterGroup;

            const groupItems = filterForm.querySelectorAll(
                "input[data-filter-checkbox]"
                + "[data-filter-group='"
                + groupName
                + "']"
            );

            if (allCheckbox.checked) {

                groupItems.forEach(function (item) {
                    item.checked = false;
                });

                if (groupName === "provider") {
                    updateOttSelectedSummary();
                }

                return;
            }

            const checkedItems = filterForm.querySelectorAll(
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

    filterForm.addEventListener("submit", function () {

        setFormValue(filterForm, "contentPage", "1");
        setFormValue(filterForm, "goodsPage", "1");
        setExistingFormValue(filterForm, "page", "1");
    });
}

/**
 * 헤더 검색창에서 검색할 때도 현재 사이드바 필터를 함께 전송한다.
 *
 * headerSearchForm과 searchFilterForm은 서로 다른 form이므로,
 * 헤더 검색 form을 제출하기 직전에 선택된 필터 값을 hidden input으로 복사한다.
 */
function copyNamedValueToHeaderForm(
        filterForm,
        headerSearchForm,
        inputName) {

    const input =
        filterForm.querySelector(
            "[name='" + inputName + "']"
        );

    if (!input) {
        return;
    }

    if ((input.type === "checkbox"
            || input.type === "radio")
            && !input.checked) {

        return;
    }

    if (String(input.value || "").trim() === "") {
        return;
    }

    appendGeneratedHiddenInput(
        headerSearchForm,
        inputName,
        input.value
    );
}

/**
 * 왼쪽 사이드바의 전체 / 콘텐츠 / 상품 탭을 초기화한다.
 */
function initializeSidebarFilterTabs() {

    const filterForm =
        document.getElementById("searchFilterForm");

    const searchRoot =
        document.querySelector("[data-search-root]");

    if (!filterForm) {
        return;
    }

    const tabButtons =
        filterForm.querySelectorAll(
            "[data-sidebar-filter-tab]"
        );

    tabButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const targetTab =
                normalizeSidebarFilterTab(
                    button.dataset.sidebarFilterTab
                );

            activateSidebarFilterTab(
                filterForm,
                targetTab
            );

            /*
             * 검색 결과 영역이 존재하면
             * 왼쪽 필터 탭과 결과 탭을 함께 변경한다.
             */
            if (searchRoot) {

                activateSearchTab(
                    searchRoot,
                    targetTab,
                    true
                );

            } else {

                setFormValue(
                    filterForm,
                    "searchTab",
                    targetTab
                );
            }
        });
    });

    const initialTab =
        normalizeSidebarFilterTab(
            filterForm.dataset.initialSidebarTab
        );

    activateSidebarFilterTab(
        filterForm,
        initialTab
    );
}

/**
 * 왼쪽 사이드바 탭 값을 정규화한다.
 *
 * 허용값:
 * ALL
 * CONTENT
 * GOODS
 */
function normalizeSidebarFilterTab(tabValue) {

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
 * 왼쪽 사이드바 탭을 활성화한다.
 *
 * ALL:
 * 콘텐츠 필터와 상품 필터 모두 표시
 *
 * CONTENT:
 * 콘텐츠 필터만 표시
 *
 * GOODS:
 * 상품 필터만 표시
 */
function activateSidebarFilterTab(
        filterForm,
        targetTab) {

    const normalizedTarget =
        normalizeSidebarFilterTab(targetTab);

    const tabButtons =
        filterForm.querySelectorAll(
            "[data-sidebar-filter-tab]"
        );

    const tabPanels =
        filterForm.querySelectorAll(
            "[data-sidebar-filter-panel]"
        );

    tabButtons.forEach(function (button) {

        const buttonTab =
            normalizeSidebarFilterTab(
                button.dataset.sidebarFilterTab
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
            normalizeSidebarFilterTab(
                panel.dataset.sidebarFilterPanel
            );

        /*
         * 전체 탭일 때는 콘텐츠와 상품 필터를
         * 모두 보이도록 처리한다.
         */
        const active =
            normalizedTarget === "ALL"
            || panelTab === normalizedTarget;

        panel.hidden = !active;
    });

    filterForm.dataset.initialSidebarTab =
        normalizedTarget;

    setFormValue(
        filterForm,
        "searchTab",
        normalizedTarget
    );
}

function initializeHeaderSearchFilterPreservation() {

    const headerSearchForm =
        document.getElementById("headerSearchForm");

    const filterForm =
        document.getElementById("searchFilterForm");

    if (!headerSearchForm || !filterForm) {
        return;
    }

    headerSearchForm.addEventListener(
        "submit",
        function () {

            removeGeneratedHeaderFilterInputs(
                headerSearchForm
            );

            copyCheckedFilterValuesToHeaderForm(
                filterForm,
                headerSearchForm,
                "contentCategories"
            );

            copyCheckedFilterValuesToHeaderForm(
                filterForm,
                headerSearchForm,
                "genreCodes"
            );

            copyCheckedFilterValuesToHeaderForm(
                filterForm,
                headerSearchForm,
                "providerIds"
            );

            copyCheckedFilterValuesToHeaderForm(
                filterForm,
                headerSearchForm,
                "productTypes"
            );

            copyNamedValueToHeaderForm(
                filterForm,
                headerSearchForm,
                "minPrice"
            );

            copyNamedValueToHeaderForm(
                filterForm,
                headerSearchForm,
                "maxPrice"
            );

            copyNamedValueToHeaderForm(
                filterForm,
                headerSearchForm,
                "discountOnly"
            );

            copyNamedValueToHeaderForm(
                filterForm,
                headerSearchForm,
                "inStockOnly"
            );

            /*
             * 새 검색을 실행하면
             * 콘텐츠와 상품 페이지 모두 1페이지로 초기화한다.
             */
            appendGeneratedHiddenInput(
                headerSearchForm,
                "contentPage",
                "1"
            );

            appendGeneratedHiddenInput(
                headerSearchForm,
                "goodsPage",
                "1"
            );

            const searchTabInput =
                filterForm.querySelector(
                    "input[name='searchTab']"
                );

            /*
             * 현재 선택된 탭을 헤더 검색창에도 전달한다.
             *
             * 전체 탭이면 ALL이 전달되어
             * 검색 결과도 전체 탭에 남는다.
             */
            appendGeneratedHiddenInput(
                headerSearchForm,
                "searchTab",
                searchTabInput
                    ? searchTabInput.value
                    : "ALL"
            );
        }
    );
}

/**
 * 사이드바에서 체크된 특정 필터 값을
 * 헤더 검색 form의 hidden input으로 복사한다.
 */
function copyCheckedFilterValuesToHeaderForm(
        filterForm,
        headerSearchForm,
        inputName) {

    const checkedInputs =
        filterForm.querySelectorAll(
            "input[name='"
            + inputName
            + "']:checked"
        );

    checkedInputs.forEach(function (input) {

        appendGeneratedHiddenInput(
            headerSearchForm,
            inputName,
            input.value
        );
    });
}

/**
 * 헤더 검색 form에 필터 전달용 hidden input을 추가한다.
 */
function appendGeneratedHiddenInput(
        form,
        name,
        value) {

    const hiddenInput =
        document.createElement("input");

    hiddenInput.type = "hidden";
    hiddenInput.name = name;
    hiddenInput.value = value;
    hiddenInput.dataset.generatedSearchFilter =
        "true";

    form.appendChild(hiddenInput);
}

/**
 * 헤더 검색을 여러 번 실행하더라도
 * 이전에 생성한 hidden input이 중복되지 않게 제거한다.
 */
function removeGeneratedHeaderFilterInputs(
        headerSearchForm) {

    const generatedInputs =
        headerSearchForm.querySelectorAll(
            "input[data-generated-search-filter='true']"
        );

    generatedInputs.forEach(function (input) {
        input.remove();
    });
}

function updateFilterAllCheckbox(
        filterForm,
        groupName) {

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

    if (groupName === "provider") {
        updateOttSelectedSummary();
    }
}

function initializeGenreToggle() {

    const toggleButton =
        document.getElementById(
            "genreToggleButton"
        );

    const extraOptions =
        document.getElementById(
            "genreExtraOptions"
        );

    if (!toggleButton || !extraOptions) {
        return;
    }

    const initiallyExpanded =
        toggleButton.dataset.expanded === "true";

    setGenreExpandedState(
        toggleButton,
        extraOptions,
        initiallyExpanded
    );

    toggleButton.addEventListener(
        "click",
        function () {

            const currentlyExpanded =
                toggleButton.getAttribute(
                    "aria-expanded"
                ) === "true";

            setGenreExpandedState(
                toggleButton,
                extraOptions,
                !currentlyExpanded
            );
        }
    );
}

function setGenreExpandedState(
        toggleButton,
        extraOptions,
        expanded) {

    extraOptions.hidden = !expanded;

    toggleButton.setAttribute(
        "aria-expanded",
        expanded ? "true" : "false"
    );

    toggleButton.dataset.expanded =
        expanded ? "true" : "false";

    toggleButton.textContent =
        expanded
            ? "장르 접기"
            : "장르 전체보기";
}

function initializeOttPlatformModal() {

    const modal =
        document.getElementById(
            "ottPlatformModal"
        );

    const openButton =
        document.getElementById(
            "ottModalOpenButton"
        );

    const cancelButton =
        document.getElementById(
            "ottModalCancelButton"
        );

    const confirmButton =
        document.getElementById(
            "ottModalConfirmButton"
        );

    if (!modal
            || !openButton
            || !cancelButton
            || !confirmButton) {

        return;
    }

    const closeElements =
        modal.querySelectorAll(
            "[data-ott-modal-close]"
        );

    let selectionSnapshot =
        createOttSelectionSnapshot();

    openButton.addEventListener(
        "click",
        function () {

            selectionSnapshot =
                createOttSelectionSnapshot();

            openOttPlatformModal(modal);
        }
    );

    confirmButton.addEventListener(
        "click",
        function () {

            updateOttSelectedSummary();

            closeOttPlatformModal(
                modal,
                openButton
            );
        }
    );

    cancelButton.addEventListener(
        "click",
        function () {

            restoreOttSelectionSnapshot(
                selectionSnapshot
            );

            updateOttSelectedSummary();

            closeOttPlatformModal(
                modal,
                openButton
            );
        }
    );

    closeElements.forEach(function (element) {

        element.addEventListener(
            "click",
            function () {

                restoreOttSelectionSnapshot(
                    selectionSnapshot
                );

                updateOttSelectedSummary();

                closeOttPlatformModal(
                    modal,
                    openButton
                );
            }
        );
    });

    document.addEventListener(
        "keydown",
        function (event) {

            if (event.key !== "Escape"
                    || modal.hidden) {

                return;
            }

            restoreOttSelectionSnapshot(
                selectionSnapshot
            );

            updateOttSelectedSummary();

            closeOttPlatformModal(
                modal,
                openButton
            );
        }
    );

    updateOttSelectedSummary();
}

function openOttPlatformModal(modal) {

    modal.hidden = false;

    document.body.classList.add(
        "search-modal-open"
    );

    const firstCheckbox =
        modal.querySelector(
            "input[type='checkbox']"
        );

    if (firstCheckbox) {

        window.requestAnimationFrame(
            function () {

                firstCheckbox.focus();
            }
        );
    }
}

function closeOttPlatformModal(
        modal,
        focusTarget) {

    modal.hidden = true;

    document.body.classList.remove(
        "search-modal-open"
    );

    if (focusTarget) {
        focusTarget.focus();
    }
}

function createOttSelectionSnapshot() {

    const providerCheckboxes =
        document.querySelectorAll(
            "[data-provider-checkbox]"
        );

    const snapshot = {};

    providerCheckboxes.forEach(
        function (checkbox) {

            snapshot[checkbox.value] =
                checkbox.checked;
        }
    );

    return snapshot;
}

function restoreOttSelectionSnapshot(snapshot) {

    const providerCheckboxes =
        document.querySelectorAll(
            "[data-provider-checkbox]"
        );

    providerCheckboxes.forEach(
        function (checkbox) {

            checkbox.checked =
                Boolean(
                    snapshot[checkbox.value]
                );
        }
    );

    const filterForm =
        document.getElementById(
            "searchFilterForm"
        );

    if (filterForm) {

        updateFilterAllCheckbox(
            filterForm,
            "provider"
        );
    }
}

function updateOttSelectedSummary() {

    const summary =
        document.getElementById(
            "ottSelectedSummary"
        );

    if (!summary) {
        return;
    }

    const checkedProviders =
        Array.from(
            document.querySelectorAll(
                "[data-provider-checkbox]:checked"
            )
        );

    if (checkedProviders.length === 0) {

        summary.textContent =
            "전체 플랫폼";

        return;
    }

    const selectedNames =
        checkedProviders.map(
            function (checkbox) {

                return checkbox.dataset.providerName;
            }
        );

    if (selectedNames.length === 1) {

        summary.textContent =
            selectedNames[0];

        return;
    }

    summary.textContent =
        selectedNames[0]
        + " 외 "
        + (selectedNames.length - 1)
        + "개";
}

/**
 * 검색 결과의 전체 / 콘텐츠 / 상품 탭을 초기화한다.
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

        button.addEventListener(
            "click",
            function () {

                const targetTab =
                    normalizeSearchTab(
                        button.dataset.searchTab
                    );

                activateSearchTab(
                    searchRoot,
                    targetTab,
                    true
                );
            }
        );
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

        button.addEventListener(
            "click",
            function () {

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
            }
        );
    });
}

/**
 * 검색 결과 탭을 활성화한다.
 *
 * 왼쪽 사이드바 탭도 동일한 값으로 동기화한다.
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

    const filterForm =
        document.getElementById(
            "searchFilterForm"
        );

    /*
     * 전체 탭도 사이드바와 동기화한다.
     *
     * 기존에는 CONTENT / GOODS만 동기화했기 때문에
     * 전체 탭 상태가 사이드바에서 콘텐츠로 바뀌는 문제가 있었다.
     */
    if (filterForm) {

        activateSidebarFilterTab(
            filterForm,
            normalizedTarget
        );
    }

    if (updateUrl) {

        updateSearchTabQuery(
            normalizedTarget
        );
    }
}

/**
 * 검색 결과 탭 값을 정규화한다.
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
 * 현재 주소의 searchTab 값만 변경한다.
 *
 * 페이지를 새로고침하지 않고
 * 현재 탭 상태를 주소에 유지한다.
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

/**
 * 페이지 이동 시 현재 선택된 필터를 유지한다.
 */
function initializePaginationFilterPreservation() {

    const filterForm =
        document.getElementById(
            "searchFilterForm"
        );

    if (!filterForm) {
        return;
    }

    const pageLinks =
        document.querySelectorAll(
            ".search-pagination a.page-btn, "
            + ".pagination a.page-btn"
        );

    pageLinks.forEach(function (link) {

        link.addEventListener(
            "click",
            function (event) {

                event.preventDefault();

                const targetUrl =
                    new URL(
                        link.href,
                        window.location.origin
                    );

                /*
                 * 기존 URL의 필터 파라미터를 지운 뒤
                 * 현재 폼의 선택값으로 다시 채운다.
                 */
                [
                    "contentCategories",
                    "genreCodes",
                    "providerIds",
                    "productTypes",
                    "minPrice",
                    "maxPrice",
                    "discountOnly",
                    "inStockOnly"
                ].forEach(function (name) {

                    targetUrl.searchParams.delete(
                        name
                    );
                });

                const filterData =
                    new FormData(filterForm);

                filterData.forEach(
                    function (value, name) {

                        /*
                         * 페이지 링크에서 이미 관리하는 값은
                         * 중복해서 추가하지 않는다.
                         */
                        if (name === "keyword"
                                || name === "contentPage"
                                || name === "goodsPage"
                                || name === "searchTab") {

                            return;
                        }

                        if (String(value).trim() !== "") {

                            targetUrl.searchParams.append(
                                name,
                                value
                            );
                        }
                    }
                );

                window.location.href =
                    targetUrl.toString();
            }
        );
    });
}

/**
 * form 안에 hidden input이 없으면 생성하고 값을 설정한다.
 */
function setFormValue(
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
 * 이미 존재하는 form input의 값만 변경한다.
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