<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%--
    상태(tab) 필터의 현재 선택값. param이 없으면 '전체'를 의미하는 빈 문자열로 취급한다.

    [정리됨] eventManage.jsp와 동일한 이유로 정리했다. PRODUCT 테이블에는 등록/수정
    요청을 구분하는 컬럼이 없어 예전 tab=register/update가 실질적으로 같은 WAITING
    목록을 서로 다른 메뉴처럼 보여주고 있었다. 지금은 PRODUCT.STATUS 값을 그대로
    tab으로 사용해 승인 대기 / 승인 완료 / 삭제 요청 상태를 명확하게 구분한다.
--%>
<c:set var="currentTab" value="${empty param.tab ? '' : param.tab}"/>

<%--
    검색 기준(searchType)의 현재 선택값. param이 없으면 '전체'를 의미하는 빈 문자열로 취급한다.
    memberManage.jsp의 searchType 필터와 동일한 방식.
--%>
<c:set var="currentSearchType" value="${empty param.searchType ? '' : param.searchType}"/>

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

            <h1 class="admin-page-title">
                상품 관리
            </h1>

            <p class="admin-page-desc">
                사업자가 요청한 상품 등록·수정·삭제 건을 확인하고 승인 또는 반려할 수 있습니다.
            </p>

        </div>


        <%-- 승인/반려 처리 결과 안내 (RedirectAttributes flash message). memberManage.jsp와 동일한 컴포넌트를 사용한다. --%>
        <c:if test="${not empty message}">
            <div class="admin-flash-message">${message}</div>
        </c:if>


        <%--
            상단 통계 카드.
            각 카드는 해당 상태로 바로 필터링된 목록으로 이동하는 링크이며,
            현재 선택된 상태(tab)와 일치하는 카드에는 active 클래스를 준다.
            eventManage.jsp / memberManage.jsp와 동일한 .member-stat-grid 컴포넌트를 재사용한다.
        --%>
        <div class="member-stat-grid">

            <a class="stat-card ${empty currentTab ? 'active' : ''}"
               href="?tab=">
                <span>전체 상품 요청</span>
                <strong>${productStats.totalCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'waiting' ? 'active' : ''}"
               href="?tab=waiting">
                <span>승인 대기</span>
                <strong>${productStats.waitingCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'approved' ? 'active' : ''}"
               href="?tab=approved">
                <span>승인 완료</span>
                <strong>${productStats.approvedCount}건</strong>
            </a>

            <a class="stat-card ${currentTab == 'delete' ? 'active' : ''}"
               href="?tab=delete">
                <span>삭제 요청</span>
                <strong>${productStats.deleteRequestedCount}건</strong>
            </a>

        </div>


        <section class="admin-content-box">

            <%-- 상태 탭. 위쪽 통계 카드와 같은 상태(tab) 값을 다루지만 목록 바로 위에서도 빠르게 전환할 수 있도록 제공한다. --%>
            <div class="tab-menu">

                <a class="${empty currentTab ? 'active' : ''}"
                   href="?tab=&searchType=${currentSearchType}&keyword=${param.keyword}">
                    전체
                </a>

                <a class="${currentTab == 'waiting' ? 'active' : ''}"
                   href="?tab=waiting&searchType=${currentSearchType}&keyword=${param.keyword}">
                    승인 대기
                </a>

                <a class="${currentTab == 'approved' ? 'active' : ''}"
                   href="?tab=approved&searchType=${currentSearchType}&keyword=${param.keyword}">
                    승인 완료
                </a>

                <a class="${currentTab == 'delete' ? 'active' : ''}"
                   href="?tab=delete&searchType=${currentSearchType}&keyword=${param.keyword}">
                    삭제 요청
                </a>

            </div>


            <div class="toolbar">

                <form method="get" action="${pageContext.request.contextPath}/admin/product/list">

                    <%-- 상태 필터. 통계 카드/탭으로도 이동할 수 있지만, 키보드·스크린 리더 사용자를 위해 select로도 동일 기능을 제공한다. --%>
                    <label for="productStatusFilter" class="sr-only">
                        상태 필터
                    </label>

                    <select id="productStatusFilter" name="tab" class="filter-select">
                        <option value=""         ${empty currentTab ? 'selected' : ''}>상태 전체</option>
                        <option value="waiting"  ${currentTab == 'waiting' ? 'selected' : ''}>승인 대기</option>
                        <option value="approved" ${currentTab == 'approved' ? 'selected' : ''}>승인 완료</option>
                        <option value="delete"   ${currentTab == 'delete' ? 'selected' : ''}>삭제 요청</option>
                    </select>

                    <%-- 검색 기준 필터. memberManage.jsp / eventManage.jsp와 동일하게 검색창 옆에 두 번째 드롭다운으로 제공한다. --%>
                    <label for="productSearchTypeFilter" class="sr-only">
                        검색 기준
                    </label>

                    <select id="productSearchTypeFilter" name="searchType" class="filter-select">
                        <option value=""        ${empty currentSearchType ? 'selected' : ''}>전체</option>
                        <option value="business" ${currentSearchType == 'business' ? 'selected' : ''}>사업자명</option>
                        <option value="product"  ${currentSearchType == 'product' ? 'selected' : ''}>상품명</option>
                    </select>

                    <%-- 검색 input과 숨김 label을 명시적으로 연결한다. --%>
                    <label for="productManageKeyword" class="sr-only">
                        사업자명 또는 상품명 검색
                    </label>

                    <input type="text"
                           id="productManageKeyword"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="사업자명, 상품명 검색">

                    <button type="submit" class="btn btn-dark search-btn">
                        검색
                    </button>

                </form>

            </div>


            <table class="data-table">

                <thead>

                    <tr>
                        <th>번호</th>
                        <th>사업자명</th>
                        <th>상품명</th>
                        <th>관련 콘텐츠</th>
                        <th>가격</th>
                        <th>할인율</th>
                        <th>재고</th>
                        <th>요청일</th>
                        <th>상태</th>
                        <th>관리</th>
                    </tr>

                </thead>


                <tbody>

                    <c:choose>

                        <c:when test="${not empty productRequestList}">

                            <c:forEach var="req" items="${productRequestList}">

                                <%--
                                    상세보기 팝업에서 쓸 값들을 미리 변수로 정리해 둔다.
                                    (요청일 문자열, 상태 한글 라벨. eventManage.jsp의 reqStartDateStr 등과 동일한 방식)
                                --%>
                                <fmt:formatDate var="reqCreatedAtStr" value="${req.createdAt}" pattern="yyyy-MM-dd"/>

                                <c:choose>
                                    <c:when test="${req.status == 'APPROVED'}">
                                        <c:set var="reqStatusLabel" value="승인"/>
                                    </c:when>
                                    <c:when test="${req.status == 'REJECTED'}">
                                        <c:set var="reqStatusLabel" value="반려"/>
                                    </c:when>
                                    <c:when test="${req.status == 'DELETE_REQUESTED'}">
                                        <c:set var="reqStatusLabel" value="삭제 요청"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="reqStatusLabel" value="대기"/>
                                    </c:otherwise>
                                </c:choose>

                                <tr>

                                    <td>${req.productNo}</td>

                                    <td>${req.businessName}</td>

                                    <td>${req.productName}</td>

                                    <td>${req.contentTitle}</td>

                                    <td><fmt:formatNumber value="${req.price}" pattern="#,##0"/>원</td>

                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty req.discountRate and req.discountRate > 0}">
                                                ${req.discountRate}%
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>

                                    <td>${req.stock}</td>

                                    <td>${reqCreatedAtStr}</td>

                                    <td>

                                        <c:choose>

                                            <c:when test="${req.status == 'APPROVED'}">
                                                <span class="status-ok">승인</span>
                                            </c:when>

                                            <c:when test="${req.status == 'REJECTED'}">
                                                <span class="status-reject">반려</span>
                                            </c:when>

                                            <c:when test="${req.status == 'DELETE_REQUESTED'}">
                                                <span class="status-reject">삭제 요청</span>
                                            </c:when>

                                            <c:otherwise>
                                                <span class="status-waiting">대기</span>
                                            </c:otherwise>

                                        </c:choose>

                                    </td>

                                    <td>

                                        <button type="button" class="btn btn-dark"
                                                data-product-no="${req.productNo}"
                                                data-business-name="${fn:escapeXml(req.businessName)}"
                                                data-product-name="${fn:escapeXml(req.productName)}"
                                                data-content-title="${fn:escapeXml(req.contentTitle)}"
                                                data-price="${req.price}"
                                                data-discount-rate="${req.discountRate}"
                                                data-stock="${req.stock}"
                                                data-created-at="${reqCreatedAtStr}"
                                                data-status="${req.status}"
                                                data-status-label="${reqStatusLabel}"
                                                data-description="${fn:escapeXml(req.description)}"
                                                onclick="openProductRequestModal(this)">
                                            상세보기
                                        </button>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                        <c:otherwise>

                            <tr>

                                <td colspan="10">

                                    <c:choose>
                                        <c:when test="${currentTab == 'waiting'}">승인 대기 중인 상품 요청이 없습니다.</c:when>
                                        <c:when test="${currentTab == 'approved'}">승인 완료된 상품이 없습니다.</c:when>
                                        <c:when test="${currentTab == 'delete'}">삭제 요청된 상품이 없습니다.</c:when>
                                        <c:otherwise>조건에 맞는 상품 요청이 없습니다.</c:otherwise>
                                    </c:choose>

                                </td>

                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>


            <div class="pagination">

                <!-- 이전 블록 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.startPage - 1}"
                class="${!pagination.prev ? 'disabled' : ''}">
                    <<
                </a>


                <!-- 이전 페이지 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.currentPage - 1}"
                class="${pagination.currentPage == 1 ? 'disabled' : ''}">
                    <
                </a>


                <!-- 페이지 번호 -->
                <c:forEach var="p"
                        begin="${pagination.startPage}"
                        end="${pagination.endPage}">

                    <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${p}"
                    class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <!-- 다음 페이지 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.currentPage + 1}"
                class="${pagination.currentPage == pagination.totalPage ? 'disabled' : ''}">
                    >
                </a>


                <!-- 다음 블록 -->
                <a href="?tab=${currentTab}&searchType=${currentSearchType}&keyword=${param.keyword}&page=${pagination.endPage + 1}"
                class="${!pagination.next ? 'disabled' : ''}">
                    >>
                </a>

            </div>

        </section>

    </main>

</div>


<%--
    상품 요청 상세 / 승인·반려 팝업입니다. eventManage.jsp의 상세 팝업과 동일한 구조(role="dialog",
    aria-modal, target-info-box)를 사용한다.
--%>
<div class="modal-overlay"
     id="productRequestModal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="productRequestModalTitle">

    <div class="modal-box">

        <div class="modal-header">

            <h3 id="productRequestModalTitle">상품 요청 상세</h3>

            <button type="button"
                    class="modal-close"
                    aria-label="상품 요청 상세 팝업 닫기"
                    onclick="closeModal('productRequestModal')">
                &times;
            </button>

        </div>

        <div class="target-info-box">
            <p><span>사업자명</span><strong id="reqProductBusinessName"></strong></p>
            <p><span>상품명</span><strong id="reqProductName"></strong></p>
            <p><span>관련 콘텐츠</span><strong id="reqProductContentTitle"></strong></p>
            <p><span>가격</span><strong id="reqProductPrice"></strong></p>
            <p><span>할인율</span><strong id="reqProductDiscountRate"></strong></p>
            <p><span>재고</span><strong id="reqProductStock"></strong></p>
            <p><span>요청일</span><strong id="reqProductCreatedAt"></strong></p>
            <p><span>상태</span><strong id="reqProductStatus"></strong></p>
        </div>

        <div class="form-group">
            <label class="form-label" for="reqProductDescription">요청 내용</label>
            <textarea class="form-textarea" id="reqProductDescription" readonly></textarea>
        </div>

        <form id="productRequestForm" action="${pageContext.request.contextPath}/admin/product/approve" method="post">

            <input type="hidden" name="productNo" id="reqProductNo">
            <input type="hidden" name="tab" value="${currentTab}">
            <input type="hidden" name="searchType" value="${currentSearchType}">
            <input type="hidden" name="keyword" value="${param.keyword}">
            <input type="hidden" name="page" value="${pagination.currentPage}">
            <input type="hidden" name="status" id="reqProductStatusRaw">

            <%--
                이미 승인/반려 처리되어 상태가 확정된 상품(APPROVED/REJECTED)은 다시 승인·반려할 수 없다.
                WAITING(등록·수정 승인 대기) 또는 DELETE_REQUESTED(삭제 요청) 상태일 때만 처리 버튼을 보여주고,
                그 외에는 eventManage.jsp와 동일하게 안내 문구만 보여준다.
                실제 표시 여부는 openProductRequestModal()이 상태값을 보고 JS로 전환한다.
            --%>
            <p id="productReadonlyNote" class="modal-readonly-note" style="display:none">
                이미 처리된 요청이라 승인·반려할 수 없습니다.
            </p>

            <div class="modal-footer" id="productRequestActions">

                <button type="submit"
                        id="productApproveBtn"
                        class="btn btn-success"
                        onclick="return confirmProductApprove();">
                    승인
                </button>

                <button type="submit"
                        id="productRejectBtn"
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

<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>

</body>
</html>
