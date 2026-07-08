<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<!DOCTYPE html>
<html>

<head>

<meta charset="UTF-8">

<title>ODITJI - 마이페이지</title>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/mypage.css">

</head>

<body>

	<jsp:include page="/WEB-INF/views/common/header.jsp"/>

	<main class="mypage-container">

		<!-- ================= Welcome ================= -->

		<section class="mypage-welcome">

			<p class="mypage-greeting">

				안녕하세요.

			</p>

			<h1>

				${loginMember.memberNickname}님 👋

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

						<c:when test="${not empty loginMember.profileImage}">

							<img
								src="${pageContext.request.contextPath}/upload/profile/${loginMember.profileImage}"
								alt="프로필">

						</c:when>

						<c:otherwise>

							<img
								src="${pageContext.request.contextPath}/images/profile.svg"
								alt="기본 프로필">

						</c:otherwise>

					</c:choose>

				</div>

				<div class="mypage-profile-info">

					<h2>

						${loginMember.memberNickname}

					</h2>

					<p>

						${loginMember.memberEmail}

					</p>

					<span class="mypage-member-type">

						<c:choose>

							<c:when test="${business ne null}">

								사업자 회원

							</c:when>

							<c:otherwise>

								일반 회원

							</c:otherwise>

						</c:choose>

					</span>

				</div>

			</div>

			<div class="mypage-profile-right">

				<button type="button"
					onclick="location.href='${pageContext.request.contextPath}/member/updateMember'">

					회원정보 수정

				</button>

			</div>

		</section>

		<!-- ================= OTT ================= -->

		<section class="mypage-ott">

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
					onclick="location.href='${pageContext.request.contextPath}/member/updateMember'">

					OTT 정보 수정

				</button>

			</div>

			<div class="mypage-ott-list">

				<c:choose>

					<c:when test="${not empty ottList}">

						<c:forEach var="ott" items="${ottList}">

							<div class="mypage-ott-chip">

								${ott.ottName}

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

					<h2>나의 활동</h2>

					<p>ODITJI에서의 활동 내역입니다.</p>

				</div>

			</div>

			<div class="mypage-activity-grid">

				<a href="${pageContext.request.contextPath}/favorite/list"
				   class="mypage-activity-card">

					<div class="mypage-activity-icon">❤️</div>

					<div class="mypage-activity-count">

						${favoriteCount}

					</div>

					<div class="mypage-activity-title">

						찜한 콘텐츠

					</div>

				</a>

				<a href="${pageContext.request.contextPath}/cart"
				   class="mypage-activity-card">

					<div class="mypage-activity-icon">🛒</div>

					<div class="mypage-activity-count">

						${cartCount}

					</div>

					<div class="mypage-activity-title">

						장바구니

					</div>

				</a>

				<a href="${pageContext.request.contextPath}/order/list"
				   class="mypage-activity-card">

					<div class="mypage-activity-icon">📦</div>

					<div class="mypage-activity-count">

						${orderCount}

					</div>

					<div class="mypage-activity-title">

						주문내역

					</div>

				</a>

				<a href="${pageContext.request.contextPath}/review/contentReviewList"
				   class="mypage-activity-card">

					<div class="mypage-activity-icon">⭐</div>

					<div class="mypage-activity-count">

						${reviewCount}

					</div>

					<div class="mypage-activity-title">

						내가 작성한 리뷰

					</div>

				</a>

			</div>

		</section>



		<!-- ================= Recent Review ================= -->

		<section class="mypage-review">

			<div class="mypage-section-header">

				<div>

					<h2>최근 작성한 리뷰</h2>

					<p>내가 최근 작성한 리뷰입니다.</p>

				</div>

				<a href="${pageContext.request.contextPath}/review/contentReviewList"
				   class="mypage-more">

					전체보기 →

				</a>

			</div>


			<c:choose>

				<c:when test="${not empty reviewList}">

					<div class="mypage-review-list">

						<c:forEach var="review"
								   items="${reviewList}"
								   end="4">

							<div class="mypage-review-card">

								<div class="mypage-review-top">

									<div>

										<h3>

											${review.contentTitle}

										</h3>

										<span>

											${review.reviewCreateDate}

										</span>

									</div>

									<div class="mypage-review-score">

										⭐ ${review.reviewRating}

									</div>

								</div>

								<p class="mypage-review-content">

									${review.reviewContent}

								</p>

							</div>

						</c:forEach>

					</div>

				</c:when>

				<c:otherwise>

					<div class="mypage-empty-box">

						<div class="mypage-empty-icon">

							📝

						</div>

						<h3>

							아직 작성한 리뷰가 없습니다.

						</h3>

						<p>

							마음에 드는 콘텐츠를 감상하고
							첫 리뷰를 작성해보세요.

						</p>

						<button type="button"
								onclick="location.href='${pageContext.request.contextPath}/content/contentList'">

							콘텐츠 보러가기

						</button>

					</div>

				</c:otherwise>

			</c:choose>

		</section>

        		<!-- ================= Business ================= -->

		<section class="mypage-business">

			<div class="mypage-section-header">

				<div>

					<h2>사업자 서비스</h2>

					<p>굿즈 판매 및 사업자 전용 서비스를 이용해보세요.</p>

				</div>

			</div>

			<div class="mypage-business-banner">

				<div class="mypage-business-info">

					<div class="mypage-business-icon">

						🏪

					</div>

					<div>

						<h3>

							사업자 회원으로 전환해보세요.

						</h3>

						<p>

							굿즈를 판매하고 다양한 사업자 전용 서비스를 이용해보세요.

						</p>

					</div>

				</div>

				<c:choose>

					<c:when test="${business eq null}">

						<button
							type="button"
							class="mypage-business-btn"
							onclick="location.href='${pageContext.request.contextPath}/business/join'">

							사업자 회원 전환

						</button>

					</c:when>

					<c:otherwise>

						<button
							type="button"
							class="mypage-business-btn"
							onclick="location.href='${pageContext.request.contextPath}/business/businessMain'">

							사업자 페이지

						</button>

					</c:otherwise>

				</c:choose>

			</div>

		</section>


		<!-- ================= Account ================= -->

		<section class="mypage-account">

			<div class="mypage-section-header">

				<div>

					<h2>계정 정보</h2>

					<p>회원 계정 정보를 확인할 수 있습니다.</p>

				</div>

			</div>

			<div class="mypage-account-grid">

				<div class="mypage-account-card">

					<h4>회원등급</h4>

					<p>

						<c:choose>

							<c:when test="${business ne null}">

								사업자 회원

							</c:when>

							<c:otherwise>

								일반 회원

							</c:otherwise>

						</c:choose>

					</p>

				</div>

				<div class="mypage-account-card">

					<h4>가입일</h4>

					<p>

						${loginMember.memberEnrollDate}

					</p>

				</div>

				<div class="mypage-account-card">

					<h4>최근 로그인</h4>

					<p>

						${loginMember.lastLoginDate}

					</p>

				</div>

			</div>

		</section>

	</main>

	<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>