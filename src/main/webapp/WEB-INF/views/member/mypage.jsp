<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="commonTag" tagdir="/WEB-INF/tags/common" %>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">

<title>ODITJI - 마이페이지</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/member.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/mypage.css">

<script defer
        src="${pageContext.request.contextPath}/js/mypage.js">
</script>

<jsp:include page="/WEB-INF/views/common/head-assets.jsp"/>
</head>


<body

    data-context-path="${pageContext.request.contextPath}"
    data-open-member-modal="${openMemberModal}"
    data-open-delete-modal="${openDeleteModal}"
    data-open-ott-modal="${param.openOttModal}"
    data-error-message="${errorMessage}"
    data-message="${message}">


<jsp:include page="/WEB-INF/views/common/header.jsp"/>


<main id="mainContent" class="mypage-container">

    <!-- =========================================================
         [마이페이지 디자인 수정]
         기존 조회 데이터와 버튼 ID는 그대로 유지하고,
         화면 배치만 시안에 맞게 좌측 비주얼 + 우측 정보 카드 구조로 변경했습니다.
    ========================================================== -->
    <div class="mypage-dashboard">

        <!-- [마이페이지 디자인 수정] 좌측 브랜드 비주얼 영역 -->
        <section class="mypage-hero" aria-labelledby="mypageGreetingTitle">
            <span class="mypage-hero-badge">MY PAGE</span>

            <div class="mypage-hero-copy">
                <p>안녕하세요,</p>
                <h1 id="mypageGreetingTitle">${loginMember.nickname}님</h1>
                <span class="mypage-hero-spark" aria-hidden="true">✦</span>
                <div class="mypage-hero-line" aria-hidden="true"></div>
                <p class="mypage-hero-message">
                    오늘도 ODITJI에서<br>
                    즐거운 콘텐츠를 찾아보세요.
                </p>
            </div>

            <!-- =========================================================
                 [마이페이지 배경 이미지 수정]
                 인삿말과 회원명은 JSP에서 동적으로 출력하고,
                 영화 장식은 mypage-left-bg.png 배경 이미지로만 표시합니다.
                 따라서 회원명과 안내 문구가 이미지에 고정되지 않습니다.
            ========================================================== -->
        </section>

        <div class="mypage-content-column">

            <!-- ================= Profile ================= -->
            <section class="mypage-panel mypage-profile" aria-labelledby="profileTitle">
                <div class="mypage-panel-heading">
                    <span class="mypage-heading-icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24"><path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm7 8a7 7 0 0 0-14 0"/></svg>
                    </span>
                    <h2 id="profileTitle">프로필</h2>
                </div>

                <div class="mypage-profile-body">
                    <div class="mypage-profile-left">
                        <div class="mypage-profile-image">
                            <c:choose>
                                <c:when test="${empty loginMember.profileImage}">
                                    <img class="profile-img"
                                         src="${pageContext.request.contextPath}/images/profile_image.jpg"
                                         alt="기본 프로필">
                                </c:when>
                                <c:when test="${fn:startsWith(loginMember.profileImage, 'http')}">
                                    <img class="profile-img"
                                         src="${loginMember.profileImage}"
                                         alt="${loginProvider eq 'NAVER' ? '네이버 프로필' : (loginProvider eq 'GOOGLE' ? '구글 프로필' : '카카오 프로필')}">
                                </c:when>
                                <c:otherwise>
                                    <img class="profile-img"
                                         src="${pageContext.request.contextPath}/uploads/profile/${loginMember.profileImage}"
                                         alt="업로드 프로필">
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="mypage-profile-info">
                            <div class="mypage-name-row">
                                <h3>${loginMember.nickname}</h3>
                                <span class="mypage-member-type">
                                    <span aria-hidden="true">◇</span>
                                    <c:choose>
                                        <c:when test="${loginMember.role eq 'ADMIN'}">관리자</c:when>
                                        <c:when test="${business ne null}">사업자</c:when>
                                        <c:otherwise>일반 회원</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <p class="mypage-profile-email">${loginMember.email}</p>
                            <p class="mypage-created-at">
                                <span aria-hidden="true">▣</span>
                                가입일 : ${dt:format(loginMember.createdAt, 'yyyy-MM-dd')}
                            </p>
                        </div>
                    </div>

                    <div class="mypage-profile-actions">
                        <!-- 기존 JS가 사용하는 ID 유지 -->
                        <button type="button" id="updateMemberBtn" class="mypage-action-btn">
                            회원정보 수정 <span aria-hidden="true">›</span>
                        </button>

                        <!-- [알림 수신 설정 위치 변경] 별도 패널 대신 프로필 영역 모달 버튼으로 이동 -->
                        <button type="button" id="notificationSettingBtn" class="mypage-action-btn">
                            알림 수신 설정 <span aria-hidden="true">›</span>
                        </button>
                    </div>
                </div>
            </section>

            <!-- ================= OTT ================= -->
            <section class="mypage-panel mypage-ott" id="mypageOttSection" aria-labelledby="ottTitle">
                <div class="mypage-ott-content">
                    <div class="mypage-panel-heading mypage-panel-heading-inline">
                        <span class="mypage-heading-icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24"><rect x="3" y="6" width="18" height="14" rx="3"/><path d="m8 3 4 3 4-3"/></svg>
                        </span>
                        <h2 id="ottTitle">내 OTT</h2>
                        <p>현재 이용 중인 OTT 플랫폼입니다.</p>
                    </div>

                    <div class="mypage-ott-list">
                        <c:choose>
                            <c:when test="${not empty ottList}">
                                <c:forEach var="ott" items="${ottList}">
                                    <a class="mypage-ott-chip"
                                       href="${ott.siteUrl}"
                                       target="_blank"
                                       rel="noopener noreferrer">
                                        <img class="mypage-ott-chip-logo"
                                             src="${ott.logoImage}"
                                             alt="${ott.platformName}">
                                        <span>
                                            <commonTag:platformDisplayName platformName="${ott.platformName}"/>
                                        </span>
                                    </a>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="mypage-empty">등록된 OTT가 없습니다.</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- 기존 JS가 사용하는 ID 유지 -->
                <button type="button" id="updateOttBtn" class="mypage-action-btn">
                    OTT 정보 수정 <span aria-hidden="true">›</span>
                </button>
            </section>

            <!-- ================= My Activity ================= -->
            <section class="mypage-panel mypage-activity" aria-labelledby="activityTitle">
                <div class="mypage-panel-heading mypage-panel-heading-inline">
                    <span class="mypage-heading-icon" aria-hidden="true">
                        <svg viewBox="0 0 24 24"><path d="M5 20V10M12 20V4M19 20v-7"/></svg>
                    </span>
                    <h2 id="activityTitle">나의 활동</h2>
                    <p>ODITJI에서의 활동 내역입니다.</p>
                </div>

                <div class="mypage-activity-grid">
                    <a href="${pageContext.request.contextPath}/favorite/list" class="mypage-activity-card activity-favorite">
                        <span class="mypage-activity-icon" aria-hidden="true">♥</span>
                        <span class="mypage-activity-count">${favoriteCount}</span>
                        <span class="mypage-activity-title">찜 목록</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/cart" class="mypage-activity-card activity-cart">
                        <span class="mypage-activity-icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24">
                                <path d="M3 4h2l2.4 12.2a2 2 0 0 0 2 1.6h7.6a2 2 0 0 0 2-1.6L21 8H6.2"/>
                                <circle cx="10" cy="20" r="1.4"/>
                                <circle cx="17" cy="20" r="1.4"/>
                            </svg>
                        </span>
                        <span class="mypage-activity-count">${cartCount}</span>
                        <span class="mypage-activity-title">장바구니</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/order/list" class="mypage-activity-card activity-order">
                        <span class="mypage-activity-icon" aria-hidden="true">▣</span>
                        <span class="mypage-activity-count">${orderCount}</span>
                        <span class="mypage-activity-title">주문내역</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/review/myReviewList" class="mypage-activity-card activity-review">
                        <span class="mypage-activity-icon" aria-hidden="true">★</span>
                        <span class="mypage-activity-count">${reviewCount}</span>
                        <span class="mypage-activity-title">내가 작성한 리뷰</span>
                    </a>

                    <!-- [마이페이지 구독 계산 결과 모달 연동 추가] -->
                    <button type="button"
                            id="subResultCardBtn"
                            class="mypage-activity-card activity-subscription">
                        <span class="mypage-activity-icon" aria-hidden="true">
                            <svg viewBox="0 0 24 24"><rect x="4" y="2" width="16" height="20" rx="2"/><line x1="8" y1="6" x2="16" y2="6"/><path d="M16 10h.01"/><path d="M12 10h.01"/><path d="M8 10h.01"/><path d="M16 14h.01"/><path d="M12 14h.01"/><path d="M8 14h.01"/><path d="M16 18h.01"/><path d="M12 18h.01"/><path d="M8 18h.01"/></svg>
                        </span>
                        <span class="mypage-activity-count" id="subResultCount">${subResultCount}</span>
                        <span class="mypage-activity-title">구독 계산 결과</span>
                    </button>
                </div>
            </section>
        </div>
    </div>

    <!-- ================= Withdraw ================= -->
    <c:choose>
        <%-- 사업자 회원 --%>
        <c:when test="${loginMember.role eq 'BUSINESS'}">
            <section class="mypage-withdraw" aria-labelledby="withdrawTitle">
                <div class="mypage-withdraw-copy">
                    <h2 id="withdrawTitle">회원탈퇴</h2>
                    <p>사업자 회원은 판매·주문·정산 데이터 보존을 위해 일반 회원탈퇴를 할 수 없습니다.</p>
                </div>
            </section>
        </c:when>

        <%-- 일반 회원 --%>
        <c:otherwise>
            <section class="mypage-withdraw" aria-labelledby="withdrawTitle">
                <div class="mypage-withdraw-copy">
                    <h2 id="withdrawTitle">회원탈퇴</h2>
                    <p>탈퇴 시 계정은 즉시 비활성화되며, 7일 후 모든 데이터가 자동으로 완전히 삭제됩니다.</p>
                </div>
                <button type="button" id="deleteBtn" class="mypage-withdraw-btn">
                    회원탈퇴 <span aria-hidden="true">›</span>
                </button>
            </section>
        </c:otherwise>
    </c:choose>
</main>


<!-- ================= MEMBER MODAL ================= -->


<div id="memberModal" class="modal-overlay hidden">


    <!-- =========================================================
         [회원정보 수정 모달 디자인 변경]
         - 기존 회원정보 수정 기능과 form 전송 구조는 그대로 유지합니다.
         - 닉네임 / 이메일 / 전화번호 / 비밀번호 / 프로필 이미지 항목을
           아이콘 + 라벨 + 입력 영역 형태로 통일합니다.
         - 다른 모달에는 영향을 주지 않도록 member-update-modal 클래스를 추가합니다.
         ========================================================= -->
    <div class="modal-box member-update-modal">

        <div class="member-update-header">

            <h2>
                회원정보 수정
            </h2>

        </div>


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


            <input type="hidden"
                   id="originalEmail"
                   value="${loginMember.email}">



            <!-- =========================================================
                 [회원정보 수정 디자인 변경 - 닉네임]
                 기존 닉네임 입력값과 중복확인 기능은 그대로 유지합니다.
                 ========================================================= -->

            <div class="form-group member-update-form-group">

                <div class="member-update-label">

                    <span class="member-update-icon"
                          aria-hidden="true">

                        <!-- 사용자 아이콘 -->
                        <svg viewBox="0 0 24 24"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="1.8"
                             stroke-linecap="round"
                             stroke-linejoin="round">

                            <path d="M20 21a8 8 0 0 0-16 0"></path>
                            <circle cx="12" cy="7" r="4"></circle>

                        </svg>

                    </span>


                    <label for="updateNickname">
                        닉네임
                    </label>

                </div>


                <div class="member-update-field">

                    <div class="duplicate-check-row">

                        <input type="text"
                               id="updateNickname"
                               name="nickname"
                               value="${loginMember.nickname}"
                               placeholder="한글/영문/숫자 2~10자"
                               maxlength="10"
                               required>


                        <button type="button"
                                id="checkUpdateNicknameBtn"
                                class="duplicate-check-btn">

                            중복확인

                        </button>

                    </div>


                    <small id="nicknameMessage"
                           class="input-message">

                    </small>

                </div>

            </div>

            <!-- 이메일 -->

            <c:if test="${!socialMember}">

                <!-- =========================================================
                     [회원정보 수정 디자인 변경 - 이메일]
                     일반 회원에게만 표시되는 기존 이메일 입력 및
                     중복확인 기능을 그대로 유지합니다.
                     ========================================================= -->
                <div class="form-group member-update-form-group">

                    <div class="member-update-label">

                        <span class="member-update-icon"
                              aria-hidden="true">

                            <!-- 이메일 아이콘 -->
                            <svg viewBox="0 0 24 24"
                                 fill="none"
                                 stroke="currentColor"
                                 stroke-width="1.8"
                                 stroke-linecap="round"
                                 stroke-linejoin="round">

                                <rect x="3"
                                      y="5"
                                      width="18"
                                      height="14"
                                      rx="2"></rect>

                                <path d="m3 7 9 6 9-6"></path>

                            </svg>

                        </span>


                        <label for="updateEmail">
                            이메일
                        </label>

                    </div>


                    <div class="member-update-field">

                        <div class="duplicate-check-row">

                            <input type="email"
                                   id="updateEmail"
                                   name="email"
                                   value="${loginMember.email}"
                                   placeholder="example@email.com"
                                   required>


                            <button type="button"
                                    id="checkUpdateEmailBtn"
                                    class="duplicate-check-btn">

                                중복확인

                            </button>

                        </div>


                        <small id="emailMessage"
                               class="input-message">

                        </small>

                    </div>

                </div>

            </c:if>

            <!-- =========================================================
                 [회원정보 수정 디자인 변경 - 전화번호]
                 기존 전화번호 입력 기능은 그대로 유지합니다.
                 ========================================================= -->

            <div class="form-group member-update-form-group">

                <div class="member-update-label">

                    <span class="member-update-icon"
                          aria-hidden="true">

                        <!-- 전화 아이콘 -->
                        <svg viewBox="0 0 24 24"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="1.8"
                             stroke-linecap="round"
                             stroke-linejoin="round">

                            <path d="M22 16.92v3a2 2 0 0 1-2.18 2
                                     19.79 19.79 0 0 1-8.63-3.07
                                     19.5 19.5 0 0 1-6-6
                                     19.79 19.79 0 0 1-3.07-8.67
                                     A2 2 0 0 1 4.11 2h3
                                     a2 2 0 0 1 2 1.72
                                     12.84 12.84 0 0 0 .7 2.81
                                     2 2 0 0 1-.45 2.11L8.09 9.91
                                     a16 16 0 0 0 6 6l1.27-1.27
                                     a2 2 0 0 1 2.11-.45
                                     12.84 12.84 0 0 0 2.81.7
                                     A2 2 0 0 1 22 16.92z"></path>

                        </svg>

                    </span>

                    <label for="updatePhone">
                        전화번호
                    </label>

                </div>

                <div class="member-update-field">

                    <input type="text"
                           id="updatePhone"
                           name="phone"
                           value="${loginMember.phone}"
                           placeholder="010-1234-5678"
                           maxlength="13">

                </div>

            </div>

            <!-- 비밀번호 변경 -->

            <c:if test="${!socialMember}">

                <!-- =========================================================
                     [회원정보 수정 디자인 변경 - 현재 비밀번호]
                     기존 비밀번호 표시/숨김 기능은 그대로 유지합니다.
                     ========================================================= -->
                <div class="form-group member-update-form-group">

                    <div class="member-update-label">

                        <span class="member-update-icon"
                              aria-hidden="true">

                            <!-- 잠금 아이콘 -->
                            <svg viewBox="0 0 24 24"
                                 fill="none"
                                 stroke="currentColor"
                                 stroke-width="1.8"
                                 stroke-linecap="round"
                                 stroke-linejoin="round">

                                <rect x="4"
                                      y="10"
                                      width="16"
                                      height="11"
                                      rx="2"></rect>

                                <path d="M8 10V7a4 4 0 0 1 8 0v3"></path>

                            </svg>

                        </span>

                        <label for="currentPw">
                            현재 비밀번호
                        </label>

                    </div>

                    <div class="member-update-field">

                        <div class="password-input-row">

                            <input type="password"
                                   id="currentPw"
                                   name="currentPw"
                                   autocomplete="current-password">

                            <button type="button"
                                    class="password-toggle-btn"
                                    data-target="currentPw"
                                    aria-label="현재 비밀번호 표시">
                                보기
                            </button>

                        </div>

                    </div>

                </div>

                <!-- =========================================================
                     [회원정보 수정 디자인 변경 - 새 비밀번호]
                     기존 비밀번호 입력 기능은 그대로 유지합니다.
                     ========================================================= -->
                <div class="form-group member-update-form-group">

                    <div class="member-update-label">

                        <span class="member-update-icon"
                              aria-hidden="true">

                            <!-- 잠금 아이콘 -->
                            <svg viewBox="0 0 24 24"
                                 fill="none"
                                 stroke="currentColor"
                                 stroke-width="1.8"
                                 stroke-linecap="round"
                                 stroke-linejoin="round">

                                <rect x="4"
                                      y="10"
                                      width="16"
                                      height="11"
                                      rx="2"></rect>

                                <path d="M8 10V7a4 4 0 0 1 8 0v3"></path>

                            </svg>

                        </span>

                        <label for="newPw">
                            새 비밀번호
                        </label>

                    </div>

                    <div class="member-update-field">

                        <div class="password-input-row">

                            <input type="password"
                                   id="newPw"
                                   name="newPw"
                                   placeholder="영문, 숫자, 특수문자 포함 8~20자"
                                   autocomplete="new-password">

                            <button type="button"
                                    class="password-toggle-btn"
                                    data-target="newPw"
                                    aria-label="새 비밀번호 표시">
                                보기
                            </button>

                        </div>

                    </div>

                </div>

                <!-- =========================================================
                     [회원정보 수정 디자인 변경 - 새 비밀번호 확인]
                     기존 비밀번호 확인 기능은 그대로 유지합니다.
                     ========================================================= -->
                <div class="form-group member-update-form-group">

                    <div class="member-update-label">

                        <span class="member-update-icon"
                              aria-hidden="true">

                            <!-- 잠금 아이콘 -->
                            <svg viewBox="0 0 24 24"
                                 fill="none"
                                 stroke="currentColor"
                                 stroke-width="1.8"
                                 stroke-linecap="round"
                                 stroke-linejoin="round">

                                <rect x="4"
                                      y="10"
                                      width="16"
                                      height="11"
                                      rx="2"></rect>

                                <path d="M8 10V7a4 4 0 0 1 8 0v3"></path>

                            </svg>

                        </span>


                        <label for="newPwCheck">
                            새 비밀번호 확인
                        </label>

                    </div>


                    <div class="member-update-field">

                        <div class="password-input-row">

                            <input type="password"
                                   id="newPwCheck"
                                   name="newPwCheck"
                                   autocomplete="new-password">

                            <button type="button"
                                    class="password-toggle-btn"
                                    data-target="newPwCheck"
                                    aria-label="새 비밀번호 확인 표시">
                                보기
                            </button>

                        </div>

                    </div>

                </div>


            </c:if>

            <!-- SNS 회원 안내 -->

            <c:if test="${socialMember}">

                <!-- =========================================================
                     [회원정보 수정 디자인 변경 - 소셜 로그인 안내]
                     기존 안내 문구는 유지하고 시안의 초록색 안내 박스 형태로 변경합니다.
                     ========================================================= -->
                <div class="info-box member-update-info-box">

                    <span class="member-update-info-icon"
                          aria-hidden="true">

                        <!-- 잠금 아이콘 -->
                        <svg viewBox="0 0 24 24"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="1.8"
                             stroke-linecap="round"
                             stroke-linejoin="round">

                            <rect x="5"
                                  y="10"
                                  width="14"
                                  height="11"
                                  rx="2"></rect>

                            <path d="M8 10V7a4 4 0 0 1 8 0v3"></path>

                        </svg>

                    </span>


                    <span>
                        소셜 로그인 회원은
                        해당 소셜 계정에서 비밀번호를 관리합니다.
                    </span>

                </div>

            </c:if>

            <!-- =========================================================
                 [회원정보 수정 디자인 변경 - 프로필 이미지]
                 기존 파일 업로드 기능과 name/id는 그대로 유지합니다.
                 닉네임/전화번호와 동일하게 아이콘 + 라벨 + 입력 영역 구조로 변경합니다.
                 ========================================================= -->

            <div class="form-group member-update-form-group member-update-profile-group">

                <div class="member-update-label">

                    <span class="member-update-icon"
                          aria-hidden="true">

                        <!-- 이미지 아이콘 -->
                        <svg viewBox="0 0 24 24"
                             fill="none"
                             stroke="currentColor"
                             stroke-width="1.8"
                             stroke-linecap="round"
                             stroke-linejoin="round">

                            <rect x="3"
                                  y="3"
                                  width="18"
                                  height="18"
                                  rx="2"></rect>

                            <circle cx="8.5"
                                    cy="8.5"
                                    r="1.5"></circle>

                            <path d="m21 15-5-5L5 21"></path>

                        </svg>

                    </span>


                    <label for="profileImageFile">
                        프로필 이미지
                    </label>

                </div>


                <div class="member-update-field">

                    <div class="member-update-file-wrap">

                        <input type="file"
                               id="profileImageFile"
                               name="profileImageFile"
                               accept="image/*">

                    </div>

                </div>

            </div>

            <div class="modal-btns member-update-buttons">

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

<!-- ================= NOTIFICATION SETTING MODAL ================= -->

<!--
    [알림 수신 설정 위치 변경]
    기존에는 독립 패널(mypageNotificationSection)로 항상 노출되어 있었으나,
    프로필 영역의 '알림 수신 설정' 버튼을 눌렀을 때만 모달로 띄우는 방식으로 변경.
    내부 목록(notificationSettingList)을 그리는 mypage.js 로직은 그대로 재사용한다.
-->
<div id="notificationModal"
     class="modal-overlay hidden">

    <div class="modal-box">

        <h2>
            알림 수신 설정
        </h2>

        <p class="modal-desc">
            받고 싶은 알림만 골라서 켜고 끌 수 있습니다.
        </p>

        <div class="mypage-notification-list" id="notificationSettingList">
            <div class="mypage-empty">알림 설정을 불러오는 중입니다...</div>
        </div>

        <div class="modal-btns">

            <button type="button"
                    id="closeNotificationModal">

                닫기

            </button>

        </div>

    </div>

</div>

<!-- ================= SUBSCRIPTION RESULT MODAL ================= -->

<!--
    [마이페이지 구독 계산 결과 모달 연동 추가]
    '나의 활동' 카드의 구독 계산 결과 개수를 클릭하면 열리는 모달.
    목록은 mypage.js에서 GET /api/subscription/saved-results로 불러와 그린다.
-->
<div id="subResultModal"
     class="modal-overlay hidden">

    <div class="modal-box">

        <h2>
            나의 OTT 구독 조합 저장 기록
        </h2>

        <div class="mypage-subresult-list" id="subResultList">
            <div class="mypage-empty">저장된 결과를 불러오는 중입니다...</div>
        </div>

        <div class="modal-btns">

            <button type="button"
                    id="closeSubResultModal">

                닫기

            </button>

        </div>

    </div>

</div>

<!-- ================= OTT MODAL ================= -->


<div id="ottModal"
     class="modal-overlay hidden">


    <div class="modal-box">

        <h2>
            OTT 정보 수정
        </h2>


        <form id="mypageOttForm"
              action="${pageContext.request.contextPath}/member/updateOtt"
              method="post">


            <input type="hidden"
                   name="memberNo"
                   value="${loginMember.memberNo}">


            <div class="sns-ott-section-head">
                <span class="sns-ott-section-label">이용 중인 OTT (중복 선택 가능)</span>
                <span class="sns-ott-count" id="mypageOttCount">0개 선택</span>
            </div>


            <div class="sns-ott-option-list" id="mypageOttOptionList">

                <c:forEach var="platform" items="${platformList}">

                    <%--
                        =========================================================
                        현재 회원이 이용 중인 OTT는 체크된 상태로 표시한다.
                        =========================================================
                    --%>
                    <c:set var="isSelected" value="false"/>

                    <c:forEach var="myOtt" items="${ottList}">
                        <c:if test="${myOtt.platformName eq platform.platformName}">
                            <c:set var="isSelected" value="true"/>
                        </c:if>
                    </c:forEach>

                    <label class="sns-ott-option">
                        <input type="checkbox"
                               name="ottList"
                               value="${platform.platformName}"
                               ${isSelected ? 'checked' : ''}>

                        <img class="sns-ott-logo-img"
                             src="${platform.logoImage}"
                             alt="${platform.platformName}">

                        <span class="sns-ott-mark"></span>

                        <span>
                            <commonTag:platformDisplayName platformName="${platform.platformName}"/>
                        </span>
                    </label>

                </c:forEach>

                <%--
                    =========================================================
                    OTT 미사용 선택 (selectOtt.jsp / join.jsp 와 동일한 패턴)

                    체크 시 다른 OTT 선택은 모두 해제/비활성화되며
                    ottList는 비워진 채로 제출된다.
                    =========================================================
                --%>
                <label class="sns-ott-option sns-ott-option--none">

                    <input type="checkbox"
                           name="noOtt"
                           id="mypageNoOtt"
                           value="Y"
                           ${empty ottList ? 'checked' : ''}>

                    <span class="sns-ott-mark"></span>

                    <span>이용 중인 OTT 없음</span>
                </label>

            </div>

            <p class="sns-ott-hint">
                이용 중인 OTT가 없다면 '이용 중인 OTT 없음'을 선택해주세요.
            </p>


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
              action="${pageContext.request.contextPath}/member/withdraw"
              method="post">


            <input type="hidden"
                   name="memberNo"
                   value="${loginMember.memberNo}">


            <p class="danger-text">

                탈퇴 신청 시 계정은 즉시 비활성화되며, 7일 후 모든 데이터가 자동으로 완전히 삭제됩니다.
                삭제 이후에는 복구할 수 없습니다.

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