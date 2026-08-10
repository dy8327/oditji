package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig의 formLogin/httpBasic disable 메서드 참조 내부를 직접 실행합니다.
 */
class SecurityConfigDisableCustomizerCoverageTest {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void loginAndHttpBasicCustomizersShouldInvokeDisable() {
        SecurityConfig config = new SecurityConfig();

        HttpSecurity http =
                mock(
                        HttpSecurity.class,
                        Answers.RETURNS_SELF);

        DefaultSecurityFilterChain expected =
                mock(DefaultSecurityFilterChain.class);

        when(http.build()).thenReturn(expected);

        SecurityFilterChain actual =
                config.securityFilterChain(http);

        assertSame(expected, actual);

        ArgumentCaptor<Customizer> formCaptor =
                ArgumentCaptor.forClass(Customizer.class);
        ArgumentCaptor<Customizer> basicCaptor =
                ArgumentCaptor.forClass(Customizer.class);

        verify(http).formLogin(formCaptor.capture());
        verify(http).httpBasic(basicCaptor.capture());

        FormLoginConfigurer<HttpSecurity> formLogin =
                mock(FormLoginConfigurer.class);

        HttpBasicConfigurer<HttpSecurity> httpBasic =
                mock(HttpBasicConfigurer.class);

        formCaptor.getValue().customize(formLogin);
        basicCaptor.getValue().customize(httpBasic);

        verify(formLogin).disable();
        verify(httpBasic).disable();
    }
}
