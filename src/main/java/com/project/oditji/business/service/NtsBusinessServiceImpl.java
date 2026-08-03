package com.project.oditji.business.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.project.oditji.business.vo.NtsBusinessVerifyVO;

@Service
public class NtsBusinessServiceImpl implements NtsBusinessService {

    private static final String NTS_API_HOST = "api.odcloud.kr";
    private static final Logger log = LoggerFactory.getLogger(NtsBusinessServiceImpl.class);

    private final RestClient restClient;

    @Value("${nts.business.service-key}")
    private String serviceKey;

    public NtsBusinessServiceImpl() {
        this.restClient = RestClient.create();
    }

    @Override
    public NtsBusinessVerifyVO verifyBusiness(
            String businessNumber,
            String representativeName,
            String openDate) {

        validateRequiredValues(businessNumber, representativeName, openDate);

        String normalizedBusinessNumber = businessNumber.replaceAll("\\D", "");

        Map<String, Object> business = new HashMap<>();
        business.put("b_no", normalizedBusinessNumber);
        business.put("start_dt", openDate);
        business.put("p_nm", representativeName.trim());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("businesses", List.of(business));

        try {
            Map<?, ?> result = requestFirstResult(
                    "/api/nts-businessman/v1/validate",
                    requestBody,
                    "국세청 응답이 없습니다.",
                    "국세청 진위확인 결과가 없습니다.",
                    "국세청 응답 형식이 올바르지 않습니다."
            );

            String valid = String.valueOf(result.get("valid"));
            if ("01".equals(valid)) {
                return checkBusinessStatus(normalizedBusinessNumber);
            }

            return new NtsBusinessVerifyVO(
                    false,
                    null,
                    "사업자등록번호, 대표자명 또는 개업일이 국세청 등록정보와 일치하지 않습니다."
            );

        } catch (NtsResponseException e) {
            return new NtsBusinessVerifyVO(false, null, e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("국세청 사업자 진위 확인 실패", e);
            }
            return new NtsBusinessVerifyVO(
                    false,
                    null,
                    "국세청 사업자 확인 중 오류가 발생했습니다."
            );
        }
    }

    private void validateRequiredValues(
            String businessNumber,
            String representativeName,
            String openDate) {

        if (businessNumber == null || businessNumber.isBlank()) {
            throw new IllegalArgumentException("사업자등록번호를 입력해주세요.");
        }
        if (representativeName == null || representativeName.isBlank()) {
            throw new IllegalArgumentException("대표자명을 입력해주세요.");
        }
        if (openDate == null || !openDate.matches("\\d{8}")) {
            throw new IllegalArgumentException("개업일은 YYYYMMDD 형식으로 입력해주세요.");
        }
    }

    private NtsBusinessVerifyVO checkBusinessStatus(String businessNumber) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("b_no", List.of(businessNumber));

        try {
            Map<?, ?> result = requestFirstResult(
                    "/api/nts-businessman/v1/status",
                    requestBody,
                    "사업자 상태조회 응답이 없습니다.",
                    "사업자 상태조회 결과가 없습니다.",
                    "사업자 상태조회 응답 형식이 올바르지 않습니다."
            );

            Object statusObject = result.get("b_stt");
            String businessStatus = statusObject == null
                    ? ""
                    : statusObject.toString().trim();

            if ("계속사업자".equals(businessStatus)) {
                return new NtsBusinessVerifyVO(
                        true,
                        businessStatus,
                        "사업자 정보가 확인되었습니다. (계속사업자)"
                );
            }

            if (businessStatus.isBlank()) {
                return new NtsBusinessVerifyVO(
                        false,
                        null,
                        "국세청에서 사업자 상태를 확인할 수 없습니다."
                );
            }

            return new NtsBusinessVerifyVO(
                    false,
                    businessStatus,
                    businessStatus + " 상태의 사업자는 가입할 수 없습니다."
            );

        } catch (NtsResponseException e) {
            return new NtsBusinessVerifyVO(false, null, e.getMessage());

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("국세청 사업자 상태조회 실패", e);
            }
            return new NtsBusinessVerifyVO(
                    false,
                    null,
                    "국세청 사업자 상태조회 중 오류가 발생했습니다."
            );
        }
    }

    private Map<?, ?> requestFirstResult(
            String path,
            Map<String, Object> requestBody,
            String noResponseMessage,
            String noDataMessage,
            String invalidFormatMessage) {

        Map<?, ?> response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host(NTS_API_HOST)
                        .path(path)
                        .queryParam("serviceKey", serviceKey)
                        .build())
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new NtsResponseException(noResponseMessage);
        }

        Object dataObject = response.get("data");
        if (!(dataObject instanceof List<?> dataList) || dataList.isEmpty()) {
            throw new NtsResponseException(noDataMessage);
        }

        Object first = dataList.get(0);
        if (!(first instanceof Map<?, ?> result)) {
            throw new NtsResponseException(invalidFormatMessage);
        }

        return result;
    }

    private static final class NtsResponseException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private NtsResponseException(String message) {
            super(message);
        }
    }
}
