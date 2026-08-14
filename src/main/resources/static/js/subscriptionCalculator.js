(function () {
  "use strict";

  var section = document.getElementById("subCalcSection");

  if (!section) {
    return;
  }

  var searchUrl = section.dataset.searchUrl;
  var calculateUrl = section.dataset.calculateUrl;
  var saveUrl = section.dataset.saveUrl;
  var resultBaseUrl = section.dataset.resultBaseUrl;
  var contentDetailUrl = section.dataset.contentDetailUrl;
  var imageBaseUrl = section.dataset.imageBaseUrl;

  var searchInput = document.getElementById("subCalcSearchInput");
  var searchResultsBox = document.getElementById("subCalcSearchResults");
  var wishlistEl = document.getElementById("subCalcWishlist");
  var wishlistEmptyEl = document.getElementById("subCalcWishlistEmpty");
  var wishlistCountEl = document.getElementById("subCalcWishlistCount");
  var calculateBtn = document.getElementById("subCalcCalculateBtn");
  var resultPanel = document.getElementById("subCalcResultPanel");
  var cardSelect = document.getElementById("subCalcCardSelect");
  var membershipSelect = document.getElementById("subCalcMembershipSelect");

  var wishlist = [];
  var searchDebounceTimer = null;
  var hasCalculatedOnce = false;
  var lastCalculationResult = null;

  /*
   * [콘텐츠 상세 -> 구독 조합 계산기 자동 담기 추가]
   * 상세페이지 CTA에서 전달된 작품이 있으면 기존 검색/담기 로직을 그대로 재사용해
   * wishlist의 첫 항목으로 자동 등록합니다.
   *
   * 계산은 자동 실행하지 않습니다.
   * 사용자가 할인 조건을 확인한 뒤 기존의 "최저가 조합 계산하기" 버튼을 눌러야
   * 오른쪽 결과 영역이 출력되는 기존 흐름을 그대로 유지합니다.
   */
  function addInitialContentFromDetail() {
    var initialItemEl = document.getElementById("subCalcInitialItem");

    if (!initialItemEl) {
      return;
    }

    var tmdbId = Number(initialItemEl.dataset.tmdbId);
    var contentType = (initialItemEl.dataset.contentType || "").trim();
    var title = (initialItemEl.dataset.title || "").trim();
    var posterPath = initialItemEl.dataset.posterPath || "";
    var platformEls = initialItemEl.querySelectorAll("[data-platform-name]");
    var platformNameList = Array.prototype.map
      .call(platformEls, function (platformEl) {
        return platformEl.dataset.platformName;
      })
      .filter(function (platformName) {
        return Boolean(platformName);
      });

    /* [OTT 구독 조합 계산기 - 담은 작품 OTT 로고 표시 추가]
       platformNameList와 인덱스를 맞춰 로고 URL 목록도 함께 읽어온다. */
    var platformLogoList = Array.prototype.map.call(platformEls, function (platformEl) {
      return platformEl.dataset.platformLogo || "";
    });

    if (!Number.isFinite(tmdbId) || tmdbId <= 0 || !contentType || !title) {
      return;
    }

    addToWishlist({
      tmdbId: tmdbId,
      contentType: contentType,
      title: title,
      posterPath: posterPath,
      platformNameList: platformNameList,
      platformLogoList: platformLogoList,
    });
  }

  function selectedTelecomCode() {
    var checked = document.querySelector('input[name="subCalcTelecom"]:checked');
    return checked ? checked.value : "";
  }

  /* 필터를 바꾸면(이미 한 번 계산한 상태라면) 바뀐 조건으로 바로 다시 계산해서
       "조건 선택 -> 즉시 반영" 흐름을 만든다. */
  function onFilterChanged() {
    if (hasCalculatedOnce && wishlist.length > 0) {
      runCalculate();
    }
  }

  document.querySelectorAll('input[name="subCalcTelecom"]').forEach(function (radio) {
    radio.addEventListener("change", onFilterChanged);
  });
  cardSelect.addEventListener("change", onFilterChanged);
  membershipSelect.addEventListener("change", onFilterChanged);

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

  /*
   * [OTT 구독 조합 계산기 - 담은 작품 OTT 로고 표시 추가]
   * 담은 작품을 볼 수 있는 OTT 로고들을 작은 아이콘으로 나열한다.
   * platformLogoList가 platformNameList와 인덱스가 맞는 경우에만 아이콘을 쓰고,
   * 로고 URL이 없는 항목은 건너뛴다(레이아웃이 깨지지 않도록 빈 아이콘을 만들지 않음).
   */
  function platformLogosHtml(item) {
    var nameList = item.platformNameList || [];
    var logoList = item.platformLogoList || [];

    if (nameList.length === 0) {
      return "";
    }

    var iconsHtml = nameList
      .map(function (platformName, index) {
        var logoUrl = logoList[index];

        if (!logoUrl) {
          return "";
        }

        return (
          '<img class="sub-calc-wishlist__platform-logo"' +
          ' src="' +
          escapeHtml(logoUrl) +
          '"' +
          ' alt="' +
          escapeHtml(platformName) +
          '"' +
          ' title="' +
          escapeHtml(platformName) +
          '"' +
          ' loading="lazy">'
        );
      })
      .join("");

    if (!iconsHtml) {
      return "";
    }

    return '<span class="sub-calc-wishlist__platform-logos">' + iconsHtml + "</span>";
  }

  /* 담은 작품 뱃지를 눌렀을 때 이동할 상세페이지 링크를 만든다.
     tmdbId/contentType이 없으면(방어적으로) 링크를 만들지 않는다. */
  function contentDetailHref(item) {
    if (!contentDetailUrl || !item.tmdbId || !item.contentType) {
      return "";
    }
    return contentDetailUrl + "?tmdbId=" + encodeURIComponent(item.tmdbId) + "&contentType=" + encodeURIComponent(item.contentType);
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
    if (!searchResultsBox.contains(event.target) && event.target !== searchInput) {
      hideSearchResults();
    }
  });

  function runSearch(keyword) {
    fetch(searchUrl + "?keyword=" + encodeURIComponent(keyword), { headers: { Accept: "application/json" } })
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
      searchResultsBox.innerHTML = '<p class="sub-calc-search-empty">검색 결과가 없어요.</p>';
      searchResultsBox.hidden = false;
      return;
    }

    var html = results
      .map(function (item, index) {
        var already = isAlreadyInWishlist(item);
        return (
          '<button type="button" class="sub-calc-search-item"' +
          ' data-index="' +
          index +
          '"' +
          (already ? " disabled" : "") +
          ">" +
          '<img class="sub-calc-search-item__poster"' +
          ' src="' +
          escapeHtml(posterUrl(item.posterPath)) +
          '"' +
          ' alt="" loading="lazy">' +
          '<span class="sub-calc-search-item__title">' +
          escapeHtml(item.title) +
          "</span>" +
          (already ? '<span class="sub-calc-search-item__added">담음</span>' : '<span class="sub-calc-search-item__add">+ 담기</span>') +
          "</button>"
        );
      })
      .join("");

    searchResultsBox.innerHTML = html;
    searchResultsBox.hidden = false;

    searchResultsBox.querySelectorAll(".sub-calc-search-item").forEach(function (btn) {
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

    /*
     * [OTT 구독 조합 계산기 - 담은 작품 전체 삭제 시 결과 초기화]
     *
     * 계산 완료 후 담은 작품의 X 버튼을 눌러
     * 마지막 작품까지 모두 삭제한 경우,
     * 이전 계산 결과가 오른쪽 영역에 남아 있지 않도록
     * 계산 상태와 저장 대상 결과를 초기화합니다.
     *
     * 오른쪽 결과 영역은 계산 전 최초 화면과 동일하게
     * 안내 문구만 표시하도록 되돌립니다.
     */
    if (wishlist.length === 0) {
      resetCalculationResult();
    }
  }

  /*
   * [OTT 구독 조합 계산기 - 계산 결과 초기화]
   *
   * 담은 작품이 모두 삭제되었을 때
   * 이전 최저가 계산 결과와 공유용 저장 데이터를 제거하고,
   * 오른쪽 결과 영역을 최초 안내 상태로 되돌립니다.
   */
  function resetCalculationResult() {
    hasCalculatedOnce = false;
    lastCalculationResult = null;

    resultPanel.innerHTML = '<p class="sub-calc-result-placeholder">' + "작품을 담고 계산하기를 누르면 결과가 여기에 표시돼요." + "</p>";
  }

  function renderWishlist() {
    wishlistCountEl.textContent = String(wishlist.length);
    calculateBtn.disabled = wishlist.length === 0;

    if (wishlist.length === 0) {
      wishlistEl.innerHTML = "";
      wishlistEl.appendChild(wishlistEmptyEl);
      return;
    }

    var html = wishlist
      .map(function (item, index) {
        return (
          '<li class="sub-calc-wishlist__item">' +
          '<img class="sub-calc-wishlist__poster"' +
          ' src="' +
          escapeHtml(posterUrl(item.posterPath)) +
          '"' +
          ' alt="" loading="lazy">' +
          '<span class="sub-calc-wishlist__title">' +
          escapeHtml(item.title) +
          "</span>" +
          platformLogosHtml(item) +
          '<button type="button" class="sub-calc-wishlist__remove"' +
          ' data-index="' +
          index +
          '" aria-label="위시리스트에서 제거">' +
          "&times;</button>" +
          "</li>"
        );
      })
      .join("");

    wishlistEl.innerHTML = html;

    wishlistEl.querySelectorAll(".sub-calc-wishlist__remove").forEach(function (btn) {
      btn.addEventListener("click", function () {
        removeFromWishlist(Number(btn.dataset.index));
      });
    });
  }

  /* 상세페이지에서 넘어온 작품이 있으면 최초 화면 구성 시 자동으로 담습니다. */
  addInitialContentFromDetail();

  /* ---------- 계산 ---------- */

  calculateBtn.addEventListener("click", runCalculate);

  function runCalculate() {
    if (wishlist.length === 0) {
      return;
    }

    resultPanel.innerHTML = '<p class="sub-calc-result-placeholder">계산 중입니다...</p>';

    var requestBody = {
      wishItemList: wishlist,
      telecomCode: selectedTelecomCode(),
      cardCompany: cardSelect.value,
      membershipName: membershipSelect.value,
    };

    fetch(calculateUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: JSON.stringify(requestBody),
    })
      .then(function (res) {
        if (!res.ok) {
          throw new Error("계산 요청 실패: " + res.status);
        }
        return res.json();
      })
      .then(function (result) {
        hasCalculatedOnce = true;
        lastCalculationResult = result;
        renderResult(result);
      })
      .catch(function () {
        resultPanel.innerHTML = '<p class="sub-calc-result-placeholder">계산 중 문제가 발생했어요. 잠시 후 다시 시도해주세요.</p>';
      });
  }

  function renderResult(result) {
    var selected = result.selectedPlatformList || [];

    if (selected.length === 0) {
      resultPanel.innerHTML = '<p class="sub-calc-result-placeholder">' + "담은 작품의 가격 정보를 찾을 수 없어요." + "</p>";
      return;
    }

    var totalRegular = result.totalRegularMonthlyPrice || 0;
    var totalDiscounted = result.totalMonthlyPrice || 0;
    var savings = totalRegular - totalDiscounted;

    var platformsHtml = selected
      .map(function (platform) {
        var hasDiscount = platform.discountSource && platform.bestPrice < platform.regularPrice;

        var priceLine =
          (hasDiscount ? '<span class="sub-calc-result-platform__regular">' + Number(platform.regularPrice).toLocaleString() + "원</span>" : "") +
          '<span class="sub-calc-result-platform__price">' +
          Number(platform.bestPrice).toLocaleString() +
          "원</span>" +
          (hasDiscount && platform.discountRate != null ? ' <span class="sub-calc-result-platform__rate">(' + platform.discountRate + "% 할인)</span>" : "");

        var sourceLine = hasDiscount ? escapeHtml(platform.discountSource) + " 적용 시" : "(기본 정가 적용)";

        var platformContentList = platform.contentList || [];
        var platformContentHtml = "";

        if (platformContentList.length > 0) {
          platformContentHtml =
            '<ul class="sub-calc-result-platform__content-list">' +
            platformContentList
              .map(function (item) {
                var posterHtml = item.posterPath
                  ? '<img class="sub-calc-result-platform__content-poster" src="' + escapeHtml(posterUrl(item.posterPath)) + '" alt="" loading="lazy">'
                  : "";
                var href = contentDetailHref(item);
                var tag = href ? "a" : "span";
                var hrefAttr = href ? ' href="' + escapeHtml(href) + '"' : "";
                return (
                  '<li class="sub-calc-result-platform__content-item">' +
                  "<" +
                  tag +
                  ' class="sub-calc-result-platform__content-link"' +
                  hrefAttr +
                  ">" +
                  posterHtml +
                  '<span class="sub-calc-result-platform__content-name">' +
                  escapeHtml(item.title) +
                  "</span></" +
                  tag +
                  "></li>"
                );
              })
              .join("") +
            "</ul>";
        }

        return (
          '<li class="sub-calc-result-platform">' +
          '<div class="sub-calc-result-platform__head">' +
          '<span class="sub-calc-result-platform__name">' +
          escapeHtml(platform.platformName) +
          "</span>" +
          "<span>" +
          priceLine +
          "</span>" +
          "</div>" +
          '<span class="sub-calc-result-platform__source">' +
          sourceLine +
          "</span>" +
          platformContentHtml +
          "</li>"
        );
      })
      .join("");

    var unresolved = result.unresolvedItemList || [];
    var unresolvedHtml = "";

    if (unresolved.length > 0) {
      unresolvedHtml =
        '<div class="sub-calc-result-unresolved">' +
        '<p class="sub-calc-result-unresolved__title">가격 정보가 없어 제외된 작품</p>' +
        "<ul>" +
        unresolved
          .map(function (item) {
            return "<li>" + escapeHtml(item.title) + "</li>";
          })
          .join("") +
        "</ul>" +
        "</div>";
    }

    resultPanel.innerHTML =
      '<div class="sub-calc-result">' +
      '<p class="sub-calc-result__eyebrow">최저가 구독 조합 결과</p>' +
      '<ul class="sub-calc-result-platform-list">' +
      platformsHtml +
      "</ul>" +
      '<div class="sub-calc-result-total">' +
      "<span>월 정가 합계</span>" +
      "<span>" +
      totalRegular.toLocaleString() +
      "원</span>" +
      "</div>" +
      '<div class="sub-calc-result-total sub-calc-result-total--main">' +
      "<span>할인 적용 총 예상 금액</span>" +
      "<strong>" +
      totalDiscounted.toLocaleString() +
      "원</strong>" +
      "</div>" +
      (savings > 0 ? '<p class="sub-calc-result-savings">' + "월 " + savings.toLocaleString() + "원 절감 효과!" + "</p>" : "") +
      unresolvedHtml +
      "</div>" +
      '<div class="sub-calc-result-actions">' +
      '<button type="button" class="sub-calc-save-btn" id="subCalcSaveBtn">' +
      "결과 저장 / 공유 링크 만들기" +
      "</button>" +
      "</div>" +
      '<div class="sub-calc-share-box" id="subCalcShareBox" hidden>' +
      '<input type="text" class="sub-calc-share-input" id="subCalcShareInput" readonly>' +
      '<button type="button" class="sub-calc-copy-btn" id="subCalcCopyBtn">복사</button>' +
      "</div>";

    bindResultActions();
  }

  /* ---------- 결과 저장 / 공유 ---------- */

  function bindResultActions() {
    var saveBtn = document.getElementById("subCalcSaveBtn");
    var shareBox = document.getElementById("subCalcShareBox");
    var shareInput = document.getElementById("subCalcShareInput");
    var copyBtn = document.getElementById("subCalcCopyBtn");

    if (!saveBtn) {
      return;
    }

    saveBtn.addEventListener("click", function () {
      if (!lastCalculationResult) {
        return;
      }

      saveBtn.disabled = true;
      saveBtn.textContent = "저장 중...";

      fetch(saveUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(lastCalculationResult),
      })
        .then(function (res) {
          if (!res.ok) {
            throw new Error("저장 요청 실패: " + res.status);
          }
          return res.json();
        })
        .then(function (data) {
          var shareUrl = window.location.origin + resultBaseUrl + data.resultId;

          shareInput.value = shareUrl;
          shareBox.hidden = false;

          saveBtn.disabled = false;
          saveBtn.textContent = "결과 저장 / 공유 링크 만들기";

          if (typeof showAlert === "function") {
            showAlert("결과가 저장됐어요. 링크를 복사해서 공유해보세요.", "success");
          }
        })
        .catch(function () {
          saveBtn.disabled = false;
          saveBtn.textContent = "결과 저장 / 공유 링크 만들기";

          if (typeof showAlert === "function") {
            showAlert("저장 중 문제가 발생했어요. 잠시 후 다시 시도해주세요.", "error");
          }
        });
    });

    copyBtn.addEventListener("click", function () {
      if (!shareInput.value) {
        return;
      }

      navigator.clipboard
        .writeText(shareInput.value)
        .then(function () {
          if (typeof showAlert === "function") {
            showAlert("공유 링크가 복사됐어요.", "success");
          }
        })
        .catch(function () {
          if (typeof showAlert === "function") {
            showAlert("링크 복사에 실패했어요.", "error");
          }
        });
    });
  }
})();
