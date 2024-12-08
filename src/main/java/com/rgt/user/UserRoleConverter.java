package com.rgt.user;


import jakarta.persistence.AttributeConverter;

public class UserRoleConverter implements AttributeConverter<UserRole, Integer> {
	@Override
	public Integer convertToDatabaseColumn(UserRole attribute) {
		if(attribute == null) {
			return null;
		}
		
		return attribute.getKey();
	}
	
	@Override
	public UserRole convertToEntityAttribute(Integer dbData) {
		if(dbData == null) {
			return null;
		}
		for(UserRole role : UserRole.values()) {
			if(role.getKey() == dbData) {
				return role;
			}
		}
		throw new IllegalArgumentException("Invalid database value for UserRole: " + dbData);
	}

}
