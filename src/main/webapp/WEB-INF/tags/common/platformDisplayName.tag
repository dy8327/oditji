<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="platformName" required="false" type="java.lang.String" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- DB/TMDB 플랫폼 영문명을 화면용 한글명으로 통일합니다. --%>
<c:choose>
    <c:when test="${platformName eq 'Netflix'}">넷플릭스</c:when>
    <c:when test="${platformName eq 'Disney Plus' or platformName eq 'Disney+'}">디즈니+</c:when>
    <c:when test="${platformName eq 'Tving' or platformName eq 'TVING'}">티빙</c:when>
    <c:when test="${platformName eq 'Wavve' or platformName eq 'wavve'}">웨이브</c:when>
    <c:when test="${platformName eq 'Watcha'}">왓챠</c:when>
    <c:when test="${platformName eq 'Coupangplay' or platformName eq 'Coupang Play'}">쿠팡플레이</c:when>
    <c:otherwise>${platformName}</c:otherwise>
</c:choose>
