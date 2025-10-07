package com.example.webapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "verification_config")
public class VerificationConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_days", nullable = false)
    private int verificationDays = 1; // default to 1 day

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getVerificationDays() { return verificationDays; }
    public void setVerificationDays(int verificationDays) { this.verificationDays = verificationDays; }
}

