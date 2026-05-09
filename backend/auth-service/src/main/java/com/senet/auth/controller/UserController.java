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
public ResponseEntity<?> updateProfile(
        @RequestHeader("X-User-Id") String userId,
        @RequestBody Map<String, String> body) {  // ← Map instead of User

    Optional<User> userOpt = userRepository.findById(UUID.fromString(userId));
    if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

    User user = userOpt.get();

    // Validate email uniqueness if changing email
    if (body.containsKey("email") && !body.get("email").equals(user.getEmail())) {
        if (userRepository.findByEmail(body.get("email")).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Email is already in use"));
        }
        user.setEmail(body.get("email"));
    }

    if (body.containsKey("name"))       user.setName(body.get("name"));
    if (body.containsKey("phone"))      user.setPhone(body.get("phone"));
    if (body.containsKey("nationalId")) user.setNationalId(body.get("nationalId"));

    User saved = userRepository.save(user);
    return ResponseEntity.ok(Map.of(
        "name",       saved.getName()       != null ? saved.getName()       : "",
        "email",      saved.getEmail()      != null ? saved.getEmail()      : "",
        "phone",      saved.getPhone()      != null ? saved.getPhone()      : "",
        "nationalId", saved.getNationalId() != null ? saved.getNationalId() : ""
    ));
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
public ResponseEntity<?> adminUpdateUser(
        @PathVariable UUID id,
        @RequestHeader("X-User-Id") String requesterId,
        @RequestBody Map<String, String> body) {

    // Prevent admin from changing their own role
    if (id.equals(UUID.fromString(requesterId)) && body.containsKey("role")) {
        return ResponseEntity.badRequest().body("You cannot change your own role");
    }

    if (body.containsKey("role")) {
        String newRole = body.get("role");
        if (!newRole.matches("^(ADMIN|EMPLOYEE|CLIENT)$")) {
            return ResponseEntity.badRequest().body("Invalid role. Must be ADMIN, EMPLOYEE, or CLIENT");
        }
    }

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
    public ResponseEntity<?> deleteUser(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String requesterId) {

        // Prevent admin from deleting themselves
        if (id.equals(UUID.fromString(requesterId))) {
            return ResponseEntity.badRequest().body("You cannot delete your own account");
        }

        if (!userRepository.existsById(id)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}