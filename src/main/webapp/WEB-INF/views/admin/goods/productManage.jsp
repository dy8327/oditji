<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="product"/>
<c:set var="currentTab" value="${empty param.tab ? 'register' : param.tab}"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 상품 관리</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/common/header.jsp"/>

<div class="admin-wrap">

    <jsp:include page="/WEB-INF/views/common/adminSidebar.jsp"/>

    <main class="main-content">

        <div class="admin-page-header">

            <a href="${pageContext.request.contextPath}/admin/main" class="back-link">
                ← 뒤로가기
            </a>

            <h1 class="admin-page-title">상품 관리</h1>

            <p class="admin-page-desc">
                사업자가 요청한 상품 등록·수정·삭제 건을 확인하고 승인 또는 반려할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <%--
                관리자 승인/반려 처리 결과 메시지 출력
                기존 페이지 레이아웃과 디자인 구조는 유지한다.
            --%>
            <c:if test="${not empty message}">
                <div class="admin-message" style="margin-bottom: 20px;">
                    <c:out value="${message}"/>
                </div>
            </c:if>

            <nav class="tab-menu">
                <a href="?tab=register" class="${currentTab == 'register' ? 'active' : ''}">상품 등록</a>
                <a href="?tab=update" class="${currentTab == 'update' ? 'active' : ''}">상품 수정</a>
                <a href="?tab=delete" class="${currentTab == 'delete' ? 'active' : ''}">상품 삭제</a>
            </nav>

            <div class="toolbar">
                <form method="get" action="${pageContext.request.contextPath}/admin/product/list">
                    <input type="hidden" name="tab" value="${currentTab}">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="사업자명, 상품명 검색">
                </form>
            </div>

            <div class="card-list">

                <c:choose>

                    <c:when test="${not empty productRequestList}">

                        <c:forEach var="req" items="${productRequestList}">

                            <article class="item-card">

                                <div class="thumb">
                                    <c:choose>
                                        <c:when test="${not empty req.mainImage}">
                                            <%--
                                                DB에는 /uploads/product/... 형태의 웹 경로가 저장되므로
                                                context-path(/oditji)를 앞에 붙여 이미지를 출력한다.
                                            --%>
                                            <img src="${pageContext.request.contextPath}${req.mainImage}"
                                                 alt="<c:out value='${req.productName}'/>">
                                        </c:when>
                                        <c:otherwise>상품</c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="item-info">

                                    <h3><c:out value="${req.productName}"/></h3>

                                    <div class="meta">
                                        <span>사업자 <c:out value="${req.businessName}"/></span>
                                        <span>관련 콘텐츠 <c:out value="${req.contentTitle}"/></span>
                                        <span>가격 <c:out value="${req.price}"/>원</span>

                                        <c:choose>
                                            <c:when test="${req.status == 'WAITING'}">
                                                <span class="status-waiting">대기</span>
                                            </c:when>
                                            <c:when test="${req.status == 'APPROVED'}">
                                                <span class="status-ok">승인</span>
                                            </c:when>
                                            <%-- 상품 삭제 요청 상태 표시 추가 --%>
                                            <c:when test="${req.status == 'DELETE_REQUESTED'}">
                                                <span class="status-reject">삭제 요청</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="status-reject">반려</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>

                                    <p style="margin-top:10px; color:var(--adm-text-sub);">
                                        <c:out value="${req.description}"/>
                                    </p>

                                </div>

                                <div class="item-actions">

                                    <button type="button" class="btn btn-dark"
                                            data-product-no="${req.productNo}"
                                            data-business-name="${req.businessName}"
                                            data-product-name="${req.productName}"
                                            data-price="${req.price}"
                                            data-description="${req.description}"
                                            onclick="openProductRequestModal(this)">
                                        상세보기
                                    </button>

                                </div>

                            </article>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>
                        <article class="item-card empty">

                            <c:choose>
                                <c:when test="${currentTab == 'update'}">수정 요청 내역이 없습니다.</c:when>
                                <c:when test="${currentTab == 'delete'}">삭제 요청 내역이 없습니다.</c:when>
                                <c:otherwise>등록 요청 내역이 없습니다.</c:otherwise>
                            </c:choose>

                        </article>
                    </c:otherwise>

                </c:choose>

            </div>

            <div class="pagination">

                <a href="?tab=${currentTab}&page=${pagination.currentPage-1}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?tab=${currentTab}&page=${p}" class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?tab=${currentTab}&page=${pagination.currentPage+1}">›</a>

            </div>

        </section>

    </main>

</div>

<%-- 상품 요청 상세 / 승인·반려 팝업 --%>
<div class="modal-overlay" id="productRequestModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>상품 요청 상세</h3>
            <span class="modal-close" onclick="closeModal('productRequestModal')">&times;</span>
        </div>

        <div class="target-info-box">
            <p><span>사업자명</span><strong id="reqProductBusinessName"></strong></p>
            <p><span>상품명</span><strong id="reqProductName"></strong></p>
            <p><span>가격</span><strong id="reqProductPrice"></strong></p>
        </div>

        <div class="form-group">
            <label class="form-label">요청 내용</label>
            <textarea class="form-textarea" id="reqProductDescription" readonly></textarea>
        </div>

        <form id="productRequestForm" action="${pageContext.request.contextPath}/admin/product/approve" method="post">

            <input type="hidden" name="productNo" id="reqProductNo">
            <input type="hidden" name="tab" value="${currentTab}">

            <div class="modal-footer">
                <%--
                    삭제 탭의 승인 버튼은 PRODUCT를 실제로 최종 삭제한다.
                    다른 탭에서는 기존과 동일하게 상품 요청을 승인한다.
                --%>
                <button type="submit"
                        class="btn btn-success"
                        onclick="return confirmProductApprove();">
                    승인
                </button>

                <button type="submit"
                        formaction="${pageContext.request.contextPath}/admin/product/reject"
                        class="btn btn-danger"
                        onclick="return confirmProductReject();">
                    반려
                </button>

                <button type="button" class="btn btn-outline" onclick="closeModal('productRequestModal')">닫기</button>
            </div>

        </form>

    </div>

</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp"/>

<script>
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}

/*
 * data-* 속성을 사용하여 상품명이나 설명에 따옴표가 포함되어도
 * JavaScript 함수 호출 문자열이 깨지지 않도록 수정한다.
 */
function openProductRequestModal(button) {
    document.getElementById('reqProductNo').value = button.dataset.productNo;
    document.getElementById('reqProductBusinessName').textContent = button.dataset.businessName || '';
    document.getElementById('reqProductName').textContent = button.dataset.productName || '';
    document.getElementById('reqProductPrice').textContent = (button.dataset.price || '0') + '원';
    document.getElementById('reqProductDescription').value = button.dataset.description || '';
    document.getElementById('productRequestModal').classList.add('open');
}

function confirmProductApprove() {
    const currentTab = '${currentTab}';

    if (currentTab === 'delete') {
        return confirm('삭제 요청을 승인하면 해당 상품이 DB에서 최종 삭제됩니다. 계속하시겠습니까?');
    }

    return confirm('이 상품 요청을 승인하시겠습니까?');
}

function confirmProductReject() {
    const currentTab = '${currentTab}';

    if (currentTab === 'delete') {
        return confirm('이 상품의 삭제 요청을 반려하시겠습니까?');
    }

    return confirm('이 상품 요청을 반려하시겠습니까?');
}
</script>

</body>
</html>
