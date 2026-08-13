/*
 * [성인인증 JavaScript 설정값 전달 수정]
 * 외부 정적 JavaScript 파일에서는 JSP 표현식을 사용할 수 없으므로,
 * adultVerify.jsp에서 전달한 window.verifyConfig 값을 사용합니다.
 */
const contextPath = window.verifyConfig?.contextPath || "";
const returnUrl = window.verifyConfig?.returnUrl || "/";
const csrfToken = window.verifyConfig?.csrfToken || "";
const csrfHeader = window.verifyConfig?.csrfHeader || "X-CSRF-TOKEN";

/**
 * Spring Security CSRF 보호가 활성화되어 있으므로
 * 성인인증 화면의 POST fetch 요청에 CSRF 헤더를 직접 추가합니다.
 *
 * 이 화면은 공통 header.jsp를 사용하지 않기 때문에
 * common.js에 의존하지 않고 verify.js 자체에서 처리합니다.
 */
function createJsonHeaders() {
  const headers = {
    "Content-Type": "application/json",
    Accept: "application/json",
  };

  if (csrfToken) {
    headers[csrfHeader] = csrfToken;
  }

  return headers;
}

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

  /*
   * [성인인증 창 미호출 오류 수정]
   * 현재 화면에는 easyVerifyBtn만 존재하는데 기존 코드는 존재하지 않는
   * smsVerifyBtn.disabled에 접근하여 fetch 호출 전에 JavaScript 오류가 발생했습니다.
   * 실제로 존재하는 버튼만 비활성화하도록 수정합니다.
   */
  if (!easyVerifyBtn) {
    showFail("본인인증 버튼을 찾을 수 없습니다. 페이지를 새로고침해주세요.");
    return;
  }

  easyVerifyBtn.disabled = true;
  clearMessage();

  try {
    /*
     * CSRF 토큰이 없는 상태에서 POST 요청을 보내면 Spring Security가 403으로 차단합니다.
     * 화면 렌더링 단계에서 토큰이 전달되지 않았다면 요청 전에 명확한 오류를 표시합니다.
     */
    if (!csrfToken) {
      throw new Error("보안 토큰을 불러오지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해주세요.");
    }

    /*
     * [포트원 SDK 로드 상태 확인 추가]
     * CDN 차단 또는 SDK 로딩 실패 시 requestIdentityVerification 호출에서
     * 원인을 알 수 없는 오류가 발생하지 않도록 사용자에게 정확한 메시지를 표시합니다.
     */
    if (!window.PortOne || typeof window.PortOne.requestIdentityVerification !== "function") {
      throw new Error("포트원 본인인증 모듈을 불러오지 못했습니다. 인터넷 연결 또는 브라우저 차단 설정을 확인해주세요.");
    }

    // 1) 서버에 성인인증 준비 요청 (가맹점 식별값 및 서버 발급 고유 ID 획득)
    const readyResponse = await fetch(contextPath + "/verify/adult/ready", {
      method: "POST",
      headers: createJsonHeaders(),
    });

    /*
     * [성인인증 준비 오류 메시지 보강]
     * 서버가 4xx/5xx를 반환하면 가능한 경우 서버 응답 메시지를 읽어
     * 로그인 세션 만료, 설정값 누락 등을 확인할 수 있게 합니다.
     */
    if (!readyResponse.ok) {
      const readyErrorText = await readyResponse.text();
      let readyErrorMessage = "본인인증 준비 작업 중 서버 통신 오류가 발생했습니다.";

      try {
        const readyErrorData = JSON.parse(readyErrorText);
        readyErrorMessage = readyErrorData.message || readyErrorData.error || readyErrorMessage;
      } catch (e) {
        if (readyErrorText && readyErrorText.trim()) {
          readyErrorMessage = readyErrorText.trim();
        }
      }

      throw new Error(readyErrorMessage);
    }

    const readyData = await readyResponse.json();

    /*
     * [존재하지 않는 화면 요소 접근 오류 수정]
     * 기존 코드는 JSP에 없는 identityVerificationId 요소에 값을 넣으면서
     * 준비 요청 성공 직후 오류가 발생했습니다.
     * 인증 ID는 readyData.verifyId를 그대로 포트원과 완료 API에 사용합니다.
     */
    if (!readyData.storeId || !readyData.verifyId) {
      throw new Error("본인인증 준비 정보가 올바르게 내려오지 않았습니다.");
    }

    const selectedChannelKey = methodType === "EASY" ? readyData.easyChannelKey : readyData.smsChannelKey;

    if (!selectedChannelKey) {
      throw new Error("설정된 인증 채널키가 존재하지 않습니다.");
    }

    // 2) PortOne V2 Browser SDK 본인인증 모달창 호출
    const response = await window.PortOne.requestIdentityVerification({
      storeId: readyData.storeId,
      channelKey: selectedChannelKey,
      identityVerificationId: readyData.verifyId,
    });

    // 인증 취소 또는 인증 도중 에러가 전달된 경우 예외 처리
    if (response && response.code !== undefined) {
      showFail(response.message || "본인인증이 취소되었거나 실패했습니다.");
      easyVerifyBtn.disabled = false;
      return;
    }

    showSuccess("인증 모달 완료. 최종 성인인증 승인 검증을 진행 중입니다...");

    // 3) 서버 최종 이력 비교 검증 및 세션 바인딩 완료 처리 요청
    const completeResponse = await fetch(
      contextPath + "/verify/adult/complete?returnUrl=" + encodeURIComponent(returnUrl),
      {
        method: "POST",
        headers: createJsonHeaders(),
        body: JSON.stringify({ verifyId: readyData.verifyId }),
      }
    );

    const completeText = await completeResponse.text();
    let completeData;

    try {
      completeData = JSON.parse(completeText);
    } catch (e) {
      if (!completeResponse.ok && completeText && completeText.trim()) {
        throw new Error(completeText.trim());
      }

      throw new Error("서버 검증 완료 처리 중 부적절한 데이터 형식이 수신되었습니다.");
    }

    /*
     * [완료 API HTTP 상태 검증 추가]
     * JSON 응답이더라도 서버 오류 상태이면 성공 처리하지 않습니다.
     */
    if (!completeResponse.ok) {
      throw new Error(completeData.message || "성인인증 완료 처리 중 서버 오류가 발생했습니다.");
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
    }
  } catch (error) {
    console.error("[성인인증 처리 오류]", error);
    showFail(error.message || "성인인증 처리 과정 중 예외 오류가 발생했습니다.");
    easyVerifyBtn.disabled = false;
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
