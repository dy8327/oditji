/* =========================================================
   ODITJI 이벤트 페이지 스크립트
   - D-day / 카운트다운 뱃지 계산
   - 진행률 바 (진행 중 / 예정 이벤트)
   - 스포트라이트 배너 자동 슬라이드 + 큰 카운트다운
   - 상세 페이지 인페이지 퀵내비 (부드러운 스크롤)
========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    initEventTimers();
    initEventSpotlightCarousel();
    initEventQuickNav();
    initEventDetailReveal();

});

function prefersReducedMotion() {
    return window.matchMedia
        && window.matchMedia("(prefers-reduced-motion: reduce)").matches;
}

/* 767px 이하에서는 스포트라이트가 opacity 크로스페이드 대신 CSS 가로
   스크롤(scroll-snap) 목록으로 바뀐다. 그 구간에서는 자동 넘김을
   scrollTo로 구현하고, 사용자가 직접 스와이프하면 그 위치에 맞춰
   인디케이터를 갱신한다. */
function isSpotlightMobileViewport() {
    return window.matchMedia
        && window.matchMedia("(max-width: 767px)").matches;
}

/* ---------------------------------------------------------
   스포트라이트 배너 자동 슬라이드
   - 메인화면(index.jsp) 히어로 캐러셀과 동일한 크로스페이드 방식
   - "진행 중" PICK 1개 + "예정" PICK 1개, 최대 2장만 존재한다.
     슬라이드가 1장 이하면(둘 중 하나만 있거나 스포트라이트 자체가
     없는 경우) 아무 것도 하지 않는다 - 화살표/점은 그 경우
     JSP에서부터 렌더링되지 않는다.
--------------------------------------------------------- */

function initEventSpotlightCarousel() {

    const root = document.getElementById("eventSpotlight");

    if (!root) {
        return;
    }

    const track = document.getElementById("eventSpotlightSlides");
    const slides = root.querySelectorAll(".event-spotlight-slide");

    if (slides.length <= 1) {
        return;
    }

    const dots = root.querySelectorAll(".event-spotlight-dot");

    let currentIndex = 0;
    let timerId = null;
    let isMobile = isSpotlightMobileViewport();

    /* 인디케이터/aria 상태만 맞추는 부분. 실제 화면 이동(크로스페이드 or
       가로 스크롤)은 show()에서 모드에 따라 별도로 처리한다. */
    function syncActiveIndex(index) {

        currentIndex = (index + slides.length) % slides.length;

        slides.forEach(function (slide, i) {

            const active = i === currentIndex;

            slide.classList.toggle("is-active", active);
            slide.setAttribute("aria-hidden", active ? "false" : "true");

            slide.querySelectorAll("a, button").forEach(function (focusable) {
                focusable.tabIndex = active ? 0 : -1;
            });

        });

        dots.forEach(function (dot, i) {

            const active = i === currentIndex;

            dot.classList.toggle("is-active", active);
            dot.setAttribute("aria-selected", active ? "true" : "false");

        });

    }

    function show(index, behavior) {

        syncActiveIndex(index);

        /* 모바일(가로 스크롤 스와이프 레이아웃)에서는 opacity 크로스페이드
           대신 해당 슬라이드 위치로 스크롤을 이동시켜 자동 넘김을 구현한다. */
        if (isMobile && track) {

            const targetSlide = slides[currentIndex];

            if (targetSlide) {
                track.scrollTo({
                    left: targetSlide.offsetLeft,
                    behavior: behavior || (prefersReducedMotion() ? "auto" : "smooth")
                });
            }

        }

    }

    function startTimer() {

        if (prefersReducedMotion()) {
            return;
        }

        stopTimer();

        timerId = window.setInterval(function () {
            show(currentIndex + 1);
        }, 3000);

    }

    function stopTimer() {

        if (timerId) {
            window.clearInterval(timerId);
            timerId = null;
        }

    }

    function restartTimer() {
        stopTimer();
        startTimer();
    }

    window.goEventSpotlight = function (index) {
        show(index);
        restartTimer();
    };

    window.moveEventSpotlight = function (direction) {
        show(currentIndex + direction);
        restartTimer();
    };

    root.addEventListener("mouseenter", stopTimer);
    root.addEventListener("mouseleave", startTimer);
    root.addEventListener("focusin", stopTimer);
    root.addEventListener("focusout", startTimer);

    root.addEventListener("keydown", function (event) {

        if (event.key === "ArrowLeft") {
            window.moveEventSpotlight(-1);
        } else if (event.key === "ArrowRight") {
            window.moveEventSpotlight(1);
        }

    });

    document.addEventListener("visibilitychange", function () {

        if (document.hidden) {
            stopTimer();
        } else {
            startTimer();
        }

    });

    /* 모바일: 손가락으로 스와이프하는 동안은 자동 넘김을 멈추고,
       스크롤이 멈춘 위치에 맞춰 인디케이터를 갱신한 뒤 타이머를
       다시 시작한다. */
    if (track) {

        track.addEventListener("touchstart", stopTimer, { passive: true });

        let scrollEndTimer = null;

        track.addEventListener("scroll", function () {

            if (!isMobile) {
                return;
            }

            window.clearTimeout(scrollEndTimer);

            scrollEndTimer = window.setTimeout(function () {

                let closestIndex = 0;
                let closestDistance = Infinity;

                slides.forEach(function (slide, i) {

                    const distance = Math.abs(slide.offsetLeft - track.scrollLeft);

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestIndex = i;
                    }

                });

                syncActiveIndex(closestIndex);
                restartTimer();

            }, 120);

        }, { passive: true });

    }

    /* 화면 폭이 데스크톱 ↔ 모바일 기준선을 넘나들 때(창 크기 조절,
       기기 회전) 넘김 방식을 다시 맞춘다. */
    window.addEventListener("resize", function () {

        const nowMobile = isSpotlightMobileViewport();

        if (nowMobile !== isMobile) {
            isMobile = nowMobile;
            show(currentIndex, "auto");
        }

    });

    show(0, "auto");
    startTimer();

}

/* ---------------------------------------------------------
   공통: 날짜 문자열 -> Date
   - "yyyy-MM-dd HH:mm:ss" 형태(시간 포함)는 그대로 파싱한다.
   - "yyyy-MM-dd" 형태(날짜만, LocalDate)는 시간 정보가 없으므로
     start는 그날 00:00:00, end는 그날 23:59:59.999(하루의 끝)로
     보정해서 파싱한다. DB의 START_DATE/END_DATE가 날짜 단위(TRUNC)
     비교 기준인 것과 동일한 기준을 맞추기 위함이다.
--------------------------------------------------------- */

function parseEventDate(value, isEnd) {

    if (!value) {
        return null;
    }

    const trimmed = value.trim();
    const dateOnly = /^\d{4}-\d{2}-\d{2}$/.test(trimmed);

    let isoLike;

    if (dateOnly) {
        isoLike = trimmed + (isEnd ? "T23:59:59.999" : "T00:00:00");
    } else {
        isoLike = trimmed.replace(" ", "T");
    }

    const parsed = new Date(isoLike);

    if (isNaN(parsed.getTime())) {
        return null;
    }

    return parsed;
}

function pad2(num) {
    return String(num).padStart(2, "0");
}

/* ---------------------------------------------------------
   D-day 뱃지 + 진행률 바
   대상: [data-event-timer] 요소
   속성: data-start, data-end, data-period(ongoing|upcoming|ended)
--------------------------------------------------------- */

function initEventTimers() {

    const timers =
        document.querySelectorAll("[data-event-timer]");

    if (!timers.length) {
        return;
    }

    function tick() {

        const now = new Date();

        timers.forEach(function (el) {
            updateEventTimer(el, now);
        });

    }

    tick();

    // 하루 단위 표시라 1분 간격이면 충분하지만, 스포트라이트의
    // 시:분:초 카운트다운을 위해 1초 간격으로 갱신한다.
    setInterval(tick, 1000);

}

function updateEventTimer(el, now) {

    const period = el.getAttribute("data-period");
    const start = parseEventDate(el.getAttribute("data-start"), false);
    const end = parseEventDate(el.getAttribute("data-end"), true);

    const badge = el.querySelector("[data-dday-text]");
    const bigTimer = el.querySelector("[data-big-countdown]");
    const progressBar = el.querySelector("[data-progress-bar]");

    let targetDate = period === "upcoming" ? start : end;

    if (!targetDate) {
        return;
    }

    const diffMs = targetDate.getTime() - now.getTime();

    /* --- 짧은 뱃지 텍스트 (D-3 / D-DAY / 종료) --- */

    if (badge) {

        if (period === "ended") {

            // 왼쪽 상단 뱃지(${periodBadge})가 이미 "종료"를 보여주므로
            // 우측 D-day 뱃지는 중복 표시하지 않고 비워서 숨긴다.
            badge.textContent = "";

        } else if (diffMs <= 0) {

            badge.textContent =
                period === "upcoming" ? "OPEN" : "종료 임박";

        } else {

            const daysLeft =
                Math.ceil(diffMs / (1000 * 60 * 60 * 24));

            const prefix = period === "upcoming" ? "시작 D-" : "D-";

            badge.textContent =
                daysLeft <= 1 ? "오늘마감".slice(0) : prefix + daysLeft;

            // 종료 하루 이내는 좀 더 직관적인 문구로 교체한다.
            if (period === "ongoing" && daysLeft <= 1) {
                badge.textContent = "마감임박";
            }

            if (period === "upcoming" && daysLeft <= 1) {
                badge.textContent = "곧 시작";
            }
        }

        badge.classList.toggle(
            "is-urgent",
            period === "ongoing" && diffMs > 0 && diffMs <= 1000 * 60 * 60 * 24
        );
    }

    /* --- 큰 시:분:초 카운트다운 (스포트라이트 배너) --- */

    if (bigTimer) {

        if (period === "ended" || diffMs <= 0) {

            bigTimer.innerHTML =
                '<span class="event-countdown-ended">' +
                (period === "upcoming" ? "오픈 준비 중" : "이벤트 종료") +
                "</span>";

        } else {

            const totalSeconds = Math.floor(diffMs / 1000);

            const days = Math.floor(totalSeconds / 86400);
            const hours = Math.floor((totalSeconds % 86400) / 3600);
            const minutes = Math.floor((totalSeconds % 3600) / 60);
            const seconds = totalSeconds % 60;

            bigTimer.innerHTML =
                buildCountdownUnit(days, "일") +
                buildCountdownUnit(pad2(hours), "시간") +
                buildCountdownUnit(pad2(minutes), "분") +
                buildCountdownUnit(pad2(seconds), "초");
        }
    }

    /* 진행률 바가 카운트다운 박스 줄(일/시간/분/초)과 같은 폭으로
       보이도록, 매 갱신마다 카운트다운의 실제 렌더 폭을 측정해
       그대로 맞춰준다. (반응형에서도 폭이 자동으로 따라간다) */
    if (bigTimer && progressBar) {
        progressBar.style.width = bigTimer.offsetWidth + "px";
    }

    /* --- 진행률 바 (진행 중 / 예정 이벤트) ---
       - 진행 중: 시작일~종료일 사이에서 현재 위치(경과율)를 그대로 표시.
       - 예정: 아직 시작 전이라 "경과율"이 없으므로, 시작일 기준
         과거 UPCOMING_GAUGE_WINDOW_DAYS일을 오픈 임박 구간으로 보고
         그 구간 안에서 게이지가 차오르게 한다. 시작일까지 이 기간보다
         많이 남았으면 0%(=아직 비어있음), 시작일이 가까워질수록
         100%에 가깝게 채워진다. */
    if (progressBar && start && end) {

        let percent = 0;

        if (period === "ongoing") {

            const total = end.getTime() - start.getTime();
            const elapsed = now.getTime() - start.getTime();

            percent = total > 0 ? (elapsed / total) * 100 : 100;

        } else if (period === "upcoming") {

            const UPCOMING_GAUGE_WINDOW_DAYS = 14;
            const windowMs = UPCOMING_GAUGE_WINDOW_DAYS * 24 * 60 * 60 * 1000;

            const windowStart = start.getTime() - windowMs;
            const elapsed = now.getTime() - windowStart;

            percent = (elapsed / windowMs) * 100;
        }

        percent = Math.min(100, Math.max(0, percent));

        progressBar.style.setProperty("--event-progress", percent + "%");
    }
}

function buildCountdownUnit(value, label) {

    return (
        '<span class="event-countdown-unit">' +
        '<strong>' + value + '</strong>' +
        '<em>' + label + '</em>' +
        "</span>"
    );
}

/* ---------------------------------------------------------
   히어로 배너 우측 하단 "이벤트 상품" 빠른이동 버튼
   - 클릭 시 헤더에 가리지 않도록 오프셋을 두고 부드럽게 스크롤
--------------------------------------------------------- */

function initEventQuickNav() {

    const link = document.querySelector("[data-event-quicknav]");

    if (!link) {
        return;
    }

    const id = link.getAttribute("href").slice(1);
    const target = document.getElementById(id);

    if (!target) {
        return;
    }

    link.addEventListener("click", function (event) {

        event.preventDefault();

        const headerHeight =
            parseFloat(getComputedStyle(document.documentElement)
                .getPropertyValue("--header-height")) || 80;

        const top =
            target.getBoundingClientRect().top +
            window.pageYOffset -
            (headerHeight + 20);

        window.scrollTo({ top: top, behavior: "smooth" });
    });

}

/* ---------------------------------------------------------
   상세 페이지 섹션 등장 효과
   - 메인화면(index.jsp)의 slider-section 등장 효과와 동일한 패턴.
   - "이벤트 안내" / "이벤트 상품" 섹션이 스크롤에 따라 살짝 아래에서
     떠오르듯 나타나 상세 페이지에 리듬감을 준다. JS가 없거나
     prefers-reduced-motion이면 처음부터 그냥 보이는 상태를 유지한다.
--------------------------------------------------------- */

function initEventDetailReveal() {

    const sections = document.querySelectorAll(
        ".event-description-section, .product-section"
    );

    if (!sections.length || prefersReducedMotion() || !("IntersectionObserver" in window)) {
        return;
    }

    const observer = new IntersectionObserver(
        function (entries) {

            entries.forEach(function (entry) {

                if (entry.isIntersecting) {
                    entry.target.classList.add("is-visible");
                    observer.unobserve(entry.target);
                }

            });

        },
        { threshold: 0.12 }
    );

    sections.forEach(function (section) {
        section.classList.add("will-reveal");
        observer.observe(section);
    });

}
