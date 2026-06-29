package com.nooki.dto;

import com.nooki.entity.User;

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
