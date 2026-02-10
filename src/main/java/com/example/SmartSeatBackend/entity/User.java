package com.example.SmartSeatBackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users") // Maps to the Postgres 'users' table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses Postgres BIGSERIAL
    @Column(name = "userid")
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "mobile_number", nullable = false, length = 10)
    private String mobileNumber;

    @Column(nullable = false, unique = true, length = 100)
    private String mail;

    @Enumerated(EnumType.STRING) // Stores 'student' as string in DB
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String password;

    // Enum definition matches the DTO validation pattern
    public enum Role {
        university,
        college,
        student

    }

    // --- Getters and Setters ---

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}