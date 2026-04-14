package com.library.service;

import com.library.dto.user.UserRequest;
import com.library.dto.user.UserResponse;
import com.library.entity.User;
import com.library.enums.Role;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if(userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EntityExistsException("User with email "
                    + userRequest.getEmail()
                    + " already exists");
        }
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setEmail(userRequest.getEmail());
        user.setRole(Role.USER);

        return userMapper.toUserResponse(userRepository.save(user));
    }
}
