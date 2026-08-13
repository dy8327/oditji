package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 비밀번호 암호화 빈과 공통 보안 필터 체인 구성을 검증합니다.
 */
class SecurityConfigCoverageTest {

    @Test
    void passwordEncoderShouldCreateWorkingBcryptEncoder() {
        SecurityConfig config = new SecurityConfig();

        PasswordEncoder encoder = config.passwordEncoder();
        String encoded = encoder.encode("oditji-password");

        assertTrue(encoder instanceof BCryptPasswordEncoder);
        assertNotEquals("oditji-password", encoded);
        assertTrue(encoder.matches("oditji-password", encoded));
    }

    @Test
    void securityFilterChainShouldConfigureAndBuildHttpSecurity() {
        SecurityConfig config = new SecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        DefaultSecurityFilterChain expectedChain = mock(DefaultSecurityFilterChain.class);

        when(http.build()).thenReturn(expectedChain);

        SecurityFilterChain actualChain = config.securityFilterChain(http);

        assertSame(expectedChain, actualChain);
        verify(http).csrf(any());
        verify(http).authorizeHttpRequests(any());
        verify(http).formLogin(any());
        verify(http).httpBasic(any());
        verify(http).build();
    }
}
