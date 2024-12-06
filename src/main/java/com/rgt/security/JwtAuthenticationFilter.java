package com.rgt.security;

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
        String token = parseBearerToken(request);

        if (token != null) {
            handleTokenAuthentication(token, response);
        }

        filterChain.doFilter(request, response);
    }

    private void handleTokenAuthentication(String token, HttpServletResponse response) throws IOException {
        try {
            String userId = tokenProvider.validateAndGetUserId(token);
            authenticateUser(userId);
        } catch (ExpiredJwtException e) {
            handleExpiredToken(e, response);
        }
    }

    private void authenticateUser(String userId) {
        AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleExpiredToken(ExpiredJwtException e, HttpServletResponse response) throws IOException {
        String expiredTokenUserId = e.getClaims().getSubject();
        if (expiredTokenUserId != null) {
            refreshAccessToken(expiredTokenUserId, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void refreshAccessToken(String userId, HttpServletResponse response) throws IOException {
        SiteUser user = userRepository.findById(Long.parseLong(userId)).orElse(null);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"User not found\"}");
            return;
        }

        // Redis에서 리프레시 토큰을 가져옴
        boolean isRefreshTokenValid = tokenProvider.validateRefreshToken(user);
        if (isRefreshTokenValid) {
            String newAccessToken = tokenProvider.create(user);
            tokenProvider.generateAndSetAccessTokenCookie(newAccessToken, response); // 쿠키로 설정
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Invalid or expired refresh token\"}");
        }
    }

    private String parseBearerToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new JwtException("Access token not found in cookies");
    }
}