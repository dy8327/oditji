<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>ODITJI | OTT 선택</title>
    <link rel="stylesheet" href="${contextPath}/css/member.css">
</head>
<body>

<div class="sns-ott-page">

    <div class="sns-ott-container">

        <h2 class="sns-ott-title">OTT 선택</h2>

        <p class="sns-ott-desc">
            현재 이용 중인 OTT를 선택해주세요.
        </p>

        <c:if test="${not empty errorMessage}">
            <div class="sns-ott-error">
                ${errorMessage}
            </div>
        </c:if>

        <form action="${contextPath}/member/platform/select"
              method="post"
              onsubmit="return validateSnsOttSelect();">

            <div class="sns-ott-option-list">

                <c:forEach var="platform" items="${platformList}">

                    <label class="sns-ott-option">
                        <input type="checkbox"
                               name="platformNoList"
                               value="${platform.platformNo}">

                        <span>
                            <c:choose>
                                <c:when test="${platform.platformName eq 'Netflix'}">
                                    넷플릭스
                                </c:when>
                                <c:when test="${platform.platformName eq 'Disney Plus'}">
                                    디즈니+
                                </c:when>
                                <c:when test="${platform.platformName eq 'Disney+'}">
                                    디즈니+
                                </c:when>
                                <c:when test="${platform.platformName eq 'Tving'}">
                                    티빙
                                </c:when>
                                <c:when test="${platform.platformName eq 'TVING'}">
                                    티빙
                                </c:when>
                                <c:when test="${platform.platformName eq 'Wavve'}">
                                    웨이브
                                </c:when>
                                <c:when test="${platform.platformName eq 'Watcha'}">
                                    왓챠
                                </c:when>
                                <c:when test="${platform.platformName eq 'Coupangplay'}">
                                    쿠팡플레이
                                </c:when>
                                <c:when test="${platform.platformName eq 'Coupang Play'}">
                                    쿠팡플레이
                                </c:when>
                                <c:otherwise>
                                    ${platform.platformName}
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </label>

                </c:forEach>

            </div>

            <button type="submit" class="sns-ott-submit-btn">
                선택 완료
            </button>

        </form>

    </div>

</div>

<script>
    function validateSnsOttSelect() {
        const checkedList = document.querySelectorAll('input[name="platformNoList"]:checked');

        if (checkedList.length === 0) {
            alert('이용 중인 OTT를 하나 이상 선택해주세요.');
            return false;
        }

        return true;
    }
</script>

</body>
</html>