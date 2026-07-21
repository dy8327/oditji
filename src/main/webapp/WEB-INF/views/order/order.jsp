<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<!DOCTYPE html>
<html lang="ko">

<head>

    <meta charset="UTF-8">
    <title>ODITJI | 주문서</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/order.css">

</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="order-container">

    <h1>주문서</h1>

    <!-- ================= ORDER ITEMS ================= -->
    <section class="order-items">

        <c:forEach var="item" items="${orderItems}">

            <article class="order-item">

                <div class="order-img">

                    <c:choose>

                        <c:when test="${empty item.mainImage}">
                            <div class="no-image">
                                NO IMAGE
                            </div>
                        </c:when>

                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}${item.mainImage}"
                                 alt="${item.productName}">
                        </c:otherwise>

                    </c:choose>

                </div>

                <div class="order-info">

                    <h3>
                        ${item.productName}
                    </h3>

                    <p class="order-item-business">
                        ${item.businessName}
                    </p>

                    <p>
                        수량: ${item.quantity}
                    </p>

                    <p>
                        단가: ₩
                        <fmt:formatNumber
                            value="${item.discountPrice}"
                            pattern="#,###"/>
                    </p>

                </div>

                <div class="order-subtotal">

                    ₩
                    <fmt:formatNumber
                        value="${item.itemTotalPrice}"
                        pattern="#,###"/>

                </div>

            </article>

        </c:forEach>

    </section>

    <!-- ================= ORDER FORM ================= -->
    <section class="order-form">

        <h2>배송 정보</h2>

        <input type="text"
               id="receiver"
               placeholder="받는 사람"
               value="${defaultReceiverName}"
               maxlength="50"
               readonly>

        <input type="text"
               id="phone"
               placeholder="연락처"
               value="${defaultReceiverPhone}"
               maxlength="20"
               readonly>

        <div class="address-search-row">

            <input type="text"
                   id="zipcode"
                   placeholder="우편번호"
                   readonly>

            <button type="button"
                    id="addressSearchBtn"
                    class="address-search-btn">
                주소 검색
            </button>

        </div>

        <input type="text"
               id="address1"
               placeholder="기본 주소"
               readonly>

        <input type="text"
               id="address2"
               placeholder="상세 주소를 입력해주세요"
               maxlength="100"
               autocomplete="off">

    </section>

    <!-- ================= SUMMARY ================= -->
    <section class="order-summary">

        <div>

            <span>총 상품 금액</span>

            <span id="totalPrice">

                <fmt:formatNumber
                    value="${totalPrice}"
                    pattern="#,###"/>

            </span>

        </div>

        <div>

            <span>배송비</span>
            <span>0</span>

        </div>

        <div class="final">

            <span>최종 결제 금액</span>

            <span id="finalPrice">

                <fmt:formatNumber
                    value="${totalPrice}"
                    pattern="#,###"/>

            </span>

        </div>

        <p class="order-error"
           id="orderError"
           style="display:none;">
        </p>

        <button id="orderBtn"
                type="button">
            결제하기
        </button>

        <button id="cancelBtn"
                type="button">
            취소하기
        </button>

    </section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<!-- 카카오 우편번호 서비스 -->
<script src="https://t1.kakaocdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js"></script>

<!-- PortOne V2 브라우저 SDK -->
<script src="https://cdn.portone.io/v2/browser-sdk.js"></script>

<script>

(function () {

    "use strict";

    var contextPath =
        "${pageContext.request.contextPath}";

    var customerEmail =
        "${defaultReceiverEmail}";

    var orderBtn =
        document.getElementById("orderBtn");

    var cancelBtn =
        document.getElementById("cancelBtn");

    var addressSearchBtn =
        document.getElementById("addressSearchBtn");

    var errorBox =
        document.getElementById("orderError");

    var receiverInput =
        document.getElementById("receiver");

    var phoneInput =
        document.getElementById("phone");

    var zipcodeInput =
        document.getElementById("zipcode");

    var address1Input =
        document.getElementById("address1");

    var address2Input =
        document.getElementById("address2");

    /*
     * =========================================================
     * 오류 메시지 출력
     * =========================================================
     */
    function showError(message) {

        errorBox.textContent = message;
        errorBox.style.display = "block";
    }

    /*
     * =========================================================
     * 오류 메시지 초기화
     * =========================================================
     */
    function clearError() {

        errorBox.textContent = "";
        errorBox.style.display = "none";
    }

    /*
     * =========================================================
     * 결제 버튼 상태 복구
     * =========================================================
     */
    function restoreOrderButton() {

        orderBtn.disabled = false;
        orderBtn.textContent = "결제하기";
    }

    /*
     * =========================================================
     * JSON 응답 처리
     * =========================================================
     */
    function readJsonResponse(response) {

        return response.json()
            .catch(function () {

                throw new Error(
                    "서버 응답을 읽을 수 없습니다."
                );
            })
            .then(function (data) {

                if (!response.ok) {

                    throw new Error(
                        data.message
                        || "HTTP 오류: "
                        + response.status
                    );
                }

                return data;
            });
    }

    /*
     * =========================================================
     * 주소 검색
     * =========================================================
     */
    function openAddressSearch() {

        clearError();

        if (typeof kakao === "undefined"
                || typeof kakao.Postcode === "undefined") {

            showError(
                "주소 검색 서비스를 불러오지 못했습니다."
            );

            return;
        }

        new kakao.Postcode({

            oncomplete: function (data) {

                var selectedAddress = "";

                if (data.userSelectedType === "R") {

                    selectedAddress =
                        data.roadAddress;

                } else {

                    selectedAddress =
                        data.jibunAddress;
                }

                if (!selectedAddress) {

                    selectedAddress =
                        data.address
                        || data.roadAddress
                        || data.jibunAddress;
                }

                zipcodeInput.value =
                    data.zonecode || "";

                address1Input.value =
                    selectedAddress || "";

                address2Input.value = "";

                clearError();
                address2Input.focus();
            }

        }).open();
    }

    /*
     * =========================================================
     * 배송 정보 검증 및 전체 주소 생성
     * =========================================================
     */
    function createDeliveryInformation() {

        var receiverName =
            receiverInput.value.trim();

        var receiverPhone =
            phoneInput.value.trim();

        var zipcode =
            zipcodeInput.value.trim();

        var address1 =
            address1Input.value.trim();

        var address2 =
            address2Input.value.trim();

        if (!receiverName) {
            throw new Error(
                "받는 사람 정보를 확인해주세요."
            );
        }

        if (!receiverPhone) {
            throw new Error(
                "연락처 정보를 확인해주세요."
            );
        }

        if (!zipcode || !address1) {
            throw new Error(
                "주소 검색 버튼을 눌러 "
                + "배송지 주소를 선택해주세요."
            );
        }

        if (!address2) {

            address2Input.focus();

            throw new Error(
                "상세 주소를 입력해주세요."
            );
        }

        var fullAddress =
            "(" + zipcode + ") "
            + address1 + " "
            + address2;

        if (fullAddress.length > 300) {
            throw new Error(
                "배송지 주소는 300자 이하로 입력해주세요."
            );
        }

        return {

            receiverName:
                receiverName,

            receiverPhone:
                receiverPhone,

            address:
                fullAddress
        };
    }

    /*
     * =========================================================
     * 서버 결제 준비
     * =========================================================
     */
    function preparePayment(deliveryInformation) {

        return fetch(
            contextPath
                + "/order/payment/prepare",
            {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(
                    deliveryInformation
                )
            }
        )
        .then(readJsonResponse)
        .then(function (data) {

            if (!data.success) {

                if (data.loginRequired) {

                    location.href =
                        contextPath
                        + "/member/login"
                        + "?redirect=/order";

                    return null;
                }

                throw new Error(
                    data.message
                    || "결제 준비에 실패했습니다."
                );
            }

            return data;
        });
    }

    /*
     * =========================================================
     * 포트원 결제창 호출
     * =========================================================
     */
    function requestPortOnePayment(
            prepareData,
            deliveryInformation) {

        if (typeof PortOne === "undefined"
                || typeof PortOne.requestPayment
                    !== "function") {

            throw new Error(
                "결제 모듈을 불러오지 못했습니다."
            );
        }

        var customer = {

            fullName:
                deliveryInformation.receiverName,

            phoneNumber:
                deliveryInformation.receiverPhone
                    .replace(/[^0-9]/g, "")
        };

        if (customerEmail) {
            customer.email = customerEmail;
        }

        return PortOne.requestPayment({

            storeId:
                prepareData.storeId,

            channelKey:
                prepareData.channelKey,

            paymentId:
                prepareData.paymentId,

            orderName:
                prepareData.orderName,

            totalAmount:
                Number(prepareData.totalAmount),

            currency:
                "CURRENCY_KRW",

            payMethod:
                "CARD",

            customer:
                customer
        });
    }

    /*
     * =========================================================
     * 서버 결제 검증 및 주문 확정
     * =========================================================
     */
    function completePayment(paymentId) {

        return fetch(
            contextPath
                + "/order/payment/complete",
            {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({

                    paymentId:
                        paymentId
                })
            }
        )
        .then(readJsonResponse);
    }

    /*
     * =========================================================
     * 주문서 취소 및 이전 페이지 이동
     *
     * 결제 요청 전 단순히 주문서를 벗어나는 기능이다.
     * 이전 방문 페이지가 있으면 해당 페이지로 돌아가고,
     * 직접 주문서 주소로 접근한 경우 장바구니로 이동한다.
     * =========================================================
     */
    function cancelOrder() {

        clearError();

        /*
         * 결제 처리가 진행 중일 때는 중복 동작을 방지한다.
         */
        if (orderBtn.disabled) {

            showError(
                "현재 결제가 진행 중입니다."
            );

            return;
        }

        var currentUrl =
            window.location.href;

        var previousUrl =
            document.referrer;

        /*
         * 같은 사이트의 이전 페이지가 존재하고
         * 이전 페이지가 현재 주문서 페이지가 아닌 경우
         */
        if (previousUrl) {

            try {

                var previousLocation =
                    new URL(previousUrl);

                var currentLocation =
                    new URL(currentUrl);

                var isSameOrigin =
                    previousLocation.origin
                    === currentLocation.origin;

                var isOrderPage =
                    previousLocation.pathname
                    === contextPath + "/order";

                if (isSameOrigin && !isOrderPage) {

                    window.history.back();

                    return;
                }

            } catch (error) {

                console.warn(
                    "이전 페이지 주소 확인 실패:",
                    error
                );
            }
        }

        /*
         * 이전 페이지가 없거나 주문서 자체인 경우
         * 장바구니 페이지로 이동한다.
         *
         * 실제 장바구니 주소가 /cart라면
         * 아래 /cart/list를 /cart로 변경하면 된다.
         */
        window.location.href =
            contextPath + "/cart/list";
    }

    addressSearchBtn.addEventListener(
        "click",
        openAddressSearch
    );

    address2Input.addEventListener(
        "keydown",
        function (event) {

            if (event.key === "Enter") {

                event.preventDefault();
                orderBtn.click();
            }
        }
    );

    /*
     * =========================================================
     * 주문 취소 버튼 처리
     * =========================================================
     */
    cancelBtn.addEventListener(
        "click",
        cancelOrder
    );

    /*
     * =========================================================
     * 최종 결제 처리
     * =========================================================
     */
    orderBtn.addEventListener(
        "click",
        async function () {

            clearError();

            orderBtn.disabled = true;
            cancelBtn.disabled = true;

            orderBtn.textContent =
                "결제 준비 중...";

            try {

                var deliveryInformation =
                    createDeliveryInformation();

                var prepareData =
                    await preparePayment(
                        deliveryInformation
                    );

                if (!prepareData) {
                    return;
                }

                orderBtn.textContent =
                    "결제 진행 중...";

                var paymentResponse =
                    await requestPortOnePayment(
                        prepareData,
                        deliveryInformation
                    );

                /*
                 * 포트원 결제 실패 또는 사용자의 결제창 취소
                 */
                if (!paymentResponse) {

                    throw new Error(
                        "결제 응답을 받지 못했습니다."
                    );
                }

                if (paymentResponse.code != null) {

                    throw new Error(
                        paymentResponse.message
                        || "결제가 취소되었거나 실패했습니다."
                    );
                }

                var completedPaymentId =
                    paymentResponse.paymentId
                    || prepareData.paymentId;

                if (completedPaymentId
                        !== prepareData.paymentId) {

                    throw new Error(
                        "결제 ID가 일치하지 않습니다."
                    );
                }

                orderBtn.textContent =
                    "결제 확인 중...";

                var completeData =
                    await completePayment(
                        completedPaymentId
                    );

                if (!completeData.success) {

                    if (completeData.loginRequired) {

                        location.href =
                            contextPath
                            + "/member/login"
                            + "?redirect=/order";

                        return;
                    }

                    throw new Error(
                        completeData.message
                        || "결제 검증에 실패했습니다."
                    );
                }

                location.href =
                    contextPath
                    + completeData.redirectUrl;

            } catch (error) {

                console.error(
                    "결제 처리 오류:",
                    error
                );

                showError(
                    error.message
                    || "결제 처리 중 오류가 발생했습니다."
                );

                restoreOrderButton();
                cancelBtn.disabled = false;
            }
        }
    );

}());

</script>

</body>

</html>