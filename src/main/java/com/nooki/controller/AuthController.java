package com.nooki.controller;

import com.nooki.dto.jwtDto.JwtResponse;
import com.nooki.dto.jwtDto.RefreshRequest;
import com.nooki.dto.user.LoginRequest;
import com.nooki.dto.user.UserRequest;
import com.nooki.entity.CustomUserDetails;
import com.nooki.entity.User;
import com.nooki.security.JwtService;
import com.nooki.security.RefreshTokenService;
import com.nooki.service.AuthService;
import com.nooki.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        UUID userId = user.getUser().getUserId();
        String role = user.getUser().getRole().toString();

        String accessToken = jwtService.generateAccessToken(userId, role);
        String refreshToken = jwtService.generateRefreshToken(userId);

        refreshTokenService.saveRefreshToken(userId, refreshToken);

        return ResponseEntity.ok(JwtResponse.create(accessToken, refreshToken, "Bearer"));
    }

    @PostMapping("/refresh")
    @Hidden
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshRequest request) {
        String clientRefreshToken = request.refreshToken();
        if(!refreshTokenService.isTokenValid(clientRefreshToken)) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        UUID userId = jwtService.extractUserIdFromRefreshToken(request.refreshToken());
        User user = userService.findById(userId);

        String newAccessToken = jwtService.generateAccessToken(userId, user.getRole().toString());
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        refreshTokenService.saveRefreshToken(userId, newRefreshToken);

        return ResponseEntity.ok(JwtResponse.create(newAccessToken, newRefreshToken, "Bearer"));
    }

    @PostMapping("/register")
    public ResponseEntity<JwtResponse> register(@Valid @RequestBody UserRequest userRequest) {
        User user = authService.createUser(userRequest);

        String newAccessToken = jwtService.generateAccessToken(user.getUserId(), user.getRole().toString());
        String newRefreshToken = jwtService.generateRefreshToken(user.getUserId());

        refreshTokenService.saveRefreshToken(user.getUserId(), newRefreshToken);

        return ResponseEntity.ok(JwtResponse.create(newAccessToken, newRefreshToken, "Bearer"));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        jwtService.blackListToken(authHeader);
        try {
            UUID userId = jwtService.extractUserIdFromRefreshToken(authHeader.substring(7));
            refreshTokenService.deleteById(userId);
        } catch (Exception ignored) {}
        return ResponseEntity.noContent().build();
    }

}
