<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">

<title>ODITJI - 마이페이지</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/mypage.css">

<script defer
        src="${pageContext.request.contextPath}/js/mypage.js">
</script>

</head>


<body

    data-context-path="${pageContext.request.contextPath}"
    data-open-member-modal="${openMemberModal}"
    data-open-delete-modal="${openDeleteModal}"
    data-open-ott-modal="${param.openOttModal}">


<jsp:include page="/WEB-INF/views/common/header.jsp"/>


<c:if test="${not empty errorMessage}">

    <script>
        alert("${errorMessage}");
    </script>

</c:if>


<c:if test="${not empty message}">

    <script>
        alert("${message}");
    </script>

</c:if>


<main class="mypage-container">


<!-- ================= Welcome ================= -->


<section class="mypage-welcome">

    <p class="mypage-greeting">
        안녕하세요.
    </p>

    <h1>
        ${loginMember.nickname}님 👋
    </h1>

    <p class="mypage-message">
        오늘도 ODITJI에서 즐거운 콘텐츠를 찾아보세요.
    </p>

</section>





<!-- ================= Profile ================= -->


<section class="mypage-profile">

    <div class="mypage-profile-left">

        <div class="mypage-profile-image">

            <c:choose>

                <c:when test="${empty loginMember.profileImage}">

                    <img class="profile-img"
                         src="${pageContext.request.contextPath}/images/default-profile.png"
                         alt="기본 프로필">

                </c:when>

                <c:when test="${fn:startsWith(loginMember.profileImage, 'http')}">

                    <img class="profile-img"
                         src="${loginMember.profileImage}"
                         alt="카카오 프로필">

                </c:when>

                <c:otherwise>

                    <img class="profile-img"
                         src="${pageContext.request.contextPath}/uploads/profile/${loginMember.profileImage}"
                         alt="업로드 프로필">

                </c:otherwise>

            </c:choose>

        </div>


        <div class="mypage-profile-info">

            <h2>
                ${loginMember.nickname}
            </h2>

            <p>
                ${loginMember.email}
            </p>

            <span class="mypage-member-type">

                <c:choose>

                    <c:when test="${loginMember.role eq 'ADMIN'}">
                        관리자
                    </c:when>

                    <c:when test="${business ne null}">
                        사업자
                    </c:when>

                    <c:otherwise>
                        일반 회원
                    </c:otherwise>

                </c:choose>

            </span>

            <p class="mypage-created-at">
                가입일 :
                ${loginMember.createdAt}
            </p>

        </div>

    </div>


    <div class="mypage-profile-right">

        <button type="button"
                id="updateMemberBtn">

            회원정보 수정

        </button>

    </div>

</section>

<!-- ================= OTT ================= -->


<section class="mypage-ott"
         id="mypageOttSection">

    <div class="mypage-section-header">

        <div>

            <h2>
                내 OTT
            </h2>

            <p>
                현재 이용 중인 OTT 플랫폼입니다.
            </p>

        </div>

        <button type="button"
                id="updateOttBtn">

            OTT 정보 수정

        </button>

    </div>


    <div class="mypage-ott-list">

        <c:choose>

            <c:when test="${not empty ottList}">

                <c:forEach var="ott"
                           items="${ottList}">

                    <div class="mypage-ott-chip">

                        ${ott.platformName}

                    </div>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <div class="mypage-empty">

                    등록된 OTT가 없습니다.

                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>





<!-- ================= My Activity ================= -->


<section class="mypage-activity">

    <div class="mypage-section-header">

        <div>

            <h2>
                나의 활동
            </h2>

            <p>
                ODITJI에서의 활동 내역입니다.
            </p>

        </div>

    </div>


    <div class="mypage-activity-grid">


        <a href="${pageContext.request.contextPath}/favorite/list"
           class="mypage-activity-card">

            <div class="mypage-activity-icon">
                ❤️
            </div>

            <div class="mypage-activity-count">
                ${favoriteCount}
            </div>

            <div class="mypage-activity-title">
                찜한 콘텐츠
            </div>

        </a>



        <a href="${pageContext.request.contextPath}/cart"
           class="mypage-activity-card">

            <div class="mypage-activity-icon">
                🛒
            </div>

            <div class="mypage-activity-count">
                ${cartCount}
            </div>

            <div class="mypage-activity-title">
                장바구니
            </div>

        </a>



        <a href="${pageContext.request.contextPath}/order/list"
           class="mypage-activity-card">

            <div class="mypage-activity-icon">
                📦
            </div>

            <div class="mypage-activity-count">
                ${orderCount}
            </div>

            <div class="mypage-activity-title">
                주문내역
            </div>

        </a>



        <a href="${pageContext.request.contextPath}/review/contentReviewList"
           class="mypage-activity-card">

            <div class="mypage-activity-icon">
                ⭐
            </div>

            <div class="mypage-activity-count">
                ${reviewCount}
            </div>

            <div class="mypage-activity-title">
                내가 작성한 리뷰
            </div>

        </a>


    </div>

</section>





<!-- ================= Withdraw ================= -->


<section class="mypage-withdraw">

    <div class="mypage-section-header">

        <div>

            <h2>
                회원탈퇴
            </h2>

            <p>
                탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.
            </p>

        </div>

    </div>


    <div class="mypage-account-danger">

        <button type="button"
                id="deleteBtn"
                class="btn-danger">

            회원탈퇴

        </button>

    </div>

</section>

</main>

<!-- ================= MEMBER MODAL ================= -->


<div id="memberModal"
     class="modal-overlay hidden">


    <div class="modal-box">

        <h2>
            회원정보 수정
        </h2>


        <form id="memberUpdateForm"
              action="${pageContext.request.contextPath}/member/update"
              method="post"
              enctype="multipart/form-data">


            <input type="hidden"
                   name="memberNo"
                   value="${loginMember.memberNo}">


            <input type="hidden"
                   id="socialMember"
                   name="socialMember"
                   value="${socialMember}">


            <input type="hidden"
                   id="originalNickname"
                   value="${loginMember.nickname}">



            <!-- 닉네임 -->

            <div class="form-group">

                <label for="updateNickname">
                    닉네임
                </label>


                <div class="row">

                    <input type="text"
                           id="updateNickname"
                           name="nickname"
                           value="${loginMember.nickname}"
                           placeholder="한글/영문/숫자 2~10자"
                           required>


                    <button type="button"
                            id="checkUpdateNicknameBtn">

                        중복확인

                    </button>

                </div>


                <small id="nicknameMessage"
                       class="input-message">

                </small>

            </div>





            <!-- 이메일 -->


            <c:if test="${!socialMember}">

                <div class="form-group">

                    <label for="updateEmail">
                        이메일
                    </label>


                    <input type="email"
                           id="updateEmail"
                           name="email"
                           value="${loginMember.email}"
                           readonly>

                </div>

            </c:if>





            <!-- 전화번호 -->


            <div class="form-group">

                <label for="updatePhone">
                    전화번호
                </label>


                <input type="text"
                       id="updatePhone"
                       name="phone"
                       value="${loginMember.phone}"
                       placeholder="010-1234-5678"
                       maxlength="13">

            </div>





            <!-- 비밀번호 변경 -->


            <c:if test="${!socialMember}">


                <div class="form-group">

                    <label for="currentPw">
                        현재 비밀번호
                    </label>


                    <input type="password"
                           id="currentPw"
                           name="currentPw">

                </div>




                <div class="form-group">

                    <label for="newPw">
                        새 비밀번호
                    </label>


                    <input type="password"
                           id="newPw"
                           name="newPw"
                           placeholder="영문, 숫자, 특수문자 포함 8~20자">

                </div>




                <div class="form-group">

                    <label for="newPwCheck">
                        새 비밀번호 확인
                    </label>


                    <input type="password"
                           id="newPwCheck"
                           name="newPwCheck">

                </div>


            </c:if>





            <!-- SNS 회원 안내 -->


            <c:if test="${socialMember}">

                <div class="info-box">

                    카카오 로그인 회원은
                    카카오 계정에서 비밀번호를 관리합니다.

                </div>

            </c:if>





            <!-- 프로필 이미지 -->


            <div class="form-group">

                <label for="profileImageFile">
                    프로필 이미지
                </label>


                <input type="file"
                       id="profileImageFile"
                       name="profileImageFile"
                       accept="image/*">

            </div>





            <div class="modal-btns">

                <button type="submit"
                        id="memberUpdateBtn">

                    저장

                </button>


                <button type="button"
                        id="closeMemberModal">

                    취소

                </button>

            </div>


        </form>

    </div>

</div>

<!-- ================= OTT MODAL ================= -->


<div id="ottModal"
     class="modal-overlay hidden">


    <div class="modal-box">

        <h2>
            OTT 정보 수정
        </h2>


        <form action="${pageContext.request.contextPath}/member/updateOtt"
              method="post">


            <input type="hidden"
                   name="memberNo"
                   value="${loginMember.memberNo}">



            <label>

                <input type="checkbox"
                       name="ottList"
                       value="Netflix">

                넷플릭스

            </label>



            <label>

                <input type="checkbox"
                       name="ottList"
                       value="Disney Plus">

                디즈니+

            </label>



            <label>

                <input type="checkbox"
                       name="ottList"
                       value="Tving">

                티빙

            </label>



            <label>

                <input type="checkbox"
                       name="ottList"
                       value="Wavve">

                웨이브

            </label>



            <label>

                <input type="checkbox"
                       name="ottList"
                       value="Watcha">

                왓챠

            </label>



            <label>

                <input type="checkbox"
                       name="ottList"
                       value="Coupangplay">

                쿠팡플레이

            </label>



            <div class="modal-btns">

                <button type="submit">

                    저장

                </button>


                <button type="button"
                        id="closeOttModal">

                    취소

                </button>

            </div>


        </form>

    </div>

</div>

<!-- ================= DELETE MODAL ================= -->


<div id="deleteModal"
     class="modal-overlay hidden">


    <div class="modal-box">

        <h2>
            회원 탈퇴
        </h2>


        <form id="deleteForm"
              action="${pageContext.request.contextPath}/member/delete"
              method="post">


            <input type="hidden"
                   name="memberNo"
                   value="${loginMember.memberNo}">


            <p class="danger-text">

                탈퇴 시 모든 데이터가 삭제되며 복구할 수 없습니다.

            </p>


            <div class="form-group">

                <label for="deleteConfirmInput">

                    탈퇴를 진행하려면 아래 문구를 입력해주세요.

                </label>


                <input type="text"
                       id="deleteConfirmInput"
                       name="deleteConfirm"
                       placeholder="탈퇴하겠습니다"
                       required
                       autocomplete="off">

            </div>



            <div class="modal-btns">

                <button type="submit"
                        class="btn-danger">

                    탈퇴하기

                </button>


                <button type="button"
                        id="closeDeleteModal">

                    취소

                </button>

            </div>


        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>