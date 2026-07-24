<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="ko">

<head>
<meta charset="UTF-8">

<title>ODITJI | 콘텐츠 검색</title>

<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/business.css">

<style>
.content-search-wrap {
    padding: 30px;
}

.content-search-form {
    display: flex;
    gap: 10px;
    margin-bottom: 24px;
}

.content-search-form .form-input {
    flex: 1;
}

.content-search-table {
    width: 100%;
    border-collapse: collapse;
}

.content-search-table th,
.content-search-table td {
    padding: 12px;
    border-bottom: 1px solid #ddd;
    text-align: left;
    vertical-align: middle;
}

.content-poster {
    width: 60px;
    height: 85px;
    object-fit: cover;
    border-radius: 4px;
}

.no-poster {
    width: 60px;
    height: 85px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #eeeeee;
    color: #777777;
    font-size: 11px;
    border-radius: 4px;
}

.empty-result {
    padding: 60px 0;
    text-align: center;
    color: #777777;
}
</style>

<script>
function chooseContent(button) {

    const contentNo =
            button.dataset.contentNo;

    const title =
            button.dataset.title;

    if (!window.opener
            || window.opener.closed) {

        alert(
            "상품 등록 화면을 찾을 수 없습니다."
        );

        return;
    }

    window.opener.selectContent(
        contentNo,
        title
    );

    window.close();
}
</script>

</head>

<body>

<div class="content-search-wrap">

    <h1 class="form-title">
        관련 콘텐츠 검색
    </h1>

    <form class="content-search-form"
          action="${pageContext.request.contextPath}/business/content/search"
          method="get">

        <%--
            콘텐츠 검색 input에 고유 id를 부여하고 label의 for와 연결한다.
            label은 화면에는 표시하지 않지만 스크린 리더에는 전달된다.
        --%>
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
                검색된 콘텐츠가 없습니다.
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
                                        data-content-no="${content.contentNo}"
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