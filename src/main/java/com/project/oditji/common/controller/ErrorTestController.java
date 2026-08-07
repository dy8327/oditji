package com.project.oditji.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorTestController {

    /**
     * 500 오류 페이지 동작 확인용 임시 URL
     */
    @GetMapping("/test/error/500")
    public String testInternalServerError() {
        throw new IllegalStateException("500 오류 페이지 테스트용 예외입니다.");
    }
}