<a href="${pageContext.request.contextPath}/content/prepare?tmdbId=${content.tmdbId}&contentType=${content.contentType}"
   class="recommend-card">

    <div class="recommend-card-thumb">

        <c:choose>

            <c:when test="${not empty content.posterPath}">

                <img src="https://image.tmdb.org/t/p/w500${content.posterPath}"
                     alt="${content.title}"
                     loading="lazy">

            </c:when>

            <c:otherwise>

                <div class="recommend-card-no-image">
                    NO IMAGE
                </div>

            </c:otherwise>

        </c:choose>

        <span class="recommend-card-type">

            <c:choose>

                <c:when test="${content.contentType eq 'MOVIE'}">
                    영화
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

            <c:if test="${not empty content.tmdbScore}">

                <span class="recommend-card-score">
                    ⭐ ${content.tmdbScore}
                </span>

            </c:if>

        </div>

        <c:if test="${not empty content.platformList}">

            <div class="recommend-card-platforms"
                 aria-label="시청 가능한 OTT">

                <c:forEach var="platform"
                           items="${content.platformList}"
                           begin="0"
                           end="2">

                    <img src="${platform.logoImage}"
                         alt="${platform.platformName}"
                         title="${platform.platformName}"
                         loading="lazy">

                </c:forEach>

                <c:if test="${content.platformList.size() > 3}">

                    <span>
                        +${content.platformList.size() - 3}
                    </span>

                </c:if>

            </div>

        </c:if>

    </div>

</a>
