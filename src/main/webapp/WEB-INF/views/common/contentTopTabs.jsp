<%@ page language="java"
         contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<nav class="content-top-tabs"
     aria-label="ODITJI 목록 분류">

    <c:url var="allListUrl" value="/content/list">
        <c:param name="type" value="all"/>
    </c:url>

    <c:url var="popularListUrl" value="/content/list">
        <c:param name="type" value="popular"/>
    </c:url>

    <c:url var="newListUrl" value="/content/list">
        <c:param name="type" value="new"/>
    </c:url>

    <c:url var="goodsListUrl" value="/goods/list"/>

    <a class="content-top-tab ${param.activeTab eq 'all' ? 'is-active' : ''}"
       href="${allListUrl}">
        영화·시리즈
    </a>

    <a class="content-top-tab ${param.activeTab eq 'popular' ? 'is-active' : ''}"
       href="${popularListUrl}">
        인기
    </a>

    <a class="content-top-tab ${param.activeTab eq 'new' ? 'is-active' : ''}"
       href="${newListUrl}">
        신규
    </a>

    <a class="content-top-tab ${param.activeTab eq 'goods' ? 'is-active' : ''}"
       href="${goodsListUrl}">
        상품
    </a>

</nav>