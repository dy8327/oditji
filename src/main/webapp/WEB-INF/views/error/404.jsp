<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>404 - ODITJI</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
</head>

<body class="error-page">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="error-wrap">

    <div class="error-card">

        <div class="error-code">404</div>

        <div class="error-title">
            페이지를 찾을 수 없습니다
        </div>

        <div class="error-sub">
            요청하신 페이지가 존재하지 않거나 이동되었습니다.
        </div>

        <div class="error-btns">

            <a href="${pageContext.request.contextPath}/"
               class="btn primary">
                홈으로 이동
            </a>

            <a href="javascript:history.back()"
               class="btn ghost">
                이전 페이지
            </a>

        </div>

    </div>

</div>

</body>
</html>