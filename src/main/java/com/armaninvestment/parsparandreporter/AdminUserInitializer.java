package com.armaninvestment.parsparandreporter;


import com.armaninvestment.parsparandreporter.entities.Role;
import com.armaninvestment.parsparandreporter.entities.User;
import com.armaninvestment.parsparandreporter.enums.ERole;
import com.armaninvestment.parsparandreporter.repositories.RoleRepository;
import com.armaninvestment.parsparandreporter.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    @Autowired
    public AdminUserInitializer(UserRepository userRepository,
                                RoleRepository roleRepository,
                                PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @PostConstruct
    public void initAdminUserAndRoles() {
        initRoles();
        initAdminUser();
    }

    private void initRoles() {
        initRoleIfNotExist(ERole.ROLE_ADMIN);
        initRoleIfNotExist(ERole.ROLE_USER);
        initRoleIfNotExist(ERole.ROLE_MODERATOR);
    }

    private void initRoleIfNotExist(ERole eRole) {
        Optional<Role> roleByName = roleRepository.findRoleByName(eRole);
        if (roleByName.isEmpty()) {
            Role role = new Role(eRole);
            roleRepository.save(role);
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            Optional<Role> roleByName = roleRepository.findRoleByName(ERole.ROLE_ADMIN);

            User adminUser = new User(
                    "admin",
                    "yazdanparast.docker@gmail.com",
                    encoder.encode("admin"));
            roleByName.ifPresent(role -> adminUser.getRoles().add(role));

            userRepository.save(adminUser);
        }
    }
}

