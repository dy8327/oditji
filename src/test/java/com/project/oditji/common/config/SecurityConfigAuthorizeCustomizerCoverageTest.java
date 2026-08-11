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
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig의 authorizeHttpRequests 내부 anyRequest().permitAll() 람다를 직접 실행합니다.
 */
class SecurityConfigAuthorizeCustomizerCoverageTest {

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void authorizeCustomizerShouldExecutePermitAllLambda() throws Exception {
        SecurityConfig config = new SecurityConfig();
        HttpSecurity http = mock(HttpSecurity.class, Answers.RETURNS_SELF);
        DefaultSecurityFilterChain expected = mock(DefaultSecurityFilterChain.class);
        when(http.build()).thenReturn(expected);

        SecurityFilterChain actual = config.securityFilterChain(http);
        assertSame(expected, actual);

        ArgumentCaptor<Customizer> authorizeCaptor = ArgumentCaptor.forClass(Customizer.class);
        verify(http).authorizeHttpRequests(authorizeCaptor.capture());

        Class<?> registryType = Class.forName(
                "org.springframework.security.config.annotation.web.configurers."
                        + "AuthorizeHttpRequestsConfigurer$AuthorizationManagerRequestMatcherRegistry");

        Object registry = mock((Class) registryType, Answers.RETURNS_DEEP_STUBS);
        authorizeCaptor.getValue().customize(registry);
    }
}
