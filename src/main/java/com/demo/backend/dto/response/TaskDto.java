package com.demo.backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private BigDecimal budget;
    private String status;
    private Long createdByClient;
}
