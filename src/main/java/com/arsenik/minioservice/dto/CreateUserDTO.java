package com.arsenik.minioservice.dto;

import org.springframework.lang.NonNull;

public class CreateUserDTO {
    private final String userName;
    private final String password;
    private final String email;
    private final String firstName;
    private final String lastName;

    public CreateUserDTO(@NonNull String userName,
                         @NonNull String password,
                         @NonNull String email,
                         String firstName,
                         String lastName) {
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
