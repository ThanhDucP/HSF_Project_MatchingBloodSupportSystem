package com.chillguy.tiny.blood.repository;

import com.chillguy.tiny.blood.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoleRepository extends JpaRepository<Role, String> {
    boolean existsByRoleIs(String role);

    Role findByRole(String role);
}
