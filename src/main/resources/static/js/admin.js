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
function openEventRequestModal(eventNo, businessName, title, period, productDetail) {
    document.getElementById('reqeventNo').value = eventNo;
    document.getElementById('reqBusinessName').textContent = businessName;
    document.getElementById('reqtitle').textContent = title;
    document.getElementById('reqEventPeriod').textContent = period;
    document.getElementById('reqDescription').value = formatProductDetail(productDetail);
    document.getElementById('eventRequestModal').classList.add('open');
}

/*
 * 상품별 상세 내역 파싱
 *
 * productDetail 형식: "상품명|할인율|가격|할인적용가;상품명|할인율|가격|할인적용가;..."
 * (adminMapper.xml selectAdminEventList의 productDetail 컬럼)
 */
function formatProductDetail(productDetail) {

    if (!productDetail) {
        return '연결된 상품이 없습니다.';
    }

    const items = productDetail.split(';').filter(Boolean);

    if (items.length === 0) {
        return '연결된 상품이 없습니다.';
    }

    const lines = [];

    items.forEach(function (item) {

        const parts = item.split('|');

        const name = parts[0];
        const rate = Number(parts[1]);
        const price = Number(parts[2]);
        const discounted = Number(parts[3]);

        if (!name) {
            return;
        }

        if (rate > 0) {

            lines.push(
                name
                + ' : '
                + price.toLocaleString()
                + '원 → '
                + discounted.toLocaleString()
                + '원 (' + rate + '% 할인)'
            );

        } else {

            lines.push(name + ' : 할인 없음');

        }

    });

    return lines.join('\n');

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

/*
 * data-* 속성을 사용하여 상품명이나 설명에 따옴표가 포함되어도
 * JavaScript 함수 호출 문자열이 깨지지 않도록 수정한다.
 */
function openProductRequestModal(button) {
    document.getElementById('reqProductNo').value = button.dataset.productNo;
    document.getElementById('reqProductBusinessName').textContent = button.dataset.businessName || '';
    document.getElementById('reqProductName').textContent = button.dataset.productName || '';
    document.getElementById('reqProductPrice').textContent = (button.dataset.price || '0') + '원';
    document.getElementById('reqProductDescription').value = button.dataset.description || '';
    document.getElementById('productRequestModal').classList.add('open');
}

/*
 * 원래 코드는 JSP EL(${currentTab})을 스크립트 안에서 직접 사용했으나,
 * admin.js로 분리되면서 더 이상 JSP 엔진이 처리하지 않는 정적 파일이 되었기 때문에
 * ${currentTab} 표현식이 그대로 문자열로 남아 동작하지 않게 된다.
 * 대신 이미 화면에 렌더링되어 있는 productRequestForm의 hidden input(tab) 값을
 * 런타임에 읽어와 동일한 기능을 유지한다.
 */
function getProductRequestCurrentTab() {
    var tabInput = document.querySelector('#productRequestForm input[name="tab"]');
    return tabInput ? tabInput.value : '';
}

function confirmProductApprove() {
    const currentTab = getProductRequestCurrentTab();

    if (currentTab === 'delete') {
        return confirm('삭제 요청을 승인하면 해당 상품이 DB에서 최종 삭제됩니다. 계속하시겠습니까?');
    }

    return confirm('이 상품 요청을 승인하시겠습니까?');
}

function confirmProductReject() {
    const currentTab = getProductRequestCurrentTab();

    if (currentTab === 'delete') {
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
