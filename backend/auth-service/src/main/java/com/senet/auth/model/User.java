package com.senet.auth.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(ADMIN|EMPLOYEE|CLIENT)$", message = "Role must be ADMIN, EMPLOYEE, or CLIENT")
    private String role;

    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10–15 digits")
    private String phone;

    @Size(max = 20, message = "National ID must not exceed 20 characters")
    private String nationalId;

    public User(String email, String password, String role, String name) {
        this.email    = email;
        this.password = password;
        this.role     = role;
        this.name     = name;
    }
}