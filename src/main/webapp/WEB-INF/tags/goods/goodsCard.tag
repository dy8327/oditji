<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="goods"
              required="true"
              type="java.lang.Object" %>
<%@ attribute name="favoriteMode"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="extraClass"
              required="false"
              type="java.lang.String" %>
<%@ attribute name="imageFallback"
              required="false"
              type="java.lang.Boolean" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%--
    검색 결과 상품 목록(goodsCardList.jsp)과 찜 목록 상품 탭(favoriteList.jsp)이
    공유하는 상품 카드이다.
    favoriteMode: "toggle"(기본, goodsCardList.jsp) / "remove"(favoriteList.jsp).
    - toggle: wishedProductNoSet 포함 여부로 ♡/♥ 상태를 판별하고 클릭 시 토글한다.
    - remove: 항상 찜한 상태(active, ♥)이며 "찜 해제"만 존재한다.
    favoriteMode="toggle"일 때는 호출하는 페이지 스코프에 wishedProductNoSet EL 변수가
    이미 채워져 있다는 전제로 태그 안에서 그대로 참조한다(페이지 스코프 EL 변수는 태그
    내부에서도 접근 가능).
    extraClass: article에 덧붙일 클래스 (예: favoriteList.jsp의 favorite-card)
    imageFallback: true면 이미지 로드 실패 시 NO IMAGE로 전환하는 fallback 마크업
    (goods.js의 .goods-card-image[data-fallback-target] error 핸들러가 참조)을 포함한다.
    기본 false(기존 goodsCardList.jsp/favoriteList.jsp 동작 그대로, fallback 없음).
    goodsList.jsp(상품 목록)만 true로 사용한다.
--%>
<c:set var="resolvedFavoriteMode" value="${empty favoriteMode ? 'toggle' : favoriteMode}" />
<c:set var="resolvedExtraClass" value="${empty extraClass ? '' : ' '.concat(extraClass)}" />
<c:set var="resolvedImageFallback" value="${empty imageFallback ? false : imageFallback}" />

<article class="card-item${resolvedExtraClass}${goods.stock <= 0 ? ' is-soldout' : ''}">

    <c:choose>
        <c:when test="${resolvedFavoriteMode eq 'remove'}">
            <button type="button"
                    class="fav-btn card-favorite-btn active"
                    data-type="goods"
                    data-product-no="${goods.productNo}"
                    aria-pressed="true"
                    aria-label="<c:out value='${goods.productName}'/> 찜 해제"
                    title="찜 해제">
                ♥
            </button>
        </c:when>
        <c:otherwise>
            <button type="button"
                    class="fav-btn card-favorite-btn${wishedProductNoSet.contains(goods.productNo) ? ' active' : ''}"
                    data-type="goods"
                    data-product-no="${goods.productNo}"
                    aria-pressed="${wishedProductNoSet.contains(goods.productNo)}"
                    aria-label="<c:out value='${goods.productName}'/> 찜하기"
                    title="찜하기">
                <c:choose>
                    <c:when test="${wishedProductNoSet.contains(goods.productNo)}">♥</c:when>
                    <c:otherwise>♡</c:otherwise>
                </c:choose>
            </button>
        </c:otherwise>
    </c:choose>

    <a class="card-link"
       href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">
        <div class="card-poster">
            <!-- ⭕ 할인 뱃지를 포스터 이미지 왼쪽 위로 이동 -->
            <c:if test="${goods.discountRate > 0}">
                <span class="discount-badge">할인</span>
            </c:if>

            <c:choose>
                <c:when test="${not empty goods.mainImage and resolvedImageFallback}">
                    <img class="goods-card-image"
                         src="${pageContext.request.contextPath}${goods.mainImage}"
                         alt="<c:out value='${goods.productName}'/>"
                         loading="lazy"
                         data-fallback-target="goodsImageFallback-${goods.productNo}">

                    <div class="no-img"
                         id="goodsImageFallback-${goods.productNo}"
                         style="display:none;">
                        NO IMAGE
                    </div>
                </c:when>
                <c:when test="${not empty goods.mainImage}">
                    <img src="${pageContext.request.contextPath}${goods.mainImage}"
                         alt="<c:out value='${goods.productName}'/>"
                         loading="lazy">
                </c:when>
                <c:otherwise>
                    <div class="no-img">NO IMAGE</div>
                </c:otherwise>
            </c:choose>

            <c:if test="${goods.stock <= 0}">
                <div class="soldout-badge">SOLD OUT</div>
            </c:if>
        </div>

        <div class="card-info">
            <p class="card-brand">
                <c:choose>
                    <c:when test="${not empty goods.businessName}">
                        <c:out value="${goods.businessName}"/>
                    </c:when>
                    <c:otherwise>판매자 정보 없음</c:otherwise>
                </c:choose>
            </p>

            <!-- ⭕ 뱃지 삭제 후 상품 제목 100% 확보 -->
            <div class="card-title-wrap">
                <h3 class="card-title"><c:out value="${goods.productName}"/></h3>
            </div>

            <!-- ⭕ 가격 영역 정렬 개선 -->
            <div class="card-meta">
                <c:choose>
                    <c:when test="${goods.discountRate > 0}">
                        <span class="price-original">
                            ₩<fmt:formatNumber value="${goods.price}" pattern="#,###"/>
                        </span>
                        <div class="price-sale-row">
                            <span class="rate">${goods.discountRate}%</span>
                            <span class="price-final">
                                ₩<fmt:formatNumber value="${goods.discountPrice}" pattern="#,###"/>
                            </span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <span class="price-final">
                            ₩<fmt:formatNumber value="${goods.price}" pattern="#,###"/>
                        </span>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="card-sub">
                <c:choose>
                    <c:when test="${goods.stock <= 0}">
                        <span class="stock-warning">품절</span>
                    </c:when>
                    <c:when test="${goods.stock <= 5}">
                        <span class="stock-warning">재고 ${goods.stock}개 남음</span>
                    </c:when>
                    <c:otherwise>재고 ${goods.stock}개</c:otherwise>
                </c:choose>
            </div>
        </div>
    </a>

</article>
