package com.project.oditji.common.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.RequestDispatcher;

/** JSON 요청 판별의 Accept/Content-Type non-null false 분기를 보완합니다. */
class CustomErrorControllerJsonNegativeHeaderCoverageTest {

    @Test
    void nonJsonAcceptAndContentTypeShouldRemainHtmlResponse() {
        CustomErrorController controller = new CustomErrorController();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/oditji/error");
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 400);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/member/edit");
        request.addHeader("Accept", "text/html,application/xhtml+xml");
        request.setContentType("text/plain;charset=UTF-8");
        request.addHeader("X-Requested-With", "fetch");

        ModelAndView response = assertInstanceOf(
                ModelAndView.class,
                controller.handleError(request));

        assertEquals("error/common", response.getViewName());
        assertEquals(400, response.getModel().get("errorCode"));
    }
}
