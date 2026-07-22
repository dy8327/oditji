<%--
    추천 콘텐츠 카드 공통 JSP

    변경 사항:
    - UTF-8 환경에 따라 깨질 수 있는 별 이모지(⭐)를
      HTML 숫자 엔티티 &#11088;로 변경했습니다.
    - 브라우저가 HTML 엔티티를 실제 별 문자로 변환하므로
      JSP 파일 저장 인코딩과 무관하게 안정적으로 표시됩니다.
--%>

<a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&contentType=${content.contentType}"
   class="recommend-card">

    <div class="recommend-card-thumb">

        <c:choose>

            <%-- 포스터 이미지가 존재하는 콘텐츠 --%>
            <c:when test="${not empty content.posterPath}">

                <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                     alt="${content.title}"
                     loading="lazy">

            </c:when>

            <%-- 포스터 이미지가 없는 콘텐츠 --%>
            <c:otherwise>

                <div class="recommend-card-no-image">
                    NO IMAGE
                </div>

            </c:otherwise>

        </c:choose>

        <%-- 영화와 TV 콘텐츠 유형 표시 --%>
        <span class="recommend-card-type">

            <c:choose>

                <c:when test="${content.contentType eq 'MOVIE'}">
                    &#50689;&#54868;
                </c:when>

                <c:otherwise>
                    TV
                </c:otherwise>

            </c:choose>

        </span>

    </div>

    <div class="recommend-card-info">

        <h3>
            ${content.title}
        </h3>

        <div class="recommend-card-meta">

            <%-- 공개일 표시 --%>
            <span>

                <c:choose>

                    <c:when test="${not empty content.releaseDate}">
                        ${content.releaseDate}
                    </c:when>

                    <c:otherwise>
                        공개일 미정
                    </c:otherwise>

                </c:choose>

            </span>

            <%--
                TMDB 평점 표시

                별 이모지를 소스에 직접 넣지 않고
                HTML 숫자 엔티티로 작성하여 문자 깨짐을 방지합니다.
            --%>
            <c:if test="${not empty content.tmdbScore}">

                <span class="recommend-card-score">
                    <span aria-hidden="true">&#11088;</span>
                    ${content.tmdbScore}
                </span>

            </c:if>

        </div>

        <%-- 콘텐츠를 시청할 수 있는 OTT 플랫폼 표시 --%>
        <c:if test="${not empty content.platformList}">

            <div class="recommend-card-platforms"
                 aria-label="시청 가능한 OTT">

                <%-- 카드에는 최대 3개의 OTT 로고만 표시 --%>
                <c:forEach var="platform"
                           items="${content.platformList}"
                           begin="0"
                           end="2">

                    <img src="${platform.logoImage}"
                         alt="${platform.platformName}"
                         title="${platform.platformName}"
                         loading="lazy">

                </c:forEach>

                <%-- OTT가 4개 이상이면 나머지 개수를 표시 --%>
                <c:if test="${content.platformList.size() > 3}">

                    <span>
                        +${content.platformList.size() - 3}
                    </span>

                </c:if>

            </div>

        </c:if>

    </div>

</a>