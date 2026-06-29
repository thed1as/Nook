package com.nooki.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nooki.config.RateLimitService;
import com.nooki.repository.UserRepository;
import com.nooki.security.CustomUserDetailsService;
import com.nooki.security.JwtFilter;
import com.nooki.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@AutoConfigureMockMvc
public abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    protected JwtFilter jwtFilter;

    @MockitoBean
    protected RateLimitService rateLimitService;
}
