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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ODITJI | 성인인증</title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdn.portone.io/v2/browser-sdk.js"></script>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/verify.css">
</head>
<body>

<%-- [성인인증 안내 화면 디자인 수정]
     기존 인증 정보와 JavaScript 로직은 그대로 유지하고,
     ODITJI 메인 페이지의 다크 테마에 맞게 화면 배치만 변경한다. --%>
<div class="verify-page">
    <main class="verify-container">
        <header class="verify-header">
            <a href="<%= contextPath %>/" class="verify-brand" aria-label="ODITJI 메인으로 이동">
                ODITJI
            </a>
            <span class="verify-header-divider"></span>
            <h1 class="verify-page-title">성인 인증</h1>
        </header>

        <section class="verify-card" aria-labelledby="verifyTitle">
            <div class="verify-card-inner">
                <%-- [성인인증 안내 화면 디자인 수정]
                     참고 이미지의 연령 표시를 ODITJI 스타일로 재구성한다. --%>
                <div class="age-symbol-wrap" aria-hidden="true">
                    <div class="age-symbol">
                        <strong>19</strong>
                        <span>+</span>
                    </div>
                    <p>ADULT ONLY</p>
                </div>

                <div class="verify-content">
                    <span class="badge-19">19+ 콘텐츠 접근 인증</span>

                    <h2 id="verifyTitle">본인/실명 인증이 필요합니다</h2>

                    <p class="desc">
                        ODITJI의 청소년 관람불가 작품 상세페이지는 국내 본인확인 절차를 통해
                        만 19세 이상 여부를 확인한 회원만 이용할 수 있습니다.
                    </p>

                    <div class="verify-law-notice">
                        <span class="notice-mark">!</span>
                        <p>
                            본 정보에는 청소년에게 유해한 정보가 포함되어 있습니다.
                            만 19세 미만의 아동·청소년은 이용할 수 없습니다.
                        </p>
                    </div>

                    <div class="mb-3 identity-area">
                        <%--
                            읽기 전용 입력창도 label의 for와 input의 id를 연결하여
                            인증 요청 고유 ID라는 의미를 보조 기술에 전달한다.
                        --%>
                        <label for="identityVerificationId"
                               class="form-label fw-semibold identity-label">
                            인증 요청 고유 ID
                        </label>

                        <input type="text"
                               id="identityVerificationId"
                               class="form-control form-control-sm text-center identity-input"
                               value="-"
                               readonly>
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

                    <%-- [성인인증 안내 화면 디자인 수정]
                         나가기 버튼은 이전 페이지로 이동하고,
                         계속하기 버튼은 기존 인증 방식 선택 영역을 연다. --%>
                    <div id="startArea" class="btn-area">
                        <button type="button"
                                class="verify-action verify-action-exit"
                                onclick="history.back()">
                            19세 미만 나가기
                        </button>
                        <button type="button"
                                id="openMethodBtn"
                                class="verify-action verify-action-continue"
                                onclick="openMethodPanel()">
                            계속하기
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
        </section>
    </main>
</div>

<%-- [성인인증 JavaScript 값 전달 수정]
     외부 정적 JavaScript 파일에서는 JSP 표현식을 해석할 수 없으므로,
     JSP 화면에서 contextPath와 returnUrl을 전역 변수로 전달합니다. --%>
<script>
    window.verifyConfig = {
        contextPath: "<%= contextPath %>",
        returnUrl: "<%= returnUrl %>"
    };
</script>

<script defer src="${pageContext.request.contextPath}/js/verify.js"></script>

</body>
</html>