document.addEventListener("DOMContentLoaded", function () {
  const modal = document.getElementById("spoilerConfirmModal");
  const yesButton = document.getElementById("spoilerConfirmYes");
  const noButton = document.getElementById("spoilerConfirmNo");
  let selectedReview = null;
  if (!modal || !yesButton || !noButton) return;

  document.querySelectorAll("[data-spoiler-open]").forEach(function (button) {
    button.addEventListener("click", function () {
      selectedReview = button.closest("[data-spoiler-review]");
      modal.hidden = false;
      document.body.classList.add("spoiler-modal-open");
    });
  });

  yesButton.addEventListener("click", function () {
    if (selectedReview) selectedReview.classList.add("revealed");
    closeModal();
  });
  noButton.addEventListener("click", closeModal);
  modal.addEventListener("click", function (event) {
    if (event.target === modal) closeModal();
  });
  document.addEventListener("keydown", function (event) {
    if (event.key === "Escape" && !modal.hidden) closeModal();
  });

  function closeModal() {
    modal.hidden = true;
    document.body.classList.remove("spoiler-modal-open");
    selectedReview = null;
  }
});
