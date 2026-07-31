<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="contextPath" value="${pageContext.request.contextPath}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ODITJI | OTT 선택</title>
    <link rel="stylesheet" href="${contextPath}/css/layout.css">
    <link rel="stylesheet" href="${contextPath}/css/member.css">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11.26.25/dist/sweetalert2.all.min.js"
        integrity="sha512-pnPZhx5S+z5FSVwy62gcyG2Mun8h6R+PG01MidzU+NGF06/ytcm2r6+AaWMBXAnDHsdHWtsxS0dH8FBKA84FlQ=="
        crossorigin="anonymous"></script>
    <script src="${pageContext.request.contextPath}/js/common.js?v=8"></script>
    <script src="${pageContext.request.contextPath}/js/member.js"></script>
</head>
<body data-context-path="${contextPath}">

<div class="sns-ott-page">

    <div class="sns-ott-container">

        <div class="sns-ott-brand">
            <span class="sns-ott-logo">ODITJI</span>
        </div>

        <span class="sns-ott-eyebrow">STEP 2 · 마지막 단계예요</span>

        <h2 class="sns-ott-title">나에게 맞는 콘텐츠 준비하기</h2>

        <p class="sns-ott-desc">
            이용 중인 OTT를 선택해주시면<br>
            취향에 맞는 콘텐츠와 상품을 우선 추천해드려요.
        </p>

        <c:if test="${not empty errorMessage}">
            <div class="sns-ott-error">
                ${errorMessage}
            </div>
        </c:if>

        <form id="snsOttForm"
              action="${contextPath}/member/platform/select"
              method="post">

            <c:if test="${needEmailInput}">
                <div class="sns-ott-email-group">
                    <label for="email" class="sns-ott-email-label">
                        이메일
                    </label>

                    <%--
                        =========================================================
                        이메일 중복확인
                        join.jsp의 checkEmail()과 동일한 로직을 재사용한다.
                        (member.js의 checkEmail()이 #email 요소를 기준으로 동작)
                        =========================================================
                    --%>
                    <div class="sns-ott-email-row">
                        <input type="email"
                               id="email"
                               name="email"
                               class="sns-ott-email-input"
                               placeholder="이메일을 입력해주세요"
                               required>

                        <button type="button"
                                class="sns-ott-check-btn"
                                onclick="checkEmail()">
                            중복확인
                        </button>
                    </div>
                </div>
            </c:if>

            <div class="sns-ott-section-head">
                <span class="sns-ott-section-label">이용 중인 OTT (중복 선택 가능)</span>
                <span class="sns-ott-count" id="snsOttCount">0개 선택</span>
            </div>

            <div class="sns-ott-option-list" id="snsOttOptionList">

                <c:forEach var="platform" items="${platformList}">

                    <label class="sns-ott-option">
                        <input type="checkbox"
                               name="platformNoList"
                               value="${platform.platformNo}">

                        <img class="sns-ott-logo-img"
                             src="${platform.logoImage}"
                             alt="${platform.platformName}">

                        <span class="sns-ott-mark"></span>

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

                <%--
                    =========================================================
                    OTT 미사용 선택

                    join.jsp의 noOtt와 동일한 패턴.
                    체크 시 다른 OTT 선택은 모두 해제/비활성화되며
                    platformNoList는 비워진 채로 제출된다.
                    =========================================================
                --%>
                <label class="sns-ott-option sns-ott-option--none">
                    <input type="checkbox"
                           name="noOtt"
                           id="snsNoOtt"
                           value="Y">

                    <span class="sns-ott-mark"></span>

                    <span>이용 중인 OTT 없음</span>
                </label>

            </div>

            <p class="sns-ott-hint">
                이용 중인 OTT가 없다면 '이용 중인 OTT 없음'을 선택해주세요. 나중에 마이페이지에서 언제든 변경할 수 있어요.
            </p>

            <button type="submit" class="sns-ott-submit-btn">
                선택 완료하고 시작하기
            </button>

            <p class="sns-ott-footnote">
                선택한 정보는 콘텐츠 추천 목적으로만 사용돼요.
            </p>

        </form>

    </div>

</div>

</body>
</html>
