package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.RoleDto;
import com.armaninvestment.parsparandreporter.entities.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapperImpl implements RoleMapper {
    public RoleMapperImpl() {
    }

    public Role toEntity(RoleDto roleDto) {
        if (roleDto == null) {
            return null;
        } else {
            Role role = new Role();
            role.setId(roleDto.getId());
            role.setName(roleDto.getName());
            return role;
        }
    }

    public RoleDto toDto(Role role) {
        if (role == null) {
            return null;
        } else {
            RoleDto roleDto = new RoleDto();
            roleDto.setId(role.getId());
            roleDto.setName(role.getName());
            return roleDto;
        }
    }

    public Role partialUpdate(RoleDto roleDto, Role role) {
        if (roleDto == null) {
            return null;
        } else {
            if (roleDto.getId() != null) {
                role.setId(roleDto.getId());
            }

            if (roleDto.getName() != null) {
                role.setName(roleDto.getName());
            }

            return role;
        }
    }
}
