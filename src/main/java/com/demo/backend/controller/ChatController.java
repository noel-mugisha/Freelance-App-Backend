package com.demo.backend.controller;

import com.demo.backend.model.Message;
import com.demo.backend.model.Task;
import com.demo.backend.model.User;
import com.demo.backend.repository.MessageRepository;
import com.demo.backend.repository.TaskRepository;
import com.demo.backend.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, MessageRepository messageRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public record ChatMessage(@NotBlank String senderUsername, @NotBlank String content) {}

    @MessageMapping("/tasks/{taskId}/sendMessage")
    public void sendMessage(@DestinationVariable Long taskId, ChatMessage msg) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return;
        User sender = userRepository.findByUsername(msg.senderUsername()).orElse(null);
        if (sender == null) return;
        Message m = Message.builder().task(task).sender(sender).content(msg.content()).build();
        messageRepository.save(m);
        messagingTemplate.convertAndSend("/topic/tasks/" + taskId + "/receiveMessage", Map.of(
                "taskId", taskId,
                "sender", sender.getUsername(),
                "content", msg.content()
        ));
    }
}
