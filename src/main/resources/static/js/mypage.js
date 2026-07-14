document.addEventListener("DOMContentLoaded", () => {
  const body = document.body;

  const modals = {
    member: document.getElementById("memberModal"),
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

    document
      .getElementById("mypageOttSection")
      ?.scrollIntoView({
        behavior: "smooth",
        block: "center",
      });
  }

  if (body.dataset.openDeleteModal === "true") {
    openModal(modals.delete);
  }

  /* =========================================================
       MEMBER UPDATE
    ========================================================= */

  const memberForm = document.getElementById("memberUpdateForm");

  let nicknameChecked = true;
  let checkedNickname = "";

  /* =========================================================
       OPEN
    ========================================================= */

  bindOpen("updateMemberBtn", modals.member);
  bindOpen("updateOttBtn", modals.ott);
  bindOpen("deleteBtn", modals.delete);

  /* =========================================================
       CLOSE
    ========================================================= */

  bindClose("closeMemberModal", modals.member);
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
       닉네임 변경 시 다시 중복확인
    ========================================================= */

  const nicknameInput =
    document.getElementById("updateNickname");

  nicknameInput?.addEventListener("input", () => {
    if (nicknameInput.value !== checkedNickname) {
      nicknameChecked = false;
    }
  });

  /* =========================================================
       닉네임 중복확인 버튼
    ========================================================= */

  document
    .getElementById("checkUpdateNicknameBtn")
    ?.addEventListener(
      "click",
      checkUpdateNickname
    );

  /* =========================================================
       MEMBER UPDATE VALIDATION
    ========================================================= */

  memberForm?.addEventListener("submit", (e) => {
    const nickname =
      memberForm.nickname.value.trim();

    const email =
      memberForm.email?.value.trim() ?? "";

    const socialMember =
      memberForm.socialMember.value === "true";

    // 닉네임
    if (nickname === "") {
      alert("닉네임을 입력해주세요.");

      e.preventDefault();
      return;
    }

    // 닉네임 중복확인
    if (!nicknameChecked) {
      alert("닉네임 중복확인을 해주세요.");

      e.preventDefault();
      return;
    }

    // 일반 회원 이메일 검증
    if (!socialMember && email === "") {
      alert("이메일을 입력해주세요.");

      e.preventDefault();
      return;
    }

    // 일반 회원만 비밀번호 변경 검증
    if (!socialMember) {
      const currentPw =
        memberForm.currentPw?.value.trim() ?? "";

      const newPw =
        memberForm.newPw?.value.trim() ?? "";

      const newPwCheck =
        memberForm.newPwCheck?.value.trim() ?? "";

      // 비밀번호 변경 시
      if (newPw !== "" || newPwCheck !== "") {
        if (currentPw === "") {
          alert("현재 비밀번호를 입력해주세요.");

          e.preventDefault();
          return;
        }

        if (newPw !== newPwCheck) {
          alert("새 비밀번호가 일치하지 않습니다.");

          e.preventDefault();
          return;
        }
      }
    }
  });

  /* =========================================================
       DELETE VALIDATION
    ========================================================= */

  const deleteForm =
    document.getElementById("deleteForm");

  const deleteConfirmInput =
    document.getElementById("deleteConfirmInput");

  deleteForm?.addEventListener("submit", (e) => {
    const confirmText =
      deleteConfirmInput?.value.trim() ?? "";

    // 1. 탈퇴 문구 일치 체크
    if (confirmText !== "탈퇴하겠습니다") {
      alert(
        "탈퇴하려면 '탈퇴하겠습니다'를 정확히 입력해주세요."
      );

      deleteConfirmInput?.focus();

      e.preventDefault();
      return;
    }

    // 2. 최종 확인
    const confirmed = confirm(
      "정말 회원을 탈퇴하시겠습니까?\n" +
      "탈퇴 후에는 모든 데이터가 삭제되며 복구할 수 없습니다."
    );

    if (!confirmed) {
      e.preventDefault();
    }
  });

  /* =========================================================
       닉네임 중복확인
    ========================================================= */

  async function checkUpdateNickname() {
    if (!nicknameInput) {
      return;
    }

    const nickname =
      nicknameInput.value.trim();

    const originalNickname =
      document
        .getElementById("originalNickname")
        ?.value ?? "";

    if (nickname === "") {
      alert("닉네임을 입력해주세요.");
      return;
    }

    if (nickname === originalNickname) {
      alert("현재 사용 중인 닉네임입니다.");

      nicknameChecked = true;
      checkedNickname = nickname;

      return;
    }

    try {
      const contextPath =
        body.dataset.contextPath ?? "";

      const memberNo =
        document
          .getElementById("memberNo")
          ?.value ?? "";

      const response = await fetch(
        `${contextPath}/member/checkUpdateNickname` +
        `?nickname=${encodeURIComponent(nickname)}` +
        `&memberNo=${encodeURIComponent(memberNo)}`
      );

      if (!response.ok) {
        throw new Error(
          `HTTP 오류: ${response.status}`
        );
      }

      const result =
        await response.text();

      if (result.trim() === "Y") {
        alert("사용 가능한 닉네임입니다.");

        nicknameChecked = true;
        checkedNickname = nickname;
      } else {
        alert("이미 사용 중인 닉네임입니다.");

        nicknameChecked = false;
        checkedNickname = "";
      }
    } catch (e) {
      console.error(e);

      alert(
        "닉네임 중복확인 중 오류가 발생했습니다."
      );
    }
  }

  /* =========================================================
       FUNCTION
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

    const openedModal =
      Object.values(modals).some(
        (item) =>
          item &&
          !item.classList.contains("hidden")
      );

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
    document
      .getElementById(buttonId)
      ?.addEventListener("click", () => {
        openModal(modal);
      });
  }

  function bindClose(buttonId, modal) {
    document
      .getElementById(buttonId)
      ?.addEventListener("click", () => {
        closeModal(modal);
      });
  }
});