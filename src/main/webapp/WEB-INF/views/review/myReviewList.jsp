<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="dt" uri="http://oditji.com/functions/datetime" %>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>ODITJI - 내가 작성한 리뷰</title>

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/mypage.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/review.css">

<script defer
        src="${pageContext.request.contextPath}/js/review.js">
</script>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/pagination-common.css?v=1">
    <script defer src="${pageContext.request.contextPath}/js/pagination.js?v=1"></script>
</head>

<body>

	<jsp:include page="/WEB-INF/views/common/header.jsp"/>

	<main id="mainContent" class="mypage-container mypage-review-page">

		<%-- =========================================================
		     [내가 작성한 리뷰 화면 디자인 수정]
		     기존 리뷰 조회·필터·삭제 기능과 URL은 그대로 유지하고,
		     마이페이지의 흑백 영화 배경과 어울리는 화면 전용 클래스만 추가합니다.
		========================================================= --%>

		<section class="mypage-section-header" aria-labelledby="myReviewTitle">

			<div>

				<span class="mypage-review-eyebrow">MY REVIEWS</span>

				<h2 id="myReviewTitle">내가 작성한 리뷰</h2>

				<p>내가 작성한 콘텐츠 리뷰와 상품 리뷰를 모두 확인할 수 있습니다.</p>

			</div>

			<a href="${pageContext.request.contextPath}/member/mypage"
			   class="mypage-more">

				마이페이지로 돌아가기

			</a>

		</section>

		<section class="mypage-review">

			<%-- [내가 작성한 리뷰 화면 디자인 수정]
			     아래 데이터 분기와 버튼 속성은 수정하지 않고, CSS에서 카드 디자인만 변경합니다. --%>

			<c:choose>

				<c:when test="${allReviewCount > 0}">

					<div class="mypage-review-tabs">

						<a class="review-tab ${reviewType eq 'ALL' ? 'active' : ''}"
						   href="${pageContext.request.contextPath}/review/myReviewList?type=ALL&page=1">
							전체 (${allReviewCount})
						</a>

						<a class="review-tab ${reviewType eq 'CONTENT' ? 'active' : ''}"
						   href="${pageContext.request.contextPath}/review/myReviewList?type=CONTENT&page=1">
							콘텐츠 리뷰 (${contentReviewCount})
						</a>

						<a class="review-tab ${reviewType eq 'PRODUCT' ? 'active' : ''}"
						   href="${pageContext.request.contextPath}/review/myReviewList?type=PRODUCT&page=1">
							상품 리뷰 (${productReviewCount})
						</a>

					</div>

					<div class="mypage-review-filter-empty" style="${empty reviewList ? 'display:block;' : 'display:none;'}">

						<div class="mypage-empty-icon">
							📝
						</div>

						<h3 class="mypage-filter-empty-title">
							<c:choose>
								<c:when test="${reviewType eq 'CONTENT'}">아직 작성한 콘텐츠 리뷰가 없습니다.</c:when>
								<c:when test="${reviewType eq 'PRODUCT'}">아직 작성한 상품 리뷰가 없습니다.</c:when>
								<c:otherwise>작성한 리뷰가 없습니다.</c:otherwise>
							</c:choose>
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

											<h3><c:out value="${review.title}"/></h3>

											<span>

												${dt:format(review.createdAt, 'yyyy.MM.dd')}

											</span>

										</div>

										<div class="mypage-review-score">

											⭐ ${review.rating}

										</div>

									</div>

									<%-- [수정] JSP 들여쓰기와 줄바꿈이 리뷰 내용의 빈 줄로 출력되지 않도록 한 줄로 작성 --%>
									<p class="mypage-review-content"><c:out value="${review.content}"/></p>

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

					<nav class="oditji-pagination"
						 data-pagination
						 data-current-page="${pageVO.currentPage}"
						 data-total-page="${pageVO.totalPage}"
						 data-page-param="page"
						 aria-label="내 리뷰 페이지"></nav>

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