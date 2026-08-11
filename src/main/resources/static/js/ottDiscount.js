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
    /* [수정] 9개 단위 페이징 내비게이션. pagination.js의 renderPaginationNav()를
       재사용해 AJAX로 목록이 바뀔 때마다 같은 위젯을 다시 그린다. */
    var paginationNav = document.getElementById("ottDiscountPagination");

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
                /* [수정] 서버가 계산한 현재 페이지/총 페이지 기준으로 페이지네이션을 다시 그린다.
                   (요청한 page가 총 페이지 수를 넘으면 서버가 마지막 페이지로 보정해 내려준다) */
                state.page = body.currentPage || 1;
                updatePaginationNav(body.currentPage, body.totalPage);
                updateHistory();
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

        return (
            '<div class="ott-discount-card" data-id="' + item.discountId + '">' +
                '<div class="ott-discount-card__top">' +
                    '<span class="ott-platform-tag ott-platform-tag--' + escapeAttr(item.platformCode) + '">' +
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
