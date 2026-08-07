/* =========================================================
   ODITJI 메인 페이지 스크립트
   - 상단 배너(히어로) 자동 슬라이드
   - 콘텐츠 가로 슬라이더 스크롤 + 좌우 끝 상태 표시
   - 섹션 스크롤 등장 효과
========================================================= */

document.addEventListener('DOMContentLoaded', function () {
    initHeroCarousel();
    initSliderEdges();
    initRowReveal();
});

function prefersReducedMotion() {
    return window.matchMedia
        && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/* 767px 이하에서는 히어로가 opacity 크로스페이드 대신 CSS 가로 스크롤
   (scroll-snap) 목록으로 바뀐다. 그 구간에서는 자동 넘김을 scrollTo로
   구현하고, 사용자가 직접 스와이프하면 그 위치에 맞춰 인디케이터를
   갱신한다. */
function isHeroMobileViewport() {
    return window.matchMedia
        && window.matchMedia('(max-width: 767px)').matches;
}

/* ---------------------------------------------------------
   히어로 배너 (자동 재생 + 좌우 화살표 + 인디케이터)
--------------------------------------------------------- */

function initHeroCarousel() {

    const root = document.getElementById('mainHero');

    if (!root) {
        return;
    }

    const track = document.getElementById('heroSlides');
    const slides = root.querySelectorAll('.hero-slide');

    if (slides.length <= 1) {
        return;
    }

    const dots = root.querySelectorAll('.hero-dot');

    let currentIndex = 0;
    let timerId = null;
    let isMobile = isHeroMobileViewport();

    /* 인디케이터/aria 상태만 맞추는 부분. 실제 화면 이동(크로스페이드 or
       가로 스크롤)은 show()에서 모드에 따라 별도로 처리한다. */
    function syncActiveIndex(index) {

        currentIndex = (index + slides.length) % slides.length;

        slides.forEach(function (slide, i) {

            const active = i === currentIndex;

            slide.classList.toggle('is-active', active);
            slide.setAttribute('aria-hidden', active ? 'false' : 'true');

            slide.querySelectorAll('a, button').forEach(function (focusable) {
                focusable.tabIndex = active ? 0 : -1;
            });

        });

        dots.forEach(function (dot, i) {

            const active = i === currentIndex;

            dot.classList.toggle('is-active', active);
            dot.setAttribute('aria-selected', active ? 'true' : 'false');

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
                    behavior: behavior || (prefersReducedMotion() ? 'auto' : 'smooth')
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

    window.goHero = function (index) {
        show(index);
        restartTimer();
    };

    window.moveHero = function (direction) {
        show(currentIndex + direction);
        restartTimer();
    };

    root.addEventListener('mouseenter', stopTimer);
    root.addEventListener('mouseleave', startTimer);
    root.addEventListener('focusin', stopTimer);
    root.addEventListener('focusout', startTimer);

    root.addEventListener('keydown', function (event) {

        if (event.key === 'ArrowLeft') {
            window.moveHero(-1);
        } else if (event.key === 'ArrowRight') {
            window.moveHero(1);
        }

    });

    document.addEventListener('visibilitychange', function () {

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

        track.addEventListener('touchstart', stopTimer, { passive: true });

        let scrollEndTimer = null;

        track.addEventListener('scroll', function () {

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
    window.addEventListener('resize', function () {

        const nowMobile = isHeroMobileViewport();

        if (nowMobile !== isMobile) {
            isMobile = nowMobile;
            show(currentIndex, 'auto');
        }

    });

    show(0, 'auto');
    startTimer();

}

/* ---------------------------------------------------------
   콘텐츠 슬라이더 (오늘의 콘텐츠 / 추천 콘텐츠)
--------------------------------------------------------- */

function moveSlider(type, dir) {

    const idMap = {
        today: 'todaySlider',
        rec: 'recSlider',
        rank: 'rankSlider',
        popular: 'popularSlider',
        new: 'newSlider'
    };

    const targetId = idMap[type] || 'recSlider';
    const track = document.getElementById(targetId);

    scrollTrack(track, dir === 'left' ? -1 : 1);

}

function scrollTrack(track, direction) {

    if (!track) {
        return;
    }

    const firstCard = track.firstElementChild;
    const trackStyle = window.getComputedStyle(track);
    const gap = parseFloat(trackStyle.columnGap || trackStyle.gap || '16') || 16;

    const cardWidth = firstCard ? firstCard.getBoundingClientRect().width : 172;
    const visibleCount = Math.max(1, Math.floor(track.clientWidth / (cardWidth + gap)));
    const step = (cardWidth + gap) * visibleCount;

    /* 오른쪽 끝(또는 왼쪽 끝)에 이미 도달한 상태에서 같은 방향으로 한 번 더
       누르면, 더 이상 움직이지 못하고 멈춰 있는 대신 반대쪽 끝으로
       순환 이동한다. (오른쪽 끝 → 다음 → 처음으로, 왼쪽 끝 → 이전 → 마지막으로) */
    const maxScrollLeft = track.scrollWidth - track.clientWidth;
    const edgeTolerance = 1;

    const isAtEnd = maxScrollLeft <= edgeTolerance
        || track.scrollLeft >= maxScrollLeft - edgeTolerance;

    const isAtStart = track.scrollLeft <= edgeTolerance;

    const behavior = prefersReducedMotion() ? 'auto' : 'smooth';

    if (direction > 0 && isAtEnd) {
        track.scrollTo({ left: 0, behavior: behavior });
        return;
    }

    if (direction < 0 && isAtStart) {
        track.scrollTo({ left: maxScrollLeft, behavior: behavior });
        return;
    }

    track.scrollBy({
        left: direction * step,
        behavior: behavior
    });

}

function initSliderEdges() {

    document.querySelectorAll('.slider').forEach(function (slider) {

        const track = slider.querySelector('.track');

        if (!track) {
            return;
        }

        const updateEdgeState = function () {

            const maxScrollLeft = track.scrollWidth - track.clientWidth - 1;

            slider.classList.toggle('at-start', track.scrollLeft <= 0);
            slider.classList.toggle('at-end', maxScrollLeft <= 0 || track.scrollLeft >= maxScrollLeft);

        };

        track.addEventListener('scroll', updateEdgeState, { passive: true });
        window.addEventListener('resize', updateEdgeState);

        updateEdgeState();

    });

}

/* ---------------------------------------------------------
   섹션 스크롤 등장 효과 (JS가 없으면 항상 보이는 상태 유지)
--------------------------------------------------------- */

function initRowReveal() {

    const rows = document.querySelectorAll('.slider-section');

    if (!rows.length || prefersReducedMotion() || !('IntersectionObserver' in window)) {
        return;
    }

    const observer = new IntersectionObserver(function (entries) {

        entries.forEach(function (entry) {

            if (entry.isIntersecting) {
                entry.target.classList.add('is-visible');
                observer.unobserve(entry.target);
            }

        });

    }, { threshold: 0.15 });

    rows.forEach(function (row) {
        row.classList.add('will-reveal');
        observer.observe(row);
    });

}
