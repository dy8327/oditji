package com.project.oditji;

import org.mybatis.spring.annotation.MapperScan; // <-- 추가 필요
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching // <-- 추가 필요
@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.project.oditji") // 스캔 범위를 패키지 전체로 지정
@MapperScan("com.project.oditji.**.dao") // 스킨 범위를 패키지 전체가 아닌 .dao 폴더 내부로 한정
public class OditjiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OditjiApplication.class, args);
    }

}