package com.project.oditji.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * ODITJI Spring Security 공통 설정.
 *
 * 현재 프로젝트의 로그인/권한 판별은 기존 세션과 MVC Interceptor 구조를 유지한다.
 * Spring Security에서는 비밀번호 암호화와 CSRF 보호만 담당하며,
 * 기존 팀원 기능의 로그인 흐름을 바꾸지 않도록 모든 URL 접근 자체는 permitAll로 둔다.
 */
@Configuration
public class SecurityConfig {

    /**
     * 회원 비밀번호 암호화에 사용할 BCrypt PasswordEncoder 빈.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 필터 체인.
     *
     * CSRF 보호는 Spring Security 기본 설정을 그대로 사용한다.
     * POST 등 상태를 변경하는 요청에는 CSRF 토큰이 필요하며,
     * JSP 폼과 fetch 요청에는 common.js가 토큰을 자동으로 포함한다.
     *
     * 인증/인가 자체는 기존 LoginCheckInterceptor,
     * AdminCheckInterceptor, BusinessCheckInterceptor 구조를 유지한다.
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
