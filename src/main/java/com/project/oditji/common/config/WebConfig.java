package com.project.oditji.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:///C:/oditji/upload/profile/");

        registry.addResourceHandler("/upload/product/**")
                .addResourceLocations("file:///C:/oditji/upload/product/");
    }
}