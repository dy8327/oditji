/* =========================================================
   ODITJI OTT 할인 혜택 페이지 스크립트
   - 플랫폼 탭 / 카테고리 칩 AJAX 필터링
   - 카드 그리드 동적 렌더링
   - 실시간 할인 계산기 모달
========================================================= */

document.addEventListener("DOMContentLoaded", function () {
    var section = document.getElementById("ottDiscountSection");
    if (!section) {
        return;
    }

    var apiUrl = section.dataset.apiUrl;
    var pageUrl = section.dataset.pageUrl;
    var grid = document.getElementById("discountCardGrid");
    var platformGroup = document.getElementById("platformFilterGroup");
    var categoryGroup = document.getElementById("categoryFilterGroup");
    /* [수정] 히어로 배너 실시간 갱신: 필터를 바꿀 때마다 이 영역만 다시 그린다. */
    var heroBody = document.getElementById("ottDiscountHeroBody");
    /* [수정] 8개 단위 페이징 내비게이션. pagination.js의 renderPaginationNav()를
       재사용해 AJAX로 목록이 바뀔 때마다 같은 위젯을 다시 그린다. */
    var paginationNav = document.getElementById("ottDiscountPagination");

    /* [수정] AJAX로 다시 그리는 카드도 서버 렌더링과 동일하게 OTT_PLATFORM에 저장된
       실제 로고 이미지를 쓰도록, ottDiscount.jsp가 내려준 JSON을 파싱해 둔다.
       (필터를 바꿔도 플랫폼 목록 자체는 바뀌지 않으므로 최초 1회만 읽으면 된다) */
    var ottLogoMap = parseLogoMap();

    function parseLogoMap() {
        var script = document.getElementById("ottPlatformLogoData");
        if (!script) {
            return {};
        }
        try {
            return JSON.parse(script.textContent) || {};
        } catch (e) {
            return {};
        }
    }

    var state = {
        platform: section.dataset.selectedPlatform || "ALL",
        category: section.dataset.selectedCategory || "ALL",
        page: 1
    };

    initFilterGroup(platformGroup, "platform");
    initFilterGroup(categoryGroup, "category");
    initCalculatorModal();
    bindCardGridDelegate();

    function initFilterGroup(group, filterKey) {
        if (!group) {
            return;
        }
        group.addEventListener("click", function (event) {
            var button = event.target.closest(".ott-filter-btn");
            if (!button || !group.contains(button)) {
                return;
            }

            var value = button.dataset.value;
            if (state[filterKey] === value) {
                return;
            }

            state[filterKey] = value;
            /* [수정] 필터가 바뀌면 이전 필터 기준 페이지 번호는 의미가 없으므로 1페이지로 되돌린다. */
            state.page = 1;
            setActiveButton(group, button);
            reloadList();
        });
    }

    function setActiveButton(group, activeButton) {
        var buttons = group.querySelectorAll(".ott-filter-btn");
        buttons.forEach(function (btn) {
            btn.classList.toggle("active", btn === activeButton);
        });
    }

    function reloadList() {
        grid.innerHTML = '<p class="ott-discount-loading">할인 혜택을 불러오는 중입니다...</p>';

        var query = "?platform=" + encodeURIComponent(state.platform) +
                "&category=" + encodeURIComponent(state.category) +
                "&page=" + encodeURIComponent(state.page);

        fetch(apiUrl + query, { headers: { "Accept": "application/json" } })
            .then(function (res) {
                if (!res.ok) {
                    throw new Error("요청 실패: " + res.status);
                }
                return res.json();
            })
            .then(function (body) {
                renderGrid(body.data || []);
                renderHero(body.heroItem);
                /* [수정 - 버그 픽스] renderPaginationNav()는 pagination.js에서
                   window.location.href를 기준으로 각 페이지 링크의 href를 만든다.
                   그런데 이 URL은 updateHistory()가 history.replaceState로 갱신해주는데,
                   기존 코드는 updatePaginationNav()를 먼저 호출하고 updateHistory()를
                   나중에 호출했다. 그 결과 페이지네이션 링크는 "직전" 필터 상태의 URL을
                   기준으로 만들어졌고, 필터를 바꾼 직후에는 항상 한 박자 뒤처진(=이전) 값을
                   가리켰다. 예) 전체 탭에서 카드사 탭으로 바꾸면 화면엔 카드사 목록이 보이지만
                   페이지 링크는 여전히 platform/category가 바뀌기 전 URL을 base로 만들어져,
                   그 링크를 눌러 다음 페이지로 이동하면 엉뚱한(이전) 탭으로 돌아가 버렸다.
                   (필터를 한 번도 안 바꾼 상태로 페이지만 넘길 때는 URL이 이미 최신이라
                   문제가 드러나지 않았다 — "전체 탭에서만 페이징이 되는 것처럼" 보인 이유)
                   순서를 뒤집어 URL을 먼저 최신 상태로 만든 뒤 페이지네이션을 그린다. */
                state.page = body.currentPage || 1;
                updateHistory();
                updatePaginationNav(body.currentPage, body.totalPage);
            })
            .catch(function () {
                grid.innerHTML = '<p class="ott-discount-error">할인 혜택을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.</p>';
            });
    }

    function updatePaginationNav(currentPage, totalPage) {
        if (!paginationNav) {
            return;
        }
        paginationNav.dataset.currentPage = currentPage || 1;
        paginationNav.dataset.totalPage = totalPage || 1;
        /* [수정] pagination.js가 window.location.href만 보고 링크를 만들면 history
           갱신 타이밍에 따라 이전 필터 값을 가리킬 수 있다(과거 버그의 원인). 현재 JS
           상태(state.platform/state.category)를 extra-params로 직접 못박아 두면
           URL 갱신 시점과 무관하게 항상 정확한 필터로 페이지 링크가 만들어진다. */
        paginationNav.dataset.extraParams = JSON.stringify({
            platform: state.platform,
            category: state.category
        });
        if (typeof renderPaginationNav === "function") {
            renderPaginationNav(paginationNav);
        }
    }

    function updateHistory() {
        if (!window.history || !window.history.replaceState) {
            return;
        }
        var url = pageUrl + "?platform=" + encodeURIComponent(state.platform) +
                "&category=" + encodeURIComponent(state.category) +
                "&page=" + encodeURIComponent(state.page);
        window.history.replaceState(null, "", url);
    }

    function renderGrid(items) {
        if (!items || items.length === 0) {
            grid.innerHTML = '<p class="ott-discount-empty">조건에 맞는 할인 혜택이 없습니다.</p>';
            return;
        }

        var html = items.map(renderCard).join("");
        grid.innerHTML = html;
    }

    /* [수정] 히어로 배너 실시간 갱신
       - JSP의 최초 서버 렌더링(heroItem)과 동일한 마크업을 그대로 JS로 재현한다.
       - 탭을 누를 때마다 배너가 뚝 끊겨 바뀌면 어색하므로, 잠깐 페이드아웃한 뒤
         내용을 교체하고 다시 페이드인하는 크로스페이드 전환을 준다
         (실제 타이밍은 ott-discount.css의 .ott-discount-hero__body.is-fading 참고). */
    var HERO_FADE_MS = 160;

    function renderHero(item) {
        if (!heroBody) {
            return;
        }
        heroBody.classList.add("is-fading");
        window.setTimeout(function () {
            heroBody.innerHTML = item ? renderHeroCard(item) : renderHeroEmpty();
            heroBody.classList.remove("is-fading");
        }, HERO_FADE_MS);
    }

    function renderHeroCard(item) {
        var badgeHtml = item.badgeText
                ? '<span class="ott-badge ott-badge--hero">' + escapeHtml(item.badgeText) + '</span>'
                : '';

        var priceHtml = '';
        if (item.regularPrice !== null && item.regularPrice !== undefined &&
                item.discountPrice !== null && item.discountPrice !== undefined) {

            var rateHtml = '';
            if (item.discountRate !== null && item.discountRate !== undefined) {
                rateHtml = '<span class="ott-discount-rate">' + item.discountRate + '% 할인</span>';
            }

            priceHtml =
                '<div class="ott-price-row ott-discount-hero__price-row">' +
                    '<span class="ott-price-regular">' + formatNumber(item.regularPrice) + '원</span>' +
                    '<span class="ott-price-arrow">→</span>' +
                    '<span class="ott-price-discount">' + formatNumber(item.discountPrice) + '원</span>' +
                    rateHtml +
                '</div>';
        }

        var ctaHtml = item.targetUrl
                ? '<a href="' + escapeAttr(item.targetUrl) + '" target="_blank" rel="noopener" class="ott-discount-hero__cta">혜택 보러가기</a>'
                : '';

        var platformKey = item.platformCode ? String(item.platformCode).toLowerCase() : '';
        var logoUrl = ottLogoMap[platformKey];
        var platformIconHtml = logoUrl
                ? '<img class="ott-platform-tag__logo" src="' + escapeAttr(logoUrl) + '" alt="" aria-hidden="true">'
                : '<span class="ott-platform-tag__icon" aria-hidden="true">' +
                    (item.platformName ? escapeHtml(String(item.platformName).charAt(0)) : '') + '</span>';

        return (
            '<div class="ott-discount-hero__card">' +
                '<div class="ott-discount-hero__card-top">' +
                    '<span class="ott-platform-tag ott-platform-tag--lg ott-platform-tag--' + escapeAttr(item.platformCode) + '">' +
                        platformIconHtml +
                        escapeHtml(item.platformName) + '</span>' +
                    badgeHtml +
                '</div>' +
                '<p class="ott-discount-hero__card-title">' + escapeHtml(item.title) + '</p>' +
                '<p class="ott-discount-hero__card-summary">' + escapeHtml(item.discountSummary) + '</p>' +
                priceHtml +
                ctaHtml +
            '</div>'
        );
    }

    function renderHeroEmpty() {
        return '<p class="ott-discount-hero__desc">카드사·통신사·멤버십 혜택을 한 곳에서 비교해 보세요.</p>';
    }

    function renderCard(item) {
        var badgeHtml = item.badgeText
                ? '<span class="ott-badge">' + escapeHtml(item.badgeText) + '</span>'
                : '';

        var priceHtml = '';
        if (item.regularPrice !== null && item.regularPrice !== undefined &&
                item.discountPrice !== null && item.discountPrice !== undefined) {

            var rateHtml = '';
            if (item.discountRate !== null && item.discountRate !== undefined) {
                rateHtml = '<span class="ott-discount-rate">' + item.discountRate + '% 할인</span>';
            }

            priceHtml =
                '<div class="ott-price-row">' +
                    '<span class="ott-price-regular">' + formatNumber(item.regularPrice) + '원</span>' +
                    '<span class="ott-price-arrow">→</span>' +
                    '<span class="ott-price-discount">' + formatNumber(item.discountPrice) + '원</span>' +
                    rateHtml +
                '</div>';
        }

        var descHtml = item.description
                ? '<p class="ott-discount-card__desc">' + escapeHtml(item.description) + '</p>'
                : '';

        var linkHtml = item.targetUrl
                ? '<a href="' + escapeAttr(item.targetUrl) + '" target="_blank" rel="noopener" class="ott-discount-card__link">혜택 자세히 보기</a>'
                : '';

        var platformKey = item.platformCode ? String(item.platformCode).toLowerCase() : '';
        var logoUrl = ottLogoMap[platformKey];
        var platformIconHtml = logoUrl
                ? '<img class="ott-platform-tag__logo" src="' + escapeAttr(logoUrl) + '" alt="" aria-hidden="true">'
                : '<span class="ott-platform-tag__icon" aria-hidden="true">' +
                    (item.platformName ? escapeHtml(String(item.platformName).charAt(0)) : '') + '</span>';

        return (
            '<div class="ott-discount-card" data-id="' + item.discountId + '">' +
                '<div class="ott-discount-card__top">' +
                    '<span class="ott-platform-tag ott-platform-tag--' + escapeAttr(item.platformCode) + '">' +
                        platformIconHtml +
                        escapeHtml(item.platformName) + '</span>' +
                    badgeHtml +
                '</div>' +
                '<h3 class="ott-discount-card__title">' + escapeHtml(item.title) + '</h3>' +
                '<p class="ott-discount-card__summary">' + escapeHtml(item.discountSummary) + '</p>' +
                priceHtml +
                descHtml +
                '<div class="ott-discount-card__bottom">' +
                    '<span class="ott-discount-card__company">' + escapeHtml(item.cardOrCompany || '') + '</span>' +
                    '<span class="ott-discount-card__date">' + formatDateOnly(item.startDate) + ' ~ ' + formatDateOnly(item.endDate) + '</span>' +
                '</div>' +
                '<div class="ott-discount-card__actions">' +
                    '<button type="button" class="ott-discount-card__calc-btn" ' +
                        'data-discount-id="' + item.discountId + '" ' +
                        'data-title="' + escapeAttr(item.title) + '" ' +
                        'data-platform-name="' + escapeAttr(item.platformName) + '" ' +
                        'data-regular-price="' + (item.regularPrice != null ? item.regularPrice : '') + '" ' +
                        'data-discount-rate="' + (item.discountRate != null ? item.discountRate : '') + '">계산하기</button>' +
                    linkHtml +
                '</div>' +
            '</div>'
        );
    }

    /* 최초 서버 렌더링된 카드 + AJAX로 새로 그려진 카드 모두에서
       "할인 계산기" 버튼 클릭을 하나의 리스너로 처리(이벤트 위임) */
    function bindCardGridDelegate() {
        grid.addEventListener("click", function (event) {
            var calcBtn = event.target.closest(".ott-discount-card__calc-btn");
            if (calcBtn) {
                openCalculatorModal({
                    title: calcBtn.dataset.title,
                    platformName: calcBtn.dataset.platformName,
                    regularPrice: calcBtn.dataset.regularPrice,
                    discountRate: calcBtn.dataset.discountRate
                });
            }
        });
    }

    function formatNumber(num) {
        if (num === null || num === undefined) {
            return "";
        }
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    function escapeHtml(value) {
        if (value === null || value === undefined) {
            return "";
        }
        var div = document.createElement("div");
        div.textContent = value;
        return div.innerHTML;
    }

    function escapeAttr(value) {
        return escapeHtml(value).replace(/"/g, "&quot;");
    }

    /* 서버에서 내려오는 startDate/endDate에 "00:00:00" 같은 시간이 붙어 있어
       날짜(yyyy-MM-dd)만 잘라서 보여준다. */
    function formatDateOnly(value) {
        if (!value) {
            return "";
        }
        return String(value).slice(0, 10);
    }
});

/* ---------------------------------------------------------
   OTT 할인 계산기 모달 (서버 요청 없이 전부 JavaScript에서 계산)

   계산 로직:
     할인 금액(월) = 원래 가격 × 할인율 ÷ 100
     최종 가격(월) = 원래 가격 - 할인 금액(월)
     절약 금액(월) = 할인 금액(월)
     N개월 결제 총액 = 최종 가격(월) × 개월 수
     N개월 총 절감액 = 할인 금액(월) × 개월 수

   이용 개월 수를 1개월로 두면 단순 계산기(할인 금액/최종 가격/절약 금액)와
   동일한 결과를 보여주고, 3/6/12개월을 선택하면 총 결제액/총 절감액까지 이어서 보여준다.
--------------------------------------------------------- */
function initCalculatorModal() {
    var modal = document.getElementById("ottCalcModal");
    if (!modal) {
        return;
    }

    var regularPriceInput = document.getElementById("ottCalcRegularPrice");
    var rateInput = document.getElementById("ottCalcRate");
    var monthsSelect = document.getElementById("ottCalcMonths");

    modal.addEventListener("click", function (event) {
        if (event.target.closest("[data-close-modal]")) {
            closeCalculatorModal();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeCalculatorModal();
        }
    });

    /* 버튼 없이 입력값이 바뀌는 즉시 계산 결과를 갱신한다. */
    if (monthsSelect) {
        monthsSelect.addEventListener("change", runCalculation);
    }
    [regularPriceInput, rateInput].forEach(function (input) {
        if (input) {
            input.addEventListener("input", runCalculation);
        }
    });
}

/**
 * 계산기 모달을 연다.
 * @param {{title:string, platformName:string, regularPrice:string, discountRate:string}} context
 *   카드의 [계산하기] 버튼에서 넘어온 값. regularPrice/discountRate가 없는 카드는
 *   입력창을 비워두고 사용자가 직접 입력하도록 한다.
 */
function openCalculatorModal(context) {
    var modal = document.getElementById("ottCalcModal");
    var subtitle = document.getElementById("ottCalcModalSubtitle");
    var regularPriceInput = document.getElementById("ottCalcRegularPrice");
    var rateInput = document.getElementById("ottCalcRate");
    var monthsSelect = document.getElementById("ottCalcMonths");
    if (!modal) {
        return;
    }

    context = context || {};
    subtitle.textContent = (context.platformName ? context.platformName + " · " : "") + (context.title || "");

    regularPriceInput.value = isFiniteNumber(context.regularPrice) ? context.regularPrice : "";
    rateInput.value = isFiniteNumber(context.discountRate) ? context.discountRate : "";
    if (monthsSelect) {
        monthsSelect.value = "1";
    }

    var panel = modal.querySelector(".ott-calc-modal__panel");
    if (panel && !panel.open) {
        panel.show();
    }

    modal.classList.add("is-open");
    modal.setAttribute("aria-hidden", "false");

    runCalculation();
}

function closeCalculatorModal() {
    var modal = document.getElementById("ottCalcModal");
    if (!modal) {
        return;
    }
    var panel = modal.querySelector(".ott-calc-modal__panel");
    if (panel && panel.open) {
        panel.close();
    }

    modal.classList.remove("is-open");
    modal.setAttribute("aria-hidden", "true");
}

function isFiniteNumber(value) {
    return value !== null && value !== undefined && value !== "" && isFinite(Number(value));
}

function runCalculation() {
    var resultBox = document.getElementById("ottCalcResult");
    var regularPriceInput = document.getElementById("ottCalcRegularPrice");
    var rateInput = document.getElementById("ottCalcRate");
    var monthsSelect = document.getElementById("ottCalcMonths");
    if (!resultBox) {
        return;
    }

    var regularPrice = Number(regularPriceInput.value);
    var rate = Number(rateInput.value);
    var months = monthsSelect ? Number(monthsSelect.value) : 1;

    if (!regularPriceInput.value || !rateInput.value ||
            !isFinite(regularPrice) || !isFinite(rate) ||
            regularPrice < 0 || rate < 0 || rate > 100) {
        resultBox.innerHTML = '<p class="ott-calc-modal__placeholder">원래 가격과 할인율을 입력하면 바로 계산해 드려요.</p>';
        return;
    }

    var monthlyDiscount = Math.round(regularPrice * rate / 100);
    var monthlyFinalPrice = regularPrice - monthlyDiscount;
    var monthlySavings = monthlyDiscount;
    var totalPayment = monthlyFinalPrice * months;
    var totalSavings = monthlyDiscount * months;

    renderCalculationResult({
        monthlyDiscount: monthlyDiscount,
        monthlyFinalPrice: monthlyFinalPrice,
        monthlySavings: monthlySavings,
        months: months,
        totalPayment: totalPayment,
        totalSavings: totalSavings
    });
}

function renderCalculationResult(data) {
    var resultBox = document.getElementById("ottCalcResult");

    var html =
        '<div class="ott-calc-result__row">' +
            '<span class="ott-calc-result__label">할인 금액(월)</span>' +
            '<span class="ott-calc-result__value">' + formatWon(data.monthlyDiscount) + '</span>' +
        '</div>' +
        '<div class="ott-calc-result__row">' +
            '<span class="ott-calc-result__label">최종 가격(월)</span>' +
            '<span class="ott-calc-result__value">' + formatWon(data.monthlyFinalPrice) + '</span>' +
        '</div>' +
        '<div class="ott-calc-result__row">' +
            '<span class="ott-calc-result__label">절약 금액(월)</span>' +
            '<span class="ott-calc-result__value">' + formatWon(data.monthlySavings) + '</span>' +
        '</div>';

    if (data.months > 1) {
        html +=
            '<div class="ott-calc-result__divider"></div>' +
            '<div class="ott-calc-result__row">' +
                '<span class="ott-calc-result__label">' + data.months + '개월 결제 총액</span>' +
                '<span class="ott-calc-result__value">' + formatWon(data.totalPayment) + '</span>' +
            '</div>' +
            '<div class="ott-calc-result__row">' +
                '<span class="ott-calc-result__label">' + data.months + '개월 총 절감액</span>' +
                '<span class="ott-calc-result__value ott-calc-result__value--highlight">' + formatWon(data.totalSavings) + '</span>' +
            '</div>';
    }

    resultBox.innerHTML = html;
}

function formatWon(value) {
    if (value === null || value === undefined || !isFinite(value)) {
        return "-";
    }
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",") + "원";
}
