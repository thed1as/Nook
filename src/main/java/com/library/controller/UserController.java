package com.library.controller;

import com.library.dto.listing.ListingResponse;
import com.library.dto.user.UserRequest;
import com.library.dto.user.UserResponse;
import com.library.service.ListingService;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "User", description = "User API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ListingService listingService;

    @Operation(summary = "Create user")
    @PostMapping("/users")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest userRequest) {
        UserResponse ur = userService.createUser(userRequest);
        return ResponseEntity.ok(ur);
    }

    @Operation(summary = "Find user by id")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        UserResponse ur = userService.getUserById(id);
        return ResponseEntity.ok(ur);
    }

//    @Operation(summary = "Find users listings by id")
    @GetMapping("/users/{id}/listings")
    public ResponseEntity<List<ListingResponse>> getUserListings(@PathVariable UUID id) {
        List<ListingResponse> lr = listingService.getUsersListings(id);
        return ResponseEntity.ok(lr);
    }
}
