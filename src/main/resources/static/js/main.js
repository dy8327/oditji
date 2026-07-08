// 슬라이더 이동 (기초 구조)
function moveSlider(type, dir) {
  const el = document.getElementById(type === "today" ? "todaySlider" : "recSlider");

  const scrollAmount = 180;

  if (dir === "left") {
    el.scrollBy({ left: -scrollAmount, behavior: "smooth" });
  } else {
    el.scrollBy({ left: scrollAmount, behavior: "smooth" });
  }
}

// 랭킹 AJAX (구조만 완성)
function loadRanking(type) {
  console.log("ranking load:", type);
}
