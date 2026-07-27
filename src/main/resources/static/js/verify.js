/*
 * [성인인증 JavaScript 설정값 전달 수정]
 * 외부 정적 JavaScript 파일에서는 JSP 표현식을 사용할 수 없으므로,
 * adultVerify.jsp에서 전달한 window.verifyConfig 값을 사용합니다.
 */
const contextPath = window.verifyConfig?.contextPath || "";
const returnUrl = window.verifyConfig?.returnUrl || "/";

function openMethodPanel() {
  document.getElementById("startArea").style.display = "none";
  document.getElementById("methodPanel").classList.add("show");
  clearMessage();
}

function closeMethodPanel() {
  document.getElementById("methodPanel").classList.remove("show");
  document.getElementById("startArea").style.display = "flex";
  clearMessage();
}

/**
 * 포트원 SDK 호출 및 서버 통합 검증 비동기 프로세스
 */
async function startAdultVerify(methodType) {
  const easyVerifyBtn = document.getElementById("easyVerifyBtn");
  const smsVerifyBtn = document.getElementById("smsVerifyBtn");

  easyVerifyBtn.disabled = true;
  smsVerifyBtn.disabled = true;
  clearMessage();

  try {
    // 1) 서버에 성인인증 준비 요청 (가맹점 식별값 및 서버 발급 고유 ID 획득)
    const readyResponse = await fetch(contextPath + "/verify/adult/ready", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
    });

    if (!readyResponse.ok) {
      throw new Error("본인인증 준비 작업 중 서버 통신 오류가 발생했습니다.");
    }

    const readyData = await readyResponse.json();

    // 실습 편의용 화면 Input 노출 업데이트
    document.getElementById("identityVerificationId").value = readyData.verifyId;

    let selectedChannelKey = methodType === "EASY" ? readyData.easyChannelKey : readyData.smsChannelKey;

    if (!selectedChannelKey) {
      throw new Error("설정된 인증 채널키가 존재하지 않습니다.");
    }

    // 2) PortOne V2 Browser SDK 본인인증 모달창 호출
    const response = await PortOne.requestIdentityVerification({
      storeId: readyData.storeId,
      channelKey: selectedChannelKey,
      identityVerificationId: readyData.verifyId,
    });

    // 인증 취소 또는 인증 도중 에러가 전달된 경우 예외 처리
    if (response.code !== undefined) {
      showFail(response.message || "본인인증이 취소되었거나 실패했습니다.");
      easyVerifyBtn.disabled = false;
      smsVerifyBtn.disabled = false;
      return;
    }

    showSuccess("인증 모달 완료. 최종 성인인증 승인 검증을 진행 중입니다...");

    // 3) 서버 최종 이력 비교 검증 및 세션 바인딩 완료 처리 요청
    const completeResponse = await fetch(contextPath + "/verify/adult/complete?returnUrl=" + encodeURIComponent(returnUrl), {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ verifyId: readyData.verifyId }),
    });

    const completeText = await completeResponse.text();
    let completeData;

    try {
      completeData = JSON.parse(completeText);
    } catch (e) {
      throw new Error("서버 검증 완료 처리 중 부적절한 데이터 형식이 수신되었습니다.");
    }

    // 4) 최종 비즈니스 분기 검증 결과 반영 리다이렉트
    if (completeData.success && completeData.adult) {
      showSuccess(completeData.message || "성인인증이 완료되었습니다.");
      setTimeout(function () {
        location.href = contextPath + completeData.redirectUrl;
      }, 1000);
    } else {
      showFail(completeData.message || "만 19세 미만 아동/청소년은 이용할 수 없습니다.");
      easyVerifyBtn.disabled = false;
      smsVerifyBtn.disabled = false;
    }
  } catch (error) {
    console.error(error);
    showFail(error.message || "성인인증 처리 과정 중 예외 오류가 발생했습니다.");
    easyVerifyBtn.disabled = false;
    smsVerifyBtn.disabled = false;
  }
}

function clearMessage() {
  const resultMessage = document.getElementById("resultMessage");
  resultMessage.className = "result-message";
  resultMessage.innerText = "";
}

function showSuccess(message) {
  const resultMessage = document.getElementById("resultMessage");
  resultMessage.className = "result-message success";
  resultMessage.innerText = message;
}

function showFail(message) {
  const resultMessage = document.getElementById("resultMessage");
  resultMessage.className = "result-message fail";
  resultMessage.innerText = message;
}
