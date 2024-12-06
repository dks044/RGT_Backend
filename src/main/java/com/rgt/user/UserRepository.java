package com.rgt.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<SiteUser, Long>{
	Boolean existsByUserName(String userName);
	SiteUser findByUserName(String userName);
}
