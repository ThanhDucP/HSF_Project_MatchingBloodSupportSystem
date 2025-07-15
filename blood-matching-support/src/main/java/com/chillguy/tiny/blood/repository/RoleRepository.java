package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    boolean existsByRoleIs(String role);

    Role findByRole(String role);
}
