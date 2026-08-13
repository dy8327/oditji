<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="ageRating"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="outerClass"
              required="true"
              type="java.lang.String" %>
<%@ attribute name="innerClass"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="mode"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="showAriaLabel"
              required="false"
              type="java.lang.Boolean" %>
<%@ attribute name="includeDataAgeRating"
              required="false"
              type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    콘텐츠 연령등급 문자열을 화면 공통 배지 값으로 변환한다.
    연령등급 변환 로직을 한 곳에 모아 JSP 간 중복을 제거한다.

    innerClass:
    기존 콘텐츠 카드(main/more variant)는 content-age-rating-badge 클래스를 쓰지만,
    콘텐츠 목록/검색결과/찜목록 그리드(content-list-modern.css)는 age-rating-badge
    클래스를 쓰는 별도 CSS 체계라서, 호출하는 쪽에서 내부 span 클래스를
    오버라이드할 수 있게 했다. 값을 안 넘기면 기존과 동일하게 동작한다.

    mode: "nested"(기본, 기존 동작 그대로 - 바깥 span + 안쪽 span 2단 구조,
          contentCard.tag/rankingCard.tag/recommendCard.jsp가 사용) /
          "flat"(span 하나에 outerClass와 is-등급 클래스를 합쳐서 렌더링,
          contentDetail.jsp/index.jsp의 뱃지가 사용) /
          "chip"(선택된 필터를 보여주는 필터 칩 버튼 안에서 쓰는 순수 장식용 배지.
          span 하나에 outerClass와 is-등급 클래스만 합치고 aria-hidden="true"를 붙인다.
          제목/설명(title, aria-label)은 넣지 않는다 - 배지 바로 옆에 같은 내용을
          텍스트로 이미 노출하는 칩 버튼 안에서만 쓰는 걸 전제로 하기 때문이다.
          (searchResult.jsp, contentList.jsp의 "선택된 필터" 칩이 사용)
          chip일 때 innerClass/showAriaLabel/includeDataAgeRating은 무시된다.
          flat일 때 innerClass는 무시된다.
    showAriaLabel: 기본 true. false로 주면 aria-label 속성을 생략한다
          (index.jsp 히어로 뱃지는 aria-label 없이 title만 쓴다).
    includeDataAgeRating: 기본 false. true면 nested 모드의 바깥 span에
          data-age-rating="${fn:escapeXml(ageRating)}" 속성을 추가한다
          (recommendCard.jsp가 원본 등급값을 DOM에 남기기 위해 사용).
          flat 모드에서는 사용하지 않는다.
--%>
<c:set var="resolvedInnerClass"
       value="${empty innerClass ? 'content-age-rating-badge' : innerClass}" />
<c:set var="resolvedMode" value="${empty mode ? 'nested' : mode}" />
<c:set var="resolvedShowAriaLabel" value="${empty showAriaLabel ? true : showAriaLabel}" />
<c:set var="resolvedIncludeDataAgeRating" value="${empty includeDataAgeRating ? false : includeDataAgeRating}" />

<c:set var="badgeLabel" value="미정" />
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

<c:choose>
    <c:when test="${resolvedMode eq 'chip'}">
        <span class="${outerClass} is-${badgeClass}"
              aria-hidden="true">
            ${badgeLabel}
        </span>
    </c:when>
    <c:when test="${resolvedMode eq 'flat'}">
        <c:choose>
            <c:when test="${resolvedShowAriaLabel}">
                <span class="${outerClass} is-${badgeClass}"
                      title="${badgeTitle}"
                      aria-label="${badgeTitle}">
                    ${badgeLabel}
                </span>
            </c:when>
            <c:otherwise>
                <span class="${outerClass} is-${badgeClass}"
                      title="${badgeTitle}">
                    ${badgeLabel}
                </span>
            </c:otherwise>
        </c:choose>
    </c:when>
    <c:otherwise>
        <span class="${outerClass}"
              title="${badgeTitle}"
              <c:if test="${resolvedIncludeDataAgeRating}">data-age-rating="${fn:escapeXml(ageRating)}"</c:if>>
            <span class="${resolvedInnerClass} is-${badgeClass}"
                  aria-label="${badgeTitle}">
                ${badgeLabel}
            </span>
        </span>
    </c:otherwise>
</c:choose>
