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

/* ---------------------------------------------------------
   히어로 배너 (자동 재생 + 좌우 화살표 + 인디케이터)
--------------------------------------------------------- */

function initHeroCarousel() {

    const root = document.getElementById('mainHero');

    if (!root) {
        return;
    }

    const slides = root.querySelectorAll('.hero-slide');

    if (slides.length <= 1) {
        return;
    }

    const dots = root.querySelectorAll('.hero-dot');

    let currentIndex = 0;
    let timerId = null;

    function show(index) {

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

    function startTimer() {

        if (prefersReducedMotion()) {
            return;
        }

        stopTimer();

        timerId = window.setInterval(function () {
            show(currentIndex + 1);
        }, 6500);

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

    show(0);
    startTimer();

}

/* ---------------------------------------------------------
   콘텐츠 슬라이더 (오늘의 콘텐츠 / 추천 콘텐츠)
--------------------------------------------------------- */

function moveSlider(type, dir) {

    const idMap = {
        today: 'todaySlider',
        rec: 'recSlider',
        rank: 'rankSlider'
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

    track.scrollBy({
        left: direction * step,
        behavior: prefersReducedMotion() ? 'auto' : 'smooth'
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
