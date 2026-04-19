package com.library.controller;

import com.library.dto.user.UserResponse;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "User", description = "User API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "Find user by id")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        UserResponse ur = userService.getUserById(id);
        return ResponseEntity.ok(ur);
    }
}