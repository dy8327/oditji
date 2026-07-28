document.addEventListener("DOMContentLoaded", function () {
  const reviewAlertMessage = document.getElementById("reviewAlertMessage");

  // [추가] 서버에서 전달한 중복 리뷰 안내 메시지를 알림으로 표시한다.
  if (reviewAlertMessage && reviewAlertMessage.dataset.message) {
    alert(reviewAlertMessage.dataset.message);
  }

  const reviewWriteBox = document.getElementById("reviewWriteBox");
  const reviewWriteForm = document.getElementById("contentReviewWriteForm");

  if (reviewWriteForm) {
    reviewWriteForm.addEventListener("submit", function (event) {
      // [추가] 본인 리뷰가 이미 있는 경우 서버 요청 전에 중복 작성을 차단한다.
      if (reviewWriteBox && reviewWriteBox.dataset.hasMyReview === "true") {
        event.preventDefault();
        alert("계정 하나당 리뷰 1개만 작성이 가능합니다");
        return;
      }

      const spoilerCheckbox = reviewWriteForm.querySelector('input[name="spoilerYn"]');

      // [추가] 스포일러 체크가 없는 경우 등록 의사를 한 번 더 확인한다.
      if (spoilerCheckbox && !spoilerCheckbox.checked) {
        const confirmed = confirm("스포일러 포함 체크를 안하셨습니다. 그대로 등록하시겠습니까?");

        if (!confirmed) {
          event.preventDefault();
        }
      }
    });
  }

  document.querySelectorAll("[data-review-item]").forEach(function (reviewItem) {
    const openButton = reviewItem.querySelector("[data-review-edit-open]");

    const cancelButton = reviewItem.querySelector("[data-review-edit-cancel]");

    const displayArea = reviewItem.querySelector("[data-review-display]");

    const editForm = reviewItem.querySelector("[data-review-edit-form]");

    const deleteForm = reviewItem.querySelector(".review-delete-form");

    // [추가] 수정 버튼을 누르면 같은 리뷰 카드 안에서 수정 폼을 연다.
    if (openButton && displayArea && editForm) {
      openButton.addEventListener("click", function (event) {
        // [추가] 수정 버튼 클릭 시 다른 버튼 이벤트가 함께 실행되지 않도록 한다.
        event.preventDefault();
        event.stopPropagation();

        displayArea.hidden = true;
        editForm.hidden = false;
        openButton.hidden = true;
      });
    }

    // [추가] 취소 버튼을 누르면 수정 폼을 닫고 기존 리뷰 내용을 다시 표시한다.
    if (cancelButton && displayArea && editForm && openButton) {
      cancelButton.addEventListener("click", function (event) {
        // [추가] 취소 버튼의 기본 동작을 막는다.
        event.preventDefault();

        editForm.hidden = true;
        displayArea.hidden = false;
        openButton.hidden = false;
      });
    }

    if (editForm) {
      editForm.addEventListener("submit", function (event) {
        const spoilerCheckbox = editForm.querySelector('input[name="spoilerYn"]');

        // [추가] 수정 시에도 스포일러 체크가 없으면 수정 의사를 확인한다.
        if (spoilerCheckbox && !spoilerCheckbox.checked) {
          const confirmed = confirm("스포일러 포함 체크를 안하셨습니다. 그대로 등록하시겠습니까?");

          if (!confirmed) {
            event.preventDefault();
          }
        }
      });
    }

    if (deleteForm) {
      deleteForm.addEventListener("submit", function (event) {
        // [추가] 리뷰가 즉시 삭제되는 것을 방지하기 위해 삭제 여부를 확인한다.
        const confirmed = confirm("작성한 리뷰를 삭제하시겠습니까?");

        if (!confirmed) {
          event.preventDefault();
        }
      });
    }
  });

  /*
   * [추가]
   * 다른 사용자가 작성한 스포일러 리뷰를 클릭했을 때
   * 해당 리뷰의 블라인드를 해제한다.
   *
   * 이벤트 위임 방식으로 처리하여
   * 리뷰 영역이 수정된 이후에도 정상적으로 동작한다.
   */
  document.addEventListener("click", function (event) {
    const spoilerOpenButton = event.target.closest("[data-spoiler-open]");

    if (!spoilerOpenButton) {
      return;
    }

    // [추가] 버튼 기본 동작과 다른 클릭 이벤트가 함께 실행되지 않도록 한다.
    event.preventDefault();
    event.stopPropagation();

    const spoilerReview = spoilerOpenButton.closest("[data-spoiler-review]");

    // [추가] 스포일러 리뷰 영역을 찾지 못하면 처리를 중단한다.
    if (!spoilerReview) {
      return;
    }

    const confirmed = confirm("스포일러가 포함되어 있습니다. 내용을 확인하시겠습니까?");

    if (!confirmed) {
      return;
    }

    /*
     * [추가]
     * 확인을 누르면 현재 클릭한 리뷰에만 revealed 클래스를 추가하여
     * 실제 리뷰 내용이 보이도록 한다.
     */
    spoilerReview.classList.add("revealed");
  });
});
