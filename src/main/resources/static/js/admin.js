/*
 * ODITJI 관리자 페이지 공통 스크립트
 * 각 admin JSP 파일에 인라인으로 있던 <script> 코드를 이 파일로 통합함.
 * (businessManage, eventManage, orderManage, productManage)
 */

/* =========================================================
 * 공통 - 모달 닫기
 * 여러 페이지에서 동일하게 쓰이던 closeModal 함수를 하나로 통합
 * ========================================================= */
function closeModal(id) {
    document.getElementById(id).classList.remove('open');
}


/* =========================================================
 * memberManage.jsp - 회원 관리 (필터/일괄처리/개별처리)
 * ========================================================= */

/*
 * JSP가 #memberListState의 data-* 속성에 현재 검색어/필터/페이지 상태를
 * 실어 보내면, 개별 처리(정지/복구/탈퇴) 버튼이 그 상태를 그대로 유지한 채
 * 다시 목록으로 돌아갈 수 있도록 여기서 읽어온다.
 */
function getMemberListState() {

    var stateEl = document.getElementById('memberListState');

    if (!stateEl) {
        return { context: '', keyword: '', searchType: '', status: '', memberType: '', page: '1' };
    }

    return {
        context: stateEl.dataset.context || '',
        keyword: stateEl.dataset.keyword || '',
        searchType: stateEl.dataset.searchType || '',
        status: stateEl.dataset.status || '',
        memberType: stateEl.dataset.memberType || '',
        page: stateEl.dataset.page || '1'
    };
}

/*
 * 회원 목록 테이블은 체크박스 일괄처리 <form> 안에 있어,
 * 개별 처리 버튼(정지/복구/탈퇴)을 그 안에 또 다른 <form>으로 중첩할 수 없다.
 * 그래서 클릭 시점에 별도의 <form>을 만들어 제출하는 방식으로 처리한다.
 */
function submitMemberAction(actionPath, memberNo) {

    var state = getMemberListState();

    var form = document.createElement('form');
    form.method = 'POST';
    form.action = state.context + actionPath;
    form.style.display = 'none';

    var fields = {
        memberNo: memberNo,
        keyword: state.keyword,
        searchType: state.searchType,
        status: state.status,
        memberType: state.memberType,
        page: state.page
    };

    Object.keys(fields).forEach(function (name) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = name;
        input.value = fields[name];
        form.appendChild(input);
    });

    document.body.appendChild(form);
    form.submit();
}

function memberSuspend(memberNo) {
    submitMemberAction('/admin/member/suspend', memberNo);
}

function memberRestore(memberNo) {
    submitMemberAction('/admin/member/restore', memberNo);
}

function memberWithdraw(memberNo) {

    var confirmed = confirm('해당 회원 데이터를 완전히 삭제하시겠습니까?\n삭제 후 복구할 수 없습니다.');

    if (!confirmed) {
        return;
    }

    submitMemberAction('/admin/member/withdraw', memberNo);
}

/* 헤더의 "전체 선택" 체크박스와 각 행 체크박스를 동기화한다. */
function toggleAllMembers(checkAllBox) {

    var checks = document.querySelectorAll('.member-check');

    checks.forEach(function (check) {
        check.checked = checkAllBox.checked;
    });

    updateSelectedMemberCount();
}

/* 선택된 회원 수를 갱신하고, 1명도 선택하지 않았으면 일괄처리 버튼을 비활성화한다. */
function updateSelectedMemberCount() {

    var checkedList = document.querySelectorAll('.member-check:checked');
    var allChecks = document.querySelectorAll('.member-check');
    var count = checkedList.length;

    var countEl = document.getElementById('selectedMemberCount');
    if (countEl) {
        countEl.textContent = count;
    }

    var checkAllBox = document.getElementById('memberCheckAll');
    if (checkAllBox) {
        checkAllBox.checked = (allChecks.length > 0 && count === allChecks.length);
    }

    ['bulkSuspendBtn', 'bulkRestoreBtn', 'bulkDeleteBtn'].forEach(function (id) {
        var btn = document.getElementById(id);
        if (btn) {
            btn.disabled = (count === 0);
        }
    });
}

/* 일괄처리 버튼(정지/복구/삭제) 클릭 시 선택 인원을 확인시켜준다. */
function confirmMemberBulkAction(label) {

    var count = document.querySelectorAll('.member-check:checked').length;

    if (count === 0) {
        alert('선택된 회원이 없습니다.');
        return false;
    }

    var message = count + '명의 회원을 ' + label + ' 처리하시겠습니까?';

    if (label === '완전삭제') {
        message += '\n삭제 후 복구할 수 없습니다.';
    }

    return confirm(message);
}


/* =========================================================
 * reviewManage.jsp / productReviewManage.jsp - 리뷰 관리 (일괄처리/내용보기)
 * 콘텐츠 리뷰와 상품 리뷰 화면은 구조가 동일해 같은 함수를 공용으로 쓴다.
 * ========================================================= */

/* 헤더의 "전체 선택" 체크박스와 각 행 체크박스를 동기화한다. */
function toggleAllReviews(checkAllBox) {

    var checks = document.querySelectorAll('.review-check');

    checks.forEach(function (check) {
        check.checked = checkAllBox.checked;
    });

    updateSelectedReviewCount();
}

/* 선택된 리뷰 수를 갱신하고, 1건도 선택하지 않았으면 일괄처리 버튼을 비활성화한다. */
function updateSelectedReviewCount() {

    var checkedList = document.querySelectorAll('.review-check:checked');
    var allChecks = document.querySelectorAll('.review-check');
    var count = checkedList.length;

    var countEl = document.getElementById('selectedReviewCount');
    if (countEl) {
        countEl.textContent = count;
    }

    var checkAllBox = document.getElementById('reviewCheckAll');
    if (checkAllBox) {
        checkAllBox.checked = (allChecks.length > 0 && count === allChecks.length);
    }

    ['bulkDeleteReviewBtn', 'bulkApproveReviewBtn', 'bulkRejectReviewBtn'].forEach(function (id) {
        var btn = document.getElementById(id);
        if (btn) {
            btn.disabled = (count === 0);
        }
    });
}

/* 일괄처리 버튼(삭제/승인/반려) 클릭 시 선택 건수를 확인시켜준다. */
function confirmReviewBulkAction(label) {

    var count = document.querySelectorAll('.review-check:checked').length;

    if (count === 0) {
        alert('선택된 리뷰가 없습니다.');
        return false;
    }

    var message = count + '건의 리뷰를 ' + label + ' 처리하시겠습니까?';

    if (label === '삭제' || label === '승인') {
        message += '\n삭제된 리뷰는 복구할 수 없습니다.';
    }

    return confirm(message);
}

/*
 * 리뷰 내용은 길어질 수 있어 표에서는 2줄로 잘려 보이므로,
 * "내용보기" 버튼을 누르면 전체 내용을 팝업으로 보여준다.
 * 리뷰 내용에 따옴표나 줄바꿈이 섞여 있어도 안전하도록 onclick 인라인 문자열이 아닌
 * data-* 속성(버튼 자신)에서 값을 읽어온다.
 */
function openReviewContentModal(button) {

    document.getElementById('reviewContentReviewNo').value = button.dataset.reviewNo;
    document.getElementById('reviewContentWriter').textContent = displayOrDash(button.dataset.writer);
    document.getElementById('reviewContentTarget').textContent = displayOrDash(button.dataset.target);
    document.getElementById('reviewContentRating').textContent = displayOrDash(button.dataset.rating) + '점';
    document.getElementById('reviewContentDate').textContent = displayOrDash(button.dataset.createdAt);
    document.getElementById('reviewContentBody').textContent = displayOrDash(button.dataset.content);

    document.getElementById('reviewContentModal').classList.add('open');
}

function deleteContentReview(reviewNo) {

    if (!confirm('이 리뷰를 삭제하시겠습니까?')) {
        return;
    }

    var form = document.createElement('form');

    form.method = 'post';
    form.action = '/oditji/admin/review/delete';

    var input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'reviewNo';
    input.value = reviewNo;

    form.appendChild(input);

    document.body.appendChild(form);
    form.submit();
}


function deleteProductReview(reviewNo) {

    if (!confirm('이 리뷰를 삭제하시겠습니까?')) {
        return;
    }

    var form = document.createElement('form');

    form.method = 'post';
    form.action = '/oditji/admin/productReview/delete';

    var input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'reviewNo';
    input.value = reviewNo;

    form.appendChild(input);

    document.body.appendChild(form);
    form.submit();
}


/* =========================================================
 * businessManage.jsp - 사업자 관리
 * ========================================================= */
function openGradeModal(businessNo, name, memberId, email, currentGrade) {
    document.getElementById('gradeBusinessNo').value = businessNo;
    document.getElementById('gradeBusinessName').textContent = name;
    document.getElementById('gradeBusinessId').textContent = memberId;
    document.getElementById('gradeBusinessEmail').textContent = email;
    document.getElementById('gradeBusinessCurrent').textContent = currentGrade;

    var radios = document.getElementsByName('gradeName');
    for (var i = 0; i < radios.length; i++) {
        radios[i].checked = (radios[i].value === currentGrade);
    }

    document.getElementById('gradeModal').classList.add('open');
}

function openApprovalModal(businessNo, businessName, memberId, email, businessNumber) {
    document.getElementById('approvalBusinessNo').value = businessNo;
    document.getElementById('approvalBusinessName').textContent = businessName;
    document.getElementById('approvalMemberId').textContent = memberId;
    document.getElementById('approvalEmail').textContent = email;
    document.getElementById('approvalBusinessNumber').textContent = businessNumber;
    document.getElementById('approvalModal').classList.add('open');
}


/* =========================================================
 * eventManage.jsp - 이벤트 관리
 * ========================================================= */

/* HTML 특수문자를 이스케이프해서 상품명/판매자명 등을 안전하게 innerHTML에 넣는다. */
function escapeHtml(value) {
    if (value === null || value === undefined) {
        return '';
    }
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

/*
 * 숫자를 "1,234원" 형태로 표시. 값이 숫자가 아니면 원본 문자열을 이스케이프해서 그대로 보여준다.
 * (아래 orderManage.jsp 섹션에도 동일한 이름의 formatWon이 있어, 이름이 겹치지 않도록
 * 이벤트 상세 전용 함수는 formatEventWon으로 따로 둔다.)
 */
function formatEventWon(value) {
    var amount = Number(value);
    if (isNaN(amount)) {
        return escapeHtml(value);
    }
    return amount.toLocaleString('ko-KR') + '원';
}

/*
 * 이벤트 상세 팝업.
 *
 * 목록 화면에서는 연결 상품 개수만 보여주고, 상품별 상세 할인 내역(상품명/판매자/
 * 기존가격/할인율/적용가격)은 이 팝업의 표에서만 그려준다. 서버가 내려준
 * productDetail("상품명|할인율|기존가격|적용가격|판매자명" 을 ';'로 이어붙인 문자열)을
 * 그대로 파싱해서 행을 만든다.
 *
 * status(원본 코드값)가 WAITING이 아니면 승인/반려 버튼을 숨기고 안내 문구만 보여준다.
 * (WAITING이 아닌 이벤트에 승인/반려를 시도하면 adminMapper.xml의 updateEventStatus가
 * 0건을 갱신해 500 오류로 이어지던 문제를 화면에서 원천적으로 막기 위함이다.)
 */
function openEventDetailModal(
    eventNo,
    businessName,
    title,
    period,
    createdAt,
    status,
    statusLabel,
    productDetail
) {

    document.getElementById('reqeventNo').value = eventNo;
    document.getElementById('reqBusinessName').textContent = businessName;
    document.getElementById('reqtitle').textContent = title;
    document.getElementById('reqEventPeriod').textContent = period;
    document.getElementById('reqCreatedAt').textContent = createdAt;
    document.getElementById('reqStatus').textContent = statusLabel;

    var tbody = document.getElementById('reqProductTableBody');
    var items = (productDetail || '')
        .split(';')
        .filter(function (item) { return item.trim() !== ''; });

    if (items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="ep-empty">연결된 상품이 없습니다.</td></tr>';
    } else {
        tbody.innerHTML = items.map(function (item) {
            var parts = item.split('|');
            var name = parts[0] || '';
            var rate = Number(parts[1]) || 0;
            var price = parts[2] || '0';
            var discounted = parts[3] || price;
            var business = parts[4] || businessName;

            // 할인이 없는 상품은 기존가격에 취소선을 넣지 않고, 할인율도 배지 대신 "-"로 표시한다.
            var priceCell = rate > 0
                ? '<span class="ep-price-original">' + formatEventWon(price) + '</span>'
                : formatEventWon(price);

            var rateCell = rate > 0
                ? '<span class="ep-rate-badge">' + rate + '% 할인</span>'
                : '<span class="ep-rate-none">-</span>';

            return '<tr>'
                + '<td class="ep-name">' + escapeHtml(name) + '</td>'
                + '<td class="ep-seller">' + escapeHtml(business) + '</td>'
                + '<td class="ep-price">' + priceCell + '</td>'
                + '<td class="ep-rate">' + rateCell + '</td>'
                + '<td class="ep-price ep-price-final"><strong>' + formatEventWon(discounted) + '</strong></td>'
                + '</tr>';
        }).join('');
    }

    // 대기(WAITING) 상태일 때만 승인/반려 버튼을 보여주고, 이미 처리된 이벤트는
    // 내용만 확인할 수 있도록 버튼 대신 안내 문구를 보여준다. (닫기 버튼은 항상 보여준다.)
    var isWaiting = (status === 'WAITING');

    var approveBtn = document.getElementById('eventApproveBtn');
    var rejectBtn = document.getElementById('eventRejectBtn');
    var noteEl = document.getElementById('eventReadonlyNote');

    if (approveBtn) {
        approveBtn.style.display = isWaiting ? '' : 'none';
    }
    if (rejectBtn) {
        rejectBtn.style.display = isWaiting ? '' : 'none';
    }
    if (noteEl) {
        noteEl.style.display = isWaiting ? 'none' : '';
    }

    document.getElementById('eventRequestModal').classList.add('open');
}


/* =========================================================
 * orderManage.jsp - 주문 조회 (조회 전용)
 * 배송 상태 변경, 주문 취소, 환불 승인/거절은 사업자 페이지에서 처리하므로
 * 이 화면의 모달은 값을 표시만 하고 서버로 전송하는 폼이 없다.
 * ========================================================= */

// 숫자를 "1,234원" 형태로 표시. 값이 없으면 "-"를 표시한다.
function formatWon(value) {
    var num = Number(value);
    if (!value || isNaN(num)) {
        return '-';
    }
    return num.toLocaleString() + '원';
}

// 값이 비어있으면 "-"를 표시한다. (undefined 문자열 등도 방어)
function displayOrDash(value) {
    if (!value || value === 'null' || value === 'undefined') {
        return '-';
    }
    return value;
}

// ORDERS.ORDER_STATUS / DELIVERY.STATUS / ORDER_ITEM.STATUS 코드값을 한글 라벨로 변환한다.
// 사업자 주문/배송 페이지(orderList.jsp, deliveryList.jsp)의 표기와 동일하게 맞춘다.
var ORDER_STATUS_LABELS = {
    'ORDERED': '주문 완료',
    'PAID': '결제 완료',
    'PREPARING': '상품 준비 중',
    'SHIPPING': '배송 중',
    'DELIVERED': '배송 완료',
    'CONFIRMED': '구매 확정',
    'CANCELED': '주문 취소',
    'CANCEL_REQUEST': '취소 요청 중',
    'REFUNDED': '환불 완료'
};

function orderStatusLabel(status) {
    if (!status || status === 'null' || status === 'undefined') {
        return '-';
    }
    // 매핑 표에 없는 새 상태값이 추가되더라도 원본 코드 그대로 노출하지 않고
    // 최소한 언더스코어를 공백으로 바꿔 사람이 읽기 쉬운 형태로 보여준다.
    return ORDER_STATUS_LABELS[status] || status.replace(/_/g, ' ');
}

function openOrderDetailModal(
    orderNo, orderItemNo, productName, contentTitle, quantity,
    productPrice, totalAmount, orderStatus, deliveryStatus,
    receiverName, receiverPhone, address, trackingNumber, courier, createdAt
) {
    document.getElementById('detailOrderNo').textContent = displayOrDash(orderNo);
    document.getElementById('detailProductName').textContent = displayOrDash(productName);
    document.getElementById('detailContentTitle').textContent = displayOrDash(contentTitle);
    document.getElementById('detailQuantity').textContent = displayOrDash(quantity) + '개';
    document.getElementById('detailProductPrice').textContent = formatWon(productPrice);
    document.getElementById('detailTotalAmount').textContent = formatWon(totalAmount);
    document.getElementById('detailOrderStatus').textContent = orderStatusLabel(orderStatus);
    document.getElementById('detailDeliveryStatus').textContent = orderStatusLabel(deliveryStatus);
    document.getElementById('detailReceiverName').textContent = displayOrDash(receiverName);
    document.getElementById('detailReceiverPhone').textContent = displayOrDash(receiverPhone);
    document.getElementById('detailAddress').textContent = displayOrDash(address);
    document.getElementById('detailCourier').textContent = displayOrDash(courier);
    document.getElementById('detailTrackingNumber').textContent = displayOrDash(trackingNumber);
    document.getElementById('detailCreatedAt').textContent = displayOrDash(createdAt);

    document.getElementById('orderDetailModal').classList.add('open');
}

function openRefundDetailModal(
    cancelNo, orderNo, orderItemNo, productName, memberId, cancelType,
    quantity, refundAmount, reason, cancelStatus, rejectReason,
    createdAt, processedAt
) {
    var statusLabel = cancelStatus;
    if (cancelStatus === 'WAITING') {
        statusLabel = '처리 대기';
    } else if (cancelStatus === 'APPROVED') {
        statusLabel = '승인 완료';
    } else if (cancelStatus === 'REJECTED') {
        statusLabel = '반려';
    }

    var typeLabel = (cancelType === 'FULL') ? '전체 취소' : '상품 부분 취소';

    document.getElementById('refundDetailOrderNo').textContent = displayOrDash(orderNo);
    document.getElementById('refundDetailProductName').textContent = displayOrDash(productName);
    document.getElementById('refundDetailMemberId').textContent = displayOrDash(memberId);
    document.getElementById('refundDetailType').textContent = typeLabel;
    document.getElementById('refundDetailQuantity').textContent = displayOrDash(quantity) + '개';
    document.getElementById('refundDetailAmount').textContent = formatWon(refundAmount);
    document.getElementById('refundDetailStatus').textContent = displayOrDash(statusLabel);
    document.getElementById('refundDetailCreatedAt').textContent = displayOrDash(createdAt);
    document.getElementById('refundDetailProcessedAt').textContent = displayOrDash(processedAt);
    document.getElementById('refundDetailReason').value = displayOrDash(reason);
    document.getElementById('refundDetailRejectReason').value = displayOrDash(rejectReason);

    document.getElementById('refundDetailModal').classList.add('open');
}


/* =========================================================
 * productManage.jsp - 상품 관리
 * ========================================================= */

// 숫자를 "1,234원" 형태로 표시한다. (formatEventWon / formatWon과 동일한 역할이지만,
// 각 화면 섹션이 독립적으로 유지되도록 상품 관리 전용으로 따로 둔다.)
function formatProductWon(value) {
    var amount = Number(value);
    if (isNaN(amount)) {
        return escapeHtml(value);
    }
    return amount.toLocaleString('ko-KR') + '원';
}

/*
 * 상품 요청 상세 팝업.
 *
 * data-* 속성을 사용하여 상품명이나 설명에 따옴표가 포함되어도
 * JavaScript 함수 호출 문자열이 깨지지 않도록 한다.
 *
 * status(원본 코드값)가 WAITING(등록·수정 승인 대기) 또는 DELETE_REQUESTED(삭제 요청)가
 * 아니면(즉 이미 APPROVED/REJECTED로 확정된 상품이면) 승인/반려 버튼을 숨기고
 * 안내 문구만 보여준다. eventManage.jsp의 openEventDetailModal()과 동일한 방식이다.
 * (WAITING/DELETE_REQUESTED가 아닌 상품에 승인/반려를 시도하면 updateProductStatus가
 * 의도치 않게 상태를 덮어써버릴 수 있는 문제를 화면에서 막기 위함이다.)
 */
function openProductRequestModal(button) {

    var status = button.dataset.status || '';

    document.getElementById('reqProductNo').value = button.dataset.productNo;
    document.getElementById('reqProductStatusRaw').value = status;

    document.getElementById('reqProductBusinessName').textContent = button.dataset.businessName || '';
    document.getElementById('reqProductName').textContent = button.dataset.productName || '';
    document.getElementById('reqProductContentTitle').textContent = button.dataset.contentTitle || '';
    document.getElementById('reqProductPrice').textContent = formatProductWon(button.dataset.price || 0);

    var discountRate = Number(button.dataset.discountRate);
    document.getElementById('reqProductDiscountRate').textContent =
        (discountRate > 0) ? discountRate + '% 할인' : '할인 없음';

    document.getElementById('reqProductStock').textContent = (button.dataset.stock || '0') + '개';
    document.getElementById('reqProductCreatedAt').textContent = button.dataset.createdAt || '';
    document.getElementById('reqProductStatus').textContent = button.dataset.statusLabel || '';
    document.getElementById('reqProductDescription').value = button.dataset.description || '';

    var isActionable = (status === 'WAITING' || status === 'DELETE_REQUESTED');

    var approveBtn = document.getElementById('productApproveBtn');
    var rejectBtn = document.getElementById('productRejectBtn');
    var noteEl = document.getElementById('productReadonlyNote');

    if (approveBtn) {
        approveBtn.style.display = isActionable ? '' : 'none';
    }
    if (rejectBtn) {
        rejectBtn.style.display = isActionable ? '' : 'none';
    }
    if (noteEl) {
        noteEl.style.display = isActionable ? 'none' : '';
    }

    document.getElementById('productRequestModal').classList.add('open');
}

/*
 * 승인/반려 확인 문구는 페이지의 현재 탭이 아니라, 모달을 열 때 저장해 둔
 * 해당 상품 자체의 실제 상태(hidden input의 status 값)를 기준으로 판단한다.
 * '전체' 탭에서 삭제 요청 건을 열람하는 경우에도 정확한 문구가 나오도록 하기 위함이다.
 */
function getProductRequestRawStatus() {
    var statusInput = document.querySelector('#productRequestForm input[name="status"]');
    return statusInput ? statusInput.value : '';
}

function confirmProductApprove() {
    var status = getProductRequestRawStatus();

    if (status === 'DELETE_REQUESTED') {
        return confirm('삭제 요청을 승인하면 해당 상품이 DB에서 최종 삭제됩니다. 계속하시겠습니까?');
    }

    return confirm('이 상품 요청을 승인하시겠습니까?');
}

function confirmProductReject() {
    var status = getProductRequestRawStatus();

    if (status === 'DELETE_REQUESTED') {
        return confirm('이 상품의 삭제 요청을 반려하시겠습니까?');
    }

    return confirm('이 상품 요청을 반려하시겠습니까?');
}


/* =========================================================
 * monitoring.jsp - 모니터링 (방문자 추이 / 상품 클릭 TOP5 차트)
 *
 * admin.js는 정적 파일이라 JSTL EL을 쓸 수 없다.
 * 컨트롤러(AdminController)가 org.json으로 미리 만든 JSON 문자열을
 * JSP의 canvas data-chart 속성에 실어 보내면, 여기서 JSON.parse 해서 사용한다.
 * ========================================================= */
function initMonitoringCharts() {

    var visitorCanvas = document.getElementById('visitorTrendChart');

    if (visitorCanvas) {

        var visitorData = JSON.parse(visitorCanvas.dataset.chart || '[]');

        new Chart(visitorCanvas, {
            type: 'line',
            data: {
                labels: visitorData.map(function (d) { return d.date; }),
                datasets: [{
                    label: '방문자 수',
                    data: visitorData.map(function (d) { return d.count; }),
                    borderColor: '#4f8dfd',
                    backgroundColor: 'rgba(79,141,253,0.15)',
                    tension: 0.3,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: { legend: { display: false } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }

    var popularCanvas = document.getElementById('popularClickChart');

    if (popularCanvas) {

        var popularData = JSON.parse(popularCanvas.dataset.chart || '[]');

        new Chart(popularCanvas, {
            type: 'bar',
            data: {
                labels: popularData.map(function (d) { return d.name; }),
                datasets: [{
                    label: '클릭 수',
                    data: popularData.map(function (d) { return d.count; }),
                    backgroundColor: '#4f8dfd'
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                plugins: { legend: { display: false } },
                scales: { x: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', initMonitoringCharts);
