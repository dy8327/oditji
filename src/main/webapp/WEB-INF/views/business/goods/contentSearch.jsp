<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>ODITJI | 콘텐츠 검색</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<script defer
        src="${pageContext.request.contextPath}/js/business.js">
</script>

</head>

<body>

<div class="content-search-wrap">

    <h1 class="form-title">
        관련 콘텐츠 검색
    </h1>

    <c:if test="${mode == 'register'}">
        <p class="content-search-guide">
            JSONL 공용 콘텐츠 저장소에서 검색합니다.
            선택한 콘텐츠는 상품 등록 요청 시 DB에 저장됩니다.
        </p>
    </c:if>

    <form class="content-search-form"
          action="${pageContext.request.contextPath}/business/content/search"
          method="get">

        <input type="hidden"
               name="mode"
               value="<c:out value='${mode}'/>">

        <label for="contentKeyword"
               style="position:absolute;
                      width:1px;
                      height:1px;
                      padding:0;
                      margin:-1px;
                      overflow:hidden;
                      clip:rect(0, 0, 0, 0);
                      white-space:nowrap;
                      border:0;">
            관련 콘텐츠 작품명 검색
        </label>

        <input class="form-input"
               type="text"
               id="contentKeyword"
               name="keyword"
               value="<c:out value='${keyword}'/>"
               placeholder="작품명을 입력하세요">

        <button class="btn btn-dark"
                type="submit">
            검색
        </button>

    </form>

    <c:choose>

        <c:when test="${empty contentList}">

            <div class="empty-result">
                <c:choose>
                    <c:when test="${mode == 'register' and empty keyword}">
                        작품명을 입력하면 JSONL 콘텐츠를 검색합니다.
                    </c:when>
                    <c:otherwise>
                        검색된 콘텐츠가 없습니다.
                    </c:otherwise>
                </c:choose>
            </div>

        </c:when>

        <c:otherwise>

            <table class="content-search-table">

                <thead>
                    <tr>
                        <th>포스터</th>
                        <th>작품명</th>
                        <th>구분</th>
                        <th>장르</th>
                        <th>선택</th>
                    </tr>
                </thead>

                <tbody>

                    <c:forEach var="content"
                               items="${contentList}">

                        <tr>

                            <td>

                                <c:choose>

                                    <c:when test="${not empty content.posterPath}">

                                        <img class="content-poster"
                                             src="https://image.tmdb.org/t/p/w200${content.posterPath}"
                                             alt="<c:out value='${content.title}'/>">

                                    </c:when>

                                    <c:otherwise>

                                        <div class="no-poster">
                                            이미지 없음
                                        </div>

                                    </c:otherwise>

                                </c:choose>

                            </td>

                            <td>
                                <c:out value="${content.title}"/>
                            </td>

                            <td>

                                <c:choose>

                                    <c:when test="${content.contentType == 'MOVIE'}">
                                        영화
                                    </c:when>

                                    <c:otherwise>
                                        TV
                                    </c:otherwise>

                                </c:choose>

                            </td>

                            <td>
                                <c:out value="${content.genreText}"/>
                            </td>

                            <td>

                                <button class="btn btn-primary"
                                        type="button"
                                        data-mode="<c:out value='${mode}'/>"
                                        data-content-no="${content.contentNo}"
                                        data-tmdb-id="${content.tmdbId}"
                                        data-content-type="<c:out value='${content.contentType}'/>"
                                        data-title="<c:out value='${content.title}'/>"
                                        onclick="chooseContent(this);">
                                    선택
                                </button>

                            </td>

                        </tr>

                    </c:forEach>

                </tbody>

            </table>

        </c:otherwise>

    </c:choose>

</div>

</body>
</html>
