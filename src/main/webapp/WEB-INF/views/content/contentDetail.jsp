<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<title>ODITJI | 콘텐츠 상세</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/content.css">

<script defer
        src="${pageContext.request.contextPath}/js/content.js"></script>
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<main class="detail-container">

<div class="back-area">
    <a href="${pageContext.request.contextPath}/content/list"
       class="back-btn">← 뒤로가기</a>
</div>

<section class="detail-header">

    <div class="detail-poster">

        <c:choose>
            <c:when test="${not empty content.posterPath}">
                <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                     alt="${content.title}">
            </c:when>
            <c:otherwise>
                <div class="no-img">NO IMAGE</div>
            </c:otherwise>
        </c:choose>

    </div>

    <div class="detail-info">

        <div class="info-box">

            <h1 class="title">${content.title}</h1>

            <p class="meta">
                ${content.contentType}
                |
                ${content.releaseDate}
                |
                ${content.ageRating}
            </p>

            <p class="genre">
                장르 : ${content.genreText}
            </p>

        </div>

        <div class="score-box">

            <span>⭐ ${content.tmdbScore}</span>
            <span>👁 ${content.viewCount}</span>

        </div>

        <div class="action-box">

            <button type="button"
                    class="btn fav-btn"
                    data-content-no="${content.contentNo}">
                ♡ 찜하기
            </button>

            <a href="${pageContext.request.contextPath}/review/list?contentNo=${content.contentNo}"
               class="btn">
                리뷰 보기
            </a>

            <a href="${pageContext.request.contextPath}/review/write?contentNo=${content.contentNo}"
               class="btn">
                리뷰 작성
            </a>

        </div>

    </div>

</section>

<section class="detail-section">

    <h2>줄거리</h2>

    <p class="overview">
        ${content.overview}
    </p>

</section>

<section class="detail-section">

    <h2>감독 / 출연</h2>

    <div class="cast-box">

        <div>
            <strong>감독</strong>
            <p>${content.director}</p>
        </div>

        <div>
            <strong>출연</strong>
            <p>${content.castNames}</p>
        </div>

    </div>

</section>

<section class="detail-section">

    <h2>시청 가능한 OTT</h2>

    <div class="ott-box">

        <c:if test="${not empty ottList}">
            <c:forEach var="ott" items="${ottList}">
                <a href="${ott.url}" target="_blank">
                    <img src="${ott.logo}" alt="OTT">
                </a>
            </c:forEach>
        </c:if>

    </div>

</section>

<section class="detail-section">

    <h2>관련 상품</h2>

    <div class="goods-grid">

        <c:if test="${not empty goodsList}">
            <c:forEach var="g" items="${goodsList}">

                <a href="${pageContext.request.contextPath}/goods/detail?goodsNo=${g.goodsNo}"
                   class="goods-card">

                    <img src="${g.image}" alt="${g.name}">

                    <div class="goods-info">
                        <p class="name">${g.name}</p>
                        <p class="price">₩ ${g.price}</p>
                    </div>

                </a>

            </c:forEach>
        </c:if>

    </div>

    <a href="${pageContext.request.contextPath}/goods/list"
       class="more-btn">
        상품 둘러보기
    </a>

</section>


<section id="reviewSection" class="detail-section">

    <h2>리뷰</h2>

    <div class="review-write-box">

        <c:choose>

            <c:when test="${not empty myReview}">

                <form action="${pageContext.request.contextPath}/review/update"
                      method="post"
                      class="review-form">

                    <input type="hidden" name="reviewNo" value="${myReview.reviewNo}">
                    <input type="hidden" name="contentNo" value="${content.contentNo}">

                    <div class="rating-box">
                        <label>평점</label>
                        <input type="number"
                               name="rating"
                               min="0"
                               max="5"
                               step="0.5"
                               value="${myReview.rating}">
                    </div>

                    <textarea name="reviewText" required>
${myReview.reviewText}
                    </textarea>

                    <button type="submit" class="btn">수정하기</button>

                </form>

            </c:when>

            <c:otherwise>

                <form action="${pageContext.request.contextPath}/review/write"
                      method="post"
                      class="review-form">

                    <input type="hidden" name="contentNo" value="${content.contentNo}">

                    <div class="rating-box">
                        <label>평점 (0 ~ 5)</label>
                        <input type="number"
                               name="rating"
                               min="0"
                               max="5"
                               step="0.5"
                               required>
                    </div>

                    <textarea name="reviewText"
                              placeholder="이 작품에 대한 리뷰를 작성하세요"
                              required></textarea>

                    <button type="submit" class="btn">등록</button>

                </form>

            </c:otherwise>

        </c:choose>

    </div>

    <div class="review-list">

        <c:choose>

            <c:when test="${not empty reviewList}">

                <c:forEach var="r" items="${reviewList}">

                    <c:if test="${r.status eq 'ACTIVE'}">

                        <div class="review-item">

                            <div class="review-meta">

                                <span class="writer">${r.writer}</span>

                                <span class="rating">
                                    ⭐ ${r.rating}
                                </span>

                                <span class="date">
                                    ${r.createdAt}
                                </span>

                            </div>

                            <p class="review-content">
                                ${r.reviewText}
                            </p>

                        </div>

                    </c:if>

                </c:forEach>

            </c:when>

            <c:otherwise>

                <div class="empty-state">
                    아직 등록된 리뷰가 없습니다
                </div>

            </c:otherwise>

        </c:choose>

    </div>

</section>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>