package com.project.oditji.mail.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/** 비밀번호 재설정 메일의 발신 정보와 발송 실패 예외 변환을 검증합니다. */
@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private MailServiceImpl mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailServiceImpl(
                mailSender,
                "admin@oditji.test");
    }

    @Test
    void passwordResetMailShouldContainRecipientCodeAndExpiryGuide() {
        mailService.sendPasswordResetCode(
                "member@test.com",
                "123456");

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("admin@oditji.test", message.getFrom());
        assertEquals("member@test.com", message.getTo()[0]);
        assertEquals(
                "[ODITJI] 비밀번호 재설정 인증번호",
                message.getSubject());
        assertTrue(message.getText().contains("123456"));
        assertTrue(message.getText().contains("5분"));
    }

    @Test
    void mailFailureShouldBeConvertedToIllegalStateException() {
        MailSendException cause = new MailSendException("failed");
        doThrow(cause).when(mailSender)
                .send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> mailService.sendPasswordResetCode(
                        "member@test.com",
                        "123456"));

        assertEquals("인증 메일 발송에 실패했습니다.",
                exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
