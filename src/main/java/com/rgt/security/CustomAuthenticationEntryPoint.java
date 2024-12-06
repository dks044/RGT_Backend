package com.rgt.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	//잘못된 인증 정보로 인한 접근 시 401 오류를 반환하도록 커스터마이징
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		
		log.error("Unauthorized error: {}", authException.getMessage());
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");

	}

}
