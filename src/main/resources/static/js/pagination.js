/* =========================================================
   ODITJI 공통 페이지네이션 위젯

   기존에는 DOMContentLoaded 시점에 [data-pagination] 요소를 한 번만 그렸으나,
   OTT 할인 정보 페이지(ottDiscount.jsp)처럼 필터를 AJAX로 바꿔 목록을 다시 받아오는
   화면에서는 페이지가 바뀔 때마다(전체 건수/총 페이지가 달라질 때마다) 같은 로직으로
   내비게이션을 다시 그려야 한다. renderPaginationNav()를 전역으로 노출해 재사용하고,
   기존 DOMContentLoaded 자동 렌더링은 그대로 유지한다.
========================================================= */
function renderPaginationNav(nav) {
  if (!nav) {
    return;
  }

  const currentPage = Math.max(Number(nav.dataset.currentPage || 1), 1);
  const totalPage = Math.max(Number(nav.dataset.totalPage || 1), 1);
  const pageParam = nav.dataset.pageParam || "page";

  if (totalPage <= 1) {
    nav.hidden = true;
    nav.replaceChildren();
    return;
  }
  nav.hidden = false;

  const blockSize = 5;
  const startPage = Math.floor((currentPage - 1) / blockSize) * blockSize + 1;
  const endPage = Math.min(startPage + blockSize - 1, totalPage);
  const fragment = document.createDocumentFragment();

  const createUrl = (page) => {
    const url = new URL(window.location.href);
    url.searchParams.set(pageParam, String(page));

    if (nav.dataset.fixedParamName && nav.dataset.fixedParamValue) {
      url.searchParams.set(nav.dataset.fixedParamName, nav.dataset.fixedParamValue);
    }

    return url.pathname + url.search + url.hash;
  };

  const appendLink = (label, targetPage, extraClass, ariaLabel) => {
    const link = document.createElement("a");
    link.className = `oditji-page-link${extraClass ? ` ${extraClass}` : ""}`;
    link.href = createUrl(targetPage);
    link.textContent = label;
    if (ariaLabel) link.setAttribute("aria-label", ariaLabel);
    fragment.appendChild(link);
  };

  if (currentPage > 1) {
    appendLink("«", 1, "oditji-page-edge", "첫 페이지");
    appendLink("‹", currentPage - 1, "", "이전 페이지");
  }

  for (let page = startPage; page <= endPage; page += 1) {
    if (page === currentPage) {
      const current = document.createElement("span");
      current.className = "oditji-page-current";
      current.textContent = String(page);
      current.setAttribute("aria-current", "page");
      fragment.appendChild(current);
    } else {
      appendLink(String(page), page, "", `${page}페이지`);
    }
  }

  if (currentPage < totalPage) {
    appendLink("›", currentPage + 1, "", "다음 페이지");
    appendLink("»", totalPage, "oditji-page-edge", "마지막 페이지");
  }

  nav.replaceChildren(fragment);
}

document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("[data-pagination]").forEach(renderPaginationNav);
});
