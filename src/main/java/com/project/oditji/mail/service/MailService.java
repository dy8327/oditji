package com.project.oditji.mail.service;

public interface MailService {

    void sendPasswordResetCode(String email, String authCode);
}