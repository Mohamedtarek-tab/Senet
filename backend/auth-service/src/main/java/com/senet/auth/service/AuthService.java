package com.senet.auth.service;

import com.senet.auth.dto.AuthRequest;
import com.senet.auth.dto.AuthResponse;
import com.senet.auth.model.User;
import com.senet.auth.repository.UserRepository;
import com.senet.auth.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdmin() {
        if (userRepository.findByEmail("admin@senet.com").isEmpty()) {
            userRepository.save(new User("admin@senet.com", passwordEncoder.encode("admin123"), "ADMIN", "Admin Hassan"));
        }
    }

    public AuthResponse login(AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            User user = userOpt.get();
            String accessToken = jwtUtil.generateToken(user, false);
            String refreshToken = jwtUtil.generateToken(user, true);
            return new AuthResponse(accessToken, refreshToken, user.getRole(), user.getId(), user.getName());
        }
        throw new RuntimeException("Invalid credentials");
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), "CLIENT", request.getName());
        userRepository.save(user);
        
        String accessToken = jwtUtil.generateToken(user, false);
        String refreshToken = jwtUtil.generateToken(user, true);
        return new AuthResponse(accessToken, refreshToken, user.getRole(), user.getId(), user.getName());
    }

    public AuthResponse refresh(String refreshToken) {
        if (jwtUtil.validateToken(refreshToken, "refresh")) {
            String userIdStr = jwtUtil.extractClaims(refreshToken).getSubject();
            User user = userRepository.findById(java.util.UUID.fromString(userIdStr))
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            String newAccessToken = jwtUtil.generateToken(user, false);
            String newRefreshToken = jwtUtil.generateToken(user, true);
            return new AuthResponse(newAccessToken, newRefreshToken, user.getRole(), user.getId(), user.getName());
        }
        throw new RuntimeException("Invalid refresh token");
    }
}