package com.project.oditji.common.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Answers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 기존 SecurityConfigCoverageTest에서 실행되지 않았던 headers 내부 설정 람다를 직접 검증합니다.
 */
class SecurityConfigAdditionalCoverageTest {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void headerCustomizerShouldApplySameOriginFrameOption() {
        SecurityConfig config = new SecurityConfig();

        HttpSecurity http =
                mock(
                        HttpSecurity.class,
                        Answers.RETURNS_SELF);

        DefaultSecurityFilterChain expectedChain =
                mock(DefaultSecurityFilterChain.class);

        when(http.build())
                .thenReturn(expectedChain);

        SecurityFilterChain actual =
                config.securityFilterChain(http);

        assertSame(expectedChain, actual);

        ArgumentCaptor<Customizer> headersCaptor =
                ArgumentCaptor.forClass(Customizer.class);

        verify(http).headers(headersCaptor.capture());

        HeadersConfigurer headersConfigurer =
                mock(HeadersConfigurer.class);

        headersCaptor.getValue()
                .customize(headersConfigurer);

        ArgumentCaptor<Customizer> frameCaptor =
                ArgumentCaptor.forClass(Customizer.class);

        verify(headersConfigurer)
                .frameOptions(frameCaptor.capture());

        HeadersConfigurer.FrameOptionsConfig frameOptions =
                mock(HeadersConfigurer.FrameOptionsConfig.class);

        frameCaptor.getValue()
                .customize(frameOptions);

        verify(frameOptions).sameOrigin();

        verify(http).csrf(any());
        verify(http).authorizeHttpRequests(any());
        verify(http).formLogin(any());
        verify(http).httpBasic(any());
    }
}
