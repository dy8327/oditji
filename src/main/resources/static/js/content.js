document.addEventListener("DOMContentLoaded", () => {

    /*
     * JSP에서 전달한 context path를 읽습니다.
     *
     * 프로젝트가 /oditji 같은 context path로 실행되는 경우에도
     * 찜 요청과 로그인 페이지 주소가 정상적으로 생성되도록 합니다.
     */
    const contextPathElement =
        document.getElementById("contentPageData");

    const pageContextPath =
        contextPathElement
            ? contextPathElement.dataset.contextPath || ""
            : "";

    /*
     * 상세페이지 상단의 찜 버튼입니다.
     */
    const favoriteButton =
        document.getElementById("detailFavoriteBtn");

    /*
     * 상세페이지 상단의 리뷰 작성 버튼입니다.
     *
     * 기존에는 별도 리뷰 작성 주소로 이동했지만,
     * 수정 후에는 현재 페이지 하단의 리뷰 작성 폼으로 이동합니다.
     */
    const scrollReviewWriteButton =
        document.getElementById("scrollReviewWriteBtn");

    /*
     * 실제 리뷰 등록 또는 수정 폼이 들어 있는 영역입니다.
     *
     * contentDetail.jsp의 id="reviewWriteBox"와 연결됩니다.
     */
    const reviewWriteBox =
        document.getElementById("reviewWriteBox");

    /*
     * 상세페이지 상단의 뒤로가기 버튼입니다.
     *
     * 기존의 고정 콘텐츠 목록 주소가 아니라,
     * 브라우저의 실제 이전 방문 기록으로 이동합니다.
     */
    const detailBackButton =
        document.getElementById("detailBackButton");

    /*
     * 찜 요청이 처리 중인지 나타냅니다.
     *
     * 사용자가 버튼을 빠르게 여러 번 눌러
     * 중복 요청이 전송되는 것을 방지합니다.
     */
    let favoriteProcessing = false;

    // ======================
    // 이전 화면으로 이동
    // ======================

    if (detailBackButton) {

        detailBackButton.addEventListener(
            "click",
            () => {

                /*
                 * 브라우저 방문 기록이 있으면
                 * 사용자가 상세페이지에 들어오기 직전에 보던 화면으로 이동합니다.
                 *
                 * 검색 결과, 콘텐츠 목록, 메인 화면,
                 * 추천 콘텐츠 화면 등의 상태를 그대로 복원할 수 있습니다.
                 */
                if (window.history.length > 1) {

                    window.history.back();
                    return;
                }

                /*
                 * 주소창에 상세 URL을 직접 입력했거나,
                 * 새 탭으로 열어 이전 방문 기록이 없는 경우에는
                 * JSP의 data-fallback-url에 지정한 콘텐츠 목록으로 이동합니다.
                 */
                const fallbackUrl =
                    detailBackButton.dataset.fallbackUrl || "";

                if (fallbackUrl) {
                    window.location.href = fallbackUrl;
                }
            }
        );
    }

    // ======================
    // 리뷰 작성 영역 이동
    // ======================

    /*
     * 상단 리뷰 작성 버튼과 하단 작성 영역이 모두 존재할 때만
     * 스크롤 이벤트를 등록합니다.
     *
     * 화면 구조가 변경되거나 특정 요소가 없는 경우에도
     * JavaScript 오류가 발생하지 않도록 방어적으로 처리합니다.
     */
    if (scrollReviewWriteButton && reviewWriteBox) {

        scrollReviewWriteButton.addEventListener(
            "click",
            () => {

                /*
                 * 하단 리뷰 작성 폼까지 부드럽게 이동합니다.
                 *
                 * block: "start"는 리뷰 작성 영역의 시작 지점을
                 * 브라우저 화면 위쪽에 맞춥니다.
                 */
                reviewWriteBox.scrollIntoView({
                    behavior: "smooth",
                    block: "start"
                });

                /*
                 * 부드러운 스크롤이 어느 정도 완료된 후
                 * 평점 입력란이나 리뷰 작성란에 포커스를 줍니다.
                 *
                 * 수정 리뷰와 신규 리뷰는 폼 구조가 조금 다를 수 있으므로
                 * 평점 input을 먼저 찾고, 없으면 textarea를 찾습니다.
                 */
                window.setTimeout(
                    () => {

                        const firstReviewInput =
                            reviewWriteBox.querySelector(
                                "input[name='rating'], "
                                + "textarea[name='reviewText']"
                            );

                        if (firstReviewInput) {

                            /*
                             * 포커스로 인해 브라우저가 다시 스크롤하는 것을
                             * 막기 위해 preventScroll을 사용합니다.
                             */
                            firstReviewInput.focus({
                                preventScroll: true
                            });
                        }
                    },
                    500
                );
            }
        );
    }

    // ======================
    // 콘텐츠 찜
    // ======================

    /*
     * 리뷰 신고 모달 로직은 상품 상세페이지와 공용으로 사용하기 위해
     * report.js로 분리되어 있습니다.
     *
     * 이 파일에서는 기존 찜 처리 기능과
     * 리뷰 작성 스크롤, 이전 화면 이동 기능을 담당합니다.
     */
    if (favoriteButton) {

        favoriteButton.addEventListener(
            "click",
            async () => {

                /*
                 * 이미 찜 요청이 진행 중이면 추가 요청을 막습니다.
                 */
                if (favoriteProcessing) {
                    return;
                }

                const loginRequired =
                    favoriteButton.dataset.loginRequired === "true";

                /*
                 * 비로그인 사용자는 서버에 찜 요청을 보내지 않고
                 * 로그인 페이지 이동 여부를 확인합니다.
                 */
                if (loginRequired) {

                    const moveLogin =
                        await showConfirm(
                            "찜 기능은 로그인 후 이용할 수 있습니다.\n"
                            + "로그인 페이지로 이동하시겠습니까?",
                            "info"
                        );

                    if (moveLogin) {

                        /*
                         * 로그인 완료 후 현재 상세페이지로 돌아올 수 있도록
                         * 현재 경로와 쿼리스트링을 returnUrl에 담습니다.
                         */
                        const currentUrl =
                            window.location.pathname
                            + window.location.search;

                        window.location.href =
                            `${pageContextPath}/member/login`
                            + `?returnUrl=${encodeURIComponent(currentUrl)}`;
                    }

                    return;
                }

                /*
                 * 버튼의 data-content-no 속성에서 콘텐츠 번호를 읽습니다.
                 */
                const contentNo =
                    Number(favoriteButton.dataset.contentNo);

                /*
                 * 콘텐츠 번호가 양의 정수가 아니면
                 * 잘못된 요청을 서버에 보내지 않습니다.
                 */
                if (!Number.isInteger(contentNo)
                        || contentNo <= 0) {

                    await showAlert("콘텐츠 정보를 확인할 수 없습니다.", "warning");
                    return;
                }

                favoriteProcessing = true;
                favoriteButton.disabled = true;

                favoriteButton.classList.add(
                    "is-processing"
                );

                try {

                    /*
                     * FavoriteController의 찜 토글 주소로
                     * 콘텐츠 번호를 JSON 형태로 전송합니다.
                     */
                    const response =
                        await fetch(
                            `${pageContextPath}/favorite/toggle`,
                            {
                                method: "POST",
                                headers: {
                                    "Content-Type": "application/json"
                                },
                                body: JSON.stringify({
                                    contentNo: contentNo
                                })
                            }
                        );

                    /*
                     * 세션이 만료되어 401이 반환되면
                     * 다음 클릭부터도 로그인 안내가 나타나도록
                     * 버튼 상태를 변경합니다.
                     */
                    if (response.status === 401) {

                        favoriteButton.dataset.loginRequired =
                            "true";

                        await showAlert(
                            "로그인 정보가 만료되었습니다. "
                            + "다시 로그인해주세요.",
                            "warning"
                        );

                        return;
                    }

                    /*
                     * 2xx 이외 응답은 오류로 처리합니다.
                     */
                    if (!response.ok) {

                        throw new Error(
                            `찜 처리 실패: ${response.status}`
                        );
                    }

                    /*
                     * 서버가 반환한 현재 찜 상태를 읽어
                     * 버튼의 문구와 스타일을 변경합니다.
                     */
                    const result =
                        await response.json();

                    updateFavoriteButton(
                        favoriteButton,
                        result.active === true
                    );

                } catch (error) {

                    console.error(
                        "콘텐츠 찜 처리 오류:",
                        error
                    );

                    await showAlert(
                        "찜 처리 중 오류가 발생했습니다.",
                        "error"
                    );

                } finally {

                    /*
                     * 성공 또는 실패와 관계없이 처리 상태를 해제하여
                     * 사용자가 버튼을 다시 누를 수 있게 합니다.
                     */
                    favoriteProcessing = false;
                    favoriteButton.disabled = false;

                    favoriteButton.classList.remove(
                        "is-processing"
                    );
                }
            }
        );
    }

    /**
     * 서버가 반환한 찜 상태를 버튼에 반영합니다.
     *
     * @param {HTMLButtonElement} button
     *        상태를 변경할 찜 버튼
     *
     * @param {boolean} active
     *        true이면 찜 활성화, false이면 찜 해제
     */
    function updateFavoriteButton(
        button,
        active) {

        /*
         * 다음 처리와 화면 상태 확인에 사용할
         * data-active 값을 갱신합니다.
         */
        button.dataset.active =
            active ? "true" : "false";

        /*
         * 찜 활성화 시 CSS용 is-active 클래스를 추가하고,
         * 찜 해제 시 제거합니다.
         */
        button.classList.toggle(
            "is-active",
            active
        );

        /*
         * 스크린리더가 버튼의 눌림 상태를 알 수 있도록
         * aria-pressed 속성도 함께 갱신합니다.
         */
        button.setAttribute(
            "aria-pressed",
            active ? "true" : "false"
        );

        /*
         * 사용자가 현재 찜 상태를 즉시 확인할 수 있도록
         * 버튼 문구와 하트 모양을 변경합니다.
         */
        button.textContent =
            active
                ? "♥ 찜 완료"
                : "♡ 찜하기";
    }
});