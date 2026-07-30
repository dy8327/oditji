/**
 * ===========================================
 * ODITJI Common Header Script
 * ===========================================
 */

/**
 * SweetAlert2가 프로젝트 자체 모달(마이페이지 모달, 주문취소 모달,
 * 리뷰 모달, OTT 선택 모달, 탈퇴/복구 모달 등) 뒤에 가려지는 문제 방지.
 *
 * 프로젝트의 커스텀 모달들은 각자의 CSS에서 자체적인 z-index로
 * 쌓임 맥락(stacking context)을 만드는 경우가 많은데, SweetAlert2의
 * 기본 z-index(1060)가 그보다 낮으면 모달이 열려 있는 상태에서 띄운
 * Swal.fire()가 모달 뒤에 렌더링되어 화면에서 보이지 않는다
 * (모달을 닫아야 비로소 알림이 보이는 것처럼 느껴짐).
 *
 * SweetAlert2는 항상 <body> 바로 아래에 자신의 컨테이너(.swal2-container)를
 * 추가하므로, 이 컨테이너의 z-index를 프로젝트에서 사용하는 어떤 모달보다도
 * 높게 강제로 고정해두면 어떤 페이지의 어떤 모달 위에서 호출되어도 항상
 * 최상단에 노출된다. 기존 CSS/JSP를 건드리지 않고 JS에서 스타일을 한 번만
 * 주입하는 방식으로 처리한다.
 */
(function ensureSwalOnTop() {
  if (document.getElementById("swal2-zindex-fix")) {
    return;
  }

  const style = document.createElement("style");
  style.id = "swal2-zindex-fix";
  style.textContent = ".swal2-container { z-index: 2147483647 !important; }";

  document.head.appendChild(style);
})();

/**
 * SweetAlert2 공통 알림/확인 헬퍼
 * 프로젝트 전체에서 기본 브라우저 alert()/confirm() 대신 이 함수들을 사용한다.
 * (SweetAlert2 CDN은 header.jsp에 이미 포함되어 있음)
 */
function showAlert(message, icon) {
  return Swal.fire({
    icon: icon || "info",
    text: message,
    confirmButtonText: "확인",
  });
}

function showConfirm(message, icon, confirmButtonText, cancelButtonText) {
  return Swal.fire({
    icon: icon || "warning",
    text: message,
    showCancelButton: true,
    confirmButtonText: confirmButtonText || "확인",
    cancelButtonText: cancelButtonText || "취소",
  }).then(function (result) {
    return result.isConfirmed;
  });
}

/**
 * onsubmit="return confirm(...)" / onclick="return confirm(...)" 형태의 인라인
 * 핸들러를 대체하기 위한 헬퍼. confirm()은 동기 함수라 값을 즉시 반환할 수 있지만
 * SweetAlert2는 비동기이므로, 일단 기본 동작(form submit)을 막아두고 사용자가
 * "확인"을 누르면 그때 실제로 form을 제출한다. 반드시 false를 반환해서 원래의
 * 동기 submit/클릭 동작을 막는다.
 *
 * - onsubmit에서 호출된 경우(event.target이 form): form.submit()으로 제출한다.
 *   (form.submit()은 submit 이벤트를 다시 발생시키지 않으므로 onsubmit 핸들러가
 *    무한 반복 호출되는 것을 막아준다.)
 * - onclick에서 호출된 경우(제출 버튼 클릭): name="action" value="approve" 처럼
 *   버튼 자신의 name/value가 서버 처리에 필요한 경우가 있어, 그 값이 함께
 *   전송되도록 form.requestSubmit(버튼)으로 제출한다.
 */
function confirmAndSubmit(event, message, icon) {
  if (event && typeof event.preventDefault === "function") {
    event.preventDefault();
  }

  var isFormSubmitEvent = event && event.target && event.target.tagName === "FORM";
  var form = isFormSubmitEvent
    ? event.target
    : (event && event.currentTarget && event.currentTarget.form) || (event && event.currentTarget && event.currentTarget.closest && event.currentTarget.closest("form"));
  var submitter = !isFormSubmitEvent && event ? event.currentTarget : null;

  showConfirm(message, icon).then(function (confirmed) {
    if (!confirmed || !form) {
      return;
    }

    if (submitter && typeof form.requestSubmit === "function") {
      form.requestSubmit(submitter);
    } else {
      form.submit();
    }
  });

  return false;
}

document.addEventListener("DOMContentLoaded", () => {
    initProfileDropdown();
    initNavigationDropdowns();

    // [추가] 상품 메뉴 안의 세부 카테고리 2단 메뉴를 초기화합니다.
    initGoodsCategoryMenu();

    initMobileNavigation();
    initMobileSearchToggle();
    initHeaderScroll();
    initBackToTop();
    initActiveMenu();
    initHeaderSearch();
    initMobileFilterToggle();
});

/** 프로필 드롭다운 */
function initProfileDropdown() {
  const profileBtn = document.getElementById("profileBtn");
  const dropdown = document.querySelector(".profile-dropdown");

  if (!profileBtn || !dropdown) return;

  profileBtn.addEventListener("click", (event) => {
    event.stopPropagation();

    const notificationDropdown = document.getElementById("notificationDropdown");
    const notificationButton = document.getElementById("notificationBtn");

    if (notificationDropdown) {
      notificationDropdown.classList.remove("open");
    }

    if (notificationButton) {
      notificationButton.setAttribute("aria-expanded", "false");
    }

    const opened = dropdown.classList.toggle("open");
    profileBtn.setAttribute("aria-expanded", String(opened));
  });

  dropdown.addEventListener("click", (event) => {
    event.stopPropagation();
  });

  document.addEventListener("click", () => {
    dropdown.classList.remove("open");
    profileBtn.setAttribute("aria-expanded", "false");
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      dropdown.classList.remove("open");
      profileBtn.setAttribute("aria-expanded", "false");
    }
  });
}

/** 콘텐츠/상품/이벤트 드롭다운 */
function initNavigationDropdowns() {
  const dropdownItems = document.querySelectorAll("[data-nav-dropdown]");

  dropdownItems.forEach((item) => {
    const trigger = item.querySelector(".header-nav-trigger");
    if (!trigger) return;

    trigger.addEventListener("click", (event) => {
      event.stopPropagation();
      const shouldOpen = !item.classList.contains("open");
      closeNavigationDropdowns(item);
      item.classList.toggle("open", shouldOpen);
      trigger.setAttribute("aria-expanded", String(shouldOpen));
    });
  });

  document.addEventListener("click", () => {
    closeNavigationDropdowns();
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeNavigationDropdowns();
    }
  });
}

/**
 * [추가] 상품 > 카테고리별 상품의 2단 메뉴를 관리합니다.
 * - 마우스를 올리면 임시로 표시
 * - 버튼을 클릭하면 고정
 * - ESC 또는 메뉴 바깥 클릭 시 원래 상태로 복귀
 */
function initGoodsCategoryMenu() {
  const categoryMenus = document.querySelectorAll("[data-category-menu]");

  categoryMenus.forEach((categoryMenu) => {
    const trigger = categoryMenu.querySelector(".header-category-trigger");

    if (!trigger) {
      return;
    }

    trigger.addEventListener("click", (event) => {
      event.stopPropagation();

      const shouldPin = !categoryMenu.classList.contains("pinned");

      closeGoodsCategoryMenus(categoryMenu);
      categoryMenu.classList.toggle("pinned", shouldPin);
      trigger.setAttribute("aria-expanded", String(shouldPin));
    });

    categoryMenu.addEventListener("click", (event) => {
      event.stopPropagation();
    });
  });

  document.addEventListener("click", () => {
    closeGoodsCategoryMenus();
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
      closeGoodsCategoryMenus();
    }
  });
}

/** [추가] 고정된 상품 세부 카테고리 메뉴를 닫습니다. */
function closeGoodsCategoryMenus(exceptMenu = null) {
  document.querySelectorAll("[data-category-menu]").forEach((categoryMenu) => {
    if (categoryMenu === exceptMenu) {
      return;
    }

    categoryMenu.classList.remove("pinned");

    const trigger = categoryMenu.querySelector(".header-category-trigger");

    if (trigger) {
      trigger.setAttribute("aria-expanded", "false");
    }
  });
}

function closeNavigationDropdowns(exceptItem = null) {
  document.querySelectorAll("[data-nav-dropdown]").forEach((item) => {
    if (item === exceptItem) return;
    item.classList.remove("open");
    const trigger = item.querySelector(".header-nav-trigger");
    if (trigger) trigger.setAttribute("aria-expanded", "false");
  });
}

/** 모바일 전체 메뉴 */
function initMobileNavigation() {
  const toggle = document.getElementById("headerNavToggle");
  const nav = document.getElementById("headerNav");

  if (!toggle || !nav) return;

  toggle.addEventListener("click", (event) => {
    event.stopPropagation();
    const opened = nav.classList.toggle("open");
    toggle.setAttribute("aria-expanded", String(opened));
  });

  nav.addEventListener("click", (event) => {
    event.stopPropagation();
  });

  document.addEventListener("click", () => {
    if (window.innerWidth <= 992) {
      nav.classList.remove("open");
      toggle.setAttribute("aria-expanded", "false");
      closeNavigationDropdowns();
    }
  });

  window.addEventListener("resize", () => {
    if (window.innerWidth > 992) {
      nav.classList.remove("open");
      toggle.setAttribute("aria-expanded", "false");
      closeNavigationDropdowns();
    }
  });
}

/**
 * 모바일(768px 이하) 헤더 검색 토글.
 * headerSearchToggle 버튼을 누르면 header-search(#headerSearch)가
 * 헤더 바로 아래로 펼쳐지고, 다시 누르거나 바깥을 클릭하면 닫힌다.
 * 열릴 때는 입력창에 자동으로 포커스를 준다.
 */
function initMobileSearchToggle() {
    const toggle = document.getElementById("headerSearchToggle");
    const search = document.getElementById("headerSearch");

    if (!toggle || !search) return;

    const input = search.querySelector("input");

    toggle.addEventListener("click", (event) => {
        event.stopPropagation();
        const opened = search.classList.toggle("open");
        toggle.setAttribute("aria-expanded", String(opened));

        if (opened) {
            const nav = document.getElementById("headerNav");
            const navToggle = document.getElementById("headerNavToggle");
            nav?.classList.remove("open");
            navToggle?.setAttribute("aria-expanded", "false");
            closeNavigationDropdowns();

            if (input && window.innerWidth <= 768) {
                input.focus();
            }
        }
    });

    search.addEventListener("click", (event) => {
        event.stopPropagation();
    });

    document.addEventListener("click", () => {
        if (window.innerWidth <= 768) {
            closeMobileSearch();
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeMobileSearch();
        }
    });

    window.addEventListener("resize", () => {
        if (window.innerWidth > 768) {
            closeMobileSearch();
        }
    });
}

function closeMobileSearch() {
    const toggle = document.getElementById("headerSearchToggle");
    const search = document.getElementById("headerSearch");
    if (!toggle || !search) return;

    search.classList.remove("open");
    toggle.setAttribute("aria-expanded", "false");
}

/** 스크롤 효과 (헤더 숨김에 맞춰 맨 위로 버튼도 함께 노출) */
function initHeaderScroll() {
  const header = document.querySelector(".header");
  if (!header) return;

    const backToTopBtn = document.getElementById("backToTopBtn");

    let lastScroll = 0;

  window.addEventListener("scroll", () => {
    const currentScroll = window.pageYOffset;
    header.classList.toggle("scrolled", currentScroll > 10);

        if (currentScroll > lastScroll && currentScroll > 150) {
            header.classList.add("hide");
            header.classList.remove("show");
            backToTopBtn?.classList.add("show");
        } else {
            header.classList.remove("hide");
            header.classList.add("show");
            if (currentScroll <= 150) {
                backToTopBtn?.classList.remove("show");
            }
        }

    lastScroll = currentScroll;
  });
}

/** 맨 위로 이동 버튼 클릭 처리 */
function initBackToTop() {
    const backToTopBtn = document.getElementById("backToTopBtn");
    if (!backToTopBtn) return;

    backToTopBtn.addEventListener("click", () => {
        window.scrollTo({ top: 0, behavior: "smooth" });
    });
}

/** 현재 경로에 맞는 1차 메뉴 활성화 */
function initActiveMenu() {
  const currentPath = window.location.pathname;
  const currentSearch = window.location.search;
  const directLinks = document.querySelectorAll(".header-nav-link[href], .header-submenu a, .header-category-submenu a");

  directLinks.forEach((menu) => {
    const url = new URL(menu.href, window.location.origin);
    const pathMatches = currentPath === url.pathname || currentPath.startsWith(url.pathname + "/");
    const typeMatches = !url.searchParams.has("type") || currentSearch.includes(`type=${url.searchParams.get("type")}`);
    const periodMatches = !url.searchParams.has("period") || currentSearch.includes(`period=${url.searchParams.get("period")}`);

    if (pathMatches && typeMatches && periodMatches) {
      menu.classList.add("active");
      const parentDropdown = menu.closest("[data-nav-dropdown]");
      const parentTrigger = parentDropdown?.querySelector(".header-nav-trigger");
      if (parentTrigger) parentTrigger.classList.add("active");
    }
  });
}

/** 검색창 Enter 및 포커스 처리 */
function initHeaderSearch() {
  const searchInput = document.querySelector(".header-search input");
  if (!searchInput) return;

  searchInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.target.closest("form")?.submit();
    }
  });

  searchInput.addEventListener("focus", () => {
    searchInput.parentElement?.classList.add("focus");
  });

  searchInput.addEventListener("blur", () => {
    searchInput.parentElement?.classList.remove("focus");
  });
}

/**
 * 상품/콘텐츠/검색 목록 화면의 모바일 필터 사이드바 펼치기·접기.
 * data-mobile-filter-toggle 버튼을 클릭하면 가장 가까운 <aside>에
 * "is-open" 클래스를 토글해서 그 안의 필터 패널이 펼쳐지도록 한다.
 * 데스크톱 화면에서는 버튼 자체가 CSS로 숨겨져 있어 영향이 없다.
 */
function initMobileFilterToggle() {
    const toggleButtons = document.querySelectorAll(
        "[data-mobile-filter-toggle]"
    );

    toggleButtons.forEach((button) => {
        const sidebar = button.closest("aside") || button.parentElement;

        if (!sidebar) return;

        button.addEventListener("click", () => {
            const isOpen = sidebar.classList.toggle("is-open");
            button.setAttribute("aria-expanded", String(isOpen));
        });
    });

    // 화면을 데스크톱 크기로 늘렸을 때 접힘 상태가 남아 레이아웃이
    // 꼬이지 않도록, 데스크톱 폭으로 돌아가면 열림 상태를 초기화한다.
    window.addEventListener("resize", () => {
        if (window.innerWidth > 992) {
            document.querySelectorAll("aside.is-open").forEach((sidebar) => {
                sidebar.classList.remove("is-open");
                sidebar
                    .querySelectorAll("[data-mobile-filter-toggle]")
                    .forEach((button) => {
                        button.setAttribute("aria-expanded", "false");
                    });
            });
        }
    });
}
// 브라우저 뒤로가기로 이전 페이지가 표시되면 서버 상태 다시 확인
window.addEventListener("pageshow", function (event) {
  const navigation = performance.getEntriesByType("navigation")[0];

  if (event.persisted || (navigation && navigation.type === "back_forward")) {
    window.location.reload();
  }
});
