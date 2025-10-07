package com.example.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webapp.entity.VerificationConfig;

public interface VerificationConfigRepository extends JpaRepository<VerificationConfig, Long> {

}
