package com.demo.backend.controller;

import com.demo.backend.mappers.TaskMapper;
import com.demo.backend.model.Task;
import com.demo.backend.model.User;
import com.demo.backend.repository.TaskRepository;
import com.demo.backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails principal, @Valid @RequestBody CreateTaskRequest req) {
        User user = userRepository.findByUsername(principal.getUsername()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        Task t = Task.builder()
                .title(req.title())
                .description(req.description())
                .budget(req.budget())
                .status("OPEN")
                .createdBy(user)
                .build();
        var savedTask = taskRepository.save(t);
        return new ResponseEntity<>(taskMapper.toDto(savedTask), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        var tasks = taskRepository.findAll().stream().map(taskMapper::toDto).toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        return taskRepository.findById(id)
                .<ResponseEntity<?>>map(task -> ResponseEntity.ok(taskMapper.toDto(task)))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Task not found")));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id, @Valid @RequestBody UpdateTaskRequest req) {
        Task t = taskRepository.findById(id).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!t.getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        if (req.title() != null) t.setTitle(req.title());
        if (req.description() != null) t.setDescription(req.description());
        if (req.budget() != null) t.setBudget(req.budget());
        if (req.status() != null) t.setStatus(req.status());
        var updated = taskRepository.save(t);
        return ResponseEntity.ok(taskMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> delete(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        Task t = taskRepository.findById(id).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!t.getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        taskRepository.delete(t);
        return ResponseEntity.noContent().build();
    }

    public record CreateTaskRequest(@NotBlank String title, String description, @Min(0) BigDecimal budget) {
    }

    public record UpdateTaskRequest(String title, String description, BigDecimal budget, String status) {
    }
}
