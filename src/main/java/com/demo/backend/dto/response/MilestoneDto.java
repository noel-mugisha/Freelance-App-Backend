package com.demo.backend.dto.response;

import lombok.Data;

@Data
public class MilestoneDto {
    private Long id;
    private Long taskId;
    private String title;
    private String status;
}

