function moveSlider(type, dir) {

    const targetId = type === 'today' ? 'todaySlider' : 'recSlider';
    const el = document.getElementById(targetId);

    if (!el) return;

    const scrollAmount = 180;

    if (dir === 'left') {
        el.scrollBy({ left: -scrollAmount, behavior: 'smooth' });
    } else {
        el.scrollBy({ left: scrollAmount, behavior: 'smooth' });
    }
}


function loadRanking(type) {

    console.log("ranking load:", type);

}