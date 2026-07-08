<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>포트원 결제 테스트</title>

    <script src="https://cdn.portone.io/v2/browser-sdk.js"></script>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f5f6f8;
            margin: 0;
            padding: 40px;
        }

        .payment-box {
            width: 420px;
            margin: 0 auto;
            background: #fff;
            border-radius: 16px;
            padding: 30px;
            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
        }

        h2 {
            margin-top: 0;
            margin-bottom: 20px;
        }

        .row {
            display: flex;
            justify-content: space-between;
            margin: 12px 0;
            font-size: 15px;
        }

        .price {
            font-size: 22px;
            font-weight: bold;
            color: #222;
        }

        .notice {
            margin-top: 20px;
            font-size: 13px;
            color: #777;
            line-height: 1.5;
        }

        button {
            width: 100%;
            margin-top: 24px;
            padding: 14px;
            border: none;
            border-radius: 10px;
            background: #111;
            color: #fff;
            font-size: 16px;
            cursor: pointer;
        }

        button:hover {
            background: #333;
        }
    </style>
</head>
<body>

<div class="payment-box">
    <h2>포트원 결제 테스트</h2>

    <div class="row">
        <span>상품명</span>
        <strong>${orderName}</strong>
    </div>

    <div class="row">
        <span>결제금액</span>
        <span class="price">${totalAmount}원</span>
    </div>

    <button type="button" onclick="requestPayment()">결제하기</button>

    <div class="notice">
        이 화면은 ODITJI 포트원 결제 테스트 화면입니다.<br>
        결제 성공 후 서버에서 포트원 결제 단건 조회 API로 검증합니다.
    </div>
</div>

<script>
    const CONTEXT_PATH = "${pageContext.request.contextPath}";

    const STORE_ID = "${storeId}";
    const CHANNEL_KEY = "${channelKey}";
    const ORDER_NAME = "${orderName}";
    const TOTAL_AMOUNT = Number("${totalAmount}");

    async function requestPayment() {
        try {
            const paymentId = "PAY_" + new Date().getTime();

            console.log("결제 요청 paymentId:", paymentId);

            const response = await PortOne.requestPayment({
                storeId: STORE_ID,
                channelKey: CHANNEL_KEY,
                paymentId: paymentId,
                orderName: ORDER_NAME,
                totalAmount: TOTAL_AMOUNT,
                currency: "CURRENCY_KRW",
                payMethod: "CARD",
                customer: {
                    fullName: "홍길동",
                    phoneNumber: "01012345678",
                    email: "test@example.com"
                }
            });

            console.log("포트원 결제 응답:", response);

            if (response.code != null) {
                alert("결제 실패\n" + response.message);
                location.href = CONTEXT_PATH + "/payment/fail";
                return;
            }

            const verifyResponse = await fetch(CONTEXT_PATH + "/payment/complete", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    paymentId: paymentId,
                    orderName: ORDER_NAME,
                    totalAmount: TOTAL_AMOUNT
                })
            });

            const result = await verifyResponse.json();

            console.log("서버 검증 결과:", result);

            if (result.success) {
                alert("결제 검증 성공");
                location.href = CONTEXT_PATH + "/payment/success";
            } else {
                alert("결제 검증 실패\n" + result.message);
                location.href = CONTEXT_PATH + "/payment/fail";
            }

        } catch (error) {
            console.error(error);
            alert("결제 처리 중 오류가 발생했습니다.");
            location.href = CONTEXT_PATH + "/payment/fail";
        }
    }
</script>

</body>
</html>