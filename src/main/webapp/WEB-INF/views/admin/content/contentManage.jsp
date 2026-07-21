<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="activeMenu" value="content"/>

<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<title>ODITJI | 콘텐츠 관리</title>
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

            <h1 class="admin-page-title">콘텐츠 관리</h1>

            <p class="admin-page-desc">
                등록된 콘텐츠를 조회하고 정보를 수정하거나 제공 OTT 플랫폼을 관리할 수 있습니다.
            </p>

        </div>

        <section class="admin-content-box">

            <div class="toolbar">

                <form method="get" action="${pageContext.request.contextPath}/admin/content/list">
                    <input type="text" class="page-search" name="keyword"
                           value="${param.keyword}" placeholder="콘텐츠명 검색">
                </form>

                <button type="button" class="btn btn-primary" onclick="openPlatformRegisterModal()">
                    플랫폼 등록
                </button>

            </div>

            <div class="card-list">

                <c:choose>

                    <c:when test="${not empty contentList}">

                        <c:forEach var="content" items="${contentList}">

                            <article class="item-card">

                                <div class="thumb">
                                    <c:choose>
                                        <c:when test="${not empty content.posterPath}">
                                            <img src="${content.posterPath}" alt="${content.title}">
                                        </c:when>
                                        <c:otherwise>포스터</c:otherwise>
                                    </c:choose>
                                </div>

                                <div class="item-info">
                                    <h3>${content.title}</h3>
                                    <div class="meta">
                                        <span>${content.releaseDate} | ${content.platformNames}</span>
                                        <span>평점 ${content.tmdbScore}</span>
                                        <span>출연진 ${content.castNames}</span>
                                    </div>
                                </div>

                                <div class="item-actions">

                                    <button type="button" class="btn btn-dark"
                                            onclick="openContentEditModal(
                                                '${content.contentNo}',
                                                '${content.title}',
                                                '${content.contentType}',
                                                '${content.genreText}',
                                                '${content.castNames}',
                                                '${content.overview}',
                                                '${content.runtime}',
                                                '${content.releaseDate}'
                                            )">
                                        정보 수정
                                    </button>

                                    <button type="button" class="btn btn-outline" onclick="openPlatformManageModal()">
                                        플랫폼 관리
                                    </button>

                                </div>

                            </article>

                        </c:forEach>

                    </c:when>

                    <c:otherwise>
                        <article class="item-card empty">등록된 콘텐츠가 없습니다.</article>
                    </c:otherwise>

                </c:choose>

            </div>

            <div class="pagination">

                <a href="?page=${pagination.currentPage-1}&keyword=${param.keyword}">‹</a>

                <c:forEach var="p" begin="1" end="${empty pagination.totalPages ? 1 : pagination.totalPages}">
                    <a href="?page=${p}&keyword=${param.keyword}"
                       class="${pagination.currentPage == p ? 'active' : ''}">${p}</a>
                </c:forEach>

                <a href="?page=${pagination.currentPage+1}&keyword=${param.keyword}">›</a>

            </div>

        </section>

    </main>

</div>

<%-- 플랫폼 등록 팝업 --%>
<div class="modal-overlay" id="platformRegisterModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>플랫폼 등록</h3>
            <span class="modal-close" onclick="closeModal('platformRegisterModal')">&times;</span>
        </div>

        <form action="${pageContext.request.contextPath}/admin/content/platform/register"
              method="post" enctype="multipart/form-data">

            <div class="form-group">
                <label class="form-label">플랫폼 이름</label>
                <input type="text" name="platformName" class="form-input" required>
            </div>

            <div class="form-group">
                <label class="form-label">사이트 URL</label>
                <input type="text" name="siteUrl" class="form-input" placeholder="https://">
            </div>

            <div class="form-group">
                <label class="form-label">플랫폼 로고</label>
                <div class="file-box">
                    <input type="file" name="logoImage" accept="image/*">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">상태</label>
                <div class="radio-group">
                    <label><input type="radio" name="isActive" value="Y" checked>활성화</label>
                    <label><input type="radio" name="isActive" value="N">비활성화</label>
                </div>
            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">등록</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('platformRegisterModal')">취소</button>
            </div>

        </form>

    </div>

</div>

<%-- 플랫폼 관리 팝업 --%>
<div class="modal-overlay" id="platformManageModal">

    <div class="modal-box">

        <div class="modal-header">
            <h3>플랫폼 관리</h3>
            <span class="modal-close" onclick="closeModal('platformManageModal')">&times;</span>
        </div>

        <form action="${pageContext.request.contextPath}/admin/content/platform/update"
              method="post" enctype="multipart/form-data">

            <div class="form-group">
                <label class="form-label">플랫폼 이름</label>
                <select name="platformNo" class="form-select">
                    <c:forEach var="platform" items="${platformList}">
                        <option value="${platform.platformNo}">${platform.platformName}</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label class="form-label">사이트 URL</label>
                <input type="text" name="siteUrl" class="form-input" placeholder="https://">
            </div>

            <div class="form-group">
                <label class="form-label">플랫폼 로고</label>
                <div class="file-box">
                    <input type="file" name="logoImage" accept="image/*">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">상태</label>
                <div class="radio-group">
                    <label><input type="radio" name="isActive" value="Y" checked>활성화</label>
                    <label><input type="radio" name="isActive" value="N">비활성화</label>
                </div>
            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">수정</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('platformManageModal')">취소</button>
            </div>

        </form>

    </div>

</div>

<%-- 콘텐츠 수정 팝업 --%>
<div class="modal-overlay" id="contentEditModal">

    <div class="modal-box modal-wide">

        <div class="modal-header">
            <h3>콘텐츠 수정</h3>
            <span class="modal-close" onclick="closeModal('contentEditModal')">&times;</span>
        </div>

        <form id="contentEditForm" action="${pageContext.request.contextPath}/admin/content/update"
              method="post" enctype="multipart/form-data">

            <input type="hidden" name="contentNo" id="editContentNo">

            <div class="form-group">
                <label class="form-label">제목</label>
                <input type="text" name="title" id="editTitle" class="form-input">
            </div>

            <div class="form-row">

                <div class="form-group">
                    <label class="form-label">구분</label>
                    <input type="text" name="contentType" id="editContentType" class="form-input" placeholder="MOVIE / TV">
                </div>

                <div class="form-group">
                    <label class="form-label">장르</label>
                    <input type="text" name="genreText" id="editGenreText" class="form-input">
                </div>

            </div>

            <div class="form-group">
                <label class="form-label">출연진</label>
                <input type="text" name="castNames" id="editCastNames" class="form-input">
            </div>

            <div class="form-group">
                <label class="form-label">줄거리</label>
                <textarea name="overview" id="editOverview" class="form-textarea"></textarea>
            </div>

            <div class="form-row">

                <div class="form-group">
                    <label class="form-label">런타임(분)</label>
                    <input type="text" name="runtime" id="editRuntime" class="form-input">
                </div>

                <div class="form-group">
                    <label class="form-label">출시연도</label>
                    <input type="text" name="releaseDate" id="editReleaseDate" class="form-input">
                </div>

            </div>

            <div class="form-group">
                <label class="form-label">작품 이미지</label>
                <div class="file-box">
                    <input type="file" name="posterImage" accept="image/*">
                </div>
            </div>

            <div class="form-group">
                <label class="form-label">제공 OTT</label>
                <div class="checkbox-group">
                    <label><input type="checkbox" name="platformNos" value="1">넷플릭스</label>
                    <label><input type="checkbox" name="platformNos" value="4">디즈니플러스</label>
                    <label><input type="checkbox" name="platformNos" value="6">쿠팡플레이</label>
                    <label><input type="checkbox" name="platformNos" value="2">티빙</label>
                    <label><input type="checkbox" name="platformNos" value="5">왓챠</label>
                    <label><input type="checkbox" name="platformNos" value="3">웨이브</label>
                </div>
            </div>

            <div class="modal-footer">
                <button type="submit" class="btn btn-primary">콘텐츠 수정</button>
                <button type="button" class="btn btn-outline" onclick="closeModal('contentEditModal')">취소</button>
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
