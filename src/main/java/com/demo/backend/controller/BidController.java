package com.demo.backend.controller;

import com.demo.backend.mappers.BidMapper;
import com.demo.backend.model.Bid;
import com.demo.backend.model.Task;
import com.demo.backend.model.User;
import com.demo.backend.dto.response.BidDto;
import com.demo.backend.repository.BidRepository;
import com.demo.backend.repository.TaskRepository;
import com.demo.backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class BidController {

    private final BidRepository bidRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;

    public BidController(BidRepository bidRepository, TaskRepository taskRepository, UserRepository userRepository, BidMapper bidMapper) {
        this.bidRepository = bidRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.bidMapper = bidMapper;
    }

    public record BidRequest(@Min(0) BigDecimal amount) {}

    @PostMapping("/api/tasks/{taskId}/bids")
    @PreAuthorize("hasRole('FREELANCER')")
    public ResponseEntity<?> placeBid(@AuthenticationPrincipal UserDetails principal, @PathVariable Long taskId, @Valid @RequestBody BidRequest req) {
        Task t = taskRepository.findById(taskId).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        User freelancer = userRepository.findByUsername(principal.getUsername()).orElse(null);
        if (freelancer == null) return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        if (bidRepository.findByTaskAndFreelancer(t, freelancer).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "You have already bid on this task"));
        }
        Bid bid = Bid.builder().task(t).freelancer(freelancer).amount(req.amount()).status("PENDING").build();
        var savedBid = bidRepository.save(bid);
        return new ResponseEntity<>(bidMapper.toDto(savedBid), HttpStatus.CREATED);
    }

    @GetMapping("/api/tasks/{taskId}/bids")
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> viewBids(@AuthenticationPrincipal UserDetails principal, @PathVariable Long taskId) {
        // First, verify the task exists and the user has access to it
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        }
        Task task = taskOpt.get();
        // Verify the current user is the task owner
        if (!task.getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        // Fetch bids with all required relationships
        List<Bid> bids = bidRepository.findByTask(task);
        // Convert to DTOs
        List<BidDto> bidDtos = bids.stream()
            .map(bidMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(bidDtos);
    }

    @PostMapping("/api/bids/{bidId}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    @Transactional
    public ResponseEntity<?> accept(@AuthenticationPrincipal UserDetails principal, @PathVariable Long bidId) {
        // First, verify the bid exists and load the task with its creator in a single query
        Optional<Bid> bidOpt = bidRepository.findByIdWithTaskAndCreator(bidId);
        if (bidOpt.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Bid not found");
            return ResponseEntity.status(404).body(response);
        }
        
        Bid bid = bidOpt.get();
        
        // Verify the current user is the task owner
        if (!bid.getTask().getCreatedBy().getUsername().equals(principal.getUsername())) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Forbidden");
            return ResponseEntity.status(403).body(response);
        }
        
        // Update the bid status
        bid.setStatus("ACCEPTED");
        // Save is not strictly necessary with @Transactional, but keeping it for clarity
        bidRepository.save(bid);
        
        // Reject all other bids for this task
        bidRepository.rejectOtherBids(bid.getTask().getId(), bidId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Bid accepted successfully");
        return ResponseEntity.ok(response);
    }
}
