<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String contextPath = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My Page | ODITJI</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="stylesheet" href="<%=contextPath%>/css/oditji.css">
</head>
<body>
<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main id="mainContent" class="odi-page">
  <section class="mypage-layout">
    <aside class="mypage-panel">
      <div class="profile-box">
        <div class="avatar">O</div>
        <div>
          <strong>ODITJI User</strong>
          <p class="section-sub" style="margin:4px 0 0;">sample@oditji.local</p>
        </div>
      </div>

      <nav class="side-menu">
        <a href="#" class="active">Favorites</a>
        <a href="#">Recent Searches</a>
        <a href="#">Reviews</a>
        <a href="#">Account</a>
        <a href="<%=contextPath%>/member/login">Logout</a>
      </nav>
    </aside>

    <section class="mypage-panel">
      <div class="section-head">
        <div>
          <h1 class="section-title">My List</h1>
          <p class="section-sub">Saved content for preview.</p>
        </div>
        <button class="btn">Edit</button>
      </div>

      <article class="list-card">
        <img class="list-thumb" src="https://image.tmdb.org/t/p/w500/6UH52Fmau8RPsMAbQbjwN3wJSCj.jpg" alt="Dune">
        <div>
          <h2 class="result-title">Dune</h2>
          <div class="result-meta"><span>Movie</span><span>2024</span><span class="rating">7.2</span></div>
        </div>
        <a href="<%=contextPath%>/contents/detail/2" class="icon-btn">V</a>
      </article>

      <article class="list-card">
        <img class="list-thumb" src="https://image.tmdb.org/t/p/w500/mBaXZ95R2OxueZhvQbcEWy2DqyO.jpg" alt="Moving">
        <div>
          <h2 class="result-title">Moving</h2>
          <div class="result-meta"><span>Series</span><span>2023</span><span class="rating">8.6</span></div>
        </div>
        <a href="<%=contextPath%>/contents/detail/3" class="icon-btn">V</a>
      </article>
    </section>
  </section>
</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>
</body>
</html>
