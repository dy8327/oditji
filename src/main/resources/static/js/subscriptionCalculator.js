(function () {
    "use strict";

    var section = document.getElementById("subCalcSection");

    if (!section) {
        return;
    }

    var searchUrl = section.dataset.searchUrl;
    var calculateUrl = section.dataset.calculateUrl;
    var imageBaseUrl = section.dataset.imageBaseUrl;

    var searchInput = document.getElementById("subCalcSearchInput");
    var searchResultsBox = document.getElementById("subCalcSearchResults");
    var wishlistEl = document.getElementById("subCalcWishlist");
    var wishlistEmptyEl = document.getElementById("subCalcWishlistEmpty");
    var wishlistCountEl = document.getElementById("subCalcWishlistCount");
    var calculateBtn = document.getElementById("subCalcCalculateBtn");
    var resultPanel = document.getElementById("subCalcResultPanel");

    var wishlist = [];
    var searchDebounceTimer = null;

    function wishlistKey(item) {
        return item.tmdbId + "_" + item.contentType;
    }

    function isAlreadyInWishlist(item) {
        var key = wishlistKey(item);
        return wishlist.some(function (existing) {
            return wishlistKey(existing) === key;
        });
    }

    function posterUrl(posterPath) {
        if (!posterPath) {
            return "";
        }
        return imageBaseUrl + posterPath;
    }

    function escapeHtml(value) {
        return String(value == null ? "" : value)
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;");
    }

    /* ---------- 검색 ---------- */

    searchInput.addEventListener("input", function () {
        var keyword = searchInput.value.trim();

        window.clearTimeout(searchDebounceTimer);

        if (keyword.length === 0) {
            hideSearchResults();
            return;
        }

        searchDebounceTimer = window.setTimeout(function () {
            runSearch(keyword);
        }, 250);
    });

    document.addEventListener("click", function (event) {
        if (!searchResultsBox.contains(event.target)
                && event.target !== searchInput) {
            hideSearchResults();
        }
    });

    function runSearch(keyword) {
        fetch(searchUrl + "?keyword=" + encodeURIComponent(keyword),
                { headers: { "Accept": "application/json" } })
            .then(function (res) {
                if (!res.ok) {
                    throw new Error("검색 요청 실패: " + res.status);
                }
                return res.json();
            })
            .then(function (results) {
                renderSearchResults(results || []);
            })
            .catch(function () {
                renderSearchResults([]);
            });
    }

    function renderSearchResults(results) {
        if (results.length === 0) {
            searchResultsBox.innerHTML =
                    '<p class="sub-calc-search-empty">검색 결과가 없어요.</p>';
            searchResultsBox.hidden = false;
            return;
        }

        var html = results.map(function (item, index) {
            var already = isAlreadyInWishlist(item);
            return '<button type="button" class="sub-calc-search-item"'
                    + ' data-index="' + index + '"'
                    + (already ? ' disabled' : '') + '>'
                    + '<img class="sub-calc-search-item__poster"'
                    + ' src="' + escapeHtml(posterUrl(item.posterPath)) + '"'
                    + ' alt="" loading="lazy">'
                    + '<span class="sub-calc-search-item__title">'
                    + escapeHtml(item.title) + '</span>'
                    + (already
                            ? '<span class="sub-calc-search-item__added">담음</span>'
                            : '<span class="sub-calc-search-item__add">+ 담기</span>')
                    + '</button>';
        }).join("");

        searchResultsBox.innerHTML = html;
        searchResultsBox.hidden = false;

        searchResultsBox.querySelectorAll(".sub-calc-search-item").forEach(
                function (btn) {
            btn.addEventListener("click", function () {
                var index = Number(btn.dataset.index);
                addToWishlist(results[index]);
                hideSearchResults();
                searchInput.value = "";
            });
        });
    }

    function hideSearchResults() {
        searchResultsBox.hidden = true;
        searchResultsBox.innerHTML = "";
    }

    /* ---------- 위시리스트 ---------- */

    function addToWishlist(item) {
        if (isAlreadyInWishlist(item)) {
            return;
        }
        wishlist.push(item);
        renderWishlist();
    }

    function removeFromWishlist(index) {
        wishlist.splice(index, 1);
        renderWishlist();
    }

    function renderWishlist() {
        wishlistCountEl.textContent = String(wishlist.length);
        calculateBtn.disabled = wishlist.length === 0;

        if (wishlist.length === 0) {
            wishlistEl.innerHTML = "";
            wishlistEl.appendChild(wishlistEmptyEl);
            return;
        }

        var html = wishlist.map(function (item, index) {
            return '<li class="sub-calc-wishlist__item">'
                    + '<img class="sub-calc-wishlist__poster"'
                    + ' src="' + escapeHtml(posterUrl(item.posterPath)) + '"'
                    + ' alt="" loading="lazy">'
                    + '<span class="sub-calc-wishlist__title">'
                    + escapeHtml(item.title) + '</span>'
                    + '<button type="button" class="sub-calc-wishlist__remove"'
                    + ' data-index="' + index + '" aria-label="위시리스트에서 제거">'
                    + '&times;</button>'
                    + '</li>';
        }).join("");

        wishlistEl.innerHTML = html;

        wishlistEl.querySelectorAll(".sub-calc-wishlist__remove").forEach(
                function (btn) {
            btn.addEventListener("click", function () {
                removeFromWishlist(Number(btn.dataset.index));
            });
        });
    }

    /* ---------- 계산 ---------- */

    calculateBtn.addEventListener("click", function () {
        if (wishlist.length === 0) {
            return;
        }

        resultPanel.innerHTML =
                '<p class="sub-calc-result-placeholder">계산 중입니다...</p>';

        fetch(calculateUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify(wishlist)
        })
            .then(function (res) {
                if (!res.ok) {
                    throw new Error("계산 요청 실패: " + res.status);
                }
                return res.json();
            })
            .then(renderResult)
            .catch(function () {
                resultPanel.innerHTML =
                        '<p class="sub-calc-result-placeholder">계산 중 문제가 발생했어요. 잠시 후 다시 시도해주세요.</p>';
            });
    });

    function renderResult(result) {
        var selected = result.selectedPlatformList || [];

        if (selected.length === 0) {
            resultPanel.innerHTML =
                    '<p class="sub-calc-result-placeholder">'
                    + '담은 작품의 가격 정보를 찾을 수 없어요.'
                    + '</p>';
            return;
        }

        var savings = (result.allPlatformMonthlyPrice || 0)
                - (result.totalMonthlyPrice || 0);

        var platformsHtml = selected.map(function (platform) {
            return '<li class="sub-calc-result-platform">'
                    + '<span class="sub-calc-result-platform__name">'
                    + escapeHtml(platform.platformName) + '</span>'
                    + '<span class="sub-calc-result-platform__price">'
                    + Number(platform.bestPrice).toLocaleString() + '원</span>'
                    + '</li>';
        }).join("");

        var unresolved = result.unresolvedItemList || [];
        var unresolvedHtml = "";

        if (unresolved.length > 0) {
            unresolvedHtml =
                    '<div class="sub-calc-result-unresolved">'
                    + '<p class="sub-calc-result-unresolved__title">가격 정보가 없어 제외된 작품</p>'
                    + '<ul>'
                    + unresolved.map(function (item) {
                        return '<li>' + escapeHtml(item.title) + '</li>';
                    }).join("")
                    + '</ul>'
                    + '</div>';
        }

        resultPanel.innerHTML =
                '<div class="sub-calc-result">'
                + '<p class="sub-calc-result__eyebrow">최저가 구독 조합</p>'
                + '<ul class="sub-calc-result-platform-list">'
                + platformsHtml
                + '</ul>'
                + '<div class="sub-calc-result-total">'
                + '<span>월 합계</span>'
                + '<strong>'
                + Number(result.totalMonthlyPrice).toLocaleString() + '원</strong>'
                + '</div>'
                + (savings > 0
                        ? '<p class="sub-calc-result-savings">'
                            + '필요한 플랫폼을 각각 구독할 때보다 매달 '
                            + savings.toLocaleString() + '원 절약돼요.'
                            + '</p>'
                        : '')
                + unresolvedHtml
                + '</div>';
    }
})();
