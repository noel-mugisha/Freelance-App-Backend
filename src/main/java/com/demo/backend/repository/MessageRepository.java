package com.demo.backend.repository;

import com.demo.backend.model.Message;
import com.demo.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByTask(Task task);
}
