package com.project.oditji.verify.service;

import tools.jackson.databind.JsonNode;
import com.project.oditji.verify.dao.VerifyDAO;
import com.project.oditji.verify.vo.IdentityVerifyLogVO;
import com.project.oditji.verify.vo.IdentityVerifyResultVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;

@Service
public class VerifyServiceImpl implements VerifyService {

    private final VerifyDAO verifyDAO;
    private final RestClient restClient;

    @Value("${portone.api-secret}")
    private String portoneApiSecret;

    public VerifyServiceImpl(VerifyDAO verifyDAO) {
        this.verifyDAO = verifyDAO;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.portone.io")
                .build();
    }

    @Override
    public IdentityVerifyResultVO verify(String identityVerificationId) {
        if (identityVerificationId == null || identityVerificationId.trim().isEmpty()) {
            return IdentityVerifyResultVO.fail("본인인증 ID가 없습니다.");
        }

        try {
            JsonNode response = getIdentityVerification(identityVerificationId);

            JsonNode identityVerification = response.path("identityVerification");
            if (identityVerification.isMissingNode() || identityVerification.isNull()) {
                identityVerification = response;
            }

            String status = getText(identityVerification, "status");

            if (!"VERIFIED".equalsIgnoreCase(status)) {
                IdentityVerifyLogVO failLog = new IdentityVerifyLogVO();
                failLog.setVerifyId(identityVerificationId);
                failLog.setVerifyStatus("FAILED");
                failLog.setRawStatus(status);
                failLog.setAdultYn("N");
                verifyDAO.insertVerifyLog(failLog);

                return IdentityVerifyResultVO.fail("본인인증이 완료 상태가 아닙니다. 현재 상태: " + status);
            }

            JsonNode customer = identityVerification.path("verifiedCustomer");
            if (customer.isMissingNode() || customer.isNull()) {
                customer = identityVerification.path("customer");
            }

            String name = getText(customer, "name");
            String birthDate = getText(customer, "birthDate");
            String phoneNumber = getText(customer, "phoneNumber");
            String gender = getText(customer, "gender");

            String adultYn = isAdult(birthDate) ? "Y" : "N";

            IdentityVerifyLogVO logVO = new IdentityVerifyLogVO();
            logVO.setVerifyId(identityVerificationId);
            logVO.setVerifyStatus("VERIFIED");
            logVO.setName(name);
            logVO.setBirthDate(birthDate);
            logVO.setPhoneNumber(phoneNumber);
            logVO.setGender(gender);
            logVO.setAdultYn(adultYn);
            logVO.setRawStatus(status);

            verifyDAO.insertVerifyLog(logVO);

            return IdentityVerifyResultVO.success(
                    "본인인증이 완료되었습니다.",
                    name,
                    birthDate,
                    phoneNumber,
                    gender,
                    adultYn
            );

        } catch (Exception e) {
            IdentityVerifyLogVO failLog = new IdentityVerifyLogVO();
            failLog.setVerifyId(identityVerificationId);
            failLog.setVerifyStatus("FAILED");
            failLog.setAdultYn("N");
            failLog.setRawStatus("SERVER_ERROR");
            verifyDAO.insertVerifyLog(failLog);

            return IdentityVerifyResultVO.fail("서버에서 본인인증 결과 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private JsonNode getIdentityVerification(String identityVerificationId) {
        String encodedId = UriUtils.encodePathSegment(identityVerificationId, StandardCharsets.UTF_8);

        return restClient.get()
                .uri("/identity-verifications/{identityVerificationId}", encodedId)
                .header(HttpHeaders.AUTHORIZATION, "PortOne " + portoneApiSecret)
                .retrieve()
                .body(JsonNode.class);
    }

    private String getText(JsonNode node, String fieldName) {
    if (node == null || node.isMissingNode() || node.isNull()) {
        return null;
    }

    JsonNode value = node.path(fieldName);

    if (value.isMissingNode() || value.isNull()) {
        return null;
    }

    return value.asString();
    }

    private boolean isAdult(String birthDate) {
        if (birthDate == null || birthDate.trim().isEmpty()) {
            return false;
        }

        try {
            LocalDate birth = LocalDate.parse(birthDate);
            LocalDate today = LocalDate.now();

            return Period.between(birth, today).getYears() >= 19;
        } catch (Exception e) {
            return false;
        }
    }
}