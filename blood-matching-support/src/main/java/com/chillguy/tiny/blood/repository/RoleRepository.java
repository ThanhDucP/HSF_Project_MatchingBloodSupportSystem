package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByRoleIs(String role);

    Optional<Role> findByRole(String role);
}
