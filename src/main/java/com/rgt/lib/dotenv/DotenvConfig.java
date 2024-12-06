package com.rgt.lib.dotenv;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DotenvConfig {
	@Bean
	Dotenv dotenv() {
		return Dotenv.load();
	}
	
	
}
