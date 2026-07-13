document.addEventListener("DOMContentLoaded", function () {

    initializeSearchFilter();
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
function initializeHeaderSearchFilterPreservation() {

    const headerSearchForm =
        document.getElementById("headerSearchForm");

    const filterForm =
        document.getElementById("searchFilterForm");

    if (!headerSearchForm || !filterForm) {
        return;
    }

    headerSearchForm.addEventListener("submit", function () {

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

        appendGeneratedHiddenInput(
            headerSearchForm,
            "searchTab",
            "ALL"
        );
    });
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
    hiddenInput.dataset.generatedSearchFilter = "true";

    form.appendChild(hiddenInput);
}


/**
 * 헤더 검색을 여러 번 실행하더라도
 * 이전에 생성한 필터 hidden input이 중복되지 않게 제거한다.
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

function updateFilterAllCheckbox(filterForm, groupName) {

    const allCheckbox = filterForm.querySelector(
        "input[data-filter-all]"
        + "[data-filter-group='"
        + groupName
        + "']"
    );

    if (!allCheckbox) {
        return;
    }

    const checkedItems = filterForm.querySelectorAll(
        "input[data-filter-checkbox]"
        + "[data-filter-group='"
        + groupName
        + "']:checked"
    );

    allCheckbox.checked = checkedItems.length === 0;

    if (groupName === "provider") {
        updateOttSelectedSummary();
    }
}

function initializeGenreToggle() {

    const toggleButton = document.getElementById("genreToggleButton");
    const extraOptions = document.getElementById("genreExtraOptions");

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

    toggleButton.addEventListener("click", function () {

        const currentlyExpanded =
            toggleButton.getAttribute("aria-expanded") === "true";

        setGenreExpandedState(
            toggleButton,
            extraOptions,
            !currentlyExpanded
        );
    });
}

function setGenreExpandedState(toggleButton, extraOptions, expanded) {

    extraOptions.hidden = !expanded;

    toggleButton.setAttribute(
        "aria-expanded",
        expanded ? "true" : "false"
    );

    toggleButton.dataset.expanded =
        expanded ? "true" : "false";

    toggleButton.textContent =
        expanded ? "장르 접기" : "장르 전체보기";
}

function initializeOttPlatformModal() {

    const modal = document.getElementById("ottPlatformModal");
    const openButton = document.getElementById("ottModalOpenButton");
    const cancelButton = document.getElementById("ottModalCancelButton");
    const confirmButton = document.getElementById("ottModalConfirmButton");

    if (!modal || !openButton || !cancelButton || !confirmButton) {
        return;
    }

    const closeElements = modal.querySelectorAll(
        "[data-ott-modal-close]"
    );

    let selectionSnapshot = createOttSelectionSnapshot();

    openButton.addEventListener("click", function () {

        selectionSnapshot = createOttSelectionSnapshot();
        openOttPlatformModal(modal);
    });

    confirmButton.addEventListener("click", function () {

        updateOttSelectedSummary();
        closeOttPlatformModal(modal, openButton);
    });

    cancelButton.addEventListener("click", function () {

        restoreOttSelectionSnapshot(selectionSnapshot);
        updateOttSelectedSummary();
        closeOttPlatformModal(modal, openButton);
    });

    closeElements.forEach(function (element) {

        element.addEventListener("click", function () {

            restoreOttSelectionSnapshot(selectionSnapshot);
            updateOttSelectedSummary();
            closeOttPlatformModal(modal, openButton);
        });
    });

    document.addEventListener("keydown", function (event) {

        if (event.key !== "Escape" || modal.hidden) {
            return;
        }

        restoreOttSelectionSnapshot(selectionSnapshot);
        updateOttSelectedSummary();
        closeOttPlatformModal(modal, openButton);
    });

    updateOttSelectedSummary();
}

function openOttPlatformModal(modal) {

    modal.hidden = false;
    document.body.classList.add("search-modal-open");

    const firstCheckbox = modal.querySelector("input[type='checkbox']");

    if (firstCheckbox) {
        window.requestAnimationFrame(function () {
            firstCheckbox.focus();
        });
    }
}

function closeOttPlatformModal(modal, focusTarget) {

    modal.hidden = true;
    document.body.classList.remove("search-modal-open");

    if (focusTarget) {
        focusTarget.focus();
    }
}

function createOttSelectionSnapshot() {

    const providerCheckboxes = document.querySelectorAll(
        "[data-provider-checkbox]"
    );

    const snapshot = {};

    providerCheckboxes.forEach(function (checkbox) {
        snapshot[checkbox.value] = checkbox.checked;
    });

    return snapshot;
}

function restoreOttSelectionSnapshot(snapshot) {

    const providerCheckboxes = document.querySelectorAll(
        "[data-provider-checkbox]"
    );

    providerCheckboxes.forEach(function (checkbox) {
        checkbox.checked = Boolean(snapshot[checkbox.value]);
    });

    const filterForm = document.getElementById("searchFilterForm");

    if (filterForm) {
        updateFilterAllCheckbox(filterForm, "provider");
    }
}

function updateOttSelectedSummary() {

    const summary = document.getElementById("ottSelectedSummary");

    if (!summary) {
        return;
    }

    const checkedProviders = Array.from(
        document.querySelectorAll("[data-provider-checkbox]:checked")
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

    summary.textContent =
        selectedNames[0]
        + " 외 "
        + (selectedNames.length - 1)
        + "개";
}

function initializeSearchTabs() {

    const searchRoot = document.querySelector("[data-search-root]");

    if (!searchRoot) {
        return;
    }

    const tabButtons = searchRoot.querySelectorAll("[data-search-tab]");
    const tabPanels = searchRoot.querySelectorAll("[data-search-panel]");

    if (tabButtons.length === 0 || tabPanels.length === 0) {
        return;
    }

    tabButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const targetTab = normalizeSearchTab(
                button.dataset.searchTab
            );

            activateSearchTab(
                searchRoot,
                targetTab,
                true
            );
        });
    });

    const initialTab = normalizeSearchTab(
        searchRoot.dataset.initialTab
    );

    activateSearchTab(
        searchRoot,
        initialTab,
        false
    );
}

function initializeSearchMoreButtons() {

    const searchRoot = document.querySelector("[data-search-root]");

    if (!searchRoot) {
        return;
    }

    const moreButtons = searchRoot.querySelectorAll(
        "[data-search-move-tab]"
    );

    moreButtons.forEach(function (button) {

        button.addEventListener("click", function () {

            const targetTab = normalizeSearchTab(
                button.dataset.searchMoveTab
            );

            activateSearchTab(
                searchRoot,
                targetTab,
                true
            );

            const tabs = searchRoot.querySelector(".search-result-tabs");

            if (tabs) {
                tabs.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });
            }
        });
    });
}

function activateSearchTab(searchRoot, targetTab, updateUrl) {

    const normalizedTarget = normalizeSearchTab(targetTab);

    const tabButtons = searchRoot.querySelectorAll("[data-search-tab]");
    const tabPanels = searchRoot.querySelectorAll("[data-search-panel]");

    tabButtons.forEach(function (button) {

        const buttonTab = normalizeSearchTab(
            button.dataset.searchTab
        );

        const active = buttonTab === normalizedTarget;

        button.classList.toggle("is-active", active);
        button.setAttribute(
            "aria-selected",
            active ? "true" : "false"
        );
    });

    tabPanels.forEach(function (panel) {

        const panelTab = normalizeSearchTab(
            panel.dataset.searchPanel
        );

        const active = panelTab === normalizedTarget;

        panel.hidden = !active;
        panel.classList.toggle("is-active", active);
    });

    searchRoot.dataset.initialTab = normalizedTarget;

    if (updateUrl) {
        updateSearchTabQuery(normalizedTarget);
    }
}

function normalizeSearchTab(tabValue) {

    const normalized = String(tabValue || "ALL")
        .trim()
        .toUpperCase();

    if (normalized === "CONTENT" || normalized === "GOODS") {
        return normalized;
    }

    return "ALL";
}

function updateSearchTabQuery(searchTab) {

    if (!window.history || !window.history.replaceState) {
        return;
    }

    const currentUrl = new URL(window.location.href);

    currentUrl.searchParams.set("searchTab", searchTab);

    window.history.replaceState(
        null,
        "",
        currentUrl.toString()
    );
}

/**
 * 기존 searchResult.jsp의 페이지 링크에 contentCategories가 아직 없더라도
 * 현재 사이드바 선택값을 페이지 이동 URL에 자동으로 유지한다.
 */
function initializePaginationFilterPreservation() {

    const filterForm = document.getElementById("searchFilterForm");

    if (!filterForm) {
        return;
    }

    const pageLinks = document.querySelectorAll(
        ".search-pagination a.page-btn, .pagination a.page-btn"
    );

    pageLinks.forEach(function (link) {

        link.addEventListener("click", function (event) {

            event.preventDefault();

            const targetUrl = new URL(
                link.href,
                window.location.origin
            );

            targetUrl.searchParams.delete("contentCategories");

            const checkedCategories = filterForm.querySelectorAll(
                "input[name='contentCategories']:checked"
            );

            checkedCategories.forEach(function (checkbox) {
                targetUrl.searchParams.append(
                    "contentCategories",
                    checkbox.value
                );
            });

            window.location.href = targetUrl.toString();
        });
    });
}

function setFormValue(form, name, value) {

    let input = form.querySelector(
        "input[name='" + name + "']"
    );

    if (!input) {
        input = document.createElement("input");
        input.type = "hidden";
        input.name = name;
        form.appendChild(input);
    }

    input.value = value;
}

function setExistingFormValue(form, name, value) {

    const input = form.querySelector(
        "input[name='" + name + "']"
    );

    if (input) {
        input.value = value;
    }
}