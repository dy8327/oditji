<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>403 - ODITJI</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/error.css">
<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>

<body class="error-page">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div id="mainContent" class="error-wrap">

    <div class="error-card">

        <div class="error-code">403</div>

        <div class="error-title">
            접근 권한이 없습니다.
        </div>

        <div class="error-sub">
            <c:choose>
                <c:when test="${not empty errorMessage}"><c:out value="${errorMessage}"/></c:when>
                <c:otherwise>해당 페이지에 접근할 수 있는 권한이 없습니다.</c:otherwise>
            </c:choose>
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