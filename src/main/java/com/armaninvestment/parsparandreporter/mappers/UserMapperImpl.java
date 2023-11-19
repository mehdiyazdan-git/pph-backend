package com.armaninvestment.parsparandreporter.mappers;

import com.armaninvestment.parsparandreporter.dtos.RoleDto;
import com.armaninvestment.parsparandreporter.dtos.UserDto;
import com.armaninvestment.parsparandreporter.entities.Role;
import com.armaninvestment.parsparandreporter.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Component
public class UserMapperImpl implements UserMapper {
    @Autowired
    private RoleMapper roleMapper;

    public UserMapperImpl() {
    }

    public User toEntity(UserDto userDto) {
        if (userDto == null) {
            return null;
        } else {
            User user = new User();
            user.setId(userDto.getId());
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPassword(userDto.getPassword());
            user.setRoles(this.roleDtoSetToRoleSet(userDto.getRoles()));
            return user;
        }
    }

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        } else {
            UserDto userDto = new UserDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            userDto.setEmail(user.getEmail());
            userDto.setPassword(user.getPassword());
            userDto.setRoles(this.roleSetToRoleDtoSet(user.getRoles()));
            return userDto;
        }
    }

    public User partialUpdate(UserDto userDto, User user) {
        if (userDto == null) {
            return null;
        } else {
            if (userDto.getId() != null) {
                user.setId(userDto.getId());
            }

            if (userDto.getUsername() != null) {
                user.setUsername(userDto.getUsername());
            }

            if (userDto.getEmail() != null) {
                user.setEmail(userDto.getEmail());
            }

            if (userDto.getPassword() != null) {
                user.setPassword(userDto.getPassword());
            }

            Set set;
            if (user.getRoles() != null) {
                set = this.roleDtoSetToRoleSet(userDto.getRoles());
                if (set != null) {
                    user.getRoles().clear();
                    user.getRoles().addAll(set);
                }
            } else {
                set = this.roleDtoSetToRoleSet(userDto.getRoles());
                if (set != null) {
                    user.setRoles(set);
                }
            }

            return user;
        }
    }

    protected Set<Role> roleDtoSetToRoleSet(Set<RoleDto> set) {
        if (set == null) {
            return null;
        } else {
            Set<Role> set1 = new HashSet(Math.max((int) ((float) set.size() / 0.75F) + 1, 16));
            Iterator var3 = set.iterator();

            while (var3.hasNext()) {
                RoleDto roleDto = (RoleDto) var3.next();
                set1.add(this.roleMapper.toEntity(roleDto));
            }

            return set1;
        }
    }

    protected Set<RoleDto> roleSetToRoleDtoSet(Set<Role> set) {
        if (set == null) {
            return null;
        } else {
            Set<RoleDto> set1 = new HashSet(Math.max((int) ((float) set.size() / 0.75F) + 1, 16));
            Iterator var3 = set.iterator();

            while (var3.hasNext()) {
                Role role = (Role) var3.next();
                set1.add(this.roleMapper.toDto(role));
            }

            return set1;
        }
    }
}
