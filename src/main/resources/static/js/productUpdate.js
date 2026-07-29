/* =========================================================
   콘텐츠 검색 팝업
========================================================= */
function openContentSearch() {

    window.open(
        contextPath + "/business/content/search",
        "contentSearchPopup",
        "width=900,height=720,"
            + "scrollbars=yes,resizable=yes"
    );
}


/* =========================================================
   콘텐츠 검색 팝업에서 콘텐츠 선택

   contentSearch.jsp에서 다음 함수를 호출한다.

   window.opener.selectContent(contentNo, title);
========================================================= */
function selectContent(contentNo, title) {

    const convertedContentNo =
            Number(contentNo);

    if (!Number.isFinite(convertedContentNo)
            || convertedContentNo <= 0) {

        alert("올바른 콘텐츠 번호가 아닙니다.");
        return;
    }

    document.getElementById(
        "contentNo"
    ).value = convertedContentNo;

    document.getElementById(
        "contentTitle"
    ).value = title;

    /*
     * 콘텐츠가 변경되었으므로
     * 새 콘텐츠에 연결된 배우 목록을 다시 조회한다.
     */
    loadActorsByContent(
        convertedContentNo,
        null
    );
}


/* =========================================================
   선택한 콘텐츠에 연결된 배우 조회
========================================================= */
async function loadActorsByContent(
        contentNo,
        actorNoToRestore) {

    const actorSelect =
            document.getElementById(
                "actorNo"
            );

    const actorLoadMessage =
            document.getElementById(
                "actorLoadMessage"
            );

    const convertedContentNo =
            Number(contentNo);

    if (!Number.isFinite(convertedContentNo)
            || convertedContentNo <= 0) {

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
                + "/business/api/actor/list"
                + "?contentNo="
                + encodeURIComponent(
                    convertedContentNo
                );

        console.log(
            "배우 조회 요청:",
            requestUrl
        );

        const response =
                await fetch(
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

        const actorList =
                await response.json();

        console.log(
            "배우 조회 결과:",
            actorList
        );

        renderActorList(
            actorList,
            actorNoToRestore
        );

    } catch (error) {

        console.error(
            "콘텐츠별 배우 조회 오류:",
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
        actorNoToRestore) {

    const actorSelect =
            document.getElementById(
                "actorNo"
            );

    const actorLoadMessage =
            document.getElementById(
                "actorLoadMessage"
            );

    actorSelect.innerHTML = "";

    /*
     * PRODUCT.ACTOR_NO는 NULL 허용
     */
    const emptyOption =
            document.createElement(
                "option"
            );

    emptyOption.value = "";
    emptyOption.textContent =
            "관련 배우 선택 안 함";

    actorSelect.appendChild(
        emptyOption
    );

    if (!Array.isArray(actorList)
            || actorList.length === 0) {

        const noActorOption =
                document.createElement(
                    "option"
                );

        noActorOption.value = "";

        noActorOption.textContent =
                "해당 콘텐츠에 연결된 배우가 없습니다.";

        actorSelect.appendChild(
            noActorOption
        );

        actorSelect.disabled = false;

        actorLoadMessage.textContent =
                "CONTENT_ACTOR에 연결된 배우 정보가 없습니다.";

        return;
    }

    actorList.forEach(
        function(actor) {

            const option =
                    document.createElement(
                        "option"
                    );

            /*
             * 서버에 전송되는 값은 ACTOR_NO
             */
            option.value =
                    actor.actorNo;

            let optionText =
                    actor.actorName;

            if (actor.characterName) {

                optionText +=
                        " / 배역: "
                        + actor.characterName;
            }

            option.textContent =
                    optionText;

            /*
             * 기존 상품에 저장된 배우 또는
             * 수정 실패 후 유지된 배우를 선택 상태로 복원한다.
             */
            if (actorNoToRestore
                    && String(actor.actorNo)
                        === String(
                            actorNoToRestore
                        )) {

                option.selected = true;
            }

            actorSelect.appendChild(
                option
            );
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
            document.getElementById(
                "actorNo"
            );

    const actorLoadMessage =
            document.getElementById(
                "actorLoadMessage"
            );

    actorSelect.innerHTML =
            "<option value=\"\">"
            + "콘텐츠를 먼저 선택해주세요."
            + "</option>";

    actorSelect.disabled = true;

    actorLoadMessage.textContent =
            "콘텐츠를 선택하면 해당 작품에 연결된 "
            + "배우가 표시됩니다.";
}


/* =========================================================
   이미지 파일명 출력
========================================================= */
function updateFileName(input) {

    const fileNameElement =
            document.getElementById(
                "selectedFileName"
            );

    if (input.files
            && input.files.length > 0) {

        fileNameElement.textContent =
                input.files[0].name;

    } else {

        const existingImagePath =
                document.getElementById(
                    "existingImagePath"
                ).value;

        fileNameElement.textContent =
                existingImagePath
                    ? existingImagePath
                    : "선택된 파일 없음";
    }
}


/* =========================================================
   상품 수정 폼 검증
========================================================= */
function validateProductForm() {

    const productName =
            document.getElementById(
                "productName"
            ).value.trim();

    const productType =
            document.getElementById(
                "productType"
            ).value;

    const price =
            Number(
                document.getElementById(
                    "price"
                ).value
            );

    const discountRate =
            Number(
                document.getElementById(
                    "discountRate"
                ).value
            );

    const stock =
            Number(
                document.getElementById(
                    "stock"
                ).value
            );

    const contentNo =
            document.getElementById(
                "contentNo"
            ).value;

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

        alert(
            "할인율은 0부터 100 사이로 입력해주세요."
        );

        return false;
    }

    if (!Number.isFinite(stock)
            || stock < 0) {

        alert("재고는 0개 이상 입력해주세요.");
        return false;
    }

    if (!contentNo) {

        alert("관련 콘텐츠를 선택해주세요.");
        return false;
    }

    return confirm(
        "상품 수정을 요청하시겠습니까?"
    );
}


/* =========================================================
   화면 최초 진입

   기존 상품의 CONTENT_NO를 기준으로 배우를 조회하고
   저장되어 있던 ACTOR_NO를 선택 상태로 복원한다.
========================================================= */
document.addEventListener(
    "DOMContentLoaded",
    function() {

        const contentNo =
                document.getElementById(
                    "contentNo"
                ).value;

        if (contentNo
                && Number(contentNo) > 0) {

            loadActorsByContent(
                contentNo,
                savedActorNo
            );

        } else {

            resetActorSelect();
        }
    }
);
