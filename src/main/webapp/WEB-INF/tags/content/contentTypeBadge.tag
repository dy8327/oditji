<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="content"
              required="true"
              type="java.lang.Object" %>
<%@ attribute name="outerClass"
              required="true"
              type="java.lang.String" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    콘텐츠 종류(영화/드라마/애니메이션/예능/다큐멘터리) 라벨과 색상 클래스를 판별해서
    배지 하나로 렌더링한다. contentCard.tag(grid/sidebar variant)와 recommendCard.tag가
    각자 같은 기준의 c:choose를 따로 갖고 있던 것을 rankingCard.tag까지 포함해
    한 곳으로 모으기 위한 공통 태그이다.

    outerClass: 호출하는 쪽 CSS(예: ranking-type)를 그대로 붙여서
    기존 클래스 체계(각 페이지 CSS의 .xxx.is-movie 등)를 그대로 재사용한다.
--%>
<c:set var="typeLabel" value="드라마" />
<c:set var="typeClass" value="drama" />

<c:choose>
    <c:when test="${fn:contains(content.genreText, '애니메이션')}">
        <c:set var="typeLabel" value="애니메이션" />
        <c:set var="typeClass" value="animation" />
    </c:when>
    <c:when test="${fn:contains(content.genreText, '다큐멘터리')}">
        <c:set var="typeLabel" value="다큐멘터리" />
        <c:set var="typeClass" value="documentary" />
    </c:when>
    <c:when test="${content.contentType eq 'TV'
                  and (fn:contains(content.genreText, '리얼리티')
                       or fn:contains(content.genreText, '토크'))}">
        <c:set var="typeLabel" value="예능" />
        <c:set var="typeClass" value="variety" />
    </c:when>
    <c:when test="${content.contentType eq 'MOVIE'}">
        <c:set var="typeLabel" value="영화" />
        <c:set var="typeClass" value="movie" />
    </c:when>
</c:choose>

<span class="${outerClass} is-${typeClass}">
    <c:out value="${typeLabel}" />
</span>
