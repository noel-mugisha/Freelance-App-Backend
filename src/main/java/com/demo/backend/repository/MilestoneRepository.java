package com.demo.backend.repository;

import com.demo.backend.model.Milestone;
import com.demo.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    @Query("SELECT m FROM Milestone m JOIN FETCH m.task t JOIN FETCH t.createdBy WHERE m.id = :id")
    Optional<Milestone> findByIdWithTaskAndCreator(@Param("id") Long id);
    
    @Query("SELECT m FROM Milestone m JOIN FETCH m.task t JOIN FETCH t.createdBy WHERE t = :task")
    List<Milestone> findByTaskWithCreator(@Param("task") Task task);
    
    @Query("SELECT m FROM Milestone m WHERE m.task = :task")
    List<Milestone> findByTask(@Param("task") Task task);
}
