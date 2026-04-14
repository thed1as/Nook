package com.library.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "username is required")
    @Size(min = 3, max = 30, message = "The name must be between 3 and 50 characters")
    private String username;
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "The password must be more than 8 characters long.")
    private String password;
    @NotBlank(message = "email is required")
    @Email
    private String email;

}
