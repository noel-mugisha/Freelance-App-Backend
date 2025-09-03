package com.demo.backend.controller;

import com.demo.backend.model.User;
import com.demo.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public record UpdateStatusRequest(String status) {}

    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest req) {
        User u = userRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        if (req.status() != null) u.setStatus(req.status());
        userRepository.save(u);
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }
}
