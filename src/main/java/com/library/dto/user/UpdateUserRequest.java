package com.library.dto.user;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String username;
    private String password;
}
