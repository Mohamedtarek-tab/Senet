package com.senet.auth.controller;

import com.senet.auth.dto.AuthRequest;
import com.senet.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.refresh(body.get("refreshToken")));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/register/employee")
    public ResponseEntity<?> registerEmployee(@RequestBody AuthRequest request) {
    try {
        return ResponseEntity.ok(authService.registerEmployee(request));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Since we are using stateless JWT, logout is handled client-side by deleting the token.
        // If we needed strict logout, we'd add the token to a blocklist here.
        return ResponseEntity.noContent().build();
    }
}
