package com.rgt.security;

import java.util.*;
import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.rgt.constants.PublicApiEndpoints;
import com.rgt.user.SiteUser;
import com.rgt.user.UserRepository;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
    	log.info("Received request for URI: {}", request.getRequestURI());
    	
        // 토큰 검증을 스킵하는 API 리스트
    	if (Arrays.stream(PublicApiEndpoints.values())
    	        .map(PublicApiEndpoints::getValue)
    	        .anyMatch(endpoint -> endpoint.equals(request.getRequestURI()))) {
    	    filterChain.doFilter(request, response); 
    	    return;
    	}
        
        String token = parseBearerToken(request);
        
        if (token == null) {
            log.warn("No JWT token found in cookies.");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 응답
            return;
        }
        
        log.info("JWT token found: {}", token);
        
        handleTokenAuthentication(token, response);
        filterChain.doFilter(request, response);
    }

    private void handleTokenAuthentication(String token, HttpServletResponse response) throws IOException {
        try {
            String userId = tokenProvider.validateAndGetUserId(token);
            authenticateUser(userId);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void authenticateUser(String userId) {
        AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

//    private void handleExpiredToken(ExpiredJwtException e, HttpServletResponse response) throws IOException {
//        String expiredTokenUserId = e.getClaims().getSubject();
//        if (expiredTokenUserId != null) {
//            refreshAccessToken(expiredTokenUserId, response);
//        } else {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        }
//    }

//    private void refreshAccessToken(String userId, HttpServletResponse response) throws IOException {
//        SiteUser user = userRepository.findById(Long.parseLong(userId)).orElse(null);
//        if (user == null) {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("{\"error\": \"User not found\"}");
//            return;
//        }
//
//        // Redis에서 리프레시 토큰을 가져옴
//        boolean isRefreshTokenValid = tokenProvider.validateRefreshToken(user);
//        if (isRefreshTokenValid) {
//            String newAccessToken = tokenProvider.create(user);
//            tokenProvider.generateAndSetAccessTokenCookie(newAccessToken, response); // 쿠키로 설정
//        } else {
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//            response.getWriter().write("{\"error\": \"Invalid or expired refresh token\"}");
//        }
//    }

    private String parseBearerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        log.warn("No JWT token found in cookies.");
        return null; // JWT가 없을 경우 null 반환
    }
}