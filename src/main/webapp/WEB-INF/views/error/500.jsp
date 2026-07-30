<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>500 - ODITJI</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
</head>

<body class="error-page">

<div class="error-wrap">

    <div class="error-card">

        <div class="error-code">500</div>

        <div class="error-title">
            서버에 문제가 발생했습니다
        </div>

        <div class="error-sub">
            잠시 후 다시 시도해주세요.
        </div>

        <div class="error-btns">

            <a href="${pageContext.request.contextPath}/"
               class="btn primary">
                홈으로 이동
            </a>

            <a href="javascript:location.reload()"
               class="btn ghost">
                새로고침
            </a>

        </div>

    </div>

</div>

</body>
</html>