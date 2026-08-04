<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="ageRating"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="outerClass"
              required="true"
              type="java.lang.String" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%--
    콘텐츠 연령등급 문자열을 화면 공통 배지 값으로 변환한다.
    연령등급 변환 로직을 한 곳에 모아 JSP 간 중복을 제거한다.
--%>
<c:set var="badgeLabel" value="?" />
<c:set var="badgeClass" value="unknown" />
<c:set var="badgeTitle" value="등급 정보 없음" />

<c:choose>
    <c:when test="${ageRating eq '전체 관람가'}">
        <c:set var="badgeLabel" value="ALL" />
        <c:set var="badgeClass" value="all" />
        <c:set var="badgeTitle" value="전체 관람가" />
    </c:when>

    <c:when test="${ageRating eq '7세 이상 관람가'}">
        <c:set var="badgeLabel" value="7" />
        <c:set var="badgeClass" value="age7" />
        <c:set var="badgeTitle" value="7세 이상 관람가" />
    </c:when>

    <c:when test="${ageRating eq '12세 이상 관람가'}">
        <c:set var="badgeLabel" value="12" />
        <c:set var="badgeClass" value="age12" />
        <c:set var="badgeTitle" value="12세 이상 관람가" />
    </c:when>

    <c:when test="${ageRating eq '15세 이상 관람가'}">
        <c:set var="badgeLabel" value="15" />
        <c:set var="badgeClass" value="age15" />
        <c:set var="badgeTitle" value="15세 이상 관람가" />
    </c:when>

    <c:when test="${ageRating eq '청소년 관람불가'}">
        <c:set var="badgeLabel" value="19" />
        <c:set var="badgeClass" value="adult" />
        <c:set var="badgeTitle" value="청소년 관람불가" />
    </c:when>
</c:choose>

<span class="${outerClass}"
      title="${badgeTitle}">
    <span class="content-age-rating-badge is-${badgeClass}"
          aria-label="${badgeTitle}">
        ${badgeLabel}
    </span>
</span>
