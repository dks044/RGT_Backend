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
import org.springframework.security.core.GrantedAuthority;
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

        String token = parseBearerToken(request);

        if (token == null) {
            log.warn("No JWT token found in cookies.");
            filterChain.doFilter(request, response); // 403 응답 대신 다음 필터로 진행
            return;
        }

        try {
            Map<String, Object> userInfo = tokenProvider.validateAndGetUserId(token); // userId와 roles을 가져옴
            String userId = (String) userInfo.get("userId");
            String roles = (String) userInfo.get("roles");
            authenticateUser(userId, roles); // 사용자 인증 설정
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response); // 다음 필터로 진행
    }

//    private void handleTokenAuthentication(String token, HttpServletResponse response) throws IOException {
//        try {
//            Map<String, Object> userInfo = tokenProvider.validateAndGetUserId(token);
//            String userId = (String) userInfo.get("userId");
//            String roles = (String) userInfo.get("roles");
//            authenticateUser(userId, roles);
//        } catch (ExpiredJwtException e) {
//            log.error("Expired JWT token: {}", e.getMessage());
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        } catch (JwtException e) {
//            log.error("Invalid JWT token: {}", e.getMessage());
//            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        }
//    }

    private void authenticateUser(String userId, String roles) {
        SiteUser user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        
        if (user != null) {
            log.info("User found: {}", user);
            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(roles);
            log.info("Authorities: {}", authorities);
            AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("User not found with ID: {}", userId);
        }
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