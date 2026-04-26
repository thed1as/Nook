package com.library.service;

import com.library.dto.user.UserResponse;
import com.library.entity.User;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

//    public UserResponse updateUser(UpdateUserRequest userRequest) {}

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::toUserResponse)
                .orElseThrow(EntityNotFoundException::new);
    }

//    Role ADMIN only later on security
    @Transactional
    public void deleteUserById(UUID userId) {
        if(!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Entity not exists");
        }
        userRepository.deleteById(userId);
    }


//    Entity getter
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User not found with email:" + email)
        );
    }

    public String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("printing user info: " + auth.getAuthorities());

        if(auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return null;
    }
}
