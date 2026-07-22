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


    const extendEndDateInput =
        document.getElementById("extendEndDate");


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
     * 등록/수정 처리 결과 알림
     *
     * JSP에서 <script>alert("${successMessage}")</script> 형태로
     * 직접 스크립트를 찍어내던 부분을 옮겨온 것이다.
     * 서버는 body 태그의 data-success-message / data-error-message
     * 속성에 c:out으로 이스케이프된 값을 내려주고,
     * business.js는 그 값이 있을 때만 알림을 띄운다.
     */
    const successMessage =
        document.body.dataset.successMessage;

    if (successMessage) {

        alert(successMessage);

    }


    const errorMessage =
        document.body.dataset.errorMessage;

    if (errorMessage) {

        alert(errorMessage);

    }



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
     * 이벤트 연장 - 최소 선택 날짜 설정
     *
     * eventExtend.jsp에 있던 인라인 스크립트를 옮겨온 것이다.
     * 현재 종료일은 input의 data-current-end-date 속성으로 전달받고,
     * 그 다음 날부터 선택할 수 있도록 min 값을 설정한다.
     */
    if (extendEndDateInput) {

        const currentEndDate =
            extendEndDateInput.dataset.currentEndDate;

        if (currentEndDate) {

            const minimumDate =
                new Date(currentEndDate + "T00:00:00");

            minimumDate.setDate(
                minimumDate.getDate() + 1
            );

            const year =
                minimumDate.getFullYear();

            const month =
                String(minimumDate.getMonth() + 1).padStart(2, "0");

            const day =
                String(minimumDate.getDate()).padStart(2, "0");

            extendEndDateInput.min =
                year + "-" + month + "-" + day;

        }

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
                        ".event-product-item"
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
                    "event-product-item";


                productItem.innerHTML = `

                    <input type="hidden"
                        name="productNoList"
                        class="productNo">


                    <div class="product-row">

                        <input class="form-input productName"
                            type="text"
                            name="productNameList"
                            placeholder="연결할 상품을 선택하세요."
                            readonly>


                        <button class="btn btn-dark productSearchButton"
                                type="button">
                            상품 검색
                        </button>


                        <label class="form-label discount-label">
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
                        ".event-product-item"
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
                        ".event-product-item"
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