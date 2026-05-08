package com.senet.auth.dto;

import java.util.UUID;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String role;
    private UUID userId;
    private String name;

    public AuthResponse(String accessToken, String refreshToken, String role, UUID userId, String name) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
        this.userId = userId;
        this.name = name;
    }

    // Getters
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public String getRole() { return role; }
    public UUID getUserId() { return userId; }
    public String getName() { return name; }
}
