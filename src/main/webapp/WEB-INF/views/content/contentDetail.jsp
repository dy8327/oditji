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

            <span class="score-tmdb">
                글로벌 평점 ⭐ ${content.tmdbScore}
            </span>

            <span class="score-divider">|</span>

            <span class="score-user">
                <c:choose>
                    <c:when test="${not empty avgRating}">
                        오딧지 평점 ⭐ ${avgRating} (${reviewCount}건)
                    </c:when>
                    <c:otherwise>
                        오딧지 평점 ⭐ 평점 없음
                    </c:otherwise>
                </c:choose>
            </span>

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

<section class="detail-section people-section">

    <h2>감독 / 출연</h2>

    <div class="people-group">
        <h3>감독 및 제작진</h3>

        <c:choose>
            <c:when test="${not empty directorList}">
                <div class="people-list">
                    <c:forEach var="director" items="${directorList}">
                        <c:url var="directorFilmographyUrl"
                               value="/content/person/${director.tmdbDirectorId}">
                            <c:param name="role"
                                     value="${director.directorType eq 'CREATOR' ? 'CREATOR' : 'DIRECTOR'}"/>
                        </c:url>

                        <a href="${directorFilmographyUrl}"
                           class="person-card">
                            <div class="person-profile">
                                <c:choose>
                                    <c:when test="${not empty director.profilePath}">
                                        <img src="https://image.tmdb.org/t/p/w185${director.profilePath}"
                                             alt="${director.directorName}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="person-no-image">NO IMAGE</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="person-info">
                                <strong>${director.directorName}</strong>
                                <span>
                                    <c:choose>
                                        <c:when test="${director.directorType eq 'CREATOR'}">크리에이터</c:when>
                                        <c:otherwise>감독</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="people-empty">등록된 감독 정보가 없습니다.</p>
            </c:otherwise>
        </c:choose>
    </div>

    <div class="people-group">
        <h3>출연진</h3>

        <c:choose>
            <c:when test="${not empty actorList}">
                <div class="people-list">
                    <c:forEach var="actor" items="${actorList}">
                        <c:url var="actorFilmographyUrl"
                               value="/content/person/${actor.tmdbActorId}">
                            <c:param name="role" value="ACTOR"/>
                        </c:url>

                        <a href="${actorFilmographyUrl}"
                           class="person-card">
                            <div class="person-profile">
                                <c:choose>
                                    <c:when test="${not empty actor.profilePath}">
                                        <img src="https://image.tmdb.org/t/p/w185${actor.profilePath}"
                                             alt="${actor.actorName}">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="person-no-image">NO IMAGE</div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div class="person-info">
                                <strong>${actor.actorName}</strong>
                                <span>
                                    <c:choose>
                                        <c:when test="${not empty actor.characterName}">
                                            ${actor.characterName} 역
                                        </c:when>
                                        <c:otherwise>출연</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="people-empty">등록된 출연진 정보가 없습니다.</p>
            </c:otherwise>
        </c:choose>
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

                                <c:choose>
                                    <c:when test="${reportedReviewSet.contains(r.reviewNo)}">
                                        <span class="report-btn reported" aria-disabled="true">
                                            🚨 신고완료
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <button type="button"
                                                class="report-btn"
                                                data-review-type="CONTENT"
                                                data-review-no="${r.reviewNo}"
                                                aria-label="${r.writer}님의 리뷰 신고">
                                            🚨 신고
                                        </button>
                                    </c:otherwise>
                                </c:choose>

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

<!-- ======================
     리뷰 신고 모달
====================== -->
<div id="reportModal" class="modal-overlay" hidden>

    <div class="modal-box">

        <h3>리뷰 신고</h3>

        <input type="hidden" id="reportReviewType" value="">
        <input type="hidden" id="reportReviewNo" value="">

        <div class="modal-field">
            <label for="reportReason">신고 사유</label>
            <select id="reportReason">
                <option value="욕설/비방">욕설/비방</option>
                <option value="스팸/광고">스팸/광고</option>
                <option value="도배">도배</option>
                <option value="음란물/불법정보">음란물/불법정보</option>
                <option value="기타">기타</option>
            </select>
        </div>

        <div class="modal-field">
            <label for="reportDetail">상세 내용</label>
            <textarea id="reportDetail"
                      maxlength="1000"
                      placeholder="신고 사유를 자세히 적어주세요"></textarea>
        </div>

        <div class="modal-actions">
            <button type="button" id="reportCancelBtn" class="btn">취소</button>
            <button type="button" id="reportSubmitBtn" class="btn btn-danger">신고하기</button>
        </div>

    </div>

</div>

</main>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

</body>
</html>