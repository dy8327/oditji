document.addEventListener("DOMContentLoaded", function () {

    const body = document.body;

    const contextPath =
        body.dataset.contextPath || "";

    initializeGoodsFilter();
    initializeImageGallery();
    initializeCartButton();
    initializeBuyButton();

    function initializeGoodsFilter() {

        const filterForm =
            document.getElementById(
                "goodsFilterForm"
            );

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

        filterCheckboxes.forEach(
            function (checkbox) {

                checkbox.addEventListener(
                    "change",
                    function () {

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
                    }
                );
            }
        );

        allCheckboxes.forEach(
            function (allCheckbox) {

                allCheckbox.addEventListener(
                    "change",
                    function () {

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

                            groupItems.forEach(
                                function (item) {

                                    item.checked = false;
                                }
                            );

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
                    }
                );
            }
        );

        filterForm.addEventListener(
            "submit",
            function () {

                const pageInput =
                    filterForm.querySelector(
                        "input[name='page']"
                    );

                if (pageInput) {
                    pageInput.value = "1";
                }
            }
        );
    }

    function initializeImageGallery() {

        const mainImage =
            document.getElementById(
                "mainImage"
            );

        const galleryImages =
            document.querySelectorAll(
                ".image-gallery img"
            );

        if (!mainImage
                || galleryImages.length === 0) {

            return;
        }

        galleryImages.forEach(
            function (galleryImage) {

                galleryImage.addEventListener(
                    "click",
                    function () {

                        const fullImage =
                            galleryImage.dataset.full;

                        if (!fullImage) {
                            return;
                        }

                        mainImage.src =
                            fullImage;

                        galleryImages.forEach(
                            function (item) {

                                item.classList.remove(
                                    "is-active"
                                );
                            }
                        );

                        galleryImage.classList.add(
                            "is-active"
                        );
                    }
                );
            }
        );
    }

    function initializeCartButton() {

        const cartButton =
            document.querySelector(
                ".detail-info .cart-btn"
            );

        if (!cartButton) {
            return;
        }

        cartButton.addEventListener(
            "click",
            async function () {

                const productNo =
                    Number(
                        cartButton.dataset.productNo
                    );

                if (!Number.isInteger(productNo)
                        || productNo <= 0) {

                    alert(
                        "상품 정보가 올바르지 않습니다."
                    );

                    return;
                }

                cartButton.disabled = true;

                const originalText =
                    cartButton.textContent;

                cartButton.textContent =
                    "담는 중...";

                try {

                    const response =
                        await fetch(
                            contextPath
                                + "/cart/add",
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        {
                                            productNo:
                                                productNo,

                                            quantity:
                                                1
                                        }
                                    )
                            }
                        );

                    if (!response.ok) {

                        throw new Error(
                            "HTTP "
                                + response.status
                        );
                    }

                    const result =
                        await response.json();

                    if (result.loginRequired) {

                        const currentUrl =
                            window.location.pathname
                            + window.location.search;

                        window.location.href =
                            contextPath
                            + "/member/login?redirect="
                            + encodeURIComponent(
                                currentUrl
                            );

                        return;
                    }

                    if (!result.success) {

                        alert(
                            result.message
                                || "장바구니 처리에 실패했습니다."
                        );

                        return;
                    }

                    const goToCart =
                        confirm(
                            "장바구니에 상품을 담았습니다.\n"
                            + "장바구니로 이동할까요?"
                        );

                    if (goToCart) {

                        window.location.href =
                            contextPath
                            + "/cart";
                    }

                } catch (error) {

                    console.error(error);

                    alert(
                        "장바구니 처리 중 오류가 발생했습니다."
                    );

                } finally {

                    cartButton.disabled = false;

                    cartButton.textContent =
                        originalText;
                }
            }
        );
    }

    function initializeBuyButton() {

        const buyButton =
            document.querySelector(
                ".detail-info .buy-btn"
            );

        if (!buyButton) {
            return;
        }

        buyButton.addEventListener(
            "click",
            function () {

                alert(
                    "바로 구매 기능은 주문 로직과 연결 후 사용할 수 있습니다."
                );
            }
        );
    }
});