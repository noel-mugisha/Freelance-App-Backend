package com.demo.backend.dto.response;

import lombok.Data;

@Data
public class BidDto {
    private Long id;
    private String taskTitle;
    private String freelancerEmail;
    private String status;
}
