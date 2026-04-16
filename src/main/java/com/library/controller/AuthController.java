package com.library.controller;

import com.library.dto.user.LoginRequest;
import com.library.dto.user.UserRequest;
import com.library.security.JwtService;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        return jwtService.generateToken(loginRequest.getPassword());
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody UserRequest userRequest) {
        authService.createUser(userRequest);
        return jwtService.generateToken(userRequest.getEmail());
    }
}
