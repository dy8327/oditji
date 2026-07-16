package com.project.oditji.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

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