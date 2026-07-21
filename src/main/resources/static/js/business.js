document.addEventListener("DOMContentLoaded", function () {

    const eventImageInput =
        document.getElementById("eventImage");

    const eventImageFileName =
        document.getElementById("eventImageFileName");


    const productList =
        document.getElementById("productList");


    const productSearchModal =
        document.getElementById("productSearchModal");

    const productSearchModalClose =
        document.getElementById("productSearchModalClose");


    const productSearchKeyword =
        document.getElementById("productSearchKeyword");

    const productSearchResetButton =
        document.getElementById("productSearchResetButton");


    const productSearchRows =
        document.querySelectorAll(".product-search-row");


    const productSelectButtons =
        document.querySelectorAll(".product-select-button");


    const productSearchNoResult =
        document.getElementById("productSearchNoResult");


    const startDateInput =
        document.getElementById("startDate");

    const endDateInput =
        document.getElementById("endDate");


    /*
     * 이벤트 등록/수정 화면에서 공통으로 사용하는 폼
     *
     * 두 화면 모두 id="eventForm"을 사용하므로
     * 어느 페이지에서 로드되어도 동일하게 동작한다.
     */
    const eventRegisterForm =
        document.getElementById("eventForm");


    let currentProductItem = null;



    /*
     * 이미지 파일명 출력
     */
    if (eventImageInput) {

        eventImageInput.addEventListener(
            "change",
            function () {

                if (eventImageInput.files.length === 0) {

                    eventImageFileName.textContent =
                        "선택된 파일 없음";

                    return;
                }


                eventImageFileName.textContent =
                    eventImageInput.files[0].name;

            }
        );

    }



    /*
     * 이벤트 종료일 최소 날짜 설정
     */
    if (startDateInput) {

        startDateInput.addEventListener(
            "change",
            function () {

                endDateInput.min =
                    startDateInput.value;


                if (
                    endDateInput.value &&
                    endDateInput.value <
                    startDateInput.value
                ) {

                    endDateInput.value =
                        "";

                }

            }
        );

    }



    /*
     * 상품 검색 모달 열기
     */
    document.addEventListener(
        "click",
        function (event) {


            if (
                event.target.classList.contains(
                    "productSearchButton"
                )
            ) {


                currentProductItem =
                    event.target.closest(
                        ".product-item"
                    );


                productSearchModal.classList.add(
                    "active"
                );


                document.body.style.overflow =
                    "hidden";


                if (productSearchKeyword) {

                    productSearchKeyword.focus();

                }

            }


        }
    );



    /*
     * 상품 행 추가
     */
    document.addEventListener(
        "click",
        function (event) {


            if (
                event.target.classList.contains(
                    "addProductButton"
                )
            ) {


                const productItem =
                    document.createElement(
                        "div"
                    );


                productItem.className =
                    "product-item";


                productItem.innerHTML = `

                    <div class="input-with-btn">

                        <input type="hidden"
                            name="productNoList"
                            class="productNo">


                        <input class="form-input productName"
                            type="text"
                            name="productNameList"
                            placeholder="연결할 상품을 선택하세요."
                            readonly>


                        <button class="btn btn-dark productSearchButton"
                                type="button">
                            상품 검색
                        </button>

                    </div>


                    <div class="discount-row">

                        <div class="discount-controls">

                            <label class="form-label">
                                할인율 (%)
                            </label>


                            <input class="form-input productDiscountRate"
                                type="number"
                                name="discountRateList"
                                min="0"
                                max="100"
                                value="0">


                            <button class="btn btn-dark removeProductButton"
                                    type="button">
                                -
                            </button>

                        </div>


                        <p class="form-hint productDiscountPreview">
                            상품을 선택하면 할인 적용가가 표시됩니다.
                        </p>

                    </div>

                `;


                productList.appendChild(
                    productItem
                );

            }


        }
    );



    /*
     * 상품 행 삭제
     */
    document.addEventListener(
        "click",
        function (event) {


            if (
                event.target.classList.contains(
                    "removeProductButton"
                )
            ) {


                const item =
                    event.target.closest(
                        ".product-item"
                    );


                item.remove();


            }


        }
    );



    /*
     * 모달 닫기
     */
    function closeProductSearchModal() {


        productSearchModal.classList.remove(
            "active"
        );


        document.body.style.overflow =
            "";


    }



    if (productSearchModalClose) {

        productSearchModalClose.addEventListener(
            "click",
            closeProductSearchModal
        );

    }



    /*
     * 모달 배경 클릭 닫기
     */
    productSearchModal.addEventListener(
        "click",
        function (event) {


            if (
                event.target ===
                productSearchModal
            ) {

                closeProductSearchModal();

            }


        }
    );



    /*
     * ESC 닫기
     */
    document.addEventListener(
        "keydown",
        function (event) {


            if (
                event.key === "Escape" &&
                productSearchModal.classList.contains(
                    "active"
                )
            ) {

                closeProductSearchModal();

            }


        }
    );



    /*
     * 상품 검색
     */
    if (productSearchKeyword) {


        productSearchKeyword.addEventListener(
            "input",
            function () {


                const keyword =
                    productSearchKeyword.value
                    .trim()
                    .toLowerCase();


                let visibleCount = 0;



                productSearchRows.forEach(
                    function (row) {


                        const text =
                            row.innerText
                            .toLowerCase();



                        const matched =
                            keyword === "" ||
                            text.includes(keyword);



                        row.style.display =
                            matched
                            ? ""
                            : "none";



                        if (matched) {

                            visibleCount++;

                        }


                    }
                );



                if (productSearchNoResult) {


                    productSearchNoResult.style.display =
                        visibleCount === 0
                        ? "block"
                        : "none";


                }


            }
        );


    }



    /*
     * 검색 초기화
     */
    if (productSearchResetButton) {


        productSearchResetButton.addEventListener(
            "click",
            function () {


                productSearchKeyword.value =
                    "";


                productSearchRows.forEach(
                    function(row){

                        row.style.display =
                            "";

                    }
                );


                if(productSearchNoResult){

                    productSearchNoResult.style.display =
                        "none";

                }


                productSearchKeyword.focus();


            }
        );


    }



    /*
     * 상품 선택
     */
    productSelectButtons.forEach(
        function(button){


            button.addEventListener(
                "click",
                function(){


                    if(!currentProductItem){

                        return;

                    }


                    currentProductItem
                        .querySelector(".productNo")
                        .value =
                        button.dataset.productNo;


                    currentProductItem
                        .querySelector(".productName")
                        .value =
                        button.dataset.productName;



                    // 여기 추가
                    currentProductItem.dataset.price =
                        button.dataset.productPrice || "";



                    const discountInput =
                        currentProductItem
                            .querySelector(".productDiscountRate");


                    const preview =
                        currentProductItem
                            .querySelector(".productDiscountPreview");



                    updateDiscountPreview(
                        Number(currentProductItem.dataset.price),
                        discountInput,
                        preview
                    );



                    closeProductSearchModal();


                }
            );


        }
    );



    /*
     * 할인 미리보기
     */
    function updateDiscountPreview(
        price,
        discountInput,
        preview
    ){


        const rate =
            Number(
                discountInput.value
            );


        if(
            !price ||
            Number.isNaN(rate)
        ){

            preview.textContent =
                "상품을 선택하면 할인 적용가가 표시됩니다.";

            return;

        }


        const discountedPrice =
            Math.round(
                price *
                (100 - rate)
                / 100
            );


        preview.textContent =
            "원가 "
            + price.toLocaleString()
            + "원 → 할인 적용가 "
            + discountedPrice.toLocaleString()
            + "원 (할인율 "
            + rate
            + "%)";


    }



    document.addEventListener(
        "input",
        function(event){

            if(
                event.target.classList.contains(
                    "productDiscountRate"
                )
            ){

                const item =
                    event.target.closest(
                        ".product-item"
                    );


                const price =
                    item.dataset.price;


                const preview =
                    item.querySelector(
                        ".productDiscountPreview"
                    );


                updateDiscountPreview(
                    Number(price),
                    event.target,
                    preview
                );

            }

        }
    );



    /*
     * 등록 전 검증
     */
    if(eventRegisterForm){


        eventRegisterForm.addEventListener(
            "submit",
            function(event){


                const productNos =
                    document.querySelectorAll(
                        ".productNo"
                    );


                let selected = false;



                productNos.forEach(
                    function(input){


                        if(input.value){

                            selected = true;

                        }


                    }
                );



                if(!selected){


                    event.preventDefault();



                    alert(
                        "이벤트에 연결할 상품을 선택해주세요."
                    );


                }


            }
        );


    }


});