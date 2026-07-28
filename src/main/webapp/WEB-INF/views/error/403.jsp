<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>404 - ODITJI</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
</head>

<body class="error-page">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="error-wrap">

    <div class="error-card">

        <div class="error-code">404</div>

        <div class="error-title">
            접근 권한이 없습니다.
        </div>

        <div class="error-sub">
            해당 페이지에 접근할 수 있는 권한이 없습니다.
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