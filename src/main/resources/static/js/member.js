/* REGEX RULES */
const regex = {
  id: /^[a-z0-9]{5,12}$/,
  pw: /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$/,
  nick: /^[a-zA-Z0-9가-힣]{2,10}$/,
  email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  phone: /^010-\d{4}-\d{4}$/,
  businessNumber: /^\d{3}-\d{2}-\d{5}$/,
  openDate: /^\d{8}$/,
};

/* DUPLICATE CHECK STATE */
let idChecked = false;
let nicknameChecked = false;

/*
 * =========================================================
 * 이메일 중복확인 상태
 *
 * 이메일 중복확인 완료 여부를 저장한다.
 * =========================================================
 */
let emailChecked = false;

let businessNumberChecked = false;
let businessVerified = false;

let checkedIdValue = "";
let checkedNicknameValue = "";

/*
 * =========================================================
 * 이메일 중복확인 당시 입력값
 *
 * 중복확인 후 이메일을 변경했는지 검사하기 위해 사용한다.
 * =========================================================
 */
let checkedEmailValue = "";

let checkedBusinessNumberValue = "";
let verifiedBusinessNumber = "";
let verifiedRepresentativeName = "";
let verifiedOpenDate = "";

/* DOM HELPER */
function getValue(id) {
  const el = document.getElementById(id);
  return el ? el.value.trim() : "";
}

function getContextPath() {
  return document.body.dataset.contextPath || "";
}

function setBorder(id, valid) {
  const el = document.getElementById(id);

  if (!el) {
    return;
  }

  el.style.border = valid ? "1px solid #ddd" : "1px solid red";
}

function focusInput(id) {
  const el = document.getElementById(id);

  if (el) {
    el.focus();
  }
}

function resetBusinessVerification() {
  businessVerified = false;

  verifiedBusinessNumber = "";
  verifiedRepresentativeName = "";
  verifiedOpenDate = "";

  const messageEl = document.getElementById("businessVerifyMessage");

  if (messageEl) {
    messageEl.textContent = "";
    messageEl.style.color = "";
  }
}

/* =============================
   ID CHECK
   Controller 응답: "Y" 또는 "N"
================================ */
async function checkId() {
  const memberId = getValue("memberId");

  if (!regex.id.test(memberId)) {
    await showAlert("아이디는 5~12자의 소문자/숫자만 가능합니다.", "warning");
    idChecked = false;
    checkedIdValue = "";
    setBorder("memberId", false);
    focusInput("memberId");
    return;
  }

  try {
    const contextPath = getContextPath();
    const url = `${contextPath}/member/checkId?memberId=${encodeURIComponent(memberId)}`;
    const response = await fetch(url);
    const result = await response.text();

    if (!response.ok) {
      await showAlert("아이디 중복확인 요청 실패: " + response.status, "error");
      idChecked = false;
      checkedIdValue = "";
      return;
    }

    if (result.trim() === "Y") {
      await showAlert("사용 가능한 아이디입니다.", "success");
      idChecked = true;
      checkedIdValue = memberId;
      setBorder("memberId", true);
      return;
    }

    if (result.trim() === "N") {
      await showAlert("이미 사용 중인 아이디입니다.", "warning");
      idChecked = false;
      checkedIdValue = "";
      setBorder("memberId", false);
      focusInput("memberId");
      return;
    }

    await showAlert("알 수 없는 서버 응답입니다: " + result, "error");
    idChecked = false;
    checkedIdValue = "";
  } catch (error) {
    console.error("아이디 중복확인 오류:", error);
    await showAlert("아이디 중복확인 중 오류가 발생했습니다.", "error");
    idChecked = false;
    checkedIdValue = "";
  }
}

/* =============================
   NICKNAME CHECK
   Controller 응답: "Y" 또는 "N"
================================ */
async function checkNickname() {
  const nickname = getValue("nickname");

  if (!regex.nick.test(nickname)) {
    await showAlert("닉네임은 2~10자의 한글/영문/숫자만 가능합니다.", "warning");
    nicknameChecked = false;
    checkedNicknameValue = "";
    setBorder("nickname", false);
    focusInput("nickname");
    return;
  }

  try {
    const contextPath = getContextPath();
    const url = `${contextPath}/member/checkNickname?nickname=${encodeURIComponent(nickname)}`;

    const response = await fetch(url);
    const result = await response.text();

    if (!response.ok) {
      await showAlert("닉네임 중복확인 요청 실패: " + response.status, "error");
      nicknameChecked = false;
      checkedNicknameValue = "";
      return;
    }

    if (result.trim() === "Y") {
      await showAlert("사용 가능한 닉네임입니다.", "success");
      nicknameChecked = true;
      checkedNicknameValue = nickname;
      setBorder("nickname", true);
      return;
    }

    if (result.trim() === "N") {
      await showAlert("이미 사용 중인 닉네임입니다.", "warning");
      nicknameChecked = false;
      checkedNicknameValue = "";
      setBorder("nickname", false);
      focusInput("nickname");
      return;
    }

    await showAlert("알 수 없는 서버 응답입니다: " + result, "error");
    nicknameChecked = false;
    checkedNicknameValue = "";
  } catch (error) {
    console.error("닉네임 중복확인 오류:", error);
    await showAlert("닉네임 중복확인 중 오류가 발생했습니다.", "error");
    nicknameChecked = false;
    checkedNicknameValue = "";
  }
}

/*
 * =========================================================
 * 일반회원 이메일 중복확인
 *
 * Controller 응답: "Y" 또는 "N"
 * 아이디 및 닉네임 중복확인 방식과 동일하게 처리한다.
 * =========================================================
 */
async function checkEmail() {
  const email = getValue("email");

  /*
   * 이메일 형식 확인
   */
  if (!regex.email.test(email)) {
    await showAlert("올바른 이메일 형식으로 입력해주세요.", "warning");

    emailChecked = false;
    checkedEmailValue = "";

    setBorder("email", false);
    focusInput("email");

    return;
  }

  try {
    /*
     * 프로젝트의 context path를 가져온다.
     */
    const contextPath = getContextPath();

    const url = `${contextPath}/member/checkEmail?email=${encodeURIComponent(email)}`;

    const response = await fetch(url);
    const result = await response.text();

    if (!response.ok) {
      await showAlert("이메일 중복확인 요청 실패: " + response.status, "error");

      emailChecked = false;
      checkedEmailValue = "";

      return;
    }

    /*
     * 사용 가능한 이메일
     */
    if (result.trim() === "Y") {
      await showAlert("사용 가능한 이메일입니다.", "success");

      emailChecked = true;
      checkedEmailValue = email;

      setBorder("email", true);

      return;
    }

    /*
     * 이미 사용 중인 이메일
     */
    if (result.trim() === "N") {
      await showAlert("이미 사용 중인 이메일입니다.", "warning");

      emailChecked = false;
      checkedEmailValue = "";

      setBorder("email", false);
      focusInput("email");

      return;
    }

    await showAlert("알 수 없는 서버 응답입니다: " + result, "error");

    emailChecked = false;
    checkedEmailValue = "";
  } catch (error) {
    console.error("이메일 중복확인 오류:", error);

    await showAlert("이메일 중복확인 중 오류가 발생했습니다.", "error");

    emailChecked = false;
    checkedEmailValue = "";
  }
}

/* ===================================
   BUSINESS NUMBER CHECK (사업자 전용)
   Controller 응답: "Y" 또는 "N"
====================================== */
async function checkBusinessNumber() {
  const businessNumber = getValue("businessNumber");

  if (!regex.businessNumber.test(businessNumber)) {
    await showAlert("사업자등록번호는 000-00-00000 형식으로 입력해주세요.", "warning");
    businessNumberChecked = false;
    checkedBusinessNumberValue = "";
    setBorder("businessNumber", false);
    focusInput("businessNumber");
    return;
  }

  try {
    const contextPath = getContextPath();
    const url = `${contextPath}/member/checkBusinessNumber?businessNumber=${encodeURIComponent(businessNumber)}`;
    const response = await fetch(url);
    const result = await response.text();

    if (!response.ok) {
      await showAlert("사업자등록번호 중복확인 요청 실패: " + response.status, "error");
      businessNumberChecked = false;
      checkedBusinessNumberValue = "";
      return;
    }

    if (result.trim() === "Y") {
      await showAlert("등록 가능한 사업자등록번호입니다.", "success");
      businessNumberChecked = true;
      checkedBusinessNumberValue = businessNumber;
      setBorder("businessNumber", true);
      return;
    }

    if (result.trim() === "N") {
      await showAlert("이미 등록된 사업자등록번호입니다.", "warning");
      businessNumberChecked = false;
      checkedBusinessNumberValue = "";
      setBorder("businessNumber", false);
      focusInput("businessNumber");
      return;
    }

    await showAlert("알 수 없는 서버 응답입니다: " + result, "error");
    businessNumberChecked = false;
    checkedBusinessNumberValue = "";
  } catch (error) {
    console.error("사업자등록번호 중복확인 오류:", error);
    await showAlert("사업자등록번호 중복확인 중 오류가 발생했습니다.", "error");
    businessNumberChecked = false;
    checkedBusinessNumberValue = "";
  }
}

/* NTS BUSINESS VERIFY(국세청 사업자 진위확인) */
async function verifyBusiness() {
  const businessNumber = getValue("businessNumber");
  const representativeName = getValue("representativeName");
  const openDate = getValue("openDate");

  const messageEl = document.getElementById("businessVerifyMessage");

  /* 사업자번호 형식 */
  if (!regex.businessNumber.test(businessNumber)) {
    await showAlert("사업자등록번호 형식을 확인해주세요.", "warning");
    focusInput("businessNumber");
    return;
  }

  /* 중복확인 여부 */
  if (!businessNumberChecked || checkedBusinessNumberValue !== businessNumber) {
    await showAlert("먼저 사업자등록번호 중복확인을 해주세요.", "warning");
    focusInput("businessNumber");
    return;
  }

  /* 대표자명 */
  if (!representativeName) {
    await showAlert("대표자명을 입력해주세요.", "warning");
    focusInput("representativeName");
    return;
  }

  /* 개업일 */
  if (!regex.openDate.test(openDate)) {
    await showAlert("개업일은 YYYYMMDD 형식의 숫자 8자리로 입력해주세요.", "warning");
    focusInput("openDate");
    return;
  }

  try {
    const contextPath = getContextPath();
    const formData = new FormData();
    formData.append("businessNumber", businessNumber);
    formData.append("representativeName", representativeName);
    formData.append("openDate", openDate);

    if (messageEl) {
      messageEl.textContent = "국세청 사업자 정보를 확인 중입니다...";
    }

    const response = await fetch(`${contextPath}/member/verifyBusiness`, {
      method: "POST",
      body: formData,
    });

    if (!response.ok) {
      businessVerified = false;

      if (messageEl) {
        messageEl.textContent = "사업자 인증 요청 중 오류가 발생했습니다.";
      }

      await showAlert("사업자 인증 요청 실패: " + response.status, "error");
      return;
    }

    const result = await response.json();

    if (result.valid === true) {
      businessVerified = true;
      verifiedBusinessNumber = businessNumber;
      verifiedRepresentativeName = representativeName;
      verifiedOpenDate = openDate;

      if (messageEl) {
        messageEl.textContent = result.message || "사업자 정보가 확인되었습니다.";
        messageEl.style.color = "green";
      }

      await showAlert(result.message || "사업자 정보가 확인되었습니다.", "success");

      return;
    }

    /* 인증 실패 */

    businessVerified = false;
    verifiedBusinessNumber = "";
    verifiedRepresentativeName = "";
    verifiedOpenDate = "";

    if (messageEl) {
      messageEl.textContent = result.message || "사업자 정보를 확인할 수 없습니다.";
      messageEl.style.color = "red";
    }

    await showAlert(result.message || "사업자 정보를 확인할 수 없습니다.", "warning");
  } catch (error) {
    console.error("국세청 사업자 인증 오류:", error);

    businessVerified = false;

    if (messageEl) {
      messageEl.textContent = "사업자 인증 중 오류가 발생했습니다.";
      messageEl.style.color = "red";
    }

    await showAlert("사업자 인증 중 오류가 발생했습니다.", "error");
  }
}

/* INIT */
document.addEventListener("DOMContentLoaded", () => {
  const form = document.getElementById("joinForm");
  const memberName = document.getElementById("memberName");
  const memberId = document.getElementById("memberId");
  const memberPw = document.getElementById("memberPw");
  const memberPwCheck = document.getElementById("memberPwCheck");
  const nickname = document.getElementById("nickname");
  const email = document.getElementById("email");
  const phone = document.getElementById("phone");
  const joinType = document.getElementById("joinType");
  const businessNumber = document.getElementById("businessNumber");
  const ottCheckboxes = document.querySelectorAll("input[name='ottList']");
  const noOttCheckbox = document.getElementById("noOtt");

  /*
   * 수정:
   * 비밀번호 표시/숨김 기능은 회원가입 페이지뿐 아니라
   * 비밀번호 변경 페이지에서도 사용하므로 form 검사 전에 실행한다.
   */
  const passwordToggleButtons = document.querySelectorAll(".password-toggle-btn");

  passwordToggleButtons.forEach((button) => {
    button.addEventListener("click", () => {
      const targetId = button.dataset.target;
      const targetInput = document.getElementById(targetId);

      if (!targetInput) {
        return;
      }

      const showPassword = targetInput.type === "password";

      targetInput.type = showPassword ? "text" : "password";
      button.textContent = showPassword ? "숨김" : "보기";
      button.setAttribute("aria-pressed", String(showPassword));
      button.setAttribute(
        "aria-label",
        showPassword ? "비밀번호 숨기기" : "비밀번호 표시"
      );
    });
  });

  const profileImageFile = document.getElementById("profileImageFile");
  const profileFileName = document.querySelector("label[for='profileImageFile']") ?.parentElement.querySelector(".file-name");
  const licenseFile = document.getElementById("licenseFile");
  const licenseFileName = document.getElementById("licenseFileName");
  const representativeName = document.getElementById("representativeName");
  const openDate = document.getElementById("openDate");

  /*
   * 회원가입 페이지가 아니라면
   * 아래 회원가입 전용 로직은 실행하지 않는다.
   */
  if (!form) {
    return;
  }


  /*
   * =========================================================
   * 일반회원 OTT 없음 선택 처리
   *
   * OTT 없음과 실제 OTT가 동시에 선택되지 않도록 한다.
   * =========================================================
   */
  if (noOttCheckbox) {
    noOttCheckbox.addEventListener("change", () => {
      if (!noOttCheckbox.checked) {
        return;
      }

      ottCheckboxes.forEach((checkbox) => {
        checkbox.checked = false;
      });
    });
  }

  ottCheckboxes.forEach((checkbox) => {
    checkbox.addEventListener("change", () => {
      if (checkbox.checked && noOttCheckbox) {
        noOttCheckbox.checked = false;
      }
    });
  });

  /* 아이디 변경 시 중복확인 초기화 */
  if (memberId) {
    memberId.addEventListener("input", () => {
      idChecked = false;
      checkedIdValue = "";

      const value = memberId.value.trim();

      if (value.length === 0) {
        setBorder("memberId", true);
      } else {
        setBorder("memberId", regex.id.test(value));
      }
    });
  }

  /* 닉네임 변경 시 중복확인 초기화 */
  if (nickname) {
    nickname.addEventListener("input", () => {
      nicknameChecked = false;
      checkedNicknameValue = "";

      const value = nickname.value.trim();

      if (value.length === 0) {
        setBorder("nickname", true);
      } else {
        setBorder("nickname", regex.nick.test(value));
      }
    });
  }

  /* 비밀번호 형식 검사 */
  if (memberPw) {
    memberPw.addEventListener("input", () => {
      const value = memberPw.value.trim();

      if (value.length === 0) {
        setBorder("memberPw", true);
      } else {
        setBorder("memberPw", regex.pw.test(value));
      }

      if (memberPwCheck && memberPwCheck.value.length > 0) {
        setBorder("memberPwCheck", memberPw.value === memberPwCheck.value);
      }
    });
  }

  /* 비밀번호 확인 검사 */
  if (memberPw && memberPwCheck) {
    memberPwCheck.addEventListener("input", () => {
      if (memberPwCheck.value.length === 0) {
        setBorder("memberPwCheck", true);
      } else {
        setBorder("memberPwCheck", memberPw.value === memberPwCheck.value);
      }
    });
  }

  /*
   * =========================================================
   * 이메일 형식 검사 및 중복확인 상태 초기화
   *
   * 중복확인을 완료한 후 이메일 입력값을 변경하면
   * 다시 중복확인을 진행하도록 상태를 초기화한다.
   * =========================================================
   */
  if (email) {
    email.addEventListener("input", () => {
      emailChecked = false;
      checkedEmailValue = "";

      const value = email.value.trim();

      if (value.length === 0) {
        setBorder("email", true);
      } else {
        setBorder("email", regex.email.test(value));
      }
    });
  }

  /* 전화번호 자동 입력 및 형식 검사 */
  if (phone) {
    // 처음 클릭하면 010- 자동 입력
    phone.addEventListener("focus", () => {
      if (phone.value === "") {
        phone.value = "010-";
      }
    });

    // 입력 시 하이픈 자동 생성 + 형식 검사
    phone.addEventListener("input", () => {
      let value = phone.value.replace(/\D/g, "");

      if (!value.startsWith("010")) {
        value = "010";
      }

      if (value.length <= 3) {
        phone.value = "010-";
      } else if (value.length <= 7) {
        phone.value = value.replace(/(\d{3})(\d+)/, "$1-$2");
      } else {
        phone.value = value.replace(/(\d{3})(\d{4})(\d{0,4})/, "$1-$2-$3");
      }

      // 선택 입력이므로 비어있거나 010-만 있으면 통과
      if (phone.value === "" || phone.value === "010-") {
        setBorder("phone", true);
      } else {
        setBorder("phone", regex.phone.test(phone.value));
      }
    });
  }

  /* 사업자등록번호 자동 하이픈 + 변경 시 중복확인 초기화 */
  if (businessNumber) {
    businessNumber.addEventListener("input", () => {
      businessNumberChecked = false;
      checkedBusinessNumberValue = "";
      resetBusinessVerification();

      let value = businessNumber.value.replace(/\D/g, "");

      if (value.length <= 3) {
        businessNumber.value = value;
      } else if (value.length <= 5) {
        businessNumber.value = value.replace(/(\d{3})(\d+)/, "$1-$2");
      } else {
        businessNumber.value = value.slice(0, 10).replace(/(\d{3})(\d{2})(\d{0,5})/, "$1-$2-$3");
      }

      if (businessNumber.value.length === 0) {
        setBorder("businessNumber", true);
      } else {
        setBorder("businessNumber", regex.businessNumber.test(businessNumber.value));
      }
    });
  }

  /* 대표자명 변경 시 사업자 인증 초기화 */
  if (representativeName) {
    representativeName.addEventListener("input", () => {
      resetBusinessVerification();
    });
  }

  /* 개업일 숫자만 입력 */
  if (openDate) {
    openDate.addEventListener("input", () => {
      resetBusinessVerification();

      openDate.value = openDate.value.replace(/\D/g, "").slice(0, 8);

      if (openDate.value.length === 0) {
        setBorder("openDate", true);
      } else {
        setBorder("openDate", regex.openDate.test(openDate.value));
      }
    });
  }

  /* 프로필 이미지 파일명 표시 */
  if (profileImageFile && profileFileName) {
    profileImageFile.addEventListener("change", () => {
      profileFileName.textContent = profileImageFile.files.length > 0 ? profileImageFile.files[0].name : "선택된 파일 없음";
    });
  }

  /* 사업자등록증 파일명 표시 */
  if (licenseFile && licenseFileName) {
    licenseFile.addEventListener("change", () => {
      if (licenseFile.files.length === 0) {
        licenseFileName.textContent = "선택된 파일 없음";
        return;
      }

      const file = licenseFile.files[0];
      const allowedExtensions = ["pdf", "jpg", "jpeg", "png"];
      const extension = file.name.split(".").pop().toLowerCase();

      if (!allowedExtensions.includes(extension)) {
        showAlert("사업자등록증은 PDF, JPG, JPEG, PNG 파일만 등록할 수 있습니다.", "warning");

        licenseFile.value = "";
        licenseFileName.textContent = "선택된 파일 없음";

        return;
      }

      licenseFileName.textContent = file.name;
    });
  }

  /* 최종 회원가입 유효성 검사*/
  async function validateJoin() {
    const currentJoinType = joinType ? joinType.value : "USER";

    const isBusinessJoin = currentJoinType === "BUSINESS";

    const idValue = memberId.value.trim();
    const pwValue = memberPw.value.trim();
    const pwCheckValue = memberPwCheck.value.trim();
    const emailValue = email.value.trim();
    const phoneValue = phone.value.trim();

    /* ---------- 공통 필드 검증 (일반/사업자 동일) ---------- */

    if (!regex.id.test(idValue)) {
      await showAlert("아이디는 5~12자의 소문자/숫자만 가능합니다.", "warning");
      focusInput("memberId");
      return false;
    }

    if (!idChecked || checkedIdValue !== idValue) {
      await showAlert("아이디 중복확인을 해주세요.", "warning");
      focusInput("memberId");
      return false;
    }

    if (!regex.pw.test(pwValue)) {
      await showAlert("비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다.", "warning");
      focusInput("memberPw");
      return false;
    }

    if (pwValue !== pwCheckValue) {
      await showAlert("비밀번호가 일치하지 않습니다.", "warning");
      focusInput("memberPwCheck");
      return false;
    }

    if (!regex.email.test(emailValue)) {
      await showAlert("이메일 형식이 올바르지 않습니다.", "warning");
      focusInput("email");
      return false;
    }

    /*
     * =========================================================
     * 이메일 중복확인 여부 검사
     *
     * 중복확인을 완료하지 않았거나 중복확인 후 이메일을
     * 변경한 경우 회원가입을 진행하지 않는다.
     * =========================================================
     */
    if (!emailChecked || checkedEmailValue !== emailValue) {
      await showAlert("이메일 중복확인을 해주세요.", "warning");
      focusInput("email");
      return false;
    }

    /* 일반회원은 전화번호 필수 */
    if (!isBusinessJoin && (phoneValue === "" || phoneValue === "010-")) {
      await showAlert("전화번호를 입력해주세요.", "warning");
      focusInput("phone");
      return false;
    }

    // 010-만 입력된 경우 빈 값으로 처리
    if (phoneValue === "010-") {
      phone.value = "";
    } else if (phoneValue !== "" && !regex.phone.test(phoneValue)) {
      phone.value = "";
      await showAlert("전화번호는 010-1234-5678 형식으로 입력해주세요.", "warning");
      focusInput("phone");
      return false;
    }

    /* ---------- 유형별 분기 검증 ---------- */

    if (isBusinessJoin) {
      return await validateBusinessFields();
    }

    return await validateUserFields();
  }

  /* 일반회원 전용 - OTT 선택 검증 */
  async function validateUserFields() {
    const nameValue = memberName ? memberName.value.trim() : "";

    const nicknameValue = nickname ? nickname.value.trim() : "";

    /* 이름 */
    if (nameValue.length < 2) {
      await showAlert("이름은 2자 이상 입력해주세요.", "warning");
      focusInput("memberName");

      return false;
    }

    /* 닉네임 */
    if (!regex.nick.test(nicknameValue)) {
      await showAlert("닉네임은 2~10자의 한글/영문/숫자만 가능합니다.", "warning");
      focusInput("nickname");

      return false;
    }

    /* 닉네임 중복확인 */
    if (!nicknameChecked || checkedNicknameValue !== nicknameValue) {
      await showAlert("닉네임 중복확인을 해주세요.", "warning");
      focusInput("nickname");

      return false;
    }

    /* OTT */
    return await validateUserOttFields();
  }

  async function validateUserOttFields() {
    const ottCount = Array.from(ottCheckboxes).filter((checkbox) => checkbox.checked).length;

    const noOttSelected = noOttCheckbox && noOttCheckbox.checked;

    /*
     * 실제 OTT 또는 OTT 없음 중 하나는 반드시 선택한다.
     */
    if (ottCount < 1 && !noOttSelected) {
      await showAlert("사용 중인 OTT를 선택하거나 OTT 없음을 선택해주세요.", "warning");
      return false;
    }

    return true;
  }

  /* 사업자 전용 - 사업자/정산 정보 검증 */
  async function validateBusinessFields() {
    const businessName = document.getElementById("businessName");

    const representativeName = document.getElementById("representativeName");

    const openDate = document.getElementById("openDate");

    const licenseFile = document.getElementById("licenseFile");

    const bankName = document.getElementById("bankName");

    const accountNumber = document.getElementById("accountNumber");

    const accountHolder = document.getElementById("accountHolder");

    const businessNumberValue = businessNumber ? businessNumber.value.trim() : "";

    /* 상호명 */
    if (!businessName || businessName.value.trim().length === 0) {
      await showAlert("상호명을 입력해주세요.", "warning");
      focusInput("businessName");

      return false;
    }

    /* 대표자명 */
    if (!representativeName || representativeName.value.trim().length === 0) {
      await showAlert("대표자명을 입력해주세요.", "warning");
      focusInput("representativeName");

      return false;
    }

    /* 개업일 */
    if (!openDate || !regex.openDate.test(openDate.value.trim())) {
      await showAlert("개업일은 YYYYMMDD 형식의 숫자 8자리로 입력해주세요.", "warning");
      focusInput("openDate");

      return false;
    }

    /* 사업자등록번호 */
    if (!regex.businessNumber.test(businessNumberValue)) {
      await showAlert("사업자등록번호는 000-00-00000 형식으로 입력해주세요.", "warning");
      focusInput("businessNumber");

      return false;
    }

    /* 사업자번호 중복 확인 */
    if (!businessNumberChecked || checkedBusinessNumberValue !== businessNumberValue) {
      await showAlert("사업자등록번호 중복확인을 해주세요.", "warning");
      focusInput("businessNumber");

      return false;
    }

    /* 국세청 사업자 진위확인 */
    if (!businessVerified || verifiedBusinessNumber !== businessNumberValue || verifiedRepresentativeName !== representativeName.value.trim() || verifiedOpenDate !== openDate.value.trim()) {
      await showAlert("사업자 정보 인증을 완료해주세요.", "warning");

      return false;
    }

    /* 사업자등록증 */
    if (!licenseFile || licenseFile.files.length === 0) {
      await showAlert("사업자등록증을 첨부해주세요.", "warning");
      focusInput("licenseFile");

      return false;
    }

    const license = licenseFile.files[0];
    const extension = license.name.split(".").pop().toLowerCase();

    const allowedExtensions = ["pdf", "jpg", "jpeg", "png"];

    if (!allowedExtensions.includes(extension)) {
      await showAlert("사업자등록증은 PDF, JPG, JPEG, PNG 파일만 등록할 수 있습니다.", "warning");

      return false;
    }

    /* 은행명 */
    if (!bankName || bankName.value.trim().length === 0) {
      await showAlert("은행명을 입력해주세요.", "warning");
      focusInput("bankName");

      return false;
    }

    /* 계좌번호 */
    if (!accountNumber || accountNumber.value.trim().length === 0) {
      await showAlert("계좌번호를 입력해주세요.", "warning");
      focusInput("accountNumber");

      return false;
    }

    /* 예금주 */
    if (!accountHolder || accountHolder.value.trim().length === 0) {
      await showAlert("예금주를 입력해주세요.", "warning");
      focusInput("accountHolder");

      return false;
    }

    return true;
  }

  /* SUBMIT HANDLER */
  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (await validateJoin()) {
      form.submit();
    }
  });
});

/* LOGIN PAGE - 정지/탈퇴 안내 및 복구 모달 */
document.addEventListener("DOMContentLoaded", () => {
  initLoginPageAlerts();
});

async function initLoginPageAlerts() {
  const authContainer = document.querySelector(".auth-container");

  if (!authContainer) {
    return;
  }

  const message = authContainer.dataset.message;

  const errorMessage = authContainer.dataset.errorMessage;

  const restoredMessage = authContainer.dataset.restoredMessage;

  const blockedMessage = authContainer.dataset.blockedMessage;

  const withdrawnMessage = authContainer.dataset.withdrawnMessage;

  // 우선순위대로 하나씩만 알림 (서로 겹칠 일은 없지만 방어적으로 순서 지정)
  if (message) {
    await showAlert(message, "info");
  } else if (errorMessage) {
    await showAlert(errorMessage, "error");
  } else if (restoredMessage) {
    await showAlert(restoredMessage, "success");
  } else if (blockedMessage) {
    await showAlert(blockedMessage, "warning");
  } else if (withdrawnMessage) {
    await openRestoreConfirm(withdrawnMessage);
  }

  initRestoreModal();
}

async function openRestoreConfirm(withdrawnMessage) {
  const restoreModal = document.getElementById("restoreModal");

  if (!restoreModal) {
    return;
  }

  const wantsRestore = await showConfirm(withdrawnMessage + "\n\n계정을 복구하시겠습니까?", "warning");

  if (wantsRestore) {
    restoreModal.classList.add("active");
  }
}

function initRestoreModal() {
  const input = document.getElementById("restoreConfirmInput");

  const submitBtn = document.getElementById("restoreSubmitBtn");

  if (!input || !submitBtn) {
    return;
  }

  input.addEventListener("input", () => {
    const isMatch = input.value.trim() === "복구";

    submitBtn.disabled = !isMatch;
    submitBtn.classList.toggle("enabled", isMatch);
  });
}

function closeRestoreModal() {
  const restoreModal = document.getElementById("restoreModal");

  if (restoreModal) {
    restoreModal.classList.remove("active");
  }
}

// 회원가입 유형(일반/사업자) 탭 전환
function switchJoinType(type) {
  document.getElementById("joinType").value = type;

  document.querySelectorAll(".join-tab-btn").forEach(function (btn) {
    btn.classList.toggle("active", btn.dataset.type === type);
  });

  var userFields = document.getElementById("userOnlyFields");

  var userBasicFields = document.getElementById("userBasicFields");

  var businessFields = document.getElementById("businessOnlyFields");

  var memberName = document.getElementById("memberName");

  var nickname = document.getElementById("nickname");

  var phone = document.getElementById("phone");

  var businessName = document.getElementById("businessName");

  var businessNumber = document.getElementById("businessNumber");

  var bankName = document.getElementById("bankName");

  var accountNumber = document.getElementById("accountNumber");

  var accountHolder = document.getElementById("accountHolder");

  var representativeName = document.getElementById("representativeName");

  var openDate = document.getElementById("openDate");

  var licenseFile = document.getElementById("licenseFile");

  if (type === "BUSINESS") {
    userBasicFields.classList.remove("active");
    userFields.classList.remove("active");
    businessFields.classList.add("active");

    // 숨겨진 일반회원 전용 필드는 제출 시 막히지 않도록 required 해제할 것 없음(체크박스라 required 아님)

    // 일반회원 전용 필수 해제
    memberName.required = false;
    nickname.required = false;
    phone.required = false;

    memberName.disabled = true;
    nickname.disabled = true;

    // 사업자 전용 필드는 필수값으로 전환
    businessName.required = true;
    representativeName.required = true;
    openDate.required = true;
    businessNumber.required = true;
    licenseFile.required = true;
    bankName.required = true;
    accountNumber.required = true;
    accountHolder.required = true;
  } else {
    businessFields.classList.remove("active");
    userFields.classList.add("active");
    userBasicFields.classList.add("active");

    // 일반회원 전용 필수
    memberName.required = true;
    nickname.required = true;
    phone.required = true;

    memberName.disabled = false;
    nickname.disabled = false;

    // 일반회원 전환 시 사업자 필드 required 해제 (숨겨진 상태라 제출 막힘 방지)
    businessName.required = false;
    representativeName.required = false;
    openDate.required = false;
    businessNumber.required = false;
    licenseFile.required = false;
    bankName.required = false;
    accountNumber.required = false;
    accountHolder.required = false;
  }
}

// 유효성 검사 실패로 폼이 다시 렌더링된 경우, 이전에 선택했던 탭을 복원
(function restoreJoinType() {
  var previousJoinType = "${joinType}";

  if (previousJoinType === "BUSINESS") {
    switchJoinType("BUSINESS");
  }
})();

/*
 * =========================================================
 * SNS 로그인 최초 OTT 선택 화면 (selectOtt.jsp)
 *
 * - 아직 이메일이 없는 SNS 자동가입 회원에게만 서버가
 *   #email 입력창을 렌더링해준다. 존재 여부로 필요성을 판단한다.
 * - 이메일 입력이 있는 경우, join.jsp와 동일하게
 *   checkEmail() 중복확인을 완료해야 제출할 수 있다.
 * - "이용 중인 OTT 없음"(#snsNoOtt)을 선택하면 실제 OTT 선택은
 *   모두 해제/비활성화된다. OTT를 1개 이상 선택하거나
 *   "없음"을 선택해야 제출할 수 있다 (join.jsp의 noOtt와 동일한 정책).
 * =========================================================
 */
document.addEventListener("DOMContentLoaded", () => {
  initSnsOttSelect();
});

function initSnsOttSelect() {
  const form = document.getElementById("snsOttForm");

  if (!form) {
    return;
  }

  const optionList = document.getElementById("snsOttOptionList");
  const countEl = document.getElementById("snsOttCount");
  const noOttCheckbox = document.getElementById("snsNoOtt");
  const emailInput = document.getElementById("email");

  const platformCheckboxes = optionList
    ? optionList.querySelectorAll('input[name="platformNoList"]')
    : [];

  /* 이메일 변경 시 중복확인 상태 초기화 (join.jsp와 동일한 방식) */
  if (emailInput) {
    emailInput.addEventListener("input", () => {
      emailChecked = false;
      checkedEmailValue = "";

      const value = emailInput.value.trim();

      if (value.length === 0) {
        setBorder("email", true);
      } else {
        setBorder("email", regex.email.test(value));
      }
    });
  }

  /* 선택 개수 카운터 갱신 */
  function updateSnsOttCount() {
    if (!optionList || !countEl) {
      return;
    }

    const checked = optionList.querySelectorAll(
      'input[name="platformNoList"]:checked',
    ).length;

    countEl.textContent = checked + "개 선택";
  }

  /*
   * [수정] 예전에는 "OTT 없음"을 체크하면 플랫폼 체크박스들을
   * disabled 처리했는데, 그 상태 전환 타이밍 때문에 "OTT 없음"을
   * 클릭해도 바로 체크 표시가 되지 않고 한 번 더 눌러야 선택되는
   * 것처럼 보이는 문제가 있었다(마이페이지에서 먼저 발견/수정됨).
   * 회원가입(join.jsp)·마이페이지와 동일하게 "체크 시 반대편
   * 선택만 해제"하는 단순한 방식으로 통일한다.
   */
  if (noOttCheckbox) {
    noOttCheckbox.addEventListener("change", () => {
      if (noOttCheckbox.checked) {
        platformCheckboxes.forEach((checkbox) => {
          checkbox.checked = false;
        });
      }

      updateSnsOttCount();
    });
  }

  platformCheckboxes.forEach((checkbox) => {
    checkbox.addEventListener("change", () => {
      if (checkbox.checked && noOttCheckbox) {
        noOttCheckbox.checked = false;
      }

      updateSnsOttCount();
    });
  });

  updateSnsOttCount();

  form.addEventListener("submit", async (event) => {
    event.preventDefault();

    if (await validateSnsOttSelect()) {
      form.submit();
    }
  });
}

async function validateSnsOttSelect() {
  const emailInput = document.getElementById("email");

  if (emailInput) {
    const email = emailInput.value.trim();

    if (email === "") {
      await showAlert("이메일을 입력해주세요.", "warning");
      emailInput.focus();
      return false;
    }

    if (!regex.email.test(email)) {
      await showAlert("올바른 이메일 형식으로 입력해주세요.", "warning");
      emailInput.focus();
      return false;
    }

    /*
     * 이메일 중복확인 여부 검사
     * join.jsp와 동일하게, 중복확인을 완료하지 않았거나
     * 중복확인 후 이메일을 변경한 경우 진행하지 않는다.
     */
    if (!emailChecked || checkedEmailValue !== email) {
      await showAlert("이메일 중복확인을 해주세요.", "warning");
      emailInput.focus();
      return false;
    }
  }

  const checkedList = document.querySelectorAll(
    'input[name="platformNoList"]:checked',
  );

  const noOttCheckbox = document.getElementById("snsNoOtt");
  const selectedNoOtt = !!(noOttCheckbox && noOttCheckbox.checked);

  if (checkedList.length === 0 && !selectedNoOtt) {
    await showAlert("이용 중인 OTT를 선택하거나 'OTT 없음'을 선택해주세요.", "warning");
    return false;
  }

  return true;
}

document.addEventListener("DOMContentLoaded", () => {

    const changePwForm = document.getElementById("changePwForm");

    if (!changePwForm) {
        return;
    }


    const newPassword = document.getElementById("newPassword");
    const confirmPassword = document.getElementById("confirmPassword");


    changePwForm.addEventListener("submit", async (event) => {

        event.preventDefault();

        const password = newPassword.value.trim();
        const confirmValue = confirmPassword.value.trim();


        if (!regex.pw.test(password)) {

            await showAlert(
                "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다.",
                "warning"
            );

            newPassword.focus();

            return;
        }


        if (password !== confirmValue) {

            await showAlert("비밀번호가 일치하지 않습니다.", "warning");

            confirmPassword.focus();

            return;
        }

        changePwForm.submit();

    });

});