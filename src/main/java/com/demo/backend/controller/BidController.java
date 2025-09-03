package com.demo.backend.controller;

import com.demo.backend.model.Bid;
import com.demo.backend.model.Task;
import com.demo.backend.model.User;
import com.demo.backend.repository.BidRepository;
import com.demo.backend.repository.TaskRepository;
import com.demo.backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
public class BidController {

    private final BidRepository bidRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public BidController(BidRepository bidRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.bidRepository = bidRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
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
        bidRepository.save(bid);
        return ResponseEntity.ok(bid);
    }

    @GetMapping("/api/tasks/{taskId}/bids")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> viewBids(@AuthenticationPrincipal UserDetails principal, @PathVariable Long taskId) {
        Task t = taskRepository.findById(taskId).orElse(null);
        if (t == null) return ResponseEntity.status(404).body(Map.of("error", "Task not found"));
        if (!t.getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        List<Bid> bids = bidRepository.findByTask(t);
        return ResponseEntity.ok(bids);
    }

    @PostMapping("/api/bids/{bidId}/accept")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<?> accept(@AuthenticationPrincipal UserDetails principal, @PathVariable Long bidId) {
        Bid bid = bidRepository.findById(bidId).orElse(null);
        if (bid == null) return ResponseEntity.status(404).body(Map.of("error", "Bid not found"));
        if (!bid.getTask().getCreatedBy().getUsername().equals(principal.getUsername())) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
        }
        bid.setStatus("ACCEPTED");
        bidRepository.save(bid);
        return ResponseEntity.ok(Map.of("message", "Bid accepted"));
    }
}
