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
/*
 * 화면에는 표시하지 않지만 스크린 리더가 읽을 수 있도록
 * 입력 요소에 접근 가능한 이름을 제공할 때 사용합니다.
 */
.sr-only {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border: 0;
}

/*
 * 기존 span 형태의 닫기 요소를 접근성에 적합한 button으로 변경했기 때문에
 * 브라우저 기본 버튼 스타일이 화면 디자인에 영향을 주지 않도록 초기화합니다.
 */
button.modal-close {
    border: 0;
    padding: 0;
    background: transparent;
    color: inherit;
    font: inherit;
    cursor: pointer;
}

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

                    <%--
                        검색창과 label을 for/id로 연결하여
                        키보드 사용자와 화면 낭독기가 검색 목적을 명확히 인식하도록 합니다.
                    --%>
                    <label for="eventKeyword"
                           class="sr-only">
                        사업자명 또는 이벤트명 검색
                    </label>

                    <input type="text"
                           id="eventKeyword"
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

                            <c:forEach var="req"
                                       items="${eventRequestList}">

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

                                                    <%--
                                                        빈 값이 섞여 들어오는 경우를 대비한 방어 코드입니다.
                                                    --%>
                                                    <c:if test="${not empty productItem}">

                                                        <c:set var="productParts"
                                                               value="${fn:split(productItem, '|')}"/>

                                                        <c:set var="productRate"
                                                               value="${productParts[1]}"/>

                                                        <div class="event-product-discount-line">

                                                            <c:out value="${productParts[0]}"/>
                                                            :

                                                            <c:choose>

                                                                <c:when test="${productRate > 0}">

                                                                    <fmt:formatNumber
                                                                            value="${productParts[2]}"
                                                                            pattern="#,###"/>원
                                                                    →

                                                                    <strong>
                                                                        <fmt:formatNumber
                                                                                value="${productParts[3]}"
                                                                                pattern="#,###"/>원
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
                                        <fmt:formatDate
                                                value="${req.startDate}"
                                                pattern="yyyy-MM-dd"/>
                                        ~
                                        <fmt:formatDate
                                                value="${req.endDate}"
                                                pattern="yyyy-MM-dd"/>
                                    </td>


                                    <td>
                                        <fmt:formatDate
                                                value="${req.createdAt}"
                                                pattern="yyyy-MM-dd"/>
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
                                                    '<fmt:formatDate value="${req.startDate}" pattern="yyyy-MM-dd"/> ~ <fmt:formatDate value="${req.endDate}" pattern="yyyy-MM-dd"/>',
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


<%--
    이벤트 요청 상세 및 승인·반려 팝업입니다.

    role="dialog":
    현재 영역이 일반 콘텐츠가 아닌 대화상자임을 화면 낭독기에 알립니다.

    aria-modal="true":
    팝업이 열린 동안 배경 영역과 분리된 모달임을 알립니다.

    aria-labelledby:
    팝업 제목 요소와 모달을 연결합니다.
--%>
<div class="modal-overlay"
     id="eventRequestModal"
     role="dialog"
     aria-modal="true"
     aria-labelledby="eventRequestModalTitle">


    <div class="modal-box">


        <div class="modal-header">

            <h3 id="eventRequestModalTitle">
                이벤트 요청 상세
            </h3>


            <%--
                클릭 가능한 span 대신 기본 키보드 동작을 지원하는 button을 사용합니다.

                aria-label은 화면에 표시된 닫기 기호(×)의 목적을
                화면 낭독기 사용자에게 명확하게 전달합니다.
            --%>
            <button type="button"
                    class="modal-close"
                    aria-label="이벤트 요청 상세 팝업 닫기"
                    onclick="closeModal('eventRequestModal')">
                &times;
            </button>

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

            <%--
                textarea의 id와 label의 for를 연결하여
                입력 영역의 이름을 화면 낭독기가 정확히 읽도록 합니다.
            --%>
            <label class="form-label"
                   for="reqDescription">
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


<script defer
        src="${pageContext.request.contextPath}/js/admin.js">
</script>


</body>

</html>