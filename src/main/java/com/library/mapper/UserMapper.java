package com.library.mapper;

import com.library.dto.user.UserRequest;
import com.library.dto.user.UserResponse;
import com.library.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    @Mapping(target = "userId", ignore = true)
    User toUser(UserRequest userRequest);
}
