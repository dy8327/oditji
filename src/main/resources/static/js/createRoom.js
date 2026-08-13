/**
 * 채팅방 생성 화면의 방 유형별 입력 상태와 버튼 동작을 관리합니다.
 */
document.addEventListener("DOMContentLoaded", function() {

    const roomType = document.getElementById("roomType");
    const maxMemberGroup = document.getElementById("maxMemberGroup");
    const maxMember = document.getElementById("maxMember");
    const roomTypeHelp = document.getElementById("roomTypeHelp");
    const cancelBtn = document.getElementById("cancelBtn");
    const createRoomForm = document.getElementById("createRoomForm");
    const roomName = document.getElementById("roomName");

    /**
     * 공지방은 전체 사업자가 열람하므로 최대 인원 입력을 사용하지 않습니다.
     * 자유방으로 돌아오면 기본 최대 인원 값을 복원합니다.
     */
    function updateRoomTypeView() {

        if (!roomType || !maxMemberGroup || !maxMember || !roomTypeHelp) {
            return;
        }

        const noticeRoom = roomType.value === "NOTICE";

        maxMemberGroup.hidden = noticeRoom;
        maxMember.disabled = noticeRoom;

        if (!noticeRoom && !maxMember.value) {
            maxMember.value = "100";
        }

        roomTypeHelp.textContent = noticeRoom
            ? "공지방은 관리자만 메시지를 작성하고 모든 사업자가 열람합니다."
            : "자유방은 사업자들이 참가하여 자유롭게 대화하는 공간입니다.";
    }

    /**
     * 방 이름의 앞뒤 공백을 제거하고 빈 이름 제출을 차단합니다.
     */
    function validateCreateRoomForm(event) {

        if (!roomName) {
            return;
        }

        roomName.value = roomName.value.trim();

        if (!roomName.value) {
            event.preventDefault();
            showAlert("채팅방 이름을 입력해주세요.", "warning");
            roomName.focus();
        }
    }

    if (roomType) {
        roomType.addEventListener("change", updateRoomTypeView);
        updateRoomTypeView();
    }

    if (cancelBtn) {
        cancelBtn.addEventListener("click", function() {
            history.back();
        });
    }

    if (createRoomForm) {
        createRoomForm.addEventListener("submit", validateCreateRoomForm);
    }
});
