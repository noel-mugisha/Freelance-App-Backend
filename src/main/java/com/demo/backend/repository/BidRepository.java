package com.demo.backend.repository;

import com.demo.backend.model.Bid;
import com.demo.backend.model.Task;
import com.demo.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByTask(Task task);
    Optional<Bid> findByTaskAndFreelancer(Task task, User freelancer);
}
