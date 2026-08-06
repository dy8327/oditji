<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%--
    header.jsp에서 <head> 레벨 자산(CSS, 공통 스크립트, CSRF meta)만 분리한 조각입니다.

    roomList.jsp의 우측 패널/모바일 모달처럼 전체 내비게이션 바(header.jsp)를
    embed=1 모드에서 생략하는 페이지도, CSRF 토큰과 common.js(CSRF 자동 첨부,
    showAlert 등)는 반드시 로드해야 하므로 이 조각만 따로 include할 수 있게 만듭니다.

    header.jsp 자신도 이 조각을 include해서 중복 정의를 피합니다.
--%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css?v=9">

<script src="${pageContext.request.contextPath}/js/vendor/sweetalert2.all.min.js"></script>
<script defer src="${pageContext.request.contextPath}/js/common.js?v=9"></script>
<meta name="viewport" content="width=device-width, initial-scale=1.0">

<%--
    Spring Security CSRF 토큰을 공통 JavaScript에서 사용할 수 있도록 노출합니다.
    Spring Security가 GET 요청에 제공하는 _csrf request attribute를 직접 참조하여
    deferred CSRF token도 실제 값으로 확정되도록 합니다.
--%>
<meta name="_csrf" content="${_csrf.token}">
<meta name="_csrf_header" content="${_csrf.headerName}">
<meta name="_csrf_parameter" content="${_csrf.parameterName}">
