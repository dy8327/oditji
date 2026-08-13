<%@ tag language="java"
         pageEncoding="UTF-8"
         body-content="empty" %>

<%@ attribute name="panelKey" required="true" type="java.lang.String" %>
<%@ attribute name="workList" required="true" type="java.lang.Object" %>
<%@ attribute name="active" required="true" type="java.lang.Boolean" %>
<%@ attribute name="roleLabel" required="true" type="java.lang.String" %>
<%@ attribute name="defaultRole" required="true" type="java.lang.String" %>
<%@ attribute name="emptyMessage" required="true" type="java.lang.String" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 출연·감독·제작 참여 탭이 공유하는 필모그래피 작품 패널입니다. --%>
<section class="filmography-tab-panel${active ? ' is-active' : ''}"
         id="filmography-panel-${panelKey}"
         role="tabpanel"
         aria-labelledby="filmography-tab-${panelKey}"
         data-tab-panel="${panelKey}"
         <c:if test="${not active}">hidden</c:if>>

    <c:choose>
        <c:when test="${not empty workList}">
            <div class="filmography-grid">
                <c:forEach var="work" items="${workList}">

                    <c:url var="contentPrepareUrl" value="/content/prepare">
                        <c:param name="tmdbId" value="${work.tmdbId}"/>
                        <c:param name="contentType" value="${work.contentType}"/>
                    </c:url>

                    <a href="${contentPrepareUrl}" class="filmography-card">
                        <div class="filmography-poster">
                            <c:choose>
                                <c:when test="${not empty work.posterPath}">
                                    <img src="https://image.tmdb.org/t/p/w342${work.posterPath}"
                                         alt="${work.title}">
                                </c:when>
                                <c:otherwise>
                                    <div class="filmography-no-image">NO IMAGE</div>
                                </c:otherwise>
                            </c:choose>

                            <span class="filmography-type">${work.contentType}</span>
                        </div>

                        <div class="filmography-info">
                            <h3>${work.title}</h3>

                            <div class="filmography-meta">
                                <span>
                                    <c:choose>
                                        <c:when test="${not empty work.releaseDate}">
                                            ${work.releaseDate}
                                        </c:when>
                                        <c:otherwise>공개일 미정</c:otherwise>
                                    </c:choose>
                                </span>

                                <c:if test="${not empty work.tmdbScore}">
                                    <span>⭐ ${work.tmdbScore}</span>
                                </c:if>
                            </div>

                            <div class="filmography-role">
                                <span class="filmography-role-label">${roleLabel}</span>
                                <span class="filmography-role-value">
                                    <c:choose>
                                        <c:when test="${not empty work.participationName}">
                                            ${work.participationName}
                                        </c:when>
                                        <c:otherwise>${defaultRole}</c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>
                    </a>
                </c:forEach>
            </div>
        </c:when>

        <c:otherwise>
            <div class="empty-state">${emptyMessage}</div>
        </c:otherwise>
    </c:choose>
</section>
