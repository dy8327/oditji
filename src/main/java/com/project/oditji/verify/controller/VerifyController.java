package com.project.oditji.verify.controller;

import com.project.oditji.verify.service.VerifyService;
import com.project.oditji.verify.vo.IdentityVerifyRequestVO;
import com.project.oditji.verify.vo.IdentityVerifyResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/verify")
public class VerifyController {

    private final VerifyService verifyService;

    @Value("${portone.store-id:test-store-id}")
    private String storeId;

    // 간편 본인인증 채널키
    @Value("${portone.identity1.channel-key:test-simple-identity-channel-key}")
    private String identity1ChannelKey;

    // SMS 본인인증 채널키
    @Value("${portone.identity2.channel-key:test-sms-identity-channel-key}")
    private String identity2ChannelKey;

    public VerifyController(VerifyService verifyService) {
        this.verifyService = verifyService;
    }

    @GetMapping
    public String verifyPage(Model model) {
        model.addAttribute("storeId", storeId);
        model.addAttribute("identity1ChannelKey", identity1ChannelKey);
        model.addAttribute("identity2ChannelKey", identity2ChannelKey);

        return "verify/verify";
    }

    @PostMapping("/complete")
    @ResponseBody
    public IdentityVerifyResultVO complete(@RequestBody IdentityVerifyRequestVO requestVO) {
        return verifyService.verify(requestVO.getIdentityVerificationId());
    }
}