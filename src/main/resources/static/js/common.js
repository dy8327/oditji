/**
 * ===========================================
 * ODITJI Common Header Script
 * ===========================================
 */

document.addEventListener("DOMContentLoaded", () => {
    initProfileDropdown();
    initNavigationDropdowns();
    initMobileNavigation();
    initHeaderScroll();
    initActiveMenu();
    initHeaderSearch();
});

/** 프로필 드롭다운 */
function initProfileDropdown() {
    const profileBtn = document.getElementById("profileBtn");
    const dropdown = document.querySelector(".profile-dropdown");

    if (!profileBtn || !dropdown) return;

    profileBtn.addEventListener("click", (event) => {
        event.stopPropagation();

        const notificationDropdown = document.getElementById(
            "notificationDropdown"
        );
        const notificationButton = document.getElementById(
            "notificationBtn"
        );

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

/** 스크롤 효과 */
function initHeaderScroll() {
    const header = document.querySelector(".header");
    if (!header) return;

    let lastScroll = 0;

    window.addEventListener("scroll", () => {
        const currentScroll = window.pageYOffset;
        header.classList.toggle("scrolled", currentScroll > 10);

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

/** 현재 경로에 맞는 1차 메뉴 활성화 */
function initActiveMenu() {
    const currentPath = window.location.pathname;
    const currentSearch = window.location.search;
    const directLinks = document.querySelectorAll(".header-nav-link[href], .header-submenu a");

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
// 브라우저 뒤로가기로 이전 페이지가 표시되면 서버 상태 다시 확인
    window.addEventListener("pageshow", function(event) {
        const navigation = performance.getEntriesByType("navigation")[0];

        if (event.persisted || (navigation && navigation.type === "back_forward")) {
            window.location.reload();
        }
    });
