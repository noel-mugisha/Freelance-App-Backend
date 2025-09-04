package com.demo.backend.repository;

import com.demo.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.createdBy WHERE t.id = :id")
    Optional<Task> findByIdWithCreator(@Param("id") Long id);
}
