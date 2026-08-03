package com.project.oditji.business.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.project.oditji.business.vo.NtsBusinessVerifyVO;

@Service
public class NtsBusinessServiceImpl
        implements NtsBusinessService {

    private final RestClient restClient;
    private static final Logger log = LoggerFactory.getLogger(NtsBusinessServiceImpl.class);

    @Value("${nts.business.service-key}")
    private String serviceKey;

    public NtsBusinessServiceImpl() {
        this.restClient = RestClient.create();
    }

    @Override
    public NtsBusinessVerifyVO verifyBusiness(String businessNumber, String representativeName, String openDate) {

        // 필수값 검사
        if (businessNumber == null || businessNumber.isBlank()) {
            throw new IllegalArgumentException("사업자등록번호를 입력해주세요.");
        }

        if (representativeName == null || representativeName.isBlank()) {
            throw new IllegalArgumentException("대표자명을 입력해주세요.");
        }

        if (openDate == null || !openDate.matches("\\d{8}")) {
            throw new IllegalArgumentException("개업일은 YYYYMMDD 형식으로 입력해주세요.");
        }

        // 국세청 API는 하이픈 없는 사업자번호 사용
        String normalizedBusinessNumber = businessNumber.replaceAll("\\D", "");

        /*
         * 요청 본문에는 businesses 배열 안에
         * 사업자등록번호, 개업일, 대표자명을 담는다.
         */
        Map<String, Object> business = new HashMap<>();

        business.put("b_no", normalizedBusinessNumber);
        business.put("start_dt", openDate);
        business.put("p_nm", representativeName.trim());

        Map<String, Object> requestBody = new HashMap<>();

        requestBody.put("businesses", List.of(business));

        try {
            Map<?, ?> response = restClient.post()
                            .uri(uriBuilder ->
                                    uriBuilder
                                            .scheme("https")
                                            .host("api.odcloud.kr")
                                            .path("/api/nts-businessman/v1/validate")
                                            .queryParam("serviceKey", serviceKey)
                                            .build())
                            .body(requestBody)
                            .retrieve()
                            .body(Map.class);

            if (response == null) {

                return new NtsBusinessVerifyVO(false, null, "국세청 응답이 없습니다.");
            }

            Object dataObject = response.get("data");

            if (!(dataObject instanceof List<?> dataList)
                    || dataList.isEmpty()) {
                return new NtsBusinessVerifyVO(false, null, "국세청 진위확인 결과가 없습니다.");
            }

            Object first = dataList.get(0);

            if (!(first instanceof Map<?, ?> result)) {
                return new NtsBusinessVerifyVO(false, null, "국세청 응답 형식이 올바르지 않습니다.");
            }

            /*
             * valid 값
             * "01" → 진위확인 성공
             * 그 외 → 불일치
             */
            String valid = String.valueOf(result.get("valid"));

            if ("01".equals(valid)) {
                return checkBusinessStatus(normalizedBusinessNumber);
            }

            return new NtsBusinessVerifyVO(false, null, "사업자등록번호, 대표자명 또는 개업일이 "
                    + "국세청 등록정보와 일치하지 않습니다.");

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("국세청 사업자 진위 확인 실패", e);
            }
            return new NtsBusinessVerifyVO(false, null, "국세청 사업자 확인 중 오류가 발생했습니다.");
        }
    }

     /* ======================
        국세청 사업자 상태조회
        계속사업자만 가입 가능
     ========================*/
    private NtsBusinessVerifyVO checkBusinessStatus(String businessNumber) {

        Map<String, Object> requestBody = new HashMap<>();

        // 상태조회 요청 본문에는 b_no 배열로 사업자등록번호를 전달한다.
        requestBody.put("b_no", List.of(businessNumber));

        try {

            Map<?, ?> response = restClient.post()
                    .uri(uriBuilder ->uriBuilder
                                    .scheme("https")
                                    .host("api.odcloud.kr")
                                    .path("/api/nts-businessman/v1/status")
                                    .queryParam("serviceKey", serviceKey)
                                    .build())
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return new NtsBusinessVerifyVO(false, null, "사업자 상태조회 응답이 없습니다.");
            }

            Object dataObject = response.get("data");

            if (!(dataObject instanceof List<?> dataList) || dataList.isEmpty()) {
                return new NtsBusinessVerifyVO(false, null, "사업자 상태조회 결과가 없습니다.");
            }

            Object first = dataList.get(0);

            if (!(first instanceof Map<?, ?> result)) {
                return new NtsBusinessVerifyVO(false, null, "사업자 상태조회 응답 형식이 올바르지 않습니다.");
            }

            /*
             * b_stt 값 예시
             * - 계속사업자
             * - 휴업자
             * - 폐업자
             */
            Object statusObject = result.get("b_stt");

            String businessStatus = statusObject == null ? "" : statusObject.toString().trim();

            // 계속사업자만 가입 허용
            if ("계속사업자".equals(businessStatus)) {
                return new NtsBusinessVerifyVO(true, businessStatus, "사업자 정보가 확인되었습니다. (계속사업자)");
            }

            // 상태값을 확인할 수 없는 경우
            if (businessStatus.isBlank()) {
                return new NtsBusinessVerifyVO(false, null, "국세청에서 사업자 상태를 확인할 수 없습니다.");
            }
            // 휴업자 / 폐업자 등
            return new NtsBusinessVerifyVO(false, businessStatus, businessStatus + " 상태의 사업자는 가입할 수 없습니다.");

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("국세청 사업자 상태조회 실패", e);
            }
            return new NtsBusinessVerifyVO(false, null, "국세청 사업자 상태조회 중 오류가 발생했습니다.");
        }
    }
}
