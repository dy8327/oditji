/* =========================================================
   JSONL 콘텐츠 검색 팝업
========================================================= */
function openContentSearch() {

    window.open(
        contextPath
            + "/business/content/search?mode=register",
        "contentSearchPopup",
        "width=900,height=720,"
            + "scrollbars=yes,resizable=yes"
    );
}


/* =========================================================
   JSONL 콘텐츠 검색 팝업에서 콘텐츠 선택

   contentSearch.jsp에서 다음 함수를 호출합니다.

   window.opener.selectCachedContent(
       tmdbId,
       contentType,
       title
   );
========================================================= */
function selectCachedContent(
        tmdbId,
        contentType,
        title) {

    const convertedTmdbId = Number(tmdbId);
    const normalizedContentType =
            String(contentType || "")
                .trim()
                .toUpperCase();

    if (!Number.isFinite(convertedTmdbId)
            || convertedTmdbId <= 0
            || (normalizedContentType !== "MOVIE"
                && normalizedContentType !== "TV")) {

        alert("올바른 콘텐츠 정보가 아닙니다.");
        return;
    }

    /*
     * 아직 CONTENT_NO는 생성하지 않습니다.
     * 상품 등록 요청 시 서버에서 콘텐츠 관련 6개 테이블을 저장합니다.
     */
    document.getElementById("contentNo").value = "";
    document.getElementById("tmdbId").value = convertedTmdbId;
    document.getElementById("contentType").value = normalizedContentType;
    document.getElementById("contentTitle").value = title;

    loadActorsByCachedContent(
        convertedTmdbId,
        normalizedContentType,
        null
    );
}


/* =========================================================
   선택한 JSONL 콘텐츠의 배우 미리보기 조회

   이 API는 TMDB 배우 정보만 반환하며 DB에는 저장하지 않습니다.
========================================================= */
async function loadActorsByCachedContent(
        tmdbId,
        contentType,
        actorIdToRestore) {

    const actorSelect =
            document.getElementById("tmdbActorId");

    const actorLoadMessage =
            document.getElementById("actorLoadMessage");

    const convertedTmdbId = Number(tmdbId);

    if (!Number.isFinite(convertedTmdbId)
            || convertedTmdbId <= 0
            || !contentType) {

        resetActorSelect();
        return;
    }

    actorSelect.disabled = true;
    actorSelect.innerHTML =
            "<option value=\"\">"
            + "배우 목록을 불러오는 중입니다."
            + "</option>";

    actorLoadMessage.textContent =
            "선택한 콘텐츠의 배우를 조회하고 있습니다.";

    try {

        const requestUrl =
                contextPath
                + "/business/api/content/actor-preview"
                + "?tmdbId="
                + encodeURIComponent(convertedTmdbId)
                + "&contentType="
                + encodeURIComponent(contentType);

        const response = await fetch(
            requestUrl,
            {
                method: "GET",
                headers: {
                    "Accept": "application/json"
                }
            }
        );

        if (!response.ok) {
            throw new Error(
                "배우 조회 실패: "
                + response.status
            );
        }

        const actorList = await response.json();

        renderActorList(
            actorList,
            actorIdToRestore
        );

    } catch (error) {

        console.error(
            "JSONL 콘텐츠 배우 조회 오류:",
            error
        );

        actorSelect.innerHTML =
                "<option value=\"\">"
                + "배우 목록 조회 실패"
                + "</option>";

        actorSelect.disabled = true;
        actorLoadMessage.textContent =
                "배우 정보를 불러오지 못했습니다.";
    }
}


/* =========================================================
   배우 목록 출력
========================================================= */
function renderActorList(
        actorList,
        actorIdToRestore) {

    const actorSelect =
            document.getElementById("tmdbActorId");

    const actorLoadMessage =
            document.getElementById("actorLoadMessage");

    actorSelect.innerHTML = "";

    const emptyOption =
            document.createElement("option");

    emptyOption.value = "";
    emptyOption.textContent =
            "관련 배우 선택 안 함";

    actorSelect.appendChild(emptyOption);

    if (!Array.isArray(actorList)
            || actorList.length === 0) {

        const noActorOption =
                document.createElement("option");

        noActorOption.value = "";
        noActorOption.textContent =
                "해당 콘텐츠의 배우 정보가 없습니다.";

        actorSelect.appendChild(noActorOption);
        actorSelect.disabled = false;
        actorLoadMessage.textContent =
                "TMDB에서 배우 정보를 찾지 못했습니다.";

        return;
    }

    actorList.forEach(
        function(actor) {

            const option =
                    document.createElement("option");

            /*
             * 등록 요청에는 ACTOR_NO가 아니라 TMDB_ACTOR_ID를 전달합니다.
             * 서버가 콘텐츠 저장 후 실제 ACTOR_NO로 변환합니다.
             */
            option.value = actor.tmdbActorId;

            let optionText = actor.actorName;

            if (actor.characterName) {
                optionText +=
                        " / 배역: "
                        + actor.characterName;
            }

            option.textContent = optionText;

            if (actorIdToRestore
                    && String(actor.tmdbActorId)
                        === String(actorIdToRestore)) {

                option.selected = true;
            }

            actorSelect.appendChild(option);
        }
    );

    actorSelect.disabled = false;
    actorLoadMessage.textContent =
            actorList.length
            + "명의 배우가 조회되었습니다.";
}


/* =========================================================
   배우 선택 영역 초기화
========================================================= */
function resetActorSelect() {

    const actorSelect =
            document.getElementById("tmdbActorId");

    const actorLoadMessage =
            document.getElementById("actorLoadMessage");

    actorSelect.innerHTML =
            "<option value=\"\">"
            + "콘텐츠를 먼저 선택해주세요."
            + "</option>";

    actorSelect.disabled = true;
    actorLoadMessage.textContent =
            "콘텐츠를 선택하면 해당 작품의 배우가 표시됩니다.";
}


/* =========================================================
   이미지 파일명 출력
========================================================= */
function updateFileName(input) {

    const fileNameElement =
            document.getElementById("selectedFileName");

    if (input.files
            && input.files.length > 0) {

        fileNameElement.textContent =
                input.files[0].name;

    } else {

        fileNameElement.textContent =
                "선택된 파일 없음";
    }
}


/* =========================================================
   상품 등록 폼 검증
========================================================= */
function validateProductForm() {

    const productName =
            document.getElementById("productName")
                .value.trim();

    const productType =
            document.getElementById("productType")
                .value;

    const price = Number(
        document.getElementById("price").value
    );

    const discountRate = Number(
        document.getElementById("discountRate").value
    );

    const stock = Number(
        document.getElementById("stock").value
    );

    const tmdbId = Number(
        document.getElementById("tmdbId").value
    );

    const contentType =
            document.getElementById("contentType")
                .value;

    const productImage =
            document.getElementById("productImage");

    if (!productName) {
        alert("상품명을 입력해주세요.");
        return false;
    }

    if (!productType) {
        alert("상품 종류를 선택해주세요.");
        return false;
    }

    if (!Number.isFinite(price)
            || price <= 0) {

        alert("가격은 1원 이상 입력해주세요.");
        return false;
    }

    if (!Number.isFinite(discountRate)
            || discountRate < 0
            || discountRate > 100) {

        alert("할인율은 0부터 100 사이로 입력해주세요.");
        return false;
    }

    if (!Number.isFinite(stock)
            || stock < 0) {

        alert("재고는 0개 이상 입력해주세요.");
        return false;
    }

    if (!Number.isFinite(tmdbId)
            || tmdbId <= 0
            || !contentType) {

        alert("관련 콘텐츠를 선택해주세요.");
        return false;
    }

    if (!productImage.files
            || productImage.files.length === 0) {

        alert("상품 대표 이미지를 선택해주세요.");
        return false;
    }

    return confirm(
        "상품 등록을 요청하시겠습니까?"
    );
}


/* =========================================================
   화면 최초 진입

   등록 실패 후 TMDB_ID와 CONTENT_TYPE이 남아 있으면
   배우 목록과 이전 배우 선택값을 복원합니다.
========================================================= */
document.addEventListener(
    "DOMContentLoaded",
    function() {

        const tmdbId =
                document.getElementById("tmdbId").value;

        const contentType =
                document.getElementById("contentType").value;

        if (tmdbId
                && Number(tmdbId) > 0
                && contentType) {

            loadActorsByCachedContent(
                tmdbId,
                contentType,
                savedTmdbActorId
            );

        } else {

            resetActorSelect();
        }
    }
);
