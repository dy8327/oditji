package com.project.oditji.business.service;

import com.project.oditji.business.vo.NtsBusinessVerifyVO;

public interface NtsBusinessService {

    NtsBusinessVerifyVO verifyBusiness(
            String businessNumber,
            String representativeName,
            String openDate);
}