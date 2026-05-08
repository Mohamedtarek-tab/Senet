package com.senet.auth.controller;

import com.senet.auth.model.User;
import com.senet.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile(@RequestHeader("X-User-Id") String userId) {
        return userRepository.findById(UUID.fromString(userId))
                .map(user -> {
                    Map<String, Object> resp = new java.util.LinkedHashMap<>();
                    resp.put("id",         user.getId());
                    resp.put("name",       user.getName());
                    resp.put("email",      user.getEmail());
                    resp.put("phone",      user.getPhone());
                    resp.put("nationalId", user.getNationalId());
                    resp.put("role",       user.getRole());
                    return ResponseEntity.ok(resp);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@RequestHeader("X-User-Id") String userId, @RequestBody User requestBody) {
        Optional<User> userOpt = userRepository.findById(UUID.fromString(userId));
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        User user = userOpt.get();
        
        // Validate email uniqueness if changing email
        if (requestBody.getEmail() != null && !requestBody.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(requestBody.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use");
            }
            user.setEmail(requestBody.getEmail());
        }
        
        if (requestBody.getName() != null) user.setName(requestBody.getName());
        if (requestBody.getPhone() != null) user.setPhone(requestBody.getPhone());
        if (requestBody.getNationalId() != null) user.setNationalId(requestBody.getNationalId());
        
        User saved = userRepository.save(user);
        Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("name",       saved.getName());
        resp.put("email",      saved.getEmail());
        resp.put("phone",      saved.getPhone());
        resp.put("nationalId", saved.getNationalId());
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Map<String, String> body) {
        Optional<User> userOpt = userRepository.findById(UUID.fromString(userId));
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        User user = userOpt.get();
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null)
            return ResponseEntity.badRequest().body("currentPassword and newPassword are required");
        if (!passwordEncoder.matches(currentPassword, user.getPassword()))
            return ResponseEntity.badRequest().body("Current password is incorrect");
        if (newPassword.length() < 6)
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",    u.getId());
            m.put("name",  u.getName());
            m.put("email", u.getEmail());
            m.put("phone", u.getPhone());
            m.put("role",  u.getRole());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminUpdateUser(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        User user = userOpt.get();
        if (body.containsKey("name"))  user.setName(body.get("name"));
        if (body.containsKey("email")) user.setEmail(body.get("email"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("role"))  user.setRole(body.get("role"));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable UUID id) {
        if (!userRepository.existsById(id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}