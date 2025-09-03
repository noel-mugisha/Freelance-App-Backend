package com.demo.backend.repository;

import com.demo.backend.model.Milestone;
import com.demo.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByTask(Task task);
}
