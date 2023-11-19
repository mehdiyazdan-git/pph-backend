package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.RoleDto;
import com.armaninvestment.parsparandreporter.entities.Role;

public interface RoleMapper {
    Role toEntity(RoleDto roleDto);

    RoleDto toDto(Role role);

    Role partialUpdate(RoleDto roleDto, Role role);
}