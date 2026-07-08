<%@ page language="java"
    contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>TMDB 관리</title>
</head>
<body>

<h2>TMDB 데이터 관리</h2>

<p>
    영화 기본 데이터 적재는 Netflix, Tving, Wavve, Disney+, Watcha에 있는 영화만
    인기순으로 가져옵니다.
</p>

<form action="${pageContext.request.contextPath}/admin/tmdb/movie" method="post">
    <button type="submit">
        1. 영화 기본 데이터 적재
    </button>
</form>

<br>

<form action="${pageContext.request.contextPath}/admin/tmdb/movie/update" method="post">
    <button type="submit">
        2. 영화 상세정보 보강
    </button>
</form>

<br>

<form action="${pageContext.request.contextPath}/admin/tmdb/movie/platform" method="post">
    <button type="submit">
        3. 영화 OTT 매핑
    </button>
</form>

<br>

<hr>

<br>

<form action="${pageContext.request.contextPath}/admin/tmdb/movie/full" method="post">
    <button type="submit">
        영화 전체 데이터 구축
    </button>
</form>

<br>

<hr>

<br>

<form action="${pageContext.request.contextPath}/admin/tmdb/tv" method="post">
    <button type="submit">
        TV 데이터 적재
    </button>
</form>

<br>

<form action="${pageContext.request.contextPath}/admin/tmdb/all" method="post">
    <button type="submit">
        전체 데이터 적재
    </button>
</form>

</body>
</html>