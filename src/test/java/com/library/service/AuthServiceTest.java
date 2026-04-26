package com.library.service;


import com.library.dto.user.UserRequest;
import com.library.dto.user.UserResponse;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

//    Auth createUser test

    @Test
    void createUser_ValidRequest_ReturnsUserResponse() {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("username");
        userRequest.setEmail("test@gmail.com");
        userRequest.setPassword("password");

        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(UUID.randomUUID());
        userResponse.setUsername("username");

        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(userRequest.getPassword()))
                .thenReturn("encoded_pass");
        when(userMapper.toUserResponse(any())).thenReturn(userResponse);

        UserResponse result = authService.createUser(userRequest);
        assertThat(result).isNotNull();
        assertEquals(userResponse, result);

        verify(userRepository, times(1)).save(any());
    }

    @Test
    void createUser_EmailAlreadyRegistered_ThrowsEntityExistsException() {
        UserRequest uq = new UserRequest();
        uq.setEmail("test@gmail.com");
        when(userRepository.existsByEmail(uq.getEmail())).thenReturn(true);
        assertThatThrownBy(() -> authService.createUser(uq))
                .isInstanceOf(EntityExistsException.class)
                .hasMessage("User with email " + uq.getEmail() + " already exists");
        verify(userRepository, never()).save(any());
    }
}
