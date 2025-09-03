package com.demo.backend.controller;

import com.demo.backend.model.Milestone;
import com.demo.backend.model.Task;
import com.demo.backend.repository.MilestoneRepository;
import com.demo.backend.repository.TaskRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class MilestoneController {

    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;

    public MilestoneController(MilestoneRepository milestoneRepository, TaskRepository taskRepository) {
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
    }

    public record CreateMilestoneRequest(@NotBlank String title) {}

    @PostMapping("/api/tasks/{taskId}/milestones")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails principal, @PathVariable Long taskId, @Valid @RequestBody CreateMilestoneRequest req) {
        Task t = taskRepository.findById(taskId).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!t.getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        Milestone m = Milestone.builder().task(t).title(req.title()).status("PENDING").build();
        milestoneRepository.save(m);
        return ResponseEntity.ok(m);
    }

    @GetMapping("/api/tasks/{taskId}/milestones")
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails principal, @PathVariable Long taskId) {
        Task t = taskRepository.findById(taskId).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!t.getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        List<Milestone> ms = milestoneRepository.findByTask(t);
        return ResponseEntity.ok(ms);
    }

    public record UpdateMilestoneRequest(String status) {}

    @PutMapping("/api/milestones/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id, @RequestBody UpdateMilestoneRequest req) {
        Milestone m = milestoneRepository.findById(id).orElse(null);
        if (m == null) return ResponseEntity.status(404).body(Map.of("error", "Milestone not found"));
        if (!m.getTask().getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        if (req.status() != null) m.setStatus(req.status());
        milestoneRepository.save(m);
        return ResponseEntity.ok(m);
    }
}
