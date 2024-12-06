package com.rgt.constants;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConstants {

    private static String applicationName;
    private static String jwtSecret;
    private static String domain;
    
    @Value("${spring.application.name}")
    public void setApplicationName(String applicationName) {
        AppConstants.applicationName = applicationName;
    }

    @Value("${jwt.secret}")
    public void setJwtSecret(String jwtSecret) {
        AppConstants.jwtSecret = jwtSecret;
    }
    
    @Value("${server.reactive.session.cookie.domain}")
    public void setDomain(String domain) {
        AppConstants.domain = domain;
    }

    public static String getApplicationName() {
        return applicationName;
    }

    public static String getJwtSecret() {
        return jwtSecret;
    }
    public static String getDomain() {
        return domain;
    }
    
}
