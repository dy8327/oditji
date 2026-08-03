package com.project.oditji.common.vo;

import java.io.Serializable;

/**
 * 주문서, 주문 조회, 관리자 주문 조회에서 공통으로 사용하는 배송지 정보입니다.
 */
public abstract class OrderAddressBaseVO implements Serializable {

    private static final long serialVersionUID = 1L;

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
