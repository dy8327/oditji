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

    document.addEventListener(
        "click",
        async function (event) {

            const cartButton =
                event.target.closest(
                    ".cart-btn"
                );

            if (!cartButton
                    || cartButton.disabled) {

                return;
            }

            const productNo =
                Number(
                    cartButton.dataset.productNo
                );

            if (!Number.isInteger(productNo)
                    || productNo <= 0) {

                await Swal.fire({
                    icon: "warning",
                    text: "상품 정보가 올바르지 않습니다.",
                    confirmButtonText: "확인"
                });

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

                    const moveLogin =
                        await Swal.fire({
                            icon: "info",
                            text:
                                "장바구니는 로그인 후 이용할 수 있습니다.\n로그인 페이지로 이동하시겠습니까?",
                            showCancelButton: true,
                            confirmButtonText: "이동",
                            cancelButtonText: "취소"
                        });


                    if (moveLogin.isConfirmed) {

                        const currentUrl =
                            window.location.pathname
                            + window.location.search;


                        window.location.href =
                            contextPath
                            + "/member/login?redirect="
                            + encodeURIComponent(currentUrl);
                    }


                    return;
                }


                if (!result.success) {

                    await Swal.fire({
                        icon: "error",
                        text:
                            result.message
                            || "장바구니 처리에 실패했습니다.",
                        confirmButtonText: "확인"
                    });

                    return;
                }


                const goToCart =
                    await Swal.fire({
                        icon: "success",
                        text:
                            "장바구니에 상품을 담았습니다.\n장바구니로 이동할까요?",
                        showCancelButton: true,
                        confirmButtonText: "이동",
                        cancelButtonText: "계속 쇼핑"
                    });


                if (goToCart.isConfirmed) {

                    window.location.href =
                        contextPath
                        + "/cart";
                }


            } catch (error) {

                console.error(error);


                await Swal.fire({
                    icon: "error",
                    text:
                        "장바구니 처리 중 오류가 발생했습니다.",
                    confirmButtonText: "확인"
                });


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
            async function () {

                const productNo =
                    Number(
                        buyButton.dataset.productNo
                    );

                if (!Number.isInteger(productNo)
                        || productNo <= 0) {

                    await Swal.fire({
                        icon: "warning",
                        text: "상품 정보가 올바르지 않습니다.",
                        confirmButtonText: "확인"
                    });

                    return;
                }

                buyButton.disabled = true;

                const originalText =
                    buyButton.textContent;

                buyButton.textContent =
                    "처리 중...";

                try {

                    const response =
                        await fetch(
                            contextPath
                                + "/order/direct",
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

                        await Swal.fire({
                            icon: "error",
                            text:
                                result.message
                                || "주문서 작성에 실패했습니다.",
                            confirmButtonText: "확인"
                        });

                        return;
                    }

                    window.location.href =
                        contextPath
                        + (
                            result.redirectUrl
                                || "/order"
                        );

                } catch (error) {

                    console.error(error);

                    await Swal.fire({
                        icon: "error",
                        text: "바로 구매 처리 중 오류가 발생했습니다.",
                        confirmButtonText: "확인"
                    });

                } finally {

                    buyButton.disabled = false;

                    buyButton.textContent =
                        originalText;
                }
            }
        );
    }
});

/*
 * 상품 카드 이미지 로딩 실패 처리를 HTML의 onerror 속성에서 분리한다.
 * 비상호작용 요소인 img에 인라인 이벤트 속성을 지정하지 않으면서
 * 기존 NO IMAGE 대체 화면 동작은 그대로 유지한다.
 */
document.addEventListener("DOMContentLoaded", function () {

    const cardImages =
        document.querySelectorAll(
            ".goods-card-image[data-fallback-target]"
        );

    cardImages.forEach(function (image) {

        image.addEventListener(
            "error",
            function () {

                const fallbackId =
                    image.dataset.fallbackTarget;

                const fallbackElement =
                    document.getElementById(
                        fallbackId
                    );

                image.style.display = "none";

                if (fallbackElement) {
                    fallbackElement.style.display = "flex";
                }
            },
            { once: true }
        );
    });
});
