<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.project.oditji.member.vo.PlatformVO" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>OTT 플랫폼 선택</title>
</head>
<body>

<%@ include file="/WEB-INF/views/common/header.jsp"%>

<h2>사용 중인 OTT 플랫폼을 선택해주세요</h2>

<p>
    선택한 플랫폼을 기준으로 맞춤 콘텐츠를 보여드릴게요.
</p>

<%
    // 에러 메시지 처리
    String errorMsg = (String) request.getAttribute("errorMsg");
    if (errorMsg != null && !errorMsg.isEmpty()) {
%>
    <p style="color:red;"><%= errorMsg %></p>
<%
    }
%>

<form action="<%= request.getContextPath() %>/member/platform/select" method="post">

<%
    // 플랫폼 리스트 처리
    List<PlatformVO> platformList = (List<PlatformVO>) request.getAttribute("platformList");
    if (platformList != null) {
        for (PlatformVO platform : platformList) {
%>
        <label style="display:block; margin-bottom:10px;">
            <input type="checkbox"
                   name="platformNoList"
                   value="<%= platform.getPlatformNo() %>">
            <%= platform.getPlatformName() %>
        </label>
<%
        }
    }
%>

    <button type="submit">선택 완료</button>
</form>
<%@ include file="/WEB-INF/views/common/footer.jsp"%>
</body>
</html>