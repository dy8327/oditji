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
        }
}