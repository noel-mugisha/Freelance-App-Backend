package com.demo.backend.mappers;

import com.demo.backend.dto.response.MilestoneDto;
import com.demo.backend.model.Milestone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MilestoneMapper {
    @Mapping(target = "taskId", source = "task.id")
    MilestoneDto toDto(Milestone milestone);
}
