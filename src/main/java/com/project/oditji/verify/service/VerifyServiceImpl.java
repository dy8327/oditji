package com.project.oditji.verify.service;

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
import tools.jackson.databind.JsonNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
public class VerifyServiceImpl implements VerifyService {

    private static final String PORTONE_BASE_URL = "https://api.portone.io";
    private static final String VERIFIED_STATUS = "VERIFIED";
    private static final String FAILED_STATUS = "FAILED";

    private static final String ADULT_Y = "Y";
    private static final String ADULT_N = "N";
    private static final String DEFAULT_RETURN_URL = "/";
    private static final DateTimeFormatter VERIFY_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(VerifyServiceImpl.class);

    private final VerifyDAO verifyDAO;
    private final RestClient restClient;
    private final Clock clock;

    private final String storeId;
    private final String easyChannelKey;
    private final String smsChannelKey;

    public VerifyServiceImpl(
            VerifyDAO verifyDAO,
            @Value("${portone.store-id}")
            String storeId,
            @Value("${portone.identity1.channel-key}")
            String easyChannelKey,
            @Value("${portone.identity2.channel-key}")
            String smsChannelKey,
            @Value("${portone.api-secret}")
            String portoneApiSecret
    ) {
        this.verifyDAO = verifyDAO;
        this.storeId = storeId;
        this.easyChannelKey = easyChannelKey;
        this.smsChannelKey = smsChannelKey;
        this.clock = Clock.systemDefaultZone();

        this.restClient = RestClient.builder()
                .baseUrl(PORTONE_BASE_URL)
                .defaultHeader( HttpHeaders.AUTHORIZATION, "PortOne " + portoneApiSecret)
                .build();
    }

    /**
     * 성인인증 준비 단계
     */
    @Override
    public AdultVerifyReadyVO prepareVerification(long memberNo) {
        String verifyId = makeVerifyId(memberNo);

        return new AdultVerifyReadyVO(storeId, easyChannelKey, smsChannelKey, verifyId);
    }

    /**
     * 성인인증 완료 단계
     *
     * 포트원 서버에서 실제 인증 결과를 조회한 뒤
     * 성인 여부를 확인하고 회원 정보를 갱신한다.
     */
    @Override
    @Transactional
    public AdultVerifyCompleteVO completeVerification(long memberNo,String verifyId, String returnUrl, HttpSession session) {
        String validationMessage = validateVerifyId(verifyId);
        if (validationMessage != null) {
            
                return AdultVerifyCompleteVO.fail(validationMessage);
        }

        if (verifyDAO.countVerifyId(verifyId) > 0) {
            
                return AdultVerifyCompleteVO.fail("이미 처리된 본인인증 요청입니다.");
        }

        try {
            JsonNode response = getIdentityVerification(verifyId);
            JsonNode identityVerification = extractIdentityVerification(response);

            String status = getString(identityVerification, "status");

            if (!VERIFIED_STATUS.equalsIgnoreCase(status)) {
                String failStatus = defaultString(status, "UNKNOWN");
                insertFailedLog(memberNo, verifyId, failStatus);

                return AdultVerifyCompleteVO.fail("본인인증이 완료되지 않았습니다. 현재 상태: " + failStatus);
            }

            JsonNode customer = extractVerifiedCustomer(identityVerification);

            String name = getString(customer, "name");
            String birthDate = extractBirthDate(customer);
            String phoneNumber = getString(customer, "phoneNumber");
            String gender = normalizeGender(getString(customer, "gender"));

            boolean adult = isAdult(birthDate);

            String adultYn = adult ? ADULT_Y : ADULT_N;

            IdentityVerifyLogVO successLog = createBaseLog(memberNo, verifyId, status);

            successLog.setVerifyStatus(VERIFIED_STATUS);
            successLog.setName(name);
            successLog.setBirthDate(birthDate);
            successLog.setPhoneNumber(phoneNumber);
            successLog.setGender(gender);
            successLog.setAdultYn(adultYn);

            verifyDAO.insertVerifyLog(successLog);

            if (!adult) {
                
                return AdultVerifyCompleteVO.fail("성인만 이용할 수 있는 콘텐츠입니다.");
            }

            verifyDAO.updateMemberAdultVerified(memberNo);
            session.setAttribute("ADULT_VERIFIED",ADULT_Y);
                
            return AdultVerifyCompleteVO.success(sanitizeReturnUrl(returnUrl));

        } catch (Exception e) {
                if (log.isErrorEnabled()) {
                        log.error("성인인증 결과 처리 실패", e);
                }
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return AdultVerifyCompleteVO.fail("성인인증 결과 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 회원의 성인인증 완료 여부 조회
     */
    @Override
    public boolean isAdultVerified(long memberNo) {
        String adultVerified = verifyDAO.selectMemberAdultVerified(memberNo);

        return ADULT_Y.equals(adultVerified);
    }

    /**
     * 포트원 본인인증 결과 조회 API 호출
     */
    private JsonNode getIdentityVerification(String identityVerificationId) {
        String encodedId = UriUtils.encodePathSegment(identityVerificationId, StandardCharsets.UTF_8);

        JsonNode response = restClient.get()
                .uri("/identity-verifications/{identityVerificationId}", encodedId)
                .retrieve()
                .body(JsonNode.class);

        if (response == null) {
            throw new IllegalStateException("포트원 본인인증 조회 결과가 없습니다.");
        }

        return response;
    }

    /**
     * 포트원 응답에서 identityVerification 객체 추출
     */
    private JsonNode extractIdentityVerification(JsonNode response) {
        JsonNode identityVerification = response.path("identityVerification");

        if (identityVerification.isMissingNode() || identityVerification.isNull()) {
            return response;
        }

        return identityVerification;
    }

    /**
     * 포트원 응답에서 인증 고객 정보 추출
     */
    private JsonNode extractVerifiedCustomer(JsonNode identityVerification) {
        JsonNode customer = identityVerification.path("verifiedCustomer");

        if (customer.isMissingNode() || customer.isNull()) {
            customer = identityVerification.path("customer");
        }

        return customer;
    }

    /**
     * 인증 요청 ID 기본 검증
     */
    private String validateVerifyId(String verifyId) {
        if (verifyId == null || verifyId.isBlank()) {
            
                return "본인인증 요청 ID가 없습니다.";
        }

        if (!verifyId.matches("^[A-Za-z0-9_-]{1,80}$")) {
            
                return "본인인증 요청 ID 형식이 올바르지 않습니다.";
        }

        return null;
    }

    /**
     * 기본 인증 로그 객체 생성
     */
    private IdentityVerifyLogVO createBaseLog(long memberNo, String verifyId, String rawStatus) {
        IdentityVerifyLogVO verifyLog = new IdentityVerifyLogVO();

        verifyLog.setMemberNo(memberNo);
        verifyLog.setVerifyId(verifyId);
        verifyLog.setRawStatus(rawStatus);
        verifyLog.setVerifyStatus(FAILED_STATUS);
        verifyLog.setAdultYn(ADULT_N);

        return verifyLog;
    }

    /**
     * 실패 인증 로그 저장
     */
    private void insertFailedLog(long memberNo, String verifyId, String rawStatus) {
        IdentityVerifyLogVO failLog = createBaseLog(memberNo, verifyId, rawStatus);

        verifyDAO.insertVerifyLog(failLog);
    }

    /**
     * 만 19세 이상 여부 확인
     */
    private boolean isAdult(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            return false;
        }

        try {
            LocalDate birth = LocalDate.parse(birthDate);
            LocalDate today = LocalDate.now(clock);

            return !birth.isAfter(today) && Period.between(birth, today).getYears() >= 19;
            
        } catch (DateTimeParseException e) {
            
                return false;
        }
    }

    /**
     * 포트원 생년월일 값을 yyyy-MM-dd 형식으로 변환
     */
    private String extractBirthDate(JsonNode customer) {
        if (customer == null || customer.isMissingNode() || customer.isNull()) {
            
                return null;
        }

        String birthDate =
                getString(customer, "birthDate");

        if (birthDate == null || birthDate.isBlank()) {
            
                return null;
        }

        String trimmedBirthDate = birthDate.trim();

        if (trimmedBirthDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            
                return trimmedBirthDate;
        }

        if (trimmedBirthDate.matches("\\d{8}")) {
            
                return trimmedBirthDate.substring(0, 4) + "-"  + trimmedBirthDate.substring(4, 6)
                    + "-" + trimmedBirthDate.substring(6, 8);
        }

        return trimmedBirthDate;
    }

    /**
     * 포트원 성별 값을 프로젝트 형식으로 변환
     */
    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
        
                return null;
        }

        String normalizedGender = gender.trim().toUpperCase(Locale.ROOT);

        if ("MALE".equals(normalizedGender) || "M".equals(normalizedGender)) {
            
                return "MALE";
        }

        if ("FEMALE".equals(normalizedGender) || "F".equals(normalizedGender)) {
            
                return "FEMALE";
        }

        return null;
    }

    /**
     * Jackson 3 JsonNode에서 문자열 값 추출
     */
    private String getString(JsonNode node, String fieldName
    ) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            
                return null;
        }

        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            
                return null;
        }

        String text = value.asString();
        if (text == null || text.isBlank()) {
            
                return null;
        }

        return text;
    }

    /**
     * 포트원 본인인증 요청 ID 생성
     */
    private String makeVerifyId(long memberNo) {
        String time = LocalDateTime.now(clock).format(VERIFY_ID_FORMATTER);
        int randomNumber = RANDOM.nextInt(900000) + 100000;

        return "oditji" + memberNo + time + randomNumber;
    }

    /**
     * 외부 사이트 리다이렉트를 막고 프로젝트 내부 경로만 허용한다.
     */
    private String sanitizeReturnUrl(String returnUrl) {
        if (returnUrl == null || returnUrl.isBlank()) {
            
                return DEFAULT_RETURN_URL;
        }

        String trimmedUrl =returnUrl.trim();
        if (!trimmedUrl.startsWith("/")) {
            
                return DEFAULT_RETURN_URL;
        }

        if (trimmedUrl.startsWith("//")) {
            
                return DEFAULT_RETURN_URL;
        }

        if (trimmedUrl.startsWith("/\\")) {
            
                return DEFAULT_RETURN_URL;
        }

        if (trimmedUrl.contains("\r") || trimmedUrl.contains("\n")) {
            
                return DEFAULT_RETURN_URL;
        }

        return trimmedUrl;
    }

    /**
     * 문자열이 비어 있으면 기본값 반환
     */
    private String defaultString(String value, String defaultValue
    ) {
        if (value == null || value.isBlank()) {
            
                return defaultValue;
        }

        return value;
    }
}