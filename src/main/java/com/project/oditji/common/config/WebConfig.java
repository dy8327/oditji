package com.project.oditji.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.project.oditji.common.interceptor.AccessLogInterceptor;
import com.project.oditji.common.interceptor.AdminCheckInterceptor;
import com.project.oditji.common.interceptor.BusinessCheckInterceptor;
import com.project.oditji.common.interceptor.LoginCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginCheckInterceptor loginCheckInterceptor;
    private final AdminCheckInterceptor adminCheckInterceptor;
    private final BusinessCheckInterceptor businessCheckInterceptor;
    private final AccessLogInterceptor accessLogInterceptor;

    public WebConfig(
            LoginCheckInterceptor loginCheckInterceptor,
            AdminCheckInterceptor adminCheckInterceptor,
            BusinessCheckInterceptor businessCheckInterceptor,
            AccessLogInterceptor accessLogInterceptor) {

        this.loginCheckInterceptor = loginCheckInterceptor;
        this.adminCheckInterceptor = adminCheckInterceptor;
        this.businessCheckInterceptor = businessCheckInterceptor;
        this.accessLogInterceptor = accessLogInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 일반회원 로그인 필수 화면
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/member/mypage",
                        "/member/update",
                        "/member/updateOtt",
                        "/member/withdraw",
                        "/cart",
                        "/favorite/list",
                        "/review/myReviewList",
                        "/order/list",
                        "/order/delivery",
                        "/order/complete/**",
                        "/verify/adult"
                );

        // 관리자 페이지 접근 권한 체크
        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns("/admin", "/admin/**", "/payment/list");

        // 사업자 페이지 접근 권한 체크
        registry.addInterceptor(businessCheckInterceptor)
                .addPathPatterns(
                        "/business",
                        "/business/**",
                        "/chat",
                        "/chat/**"
                );

        // 전체 접속 로그 기록
        registry.addInterceptor(accessLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico",
                        "/error"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:///C:/oditji/uploads/profile/");

        registry.addResourceHandler("/uploads/product/**")
                .addResourceLocations("file:///C:/oditji/uploads/product/");

        registry.addResourceHandler("/uploads/event/**")
                .addResourceLocations("file:///C:/oditji/uploads/event/");
    }
}