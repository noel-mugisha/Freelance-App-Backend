package com.demo.backend.mappers;

import com.demo.backend.dto.response.TaskDto;
import com.demo.backend.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    @Mapping(target = "createdByClient", source = "createdBy.id")
    TaskDto toDto(Task task);
}
