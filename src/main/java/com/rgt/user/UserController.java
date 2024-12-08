package com.rgt.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import com.rgt.security.TokenProvider;
import com.rgt.user.dto.CreateUserDTO;
import com.rgt.user.dto.LoginDTO;
import com.rgt.user.dto.ResponseUserDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "사용자 인증 관련 API")
public class UserController {
	private final UserService userService;
	private final TokenProvider tokenProvider;
	
    @Operation(summary = "사용자 가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody CreateUserDTO createUserDTO) {
        try {
            userService.create(createUserDTO.getUserName(), createUserDTO.getPassword(),false);
            return ResponseEntity.ok("User successfully created.");
        } catch (IllegalArgumentException e) {
            log.error("Failed to create user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during user signup", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to create user due to an unexpected error.");
        }
    }
    
    @Operation(summary = "사용자 로그인", description = "사용자를 인증하고 액세스 토큰을 생성합니다.")
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(HttpServletResponse response, @RequestBody LoginDTO loginDTO) {
        try {
            SiteUser user = userService.find(loginDTO.getUserName(), loginDTO.getPassword());
            String accessToken = tokenProvider.create(user);
            tokenProvider.generateAndSetAccessTokenCookie(accessToken, response);
            String refreshToken = tokenProvider.createRefreshToken(user);
            
            // 사용자 권한 설정
//            List<GrantedAuthority> authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole().getValue());
//            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUserName(), null, authorities);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // [디버깅용] 사용자 로그인 
            String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            log.info("User login {}", userId);
            
            return ResponseEntity.ok()
                .body(ResponseUserDTO.builder()
                    .userName(user.getUserName())
                    .role(user.getRole())
                    .build());
        } catch (Exception e) {
            log.error("Invalid credentials => " + e.getMessage());
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }

    
    @Operation(summary = "사용자 로그아웃", description = "사용자를 로그아웃하고 인증 정보를 제거합니다.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            // 요청에서 인증 정보 가져오기
            String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userId != null) {
                // SecurityContext 초기화
                SecurityContextHolder.clearContext();

                // 쿠키에서 액세스 토큰 제거
                tokenProvider.deleteAccessTokenFromCookie(request, response);

                // Redis에서 리프레시 토큰 제거
                SiteUser user = userService.find(Long.parseLong(userId));
                tokenProvider.deleteRefreshTokenFromRedis(user);

                return ResponseEntity.ok("Successfully logged out.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
            }
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Failed to log out due to an unexpected error.");
        }
    }
    
    @Operation(summary = "사용자 검증", description = "현재 사용자가 인가된 사용자인지 검증하고 Boolean타입으로 유무를 반환합니다.")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/authentication")
    public ResponseEntity<Boolean> isAuthentication(HttpServletRequest request) {
        // 인증된 사용자의 경우
        return ResponseEntity.ok().body(true);
    }
}
