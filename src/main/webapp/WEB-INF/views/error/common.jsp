<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title><c:out value="${errorCode}"/> - ODITJI</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
</head>
<body class="error-page">
<div class="error-wrap">
    <div class="error-card">
        <div class="error-code"><c:out value="${errorCode}"/></div>
        <div class="error-title">요청을 처리할 수 없습니다</div>
        <div class="error-sub"><c:out value="${errorMessage}"/></div>
        <div class="error-btns">
            <a href="${pageContext.request.contextPath}/" class="btn primary">홈으로 이동</a>
            <a href="javascript:history.back()" class="btn ghost">이전 페이지</a>
        </div>
    </div>
</div>
</body>
</html>