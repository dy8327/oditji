package com.project.oditji.payment.vo;

public class PaymentCompleteResponseVO {

    private boolean success;
    private String message;

    public PaymentCompleteResponseVO() {
    }

    public PaymentCompleteResponseVO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static PaymentCompleteResponseVO success(String message) {
        return new PaymentCompleteResponseVO(true, message);
    }

    public static PaymentCompleteResponseVO fail(String message) {
        return new PaymentCompleteResponseVO(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}