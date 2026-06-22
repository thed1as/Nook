package com.library.service;

import com.library.dto.exception.customException.userException.UserIllegalStateException;
import com.library.dto.exception.customException.userException.UserNotFoundException;
import com.library.dto.user.UserResponse;
import com.library.entity.CustomUserDetails;
import com.library.entity.User;
import com.library.enums.Role;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
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
        if(!userRepository.existsById(userId)) {
            log.warn("Attempt to delete non-existing user with ID: {}", userId);
            throw new UserNotFoundException("Entity not exists");
        }
        userRepository.deleteById(userId);

        log.info("Successfully deleted user with ID: {}", userId);
    }


//    Entity getter
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("User not found with email:" + email)
        );
    }

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        log.debug("Extracting context Authentication: {}", auth);
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            log.debug("Found auth principal type: {}", auth.getPrincipal().getClass());
            return userDetails.getUser().getUserId();
        }

        return null;
    }

    @Transactional
    public boolean makeHost() {
        User user = userRepository.findById(getCurrentUserId()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(user.getRole().equals(Role.HOST)) {
            throw new UserIllegalStateException("You're already host");
        }

        user.setRole(Role.HOST);
        userRepository.save(user);
        return true;
    }
}