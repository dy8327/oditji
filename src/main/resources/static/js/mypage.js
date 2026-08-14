document.addEventListener("DOMContentLoaded", () => {
  const body = document.body;

  /* =========================================================
       플래시 메시지 알림

       JSTL EL은 정적 .js 파일 안에서 동작하지 않으므로
       JSP에서 body의 data-* 속성으로 값을 내려주고 여기서 읽는다.
    ========================================================= */

  if (body.dataset.errorMessage) {
    showAlert(body.dataset.errorMessage, "error");
  }

  if (body.dataset.message) {
    showAlert(body.dataset.message, "info");
  }

  const modals = {
    member: document.getElementById("memberModal"),
    notification: document.getElementById("notificationModal"),
    ott: document.getElementById("ottModal"),
    delete: document.getElementById("deleteModal"),
    subResult: document.getElementById("subResultModal"),
  };

  /* =========================================================
       REDIRECT 후 모달 다시 열기
    ========================================================= */

  if (body.dataset.openMemberModal === "true") {
    openModal(modals.member);
  }

  if (body.dataset.openOttModal === "true") {
    openModal(modals.ott);

    document.getElementById("mypageOttSection")?.scrollIntoView({
      behavior: "smooth",
      block: "center",
    });
  }

  if (body.dataset.openDeleteModal === "true") {
    openModal(modals.delete);
  }

  /* =========================================================
       [알림 수신 설정 추가]
       마이페이지 진입 시 현재 설정을 불러와 토글로 그려주고,
       클릭 시 Ajax로 저장한다. (common.js가 fetch에 CSRF 헤더를
       자동으로 붙여주므로 별도 토큰 처리는 필요 없다.)
    ========================================================= */

  const contextPath = body.dataset.contextPath ?? "";
  const notificationSettingList = document.getElementById("notificationSettingList");

  if (notificationSettingList) {
    loadNotificationSettingList();
  }

  async function loadNotificationSettingList() {
    try {
      const response = await fetch(`${contextPath}/api/notification/setting`);

      if (!response.ok) {
        throw new Error(`HTTP 오류: ${response.status}`);
      }

      const result = await response.json();
      renderNotificationSettingList(result.settingList ?? []);
    } catch (error) {
      console.error(error);
      notificationSettingList.innerHTML =
        '<div class="mypage-empty">알림 설정을 불러오지 못했습니다.</div>';
    }
  }

  function renderNotificationSettingList(settingList) {
    if (!settingList.length) {
      notificationSettingList.innerHTML =
        '<div class="mypage-empty">표시할 알림 설정이 없습니다.</div>';
      return;
    }

    notificationSettingList.innerHTML = "";

    settingList.forEach((setting) => {
      const item = document.createElement("div");
      item.className = "mypage-notification-item";

      const copy = document.createElement("div");
      copy.className = "mypage-notification-item-copy";

      const label = document.createElement("span");
      label.className = "mypage-notification-item-label";
      label.textContent = setting.label;

      const desc = document.createElement("span");
      desc.className = "mypage-notification-item-desc";
      desc.textContent = setting.description;

      copy.append(label, desc);

      const toggleLabel = document.createElement("label");
      toggleLabel.className = "mypage-toggle-switch";

      const checkbox = document.createElement("input");
      checkbox.type = "checkbox";
      checkbox.checked = !!setting.enabled;
      checkbox.addEventListener("change", () => {
        updateNotificationSetting(setting.noticeCategory, checkbox.checked, checkbox);
      });

      const track = document.createElement("span");
      track.className = "mypage-toggle-switch-track";

      toggleLabel.append(checkbox, track);
      item.append(copy, toggleLabel);
      notificationSettingList.appendChild(item);
    });
  }

  async function updateNotificationSetting(noticeCategory, enabled, checkbox) {
    checkbox.disabled = true;

    try {
      const response = await fetch(`${contextPath}/api/notification/setting`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ noticeCategory, enabled }),
      });

      const result = await response.json();

      if (!response.ok || !result.success) {
        throw new Error(result.message || "알림 설정 저장에 실패했습니다.");
      }
    } catch (error) {
      console.error(error);
      checkbox.checked = !enabled;
      await showAlert("알림 설정 저장에 실패했습니다.", "error");
    } finally {
      checkbox.disabled = false;
    }
  }

  /* =========================================================
       [마이페이지 구독 계산 결과 모달 연동 추가]
       카드 클릭 시 모달을 열고 저장된 결과 목록을 불러온다.
    ========================================================= */

  const subResultCountEl = document.getElementById("subResultCount");
  const subResultList = document.getElementById("subResultList");

  document.getElementById("subResultCardBtn")?.addEventListener("click", () => {
    openModal(modals.subResult);
    loadSubResultList();
  });

  async function loadSubResultList() {
    if (!subResultList) {
      return;
    }

    subResultList.innerHTML = '<div class="mypage-empty">저장된 결과를 불러오는 중입니다...</div>';

    try {
      const response = await fetch(`${contextPath}/api/subscription/saved-results`);

      if (!response.ok) {
        throw new Error(`HTTP 오류: ${response.status}`);
      }

      const result = await response.json();
      renderSubResultList(result.savedResultList ?? []);
    } catch (error) {
      console.error(error);
      subResultList.innerHTML = '<div class="mypage-empty">저장된 결과를 불러오지 못했습니다.</div>';
    }
  }

  function renderSubResultList(savedResultList) {
    if (!subResultList) {
      return;
    }

    if (!savedResultList.length) {
      subResultList.innerHTML = '<div class="mypage-empty">저장된 구독 계산 결과가 없습니다.</div>';
      return;
    }

    subResultList.innerHTML = "";

    savedResultList.forEach((savedResult) => {
      subResultList.appendChild(createSubResultItem(savedResult));
    });
  }

  function createSubResultItem(savedResult) {
    const item = document.createElement("div");
    item.className = "mypage-subresult-item";

    const header = document.createElement("div");
    header.className = "mypage-subresult-item-header";

    const createdAt = document.createElement("span");
    createdAt.className = "mypage-subresult-item-date";
    createdAt.textContent = formatSubResultDate(savedResult.createdAt);

    header.appendChild(createdAt);

    const platformGroupList = savedResult.platformGroupList;
    const hasPlatformGroups = Array.isArray(platformGroupList) && platformGroupList.length > 0;

    let platformSection;

    if (hasPlatformGroups) {
      platformSection = createSubResultPlatformGroupList(platformGroupList);
    } else {
      /* 구버전에 저장된 결과 등 OTT별 그룹 정보가 없는 경우, 기존 방식(플랫폼명 나열 + 콘텐츠 요약)으로 대체 표시한다 */
      const platforms = document.createElement("p");
      platforms.className = "mypage-subresult-item-platforms";
      platforms.textContent = savedResult.platformNameList?.length ? savedResult.platformNameList.join(", ") : "선택한 플랫폼 정보 없음";

      const content = document.createElement("p");
      content.className = "mypage-subresult-item-content";
      content.textContent = formatSubResultContentSummary(savedResult.contentList);

      platformSection = document.createDocumentFragment();
      platformSection.append(platforms, content);
    }

    const priceRow = document.createElement("div");
    priceRow.className = "mypage-subresult-item-price-row";

    const priceDetail = document.createElement("span");
    priceDetail.className = "mypage-subresult-item-price-detail";
    priceDetail.textContent = `정가 ${formatSubResultPrice(savedResult.totalPrice)} · 절감 ${formatSubResultPrice(savedResult.discountPrice)}`;

    const finalPrice = document.createElement("span");
    finalPrice.className = "mypage-subresult-item-final-price";
    finalPrice.textContent = `월 ${formatSubResultPrice(savedResult.finalPrice)}`;

    priceRow.append(priceDetail, finalPrice);

    const actions = document.createElement("div");
    actions.className = "mypage-subresult-item-actions";

    const detailBtn = document.createElement("button");
    detailBtn.type = "button";
    detailBtn.className = "mypage-subresult-detail-btn";
    detailBtn.textContent = "상세보기";
    detailBtn.addEventListener("click", () => {
      window.open(`${contextPath}/subscription/result/${encodeURIComponent(savedResult.resultId)}`, "_blank", "noopener,noreferrer");
    });

    const copyLinkBtn = document.createElement("button");
    copyLinkBtn.type = "button";
    copyLinkBtn.className = "mypage-subresult-copy-btn";
    copyLinkBtn.textContent = "공유링크 복사";
    copyLinkBtn.addEventListener("click", () => {
      copySubResultLink(savedResult.resultId, copyLinkBtn);
    });

    const deleteBtn = document.createElement("button");
    deleteBtn.type = "button";
    deleteBtn.className = "mypage-subresult-delete-btn";
    deleteBtn.textContent = "삭제";
    deleteBtn.addEventListener("click", () => {
      deleteSubResult(savedResult.resultId, deleteBtn);
    });

    actions.append(detailBtn, copyLinkBtn, deleteBtn);

    item.append(header, platformSection, priceRow, actions);

    return item;
  }

  /*
   * [OTT별 콘텐츠 그룹 표시 추가]
   * 선택된 플랫폼별로 "플랫폼명 (가격) - 담긴 작품"을 한 줄씩 묶어 보여준다.
   * 한 작품이 여러 선택 플랫폼에서 모두 보인다면, 해당하는 모든 플랫폼 줄에 함께 표시된다.
   */
  function createSubResultPlatformGroupList(platformGroupList) {
    const wrapper = document.createElement("div");
    wrapper.className = "mypage-subresult-platform-groups";

    platformGroupList.forEach((platform) => {
      const row = document.createElement("div");
      row.className = "mypage-subresult-platform-group";

      const head = document.createElement("div");
      head.className = "mypage-subresult-platform-group-head";

      const name = document.createElement("span");
      name.className = "mypage-subresult-platform-group-name";
      name.textContent = platform.platformName ?? "";

      const price = document.createElement("span");
      price.className = "mypage-subresult-platform-group-price";
      price.textContent = `월 ${formatSubResultPrice(platform.bestPrice)}`;

      head.append(name, price);

      const contentSummary = document.createElement("p");
      contentSummary.className = "mypage-subresult-platform-group-content";
      contentSummary.textContent = formatSubResultContentSummary(platform.contentList);

      row.append(head, contentSummary);
      wrapper.appendChild(row);
    });

    return wrapper;
  }

  /*
   * [OTT 구독 조합 계산기 - 선택한 콘텐츠 표시 추가]
   * 담았던 작품이 많을 수 있으므로 카드 안에서는 앞 3편만 제목으로 보여주고
   * 나머지는 "외 N편"으로 요약한다. 전체 목록은 "상세보기"(result.jsp)에서 확인한다.
   */
  const SUB_RESULT_CONTENT_PREVIEW_COUNT = 3;

  function formatSubResultContentSummary(contentList) {
    if (!contentList?.length) {
      return "선택한 콘텐츠 정보 없음";
    }

    const titles = contentList.map((item) => item.title).filter(Boolean);

    if (!titles.length) {
      return "선택한 콘텐츠 정보 없음";
    }

    const preview = titles.slice(0, SUB_RESULT_CONTENT_PREVIEW_COUNT).join(", ");
    const remaining = titles.length - SUB_RESULT_CONTENT_PREVIEW_COUNT;

    return remaining > 0 ? `${preview} 외 ${remaining}편` : preview;
  }

  async function copySubResultLink(resultId, copyLinkBtn) {
    const shareUrl = `${window.location.origin}${contextPath}/subscription/result/${encodeURIComponent(resultId)}`;

    try {
      await navigator.clipboard.writeText(shareUrl);
      await showAlert("공유 링크가 복사됐어요.", "success");
    } catch (error) {
      console.error(error);
      await showAlert("링크 복사에 실패했어요.", "error");
    }
  }

  async function deleteSubResult(resultId, deleteBtn) {
    const confirmed = await showConfirm("이 저장 결과를 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.", "warning");

    if (!confirmed) {
      return;
    }

    deleteBtn.disabled = true;

    try {
      const response = await fetch(`${contextPath}/api/subscription/saved-results/${encodeURIComponent(resultId)}`, {
        method: "DELETE",
      });

      const result = await response.json();

      if (!response.ok || !result.success) {
        throw new Error(result.message || "저장 결과 삭제에 실패했습니다.");
      }

      await loadSubResultList();
      await refreshSubResultCount();
    } catch (error) {
      console.error(error);
      await showAlert("저장 결과 삭제에 실패했습니다.", "error");
      deleteBtn.disabled = false;
    }
  }

  async function refreshSubResultCount() {
    if (!subResultCountEl) {
      return;
    }

    try {
      const response = await fetch(`${contextPath}/api/subscription/saved-results`);

      if (!response.ok) {
        throw new Error(`HTTP 오류: ${response.status}`);
      }

      const result = await response.json();
      subResultCountEl.textContent = (result.savedResultList ?? []).length;
    } catch (error) {
      console.error(error);
    }
  }

  function formatSubResultDate(isoString) {
    if (!isoString) {
      return "";
    }

    const date = new Date(isoString);

    if (Number.isNaN(date.getTime())) {
      return "";
    }

    const pad = (value) => String(value).padStart(2, "0");

    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  function formatSubResultPrice(price) {
    return `${Number(price ?? 0).toLocaleString()}원`;
  }

  /* =========================================================
       MEMBER UPDATE
    ========================================================= */

  const memberForm = document.getElementById("memberUpdateForm");
  const nicknameInput = document.getElementById("updateNickname");
  const emailInput = document.getElementById("updateEmail");
  const nicknameMessage = document.getElementById("nicknameMessage");
  const emailMessage = document.getElementById("emailMessage");

  const originalNickname = document.getElementById("originalNickname")?.value.trim() ?? "";

  const originalEmail = document.getElementById("originalEmail")?.value.trim() ?? "";

  let nicknameChecked = true;
  let checkedNickname = originalNickname;
  let emailChecked = true;
  let checkedEmail = originalEmail;

  /* =========================================================
       OPEN
    ========================================================= */

  bindOpen("updateMemberBtn", modals.member);
  bindOpen("notificationSettingBtn", modals.notification);
  bindOpen("updateOttBtn", modals.ott);
  bindOpen("deleteBtn", modals.delete);

  /* =========================================================
       CLOSE
    ========================================================= */

  bindClose("closeMemberModal", modals.member);
  bindClose("closeNotificationModal", modals.notification);
  bindClose("closeOttModal", modals.ott);
  bindClose("closeDeleteModal", modals.delete);
  bindClose("closeSubResultModal", modals.subResult);

  /* =========================================================
       OUTSIDE CLICK
    ========================================================= */

  Object.values(modals).forEach((modal) => {
    if (!modal) {
      return;
    }

    modal.addEventListener("click", (e) => {
      if (e.target === modal) {
        closeModal(modal);
      }
    });
  });

  /* =========================================================
       ESC
    ========================================================= */

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      closeAllModals();
    }
  });

  /* =========================================================
       입력값 변경 시 중복확인 상태 초기화
    ========================================================= */

  nicknameInput?.addEventListener("input", () => {
    const nickname = nicknameInput.value.trim();

    if (nickname === originalNickname) {
      nicknameChecked = true;
      checkedNickname = originalNickname;
      setMessage(nicknameMessage, "", "");
      return;
    }

    if (nickname !== checkedNickname) {
      nicknameChecked = false;
      checkedNickname = "";
      setMessage(nicknameMessage, "닉네임 중복확인이 필요합니다.", "invalid");
    }
  });

  emailInput?.addEventListener("input", () => {
    const email = emailInput.value.trim();

    if (email === originalEmail) {
      emailChecked = true;
      checkedEmail = originalEmail;
      setMessage(emailMessage, "", "");
      return;
    }

    if (email !== checkedEmail) {
      emailChecked = false;
      checkedEmail = "";
      setMessage(emailMessage, "이메일 중복확인이 필요합니다.", "invalid");
    }
  });

  /* =========================================================
       중복확인 버튼
    ========================================================= */

  document.getElementById("checkUpdateNicknameBtn")?.addEventListener("click", checkUpdateNickname);

  document.getElementById("checkUpdateEmailBtn")?.addEventListener("click", checkUpdateEmail);

  /* =========================================================
       비밀번호 보기 / 숨기기
    ========================================================= */

  document.querySelectorAll(".password-toggle-btn").forEach((button) => {
    button.addEventListener("click", () => {
      const targetId = button.dataset.target;
      const input = document.getElementById(targetId);

      if (!input) {
        return;
      }

      const showing = input.type === "text";
      input.type = showing ? "password" : "text";
      button.textContent = showing ? "보기" : "숨기기";
      button.setAttribute("aria-label", showing ? "비밀번호 표시" : "비밀번호 숨기기");
    });
  });

  /* =========================================================
       MEMBER UPDATE VALIDATION
    ========================================================= */

  memberForm?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const nickname = nicknameInput?.value.trim() ?? "";
    const email = emailInput?.value.trim() ?? "";
    const socialMember = memberForm.socialMember.value === "true";

    if (!isValidNickname(nickname)) {
      await showAlert("닉네임은 한글, 영문, 숫자 2~10자로 입력해주세요.", "warning");
      nicknameInput?.focus();
      return;
    }

    if (nickname !== originalNickname && (!nicknameChecked || checkedNickname !== nickname)) {
      await showAlert("변경한 닉네임의 중복확인을 해주세요.", "warning");
      nicknameInput?.focus();
      return;
    }

    if (!socialMember) {
      if (!isValidEmail(email)) {
        await showAlert("올바른 이메일 형식으로 입력해주세요.", "warning");
        emailInput?.focus();
        return;
      }

      if (email !== originalEmail && (!emailChecked || checkedEmail !== email)) {
        await showAlert("변경한 이메일의 중복확인을 해주세요.", "warning");
        emailInput?.focus();
        return;
      }

      const currentPw = memberForm.currentPw?.value.trim() ?? "";
      const newPw = memberForm.newPw?.value.trim() ?? "";
      const newPwCheck = memberForm.newPwCheck?.value.trim() ?? "";

      if (newPw !== "" || newPwCheck !== "") {
        if (currentPw === "") {
          await showAlert("현재 비밀번호를 입력해주세요.", "warning");
          return;
        }

        if (!/^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d]).{8,20}$/.test(newPw)) {
          await showAlert("새 비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자로 입력해주세요.", "warning");
          return;
        }

        if (newPw !== newPwCheck) {
          await showAlert("새 비밀번호가 일치하지 않습니다.", "warning");
          return;
        }
      }
    }

    memberForm.submit();
  });

  /* =========================================================
       OTT 정보 수정 (마이페이지)

       [수정] 예전에는 "OTT 없음"을 체크하면 플랫폼 체크박스들을
       disabled 처리했는데, 그 상태 전환 타이밍 때문에 "OTT 없음"을
       클릭해도 바로 체크 표시가 되지 않고 한 번 더 눌러야 선택되는
       것처럼 보이는 문제가 있었다. 회원가입(join.jsp)의 OTT 선택은
       이 disabled 처리 없이 "체크 시 반대편 선택만 해제"하는 단순한
       방식이라 이런 문제가 없다. 마이페이지도 동일한 단순 방식으로
       맞춘다: "OTT 없음"을 선택하면 플랫폼 체크만 전부 해제하고,
       플랫폼을 하나라도 선택하면 "OTT 없음" 체크만 해제한다.
    ========================================================= */

  initMypageOttSelect();

  function initMypageOttSelect() {
    const form = document.getElementById("mypageOttForm");

    if (!form) {
      return;
    }

    const optionList = document.getElementById("mypageOttOptionList");
    const countEl = document.getElementById("mypageOttCount");
    const noOttCheckbox = document.getElementById("mypageNoOtt");

    const platformCheckboxes = optionList ? optionList.querySelectorAll('input[name="ottList"]') : [];

    function updateMypageOttCount() {
      if (!optionList || !countEl) {
        return;
      }

      const checked = optionList.querySelectorAll('input[name="ottList"]:checked').length;

      countEl.textContent = checked + "개 선택";
    }

    if (noOttCheckbox) {
      noOttCheckbox.addEventListener("change", () => {
        if (noOttCheckbox.checked) {
          platformCheckboxes.forEach((checkbox) => {
            checkbox.checked = false;
          });
        }

        updateMypageOttCount();
      });
    }

    platformCheckboxes.forEach((checkbox) => {
      checkbox.addEventListener("change", () => {
        if (checkbox.checked && noOttCheckbox) {
          noOttCheckbox.checked = false;
        }

        updateMypageOttCount();
      });
    });

    updateMypageOttCount();

    form.addEventListener("submit", async (e) => {
      const checkedCount = optionList ? optionList.querySelectorAll('input[name="ottList"]:checked').length : 0;

      const selectedNoOtt = !!(noOttCheckbox && noOttCheckbox.checked);

      if (checkedCount === 0 && !selectedNoOtt) {
        e.preventDefault();
        await showAlert("이용 중인 OTT를 선택하거나 'OTT 없음'을 선택해주세요.", "warning");
      }
    });
  }

  /* =========================================================
       DELETE VALIDATION
    ========================================================= */

  const deleteForm = document.getElementById("deleteForm");
  const deleteConfirmInput = document.getElementById("deleteConfirmInput");

  deleteForm?.addEventListener("submit", async (e) => {
    e.preventDefault();

    const confirmText = deleteConfirmInput?.value.trim() ?? "";

    if (confirmText !== "탈퇴하겠습니다") {
      showAlert("탈퇴하려면 '탈퇴하겠습니다'를 정확히 입력해주세요.", "warning");
      deleteConfirmInput?.focus();
      return;
    }

    const confirmed = await showConfirm("정말 회원을 탈퇴하시겠습니까?\n" + "탈퇴 후 7일 동안 복구할 수 있으며, 이후 데이터가 삭제됩니다.", "warning");

    if (!confirmed) {
      return;
    }

    deleteForm.submit();
  });

  /* =========================================================
       닉네임 중복확인
    ========================================================= */

  async function checkUpdateNickname() {
    if (!nicknameInput) {
      return;
    }

    const nickname = nicknameInput.value.trim();

    if (!isValidNickname(nickname)) {
      nicknameChecked = false;
      checkedNickname = "";
      setMessage(nicknameMessage, "한글, 영문, 숫자 2~10자로 입력해주세요.", "invalid");
      nicknameInput.focus();
      return;
    }

    if (nickname === originalNickname) {
      nicknameChecked = true;
      checkedNickname = nickname;
      setMessage(nicknameMessage, "현재 사용 중인 닉네임입니다.", "valid");
      return;
    }

    const result = await requestDuplicateCheck("/member/checkUpdateNickname", "nickname", nickname, "닉네임");

    if (result === null) {
      return;
    }

    nicknameChecked = result;
    checkedNickname = result ? nickname : "";

    setMessage(nicknameMessage, result ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.", result ? "valid" : "invalid");
  }

  /* =========================================================
       이메일 중복확인
    ========================================================= */

  async function checkUpdateEmail() {
    if (!emailInput) {
      return;
    }

    const email = emailInput.value.trim();

    if (!isValidEmail(email)) {
      emailChecked = false;
      checkedEmail = "";
      setMessage(emailMessage, "올바른 이메일 형식으로 입력해주세요.", "invalid");
      emailInput.focus();
      return;
    }

    if (email === originalEmail) {
      emailChecked = true;
      checkedEmail = email;
      setMessage(emailMessage, "현재 사용 중인 이메일입니다.", "valid");
      return;
    }

    const result = await requestDuplicateCheck("/member/checkUpdateEmail", "email", email, "이메일");

    if (result === null) {
      return;
    }

    emailChecked = result;
    checkedEmail = result ? email : "";

    setMessage(emailMessage, result ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.", result ? "valid" : "invalid");
  }

  async function requestDuplicateCheck(path, parameterName, value, label) {
    try {
      const contextPath = body.dataset.contextPath ?? "";
      const params = new URLSearchParams({ [parameterName]: value });
      const response = await fetch(`${contextPath}${path}?${params.toString()}`);

      if (!response.ok) {
        throw new Error(`HTTP 오류: ${response.status}`);
      }

      const result = (await response.text()).trim();
      return result === "Y";
    } catch (error) {
      console.error(error);
      await showAlert(`${label} 중복확인 중 오류가 발생했습니다.`, "error");
      return null;
    }
  }

  function isValidNickname(nickname) {
    return /^[a-zA-Z0-9가-힣]{2,10}$/.test(nickname);
  }

  function isValidEmail(email) {
    return /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email);
  }

  function setMessage(element, message, stateClass) {
    if (!element) {
      return;
    }

    element.textContent = message;
    element.classList.remove("valid", "invalid");

    if (stateClass) {
      element.classList.add(stateClass);
    }
  }

  /* =========================================================
       MODAL FUNCTION
    ========================================================= */

  function openModal(modal) {
    if (!modal) {
      return;
    }

    closeAllModals();
    modal.classList.remove("hidden");
    body.style.overflow = "hidden";
  }

  function closeModal(modal) {
    if (!modal) {
      return;
    }

    modal.classList.add("hidden");

    const openedModal = Object.values(modals).some((item) => item && !item.classList.contains("hidden"));

    if (!openedModal) {
      body.style.overflow = "";
    }
  }

  function closeAllModals() {
    Object.values(modals).forEach((modal) => {
      modal?.classList.add("hidden");
    });

    body.style.overflow = "";
  }

  function bindOpen(buttonId, modal) {
    document.getElementById(buttonId)?.addEventListener("click", () => {
      openModal(modal);
    });
  }

  function bindClose(buttonId, modal) {
    document.getElementById(buttonId)?.addEventListener("click", () => {
      closeModal(modal);
    });
  }
});
