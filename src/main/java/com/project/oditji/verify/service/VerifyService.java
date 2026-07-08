package com.project.oditji.verify.service;

import com.project.oditji.verify.vo.IdentityVerifyResultVO;

public interface VerifyService {

    IdentityVerifyResultVO verify(String identityVerificationId);
}