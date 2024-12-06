package com.rgt.user;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	
	//TODO: 계정생성 관련 함수 만들어야함.
	public void create(String username, String password) {
		userRepository.save(null);
	}
}
