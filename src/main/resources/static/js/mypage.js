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
