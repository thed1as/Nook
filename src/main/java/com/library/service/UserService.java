package com.library.service;

import com.library.dto.listing.ListingResponse;
import com.library.dto.user.UserRequest;
import com.library.dto.user.UserResponse;
import com.library.entity.User;
import com.library.enums.Role;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
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
        user.setPassword(userRequest.getPassword());
        user.setEmail(userRequest.getEmail());
        user.setRole(Role.USER);

        return userMapper.toUserResponse(userRepository.save(user));
    }

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
    public User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with id:" + userId));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("User not found with email:" + email)
        );
    }
}
