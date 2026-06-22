package com.library.dto.jwtDto;

public record JwtResponse(String accessToken, String refreshToken, String tokenType) {
    public static JwtResponse create(String accessToken, String refreshToken, String tokenType) {
        return new JwtResponse(accessToken, refreshToken, tokenType);
    }
}
