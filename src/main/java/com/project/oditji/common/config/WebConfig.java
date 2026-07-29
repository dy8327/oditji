package com.project.oditji.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.project.oditji.common.interceptor.AccessLogInterceptor;
import com.project.oditji.common.interceptor.AdminCheckInterceptor;
import com.project.oditji.common.interceptor.BusinessCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminCheckInterceptor adminCheckInterceptor;
    private final BusinessCheckInterceptor businessCheckInterceptor;
    private final AccessLogInterceptor accessLogInterceptor;

    public WebConfig(
            AdminCheckInterceptor adminCheckInterceptor,
            BusinessCheckInterceptor businessCheckInterceptor,
            AccessLogInterceptor accessLogInterceptor) {

        this.adminCheckInterceptor = adminCheckInterceptor;
        this.businessCheckInterceptor = businessCheckInterceptor;
        this.accessLogInterceptor = accessLogInterceptor;
    }

    // 관리자 페이지 접근 권한 체크 + 전체 접속 로그(ACCESS_LOG) 기록
  
@Override
        public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminCheckInterceptor)
                .addPathPatterns("/admin", "/admin/**", "/payment/list");

        registry.addInterceptor(businessCheckInterceptor)
                .addPathPatterns(
                        "/business",
                        "/business/**",
                        "/chat",
                        "/chat/**"
                );

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

                /*
                 * =========================================================
                 * 이벤트 이미지 외부 업로드 폴더 연결
                 *
                 * 실제 저장 경로:
                 * C:/oditji/uploads/event/
                 *
                 * 브라우저 접근 경로:
                 * /oditji/uploads/event/파일명
                 * =========================================================
                 */
                registry.addResourceHandler("/uploads/event/**")
                                .addResourceLocations("file:///C:/oditji/uploads/event/");
        }
}