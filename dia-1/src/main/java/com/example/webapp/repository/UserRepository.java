package com.example.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webapp.models.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);

}
