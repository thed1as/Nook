package com.library.controller;

import com.library.dto.user.LoginRequest;
import com.library.dto.user.UserRequest;
import com.library.security.CustomUserDetailsService;
import com.library.security.JwtService;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest loginRequest) {
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(loginRequest.getEmail());
        if(!passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())) {
            throw new RuntimeException("Wrong password");
        }

        return jwtService.generateToken(loginRequest.getEmail());
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody UserRequest userRequest) {
        authService.createUser(userRequest);
        return jwtService.generateToken(userRequest.getEmail());
    }
}
