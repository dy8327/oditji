document.addEventListener("DOMContentLoaded", function () {

    const body = document.body;

    const contextPath =
        body.dataset.contextPath || "";

    const selectAll =
        document.getElementById("selectAll");

    const deleteSelectedBtn =
        document.getElementById("deleteSelectedBtn");

    const checkoutBtn =
        document.getElementById("checkoutBtn");

    /*
     * 장바구니 화면이 아닌 경우 종료
     */
    if (!document.querySelector(".cart-item")) {
        return;
    }

    updateSelectAllState();
    updateSummary();

    /*
     * 전체 선택
     */
    if (selectAll) {

        selectAll.addEventListener("change", function () {

            const availableChecks =
                getAvailableItemChecks();

            availableChecks.forEach(function (checkbox) {

                checkbox.checked =
                    selectAll.checked;
            });

            updateSummary();
        });
    }

    /*
     * 개별 선택
     */
    document.addEventListener("change", function (event) {

        const checkbox =
            event.target.closest(".cart-item-check");

        if (!checkbox) {
            return;
        }

        updateSelectAllState();
        updateSummary();
    });

    /*
     * 수량 증가·감소
     */
    document.addEventListener("click", async function (event) {

        const button =
            event.target.closest(".quantity-btn");

        if (!button) {
            return;
        }

        const cartItemNo =
            button.dataset.cartItemNo;

        const row =
            getCartRow(cartItemNo);

        if (!row) {
            return;
        }

        const input =
            row.querySelector(".quantity-input");

        if (!input || input.disabled) {
            return;
        }

        const stock =
            parsePositiveInteger(
                row.dataset.stock,
                0
            );

        let quantity =
            parsePositiveInteger(
                input.value,
                1
            );

        if (button.classList.contains("plus")) {

            if (quantity >= stock) {

                alert(
                    "현재 재고는 "
                    + stock
                    + "개입니다."
                );

                return;
            }

            quantity++;

        } else {

            quantity =
                Math.max(
                    1,
                    quantity - 1
                );
        }

        await changeQuantity(
            row,
            input,
            quantity
        );
    });

    /*
     * 수량 직접 입력
     */
    document.addEventListener("change", async function (event) {

        const input =
            event.target.closest(".quantity-input");

        if (!input || input.disabled) {
            return;
        }

        const cartItemNo =
            input.dataset.cartItemNo;

        const row =
            getCartRow(cartItemNo);

        if (!row) {
            return;
        }

        const stock =
            parsePositiveInteger(
                row.dataset.stock,
                0
            );

        let quantity =
            parsePositiveInteger(
                input.value,
                1
            );

        if (quantity > stock) {

            alert(
                "현재 재고는 "
                + stock
                + "개입니다."
            );

            quantity =
                stock;
        }

        if (quantity < 1) {
            quantity = 1;
        }

        await changeQuantity(
            row,
            input,
            quantity
        );
    });

    /*
     * 개별 삭제
     */
    document.addEventListener("click", async function (event) {

        const button =
            event.target.closest(".cart-delete-btn");

        if (!button) {
            return;
        }

        const cartItemNo =
            button.dataset.cartItemNo;

        if (!cartItemNo) {
            return;
        }

        const confirmed =
            confirm(
                "이 상품을 장바구니에서 삭제할까요?"
            );

        if (!confirmed) {
            return;
        }

        button.disabled = true;

        try {

            const result =
                await requestJson(
                    contextPath + "/cart/delete",
                    {
                        cartItemNo:
                            Number(cartItemNo)
                    }
                );

            if (!result.success) {

                handleServerFailure(result);
                return;
            }

            const row =
                getCartRow(cartItemNo);

            if (row) {
                row.remove();
            }

            refreshAfterDelete();

        } catch (error) {

            console.error(error);

            alert(
                "상품 삭제 중 오류가 발생했습니다."
            );

        } finally {

            button.disabled = false;
        }
    });

    /*
     * 선택 삭제
     */
    if (deleteSelectedBtn) {

        deleteSelectedBtn.addEventListener(
            "click",
            async function () {

                const selectedChecks =
                    Array.from(
                        document.querySelectorAll(
                            ".cart-item-check:checked"
                        )
                    );

                if (selectedChecks.length === 0) {

                    alert(
                        "삭제할 상품을 선택해주세요."
                    );

                    return;
                }

                const confirmed =
                    confirm(
                        "선택한 상품을 장바구니에서 삭제할까요?"
                    );

                if (!confirmed) {
                    return;
                }

                const cartItemNos =
                    selectedChecks.map(
                        function (checkbox) {

                            return Number(
                                checkbox.value
                            );
                        }
                    );

                deleteSelectedBtn.disabled = true;

                try {

                    const result =
                        await requestJson(
                            contextPath
                                + "/cart/delete-selected",
                            {
                                cartItemNos:
                                    cartItemNos
                            }
                        );

                    if (!result.success) {

                        handleServerFailure(result);
                        return;
                    }

                    cartItemNos.forEach(
                        function (cartItemNo) {

                            const row =
                                getCartRow(
                                    String(cartItemNo)
                                );

                            if (row) {
                                row.remove();
                            }
                        }
                    );

                    refreshAfterDelete();

                } catch (error) {

                    console.error(error);

                    alert(
                        "선택 상품 삭제 중 오류가 발생했습니다."
                    );

                } finally {

                    deleteSelectedBtn.disabled = false;
                }
            }
        );
    }

    /*
     * 선택 상품 주문하기
     */
    if (checkoutBtn) {

        checkoutBtn.addEventListener(
            "click",
            async function () {

                const selectedChecks =
                    Array.from(
                        document.querySelectorAll(
                            ".cart-item-check:checked"
                        )
                    );

                if (selectedChecks.length === 0) {

                    alert(
                        "주문할 상품을 선택해주세요."
                    );

                    return;
                }

                const cartItemNos =
                    selectedChecks.map(
                        function (checkbox) {

                            return Number(
                                checkbox.value
                            );
                        }
                    );

                checkoutBtn.disabled = true;

                try {

                    const result =
                        await requestJson(
                            contextPath
                                + "/order/checkout",
                            {
                                cartItemNos:
                                    cartItemNos
                            }
                        );

                    if (!result.success) {

                        handleServerFailure(result);
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

                    alert(
                        "주문서 작성 중 오류가 발생했습니다."
                    );

                } finally {

                    checkoutBtn.disabled = false;
                }
            }
        );
    }

    async function changeQuantity(
        row,
        input,
        quantity
    ) {

        const cartItemNo =
            input.dataset.cartItemNo;

        const previousQuantity =
            parsePositiveInteger(
                input.dataset.previousQuantity,
                1
            );

        input.disabled = true;

        const buttons =
            row.querySelectorAll(
                ".quantity-btn"
            );

        buttons.forEach(function (button) {
            button.disabled = true;
        });

        try {

            const result =
                await requestJson(
                    contextPath + "/cart/update",
                    {
                        cartItemNo:
                            Number(cartItemNo),

                        quantity:
                            quantity
                    }
                );

            if (!result.success) {

                input.value =
                    previousQuantity;

                handleServerFailure(result);
                return;
            }

            input.value =
                quantity;

            input.dataset.previousQuantity =
                String(quantity);

            updateRowPrice(row);
            updateSummary();

        } catch (error) {

            console.error(error);

            input.value =
                previousQuantity;

            alert(
                "수량 변경 중 오류가 발생했습니다."
            );

        } finally {

            input.disabled = false;

            buttons.forEach(function (button) {
                button.disabled = false;
            });
        }
    }

    function updateRowPrice(row) {

        const unitPrice =
            parsePositiveInteger(
                row.dataset.unitPrice,
                0
            );

        const input =
            row.querySelector(
                ".quantity-input"
            );

        const quantity =
            parsePositiveInteger(
                input ? input.value : 0,
                0
            );

        const totalElement =
            row.querySelector(
                ".item-total-price"
            );

        if (totalElement) {

            totalElement.textContent =
                formatPrice(
                    unitPrice * quantity
                );
        }
    }

    function updateSummary() {

        let selectedTotal = 0;

        document.querySelectorAll(
            ".cart-item"
        ).forEach(function (row) {

            const checkbox =
                row.querySelector(
                    ".cart-item-check"
                );

            if (!checkbox
                    || !checkbox.checked
                    || checkbox.disabled) {

                return;
            }

            const unitPrice =
                parsePositiveInteger(
                    row.dataset.unitPrice,
                    0
                );

            const input =
                row.querySelector(
                    ".quantity-input"
                );

            const quantity =
                parsePositiveInteger(
                    input ? input.value : 0,
                    0
                );

            selectedTotal +=
                unitPrice * quantity;
        });

        const selectedProductPrice =
            document.getElementById(
                "selectedProductPrice"
            );

        const finalPrice =
            document.getElementById(
                "finalPrice"
            );

        if (selectedProductPrice) {

            selectedProductPrice.textContent =
                formatPrice(selectedTotal);
        }

        if (finalPrice) {

            finalPrice.textContent =
                formatPrice(selectedTotal);
        }

        if (checkoutBtn) {

            checkoutBtn.disabled =
                selectedTotal <= 0;
        }
    }

    function updateSelectAllState() {

        if (!selectAll) {
            return;
        }

        const availableChecks =
            getAvailableItemChecks();

        if (availableChecks.length === 0) {

            selectAll.checked = false;
            selectAll.indeterminate = false;
            selectAll.disabled = true;

            return;
        }

        selectAll.disabled = false;

        const checkedCount =
            availableChecks.filter(
                function (checkbox) {

                    return checkbox.checked;
                }
            ).length;

        selectAll.checked =
            checkedCount
                === availableChecks.length;

        selectAll.indeterminate =
            checkedCount > 0
                && checkedCount
                    < availableChecks.length;
    }

    function getAvailableItemChecks() {

        return Array.from(
            document.querySelectorAll(
                ".cart-item-check:not(:disabled)"
            )
        );
    }

    function getCartRow(cartItemNo) {

        return document.querySelector(
            ".cart-item[data-cart-item-no='"
                + cartItemNo
                + "']"
        );
    }

    function refreshAfterDelete() {

        updateSelectAllState();
        updateSummary();

        const remainingItems =
            document.querySelectorAll(
                ".cart-item"
            );

        if (remainingItems.length === 0) {
            window.location.reload();
        }
    }

    async function requestJson(
        url,
        requestBody
    ) {

        const response =
            await fetch(
                url,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            requestBody
                        )
                }
            );

        if (!response.ok) {

            throw new Error(
                "HTTP "
                    + response.status
            );
        }

        return response.json();
    }

    function handleServerFailure(result) {

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

        alert(
            result.message
                || "요청 처리에 실패했습니다."
        );
    }

    function parsePositiveInteger(
        value,
        defaultValue
    ) {

        const parsed =
            Number.parseInt(
                value,
                10
            );

        if (Number.isNaN(parsed)
                || parsed < 0) {

            return defaultValue;
        }

        return parsed;
    }

    function formatPrice(value) {

        return Number(value)
            .toLocaleString("ko-KR")
            + "원";
    }
});