/**
 * ODITJI 라이트/다크 모드 토글
 *
 * - 기본값은 "다크 모드"입니다. localStorage에 저장된 값이 없으면
 *   항상 dark로 시작합니다(요구사항: "처음 웹에 들어갈 때는 다크모드로 시작").
 * - 실제 테마 적용은 head-assets.jsp 상단의 인라인 스크립트가 CSS 로드보다
 *   먼저 <html data-theme="..."> 를 설정해 깜빡임(FOUC)을 막습니다.
 *   이 파일은 그 값과 동기화된 상태에서, 헤더의 토글 버튼 클릭을 처리하고
 *   버튼의 aria-pressed/라벨을 갱신하는 역할만 합니다.
 */
(function () {
  var STORAGE_KEY = "oditji-theme";

  function getCurrentTheme() {
    var attr = document.documentElement.getAttribute("data-theme");
    return attr === "light" ? "light" : "dark";
  }

  function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);

    try {
      localStorage.setItem(STORAGE_KEY, theme);
    } catch (e) {
      /* 프라이빗 브라우징 등으로 localStorage 사용이 막힌 경우,
         저장은 못 해도 현재 화면 전환 자체는 계속 동작하도록 무시합니다. */
    }

    syncToggleButtons(theme);
  }

  function syncToggleButtons(theme) {
    var buttons = document.querySelectorAll("#themeToggleBtn, .theme-toggle-btn");

    buttons.forEach(function (btn) {
      var isLight = theme === "light";
      btn.setAttribute("aria-pressed", String(isLight));
      btn.setAttribute("aria-label", isLight ? "다크 모드로 전환" : "라이트 모드로 전환");
      btn.setAttribute("title", isLight ? "다크 모드로 전환" : "라이트 모드로 전환");
    });
  }

  function toggleTheme() {
    var next = getCurrentTheme() === "light" ? "dark" : "light";
    applyTheme(next);
  }

  function initThemeToggle() {
    syncToggleButtons(getCurrentTheme());

    /* [수정] 프로필 드롭다운 안에 라이트/다크 전환 항목을 추가했는데,
       common.js의 initProfileDropdown()이 .profile-dropdown 안 클릭을
       전부 stopPropagation()으로 막아서(드롭다운이 안 닫히게 하려는
       목적) document에 붙인 버블 단계 리스너까지 이벤트가 올라오지
       못했다. 캡처 단계(true)로 등록하면 버블이 시작되기 전에 먼저
       실행되므로 stopPropagation의 영향을 받지 않는다. */
    document.addEventListener(
      "click",
      function (event) {
        var btn = event.target.closest("#themeToggleBtn, .theme-toggle-btn");
        if (btn) {
          toggleTheme();
        }
      },
      true
    );
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initThemeToggle);
  } else {
    initThemeToggle();
  }
})();
