package com.rgt.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.rgt.user.UserRepository;

import lombok.RequiredArgsConstructor;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	
    @Autowired
    @Qualifier("customCorsConfigurationSource")
    private CorsConfigurationSource configurationSource;
    
    //INFO: 모든경로에 인증이 필요없게 설정 후, 컨트롤러 메소드에만 추가적으로 권한 라우트 설정함
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화

            .cors(cors -> cors.configurationSource(configurationSource))
            .httpBasic(HttpBasicConfigurer::disable)
            .sessionManagement(sessionManagement -> 
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizeHttpRequests -> 
                authorizeHttpRequests
                    .requestMatchers("/", 
//                                     "/api/auth/login", 
//                                     "/api/auth/signup",
//                                     "/api/books",
//                                     "/swagger-ui/**", 
//                                     "/v3/api-docs/**", 
//                                     "/swagger-ui.html", 
//                                     "/webjars/**", 
//                                     "/api-docs/swagger-config",
//                                     "/api-docs/**"
                    				 "/**"
                    				)
                    .permitAll() // 인증이 필요 없는 경로
                    .anyRequest()
                    .authenticated()); // 나머지 요청은 인증 필요

        
        http.addFilterAfter(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(handling -> handling.authenticationEntryPoint(customAuthenticationEntryPoint));

        return http.build();
    }
    
    
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
       
}