/**
 * ===========================================
 * ODITJI Common Header Script
 * ===========================================
 */

document.addEventListener("DOMContentLoaded", () => {
  initProfileDropdown();
  initHeaderScroll();
  initActiveMenu();
});

/**
 * ===========================================
 * 프로필 드롭다운
 * ===========================================
 */

function initProfileDropdown() {
  const profileBtn = document.getElementById("profileBtn");
  const dropdown = document.querySelector(".profile-dropdown");

  if (!profileBtn || !dropdown) return;

  profileBtn.addEventListener("click", (e) => {
    e.stopPropagation();

    dropdown.classList.toggle("open");
  });

  document.addEventListener("click", () => {
    dropdown.classList.remove("open");
  });

  dropdown.addEventListener("click", (e) => {
    e.stopPropagation();
  });

  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") {
      dropdown.classList.remove("open");
    }
  });
}

/**
 * ===========================================
 * Header Scroll Effect
 * ===========================================
 */

function initHeaderScroll() {
  const header = document.querySelector(".header");

  if (!header) return;

  let lastScroll = 0;

  window.addEventListener("scroll", () => {
    const currentScroll = window.pageYOffset;

    /**
     * 그림자
     */

    if (currentScroll > 10) {
      header.classList.add("scrolled");
    } else {
      header.classList.remove("scrolled");
    }

    /**
     * 스크롤 방향
     */

    if (currentScroll > lastScroll && currentScroll > 150) {
      header.classList.add("hide");
      header.classList.remove("show");
    } else {
      header.classList.remove("hide");
      header.classList.add("show");
    }

    lastScroll = currentScroll;
  });
}

/**
 * ===========================================
 * 현재 메뉴 Active
 * ===========================================
 */

function initActiveMenu() {
  const currentPath = window.location.pathname;

  document.querySelectorAll(".header-nav a").forEach((menu) => {
    const href = menu.getAttribute("href");

    if (!href) return;

    if (currentPath === href || currentPath.startsWith(href)) {
      menu.classList.add("active");
    }
  });
}

/**
 * ===========================================
 * 검색창 Enter
 * ===========================================
 */

const searchInput = document.querySelector(".header-search input");

if (searchInput) {
  searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      e.target.closest("form").submit();
    }
  });
}

/**
 * ===========================================
 * 검색창 포커스 애니메이션
 * ===========================================
 */

if (searchInput) {
  searchInput.addEventListener("focus", () => {
    searchInput.parentElement.classList.add("focus");
  });

  searchInput.addEventListener("blur", () => {
    searchInput.parentElement.classList.remove("focus");
  });
}
