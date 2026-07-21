<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="activeMenu" value="event"/>
<c:set var="currentTab" value="${empty param.tab ? 'register' : param.tab}"/>

<!DOCTYPE html>
<html lang="ko">

<head>

<meta charset="UTF-8">
<title>ODITJI | 이벤트 관리</title>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin.css">

<style>
.event-product-discount-line {
    margin-bottom: 4px;
}

.event-product-discount-line:last-child {
    margin-bottom: 0;
}
</style>

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
                이벤트 관리
            </h1>

            <p class="admin-page-desc">
                사업자가 요청한 이벤트 등록·수정·연장 건을 확인하고 승인 또는 반려할 수 있습니다.
                이벤트는 종료일이 지나면 자동으로 삭제됩니다.
            </p>

        </div>


        <section class="admin-content-box">

            <nav class="tab-menu">

                <a href="?tab=waiting"
                class="${currentTab == 'waiting' ? 'active' : ''}">
                    승인 대기
                </a>

                <a href="?tab=approved"
                class="${currentTab == 'approved' ? 'active' : ''}">
                    승인 완료
                </a>

                <a href="?tab=end"
                class="${currentTab == 'end' ? 'active' : ''}">
                    종료 이벤트
                </a>

            </nav>


            <div class="toolbar">

                <form method="get"
                      action="${pageContext.request.contextPath}/admin/event/list">

                    <input type="hidden"
                           name="tab"
                           value="${currentTab}">

                    <input type="text"
                           class="page-search"
                           name="keyword"
                           value="${param.keyword}"
                           placeholder="사업자명, 이벤트명 검색">

                </form>

            </div>


            <table class="data-table">

                <thead>

                    <tr>

                        <th>번호</th>
                        <th>사업자명</th>
                        <th>이벤트명</th>
                        <th>연결 상품</th>
                        <th>할인 적용가</th>

                        <th>

                            <c:choose>

                                <c:when test="${currentTab == 'extend'}">
                                    연장 기간
                                </c:when>

                                <c:otherwise>
                                    이벤트 기간
                                </c:otherwise>

                            </c:choose>

                        </th>

                        <th>요청일</th>
                        <th>처리 상태</th>
                        <th>관리</th>

                    </tr>

                </thead>


                <tbody>

                    <c:choose>

                        <c:when test="${not empty eventRequestList}">

                            <c:forEach var="req" items="${eventRequestList}">

                                <tr>

                                    <td>${req.eventNo}</td>

                                    <td>${req.businessName}</td>

                                    <td>${req.title}</td>

                                    <td>${req.productName}</td>


                                    <td>

                                        <c:choose>

                                            <c:when test="${not empty req.productDetail}">

                                                <c:forEach var="productItem"
                                                           items="${fn:split(req.productDetail, ';')}"
                                                           varStatus="productStatus">

                                                    <%-- 빈 값이 섞여 들어오는 경우를 대비한 방어 코드 --%>
                                                    <c:if test="${not empty productItem}">

                                                        <c:set var="productParts"
                                                               value="${fn:split(productItem, '|')}"/>

                                                        <c:set var="productRate" value="${productParts[1]}"/>

                                                        <div class="event-product-discount-line">

                                                            <c:out value="${productParts[0]}"/>
                                                            :

                                                            <c:choose>

                                                                <c:when test="${productRate > 0}">
                                                                    <fmt:formatNumber value="${productParts[2]}" pattern="#,###"/>원
                                                                    →
                                                                    <strong>
                                                                        <fmt:formatNumber value="${productParts[3]}" pattern="#,###"/>원
                                                                    </strong>
                                                                    (${productRate}%)
                                                                </c:when>

                                                                <c:otherwise>
                                                                    할인 없음
                                                                </c:otherwise>

                                                            </c:choose>

                                                        </div>

                                                    </c:if>

                                                </c:forEach>

                                            </c:when>

                                            <c:otherwise>
                                                할인 없음
                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <td>
                                        ${req.startDate} ~ ${req.endDate}
                                    </td>


                                    <td>
                                        <fmt:formatDate value="${req.createdAt}" pattern="yyyy-MM-dd"/>
                                    </td>


                                    <td>

                                        <c:choose>

                                            <c:when test="${req.status == 'APPROVED'}">

                                                <span class="status-ok">
                                                    승인
                                                </span>

                                            </c:when>


                                            <c:when test="${req.status == 'REJECTED'}">

                                                <span class="status-reject">
                                                    반려
                                                </span>

                                            </c:when>


                                            <c:otherwise>

                                                <span class="status-waiting">
                                                    대기
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <td>

                                        <button type="button"
                                                class="btn btn-dark"
                                                onclick="openEventRequestModal(
                                                    '${req.eventNo}',
                                                    '${req.businessName}',
                                                    '${req.title}',
                                                    '${req.startDate} ~ ${req.endDate}',
                                                    '${req.productDetail}'
                                                )">
                                            상세보기
                                        </button>

                                    </td>

                                </tr>

                            </c:forEach>

                        </c:when>

                                                <c:otherwise>

                            <tr>

                                <td colspan="9">

                                    <c:choose>

                                        <c:when test="${currentTab == 'approved'}">
                                            승인 완료된 이벤트가 없습니다.
                                        </c:when>

                                        <c:when test="${currentTab == 'end'}">
                                            종료된 이벤트가 없습니다.
                                        </c:when>

                                        <c:otherwise>
                                            승인 대기 이벤트가 없습니다.
                                        </c:otherwise>

                                    </c:choose>

                                </td>

                            </tr>

                        </c:otherwise>

                    </c:choose>

                </tbody>

            </table>


            <div class="pagination">

                <a href="?tab=${currentTab}&page=${pagination.currentPage-1}">
                    ‹
                </a>


                <c:forEach var="p"
                           begin="1"
                           end="${empty pagination.totalPages ? 1 : pagination.totalPages}">

                    <a href="?tab=${currentTab}&page=${p}"
                       class="${pagination.currentPage == p ? 'active' : ''}">
                        ${p}
                    </a>

                </c:forEach>


                <a href="?tab=${currentTab}&page=${pagination.currentPage+1}">
                    ›
                </a>

            </div>


        </section>

    </main>

</div>


<%-- 이벤트 요청 상세 / 승인·반려 팝업 --%>

<div class="modal-overlay"
     id="eventRequestModal">


    <div class="modal-box">


        <div class="modal-header">

            <h3 id="eventRequestModalTitle">
                이벤트 요청 상세
            </h3>


            <span class="modal-close"
                  onclick="closeModal('eventRequestModal')">
                &times;
            </span>

        </div>


        <div class="target-info-box">


            <p>

                <span>
                    사업자명
                </span>

                <strong id="reqBusinessName"></strong>

            </p>


            <p>

                <span>
                    이벤트명
                </span>

                <strong id="reqtitle"></strong>

            </p>


            <p>

                <span>
                    이벤트 기간
                </span>

                <strong id="reqEventPeriod"></strong>

            </p>


        </div>


        <div class="form-group">

            <label class="form-label">
                요청 내용
            </label>


            <textarea class="form-textarea"
                      id="reqDescription"
                      readonly></textarea>

        </div>


        <form id="eventRequestForm"
              action="${pageContext.request.contextPath}/admin/event/approve"
              method="post">


            <input type="hidden"
                   name="eventNo"
                   id="reqeventNo">


            <input type="hidden"
                   name="tab"
                   value="${currentTab}">


            <div class="modal-footer">


                <button type="submit"
                        class="btn btn-success">
                    승인
                </button>


                <button type="submit"
                        formaction="${pageContext.request.contextPath}/admin/event/reject"
                        class="btn btn-danger">
                    반려
                </button>


                <button type="button"
                        class="btn btn-outline"
                        onclick="closeModal('eventRequestModal')">
                    닫기
                </button>


            </div>


        </form>


    </div>


</div>


<jsp:include page="/WEB-INF/views/common/footer.jsp"/>


<script>

function closeModal(id) {

    document.getElementById(id).classList.remove('open');

}


function openEventRequestModal(eventNo, businessName, title, period, productDetail) {

    document.getElementById('reqeventNo').value = eventNo;

    document.getElementById('reqBusinessName').textContent = businessName;

    document.getElementById('reqtitle').textContent = title;

    document.getElementById('reqEventPeriod').textContent = period;

    document.getElementById('reqDescription').value = formatProductDetail(productDetail);

    document.getElementById('eventRequestModal').classList.add('open');

}

/*
 * 상품별 상세 내역 파싱
 *
 * productDetail 형식: "상품명|할인율|가격|할인적용가;상품명|할인율|가격|할인적용가;..."
 * (adminMapper.xml selectAdminEventList의 productDetail 컬럼)
 */
function formatProductDetail(productDetail) {

    if (!productDetail) {

        return '연결된 상품이 없습니다.';

    }

    const items = productDetail.split(';').filter(Boolean);

    if (items.length === 0) {

        return '연결된 상품이 없습니다.';

    }

    const lines = [];

    items.forEach(function (item) {

        const parts = item.split('|');

        const name = parts[0];
        const rate = Number(parts[1]);
        const price = Number(parts[2]);
        const discounted = Number(parts[3]);

        if (!name) {
            return;
        }

        if (rate > 0) {

            lines.push(
                name
                + ' : '
                + price.toLocaleString()
                + '원 → '
                + discounted.toLocaleString()
                + '원 (' + rate + '% 할인)'
            );

        } else {

            lines.push(name + ' : 할인 없음');

        }

    });

    return lines.join('\n');

}

</script>


</body>

</html>