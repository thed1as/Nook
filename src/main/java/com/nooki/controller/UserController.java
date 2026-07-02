package com.nooki.controller;

import com.nooki.dto.user.UserResponse;
import com.nooki.security.JwtService;
import com.nooki.security.RefreshTokenService;
import com.nooki.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User", description = "User API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Find user by id")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        UserResponse ur = userService.getUserById(id);
        return ResponseEntity.ok(ur);
    }

    @Operation(summary = "delete my account")
    @PostMapping("/user/my/delete")
    @PreAuthorize("hasRole('USER')")
    public void deleteMyAccount(@RequestHeader("Authorization") String authHeader) {
        userService.deleteMyAccountById();
        jwtService.blackListToken(authHeader);
        try {
            UUID userId = jwtService.extractUserIdFromRefreshToken(authHeader.substring(7));
            refreshTokenService.deleteById(userId);
        } catch (Exception ignored) {}
    }
}