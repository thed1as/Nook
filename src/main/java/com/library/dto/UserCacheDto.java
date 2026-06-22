package com.library.dto;

import com.library.entity.User;

import java.util.UUID;

public record UserCacheDto(UUID id,
                           String email,
                           String password,
                           String role) {

    public static UserCacheDto from(User user) {
        return new UserCacheDto(user.getUserId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().toString());
    }
}
