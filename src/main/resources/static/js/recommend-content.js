document.addEventListener("DOMContentLoaded", function () {

    initializeRecommendSliders();

});

function initializeRecommendSliders() {

    const buttons =
        document.querySelectorAll(
            "[data-slider-target][data-slider-direction]"
        );

    buttons.forEach(function (button) {

        button.addEventListener("click", function () {

            const targetId =
                button.dataset.sliderTarget;

            const direction =
                button.dataset.sliderDirection;

            const track =
                document.getElementById(targetId);

            if (!track) {
                return;
            }

            const firstCard =
                track.querySelector(".recommend-card");

            const cardWidth =
                firstCard
                    ? firstCard.getBoundingClientRect().width
                    : 180;

            const computedStyle =
                window.getComputedStyle(track);

            const gap =
                parseFloat(computedStyle.columnGap)
                || parseFloat(computedStyle.gap)
                || 16;

            const scrollAmount =
                (cardWidth + gap) * 3;

            track.scrollBy({
                left: direction === "left"
                        ? -scrollAmount
                        : scrollAmount,
                behavior: "smooth"
            });
        });
    });
}
