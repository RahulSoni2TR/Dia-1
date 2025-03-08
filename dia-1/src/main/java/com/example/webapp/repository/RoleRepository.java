package com.example.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webapp.models.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
 //   Role findByRoleName(String roleName);
    Optional<Role> findByRoleName(String roleName);
}
