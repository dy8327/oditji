<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">

<title>ODITJI | 상품 상세</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/goods.css">
  <link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/component.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/favorite.css">

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/report.css">

<script>
    const contextPath = "${pageContext.request.contextPath}";
</script>

<script defer
        src="${pageContext.request.contextPath}/js/goods.js">
</script>

<script defer
        src="${pageContext.request.contextPath}/js/report.js">
</script>

<script defer
        src="${pageContext.request.contextPath}/js/favorite.js">
</script>

</head>

<body data-context-path="${pageContext.request.contextPath}">

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="goods-detail-container">

<div class="back-area">

    <a href="${pageContext.request.contextPath}/goods/list"
       class="back-btn">
        ← 목록으로
    </a>

</div>

<div class="goods-detail-layout">

<aside class="goods-detail-left-sidebar">

    <c:if test="${not empty content}">

        <section class="detail-side-card">

            <div class="detail-side-card-header">
                <h2>관련 콘텐츠</h2>
            </div>

            <div class="detail-side-card-body">

                <a class="side-content-item"
                   href="${pageContext.request.contextPath}/content/contentDetail/${content.contentNo}">

                    <div class="side-content-poster">

                        <c:choose>

                            <c:when test="${not empty content.posterPath}">

                                <img src="https://image.tmdb.org/t/p/w342${content.posterPath}"
                                     alt="<c:out value='${content.title}'/>">

                            </c:when>

                            <c:otherwise>

                                <div class="no-img">
                                    NO IMAGE
                                </div>

                            </c:otherwise>

                        </c:choose>

                    </div>

                    <div class="side-content-info">

                        <strong>
                            <c:out value="${content.title}"/>
                        </strong>

                        <span>

                            <c:out value="${content.contentType}"/>

                            <c:if test="${not empty content.tmdbScore}">
                                ·
                                ⭐
                                <fmt:formatNumber
                                    value="${content.tmdbScore}"
                                    pattern="0.0"/>
                            </c:if>

                        </span>

                    </div>

                </a>

            </div>

        </section>

    </c:if>

    <c:if test="${not empty actor}">

        <section class="detail-side-card">

            <div class="detail-side-card-header">
                <h2>관련 배우</h2>
            </div>

            <div class="detail-side-card-body">

                <div class="actor-card"
                     onclick="location.href='${pageContext.request.contextPath}/content/person/${actor.tmdbActorId}'">

                    <div class="actor-card__photo">

                        <c:choose>

                            <c:when test="${not empty actor.profilePath}">

                                <img src="https://image.tmdb.org/t/p/w185${actor.profilePath}"
                                     alt="<c:out value='${actor.actorName}'/>">

                            </c:when>

                            <c:otherwise>

                                <div class="actor-card__no-image">
                                    NO IMAGE
                                </div>

                            </c:otherwise>

                        </c:choose>

                    </div>

                    <div class="actor-card__info">

                        <strong>
                            <c:out value="${actor.actorName}"/>
                        </strong>

                        <span>
                            출연 배우
                        </span>

                    </div>

                </div>

            </div>

        </section>

    </c:if>

</aside>

<div class="goods-detail-main">

<section class="detail-header">

    <div class="detail-poster">

        <c:choose>

            <c:when test="${not empty goods.mainImage}">

                <c:choose>

                    <c:when test="${fn:startsWith(goods.mainImage, 'http://')
                                    or fn:startsWith(goods.mainImage, 'https://')}">

                        <img id="mainImage"
                             src="${goods.mainImage}"
                             alt="<c:out value='${goods.productName}'/>">

                    </c:when>

                    <c:otherwise>

                        <img id="mainImage"
                             src="${pageContext.request.contextPath}${goods.mainImage}"
                             alt="<c:out value='${goods.productName}'/>">

                    </c:otherwise>

                </c:choose>

            </c:when>

            <c:otherwise>

                <div class="no-img">
                    NO IMAGE
                </div>

            </c:otherwise>

        </c:choose>

        <c:if test="${not empty imageList}">

            <div class="image-gallery">

                <c:forEach var="img"
                           items="${imageList}">

                    <c:set var="imageUrl"
                           value="${img.imagePath}"/>

                    <c:if test="${not fn:startsWith(imageUrl, 'http://')
                                and not fn:startsWith(imageUrl, 'https://')}">

                        <c:set var="imageUrl"
                               value="${pageContext.request.contextPath}${imageUrl}"/>

                    </c:if>

                    <img src="${imageUrl}"
                         alt="<c:out value='${goods.productName}'/>"
                         class="${img.isMain eq 'Y' ? 'is-active' : ''}"
                         data-full="${imageUrl}">

                </c:forEach>

            </div>

        </c:if>

    </div>

    <div class="detail-info">

        <p class="detail-brand">
            <c:out value="${goods.businessName}"/>
        </p>

        <h1 class="detail-title">
            <c:out value="${goods.productName}"/>
        </h1>

        <div class="detail-meta">

            <span>
                <c:out value="${goods.productType}"/>
            </span>

            <span class="divider">
                |
            </span>

            <c:choose>

                <c:when test="${goods.stock <= 0}">

                    <span class="status-badge sold-out">
                        품절
                    </span>

                </c:when>

                <c:otherwise>

                    <span class="status-badge on-sale">
                        판매중
                    </span>

                </c:otherwise>

            </c:choose>

            <span class="divider">
                |
            </span>

            <span>
                재고
                <fmt:formatNumber
                    value="${goods.stock}"
                    pattern="#,###"/>개
            </span>

        </div>

        <div class="price-box">

            <c:choose>

                <c:when test="${goods.discountRate > 0}">

                    <span class="price-original">
                        ₩
                        <fmt:formatNumber
                            value="${goods.price}"
                            pattern="#,###"/>
                    </span>

                    <span class="price-final">

                        <span class="rate">
                            ${goods.discountRate}%
                        </span>

                        ₩
                        <fmt:formatNumber
                            value="${goods.discountPrice}"
                            pattern="#,###"/>

                    </span>

                </c:when>

                <c:otherwise>

                    <span class="price-final">
                        ₩
                        <fmt:formatNumber
                            value="${goods.price}"
                            pattern="#,###"/>
                    </span>

                </c:otherwise>

            </c:choose>

        </div>

        <p class="detail-desc"><c:out value="${goods.description}"/></p>

        <div class="action-box">

            <button type="button"
                    id="detailWishBtn"
                    class="btn fav-btn detail-favorite-btn${wishActive ? ' active' : ''}"
                    data-type="goods"
                    data-product-no="${goods.productNo}"
                    aria-pressed="${wishActive}"
                    aria-label="찜하기"
                    title="찜하기">

                <span class="fav-icon">

                    <c:choose>

                        <c:when test="${wishActive}">♥</c:when>

                        <c:otherwise>♡</c:otherwise>

                    </c:choose>

                </span>

                찜하기

            </button>

            <button type="button"
                    class="btn cart-btn"
                    data-product-no="${goods.productNo}"
                    ${goods.stock <= 0 ? 'disabled' : ''}>
                🛒 장바구니
            </button>

            <button type="button"
                    class="btn btn-primary buy-btn"
                    data-product-no="${goods.productNo}"
                    ${goods.stock <= 0 ? 'disabled' : ''}>
                바로 구매
            </button>

        </div>

    </div>

</section>

<section id="reviewSection"
         class="detail-section">

    <h2>
        상품 리뷰
    </h2>

    <div class="score-box">

        <c:choose>

            <c:when test="${reviewCount > 0 and not empty avgRating}">

                <span class="score-user">
                    ⭐
                    <fmt:formatNumber
                        value="${avgRating}"
                        pattern="0.0"/>
                    (${reviewCount}건)
                </span>

            </c:when>

            <c:otherwise>

                <span class="score-user">
                    아직 등록된 리뷰가 없습니다
                </span>

            </c:otherwise>

        </c:choose>

    </div>

    <div class="review-list">

        <c:choose>

            <c:when test="${not empty reviewList}">

                <c:forEach var="r"
                           items="${reviewList}">

                    <div class="review-item">

                        <div class="review-meta">

                            <span class="writer">
                                <c:out value="${r.writer}"/>
                            </span>

                            <span class="rating">
                                ⭐
                                <fmt:formatNumber
                                    value="${r.rating}"
                                    pattern="0.0"/>
                            </span>

                            <span class="date">
                                <fmt:formatDate
                                    value="${r.createdAt}"
                                    pattern="yyyy-MM-dd"/>
                            </span>

                            <c:choose>

                                <%-- [수정] 로그인 회원이 작성한 상품 리뷰에는 신고 버튼 대신 삭제 버튼을 표시한다. --%>
                                <c:when test="${not empty sessionScope.loginMember
                                                and sessionScope.loginMember.memberNo eq r.memberNo}">

                                    <div class="my-product-review-actions">

                                        <form action="${pageContext.request.contextPath}/review/deleteProductReview"
                                              method="post"
                                              class="product-review-delete-form"
                                              onsubmit="return confirmAndSubmit(event, '상품 리뷰를 삭제하시겠습니까?');">

                                            <input type="hidden"
                                                   name="reviewNo"
                                                   value="${r.reviewNo}">

                                            <%-- [수정] 상품 상세 화면에서 삭제한 경우 현재 상품 리뷰 영역으로 돌아가기 위해 전달한다. --%>
                                            <input type="hidden"
                                                   name="productNo"
                                                   value="${goods.productNo}">

                                            <button type="submit"
                                                    class="product-review-delete-btn">
                                                삭제
                                            </button>

                                        </form>

                                    </div>

                                </c:when>

                                <%-- [수정] 이미 신고한 다른 회원의 상품 리뷰에는 신고완료를 표시한다. --%>
                                <c:when test="${not empty reportedReviewSet
                                                and reportedReviewSet.contains(r.reviewNo)}">

                                    <span class="report-btn reported"
                                          aria-disabled="true">
                                        🚨 신고완료
                                    </span>

                                </c:when>

                                <%-- [수정] 본인 리뷰가 아니며 신고하지 않은 리뷰에만 신고 버튼을 표시한다. --%>
                                <c:otherwise>

                                    <button type="button"
                                            class="report-btn"
                                            data-review-type="PRODUCT"
                                            data-review-no="${r.reviewNo}"
                                            aria-label="<c:out value='${r.writer}'/>님의 리뷰 신고">
                                        🚨 신고
                                    </button>

                                </c:otherwise>

                            </c:choose>

                        </div>

                        <p class="review-content"><c:out value="${r.content}"/></p>

                    </div>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <div class="empty-state">
                    구매 후 첫 리뷰를 남겨보세요
                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>

<div id="reportModal"
     class="modal-overlay"
     hidden>

    <div class="modal-box">

        <h3>리뷰 신고</h3>

        <input type="hidden"
               id="reportReviewType"
               value="">

        <input type="hidden"
               id="reportReviewNo"
               value="">

        <div class="modal-field">

            <label for="reportReason">
                신고 사유
            </label>

            <select id="reportReason">

                <option value="욕설/비방">
                    욕설/비방
                </option>

                <option value="스팸/광고">
                    스팸/광고
                </option>

                <option value="음란물/불법정보">
                    음란물/불법정보
                </option>

                <option value="기타">
                    기타
                </option>

            </select>

        </div>

        <div class="modal-field">

            <label for="reportDetail">
                상세 내용
            </label>

            <textarea id="reportDetail"
                      maxlength="1000"
                      placeholder="신고 사유를 자세히 적어주세요"></textarea>

        </div>

        <div class="modal-actions">

            <button type="button"
                    id="reportCancelBtn"
                    class="btn">
                취소
            </button>

            <button type="button"
                    id="reportSubmitBtn"
                    class="btn btn-danger">
                신고하기
            </button>

        </div>

    </div>

</div>

</div>

<aside class="goods-detail-sidebar">

    <jsp:include page="/WEB-INF/views/common/goodsRightSidebar.jsp"/>

</aside>

</div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>

</html>
