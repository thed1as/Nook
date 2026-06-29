package com.nooki.mapper;

import com.nooki.dto.user.UserRequest;
import com.nooki.dto.user.UserResponse;
import com.nooki.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);

    @Mapping(target = "userId", ignore = true)
    User toUser(UserRequest userRequest);
}
