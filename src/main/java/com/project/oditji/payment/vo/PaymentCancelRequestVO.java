package com.project.oditji.payment.vo;

public class PaymentCancelRequestVO {

    private String paymentId;
    private String reason;

    public PaymentCancelRequestVO() {
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}