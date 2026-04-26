package com.library.service;

import com.library.dto.user.UserResponse;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Test
    void getUserById_validRequest_ReturnUserResponse() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        UserResponse userResponse = new UserResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(result, userResponse);

        verify(userRepository).findById(userId);
        verify(userMapper).toUserResponse(user);
    }

    @Test
    void getUserById_failRequest_ThrowEntityNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteUserById_validRequest_DeleteUserResponse() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUserById(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUserById_failRequest_ThrowEntityNotFoundException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUserById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Entity not exists");

        verify(userRepository, never()).deleteById(userId);
    }

    @Test
    void getUserByEmail_validRequest_ReturnUserResponse() {
        String email = "test@gmail.com";

        User user = new User();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(result, user);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void getUserByEmail_failRequest_ThrowEntityNotFoundException() {
        String email = "test@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail(email))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with email:");
    }

    @Test
    void getCurrentUserEmail_UserAuthenticated_ReturnsEmail() {
        String expectedEmail = "test@gmail.com";

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(expectedEmail);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        try {
            String result = userService.getCurrentUserEmail();
            assertEquals(expectedEmail, result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void getCurrentUserEmail_AnonymousUser_ReturnsNull() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);

        try {
            String result = userService.getCurrentUserEmail();

            assertNull(result);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
