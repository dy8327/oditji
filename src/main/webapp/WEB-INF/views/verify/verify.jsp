<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%
    String contextPath = request.getContextPath();
    String storeId = (String) request.getAttribute("storeId");
    String identity1ChannelKey = (String) request.getAttribute("identity1ChannelKey");
    String identity2ChannelKey = (String) request.getAttribute("identity2ChannelKey");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ODITJI 본인인증 실습</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">

    <!-- PortOne V2 Browser SDK -->
    <script src="https://cdn.portone.io/v2/browser-sdk.js"></script>
</head>

<body class="bg-light">

<div class="container py-5">

    <div class="row justify-content-center">
        <div class="col-lg-7 col-md-9">

            <div class="card shadow-sm border-0">
                <div class="card-body p-4">

                    <div class="mb-4">
                        <h2 class="fw-bold mb-2">ODITJI 본인인증 실습</h2>
                        <p class="text-muted mb-0">
                            간편 본인인증과 SMS 본인인증 채널을 각각 테스트하는 화면입니다.
                        </p>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">인증 요청 ID</label>
                        <input type="text" id="identityVerificationId" class="form-control" readonly>
                        <div class="form-text">
                            본인인증 1건을 구분하는 고유 ID입니다.
                        </div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label fw-semibold">선택한 인증 방식</label>
                        <input type="text" id="selectedVerifyType" class="form-control" value="-" readonly>
                    </div>

                    <div id="statusBox" class="alert alert-secondary">
                        인증 전입니다.
                    </div>

                    <div class="d-grid gap-2">
                        <button type="button" class="btn btn-dark py-2"
                                onclick="requestIdentityVerification('SIMPLE')">
                            간편 본인인증 테스트
                        </button>

                        <button type="button" class="btn btn-outline-dark py-2"
                                onclick="requestIdentityVerification('SMS')">
                            SMS 본인인증 테스트
                        </button>
                    </div>

                </div>
            </div>

            <div class="card shadow-sm border-0 mt-4">
                <div class="card-body p-4">

                    <h5 class="fw-bold mb-3">인증 결과</h5>

                    <table class="table table-bordered align-middle mb-0">
                        <tbody>
                        <tr>
                            <th class="table-light" style="width: 160px;">인증 성공 여부</th>
                            <td id="resultSuccess">-</td>
                        </tr>
                        <tr>
                            <th class="table-light">이름</th>
                            <td id="resultName">-</td>
                        </tr>
                        <tr>
                            <th class="table-light">생년월일</th>
                            <td id="resultBirthDate">-</td>
                        </tr>
                        <tr>
                            <th class="table-light">휴대폰번호</th>
                            <td id="resultPhoneNumber">-</td>
                        </tr>
                        <tr>
                            <th class="table-light">성별</th>
                            <td id="resultGender">-</td>
                        </tr>
                        <tr>
                            <th class="table-light">성인 여부</th>
                            <td id="resultAdult">-</td>
                        </tr>
                        <tr>
                            <th class="table-light">메시지</th>
                            <td id="resultMessage">-</td>
                        </tr>
                        </tbody>
                    </table>

                </div>
            </div>

            <div class="alert alert-info mt-4">
                API Secret은 서버에서만 사용되며, 이 화면에는 출력되지 않습니다.
            </div>

        </div>
    </div>

</div>

<script>
    const CONTEXT_PATH = "<%= contextPath %>";

    /*
        storeId, channelKey는 포트원 브라우저 SDK 호출에 필요합니다.
        화면에는 직접 출력하지 않지만, 브라우저 JavaScript 내부에서는 사용됩니다.
        API Secret은 절대 JSP/JavaScript에 작성하면 안 됩니다.
    */
    const STORE_ID = "<%= storeId %>";

    // 간편 본인인증 채널키
    const IDENTITY1_CHANNEL_KEY = "<%= identity1ChannelKey %>";

    // SMS 본인인증 채널키
    const IDENTITY2_CHANNEL_KEY = "<%= identity2ChannelKey %>";

    function makeIdentityVerificationId() {
    const now = new Date().getTime();
    const random = Math.floor(Math.random() * 1000000);

    return "OIDTJI" + now + random;
    }

    function resetResult() {
        document.getElementById("resultSuccess").innerText = "-";
        document.getElementById("resultName").innerText = "-";
        document.getElementById("resultBirthDate").innerText = "-";
        document.getElementById("resultPhoneNumber").innerText = "-";
        document.getElementById("resultGender").innerText = "-";
        document.getElementById("resultAdult").innerText = "-";
        document.getElementById("resultMessage").innerText = "-";
    }

    function setStatus(type, message) {
        const statusBox = document.getElementById("statusBox");

        if (type === "success") {
            statusBox.className = "alert alert-success";
        } else if (type === "danger") {
            statusBox.className = "alert alert-danger";
        } else if (type === "info") {
            statusBox.className = "alert alert-info";
        } else {
            statusBox.className = "alert alert-secondary";
        }

        statusBox.innerText = message;
    }

    function setResult(result) {
        document.getElementById("resultSuccess").innerText = result.success ? "성공" : "실패";
        document.getElementById("resultName").innerText = result.name || "-";
        document.getElementById("resultBirthDate").innerText = result.birthDate || "-";
        document.getElementById("resultPhoneNumber").innerText = result.phoneNumber || "-";
        document.getElementById("resultGender").innerText = result.gender || "-";

        if (result.adultYn === "Y") {
            document.getElementById("resultAdult").innerText = "성인";
        } else if (result.adultYn === "N") {
            document.getElementById("resultAdult").innerText = "미성년자 또는 확인 불가";
        } else {
            document.getElementById("resultAdult").innerText = "-";
        }

        document.getElementById("resultMessage").innerText = result.message || "-";
    }

    function getChannelKeyByType(verifyType) {
        if (verifyType === "SIMPLE") {
            return IDENTITY1_CHANNEL_KEY;
        }

        if (verifyType === "SMS") {
            return IDENTITY2_CHANNEL_KEY;
        }

        return null;
    }

    function getVerifyTypeName(verifyType) {
        if (verifyType === "SIMPLE") {
            return "간편 본인인증";
        }

        if (verifyType === "SMS") {
            return "SMS 본인인증";
        }

        return "알 수 없음";
    }

    async function requestIdentityVerification(verifyType) {
        resetResult();

        const channelKey = getChannelKeyByType(verifyType);
        const verifyTypeName = getVerifyTypeName(verifyType);

        if (!channelKey) {
            setStatus("danger", "인증 채널키를 찾을 수 없습니다.");
            return;
        }

        document.getElementById("selectedVerifyType").value = verifyTypeName;

        const identityVerificationId = makeIdentityVerificationId();
        document.getElementById("identityVerificationId").value = identityVerificationId;

        try {
            setStatus("info", verifyTypeName + " 창을 호출하는 중입니다.");

            const response = await PortOne.requestIdentityVerification({
                storeId: STORE_ID,
                channelKey: channelKey,
                identityVerificationId: identityVerificationId
            });

            if (response.code !== undefined) {
                setStatus("danger", response.message || "본인인증 중 오류가 발생했습니다.");
                setResult({
                    success: false,
                    message: response.message || "본인인증 실패",
                    adultYn: "N"
                });
                return;
            }

            setStatus("info", "브라우저 본인인증 완료. 서버 검증 중입니다.");

            const serverResponse = await fetch(CONTEXT_PATH + "/verify/complete", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    identityVerificationId: identityVerificationId
                })
            });

            const result = await serverResponse.json();

            setResult(result);

            if (result.success) {
                setStatus("success", "서버 검증까지 완료되었습니다.");
            } else {
                setStatus("danger", result.message || "서버 검증 실패");
            }

        } catch (error) {
            console.error(error);

            setStatus("danger", "본인인증 처리 중 예외가 발생했습니다.");
            setResult({
                success: false,
                message: error.message,
                adultYn: "N"
            });
        }
    }

    document.getElementById("identityVerificationId").value = makeIdentityVerificationId();
</script>

</body>
</html>