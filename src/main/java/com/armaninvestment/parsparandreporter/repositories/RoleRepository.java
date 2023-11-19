package com.armaninvestment.parsparandreporter.repositories;

import com.armaninvestment.parsparandreporter.entities.Role;
import com.armaninvestment.parsparandreporter.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    boolean existsByName(ERole name);

    Optional<Role> findRoleByName(ERole name);

    Optional<Role> findByName(ERole eRole);
}