package com.rgt.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.springframework.boot.web.server.Cookie;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.rgt.constants.AppConstants;
import com.rgt.user.SiteUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
//TODO: 배포환경에 맞춰서 도메인 등 변경 필요
//TODO: secure(true)는 HTTPS 연결에서만 쿠키를 전송하도록 설정
//TODO: sameSite(Cookie.SameSite.NONE.attributeValue())는 쿠키가 모든 크로스사이트 요청에 대해 전송되도록 설정
//이 경우, Secure 속성과 함께 사용해야 하므로 HTTPS 환경에서만 사용해야함!
public class TokenProvider {
	
	private final StringRedisTemplate stringRedisTemplate;
	
	//액세스 토큰 생성
	public String create(SiteUser user) {
		//기한 설정 (현재 1일)
		Date expiryDate = Date.from(Instant.now()
											.plus(1,ChronoUnit.DAYS));
		SecretKey secretKey = Keys.hmacShaKeyFor(AppConstants.getJwtSecret().getBytes());
		
		String token = Jwts.builder()
					.setSubject(String.valueOf(user.getId()))
					.setIssuer(AppConstants.getApplicationName())
					.setIssuedAt(new Date())
					.setExpiration(expiryDate)
					.claim("roles", user.getRole().getValue())
					.signWith(secretKey,SignatureAlgorithm.HS512)
					.compact();
		log.info("user role {} ",user.getRole().getValue());
		return token;
	}
	//검증된 사용자의 userId을 조회
	public Map<String, Object> validateAndGetUserId(String token) {
	    Claims claims = Jwts.parserBuilder()
	            .setSigningKey(Keys.hmacShaKeyFor(AppConstants.getJwtSecret().getBytes()))
	            .build()
	            .parseClaimsJws(token)
	            .getBody();
	    
	    String roles = claims.get("roles", String.class);
	    log.info("User roles: {}", roles);
	    
	    Map<String, Object> userInfo = new HashMap<>();
	    userInfo.put("userId", claims.getSubject());
	    userInfo.put("roles", roles);
	    return userInfo;
	}
	
	//리프래쉬 토큰 생성 (+Redis 할당)
	public String createRefreshToken(final SiteUser user) {
	    Date expiryDate = Date.from(Instant.now().plus(7, ChronoUnit.DAYS)); //7일 후 만료
	    SecretKey secretKey = Keys.hmacShaKeyFor(AppConstants.getJwtSecret().getBytes());
	    
	    String refreshToken = Jwts.builder()
	    		.setSubject(String.valueOf(user.getId()))
				.setIssuer(AppConstants.getApplicationName())
				.setIssuedAt(new Date())
				.setExpiration(expiryDate)
				.claim("token_type", "refresh")
				.claim("roles", user.getRole().getValue())
				.signWith(secretKey,SignatureAlgorithm.HS512)
				.compact();

	    stringRedisTemplate.opsForValue().set(String.valueOf(user.getId()),refreshToken,60* 24 * 7,TimeUnit.MINUTES);
	    return refreshToken;
	}
	
	//리프래쉬 토큰 검증
	public boolean validateRefreshToken(final SiteUser user) {
	    try {
	        String token = stringRedisTemplate.opsForValue().get(user.getId());
	        if (token == null) {
	            throw new JwtException("Refresh token not found for user: " + user.getId());
	        }
	    	
	        Claims claims = Jwts.parserBuilder()
	                .setSigningKey(AppConstants.getJwtSecret())
	                .build()
	                .parseClaimsJws(token)
	                .getBody();

	        // 리프레시 토큰임을 나타내는 'token_type' 클레임 검증
	        String tokenType = claims.get("token_type", String.class);
	        if (!"refresh".equals(tokenType)) {
	            return false;
	        }

	        // 유효기간 검증
	        Date expiration = claims.getExpiration();
	        return !expiration.before(new Date()); // 현재 날짜와 시간이 만료 날짜와 시간 이전이면 false, 그렇지 않으면 true
	    } catch (JwtException | IllegalArgumentException e) {
	        log.error("Invalid & Expired JWT RefreshToken", e);
	        return false;
	    }
	}
	
	
    // 액세스 토큰을 쿠키로 발급하고 클라이언트에 전송
    public void generateAndSetAccessTokenCookie(String token, HttpServletResponse response) {
        ResponseCookie responseCookie = ResponseCookie.from("access", token)
                //.domain(AppConstants.getDomain())
        		.maxAge(1 * 24 * 60 * 60)
                .path("/")
                .httpOnly(true)
                .secure(false)
                //.sameSite(Cookie.SameSite.NONE.attributeValue())
//                .sameSite(Cookie.SameSite.LAX.attributeValue())
                .sameSite("None")
                .build();
        
        response.setHeader("Set-Cookie", responseCookie.toString());
    }
	
    //쿠키에 포함된 액세스 토큰을 제거
    public void deleteAccessTokenFromCookie(HttpServletRequest request, HttpServletResponse response) {
		ResponseCookie deleteCookie = ResponseCookie.from("access", "")
		        //.domain(AppConstants.getDomain())
		        .path("/")
		        .httpOnly(true)
		        .secure(false)
		        //.sameSite(Cookie.SameSite.NONE.attributeValue())
		        .sameSite(Cookie.SameSite.LAX.attributeValue())
		        .maxAge(0)
		        .build();
		response.addHeader("Set-Cookie", deleteCookie.toString());
		log.info("User's access token removed successfully");
    }
    
    // 레디스에 저장된 리프래쉬 토큰을 제거
    public void deleteRefreshTokenFromRedis(final SiteUser user) {
        boolean isDeleted = stringRedisTemplate.delete(String.valueOf(user.getId()));
        if (isDeleted) {
            log.info("User {}'s refresh token removed successfully", user.getId());
        } else {
            log.warn("Failed to remove refresh token for user {}", user.getId());
        }
    }
    

}
