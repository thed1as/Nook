package com.nooki.service;

import com.nooki.dto.exception.customException.userException.UserIllegalStateException;
import com.nooki.dto.exception.customException.userException.UserNotFoundException;
import com.nooki.dto.user.UserResponse;
import com.nooki.entity.CustomUserDetails;
import com.nooki.entity.User;
import com.nooki.mapper.UserMapper;
import com.nooki.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User findById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

//    Role ADMIN only later on security
    @Transactional
    public void deleteUserById(UUID userId) {
        userRepository.deleteById(userId);
        log.info("Successfully deleted user with ID: {}", userId);
    }

    @Transactional
    public void deleteMyAccountById() {
        UUID userId = getCurrentUserId();
        if(userRepository.findById(userId).isEmpty()) {
            log.warn("User with ID: {}", userId);
            throw new UserIllegalStateException("User with ID: " + userId + " not found");
        }
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Extracting context Authentication: {}", auth);
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            log.debug("Found auth principal type: {}", auth.getPrincipal().getClass());
            return userDetails.getUser().getUserId();
        }

        return null;
    }
}