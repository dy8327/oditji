<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- 검색 결과의 전체/상품 탭이 공통으로 사용하는 상품 카드 목록입니다. --%>
<section class="card-list">
                                <c:forEach var="goods" items="${searchGoodsItems}">
                                    <article class="card-item${goods.stock <= 0 ? ' is-soldout' : ''}">
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

                                        <a class="card-link"
                                           href="${pageContext.request.contextPath}/goods/goodsDetail/${goods.productNo}">
                                            <div class="card-poster">
                                                <!-- ⭕ 할인 뱃지를 포스터 이미지 왼쪽 위로 이동 -->
                                                <c:if test="${goods.discountRate > 0}">
                                                    <span class="discount-badge">할인</span>
                                                </c:if>

                                                <c:choose>
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
                                </c:forEach>
                            </section>
