package com.rgt.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rgt.security.TokenProvider;
import com.rgt.user.dto.CreateUserDTO;
import com.rgt.user.dto.LoginDTO;
import com.rgt.user.dto.ResponseUserDTO;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
	private final UserService userService;
	private final TokenProvider tokenProvider;
	

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody CreateUserDTO createUserDTO) {
        try {
            userService.create(createUserDTO.getUserName(), createUserDTO.getPassword());
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
    
    @PostMapping("/login")
    public ResponseEntity<?> authenticate(HttpServletResponse response,@RequestBody LoginDTO loginDTO){
    	try {
			SiteUser user = userService.find(loginDTO.getUserName(), loginDTO.getPassword());
			String accessToken = tokenProvider.create(user);
			tokenProvider.generateAndSetAccessTokenCookie(accessToken, response);
			String refreshToken = tokenProvider.createRefreshToken(user);
			return ResponseEntity.ok().body(
								ResponseUserDTO.builder()
									.userName(user.getUserName())
									.build()
								);
		} catch (Exception e) {
			log.error("Invalid credentials => "+e.getMessage());
			return ResponseEntity.badRequest().body("Invalid credentials");
		}
    }
    
}
