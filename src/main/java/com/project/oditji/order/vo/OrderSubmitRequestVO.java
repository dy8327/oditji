package com.project.oditji.order.vo;

/**
 * 주문서 화면(order.jsp)에서 배송 정보를 입력하고
 * "결제하기" 버튼을 눌렀을 때 사용하는 요청 VO.
 * POST /order/submit 요청 본문에 대응한다.
 */
public class OrderSubmitRequestVO {

    private String receiverName;
    private String receiverPhone;
    private String address;

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
