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
 * orderManage.jsp - 주문 관리
 * ========================================================= */
function openOrderStatusModal(orderNo, productName, status) {
    document.getElementById('statusOrderNo').value = orderNo;
    document.getElementById('statusOrderNoText').textContent = orderNo;
    document.getElementById('statusProductName').textContent = productName;
    document.getElementById('statusCurrent').textContent = status;

    var radios = document.getElementsByName('orderStatus');
    for (var i = 0; i < radios.length; i++) {
        radios[i].checked = (radios[i].value === status);
    }

    document.getElementById('orderStatusModal').classList.add('open');
}

function openRefundModal(cancelNo, orderItemNo, productName, memberId, reason) {
    document.getElementById('refundCancelNo').value = cancelNo;
    document.getElementById('refundOrderItemNo').textContent = orderItemNo;
    document.getElementById('refundProductName').textContent = productName;
    document.getElementById('refundMemberId').textContent = memberId;
    document.getElementById('refundReason').value = reason;
    document.getElementById('refundModal').classList.add('open');
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
