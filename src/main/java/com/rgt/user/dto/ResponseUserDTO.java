package com.rgt.user.dto;

import com.rgt.user.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUserDTO {
	String userName;
	UserRole role;
}
