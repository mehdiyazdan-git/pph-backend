package com.armaninvestment.parsparandreporter.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO for {@link com.armaninvestment.parsparandreporter.entities.User}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto implements Serializable {
    private Long id;
    @Size(max = 20)
    @NotBlank
    private String username;
    @Size(max = 50)
    @Email
    @NotBlank
    private String email;
    @Size(max = 120)
    @NotBlank
    private String password;
    private Set<RoleDto> roles = new HashSet<>();
}