package com.project.oditji.subscription.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** 구독 계산기 화면 라우팅을 검증합니다. */
class SubscriptionControllerTest {

    @Test
    void calculatorShouldReturnCalculatorView() {
        SubscriptionController controller = new SubscriptionController();

        assertEquals("subscription/calculator", controller.calculator());
    }
}
