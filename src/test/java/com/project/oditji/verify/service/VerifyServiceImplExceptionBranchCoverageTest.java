package com.project.oditji.verify.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.client.RestClient;

import com.project.oditji.verify.dao.VerifyDAO;
import com.project.oditji.verify.vo.AdultVerifyCompleteVO;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import jakarta.servlet.http.HttpSession;

/** PortOne 조회 예외 catch와 로깅 가드의 true/false 분기를 검증합니다. */
class VerifyServiceImplExceptionBranchCoverageTest {

    @Test
    void portOneFailureShouldRollbackAndReturnSafeMessage() {
        VerifyDAO verifyDAO = mock(VerifyDAO.class);
        VerifyServiceImpl service = createService(verifyDAO);
        when(verifyDAO.countVerifyId("valid-id")).thenReturn(0);

        TransactionStatus transactionStatus = mock(TransactionStatus.class);

        try (MockedStatic<TransactionAspectSupport> transactionAspect =
                     mockStatic(TransactionAspectSupport.class)) {

            transactionAspect.when(TransactionAspectSupport::currentTransactionStatus)
                    .thenReturn(transactionStatus);

            AdultVerifyCompleteVO result = service.completeVerification(
                    10L,
                    "valid-id",
                    "/content/1",
                    mock(HttpSession.class));

            assertFalse(result.isSuccess());
            assertEquals(
                    "성인인증 결과 조회 중 오류가 발생했습니다.",
                    result.getMessage());
            verify(transactionStatus).setRollbackOnly();
        }
    }

    @Test
    void portOneFailureShouldAlsoWorkWhenErrorLoggingIsDisabled() {
        VerifyDAO verifyDAO = mock(VerifyDAO.class);
        VerifyServiceImpl service = createService(verifyDAO);
        when(verifyDAO.countVerifyId("valid-id-off")).thenReturn(0);

        Logger logger = (Logger) LoggerFactory.getLogger(VerifyServiceImpl.class);
        Level originalLevel = logger.getLevel();
        TransactionStatus transactionStatus = mock(TransactionStatus.class);

        try (MockedStatic<TransactionAspectSupport> transactionAspect =
                     mockStatic(TransactionAspectSupport.class)) {

            transactionAspect.when(TransactionAspectSupport::currentTransactionStatus)
                    .thenReturn(transactionStatus);
            logger.setLevel(Level.OFF);

            AdultVerifyCompleteVO result = service.completeVerification(
                    11L,
                    "valid-id-off",
                    null,
                    mock(HttpSession.class));

            assertFalse(result.isSuccess());
            verify(transactionStatus).setRollbackOnly();
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    private VerifyServiceImpl createService(VerifyDAO verifyDAO) {
        VerifyServiceImpl service = new VerifyServiceImpl(
                verifyDAO,
                "store",
                "easy",
                "sms",
                "secret");

        RestClient restClient = mock(RestClient.class);
        when(restClient.get()).thenThrow(new IllegalStateException("network failure"));
        ReflectionTestUtils.setField(service, "restClient", restClient);
        return service;
    }
}
