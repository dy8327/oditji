package com.project.oditji.mail.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;
     private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

    public MailServiceImpl(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String fromEmail) {

        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendPasswordResetCode(String email, String authCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("[ODITJI] 비밀번호 재설정 인증번호");
        message.setText(
                "ODITJI 비밀번호 재설정 인증번호입니다.\n\n"
                + "인증번호: " + authCode + "\n\n"
                + "인증번호는 5분 동안 유효합니다.\n"
                + "본인이 요청하지 않았다면 이 메일을 무시해 주세요.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("비밀번호 재설정 인증 메일 발송 실패: email={}", email, e);
            throw new IllegalStateException("인증 메일 발송에 실패했습니다.", e);
        }
    }
}