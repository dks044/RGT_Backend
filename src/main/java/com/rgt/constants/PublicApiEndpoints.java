package com.rgt.constants;

import lombok.Getter;

@Getter
public enum PublicApiEndpoints {
    API_LOGIN("/api/auth/login"),
    API_SIGNUP("/api/auth/signup"),
    API_BOOKS("/api/books"),
    API_BOOK_DETAIL("/api/books/{id}");

    private final String value;

	PublicApiEndpoints(String value) {
        this.value = value;
    }

    public static String[] getAllValues() {
        return java.util.Arrays.stream(values())
                               .map(PublicApiEndpoints::getValue)
                               .toArray(String[]::new);
    }
}
