package com.library.controller;

import com.library.dto.user.UserRequest;
import com.library.dto.user.UserResponse;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User", description = "User API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Find user by id")
    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        UserResponse ur = userService.getUserById(id);
        return ResponseEntity.ok(ur);
    }

    @Operation(summary = "become a host")
    @PostMapping("/user/me/role")
    @PreAuthorize("hasRole('USER')")
    public void updateRoleToHost() {
        userService.makeHost();
    }
}