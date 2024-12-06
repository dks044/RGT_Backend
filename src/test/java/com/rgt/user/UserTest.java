package com.rgt.user;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;

@SpringBootTest
public class UserTest {
	@Autowired
	UserService userService;
	@Autowired
	UserRepository userRepository;
	
	@Test
	@Transactional
	@Rollback(false)
	void 유저생성테스트() {
		userService.create("admin", "rgtsubject");
		SiteUser foundUser =  userRepository.findByUserName("admin");
		assertNotNull(foundUser);
	}
	
}
