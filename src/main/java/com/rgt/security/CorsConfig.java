package com.rgt.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.rgt.constants.AppConstants;

@Configuration
public class CorsConfig {
	@Bean(name = "customCorsConfigurationSource")
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true); // 다른 출처의 자원을 사용할 수 있게 
        // CloudFront 도메인과 로컬 개발 환경 동시 허용

        //TODO: 프론트 배포후 도메인 추가 필요
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // 로컬 개발 환경
                "https://www.rgt-subject.kro.kr" // 배포 환경 도메인
            ));

        
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        
        config.setAllowedHeaders(List.of("Authorization","Content-Type", "Accept"));
        //config.se	tExposedHeaders(List.of("x-auth-token"));
        
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return  source;
    }
}
