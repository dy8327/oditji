/**
 * ODITJI 검색 결과 화면 스크립트
 *
 * 담당 기능
 * 1. 콘텐츠/상품 결과 탭과 왼쪽 필터 영역을 동기화합니다.
 * 2. 전체 체크박스, 장르 펼치기, OTT 선택 모달을 처리합니다.
 * 3. 헤더 재검색과 페이지 이동 시 현재 필터를 유지합니다.
 * 4. 결과 상단의 선택 필터 칩을 클릭하면 해당 조건만 제거합니다.
 */
document.addEventListener("DOMContentLoaded", function () {
    initializeSearchFilter();
    initializeHeaderSearchFilterPreservation();
    initializeGenreToggle();
    initializeOttPlatformModal();
    initializeSearchTabs();
    initializeSearchMoreButtons();
    initializeActiveFilterChips();
    initializePaginationFilterPreservation();
});

/**
 * 사이드바 필터 폼의 기본 동작을 초기화합니다.
 */
function initializeSearchFilter() {
    const filterForm = document.getElementById("searchFilterForm");

    if (!filterForm) {
        return;
    }

    const allCheckboxes = filterForm.querySelectorAll(
        "input[type='checkbox'][data-filter-all]"
    );

    const itemCheckboxes = filterForm.querySelectorAll(
        "input[type='checkbox'][data-filter-checkbox]"
    );

    itemCheckboxes.forEach(function (checkbox) {
        checkbox.addEventListener("change", function () {
            synchronizeAllCheckbox(filterForm, checkbox.dataset.filterGroup);

            if (checkbox.dataset.filterGroup === "provider") {
                updateOttSelectedSummary();
            }
        });
    });

    allCheckboxes.forEach(function (allCheckbox) {
        allCheckbox.addEventListener("change", function () {
            const groupName = allCheckbox.dataset.filterGroup;
            const groupItems = getFilterGroupItems(filterForm, groupName);

            if (allCheckbox.checked) {
                groupItems.forEach(function (item) {
                    item.checked = false;
                });
            } else {
                const checkedItems = Array.from(groupItems).filter(function (item) {
                    return item.checked;
                });

                if (checkedItems.length === 0) {
                    allCheckbox.checked = true;
                }
            }

            if (groupName === "provider") {
                updateOttSelectedSummary();
            }
        });
    });

    filterForm.addEventListener("submit", function () {
        setFormValue(filterForm, "contentPage", "1");
        setFormValue(filterForm, "goodsPage", "1");
    });

    const initialTab = normalizeSearchTab(filterForm.dataset.initialSidebarTab);
    synchronizeSidebarPanels(filterForm, initialTab);
}

/**
 * 지정한 필터 그룹의 실제 선택 항목을 반환합니다.
 */
function getFilterGroupItems(filterForm, groupName) {
    if (!groupName) {
        return [];
    }

    return filterForm.querySelectorAll(
        "input[data-filter-checkbox][data-filter-group='" + groupName + "']"
    );
}

/**
 * 그룹의 개별 항목 선택 여부에 따라 '전체' 체크박스를 갱신합니다.
 */
function synchronizeAllCheckbox(filterForm, groupName) {
    if (!groupName) {
        return;
    }

    const allCheckbox = filterForm.querySelector(
        "input[data-filter-all][data-filter-group='" + groupName + "']"
    );

    if (!allCheckbox) {
        return;
    }

    const groupItems = getFilterGroupItems(filterForm, groupName);
    const checkedItems = Array.from(groupItems).filter(function (item) {
        return item.checked;
    });

    allCheckbox.checked = checkedItems.length === 0;
}

/**
 * 헤더 검색창에서 다시 검색하더라도 사이드바 필터가 사라지지 않도록
 * 제출 직전에 선택값을 hidden input으로 복사합니다.
 */
function initializeHeaderSearchFilterPreservation() {
    const headerSearchForm = document.getElementById("headerSearchForm");
    const filterForm = document.getElementById("searchFilterForm");

    if (!headerSearchForm || !filterForm) {
        return;
    }

    headerSearchForm.addEventListener("submit", function () {
        removeGeneratedHeaderInputs(headerSearchForm);

        const filterData = new FormData(filterForm);

        filterData.forEach(function (value, name) {
            if (name === "keyword"
                    || name === "contentPage"
                    || name === "goodsPage") {
                return;
            }

            if (String(value).trim() === "") {
                return;
            }

            appendGeneratedHeaderInput(headerSearchForm, name, value);
        });

        appendGeneratedHeaderInput(headerSearchForm, "contentPage", "1");
        appendGeneratedHeaderInput(headerSearchForm, "goodsPage", "1");
    });
}

/**
 * 이전 제출 때 생성한 필터 hidden input만 제거합니다.
 */
function removeGeneratedHeaderInputs(headerSearchForm) {
    headerSearchForm
        .querySelectorAll("input[data-generated-search-filter='true']")
        .forEach(function (input) {
            input.remove();
        });
}

/**
 * 헤더 검색 폼에 필터용 hidden input을 추가합니다.
 */
function appendGeneratedHeaderInput(form, name, value) {
    const input = document.createElement("input");

    input.type = "hidden";
    input.name = name;
    input.value = value;
    input.dataset.generatedSearchFilter = "true";

    form.appendChild(input);
}

/**
 * 기본 5개 장르와 추가 장르 사이를 펼치고 접습니다.
 */
function initializeGenreToggle() {
    const toggleButton = document.getElementById("genreToggleButton");
    const extraOptions = document.getElementById("genreExtraOptions");

    if (!toggleButton || !extraOptions) {
        return;
    }

    toggleButton.addEventListener("click", function () {
        const expanded = toggleButton.dataset.expanded === "true";
        const nextExpanded = !expanded;

        toggleButton.dataset.expanded = String(nextExpanded);
        toggleButton.setAttribute("aria-expanded", String(nextExpanded));
        toggleButton.textContent = nextExpanded ? "장르 접기" : "장르 전체보기";
        extraOptions.hidden = !nextExpanded;
    });
}

/**
 * OTT 선택 모달을 초기화합니다.
 *
 * 취소, 배경 클릭, ESC 닫기에서는 모달을 열기 전 선택 상태로 되돌리고,
 * '선택 완료'에서는 현재 체크 상태를 그대로 유지합니다.
 */
function initializeOttPlatformModal() {
    const modal = document.getElementById("ottPlatformModal");
    const openButton = document.getElementById("ottModalOpenButton");
    const cancelButton = document.getElementById("ottModalCancelButton");
    const confirmButton = document.getElementById("ottModalConfirmButton");

    if (!modal || !openButton) {
        return;
    }

    const closeButtons = modal.querySelectorAll("[data-ott-modal-close]");
    let providerSnapshot = [];

    function createProviderSnapshot() {
        providerSnapshot = Array.from(
            modal.querySelectorAll("input[data-provider-checkbox]")
        ).map(function (checkbox) {
            return {
                value: checkbox.value,
                checked: checkbox.checked
            };
        });
    }

    function restoreProviderSnapshot() {
        const providerCheckboxes = modal.querySelectorAll(
            "input[data-provider-checkbox]"
        );

        providerCheckboxes.forEach(function (checkbox) {
            const savedItem = providerSnapshot.find(function (item) {
                return item.value === checkbox.value;
            });

            checkbox.checked = Boolean(savedItem && savedItem.checked);
        });

        const filterForm = document.getElementById("searchFilterForm");

        if (filterForm) {
            synchronizeAllCheckbox(filterForm, "provider");
        }

        updateOttSelectedSummary();
    }

    function openModal() {
        createProviderSnapshot();
        modal.hidden = false;
        document.body.classList.add("search-modal-open");

        const firstInput = modal.querySelector("input, button");

        if (firstInput) {
            window.setTimeout(function () {
                firstInput.focus();
            }, 0);
        }
    }

    function closeModal(restoreSelection) {
        if (restoreSelection) {
            restoreProviderSnapshot();
        }

        modal.hidden = true;
        document.body.classList.remove("search-modal-open");
        openButton.focus();
    }

    openButton.addEventListener("click", openModal);

    closeButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            closeModal(true);
        });
    });

    if (cancelButton) {
        cancelButton.addEventListener("click", function () {
            closeModal(true);
        });
    }

    if (confirmButton) {
        confirmButton.addEventListener("click", function () {
            updateOttSelectedSummary();
            closeModal(false);
        });
    }

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && !modal.hidden) {
            closeModal(true);
        }
    });

    updateOttSelectedSummary();
}

/**
 * OTT 선택 버튼 아래의 현재 선택 상태 문구를 갱신합니다.
 */
function updateOttSelectedSummary() {
    const summary = document.getElementById("ottSelectedSummary");
    const selectedProviders = document.querySelectorAll(
        "input[data-provider-checkbox]:checked"
    );

    if (!summary) {
        return;
    }

    const selectedNames = Array.from(selectedProviders).map(function (checkbox) {
        return checkbox.dataset.providerName || checkbox.value;
    });

    if (selectedNames.length === 0) {
        summary.textContent = "전체 플랫폼";
        return;
    }

    if (selectedNames.length === 1) {
        summary.textContent = selectedNames[0];
        return;
    }

    summary.textContent = selectedNames[0]
        + " 외 "
        + (selectedNames.length - 1)
        + "개";
}

/**
 * 전체/콘텐츠/상품 결과 탭을 초기화합니다.
 */
function initializeSearchTabs() {
    const searchRoot = document.querySelector("[data-search-root]");

    if (!searchRoot) {
        return;
    }

    const tabButtons = searchRoot.querySelectorAll("[data-search-tab]");

    tabButtons.forEach(function (button) {
        button.addEventListener("click", function () {
            activateSearchTab(
                searchRoot,
                normalizeSearchTab(button.dataset.searchTab),
                true
            );
        });
    });

    activateSearchTab(
        searchRoot,
        normalizeSearchTab(searchRoot.dataset.initialTab),
        false
    );
}

/**
 * 탭에 해당하는 결과 패널과 사이드바 필터 패널을 함께 전환합니다.
 */
function activateSearchTab(searchRoot, targetTab, updateUrl) {
    const normalizedTarget = normalizeSearchTab(targetTab);
    const tabButtons = searchRoot.querySelectorAll("[data-search-tab]");
    const tabPanels = searchRoot.querySelectorAll("[data-search-panel]");

    tabButtons.forEach(function (button) {
        const active = normalizeSearchTab(button.dataset.searchTab)
            === normalizedTarget;

        button.classList.toggle("is-active", active);
        button.setAttribute("aria-pressed", active ? "true" : "false");
    });

    tabPanels.forEach(function (panel) {
        const active = normalizeSearchTab(panel.dataset.searchPanel)
            === normalizedTarget;

        panel.hidden = !active;
        panel.classList.toggle("is-active", active);
    });

    searchRoot.dataset.initialTab = normalizedTarget;

    const filterForm = document.getElementById("searchFilterForm");

    if (filterForm) {
        setFormValue(filterForm, "searchTab", normalizedTarget);
        synchronizeSidebarPanels(filterForm, normalizedTarget);
    }

    if (updateUrl) {
        updateSearchTabQuery(normalizedTarget);
    }
}

/**
 * 탭별로 필요한 왼쪽 필터만 표시합니다.
 * ALL에서는 콘텐츠와 상품 필터를 모두 표시합니다.
 */
function synchronizeSidebarPanels(filterForm, targetTab) {
    const normalizedTarget = normalizeSearchTab(targetTab);
    const filterPanels = filterForm.querySelectorAll(
        "[data-sidebar-filter-panel]"
    );

    filterPanels.forEach(function (panel) {
        const panelType = normalizeSearchTab(panel.dataset.sidebarFilterPanel);
        const visible = normalizedTarget === "ALL" || panelType === normalizedTarget;

        panel.hidden = !visible;
    });

    filterForm.dataset.initialSidebarTab = normalizedTarget;
}

/**
 * 검색 결과 탭 값을 허용된 세 값으로 정규화합니다.
 */
function normalizeSearchTab(tabValue) {
    const normalized = String(tabValue || "ALL")
        .trim()
        .toUpperCase();

    if (normalized === "CONTENT" || normalized === "GOODS") {
        return normalized;
    }

    return "ALL";
}

/**
 * 탭 변경 상태를 새로고침 없이 주소의 searchTab 값에 반영합니다.
 */
function updateSearchTabQuery(searchTab) {
    if (!window.history || !window.history.replaceState) {
        return;
    }

    const currentUrl = new URL(window.location.href);
    currentUrl.searchParams.set("searchTab", searchTab);

    window.history.replaceState(null, "", currentUrl.toString());
}

/**
 * 전체 탭의 '콘텐츠 전체보기', '상품 전체보기' 버튼을 처리합니다.
 */
function initializeSearchMoreButtons() {
    const searchRoot = document.querySelector("[data-search-root]");

    if (!searchRoot) {
        return;
    }

    searchRoot
        .querySelectorAll("[data-search-move-tab]")
        .forEach(function (button) {
            button.addEventListener("click", function () {
                const targetTab = normalizeSearchTab(
                    button.dataset.searchMoveTab
                );

                activateSearchTab(searchRoot, targetTab, true);

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

/**
 * 선택 필터 칩을 클릭하면 해당 폼 요소를 해제하고 즉시 다시 검색합니다.
 */
function initializeActiveFilterChips() {
    const filterForm = document.getElementById("searchFilterForm");

    if (!filterForm) {
        return;
    }

    document.querySelectorAll("[data-filter-chip]").forEach(function (chip) {
        chip.addEventListener("click", function () {
            const inputName = chip.dataset.filterName;
            const inputValue = chip.dataset.filterValue;

            if (!inputName) {
                return;
            }

            const matchingInputs = filterForm.querySelectorAll(
                "[name='" + escapeCssAttributeValue(inputName) + "']"
            );

            matchingInputs.forEach(function (input) {
                const isCheckable = input.type === "checkbox"
                    || input.type === "radio";

                if (inputValue !== undefined
                        && inputValue !== ""
                        && input.value !== inputValue) {
                    return;
                }

                if (isCheckable) {
                    input.checked = false;
                } else {
                    input.value = "";
                }
            });

            synchronizeAllCheckbox(filterForm, "contentCategory");
            synchronizeAllCheckbox(filterForm, "genre");
            synchronizeAllCheckbox(filterForm, "ageRating");
            synchronizeAllCheckbox(filterForm, "provider");
            updateOttSelectedSummary();

            setFormValue(filterForm, "contentPage", "1");
            setFormValue(filterForm, "goodsPage", "1");
            filterForm.submit();
        });
    });
}

/**
 * querySelector 속성 선택자에 들어가는 간단한 값을 이스케이프합니다.
 */
function escapeCssAttributeValue(value) {
    return String(value).replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

/**
 * 페이지 링크를 클릭할 때 현재 사이드바 필터를 대상 URL에 다시 추가합니다.
 */
function initializePaginationFilterPreservation() {
    const filterForm = document.getElementById("searchFilterForm");

    if (!filterForm) {
        return;
    }

    document
        .querySelectorAll(".search-pagination a.page-btn")
        .forEach(function (link) {
            link.addEventListener("click", function (event) {
                event.preventDefault();

                const targetUrl = new URL(link.href, window.location.origin);
                const filterParameterNames = [
                    "contentCategories",
                    "genreCodes",
                    "providerIds",
                    "ageRatings",
                    "productTypes",
                    "minPrice",
                    "maxPrice",
                    "discountOnly",
                    "inStockOnly"
                ];

                filterParameterNames.forEach(function (name) {
                    targetUrl.searchParams.delete(name);
                });

                const filterData = new FormData(filterForm);

                filterData.forEach(function (value, name) {
                    if (!filterParameterNames.includes(name)) {
                        return;
                    }

                    if (String(value).trim() !== "") {
                        targetUrl.searchParams.append(name, value);
                    }
                });

                window.location.href = targetUrl.toString();
            });
        });
}

/**
 * 폼 안의 hidden input 값을 만들거나 갱신합니다.
 */
function setFormValue(form, name, value) {
    let input = form.querySelector("input[name='" + name + "']");

    if (!input) {
        input = document.createElement("input");
        input.type = "hidden";
        input.name = name;
        form.appendChild(input);
    }

    input.value = value;
}
