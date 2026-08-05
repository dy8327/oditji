package com.project.oditji.common.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

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
    private final String profileUploadPath;
    private final String productUploadPath;
    private final String eventUploadPath;

public WebConfig(
        LoginCheckInterceptor loginCheckInterceptor,
        AdminCheckInterceptor adminCheckInterceptor,
        BusinessCheckInterceptor businessCheckInterceptor,
        AccessLogInterceptor accessLogInterceptor,
        @Value("${oditji.upload.profile-path}") String profileUploadPath,
        @Value("${oditji.upload.product-path}") String productUploadPath,
        @Value("${oditji.upload.event-path}") String eventUploadPath) {

    this.loginCheckInterceptor = loginCheckInterceptor;
    this.adminCheckInterceptor = adminCheckInterceptor;
    this.businessCheckInterceptor = businessCheckInterceptor;
    this.accessLogInterceptor = accessLogInterceptor;
    this.profileUploadPath = profileUploadPath;
    this.productUploadPath = productUploadPath;
    this.eventUploadPath = eventUploadPath;
}

@Bean
public InternalResourceViewResolver jspViewResolver() {
    InternalResourceViewResolver resolver =
            new InternalResourceViewResolver();

    resolver.setPrefix("/WEB-INF/views/");
    resolver.setSuffix(".jsp");

    return resolver;
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
                        "/error",
                        "/error/**"
                );
    }
    @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations(toResourceLocation(profileUploadPath));

        registry.addResourceHandler("/uploads/product/**")
                .addResourceLocations(toResourceLocation(productUploadPath));

        registry.addResourceHandler("/uploads/event/**")
                .addResourceLocations(toResourceLocation(eventUploadPath));
        }

        private String toResourceLocation(String path) {
        String location = Paths.get(path).toAbsolutePath().normalize().toUri().toString();
        return location.endsWith("/") ? location : location + "/";
        }
    
}