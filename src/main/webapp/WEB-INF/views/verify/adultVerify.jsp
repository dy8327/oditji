<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String contextPath = request.getContextPath();
    String returnUrl = (String) request.getAttribute("returnUrl");
    if (returnUrl == null || returnUrl.trim().isEmpty()) {
        returnUrl = "/";
    }
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>ODITJI | 성인인증</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.portone.io/v2/browser-sdk.js"></script>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/verify.css">
</head>
<body>

<div class="verify-wrap">
    <div class="verify-card">
        <span class="badge-19">19+ 콘텐츠 접근 인증</span>

        <h1>본인/실명 인증이 필요합니다</h1>

        <p class="desc">
            ODITJI의 청소년 관람불가 작품 상세페이지는 국내 본인확인 절차를 통해
            만 19세 이상 여부를 확인한 회원만 이용할 수 있습니다.
        </p>

        <div class="mb-3">
            <label class="form-label fw-semibold text-secondary" style="font-size: 13px;">인증 요청 고유 ID</label>
            <input type="text" id="identityVerificationId" class="form-control form-control-sm text-center bg-light" value="-" readonly>
        </div>

        <div class="standard-box">
            <div class="standard-title">국내 표준: 본인/실명 인증형</div>
            <ul class="standard-list">
                <li>휴대폰 명의자 기준으로 본인 여부를 확인합니다.</li>
                <li>이름, 생년월일, 성별, 휴대폰번호를 인증기관을 통해 확인합니다.</li>
                <li>인증 완료 후 ODITJI 서버에서 만 19세 이상 여부를 최종 판단합니다.</li>
            </ul>
        </div>

        <div class="notice">
            인증 결과는 성인 여부 확인 목적으로만 사용되며,
            인증 성공 시 회원의 성인인증 상태가 저장됩니다.
        </div>

        <div id="startArea" class="btn-area">
            <button type="button" class="btn btn-secondary btn-custom" onclick="history.back()">이전</button>
            <button type="button" id="openMethodBtn" class="btn btn-dark btn-custom" onclick="openMethodPanel()">
                본인인증 시작
            </button>
        </div>

        <div id="methodPanel" class="method-panel">
            <p class="method-title">인증 방식을 선택해주세요</p>
            <p class="method-desc">
                간편인증 또는 휴대폰 문자 인증 중 원하는 방식을 선택할 수 있습니다.
            </p>

            <div class="method-list">
                <button type="button" id="easyVerifyBtn" class="method-btn method-btn-main" onclick="startAdultVerify('EASY')">
                    ⚡ 간편인증으로 시작
                    <span class="method-sub">카카오, PASS, 토스, 네이버 등</span>
                </button>

                <button type="button" id="smsVerifyBtn" class="method-btn" onclick="startAdultVerify('SMS')">
                    휴대폰 문자(SMS) 인증
                    <span class="method-sub">앱 사용이 어려운 사용자용</span>
                </button>
            </div>

            <button type="button" class="method-back" onclick="closeMethodPanel()">
                이전 선택으로 돌아가기
            </button>
        </div>

        <div id="resultMessage" class="result-message"></div>
    </div>
</div>

<script>
    const contextPath = '<%= contextPath %>';
    const returnUrl = '<%= returnUrl %>';

    function openMethodPanel() {
        document.getElementById('startArea').style.display = 'none';
        document.getElementById('methodPanel').classList.add('show');
        clearMessage();
    }

    function closeMethodPanel() {
        document.getElementById('methodPanel').classList.remove('show');
        document.getElementById('startArea').style.display = 'flex';
        clearMessage();
    }

    /**
     * 포트원 SDK 호출 및 서버 통합 검증 비동기 프로세스
     */
    async function startAdultVerify(methodType) {
        const easyVerifyBtn = document.getElementById('easyVerifyBtn');
        const smsVerifyBtn = document.getElementById('smsVerifyBtn');

        easyVerifyBtn.disabled = true;
        smsVerifyBtn.disabled = true;
        clearMessage();

        try {
            // 1) 서버에 성인인증 준비 요청 (가맹점 식별값 및 서버 발급 고유 ID 획득)
            const readyResponse = await fetch(contextPath + '/verify/adult/ready', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });

            if (!readyResponse.ok) {
                throw new Error('본인인증 준비 작업 중 서버 통신 오류가 발생했습니다.');
            }

            const readyData = await readyResponse.json();
            
            // 실습 편의용 화면 Input 노출 업데이트
            document.getElementById('identityVerificationId').value = readyData.verifyId;

            let selectedChannelKey = (methodType === 'EASY') ? readyData.easyChannelKey : readyData.smsChannelKey;

            if (!selectedChannelKey) {
                throw new Error('설정된 인증 채널키가 존재하지 않습니다.');
            }

            // 2) PortOne V2 Browser SDK 본인인증 모달창 호출
            const response = await PortOne.requestIdentityVerification({
                storeId: readyData.storeId,
                channelKey: selectedChannelKey,
                identityVerificationId: readyData.verifyId
            });

            // 인증 취소 또는 인증 도중 에러가 전달된 경우 예외 처리
            if (response.code !== undefined) {
                showFail(response.message || '본인인증이 취소되었거나 실패했습니다.');
                easyVerifyBtn.disabled = false;
                smsVerifyBtn.disabled = false;
                return;
            }

            showSuccess('인증 모달 완료. 최종 성인인증 승인 검증을 진행 중입니다...');

            // 3) 서버 최종 이력 비교 검증 및 세션 바인딩 완료 처리 요청
            const completeResponse = await fetch(
                contextPath + '/verify/adult/complete?returnUrl=' + encodeURIComponent(returnUrl),
                {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ verifyId: readyData.verifyId })
                }
            );

            const completeText = await completeResponse.text();
            let completeData;

            try {
                completeData = JSON.parse(completeText);
            } catch (e) {
                throw new Error('서버 검증 완료 처리 중 부적절한 데이터 형식이 수신되었습니다.');
            }

            // 4) 최종 비즈니스 분기 검증 결과 반영 리다이렉트
            if (completeData.success && completeData.adult) {
                showSuccess(completeData.message || '성인인증이 완료되었습니다.');
                setTimeout(function () {
                    location.href = contextPath + completeData.redirectUrl;
                }, 1000);
            } else {
                showFail(completeData.message || '만 19세 미만 아동/청소년은 이용할 수 없습니다.');
                easyVerifyBtn.disabled = false;
                smsVerifyBtn.disabled = false;
            }

        } catch (error) {
            console.error(error);
            showFail(error.message || '성인인증 처리 과정 중 예외 오류가 발생했습니다.');
            easyVerifyBtn.disabled = false;
            smsVerifyBtn.disabled = false;
        }
    }

    function clearMessage() {
        const resultMessage = document.getElementById('resultMessage');
        resultMessage.className = 'result-message';
        resultMessage.innerText = '';
    }

    function showSuccess(message) {
        const resultMessage = document.getElementById('resultMessage');
        resultMessage.className = 'result-message success';
        resultMessage.innerText = message;
    }

    function showFail(message) {
        const resultMessage = document.getElementById('resultMessage');
        resultMessage.className = 'result-message fail';
        resultMessage.innerText = message;
    }
</script>

</body>
</html>