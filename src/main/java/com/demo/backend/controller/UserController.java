package com.demo.backend.controller;

import com.demo.backend.model.User;
import com.demo.backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public record UpdateProfileRequest(@Size(max = 150) String fullName, @Size(max = 5000) String bio) {}

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "fullName", user.getFullName(),
                "bio", user.getBio(),
                "status", user.getStatus()
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails principal, @Valid @RequestBody UpdateProfileRequest req) {
        if (principal == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        user.setFullName(req.fullName());
        user.setBio(req.bio());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }
}
