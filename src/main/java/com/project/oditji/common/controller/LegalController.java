package com.project.oditji.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    @GetMapping("/terms")
    public String terms() {
        return "legal/terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "legal/privacy";
    }
}