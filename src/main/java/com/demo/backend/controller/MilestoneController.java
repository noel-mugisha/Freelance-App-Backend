package com.demo.backend.controller;

import com.demo.backend.mappers.MilestoneMapper;
import com.demo.backend.model.Milestone;
import com.demo.backend.model.Task;
import com.demo.backend.repository.MilestoneRepository;
import com.demo.backend.repository.TaskRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Transactional
public class MilestoneController {

    private final MilestoneRepository milestoneRepository;
    private final TaskRepository taskRepository;
    private final MilestoneMapper milestoneMapper;

    public MilestoneController(MilestoneRepository milestoneRepository, TaskRepository taskRepository, MilestoneMapper milestoneMapper) {
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
        this.milestoneMapper = milestoneMapper;
    }

    public record CreateMilestoneRequest(@NotBlank String title) {}

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/api/tasks/{taskId}/milestones")
    public ResponseEntity<?> create(@AuthenticationPrincipal UserDetails principal, @PathVariable Long taskId, @Valid @RequestBody CreateMilestoneRequest req) {
        // Find task with creator loaded in a single query
        Optional<Task> taskOpt = taskRepository.findByIdWithCreator(taskId);
        if (taskOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Task not found");
            return ResponseEntity.status(404).body(response);
        }
        Task task = taskOpt.get();
        if (!task.getCreatedBy().getUsername().equals(principal.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Forbidden");
            return ResponseEntity.status(403).body(response);
        }
        Milestone milestone = Milestone.builder()
            .task(task)
            .title(req.title())
            .status("PENDING")
            .build();
        Milestone savedMilestone = milestoneRepository.save(milestone);
        return new ResponseEntity<>(milestoneMapper.toDto(savedMilestone), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/api/tasks/{taskId}/milestones")
    @Transactional(readOnly = true)
    public ResponseEntity<?> list(@AuthenticationPrincipal UserDetails principal, @PathVariable Long taskId) {
        // Find task with creator loaded in a single query
        Optional<Task> taskOpt = taskRepository.findByIdWithCreator(taskId);
        if (taskOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Task not found");
            return ResponseEntity.status(404).body(response);
        }
        Task task = taskOpt.get();
        if (!task.getCreatedBy().getUsername().equals(principal.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Forbidden");
            return ResponseEntity.status(403).body(response);
        }
        // Fetch milestones with task and creator already loaded
        List<Milestone> milestones = milestoneRepository.findByTaskWithCreator(task);
        return ResponseEntity.ok(milestones.stream().map(milestoneMapper::toDto).collect(Collectors.toList()));
    }

    public record UpdateMilestoneRequest(String status) {}

    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping("/api/milestones/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id, @RequestBody UpdateMilestoneRequest req) {
        // Find milestone with task and creator loaded in a single query
        Optional<Milestone> milestoneOpt = milestoneRepository.findByIdWithTaskAndCreator(id);
        if (milestoneOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Milestone not found");
            return ResponseEntity.status(404).body(response);
        }
        Milestone milestone = milestoneOpt.get();
        if (!milestone.getTask().getCreatedBy().getUsername().equals(principal.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Forbidden");
            return ResponseEntity.status(403).body(response);
        }
        if (req.status() != null) {
            milestone.setStatus(req.status());
        }
        Milestone updatedMilestone = milestoneRepository.save(milestone);
        return ResponseEntity.ok(milestoneMapper.toDto(updatedMilestone));
    }
}
