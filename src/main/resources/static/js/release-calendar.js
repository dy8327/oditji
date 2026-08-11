/**
 * 출시 알림 캘린더(release-calendar.jsp) 전용 스크립트.
 *
 * 1) 연·월 선택(select)을 바꾸면 별도 클릭 없이 바로 해당 달로 이동한다.
 *    (JS가 없는 환경에서는 "이동" 버튼으로 폼을 제출해 동일하게 동작한다.)
 * 2) 데스크톱에서는 "+N개 더보기" 버튼을 누르면 그 날짜의 전체 출시 목록을
 *    버튼 근처에 앵커된 팝오버로 띄운다.
 * 3) 모바일(폭 720px 이하)에서는 포스터가 너무 작아 탭하기 어려우므로
 *    포스터 미리보기 대신 날짜 셀 전체를 탭 대상으로 쓰고, 같은 패널을
 *    화면 중앙 모달(release-calendar.css)로 띄운다.
 */
(function () {

    "use strict";

    var MOBILE_BREAKPOINT = 720;

    function isMobile() {
        return window.matchMedia(
            "(max-width: " + MOBILE_BREAKPOINT + "px)"
        ).matches;
    }

    function initReleaseCalendarJumpForm() {

        var form = document.querySelector("[data-calendar-jump-form]");

        if (!form) {
            return;
        }

        var yearSelect = form.querySelector("#releaseCalendarYearSelect");
        var monthSelect = form.querySelector("#releaseCalendarMonthSelect");

        if (!yearSelect || !monthSelect) {
            return;
        }

        // JS가 정상 동작하면 select 변경만으로 바로 이동하므로
        // "이동" 버튼은 스크린리더/키보드 접근성을 위해 숨김 처리한다.
        form.classList.add("is-enhanced");

        function submitForm() {
            form.submit();
        }

        yearSelect.addEventListener("change", submitForm);
        monthSelect.addEventListener("change", submitForm);
    }

    function initReleaseCalendarPopovers() {

        var grid = document.querySelector(".release-calendar-grid");
        var backdrop = document.querySelector("[data-calendar-backdrop]");
        var popovers = document.querySelectorAll("[data-calendar-popover]");

        if (!grid || popovers.length === 0) {
            return;
        }

        var openPopover = null;
        var openTrigger = null;

        function lockBodyScroll(shouldLock) {
            document.body.classList.toggle(
                "release-calendar-no-scroll",
                shouldLock
            );
        }

        function closeOpenPopover() {

            if (!openPopover) {
                return;
            }

            openPopover.classList.remove("is-open");
            openPopover.style.top = "";
            openPopover.style.left = "";

            if (backdrop) {
                backdrop.classList.remove("is-open");
            }

            lockBodyScroll(false);

            if (openTrigger) {
                if (openTrigger.hasAttribute("aria-expanded")) {
                    openTrigger.setAttribute("aria-expanded", "false");
                }
                if (typeof openTrigger.focus === "function") {
                    openTrigger.focus();
                }
            }

            openPopover = null;
            openTrigger = null;
        }

        function positionPopoverNearTrigger(trigger, popover) {

            var triggerRect = trigger.getBoundingClientRect();

            // 먼저 화면에 그려 크기를 잰 뒤, 실제 위치를 계산한다.
            // (모바일에서는 CSS가 이 top/left를 무시하고 화면 중앙에 띄운다.)
            popover.style.top = "0px";
            popover.style.left = "0px";
            popover.classList.add("is-open");

            var popoverRect = popover.getBoundingClientRect();

            var viewportWidth = document.documentElement.clientWidth;
            var viewportHeight = document.documentElement.clientHeight;

            var left = triggerRect.left;
            var top = triggerRect.top;

            // 오른쪽으로 넘치면 트리거 오른쪽 끝에 맞춰 왼쪽으로 정렬한다.
            if (left + popoverRect.width > viewportWidth - 8) {
                left = triggerRect.right - popoverRect.width;
            }

            if (left < 8) {
                left = 8;
            }

            // 아래쪽으로 넘치면 트리거 위쪽에 띄운다.
            if (top + popoverRect.height > viewportHeight - 8) {
                top = triggerRect.top - popoverRect.height - 6;
            }

            if (top < 8) {
                top = 8;
            }

            popover.style.left = left + "px";
            popover.style.top = top + "px";
        }

        function openPopoverFromTrigger(trigger, popover) {

            var isCurrentlyOpen = popover === openPopover;

            closeOpenPopover();

            if (isCurrentlyOpen) {
                return;
            }

            positionPopoverNearTrigger(trigger, popover);

            if (trigger.hasAttribute("aria-expanded")) {
                trigger.setAttribute("aria-expanded", "true");
            }

            if (backdrop && isMobile()) {
                backdrop.classList.add("is-open");
                lockBodyScroll(true);
            }

            openPopover = popover;
            openTrigger = trigger;

            var closeButton = popover.querySelector(
                "[data-calendar-popover-close]"
            );

            if (closeButton) {
                closeButton.focus();
            }
        }

        popovers.forEach(function (popover) {

            // 팝오버 내부 클릭(리스트 스크롤 포함)이 바깥 클릭/스크롤
            // 리스너로 전파되어 바로 닫히는 것을 막는다.
            popover.addEventListener("click", function (event) {
                event.stopPropagation();
            });
        });

        // 데스크톱: "+N개 더보기" 버튼 → 버튼 근처에 앵커된 팝오버.
        document
            .querySelectorAll("[data-calendar-more-toggle]")
            .forEach(function (button) {

                var popoverId = button.getAttribute("aria-controls");
                var popover = popoverId
                    ? document.getElementById(popoverId)
                    : null;

                if (!popover) {
                    return;
                }

                button.addEventListener("click", function (event) {
                    event.stopPropagation();
                    openPopoverFromTrigger(button, popover);
                });
            });

        // 모바일: 출시작이 있는 날짜 셀 전체가 탭 대상이며, 같은 패널이
        // 화면 중앙 모달로 열린다. (has-content 클래스가 없는 빈 셀은 무시)
grid.addEventListener("click", function (event) {

    // 포스터/작품명을 직접 클릭한 경우에는
    // 기존처럼 작품 상세 페이지로 이동
    if (event.target.closest("a")) {
        return;
    }

    var cell = event.target.closest(
        ".release-calendar-cell--has-content"
    );

    if (!cell) {
        return;
    }

    var popoverId = cell.getAttribute("data-calendar-cell-popover");
    var popover = popoverId
        ? document.getElementById(popoverId)
        : null;

    if (!popover) {
        return;
    }

    event.stopPropagation();
    openPopoverFromTrigger(cell, popover);
});

        grid.addEventListener("keydown", function (event) {

            if (event.key !== "Enter" && event.key !== " ") {
                return;
            }

            if (!isMobile()) {
                return;
            }

            var cell = event.target.closest(
                ".release-calendar-cell--has-content"
            );

            if (!cell || event.target !== cell) {
                return;
            }

            var popoverId = cell.getAttribute("data-calendar-cell-popover");
            var popover = popoverId
                ? document.getElementById(popoverId)
                : null;

            if (!popover) {
                return;
            }

            event.preventDefault();
            openPopoverFromTrigger(cell, popover);
        });

        // 모바일에서만 출시작이 있는 날짜 셀이 키보드 포커스를 받도록 한다.
        // (데스크톱에서는 포스터 링크·"더보기" 버튼이 이미 탭 순서에 있으므로
        // 셀 자체를 추가 탭 정지점으로 만들지 않는다.)
        var focusableCells = document.querySelectorAll(
            ".release-calendar-cell--has-content"
        );

        function updateCellFocusability() {
            var mobile = isMobile();
            focusableCells.forEach(function (cell) {
                if (mobile) {
                    cell.setAttribute("tabindex", "0");
                    cell.setAttribute("role", "button");
                    cell.setAttribute("aria-haspopup", "dialog");
                } else {
                    cell.removeAttribute("tabindex");
                    cell.removeAttribute("role");
                    cell.removeAttribute("aria-haspopup");
                }
            });
        }

        updateCellFocusability();
        window.addEventListener("resize", updateCellFocusability);

        document
            .querySelectorAll("[data-calendar-popover-close]")
            .forEach(function (closeButton) {
                closeButton.addEventListener("click", function (event) {
                    event.stopPropagation();
                    closeOpenPopover();
                });
            });

        if (backdrop) {
            backdrop.addEventListener("click", closeOpenPopover);
        }

        document.addEventListener("click", closeOpenPopover);

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                closeOpenPopover();
            }
        });

        // 화면 크기가 바뀌면 좌표/레이아웃이 어긋날 수 있으므로 닫아서
        // 어색하게 붕 떠 있는 팝오버가 남지 않게 한다. 단, 팝오버 안쪽
        // 목록을 스크롤하는 것은 닫힘 대상에서 제외한다.
        window.addEventListener("resize", closeOpenPopover);

        window.addEventListener(
            "scroll",
            function (event) {
                if (openPopover && openPopover.contains(event.target)) {
                    return;
                }
                closeOpenPopover();
            },
            true
        );
    }

    function init() {
        initReleaseCalendarJumpForm();
        initReleaseCalendarPopovers();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

})();
