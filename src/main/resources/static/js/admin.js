const Admin = {
  contextPath: document.body.dataset.contextPath || "",

  /* =========================
       INIT
    ========================= */
  init() {
    this.sidebarActive();
    this.bindEvents();
  },

  bindEvents() {
    this.initSearch();
    this.initDelete();
    this.initAddButton();
    this.initTableUX();
  },

  /* =========================
       SIDEBAR ACTIVE
    ========================= */
  sidebarActive() {
    const path = location.pathname;

    document.querySelectorAll(".admin-menu a").forEach((a) => {
      const href = a.getAttribute("href");

      if (href && path.startsWith(href)) {
        a.classList.add("active");
      }
    });
  },

  /* =========================
       SEARCH (공통)
    ========================= */
  initSearch() {
    const searchBox = document.querySelector(".admin-search input");
    const searchBtn = document.querySelector(".admin-search button");

    if (!searchBox || !searchBtn) return;

    searchBtn.addEventListener("click", () => {
      const keyword = searchBox.value.trim();

      const url = new URL(location.href);

      if (keyword.length > 0) {
        url.searchParams.set("keyword", keyword);
      } else {
        url.searchParams.delete("keyword");
      }

      location.href = url.toString();
    });
  },

  /* =========================
       DELETE (공통)
    ========================= */
  initDelete() {
    document.addEventListener("click", (e) => {
      const btn = e.target.closest(".delete-btn");
      if (!btn) return;

      const ok = confirm("정말 삭제하시겠습니까?");
      if (!ok) return;

      const url = btn.dataset.url;

      if (!url) {
        console.warn("delete-btn에 data-url 없음");
        return;
      }

      location.href = this.contextPath + url;
    });
  },

  /* =========================
       ADD BUTTON (등록)
    ========================= */
  initAddButton() {
    document.addEventListener("click", (e) => {
      const btn = e.target.closest(".add-btn");
      if (!btn) return;

      const url = btn.dataset.url;

      if (!url) {
        console.warn("add-btn에 data-url 없음");
        return;
      }

      location.href = this.contextPath + url;
    });
  },

  /* =========================
       TABLE UX (hover + row click 확장 대비)
    ========================= */
  initTableUX() {
    const rows = document.querySelectorAll(".admin-table tbody tr");

    rows.forEach((row) => {
      row.addEventListener("mouseenter", () => {
        row.classList.add("hover");
      });

      row.addEventListener("mouseleave", () => {
        row.classList.remove("hover");
      });
    });
  },

  /* =========================
       UTILS
    ========================= */
  toast(message) {
    const el = document.createElement("div");
    el.className = "admin-toast";
    el.innerText = message;

    document.body.appendChild(el);

    setTimeout(() => el.classList.add("show"), 10);

    setTimeout(() => {
      el.remove();
    }, 2000);
  },
};

/* =========================
   INIT RUN
========================= */
document.addEventListener("DOMContentLoaded", () => {
  Admin.init();
});
