package com.nooki.service;

import com.nooki.dto.exception.customException.userException.UserNotFoundException;
import com.nooki.dto.user.UserResponse;
import com.nooki.entity.CustomUserDetails;
import com.nooki.entity.User;
import com.nooki.mapper.UserMapper;
import com.nooki.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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

    @Nested
    @DisplayName("Get user")
    class GetUser {
        @Test
        @DisplayName("valid request user found should return userResponse")
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
        @DisplayName("valid request should user not found should throw EntityNotFoundException")
        void getUserById_failRequest_ThrowEntityNotFoundException() {
            UUID userId = UUID.randomUUID();

            when(userRepository.findById(userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(userId))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("valid request received the user's id")
        void getCurrentUserId_UserAuthenticated_ReturnsEmail() {
            UUID userId = UUID.randomUUID();

            User user = new User();
            user.setUserId(userId);

            CustomUserDetails ud = new CustomUserDetails(user);

            Authentication authentication = mock(Authentication.class);
            SecurityContext securityContext = mock(SecurityContext.class);

            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(ud);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            SecurityContextHolder.setContext(securityContext);

            try {
                UUID result = userService.getCurrentUserId();
                assertEquals(userId, result);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }
    }

    @Nested
    @DisplayName("Delete user")
    class DeleteUser {
        @Test
        @DisplayName("valid request should delete user")
        void deleteUserById_validRequest_DeleteUser() {
            UUID userId = UUID.randomUUID();
            userService.deleteUserById(userId);
            verify(userRepository).deleteById(userId);
        }
    }
}
