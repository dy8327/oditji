/* =========================================================
   JSONL 콘텐츠 검색 팝업
========================================================= */
function openContentSearch() {
  window.open(contextPath + "/business/content/search?mode=register", "contentSearchPopup", "width=900,height=720," + "scrollbars=yes,resizable=yes");
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
function selectCachedContent(tmdbId, contentType, title) {
  const convertedTmdbId = Number(tmdbId);
  const normalizedContentType = String(contentType || "")
    .trim()
    .toUpperCase();

  if (!Number.isFinite(convertedTmdbId) || convertedTmdbId <= 0 || (normalizedContentType !== "MOVIE" && normalizedContentType !== "TV")) {
    showAlert("올바른 콘텐츠 정보가 아닙니다.", "warning");
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

  loadActorsByCachedContent(convertedTmdbId, normalizedContentType, null);
}

/* =========================================================
   선택한 JSONL 콘텐츠의 배우 미리보기 조회

   이 API는 TMDB 배우 정보만 반환하며 DB에는 저장하지 않습니다.
========================================================= */
async function loadActorsByCachedContent(tmdbId, contentType, actorIdToRestore) {
  const actorSelect = document.getElementById("tmdbActorId");

  const actorLoadMessage = document.getElementById("actorLoadMessage");

  const convertedTmdbId = Number(tmdbId);

  if (!Number.isFinite(convertedTmdbId) || convertedTmdbId <= 0 || !contentType) {
    resetActorSelect();
    return;
  }

  actorSelect.disabled = true;
  actorSelect.innerHTML = '<option value="">' + "배우 목록을 불러오는 중입니다." + "</option>";

  actorLoadMessage.textContent = "선택한 콘텐츠의 배우를 조회하고 있습니다.";

  try {
    const requestUrl = contextPath + "/business/api/content/actor-preview" + "?tmdbId=" + encodeURIComponent(convertedTmdbId) + "&contentType=" + encodeURIComponent(contentType);

    const response = await fetch(requestUrl, {
      method: "GET",
      headers: {
        Accept: "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("배우 조회 실패: " + response.status);
    }

    const actorList = await response.json();

    renderActorList(actorList, actorIdToRestore);
  } catch (error) {
    console.error("JSONL 콘텐츠 배우 조회 오류:", error);

    actorSelect.innerHTML = '<option value="">' + "배우 목록 조회 실패" + "</option>";

    actorSelect.disabled = true;
    actorLoadMessage.textContent = "배우 정보를 불러오지 못했습니다.";
  }
}

/* =========================================================
   배우 목록 출력
========================================================= */
function renderActorList(actorList, actorIdToRestore) {
  const actorSelect = document.getElementById("tmdbActorId");

  const actorLoadMessage = document.getElementById("actorLoadMessage");

  actorSelect.innerHTML = "";

  const emptyOption = document.createElement("option");

  emptyOption.value = "";
  emptyOption.textContent = "관련 배우 선택 안 함";

  actorSelect.appendChild(emptyOption);

  if (!Array.isArray(actorList) || actorList.length === 0) {
    const noActorOption = document.createElement("option");

    noActorOption.value = "";
    noActorOption.textContent = "해당 콘텐츠의 배우 정보가 없습니다.";

    actorSelect.appendChild(noActorOption);
    actorSelect.disabled = false;
    actorLoadMessage.textContent = "TMDB에서 배우 정보를 찾지 못했습니다.";

    return;
  }

  actorList.forEach(function (actor) {
    const option = document.createElement("option");

    /*
     * 등록 요청에는 ACTOR_NO가 아니라 TMDB_ACTOR_ID를 전달합니다.
     * 서버가 콘텐츠 저장 후 실제 ACTOR_NO로 변환합니다.
     */
    option.value = actor.tmdbActorId;

    let optionText = actor.actorName;

    if (actor.characterName) {
      optionText += " / 배역: " + actor.characterName;
    }

    option.textContent = optionText;

    if (actorIdToRestore && String(actor.tmdbActorId) === String(actorIdToRestore)) {
      option.selected = true;
    }

    actorSelect.appendChild(option);
  });

  actorSelect.disabled = false;
  actorLoadMessage.textContent = actorList.length + "명의 배우가 조회되었습니다.";
}

/* =========================================================
   배우 선택 영역 초기화
========================================================= */
function resetActorSelect() {
  const actorSelect = document.getElementById("tmdbActorId");

  const actorLoadMessage = document.getElementById("actorLoadMessage");

  actorSelect.innerHTML = '<option value="">' + "콘텐츠를 먼저 선택해주세요." + "</option>";

  actorSelect.disabled = true;
  actorLoadMessage.textContent = "콘텐츠를 선택하면 해당 작품의 배우가 표시됩니다.";
}

/* =========================================================
   이미지 파일명 출력
========================================================= */
function updateFileName(input) {
  const fileNameElement = document.getElementById("selectedFileName");

  if (input.files && input.files.length > 0) {
    fileNameElement.textContent = input.files[0].name;
  } else {
    fileNameElement.textContent = "선택된 파일 없음";
  }
}

/* =========================================================
   상품 등록 폼 검증
========================================================= */
function validateProductForm(event) {
  const productName = document.getElementById("productName").value.trim();

  const productType = document.getElementById("productType").value;

  const price = Number(document.getElementById("price").value);

  const discountRate = Number(document.getElementById("discountRate").value);

  const stock = Number(document.getElementById("stock").value);

  const tmdbId = Number(document.getElementById("tmdbId").value);

  const contentType = document.getElementById("contentType").value;

  const productImage = document.getElementById("productImage");

  if (!productName) {
    showAlert("상품명을 입력해주세요.", "warning");
    return false;
  }

  if (!productType) {
    showAlert("상품 종류를 선택해주세요.", "warning");
    return false;
  }

  if (!Number.isFinite(price) || price <= 0) {
    showAlert("가격은 1원 이상 입력해주세요.", "warning");
    return false;
  }

  if (!Number.isFinite(discountRate) || discountRate < 0 || discountRate > 100) {
    showAlert("할인율은 0부터 100 사이로 입력해주세요.", "warning");
    return false;
  }

  if (!Number.isFinite(stock) || stock < 0) {
    showAlert("재고는 0개 이상 입력해주세요.", "warning");
    return false;
  }

  if (!Number.isFinite(tmdbId) || tmdbId <= 0 || !contentType) {
    showAlert("관련 콘텐츠를 선택해주세요.", "warning");
    return false;
  }

  if (!productImage.files || productImage.files.length === 0) {
    showAlert("상품 대표 이미지를 선택해주세요.", "warning");
    return false;
  }

  return confirmAndSubmit(event, "상품 등록을 요청하시겠습니까?");
}

/* =========================================================
   화면 최초 진입

   등록 실패 후 TMDB_ID와 CONTENT_TYPE이 남아 있으면
   배우 목록과 이전 배우 선택값을 복원합니다.
========================================================= */
document.addEventListener("DOMContentLoaded", function () {
  const tmdbId = document.getElementById("tmdbId").value;

  const contentType = document.getElementById("contentType").value;

  if (tmdbId && Number(tmdbId) > 0 && contentType) {
    loadActorsByCachedContent(tmdbId, contentType, savedTmdbActorId);
  } else {
    resetActorSelect();
  }
});

/* =========================================================
 * [상품 옵션 기능 추가] 의상/신발 색상-사이즈별 재고 입력
 * ========================================================= */
document.addEventListener("DOMContentLoaded", function () {
  const typeSelect = document.getElementById("productType");
  const section = document.getElementById("productOptionSection");
  const rows = document.getElementById("productOptionRows");
  const addButton = document.getElementById("addProductOptionBtn");
  const stockInput = document.getElementById("stock");
  if (!typeSelect || !section || !rows || !addButton || !stockInput) return;

  function isOptionType() {
    return typeSelect.value === "CLOTHES" || typeSelect.value === "SHOES";
  }
  function sizeOptions() {
    return typeSelect.value === "SHOES" ? ["220", "225", "230", "235", "240", "245", "250", "255", "260", "265", "270", "275", "280", "285", "290"] : ["XS", "S", "M", "L", "XL", "2XL", "3XL", "FREE"];
  }
  function reindex() {
    rows.querySelectorAll(".product-option-row").forEach((row, i) => {
      row.querySelector(".option-color").name = `optionList[${i}].colorName`;
      row.querySelector(".option-size").name = `optionList[${i}].sizeName`;
      row.querySelector(".option-stock").name = `optionList[${i}].stock`;
    });
    const total = [...rows.querySelectorAll(".option-stock")].reduce((sum, el) => sum + Math.max(0, Number(el.value) || 0), 0);
    stockInput.value = String(total);
  }
  function addRow() {
    const row = document.createElement("div");
    row.className = "product-option-row";
    row.innerHTML = `<input class="form-input option-color" type="text" maxlength="50" placeholder="색상 (예: 블랙)" required>
      <select class="form-input option-size" required><option value="">사이즈 선택</option>${sizeOptions()
        .map((v) => `<option value="${v}">${v}</option>`)
        .join("")}</select>
      <input class="form-input option-stock" type="number" min="0" value="0" placeholder="재고" required>
      <button type="button" class="option-remove-btn">삭제</button>`;
    row.querySelector(".option-remove-btn").addEventListener("click", () => {
      row.remove();
      if (!rows.children.length) addRow();
      reindex();
    });
    row.querySelector(".option-stock").addEventListener("input", reindex);
    rows.appendChild(row);
    reindex();
  }
  function refresh() {
    const enabled = isOptionType();
    section.hidden = !enabled;
    stockInput.readOnly = enabled;
    stockInput.closest(".form-group").style.display = enabled ? "none" : "";
    if (enabled && !rows.children.length) addRow();
    rows.querySelectorAll(".option-size").forEach((select) => {
      const old = select.value;
      select.innerHTML = `<option value="">사이즈 선택</option>${sizeOptions()
        .map((v) => `<option value="${v}">${v}</option>`)
        .join("")}`;
      if ([...select.options].some((o) => o.value === old)) select.value = old;
    });
    if (!enabled) {
      rows.innerHTML = "";
      stockInput.readOnly = false;
    }
    reindex();
  }
  addButton.addEventListener("click", addRow);
  typeSelect.addEventListener("change", refresh);
  refresh();
});
