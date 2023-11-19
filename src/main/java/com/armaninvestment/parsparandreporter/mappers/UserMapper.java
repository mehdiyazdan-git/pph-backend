package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.UserDto;
import com.armaninvestment.parsparandreporter.entities.User;


public interface UserMapper {
    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    User partialUpdate(UserDto userDto, User user);
}