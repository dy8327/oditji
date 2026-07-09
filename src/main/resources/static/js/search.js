document.addEventListener("DOMContentLoaded", function () {

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
     * 필터 적용 시 첫 페이지부터 검색한다.
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
});