/* =========================================================
   REGEX RULES
========================================================= */
const regex = {
  id: /^[a-z0-9]{5,12}$/,
  pw: /^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,20}$/,
  nick: /^[a-zA-Z0-9가-힣]{2,10}$/,
  email: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  phone: /^010-\d{4}-\d{4}$/,
  businessNumber: /^\d{3}-\d{2}-\d{5}$/,
};

/* =========================================================
   DUPLICATE CHECK STATE
========================================================= */
let idChecked = false;
let nicknameChecked = false;
let businessNumberChecked = false;

let checkedIdValue = "";
let checkedNicknameValue = "";
let checkedBusinessNumberValue = "";

/* =========================================================
   DOM HELPER
========================================================= */
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

/* =========================================================
   ID CHECK
   Controller 응답: "Y" 또는 "N"
========================================================= */
async function checkId() {
  const memberId = getValue("memberId");

  if (!regex.id.test(memberId)) {
    alert("아이디는 5~12자의 소문자/숫자만 가능합니다.");
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
      alert("아이디 중복확인 요청 실패: " + response.status);
      idChecked = false;
      checkedIdValue = "";
      return;
    }

    if (result.trim() === "Y") {
      alert("사용 가능한 아이디입니다.");
      idChecked = true;
      checkedIdValue = memberId;
      setBorder("memberId", true);
      return;
    }

    if (result.trim() === "N") {
      alert("이미 사용 중인 아이디입니다.");
      idChecked = false;
      checkedIdValue = "";
      setBorder("memberId", false);
      focusInput("memberId");
      return;
    }

    alert("알 수 없는 서버 응답입니다: " + result);
    idChecked = false;
    checkedIdValue = "";
  } catch (error) {
    console.error("아이디 중복확인 오류:", error);
    alert("아이디 중복확인 중 오류가 발생했습니다.");
    idChecked = false;
    checkedIdValue = "";
  }
}

/* =========================================================
   NICKNAME CHECK
   Controller 응답: "Y" 또는 "N"
========================================================= */
async function checkNickname() {
  const nickname = getValue("nickname");

  if (!regex.nick.test(nickname)) {
    alert("닉네임은 2~10자의 한글/영문/숫자만 가능합니다.");
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
      alert("닉네임 중복확인 요청 실패: " + response.status);
      nicknameChecked = false;
      checkedNicknameValue = "";
      return;
    }

    if (result.trim() === "Y") {
      alert("사용 가능한 닉네임입니다.");
      nicknameChecked = true;
      checkedNicknameValue = nickname;
      setBorder("nickname", true);
      return;
    }

    if (result.trim() === "N") {
      alert("이미 사용 중인 닉네임입니다.");
      nicknameChecked = false;
      checkedNicknameValue = "";
      setBorder("nickname", false);
      focusInput("nickname");
      return;
    }

    alert("알 수 없는 서버 응답입니다: " + result);
    nicknameChecked = false;
    checkedNicknameValue = "";
  } catch (error) {
    console.error("닉네임 중복확인 오류:", error);
    alert("닉네임 중복확인 중 오류가 발생했습니다.");
    nicknameChecked = false;
    checkedNicknameValue = "";
  }
}

/* =========================================================
   BUSINESS NUMBER CHECK (사업자 전용)
   Controller 응답: "Y" 또는 "N"
========================================================= */
async function checkBusinessNumber() {
  const businessNumber = getValue("businessNumber");

  if (!regex.businessNumber.test(businessNumber)) {
    alert("사업자등록번호는 000-00-00000 형식으로 입력해주세요.");
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
      alert("사업자등록번호 중복확인 요청 실패: " + response.status);
      businessNumberChecked = false;
      checkedBusinessNumberValue = "";
      return;
    }

    if (result.trim() === "Y") {
      alert("등록 가능한 사업자등록번호입니다.");
      businessNumberChecked = true;
      checkedBusinessNumberValue = businessNumber;
      setBorder("businessNumber", true);
      return;
    }

    if (result.trim() === "N") {
      alert("이미 등록된 사업자등록번호입니다.");
      businessNumberChecked = false;
      checkedBusinessNumberValue = "";
      setBorder("businessNumber", false);
      focusInput("businessNumber");
      return;
    }

    alert("알 수 없는 서버 응답입니다: " + result);
    businessNumberChecked = false;
    checkedBusinessNumberValue = "";
  } catch (error) {
    console.error("사업자등록번호 중복확인 오류:", error);
    alert("사업자등록번호 중복확인 중 오류가 발생했습니다.");
    businessNumberChecked = false;
    checkedBusinessNumberValue = "";
  }
}

/* =========================================================
   INIT
========================================================= */
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
  const fileInput = document.getElementById("profileImageFile");
  const fileName = document.querySelector(".file-name");

  if (!form) {
    return;
  }

  /* =========================
     아이디 변경 시 중복확인 초기화
  ========================= */
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

  /* =========================
     닉네임 변경 시 중복확인 초기화
  ========================= */
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

  /* =========================
     비밀번호 형식 검사
  ========================= */
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

  /* =========================
     비밀번호 확인 검사
  ========================= */
  if (memberPw && memberPwCheck) {
    memberPwCheck.addEventListener("input", () => {
      if (memberPwCheck.value.length === 0) {
        setBorder("memberPwCheck", true);
      } else {
        setBorder("memberPwCheck", memberPw.value === memberPwCheck.value);
      }
    });
  }

  /* =========================
     이메일 형식 검사
  ========================= */
  if (email) {
    email.addEventListener("input", () => {
      const value = email.value.trim();

      if (value.length === 0) {
        setBorder("email", true);
      } else {
        setBorder("email", regex.email.test(value));
      }
    });
  }

  /* =========================
    전화번호 자동 입력 및 형식 검사
  ========================= */
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

  /* =========================
     사업자등록번호 자동 하이픈 + 변경 시 중복확인 초기화
  ========================= */
  if (businessNumber) {

      businessNumber.addEventListener("input", () => {

          businessNumberChecked = false;
          checkedBusinessNumberValue = "";

          let value = businessNumber.value.replace(/\D/g, "");

          if (value.length <= 3) {
              businessNumber.value = value;
          } else if (value.length <= 5) {
              businessNumber.value = value.replace(/(\d{3})(\d+)/, "$1-$2");
          } else {
              businessNumber.value = value
                  .slice(0, 10)
                  .replace(/(\d{3})(\d{2})(\d{0,5})/, "$1-$2-$3");
          }

          if (businessNumber.value.length === 0) {
              setBorder("businessNumber", true);
          } else {
              setBorder("businessNumber", regex.businessNumber.test(businessNumber.value));
          }
      });
  }



  /* =========================
     파일명 표시
  ========================= */
  if (fileInput && fileName) {
    fileInput.addEventListener("change", () => {
      fileName.textContent = fileInput.files.length ? fileInput.files[0].name : "선택된 파일 없음";
    });
  }

  /* =========================
     최종 회원가입 유효성 검사
  ========================= */
  function validateJoin() {
    const currentJoinType = joinType ? joinType.value : "USER";
    const isBusinessJoin = currentJoinType === "BUSINESS";

    const nameValue = memberName.value.trim();
    const idValue = memberId.value.trim();
    const pwValue = memberPw.value.trim();
    const pwCheckValue = memberPwCheck.value.trim();
    const nicknameValue = nickname.value.trim();
    const emailValue = email.value.trim();
    const phoneValue = phone.value.trim();

    /* ---------- 공통 필드 검증 (일반/사업자 동일) ---------- */

    if (nameValue.length < 2) {
      alert("이름은 2자 이상 입력해주세요.");
      focusInput("memberName");
      return false;
    }

    if (!regex.id.test(idValue)) {
      alert("아이디는 5~12자의 소문자/숫자만 가능합니다.");
      focusInput("memberId");
      return false;
    }

    if (!idChecked || checkedIdValue !== idValue) {
      alert("아이디 중복확인을 해주세요.");
      focusInput("memberId");
      return false;
    }

    if (!regex.pw.test(pwValue)) {
      alert("비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 합니다.");
      focusInput("memberPw");
      return false;
    }

    if (pwValue !== pwCheckValue) {
      alert("비밀번호가 일치하지 않습니다.");
      focusInput("memberPwCheck");
      return false;
    }

    if (!regex.nick.test(nicknameValue)) {
      alert("닉네임은 2~10자의 한글/영문/숫자만 가능합니다.");
      focusInput("nickname");
      return false;
    }

    if (!nicknameChecked || checkedNicknameValue !== nicknameValue) {
      alert("닉네임 중복확인을 해주세요.");
      focusInput("nickname");
      return false;
    }

    if (!regex.email.test(emailValue)) {
      alert("이메일 형식이 올바르지 않습니다.");
      focusInput("email");
      return false;
    }

    // 010-만 입력된 경우 빈 값으로 처리
    if (phoneValue === "010-") {
      phone.value = "";
    } else if (phoneValue !== "" && !regex.phone.test(phoneValue)) {
      phone.value = "";
      alert("전화번호는 010-1234-5678 형식으로 입력해주세요.");
      focusInput("phone");
      return false;
    }

    /* ---------- 유형별 분기 검증 ---------- */

    if (isBusinessJoin) {
      return validateBusinessFields();
    }

    return validateUserOttFields();
  }

  /* =========================
     일반회원 전용 - OTT 선택 검증
  ========================= */
  function validateUserOttFields() {
    const ottCount = Array.from(ottCheckboxes).filter((checkbox) => checkbox.checked).length;

    if (ottCount < 1) {
      alert("OTT는 최소 1개 이상 선택해야 합니다.");
      return false;
    }

    return true;
  }

  /* =========================
     사업자 전용 - 사업자/정산 정보 검증
  ========================= */
  function validateBusinessFields() {
    const businessName = document.getElementById("businessName");
    const bankName = document.getElementById("bankName");
    const accountNumber = document.getElementById("accountNumber");
    const accountHolder = document.getElementById("accountHolder");

    const businessNumberValue = businessNumber ? businessNumber.value.trim() : "";

    if (!businessName || businessName.value.trim().length === 0) {
      alert("상호명을 입력해주세요.");
      focusInput("businessName");
      return false;
    }

    if (!regex.businessNumber.test(businessNumberValue)) {
      alert("사업자등록번호는 000-00-00000 형식으로 입력해주세요.");
      focusInput("businessNumber");
      return false;
    }

    if (!businessNumberChecked || checkedBusinessNumberValue !== businessNumberValue) {
      alert("사업자등록번호 중복확인을 해주세요.");
      focusInput("businessNumber");
      return false;
    }

    if (!bankName || bankName.value.trim().length === 0) {
      alert("은행명을 입력해주세요.");
      focusInput("bankName");
      return false;
    }

    if (!accountNumber || accountNumber.value.trim().length === 0) {
      alert("계좌번호를 입력해주세요.");
      focusInput("accountNumber");
      return false;
    }

    if (!accountHolder || accountHolder.value.trim().length === 0) {
      alert("예금주를 입력해주세요.");
      focusInput("accountHolder");
      return false;
    }

    return true;
  }

  /* =========================
     SUBMIT HANDLER
  ========================= */
  form.addEventListener("submit", (event) => {
    if (!validateJoin()) {
      event.preventDefault();
    }
  });
});
