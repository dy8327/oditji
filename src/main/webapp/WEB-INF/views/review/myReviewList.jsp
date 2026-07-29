<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">

<title>ODITJI - 내가 작성한 리뷰</title>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/mypage.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/review.css">

<script defer
        src="${pageContext.request.contextPath}/js/review.js">
</script>

</head>

<body>

	<jsp:include page="/WEB-INF/views/common/header.jsp"/>

	<main class="mypage-container">

		<section class="mypage-section-header">

			<div>

				<h2>내가 작성한 리뷰</h2>

				<p>내가 작성한 콘텐츠 리뷰와 상품 리뷰를 모두 확인할 수 있습니다.</p>

			</div>

			<a href="${pageContext.request.contextPath}/member/mypage"
			   class="mypage-more">

				마이페이지로 돌아가기

			</a>

		</section>

		<section class="mypage-review">

			<c:choose>

				<c:when test="${not empty reviewList}">

					<div class="mypage-review-tabs">

						<button type="button"
								class="review-tab active"
								data-type="ALL">
							전체 (<span id="allCount">0</span>)
						</button>

						<button type="button"
								class="review-tab"
								data-type="CONTENT">
							콘텐츠 리뷰 (<span id="contentCount">0</span>)
						</button>

						<button type="button"
								class="review-tab"
								data-type="PRODUCT">
							상품 리뷰 (<span id="productCount">0</span>)
						</button>

					</div>

					<div class="mypage-review-filter-empty" style="display:none;">

						<div class="mypage-empty-icon">
							📝
						</div>

						<h3 class="mypage-filter-empty-title">
							선택한 유형의 리뷰가 없습니다.
						</h3>

						<p class="mypage-filter-empty-message">
						</p>

					</div>

					<div class="mypage-review-list">

						<c:forEach var="review" items="${reviewList}">

							<c:choose>
								<c:when test="${review.reviewType == 'PRODUCT'}">

									<c:url var="reviewLinkUrl"
										value="/goods/goodsDetail/${review.targetNo}" />

								</c:when>

								<c:otherwise>
									<c:url var="reviewLinkUrl"
										value="/content/contentDetail/${review.targetNo}" />
								</c:otherwise>
							</c:choose>

							<div class="mypage-review-card"
								data-type="${review.reviewType}">
								<%--
									상세 페이지 이동은 탐색 기능이므로 클릭 이벤트를 가진 div 대신
									기본 키보드 탐색과 링크 의미를 제공하는 a 요소를 사용한다.
								--%>
								<a class="mypage-review-clickable"
								   href="${reviewLinkUrl}"
								   style="display:block;color:inherit;text-decoration:none;">

									<div class="mypage-review-top">

										<div>

											<span class="mypage-member-type">

												<c:choose>

													<c:when test="${review.reviewType == 'PRODUCT'}">
														상품 리뷰
													</c:when>

													<c:otherwise>
														콘텐츠 리뷰
													</c:otherwise>

												</c:choose>

											</span>

											<h3>${review.title}</h3>

											<span>

												<fmt:formatDate value="${review.createdAt}"
																pattern="yyyy.MM.dd"/>

											</span>

										</div>

										<div class="mypage-review-score">

											⭐ ${review.rating}

										</div>

									</div>

									<p class="mypage-review-content">

										${review.content}

									</p>

								</a>

								<div class="mypage-review-actions">

									<c:choose>

										<c:when test="${review.reviewType == 'PRODUCT'}">

											<form action="${pageContext.request.contextPath}/review/deleteProductReview"
												method="post"
												class="mypage-review-delete-form"
												onsubmit="return confirmAndSubmit(event, '리뷰를 삭제하시겠습니까?');">

												<input type="hidden"
													name="reviewNo"
													value="${review.reviewNo}">

												<button type="submit"
														class="mypage-review-delete-btn">

													삭제

												</button>

											</form>

										</c:when>

										<c:otherwise>

											<form action="${pageContext.request.contextPath}/review/deleteContentReview"
												method="post"
												class="mypage-review-delete-form"
												onsubmit="return confirmAndSubmit(event, '리뷰를 삭제하시겠습니까?');">

												<input type="hidden"
													name="reviewNo"
													value="${review.reviewNo}">

												<button type="submit"
														class="mypage-review-delete-btn">

													삭제

												</button>

											</form>

										</c:otherwise>

									</c:choose>

								</div>

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

							마음에 드는 콘텐츠나 구매한 상품에
							첫 리뷰를 작성해보세요.

						</p>

						<button type="button"
								onclick="location.href='${pageContext.request.contextPath}/content/list'">

							콘텐츠 보러가기

						</button>

					</div>

				</c:otherwise>

			</c:choose>

		</section>

	</main>

	<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>