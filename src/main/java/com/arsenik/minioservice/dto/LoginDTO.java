package com.arsenik.minioservice.dto;

import org.springframework.lang.NonNull;

public class LoginDTO {
    private final String userName;
    private final String password;
    private final String email;

    public LoginDTO(String userName, @NonNull String password, String email) {
        this.userName = userName;
        this.password = password;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
