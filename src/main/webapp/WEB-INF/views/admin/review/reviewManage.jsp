<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:set var="reviewPageTitle" value="콘텐츠 리뷰 관리" scope="request"/>
<c:set var="reviewPageDescription" value="작품(콘텐츠)에 등록된 전체 리뷰를 조회하고, 신고 접수된 리뷰를 확인하여 승인(리뷰 삭제) 또는 반려 처리할 수 있습니다. 여러 건을 한 번에 선택해 일괄 처리할 수도 있습니다." scope="request"/>
<c:set var="reviewPageStats" value="${reviewStats}" scope="request"/>
<c:set var="reviewItems" value="${reviewList}" scope="request"/>
<c:set var="reviewBasePath" value="/admin/review" scope="request"/>
<c:set var="reviewIdPrefix" value="contentReview" scope="request"/>
<c:set var="reviewTargetSearchValue" value="content" scope="request"/>
<c:set var="reviewTargetSearchLabel" value="콘텐츠명" scope="request"/>
<c:set var="reviewTargetColumnLabel" value="콘텐츠" scope="request"/>
<c:set var="reviewKind" value="CONTENT" scope="request"/>
<c:set var="reviewDeleteFunction" value="deleteContentReview" scope="request"/>

<jsp:include page="/WEB-INF/views/admin/review/reviewManageTemplate.jsp"/>
