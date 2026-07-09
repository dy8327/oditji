package com.project.oditji.verify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.oditji.verify.dao.VerifyDAO;
import com.project.oditji.verify.vo.AdultVerifyCompleteVO;
import com.project.oditji.verify.vo.AdultVerifyReadyVO;
import com.project.oditji.verify.vo.IdentityVerifyLogVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Service
public class VerifyServiceImpl implements VerifyService {

    private final VerifyDAO verifyDAO;
    private final RestClient restClient;

    private final String storeId;
    private final String easyChannelKey;
    private final String smsChannelKey;
    private final String portoneApiSecret;

    private static final SecureRandom RANDOM = new SecureRandom();

    public VerifyServiceImpl(
            VerifyDAO verifyDAO,
            @Value("${portone.store-id}") String storeId,
            @Value("${portone.identity1.channel-key}") String easyChannelKey,
            @Value("${portone.identity2.channel-key}") String smsChannelKey,
            @Value("${portone.api-secret}") String portoneApiSecret
    ) {
        this.verifyDAO = verifyDAO;
        this.storeId = storeId;
        this.easyChannelKey = easyChannelKey;
        this.smsChannelKey = smsChannelKey;
        this.portoneApiSecret = portoneApiSecret;
        
        // RestClient를 빌더 방식으로 안전하게 초기화합니다.
        this.restClient = RestClient.builder()
                .baseUrl("https://api.portone.io")
                .build();
    }

    /**
     * 1. 성인인증 준비단계
     */
    @Override
    public AdultVerifyReadyVO prepareVerification(long memberNo) {
        String verifyId = makeVerifyId(String.valueOf(memberNo));
        return new AdultVerifyReadyVO(storeId, easyChannelKey, smsChannelKey, verifyId);
    }

    /**
     * 2. 성인인증 완료단계 (최종 검증 및 회원 권한 업데이트)
     */
    @Override
    @Transactional
    public AdultVerifyCompleteVO completeVerification(
            long memberNo,
            String verifyId,
            String returnUrl,
            HttpSession session
    ) {
        // 1) 기본 데이터 및 유효성 검증
        if (verifyId == null || verifyId.isBlank()) {
            return AdultVerifyCompleteVO.fail("본인인증 요청 ID가 없습니다.");
        }

        if (!verifyId.matches("^[A-Za-z0-9_-]{1,80}$")) {
            return AdultVerifyCompleteVO.fail("본인인증 요청 ID 형식이 올바르지 않습니다.");
        }

        if (verifyDAO.countVerifyId(verifyId) > 0) {
            return AdultVerifyCompleteVO.fail("이미 처리된 본인인증 요청입니다.");
        }

        try {
            // 2) 포트원 API 연동 호출 (RestClient 활용)
            JsonNode response = getIdentityVerification(verifyId);

            JsonNode identityVerification = response.path("identityVerification");
            if (identityVerification.isMissingNode() || identityVerification.isNull()) {
                identityVerification = response;
            }

            String status = getText(identityVerification, "status");

            // 3) 인증 상태 체크
            if (!"VERIFIED".equalsIgnoreCase(status)) {
                IdentityVerifyLogVO failLog = createBaseLog(memberNo, verifyId, status);
                verifyDAO.insertVerifyLog(failLog);
                return AdultVerifyCompleteVO.fail("본인인증이 완료되지 않았습니다. 현재 상태: " + status);
            }

            // 4) 고객 정보 데이터 파싱
            JsonNode customer = identityVerification.path("verifiedCustomer");
            if (customer.isMissingNode() || customer.isNull()) {
                customer = identityVerification.path("customer");
            }

            String name = getText(customer, "name");
            String birthDate = extractBirthDate(customer);
            String phoneNumber = getText(customer, "phoneNumber");
            String gender = normalizeGender(getText(customer, "gender"));

            // 5) 성인 여부 판별 (만 19세 이상)
            boolean isAdultUser = isAdult(birthDate);
            String adultYn = isAdultUser ? "Y" : "N";

            // 6) 결과 로그 설정 및 데이터베이스 적재
            IdentityVerifyLogVO logVO = createBaseLog(memberNo, verifyId, status);
            logVO.setVerifyStatus("VERIFIED");
            logVO.setName(name);
            logVO.setBirthDate(birthDate);
            logVO.setPhoneNumber(phoneNumber);
            logVO.setGender(gender);
            logVO.setAdultYn(adultYn);
            verifyDAO.insertVerifyLog(logVO);

            // 7) 만약 성인이 아니라면 실패 처리 후 탈출
            if (!isAdultUser) {
                return AdultVerifyCompleteVO.fail("성인만 이용할 수 있는 콘텐츠입니다.");
            }

            // 8) 성인 인증 완료 유저 데이터 갱신 및 세션 기록
            verifyDAO.updateMemberAdultVerified(memberNo);
            session.setAttribute("ADULT_VERIFIED", "Y");

            return AdultVerifyCompleteVO.success(sanitizeReturnUrl(returnUrl));

        } catch (Exception e) {
            // 서버 통신 혹은 파싱 에러 밎 오류 처리 로그 기록
            IdentityVerifyLogVO failLog = createBaseLog(memberNo, verifyId, "SERVER_ERROR");
            verifyDAO.insertVerifyLog(failLog);
            return AdultVerifyCompleteVO.fail("성인인증 결과 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 3. 성인인증 이력 조회 여부
     */
    @Override
    public boolean isAdultVerified(Long memberNo) {
        String adultVerified = verifyDAO.selectMemberAdultVerified(memberNo);
        return "Y".equals(adultVerified);
    }

    /* ==========================================
     * 내부 유틸리티 및 헬퍼 메서드
     * ========================================== */

    private JsonNode getIdentityVerification(String identityVerificationId) {
        String encodedId = UriUtils.encodePathSegment(identityVerificationId, StandardCharsets.UTF_8);

        return restClient.get()
                .uri("/identity-verifications/{identityVerificationId}", encodedId)
                .header(HttpHeaders.AUTHORIZATION, "PortOne " + portoneApiSecret)
                .retrieve()
                .body(JsonNode.class);
    }

    private IdentityVerifyLogVO createBaseLog(Long memberNo, String verifyId, String rawStatus) {
        IdentityVerifyLogVO log = new IdentityVerifyLogVO();
        log.setMemberNo(memberNo);
        log.setVerifyId(verifyId);
        log.setRawStatus(rawStatus);
        log.setVerifyStatus("FAILED");
        log.setAdultYn("N");
        return log;
    }

    private boolean isAdult(String birthDate) {
        if (birthDate == null || birthDate.trim().isEmpty()) {
            return false;
        }
        try {
            LocalDate birth = LocalDate.parse(birthDate);
            return Period.between(birth, LocalDate.now()).getYears() >= 19;
        } catch (Exception e) {
            return false;
        }
    }

    private String extractBirthDate(JsonNode customer) {
        if (customer == null || customer.isMissingNode() || customer.isNull()) {
            return null;
        }
        String direct = getText(customer, "birthDate");
        if (direct != null && direct.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return direct;
        }
        if (direct != null && direct.matches("\\d{8}")) {
            return direct.substring(0, 4) + "-" + direct.substring(4, 6) + "-" + direct.substring(6, 8);
        }
        return direct;
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        String upper = gender.toUpperCase();
        if ("MALE".equals(upper) || "M".equals(upper)) return "MALE";
        if ("FEMALE".equals(upper) || "F".equals(upper)) return "FEMALE";
        return null;
    }

    private String getText(JsonNode node, String fieldName) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        // 원래 코드의 .asString()은 오타 혹은 커스텀 모듈일 확률이 높으므로 
        // Jackson 표준 문법인 .asText()를 사용하여 안전하게 텍스트를 추출합니다.
        return value.asText(); 
    }

    private String makeVerifyId(String memberId) {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNumber = RANDOM.nextInt(900000) + 100000;
        return "oditji" + memberId + time + randomNumber;
    }

    private String sanitizeReturnUrl(String returnUrl) {
        if (returnUrl == null || returnUrl.isBlank() || 
            returnUrl.startsWith("http://") || returnUrl.startsWith("https://") || 
            !returnUrl.startsWith("/")) {
            return "/";
        }
        return returnUrl;
    }
}