package com.rgt.user;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.security.auth.message.AuthException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
    @Transactional
    public void create(String username, String password) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Username and password must not be empty.");
        }

        // 중복 사용자 확인
        if (userRepository.existsByUserName(username)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        SiteUser user = SiteUser.builder()
                .userName(username)
                .password(passwordEncoder.encode(password))
                .createUserDate(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }
    
    @Transactional
    public SiteUser find(String userName, String password) throws AuthException {
        SiteUser user = userRepository.findByUserName(userName);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("Invalid credentials");
        }
        return user;
    }
}
